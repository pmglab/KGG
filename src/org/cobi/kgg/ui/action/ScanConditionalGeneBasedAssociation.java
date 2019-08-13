/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.jet.stat.Descriptive;
import cern.jet.stat.Gamma;
import cern.jet.stat.Probability;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import jsc.independentsamples.MannWhitneyTest;
import jsc.tests.H1;
import net.sf.picard.liftover.LiftOver;
import org.cobi.kgg.business.GenotypeSetUtil;
import org.cobi.kgg.business.SetBasedTest;
import org.cobi.kgg.business.entity.Chromosome;
import org.cobi.kgg.business.entity.Constants;
import static org.cobi.kgg.business.entity.Constants.CHROM_NAMES;
import org.cobi.kgg.business.entity.CorrelationBasedByteLDSparseMatrix;
import org.cobi.kgg.business.entity.EnrichTissue;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.HaplotypeDataset;
import org.cobi.kgg.business.entity.PValueWeight;
import org.cobi.kgg.business.entity.PlinkDataset;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.SNPPosiComparator;
import org.cobi.kgg.business.entity.StatusGtySet;
import org.cobi.kgg.ui.ArrayListObjectArrayTableModel;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.DriverTissueTopComponent;
import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.file.Zipper;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.math.OutlierDetector;
import org.cobi.util.text.ObjectArrayDoubleComparator;
import org.cobi.util.text.Util;
import org.ejml.data.DenseMatrix64F;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.rosuda.REngine.Rserve.RConnection;
import umontreal.iro.lecuyer.probdist.ChiSquareDist;

@ActionID(
    category = "Gene",
    id = "org.cobi.kgg.ui.action.ScanConditionalGeneBasedAssociation"
)
@ActionRegistration(
    displayName = "#CTL_ScanConditionalGeneBasedAssociation"
)
@ActionReference(path = "Actions/Gene", position = 550, separatorAfter = 600)
@Messages("CTL_ScanConditionalGeneBasedAssociation=ConditionalAssociation")
public final class ScanConditionalGeneBasedAssociation implements ActionListener, Constants {

  private final static Logger LOG = Logger.getLogger(ScanConditionalGeneBasedAssociation.class.getName());
  private final static RequestProcessor RP = new RequestProcessor("Scan conditional gene", 1, true);
  private RequestProcessor.Task buildTask = null;

  private Genome genome;
  private Map<String, int[]> geneGenomeIndexes;

  private IntArrayList chromIDs;
  private int snpPIndex;
  private List<Object[]> geneTable;
  private List<Object[]> tissueTable;
  GeneBasedAssociation geneSet;
  private double minExp = 0.01;
  private double pValueCutoff = 0.05;
  private String expressionPath = null;

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO implement action body

  }
//Genome genome, Map<String, int[]> geneGenomeIndexes,  int snpPIndex

  public Genome getGenome() {
    return genome;
  }

  public void setExpressionPath(String expressionPath) {
    this.expressionPath = expressionPath;
  }

  public void setGenome(Genome genome) {
    this.genome = genome;
  }

  public int getSnpPIndex() {
    return snpPIndex;
  }

  public void setpValueCutoff(double pValueCutoff) {
    this.pValueCutoff = pValueCutoff;
  }

  public void setSnpPIndex(int snpPIndex) {
    this.snpPIndex = snpPIndex;
  }

  public void setMinExp(double minExp) {
    this.minExp = minExp;
  }

  public GeneBasedAssociation getGeneSet() {
    return geneSet;
  }

  public void setGeneSet(GeneBasedAssociation geneSet) {
    this.geneSet = geneSet;
  }

  public ScanConditionalGeneBasedAssociation(List<Object[]> geneTable) {
    this.geneTable = geneTable;
  }

  public void setTissueTable(List<Object[]> tissueTable) {
    this.tissueTable = tissueTable;
  }

  public Map<String, int[]> getGeneGenomeIndexes() {
    return geneGenomeIndexes;
  }

  public void setGeneGenomeIndexes(Map<String, int[]> geneGenomeIndexes) {
    this.geneGenomeIndexes = geneGenomeIndexes;
  }

  public void conditionScan(ArrayListObjectArrayTableModel listPValueTableModel) {
    //record the classification settings
    ScanConditionalGeneAssocSwingWorker worker = new ScanConditionalGeneAssocSwingWorker(listPValueTableModel);
    // buildTask = buildTask.create(buildingThread); //the task is not started yet
    buildTask = RP.create(worker); //the task is not started yet
    buildTask.schedule(0); //start the task
  }

  public void conditionScan(ArrayListObjectArrayTableModel listPValueTableModel, ArrayListObjectArrayTableModel driverTissueTableModel,
      DriverTissueTopComponent dtTopComponent) {
    //record the classification settings
    ScanConditionalGeneAssocSwingWorker worker = new ScanConditionalGeneAssocSwingWorker(listPValueTableModel, driverTissueTableModel, dtTopComponent);
    // buildTask = buildTask.create(buildingThread); //the task is not started yet
    buildTask = RP.create(worker); //the task is not started yet
    buildTask.schedule(0); //start the task
  }

  private boolean handleCancel() {
    if (null == buildTask) {
      return false;
    }
    return buildTask.cancel();
  }

  public void setChromIDs(IntArrayList chromIDs) {
    this.chromIDs = chromIDs;
  }

  class ScanConditionalGeneAssocSwingWorker extends SwingWorker<Void, String> {

    int runningThread = 0;
    boolean succeed = false;
    ProgressHandle ph = null;
    long time;
    ArrayListObjectArrayTableModel listPValueTableModel;
    ArrayListObjectArrayTableModel driverTissueTableModel;
    DriverTissueTopComponent dtTopComponent;

    public ScanConditionalGeneAssocSwingWorker(ArrayListObjectArrayTableModel listPValueTableModel) {
      ph = ProgressHandleFactory.createHandle("task thats shows progress", new Cancellable() {
        @Override
        public boolean cancel() {
          return handleCancel();
        }
      });
      this.listPValueTableModel = listPValueTableModel;
      time = System.nanoTime();
    }

    public ScanConditionalGeneAssocSwingWorker(ArrayListObjectArrayTableModel listPValueTableModel, ArrayListObjectArrayTableModel driverTissueTableModel,
        DriverTissueTopComponent dtTopComponent) {
      ph = ProgressHandleFactory.createHandle("task thats shows progress", new Cancellable() {
        @Override
        public boolean cancel() {
          return handleCancel();
        }
      });
      this.listPValueTableModel = listPValueTableModel;
      this.driverTissueTableModel = driverTissueTableModel;
      this.dtTopComponent = dtTopComponent;

      time = System.nanoTime();
    }

    private double calPolyM(double[] values) throws Exception {
      //i=5
      //String[] arrangeStr = new String[]{"0", "1.0.0.0", "2.0.0.0", "3.0.0.0", "4.0.0.0", "5.0.0.0", "0.1.0.0", "1.1.0.0", "2.1.0.0", "3.1.0.0", "4.1.0.0", "0.2.0.0", "1.2.0.0", "2.2.0.0", "3.2.0.0", "0.3.0.0", "1.3.0.0", "2.3.0.0", "0.4.0.0", "1.4.0.0", "0.5.0.0", "0.0.1.0", "1.0.1.0", "2.0.1.0", "3.0.1.0", "4.0.1.0", "0.1.1.0", "1.1.1.0", "2.1.1.0", "3.1.1.0", "0.2.1.0", "1.2.1.0", "2.2.1.0", "0.3.1.0", "1.3.1.0", "0.4.1.0", "0.0.2.0", "1.0.2.0", "2.0.2.0", "3.0.2.0", "0.1.2.0", "1.1.2.0", "2.1.2.0", "0.2.2.0", "1.2.2.0", "0.3.2.0", "0.0.3.0", "1.0.3.0", "2.0.3.0", "0.1.3.0", "1.1.3.0", "0.2.3.0", "0.0.4.0", "1.0.4.0", "0.1.4.0", "0.0.5.0", "0.0.0.1", "1.0.0.1", "2.0.0.1", "3.0.0.1", "4.0.0.1", "0.1.0.1", "1.1.0.1", "2.1.0.1", "3.1.0.1", "0.2.0.1", "1.2.0.1", "2.2.0.1", "0.3.0.1", "1.3.0.1", "0.4.0.1", "0.0.1.1", "1.0.1.1", "2.0.1.1", "3.0.1.1", "0.1.1.1", "1.1.1.1", "2.1.1.1", "0.2.1.1", "1.2.1.1", "0.3.1.1", "0.0.2.1", "1.0.2.1", "2.0.2.1", "0.1.2.1", "1.1.2.1", "0.2.2.1", "0.0.3.1", "1.0.3.1", "0.1.3.1", "0.0.4.1", "0.0.0.2", "1.0.0.2", "2.0.0.2", "3.0.0.2", "0.1.0.2", "1.1.0.2", "2.1.0.2", "0.2.0.2", "1.2.0.2", "0.3.0.2", "0.0.1.2", "1.0.1.2", "2.0.1.2", "0.1.1.2", "1.1.1.2", "0.2.1.2", "0.0.2.2", "1.0.2.2", "0.1.2.2", "0.0.3.2", "0.0.0.3", "1.0.0.3", "2.0.0.3", "0.1.0.3", "1.1.0.3", "0.2.0.3", "0.0.1.3", "1.0.1.3", "0.1.1.3", "0.0.2.3", "0.0.0.4", "1.0.0.4", "0.1.0.4", "0.0.1.4", "0.0.0.5"};
      //double[] coeffs = new double[]{0.954339903, -0.029291918, 0.000217193, -1.07E-05, 2.59E-08, 1.37E-11, -0.072308426, 0.006420594, 0.000162163, 1.34E-06, -1.63E-09, 0.012975809, -0.001282845, -4.38E-05, 2.69E-08, -0.000777472, 0.000103207, 8.06E-07, 3.78E-06, -3.25E-06, -2.10E-07, 0.097161244, -0.005510891, -0.000165918, -1.33E-06, 1.53E-09, -0.034170245, 0.002598851, 9.14E-05, -6.52E-08, 0.003214508, -0.000312167, -2.40E-06, -2.81E-05, 1.35E-05, 8.98E-07, 0.021188862, -0.001332393, -4.77E-05, 3.88E-08, -0.004136926, 0.000314518, 2.37E-06, 6.20E-05, -2.11E-05, -1.42E-06, 0.00170126, -0.000105456, -7.76E-07, -5.47E-05, 1.47E-05, 9.51E-07, 1.70E-05, -3.83E-06, -1.93E-07, -2.92E-08, 0.208576589, 0.005285882, 0.000116827, 2.22E-06, -1.77E-09, 0.003541547, -0.004148653, -0.000101705, -6.15E-09, -0.000398534, 0.000585217, 1.22E-06, -4.18E-05, -2.75E-05, -1.90E-05, -0.011233316, 0.004243996, 0.00010652, -8.06E-09, 0.001997873, -0.001194141, -2.33E-06, 0.000262665, 8.58E-05, 7.14E-05, -0.001603144, 0.000609336, 1.10E-06, -0.000402851, -8.92E-05, -9.96E-05, 0.000181716, 3.09E-05, 6.08E-05, -1.37E-05, -0.072077437, -0.002458087, -6.08E-05, -4.35E-08, 0.010251665, 0.000977591, 1.04E-06, -0.001112533, -6.40E-05, -5.37E-05, -0.010411307, -0.001010853, -1.00E-06, 0.002571971, 0.000132773, 0.000140187, -0.001461226, -6.88E-05, -0.00011753, 3.10E-05, 0.015350714, 0.000498635, 7.70E-07, -0.002501818, -6.16E-05, -1.36E-05, 0.002726645, 6.40E-05, -3.06E-06, 1.77E-05, -0.001664156, -2.25E-05, 6.23E-05, -7.63E-05, 4.47E-05};
      //i=4
      // String[] arrangeStr = new String[]{"0", "1.0.0.0", "2.0.0.0", "3.0.0.0", "4.0.0.0", "0.1.0.0", "1.1.0.0", "2.1.0.0", "3.1.0.0", "0.2.0.0", "1.2.0.0", "2.2.0.0", "0.3.0.0", "1.3.0.0", "0.4.0.0", "0.0.1.0", "1.0.1.0", "2.0.1.0", "3.0.1.0", "0.1.1.0", "1.1.1.0", "2.1.1.0", "0.2.1.0", "1.2.1.0", "0.3.1.0", "0.0.2.0", "1.0.2.0", "2.0.2.0", "0.1.2.0", "1.1.2.0", "0.2.2.0", "0.0.3.0", "1.0.3.0", "0.1.3.0", "0.0.4.0", "0.0.0.1", "1.0.0.1", "2.0.0.1", "3.0.0.1", "0.1.0.1", "1.1.0.1", "2.1.0.1", "0.2.0.1", "1.2.0.1", "0.3.0.1", "0.0.1.1", "1.0.1.1", "2.0.1.1", "0.1.1.1", "1.1.1.1", "0.2.1.1", "0.0.2.1", "1.0.2.1", "0.1.2.1", "0.0.3.1", "0.0.0.2", "1.0.0.2", "2.0.0.2", "0.1.0.2", "1.1.0.2", "0.2.0.2", "0.0.1.2", "1.0.1.2", "0.1.1.2", "0.0.2.2", "0.0.0.3", "1.0.0.3", "0.1.0.3", "0.0.1.3", "0.0.0.4"};
      //double[] coeffs = new double[]{1.052970468, -0.022269638, 0.000178037, -2.46E-06, -6.10E-10, -0.11400576, 0.004016079, -3.56E-05, 2.25E-07, 0.017354673, -0.00042131, -7.77E-07, -0.001046217, 1.29E-05, 2.26E-05, 0.132062835, -0.003662349, 3.87E-05, -2.23E-07, -0.038383951, 0.000862687, 1.71E-06, 0.003439289, -4.03E-05, -9.96E-05, 0.020948887, -0.000445816, -9.59E-07, -0.003742113, 4.20E-05, 0.000163844, 0.001349466, -1.46E-05, -0.00011942, 3.26E-05, 0.086551864, 0.004432755, -5.83E-05, 2.67E-07, 0.023737033, -0.001302383, 2.07E-07, -0.002595799, 7.30E-05, 9.84E-05, -0.027760214, 0.001325325, -7.38E-09, 0.005955149, -0.000151882, -0.000342007, -0.003353826, 7.91E-05, 0.000391861, -0.000148348, -0.023650424, -0.000774998, 1.39E-06, -0.000895142, 0.000115194, 0.000154468, 0.001255554, -0.000120181, -0.000378895, 0.000226872, 0.002452838, 5.25E-05, 7.17E-05, -0.000101553, -3.98E-05};
//i=3
      String[] arrangeStr = new String[]{"0", "1.0.0.0", "2.0.0.0", "3.0.0.0", "0.1.0.0", "1.1.0.0", "2.1.0.0", "0.2.0.0", "1.2.0.0", "0.3.0.0", "0.0.1.0", "1.0.1.0", "2.0.1.0", "0.1.1.0", "1.1.1.0", "0.2.1.0", "0.0.2.0", "1.0.2.0", "0.1.2.0", "0.0.3.0", "0.0.0.1", "1.0.0.1", "2.0.0.1", "0.1.0.1", "1.1.0.1", "0.2.0.1", "0.0.1.1", "1.0.1.1", "0.1.1.1", "0.0.2.1", "0.0.0.2", "1.0.0.2", "0.1.0.2", "0.0.1.2", "0.0.0.3"};
      double[] coeffs = new double[]{1.114698859, -0.01219816, 1.78E-05, -4.86E-07, -0.093004463, 0.001129028, -6.90E-06, 0.007923439, -2.48E-05, -0.000191147, 0.102958415, -0.00100167, 7.97E-06, -0.016732947, 4.53E-05, 0.00059516, 0.008771119, -2.10E-05, -0.000615448, 0.000211466, 0.033813558, 0.001314718, -8.58E-06, 0.014037403, -4.05E-05, -0.000473908, -0.014890609, 3.26E-05, 0.000968427, -0.000491002, -0.007470854, -8.82E-06, -0.000136627, 0.000120059, 0.00053491};
      //i=2        
//String[] arrangeStr = new String[]{"0", "1.0.0.0", "2.0.0.0", "0.1.0.0", "1.1.0.0", "0.2.0.0", "0.0.1.0", "1.0.1.0", "0.1.1.0", "0.0.2.0", "0.0.0.1", "1.0.0.1", "0.1.0.1", "0.0.1.1", "0.0.0.2"};
      // double[] coeffs = new double[]{1.144032308, -0.009615067, -1.21E-05, -0.05885926, 0.000874856, 0.002558434, 0.064997587, -0.000866828, -0.005483038, 0.002915841, 0.015925892, 0.001307818, 0.006275873, -0.006653352, -0.000974707};
      double sum = coeffs[0];
      double indiv = 1;
      int p;
      for (int i = 1; i < coeffs.length; i++) {
        indiv = coeffs[i];
        for (int j = 0; j < 4; j++) {
          p = arrangeStr[i].charAt(j * 2) - '0';
          if (p > 0) {
            indiv *= Math.pow(values[j], p);
          }
        }
        // System.out.println(indiv);
        sum += indiv;
      }
      if (sum < 0) {
        sum = 1;
      } else if (sum > 2) {
        sum = 1.5;
      }

      return sum;
    }

    void condtionalTestOnChrom(int currChromIndex, List<Object[]> geneTableTmp, boolean readTxt, boolean writeLDMatrix) throws Exception {
      Set<Integer> positionsSet = new HashSet<Integer>();
      Chromosome currChrom = genome.readChromosomefromDisk(currChromIndex);

      Map<Double, Integer> orderMap = new HashMap<Double, Integer>();
      DoubleArrayList rankList = new DoubleArrayList();
      Set<Integer> groupID = new HashSet<Integer>();
      double order;
      String ordeValue;
      //pickup positions of  SNPs
      for (int i = 0; i < geneTableTmp.size(); i++) {
        ordeValue = geneTableTmp.get(i)[6].toString();
        if (ordeValue.equals("-") || ordeValue.equals(".")) {
          continue;
        }
        order = Double.parseDouble(ordeValue);

        while (orderMap.containsKey(order)) {
          order += 0.000001;
        }
        rankList.add(order);
        orderMap.put(order, i);

        String geneSymb = geneTableTmp.get(i)[1].toString();
        int[] indexes1 = geneGenomeIndexes.get(geneSymb);
        if (indexes1 == null) {
          continue;
        }
        groupID.add(Integer.parseInt(geneTableTmp.get(i)[0].toString()));
        List<SNP> snps = currChrom.genes.get(indexes1[1]).snps;
        for (SNP snp : snps) {
          positionsSet.add(snp.physicalPosition);
        }
      }

      File CHAIN_FILE = null;
      LiftOver liftOverLDGenome2pValueFile = null;
      Zipper ziper = new Zipper();
      String pValueFileGenomeVersion = genome.getFinalBuildGenomeVersion();

      Genome ldGenome;
      if (genome.getLdSourceCode() == -2) {
        ldGenome = GlobalManager.currentProject.getGenomeByName(genome.getSameLDGenome());
      } else {
        ldGenome = genome;
      }
      String info;
      String ldFileGenomeVersion = ldGenome.getLdFileGenomeVersion();

      try {
        CorrelationBasedByteLDSparseMatrix ldRsMatrixes = null;
        if (readTxt) {
          info = "Reading LD information of Key SNPs";
          LOG.info(info);
          if (ldFileGenomeVersion != null && !pValueFileGenomeVersion.equals(ldFileGenomeVersion)) {
            CHAIN_FILE = new File(GlobalManager.RESOURCE_PATH + "liftOver/" + ldFileGenomeVersion + "ToH" + pValueFileGenomeVersion.substring(1) + ".over.chain.gz");
            if (!CHAIN_FILE.exists()) {
              if (!CHAIN_FILE.getParentFile().exists()) {
                CHAIN_FILE.getParentFile().mkdirs();
              }
              String url = "http://hgdownload.cse.ucsc.edu/goldenPath/" + ldFileGenomeVersion + "/liftOver/" + CHAIN_FILE.getName();
              //HttpClient4API.downloadAFile(url, CHAIN_FILE);
              HttpClient4API.simpleRetriever(url, CHAIN_FILE.getCanonicalPath(), GlobalManager.proxyBean);
              ziper.extractTarGz(CHAIN_FILE.getCanonicalPath(), CHAIN_FILE.getParent());
            }
            CHAIN_FILE = new File(GlobalManager.RESOURCE_PATH + "liftOver/" + ldFileGenomeVersion + "ToH" + pValueFileGenomeVersion.substring(1) + ".over.chain");
            liftOverLDGenome2pValueFile = new LiftOver(CHAIN_FILE);
          }

          if (positionsSet == null || positionsSet.isEmpty()) {
            return;
          }
          //ld source code
          //-2 others LD
          //0 genotype plink binary file
          //1 hapap ld
          //2 1kG haplomap
          //3 local LD calcualted by plink
          //4 1kG haplomap vcf format

          if (ldGenome.getLdSourceCode() == 0) {
            if (ldGenome.getPlinkSet() == null || !ldGenome.getPlinkSet().avaibleFiles()) {
              String infor = "No Plink binary file to account for LD for SNPs! ";
              LOG.info(infor);
              return;
            }
            ldRsMatrixes = calculateLocalLDRSquarebyGenotypesPositions(ldGenome.getPlinkSet(), positionsSet, CHROM_NAMES[currChromIndex], liftOverLDGenome2pValueFile);

          } else if (ldGenome.getLdSourceCode() == 4) {
            if (ldGenome.getChromLDFiles()[currChromIndex] != null) {
              File hapMapLDFile = new File(ldGenome.getChromLDFiles()[currChromIndex]);
              if (!hapMapLDFile.exists()) {
                String infor = "No vcf file to account for LD for SNPs on chromosome " + currChromIndex + "! ";
                LOG.info(infor);
                return;
              }
            }
            ldRsMatrixes = calculateLocalLDRSquarebyHaplotypeVCFByPositions(ldGenome.getChromLDFiles()[currChromIndex], CHROM_NAMES[currChromIndex], positionsSet, liftOverLDGenome2pValueFile);
          }
          if (writeLDMatrix) {
            genome.writeCrossGeneLDMatrixToDisk(currChrom, ldRsMatrixes);
          }
        } else {
          ldRsMatrixes = genome.readChromosomeCrossGeneLDfromDisk(currChrom.getId());
        }

        boolean ingoreSNPNoLD = true;
        float geneWeight = 1;
        List<SNP> allSnpList = new ArrayList<SNP>();
        List<SNP> allLastSnpList = new ArrayList<SNP>();
        List<SNP> tmpSnpList = new ArrayList<SNP>();
        double[] tmpResult = new double[2];
        double[] accuResult = new double[2];
        Arrays.fill(accuResult, 0);
        int snpPVTypeIndex = 0;
        double df = 0;
        double Y = 0;
        boolean usingSquareChi = false;
        double p1 = 0;
        DoubleArrayList pValueArray = new DoubleArrayList();
        IntArrayList keySNPPositions = new IntArrayList();
        DoubleArrayList blockpValueArray = new DoubleArrayList();
        RConnection rcon = null;
        if (usingSquareChi) {
          rcon = new RConnection();
          rcon.eval("pack=\"survey\"; if (!require(pack,character.only = TRUE)) { install.packages(pack,dep=TRUE,repos='http://cran.us.r-project.org');if(!require(pack,character.only = TRUE)) stop(\"Package not found\")}");
          rcon.eval("library(survey)");
        }

        double lastChisquare = 0;
        double currChisquare = 0;
        rankList.quickSort();
        rankList.reverse();

        double orgP;
        int rankSize = rankList.size();
        List<Integer> groupIDs = new ArrayList<Integer>(groupID);
        Collections.sort(groupIDs);
        int groupSize = groupIDs.size();
        String groupIDStr;
        int addedGeneNum = 0;
        double factor = 1.25, chi;
        double[] chis = new double[4];
        ScanGeneBasedAssociation sgba = new ScanGeneBasedAssociation(geneSet);

        for (int g = 0; g < groupSize; g++) {
          allSnpList.clear();
          allLastSnpList.clear();
          groupIDStr = String.valueOf(groupIDs.get(g));
          addedGeneNum = 0;
          Arrays.fill(accuResult, 0);
          for (int i = 0; i < rankSize; i++) {
            int index = orderMap.get(rankList.getQuick(i));

            if (!geneTableTmp.get(index)[0].toString().equals(groupIDStr)) {
              continue;
            }
            String geneSymb = geneTableTmp.get(index)[1].toString();

            int[] indexes1 = geneGenomeIndexes.get(geneSymb);
            if (indexes1 == null) {
              continue;
            }
            Gene gene = currChrom.genes.get(indexes1[1]);
            addedGeneNum++;
            allSnpList.addAll(gene.snps);
            Collections.sort(allSnpList, new SNPPosiComparator());
            if (usingSquareChi) {
              currChisquare = snpSetPValuebyJohnnyChiSquare(allSnpList, pValueArray, ldRsMatrixes, snpPVTypeIndex, false, rcon);
              if (currChisquare < 1E-0) {
                currChisquare = MultipleTestingMethod.iterativeChisquareInverse(2, currChisquare);
              } else {
                currChisquare = ChiSquareDist.inverseF(2, 1 - currChisquare);
              }
              p1 = Probability.chiSquareComplemented(1, (currChisquare - lastChisquare) <= 0 ? 0 : currChisquare - lastChisquare);
              lastChisquare = currChisquare;
            } else {
              boolean toPrint = false;
              // p1=snpSetPValuebyMyChiSquareApproxEJML(allSnpList, ldRsMatrixes, snpPVTypeIndex, toPrint, tmpResult);                           
              p1 = sgba.snpSetPValuebyMyScaledChisquareTestBlock(allSnpList, pValueArray, ldRsMatrixes, 0, keySNPPositions, blockpValueArray, null, tmpResult);
              chis[2] = MultipleTestingMethod.zScore(p1 / 2);
              chis[2] = chis[2] * chis[2];

              df = tmpResult[0] - accuResult[0];
              Y = tmpResult[1] - accuResult[1];

              p1 = Gamma.incompleteGammaComplement(accuResult[0] / 2, accuResult[1] / 2);
              chis[1] = MultipleTestingMethod.zScore(p1 / 2);
              chis[1] = chis[1] * chis[1];

              if (Y < 0) {
                Y = 0;
              }
              if (df < 1) {
                df = 1;
              }
              // System.out.println(allSnpList.size() + "\t" + gene.getSymbol());
              p1 = Gamma.incompleteGammaComplement(df / 2, Y / 2);
              chis[3] = MultipleTestingMethod.zScore(p1 / 2);
              chis[3] = chis[3] * chis[3];
              accuResult[0] = tmpResult[0];
              accuResult[1] = tmpResult[1];
            }
            tmpSnpList.clear();

            if (addedGeneNum > 1) {
              tmpSnpList.addAll(gene.snps);
              Collections.sort(tmpSnpList, new SNPPosiComparator());
              //orgP = snpSetPValuebyMyChiSquareApproxEJML(tmpSnpList, ldRsMatrixes, snpPVTypeIndex, false, tmpResult);
              orgP = sgba.snpSetPValuebyMyScaledChisquareTestBlock(allSnpList, pValueArray, ldRsMatrixes, 0, keySNPPositions, blockpValueArray, null, tmpResult);
              chis[0] = MultipleTestingMethod.zScore(orgP / 2);
              chis[0] = chis[0] * chis[0];

              if (p1 * 5 > orgP) {
                //if it is reasonable
                allLastSnpList.addAll(tmpSnpList);
                //because the conditional test has some inflation, we need an inflation factor to correct it
                factor = calPolyM(chis);
                //  System.out.println(factor);
                factor = 1.25;
                chi = chis[3] / factor;
                p1 = Gamma.incompleteGammaComplement(0.5, chi / 2);
                geneTableTmp.get(index)[8] = Util.formatPValue(p1);
              } else {
                geneTableTmp.get(index)[8] = "-";
              }
              allSnpList.clear();
              allSnpList.addAll(allLastSnpList);
            } else {
              allLastSnpList.addAll(allSnpList);
              geneTableTmp.get(index)[8] = Util.formatPValue(p1);
            }
          }
        }

        if (usingSquareChi) {
          rcon.close();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    @Override
    protected Void doInBackground() {
      try {

        long startTime = System.currentTimeMillis();

        String inforString = "Scan conditional gene-based association on the genome ...";

        StatusDisplayer.getDefault().setStatusText(inforString);

//pathwayBasedAssociation.getpValueSources();
        ph.start(); //we must start the PH before we swith to determinate
        ph.switchToIndeterminate();
        List<Object[]> geneTablebyConditionP = new ArrayList<Object[]>();
        geneTablebyConditionP.addAll(geneTable);
        List<String> sigGeneList = conditionAnaInGeneList(geneTablebyConditionP, 1);
        List<String> sigGeneList0 = new ArrayList<String>();
        sigGeneList0.addAll(sigGeneList);
        List<Object[]> geneTablebyConditionP0 = new ArrayList<Object[]>();
        for (int i = 0; i < geneTablebyConditionP.size(); i++) {
          Object[] objs = new Object[geneTablebyConditionP.get(i).length];
          System.arraycopy(geneTablebyConditionP.get(i), 0, objs, 0, objs.length);
          geneTablebyConditionP0.add(objs);
        }
        Map<String, double[]> tissueScores = new HashMap<String, double[]>();
        //if add 
        int[] methodIDs = new int[]{0, 1, 2, 3};
        List<EnrichTissue> results0 = null, results1 = null;
        List[] allResults = new List[methodIDs.length];
        DecimalFormat decimalFormat = new DecimalFormat("0.0E0");
        if (expressionPath != null && !sigGeneList.isEmpty()) {
          Set<String> allHGNCGenes = loadGeneSysmbols();

          for (int f = 0; f < methodIDs.length; f++) {
            List<double[]> spcValues = new ArrayList<double[]>();
            List<String> spcGenes = new ArrayList<String>();
            List<String> tissueList = new ArrayList<String>();
            produceRobustZScore(expressionPath, minExp, tissueList, spcGenes, spcValues, methodIDs[f]);
            geneTablebyConditionP.clear();
            for (int i = 0; i < geneTablebyConditionP0.size(); i++) {
              Object[] objs = new Object[geneTablebyConditionP0.get(i).length];
              System.arraycopy(geneTablebyConditionP0.get(i), 0, objs, 0, objs.length);
              geneTablebyConditionP.add(objs);
            }
            sigGeneList.clear();
            sigGeneList.addAll(sigGeneList0);

            results0 = extractMaxMedianTest(geneTablebyConditionP, allHGNCGenes, tissueList, spcGenes, spcValues, sigGeneList);
            int iter = 1;
            int tissueNum = 0;
            boolean notConverged = false;
            do {
              iter++;
//note some cells in geneTablebyConditionP wil be modified
              results1 = extractMaxMedianTest(geneTablebyConditionP, allHGNCGenes, tissueList, spcGenes, spcValues, sigGeneList);
//note some cells in geneTablebyConditionP wil be modified
              sigGeneList = conditionAnaInGeneList(geneTablebyConditionP, iter);
              tissueNum = results1.size();
              notConverged = false;
              for (int i = 0; i < tissueNum; i++) {
                if (results0.get(i).p != results1.get(i).p) {
                  notConverged = true;
                  break;
                }
              }
              if (!notConverged) {
                break;
              }
              results0.clear();
              results0.addAll(results1);
            } while (true);
            allResults[f] = results1;
            for (int ii = 0; ii < results1.size(); ii++) {
              double[] values = tissueScores.get(results1.get(ii).getName());
              if (values == null) {
                values = new double[methodIDs.length + 1];
                tissueScores.put(results1.get(ii).getName(), values);
              }
              values[f] = results1.get(ii).p;
            }

            LOG.info("Method " + f + " has been finished!");
          } //end of  for (int f = 0; f < methodIDs.length; f++) 

          int incNum = 0;
          for (Map.Entry<String, double[]> item : tissueScores.entrySet()) {
            double[] values = item.getValue();
            int len = values.length - 1;
            values[len] = 0;
            incNum = 0;
            for (int i = 0; i < len; i++) {
              if (values[i] == 0) {
                values[i] = 1E-17;
              }
              if (!Double.isNaN(values[i])) {
                values[len] += -Math.log10(values[i]);
                incNum++;
              }
            }
            //take the average
            values[len] = values[len] / incNum;
            // values[len] = 2 * values[len];
            // values[len] = Probability.chiSquareComplemented(len * 2, values[len]);

            String[] rows = new String[values.length + 1];
            rows[0] = item.getKey();
            for (int i = 0; i < len; i++) {
              rows[i + 1] = decimalFormat.format(values[i]);
            }
            rows[len + 1] = String.valueOf(values[len]);
            tissueTable.add(rows);
            // System.out.println(t.name + "\t" + t.sigGeneNum + "\t" + decimalFormat.format(t.p));
          }
          Collections.sort(tissueTable, new ObjectArrayDoubleComparator(methodIDs.length + 1));

          LOG.info("Updating conditional p-values!");
          updateRankofGenes(methodIDs.length, tissueScores, geneTablebyConditionP, allHGNCGenes);
          // tissueTable.clear();
          //redo the analysis with the combined score
          conditionAnaInGeneList(geneTablebyConditionP, 2);
          geneTable.clear();
          geneTable.addAll(geneTablebyConditionP);
        }

        //GlobalManager.mainView.displayPathwayTree(pathwayTreeRoot);
        // InterfaceUtil.saveTreeNode2XMLFile(pathwayTreeRoot, storagePath);
        System.gc();

        succeed = true;
      } catch (Exception ex) {
        StatusDisplayer.getDefault().setStatusText("Scan driver tissue task was CANCELLED!");
        java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }
      return null;
    }

    private List<String> conditionAnaInGeneList(List<Object[]> geneTablebyConditionP, int iterID) throws Exception {
      List<String> sigGeneList = new ArrayList<String>();
      List<Object[]> geneTableTmp = new ArrayList<Object[]>();
      int size = geneTablebyConditionP.size();
      if (size < 1) {
        sigGeneList.add((String) geneTablebyConditionP.get(0)[1]);
        return sigGeneList;
      }
      int chrSize = chromIDs.size();

      for (int t = 0; t < chrSize; t++) {
        int currChromIndex = chromIDs.get(t);
        String chrC = CHROM_NAMES[currChromIndex];
        geneTableTmp.clear();
        for (int i = 0; i < size; i++) {
          String chr = geneTablebyConditionP.get(i)[2].toString();
          boolean sel = (Boolean) geneTablebyConditionP.get(i)[7];
          if (!sel) {
            continue;
          }
          if (chrC.equals(chr)) {
            geneTableTmp.add(geneTablebyConditionP.get(i));
          }
        }

        if (geneTableTmp.isEmpty()) {
          continue;
        }

        condtionalTestOnChrom(currChromIndex, geneTableTmp, iterID == 1, expressionPath != null);
        for (Object[] items : geneTableTmp) {
          String pv = items[8].toString();
          if (pv.endsWith("-") || pv.endsWith(".")) {
            continue;
          }
          if (Double.parseDouble(pv) <= pValueCutoff) {
            sigGeneList.add((String) items[1]);
          }
        }

        System.gc();
        publish("Iteration " + iterID + ", Chromosome " + CHROM_NAMES[currChromIndex] + " is processed!");
      }
      return sigGeneList;
    }

    private void produceRobustZScore(String fileName, double minCut, List<String> tissueNames, List<String> spcGenes, List<double[]> spcValues, int altMethodID) throws Exception {
      List<String> geneGenes = new ArrayList<String>();

      double[][] expressionValues = readDataTranscriptRowC(fileName, tissueNames, geneGenes);
      int tissueSizeOrg = tissueNames.size();
      int minTusseNum = tissueSizeOrg * 2 / 10;
      minTusseNum = 1;

      geneGenes.clear();
      String[] tissueSENames = new String[tissueNames.size()];

      for (int j = 0; j < tissueSizeOrg; j++) {
        tissueSENames[j] = tissueNames.get(j) + ".SE";
      }
      double[][] expressionSE = readDataTranscriptRowC(fileName, tissueSENames, geneGenes);

      double[][] x;
      double[] y;
      int geneSize = geneGenes.size();

      OutlierDetector scl = new OutlierDetector();
      double p;

      double sampleMean, sampleSD;

      boolean toPrint = false;
      double cutoff = -Probability.normalInverse(1E-6);

      double naNum = 0;
      int ingnoredNum = 0;
      int nonZeroNum = 0;
      DoubleArrayList subArray = new DoubleArrayList();
      double[] seValues = new double[tissueSizeOrg];
      int[] sigGenes = new int[tissueSizeOrg];
      Arrays.fill(sigGenes, 0);
      double sumZ2, mean, median, sd, MAD;
      double[] pVL;
      for (int i = 0; i < geneSize; i++) {
        naNum = 0;
        nonZeroNum = 0;

        for (int k = 0; k < expressionValues[i].length; k++) {
          if (Double.isNaN(expressionValues[i][k])) {
            naNum++;
            continue;
          }
          if (expressionValues[i][k] >= minCut) {
            nonZeroNum++;
          }

        }

        //ignore transcript with low epxression ain all tissues;
        if (naNum == expressionValues[i].length) {
          ingnoredNum++;
          continue;
        }
        //if (zeroNum == expressionValues[i].length)
        if (nonZeroNum < 10) {
          ingnoredNum++;
          continue;
        }

        Arrays.fill(seValues, Double.NaN);
        for (int k = 0; k < expressionValues[i].length; k++) {
          if (!Double.isNaN(expressionValues[i][k])) {
            subArray.add(expressionValues[i][k]);
          }

          if (Double.isNaN(expressionSE[i][k]) || expressionSE[i][k] == 0) {
            int sss = 0;
            continue;
          }

          seValues[k] = 1 / expressionSE[i][k];
          // seValues[k] = 1 / Math.sqrt(expressionSE[i][k]);
          // seValues[k] = expressionSE[i][k];
          // seValues[k] = 1;
        }
        // System.out.println(Descriptive.mean(subArray) + "\t" + Descriptive.sampleVariance(subArray, Descriptive.mean(subArray)));
        subArray.clear();
        // Normality norm = new Normality(data[i]);
        //p = norm.shapiroWilkPvalue();
        // System.out.println(p);
        // System.out.println(geneGenes.get(i));
        //scl.iterativeWeighter(data[i], weights, residues, 100);
        if (altMethodID == 0) {
          pVL = scl.robustZScore(expressionValues[i], seValues, false);
        } else {
          ///code for paper writing 
          sumZ2 = 0;
          subArray.clear();
          for (int k = 0; k < expressionValues[i].length; k++) {
            if (!Double.isNaN(expressionValues[i][k])) {
              subArray.add(expressionValues[i][k]);
              sumZ2 += expressionValues[i][k] * expressionValues[i][k];
            }
          }
          // System.out.println(Descriptive.mean(subArray) + "\t" + Descriptive.sampleVariance(subArray, Descriptive.mean(subArray)));
          mean = Descriptive.mean(subArray);
          median = Descriptive.median(subArray);
          sd = Descriptive.sampleVariance(subArray, Descriptive.mean(subArray));
          sd = Math.sqrt(sd);
          subArray.clear();
          for (int k = 0; k < expressionValues[i].length; k++) {
            if (!Double.isNaN(expressionValues[i][k])) {
              if (expressionValues[i][k] - median != 0) {
                subArray.add(Math.abs(expressionValues[i][k] - median));
              }
            }
          }
          MAD = 1.483 * Descriptive.median(subArray);
          pVL = new double[expressionValues[i].length];
          Arrays.fill(pVL, Double.NaN);
          for (int k = 0; k < expressionValues[i].length; k++) {
            if (!Double.isNaN(expressionValues[i][k])) {
              switch (altMethodID) {
                case 1:
                  pVL[k] = (expressionValues[i][k] - mean) / sd;
                  // pVL[k] = (expressionValues[i][k]) ;
                  break;
                case 2:
                  pVL[k] = (expressionValues[i][k] - median) / MAD;
                  break;
                case 3:
                  pVL[k] = (expressionValues[i][k] * expressionValues[i][k]) / sumZ2;
                  break;
                default:
                  break;
              }
            }
          }
        }

        if (pVL == null) {
          continue;
        }
        spcGenes.add(geneGenes.get(i));
        spcValues.add(pVL);

        //System.out.println(linReg.coef[i] + " " + linReg.coefSD[i]);
      }

    }

    public double[][] readDataTranscriptRowC(String fileName, String[] tissueName, List<String> geneGenes) throws Exception {
      BufferedReader br = LocalFileFunc.getBufferedReader(fileName);
      String line = null;

      int[] tissueColIndexes = new int[tissueName.length];
      Arrays.fill(tissueColIndexes, -1);
      //skeep the head
      line = br.readLine();
      String[] cells = line.split("\t");
      for (int j = 0; j < tissueColIndexes.length; j++) {
        for (int i = 1; i < cells.length; i++) {
          if (tissueName[j].equals(cells[i])) {
            tissueColIndexes[j] = i;
            break;
          }
        }
      }
      List<String[]> dataStr = new ArrayList<String[]>();

      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.trim().length() == 0) {
          continue;
        }
        cells = line.split("\t");
        geneGenes.add(cells[0]);
        dataStr.add(cells);
      }
      br.close();
      int sizeT = tissueName.length;
      int sizeG = geneGenes.size();
      double[][] data = new double[sizeG][sizeT];

      for (int i = 0; i < sizeG; i++) {
        for (int j = 0; j < sizeT; j++) {
          if (dataStr.get(i)[tissueColIndexes[j]] == null) {
            data[i][j] = Double.NaN;
            continue;
          } else if (dataStr.get(i)[tissueColIndexes[j]].equals("NA")) {
            data[i][j] = Double.NaN;
            continue;
          }
          data[i][j] = Double.parseDouble(dataStr.get(i)[tissueColIndexes[j]]);
          if (Double.isNaN(data[i][j])) {
            //int sss=0;
          }
        }
      }
      return data;
    }

    public double[][] readDataTranscriptRowC(String fileName, List<String> tissueNames, List<String> geneGenes) throws Exception {
      BufferedReader br = LocalFileFunc.getBufferedReader(fileName);
      String line = null;
      line = br.readLine();
      String[] cells = line.split("\t");
      int tissueNum = (cells.length - 1) / 2 + 1;
      IntArrayList effectiveIndexes = new IntArrayList();
      for (int i = 1; i < cells.length; i++) {
        if (!cells[i].endsWith("SE")) {
          tissueNames.add(cells[i]);
          effectiveIndexes.add(i);
        }
      }
      tissueNum--;
      List<String[]> dataStr = new ArrayList<String[]>();

      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.trim().length() == 0) {
          continue;
        }
        cells = line.split("\t");
        geneGenes.add(cells[0]);
        dataStr.add(cells);
      }
      br.close();

      int sizeG = geneGenes.size();
      double[][] data = new double[sizeG][tissueNum];

      for (int i = 0; i < sizeG; i++) {
        for (int j = 0; j < tissueNum; j++) {
          if (dataStr.get(i)[effectiveIndexes.getQuick(j)] == null) {
            data[i][j] = Double.NaN;
            continue;
          } else if (dataStr.get(i)[effectiveIndexes.getQuick(j)].equals("NA")) {
            data[i][j] = Double.NaN;
            continue;
          }
          data[i][j] = Double.parseDouble(dataStr.get(i)[effectiveIndexes.getQuick(j)]);
          if (Double.isNaN(data[i][j])) {
            //int sss=0;
          }
        }

      }
      return data;
    }

    public List<EnrichTissue> extractMaxMedianTest(List<Object[]> geneTablebyConditionP, Set<String> allHGNCGenes, List<String> tissueNames,
        List<String> spcGenes, List<double[]> spcValues, List<String> testedGenes) throws Exception {

      List<EnrichTissue> results = new ArrayList<EnrichTissue>();
      Map<String, IntArrayList> geneExp = new HashMap<String, IntArrayList>();
      List<double[]> valueList = new ArrayList<double[]>();

      int index0 = 0;
      int geneIndex = 0;
      String geneSymb;
      int index;
      int geneNum = spcGenes.size();
      Map<String, double[]> geneScoreMap = new HashMap<String, double[]>();

      for (int g = 0; g < geneNum; g++) {
        double[] values = spcValues.get(g);
        valueList.add(values);
        geneSymb = spcGenes.get(g);
        index = geneSymb.indexOf(':');

        // a gene may have multiple transcripts
        if (index > 0) {
          geneSymb = geneSymb.substring(0, index);
        }
        IntArrayList indexes = geneExp.get(geneSymb);
        if (indexes == null) {
          indexes = new IntArrayList();
          geneExp.put(geneSymb, indexes);
        }
        indexes.add(index0);
        index0++;
        //System.out.println(cells[180] + "\t" + cells[180 + 52]);
      }

      int observedSig = 0;
      int totalSig = 0;
      double maxV;
      double minV;
      int validSelGeneNum = 0;
      int testNum = valueList.get(0).length;
      double p;
      double fwr = 0.05;

      double bonfP = 0.05 / 26513;
      double cutoff;
      bonfP = 1E-6;

      List<String> allGenes = new ArrayList<String>(geneExp.keySet());
      int validAllGeneNum = 0;
      boolean toPrint = false;

      int effectNum = 0;

      boolean useMax = true;
      Set<String> testedGeneSet = new HashSet<String>(testedGenes);
      DoubleArrayList allExpressionValues = new DoubleArrayList();
      DoubleArrayList nonSampleExpressionValues = new DoubleArrayList();
      DoubleArrayList sampleExpressionValues = new DoubleArrayList();

      for (int colIndex = 0; colIndex < testNum; colIndex++) {
        //oly tissue specific
        observedSig = 0;
        toPrint = false;

        validAllGeneNum = 0;
        nonSampleExpressionValues.clear();
        allExpressionValues.clear();
        for (String geneS : allGenes) {
          if (!allHGNCGenes.contains(geneS)) {
            continue;
          }
          IntArrayList indexes = geneExp.get(geneS);
          if (indexes == null) {
            continue;
          }

          maxV = -Double.MAX_VALUE;
          minV = Double.MAX_VALUE;
          effectNum = 0;

          for (int i = 0; i < indexes.size(); i++) {
            if (Double.isNaN(valueList.get(indexes.getQuick(i))[colIndex])) {
              continue;
            }

            if (maxV < valueList.get(indexes.getQuick(i))[colIndex]) {
              maxV = valueList.get(indexes.getQuick(i))[colIndex];
            }
            if (minV > valueList.get(indexes.getQuick(i))[colIndex]) {
              minV = valueList.get(indexes.getQuick(i))[colIndex];
            }
            effectNum++;
          }

          if (effectNum == 0) {
            continue;
          }
          validAllGeneNum++;
          if (!testedGeneSet.contains(geneS)) {
            if (useMax) {
              nonSampleExpressionValues.add(maxV);
            } else {
              nonSampleExpressionValues.add(minV);
            }
          }
          if (useMax) {
            allExpressionValues.add(maxV);
          } else {
            allExpressionValues.add(minV);
          }
        }
        bonfP = fwr / validAllGeneNum;

        validSelGeneNum = 0;
        sampleExpressionValues.clear();
        for (String geneS : testedGenes) {
          if (!allHGNCGenes.contains(geneS)) {
            continue;
          }
          IntArrayList indexes = geneExp.get(geneS);
          if (indexes == null) {

            continue;
          }

          maxV = -Double.MAX_VALUE;
          minV = Double.MAX_VALUE;
          effectNum = 0;
          for (int i = 0; i < indexes.size(); i++) {
            if (Double.isNaN(valueList.get(indexes.getQuick(i))[colIndex])) {
              continue;
            }
            if (maxV < valueList.get(indexes.getQuick(i))[colIndex]) {
              maxV = valueList.get(indexes.getQuick(i))[colIndex];
            }
            if (minV > valueList.get(indexes.getQuick(i))[colIndex]) {
              minV = valueList.get(indexes.getQuick(i))[colIndex];
            }
            effectNum++;
          }
          if (Double.isNaN(maxV)) {
            continue;
          }
          if (effectNum == 0) {
            continue;
          }
          //validSelGeneNum * 
          cutoff = -Probability.normalInverse(bonfP / (effectNum));

          if (maxV > cutoff) {
            observedSig++;
            if (toPrint) {
              // System.out.println(geneS);
            }
          }
          validSelGeneNum++;
          if (useMax) {
            sampleExpressionValues.add(maxV);
          } else {
            sampleExpressionValues.add(minV);
          }
        }

        validAllGeneNum = 0;
        totalSig = 0;

        for (String geneS : allGenes) {
          if (!allHGNCGenes.contains(geneS)) {
            continue;
          }
          IntArrayList indexes = geneExp.get(geneS);
          if (indexes == null) {
            continue;
          }

          maxV = -Double.MAX_VALUE;
          minV = Double.MAX_VALUE;
          effectNum = 0;
          for (int i = 0; i < indexes.size(); i++) {
            if (Double.isNaN(valueList.get(indexes.getQuick(i))[colIndex])) {
              continue;
            }

            if (maxV < valueList.get(indexes.getQuick(i))[colIndex]) {
              maxV = valueList.get(indexes.getQuick(i))[colIndex];
            }
            if (minV > valueList.get(indexes.getQuick(i))[colIndex]) {
              minV = valueList.get(indexes.getQuick(i))[colIndex];
            }
            effectNum++;
          }

          if (Double.isNaN(maxV)) {
            continue;
          }
          if (effectNum == 0) {
            continue;
          }

          cutoff = -Probability.normalInverse(bonfP / (effectNum));

          if (maxV > cutoff) {
            totalSig++;

          }
          validAllGeneNum++;
        }

        if (totalSig == 0) {
          // continue;

        }

        //p = MultipleTestingMethod.hypergeometricEnrichmentTest(validAllGeneNum, totalSig, validSelGeneNum, observedSig);
        double[] pValuesOutPathway = new double[nonSampleExpressionValues.size()];
        for (int t = 0; t < pValuesOutPathway.length; t++) {
          pValuesOutPathway[t] = nonSampleExpressionValues.getQuick(t);
        }
        double[] pValuesInPathway = new double[sampleExpressionValues.size()];
        for (int t = 0; t < pValuesInPathway.length; t++) {
          pValuesInPathway[t] = sampleExpressionValues.getQuick(t);
        }
        if (pValuesInPathway.length < pValuesOutPathway.length) {
          //   MannWhitneyTest mt = new MannWhitneyTest(pValuesInPathway, pValuesOutPathway, H1.LESS_THAN);
          MannWhitneyTest mt = new MannWhitneyTest(pValuesInPathway, pValuesOutPathway, H1.GREATER_THAN);
          // WilcoxonTest wt = new WilcoxonTest(pValuesInPathway, median, H1.LESS_THAN);           
          p = mt.getSP();
        } else {
          p = (Double.NaN);
        }
        //System.out.println(tissueNames[colIndex + 1] + "\t" + validAllGeneNum + "\t" + totalSig + "\t" + validSelGeneNum + "\t" + observedSig);
        //System.out.println(tissueNames[valueIndexes[colIndex]] + "\t" + observedSig + "\t" + String.format("%g", p) + "\t" + largeDiffNum + "\t" + allGenes.size());
        // System.out.println(tissueNames[valueIndexes[colIndex]] + "\t" + observedSig + "\t" + String.format("%g", p));
        results.add(new EnrichTissue(tissueNames.get(colIndex), colIndex, validSelGeneNum, observedSig, p));

        allExpressionValues.quickSort();
        //calculate gene's tissue specific score 
        double rankID;
        for (String geneS : allGenes) {
          if (!allHGNCGenes.contains(geneS)) {
            continue;
          }
          IntArrayList indexes = geneExp.get(geneS);
          if (indexes == null) {
            continue;
          }

          maxV = -Double.MAX_VALUE;
          minV = Double.MAX_VALUE;
          effectNum = 0;
          for (int i = 0; i < indexes.size(); i++) {
            if (Double.isNaN(valueList.get(indexes.getQuick(i))[colIndex])) {
              continue;
            }

            if (maxV < valueList.get(indexes.getQuick(i))[colIndex]) {
              maxV = valueList.get(indexes.getQuick(i))[colIndex];
            }
            if (minV > valueList.get(indexes.getQuick(i))[colIndex]) {
              minV = valueList.get(indexes.getQuick(i))[colIndex];
            }
            effectNum++;
          }
          if (Double.isNaN(maxV)) {
            continue;
          }
          if (effectNum == 0) {
            continue;
          }

          cutoff = -Probability.normalInverse(bonfP / (effectNum));

          rankID = allExpressionValues.binarySearch(maxV);
          if (rankID < 0) {
            rankID = -rankID - 1;
          }
          //The new approach 
          rankID = rankID / allExpressionValues.size();
          double[] scores = geneScoreMap.get(geneS);
          if (scores == null) {
            scores = new double[]{0};
            geneScoreMap.put(geneS, scores);
          }
          if (p == 0) {
            scores[0] += (17 * rankID);
          } else {
            scores[0] += (-Math.log10(p) * rankID);
          }
          /*
          if (maxV > cutoff) {
            double[] scores = geneScoreMap.get(geneS);
            if (scores == null) {
              scores = new double[]{0};
              geneScoreMap.put(geneS, scores);
            }
            if (p == 0) {
              scores[0] += 17;
            } else {
              scores[0] += (-Math.log10(p));
            }
          }
           */
          validAllGeneNum++;

        }

      }
      int tableRow = geneTablebyConditionP.size();
      // Collections.sort(results, new EnrichTissueComparator());
      for (int i = 0; i < tableRow; i++) {
        String geneS = (String) geneTablebyConditionP.get(i)[1];
        double[] scores = geneScoreMap.get(geneS);
        if (scores != null) {
          geneTablebyConditionP.get(i)[6] = String.valueOf(scores[0]);
        } else {
          geneTablebyConditionP.get(i)[6] = "-";
        }
      }

      return results;

    }

    public void updateRankofGenes(int methodNum, Map<String, double[]> tissueScoreMap, List<Object[]> geneTablebyConditionP, Set<String> allHGNCGenes) throws Exception {

      Map<String, IntArrayList> geneExp = new HashMap<String, IntArrayList>();
      List<double[]> valueList = new ArrayList<double[]>();

      int index0 = 0;
      int geneIndex = 0;
      String geneSymb;
      int index;

      List<String> spcGenes = new ArrayList();
      List<double[]> spcValues = new ArrayList();
      List<String> tissueNames = new ArrayList();
      int geneNum = spcGenes.size();
      Map<String, double[]> geneScoreMap = new HashMap<String, double[]>();
      for (int methodIndex = 0; methodIndex < methodNum; methodIndex++) {
        tissueNames.clear();
        spcGenes.clear();
        spcValues.clear();
        produceRobustZScore(expressionPath, minExp, tissueNames, spcGenes, spcValues, methodIndex);
        geneNum = spcGenes.size();
        for (int g = 0; g < geneNum; g++) {
          double[] values = spcValues.get(g);
          valueList.add(values);
          geneSymb = spcGenes.get(g);
          index = geneSymb.indexOf(':');

          // a gene may have multiple transcripts
          if (index > 0) {
            geneSymb = geneSymb.substring(0, index);
          }
          IntArrayList indexes = geneExp.get(geneSymb);
          if (indexes == null) {
            indexes = new IntArrayList();
            geneExp.put(geneSymb, indexes);
          }
          indexes.add(index0);
          index0++;
          //System.out.println(cells[180] + "\t" + cells[180 + 52]);
        }

        double maxV;
        double minV;

        int testNum = valueList.get(0).length;
        double p;

        List<String> allGenes = new ArrayList<String>(geneExp.keySet());

        int effectNum = 0;

        DoubleArrayList allExpressionValues = new DoubleArrayList();

        for (int colIndex = 0; colIndex < testNum; colIndex++) {
          double[] tissueScores = tissueScoreMap.get(tissueNames.get(colIndex));

          //oly tissue specific 
          allExpressionValues.clear();
          for (String geneS : allGenes) {
            if (!allHGNCGenes.contains(geneS)) {
              continue;
            }
            IntArrayList indexes = geneExp.get(geneS);
            if (indexes == null) {
              continue;
            }

            maxV = -Double.MAX_VALUE;
            minV = Double.MAX_VALUE;
            effectNum = 0;

            for (int i = 0; i < indexes.size(); i++) {
              if (Double.isNaN(valueList.get(indexes.getQuick(i))[colIndex])) {
                continue;
              }

              if (maxV < valueList.get(indexes.getQuick(i))[colIndex]) {
                maxV = valueList.get(indexes.getQuick(i))[colIndex];
              }
              if (minV > valueList.get(indexes.getQuick(i))[colIndex]) {
                minV = valueList.get(indexes.getQuick(i))[colIndex];
              }
              effectNum++;
            }

            if (effectNum == 0) {
              continue;
            }

            allExpressionValues.add(maxV);
          }

          allExpressionValues.quickSort();
          //calculate gene's tissue specific score 
          double rankID;
          for (String geneS : allGenes) {
            if (!allHGNCGenes.contains(geneS)) {
              continue;
            }
            IntArrayList indexes = geneExp.get(geneS);
            if (indexes == null) {
              continue;
            }

            maxV = -Double.MAX_VALUE;
            minV = Double.MAX_VALUE;
            effectNum = 0;
            for (int i = 0; i < indexes.size(); i++) {
              if (Double.isNaN(valueList.get(indexes.getQuick(i))[colIndex])) {
                continue;
              }

              if (maxV < valueList.get(indexes.getQuick(i))[colIndex]) {
                maxV = valueList.get(indexes.getQuick(i))[colIndex];
              }
              if (minV > valueList.get(indexes.getQuick(i))[colIndex]) {
                minV = valueList.get(indexes.getQuick(i))[colIndex];
              }
              effectNum++;
            }
            if (Double.isNaN(maxV)) {
              continue;
            }
            if (effectNum == 0) {
              continue;
            }

            rankID = allExpressionValues.binarySearch(maxV);
            if (rankID < 0) {
              rankID = -rankID - 1;
            }
            //The new approach 
            rankID = rankID / allExpressionValues.size();
            double[] scores = geneScoreMap.get(geneS);
            if (scores == null) {
              scores = new double[]{0};
              geneScoreMap.put(geneS, scores);
            }
            p = tissueScores[methodIndex];
            if (p == 0) {
              scores[0] += (17 * rankID);
            } else {
              scores[0] += (-Math.log10(p) * rankID);
            }

          }
        }
      }

      int tableRow = geneTablebyConditionP.size();
      // Collections.sort(results, new EnrichTissueComparator());
      for (int i = 0; i < tableRow; i++) {
        String geneS = (String) geneTablebyConditionP.get(i)[1];
        double[] scores = geneScoreMap.get(geneS);
        if (scores != null) {
          geneTablebyConditionP.get(i)[6] = String.valueOf(scores[0]);
        } else {
          geneTablebyConditionP.get(i)[6] = "-";
        }
      }

    }

    public Set<String> loadGeneSysmbols() {
      Set<String> approvedGeneSymb = new HashSet<String>();
      try {
        BufferedReader br = LocalFileFunc.getBufferedReader("resources/HgncGene.txt.gz");
        String line = null;
        String[] names;
        while ((line = br.readLine()) != null) {
          if (line.trim().length() == 0) {
            continue;
          }
          String[] cells = line.split("\t", -1);
          if (!cells[3].equals("Approved")) {
            //  continue;
          }
          approvedGeneSymb.add(cells[1]);

        }
        br.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      return approvedGeneSymb;
    }

    @Override
    protected void process(List<String> chunks) {
      // TODO Auto-generated method stub  
      for (String message : chunks) {
        LOG.info(message);
        StatusDisplayer.getDefault().setStatusText(message);
      }

    }

    @Override
    protected void done() {
      try {
        String message;
        if (!succeed) {
          message = ("Scan conditional gene-based association failed!");
          LOG.info(message);
          return;
        }

        message = ("Conditional gene-based association scan has been finished!");
        LOG.info(message);
        StatusDisplayer.getDefault().setStatusText(message);
        ph.finish();

        String prjName = GlobalManager.currentProject.getName();
        String prjWorkingPath = GlobalManager.currentProject.getWorkingPath();

        time = System.nanoTime() - time;
        time = time / 1000000000;
        long min = time / 60;
        long sec = time % 60;
        String info = ("Elapsed time: " + min + " min. " + sec + " sec.");
        listPValueTableModel.fireTableDataChanged();
        if (driverTissueTableModel != null) {
          driverTissueTableModel.setDataList(tissueTable);
          driverTissueTableModel.fireTableDataChanged();
          dtTopComponent.showTissueTab();
        }
        JOptionPane.showMessageDialog(null, "The conditional gene-based test has been finished!", "Message", JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        ErrorManager.getDefault().notify(e);
      }
    }
  }

  // this function attempts to calculate SNP LD only within genes because it is too slow to do others
  public CorrelationBasedByteLDSparseMatrix calculateLocalLDRSquarebyGenotypesPositions(PlinkDataset plinkSet, Set<Integer> positionsSet,
      String chromName, LiftOver liftOver) throws Exception {
    if (positionsSet == null || positionsSet.isEmpty()) {
      return null;
    }
    if (!plinkSet.avaibleFiles()) {
      String infor = "No Plink binary file on chromosome " + chromName;

      LOG.info(infor);
      return null;
    }
    Map<String, StatusGtySet> indivGtyMap = new HashMap<String, StatusGtySet>();
    List<SNP> mappedSNP = plinkSet.readSNPsinPlinkBinaryMapFileByPositions(positionsSet, chromName, liftOver);
    IntArrayList snpPositionList = new IntArrayList();
    snpPositionList.addAllOf(positionsSet);
    snpPositionList.quickSort();
    int ldSNPNum = snpPositionList.size();
    OpenIntIntHashMap allIndexMap = new OpenIntIntHashMap(ldSNPNum);
    for (int i = 0; i < ldSNPNum; i++) {
      allIndexMap.put(snpPositionList.getQuick(i), i);
    }
    snpPositionList.clear();

    CorrelationBasedByteLDSparseMatrix ldRsMatrix = new CorrelationBasedByteLDSparseMatrix(allIndexMap);

    String infor = "Reading local genotypes on chromosome " + chromName + "...";
    plinkSet.readPlinkBinaryGenotypeinPedigreeFile(mappedSNP, indivGtyMap);
    LOG.info(infor);
    List<StatusGtySet> chromGtys = new ArrayList<StatusGtySet>(indivGtyMap.values());
    infor = "Calculating local pair-wise LD of SNP within genes on chromosome " + chromName + "...";

    LOG.info(infor);
    if (!GenotypeSetUtil.calculateGenotypeCorrelationSquareFastBit(mappedSNP, chromGtys, ldRsMatrix, genome.getMinEffectiveR2())) {
      infor = "Using slow function due to mssing genotypes!!!";

      LOG.info(infor);
      GenotypeSetUtil.calculateGenotypeCorrelationSquareFast(mappedSNP, chromGtys, ldRsMatrix, genome.getMinEffectiveR2());
    }

    return ldRsMatrix;
  }

  // this function attempts to calculate SNP LD only within genes because it is too slow to do others
  public CorrelationBasedByteLDSparseMatrix calculateLocalLDRSquarebyHaplotypeVCFByPositions(
      String vcfFilePath, String chromName, Set<Integer> positionsSet, LiftOver liftOver) throws Exception {

    if (vcfFilePath == null) {
      return null;
    }
    File vcfFile = new File(vcfFilePath);

    HaplotypeDataset haplotypeDataset = new HaplotypeDataset();
    if (!vcfFile.exists()) {
      String infor = "The VCF file for chromosome " + chromName + " does not exist!";

      LOG.info(infor);
      return null;
    }

    String infor = "Reading haplotpyes on chromosome " + chromName + "...";

    LOG.info(infor);

    List<StatusGtySet> chromGtys = new ArrayList< StatusGtySet>();
    List<SNP> mappedSNP = haplotypeDataset.readSNPMapHapVCFFileByPositions(vcfFile, chromName, positionsSet, liftOver, chromGtys);

    Set<Integer> addedPositionSet = new HashSet<Integer>();
    for (SNP snp : mappedSNP) {
      addedPositionSet.add(snp.physicalPosition);
    }
    //count SNPs have not genotype information
    StringBuilder sb = new StringBuilder();
    int unmappedSNPinGeneNum = 0;
    int totalSNPinGeneNum = positionsSet.size();
    IntArrayList snpPositionList = new IntArrayList();

    sb.append("Position\n");
    for (Integer pos1 : positionsSet) {
      if (!addedPositionSet.contains(pos1)) {
        sb.append(pos1);
        sb.append('\n');
        unmappedSNPinGeneNum++;
      } else {
        snpPositionList.add(pos1);
      }
    }
    /*
         if (unmappedSNPinGeneNum > 0) {
         String prjName = GlobalManager.currentProject.getName();
         String prjWorkingPath = GlobalManager.currentProject.getWorkingPath();
         File outPath = new File(prjWorkingPath + File.separator + prjName + File.separator + geneScan.getName() + ".NoLDSNPs." + chromName + ".txt");
         BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
         bw.write(sb.toString());
         bw.close();

         sb.delete(0, sb.length());
         infor = "Warning!!! " + unmappedSNPinGeneNum + " variants within genes out of " + totalSNPinGeneNum + " on chromosome " + chromName + " have NO haplotype information and will be assumed to be independent of others! Detailed information of these variants is saved in " + outPath.getCanonicalPath() + ".";
            
         LOG.info(infor);
         }
     */
    infor = "Calculating local pair-wise LD of SNP within genes on chromosome " + chromName + " ...";

    LOG.info(infor);
    snpPositionList.quickSort();
    int ldSNPNum = snpPositionList.size();
    OpenIntIntHashMap allIndexMap = new OpenIntIntHashMap(ldSNPNum);
    for (int i = 0; i < ldSNPNum; i++) {
      allIndexMap.put(snpPositionList.getQuick(i), i);
    }
    snpPositionList.clear();
    CorrelationBasedByteLDSparseMatrix ldRsMatrix = new CorrelationBasedByteLDSparseMatrix(allIndexMap);
    //GenotypeSetUtil.calculateGenotypeCorrelationSquare(mappedSNP, chromGtys, ldRsMatrix);

    if (!GenotypeSetUtil.calculateLDRSequareByHaplotypeFast(mappedSNP, chromGtys, ldRsMatrix, genome.getMinEffectiveR2())) {
      infor = "Using slow function due to mssing genotypes!!!";

      LOG.info(infor);
      GenotypeSetUtil.calculateLDRSequareByHaplotype(mappedSNP, chromGtys, ldRsMatrix, genome.getMinEffectiveR2());
    }
    mappedSNP.clear();
    chromGtys.clear();
    addedPositionSet.clear();

    return ldRsMatrix;
  }

  public String LDPruning(List<SNP> mainSnpMap, CorrelationBasedByteLDSparseMatrix ldCorr, double maxCorr, boolean ingoreNOGty) throws Exception {
    List<SNP> tmpSNPMap = new ArrayList<SNP>();
    tmpSNPMap.addAll(mainSnpMap);
    mainSnpMap.clear();
    int listSize = tmpSNPMap.size();
    Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
    int windowSize = 50;
    int stepLen = 5;
    double r, c;
    int[] counts = new int[2];
    for (int s = 0; s < listSize; s += stepLen) {
      for (int i = s; (i - s <= windowSize) && (i < listSize); i++) {
        SNP snp1 = tmpSNPMap.get(i);
        if (ingoreNOGty) {
          if (snp1.genotypeOrder < 0) {
            highlyCorrIndexes.add(i);
            continue;
          }
        }
        if (highlyCorrIndexes.contains(i)) {
          continue;
        }
        for (int j = i + 1; (j - i <= windowSize) && (j < listSize); j++) {
          if (highlyCorrIndexes.contains(j)) {
            continue;
          }
          SNP snp2 = tmpSNPMap.get(j);
          if (ingoreNOGty) {
            if (snp2.genotypeOrder < 0) {
              highlyCorrIndexes.add(j);
              continue;
            }
          }

          r = ldCorr.getLDAt(snp1.physicalPosition, snp2.physicalPosition);
          /*
                     r = Math.sqrt(ldRMatrix.getQuick(t, j));
                     //for R
                     c = (0.6065 * r - 1.033) * r + 1.7351;
                     if (c > 2) {
                     c = 2;
                     }
           */

          //R2 
          //y = -35.741x6 + 111.16x5 - 128.42x4 + 66.906x3 - 14.641x2 + 0.6075x + 0.8596
          //c = (((((-35.741 * r + 111.16) * r - 128.42) * r + 66.906) * r - 14.641) * r + 0.6075) * r + 0.8596;
          //y = 0.2725x2 - 0.3759x + 0.8508
          //c = (0.2725 * r - 0.3759) * r + 0.8508;
          // y = 0.2814x2 - 0.4308x + 0.86
          //c = (0.2814 * r - 0.4308) * r + 0.86;
          //y = -0.155x + 0.8172
          //c = -0.155 * r + 0.8172;
          // c = 0.9;
          // r = Math.pow(r, c);
          if (r >= maxCorr) {
            highlyCorrIndexes.add(j);
          }
        }
      }
    }

    counts[0] = listSize - highlyCorrIndexes.size();
    counts[1] = listSize;
    String info = (listSize - (highlyCorrIndexes.size()) + " SNPs (out of " + listSize + ") passed LD pruning (r2>=" + maxCorr + ").");

    for (int s = 0; s < listSize; s++) {
      SNP snp1 = tmpSNPMap.get(s);
      if (!highlyCorrIndexes.contains(s)) {
        mainSnpMap.add(snp1);
      }
    }
    return info;
  }

  //use a faster matrix invers function DenseMatrix64F
  private double snpSetPValuebyMyChiSquareApproxEJML(List<SNP> snpList, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
      boolean printMatirx, double[] results) throws Exception {
    boolean ignoreNoLDSNP = true;
    int snpNum = snpList.size();
    Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
    double maxCorr = 0.98;
    double r, c;
    LDPruning(snpList, ldCorr, maxCorr, ignoreNoLDSNP);

    double p1 = Double.NaN;
    //CALB2

    List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();
    snpNum = snpList.size();
    //here I think the only uesfulness of the pvalueWeightList is to filter out the null p1-value SNPs
    for (int k = 0; k < snpNum; k++) {
      SNP snp = snpList.get(k);
      if (ignoreNoLDSNP) {
        if (snp.genotypeOrder < 0) {
          continue;
        }
      }
      double[] pValues = snp.getpValues();
      if (pValues == null) {
        continue;
      }

      if (!Double.isNaN(pValues[snpPVTypeIndex])) //
      {
        PValueWeight pv = new PValueWeight();
        pv.physicalPos = snp.physicalPosition;
        pv.pValue = pValues[snpPVTypeIndex];
        pv.chiSquare = pv.pValue / 2;
        pv.chiSquare = MultipleTestingMethod.zScore(pv.chiSquare);
        pv.chiSquare = pv.chiSquare * pv.chiSquare;

        pv.weight = 1;
        pvalueWeightList.add(pv);
      }
    }

    snpNum = pvalueWeightList.size();

    if (snpNum == 0) {
      results[0] = 1;
      results[1] = pvalueWeightList.get(0).chiSquare;
      return Double.NaN;
    } else if (snpNum == 1) {
      results[0] = 1;
      results[1] = pvalueWeightList.get(0).chiSquare;
      return pvalueWeightList.get(0).pValue;
    }
//min (Ax - b)'(Ax-b) = min x'A'Ax + b'Ax
//s.t. x > 0 
//Can therefore consider the packages quadprog and nnls on CRAN.

    DoubleMatrix2D ldRMatrix = new DenseDoubleMatrix2D(snpNum, snpNum);

    for (int i = 0; i < snpNum; i++) {
      PValueWeight pv = pvalueWeightList.get(i);
      ldRMatrix.setQuick(i, i, pv.weight * pv.weight);
      for (int j = i + 1; j < snpNum; j++) {
        //Math.sqrt(pValueArray[t].var * pValueArray[j].var) 
        r = 0;
        ldRMatrix.setQuick(i, j, pv.weight * pvalueWeightList.get(j).weight * ldCorr.getLDAt(pv.physicalPos, pvalueWeightList.get(j).physicalPos));
        if (ldRMatrix.getQuick(i, j) > 0) {
          /*
                     r = Math.sqrt(ldRMatrix.getQuick(t, j));
                     //for R
                     c = (0.6065 * r - 1.033) * r + 1.7351;
                     if (c > 2) {
                     c = 2;
                     }
           */

          r = (ldRMatrix.getQuick(i, j));
          //R2
          //R2
          //y = -35.741x6 + 111.16x5 - 128.42x4 + 66.906x3 - 14.641x2 + 0.6075x + 0.8596
          //c = (((((-35.741 * r + 111.16) * r - 128.42) * r + 66.906) * r - 14.641) * r + 0.6075) * r + 0.8596;
          //y = 0.2725x2 - 0.3759x + 0.8508
          //c = (0.2725 * r - 0.3759) * r + 0.8508;
          // y = 0.2814x2 - 0.4308x + 0.86
          //c = (0.2814 * r - 0.4308) * r + 0.86;
          //y = -0.155x + 0.8172
          //c = -0.155 * r + 0.8172;
          // c = 0.9;

          // r = Math.pow(r, c);
          ldRMatrix.setQuick(i, j, r);
        } else {
          ldRMatrix.setQuick(i, j, 0);
        }
        ldRMatrix.setQuick(j, i, r);
      }
    }

    //remove redundant SNPs according to LD
    int originalSampleSize = snpNum;
    maxCorr = 0.98;
    for (int i = 0; i < originalSampleSize; i++) {
      for (int j = i + 1; j < originalSampleSize; j++) {
        if (Math.abs(ldRMatrix.getQuick(i, j)) >= maxCorr) {
          if (!highlyCorrIndexes.contains(j) && !highlyCorrIndexes.contains(i)) {
            highlyCorrIndexes.add(j);
            // System.out.println(t + " <-> " + j);
          }
        }
      }
    }

    if (highlyCorrIndexes.size() > 0) {
      // System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
      snpNum = originalSampleSize - highlyCorrIndexes.size();
      List<PValueWeight> tmpPvalueWeightList = new ArrayList<PValueWeight>(pvalueWeightList);
      pvalueWeightList.clear();
      DoubleMatrix2D tmpCorMat = new DenseDoubleMatrix2D(snpNum, snpNum);
      int incRow = 0;
      int incCol = 0;
      for (int i = 0; i < originalSampleSize; i++) {
        if (highlyCorrIndexes.contains(i)) {
          continue;
        }
        pvalueWeightList.add(tmpPvalueWeightList.get(i));
        incCol = 0;
        for (int j = 0; j < originalSampleSize; j++) {
          if (highlyCorrIndexes.contains(j)) {
            continue;
          }
          tmpCorMat.setQuick(incRow, incCol, ldRMatrix.getQuick(i, j));
          incCol++;
        }
        incRow++;
      }
      ldRMatrix = tmpCorMat;
      tmpPvalueWeightList.clear();
      // System.out.println(corrMat.toString());
    }
    if (snpNum == 1) {
      results[0] = 1;
      results[1] = pvalueWeightList.get(0).chiSquare;
      return pvalueWeightList.get(0).pValue;
    }
//System.out.println(gene.+"\t"+snpNum);


    /*
         do {
         blockCluster = new MarixDensity().getCluster(ldRMatrix, threshold, maxBlockSize, scale, pairThreshold);
         threshold += 0.05;
         pairThreshold += 0.05;
         //  snpNum < 500 ||
         } while (blockCluster.size() > 3);
     */
    double df = 0;
    double Y = 0;
    double dft = 0;
    double Yt = 0;

    SetBasedTest sbt = new SetBasedTest();
    double[] result = new double[2];
    if (printMatirx) {
      System.out.println(ldRMatrix.toString());
    }

    DenseMatrix64F A = new DenseMatrix64F(snpNum, snpNum);

    DenseMatrix64F b1 = new DenseMatrix64F(snpNum, 1);
    for (int i = 0; i < snpNum; i++) {
      A.set(i, i, 1);
      for (int j = i + 1; j < snpNum; j++) {
        A.set(i, j, ldRMatrix.getQuick(i, j));
        A.set(j, i, ldRMatrix.getQuick(i, j));
      }
    }

    for (int k = 0; k < snpNum; k++) {
      b1.set(k, 0, pvalueWeightList.get(k).chiSquare);
    }

    DenseMatrix64F b2 = new DenseMatrix64F(snpNum, 1);
    for (int i = 0; i < snpNum; i++) {
      b2.set(i, 0, 1);
    }

    sbt.mySudoSVDSolverOverlappedWindow(A, b1, b2, result);

    dft += result[0];
    Yt += result[1];

    //   System.out.println(ldRMatrix.toString());
    p1 = Gamma.incompleteGammaComplement(dft / 2, Yt / 2);

    results[0] = dft;
    results[1] = Yt;
    return p1;
  }

  //use a faster matrix invers function DenseMatrix64F
  private double snpSetPValuebyJohnnyChiSquare(List<SNP> snpList, DoubleArrayList pValueArray, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
      boolean needWeight, RConnection rcon) throws Exception {

    int snpNum = snpList.size();
    // System.out.println(gene.getSymbol() + " " + snpNum);
    //because it is very time consumming, when SNP number is over 200. I try to spl

    double p1 = Double.NaN;

    for (int k = 0; k < snpNum; k++) {
      SNP snp = snpList.get(k);
      double[] pValues = snp.getpValues();
      if (pValues == null) {
        continue;
      }
      if (!Double.isNaN(pValues[snpPVTypeIndex])) //
      {
        pValueArray.add(pValues[snpPVTypeIndex]);
      }
    }

    int[] keySNPPosition = new int[1];
    keySNPPosition[0] = -1;
    List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();

    boolean ignoreNoLDSNP = true;

    //here I think the only uesfulness of the pvalueWeightList is to filter out the null p1-value SNPs
    for (int k = 0; k < snpNum; k++) {
      SNP snp = snpList.get(k);
      if (ignoreNoLDSNP) {
        if (snp.genotypeOrder < 0) {
          continue;
        }
      }
      double[] pValues = snp.getpValues();
      if (pValues == null) {
        continue;
      }

      if (!Double.isNaN(pValues[snpPVTypeIndex])) //
      {
        PValueWeight pv = new PValueWeight();
        pv.physicalPos = snp.physicalPosition;
        pv.pValue = pValues[snpPVTypeIndex];
        pv.chiSquare = pv.pValue / 2;
        pv.chiSquare = MultipleTestingMethod.zScore(pv.chiSquare);
        pv.chiSquare = pv.chiSquare * pv.chiSquare;

        pv.weight = 1;
        pvalueWeightList.add(pv);
      }
    }

    snpNum = pvalueWeightList.size();

    if (snpNum == 0) {
      keySNPPosition[0] = -1;
      return Double.NaN;
    } else if (snpNum == 1) {
      keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
      return pvalueWeightList.get(0).pValue;
    }

    DoubleMatrix2D ldRMatrix = new DenseDoubleMatrix2D(snpNum, snpNum);

    for (int i = 0; i < snpNum; i++) {
      PValueWeight pv = pvalueWeightList.get(i);
      ldRMatrix.setQuick(i, i, pv.weight * pv.weight);
      for (int j = i + 1; j < snpNum; j++) {
        //Math.sqrt(pValueArray[t].var * pValueArray[j].var) 
        ldRMatrix.setQuick(i, j, pv.weight * pvalueWeightList.get(j).weight * ldCorr.getLDAt(pv.physicalPos, pvalueWeightList.get(j).physicalPos));
        if (ldRMatrix.getQuick(i, j) > 0) {
          ldRMatrix.setQuick(i, j, Math.sqrt(ldRMatrix.getQuick(i, j)));
        } else {
          ldRMatrix.setQuick(i, j, 0);
        }
        ldRMatrix.setQuick(j, i, ldRMatrix.getQuick(i, j));
      }
    }
//remove redundant SNPs according to LD
    int originalSampleSize = snpNum;
    Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
    double maxCorr = 0.99;
    for (int i = 0; i < originalSampleSize; i++) {
      for (int j = i + 1; j < originalSampleSize; j++) {
        if (Math.abs(ldRMatrix.getQuick(i, j)) >= maxCorr) {
          if (!highlyCorrIndexes.contains(j) && !highlyCorrIndexes.contains(i)) {
            highlyCorrIndexes.add(j);
            // System.out.println(t + " <-> " + j);
          }
        }
      }
    }

    if (highlyCorrIndexes.size() > 0) {
      // System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
      snpNum = originalSampleSize - highlyCorrIndexes.size();
      List<PValueWeight> tmpPvalueWeightList = new ArrayList<PValueWeight>(pvalueWeightList);
      pvalueWeightList.clear();
      DoubleMatrix2D tmpCorMat = new DenseDoubleMatrix2D(snpNum, snpNum);
      int incRow = 0;
      int incCol = 0;
      for (int i = 0; i < originalSampleSize; i++) {
        if (highlyCorrIndexes.contains(i)) {
          continue;
        }
        pvalueWeightList.add(tmpPvalueWeightList.get(i));
        incCol = 0;
        for (int j = 0; j < originalSampleSize; j++) {
          if (highlyCorrIndexes.contains(j)) {
            continue;
          }
          tmpCorMat.setQuick(incRow, incCol, ldRMatrix.getQuick(i, j));
          incCol++;
        }
        incRow++;
      }
      ldRMatrix = tmpCorMat;
      tmpPvalueWeightList.clear();
      // System.out.println(corrMat.toString());
    }

    if (snpNum == 1) {
      keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
      return pvalueWeightList.get(0).pValue;
    }

    double df = 0;
    double Y = 0;
    for (int i = 0; i < snpNum; i++) {
      PValueWeight pv = pvalueWeightList.get(i);
      Y += (pv.weight * pv.chiSquare);
    }
    EigenvalueDecomposition ed = new EigenvalueDecomposition(ldRMatrix);
    DoubleMatrix1D eVR = ed.getRealEigenvalues();
    double[] weights = eVR.toArray();
    boolean hasNeg = false;
    for (int i = 0; i < weights.length; i++) {
      if (weights[i] < 0) {
        hasNeg = true;
        //  weights[t] = 0;
      }
    }
    if (hasNeg) {
      int sss = 0;
      // System.out.println(ed.getD().toString());
    }
    rcon.assign("weights", weights);

    double p = rcon.eval("pchisqsum(" + Y + ",df=rep(1," + snpNum + "),method=\"int\",lower=FALSE,a=weights)").asDouble();//This step will cost a lot of time. 
    if (p < 1E-8) {
      int sss = 0;
      System.out.println(eVR.toString());
      System.out.println(ed.getImagEigenvalues());
    }

    return p;
  }

}
