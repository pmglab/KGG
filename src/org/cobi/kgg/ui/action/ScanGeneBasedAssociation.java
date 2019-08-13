/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import cern.jet.stat.Descriptive;
import cern.jet.stat.Gamma;
import cern.jet.stat.Probability;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingWorker;
import net.sf.samtools.util.AsciiLineReader;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import java.util.logging.Logger;
import org.cobi.kgg.business.EffectiveNumberEstimator;
import org.cobi.kgg.business.ManhattanPlotPainter;
import org.cobi.kgg.business.PValuePainter;
import org.cobi.kgg.business.SetBasedTest;
import org.cobi.kgg.business.entity.Chromosome;
import static org.cobi.kgg.business.entity.Constants.CHROM_NAMES;

import org.cobi.kgg.business.entity.CorrelationBasedByteLDSparseMatrix;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.PValueGene;
import org.cobi.kgg.business.entity.PValueWeight;
import org.cobi.kgg.business.entity.PValueWeightComparator;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.SNPPosiComparator;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.FormatShowingDialog;
import org.cobi.kgg.ui.dialog.ProjectTopComponent;
import org.cobi.kgg.ui.dialog.RunningResultViewerTopComponent;
import org.cobi.util.math.MarixDensity;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.text.Util;
import org.ejml.data.DenseMatrix64F;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author mxli
 */
public class ScanGeneBasedAssociation implements ActionListener {
    //reference http://rubenlaguna.com/wp/2010/01/18/cancellable-tasks-and-progress-indicators-netbeans-platform/index.html/

    private final static Logger LOG = Logger.getLogger(ScanGeneBasedAssociation.class.getName());
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private RequestProcessor.Task buildTask = null;
    GeneBasedAssociation gbAssoc = null;
    private RequestProcessor.Task theTask = null;
    private int[] testedPValueIndexes;
    private double[] propposedWeightsforCategory;
    //plotGenePValues settings
    private int manhattanPlotWidth;
    private int manhattanPlotHeight;
    private double manhattanPlotLabeGenePValue;
    private double manhattanPlotLableSNPPValue;
    private double manhattanPlotMinPValue;
    private boolean manhattanPlotSNPOutSideGene;
    private int qqPlotWidth;
    private int qqPlotHeight;
    private double qqPlotMinPValue;
    private boolean qqPlotAllSNP;
    private boolean qqPlotSNPInsideGene;
    private boolean qqPlotSNPOutSideGene;
    private boolean toWeight;
    double MIN_R2 = 1E-5;
    private String geneTestMethodName;
    private boolean networkWeightGenePValue = false;
    RunningResultViewerTopComponent runningResultTopComp;
    private File imgFolder;
    private Set<Byte> geneGroupIDSet;
    SetBasedTest sbt = new SetBasedTest();
    private String varWeighFilePath;

    public Set<Byte> getGeneGroupIDSet() {
        return geneGroupIDSet;
    }

    public void setVarWeighFilePath(String varWeighFilePath) {
        this.varWeighFilePath = varWeighFilePath;
    }

    public void setGeneGroupIDSet(Set<Byte> geneGroupIDSet) {
        this.geneGroupIDSet = geneGroupIDSet;
    }

    public void setPpiWeightGenePValue(boolean ppiWeightGenePValue) {
        this.networkWeightGenePValue = ppiWeightGenePValue;
    }

    public String getGeneTestMethodName() {
        return geneTestMethodName;
    }

    public void setGeneTestMethodName(String geneTestMethodName) {
        this.geneTestMethodName = geneTestMethodName;
    }

    public boolean isToWeight() {
        return toWeight;
    }

    public void setToWeight(boolean toWeight) {
        this.toWeight = toWeight;
    }

    public boolean isManhattanPlotSNPOutSideGene() {
        return manhattanPlotSNPOutSideGene;
    }

    public void setManhattanPlotSNPOutSideGene(boolean manhattanPlotSNPOutSideGene) {
        this.manhattanPlotSNPOutSideGene = manhattanPlotSNPOutSideGene;
    }

    public int getManhattanPlotHeight() {
        return manhattanPlotHeight;
    }

    public void setManhattanPlotHeight(int manhattanPlotHeight) {
        this.manhattanPlotHeight = manhattanPlotHeight;
    }

    public double getManhattanPlotLabeGenePValue() {
        return manhattanPlotLabeGenePValue;
    }

    public void setManhattanPlotLabeGenePValue(double manhattanPlotLabeGenePValue) {
        this.manhattanPlotLabeGenePValue = manhattanPlotLabeGenePValue;
    }

    public double getManhattanPlotLableSNPPValue() {
        return manhattanPlotLableSNPPValue;
    }

    public void setManhattanPlotLableSNPPValue(double manhattanPlotLableSNPPValue) {
        this.manhattanPlotLableSNPPValue = manhattanPlotLableSNPPValue;
    }

    public double getManhattanPlotMinPValue() {
        return manhattanPlotMinPValue;
    }

    public void setManhattanPlotMinPValue(double manhattanPlotMinPValue) {
        this.manhattanPlotMinPValue = manhattanPlotMinPValue;
    }

    public int getManhattanPlotWidth() {
        return manhattanPlotWidth;
    }

    public void setManhattanPlotWidth(int manhattanPlotWidth) {
        this.manhattanPlotWidth = manhattanPlotWidth;
    }

    public boolean isQqPlotAllSNP() {
        return qqPlotAllSNP;
    }

    public void setQqPlotAllSNP(boolean qqPlotAllSNP) {
        this.qqPlotAllSNP = qqPlotAllSNP;
    }

    public int getQqPlotHeight() {
        return qqPlotHeight;
    }

    public void setQqPlotHeight(int qqPlotHeight) {
        this.qqPlotHeight = qqPlotHeight;
    }

    public double getQqPlotMinPValue() {
        return qqPlotMinPValue;
    }

    public void setQqPlotMinPValue(double qqPlotMinPValue) {
        this.qqPlotMinPValue = qqPlotMinPValue;
    }

    public boolean isQqPlotOutSideGene() {
        return qqPlotSNPOutSideGene;
    }

    public void setQqPlotOutSideGene(boolean qqPlotOutSideGene) {
        this.qqPlotSNPOutSideGene = qqPlotOutSideGene;
    }

    public boolean isQqPlotSNPInsideGene() {
        return qqPlotSNPInsideGene;
    }

    public void setQqPlotSNPInsideGene(boolean qqPlotSNPInsideGene) {
        this.qqPlotSNPInsideGene = qqPlotSNPInsideGene;
    }

    public int getQqPlotWidth() {
        return qqPlotWidth;
    }

    public void setQqPlotWidth(int qqPlotWidth) {
        this.qqPlotWidth = qqPlotWidth;
    }

    public double[] getPropposedWeightsforCategory() {
        return propposedWeightsforCategory;
    }

    public void setPropposedWeightsforCategory(double[] propposedWeightsforCategory) {
        this.propposedWeightsforCategory = propposedWeightsforCategory;
    }

    /**
     *
     * @return
     */
    public int[] getTestedPValueIndexes() {
        return testedPValueIndexes;
    }

    /**
     *
     * @param testedPValueIndexes
     */
    public void setTestedPValueIndexes(int[] testedPValueIndexes) {
        this.testedPValueIndexes = testedPValueIndexes;
    }

    /**
     *
     * @param name
     */
    public ScanGeneBasedAssociation(GeneBasedAssociation gbAssoc, RunningResultViewerTopComponent runningResultTopComp) {
        this.gbAssoc = gbAssoc;
        this.runningResultTopComp = runningResultTopComp;
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
            StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
            java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public ScanGeneBasedAssociation(GeneBasedAssociation gbAssoc) {
        this.gbAssoc = gbAssoc;
    }

    public Map<String, Double> calculatePPIGeneAttributeWeight() throws Exception {
        Map<String, Double> genePPIAtts = new HashMap<String, Double>();
        int[] indexes = new int[]{1, 2, 3, 5, 7, 8, 9};

        File file = new File(GlobalManager.RESOURCE_PATH + "PPIGeneAttribute.txt.gz");
        LineReader br = null;
        if (file.getName().endsWith(".zip") || file.getName().endsWith(".gz") || file.getName().endsWith(".tar.gz")) {
            br = new CompressedFileReader(file);
        } else {
            br = new AsciiLineReader(file);
        }
        String line;
        double[] average = new double[indexes.length];
        Arrays.fill(average, 0);
        //skip the head line
        br.readLine();

        double geneWeight = 1;
        /*   //The second time
         * Call:
         glm(formula = hi$DiseaseOrNot ~ hi$Degree + hi$Betweenness + 
         hi$Closeness + hi$PageRank + hi$ClusteringCoefficient, family = binomial(link = "logit"), 
         na.action = na.pass)
        
         Coefficients...
         Variable      Coeff.      td. Error       z value      Pr(>|z|)
         1      -1.2241       0.2555      -4.7914 1.6559E-6
         2      -1.1869       0.6869      -1.7279 0.0840118
         3       0.3932       0.1142       3.4434 0.0005745
         4      -0.0056       0.0015      -3.7595 0.0001702
         5       0.002        0.0011       1.9333 0.0532025
         6      -1.7354       0.1938      -8.9531 3.4558E-19
         7       0.4478       0.1127       3.9725 7.1110E-5
         Intercept     -0.7212     0.9058    -0.7962 0.4258918
        
         Odds Ratios...
         Variable         O.R.
         1       0.294 
         2       0.3052
         3       1.4818
         4       0.9944
         5       1.0021
         6       0.1763
         7       1.5649
         Significance of the model with these variable(s)  p1-value 4.967775243731454E-72   
         * 
         * 
         */
//skip first line
        br.readLine();
        double[] params = new double[]{-1.2241, -1.1869, 0.3932, -0.0056, 0.002, -1.7354, 0.4478};
        DoubleArrayList weights = new DoubleArrayList();
        boolean hasMissing;
        while ((line = br.readLine()) != null) {
            //line = line.trim();
            //System.out.println(line);
            if (line.trim().length() == 0) {
                continue;
            }
            geneWeight = -0.7212;
            hasMissing = false;
            String[] cells = line.split("\t", -1);
            for (int i = 0; i < indexes.length; i++) {
                if (cells[indexes[i]].equals("NA")) {
                    hasMissing = true;
                    break;
                }
                geneWeight += Double.parseDouble(cells[indexes[i]]) * params[i];
            }
            if (hasMissing) {
                continue;
            }

            geneWeight = 1 / (1 + Math.exp(-geneWeight));
            // geneWeight = Math.pow(10, geneWeight);
            genePPIAtts.put(cells[0], geneWeight);
            weights.add(geneWeight);
        }
        br.close();

        weights.quickSort();
        double median = Descriptive.median(weights);
        genePPIAtts.put("A$V$G", median);

        /*       
         //* Code for paper writing
         File imgFile = new File(HTML_LOG_PATH +  "ppiweight1." + "HistPlotGeneWeight" + ".png"); 
         HistogramPainter painter = new HistogramPainter(600, 400);
         painter.drawHistogramPlot(weights, 100, null, "Cond. Prob", imgFile.getCanonicalPath());
         GlobalManager.mainView.insertImage(imgFile);
         * 
         */
        return genePPIAtts;
    }

    /**
     *
     * @param QQPlot
     * @throws Exception
     */
    public void generateGeneAssociationPValues() throws Exception {
        String prjName = GlobalManager.currentProject.getName();
        String prjWorkingPath = GlobalManager.currentProject.getWorkingPath();
        imgFolder = new File(prjWorkingPath + File.separator + prjName + File.separator + "image" + File.separator);
        if (!imgFolder.exists()) {
            imgFolder.mkdirs();
        }
        double[][] traitCorrMatrix = gbAssoc.getGenome().loadTraitCorrelationMatrixFromDisk();

        if (gbAssoc.isMultVariateTest()) {
            ScanMultivarGeneBasedAssocSwingWorker worker = new ScanMultivarGeneBasedAssocSwingWorker(traitCorrMatrix);
            buildTask = RP.create(worker); //the task is not started yet
        } else {
            ScanGeneBasedAssocSwingWorker worker = new ScanGeneBasedAssocSwingWorker();
            buildTask = RP.create(worker); //the task is not started yet
        }
        buildTask.schedule(0); //start the task
    }

    private double genePValuebySimesTest(Gene gene, DoubleArrayList pValueArray, int snpPVTypeIndex, IntArrayList blockKeySNPPositions,
            DoubleArrayList blockPValues) {
        int snpNum = gene.snps.size();
        List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();
        //calcaulte average score
        for (int k = 0; k < snpNum; k++) {
            SNP snp = gene.snps.get(k);
            double[] pValues = snp.getpValues();
            if (pValues == null) {
                continue;
            }
            if (!Double.isNaN(pValues[snpPVTypeIndex])) //
            {
                PValueWeight pv = new PValueWeight();
                pv.physicalPos = snp.physicalPosition;
                pv.pValue = pValues[snpPVTypeIndex];
                pv.weight = 1.0;
                pvalueWeightList.add(pv);
                pValueArray.add(pv.pValue);
            }
        }

        snpNum = pvalueWeightList.size();
        if (snpNum == 0) {
            blockPValues.add(1);
            return 1;
        } else if (snpNum == 1) {
            blockKeySNPPositions.add(-pvalueWeightList.get(0).physicalPos);
            blockPValues.add(pvalueWeightList.get(0).pValue);
            return pvalueWeightList.get(0).pValue;
        }

        Collections.sort(pvalueWeightList, new PValueWeightComparator());

        double pMin = pvalueWeightList.get(0).pValue * snpNum;
        double pTmp = 0;
        int keySNP = pvalueWeightList.get(0).physicalPos;
        for (int i = 1; i < snpNum; i++) {
            pTmp = snpNum * pvalueWeightList.get(i).pValue / (i + 1);
            if (pTmp < pMin) {
                keySNP = pvalueWeightList.get(i).physicalPos;
                pMin = pTmp;
            }
        }
        blockKeySNPPositions.add(-keySNP);
        blockPValues.add(pTmp);
        return pMin;
    }

    private double genePValuebySimesTest(List<PValueWeight> pvalueWeightList, IntArrayList blockKeySNPPositions, DoubleArrayList blockPValues) {
        int snpNum = pvalueWeightList.size();
        if (snpNum == 0) {
            /*
             blockKeySNPPositions.add(-1);
             blockPValues.add(1);
             */
            return Double.NaN;
        } else if (snpNum == 1) {
            blockKeySNPPositions.add(-pvalueWeightList.get(0).physicalPos);
            blockPValues.add(pvalueWeightList.get(0).pValue);
            return pvalueWeightList.get(0).pValue;
        }

        Collections.sort(pvalueWeightList, new PValueWeightComparator());

        double pMin = pvalueWeightList.get(0).pValue * snpNum;
        double pTmp = 0;
        int keySNP = pvalueWeightList.get(0).physicalPos;
        for (int i = 1; i < snpNum; i++) {
            pTmp = snpNum * pvalueWeightList.get(i).pValue / (i + 1);
            if (pTmp < pMin) {
                keySNP = pvalueWeightList.get(i).physicalPos;
                pMin = pTmp;
            }
        }
        blockKeySNPPositions.add(-keySNP);
        blockPValues.add(pTmp);
        return pMin;
    }

    //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
    // which are more powerful gene with large number of SNPs    
    private void snpSetPValueOnlybyMySimesTestBlock(List<SNP> snpList, CorrelationBasedByteLDSparseMatrix ldCorr,
            int snpPVTypeIndex, boolean needWeight, IntArrayList blockKeySNPPositions, DoubleArrayList blockPValues, DoubleArrayList keySNPPValues, DoubleArrayList blockWeights, boolean allSimesTest) throws Exception {

        int totaSNPSize = snpList.size();
        //because it is very time consumming, when SNP number is over 200. I try to spl

        int minBlockLen = 1;
        if (allSimesTest) {
            //when it use allSimesTest, too many blocks will make GATES very slow but not substantical difference to the p1-values
            minBlockLen = 5;
        }
        int maxBlockLen = 50;
        int maxCheckingNum = 5;
        int maxCheckingDistance = 10000;
        //Sometimes the SNPs with pvalues have not registration in HapMap
        int minBlockDistance = 5000;
        // check number of snps for rs genome and pos genome
        double minWthinBlockR2 = 0.25;
        double p = Double.NaN;
        int[] keySNPIndex = new int[1];
        keySNPIndex[0] = -1;
        double[] pvalueWeight = new double[2];
        if (totaSNPSize <= maxBlockLen) {
            if (needWeight) {
                p = snpSetPValuebyMyWeightedSimesTest(snpList, ldCorr, snpPVTypeIndex, keySNPIndex);
            } else {
                p = snpSetPValuebyMySimesTest(snpList, ldCorr, snpPVTypeIndex, keySNPIndex, pvalueWeight);
            }
            if (keySNPIndex[0] != -1) {
                blockKeySNPPositions.add(keySNPIndex[0]);
                blockPValues.add(p);
                keySNPPValues.add(pvalueWeight[0]);
                blockWeights.add(pvalueWeight[1]);
            }
            //System.out.println(rLDMatrix.toString());
            return;
        }

        //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
        // which are more powerful gene with large number of SNPs   
        // System.out.println(poweredCorrMat.toString()); 
        boolean allAreLess = false;
        boolean debug = false;
        int movedRow;

        int inBlockFirst = 0;
        int outBlockFirst = minBlockLen;
        if (outBlockFirst > totaSNPSize) {
            outBlockFirst = totaSNPSize;
        }
        int inBlockLast = outBlockFirst - 1;
        int checkingIndex = outBlockFirst;
        int checkingLen = 1;
        List<SNP> tmpSNPList = new ArrayList<SNP>();
        Collections.sort(snpList, new SNPPosiComparator());

        while (inBlockLast <= totaSNPSize) {
            //find a site whose LD beteen inBlockFirst start to be less than minWthinBlockR2
            while ((outBlockFirst < totaSNPSize)
                    && (outBlockFirst - inBlockFirst < maxBlockLen)
                    && (ldCorr.getLDAt(snpList.get(inBlockFirst).physicalPosition, snpList.get(outBlockFirst).physicalPosition) >= minWthinBlockR2
                    || Math.abs(snpList.get(inBlockFirst).physicalPosition - snpList.get(outBlockFirst).physicalPosition) <= minBlockDistance)) {
                outBlockFirst++;
            }
            inBlockLast = outBlockFirst - 1;

            if (outBlockFirst >= totaSNPSize) {
                if (debug) {
                    System.out.print(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(totaSNPSize - 1).physicalPosition + "\t" + (totaSNPSize - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < totaSNPSize; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                if (needWeight) {
                    p = snpSetPValuebyMyWeightedSimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex);
                } else {
                    p = snpSetPValuebyMySimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex, pvalueWeight);
                }
                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                    keySNPPValues.add(pvalueWeight[0]);
                    blockWeights.add(pvalueWeight[1]);
                }
                break;
            }

            if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition
                            + "\t" + snpList.get(inBlockLast).physicalPosition + "\t"
                            + (outBlockFirst - inBlockFirst) + "\t");
                }

                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                if (needWeight) {
                    p = snpSetPValuebyMyWeightedSimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex);
                } else {
                    p = snpSetPValuebyMySimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex, pvalueWeight);
                }
                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                    keySNPPValues.add(pvalueWeight[0]);
                    blockWeights.add(pvalueWeight[1]);
                }

                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > totaSNPSize) {
                    outBlockFirst = totaSNPSize;
                }
                inBlockLast = outBlockFirst - 1;
                continue;
            }

            movedRow = inBlockLast;
            //check LD  beteen inBlockLast and checkingIndex
            while (movedRow >= inBlockFirst) {
                allAreLess = false;
                checkingIndex = outBlockFirst;
                checkingLen = 1;

                while ((checkingIndex < totaSNPSize)
                        && (ldCorr.getLDAt(snpList.get(movedRow).physicalPosition, snpList.get(checkingIndex).physicalPosition) < minWthinBlockR2)) {

                    if ((checkingLen >= maxCheckingNum && (snpList.get(checkingIndex).physicalPosition
                            - snpList.get(movedRow).physicalPosition) >= maxCheckingDistance)) {
                        allAreLess = true;
                        break;
                    }
                    checkingLen++;
                    checkingIndex++;
                }

                if (!allAreLess) {
                    break;
                }
                movedRow--;
            }

            if (allAreLess) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(outBlockFirst).physicalPosition + "\t" + (outBlockFirst - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                if (needWeight) {
                    p = snpSetPValuebyMyWeightedSimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex);
                } else {
                    p = snpSetPValuebyMySimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex, pvalueWeight);
                }
                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                    keySNPPValues.add(pvalueWeight[0]);
                    blockWeights.add(pvalueWeight[1]);
                }
                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > totaSNPSize) {
                    outBlockFirst = totaSNPSize;
                }
                inBlockLast = outBlockFirst - 1;

            } else {
                // System.out.println(ldRsMatrix.getLDAt(snpPositionArray[startIndex], snpPositionArray[checkingIndex]) + "\t" + snpPositionArray[inBlockFirst] + "\t" + snpPositionArray[stopIndex - 1] + "\t" + (stopIndex - inBlockFirst));
                //go to the minExtendLen and re-check
                inBlockLast = checkingIndex;
                outBlockFirst = inBlockLast + 1;
                //force to cut into a block with maxBlockLen
                if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                    outBlockFirst = inBlockFirst + maxBlockLen;
                }
            }
        }

        /*
         //combine the block wise p1-values by scaled chisqure test
         int blockSize = blockKeySNPPositions.size();
         PValueWeight[] blockKeySNPs = new PValueWeight[blockSize];

         for (int t = 0; t < blockSize; t++) {
         blockKeySNPs[t] = new PValueWeight();
         blockKeySNPs[t].pValue = blockPValues.get(t);
         blockKeySNPs[t].weight = 1;
         blockKeySNPs[t].physicalPos = t;
         }
         DoubleMatrix2D rLDMatrix = new DenseDoubleMatrix2D(blockSize, blockSize);
         for (int t = 0; t < blockSize; t++) {
         rLDMatrix.setQuick(t, t, 1);
         for (int j = t + 1; j < blockSize; j++) {

         double x = ldCorr.getLDAt(blockKeySNPPositions.getQuick(t), blockKeySNPPositions.getQuick(j));
         if (x > MIN_R2) {
         // ldCorr.setLDAt(pvalueWeightList.get(t).physicalPos, pvalueWeightList.get(j).physicalPos, (float) x);
         rLDMatrix.setQuick(t, j, x);
         rLDMatrix.setQuick(j, t, x);
         } else {
         rLDMatrix.setQuick(t, j, 0);
         rLDMatrix.setQuick(j, t, 0);
         }

         }
         }
         */
    }

    //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
    // which are more powerful gene with large number of SNPs    
    private double snpSetPValuebyMySimesTestBlock(Gene gene, DoubleArrayList pValueArray, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex, boolean needWeight,
            IntArrayList blockKeySNPPositions, DoubleArrayList blockPValues, DoubleArrayList keySNPPValues, DoubleArrayList blockWeights, boolean allSimesTest, int[] keyBlockIndex) throws Exception {
        List<SNP> snpList = gene.snps;
         

        // System.out.println(gene.getSymbol() + " " + snpNum);
        //because it is very time consumming, when SNP number is over 200. I try to spl
        double p = Double.NaN;
        double maxCorr = 0.98;
        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();
        Collections.sort(snpList, new SNPPosiComparator());
        ldPruning(snpList, ldCorr, maxCorr, ignoreNoLDSNP);
        int snpNum = snpList.size();
        for (int k = 0; k < snpNum; k++) {
            SNP snp = gene.snps.get(k);
            double[] pValues = snp.getpValues();
            if (pValues == null) {
                continue;
            }
            if (!Double.isNaN(pValues[snpPVTypeIndex])) //
            {
                pValueArray.add(pValues[snpPVTypeIndex]);
            }
        }

        //because it is very time consumming, when SNP number is over 200. I try to spl
        int minBlockLen = 1;
        if (allSimesTest) {
            //when it use allSimesTest, too many blocks will make GATES very slow but not substantical difference to the p1-values
            minBlockLen = 5;
        }
        int maxBlockLen = 50;
        int maxCheckingNum = 5;
        int maxCheckingDistance = 10000;

        // check number of snps for rs genome and pos genome
        double minWthinBlockR2 = 0.25;

        int[] keySNPIndex = new int[1];
        keySNPIndex[0] = -1;
        double[] pvalueWeight = new double[2];
        if (snpNum <= maxBlockLen) {
            if (needWeight) {
                p = snpSetPValuebyMyWeightedSimesTest(snpList, ldCorr, snpPVTypeIndex, keySNPIndex);
            } else {
                p = snpSetPValuebyMySimesTest(snpList, ldCorr, snpPVTypeIndex, keySNPIndex, pvalueWeight);
            }
            if (keySNPIndex[0] != -1) {
                blockKeySNPPositions.add(keySNPIndex[0]);
                blockPValues.add(p);
                keySNPPValues.add(pvalueWeight[0]);
                blockWeights.add(pvalueWeight[1]);
            }
            //System.out.println(rLDMatrix.toString());
            return p;
        }

        //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
        // which are more powerful gene with large number of SNPs   
        // System.out.println(poweredCorrMat.toString()); 
        boolean allAreLess = false;
        boolean debug = false;
        int movedRow;

        int inBlockFirst = 0;
        int outBlockFirst = minBlockLen;
        if (outBlockFirst > snpNum) {
            outBlockFirst = snpNum;
        }
        int inBlockLast = outBlockFirst - 1;
        int checkingIndex = outBlockFirst;
        int checkingLen = 1;
        List<SNP> tmpSNPList = new ArrayList<SNP>();
        while (inBlockLast <= snpNum) {
            //find a site whose LD beteen inBlockFirst start to be less than minWthinBlockR2
            while ((outBlockFirst < snpNum)
                    && (outBlockFirst - inBlockFirst < maxBlockLen)
                    && (ldCorr.getLDAt(snpList.get(inBlockFirst).physicalPosition, snpList.get(outBlockFirst).physicalPosition) >= minWthinBlockR2)) {
                outBlockFirst++;
            }
            inBlockLast = outBlockFirst - 1;

            if (outBlockFirst >= snpNum) {
                if (debug) {
                    System.out.print(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(snpNum - 1).physicalPosition + "\t" + (snpNum - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < snpNum; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                if (needWeight) {
                    p = snpSetPValuebyMyWeightedSimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex);
                } else {
                    p = snpSetPValuebyMySimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex, pvalueWeight);
                }
                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                    keySNPPValues.add(pvalueWeight[0]);
                    blockWeights.add(pvalueWeight[1]);
                }
                break;
            }

            if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition
                            + "\t" + snpList.get(inBlockLast).physicalPosition + "\t"
                            + (outBlockFirst - inBlockFirst) + "\t");
                }

                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                if (needWeight) {
                    p = snpSetPValuebyMyWeightedSimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex);
                } else {
                    p = snpSetPValuebyMySimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex, pvalueWeight);
                }
                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                    keySNPPValues.add(pvalueWeight[0]);
                    blockWeights.add(pvalueWeight[1]);
                }

                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > snpNum) {
                    outBlockFirst = snpNum;
                }
                inBlockLast = outBlockFirst - 1;
                continue;
            }

            movedRow = inBlockLast;
            //check LD  beteen inBlockLast and checkingIndex
            while (movedRow >= inBlockFirst) {
                allAreLess = false;
                checkingIndex = outBlockFirst;
                checkingLen = 1;

                while ((checkingIndex < snpNum)
                        && (ldCorr.getLDAt(snpList.get(movedRow).physicalPosition, snpList.get(checkingIndex).physicalPosition) < minWthinBlockR2)) {

                    if ((checkingLen >= maxCheckingNum && (snpList.get(checkingIndex).physicalPosition
                            - snpList.get(movedRow).physicalPosition) >= maxCheckingDistance)) {
                        allAreLess = true;
                        break;
                    }
                    checkingLen++;
                    checkingIndex++;
                }

                if (!allAreLess) {
                    break;
                }
                movedRow--;
            }

            if (allAreLess) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(outBlockFirst).physicalPosition + "\t" + (outBlockFirst - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                if (needWeight) {
                    p = snpSetPValuebyMyWeightedSimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex);
                } else {
                    p = snpSetPValuebyMySimesTest(tmpSNPList, ldCorr, snpPVTypeIndex, keySNPIndex, pvalueWeight);
                }

                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                    keySNPPValues.add(pvalueWeight[0]);
                    blockWeights.add(pvalueWeight[1]);
                }
                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > snpNum) {
                    outBlockFirst = snpNum;
                }
                inBlockLast = outBlockFirst - 1;

            } else {
                // System.out.println(ldRsMatrix.getLDAt(snpPositionArray[startIndex], snpPositionArray[checkingIndex]) + "\t" + snpPositionArray[inBlockFirst] + "\t" + snpPositionArray[stopIndex - 1] + "\t" + (stopIndex - inBlockFirst));
                //go to the minExtendLen and re-check
                inBlockLast = checkingIndex;
                outBlockFirst = inBlockLast + 1;
                //force to cut into a block with maxBlockLen
                if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                    outBlockFirst = inBlockFirst + maxBlockLen;
                }
            }
        }

        //combine the block wise p1-values by scaled chisqure test
        int blockSize = blockKeySNPPositions.size();
        if (blockSize == 0) {
            return Double.NaN;
        }
        PValueWeight[] blockKeySNPs = new PValueWeight[blockSize];

        for (int i = 0; i < blockSize; i++) {
            blockKeySNPs[i] = new PValueWeight();
            blockKeySNPs[i].pValue = blockPValues.get(i);
            blockKeySNPs[i].weight = 1;
            blockKeySNPs[i].physicalPos = i;
        }
        /*
         if (blockSize == 112) {
         if (blockKeySNPPositions.getQuick(0) == 2158469) {
         int tt = 0;

         for (int t = 0; t < blockSize; t++) {
         for (tt = 0; tt < snpList.size(); tt++) {
         if (snpList.get(tt).physicalPosition == blockKeySNPPositions.getQuick(t)) {
         break;
         }
         }
         System.out.println(snpList.get(tt).getRsID() + "\t" + snpList.get(tt).physicalPosition + "\t" + blockKeySNPs[t].pValue);
         }
         }
         int sss = 0;

         }*/

        DoubleMatrix2D rLDMatrix = new DenseDoubleMatrix2D(blockSize, blockSize);
        for (int i = 0; i < blockSize; i++) {
            rLDMatrix.setQuick(i, i, 1);
            for (int j = i + 1; j < blockSize; j++) {

                double x = ldCorr.getLDAt(blockKeySNPPositions.getQuick(i), blockKeySNPPositions.getQuick(j));

                if (x > MIN_R2) {
                    // ldCorr.setLDAt(pvalueWeightList.get(t).physicalPos, pvalueWeightList.get(j).physicalPos, (float) x);
                    rLDMatrix.setQuick(i, j, x);
                    rLDMatrix.setQuick(j, i, x);
                } else {
                    rLDMatrix.setQuick(i, j, 0);
                    rLDMatrix.setQuick(j, i, 0);
                }

            }
        }

        /*
         if (blockSize == 112) {
         if (blockKeySNPPositions.getQuick(0) == 2158469) {
         System.out.println(rLDMatrix.toString());
         int sss = 0;
         }
         }*/
        //  p1 = MultipleTestingMethod.combinePValuebyScaleedFisherCombinationTestCovLogP(blockKeySNPs, rLDMatrix);
        //  keySNPIndex[0]=0;
        if (allSimesTest) {
            p = SetBasedTest.combineGATESPValuebyWeightedSimeCombinationTestMyMe(blockKeySNPs, rLDMatrix, keyBlockIndex);
        } else {
            p = SetBasedTest.combinePValuebyScaleedFisherCombinationTestCovLogP(blockKeySNPs, rLDMatrix);
        }

        // System.out.println(rLDMatrix.toString());
        return p;
        //Note it is usually r2, wwe have to transform it
        // return combinePValuebyVEGAS(pvalueWeightArray, subLDCorr);
    }

    //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
    // which are more powerful gene with large number of SNPs    
    private double[] snpSetPValuebyMySimesTestBlockVarWeight(Gene gene, DoubleArrayList pValueArray, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex, String chromID,
            boolean allSimesTest, int[] keyBlockIndex, Map<String, double[]> varMap, int weightNum) throws Exception {
        List<SNP> snpList = gene.snps;
        List<double[]> blockPValues = new ArrayList<double[]>();
        List<int[]> blockKeySNPPositions = new ArrayList<int[]>();
        // System.out.println(gene.getSymbol() + " " + snpNum);
        //because it is very time consumming, when SNP number is over 200. I try to spl
        double[] p;
        double maxCorr = 0.98;
        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();
        Collections.sort(snpList, new SNPPosiComparator());
        ldPruning(snpList, ldCorr, maxCorr, ignoreNoLDSNP);
        int snpNum = snpList.size();
        for (int k = 0; k < snpNum; k++) {
            SNP snp = gene.snps.get(k);
            double[] pValues = snp.getpValues();
            if (pValues == null) {
                continue;
            }
            if (!Double.isNaN(pValues[snpPVTypeIndex])) //
            {
                pValueArray.add(pValues[snpPVTypeIndex]);
            }
        }

        //because it is very time consumming, when SNP number is over 200. I try to spl
        int minBlockLen = 1;
        if (allSimesTest) {
            //when it use allSimesTest, too many blocks will make GATES very slow but not substantical difference to the p1-values
            minBlockLen = 5;
        }
        int maxBlockLen = 50;
        int maxCheckingNum = 5;
        int maxCheckingDistance = 10000;

        // check number of snps for rs genome and pos genome
        double minWthinBlockR2 = 0.25;

        int[] keySNPIndex = new int[weightNum];
        Arrays.fill(keySNPIndex, -1);

        if (snpNum <= maxBlockLen) {
            p = snpSetPValuebyMyWeightedSimesTest(snpList, chromID, varMap, weightNum, ldCorr, snpPVTypeIndex, keySNPIndex);
            int[] keySNPIndex1 = new int[weightNum];
            System.arraycopy(keySNPIndex, 0, keySNPIndex1, 0, weightNum);
            blockKeySNPPositions.add(keySNPIndex1);
            blockPValues.add(p);
            //System.out.println(rLDMatrix.toString());
            return p;
        }

        //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
        // which are more powerful gene with large number of SNPs   
        // System.out.println(poweredCorrMat.toString()); 
        boolean allAreLess = false;
        boolean debug = false;
        int movedRow;

        int inBlockFirst = 0;
        int outBlockFirst = minBlockLen;
        if (outBlockFirst > snpNum) {
            outBlockFirst = snpNum;
        }
        int inBlockLast = outBlockFirst - 1;
        int checkingIndex = outBlockFirst;
        int checkingLen = 1;
        List<SNP> tmpSNPList = new ArrayList<SNP>();
        while (inBlockLast <= snpNum) {
            //find a site whose LD beteen inBlockFirst start to be less than minWthinBlockR2
            while ((outBlockFirst < snpNum)
                    && (outBlockFirst - inBlockFirst < maxBlockLen)
                    && (ldCorr.getLDAt(snpList.get(inBlockFirst).physicalPosition, snpList.get(outBlockFirst).physicalPosition) >= minWthinBlockR2)) {
                outBlockFirst++;
            }
            inBlockLast = outBlockFirst - 1;

            if (outBlockFirst >= snpNum) {
                if (debug) {
                    System.out.print(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(snpNum - 1).physicalPosition + "\t" + (snpNum - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < snpNum; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                Arrays.fill(keySNPIndex, -1);
                p = snpSetPValuebyMyWeightedSimesTest(tmpSNPList, chromID, varMap, weightNum, ldCorr, snpPVTypeIndex, keySNPIndex);
                int[] keySNPIndex1 = new int[weightNum];
                System.arraycopy(keySNPIndex, 0, keySNPIndex1, 0, weightNum);
                blockKeySNPPositions.add(keySNPIndex1);
                blockPValues.add(p);
                break;
            }

            if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition
                            + "\t" + snpList.get(inBlockLast).physicalPosition + "\t"
                            + (outBlockFirst - inBlockFirst) + "\t");
                }

                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                Arrays.fill(keySNPIndex, -1);
                p = snpSetPValuebyMyWeightedSimesTest(tmpSNPList, chromID, varMap, weightNum, ldCorr, snpPVTypeIndex, keySNPIndex);
                int[] keySNPIndex1 = new int[weightNum];
                System.arraycopy(keySNPIndex, 0, keySNPIndex1, 0, weightNum);
                blockKeySNPPositions.add(keySNPIndex1);
                blockPValues.add(p);

                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > snpNum) {
                    outBlockFirst = snpNum;
                }
                inBlockLast = outBlockFirst - 1;
                continue;
            }

            movedRow = inBlockLast;
            //check LD  beteen inBlockLast and checkingIndex
            while (movedRow >= inBlockFirst) {
                allAreLess = false;
                checkingIndex = outBlockFirst;
                checkingLen = 1;

                while ((checkingIndex < snpNum)
                        && (ldCorr.getLDAt(snpList.get(movedRow).physicalPosition, snpList.get(checkingIndex).physicalPosition) < minWthinBlockR2)) {

                    if ((checkingLen >= maxCheckingNum && (snpList.get(checkingIndex).physicalPosition
                            - snpList.get(movedRow).physicalPosition) >= maxCheckingDistance)) {
                        allAreLess = true;
                        break;
                    }
                    checkingLen++;
                    checkingIndex++;
                }

                if (!allAreLess) {
                    break;
                }
                movedRow--;
            }

            if (allAreLess) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(outBlockFirst).physicalPosition + "\t" + (outBlockFirst - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                Arrays.fill(keySNPIndex, -1);
                p = snpSetPValuebyMyWeightedSimesTest(tmpSNPList, chromID, varMap, weightNum, ldCorr, snpPVTypeIndex, keySNPIndex);
                int[] keySNPIndex1 = new int[weightNum];
                System.arraycopy(keySNPIndex, 0, keySNPIndex1, 0, weightNum);
                blockKeySNPPositions.add(keySNPIndex1);
                blockPValues.add(p);
                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > snpNum) {
                    outBlockFirst = snpNum;
                }
                inBlockLast = outBlockFirst - 1;

            } else {
                // System.out.println(ldRsMatrix.getLDAt(snpPositionArray[startIndex], snpPositionArray[checkingIndex]) + "\t" + snpPositionArray[inBlockFirst] + "\t" + snpPositionArray[stopIndex - 1] + "\t" + (stopIndex - inBlockFirst));
                //go to the minExtendLen and re-check
                inBlockLast = checkingIndex;
                outBlockFirst = inBlockLast + 1;
                //force to cut into a block with maxBlockLen
                if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                    outBlockFirst = inBlockFirst + maxBlockLen;
                }
            }
        }

        double[] genePValues = new double[weightNum];
        Arrays.fill(genePValues, Double.NaN);
        for (int t = 0; t < weightNum; t++) {
            //combine the block wise p1-values by scaled chisqure test
            int blockSize = blockKeySNPPositions.size();
            if (blockSize == 0) {
                return genePValues;
            }

            PValueWeight[] blockKeySNPs = new PValueWeight[blockSize];

            for (int i = 0; i < blockSize; i++) {
                blockKeySNPs[i] = new PValueWeight();
                blockKeySNPs[i].pValue = blockPValues.get(i)[t];
                blockKeySNPs[i].weight = 1;
                blockKeySNPs[i].physicalPos = i;
            }
            DoubleMatrix2D rLDMatrix = new DenseDoubleMatrix2D(blockSize, blockSize);
            for (int i = 0; i < blockSize; i++) {
                rLDMatrix.setQuick(i, i, 1);
                for (int j = i + 1; j < blockSize; j++) {
                    double x = ldCorr.getLDAt(blockKeySNPPositions.get(i)[t], blockKeySNPPositions.get(j)[t]);
                    if (x > MIN_R2) {
                        // ldCorr.setLDAt(pvalueWeightList.get(t).physicalPos, pvalueWeightList.get(j).physicalPos, (float) x);
                        rLDMatrix.setQuick(i, j, x);
                        rLDMatrix.setQuick(j, i, x);
                    } else {
                        rLDMatrix.setQuick(i, j, 0);
                        rLDMatrix.setQuick(j, i, 0);
                    }

                }
            }
            double p1 = Double.NaN;
            if (allSimesTest) {
                p1 = SetBasedTest.combineGATESPValuebyWeightedSimeCombinationTestMyMe(blockKeySNPs, rLDMatrix, keyBlockIndex);
            } else {
                p1 = SetBasedTest.combinePValuebyScaleedFisherCombinationTestCovLogP(blockKeySNPs, rLDMatrix);
            }
            genePValues[t] = p1;
        }
        return genePValues;
        //Note it is usually r2, wwe have to transform it
        // return combinePValuebyVEGAS(pvalueWeightArray, subLDCorr);
    }

    //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
    // which are more powerful gene with large number of SNPs    
    private double snpSetPValuebyMyScaledChisquareTestBlock1(Gene gene, DoubleArrayList pValueArray, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
            boolean needWeight, IntArrayList blockKeySNPPositions, DoubleArrayList blockPValues, boolean allSimesTest, RConnection rconn) throws Exception {

        List<SNP> snpList = gene.snps;
        int totaSNPSize = snpList.size();

        int snpNum = gene.snps.size();

        //remove redundant SNP
        //System.out.println(gene.getSymbol() + " " + snpNum);
        //because it is very time consumming, when SNP number is over 200. I try to spl
        double p = Double.NaN;

        for (int k = 0; k < snpNum; k++) {
            SNP snp = gene.snps.get(k);
            double[] pValues = snp.getpValues();
            if (pValues == null) {
                continue;
            }
            if (!Double.isNaN(pValues[snpPVTypeIndex])) //
            {
                pValueArray.add(pValues[snpPVTypeIndex]);
            }
        }
        double[] tmpResults = new double[2];
        double[] totalResults = new double[2];
        Arrays.fill(totalResults, 0);
        //because it is very time consumming, when SNP number is over 200. I try to spl
        int minBlockLen = 1;
        if (allSimesTest) {
            //when it use allSimesTest, too many blocks will make GATES very slow but not substantical difference to the p1-values
            minBlockLen = 5;
        }
        int maxBlockLen = 200;
        int maxCheckingNum = 50;
        int maxCheckingDistance = 100000;

        // check number of snps for rs genome and pos genome
        double minWthinBlockR2 = 0.1;

        boolean noRC = true;
        if (rconn != null) {
            noRC = false;
        }
        int[] keySNPIndex = new int[1];
        keySNPIndex[0] = -1;
        if (totaSNPSize <= maxBlockLen) {
            if (needWeight) {
                if (noRC) {
                    p = snpSetPValuebyMyChiSquareApproxEJML(snpList, ldCorr, snpPVTypeIndex, false, tmpResults);
                } else {
                    p = snpSetPValuebyMyChiSquareApproxNnls(snpList, ldCorr, snpPVTypeIndex, false, tmpResults, rconn);
                }
            } else if (noRC) {
                p = snpSetPValuebyMyChiSquareApproxEJML(snpList, ldCorr, snpPVTypeIndex, false, tmpResults);
            } else {
                p = snpSetPValuebyMyChiSquareApproxNnls(snpList, ldCorr, snpPVTypeIndex, false, tmpResults, rconn);
            }

            totalResults[0] += tmpResults[0];
            totalResults[1] += tmpResults[1];

            if (keySNPIndex[0] != -1) {
                blockKeySNPPositions.add(keySNPIndex[0]);
                blockPValues.add(p);
            }
            //System.out.println(rLDMatrix.toString());
            return p;
        }

        //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
        // which are more powerful gene with large number of SNPs   
        // System.out.println(poweredCorrMat.toString()); 
        boolean allAreLess = false;
        boolean debug = false;
        int movedRow;

        int inBlockFirst = 0;
        int outBlockFirst = minBlockLen;
        if (outBlockFirst > totaSNPSize) {
            outBlockFirst = totaSNPSize;
        }
        int inBlockLast = outBlockFirst - 1;
        int checkingIndex = outBlockFirst;
        int checkingLen = 1;
        List<SNP> tmpSNPList = new ArrayList<SNP>();
        Collections.sort(snpList, new SNPPosiComparator());

        while (inBlockLast <= totaSNPSize) {
            //find a site whose LD beteen inBlockFirst start to be less than minWthinBlockR2
            while ((outBlockFirst < totaSNPSize)
                    && (outBlockFirst - inBlockFirst < maxBlockLen)
                    && (ldCorr.getLDAt(snpList.get(inBlockFirst).physicalPosition, snpList.get(outBlockFirst).physicalPosition) >= minWthinBlockR2)) {
                outBlockFirst++;
            }
            inBlockLast = outBlockFirst - 1;

            if (outBlockFirst >= totaSNPSize) {
                if (debug) {
                    System.out.print(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(totaSNPSize - 1).physicalPosition + "\t" + (totaSNPSize - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < totaSNPSize; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                if (noRC) {
                    if (needWeight) {
                        p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);
                    } else {
                        p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);
                    }
                } else {
                    p = snpSetPValuebyMyChiSquareApproxNnls(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults, rconn);
                }

                totalResults[0] += tmpResults[0];
                totalResults[1] += tmpResults[1];

                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                }
                break;
            }

            if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition
                            + "\t" + snpList.get(inBlockLast).physicalPosition + "\t"
                            + (outBlockFirst - inBlockFirst) + "\t");
                }

                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                if (noRC) {
                    if (needWeight) {
                        p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);
                    } else {
                        p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);
                    }
                } else {
                    p = snpSetPValuebyMyChiSquareApproxNnls(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults, rconn);
                }

                totalResults[0] += tmpResults[0];
                totalResults[1] += tmpResults[1];

                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                }

                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > totaSNPSize) {
                    outBlockFirst = totaSNPSize;
                }
                inBlockLast = outBlockFirst - 1;
                continue;
            }

            movedRow = inBlockLast;
            //check LD  beteen inBlockLast and checkingIndex
            while (movedRow >= inBlockFirst) {
                allAreLess = false;
                checkingIndex = outBlockFirst;
                checkingLen = 1;

                while ((checkingIndex < totaSNPSize)
                        && (ldCorr.getLDAt(snpList.get(movedRow).physicalPosition, snpList.get(checkingIndex).physicalPosition) < minWthinBlockR2)) {

                    if ((checkingLen >= maxCheckingNum && (snpList.get(checkingIndex).physicalPosition
                            - snpList.get(movedRow).physicalPosition) >= maxCheckingDistance)) {
                        allAreLess = true;
                        break;
                    }
                    checkingLen++;
                    checkingIndex++;
                }

                if (!allAreLess) {
                    break;
                }
                movedRow--;
            }

            if (allAreLess) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(outBlockFirst).physicalPosition + "\t" + (outBlockFirst - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                if (noRC) {
                    if (needWeight) {
                        p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);
                    } else {
                        p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);
                    }
                } else {
                    p = snpSetPValuebyMyChiSquareApproxNnls(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults, rconn);
                }

                totalResults[0] += tmpResults[0];
                totalResults[1] += tmpResults[1];

                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                }
                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > totaSNPSize) {
                    outBlockFirst = totaSNPSize;
                }
                inBlockLast = outBlockFirst - 1;

            } else {
                // System.out.println(ldRsMatrix.getLDAt(snpPositionArray[startIndex], snpPositionArray[checkingIndex]) + "\t" + snpPositionArray[inBlockFirst] + "\t" + snpPositionArray[stopIndex - 1] + "\t" + (stopIndex - inBlockFirst));
                //go to the minExtendLen and re-check
                inBlockLast = checkingIndex;
                outBlockFirst = inBlockLast + 1;
                //force to cut into a block with maxBlockLen
                if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                    outBlockFirst = inBlockFirst + maxBlockLen;
                }
            }
        }

        p = Gamma.incompleteGammaComplement(totalResults[0] / 2, totalResults[1] / 2);
        if (p < 1E-8) {
            int sss = 0;
        }

        //  p1 = MultipleTestingMethod.combinePValuebyScaleedFisherCombinationTestCovLogP(blockKeySNPs, rLDMatrix);
        //  keySNPIndex[0]=0;
        if (allSimesTest) {
            //    p = SetBasedTest.combineGATESPValuebyWeightedSimeCombinationTestMyMe(blockKeySNPs, rLDMatrix);
        } else {
            //  p = SetBasedTest.combinePValuebyScaleedFisherCombinationTestCovLogP(blockKeySNPs, rLDMatrix);
        }

        // System.out.println(rLDMatrix.toString());
        return p;
        //Note it is usually r2, wwe have to transform it
        // return combinePValuebyVEGAS(pvalueWeightArray, subLDCorr);
    }

    public String ldPruning(List<SNP> mainSnpMap, CorrelationBasedByteLDSparseMatrix ldCorr, double maxCorr, boolean ingoreNOGty) throws Exception {
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
                     r = Math.sqrt(ldRMatrix.getQuick(i, j));
                     //for R
                     c = (0.6065 * r - 1.033) * r + 1.7351;
                     if (c > 2) {
                     c = 2;
                     }
                     */

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

    public double snpSetPValuebyMyScaledChisquareTestBlock(List<SNP> snpList, DoubleArrayList pValueArray, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
            IntArrayList blockKeySNPPositions, DoubleArrayList blockPValues, RConnection rconn, double[] totalResults) throws Exception {

        //remove redundant SNP
        boolean ignoreNoLDSNP = (gbAssoc == null ? true : gbAssoc.isIgnoreNoLDSNP());
        //remove redundant SNPs according to LD

        double maxCorr = 0.98;
        Collections.sort(snpList, new SNPPosiComparator());
        ldPruning(snpList, ldCorr, maxCorr, ignoreNoLDSNP);

        //because it is very time consumming, when SNP number is over 200. I try to spl
        double p = Double.NaN;
        int totaSNPSize = snpList.size();
        //System.out.println(gene.getSymbol() + " " + snpNum);
        for (int k = 0; k < totaSNPSize; k++) {
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

        double[] tmpResults = new double[2];

        Arrays.fill(totalResults, 0);
        //because it is very time consumming, when SNP number is over 200. I try to spl
        int minBlockLen = 5;

        int maxBlockLen = 1000;
        int maxCheckingNum = 100;
        int maxCheckingDistance = 2000000;

        // check number of snps for rs genome and pos genome
        double minWthinBlockR2 = 0.1;

        if (rconn != null) {
            // noRC = false;
        }
        totalResults[0] = 0;
        totalResults[1] = 0;

        int[] keySNPIndex = new int[1];
        keySNPIndex[0] = -1;
        if (totaSNPSize <= maxBlockLen) {
            p = snpSetPValuebyMyChiSquareApproxEJML(snpList, ldCorr, snpPVTypeIndex, false, tmpResults);
            totalResults[0] += tmpResults[0];
            totalResults[1] += tmpResults[1];

            if (keySNPIndex[0] != -1) {
                blockKeySNPPositions.add(keySNPIndex[0]);
                blockPValues.add(p);
            }
            //System.out.println(rLDMatrix.toString());
            return p;
        }

        //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
        // which are more powerful gene with large number of SNPs   
        // System.out.println(poweredCorrMat.toString()); 
        boolean allAreLess = false;
        boolean debug = false;
        int movedRow;

        int inBlockFirst = 0;
        int outBlockFirst = minBlockLen;
        if (outBlockFirst > totaSNPSize) {
            outBlockFirst = totaSNPSize;
        }
        int inBlockLast = outBlockFirst - 1;
        int checkingIndex = outBlockFirst;
        int checkingLen = 1;
        List<SNP> tmpSNPList = new ArrayList<SNP>();

        while (inBlockLast <= totaSNPSize) {
            //find a site whose LD beteen inBlockFirst start to be less than minWthinBlockR2
            while ((outBlockFirst < totaSNPSize)
                    && (outBlockFirst - inBlockFirst < maxBlockLen)
                    && (ldCorr.getLDAt(snpList.get(inBlockFirst).physicalPosition, snpList.get(outBlockFirst).physicalPosition) >= minWthinBlockR2)) {
                outBlockFirst++;
            }
            inBlockLast = outBlockFirst - 1;

            if (outBlockFirst >= totaSNPSize) {
                if (debug) {
                    System.out.print(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(totaSNPSize - 1).physicalPosition + "\t" + (totaSNPSize - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < totaSNPSize; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);

                totalResults[0] += tmpResults[0];
                totalResults[1] += tmpResults[1];

                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                }
                break;
            }

            if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition
                            + "\t" + snpList.get(inBlockLast).physicalPosition + "\t"
                            + (outBlockFirst - inBlockFirst) + "\t");
                }

                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);
                totalResults[0] += tmpResults[0];
                totalResults[1] += tmpResults[1];

                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                }

                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > totaSNPSize) {
                    outBlockFirst = totaSNPSize;
                }
                inBlockLast = outBlockFirst - 1;
                continue;
            }

            movedRow = inBlockLast;
            //check LD  beteen inBlockLast and checkingIndex
            while (movedRow >= inBlockFirst) {
                allAreLess = false;
                checkingIndex = outBlockFirst;
                checkingLen = 1;

                while ((checkingIndex < totaSNPSize)
                        && (ldCorr.getLDAt(snpList.get(movedRow).physicalPosition, snpList.get(checkingIndex).physicalPosition) < minWthinBlockR2)) {
                    if ((checkingLen >= maxCheckingNum && (snpList.get(checkingIndex).physicalPosition
                            - snpList.get(movedRow).physicalPosition) >= maxCheckingDistance)) {
                        allAreLess = true;
                        break;
                    }
                    checkingLen++;
                    checkingIndex++;
                }

                if (!allAreLess) {
                    break;
                }
                movedRow--;
            }

            if (allAreLess) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(outBlockFirst).physicalPosition + "\t" + (outBlockFirst - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);

                totalResults[0] += tmpResults[0];
                totalResults[1] += tmpResults[1];

                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                }
                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > totaSNPSize) {
                    outBlockFirst = totaSNPSize;
                }
                inBlockLast = outBlockFirst - 1;

            } else {
                // System.out.println(ldRsMatrix.getLDAt(snpPositionArray[startIndex], snpPositionArray[checkingIndex]) + "\t" + snpPositionArray[inBlockFirst] + "\t" + snpPositionArray[stopIndex - 1] + "\t" + (stopIndex - inBlockFirst));
                //go to the minExtendLen and re-check
                inBlockLast = checkingIndex;
                outBlockFirst = inBlockLast + 1;
                //force to cut into a block with maxBlockLen
                if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                    outBlockFirst = inBlockFirst + maxBlockLen;
                }
            }
        }

        p = Gamma.incompleteGammaComplement(totalResults[0] / 2, totalResults[1] / 2);
        return p;
        //Note it is usually r2, wwe have to transform it
        // return combinePValuebyVEGAS(pvalueWeightArray, subLDCorr);
    }

    //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
    // which are more powerful gene with large number of SNPs    
    private double snpSetPValuebyOurWeightedScaledChiSquare(Gene gene, DoubleArrayList pValueArray, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
            boolean needWeight, IntArrayList blockKeySNPPositions, DoubleArrayList blockPValues) throws Exception {
        List<SNP> snpList = gene.snps;
        int snpNum = gene.snps.size();
        // System.out.println(gene.getSymbol() + " " + snpNum);
        //because it is very time consumming, when SNP number is over 200. I try to spl

        double p = Double.NaN;
        if (gene.getSymbol().equals("SPEF2")) {
            int sss = 0;
        }
        for (int k = 0; k < snpNum; k++) {
            SNP snp = gene.snps.get(k);
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

        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();
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

        double df = 0;
        double Y = 0;
        double allWeight = 0;
        for (int i = 0; i < snpNum; i++) {
            PValueWeight pv = pvalueWeightList.get(i);
            //the  pv.pValue is actually a chi-square
            Y += (pv.weight * pv.chiSquare);
            allWeight += pv.weight;
        }

        df = 0;
        for (int i = 0; i < snpNum; i++) {
            PValueWeight pv = pvalueWeightList.get(i);
            df += (pv.weight * pv.weight);
            for (int j = i + 1; j < snpNum; j++) {
                df += (2 * pv.weight * pvalueWeightList.get(i).weight * ldCorr.getLDAt(pvalueWeightList.get(i).physicalPos, pvalueWeightList.get(j).physicalPos));
            }
        }
        Y /= df;
        Y *= allWeight;
        df = allWeight * allWeight / df;

        //calcualte the scalled chi 
        p = Probability.chiSquareComplemented(df, Y);
        if (p < 0.0) {
            p = 0.0;
        }
        return p;
    }

    //user a colt matrix function
    //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
    // which are more powerful gene with large number of SNPs    
    private double snpSetPValuebyMyScaledChiSquare(Gene gene, DoubleArrayList pValueArray, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
            boolean needWeight, IntArrayList blockKeySNPPositions, DoubleArrayList blockPValues) throws Exception {
        List<SNP> snpList = gene.snps;
        int snpNum = gene.snps.size();
        if (gene.getSymbol().equals("CSMD1")) {
            return 1;
        }
        System.out.println(gene.getSymbol() + " " + snpNum);
        //because it is very time consumming, when SNP number is over 200. I try to spl

        double p1 = Double.NaN;

        for (int k = 0; k < snpNum; k++) {
            SNP snp = gene.snps.get(k);
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

        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();

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
        Algebra alg = new Algebra();
        //0.75 * 
        for (int i = 0; i < snpNum; i++) {
            PValueWeight pv = pvalueWeightList.get(i);
            ldRMatrix.setQuick(i, i, pv.weight * pv.weight);
            for (int j = i + 1; j < snpNum; j++) {
                //Math.sqrt(pValueArray[t].var * pValueArray[j].var) 
                ldRMatrix.setQuick(i, j, pv.weight * pvalueWeightList.get(j).weight * ldCorr.getLDAt(pv.physicalPos, pvalueWeightList.get(j).physicalPos));
                if (ldRMatrix.getQuick(i, j) > 0) {
                    ldRMatrix.setQuick(i, j, Math.pow(ldRMatrix.getQuick(i, j), 0.75));
                } else {
                    ldRMatrix.setQuick(i, j, 0);
                }
                ldRMatrix.setQuick(j, i, ldRMatrix.getQuick(i, j));
            }
        }
        if (gene.getSymbol().equals("KCNJ4")) {
            int sss = 0;
            //  System.out.println(ldRMatrix.toString());
        }
//remove redundant SNPs according to LD
        int originalSampleSize = snpNum;
        Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
        double maxCorr = 0.98;
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
        double[] pValues = new double[snpNum];
        for (int k = 0; k < snpNum; k++) {
            pValues[k] = pvalueWeightList.get(k).chiSquare;
        }
        p1 = SetBasedTest.mySudoSVDSolverColt(ldRMatrix, pValues);

        if (p1 < 1E-8) {
            // System.out.println(ldRMatrix.toString());
            SingularValueDecomposition s = new SingularValueDecomposition(ldRMatrix);
            DoubleMatrix2D U = s.getU();
            DoubleMatrix2D S = s.getS();
            DoubleMatrix2D V = s.getV();
            System.out.println(U.toString());
            System.out.println(S.toString());
            System.out.println(V.toString());

        }

        return p1;
    }

    //use a faster matrix invers function DenseMatrix64F
    private double snpSetPValuebyMyChiSquareApproxEJML(Gene gene, DoubleArrayList pValueArray, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
            boolean needWeight, IntArrayList blockKeySNPPositions, DoubleArrayList blockPValues) throws Exception {
        List<SNP> snpList = gene.snps;
        int snpNum = gene.snps.size();

        //because it is very time consumming, when SNP number is over 200. I try to spl
        double p1 = Double.NaN;
        //CALB2
        if (!gene.getSymbol().equals("CSMD1")) {
            int sss = 0;
            // return 1;
        }
        //  System.out.println(gene.getSymbol());
        for (int k = 0; k < snpNum; k++) {
            SNP snp = gene.snps.get(k);
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

        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();

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
                    ldRMatrix.setQuick(i, j, Math.pow(ldRMatrix.getQuick(i, j), 0.75));
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

        /*
         BufferedWriter bw = new BufferedWriter(new FileWriter("corr.txt", true));
         System.out.println(ldRMatrix.toString());
         bw.write(ldRMatrix.toString());
         bw.close();
         */
        if (snpNum == 1) {
            keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
            return pvalueWeightList.get(0).pValue;
        }
        //System.out.println(gene.getSymbol() + " " + snpNum);

        double df = 0;
        double Y = 0;

        double threshold = 0.2;
        double scale = 10;
        double pairThreshold = 0.5;
        int maxBlockSize = 10;
        List<IntArrayList> blockCluster = null;
        do {
            blockCluster = new MarixDensity().getCluster(ldRMatrix, threshold, maxBlockSize, scale, pairThreshold);

            if (snpNum < 500) {
                break;
            } else if (blockCluster.size() > 2) {
                break;
            } else {
                threshold += 0.05;
                //pairThreshold += 0.05;
            }
        } while (threshold < 0.8);

        double dft = 0;
        double Yt = 0;
        for (int t = 0; t < blockCluster.size(); t++) {
            IntArrayList items = blockCluster.get(t);
            snpNum = items.size();

            DenseMatrix64F A = new DenseMatrix64F(snpNum, snpNum);
            DenseMatrix64F x1 = new DenseMatrix64F(snpNum, 1);
            DenseMatrix64F b1 = new DenseMatrix64F(snpNum, 1);
            for (int i = 0; i < snpNum; i++) {
                A.set(i, i, 1);
                for (int j = i + 1; j < snpNum; j++) {
                    A.set(i, j, ldRMatrix.getQuick(items.getQuick(i), items.getQuick(j)));
                    A.set(j, i, ldRMatrix.getQuick(i, j));
                }
            }

            for (int k = 0; k < snpNum; k++) {
                b1.set(k, 0, pvalueWeightList.get(items.getQuick(k)).chiSquare);
            }

            DenseMatrix64F x2 = new DenseMatrix64F(snpNum, 1);
            DenseMatrix64F b2 = new DenseMatrix64F(snpNum, 1);
            for (int i = 0; i < snpNum; i++) {
                b2.set(i, 0, 1);
            }
            SetBasedTest.mySudoSVDSolverEJML(A, b1, b2, x1, x2);
            df = 0;
            Y = 0;
            for (int i = 0; i < snpNum; i++) {
                PValueWeight pv = pvalueWeightList.get(items.getQuick(i));
                Y += (pv.weight * x1.get(i, 0));
                //  Y1 += chisquareArray[t][0];
                //   pValueArray[t].var *
                df += (pv.weight * x2.get(i, 0));
            }
            if (Y < 0) {
                Y = 0;
            }
            if (df < 1) {
                df = 1;
            }
            dft += df;
            Yt += Y;
        }

        p1 = Gamma.incompleteGammaComplement(dft / 2, Yt / 2);

        return p1;
    }

    //use a faster matrix invers function DenseMatrix64F
    private double snpSetPValuebyMyChiSquareApproxEJML(List<SNP> snpList, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
            boolean needWeight, double[] results) throws Exception {
        int snpNum = snpList.size();
        // System.out.println("Size " + snpNum);
        //because it is very time consumming, when SNP number is over 200. I try to spl

        double p1 = Double.NaN;

        int[] keySNPPosition = new int[1];
        keySNPPosition[0] = -1;
        List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();

        boolean ignoreNoLDSNP = (gbAssoc == null ? true : gbAssoc.isIgnoreNoLDSNP());

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
            results[0] = 0;
            results[1] = 0;
            return 1;
        } else if (snpNum == 1) {
            keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
            results[0] = 1;
            results[1] = pvalueWeightList.get(0).chiSquare;
            return pvalueWeightList.get(0).pValue;
        }

        DoubleMatrix2D ldRMatrix = new DenseDoubleMatrix2D(snpNum, snpNum);
        double r, c = 0;
        for (int i = 0; i < snpNum; i++) {
            PValueWeight pv = pvalueWeightList.get(i);
            ldRMatrix.setQuick(i, i, pv.weight * pv.weight);
            for (int j = i + 1; j < snpNum; j++) {
                //Math.sqrt(pValueArray[t].var * pValueArray[j].var) 
                ldRMatrix.setQuick(i, j, pv.weight * pvalueWeightList.get(j).weight * ldCorr.getLDAt(pv.physicalPos, pvalueWeightList.get(j).physicalPos));
                if (ldRMatrix.getQuick(i, j) > 0) {
                    r = ldRMatrix.getQuick(i, j);
                    /*
                     r = Math.sqrt(r);
                     //for R
                     c = (0.6065 * r - 1.033) * r + 1.7351;
                     if (c > 2) {
                     c = 2;
                     }
                     */

                    //R2
                    //y = 41.773x6 - 128.05x5 + 151.17x4 - 86.321x3 + 24.67x2 - 3.3787x + 0.9174
                    //  c = (((((41.773 * r - 128.05) * r + 151.17) * r - 86.321) * r + 24.67) * r - 3.3787) * r + 0.9174;
                    // c = (((((-35.741 * r + 111.16) * r - 128.42) * r + 66.906) * r - 14.641) * r + 0.6075) * r + 0.8596;
                    //y = 0.2725x2 - 0.3759x + 0.8508
                    //c = (0.2725 * r - 0.3759) * r + 0.8508;
                    // y = 0.2814x2 - 0.4308x + 0.86
                    //c = (0.2814 * r - 0.4308) * r + 0.86;
                    //y = -0.155x + 0.8172
                    //c = -0.155 * r + 0.8172;
                    //y = 0.6982x-0.042
                    //c = 0.6982 * Math.pow(r, -0.042);
                    //c = 0.85;
                    //r = Math.pow(r, c); 
                } else {
                    r = 0;
                }
                ldRMatrix.setQuick(i, j, r);
                ldRMatrix.setQuick(j, i, r);
            }
        }

        //remove redundant SNPs according to LD
        int originalSampleSize = snpNum;
        Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
        double maxCorr = 0.98;
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
            results[0] = 1;
            results[1] = pvalueWeightList.get(0).chiSquare;
            return pvalueWeightList.get(0).pValue;
        }

        double df = 0;
        double Y = 0;
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

        /*
         DenseMatrix64F x1 = new DenseMatrix64F(snpNum, 1);
         DenseMatrix64F x2 = new DenseMatrix64F(snpNum, 1);
         SetBasedTest.mySudoSVDSolverEJML(A, b1, b2, x1, x2);
         // SetBasedTest.myInverseUJMP(A, b1, b2, x1, x2);
         df = 0;
         Y = 0;
         for (int i = 0; i < snpNum; i++) {
         PValueWeight pv = pvalueWeightList.get(i);
         Y += (pv.weight * x1.get(i, 0));
         //  Y1 += chisquareArray[t][0];
         //   pValueArray[t].var *
         df += (pv.weight * x2.get(i, 0));
         }
         if (Y <= 0) {
         Y = 0;
         }
         if (df <= 0) {
         df = 0.1;
         }

         results[0] = df;
         results[1] = Y;
         */
        sbt.mySudoSVDSolverOverlappedWindow(A, b1, b2, results);
        //It has some inflation at the tail
        //SetBasedTest.combinePValuebyCorrectedChiFisherCombinationTestMXLiS(A, b1, results);

        p1 = Gamma.incompleteGammaComplement(results[0] / 2, results[1] / 2);
        return p1;
    }

    //use a faster matrix invers function DenseMatrix64F
    private double snpSetPValuebyMyChiSquareApproxNnls(List<SNP> snpList, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
            boolean needWeight, double[] results, RConnection rconn) throws Exception {
        int snpNum = snpList.size();
        // System.out.println(gene.getSymbol() + " " + snpNum);
        //because it is very time consumming, when SNP number is over 200. I try to spl

        double p1 = Double.NaN;

        int[] keySNPPosition = new int[1];
        keySNPPosition[0] = -1;
        List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();

        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();

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
            results[0] = 0;
            results[1] = 0;
            return 1;
        } else if (snpNum == 1) {
            keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
            results[0] = 1;
            results[1] = pvalueWeightList.get(0).chiSquare;
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
                    ldRMatrix.setQuick(i, j, Math.pow(ldRMatrix.getQuick(i, j), 0.75));
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
            results[0] = 1;
            results[1] = pvalueWeightList.get(0).chiSquare;
            return pvalueWeightList.get(0).pValue;
        }

        double df = 0;
        double Y = 0;
        double[] corMatrix = new double[snpNum * snpNum];
        double[] b1 = new double[snpNum];

        for (int i = 0; i < snpNum; i++) {
            for (int j = i; j < snpNum; j++) {
                corMatrix[i * snpNum + j] = ldRMatrix.getQuick(i, j);
            }
        }
        rconn.assign("corMatrix", corMatrix);
        rconn.voidEval("corMatrix<-matrix(corMatrix,nrow=" + snpNum + ",ncol=" + snpNum + ", byrow = TRUE)");

        for (int k = 0; k < snpNum; k++) {
            b1[k] = pvalueWeightList.get(k).chiSquare;
        }
        rconn.assign("b1", b1);
        double[] x1 = rconn.eval("nnls(corMatrix, b1)$x").asDoubles();//This step will cost a lot of time. 

        double[] b2 = new double[snpNum];
        Arrays.fill(b2, 1);
        rconn.assign("b2", b2);

        double[] x2 = rconn.eval("nnls(corMatrix, b2)$x").asDoubles();//This step will cost a lot of time. 

        df = 0;
        Y = 0;
        for (int i = 0; i < snpNum; i++) {
            PValueWeight pv = pvalueWeightList.get(i);
            Y += (pv.weight * x1[i]);
            //  Y1 += chisquareArray[t][0];
            //   pValueArray[t].var *
            df += (pv.weight * x2[i]);
        }
        if (Y <= 0) {
            Y = 0;
        }
        if (df <= 0) {
            df = 0;
        }

        results[0] = df;
        results[1] = Y;
        p1 = Gamma.incompleteGammaComplement(df / 2, Y / 2);
        if (p1 < 1E-8) {
            int sss = 0;
        }

        return p1;
    }

    //use a faster matrix invers function DenseMatrix64F
    private double snpSetPValuebyJohnnyChiSquare(Gene gene, DoubleArrayList pValueArray, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
            boolean needWeight, IntArrayList blockKeySNPPositions, DoubleArrayList blockPValues, RConnection rcon) throws Exception {
        List<SNP> snpList = gene.snps;
        int snpNum = gene.snps.size();
        // System.out.println(gene.getSymbol() + " " + snpNum);
        //because it is very time consumming, when SNP number is over 200. I try to spl

        double p1 = Double.NaN;

        for (int k = 0; k < snpNum; k++) {
            SNP snp = gene.snps.get(k);
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

        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();

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
                weights[i] = 0;
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

    //this function is only suitable for gene with smaill number of SNPs in terms of computational time and power
    private double snpSetPValuebyMySimesTest(List<SNP> snpList, CorrelationBasedByteLDSparseMatrix ldCorr,
            int snpPVTypeIndex, int[] keySNPPosition, double[] pvalueWeight) throws Exception {
        List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();
        int snpNum = snpList.size();
        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();
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
                pv.weight = 1.0;
                pvalueWeightList.add(pv);
            }
        }
        snpNum = pvalueWeightList.size();

        if (snpNum == 0) {
            keySNPPosition[0] = -1;
            pvalueWeight[0] = Double.NaN;
            pvalueWeight[1] = Double.NaN;
            return Double.NaN;
        } else if (snpNum == 1) {
            keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
            pvalueWeight[0] = pvalueWeightList.get(0).pValue;
            pvalueWeight[1] = 1;
            return pvalueWeightList.get(0).pValue;
        }
        DoubleMatrix2D pCovMatrix = new DenseDoubleMatrix2D(snpNum, snpNum);
        Collections.sort(pvalueWeightList, new PValueWeightComparator());
        for (int i = 0; i < snpNum; i++) {
            pCovMatrix.setQuick(i, i, 1);
            for (int j = i + 1; j < snpNum; j++) {

                double x = ldCorr.getLDAt(pvalueWeightList.get(i).physicalPos, pvalueWeightList.get(j).physicalPos);
                //when r2
                //y = 0.7723x6 - 1.5659x5 + 1.201x4 - 0.2355x3 + 0.2184x2 + 0.6086x
                x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;

                if (x > MIN_R2) {
                    // ldCorr.setLDAt(pvalueWeightList.get(t).physicalPos, pvalueWeightList.get(j).physicalPos, (float) x);
                    pCovMatrix.setQuick(i, j, x);
                    pCovMatrix.setQuick(j, i, x);
                } else {
                    pCovMatrix.setQuick(i, j, 0);
                    pCovMatrix.setQuick(j, i, 0);
                }

            }
        }
        // ldCorr.releaseLDData();
        // System.out.println(pCovMatrix.toString());

        double totalEffetiveSize = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(pCovMatrix, null);
        keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
        double minP = totalEffetiveSize * pvalueWeightList.get(0).pValue;
        pvalueWeight[0] = pvalueWeightList.get(0).pValue;
        pvalueWeight[1] = totalEffetiveSize;
        double p;
        Set<Integer> selectedIndex = new HashSet<Integer>();
        selectedIndex.add(0);
        double ess = 0;
        snpNum--;
        double maxCurrMe = 1;
        for (int i = 1; i <= snpNum; i++) {
            //Note: a smart decise to avoid unneccessary expoloration
            if (minP <= pvalueWeightList.get(i).pValue) {
                return minP;
            }
            selectedIndex.add(i);

            //System.out.println(j);
            maxCurrMe += 1;
            if (minP > totalEffetiveSize * pvalueWeightList.get(i).pValue / (maxCurrMe)) {
                ess = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(pCovMatrix, selectedIndex);

                maxCurrMe = ess;
                p = totalEffetiveSize * pvalueWeightList.get(i).pValue / (ess);
                if (p < minP) {
                    keySNPPosition[0] = pvalueWeightList.get(i).physicalPos;
                    minP = p;
                    pvalueWeight[0] = pvalueWeightList.get(i).pValue;
                    pvalueWeight[1] = totalEffetiveSize / ess;
                }
            }
        }

        return minP;
        //Note it is usually r2, wwe have to transform it
        // return combinePValuebyVEGAS(pvalueWeightArray, subLDCorr);
    }

    //this function is only suitable for gene with smaill number of SNPs in terms of computational time and power
    private double snpSetPValuebyMyWeightedSimesTest(List<SNP> snpList, CorrelationBasedByteLDSparseMatrix ldCorr,
            int snpPVTypeIndex, int[] keySNPPosition) throws Exception {
        List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();
        int snpNum = snpList.size();

        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();
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
        DoubleMatrix2D pCovMatrix = new DenseDoubleMatrix2D(snpNum, snpNum);
        //no big differentce as you have to search
        Collections.sort(pvalueWeightList, new PValueWeightComparator());
        for (int i = 0; i < snpNum; i++) {
            pCovMatrix.setQuick(i, i, 1);
            for (int j = i + 1; j < snpNum; j++) {
                double x = ldCorr.getLDAt(pvalueWeightList.get(i).physicalPos, pvalueWeightList.get(j).physicalPos);
                //when r2
                //y = 0.7723x6 - 1.5659x5 + 1.201x4 - 0.2355x3 + 0.2184x2 + 0.6086x
                x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;

                if (x > MIN_R2) {
                    // ldCorr.setLDAt(pvalueWeightList.get(t).physicalPos, pvalueWeightList.get(j).physicalPos, (float) x);
                    pCovMatrix.setQuick(i, j, x);
                    pCovMatrix.setQuick(j, i, x);
                } else {
                    pCovMatrix.setQuick(i, j, 0);
                    pCovMatrix.setQuick(j, i, 0);
                }
            }
        }
        // ldCorr.releaseLDData();

        double totalEffetiveSize = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(pCovMatrix, null);
        Set<Integer> selectedIndex = new HashSet<Integer>();
        selectedIndex.add(0);
        double ess = 0;
        snpNum--;

        double accumulatedIndex = 1;
        pvalueWeightList.get(0).effectiveIndex = 1;
        double totalAdjustedEffectSize = pvalueWeightList.get(0).effectiveIndex * pvalueWeightList.get(0).weight;

        for (int i = 1; i < snpNum; i++) {
            selectedIndex.add(i);
            ess = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(pCovMatrix, selectedIndex);
            pvalueWeightList.get(i).effectiveIndex = ess - accumulatedIndex;
            accumulatedIndex = ess;
            totalAdjustedEffectSize += pvalueWeightList.get(i).effectiveIndex * pvalueWeightList.get(i).weight;
        }

        //to save time the last SNP
        pvalueWeightList.get(snpNum).effectiveIndex = totalEffetiveSize - accumulatedIndex;
        totalAdjustedEffectSize += pvalueWeightList.get(snpNum).effectiveIndex * pvalueWeightList.get(snpNum).weight;
        //replace back the SNP number
        snpNum++;

        pvalueWeightList.get(0).effectiveIndex = pvalueWeightList.get(0).effectiveIndex * pvalueWeightList.get(0).weight;
        for (int i = 1; i < snpNum; i++) {
            pvalueWeightList.get(i).effectiveIndex = pvalueWeightList.get(i).effectiveIndex * pvalueWeightList.get(i).weight
                    + pvalueWeightList.get(i - 1).effectiveIndex;
        }
        keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
        double minP = totalAdjustedEffectSize * pvalueWeightList.get(0).pValue / pvalueWeightList.get(0).effectiveIndex;
        double p;

        for (int i = 1; i < snpNum; i++) {
            p = totalAdjustedEffectSize * pvalueWeightList.get(i).pValue / pvalueWeightList.get(i).effectiveIndex;
            if (p < minP) {
                keySNPPosition[0] = pvalueWeightList.get(i).physicalPos;
                minP = p;
            }
        }

        return minP;
    }

    //this function is only suitable for gene with smaill number of SNPs in terms of computational time and power
    private double[] snpSetPValuebyMyWeightedSimesTest(List<SNP> snpList, String chromID, Map<String, double[]> varWeights, int weightNum, CorrelationBasedByteLDSparseMatrix ldCorr,
            int snpPVTypeIndex, int[] keySNPPosition) throws Exception {
        List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();
        int snpNum;
        double[] genePs = new double[weightNum];
        double[] meanWeights = varWeights.get("!MEAN@");
        boolean ignoreNoLDSNP = gbAssoc.isIgnoreNoLDSNP();
        for (int t = 0; t < weightNum; t++) {
            snpNum = snpList.size();
            pvalueWeightList.clear();
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
                if (!Double.isNaN(pValues[snpPVTypeIndex])) {
                    PValueWeight pv = new PValueWeight();
                    pv.physicalPos = snp.physicalPosition;
                    pv.pValue = pValues[snpPVTypeIndex];
                    double[] weights1 = varWeights.get(chromID + ":" + pv.physicalPos);
                    if (weights1 == null) {
                        pv.weight = meanWeights[t];
                    } else {
                        pv.weight = weights1[t];
                    }
                    pvalueWeightList.add(pv);
                }
            }

            snpNum = pvalueWeightList.size();
            if (snpNum == 0) {
                keySNPPosition[t] = -1;
                genePs[t] = Double.NaN;
                continue;
            } else if (snpNum == 1) {
                keySNPPosition[t] = pvalueWeightList.get(0).physicalPos;
                genePs[t] = pvalueWeightList.get(0).pValue;
                continue;
            }

            DoubleMatrix2D pCovMatrix = new DenseDoubleMatrix2D(snpNum, snpNum);
            //no big differentce as you have to search
            Collections.sort(pvalueWeightList, new PValueWeightComparator());
            for (int i = 0; i < snpNum; i++) {
                pCovMatrix.setQuick(i, i, 1);
                for (int j = i + 1; j < snpNum; j++) {
                    double x = ldCorr.getLDAt(pvalueWeightList.get(i).physicalPos, pvalueWeightList.get(j).physicalPos);
                    //when r2
                    //y = 0.7723x6 - 1.5659x5 + 1.201x4 - 0.2355x3 + 0.2184x2 + 0.6086x
                    x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;

                    if (x > MIN_R2) {
                        // ldCorr.setLDAt(pvalueWeightList.get(t).physicalPos, pvalueWeightList.get(j).physicalPos, (float) x);
                        pCovMatrix.setQuick(i, j, x);
                        pCovMatrix.setQuick(j, i, x);
                    } else {
                        pCovMatrix.setQuick(i, j, 0);
                        pCovMatrix.setQuick(j, i, 0);
                    }
                }
            }

            double totalEffetiveSize = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(pCovMatrix, null);
            Set<Integer> selectedIndex = new HashSet<Integer>();
            selectedIndex.add(0);
            double ess = 0;
            snpNum--;

            double accumulatedIndex = 1;
            pvalueWeightList.get(0).effectiveIndex = 1;
            double totalAdjustedEffectSize = pvalueWeightList.get(0).effectiveIndex * pvalueWeightList.get(0).weight;

            for (int i = 1; i < snpNum; i++) {
                selectedIndex.add(i);
                ess = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(pCovMatrix, selectedIndex);
                pvalueWeightList.get(i).effectiveIndex = ess - accumulatedIndex;
                accumulatedIndex = ess;
                totalAdjustedEffectSize += pvalueWeightList.get(i).effectiveIndex * pvalueWeightList.get(i).weight;
            }

            //to save time the last SNP
            pvalueWeightList.get(snpNum).effectiveIndex = totalEffetiveSize - accumulatedIndex;
            totalAdjustedEffectSize += pvalueWeightList.get(snpNum).effectiveIndex * pvalueWeightList.get(snpNum).weight;
            //replace back the SNP number
            snpNum++;

            pvalueWeightList.get(0).effectiveIndex = pvalueWeightList.get(0).effectiveIndex * pvalueWeightList.get(0).weight;
            for (int i = 1; i < snpNum; i++) {
                pvalueWeightList.get(i).effectiveIndex = pvalueWeightList.get(i).effectiveIndex * pvalueWeightList.get(i).weight
                        + pvalueWeightList.get(i - 1).effectiveIndex;
            }
            keySNPPosition[t] = pvalueWeightList.get(0).physicalPos;
            double minP = totalAdjustedEffectSize * pvalueWeightList.get(0).pValue / pvalueWeightList.get(0).effectiveIndex;
            double p;

            for (int i = 1; i < snpNum; i++) {
                p = totalAdjustedEffectSize * pvalueWeightList.get(i).pValue / pvalueWeightList.get(i).effectiveIndex;
                if (p < minP) {
                    keySNPPosition[t] = pvalueWeightList.get(i).physicalPos;
                    minP = p;
                }
            }
            genePs[t] = minP;
        }
        return genePs;
    }

    class ScanGeneBasedAssocSwingWorker extends SwingWorker<Void, String> {

        private final int NUM = 100;
        boolean succeed = false;
        ProgressHandle ph = null;
        long time = 0;
        RConnection rcon;
        boolean needRCon = false;

        public ScanGeneBasedAssocSwingWorker() {
            ph = ProgressHandleFactory.createHandle("Gene-based association task", new Cancellable() {
                @Override
                public boolean cancel() {
                    return handleCancel();
                }
            });
            runningResultTopComp.setVisible(true);
            runningResultTopComp.newPane();
            time = System.nanoTime();
        }

        public int readWeight(String fileName, Map<String, double[]> weightMap, List<String> heads) {
            BufferedReader br = null;

            String line;
            double[] weight = null;
            final String missing = ".";
            try {
                br = new BufferedReader(new FileReader(fileName));
                line = br.readLine();
                String[] cells = line.split("\\s+");
                if (!Util.isNumeric(cells[1])) {
                    //it must be heads
                    heads.addAll(Arrays.asList(cells));
                    line = br.readLine();
                } else {
                    heads.add("chrom");
                    heads.add("pos");
                    for (int i = 2; i < cells.length; i++) {
                        heads.add("Weight" + (i - 1));
                    }
                }
                do {
                    cells = line.split("\\s+");
                    weight = new double[cells.length - 2];
                    for (int i = 0; i < weight.length; i++) {
                        if (cells[i + 2].equals(missing)) {
                            weight[i] = Double.NaN;
                        } else {
                            weight[i] = Double.parseDouble(cells[i + 2]);
                        }
                    }
                    weightMap.put(cells[0] + ":" + cells[1], weight);
                } while ((line = br.readLine()) != null);
                br.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            double[] means = new double[weight.length];
            int[] counts = new int[weight.length];
            Arrays.fill(counts, 0);
            Arrays.fill(means, 0);

            for (Map.Entry<String, double[]> item : weightMap.entrySet()) {
                weight = item.getValue();
                for (int i = 0; i < means.length; i++) {
                    if (!Double.isNaN(weight[i])) {
                        means[i] += weight[i];
                        counts[i]++;
                    }
                }
            }
            for (int i = 0; i < means.length; i++) {
                means[i] = means[i] / counts[i];
            }
            //replace the missing value with mean
            for (Map.Entry<String, double[]> item : weightMap.entrySet()) {
                weight = item.getValue();
                for (int i = 0; i < means.length; i++) {
                    if (Double.isNaN(weight[i])) {
                        weight[i] = means[i];
                    }
                }
            }
            weightMap.put("!MEAN@", means);
            return weight.length;
        }

        @Override
        protected Void doInBackground() {
            try {
                StatusDisplayer.getDefault().setStatusText("Scan gene-based association on the genome ...");
                long startTime = System.currentTimeMillis();
                final List<PValueGene> pValueGenes = new ArrayList<PValueGene>();
                DoubleArrayList genePValues1 = new DoubleArrayList();
                DoubleArrayList geneWeightedPValues = new DoubleArrayList();
                final DoubleArrayList snpInGenePValues = new DoubleArrayList();
                final DoubleArrayList snpOutGenePValues = new DoubleArrayList();
                final DoubleArrayList snpGenomePValues = new DoubleArrayList();
                //for writing paper
               // final DoubleArrayList smallGenePValues = new DoubleArrayList();
               // final DoubleArrayList largeGenePValues = new DoubleArrayList();
                int snpNum;
                DoubleArrayList pValueArray = new DoubleArrayList();
                double minPValue = 1;
                double p1, p2;
                HashSet<Integer> selectiveChromIDs = new HashSet<Integer>();
                int SNPpValueNameNum = testedPValueIndexes.length;
                boolean filterGene = false;
                if (geneGroupIDSet != null && !geneGroupIDSet.isEmpty()) {
                    filterGene = true;
                }
                double[] pValues = null;
                String infor = " Loading genome from disk...";
                runningResultTopComp.insertIcon(imgFolder, "Next.png", infor);
                LOG.info(infor);

                int geneTestMethodID = 0;
                if (gbAssoc.getTestedMethod().equals("GATES")) {
                    geneTestMethodID = 1;
                } else if (gbAssoc.getTestedMethod().equals("ECS")) {
                    geneTestMethodID = 2;
                }

                int[] keyBlockIndex = new int[1];

                Map<String, double[]> varWeights = new HashMap<String, double[]>();
                int weightNum = -1;
                List<String> weighNames = new ArrayList<String>();

                if (varWeighFilePath != null) {
                    weightNum = readWeight(varWeighFilePath, varWeights, weighNames);
                }
                double[] tmpResult = new double[2];
                for (int k = 0; k < SNPpValueNameNum; k++) {
                    ph.start(); //we must start the PH before we swith to determinate
                    ph.switchToIndeterminate();
                    int pValueIndex = testedPValueIndexes[k];
                    String pValueName = gbAssoc.getGenome().getpValueNames()[pValueIndex];

                    //a special code to generate inflation facter for the scaled chi-squre test. but it is somewhat empirical 
                    selectiveChromIDs.clear();

                    for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
                        Chromosome chromosome = gbAssoc.getGenome().readChromosomefromDisk(chromIndex);
                        if (chromosome == null || chromosome.genes.isEmpty()) {
                            continue;
                        }
                        List<Gene> genes = chromosome.genes;
                        int geneNum = genes.size();
                        selectiveChromIDs.add(chromIndex);
                        CorrelationBasedByteLDSparseMatrix ldRsMatrix = null;
                        if (gbAssoc.getGenome().getLdSourceCode() == -2) {
                            ldRsMatrix = GlobalManager.currentProject.getGenomeByName(gbAssoc.getGenome().getSameLDGenome()).readChromosomeLDfromDisk(chromIndex);
                        } else {
                            ldRsMatrix = gbAssoc.getGenome().readChromosomeLDfromDisk(chromIndex);
                        }

                        if (ldRsMatrix == null || ldRsMatrix.isEmpty()) {
                            infor = "Chromosome " + CHROM_NAMES[chromIndex] + " has no LD information! Force to use the original Simes test to cobmine p-values of SNPs within genes!";

                            LOG.info(infor);
                            runningResultTopComp.insertText(infor);

                            for (int j = 0; j < geneNum; j++) {
                                Gene gene = genes.get(j);
                                if (filterGene && !geneGroupIDSet.contains(gene.getGeneGroupID())) {
                                    continue;
                                }

                                PValueGene pValueGene = new PValueGene(gene.getSymbol());
                                pValueArray.clear();
                                pValueGene.setpValue(genePValuebySimesTest(gene, pValueArray, pValueIndex, pValueGene.keySNPPositions, pValueGene.blockPValues));
                                pValueGenes.add(pValueGene);

                                // System.out.println(pValueGene.getpValue());
                                if (qqPlotSNPInsideGene) {
                                    snpInGenePValues.addAllOf(pValueArray);
                                }
                                if (qqPlotAllSNP) {
                                    snpGenomePValues.addAllOf(pValueArray);
                                }
                            }
                        }//Note this thread is not safe
                        /*else if (geneTestMethodID == 2) {
                            int maxThreadNum = Runtime.getRuntime().availableProcessors() - 1;
                            maxThreadNum = maxThreadNum < 1 ? 1 : maxThreadNum;
                            ExecutorService exec = Executors.newFixedThreadPool(maxThreadNum);
                            final CompletionService<String> serv = new ExecutorCompletionService<String>(exec);
                            int runningThread = 0;

//List<Gene> genes, CorrelationBasedByteLDSparseMatrix ldCorr, boolean ignoreNoLDSNP, int snpPVTypeIndex, boolean allSimesTest
                            int[] blocks = org.cobi.util.math.Array.partitionEvenBlock(maxThreadNum, 0, genes.size());
                            int blockNum = blocks.length - 1;
                            for (int s = 0; s < blockNum; s++) {
                                EffectiveChiSquareTask task = new EffectiveChiSquareTask(genes.subList(blocks[s], blocks[s + 1]), ldRsMatrix, gbAssoc.isIgnoreNoLDSNP(), pValueIndex) {
                                    @Override
                                    public void fireTaskComplete() throws Exception {
                                        //  System.out.println(infor);
                                        if (qqPlotSNPInsideGene) {
                                            snpInGenePValues.addAllOf(this.getSnpPValueArray());
                                        }
                                        if (qqPlotAllSNP) {
                                            snpGenomePValues.addAllOf(this.getSnpPValueArray());
                                        }
                                        pValueGenes.addAll(this.getpValueGenes());
                                    }

                                };
                                serv.submit(task);
                                runningThread++;
                            }

                            for (int s = 0; s < runningThread; s++) {
                                Future<String> task = serv.take();
                                infor = task.get();
                            }
                            exec.shutdown();
                        }*/ else {
                            for (int j = 0; j < geneNum; j++) {
                                Gene gene = genes.get(j);
                                // System.out.println(gene.getSymbol()+"\t"+gene.snps.size());

                                if (filterGene && !geneGroupIDSet.contains(gene.getGeneGroupID())) {
                                    continue;
                                }
                                PValueGene pValueGene = new PValueGene(gene.getSymbol());
                                pValueArray.clear();

                                p1 = Double.NaN;
                                keyBlockIndex[0] = 0;
                                if (weightNum > 0) {
                                    pValues = snpSetPValuebyMySimesTestBlockVarWeight(gene, pValueArray, ldRsMatrix, pValueIndex, CHROM_NAMES[chromIndex], true, keyBlockIndex, varWeights, weightNum);
                                } else if (geneTestMethodID == 0) {
                                    p1 = snpSetPValuebyMySimesTestBlock(gene, pValueArray, ldRsMatrix, pValueIndex, false, pValueGene.keySNPPositions, pValueGene.blockPValues, pValueGene.keySNPPValues, pValueGene.blockWeights, false, keyBlockIndex);
                                } else if (geneTestMethodID == 1) {
                                    //original                                 
                                    p1 = snpSetPValuebyMySimesTestBlock(gene, pValueArray, ldRsMatrix, pValueIndex, false, pValueGene.keySNPPositions, pValueGene.blockPValues, pValueGene.keySNPPValues, pValueGene.blockWeights, true, keyBlockIndex);

                                } else if (geneTestMethodID == 2) {
                                    p1 = snpSetPValuebyMyScaledChisquareTestBlock(gene.snps, pValueArray, ldRsMatrix, pValueIndex, pValueGene.keySNPPositions, pValueGene.blockPValues, null, tmpResult);

//new ones
                                    //  p1 = snpSetPValuebyOurWeightedScaledChiSquare(gene, pValueArray, ldRsMatrix, pValueIndex, false, pValueGene.keySNPPositions, pValueGene.blockPValues);
                                    //    p1 = snpSetPValuebyMyChiSquareApproxEJML(gene, pValueArray, ldRsMatrix, pValueIndex, false, pValueGene.keySNPPositions, pValueGene.blockPValues);
                                    // p1 = snpSetPValuebyJohnnyChiSquare(gene, pValueArray, ldRsMatrix, pValueIndex, false, pValueGene.keySNPPositions, pValueGene.blockPValues, rcon);
                                    //  p1 = snpSetPValuebyMyScaledChiSquare(gene, pValueArray, ldRsMatrix, pValueIndex, false, pValueGene.keySNPPositions, pValueGene.blockPValues);
                                    //   p1 = snpSetPValuebyMyScaledChiSquareSimple(gene, pValueArray, ldRsMatrix, pValueIndex, false, pValueGene.keySNPPositions, pValueGene.blockPValues);
                                }

                                if (pValues != null) {
                                    p1 = pValues[0];
                                    pValueGene.setpValues(pValues);
                                }

                                pValueGene.setpValue(p1);
                                pValueGene.keyBlockIndex = keyBlockIndex[0];
                                pValueGenes.add(pValueGene);

                                /*
                                if (gene.snps.size() < 5) {
                                    smallGenePValues.add(p1);
                                } else {
                                    largeGenePValues.add(p1);
                                }*/

                                // System.out.println(pValueGene.getpValue());
                                if (qqPlotSNPInsideGene) {
                                    snpInGenePValues.addAllOf(pValueArray);
                                }
                                if (qqPlotAllSNP) {
                                    snpGenomePValues.addAllOf(pValueArray);
                                }

                            }
                        }

                        if (qqPlotSNPOutSideGene) {
                            List<SNP> snpOutGenes = chromosome.snpsOutGenes;
                            snpNum = snpOutGenes.size();
                            for (int j = 0; j < snpNum; j++) {
                                SNP snp = snpOutGenes.get(j);
                                double[] snppValues = snp.getpValues();
                                if (snppValues == null) {
                                    continue;
                                }
                                if (!Double.isNaN(snppValues[pValueIndex])) {
                                    snpOutGenePValues.add(snppValues[k]);
                                }
                            }
                        }

                        if (qqPlotAllSNP) {
                            List<SNP> snpOutGenes = chromosome.snpsOutGenes;
                            snpNum = snpOutGenes.size();
                            for (int j = 0; j < snpNum; j++) {
                                SNP snp = snpOutGenes.get(j);
                                double[] snppValues = snp.getpValues();
                                if (snppValues == null) {
                                    continue;
                                }
                                if (!Double.isNaN(snppValues[pValueIndex])) {
                                    snpGenomePValues.add(snppValues[k]);
                                }
                            }
                        }

                        infor = "Chromosome " + CHROM_NAMES[chromIndex] + " scanned. " + geneNum + " genes;";
                        runningResultTopComp.insertIcon(imgFolder, "chromosome.png", infor);
                        LOG.info(infor);
                    }

                    int geneNum = pValueGenes.size();

                    if (networkWeightGenePValue) {
                        Map<String, Double> allGeneWeightMap = calculatePPIGeneAttributeWeight();
                        double totalWeight = 0;
                        int withWeightGeneNum = 0;
                        for (int i = 0; i < geneNum; i++) {
                            PValueGene pValueGene = pValueGenes.get(i);
                            if (allGeneWeightMap.containsKey(pValueGene.getSymbol())) {
                                totalWeight += allGeneWeightMap.get(pValueGene.getSymbol());
                                withWeightGeneNum++;
                            }
                        }

                        double weightScale = withWeightGeneNum / totalWeight;
                        double sumAP = 0;
                        double adjustedP = 0;
                        for (int i = 0; i < geneNum; i++) {
                            PValueGene pValueGene = pValueGenes.get(i);

                            if (allGeneWeightMap.containsKey(pValueGene.getSymbol())) {
                                adjustedP = pValueGene.pValue / (allGeneWeightMap.get(pValueGene.getSymbol()) * weightScale);
                                if (adjustedP > 1) {
                                    adjustedP = 1;
                                }
                                geneWeightedPValues.add(adjustedP);
                                pValueGene.pValue = adjustedP;
                                //sumAP+=allGeneWeightMap.get(pValueGene.getSymbol()) * weightScale;                    
                            } else {
                                geneWeightedPValues.add(pValueGene.pValue);
                            }
                        }
                    }

                    Map<String, Double> pValueGeneMap = new HashMap<String, Double>(geneNum);
                    for (int i = 0; i < geneNum; i++) {
                        PValueGene pValueGene = pValueGenes.get(i);
                        pValueGeneMap.put(pValueGene.getSymbol(), pValueGene.pValue);
                        if (!Double.isNaN(pValueGene.pValue)) {
                            genePValues1.add(pValueGene.pValue);
                        }
                        if (minPValue > pValueGene.pValue) {
                            minPValue = pValueGene.pValue;
                        }
                    }

                    gbAssoc.saveGenePValuestoDisk(pValueName, weighNames, pValueGenes);

                    infor = "There are " + pValueGenes.size() + " genes in total observed on the GWAS dataset.";
                    //System.out.println(uniqueSNPIDs.size());
                    runningResultTopComp.insertText(infor);
                    LOG.info(infor);

                    //draw Manhattan plotGenePValues
                    if (!selectiveChromIDs.isEmpty()) {
                        ManhattanPlotPainter manhattanPlotPainter1 = new ManhattanPlotPainter(manhattanPlotWidth, manhattanPlotHeight);
                        manhattanPlotPainter1.setSelectiveChromIDs(selectiveChromIDs);
                        manhattanPlotPainter1.setManhattanPlotMinPValue(manhattanPlotMinPValue);
                        manhattanPlotPainter1.setManhattanPlotLabeGenePValue(manhattanPlotLabeGenePValue);
                        manhattanPlotPainter1.setManhattanPlotLableSNPPValue(manhattanPlotLableSNPPValue);
                        File manhattanImgFile1 = new File(imgFolder.getCanonicalPath() + File.separator + gbAssoc.getName() + "." + pValueName + "." + "GeneManhattanPlot1" + ".png");
                        manhattanPlotPainter1.plotGenePValues(pValueGeneMap, gbAssoc.getGenome(), -Math.log10(minPValue), manhattanImgFile1.getCanonicalPath());
                        infor = "Manhattan plot of gene p-values";
                        runningResultTopComp.insertText(infor);
                        LOG.info(infor);
                        runningResultTopComp.insertImage(manhattanImgFile1);
                        manhattanImgFile1 = new File(imgFolder.getCanonicalPath() + File.separator + gbAssoc.getName() + "." + pValueName + "." + "SNPManhattanPlot1" + ".png");
                        manhattanPlotPainter1.plotSNPPValues(gbAssoc.getGenome(), -Math.log10(minPValue), testedPValueIndexes, manhattanImgFile1.getCanonicalPath());
                        infor = "Manhattan plot of SNP p-values";
                        runningResultTopComp.insertText(infor);
                        LOG.info(infor);
                        runningResultTopComp.insertImage(manhattanImgFile1);
                    }

                    //draw qq plotGenePValues
                    PValuePainter painter = new PValuePainter(qqPlotWidth, qqPlotHeight);
                    File imgFile = new File(imgFolder.getCanonicalPath() + File.separator + gbAssoc.getName() + "." + pValueName + "." + "QQPlotGene" + ".png");

                    genePValues1.quickSort();
                    snpInGenePValues.quickSort();
                    snpGenomePValues.quickSort();
                    snpOutGenePValues.quickSort();
                    if (qqPlotSNPInsideGene && qqPlotSNPOutSideGene) {
                        /*
                        //As SNPs in hight LD are pruned in the new version, the message will be confusing
                        double outGeneSNPProp = snpOutGenePValues.size() * 1.0 / (snpInGenePValues.size() + snpOutGenePValues.size()) * 100;
                        infor = Util.doubleToString(100 - outGeneSNPProp, 3) + "% of SNPs are inside genes on the whole genome.";
                        runningResultTopComp.insertText(infor);
                        LOG.info(infor);
                         */
                    }
                    List<String> titles = new ArrayList<String>();
                    List<DoubleArrayList> pvalueList = new ArrayList<DoubleArrayList>();

                    titles.add("Gene p-values");
                    pvalueList.add(genePValues1);
                    if (networkWeightGenePValue) {
                        geneWeightedPValues.quickSort();
                        titles.add("Network weighted gene p-values");
                        pvalueList.add(geneWeightedPValues);
                    }

                    if (!snpGenomePValues.isEmpty() && qqPlotAllSNP) {
                        titles.add("All SNPs");
                        pvalueList.add(snpGenomePValues);
                    }
                    if (!snpInGenePValues.isEmpty() && qqPlotSNPInsideGene) {
                        titles.add("SNPs inside of gene");
                        pvalueList.add(snpInGenePValues);
                    }
                    if (!snpOutGenePValues.isEmpty() && qqPlotSNPOutSideGene) {
                        titles.add("SNPs outside of gene");
                        pvalueList.add(snpOutGenePValues);
                    }
                    if (!pvalueList.isEmpty()) {
                        painter.drawMultipleQQPlot(pvalueList, titles, null, imgFile.getCanonicalPath(), qqPlotMinPValue);
                        //painter.drawQQPlot(weightedSNPGenePValues, "QQ Plot of " + "gene-based pMin-values", imgFile.getCanonicalPath());
                        runningResultTopComp.insertImage(imgFile);
                    }
                    /*
                    //painting for manuscrpt writing
                    {
                        imgFile = new File(imgFolder.getCanonicalPath() + File.separator + gbAssoc.getName() + "." + pValueName + "." + "QQPlotGeneCompare" + ".png");
                        titles.clear();
                        titles.add("All genes");
                        titles.add("Small genes");
                        titles.add("large genes");
                        pvalueList.clear();
                        pvalueList.add(genePValues1);
                        smallGenePValues.quickSort();
                        pvalueList.add(smallGenePValues);
                        largeGenePValues.quickSort();
                        pvalueList.add(largeGenePValues);
                        painter.drawMultipleQQPlot(pvalueList, titles, null, imgFile.getCanonicalPath(), qqPlotMinPValue);
                        runningResultTopComp.insertImage(imgFile);
                    }
                    */

                    infor = "Gene-based association scan has been finished for " + pValueName;
                    publish(infor);
                    runningResultTopComp.insertText(infor);
                    LOG.info(infor);
                }

                pValueGenes.clear();
                genePValues1.clear();
                snpInGenePValues.clear();
                snpGenomePValues.clear();

                //genome.storeGenome2Disk();
                System.gc();
                succeed = true;
            } catch (InterruptedException ex) {
                StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

            } catch (RserveException ex) {
                if (ex.getMessage().contains("Cannot connect")) {
                    // System.out.println(ex.getMessage() + "\t" + ex.getRequestReturnCode() + "\t" + ex.getRequestErrorDescription());
                    String infor = "Please open your R and type the following commands to allow kgg to use it:\npack=\"Rserve\";\n"
                            + "if (!require(pack,character.only = TRUE))   { install.packages(pack,dep=TRUE,repos=\'http://cran.us.r-project.org\');   if(!require(pack,character.only = TRUE)) stop(\"Package not found\")   }\n"
                            + "library(\"Rserve\");\nRserve(debug = FALSE, port = 6311, args = NULL)\n";
                    FormatShowingDialog dialog = new FormatShowingDialog(null, false, "Rserve connection!", infor);
                    //  dialog.setLocationRelativeTo(this);
                    dialog.setVisible(true);
                    // java.util.logging.Logger.getLogger(infor);
                };
            } catch (Exception ex) {
                ex.printStackTrace();
                StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

            return null;
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
                if (needRCon) {
                    this.rcon.close();
                }
                String message;
                if (!succeed) {
                    message = ("Gene-based association scan failed!");
                    LOG.info(message);
                    return;
                }
                GlobalManager.currentProject.addGeneBasedAssociationScan(gbAssoc);
                GlobalManager.geneAssocSetModel.addElement(gbAssoc);

                ProjectTopComponent projectTopComponent = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                projectTopComponent.showProject(GlobalManager.currentProject);

                message = ("Gene-based association scan has been finished!");
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
                runningResultTopComp.insertText(info);
                File geneAssociationResultFilePath = new File(prjWorkingPath + File.separator + prjName + File.separator
                        + gbAssoc.getName() + ".html");
                runningResultTopComp.savePane(geneAssociationResultFilePath);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    class ScanMultivarGeneBasedAssocSwingWorker extends SwingWorker<Void, String> {

        private final int NUM = 100;

        double[][] traitCorrMatrix;
        boolean succeed = false;

        ProgressHandle ph = null;
        long time;

        public ScanMultivarGeneBasedAssocSwingWorker(double[][] traitCorrMatrix) {
            ph = ProgressHandleFactory.createHandle("Gene-based association task", new Cancellable() {
                @Override
                public boolean cancel() {
                    return handleCancel();
                }
            });

            runningResultTopComp.setVisible(true);
            runningResultTopComp.newPane();
            this.traitCorrMatrix = traitCorrMatrix;
            time = System.nanoTime();
        }

        @Override
        protected Void doInBackground() {
            try {
                StatusDisplayer.getDefault().setStatusText("Scan gene-based association on the genome ...");
                long startTime = System.currentTimeMillis();

                List<PValueGene> pValueGenes = new ArrayList<PValueGene>();
                DoubleArrayList genePValues1 = new DoubleArrayList();
                DoubleArrayList geneWeightedPValues = new DoubleArrayList();
                DoubleArrayList snpInGenePValues = new DoubleArrayList();
                DoubleArrayList snpOutGenePValues = new DoubleArrayList();
                DoubleArrayList snpGenomePValues = new DoubleArrayList();
                int snpNum;
                DoubleArrayList snpPValueArray = new DoubleArrayList();
                double minPValue = 1;
                double p1, p2;
                HashSet<Integer> selectiveChromIDs = new HashSet<Integer>();
                int SNPpValueNameNum = testedPValueIndexes.length;

                String infor = " Loading genome from disk and conducting gene-based association analysis...";
                runningResultTopComp.insertIcon(imgFolder, "Next.png", infor);
                LOG.info(infor);

                int geneTestMethodID = 0;
                if (gbAssoc.getTestedMethod().equals("GATES")) {
                    geneTestMethodID = 1;
                }
                int trait1Index = 0;
                int trait2Index = 0;
                int snp1Index = 0;
                int snp2Index = 0;
                int accu1Index = 0;
                int accu2Index = 0;
                boolean filterGene = false;
                if (geneGroupIDSet != null) {
                    filterGene = true;
                }
                ph.start(); //we must start the PH before we swith to determinate
                ph.switchToIndeterminate();

                IntArrayList[] keySNPPositions = new IntArrayList[SNPpValueNameNum];
                DoubleArrayList[] blockPValues = new DoubleArrayList[SNPpValueNameNum];
                DoubleArrayList[] keySNPPValues = new DoubleArrayList[SNPpValueNameNum];
                DoubleArrayList[] blockWeights = new DoubleArrayList[SNPpValueNameNum];
                for (int k = 0; k < SNPpValueNameNum; k++) {
                    keySNPPositions[k] = new IntArrayList();
                    blockPValues[k] = new DoubleArrayList();
                    keySNPPValues[k] = new DoubleArrayList();
                    blockWeights[k] = new DoubleArrayList();
                }
                //a special code to generate inflation facter for the scaled chi-squre test. but it is somewhat empirical 
                selectiveChromIDs.clear();
                List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();
                int[] keyIndex = new int[1];
                //code for paper writing 
                // BufferedWriter bw = new BufferedWriter(new FileWriter("gene-size.txt", true));
                DoubleMatrix2D traitldCorr = new DenseDoubleMatrix2D(traitCorrMatrix);
                int traitNum = traitCorrMatrix.length;
                for (int i = 0; i < traitNum; i++) {
                    for (int j = i + 1; j < traitNum; j++) {
                        double x = traitldCorr.getQuick(i, j);
                        x = x * x;
                        //when r2                 
                        //I do not know why it seems if I use x=x*x  it woks better in terms of type 1 error
                        x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;
                        //x = x * x;
                        traitldCorr.setQuick(i, j, x);
                        traitldCorr.setQuick(j, i, x);
                    }
                }
                Map<String, Double> tastSNPP = new HashMap<String, Double>();

                for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
                    Chromosome chromosome = gbAssoc.getGenome().readChromosomefromDisk(chromIndex);
                    if (chromosome == null || chromosome.genes.isEmpty()) {
                        continue;
                    }
                    List<Gene> genes = chromosome.genes;
                    int geneNum = genes.size();
                    selectiveChromIDs.add(chromIndex);
                    CorrelationBasedByteLDSparseMatrix ldRsMatrix = null;
                    if (gbAssoc.getGenome().getLdSourceCode() == -2) {
                        ldRsMatrix = GlobalManager.currentProject.getGenomeByName(gbAssoc.getGenome().getSameLDGenome()).readChromosomeLDfromDisk(chromIndex);
                    } else {
                        ldRsMatrix = gbAssoc.getGenome().readChromosomeLDfromDisk(chromIndex);
                    }

                    if (ldRsMatrix == null || ldRsMatrix.isEmpty()) {
                        infor = "Chromosome " + CHROM_NAMES[chromIndex] + " has no LD information! Force to use the original Simes test to cobmine p-values of SNPs within genes!";

                        LOG.info(infor);
                        runningResultTopComp.insertText(infor);

                        for (int j = 0; j < geneNum; j++) {
                            Gene gene = genes.get(j);
                            if (filterGene && !geneGroupIDSet.contains(gene.getGeneGroupID())) {
                                continue;
                            }
                            PValueGene pValueGene = new PValueGene(gene.getSymbol());
                            snpPValueArray.clear();
                            pvalueWeightList.clear();
                            for (int k = 0; k < SNPpValueNameNum; k++) {
                                int pValueIndex = testedPValueIndexes[k];
                                snpNum = gene.snps.size();
                                for (int t = 0; t < snpNum; t++) {
                                    SNP snp = gene.snps.get(t);
                                    double[] pValues = snp.getpValues();
                                    if (pValues == null) {
                                        continue;
                                    }
                                    if (!Double.isNaN(pValues[pValueIndex])) //
                                    {
                                        PValueWeight pv = new PValueWeight();
                                        pv.physicalPos = snp.physicalPosition;
                                        pv.pValue = pValues[pValueIndex];
                                        pv.weight = 1.0;
                                        pvalueWeightList.add(pv);
                                        snpPValueArray.add(pv.pValue);
                                    }
                                }
                            }
                            snpNum = gene.snps.size();
                            for (int t = 0; t < snpNum; t++) {
                                SNP snp = gene.snps.get(t);
                                double[] pValues = snp.getpValues();
                                PValueWeight[] pValueArray = new PValueWeight[pValues.length];
                                for (int s = 0; s < pValues.length; s++) {
                                    pValueArray[s] = new PValueWeight();
                                    pValueArray[s].physicalPos = t;
                                    pValueArray[s].pValue = pValues[s];
                                    pValueArray[s].weight = 1;
                                }
                                double tp = SetBasedTest.combinePValuebyWeightedSimeCombinationTestMyMe(pValueArray, traitldCorr, false);
                                //for multivaiate assoc test by TASTE
                                tastSNPP.put(CHROM_NAMES[chromIndex] + ":" + snp.getPhysicalPosition(), tp);
                            }

                            pValueGene.setpValue(genePValuebySimesTest(pvalueWeightList, pValueGene.keySNPPositions, pValueGene.blockPValues));
                            pValueGenes.add(pValueGene);
                            if (!Double.isNaN(pValueGene.getpValue())) {
                                genePValues1.add(pValueGene.getpValue());
                            }
                            // System.out.println(pValueGene.getpValue());

                            if (qqPlotSNPInsideGene) {
                                snpInGenePValues.addAllOf(snpPValueArray);
                            }
                            if (qqPlotAllSNP) {
                                snpGenomePValues.addAllOf(snpPValueArray);
                            }
                            if (minPValue > pValueGene.pValue) {
                                minPValue = pValueGene.pValue;
                            }
                        }
                    } else {
                        for (int j = 0; j < geneNum; j++) {
                            Gene gene = genes.get(j);
                            if (filterGene && !geneGroupIDSet.contains(gene.getGeneGroupID())) {
                                continue;
                            }
                            PValueGene pValueGene = new PValueGene(gene.getSymbol());
                            snpPValueArray.clear();
                            p1 = 1;
                            int toalKeySNP = 0;
                            int geneSnpNum = gene.snps.size();
                            for (int t = 0; t < geneSnpNum; t++) {
                                SNP snp = gene.snps.get(t);
                                double[] pValues = snp.getpValues();
                                PValueWeight[] pValueArray = new PValueWeight[pValues.length];
                                for (int s = 0; s < pValues.length; s++) {
                                    pValueArray[s] = new PValueWeight();
                                    pValueArray[s].physicalPos = t;
                                    pValueArray[s].pValue = pValues[s];
                                    pValueArray[s].weight = 1;
                                }
                                double tp = SetBasedTest.combinePValuebyWeightedSimeCombinationTestMyMe(pValueArray, traitldCorr, false);
                                //for multivaiate assoc test by TASTE
                                tastSNPP.put(CHROM_NAMES[chromIndex] + ":" + snp.getPhysicalPosition(), tp);
                            }

                            for (int k = 0; k < SNPpValueNameNum; k++) {
                                keySNPPositions[k].clear();
                                blockPValues[k].clear();
                                keySNPPValues[k].clear();
                                blockWeights[k].clear();
                                int pValueIndex = testedPValueIndexes[k];

                                for (int t = 0; t < geneSnpNum; t++) {
                                    SNP snp = gene.snps.get(t);
                                    double[] pValues = snp.getpValues();
                                    if (pValues == null) {
                                        continue;
                                    }
                                    if (!Double.isNaN(pValues[pValueIndex])) //
                                    {
                                        snpPValueArray.add(pValues[pValueIndex]);
                                    }
                                }

                                if (geneTestMethodID == 0) {
                                    snpSetPValueOnlybyMySimesTestBlock(gene.snps, ldRsMatrix, pValueIndex, false, keySNPPositions[k], blockPValues[k], keySNPPValues[k], blockWeights[k], false);
                                } else if (geneTestMethodID == 1) {
                                    snpSetPValueOnlybyMySimesTestBlock(gene.snps, ldRsMatrix, pValueIndex, false, keySNPPositions[k], blockPValues[k], keySNPPValues[k], blockWeights[k], true);
                                }
                                toalKeySNP += keySNPPositions[k].size();
                            }

                            if (toalKeySNP == 0) {
                                continue;
                            }

                            DoubleMatrix2D newCorrMatrix = new DenseDoubleMatrix2D(toalKeySNP, toalKeySNP);
                            //System.out.println(gene.getSymbol() + ": " + toalKeySNP);

                            PValueWeight[] pvs = new PValueWeight[toalKeySNP];

                            for (int t = 0; t < toalKeySNP; t++) {
                                newCorrMatrix.setQuick(t, t, 1);
                                trait1Index = 0;
                                accu1Index = keySNPPositions[trait1Index].size();
                                while (t >= accu1Index) {
                                    trait1Index++;
                                    accu1Index += keySNPPositions[trait1Index].size();
                                }
                                snp1Index = keySNPPositions[trait1Index].getQuick(t - (accu1Index - keySNPPositions[trait1Index].size()));
                                pvs[t] = new PValueWeight();
                                pvs[t].pValue = blockPValues[trait1Index].getQuick(t - (accu1Index - blockPValues[trait1Index].size()));
                                pvs[t].physicalPos = t;
                                pvs[t].weight = 1;

                                for (int s = t + 1; s < toalKeySNP; s++) {
                                    trait2Index = 0;
                                    accu2Index = keySNPPositions[trait2Index].size();
                                    while (s >= accu2Index) {
                                        trait2Index++;
                                        accu2Index += keySNPPositions[trait2Index].size();
                                    }
                                    snp2Index = keySNPPositions[trait2Index].getQuick(s - (accu2Index - keySNPPositions[trait2Index].size()));
                                    newCorrMatrix.setQuick(t, s, traitCorrMatrix[trait1Index][trait2Index] * traitCorrMatrix[trait1Index][trait2Index] * ldRsMatrix.getLDAt(snp1Index, snp2Index));
                                    newCorrMatrix.setQuick(s, t, newCorrMatrix.getQuick(t, s));
                                }
                            }

                            if (geneTestMethodID == 0) {
                                p1 = SetBasedTest.combinePValuebyScaleedFisherCombinationTestCovLogP(pvs, newCorrMatrix);
                            } else if (geneTestMethodID == 1) {
                                p1 = SetBasedTest.combineGATESPValuebyNoWeightSimeCombinationTestMyMe(pvs, newCorrMatrix, keyIndex);
                                // p1 = SetBasedTest.combineGATESPValuebyWeightedSimeCombinationTestMyMe(pvs, newCorrMatrix.copy());
                            }
                            pValueGene.setpValue(p1);
                            pValueGenes.add(pValueGene);
                            if (!Double.isNaN(p1)) {
                                genePValues1.add(pValueGene.getpValue());
                            }
                            //  bw.write(gene.snps.size() + "\t" + p1 + "\n");
                            // System.out.println(pValueGene.getpValue());

                            if (qqPlotSNPInsideGene) {
                                snpInGenePValues.addAllOf(snpPValueArray);
                            }
                            if (qqPlotAllSNP) {
                                snpGenomePValues.addAllOf(snpPValueArray);
                            }
                            if (minPValue > pValueGene.pValue) {
                                minPValue = pValueGene.pValue;
                            }
                        }
                    }

                    if (qqPlotSNPOutSideGene) {
                        List<SNP> snpOutGenes = chromosome.snpsOutGenes;
                        snpNum = snpOutGenes.size();
                        for (int k = 0; k < SNPpValueNameNum; k++) {
                            int pValueIndex = testedPValueIndexes[k];
                            for (int j = 0; j < snpNum; j++) {
                                SNP snp = snpOutGenes.get(j);
                                double[] snppValues = snp.getpValues();
                                if (snppValues == null) {
                                    continue;
                                }
                                if (!Double.isNaN(snppValues[pValueIndex])) {
                                    snpOutGenePValues.add(snppValues[k]);
                                }
                            }
                        }
                    }

                    if (qqPlotAllSNP) {
                        List<SNP> snpOutGenes = chromosome.snpsOutGenes;
                        snpNum = snpOutGenes.size();
                        for (int k = 0; k < SNPpValueNameNum; k++) {
                            int pValueIndex = testedPValueIndexes[k];
                            for (int j = 0; j < snpNum; j++) {
                                SNP snp = snpOutGenes.get(j);
                                double[] snppValues = snp.getpValues();
                                if (snppValues == null) {
                                    continue;
                                }
                                if (!Double.isNaN(snppValues[pValueIndex])) {
                                    snpGenomePValues.add(snppValues[k]);
                                }
                            }
                        }
                    }

                    infor = "Chromosome " + CHROM_NAMES[chromIndex] + " scanned. " + geneNum + " genes;";
                    runningResultTopComp.insertIcon(imgFolder, "chromosome.png", infor);
                    LOG.info(infor);
                }

                int geneNum = pValueGenes.size();

                if (networkWeightGenePValue) {
                    Map<String, Double> allGeneWeightMap = calculatePPIGeneAttributeWeight();
                    double totalWeight = 0;
                    int withWeightGeneNum = 0;
                    for (int i = 0; i < geneNum; i++) {
                        PValueGene pValueGene = pValueGenes.get(i);
                        if (allGeneWeightMap.containsKey(pValueGene.getSymbol())) {
                            totalWeight += allGeneWeightMap.get(pValueGene.getSymbol());
                            withWeightGeneNum++;
                        }
                    }

                    double weightScale = withWeightGeneNum / totalWeight;
                    double sumAP = 0;
                    double adjustedP = 0;
                    for (int i = 0; i < geneNum; i++) {
                        PValueGene pValueGene = pValueGenes.get(i);

                        if (allGeneWeightMap.containsKey(pValueGene.getSymbol())) {
                            adjustedP = pValueGene.pValue / (allGeneWeightMap.get(pValueGene.getSymbol()) * weightScale);
                            if (adjustedP > 1) {
                                geneWeightedPValues.add(1);
                            } else {
                                geneWeightedPValues.add(adjustedP);
                            }
                            pValueGene.pValue = adjustedP;
                            //sumAP+=allGeneWeightMap.get(pValueGene.getSymbol()) * weightScale;                    
                        } else {
                            geneWeightedPValues.add(pValueGene.pValue);
                        }
                    }
                }

                Map<String, Double> pValueGeneMap = new HashMap<String, Double>(geneNum);

                for (int i = 0; i < geneNum; i++) {
                    PValueGene pValueGene = pValueGenes.get(i);
                    pValueGeneMap.put(pValueGene.getSymbol(), pValueGene.pValue);
                }
                gbAssoc.saveGenePValuestoDisk("MulVar", null, pValueGenes);
                gbAssoc.saveTASTESNPPValuestoDisk("TASTE", tastSNPP);

                infor = "There are " + pValueGenes.size() + " genes in total observed on the GWAS dataset.";
                //System.out.println(uniqueSNPIDs.size());
                runningResultTopComp.insertText(infor);
                LOG.info(infor);

                //draw Manhattan plotGenePValues
                if (!selectiveChromIDs.isEmpty()) {
                    ManhattanPlotPainter manhattanPlotPainter1 = new ManhattanPlotPainter(manhattanPlotWidth, manhattanPlotHeight);
                    manhattanPlotPainter1.setSelectiveChromIDs(selectiveChromIDs);
                    manhattanPlotPainter1.setManhattanPlotMinPValue(manhattanPlotMinPValue);
                    manhattanPlotPainter1.setManhattanPlotLabeGenePValue(manhattanPlotLabeGenePValue);
                    manhattanPlotPainter1.setManhattanPlotLableSNPPValue(manhattanPlotLableSNPPValue);
                    File manhattanImgFile1 = new File(imgFolder.getCanonicalPath() + File.separator + gbAssoc.getName() + ".MulVar." + "GeneManhattanPlot1" + ".png");
                    manhattanPlotPainter1.plotGenePValues(pValueGeneMap, gbAssoc.getGenome(), -Math.log10(minPValue), manhattanImgFile1.getCanonicalPath());
                    infor = "Manhattan plot of gene p-values";
                    runningResultTopComp.insertText(infor);
                    LOG.info(infor);
                    runningResultTopComp.insertImage(manhattanImgFile1);
                    manhattanImgFile1 = new File(imgFolder.getCanonicalPath() + File.separator + gbAssoc.getName() + ".MulVar." + "SNPManhattanPlot1" + ".png");
                    manhattanPlotPainter1.plotSNPPValues(gbAssoc.getGenome(), -Math.log10(minPValue), testedPValueIndexes, manhattanImgFile1.getCanonicalPath());
                    infor = "Manhattan plot of SNP p-values";
                    runningResultTopComp.insertText(infor);
                    LOG.info(infor);
                    runningResultTopComp.insertImage(manhattanImgFile1);
                }

                //code for paper writing
                //  bw.close();
                //draw qq plotGenePValues
                PValuePainter painter = new PValuePainter(qqPlotWidth, qqPlotHeight);
                File imgFile = new File(imgFolder.getCanonicalPath() + File.separator + gbAssoc.getName() + ".MulVar." + "QQPlotGene" + ".png");

                genePValues1.quickSort();
                snpInGenePValues.quickSort();
                snpGenomePValues.quickSort();
                snpOutGenePValues.quickSort();
                if (qqPlotSNPInsideGene && qqPlotSNPOutSideGene) {
                    /*
                        //As SNPs in hight LD are pruned in the new version, the message will be confusing
                    double outGeneSNPProp = snpOutGenePValues.size() * 1.0 / (snpInGenePValues.size() + snpOutGenePValues.size()) * 100;
                    infor = Util.doubleToString(100 - outGeneSNPProp, 3) + "% of SNPs are inside genes on the whole genome.";
                    runningResultTopComp.insertText(infor);
                    LOG.info(infor);
                     */
                }
                List<String> titles = new ArrayList<String>();
                List<DoubleArrayList> pvalueList = new ArrayList<DoubleArrayList>();

                titles.add("Gene p-values");
                pvalueList.add(genePValues1);
                if (networkWeightGenePValue) {
                    geneWeightedPValues.quickSort();
                    titles.add("Network weighted gene p-values");
                    pvalueList.add(geneWeightedPValues);
                }

                if (!snpGenomePValues.isEmpty() && qqPlotAllSNP) {
                    titles.add("All SNPs");
                    pvalueList.add(snpGenomePValues);
                }
                if (!snpInGenePValues.isEmpty() && qqPlotSNPInsideGene) {
                    titles.add("SNPs inside of gene");
                    pvalueList.add(snpInGenePValues);
                }
                if (!snpOutGenePValues.isEmpty() && qqPlotSNPOutSideGene) {
                    titles.add("SNPs outside of gene");
                    pvalueList.add(snpOutGenePValues);
                }
                if (!pvalueList.isEmpty()) {
                    painter.drawMultipleQQPlot(pvalueList, titles, null, imgFile.getCanonicalPath(), qqPlotMinPValue);
                    //painter.drawQQPlot(weightedSNPGenePValues, "QQ Plot of " + "gene-based pMin-values", imgFile.getCanonicalPath());
                    runningResultTopComp.insertImage(imgFile);
                }
                infor = "The multivariate gene-based association scan has been finished!";
                publish(infor);
                runningResultTopComp.insertText(infor);
                LOG.info(infor);

                pValueGenes.clear();
                genePValues1.clear();
                snpInGenePValues.clear();
                snpGenomePValues.clear();

                //genome.storeGenome2Disk();
                System.gc();
                succeed = true;
            } catch (InterruptedException ex) {
                StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

            } catch (Exception ex) {
                ex.printStackTrace();
                StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

            return null;
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
                    message = ("Gene-based association scan failed!");
                    LOG.info(message);
                    return;
                }

                GlobalManager.currentProject.addGeneBasedAssociationScan(gbAssoc);
                GlobalManager.geneAssocSetModel.addElement(gbAssoc);

                ProjectTopComponent projectTopComponent = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                projectTopComponent.showProject(GlobalManager.currentProject);

                message = ("Gene-based association scan has been finished!");
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
                runningResultTopComp.insertText(info);
                File geneAssociationResultFilePath = new File(prjWorkingPath + File.separator + prjName + File.separator
                        + gbAssoc.getName() + ".html");
                runningResultTopComp.savePane(geneAssociationResultFilePath);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private boolean handleCancel() {

        if (null == theTask) {
            return false;
        }
        return theTask.cancel();
    }
}
