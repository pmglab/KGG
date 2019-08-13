/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import cern.jet.stat.Descriptive;
import cern.jet.stat.Probability;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.logging.Logger;
import net.sf.picard.liftover.LiftOver;
import net.sf.picard.util.Interval;
import net.sf.samtools.util.AsciiLineReader;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import org.cobi.kgg.business.GeneRegionParser;
import org.cobi.kgg.business.GenotypeSetUtil;
import org.cobi.kgg.business.ManhattanPlotPainter;
import org.cobi.kgg.business.PValuePainter;
import org.cobi.kgg.business.entity.Chromosome;
import org.cobi.kgg.business.entity.Constants;

import org.cobi.kgg.business.entity.CorrelationBasedByteLDSparseMatrix;
import org.cobi.kgg.business.entity.Gene;

import org.cobi.kgg.business.entity.Project;
import org.cobi.kgg.ui.dialog.RunningResultViewerTopComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.HaplotypeDataset;
import org.cobi.kgg.business.entity.PValueFileSetting;
import org.cobi.kgg.business.entity.ReferenceGenome;
import org.cobi.kgg.business.entity.RefmRNA;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.StatusGtySet;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.ProjectTopComponent;
import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.file.Zipper;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.text.LocalFile;
import org.cobi.util.text.Util;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.WindowManager;

/**
 *
 * @author MXLi
 */
public class BuildGenome implements ActionListener, Constants {
//reference http://rubenlaguna.com/wp/2010/01/18/cancellable-tasks-and-progress-indicators-netbeans-platform/index.html/

  private final static RequestProcessor RP = new RequestProcessor("Building analysis genome progress", 1, true);
  private final static Logger LOG = Logger.getLogger(BuildGenome.class.getName());
  private RequestProcessor.Task buildTask = null;
  boolean succeed = false;
  RunningResultViewerTopComponent runningResultTopComp;
  Genome genome;
  Project project;
  PValueFileSetting pvSetting;
  long startTime, endTime;
  File imgFolder;

  public BuildGenome(Genome genome, Project project, RunningResultViewerTopComponent runningResultTopComp) {
    this.project = project;
    this.genome = genome;

    this.pvSetting = genome.getpValueSourceSetting();
    this.runningResultTopComp = runningResultTopComp;
    String prjName = project.getName();
    String prjWorkingPath = project.getWorkingPath();
    imgFolder = new File(prjWorkingPath + File.separator + prjName + File.separator + "image" + File.separator);
    if (!imgFolder.exists()) {
      imgFolder.mkdirs();
    }

    /*
         String imgExten = im.getName().substring(im.getName().indexOf('.') + 1);
         ImageIcon imageIcon = InterfaceUtil.readImageIcon(iconPath);
         * 
     */
    try {
      // ImageIO.write(rendered, imgExten, im);
    } catch (Exception ex) {
      ErrorManager.getDefault().notify(ex);
    }
  }

  /**
   *
   * @param tmpChromosomes
   * @return
   * @throws Exception
   */
  public void mapAndStorePValuesOntoGenomeByCoordinates(List<HashMap<String, SNP>> tmpChromosomes) throws Exception {
    Map<String, int[]> rssnpGenomeIndexes;
    Map<String, int[]> geneGenomeIndexes;
    Map<String, int[]> positionGenomeIndexes;
    int snpSizeInGene = 0;
    int snpSizeOutGene = 0;

    int storedGeneIndex;
    SNP gSNP;
    String rsID;
    Gene gene;

    int extendGene5PLenInt = (int) (genome.getExtendedGene5PLen() * 1000);
    int extendGene3PLenInt = (int) (genome.getExtendedGene3PLen() * 1000);

    rssnpGenomeIndexes = new HashMap<String, int[]>();
    positionGenomeIndexes = new HashMap<String, int[]>();
    geneGenomeIndexes = new HashMap<String, int[]>();

    String geneEntrezID = null;

    File chekPath = new File(genome.getStoragePath());
    if (!chekPath.exists()) {
      chekPath.mkdir();
    }

    boolean toExclude = false;
    int regionNum = 0;

    File CHAIN_FILE = null;
    boolean needCovertCord = false;
    Zipper ziper = new Zipper();
    LiftOver liftOverRefGenome2pValueFile = null;
    if (!pvSetting.getPosGenomeVersion().equals(genome.getFinalBuildGenomeVersion())) {
      CHAIN_FILE = new File(GlobalManager.RESOURCE_PATH + "liftOver/" + genome.getFinalBuildGenomeVersion() + "ToH" + pvSetting.getPosGenomeVersion().substring(1) + ".over.chain.gz");
      if (!CHAIN_FILE.exists()) {
        if (!CHAIN_FILE.getParentFile().exists()) {
          CHAIN_FILE.getParentFile().mkdirs();
        }
        String url = "http://hgdownload.cse.ucsc.edu/goldenPath/" + genome.getFinalBuildGenomeVersion() + "/liftOver/" + CHAIN_FILE.getName();
        //  HttpClient4API.downloadAFile(url, CHAIN_FILE);
        // HttpClient4API.simpleRetriever(url, CHAIN_FILE.getCanonicalPath());
        //ziper.extractTarGz(CHAIN_FILE.getCanonicalPath(), CHAIN_FILE.getParent());
      }
      CHAIN_FILE = new File(GlobalManager.RESOURCE_PATH + "liftOver/" + genome.getFinalBuildGenomeVersion() + "ToH" + pvSetting.getPosGenomeVersion().substring(1) + ".over.chain");
      liftOverRefGenome2pValueFile = new LiftOver(CHAIN_FILE);
      needCovertCord = true;
    }
    LiftOver liftOverLDGenome2pValueFile = null;
    if (genome.getLdFileGenomeVersion() != null && !pvSetting.getPosGenomeVersion().equals(genome.getLdFileGenomeVersion())) {
      CHAIN_FILE = new File(GlobalManager.RESOURCE_PATH + "liftOver/" + genome.getLdFileGenomeVersion() + "ToH" + pvSetting.getPosGenomeVersion().substring(1) + ".over.chain.gz");
      if (!CHAIN_FILE.exists()) {
        if (!CHAIN_FILE.getParentFile().exists()) {
          CHAIN_FILE.getParentFile().mkdirs();
        }
        String url = "http://hgdownload.cse.ucsc.edu/goldenPath/" + genome.getLdFileGenomeVersion() + "/liftOver/" + CHAIN_FILE.getName();
        //HttpClient4API.downloadAFile(url, CHAIN_FILE);
        HttpClient4API.simpleRetriever(url, CHAIN_FILE.getCanonicalPath(), GlobalManager.proxyBean);
        ziper.extractTarGz(CHAIN_FILE.getCanonicalPath(), CHAIN_FILE.getParent());
      }
      CHAIN_FILE = new File(GlobalManager.RESOURCE_PATH + "liftOver/" + genome.getLdFileGenomeVersion() + "ToH" + pvSetting.getPosGenomeVersion().substring(1) + ".over.chain");
      liftOverLDGenome2pValueFile = new LiftOver(CHAIN_FILE);
    }

    GeneRegionParser grp = new GeneRegionParser();
    ReferenceGenome refGenome = null;
    boolean isCustomGene = false;

    boolean hasNoRefGene = true;
    boolean hasMapVarG = false;
    Map<String, String[]> varGeneMap = new HashMap<String, String[]>();
    if (genome.getGeneVarMapFile().length() > 0) {
      hasMapVarG = true;
      readGeneVarMapFile(genome.getGeneVarMapFile(), varGeneMap);
    }
    int[] indices = {1, 9};
    ArrayList<String[]> allGenes = new ArrayList<String[]>();
    //it often meeting errors when 
    //  LocalFile.retrieveData(GlobalManager.RESOURCE_PATH + "HgncGene.txt", allGenes, indices, "\t");

    BufferedReader br = LocalFileFunc.getBufferedReader(GlobalManager.RESOURCE_PATH + "HgncGene.txt.gz");
    String line = null;
    String[] cells = null;
    String[] row = null;

    line = br.readLine();
    int symbIndex = -1;
    int catIndex = -1;
    int enGIndex = -1;
    cells = line.split("\t", -1);
    for (int i = 0; i < cells.length; i++) {
      if (cells[i].equals("Approved Symbol")) {
        symbIndex = i;
      } else if (cells[i].equals("Locus Group")) {
        catIndex = i;
      }
    }
    while ((line = br.readLine()) != null) {
      //line = line.trim();
      if (line.trim().length() == 0) {
        continue;
      }
      // System.out.println(line);
      cells = line.split("\t", -1);
      if (cells.length <= catIndex) {
        //  System.out.println(line);
        continue;
      }
      row = new String[2];
      row[0] = cells[symbIndex];
      row[1] = cells[catIndex];
      allGenes.add(row);

    }
    br.close();

    Map<String, Byte> groupID = new HashMap<String, Byte>();
    for (byte i = 0; i < geneGroups.length; i++) {
      groupID.put(geneGroups[i], i);
    }
    int[] geneGroupCounts = new int[geneGroups.length];
    Arrays.fill(geneGroupCounts, 0);
    Map<String, Byte> genesGroupMap = new HashMap<String, Byte>();
    for (String[] item : allGenes) {
      if (item[1] == null || item[1].isEmpty()) {
        continue;
      }
      genesGroupMap.put(item[0], groupID.get(item[1]));
    }

    boolean isExclusionModel = genome.isExclusionModel();
    int geneNum = 0;

    boolean toInclude;
    boolean includeWhole = false;
    boolean excludeWhole = false;
    boolean hasGeneDB = false;
    if (genome.getGeneDB() != null) {
      if (genome.getGeneDB().equals("RefGene")) {
        refGenome = grp.readRefGene(GlobalManager.RESOURCE_PATH + "/" + genome.getFinalBuildGenomeVersion() + "_refGene.txt.gz", 2, extendGene5PLenInt, extendGene3PLenInt, liftOverRefGenome2pValueFile,
            genome.getFinalBuildGenomeVersion() + " to " + pvSetting.getPosGenomeVersion());
        hasNoRefGene = true;
      } else if (genome.getGeneDB().equals("GEncode")) {
        if (genome.getFinalBuildGenomeVersion().equals("hg19")) {
          refGenome = grp.readRefGene(GlobalManager.RESOURCE_PATH + "/" + genome.getFinalBuildGenomeVersion() + "_gencode.txt.gz", 2, extendGene5PLenInt, extendGene3PLenInt, liftOverRefGenome2pValueFile,
              genome.getFinalBuildGenomeVersion() + " to " + pvSetting.getPosGenomeVersion());
          hasNoRefGene = true;
        } else if (genome.getFinalBuildGenomeVersion().equals("hg38")) {
          refGenome = grp.readRefGene(GlobalManager.RESOURCE_PATH + "/" + genome.getFinalBuildGenomeVersion() + "_gencode.txt.gz", 2, extendGene5PLenInt, extendGene3PLenInt, liftOverRefGenome2pValueFile,
              genome.getFinalBuildGenomeVersion() + " to " + pvSetting.getPosGenomeVersion());
          hasNoRefGene = true;
        }
      } else if (genome.getGeneDB().length() > 0) {
        refGenome = grp.readCustomGene(genome.getGeneDB(), liftOverLDGenome2pValueFile, genome.getFinalBuildGenomeVersion() + " to " + pvSetting.getPosGenomeVersion());
        isCustomGene = true;
        hasNoRefGene = true;
      }
      hasGeneDB = true;
    }

//CHROM_NAMES.length
    for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
      HashMap<String, SNP> tmpChromosome = tmpChromosomes.get(chromIndex);
      if (tmpChromosome == null || tmpChromosome.isEmpty()) {
        continue;
      }
      List<SNP> snpList = new ArrayList<SNP>(tmpChromosome.values());
      //no annotation for SNPs br XY chromsome
      if (CHROM_NAMES[chromIndex].equals("XY")) {
        continue;
      }
      geneNum = 0;
      IntArrayList selectedRegions = genome.getExcludeRegionMap().get(CHROM_NAMES[chromIndex]);
      if (selectedRegions != null) {
        regionNum = selectedRegions.size() / 2;
        if (isExclusionModel) {
          excludeWhole = false;
          for (int i = 0; i < regionNum; i++) {
            if (selectedRegions.get(i * 2) == -9 && selectedRegions.get(i * 2 + 1) == Integer.MAX_VALUE) {
              excludeWhole = true;
              break;
            }
            if (needCovertCord) {
              if (selectedRegions.get(i * 2) != -9) {
                Interval interval = new Interval("chr" + CHROM_NAMES[chromIndex], (int) selectedRegions.get(i * 2), (int) selectedRegions.get(i * 2));
                Interval int2 = liftOverRefGenome2pValueFile.liftOver(interval);
                if (int2 != null) {
                  selectedRegions.set(i * 2, int2.getStart());
                }
              }
              if (selectedRegions.get(i * 2 + 1) != Integer.MAX_VALUE) {
                Interval interval = new Interval("chr" + CHROM_NAMES[chromIndex], (int) selectedRegions.get(i * 2 + 1), (int) selectedRegions.get(i * 2 + 1));
                Interval int2 = liftOverRefGenome2pValueFile.liftOver(interval);
                if (int2 != null) {
                  selectedRegions.set(i * 2 + 1, int2.getStart());
                }
              }
            }
          }
          if (excludeWhole) {
            String infor = "All SNPs and genes on chromosome " + CHROM_NAMES[chromIndex] + " are excluded as you specified!";
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
            continue;
          }
        } else {
          includeWhole = false;
          for (int i = 0; i < regionNum; i++) {
            if (selectedRegions.get(i * 2) == -9 && selectedRegions.get(i * 2 + 1) == Integer.MAX_VALUE) {
              includeWhole = true;
              break;
            }
            if (needCovertCord) {
              if (selectedRegions.get(i * 2) != -9) {
                Interval interval = new Interval("chr" + CHROM_NAMES[chromIndex], (int) selectedRegions.get(i * 2), (int) selectedRegions.get(i * 2));
                Interval int2 = liftOverRefGenome2pValueFile.liftOver(interval);
                if (int2 != null) {
                  selectedRegions.set(i * 2, int2.getStart());
                }
              }
              if (selectedRegions.get(i * 2 + 1) != Integer.MAX_VALUE) {
                Interval interval = new Interval("chr" + CHROM_NAMES[chromIndex], (int) selectedRegions.get(i * 2 + 1), (int) selectedRegions.get(i * 2 + 1));
                Interval int2 = liftOverRefGenome2pValueFile.liftOver(interval);
                if (int2 != null) {
                  selectedRegions.set(i * 2 + 1, int2.getStart());
                }
              }
            }
          }

        }
      }

      HashSet<String> excludedRSIDs = new HashSet<String>();
      CorrelationBasedByteLDSparseMatrix ldRsMatrix = null;

      storedGeneIndex = 0;
      Chromosome chromosome = new Chromosome(chromIndex, CHROM_NAMES[chromIndex]);
      int snpNum = snpList.size();
      Set<Integer> hasAddedGeneIndexes = new HashSet<Integer>();
      int finalSNPWithinGeneNum = 0;
      boolean aNewMappedVar = false;
      if (hasGeneDB) {
        for (int i = 0; i < snpNum; i++) {
          gSNP = snpList.get(i);
          // System.out.println(gSNP.rsID); 
          //CCL2 rs2530797
          if (selectedRegions != null) {
            if (isExclusionModel) {
              toExclude = false;
              for (int j = 0; j < regionNum; j++) {
                if (gSNP.physicalPosition < selectedRegions.get(j * 2) || gSNP.physicalPosition > selectedRegions.get(j * 2 + 1)) {
                } else {
                  toExclude = true;
                  break;
                }
              }
              if (toExclude) {
                excludedRSIDs.add(CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition);
                continue;
              }
            } else if (!includeWhole) {
              toInclude = false;
              for (int j = 0; j < regionNum; j++) {
                if (gSNP.physicalPosition >= selectedRegions.get(j * 2) && gSNP.physicalPosition <= selectedRegions.get(j * 2 + 1)) {
                  toInclude = true;
                  break;
                }
              }
              if (!toInclude) {
                excludedRSIDs.add(CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition);
                continue;
              }
            }
          } else if (!isExclusionModel) {
            continue;
          }

          //subject to amend because an SNP may belongs to multiple genes; Now I only condsider one gene
          Set<String> mrnaLabels;
          if (isCustomGene) {
            mrnaLabels = refGenome.getVarFeatureCustomGene(CHROM_NAMES[chromIndex], gSNP);
          } else {
            mrnaLabels = refGenome.getVarFeature(CHROM_NAMES[chromIndex], gSNP);
          }

          String posLabel = CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition;

          hasAddedGeneIndexes.clear();
          //trust the mapping of dbSNP first
          if (mrnaLabels != null && !mrnaLabels.isEmpty()) {
            for (String mrnaLable : mrnaLabels) {
              RefmRNA refGene = refGenome.getmRNA(mrnaLable);
              //note a gene could have multiple mRNAs
              //Here I just use the widest bounaries
              int[] geneIndexes = geneGenomeIndexes.get(refGene.getGeneSymb());

              if (geneIndexes == null) {
                gene = new Gene();
                gene.setEntrezID(-1);
                Byte gi = genesGroupMap.get(refGene.getGeneSymb());
                if (gi != null) {
                  gene.setGeneGroupID(gi);
                  geneGroupCounts[gi]++;
                } else {
                  geneGroupCounts[geneGroupCounts.length - 1]++;
                }
                gene.start = refGene.getStart();
                gene.end = refGene.getEnd();
                gene.setSymbol(refGene.getGeneSymb());
                chromosome.addGene(gene);
                //very important: the chromIndexInFile must be consistent with the indexesRS br CHROM_NAMES!!!!!
                geneGenomeIndexes.put(refGene.getGeneSymb(), new int[]{chromIndex, storedGeneIndex});
                gene.addSNP(gSNP);

                //a SNP can belong to different genes
                if (!positionGenomeIndexes.containsKey(posLabel)) {
                  positionGenomeIndexes.put(posLabel, new int[]{chromIndex, storedGeneIndex, 0});
                  rssnpGenomeIndexes.put(gSNP.rsID, new int[]{chromIndex, storedGeneIndex, 0});
                  if (gSNP.orgRsID != null) {
                    //allow duplicate identification
                    rssnpGenomeIndexes.put(gSNP.orgRsID, new int[]{chromIndex, storedGeneIndex, 0});
                  }
                } else {
                  //multiple mapping index wil be very few cases
                  // System.out.println(rsID);                                        
                  int[] indexesPos = positionGenomeIndexes.get(posLabel);
                  int orgLen = indexesPos.length;
                  boolean isInSameGene = false;
                  for (int ss = 0; ss < indexesPos.length; ss += 3) {
                    if (indexesPos[ss] == chromIndex && indexesPos[ss + 1] == storedGeneIndex) {
                      isInSameGene = true;
                      break;
                    }
                  }

                  if (!isInSameGene) {
                    int[] newIndexPOS = new int[orgLen + 3];
                    System.arraycopy(indexesPos, 0, newIndexPOS, 0, orgLen);
                    newIndexPOS[orgLen] = chromIndex;
                    newIndexPOS[orgLen + 1] = storedGeneIndex;
                    newIndexPOS[orgLen + 2] = 0;
                    positionGenomeIndexes.put(posLabel, newIndexPOS);

                    int[] indexesRS = rssnpGenomeIndexes.get(gSNP.rsID);
                    int[] newIndexRS = new int[orgLen + 3];
                    System.arraycopy(indexesRS, 0, newIndexRS, 0, orgLen);
                    newIndexRS[orgLen] = chromIndex;
                    newIndexRS[orgLen + 1] = storedGeneIndex;
                    newIndexRS[orgLen + 2] = 0;
                    rssnpGenomeIndexes.put(gSNP.rsID, newIndexRS);

                    if (gSNP.orgRsID != null) {
                      //allow duplicate identification
                      rssnpGenomeIndexes.put(gSNP.orgRsID, newIndexRS);
                    }
                  }
                }


                /*
                             * if (gSNP.orgRsID != null) {
                             //allow duplicate identification
                             rssnpGenomeIndexes.put(gSNP.orgRsID, new int[]{chromIndex, storedGeneIndex, 0});
                             positionGenomeIndexes.put(CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition, new int[]{chromIndex, storedGeneIndex, 0});
                             }
                 */
                snpSizeInGene++;
                geneNum++;

                hasAddedGeneIndexes.add(storedGeneIndex);
                storedGeneIndex++;
              } else if (geneIndexes[0] != chromIndex) {
                //br this situation it is may be the duplicated gene symbole
                // String infor = "Warning!! The gene " + mrnaLable + " is mapped on chromosomes " + CHROM_NAMES[geneIndexes[0]] + " and " + CHROM_NAMES[chromIndex];
                // GlobalManager.mainView.insertText(infor);
                // GlobalManager.addInforLog(infor);
                /*
                             //define a new gene
                             gene = new Gene();
                             gene.setEntrezID(-1);
                            
                             gene.start = refGene.getStart();
                             gene.end = refGene.getEnd();
                             gene.setSymbol(refGene.getGeneSymb());
                             chromosome.addGene(gene);
                             geneGenomeIndexes.remove(mrnaLable);
                             //very important: the chromIndexInFile must be consistent with the indexesRS br CHROM_NAMES!!!!!
                             geneGenomeIndexes.put(mrnaLable, new int[]{chromIndex, storedGeneIndex});
                             gene.addSNP(gSNP);
                             rssnpGenomeIndexes.put(gSNP.rsID, new int[]{chromIndex, storedGeneIndex, 0});
                             positionGenomeIndexes.put(CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition, new int[]{chromIndex, storedGeneIndex, 0});
                             snpSizeInGene++;
                             if (gSNP.orgRsID != null) {
                             //allow duplicate identification
                             rssnpGenomeIndexes.put(gSNP.orgRsID, new int[]{chromIndex, storedGeneIndex, 0});
                             positionGenomeIndexes.put(CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition, new int[]{chromIndex, storedGeneIndex, 0});
                             }
                             geneNum++;
                             storedGeneIndex++;
                 */
              } else {
                //a SNP can belong to different genes trasncirpts or even genes
                gene = chromosome.genes.get(geneIndexes[1]);
                if (hasAddedGeneIndexes.contains(geneIndexes[1])) {
                  //use the widest bounaries
                  if (gene.start < refGene.getStart()) {
                    gene.start = refGene.getStart();
                  }
                  if (gene.end > refGene.getEnd()) {
                    gene.end = refGene.getEnd();
                  }
                  continue;
                }
                int[] positions = positionGenomeIndexes.get(posLabel);
                if (positions != null) {
                  //if the SNP have been in the gene
                  if (positions[1] == geneIndexes[1]) {
                    continue;
                  }
                }

                int currentSNPIndex = gene.snps.size();
                if (!positionGenomeIndexes.containsKey(posLabel)) {
                  positionGenomeIndexes.put(posLabel, new int[]{chromIndex, geneIndexes[1], currentSNPIndex});
                  rssnpGenomeIndexes.put(gSNP.rsID, new int[]{chromIndex, geneIndexes[1], currentSNPIndex});
                  if (gSNP.orgRsID != null) {
                    //allow duplicate identification
                    rssnpGenomeIndexes.put(gSNP.orgRsID, new int[]{chromIndex, geneIndexes[1], currentSNPIndex});
                  }
                } else {
                  //multiple mapping index wil be very few cases
                  // System.out.println(rsID);                                        
                  int[] indexesPos = positionGenomeIndexes.get(posLabel);
                  int orgLen = indexesPos.length;
                  boolean isInSameGene = false;
                  for (int ss = 0; ss < indexesPos.length; ss += 3) {
                    if (indexesPos[ss] == chromIndex && indexesPos[ss + 1] == geneIndexes[1]) {
                      isInSameGene = true;
                      break;
                    }
                  }

                  if (!isInSameGene) {
                    int[] newIndexPOS = new int[orgLen + 3];
                    System.arraycopy(indexesPos, 0, newIndexPOS, 0, orgLen);
                    newIndexPOS[orgLen] = chromIndex;
                    newIndexPOS[orgLen + 1] = geneIndexes[1];
                    newIndexPOS[orgLen + 2] = currentSNPIndex;
                    positionGenomeIndexes.put(posLabel, newIndexPOS);

                    int[] indexesRS = rssnpGenomeIndexes.get(gSNP.rsID);
                    int[] newIndexRS = new int[orgLen + 3];
                    System.arraycopy(indexesRS, 0, newIndexRS, 0, orgLen);
                    newIndexRS[orgLen] = chromIndex;
                    newIndexRS[orgLen + 1] = geneIndexes[1];
                    newIndexRS[orgLen + 2] = currentSNPIndex;
                    rssnpGenomeIndexes.put(gSNP.rsID, newIndexRS);

                    if (gSNP.orgRsID != null) {
                      //allow duplicate identification
                      rssnpGenomeIndexes.put(gSNP.orgRsID, newIndexRS);
                    }
                  }
                }

                snpSizeInGene++;
                /*
                             if (gSNP.orgRsID != null) {
                             //allow duplicate identification
                             rssnpGenomeIndexes.put(gSNP.orgRsID, new int[]{chromIndex, geneIndexes[1], gene.snps.size()});
                             positionGenomeIndexes.put(CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition, new int[]{chromIndex, geneIndexes[1], gene.snps.size()});
                             }
                             * 
                 */
                //use the widest bounaries
                if (gene.start < refGene.getStart()) {
                  gene.start = refGene.getStart();
                }
                if (gene.end > refGene.getEnd()) {
                  gene.end = refGene.getEnd();
                }
                hasAddedGeneIndexes.add(geneIndexes[1]);
                gene.addSNP(gSNP);
              }
            }
            finalSNPWithinGeneNum++;
          } else {
            snpSizeOutGene++;
            chromosome.snpsOutGenes.add(gSNP);

          }
        }
      }

      int index;
      //Use the gene-var file to re-map the variants onto genes
      if (hasMapVarG) {
        for (int i = 0; i < snpNum; i++) {
          gSNP = snpList.get(i);
          // System.out.println(gSNP.rsID); 
          //CCL2 rs2530797
          if (selectedRegions != null) {
            if (isExclusionModel) {
              toExclude = false;
              for (int j = 0; j < regionNum; j++) {
                if (gSNP.physicalPosition < selectedRegions.get(j * 2) || gSNP.physicalPosition > selectedRegions.get(j * 2 + 1)) {
                } else {
                  toExclude = true;
                  break;
                }
              }
              if (toExclude) {
                excludedRSIDs.add(CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition);
                continue;
              }
            } else if (!includeWhole) {
              toInclude = false;
              for (int j = 0; j < regionNum; j++) {
                if (gSNP.physicalPosition >= selectedRegions.get(j * 2) && gSNP.physicalPosition <= selectedRegions.get(j * 2 + 1)) {
                  toInclude = true;
                  break;
                }
              }
              if (!toInclude) {
                excludedRSIDs.add(CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition);
                continue;
              }
            }
          } else if (!isExclusionModel) {
            continue;
          }

          aNewMappedVar = false;
          String posLabel = CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition;

          String[] geneS = varGeneMap.get(posLabel);
          if (geneS == null) {
            if (!hasGeneDB) {
              snpSizeOutGene++;
              chromosome.snpsOutGenes.add(gSNP);
            }
            continue;
          }

          for (String g : geneS) {
            if (g == null) {
              continue;
            }

            int[] geneIndexes = geneGenomeIndexes.get(g);
            if (geneIndexes == null) {
              gene = new Gene();
              gene.setEntrezID(-1);
              Byte gi = genesGroupMap.get(g);
              if (gi != null) {
                gene.setGeneGroupID(gi);
                geneGroupCounts[gi]++;
              } else {
                geneGroupCounts[geneGroupCounts.length - 1]++;
              }
              gene.setSymbol(g);
              chromosome.addGene(gene);
              //very important: the chromIndexInFile must be consistent with the indexesRS br CHROM_NAMES!!!!!
              geneGenomeIndexes.put(g, new int[]{chromIndex, storedGeneIndex});
              gene.addSNP(gSNP);

              //a SNP can belong to different genes
              if (!positionGenomeIndexes.containsKey(posLabel)) {
                positionGenomeIndexes.put(posLabel, new int[]{chromIndex, storedGeneIndex, 0});
                rssnpGenomeIndexes.put(gSNP.rsID, new int[]{chromIndex, storedGeneIndex, 0});
                if (gSNP.orgRsID != null) {
                  //allow duplicate identification
                  rssnpGenomeIndexes.put(gSNP.orgRsID, new int[]{chromIndex, storedGeneIndex, 0});
                }
              } else {
                //multiple mapping index wil be very few cases
                // System.out.println(rsID);                                        
                int[] indexesPos = positionGenomeIndexes.get(posLabel);
                int orgLen = indexesPos.length;
                boolean isInSameGene = false;
                for (int ss = 0; ss < indexesPos.length; ss += 3) {
                  if (indexesPos[ss] == chromIndex && indexesPos[ss + 1] == storedGeneIndex) {
                    isInSameGene = true;
                    break;
                  }
                }

                if (!isInSameGene) {
                  int[] newIndexPOS = new int[orgLen + 3];
                  System.arraycopy(indexesPos, 0, newIndexPOS, 0, orgLen);
                  newIndexPOS[orgLen] = chromIndex;
                  newIndexPOS[orgLen + 1] = storedGeneIndex;
                  newIndexPOS[orgLen + 2] = 0;
                  positionGenomeIndexes.put(posLabel, newIndexPOS);

                  int[] indexesRS = rssnpGenomeIndexes.get(gSNP.rsID);
                  int[] newIndexRS = new int[orgLen + 3];
                  System.arraycopy(indexesRS, 0, newIndexRS, 0, orgLen);
                  newIndexRS[orgLen] = chromIndex;
                  newIndexRS[orgLen + 1] = storedGeneIndex;
                  newIndexRS[orgLen + 2] = 0;
                  rssnpGenomeIndexes.put(gSNP.rsID, newIndexRS);

                  if (gSNP.orgRsID != null) {
                    //allow duplicate identification
                    rssnpGenomeIndexes.put(gSNP.orgRsID, newIndexRS);
                  }
                }
              }


              /*
                             * if (gSNP.orgRsID != null) {
                             //allow duplicate identification
                             rssnpGenomeIndexes.put(gSNP.orgRsID, new int[]{chromIndex, storedGeneIndex, 0});
                             positionGenomeIndexes.put(CHROM_NAMES[chromIndex] + ":" + gSNP.physicalPosition, new int[]{chromIndex, storedGeneIndex, 0});
                             }
               */
              snpSizeInGene++;
              geneNum++;

              storedGeneIndex++;
              aNewMappedVar = true;
            } else {
              //a SNP can belong to different genes trasncirpts or even genes
              gene = chromosome.genes.get(geneIndexes[1]);

              int[] positions = positionGenomeIndexes.get(posLabel);
              if (positions != null) {
                //if the SNP have been in the gene
                if (positions[1] == geneIndexes[1]) {
                  continue;
                }
              }

              int currentSNPIndex = gene.snps.size();
              if (!positionGenomeIndexes.containsKey(posLabel)) {
                positionGenomeIndexes.put(posLabel, new int[]{chromIndex, geneIndexes[1], currentSNPIndex});
                rssnpGenomeIndexes.put(gSNP.rsID, new int[]{chromIndex, geneIndexes[1], currentSNPIndex});
                if (gSNP.orgRsID != null) {
                  //allow duplicate identification
                  rssnpGenomeIndexes.put(gSNP.orgRsID, new int[]{chromIndex, geneIndexes[1], currentSNPIndex});
                }
              } else {
                //multiple mapping index wil be very few cases
                // System.out.println(rsID);                                        
                int[] indexesPos = positionGenomeIndexes.get(posLabel);
                int orgLen = indexesPos.length;
                boolean isInSameGene = false;
                for (int ss = 0; ss < indexesPos.length; ss += 3) {
                  if (indexesPos[ss] == chromIndex && indexesPos[ss + 1] == geneIndexes[1]) {
                    isInSameGene = true;
                    break;
                  }
                }

                if (!isInSameGene) {
                  int[] newIndexPOS = new int[orgLen + 3];
                  System.arraycopy(indexesPos, 0, newIndexPOS, 0, orgLen);
                  newIndexPOS[orgLen] = chromIndex;
                  newIndexPOS[orgLen + 1] = geneIndexes[1];
                  newIndexPOS[orgLen + 2] = currentSNPIndex;
                  positionGenomeIndexes.put(posLabel, newIndexPOS);

                  int[] indexesRS = rssnpGenomeIndexes.get(gSNP.rsID);
                  int[] newIndexRS = new int[orgLen + 3];
                  System.arraycopy(indexesRS, 0, newIndexRS, 0, orgLen);
                  newIndexRS[orgLen] = chromIndex;
                  newIndexRS[orgLen + 1] = geneIndexes[1];
                  newIndexRS[orgLen + 2] = currentSNPIndex;
                  rssnpGenomeIndexes.put(gSNP.rsID, newIndexRS);

                  if (gSNP.orgRsID != null) {
                    //allow duplicate identification
                    rssnpGenomeIndexes.put(gSNP.orgRsID, newIndexRS);
                  }
                }
              }

              gene.addSNP(gSNP);
            }
          }

          if (aNewMappedVar) {
            snpSizeInGene++;
            if (hasGeneDB) {
              snpSizeOutGene--;
            }
          }
        }
      }

      StringBuilder info = new StringBuilder();
      String outInfor = null;
      if (excludedRSIDs.size() > 0) {
        outInfor = excludedRSIDs.size() + " SNPs are excluded by your setting.\n";
        info.append(outInfor);
      }

      //ld source code
      //-2 others LD
      //0 genotype plink binary file
      //1 hapap ld
      //2 1kG haplomap
      //3 local LD calcualted by plink
      //4 1kG haplomap vcf format
      //assume the hapmap use the lastest rs ID
      if (genome.getLdSourceCode() == 0) {
        ldRsMatrix = calculateLocalLDRSquarebyGenotypesInGene(chromosome, positionGenomeIndexes, CHROM_NAMES[chromIndex], false);
      } else if (genome.getLdSourceCode() == 1) {
        ldRsMatrix = readHapMapLDRSquareByPositions(genome.getChromLDFiles()[chromIndex], chromosome, positionGenomeIndexes, liftOverLDGenome2pValueFile, finalSNPWithinGeneNum, genome.getMinEffectiveR2());
      } else if (genome.getLdSourceCode() == 2) {
        // ldRsMatrix = calculateLocalLDRSquarebyHaplotypeInGene(chromosome, positionGenomeIndexes, CHROM_NAMES[chromIndex], genome.getHaploMapFilesList().get(chromIndex), liftOverLDGenome2pValueFile);
      } else if (genome.getLdSourceCode() == 3) {
        //for local LD currently it is assume these is only one LD file
        //this source will be removed
      } else if (genome.getLdSourceCode() == 4) {
        ldRsMatrix = calculateLocalLDRSquarebyHaplotypeVCFInGene(chromosome, positionGenomeIndexes, CHROM_NAMES[chromIndex], genome.getChromLDFiles()[chromIndex], liftOverLDGenome2pValueFile);
      }
      System.gc();
      positionGenomeIndexes.clear();

      //store information by chromosomes
      genome.writeChromosomeAndLDToDiskClean(chromosome, ldRsMatrix, rssnpGenomeIndexes);

      ldRsMatrix = null;
      chromosome = null;
      System.gc();
      outInfor = "Chromosome " + CHROM_NAMES[chromIndex] + " is finished; #Gene " + geneNum;
      info.append(outInfor);
      runningResultTopComp.insertIcon(imgFolder, "chromosome.png", outInfor.toString());
      LOG.info(info.toString());
    }
    double protion = snpSizeInGene * 100.0 / (snpSizeInGene + snpSizeOutGene);
    String info = "#Gene: " + geneGenomeIndexes.size() + " #SNPs in Gene: " + snpSizeInGene + "(=" + Util.roundDouble(protion, 2) + "%) #SNPs beyond Gene: "
        + snpSizeOutGene + "(=" + Util.roundDouble(100 - protion, 2) + "%)";
    runningResultTopComp.insertText(info);
    LOG.info(info);

    StringBuilder sb = new StringBuilder("Gene groups: ");
    for (int i = 0; i < geneGroups.length; i++) {
      info = (geneGroups[i] + " (" + geneGroupCounts[i] + ") ");
      sb.append(info);
    }
    runningResultTopComp.insertText(sb.toString());
    LOG.info(sb.toString());

    genome.setGeneSumCount(geneGroupCounts);
    //store  all index information
    genome.writeSNPGeneIndexToDisk(geneGenomeIndexes);
    rssnpGenomeIndexes = null;
    geneGenomeIndexes = null;
    positionGenomeIndexes = null;

    if (genome.isToAdjustPValue()) {
      //after adjustedment of the z-score and chi-square have been converted into p-values
      if (pvSetting.getTestInputType().equals("z-scores")) {
        genomicControlAdjustmentWithZScore(genome);
      } else if (pvSetting.getTestInputType().equals("p-values")) {
        genomicControlAdjustmentWithPValues(genome);
      } else if (pvSetting.getTestInputType().equals("chi-square")) {
        genomicControlAdjustmentWithChiSquare(genome);
      }

    } else {
      //Note this function will convert (if any) z-scores br to chis-quares to p-values.
      storeAndDrawPValueQQPlot(genome);
    }

  }

  public int genomicControlAdjustmentWithChiSquare(Genome genome) throws Exception {
    // TODO: implement
    StringBuilder tmpBuffer = new StringBuilder();

    PValuePainter painter = new PValuePainter(600, 400);
    List<String> titles = new ArrayList<String>();

    List<DoubleArrayList> pValueArrayLists = new ArrayList<DoubleArrayList>();
    String inf = "Adjusting p-values by genomic control method...";
    runningResultTopComp.insertText(inf);
    LOG.info(inf);

    int pValueNum = pvSetting.getsNPPValueIndexes().length;
    double chiSquare = 0;
    int snpNum;
    int[] pIndexes = new int[pValueNum];
    HashSet<Integer> selectiveChromIDs = new HashSet<Integer>();
    double minPValue = 1;

    for (int pIndex = 0; pIndex < pValueNum; pIndex++) {
      String name = genome.getpValueNames()[pIndex];
      pIndexes[pIndex] = pIndex;
      pValueArrayLists.add(new DoubleArrayList());
      pValueArrayLists.add(new DoubleArrayList());

      titles.add("Original " + name);
      titles.add("Adjusted " + name);
      for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
        Chromosome chromosome = genome.readChromosomefromDisk(chromIndex);
        if (chromosome == null || chromosome.genes.isEmpty()) {
          continue;
        }

        List<Gene> genes = chromosome.genes;
        int geneNum = genes.size();

        for (int i = 0; i < geneNum; i++) {
          Gene gene = genes.get(i);
          List<SNP> snps = gene.snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            if (mSNP.hasAdjustedP) {
              continue;
            }
            double[] snppValues = mSNP.getpValues();
            if (snppValues == null) {
              continue;
            }
            chiSquare = snppValues[pIndex];

            if (!Double.isNaN(chiSquare)) {
              pValueArrayLists.get(pIndex * 2).add(chiSquare);
            }
            mSNP.hasAdjustedP = true;
          }
        }

        List<SNP> snpOutGenes = chromosome.snpsOutGenes;
        snpNum = snpOutGenes.size();
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          if (mSNP.hasAdjustedP) {
            continue;
          }
          double[] snppValues = mSNP.getpValues();
          if (snppValues == null) {
            continue;
          }
          chiSquare = snppValues[pIndex];
          if (!Double.isNaN(chiSquare)) {
            pValueArrayLists.get(pIndex * 2).add(chiSquare);
          }
          mSNP.hasAdjustedP = true;
        }
        //set back the indicator
        for (int i = 0; i < geneNum; i++) {
          List<SNP> snps = genes.get(i).snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            //a lazy setting to save memory, but may confusing 
            mSNP.hasAdjustedP = false;
          }
        }
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          //a lazy setting to save memory, but may confusing 
          mSNP.hasAdjustedP = false;
        }
      }

      //adjusted by the median of chisqure
      pValueArrayLists.get(pIndex * 2).quickSort();
      double median = Descriptive.median(pValueArrayLists.get(pIndex * 2));
      //not precise at all
      double expectedMedian = pvSetting.getChiSquareDf() * Math.pow(1 - 2.0 / (9 * pvSetting.getChiSquareDf()), 3);
      if (pvSetting.getChiSquareDf() == 1) {
        //only an approximate of the expectedMedian
        expectedMedian = 0.456;
      }

      tmpBuffer.append("The median of Chi-square statistics ");
      tmpBuffer.append(name);
      tmpBuffer.append(" is ");
      tmpBuffer.append(median);
      tmpBuffer.append("; inflation factor(λ) is ");
      median = median / expectedMedian;
      tmpBuffer.append(Util.doubleToString(median, 4));
      tmpBuffer.append("\n");

      runningResultTopComp.insertText(tmpBuffer.toString());
      LOG.info(tmpBuffer.toString());
      tmpBuffer.delete(0, tmpBuffer.length());

      double pValue;
      //adjust the chi-square by inflation factor

      selectiveChromIDs.clear();
      for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
        Chromosome chromosome = genome.readChromosomefromDisk(chromIndex);
        if (chromosome == null || chromosome.genes.isEmpty()) {
          continue;
        }
        selectiveChromIDs.add(chromIndex);
        List<Gene> genes = chromosome.genes;
        int geneNum = genes.size();

        for (int i = 0; i < geneNum; i++) {
          Gene gene = genes.get(i);
          List<SNP> snps = gene.snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            if (mSNP.hasAdjustedP) {
              continue;
            }

            double[] snppValues = mSNP.getpValues();
            if (snppValues == null) {
              continue;
            }
            chiSquare = snppValues[pIndex];
            if (!Double.isNaN(chiSquare)) {
              pValueArrayLists.get(pIndex * 2).add(Probability.chiSquareComplemented(pvSetting.getChiSquareDf(), chiSquare));
              pValue = Probability.chiSquareComplemented(pvSetting.getChiSquareDf(), chiSquare / median);
              mSNP.getpValues()[pIndex] = pValue;
              pValueArrayLists.get(pIndex * 2 + 1).add(pValue);
              if (minPValue > mSNP.getpValues()[pIndex]) {
                minPValue = pValue;
              }
              mSNP.hasAdjustedP = true;
            }
          }
        }

        List<SNP> snpOutGenes = chromosome.snpsOutGenes;
        snpNum = snpOutGenes.size();
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          if (mSNP.hasAdjustedP) {
            continue;
          }
          double[] snppValues = mSNP.getpValues();
          if (snppValues == null) {
            continue;
          }
          chiSquare = snppValues[pIndex];
          if (!Double.isNaN(chiSquare)) {
            pValueArrayLists.get(pIndex * 2).add(Probability.chiSquareComplemented(pvSetting.getChiSquareDf(), chiSquare));
            pValue = Probability.chiSquareComplemented(pvSetting.getChiSquareDf(), chiSquare / median);
            mSNP.getpValues()[pIndex] = pValue;
            pValueArrayLists.get(pIndex * 2 + 1).add(pValue);
            if (minPValue > mSNP.getpValues()[pIndex]) {
              minPValue = pValue;
            }
            mSNP.hasAdjustedP = true;
          }
        }
        //set back the indicator
        for (int i = 0; i < geneNum; i++) {
          List<SNP> snps = genes.get(i).snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            //a lazy setting to save memory, but may confusing 
            mSNP.hasAdjustedP = false;
          }
        }
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          //a lazy setting to save memory, but may confusing 
          mSNP.hasAdjustedP = false;
        }
        genome.writeChromosomeToDisk(chromosome);
      }

      pValueArrayLists.get(pIndex * 2).quickSort();
      pValueArrayLists.get(pIndex * 2 + 1).quickSort();

      double familywiseErrorRate = 0.05;
      double threshlod = MultipleTestingMethod.BenjaminiHochbergFDR("Variants", familywiseErrorRate, pValueArrayLists.get(pIndex * 2 + 1));
      tmpBuffer.append("The p-value threshold for the FDR ");
      tmpBuffer.append(familywiseErrorRate);
      tmpBuffer.append(" is ");
      tmpBuffer.append(Util.formatPValue(threshlod));
      tmpBuffer.append(" in the adjusted p-value set.\n");
      runningResultTopComp.insertText(tmpBuffer.toString());
      LOG.info(tmpBuffer.toString());
      tmpBuffer.delete(0, tmpBuffer.length());
    }

    ManhattanPlotPainter manhattanPlotPainter1 = new ManhattanPlotPainter(1200, 500);
    manhattanPlotPainter1.setSelectiveChromIDs(selectiveChromIDs);

    manhattanPlotPainter1.setManhattanPlotMinPValue(1E-20);
    File manhattanImgFile1 = new File(imgFolder.getCanonicalPath() + File.separator + genome.getName() + ".SNPManhattanPlot.png");
    manhattanPlotPainter1.plotSNPPValues(genome, -Math.log10(minPValue), pIndexes, manhattanImgFile1.getCanonicalPath());
    String infor = "Manhattan plot of SNP adjusted p-values";
    runningResultTopComp.insertText(infor);
    LOG.info(infor);
    runningResultTopComp.insertImage(manhattanImgFile1);

    double minimalConvertablePValue = 1E-311;
    File imgFile = new File(imgFolder.getCanonicalPath() + File.separator + genome.getName() + ".gc.png");
    painter.drawMultipleQQPlot(pValueArrayLists, titles, null, imgFile.getCanonicalPath(), minimalConvertablePValue);
    runningResultTopComp.insertImage(imgFile);
    return 0;
  }

  /**
   * @param tmpChromosomes
   * @param pValueNames
   * @return
   * @throws Exception
   * @pdOid 8ca43f5a-4532-4606-9f80-2f2169397b7a
   */
  public int genomicControlAdjustmentWithPValues(Genome genome) throws Exception {
    // TODO: implement
    StringBuilder tmpBuffer = new StringBuilder();

    PValuePainter painter = new PValuePainter(600, 400);
    List<String> titles = new ArrayList<String>();
    List<DoubleArrayList> pValueArrayLists = new ArrayList<DoubleArrayList>();
    String inf = "Adjusting p-values by genomic control method...";
    runningResultTopComp.insertText(inf);
    LOG.info(inf);
    /*
         * //code to transform the pvalues to chi-square test
         *  ChiSquaredDistribution chiDis = new ChiSquaredDistributionImpl(1) has very low precise.
         * DecimalFormat df = new DecimalFormat("0.00E0", new DecimalFormatSymbols(Locale.US));
         double p =1E-311;
         p = Probability.normalInverse(0.5 * p);// two tails to be one tail
         p = p * p;
         System.out.println(p);
         p = Probability.chiSquareComplemented(1, p);
        
         String formattedNumber = df.format(p, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
         System.out.println(formattedNumber);
     */

    int pValueNum = pvSetting.getsNPPValueIndexes().length;
    double pValue = 0;
    double chiSquare = 0;
    int snpNum;

    int[] pIndexes = new int[pValueNum];
    HashSet<Integer> selectiveChromIDs = new HashSet<Integer>();
    double minPValue = 1;

    for (int pIndex = 0; pIndex < pValueNum; pIndex++) {
      String name = genome.getpValueNames()[pIndex];
      pIndexes[pIndex] = pIndex;

      pValueArrayLists.add(new DoubleArrayList());
      pValueArrayLists.add(new DoubleArrayList());

      titles.add("Original " + name);
      titles.add("Adjusted " + name);

      for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
        Chromosome chromosome = genome.readChromosomefromDisk(chromIndex);
        if (chromosome == null || chromosome.genes.isEmpty()) {
          continue;
        }
        List<Gene> genes = chromosome.genes;
        int geneNum = genes.size();

        for (int i = 0; i < geneNum; i++) {
          List<SNP> snps = genes.get(i).snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            //a lazy setting to save memory, but may confusing 
            if (mSNP.hasAdjustedP) {
              continue;
            }
            double[] snppValues = mSNP.getpValues();
            if (snppValues == null) {
              continue;
            }
            pValue = snppValues[pIndex];

            if (!Double.isNaN(pValue)) {
              pValueArrayLists.get(pIndex * 2).add(pValue);
              chiSquare = MultipleTestingMethod.zScore(pValue / 2);// two tails to be one tail;
              pValueArrayLists.get(pIndex * 2 + 1).add(chiSquare * chiSquare);
              mSNP.hasAdjustedP = true;
            }
          }
        }

        List<SNP> snpOutGenes = chromosome.snpsOutGenes;
        snpNum = snpOutGenes.size();
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          //a lazy setting to save memory, but may confusing 
          if (mSNP.hasAdjustedP) {
            continue;
          }
          double[] snppValues = mSNP.getpValues();
          if (snppValues == null) {
            continue;
          }
          pValue = snppValues[pIndex];
          if (!Double.isNaN(pValue)) {
            pValueArrayLists.get(pIndex * 2).add(pValue);
            chiSquare = MultipleTestingMethod.zScore(pValue / 2);// two tails to be one tail;
            pValueArrayLists.get(pIndex * 2 + 1).add(chiSquare * chiSquare);
            mSNP.hasAdjustedP = true;
          }
        }

        //set back the indicator
        for (int i = 0; i < geneNum; i++) {
          List<SNP> snps = genes.get(i).snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            //a lazy setting to save memory, but may confusing 
            mSNP.hasAdjustedP = false;
          }
        }
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          //a lazy setting to save memory, but may confusing 
          mSNP.hasAdjustedP = false;
        }
      }

      //adjusted by the median of chisqure         
      pValueArrayLists.get(pIndex * 2 + 1).quickSort();
      double median = Descriptive.median(pValueArrayLists.get(pIndex * 2 + 1));
      double expectedMedian = 0.456;

      pValueArrayLists.get(pIndex * 2 + 1).clear();

      tmpBuffer.append("The median of Chi-square statistics for p-value '");
      tmpBuffer.append(name);
      tmpBuffer.append("' is ");
      tmpBuffer.append(median);
      tmpBuffer.append("; inflation factor(λ) is ");
      median = median / expectedMedian;
      tmpBuffer.append(Util.doubleToString(median, 4));

      /*
             if (median <= 1.0) {
             tmpBuffer.append("\n");
             tmpBuffer.append("No need to adjust the p-values!");
             runningResultTopComp.insertText(tmpBuffer.toString());
             LOG.info(tmpBuffer.toString());
             tmpBuffer.delete(0, tmpBuffer.length());
             continue;
             }*/
      runningResultTopComp.insertText(tmpBuffer.toString());
      LOG.info(tmpBuffer.toString());
      tmpBuffer.delete(0, tmpBuffer.length());

      selectiveChromIDs.clear();
      for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
        Chromosome chromosome = genome.readChromosomefromDisk(chromIndex);
        if (chromosome == null || chromosome.genes.isEmpty()) {
          continue;
        }

        selectiveChromIDs.add(chromIndex);
        List<Gene> genes = chromosome.genes;
        int geneNum = genes.size();
        genes = chromosome.genes;
        geneNum = genes.size();

        for (int i = 0; i < geneNum; i++) {
          Gene gene = genes.get(i);
          List<SNP> snps = gene.snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            if (mSNP.hasAdjustedP) {
              continue;
            }
            double[] snppValues = mSNP.getpValues();
            if (snppValues == null) {
              continue;
            }
            pValue = snppValues[pIndex];
            if (!Double.isNaN(pValue)) {
              chiSquare = MultipleTestingMethod.zScore(pValue / 2);// two tails to be one tail;
              chiSquare = chiSquare * chiSquare;
              pValue = Probability.chiSquareComplemented(1, chiSquare / median);
              //if (pValue<=1E-20)  System.out.println(mSNP.rsID+" "+pValue);
              mSNP.getpValues()[pIndex] = pValue;
              pValueArrayLists.get(pIndex * 2 + 1).add(pValue);
              if (minPValue > mSNP.getpValues()[pIndex]) {
                minPValue = pValue;
              }
              mSNP.hasAdjustedP = true;
            }
          }
        }

        List<SNP> snpOutGenes = chromosome.snpsOutGenes;
        snpNum = snpOutGenes.size();
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          if (mSNP.hasAdjustedP) {
            continue;
          }
          double[] snppValues = mSNP.getpValues();
          if (snppValues == null) {
            continue;
          }
          pValue = snppValues[pIndex];
          if (!Double.isNaN(pValue)) {
            chiSquare = MultipleTestingMethod.zScore(pValue / 2);// two tails to be one tail;
            chiSquare = chiSquare * chiSquare;
            pValue = Probability.chiSquareComplemented(1, chiSquare / median);
            //if (pValue<=1E-20)  System.out.println(mSNP.rsID+" "+pValue);
            mSNP.getpValues()[pIndex] = pValue;
            pValueArrayLists.get(pIndex * 2 + 1).add(pValue);
            if (minPValue > mSNP.getpValues()[pIndex]) {
              minPValue = pValue;
            }
            mSNP.hasAdjustedP = true;
          }
        }
        //set back the indicator
        for (int i = 0; i < geneNum; i++) {
          List<SNP> snps = genes.get(i).snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            //a lazy setting to save memory, but may confusing 
            mSNP.hasAdjustedP = false;
          }
        }
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          //a lazy setting to save memory, but may confusing 
          mSNP.hasAdjustedP = false;
        }
        genome.writeChromosomeToDisk(chromosome);
      }
      //System.out.println(adjustedPValueList.size());

      pValueArrayLists.get(pIndex * 2).quickSort();
      pValueArrayLists.get(pIndex * 2 + 1).quickSort();

      double familywiseErrorRate = 0.05;
      double threshlod = MultipleTestingMethod.BenjaminiHochbergFDR("Variants", familywiseErrorRate, pValueArrayLists.get(pIndex * 2 + 1));
      tmpBuffer.append("The p-value threshold for the FDR ");
      tmpBuffer.append(familywiseErrorRate);
      tmpBuffer.append(" is ");
      tmpBuffer.append(Util.formatPValue(threshlod));
      tmpBuffer.append(" in the adjusted p-value set.\n");
      runningResultTopComp.insertText(tmpBuffer.toString());
      LOG.info(tmpBuffer.toString());
      tmpBuffer.delete(0, tmpBuffer.length());
    }

    ManhattanPlotPainter manhattanPlotPainter1 = new ManhattanPlotPainter(1200, 500);
    manhattanPlotPainter1.setSelectiveChromIDs(selectiveChromIDs);

    manhattanPlotPainter1.setManhattanPlotMinPValue(1E-20);
    File manhattanImgFile1 = new File(imgFolder.getCanonicalPath() + File.separator + genome.getName() + ".SNPManhattanPlot.png");
    manhattanPlotPainter1.plotSNPPValues(genome, -Math.log10(minPValue), pIndexes, manhattanImgFile1.getCanonicalPath());
    String infor = "Manhattan plot of SNP adjusted p-values";
    runningResultTopComp.insertText(infor);
    LOG.info(infor);
    runningResultTopComp.insertImage(manhattanImgFile1);

    File imgFile = new File(imgFolder.getCanonicalPath() + File.separator + genome.getName() + "." + "gc.png");
    painter.drawMultipleQQPlot(pValueArrayLists, titles, null, imgFile.getCanonicalPath(), 1e-20);
    runningResultTopComp.insertImage(imgFile);

    return 0;
  }

  /**
   * this function can handle p-values less than 1.0E-16 because we use
   *
   * @param tmpChromosomes
   * @param pValueNames
   * @return
   * @throws Exception
   */
  public int genomicControlAdjustmentWithZScore(Genome genome) throws Exception {
    // TODO: implement
    StringBuilder tmpBuffer = new StringBuilder();
    List<DoubleArrayList> pValueArrayLists = new ArrayList<DoubleArrayList>();

    PValuePainter painter = new PValuePainter(600, 400);
    List<String> titles = new ArrayList<String>();

    String inf = "Adjusting p-values by genomic control method...";
    runningResultTopComp.insertText(inf);
    LOG.info(inf);

    int pValueNum = pvSetting.getsNPPValueIndexes().length;
    double chiSquare = 0;
    int snpNum;
    double zScore = 0;
    double minimalConvertablePValue = 1E-311;
    int[] pIndexes = new int[pValueNum];
    HashSet<Integer> selectiveChromIDs = new HashSet<Integer>();
    double minPValue = 1;

    for (int pIndex = 0; pIndex < pValueNum; pIndex++) {
      String name = genome.getpValueNames()[pIndex];
      pIndexes[pIndex] = pIndex;
      pValueArrayLists.add(new DoubleArrayList());
      pValueArrayLists.add(new DoubleArrayList());

      titles.add("Original " + name);
      titles.add("Adjusted " + name);

      for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
        Chromosome chromosome = genome.readChromosomefromDisk(chromIndex);
        if (chromosome == null || chromosome.genes.isEmpty()) {
          continue;
        }

        List<Gene> genes = chromosome.genes;
        int geneNum = genes.size();

        for (int i = 0; i < geneNum; i++) {
          Gene gene = genes.get(i);
          List<SNP> snps = gene.snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            if (mSNP.hasAdjustedP) {
              continue;
            }
            double[] snppValues = mSNP.getpValues();
            if (snppValues == null) {
              continue;
            }
            zScore = snppValues[pIndex];

            if (!Double.isNaN(zScore)) {
              chiSquare = zScore * zScore;
              pValueArrayLists.get(pIndex * 2).add(chiSquare);
              mSNP.hasAdjustedP = true;
            }
          }
        }

        List<SNP> snpOutGenes = chromosome.snpsOutGenes;
        snpNum = snpOutGenes.size();
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          if (mSNP.hasAdjustedP) {
            continue;
          }
          double[] snppValues = mSNP.getpValues();
          if (snppValues == null) {
            continue;
          }
          zScore = snppValues[pIndex];
          if (!Double.isNaN(zScore)) {
            chiSquare = zScore * zScore;
            pValueArrayLists.get(pIndex * 2).add(chiSquare);
            mSNP.hasAdjustedP = true;
          }
        }

        //set back the indicator
        for (int i = 0; i < geneNum; i++) {
          List<SNP> snps = genes.get(i).snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            //a lazy setting to save memory, but may confusing 
            mSNP.hasAdjustedP = false;
          }
        }
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          //a lazy setting to save memory, but may confusing 
          mSNP.hasAdjustedP = false;
        }
      }

      //adjusted by the median of chisqure
      int pSize = pValueArrayLists.get(pIndex * 2).size();
      pValueArrayLists.get(pIndex * 2).quickSort();
      double median = Descriptive.median(pValueArrayLists.get(pIndex * 2));
      pValueArrayLists.get(pIndex * 2).clear();
      double expectedMedian = 0.456;

      tmpBuffer.append("The median of Chi-square statistics for p value of ");
      tmpBuffer.append(name);
      tmpBuffer.append(" is ");
      tmpBuffer.append(median);
      tmpBuffer.append("; inflation factor(λ) is ");
      median = median / expectedMedian;
      tmpBuffer.append(Util.doubleToString(median, 4));
      tmpBuffer.append("\n");

      runningResultTopComp.insertText(tmpBuffer.toString());
      LOG.info(tmpBuffer.toString());
      tmpBuffer.delete(0, tmpBuffer.length());

      //adjust the chi-square by inflation factor
      double pValue;
      selectiveChromIDs.clear();

      for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
        Chromosome chromosome = genome.readChromosomefromDisk(chromIndex);
        if (chromosome == null || chromosome.genes.isEmpty()) {
          continue;
        }
        selectiveChromIDs.add(chromIndex);

        List<Gene> genes = chromosome.genes;
        int geneNum = genes.size();

        for (int i = 0; i < geneNum; i++) {
          Gene gene = genes.get(i);
          List<SNP> snps = gene.snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            if (mSNP.hasAdjustedP) {
              continue;
            }
            double[] snppValues = mSNP.getpValues();
            if (snppValues == null) {
              continue;
            }
            zScore = snppValues[pIndex];
            if (!Double.isNaN(zScore)) {
              chiSquare = zScore * zScore;
              pValueArrayLists.get(pIndex * 2).add(Probability.chiSquareComplemented(1, chiSquare));

              pValue = Probability.chiSquareComplemented(1, chiSquare / median);
              //if (pValue<=1E-20)  System.out.println(mSNP.rsID+" "+pValue);

              mSNP.getpValues()[pIndex] = pValue;
              pValueArrayLists.get(pIndex * 2 + 1).add(pValue);
              if (minPValue > mSNP.getpValues()[pIndex]) {
                minPValue = pValue;
              }
              mSNP.hasAdjustedP = true;
            }
          }
        }

        List<SNP> snpOutGenes = chromosome.snpsOutGenes;
        snpNum = snpOutGenes.size();
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          if (mSNP.hasAdjustedP) {
            continue;
          }
          double[] snppValues = mSNP.getpValues();
          if (snppValues == null) {
            continue;
          }
          zScore = snppValues[pIndex];
          if (!Double.isNaN(zScore)) {
            chiSquare = zScore * zScore;
            pValueArrayLists.get(pIndex * 2).add(Probability.chiSquareComplemented(1, chiSquare));
            pValue = Probability.chiSquareComplemented(1, chiSquare / median);
            //if (pValue<=1E-20)  System.out.println(mSNP.rsID+" "+pValue);
            mSNP.getpValues()[pIndex] = pValue;
            pValueArrayLists.get(pIndex * 2 + 1).add(pValue);
            if (minPValue > mSNP.getpValues()[pIndex]) {
              minPValue = pValue;
            }
            mSNP.hasAdjustedP = true;
          }
        }

        //set back the indicator
        for (int i = 0; i < geneNum; i++) {
          List<SNP> snps = genes.get(i).snps;
          snpNum = snps.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snps.get(j);
            //a lazy setting to save memory, but may confusing 
            mSNP.hasAdjustedP = false;
          }
        }
        for (int j = 0; j < snpNum; j++) {
          SNP mSNP = snpOutGenes.get(j);
          //a lazy setting to save memory, but may confusing 
          mSNP.hasAdjustedP = false;
        }
        genome.writeChromosomeToDisk(chromosome);
      }

      // System.out.println(adjustedPValueList.size());
      pValueArrayLists.get(pIndex * 2).quickSort();
      pValueArrayLists.get(pIndex * 2 + 1).quickSort();

      double familywiseErrorRate = 0.05;
      double threshlod = MultipleTestingMethod.BenjaminiHochbergFDR("Variants", familywiseErrorRate, pValueArrayLists.get(pIndex * 2 + 1));
      tmpBuffer.append("The p-value threshold for the FDR ");
      tmpBuffer.append(familywiseErrorRate);
      tmpBuffer.append(" is ");
      tmpBuffer.append(Util.formatPValue(threshlod));
      tmpBuffer.append(" in the adjusted p-value set.\n");
      runningResultTopComp.insertText(tmpBuffer.toString());
      LOG.info(tmpBuffer.toString());
      tmpBuffer.delete(0, tmpBuffer.length());
    }

    ManhattanPlotPainter manhattanPlotPainter1 = new ManhattanPlotPainter(1200, 500);
    manhattanPlotPainter1.setSelectiveChromIDs(selectiveChromIDs);

    manhattanPlotPainter1.setManhattanPlotMinPValue(1E-20);
    File manhattanImgFile1 = new File(imgFolder.getCanonicalPath() + File.separator + genome.getName() + ".SNPManhattanPlot.png");
    manhattanPlotPainter1.plotSNPPValues(genome, -Math.log10(minPValue), pIndexes, manhattanImgFile1.getCanonicalPath());
    String infor = "Manhattan plot of SNP adjusted p-values";
    runningResultTopComp.insertText(infor);
    LOG.info(infor);
    runningResultTopComp.insertImage(manhattanImgFile1);

    File imgFile = new File(imgFolder.getCanonicalPath() + File.separator + genome.getName() + "." + "gc.png");
    painter.drawMultipleQQPlot(pValueArrayLists, titles, null, imgFile.getCanonicalPath(), minimalConvertablePValue);
    runningResultTopComp.insertImage(imgFile);
    return 0;
  }

  private void storeAndDrawPValueQQPlot(Genome geome) throws Exception {
    //draw QQ plot of pvalues for reference
    List<DoubleArrayList> allpValueList = new ArrayList<DoubleArrayList>();
    double pV = 0.0;
    int pValueNum = pvSetting.getsNPPValueIndexes().length;
    int snpNum;
    List<String> names = new ArrayList<String>();
    StringBuilder message = new StringBuilder();
    int[] pIndexes = new int[1];
    double minPValue = 1;
    HashSet<Integer> selectiveChromIDs = new HashSet<Integer>();
    //check the distribution of p-values and convert (if any) z-scores br to chis-quares.
    for (int pIndex = 0; pIndex < pValueNum; pIndex++) {
      allpValueList.add(new DoubleArrayList());
      String name = genome.getpValueNames()[pIndex];
      pIndexes[0] = pIndex;
      names.add(name);
      if (pvSetting.getTestInputType().equals("z-scores")) {
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
          Chromosome chromosome = geome.readChromosomefromDisk(chromIndex);
          if (chromosome == null || chromosome.genes.isEmpty()) {
            continue;
          }
          selectiveChromIDs.add(chromIndex);
          List<Gene> genes = chromosome.genes;
          int geneNum = genes.size();

          for (int i = 0; i < geneNum; i++) {
            Gene gene = genes.get(i);
            List<SNP> snps = gene.snps;
            snpNum = snps.size();
            for (int j = 0; j < snpNum; j++) {
              SNP mSNP = snps.get(j);
              double[] snppValues = mSNP.getpValues();
              if (snppValues == null) {
                continue;
              }
              pV = snppValues[pIndex];
              if (!Double.isNaN(pV)) {
                pV = Probability.chiSquareComplemented(1, pV * pV);
                //convert z into chi square
                mSNP.getpValues()[pIndex] = pV;
                allpValueList.get(pIndex).add(pV);
                if (minPValue > pV) {
                  minPValue = pV;
                }
              }
            }
          }

          List<SNP> snpOutGenes = chromosome.snpsOutGenes;
          snpNum = snpOutGenes.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snpOutGenes.get(j);
            double[] snppValues = mSNP.getpValues();
            if (snppValues == null) {
              continue;
            }
            pV = snppValues[pIndex];
            if (!Double.isNaN(pV)) {
              pV = Probability.chiSquareComplemented(1, pV * pV);
              //convert z into chi square
              mSNP.getpValues()[pIndex] = pV;
              allpValueList.get(pIndex).add(pV);
              if (minPValue > pV) {
                minPValue = pV;
              }
            }
          }
          geome.writeChromosomeToDisk(chromosome);
        }
      } else if (pvSetting.getTestInputType().equals("p-values")) {
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
          Chromosome chromosome = geome.readChromosomefromDisk(chromIndex);
          if (chromosome == null || chromosome.genes.isEmpty()) {
            continue;
          }
          selectiveChromIDs.add(chromIndex);
          List<Gene> genes = chromosome.genes;
          int geneNum = genes.size();

          for (int i = 0; i < geneNum; i++) {
            Gene gene = genes.get(i);
            List<SNP> snps = gene.snps;
            snpNum = snps.size();
            for (int j = 0; j < snpNum; j++) {
              SNP mSNP = snps.get(j);
              double[] snppValues = mSNP.getpValues();
              if (snppValues == null) {
                continue;
              }
              pV = snppValues[pIndex];
              if (!Double.isNaN(pV)) {
                allpValueList.get(pIndex).add(pV);
                if (minPValue > pV) {
                  minPValue = pV;
                }
              }
            }
          }

          List<SNP> snpOutGenes = chromosome.snpsOutGenes;
          snpNum = snpOutGenes.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snpOutGenes.get(j);
            double[] snppValues = mSNP.getpValues();
            if (snppValues == null) {
              continue;
            }
            pV = snppValues[pIndex];
            if (!Double.isNaN(pV)) {
              allpValueList.get(pIndex).add(pV);
              if (minPValue > pV) {
                minPValue = pV;
              }
            }
          }
          geome.writeChromosomeToDisk(chromosome);
        }
      } else if (pvSetting.getTestInputType().equals("chi-square")) {
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
          Chromosome chromosome = geome.readChromosomefromDisk(chromIndex);
          if (chromosome == null || chromosome.genes.isEmpty()) {
            continue;
          }
          selectiveChromIDs.add(chromIndex);
          List<Gene> genes = chromosome.genes;
          int geneNum = genes.size();

          for (int i = 0; i < geneNum; i++) {
            Gene gene = genes.get(i);
            List<SNP> snps = gene.snps;
            snpNum = snps.size();
            for (int j = 0; j < snpNum; j++) {
              SNP mSNP = snps.get(j);
              double[] snppValues = mSNP.getpValues();
              if (snppValues == null) {
                continue;
              }
              pV = snppValues[pIndex];
              if (!Double.isNaN(pV)) {
                pV = Probability.chiSquareComplemented(pvSetting.getChiSquareDf(), pV);
                //convert chi square to pValue
                mSNP.getpValues()[pIndex] = pV;
                allpValueList.get(pIndex).add(pV);
                if (minPValue > pV) {
                  minPValue = pV;
                }
              }
            }
          }

          List<SNP> snpOutGenes = chromosome.snpsOutGenes;
          snpNum = snpOutGenes.size();
          for (int j = 0; j < snpNum; j++) {
            SNP mSNP = snpOutGenes.get(j);
            double[] snppValues = mSNP.getpValues();
            if (snppValues == null) {
              continue;
            }
            pV = snppValues[pIndex];
            if (!Double.isNaN(pV)) {
              pV = Probability.chiSquareComplemented(pvSetting.getChiSquareDf(), pV);
              //convert chi square to pValue
              mSNP.getpValues()[pIndex] = pV;
              allpValueList.get(pIndex).add(pV);
              if (minPValue > pV) {
                minPValue = pV;
              }
            }
          }
          geome.writeChromosomeToDisk(chromosome);
        }
      }

      message.delete(0, message.length());
      if (allpValueList.isEmpty()) {
        message.append("These is no valid p values for \'").append(name).append("\'!");
        runningResultTopComp.insertText(message.toString());
        LOG.info(message.toString());
      } else {
        allpValueList.get(pIndex).quickSort();

        double familywiseErrorRate = 0.05;
        double threshlod = MultipleTestingMethod.BenjaminiHochbergFDR("Variants", familywiseErrorRate, allpValueList.get(pIndex));

        message.append("The p-value threshold for the FDR ");
        message.append(familywiseErrorRate);
        message.append(" is ");
        message.append(Util.formatPValue(threshlod));
        message.append(" for all SNPs of ").append(name);
        runningResultTopComp.insertText(message.toString());
        LOG.info(message.toString());
      }
    }

    ManhattanPlotPainter manhattanPlotPainter1 = new ManhattanPlotPainter(1200, 500);
    manhattanPlotPainter1.setSelectiveChromIDs(selectiveChromIDs);

    manhattanPlotPainter1.setManhattanPlotMinPValue(1E-20);
    File manhattanImgFile1 = new File(imgFolder.getCanonicalPath() + File.separator + genome.getName() + ".SNPManhattanPlot.png");
    manhattanPlotPainter1.plotSNPPValues(genome, -Math.log10(minPValue), pIndexes, manhattanImgFile1.getCanonicalPath());
    String infor = "Manhattan plot of SNP p-values";

    runningResultTopComp.insertText(infor);
    LOG.info(infor);
    runningResultTopComp.insertImage(manhattanImgFile1);

    PValuePainter painter = new PValuePainter(600, 400);
    File imgFile = new File(imgFolder.getCanonicalPath() + File.separator + genome.getName() + ".org.png");
    painter.drawMultipleQQPlot(allpValueList, names, null, imgFile.getCanonicalPath(), 1E-20);
    runningResultTopComp.insertImage(imgFile);
    allpValueList.clear();

  }

  // this function attempts to calculate SNP LD only within genes because it is too slow to do others
  public CorrelationBasedByteLDSparseMatrix calculateLocalLDRSquarebyHaplotypeVCFInGene(Chromosome chromosome,
      Map<String, int[]> snpGenomeIndexes, String chromName,
      String vcfFilePath, LiftOver liftOver) throws Exception {

    if (chromosome == null || chromosome.genes.isEmpty()) {
      return null;
    }

    if (vcfFilePath == null) {
      return null;
    }
    File vcfFile = new File(vcfFilePath);

    HaplotypeDataset haplotypeDataset = new HaplotypeDataset();
    if (!vcfFile.exists()) {
      String infor = "The VCF file for chromosome " + chromosome.getName() + " does not exist!";
      runningResultTopComp.insertText(infor);
      LOG.info(infor);
      return null;
    }

    String infor = "Reading haplotpyes on chromosome " + chromosome.getName() + "...";
    runningResultTopComp.insertText(infor);
    LOG.info(infor);

    List<StatusGtySet> chromGtys = new ArrayList< StatusGtySet>();
    boolean[] isPhased = new boolean[1];
    isPhased[0] = false;
    Set<Integer> variantsWithLD = haplotypeDataset.readSNPMapHapVCFFileBySNPList(chromosome, vcfFile, chromName, snpGenomeIndexes, liftOver, chromGtys, isPhased);
    if (variantsWithLD == null || variantsWithLD.isEmpty()) {
      return null;
    }
    IntArrayList snpPositionList = new IntArrayList();
//count SNPs have not genotype information
    StringBuilder sb = new StringBuilder();
    int unmappedSNPinGeneNum = 0;
    int totalSNPinGeneNum = 0;
    sb.append("Position\tID\n");
    for (Gene gene : chromosome.genes) {
      List<SNP> snps = gene.snps;
      for (SNP snp : snps) {
        if (!variantsWithLD.contains(snp.physicalPosition)) {
          sb.append(snp.physicalPosition);
          sb.append('\t');
          sb.append(snp.rsID);
          sb.append('\n');
          unmappedSNPinGeneNum++;
        } else {
          snpPositionList.add(snp.physicalPosition);
        }
      }
      totalSNPinGeneNum += snps.size();
    }

    variantsWithLD.clear();
    snpPositionList.quickSort();
    int ldSNPNum = snpPositionList.size();
    OpenIntIntHashMap allIndexMap = new OpenIntIntHashMap(ldSNPNum);
    for (int i = 0; i < ldSNPNum; i++) {
      allIndexMap.put(snpPositionList.getQuick(i), i);
    }
    snpPositionList.clear();

    if (unmappedSNPinGeneNum > 0) {
      File outPath = new File(project.getWorkingPath() + File.separator + project.getName() + File.separator + genome.getName() + File.separator + "NoLDSNPs." + chromosome.getName() + ".txt");
      BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
      bw.write(sb.toString());
      bw.close();

      sb.delete(0, sb.length());

      infor = "Warning!!! " + unmappedSNPinGeneNum + " variants within genes out of " + totalSNPinGeneNum + " on chromosome " + chromosome.getName() + " have NO haplotype information and will be assumed to be independent of others or will be ingored!\n Detailed information of these variants is saved in " + outPath.getCanonicalPath() + ".";
      runningResultTopComp.insertText(infor);
      LOG.info(infor);
    }

    infor = "Calculating local pair-wise LD of SNP within genes on chromosome " + chromosome.getName() + "...";
    runningResultTopComp.insertText(infor);
    LOG.info(infor);
    List<Gene> genes = chromosome.genes;
    int geneNum = genes.size();

    CorrelationBasedByteLDSparseMatrix ldRsMatrix = new CorrelationBasedByteLDSparseMatrix(allIndexMap);
    for (int i = 0; i < geneNum; i++) {
      Gene gene = genes.get(i);

      List<SNP> geneSNPs = gene.snps;
      //System.out.println(gene.getSymbol()+" : "+geneSNPs.size());
      if (isPhased[0]) {
        if (!GenotypeSetUtil.calculateLDRSequareByHaplotypeFast(geneSNPs, chromGtys, ldRsMatrix, genome.getMinEffectiveR2())) {
          infor = "Using slow function due to mssing genotypes!!!";
          runningResultTopComp.insertText(infor);
          LOG.info(infor);
          GenotypeSetUtil.calculateLDRSequareByHaplotype(geneSNPs, chromGtys, ldRsMatrix, genome.getMinEffectiveR2());
        }
      } else if (!GenotypeSetUtil.calculateGenotypeCorrelationSquareFastBit(geneSNPs, chromGtys, ldRsMatrix, genome.getMinEffectiveR2())) {
        infor = "Using slow function due to mssing genotypes!!!";
        //runningResultTopComp.insertText(infor);
        //LOG.info(infor);
        GenotypeSetUtil.calculateGenotypeCorrelationSquareFast(geneSNPs, chromGtys, ldRsMatrix, genome.getMinEffectiveR2());
      }
    }
    return ldRsMatrix;
  }

  /*
     // this function attempts to calculate SNP LD only within genes because it is too slow to do others
     public CorrelationBasedByteLDSparseMatrix calculateLocalLDRSquarebyHaplotypeInGene(Chromosome chromosome,
     Map<String, int[]> snpGenomeIndexes, String chromName,
     File[] mapHapFiles, LiftOver liftOver) throws Exception {

     if (chromosome == null || chromosome.genes.isEmpty()) {
     return null;
     }

     if (mapHapFiles == null) {
     return null;
     }

     HaplotypeDataset haplotypeDataset = new HaplotypeDataset();
     if (!mapHapFiles[0].exists()) {
     String infor = "No map file for chromosome " + chromosome.getName();
     runningResultTopComp.insertText(infor);
     LOG.info(infor);
     return null;
     }

     if (!mapHapFiles[1].exists()) {
     String infor = "No haplotype file for chromosome " + chromosome.getName();
     runningResultTopComp.insertText(infor);
     LOG.info(infor);
     return null;
     }

     List<StatusGtySet> chromGtys = new ArrayList< StatusGtySet>();
     List<SNP> mappedSNP = haplotypeDataset.readSNPMapFileBySNPList(chromosome, mapHapFiles, chromName, snpGenomeIndexes, liftOver);

     String infor = "Reading haplotpyes on chromosome " + chromosome.getName() + "...";
     runningResultTopComp.insertText(infor);
     LOG.info(infor);

     haplotypeDataset.readHaplotypesBySNPList(chromName, mappedSNP, mapHapFiles, chromGtys);

     infor = "Calculating local pair-wise LD of SNP within genes on chromosome " + chromosome.getName() + "...";
     runningResultTopComp.insertText(infor);
     LOG.info(infor);
     List<Gene> genes = chromosome.genes;
     int geneNum = genes.size();
        
     CorrelationBasedByteLDSparseMatrix ldRsMatrix = new CorrelationBasedByteLDSparseMatrix(allIndexMap);
     for (int i = 0; i < geneNum; i++) {
     Gene gene = genes.get(i);
     List<SNP> geneSNPs = gene.snps;
     // GenotypeSetUtil.calculateGenotypeCorrelationSquare(geneSNPs, chromGtys, ldRsMatrix);
     GenotypeSetUtil.calculateLDRSequareByHaplotype(geneSNPs, chromGtys, ldRsMatrix, genome.getMinEffectiveR2());
     }
     return ldRsMatrix;
     }
   */
  public CorrelationBasedByteLDSparseMatrix readHapMapLDRSquareByPositions(String hapMapFilePath, Chromosome chromosome,
      Map<String, int[]> positionGenomeIndexes, LiftOver liftOver, int totalSNPsWithinGenes, double minR2InGene) throws Exception {
    double minR2OutGene = 0.8;

    boolean outsideGene = false;

    if (chromosome == null || chromosome.genes.isEmpty()) {
      return null;
    }
    if (hapMapFilePath == null) {
      return null;
    }

    List<Gene> genes = chromosome.genes;
    List<SNP> snpOutGenes = chromosome.snpsOutGenes;
    boolean needConvert = false;
    if (liftOver != null) {
      needConvert = true;
    }

    int chromIndex = chromosome.getId();
    String chromName = CHROM_NAMES[chromIndex];

    //read genomic information of genes and snps; SNP outside genes are stored br  outSNPList.
    File hapMapLDFile = new File(hapMapFilePath);
    if (!hapMapLDFile.exists()) {
      String infor = "No hapmap LD file on chromosome " + chromosome.getName();
      runningResultTopComp.insertText(infor);
      LOG.info(infor);
      return null;
    }
    String infor = "Reading hapmap LD file on chromosome " + chromosome.getName() + "...";
    runningResultTopComp.insertText(infor);
    LOG.info(infor);
    LineReader br = null;
    if (hapMapLDFile.getName().endsWith(".zip") || hapMapLDFile.getName().endsWith(".gz") || hapMapLDFile.getName().endsWith(".tar.gz")) {
      br = new CompressedFileReader(hapMapLDFile);
    } else {
      br = new AsciiLineReader(hapMapLDFile);
    }
    String line;
    int[] pos1;
    int[] pos2;
    int snp1pos = 0, snp2pos = 0;
    float rsq = 0;

    SNP snp1 = null, snp2 = null;
    int[] indexesInLDFile = new int[4];

    indexesInLDFile[0] = 0; //snp1 pos
    indexesInLDFile[1] = 1;// snp2 pos
    indexesInLDFile[2] = 6; //coefficient
    indexesInLDFile[3] = -1; // chromsome;

    int maxColNum = indexesInLDFile[0];
    int colNum = indexesInLDFile.length;

    for (int i = 1; i < colNum; i++) {
      if (indexesInLDFile[i] > maxColNum) {
        maxColNum = indexesInLDFile[i];
      }
    }

    Set<Integer> snpWithinGenePositionSet = new HashSet<Integer>();
    IntArrayList pos1List = new IntArrayList();
    IntArrayList pos2List = new IntArrayList();
    FloatArrayList ldList = new FloatArrayList();
    boolean notValid = true;
    while ((line = br.readLine()) != null) {
      // System.out.println(line);
      line = line.trim();
      if (line.length() == 0) {
        continue;
      }
      StringTokenizer st = new StringTokenizer(line);

      notValid = false;
      outsideGene = false;
      snp1 = null;
      snp2 = null;
      pos1 = null;
      pos2 = null;
      for (int i = 0; i <= maxColNum; i++) {
        if (st.hasMoreTokens()) {
          //assume there is not duplicated SNPs
          if (i == indexesInLDFile[0]) {
            snp1pos = Integer.parseInt(st.nextToken().trim());

            if (needConvert && snp1pos > 0) {
              Interval interval = new Interval("chr" + chromName, snp1pos, snp1pos);
              Interval int2 = liftOver.liftOver(interval);
              if (int2 != null) {
                snp1pos = int2.getStart();
              }
            }
            pos1 = positionGenomeIndexes.get(chromName + ":" + snp1pos);
            if (pos1 == null) {
              notValid = true;
              break;
            }
            if (pos1[1] == -1) {
              //a snp outside of gene
              snp1 = snpOutGenes.get(pos1[2]);
              outsideGene = true;
            } else {
              if (chromIndex != pos1[0]) {
                String infor1 = "Warning! " + snp1pos + " are mapped into chromosome " + CHROM_NAMES[pos1[0]];
                System.out.println(infor1);
                outsideGene = true;
                continue;
              }
              snpWithinGenePositionSet.add(snp1pos);
              snp1 = genes.get(pos1[1]).snps.get(pos1[2]);
            }
          } else if (i == indexesInLDFile[1]) {
            snp2pos = Integer.parseInt(st.nextToken().trim());
            if (needConvert && snp2pos > 0) {
              Interval interval = new Interval("chr" + chromName, snp2pos, snp2pos);
              Interval int2 = liftOver.liftOver(interval);
              if (int2 != null) {
                snp2pos = int2.getStart();
              }
            }
            pos2 = positionGenomeIndexes.get(chromName + ":" + snp2pos);
            if (pos2 == null) {
              notValid = true;
              break;
            }
            if (pos2[1] == -1) {
              //a snp outside of gene
              snp2 = snpOutGenes.get(pos2[2]);
              outsideGene = true;
            } else {
              if (chromIndex != pos2[0]) {
                String infor1 = "Warning! " + snp2pos + " are mapped into chromosome " + CHROM_NAMES[pos2[0]];
                System.out.println(infor1);
                outsideGene = true;
                continue;
              }
              snpWithinGenePositionSet.add(snp2pos);
              snp2 = genes.get(pos2[1]).snps.get(pos2[2]);
            }

          } else if (i == indexesInLDFile[2]) {
            rsq = Float.parseFloat(st.nextToken().trim());
            if (rsq >= genome.getMinR2HighlyCorrelated()) {
              //add highly correlated SNPs and assume minR2OutGene <minR2HighlyCorrelated
              if (snp1 != null && snp2 != null) {
                if (snp1.correlatedRSID == null) {
                  snp1.correlatedRSID = new ArrayList<String>();
                }
                snp1.correlatedRSID.add(snp2.rsID);
                if (snp2.correlatedRSID == null) {
                  snp2.correlatedRSID = new ArrayList<String>();
                }
                snp2.correlatedRSID.add(snp1.rsID);
              }
            }

            if (outsideGene) {
              if (rsq < minR2OutGene) {
                notValid = true;
                break;
              }
            } else if (rsq < minR2InGene) {
              notValid = true;
              break;
            }
          } else {
            st.nextToken();
          }
        } else {
          break;
        }
      }

      if (notValid) {
        continue;
      }
      pos1List.add(snp1.physicalPosition);
      pos2List.add(snp2.physicalPosition);
      ldList.add(rsq);
    }
    br.close();

    StringBuilder runningInfo = new StringBuilder();
    runningInfo.append("The number of SNPs within genes on chromosome ");
    runningInfo.append(chromName);
    runningInfo.append(" in the HapMap LD file ");
    runningInfo.append(hapMapLDFile.getName());
    runningInfo.append(" is ");
    runningInfo.append(snpWithinGenePositionSet.size());
    runningInfo.append(".");
    runningResultTopComp.insertText(runningInfo.toString());
    LOG.info(runningInfo.toString());

    //count SNPs have not genotype information
    StringBuilder sb = new StringBuilder();
    IntArrayList snpPositionList = new IntArrayList();
    int unmappedSNPinGeneNum = 0;
    int totalSNPinGeneNum = 0;
    sb.append("Position\tID\n");
    for (Gene gene : chromosome.genes) {
      List<SNP> snps = gene.snps;
      for (SNP snp : snps) {
        if (!snpWithinGenePositionSet.contains(snp.physicalPosition)) {
          sb.append(snp.physicalPosition);
          sb.append('\t');
          sb.append(snp.rsID);
          sb.append('\n');
          unmappedSNPinGeneNum++;
        } else {
          snpPositionList.add(snp.physicalPosition);
        }
      }
      totalSNPinGeneNum += snps.size();
    }
    snpWithinGenePositionSet.clear();

    snpPositionList.quickSort();
    int ldSNPNum = snpPositionList.size();
    OpenIntIntHashMap allIndexMap = new OpenIntIntHashMap(ldSNPNum);
    for (int i = 0; i < ldSNPNum; i++) {
      allIndexMap.put(snpPositionList.getQuick(i), i);
    }
    snpPositionList.clear();
    CorrelationBasedByteLDSparseMatrix ldRsMatrix = new CorrelationBasedByteLDSparseMatrix(allIndexMap);
    ldSNPNum = ldList.size();
    for (int i = 0; i < ldSNPNum; i++) {
      ldRsMatrix.addLDAt(pos1List.getQuick(i), pos2List.getQuick(i), ldList.getQuick(i));
    }
    pos1List.clear();
    pos2List.clear();
    ldList.clear();

    if (unmappedSNPinGeneNum > 0) {
      File outPath = new File(project.getWorkingPath() + File.separator + project.getName() + File.separator + genome.getName() + File.separator + "NoLDSNPs." + chromosome.getName() + ".txt");
      BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
      bw.write(sb.toString());
      bw.close();

      sb.delete(0, sb.length());
      infor = "Warning!!! " + unmappedSNPinGeneNum + " variants within genes out of " + totalSNPinGeneNum + " on chromosome " + chromosome.getName() + " have NO haplotype information and will be assumed to be independent of others or will be ingored! Detailed information of these variants is saved in " + outPath.getCanonicalPath() + ".";
      runningResultTopComp.insertText(infor);
      LOG.info(infor);
    }

    infor = ldRsMatrix.size() + " LD pairs have been read.";
    runningResultTopComp.insertText(infor);
    LOG.info(infor);
    return ldRsMatrix;
  }

  // this function attempts to calculate SNP LD only within genes because it is too slow to do others
  public CorrelationBasedByteLDSparseMatrix calculateLocalLDRSquarebyGenotypesInGene(Chromosome chromosome,
      Map<String, int[]> snpGenomeIndexes, String chromName, boolean mappedByRSID) throws Exception {

    if (chromosome == null || chromosome.genes.isEmpty()) {
      return null;
    }
    if (!genome.getPlinkSet().avaibleFiles()) {
      String infor = "No Plink binary file on chromosome " + chromosome.getName();
      runningResultTopComp.insertText(infor);
      LOG.severe(infor);
      return null;
    }
    Map<String, StatusGtySet> indivGtyMap = new HashMap<String, StatusGtySet>();
    List<SNP> mappedSNP = null;

    if (mappedByRSID) {
      mappedSNP = genome.getPlinkSet().readSNPsinPlinkBinaryMapFileByRSID(chromosome, snpGenomeIndexes, chromName);
    } else {
      mappedSNP = genome.getPlinkSet().readSNPsinPlinkBinaryMapFileByPositions(chromosome, snpGenomeIndexes, chromName);
    }

    IntArrayList snpPositionList = new IntArrayList();
    for (SNP snp : mappedSNP) {
      snpPositionList.add(snp.physicalPosition);
    }
    snpPositionList.quickSort();
    int ldSNPNum = snpPositionList.size();
    OpenIntIntHashMap allIndexMap = new OpenIntIntHashMap(ldSNPNum);
    for (int i = 0; i < ldSNPNum; i++) {
      allIndexMap.put(snpPositionList.getQuick(i), i);
    }
    snpPositionList.clear();

    CorrelationBasedByteLDSparseMatrix ldRsMatrix = new CorrelationBasedByteLDSparseMatrix(allIndexMap);

    String infor = "Reading local genotypes on chromosome " + chromosome.getName() + "...";
    genome.getPlinkSet().readPlinkBinaryGenotypeinPedigreeFile(mappedSNP, indivGtyMap);
    runningResultTopComp.insertText(infor);
    LOG.info(infor);
    List<StatusGtySet> chromGtys = new ArrayList<StatusGtySet>(indivGtyMap.values());
    infor = "Calculating local pair-wise LD of SNP within genes on chromosome " + chromosome.getName() + "...";
    runningResultTopComp.insertText(infor);
    LOG.info(infor);
    List<Gene> genes = chromosome.genes;
    int geneNum = genes.size();

    for (int i = 0; i < geneNum; i++) {
      Gene gene = genes.get(i);

      List<SNP> geneSNPs = gene.snps;
      if (!GenotypeSetUtil.calculateGenotypeCorrelationSquareFastBit(geneSNPs, chromGtys, ldRsMatrix, genome.getMinEffectiveR2())) {
        infor = "Using slow function due to mssing genotypes!!!";
        runningResultTopComp.insertText(infor);
        LOG.info(infor);
        GenotypeSetUtil.calculateGenotypeCorrelationSquareFast(geneSNPs, chromGtys, ldRsMatrix, genome.getMinEffectiveR2());
      }

    }

    return ldRsMatrix;
  }

  /**
   * @return @throws Exception
   * @pdOid f0621cff-9d97-421e-a77a-765bd0938dfb
   */
  public List<HashMap<String, SNP>> readMarkerPValuesSingleTestPerColumnByPositions(String[] pValueNames) throws Exception {
    //retrieve SNP-Gene mapping information
    List<HashMap<String, SNP>> tmpChromosomes = new ArrayList<HashMap<String, SNP>>();
    int i;
    for (i = 0; i < CHROM_NAMES.length; i++) {
      HashMap<String, SNP> tmpChromosome = new HashMap<String, SNP>();
      tmpChromosomes.add(tmpChromosome);
    }

    StringBuilder message = new StringBuilder();
    String currentLine = null;
    String tmpStr, currChr = null, snpID = null;
    StringBuilder tmpBuffer = new StringBuilder();
    int nonRSmarker = 0;
    int chromIndexInFile = pvSetting.getChromIndexInFile();
    int markerIndexInFile = pvSetting.getMarkerIndexInFile();
    int positionIndexInFile = pvSetting.getPositionIndexInFile();
    int imputQualIndexInFile = pvSetting.getImputInfoIndexInFile();
    String missingPValueLabel = pvSetting.getMissingLabel();

    int maxColNum = (chromIndexInFile > markerIndexInFile) ? chromIndexInFile : markerIndexInFile;
    maxColNum = (positionIndexInFile > maxColNum) ? positionIndexInFile : maxColNum;
    maxColNum = (imputQualIndexInFile > maxColNum) ? imputQualIndexInFile : maxColNum;

    int[] sNPPValueIndexes = pvSetting.getsNPPValueIndexes();
    int pValueNum = sNPPValueIndexes.length;
    for (i = 0; i < pValueNum; i++) {
      if (sNPPValueIndexes[i] > maxColNum) {
        maxColNum = sNPPValueIndexes[i];
      }
    }

    long lineCounter = 0;
    String delimiter = "\t\" \",";

    String pValueFilePath = genome.getpValueSource().getCanonicalPath();
    File file = new File(pValueFilePath);
    LineReader br = null;
    if (pValueFilePath.endsWith(".zip") || pValueFilePath.endsWith(".gz") || pValueFilePath.endsWith(".tar.gz")) {
      br = new CompressedFileReader(file);
    } else {
      br = new AsciiLineReader(file);
    }

    int positon = 0;

    int tmpIndex = -1;
    boolean incomplete = true;
    boolean hasNoP = true;
    boolean isImputQualLarger = pvSetting.isIsImputInfoLarger();
    float imputQualCut = pvSetting.getImputInfoCutoff();
    int ignoredLineNum = 0;
    int duplicateLineNum = 0;
    int lowImputQualLineNum = 0;

    try {
      if (pvSetting.isHasTitleRow()) //read column testPValueNames
      {
        if ((currentLine = br.readLine()) != null) {
          lineCounter++;
          StringTokenizer st = new StringTokenizer(currentLine.trim(), delimiter);
          for (i = 0; i <= maxColNum; i++) {
            if (st.hasMoreTokens()) {
              tmpBuffer.append(st.nextToken().trim());
              tmpStr = tmpBuffer.toString();
              tmpBuffer.delete(0, tmpBuffer.length());
              tmpIndex = Arrays.binarySearch(sNPPValueIndexes, i);
              if (tmpIndex >= 0) {
                pValueNames[tmpIndex] = tmpStr;
              }
            }
          }
        }
      } else {
        for (int t = 0; t < pValueNum; t++) {
          pValueNames[t] = "Col" + (sNPPValueIndexes[t] + 1);
        }
      }
      int currChromOrder = -1;
      String snpPosID = null;

      while ((currentLine = br.readLine()) != null) {
        lineCounter++;
        StringTokenizer st = new StringTokenizer(currentLine.trim());
        double[] tmpPValues = new double[pValueNum];
        //Arrays.fill(tmpPValues, Double.NaN);
        incomplete = true;
        hasNoP = true;
        positon = -1;
        for (i = 0; i <= maxColNum; i++) {
          if (st.hasMoreTokens()) {
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(st.nextToken().trim());
            tmpStr = tmpBuffer.toString();
            if (i == chromIndexInFile) {
              currChr = tmpStr;
              if (currChr.equals(".")) {
                break;
              }
              if (currChr.startsWith("c") || currChr.startsWith("C")) {
                currChr = currChr.substring(3);
              }
              //some times, users number the chromosome
              if (currChr.equals("X")) {
                currChr = "23";
              } else if (currChr.equals("Y")) {
                currChr = "24";
              } else if (currChr.equals("XY")) {
                currChr = "25";
              } else if (currChr.equals("MT")) {
                currChr = "26";
              }
              //the index br CHROM_NAMES array
              //sometimes using 0 to denote SNPs with unknown chromosomes
              currChromOrder = Integer.valueOf(currChr) - 1;
              if (currChromOrder == -1) {
                currChromOrder = 26;
              }
            } else if (i == markerIndexInFile) {
              snpID = tmpStr;
              // System.out.println(snpID);
              if (i == positionIndexInFile) {
                positon = Integer.parseInt(tmpBuffer.toString());
                // System.out.println(snpID);
              }
            } else if (i == positionIndexInFile) {
              positon = Integer.parseInt(tmpBuffer.toString());
              // System.out.println(snpID);
            } else if (i == imputQualIndexInFile) {
              tmpStr = tmpBuffer.toString();
              if (tmpStr.equals(missingPValueLabel)) {
                lowImputQualLineNum++;
                break;
              }
              float infor = Float.parseFloat(tmpStr);
              if (isImputQualLarger) {
                if (infor < imputQualCut) {
                  lowImputQualLineNum++;
                  break;
                }
              } else if (infor > imputQualCut) {
                lowImputQualLineNum++;
                break;
              }
              // System.out.println(snpID);
            } else {
              tmpIndex = Arrays.binarySearch(sNPPValueIndexes, i);
              if (tmpIndex >= 0) {
                if (tmpStr.equals(missingPValueLabel)) {
                  tmpPValues[tmpIndex] = Double.NaN;
                } else {
                  tmpPValues[tmpIndex] = Double.valueOf(tmpStr);
                  hasNoP = false;
                }
              }
            }

          } else {
            break;
          }
          if (i == maxColNum) {
            incomplete = false;
          }
        }

        if (incomplete || hasNoP) {
          ignoredLineNum++;
          /*
                     message.append("Line ");
                     message.append(lineCounter);
                     message.append(" has error and is ignored! ");
                     message.append(currentLine);
                     //runningResultTopComp.insertText(message.toString());
                     //LOG.info(message.toString());
                     message.delete(0, message.length());
           */
          continue;
        }

        snpPosID = currChr + ":" + positon;

        if (snpID == null || !snpID.startsWith("rs")) {
          snpID = snpPosID;
        }
        if (tmpChromosomes.get(currChromOrder).containsKey(snpPosID)) {
          duplicateLineNum++;
          message.append("Duplicated SNP positions ");
          message.append(snpPosID);
          message.append(" at the line and another line: ");
          message.append(currentLine);
          message.append("\n");
          // message.append("Dose a column contain p-values or statistics of different tests at the same SNPs?\n" + "If yes, please choose the Input format as\"Multiple tests per column\"");
          // throw new Exception(message.toString());
          /*
                     runningResultTopComp.insertText(message.toString());
                     LOG.info(message.toString());
                     message.delete(0, message.length());
                     *
           */
        } else {
          SNP gSNP = new SNP(tmpPValues, positon);
          gSNP.setRsID(snpID);
//                    gSNP.addFeatureValue(11.1);
//                    gSNP.addFeatureValue(22.2);
          //to save memory the rsID is null for every snp
          tmpChromosomes.get(currChromOrder).put(snpPosID, gSNP);
        }
        /*
                 // System.out.println(snpID);
                 if (lineCounter%100000==0){
                 System.out.println(lineCounter);
                 }
         */
      }
    } catch (NumberFormatException nex) {
      String info = nex.toString() + " when parsing at line " + lineCounter + ": " + currentLine;
      throw new Exception(info);
    }
    br.close();

    if (duplicateLineNum > 0) {
      String infor = duplicateLineNum + " duplicate lines are ignored! See details in the Log file!";
      runningResultTopComp.insertText(infor);
      LOG.log(Level.WARNING, "{0}\n{1}", new Object[]{infor, message.toString()});
      String info = ("Duplicated marker positions!!!\n"
          + "Dose a column contain p-values of different tests at the same SNPs?\n"
          + "If yes, please choose the Input format as\"Multiple tests per column\"");

      Object[] options = {"Yes", "No, proceed!"};
      int response = JOptionPane.showOptionDialog(GlobalManager.mainFrame, info, "Message",
          JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
          null, options, options[0]);
      if (response == 0) {
        return null;
      }
    }
    message.delete(0, message.length());
    if (lowImputQualLineNum > 0) {
      double prop = 100.0 * lowImputQualLineNum * 1.0 / (lineCounter - 1);
      String infor = lowImputQualLineNum + " out of " + (lineCounter - 1) + " (" + Util.doubleToString(prop, 2) + "%) SNPs are ignored due to their imputation quality is " + (isImputQualLarger ? " less than " : " over ") + imputQualCut + "!";
      runningResultTopComp.insertText(infor);
      LOG.warning(infor);
    }

    int totalNum = 0;
    for (i = 0; i < CHROM_NAMES.length; i++) {
      message.append(tmpChromosomes.get(i).size());
      message.append(" SNPs on chromosmome ");
      message.append(CHROM_NAMES[i]);
      runningResultTopComp.insertIcon(imgFolder, "chromosome.png", message.toString());
      LOG.info(message.toString());
      message.delete(0, message.length());
      totalNum += tmpChromosomes.get(i).size();
    }

    if (ignoredLineNum > 0) {
      message.append(ignoredLineNum).append(" lines are ignroed!!");
      runningResultTopComp.insertText(message.toString());
      LOG.info(message.toString());
    }

    String infor = "In total, " + totalNum + " SNPs on the whole genome.";
    runningResultTopComp.insertIcon(imgFolder, "Script.png", infor);
    LOG.info(infor);

    return tmpChromosomes;
  }

  public void readGeneVarMapFile(String filePath, Map<String, String[]> geneVarMap) throws Exception {
    File file = new File(filePath);
    LineReader br = null;

    if (filePath.endsWith(".zip") || filePath.endsWith(".gz") || filePath.endsWith(".tar.gz")) {
      br = new CompressedFileReader(file);
    } else {
      br = new AsciiLineReader(file);
    }
    String currentLine, tmpStr;
    int i, geneIndex = -1, varChrIndex = -1, varPosIndex = -1;
    currentLine = br.readLine();
    StringTokenizer st = new StringTokenizer(currentLine.trim(), "\t, ");
    i = 0;
    while (st.hasMoreElements()) {
      tmpStr = st.nextToken();
      if (tmpStr.equals("gene")) {
        geneIndex = i;
      } else if (tmpStr.equals("chr")) {
        varChrIndex = i;
      } else if (tmpStr.equals("pos")) {
        varPosIndex = i;
      }
      i++;
    }
    if (geneIndex == -1) {
      throw new Exception("No colummn with 'gene' in the gene-variant map file!");
    }
    if (varChrIndex == -1) {
      throw new Exception("No colummn with 'chr' in the gene-variant map file!");
    }
    if (varPosIndex == -1) {
      throw new Exception("No colummn with 'pos' in the gene-variant map file!");
    }
    int maxColNum = Math.max(geneIndex, varChrIndex);
    maxColNum = Math.max(maxColNum, varPosIndex);
    String gene, chr, pos;
    String[] cells, cellsTmp;
    while ((currentLine = br.readLine()) != null) {
      gene = null;
      chr = null;
      pos = null;
      currentLine = currentLine.trim();
      if (currentLine.length() == 0) {
        continue;
      }
      st = new StringTokenizer(currentLine, "\t, ");
      for (i = 0; i <= maxColNum; i++) {
        if (st.hasMoreTokens()) {
          tmpStr = st.nextToken();
          if (i == geneIndex) {
            gene = tmpStr;
          } else if (i == varChrIndex) {
            chr = tmpStr;
          } else if (i == varPosIndex) {
            pos = tmpStr;
          }
        }
      }
      if (gene == null || chr == null || pos == null) {
        System.err.println("Warning: invalid line in the gene-variant map file, " + currentLine);
        continue;
      }
      pos = chr + ":" + pos;

      cells = geneVarMap.get(pos);
      if (cells == null) {
        cells = new String[1];
        cells[cells.length - 1] = gene;
        geneVarMap.put(pos, cells);
      } else {
        cellsTmp = new String[cells.length + 1];
        System.arraycopy(cells, 0, cellsTmp, 0, cells.length);
        cellsTmp[cellsTmp.length - 1] = gene;
        geneVarMap.put(pos, cellsTmp);
      }

    }

    br.close();

  }

  /**
   * @return @throws Exception
   * @pdOid f0621cff-9d97-421e-a77a-765bd0938dfb
   */
  public List<HashMap<String, SNP>> readMarkerPValuesMultipleTestsPerColumnByPositions(String[] pValueNames) throws Exception {
    //retrieve SNP-Gene mapping information
    List<HashMap<String, SNP>> tmpChromosomes = new ArrayList<HashMap<String, SNP>>();
    Map<String, Integer> testNamePValueMap = new HashMap<String, Integer>();

    int i;
    for (i = 0; i < CHROM_NAMES.length; i++) {
      HashMap<String, SNP> tmpChromosome = new HashMap<String, SNP>();
      tmpChromosomes.add(tmpChromosome);
    }

    StringBuilder message = new StringBuilder();
    String currentLine;
    String tmpStr, currChr = null, snpID = null;
    StringBuilder tmpBuffer = new StringBuilder();
    int chromIndexInFile = pvSetting.getChromIndexInFile();
    int markerIndexInFile = pvSetting.getMarkerIndexInFile();
    int positionIndexInFile = pvSetting.getPositionIndexInFile();
    int testNameIndex = pvSetting.getTestNameIndex();
    String missingPValueLabel = pvSetting.getMissingLabel();

    int maxColNum = (chromIndexInFile > markerIndexInFile) ? chromIndexInFile : markerIndexInFile;
    maxColNum = (testNameIndex > maxColNum) ? testNameIndex : maxColNum;
    maxColNum = (positionIndexInFile > maxColNum) ? positionIndexInFile : maxColNum;

    int[] sNPPValueIndexes = pvSetting.getsNPPValueIndexes();
    int pValueNum = sNPPValueIndexes.length;
    String testName;
    int availableTestNamePValueTypes = 0;
    //we define an extended pValueNames as testName@testName
    String[] extendedPValueNames = new String[pValueNum];
    double[] tmpPValues = new double[pValueNum];

    for (i = 0; i < pValueNum; i++) {
      if (sNPPValueIndexes[i] > maxColNum) {
        maxColNum = sNPPValueIndexes[i];
      }
    }

    long lineCounter = 0;
    String delimiter = "\t\" \",";
    int positon = 0;

    String pValueFilePath = genome.getpValueSource().getCanonicalPath();
    File file = new File(pValueFilePath);
    LineReader br = null;
    if (pValueFilePath.endsWith(".zip") || pValueFilePath.endsWith(".gz") || pValueFilePath.endsWith(".tar.gz")) {
      br = new CompressedFileReader(file);
    } else {
      br = new AsciiLineReader(file);
    }

    pValueNames = new String[pValueNum];
    int tmpIndex = -1;
    boolean isNewTestNamePValueType;
    int ignoredLineNum = 0;
    if (pvSetting.isHasTitleRow()) //read column testPValueNames
    {
      if ((currentLine = br.readLine()) != null) {
        lineCounter++;
        StringTokenizer st = new StringTokenizer(currentLine.trim(), delimiter);
        for (i = 0; i <= maxColNum; i++) {
          if (st.hasMoreTokens()) {
            tmpBuffer.append(st.nextToken().trim());
            tmpStr = tmpBuffer.toString();
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpIndex = Arrays.binarySearch(sNPPValueIndexes, i);
            if (tmpIndex >= 0) {
              pValueNames[tmpIndex] = tmpStr;
            }
          }
        }
      }
    } else {
      for (int t = 0; t < pValueNum; t++) {
        pValueNames[t] = "Col" + (sNPPValueIndexes[t] + 1);
      }
    }
    int currChromOrder = -1;
    String snpPosID = null;
    while ((currentLine = br.readLine()) != null) {
      lineCounter++;
      StringTokenizer st = new StringTokenizer(currentLine.trim());
      Arrays.fill(extendedPValueNames, null);
      testName = null;
      isNewTestNamePValueType = false;
      for (i = 0; i <= maxColNum; i++) {
        if (st.hasMoreTokens()) {
          tmpBuffer.append(st.nextToken().trim());
          tmpStr = tmpBuffer.toString();

          if (i == chromIndexInFile) {
            currChr = tmpStr;
            //some times, users number the chromosome
            if (currChr.equals("X")) {
              currChr = "23";
            } else if (currChr.equals("Y")) {
              currChr = "24";
            } else if (currChr.equals("XY")) {
              currChr = "25";
            } else if (currChr.equals("MT")) {
              currChr = "26";
            }
            //the index br CHROM_NAMES array
            currChromOrder = Integer.valueOf(currChr) - 1;
          } else if (i == markerIndexInFile) {
            snpID = tmpStr;
            //sometimes the marker ID and position are in the came column
            if (i == positionIndexInFile) {
              positon = Integer.parseInt(tmpBuffer.toString());
              // System.out.println(snpID);
            }
          } else if (i == testNameIndex) {
            testName = tmpStr;
          } else if (i == positionIndexInFile) {
            positon = Integer.parseInt(tmpBuffer.toString());
            // System.out.println(snpID);
          } else {
            tmpIndex = Arrays.binarySearch(sNPPValueIndexes, i);
            if (tmpIndex >= 0) {
              if (tmpStr.equals(missingPValueLabel)) {
                tmpPValues[tmpIndex] = Double.NaN;
              } else {
                tmpPValues[tmpIndex] = Double.valueOf(tmpStr);
              }
              //we define an extended pValueNames as testName@testName
              extendedPValueNames[tmpIndex] = testName + '@' + pValueNames[tmpIndex];
              if (testNamePValueMap.get(extendedPValueNames[tmpIndex]) == null) {
                testNamePValueMap.put(extendedPValueNames[tmpIndex], availableTestNamePValueTypes);
                availableTestNamePValueTypes++;
                isNewTestNamePValueType = true;
              }
            }
          }
          tmpBuffer.delete(0, tmpBuffer.length());
        } else {
          break;
        }
      }

      if (i < maxColNum) {
        message.append("Line ");
        message.append(lineCounter);
        message.append(" has error and is ignored!\n");
        LOG.info(message.toString());
        message.delete(0, message.length());
        ignoredLineNum++;
        continue;
      }
      snpPosID = currChr + ":" + positon;

      if (snpID == null || !snpID.startsWith("rs")) {
        snpID = snpPosID;
      }

      SNP gSNP = tmpChromosomes.get(currChromOrder).get(snpID);
      if (gSNP == null) {
        gSNP = new SNP(positon);
        gSNP.setRsID(snpID);
        double[] snpPValues = new double[availableTestNamePValueTypes];
        Arrays.fill(snpPValues, Double.NaN);
        for (int j = 0; j < pValueNum; j++) {
          snpPValues[testNamePValueMap.get(extendedPValueNames[j])] = tmpPValues[j];
        }
        gSNP.setpValues(snpPValues);
        tmpChromosomes.get(currChromOrder).put(snpID, gSNP);
      } else {
        //indicating additional new pValues
        double[] snpPValues = gSNP.getpValues();
        if (snpPValues == null) {
          continue;
        }
        int orgPvalueNum = snpPValues.length;
        int newPValueNum = extendedPValueNames.length;
        if (isNewTestNamePValueType) {
          double[] copiedPValues = Arrays.copyOf(snpPValues, orgPvalueNum);
          snpPValues = new double[availableTestNamePValueTypes];
          Arrays.fill(snpPValues, Double.NaN);
          System.arraycopy(copiedPValues, 0, snpPValues, 0, orgPvalueNum);
          copiedPValues = null;
        }

        for (int j = 0; j < newPValueNum; j++) {
          snpPValues[testNamePValueMap.get(extendedPValueNames[j])] = tmpPValues[j];
        }
        gSNP.setpValues(snpPValues);
      }
    }
    br.close();

    // update the p-Value names
    pValueNum = availableTestNamePValueTypes;
    pValueNames = new String[pValueNum];
    for (Map.Entry<String, Integer> m : testNamePValueMap.entrySet()) {
      pValueNames[m.getValue()] = m.getKey();
    }
    int totalNum = 0;
    for (i = 0; i < CHROM_NAMES.length; i++) {
      message.append(tmpChromosomes.get(i).size());
      message.append(" SNPs on chromosmome ");
      message.append(CHROM_NAMES[i]);

      runningResultTopComp.insertIcon(imgFolder, "chromosome.png", message.toString());
      LOG.info(message.toString());
      message.delete(0, message.length());
      totalNum += tmpChromosomes.get(i).size();
    }

    /*
         if (nonRSmarker > 0) {
         message.append(nonRSmarker).append(" SNPs starting with non \"rs\", will be put on the non-gene region!");
         }
         * 
     */
    if (ignoredLineNum > 0) {
      message.append(ignoredLineNum).append(" lines are ignroed!!");
      runningResultTopComp.insertText(message.toString());
      LOG.info(message.toString());
    }

    String infor = "In total, " + totalNum + " SNPs on the whole genome.";
    runningResultTopComp.insertIcon(imgFolder, "Script.png", infor);
    LOG.info(infor);
    message.delete(0, message.length());

    return tmpChromosomes;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    startTime = System.currentTimeMillis();
    //record the classification settings
    BuildGenomeSwingWorker worker = new BuildGenomeSwingWorker();
    buildTask = RP.create(worker); //the task is not started yet
    buildTask.schedule(0); //start the task

  }

  class BuildGenomeSwingWorker extends SwingWorker<Void, String> {

    private final int NUM = 100;
    int runningThread = 0;
    boolean finished = false;
    ProgressHandle ph = null;
    long time = 0;

    public BuildGenomeSwingWorker() {

      time = System.nanoTime();
      ph = ProgressHandleFactory.createHandle("Building analysis genome progress", new Cancellable() {
        @Override
        public boolean cancel() {
          return handleCancel();

        }
      });
      runningResultTopComp.newPane();
    }

    @Override
    protected Void doInBackground() {
      try {

        ph.start(); //we must start the PH before we swith to determinate
        ph.switchToIndeterminate();

        StatusDisplayer.getDefault().setStatusText("Building analysis genome ...");

        finished = false;
        String infor = ("Start to build genome and map p-values onto human reference genome.\nReading p-values of the association result set...");
        runningResultTopComp.insertIcon(imgFolder, "Next.png", infor);
        LOG.info(infor);
        String[] pValueNames = new String[pvSetting.getsNPPValueIndexes().length];

        List<HashMap<String, SNP>> tmpChroms = null;
        if (pvSetting.getPvalueColType() == 0) {
          tmpChroms = readMarkerPValuesSingleTestPerColumnByPositions(pValueNames);
        } else {
          tmpChroms = readMarkerPValuesMultipleTestsPerColumnByPositions(pValueNames);
        }
        if (tmpChroms == null) {
          return null;
        }
        genome.setpValueNames(pValueNames);
//                genome.addAVariantFeatureNames("lj");//Just for test!
//                genome.addAVariantFeatureNames("mx");//Just for test!

        String weightInfor = "Mapping SNPs onto genome ...";
        runningResultTopComp.insertText(weightInfor);
        LOG.info(weightInfor);

        String refGenomeVersion = genome.getFinalBuildGenomeVersion();
        if (refGenomeVersion.contains("hg19")) {
          refGenomeVersion = "hg19";
        } else if (refGenomeVersion.contains("hg18")) {
          refGenomeVersion = "hg18";
        } else if (refGenomeVersion.contains("hg38")) {
          refGenomeVersion = "hg38";
        } else if (refGenomeVersion.contains("hg17")) {
          refGenomeVersion = "hg17";
        } else {
          NotifyDescriptor nd = new NotifyDescriptor.Message("Unsupported reference genome version " + refGenomeVersion, NotifyDescriptor.ERROR_MESSAGE);
          DialogDisplayer.getDefault().notifyLater(nd);
          return null;
        }

        genome.setFinalBuildGenomeVersion(refGenomeVersion);

        //ld source code
        //-2 others LD
        //0 genotype plink binary file
        //1 hapap ld
        //2 1kG haplomap
        //3 local LD calcualted by plink
        //4 1kG haplomap vcf format
        if (genome.getLdSourceCode() == 0) {
          // plink
        } else if (genome.getLdSourceCode() == 1) {
        } else if (genome.getLdSourceCode() == 2) {
        } else if (genome.getLdSourceCode() == 3) {
          //  genome.setLdMatrixStoragePath(genomeSelected.getLdMatrixStoragePath());
        } else if (genome.getLdSourceCode() == 4) {
        }

        mapAndStorePValuesOntoGenomeByCoordinates(tmpChroms);

        tmpChroms.clear();
        tmpChroms = null;
        //genome.storeGenome2Disk();
        System.gc();
        finished = true;
      } catch (InterruptedException ex) {
        StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
        java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

      } catch (Exception ex) {
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

        if (finished) {
          // runningResultTopComp.addGenomeNode(genome);
          project.genomeSet.add(genome);
          GlobalManager.genomeSetModel.addElement(genome);
          ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
          projTopComp.showProject(project);

          message = ("The geome has been built successfully!");
          LOG.info(message);
          StatusDisplayer.getDefault().setStatusText(message);

        }
        ph.finish();

        time = System.nanoTime() - time;
        time = time / 1000000000;
        long min = time / 60;
        long sec = time % 60;
        String info = ("Elapsed time: " + min + " min. " + sec + " sec.");
        runningResultTopComp.insertText(info);
        File buildGenomResultFilePath = new File(project.getWorkingPath() + File.separator + project.getName() + File.separator + genome.getName() + ".html");
        runningResultTopComp.savePane(buildGenomResultFilePath);
      } catch (Exception e) {
        ErrorManager.getDefault().notify(e);
      }
    }
  }

  private boolean handleCancel() {

    if (null == buildTask) {
      return false;
    }
    return buildTask.cancel();
  }
}
