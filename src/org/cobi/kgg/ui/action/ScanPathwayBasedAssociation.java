/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.stat.Descriptive;
import cern.jet.stat.Gamma;
import cern.jet.stat.Probability;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.SwingWorker;
import net.sf.picard.liftover.LiftOver;
import net.sf.picard.util.Interval;
import net.sf.samtools.util.AsciiLineReader;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import java.util.logging.Logger;
import jsc.independentsamples.MannWhitneyTest;
import jsc.tests.H1;
import org.cobi.kgg.business.CandidateGeneExtender;
import org.cobi.kgg.business.GenotypeLDCalculator;
import org.cobi.kgg.business.GenotypeSetUtil;
import org.cobi.kgg.business.PValuePainter;
import org.cobi.kgg.business.PathwayExplorer;
import org.cobi.kgg.business.SetBasedTest;
import org.cobi.kgg.business.entity.Chromosome;
import static org.cobi.kgg.business.entity.Constants.CHROM_NAMES;
import org.cobi.kgg.business.entity.CorrelationBasedByteLDSparseMatrix;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.GenePValueComparator;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.HaplotypeDataset;
import org.cobi.kgg.business.entity.PValueGene;
import org.cobi.kgg.business.entity.PValueGeneComparator;
import org.cobi.kgg.business.entity.PValueWeight;
import org.cobi.kgg.business.entity.PValueWeightComparatorIndex;
import org.cobi.kgg.business.entity.Pathway;
import org.cobi.kgg.business.entity.PathwayBasedAssociation;
import org.cobi.kgg.business.entity.PlinkDataset;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.StatusGtySet;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.MyTreeNode;
import org.cobi.kgg.ui.dialog.ProjectTopComponent;
import org.cobi.kgg.ui.dialog.RunningResultViewerTopComponent;
import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.file.Zipper;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.text.LocalExcelFile;
import org.cobi.util.text.LocalFile;
import org.cobi.util.text.Util;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author mxli
 */
public class ScanPathwayBasedAssociation {

    private final static Logger LOG = Logger.getLogger(ScanPPIBasedAssociation.class.getName());
    private final static RequestProcessor RP = new RequestProcessor("Scan pathways", 1, true);
    private RequestProcessor.Task buildTask = null;

    private PathwayExplorer pathExploer;
    final private PathwayBasedAssociation pathwayBasedAssociation;
    private boolean noNeedSNP;
    private String pathwayExportPath = "./";
    private String pathwayExportFormat = "Excel(.xls)";
    private String pruningMethod = "LD-attenuating rank-sum test";
    private org.apache.log4j.Logger logOutput = org.apache.log4j.Logger.getRootLogger();

    public String getPruningMethod() {
        return pruningMethod;
    }

    public void setPruningMethod(String pruningMethod) {
        this.pruningMethod = pruningMethod;
    }

    public String getPathwayExportPath() {
        return pathwayExportPath;
    }

    public void setPathwayExportPath(String pathwayExportPath) {
        this.pathwayExportPath = pathwayExportPath;
    }

    public String getPathwayExportFormat() {
        return pathwayExportFormat;
    }

    public void setPathwayExportFormat(String pathwayExportFormat) {
        this.pathwayExportFormat = pathwayExportFormat;
    }
    RunningResultViewerTopComponent runningResultTopComp;
    File imgFolder;
    boolean toWeightHyst = false;

    public ScanPathwayBasedAssociation(PathwayBasedAssociation pathwayBasedAssociation, RunningResultViewerTopComponent runningResultTopComp) throws Exception {
        this.pathExploer = new PathwayExplorer();
        this.pathwayBasedAssociation = pathwayBasedAssociation;
        this.runningResultTopComp = runningResultTopComp;
    }

    public ScanPathwayBasedAssociation(PathwayBasedAssociation pathwayBasedAssociation) throws Exception {
        this.pathExploer = new PathwayExplorer();
        this.pathwayBasedAssociation = pathwayBasedAssociation;
    }

    private boolean handleCancel() {

        if (null == buildTask) {
            return false;
        }
        return buildTask.cancel();
    }

    // this function attempts to calculate SNP LD only within genes because it is too slow to do others
    public CorrelationBasedByteLDSparseMatrix calculateLocalLDRSquarebyGenotypesPositions(PlinkDataset plinkSet, Set<Integer> positionsSet,
            String chromName, LiftOver liftOver) throws Exception {
        if (positionsSet == null || positionsSet.isEmpty()) {
            return null;
        }
        if (!plinkSet.avaibleFiles()) {
            String infor = "No Plink binary file on chromosome " + chromName;
            runningResultTopComp.insertText(infor);
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
        runningResultTopComp.insertText(infor);
        LOG.info(infor);
        List<StatusGtySet> chromGtys = new ArrayList<StatusGtySet>(indivGtyMap.values());
        infor = "Calculating local pair-wise LD of SNP within genes on chromosome " + chromName + "...";
        runningResultTopComp.insertText(infor);
        LOG.info(infor);
        if (!GenotypeSetUtil.calculateGenotypeCorrelationSquareFastBit(mappedSNP, chromGtys, ldRsMatrix, pathwayBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2())) {
            infor = "Using slow function due to mssing genotypes!!!";
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
            GenotypeSetUtil.calculateGenotypeCorrelationSquareFast(mappedSNP, chromGtys, ldRsMatrix, pathwayBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2());
        }

        return ldRsMatrix;
    }

    //read all involved positions and the LDs
    public CorrelationBasedByteLDSparseMatrix readHapMapLDRSquareByPositions(String hapMapFilePath, int chromIndex, Set<Integer> positionsSet,
            int[] indexesInLDFile, LiftOver liftOver) throws Exception {
        double minR2InGene = 0.0001;

        if (hapMapFilePath == null) {
            return null;
        }
        boolean needConvert = false;
        if (liftOver != null) {
            needConvert = true;
        }

        String chromName = CHROM_NAMES[chromIndex];
        //read genomic information of genes and snps; SNP outside genes are stored br  outSNPList.
        File hapMapLDFile = new File(hapMapFilePath);
        if (!hapMapLDFile.exists()) {
            String infor = "No hapmap LD file on chromosome " + chromName;
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
            return null;
        }
        String infor = "Reading hapmap LD file on chromosome " + chromName + "...";
        runningResultTopComp.insertText(infor);
        LOG.info(infor);
        LineReader br = null;
        if (hapMapLDFile.getName().endsWith(".zip") || hapMapLDFile.getName().endsWith(".gz") || hapMapLDFile.getName().endsWith(".tar.gz")) {
            br = new CompressedFileReader(hapMapLDFile);
        } else {
            br = new AsciiLineReader(hapMapLDFile);
        }
        String line;

        int snp1pos = 0, snp2pos = 0;
        float rsq = 0;

        int maxColNum = indexesInLDFile[0];
        int colNum = indexesInLDFile.length;
        for (int i = 1; i < colNum; i++) {
            if (indexesInLDFile[i] > maxColNum) {
                maxColNum = indexesInLDFile[i];
            }
        }

        Set<Integer> addedPositionSet = new HashSet<Integer>();
        boolean notValid = true;
        IntArrayList pos1List = new IntArrayList();
        IntArrayList pos2List = new IntArrayList();
        FloatArrayList ldList = new FloatArrayList();

        while ((line = br.readLine()) != null) {
            // System.out.println(line);
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line);
            snp1pos = -1;
            snp2pos = -1;
            notValid = false;
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

                        if (!positionsSet.contains(snp1pos)) {
                            notValid = true;
                            break;
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
                        if (!positionsSet.contains(snp2pos)) {
                            notValid = true;
                            break;
                        }

                    } else if (i == indexesInLDFile[2]) {
                        rsq = Float.parseFloat(st.nextToken().trim());

                        if (rsq < minR2InGene) {
                            notValid = true;
                            break;
                        }
                    } else {
                        st.nextToken();
                    }
                }
            }

            if (notValid) {
                continue;
            }
            addedPositionSet.add(snp1pos);
            addedPositionSet.add(snp2pos);
            pos1List.add(snp1pos);
            pos2List.add(snp2pos);
            ldList.add(rsq);
        }
        br.close();

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
            String prjName = GlobalManager.currentProject.getName();
            String prjWorkingPath = GlobalManager.currentProject.getWorkingPath();
            File outPath = new File(prjWorkingPath + File.separator + prjName + File.separator + pathwayBasedAssociation.getName() + ".NoLDSNPs." + chromName + ".txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
            bw.write(sb.toString());
            bw.close();

            sb.delete(0, sb.length());
            infor = "Warning!!! " + unmappedSNPinGeneNum + " variants within genes out of " + totalSNPinGeneNum + " on chromosome " + chromName + " have NO haplotype information and will be assumed to be independent of others or will be ingored! Detailed information of these variants is saved in " + outPath.getCanonicalPath() + ".";
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
        }

        addedPositionSet.clear();
        infor = ldRsMatrix.size() + " LD pairs have been read. ";
        runningResultTopComp.insertText(infor);
        LOG.info(infor);

        return ldRsMatrix;
    }

    /*
     // this function attempts to calculate SNP LD only within genes because it is too slow to do others
     public CorrelationBasedByteLDSparseMatrix calculateLocalLDRSquarebyHaplotypeInGene(File[] mapHapFiles,
     String chromName, Set<Integer> positionsSet,
     LiftOver liftOver) throws Exception {
        
     if (mapHapFiles == null) {
     return null;
     }
     HaplotypeDataset haplotypeDataset = new HaplotypeDataset();
     if (!mapHapFiles[0].exists()) {
     String infor = "No map file for chromosome " + chromName;
     runningResultTopComp.insertText(infor);
     LOG.info(infor);
     return null;
     }

     if (!mapHapFiles[1].exists()) {
     String infor = "No haplotype file for chromosome " + chromName;
     runningResultTopComp.insertText(infor);
     LOG.info(infor);
     return null;
     }

     List<StatusGtySet> chromGtys = new ArrayList< StatusGtySet>();
     List<SNP> mappedSNP = haplotypeDataset.readSNPMapFileByPositions(mapHapFiles, chromName, positionsSet, liftOver);

     String infor = "Reading haplotpyes on chromosome " + chromName + "...";
     runningResultTopComp.insertText(infor);
     LOG.info(infor);

     haplotypeDataset.readHaplotypesBySNPList(chromName, mappedSNP, mapHapFiles, chromGtys);

     infor = "Calculating local pair-wise LD of SNP within genes on chromosome " + chromName + "...";
     runningResultTopComp.insertText(infor);
     LOG.info(infor);
     // GenotypeSetUtil.calculateGenotypeCorrelationSquare(mappedSNP, chromGtys, ldRsMatrix);
     GenotypeSetUtil.calculateLDRSequareByHaplotype(mappedSNP, chromGtys, ldRsMatrix, pathwayBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2());

     return ldRsMatrix;
     }*/
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
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
            return null;
        }

        String infor = "Reading haplotpyes on chromosome " + chromName + "...";
        runningResultTopComp.insertText(infor);
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

        if (unmappedSNPinGeneNum > 0) {
            String prjName = GlobalManager.currentProject.getName();
            String prjWorkingPath = GlobalManager.currentProject.getWorkingPath();
            File outPath = new File(prjWorkingPath + File.separator + prjName + File.separator + pathwayBasedAssociation.getName() + ".NoLDSNPs." + chromName + ".txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
            bw.write(sb.toString());
            bw.close();

            sb.delete(0, sb.length());
            infor = "Warning!!! " + unmappedSNPinGeneNum + " variants within genes out of " + totalSNPinGeneNum + " on chromosome " + chromName + " have NO haplotype information and will be assumed to be independent of others or will be ingored! Detailed information of these variants is saved in " + outPath.getCanonicalPath() + ".";
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
        }

        infor = "Calculating local pair-wise LD of SNP within genes on chromosome " + chromName + " ...";
        runningResultTopComp.insertText(infor);
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

        if (!GenotypeSetUtil.calculateLDRSequareByHaplotypeFast(mappedSNP, chromGtys, ldRsMatrix, pathwayBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2())) {
            infor = "Using slow function due to mssing genotypes!!!";
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
            GenotypeSetUtil.calculateLDRSequareByHaplotype(mappedSNP, chromGtys, ldRsMatrix, pathwayBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2());
        }
        mappedSNP.clear();
        chromGtys.clear();
        addedPositionSet.clear();

        return ldRsMatrix;
    }

    /**
     *
     * @param genePValues
     * @param pathwayPThreshold
     * @param genePThreshold
     * @param pathwayList
     * @return
     * @throws Exception
     */
    public DoubleArrayList setBasedTestByHYST(Map<String, PValueGene> genePValueMap,
            List<Pathway> pathwayList, Map<String, Pathway> dbPathwaySet, DoubleArrayList pathwayGenePValues, String sourcePValueName) throws Exception {
        Map<String, Pathway> searchedPathways = new HashMap<String, Pathway>();
        DoubleArrayList pathwayPValues = new DoubleArrayList();
        Genome genome = pathwayBasedAssociation.getGeneScan().getGenome();
        Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();

        Set<Integer>[] positionsSets = new Set[CHROM_NAMES.length];
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            positionsSets[chromIndex] = new HashSet<Integer>();
        }
        PValueGene gp = null;

        Set<String> uniquePathwayGenes = new HashSet<String>();
        //pickup positions of key SNPs
        for (Map.Entry<String, Pathway> mPath : dbPathwaySet.entrySet()) {
            Pathway curPath = mPath.getValue();

            Set<String> pGenes = curPath.getGeneSymbols();
            uniquePathwayGenes.addAll(pGenes);
            Iterator<String> pGeneIter = pGenes.iterator();
            while (pGeneIter.hasNext()) {
                String pGeneSymb = pGeneIter.next();

                int[] indexes1 = geneGenomeIndexes.get(pGeneSymb);
                if (indexes1 == null) {
                    continue;
                }
                gp = genePValueMap.get(pGeneSymb);
                if (gp == null) {
                    continue;
                }

                for (int i = 0; i < gp.keySNPPositions.size(); i++) {
                    positionsSets[indexes1[0]].add(gp.keySNPPositions.getQuick(i));
                }
            }
        }

        for (String pGeneSymb : uniquePathwayGenes) {
            gp = genePValueMap.get(pGeneSymb);
            if (gp == null || Double.isNaN(gp.pValue)) {
                continue;
            }

            pathwayGenePValues.add(gp.pValue);
        }
        uniquePathwayGenes.clear();

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

        CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes = new CorrelationBasedByteLDSparseMatrix[CHROM_NAMES.length];
        info = "Reading LD information of Key SNPs";
        LOG.info(info);
        runningResultTopComp.insertText(info);
        GenotypeLDCalculator ldCaculator = new GenotypeLDCalculator();
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (positionsSets[chromIndex] == null || positionsSets[chromIndex].isEmpty()) {
                continue;
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
                    runningResultTopComp.insertText(infor);
                    LOG.info(infor);
                    break;
                }

                ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyGenotypesPositions(ldGenome.getPlinkSet(), positionsSets[chromIndex], CHROM_NAMES[chromIndex], liftOverLDGenome2pValueFile);

            } else if (ldGenome.getLdSourceCode() == 1) {
                if (ldGenome.getChromLDFiles()[chromIndex] != null) {
                    File hapMapLDFile = new File(ldGenome.getChromLDFiles()[chromIndex]);
                    if (!hapMapLDFile.exists()) {
                        String infor = "No hapmap LD file on chromosome " + CHROM_NAMES[chromIndex];
                        runningResultTopComp.insertText(infor);
                        LOG.info(infor);
                        continue;
                    }
                }
                int[] indexesInLDFile = new int[]{0, 1, 6};
                ldRsMatrixes[chromIndex] = readHapMapLDRSquareByPositions(ldGenome.getChromLDFiles()[chromIndex], chromIndex, positionsSets[chromIndex], indexesInLDFile, liftOverLDGenome2pValueFile);

            } else if (ldGenome.getLdSourceCode() == 2) {
                if (ldGenome.getHaploMapFilesList() == null) {
                    continue;
                }
                // ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyHaplotypeInGene(ldGenome.getHaploMapFilesList().get(chromIndex), CHROM_NAMES[chromIndex], positionsSets[chromIndex], liftOverLDGenome2pValueFile);
            } else if (ldGenome.getLdSourceCode() == 4) {
                if (ldGenome.getChromLDFiles()[chromIndex] != null) {
                    File hapMapLDFile = new File(ldGenome.getChromLDFiles()[chromIndex]);
                    if (!hapMapLDFile.exists()) {
                        continue;
                    }
                }
                ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyHaplotypeVCFByPositions(ldGenome.getChromLDFiles()[chromIndex], CHROM_NAMES[chromIndex], positionsSets[chromIndex], liftOverLDGenome2pValueFile);
            }
        }
        pathwayBasedAssociation.saveBetweenGeneLDToDisk(ldRsMatrixes, sourcePValueName);
        List<Integer> chrIDs = new ArrayList<Integer>();
        List<List<PValueWeight>> genepBlocks = new ArrayList<List<PValueWeight>>();
        Map<String, Float> availableGWASGeneSet = new HashMap<String, Float>();
        boolean ingoreSNPNoLD = pathwayBasedAssociation.isIgnoreNoLDSNPs();
        float geneWeight = 1;

        for (Map.Entry<String, Pathway> mPath : dbPathwaySet.entrySet()) {
            Pathway curPath = mPath.getValue();
            String pathID = mPath.getKey();

            chrIDs.clear();
            genepBlocks.clear();
            availableGWASGeneSet.clear();
            Map<String, Float> geneSymbolWeightMap = curPath.getGeneSymbolWeightMap();
            for (Map.Entry<String, Float> pGeneIter : geneSymbolWeightMap.entrySet()) {
                String pGeneSymb = pGeneIter.getKey();

                int[] indexes1 = geneGenomeIndexes.get(pGeneSymb);
                if (indexes1 == null) {
                    continue;
                }
                gp = genePValueMap.get(pGeneSymb);
                if (gp == null) {
                    continue;
                }

                if (toWeightHyst) {
                    geneWeight = pGeneIter.getValue();
                }
                availableGWASGeneSet.put(pGeneSymb, geneWeight);
                List<PValueWeight> genePBlock = new ArrayList<PValueWeight>();
                for (int i = 0; i < gp.keySNPPositions.size(); i++) {
                    if (ingoreSNPNoLD) {
                        if (ldRsMatrixes[indexes1[0]] == null || ldRsMatrixes[indexes1[0]].isEmpty()) {
                            continue;
                        }
                        if (!ldRsMatrixes[indexes1[0]].positionIndexMap.containsKey(gp.keySNPPositions.getQuick(i))) {
                            continue;
                        }
                    }
                    PValueWeight gpb = new PValueWeight();
                    gpb.pValue = gp.blockPValues.getQuick(i);
                    gpb.weight = geneWeight;
                    gpb.physicalPos = gp.keySNPPositions.getQuick(i);
                    genePBlock.add(gpb);
                }

                //now every gene has a key SNP
                if (gp.keySNPPositions.size() == 0) {
                    continue;
                    /*
                     PValueWeight gpb = new PValueWeight();
                     gpb.pValue = gp.pValue;
                     gpb.weight = geneWeight;
                     gpb.physicalPos = -1;
                     genePBlock.add(gpb);
                     */
                }
                genepBlocks.add(genePBlock);
                chrIDs.add(indexes1[0]);
            }

            if (genepBlocks.isEmpty()) {
                continue;
            }
            Pathway newPathway = new Pathway(pathID, pathID, curPath.getURL());
            newPathway.getGeneSymbolWeightMap().putAll(availableGWASGeneSet);

            double p = calculatePathwaybasedPValuesByHYST(chrIDs, genepBlocks, ldRsMatrixes);

            newPathway.setTotalGeneNum(geneSymbolWeightMap.size());
            pathwayPValues.add(p);
            newPathway.setHystPValue(p);
            searchedPathways.put(pathID, newPathway);
        }
        pathwayList.addAll(searchedPathways.values());
        return pathwayPValues;
    }

    public DoubleArrayList setBasedTestByWMW(Map<String, PValueGene> genePValueMap,
            List<Pathway> pathwayList, Map<String, Pathway> dbPathwaySet, DoubleArrayList pathwayGenePValues, String sourcePValueName) throws Exception {
        Map<String, Pathway> searchedPathways = new HashMap<String, Pathway>();
        DoubleArrayList pathwayPValues = new DoubleArrayList();
        Genome genome = pathwayBasedAssociation.getGeneScan().getGenome();
        Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();

        Set<Integer>[] positionsSets = new Set[CHROM_NAMES.length];
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            positionsSets[chromIndex] = new HashSet<Integer>();
        }
        PValueGene gp = null;

        Set<String> uniquePathwayGenes = new HashSet<String>();
        //pickup positions of key SNPs
        Map<String, Integer> genePValueMapIndex = new HashMap<String, Integer>();
        double[] allGenePArray = new double[genePValueMap.size()];
        int index = 0;
        for (Map.Entry<String, PValueGene> mPath : genePValueMap.entrySet()) {
            allGenePArray[index] = mPath.getValue().pValue;
            genePValueMapIndex.put(mPath.getKey(), index);
            index++;
        }

        for (Map.Entry<String, Pathway> mPath : dbPathwaySet.entrySet()) {
            Pathway curPath = mPath.getValue();

            Set<String> pGenes = curPath.getGeneSymbols();
            uniquePathwayGenes.addAll(pGenes);
            Iterator<String> pGeneIter = pGenes.iterator();
            while (pGeneIter.hasNext()) {
                String pGeneSymb = pGeneIter.next();

                int[] indexes1 = geneGenomeIndexes.get(pGeneSymb);
                if (indexes1 == null) {
                    continue;
                }
                gp = genePValueMap.get(pGeneSymb);
                if (gp == null) {
                    continue;
                }

                for (int i = 0; i < gp.keySNPPositions.size(); i++) {
                    positionsSets[indexes1[0]].add(gp.keySNPPositions.getQuick(i));
                }
            }
        }

        for (String pGeneSymb : uniquePathwayGenes) {
            gp = genePValueMap.get(pGeneSymb);
            if (gp == null || Double.isNaN(gp.pValue)) {
                continue;
            }

            pathwayGenePValues.add(gp.pValue);
        }
        uniquePathwayGenes.clear();

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

        CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes = new CorrelationBasedByteLDSparseMatrix[CHROM_NAMES.length];
        info = "Reading LD information of Key SNPs";
        LOG.info(info);
        runningResultTopComp.insertText(info);
        GenotypeLDCalculator ldCaculator = new GenotypeLDCalculator();
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (positionsSets[chromIndex] == null || positionsSets[chromIndex].isEmpty()) {
                continue;
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
                    runningResultTopComp.insertText(infor);
                    LOG.info(infor);
                    break;
                }

                ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyGenotypesPositions(ldGenome.getPlinkSet(), positionsSets[chromIndex], CHROM_NAMES[chromIndex], liftOverLDGenome2pValueFile);

            } else if (ldGenome.getLdSourceCode() == 1) {
                if (ldGenome.getChromLDFiles()[chromIndex] != null) {
                    File hapMapLDFile = new File(ldGenome.getChromLDFiles()[chromIndex]);
                    if (!hapMapLDFile.exists()) {
                        String infor = "No hapmap LD file on chromosome " + CHROM_NAMES[chromIndex];
                        runningResultTopComp.insertText(infor);
                        LOG.info(infor);
                        continue;
                    }
                }
                int[] indexesInLDFile = new int[]{0, 1, 6};
                ldRsMatrixes[chromIndex] = readHapMapLDRSquareByPositions(ldGenome.getChromLDFiles()[chromIndex], chromIndex, positionsSets[chromIndex], indexesInLDFile, liftOverLDGenome2pValueFile);

            } else if (ldGenome.getLdSourceCode() == 2) {
                if (ldGenome.getHaploMapFilesList() == null) {
                    continue;
                }
                // ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyHaplotypeInGene(ldGenome.getHaploMapFilesList().get(chromIndex), CHROM_NAMES[chromIndex], positionsSets[chromIndex], liftOverLDGenome2pValueFile);
            } else if (ldGenome.getLdSourceCode() == 4) {
                if (ldGenome.getChromLDFiles()[chromIndex] != null) {
                    File hapMapLDFile = new File(ldGenome.getChromLDFiles()[chromIndex]);
                    if (!hapMapLDFile.exists()) {
                        continue;
                    }
                }
                ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyHaplotypeVCFByPositions(ldGenome.getChromLDFiles()[chromIndex], CHROM_NAMES[chromIndex], positionsSets[chromIndex], liftOverLDGenome2pValueFile);
            }
        }
        pathwayBasedAssociation.saveBetweenGeneLDToDisk(ldRsMatrixes, sourcePValueName);
        List<Integer> chrIDs = new ArrayList<Integer>();
        List<List<PValueWeight>> genepBlocks = new ArrayList<List<PValueWeight>>();
        Map<String, Float> availableGWASGeneSet = new HashMap<String, Float>();
        boolean ingoreSNPNoLD = pathwayBasedAssociation.isIgnoreNoLDSNPs();
        float geneWeight = 1;
        IntArrayList pathwayPValueIndex = new IntArrayList();

        RConnection rcon = new RConnection();
        // rcon.eval("pack=\"survey\"; if (!require(pack,character.only = TRUE)) { install.packages(pack,dep=TRUE,repos='http://cran.us.r-project.org');if(!require(pack,character.only = TRUE)) stop(\"Package not found\")}");
        rcon.eval("library(limma)");
        rcon.assign("allGenePArray", allGenePArray);

        for (Map.Entry<String, Pathway> mPath : dbPathwaySet.entrySet()) {
            Pathway curPath = mPath.getValue();
            String pathID = mPath.getKey();

            chrIDs.clear();
            genepBlocks.clear();
            availableGWASGeneSet.clear();
            pathwayPValueIndex.clear();
            Map<String, Float> geneSymbolWeightMap = curPath.getGeneSymbolWeightMap();
            for (Map.Entry<String, Float> pGeneIter : geneSymbolWeightMap.entrySet()) {
                String pGeneSymb = pGeneIter.getKey();

                int[] indexes1 = geneGenomeIndexes.get(pGeneSymb);
                if (indexes1 == null) {
                    continue;
                }
                gp = genePValueMap.get(pGeneSymb);
                if (gp == null) {
                    continue;
                }

                if (toWeightHyst) {
                    geneWeight = pGeneIter.getValue();
                }
                availableGWASGeneSet.put(pGeneSymb, geneWeight);
                List<PValueWeight> genePBlock = new ArrayList<PValueWeight>();
                int keyBlock = gp.keyBlockIndex;
                if (ingoreSNPNoLD) {
                    if (ldRsMatrixes[indexes1[0]] == null || ldRsMatrixes[indexes1[0]].isEmpty()) {
                        continue;
                    }
                    if (!ldRsMatrixes[indexes1[0]].positionIndexMap.containsKey(gp.keySNPPositions.getQuick(keyBlock))) {
                        continue;
                    }
                }
                PValueWeight gpb = new PValueWeight();
                gpb.pValue = gp.blockPValues.getQuick(keyBlock);
                gpb.weight = geneWeight;
                gpb.physicalPos = gp.keySNPPositions.getQuick(keyBlock);
                genePBlock.add(gpb);

                pathwayPValueIndex.add(genePValueMapIndex.get(pGeneSymb));

                //now every gene has a key SNP
                if (gp.keySNPPositions.size() == 0) {
                    continue;
                    /*
                     PValueWeight gpb = new PValueWeight();
                     gpb.pValue = gp.pValue;
                     gpb.weight = geneWeight;
                     gpb.physicalPos = -1;
                     genePBlock.add(gpb);
                     */
                }
                genepBlocks.add(genePBlock);
                chrIDs.add(indexes1[0]);
            }

            if (genepBlocks.isEmpty()) {
                continue;
            }

            int[] pIndex = new int[pathwayPValueIndex.size()];
            for (int i = 0; i < pIndex.length; i++) {
                pIndex[i] = pathwayPValueIndex.getQuick(i);
            }
            rcon.assign("pIndex", pIndex);

            double avgCorr = averageLDInPathway(chrIDs, genepBlocks, ldRsMatrixes);
            avgCorr = avgCorr / (chrIDs.size() * (chrIDs.size() - 1) / 2);
            double p = rcon.eval("(rankSumTestWithCorrelation(pIndex, allGenePArray, correlation=" + avgCorr + "))[1]").asDouble();//This step will cost a lot of time. 

            Pathway newPathway = new Pathway(pathID, pathID, curPath.getURL());
            newPathway.getGeneSymbolWeightMap().putAll(availableGWASGeneSet);

            newPathway.setTotalGeneNum(geneSymbolWeightMap.size());
            pathwayPValues.add(p);
            newPathway.setWilcoxonPValue(p);
            searchedPathways.put(pathID, newPathway);
        }

        rcon.close();
        pathwayList.addAll(searchedPathways.values());
        return pathwayPValues;
    }

    public DoubleArrayList setBasedTestByHYSTWhileRemovingRedundantGenes(Map<String, PValueGene> genePValueMap,
            List<Pathway> sortedPathwayList, String sourcePValueName) throws Exception {
        Map<String, Pathway> searchedPathways = new HashMap<String, Pathway>();
        DoubleArrayList pathwayPValues = new DoubleArrayList();
        Genome genome = pathwayBasedAssociation.getGeneScan().getGenome();
        Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();
        PValueGene gp = null;
        String info;
        CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes = pathwayBasedAssociation.loadBetweenGeneLDToDisk(sourcePValueName);
        boolean allNull = true;

        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (ldRsMatrixes[chromIndex] != null && !ldRsMatrixes[chromIndex].isEmpty()) {
                allNull = false;
                break;
            }
        }

        if (allNull) {
            info = "Warning, no LD information about SNPs between genes! These SNPs will be treated as independent markers!";
            LOG.info(info);
            runningResultTopComp.insertText(info);
        }
        List<Integer> chrIDs = new ArrayList<Integer>();
        List<List<PValueWeight>> genepBlocks = new ArrayList<List<PValueWeight>>();
        Map<String, Float> availableGWASGeneSet = new HashMap<String, Float>();

        float geneWeight = 1;
        Set<String> enteredGene = new HashSet<String>();
        List<String> containedGene = new ArrayList<String>();
        for (Pathway curPath : sortedPathwayList) {
            chrIDs.clear();
            genepBlocks.clear();
            availableGWASGeneSet.clear();
            containedGene.clear();

            Map<String, Float> geneSymbolWeightMap = curPath.getGeneSymbolWeightMap();
            for (Map.Entry<String, Float> pGeneIter : geneSymbolWeightMap.entrySet()) {
                String pGeneSymb = pGeneIter.getKey();
                if (enteredGene.contains(pGeneSymb)) {
                    containedGene.add(pGeneSymb);
                    continue;
                } else {
                    enteredGene.add(pGeneSymb);
                }

                int[] indexes1 = geneGenomeIndexes.get(pGeneSymb);
                if (indexes1 == null) {
                    continue;
                }
                gp = genePValueMap.get(pGeneSymb);
                if (gp == null) {
                    continue;
                }
                if (toWeightHyst) {
                    geneWeight = pGeneIter.getValue();
                }
                availableGWASGeneSet.put(pGeneSymb, geneWeight);
                List<PValueWeight> genePBlock = new ArrayList<PValueWeight>();
                for (int i = 0; i < gp.keySNPPositions.size(); i++) {
                    PValueWeight gpb = new PValueWeight();
                    gpb.pValue = gp.blockPValues.getQuick(i);
                    gpb.weight = geneWeight;
                    gpb.physicalPos = gp.keySNPPositions.getQuick(i);
                    genePBlock.add(gpb);
                }
                if (gp.keySNPPositions.size() == 0) {
                    PValueWeight gpb = new PValueWeight();
                    gpb.pValue = gp.pValue;
                    gpb.weight = geneWeight;
                    gpb.physicalPos = -1;
                    genePBlock.add(gpb);
                }
                genepBlocks.add(genePBlock);
                chrIDs.add(indexes1[0]);
            }
            //remove overlapped genes
            for (String ge : containedGene) {
                geneSymbolWeightMap.remove(ge);
            }
            if (genepBlocks.isEmpty()) {
                continue;
            }
            double p = calculatePathwaybasedPValuesByHYST(chrIDs, genepBlocks, ldRsMatrixes);
            pathwayPValues.add(p);
            curPath.setHystPValue(p);
        }
        sortedPathwayList.addAll(searchedPathways.values());

        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (ldRsMatrixes[chromIndex] != null && !ldRsMatrixes[chromIndex].isEmpty()) {
                ldRsMatrixes[chromIndex].releaseLDData();
            }
        }
        return pathwayPValues;
    }

    public DoubleArrayList setBasedTestByHYST(Map<String, PValueGene> genePValueMap,
            List<Pathway> pathwayList, String sourcePValueName) throws Exception {
        Map<String, Pathway> searchedPathways = new HashMap<String, Pathway>();
        DoubleArrayList pathwayPValues = new DoubleArrayList();
        Genome genome = pathwayBasedAssociation.getGeneScan().getGenome();
        Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();
        PValueGene gp = null;
        String info;
        CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes = pathwayBasedAssociation.loadBetweenGeneLDToDisk(sourcePValueName);
        boolean allNull = true;

        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (ldRsMatrixes[chromIndex] != null && !ldRsMatrixes[chromIndex].isEmpty()) {
                allNull = false;
                break;
            }
        }

        if (allNull) {
            info = "Warning, no LD information about SNPs between genes! These SNPs will be treated as independent markers!";
            LOG.info(info);
            runningResultTopComp.insertText(info);
        }
        List<Integer> chrIDs = new ArrayList<Integer>();
        List<List<PValueWeight>> genepBlocks = new ArrayList<List<PValueWeight>>();
        Map<String, Float> availableGWASGeneSet = new HashMap<String, Float>();

        float geneWeight = 1;

        for (Pathway curPath : pathwayList) {
            chrIDs.clear();
            genepBlocks.clear();
            availableGWASGeneSet.clear();

            Map<String, Float> geneSymbolWeightMap = curPath.getGeneSymbolWeightMap();
            for (Map.Entry<String, Float> pGeneIter : geneSymbolWeightMap.entrySet()) {
                String pGeneSymb = pGeneIter.getKey();

                int[] indexes1 = geneGenomeIndexes.get(pGeneSymb);
                if (indexes1 == null) {
                    continue;
                }
                gp = genePValueMap.get(pGeneSymb);
                if (gp == null) {
                    continue;
                }
                if (toWeightHyst) {
                    geneWeight = pGeneIter.getValue();
                }
                availableGWASGeneSet.put(pGeneSymb, geneWeight);
                List<PValueWeight> genePBlock = new ArrayList<PValueWeight>();
                for (int i = 0; i < gp.keySNPPositions.size(); i++) {
                    PValueWeight gpb = new PValueWeight();
                    gpb.pValue = gp.blockPValues.getQuick(i);
                    gpb.weight = geneWeight;
                    gpb.physicalPos = gp.keySNPPositions.getQuick(i);
                    genePBlock.add(gpb);
                }
                if (gp.keySNPPositions.size() == 0) {
                    PValueWeight gpb = new PValueWeight();
                    gpb.pValue = gp.pValue;
                    gpb.weight = geneWeight;
                    gpb.physicalPos = -1;
                    genePBlock.add(gpb);
                }
                genepBlocks.add(genePBlock);
                chrIDs.add(indexes1[0]);
            }
            //remove overlapped genes 
            if (genepBlocks.isEmpty()) {
                continue;
            }
            double p = calculatePathwaybasedPValuesByHYST(chrIDs, genepBlocks, ldRsMatrixes);
            pathwayPValues.add(p);
            curPath.setHystPValue(p);
        }
        pathwayList.addAll(searchedPathways.values());

        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (ldRsMatrixes[chromIndex] != null && !ldRsMatrixes[chromIndex].isEmpty()) {
                ldRsMatrixes[chromIndex].releaseLDData();
            }
        }
        return pathwayPValues;
    }

    public DoubleArrayList setBasedTestByWilcoxWhileRemovingRedundantGenes(Map<String, PValueGene> genePValueMap,
            List< Pathway> sortedPathwayList, Map<String, Double> pathwayNewPMap, String sourcePValueName, double minR2, int minSize) throws Exception {
        DoubleArrayList pathwayPValues = new DoubleArrayList();
        Genome genome = pathwayBasedAssociation.getGeneScan().getGenome();
        String geneTestMethod = pathwayBasedAssociation.getGeneScan().getTestedMethod();
        int geneScanMethod = 1;
        if (geneTestMethod != null) {
            if (geneTestMethod.equals("HYST")) {
                geneScanMethod = 0;
            }
        }
        Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();
        PValueGene gp = null;
        String info;
        CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes = pathwayBasedAssociation.loadBetweenGeneLDToDisk(sourcePValueName);
        boolean allNull = true;

        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (ldRsMatrixes[chromIndex] != null && !ldRsMatrixes[chromIndex].isEmpty()) {
                allNull = false;
                break;
            }
        }
        if (allNull) {
            info = "Warning, no LD information about SNPs between genes! These SNPs will be treated as independent markers!";
            LOG.info(info);
            runningResultTopComp.insertText(info);
        }

        Map<String, Double> pValueListInPathway = null;
        DoubleArrayList pValueListOutPathway = new DoubleArrayList();

        Set<String> allEnteredGene = new HashSet<String>();
        List<String> currentPathwayContainedGene = new ArrayList<String>();

        List<PValueGene>[] geneSetPG = new List[CHROM_NAMES.length];
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            geneSetPG[chromIndex] = new ArrayList<PValueGene>();
        }

        for (Pathway curPath : sortedPathwayList) {
            Map<String, Float> geneSymbolWeightMap = curPath.getGeneSymbolWeightMap();
            for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
                geneSetPG[chromIndex].clear();
            }
            currentPathwayContainedGene.clear();
            for (Map.Entry<String, Float> pGeneIter : geneSymbolWeightMap.entrySet()) {
                String pGeneSymb = pGeneIter.getKey();

                //may introduce biase
                if (!genePValueMap.containsKey(pGeneSymb)) {
                    continue;
                }

                if (allEnteredGene.contains(pGeneSymb)) {
                    //remove overlapped genes
                    continue;
                } else {
                    allEnteredGene.add(pGeneSymb);
                    currentPathwayContainedGene.add(pGeneSymb);
                }

                int[] indexes1 = geneGenomeIndexes.get(pGeneSymb);
                if (indexes1 == null) {
                    continue;
                }

                gp = genePValueMap.get(pGeneSymb);
                if (gp == null) {
                    continue;
                }
                geneSetPG[indexes1[0]].add(gp);
            }

            if (currentPathwayContainedGene.size() < minSize) {
                pathwayNewPMap.put(curPath.getID(), Double.NaN);
                continue;
            }

            pValueListInPathway = correctGeneLDInPathwayByEFS(geneSetPG, ldRsMatrixes, minR2, geneScanMethod);

            for (Map.Entry<String, PValueGene> mGene : genePValueMap.entrySet()) {
                if (!currentPathwayContainedGene.contains(mGene.getKey())) {
                    if (!Double.isNaN(mGene.getValue().pValue)) {
                        pValueListOutPathway.add(mGene.getValue().pValue);
                    }
                }
            }

            int subPopulationSize = pValueListInPathway.size();
            double[] pValuesInPathway = new double[subPopulationSize];
            int index = 0;
            for (Map.Entry<String, Double> pGeneIter : pValueListInPathway.entrySet()) {
                pValuesInPathway[index] = pGeneIter.getValue();
                index++;
            }
            double[] pValuesOutPathway = new double[pValueListOutPathway.size()];
            for (int t = 0; t < pValuesOutPathway.length; t++) {
                pValuesOutPathway[t] = pValueListOutPathway.getQuick(t);
            }

            if (subPopulationSize >= minSize) {
                MannWhitneyTest mt = new MannWhitneyTest(pValuesInPathway, pValuesOutPathway, H1.LESS_THAN);
                // WilcoxonTest wt = new WilcoxonTest(pValuesInPathway, median, H1.LESS_THAN);
                pathwayNewPMap.put(curPath.getID(), mt.getSP());

            } else {
                pathwayNewPMap.put(curPath.getID(), Double.NaN);
            }
        }

        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (ldRsMatrixes[chromIndex] != null && !ldRsMatrixes[chromIndex].isEmpty()) {
                ldRsMatrixes[chromIndex].releaseLDData();
            }
        }
        return pathwayPValues;
    }

    public double calculatePathwaybasedPValuesByHYST(List<Integer> chrIDs, List<List<PValueWeight>> genepBlocks,
            CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes) throws Exception {
        double MIN_R2 = 1E-3;
        List<PValueWeight> geneTmpBlocks = new ArrayList<PValueWeight>();

        List<Integer> sameChrIDIndexes = new ArrayList<Integer>();
        int chrIDNum = chrIDs.size();
        Set<Integer> allChroms = new HashSet<Integer>();

        for (int i = 0; i < chrIDNum; i++) {
            allChroms.add(chrIDs.get(i));
        }
        int[] availChroms = new int[allChroms.size()];
        int t = 0;
        for (Integer ch : allChroms) {
            availChroms[t] = ch;
            t++;
        }
        DoubleArrayList chromPValues = new DoubleArrayList();
        double p = 1;
        int curChrID;
        for (int curChrIDIndex = 0; curChrIDIndex < availChroms.length; curChrIDIndex++) {
            sameChrIDIndexes.clear();
            curChrID = availChroms[curChrIDIndex];
            for (int i = 0; i < chrIDNum; i++) {
                if (chrIDs.get(i) == curChrID) {
                    sameChrIDIndexes.add(i);
                }
            }
            if (sameChrIDIndexes.isEmpty()) {
                continue;
            }

            geneTmpBlocks.clear();
            for (int i = sameChrIDIndexes.size() - 1; i >= 0; i--) {
                geneTmpBlocks.addAll(genepBlocks.get(sameChrIDIndexes.get(i)));
            }
            int blockSize = geneTmpBlocks.size();
            DenseDoubleMatrix2D rLDMatrix = new DenseDoubleMatrix2D(blockSize, blockSize);
            rLDMatrix.assign(0);
            IntArrayList orginalPositions = new IntArrayList(blockSize);
            Collections.sort(geneTmpBlocks, new PValueWeightComparatorIndex());
            for (int k = 0; k < blockSize; k++) {
                rLDMatrix.setQuick(k, k, 1);
                if (ldRsMatrixes[curChrID] != null) {
                    for (int j = k + 1; j < blockSize; j++) {
                        double x = ldRsMatrixes[curChrID].getLDAt(geneTmpBlocks.get(k).physicalPos, geneTmpBlocks.get(j).physicalPos);
                        if (x > MIN_R2) {
                            // ldCorr.setLDAt(pvalueWeightList.get(k).physicalPos, pvalueWeightList.get(t).physicalPos, (float) x);
                            rLDMatrix.setQuick(k, j, x);
                            rLDMatrix.setQuick(j, k, x);
                        } else {
                            rLDMatrix.setQuick(k, j, 0);
                            rLDMatrix.setQuick(j, k, 0);
                        }

                    }
                } else {
                    for (int j = k + 1; j < blockSize; j++) {
                        rLDMatrix.setQuick(k, j, 0);
                        rLDMatrix.setQuick(j, k, 0);
                    }
                }
                orginalPositions.add(geneTmpBlocks.get(k).physicalPos);
                geneTmpBlocks.get(k).physicalPos = k;
            }
            p = SetBasedTest.combinePValuebyScaleedFisherCombinationTestCovLogP(geneTmpBlocks.toArray(new PValueWeight[0]), rLDMatrix);
            if (Double.isNaN(p)) {
                continue;
            }
            //restore the postions
            for (int k = 0; k < blockSize; k++) {
                geneTmpBlocks.get(k).physicalPos = orginalPositions.getQuick(k);
            }
            chromPValues.add(p);
        }
// SNPs on differnet chromosome are independent
        double Y = 0;
        for (int i = chromPValues.size() - 1; i >= 0; i--) {
            Y += (-2 * Math.log(chromPValues.getQuick(i)));
        }
        if (Double.isInfinite(Y)) {
            return 0;
        }

        p = Probability.chiSquareComplemented(2 * chromPValues.size(), Y);
        if (p < 0.0) {
            p = 0.0;
        }

        return p;
    }

    public double averageLDInPathway(List<Integer> chrIDs, List<List<PValueWeight>> genepBlocks,
            CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes) throws Exception {
        double MIN_R2 = 1E-3;
        List<PValueWeight> geneTmpBlocks = new ArrayList<PValueWeight>();

        List<Integer> sameChrIDIndexes = new ArrayList<Integer>();
        int chrIDNum = chrIDs.size();
        Set<Integer> allChroms = new HashSet<Integer>();

        for (int i = 0; i < chrIDNum; i++) {
            allChroms.add(chrIDs.get(i));
        }
        int[] availChroms = new int[allChroms.size()];
        int t = 0;
        for (Integer ch : allChroms) {
            availChroms[t] = ch;
            t++;
        }

        int curChrID;
        double correlation = 0;
        for (int curChrIDIndex = 0; curChrIDIndex < availChroms.length; curChrIDIndex++) {
            sameChrIDIndexes.clear();
            curChrID = availChroms[curChrIDIndex];
            for (int i = 0; i < chrIDNum; i++) {
                if (chrIDs.get(i) == curChrID) {
                    sameChrIDIndexes.add(i);
                }
            }
            if (sameChrIDIndexes.isEmpty()) {
                continue;
            }

            geneTmpBlocks.clear();
            for (int i = sameChrIDIndexes.size() - 1; i >= 0; i--) {
                geneTmpBlocks.addAll(genepBlocks.get(sameChrIDIndexes.get(i)));
            }
            int blockSize = geneTmpBlocks.size();
            DenseDoubleMatrix2D rLDMatrix = new DenseDoubleMatrix2D(blockSize, blockSize);
            rLDMatrix.assign(0);
            IntArrayList orginalPositions = new IntArrayList(blockSize);
            Collections.sort(geneTmpBlocks, new PValueWeightComparatorIndex());
            for (int k = 0; k < blockSize; k++) {
                rLDMatrix.setQuick(k, k, 1);
                if (ldRsMatrixes[curChrID] != null) {
                    for (int j = k + 1; j < blockSize; j++) {
                        double x = ldRsMatrixes[curChrID].getLDAt(geneTmpBlocks.get(k).physicalPos, geneTmpBlocks.get(j).physicalPos);
                        if (x > MIN_R2) {
                            // ldCorr.setLDAt(pvalueWeightList.get(k).physicalPos, pvalueWeightList.get(t).physicalPos, (float) x);
                            rLDMatrix.setQuick(k, j, x);
                            rLDMatrix.setQuick(j, k, x);
                        } else {
                            rLDMatrix.setQuick(k, j, 0);
                            rLDMatrix.setQuick(j, k, 0);
                        }
                        //when r2
                        //y = 0.7723x6 - 1.5659x5 + 1.201x4 - 0.2355x3 + 0.2184x2 + 0.6086x
                        // x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;
                        //x=Math.sqrt(x);
                        x = x * x;
                        correlation += x;
                    }
                }
                orginalPositions.add(geneTmpBlocks.get(k).physicalPos);
                geneTmpBlocks.get(k).physicalPos = k;
            }
            //restore the postions
            for (int k = 0; k < blockSize; k++) {
                geneTmpBlocks.get(k).physicalPos = orginalPositions.getQuick(k);
            }
        }
        return correlation;
    }

    public double significanceTest1(List<Pathway> pathwayList, DoubleArrayList pathwayPValueHyper,
            DoubleArrayList pathwayPValueWilcoxon, final List<PValueGene> geneList, double[] adjustePCutoffs, StringBuilder inforSb) throws Exception {

        int pathwayNumHyper = pathwayPValueHyper.size();
        // Unified Test Code
        // 0 Benjamini & Hochberg (1995)
        // 1 Standard Bonferroni
        // 2 Adaptive Benjamini (2006)
        // 3 Storey (2002)
        // 4 Hypergeometric Test
        // 5 Fisher Combination Test
        // 6 Simes Combination Test
        // 7 Fixed p-value threshold

        StringBuilder logInfo2 = new StringBuilder();
        StringBuilder logInfo3 = new StringBuilder();

        logInfo2.append(" For enrichment test of selected risk genes based on Hypergeometric distribution test:\n");
        logInfo3.append("\nFor enrichment test of risk genes based on Wilcoxon signed rank test (one sided):\n");

        String multiTestMethodName = pathwayBasedAssociation.getPathwayMultipleTestingMethod();
        double pathwayMultipleTestingPValue = pathwayBasedAssociation.getPathwayMultipleTestingPValue();

        String multiTestEnrichmentMethodName = pathwayBasedAssociation.getPathwayEnrichmentMultipleTestingMethod();
        double pathwayMultipleTestingEnrichmentPValue = pathwayBasedAssociation.getPathwayEnrichmentMultipleTestingPValue();

        double adjustedPathwayMultipleTestingPValueHyper = pathwayMultipleTestingEnrichmentPValue;
        double adjustedPathwayMultipleTestingPValueWilcoxon = pathwayMultipleTestingEnrichmentPValue;

        if (multiTestEnrichmentMethodName.equals("Benjamini & Hochberg (1995)")) {
            // Benjamini & Hochberg, 1995 test
            adjustedPathwayMultipleTestingPValueHyper = MultipleTestingMethod.BenjaminiHochbergFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueHyper);
            logInfo2.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate. ");

            adjustedPathwayMultipleTestingPValueWilcoxon = MultipleTestingMethod.BenjaminiHochbergFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueWilcoxon);
            logInfo3.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate. ");

        } else if (multiTestEnrichmentMethodName.equals("Standard Bonferroni")) {
            // Standard Bonferroni        
            adjustedPathwayMultipleTestingPValueHyper = pathwayMultipleTestingEnrichmentPValue / pathwayNumHyper;
            logInfo2.append("The significance level for Bonferroni correction  to control familywise error rate ");
            logInfo2.append(pathwayMultipleTestingPValue);
            logInfo2.append(" for gene-sets ");
            logInfo2.append(Util.formatPValue(adjustedPathwayMultipleTestingPValueHyper));
            logInfo2.append("=(");
            logInfo2.append(pathwayMultipleTestingPValue);
            logInfo2.append("/");
            logInfo2.append(pathwayNumHyper);
            logInfo2.append(") in database.");
        } else if (multiTestEnrichmentMethodName.equals("Benjamini & Yekutieli (2001)")) {
            // Adaptive Benjamini 2006 test
            adjustedPathwayMultipleTestingPValueHyper = MultipleTestingMethod.BenjaminiYekutieliFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueHyper);
            logInfo2.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");

            adjustedPathwayMultipleTestingPValueWilcoxon = MultipleTestingMethod.BenjaminiYekutieliFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueWilcoxon);
            logInfo3.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");
        } else if (multiTestEnrichmentMethodName.equals("Fixed p-value threshold")) {
            logInfo2.append("The fixed p value threshold ");
            logInfo3.append("The fixed p value threshold ");
        } else if (multiTestEnrichmentMethodName.equals("Storey (2002)")) {
            // Storey, 2002 test
            adjustedPathwayMultipleTestingPValueHyper = MultipleTestingMethod.storeyFDRTest(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueHyper);
            logInfo2.append("The significance level for Storey (2002) FDR test to control error rate ");

            adjustedPathwayMultipleTestingPValueWilcoxon = MultipleTestingMethod.storeyFDRTest(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueWilcoxon);
            logInfo3.append("The significance level for Storey (2002) FDR test to control error rate ");
        }

        if (!multiTestEnrichmentMethodName.equals("Standard Bonferroni")) {
            logInfo2.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo2.append(" for gene-sets is ");
            logInfo2.append(Util.formatPValue(adjustedPathwayMultipleTestingPValueHyper));
            logInfo2.append(".");

            logInfo3.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo3.append(" for gene-sets is ");
            logInfo3.append(Util.formatPValue(adjustedPathwayMultipleTestingPValueWilcoxon));
            logInfo3.append(".");
        }

        //Use Hypergenometirc or Wilcoxon tests to prune
        //by default checking
        boolean toUseWilcoxonTest = true;
        if (!pruningMethod.equals("LD-attenuating rank-sum test")) {
            toUseWilcoxonTest = false;
        }
        Set<String> signifAssocPathwayGenes = new HashSet<String>();

        int significantPahtwaysNum = 0;

        List<Pathway> tmpPathwayList = new ArrayList<Pathway>();
        boolean filterNonSigEnriched = pathwayBasedAssociation.isFilterNonSigEnriched();
        significantPahtwaysNum = 0;
        int totalPahwayNum = pathwayList.size();
        Set<String> signifEnrichedPathwayGenes = new HashSet<String>();
        for (int i = 0; i < totalPahwayNum; i++) {
            Pathway pathway = pathwayList.get(i);
            if (toUseWilcoxonTest) {
                if (!filterNonSigEnriched) {
                    tmpPathwayList.add(pathway);
                    if (pathway.getWilcoxonPValue() <= adjustedPathwayMultipleTestingPValueWilcoxon) {
                        significantPahtwaysNum++;
                        signifEnrichedPathwayGenes.addAll(pathway.getGeneSymbols());
                    }
                } else if (pathway.getWilcoxonPValue() <= adjustedPathwayMultipleTestingPValueWilcoxon) {
                    significantPahtwaysNum++;
                    tmpPathwayList.add(pathway);
                    signifEnrichedPathwayGenes.addAll(pathway.getGeneSymbols());
                }
            } else if (!filterNonSigEnriched) {
                tmpPathwayList.add(pathway);
                if (pathway.getEnrichedPValue() <= adjustedPathwayMultipleTestingPValueHyper) {
                    significantPahtwaysNum++;
                    signifEnrichedPathwayGenes.addAll(pathway.getGeneSymbols());
                }
            } else if (pathway.getEnrichedPValue() <= adjustedPathwayMultipleTestingPValueHyper) {
                significantPahtwaysNum++;
                tmpPathwayList.add(pathway);
                signifEnrichedPathwayGenes.addAll(pathway.getGeneSymbols());
            }
        }

        inforSb.append('\n');

        /*
         logOutput.info(logInfo2);
         if (GlobalManager.aotcWindow != null) {
         SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         GlobalManager.aotcWindow.insertMessage("INFO " + sdfData.format(new Date()) + logInfo2.toString());
         }
         if (runningResultTopComp != null) {
         runningResultTopComp.insertText(logInfo2.toString());
         }
         inforSb.append(logInfo2);
         inforSb.append('\n');
         */
        logInfo2.delete(0, logInfo2.length());

        logOutput.info(logInfo3);
        if (GlobalManager.aotcWindow != null) {
            SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GlobalManager.aotcWindow.insertMessage("\nINFO " + sdfData.format(new Date()) + logInfo3.toString());
        }
        if (runningResultTopComp != null) {
            runningResultTopComp.insertText(logInfo3.toString());
        }
        inforSb.append(logInfo3);
        inforSb.append('\n');
        logInfo3.delete(0, logInfo3.length());

        if (filterNonSigEnriched) {
            pathwayList.clear();
            pathwayList.addAll(tmpPathwayList);
            tmpPathwayList.clear();
            //logInfo1.append("\nAfter pruned by ").append(toUseWilcoxonTest ? "Wilcoxon" : "Hypergeometric").append(" test, ").append(pathwayList.size()).append(" genes sets were retained!");
        }

        //calculate p-value cutoff for genes in signficant pathways
        int geneNum = geneList.size();
        // Unified Test Code
        // 0 Benjamini & Hochberg (1995)
        // 1 Standard Bonferroni
        // 2 Adaptive Benjamini (2006)
        // 3 Storey (2002)
        // 4 Hypergeometric Test
        // 5 Fisher Combination Test
        // 6 Simes Combination Test
        // 7 Fixed p-value threshold
        double pathwayGenePValueCutoff = pathwayBasedAssociation.getPathwayGeneMultipleTestingPValue();
        String pathwayGenePValueMultiTestMethod = pathwayBasedAssociation.getPathwayGeneMultipleTestingMethod();

        double adjustedPValue = pathwayGenePValueCutoff;
        if (!signifAssocPathwayGenes.isEmpty()) {
            // we need get all p-values to determin the threshlod to select
            // genes
            DoubleArrayList geneInSigPathwayspValues = new DoubleArrayList(signifAssocPathwayGenes.size());
            for (int i = 0; i < geneNum; i++) {
                if (signifAssocPathwayGenes.contains(geneList.get(i).getSymbol())) {
                    geneInSigPathwayspValues.add(geneList.get(i).pValue);
                }
            }
            geneInSigPathwayspValues.quickSort();

            if (pathwayGenePValueMultiTestMethod.equals("Benjamini & Hochberg (1995)")) {
                // Benjamini & Hochberg, 1995 test
                adjustedPValue = MultipleTestingMethod.BenjaminiHochbergFDR("Genes in significant pathways", pathwayGenePValueCutoff, geneInSigPathwayspValues);

            } else if (pathwayGenePValueMultiTestMethod.equals("Standard Bonferroni")) {
                adjustedPValue = pathwayGenePValueCutoff / geneInSigPathwayspValues.size();

            } else if (pathwayGenePValueMultiTestMethod.equals("Benjamini & Yekutieli (2001)")) {
                // Adaptive Benjamini 2006 test
                adjustedPValue = MultipleTestingMethod.BenjaminiYekutieliFDR("Genes in significant pathways", pathwayGenePValueCutoff, geneInSigPathwayspValues);

            } else if (pathwayGenePValueMultiTestMethod.equals("Storey (2002)")) {
                // Storey, 2002 test
                adjustedPValue = MultipleTestingMethod.storeyFDRTest("Genes in significant pathways", pathwayGenePValueCutoff, geneInSigPathwayspValues);

            } else if (pathwayGenePValueMultiTestMethod.equals("Fixed p-value threshold")) {

            }
            geneInSigPathwayspValues.clear();
            if (pathwayGenePValueMultiTestMethod.equals("Standard Bonferroni") && pathwayGenePValueMultiTestMethod.equals("Fixed p-value threshold")) {

            }
        } else {
            //do not carry out hyper geometric test otherwise
            adjustedPValue = 0;
        }

        Set<String> signifGenes = new HashSet<String>();
        //get inter section of the genes 
        signifAssocPathwayGenes.retainAll(signifEnrichedPathwayGenes);
        for (int i = 0; i < geneNum; i++) {
            if (signifAssocPathwayGenes.contains(geneList.get(i).getSymbol()) && geneList.get(i).pValue <= adjustedPValue) {
                signifGenes.add(geneList.get(i).getSymbol());
            }
        }
        StringBuilder logInfo1 = new StringBuilder();
        logInfo1.append("\nIn all of significantly enriched gene sets (by ").append(toUseWilcoxonTest ? "Wilcoxon" : "Hypergeometric").append(" test), ").append(signifGenes.size()).append(" genes are significant (p-value cutoff ").append(Util.formatPValue(adjustedPValue)).append(" according to ").append(pathwayGenePValueMultiTestMethod).append(" test for the overall error rate ").append(pathwayGenePValueCutoff).append("\n").append(signifGenes.toString());
        logInfo1.append("\n****************************************************************************\n");

        logOutput.info(logInfo1);
        if (GlobalManager.aotcWindow != null) {
            SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GlobalManager.aotcWindow.insertMessage("INFO " + sdfData.format(new Date()) + logInfo1.toString());
        }

        if (runningResultTopComp != null) {
            runningResultTopComp.insertText(logInfo1.toString());
        }
        inforSb.append(logInfo1);
        logInfo1.delete(0, logInfo1.length());

        adjustePCutoffs[0] = 0;
        adjustePCutoffs[1] = adjustedPathwayMultipleTestingPValueHyper;
        adjustePCutoffs[2] = adjustedPathwayMultipleTestingPValueWilcoxon;
        return adjustedPValue;

    }

    public double significanceTest2(List<Pathway> pathwayList, DoubleArrayList[] pathwayPValueOrg, Map<String, Double> genePMap, double[] adjustePCutoffs, StringBuilder inforSb) throws Exception {

        int pathwayNumHyper = pathwayPValueOrg[0].size();
        // Unified Test Code
        // 0 Benjamini & Hochberg (1995)
        // 1 Standard Bonferroni
        // 2 Adaptive Benjamini (2006)
        // 3 Storey (2002)
        // 4 Hypergeometric Test
        // 5 Fisher Combination Test
        // 6 Simes Combination Test
        // 7 Fixed p-value threshold

        StringBuilder logInfo1 = new StringBuilder();
        StringBuilder logInfo2 = new StringBuilder();
        StringBuilder logInfo3 = new StringBuilder();

        logInfo2.append(" For enrichment test of selected risk genes based on Hypergeometric distribution test:\n");
        logInfo3.append("\nFor enrichment test of risk genes based on Wilcoxon signed rank test (one sided):\n");
        logInfo1.append("\nFor association test of risk genes based on HYST:\n");

        String multiTestMethodName = pathwayBasedAssociation.getPathwayMultipleTestingMethod();
        double pathwayMultipleTestingPValue = pathwayBasedAssociation.getPathwayMultipleTestingPValue();

        String multiTestEnrichmentMethodName = pathwayBasedAssociation.getPathwayEnrichmentMultipleTestingMethod();
        double pathwayMultipleTestingEnrichmentPValue = pathwayBasedAssociation.getPathwayEnrichmentMultipleTestingPValue();

        double adjustedPathwayMultipleTestingPValueHyst = pathwayMultipleTestingEnrichmentPValue;
        double adjustedPathwayMultipleTestingPValueHyper = pathwayMultipleTestingEnrichmentPValue;
        double adjustedPathwayMultipleTestingPValueWilcoxon = pathwayMultipleTestingEnrichmentPValue;

        if (multiTestEnrichmentMethodName.equals("Benjamini & Hochberg (1995)")) {
            // Benjamini & Hochberg, 1995 test
            adjustedPathwayMultipleTestingPValueHyst = MultipleTestingMethod.BenjaminiHochbergFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueOrg[0]);
            logInfo1.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate. ");

            adjustedPathwayMultipleTestingPValueHyper = MultipleTestingMethod.BenjaminiHochbergFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueOrg[1]);
            logInfo2.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate. ");

            adjustedPathwayMultipleTestingPValueWilcoxon = MultipleTestingMethod.BenjaminiHochbergFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueOrg[2]);
            logInfo3.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate. ");

        } else if (multiTestEnrichmentMethodName.equals("Standard Bonferroni")) {
            // Standard Bonferroni     
            adjustedPathwayMultipleTestingPValueHyst = pathwayMultipleTestingEnrichmentPValue / pathwayNumHyper;
            logInfo1.append("The significance level for Bonferroni correction  to control familywise error rate ");
            logInfo1.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo1.append(" for gene-sets ");
            logInfo1.append(Util.formatPValue(adjustedPathwayMultipleTestingPValueHyst));
            logInfo1.append("=(");
            logInfo1.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo1.append("/");
            logInfo1.append(pathwayNumHyper);
            logInfo1.append(") in database.");

            adjustedPathwayMultipleTestingPValueHyper = pathwayMultipleTestingEnrichmentPValue / pathwayNumHyper;
            logInfo2.append("The significance level for Bonferroni correction  to control familywise error rate ");
            logInfo2.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo2.append(" for gene-sets ");
            logInfo2.append(Util.formatPValue(adjustedPathwayMultipleTestingPValueHyper));
            logInfo2.append("=(");
            logInfo2.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo2.append("/");
            logInfo2.append(pathwayNumHyper);
            logInfo2.append(") in database.");

            adjustedPathwayMultipleTestingPValueWilcoxon = pathwayMultipleTestingEnrichmentPValue / pathwayNumHyper;
            logInfo3.append("The significance level for Bonferroni correction  to control familywise error rate ");
            logInfo3.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo3.append(" for gene-sets ");
            logInfo3.append(Util.formatPValue(adjustedPathwayMultipleTestingPValueWilcoxon));
            logInfo3.append("=(");
            logInfo3.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo3.append("/");
            logInfo3.append(pathwayNumHyper);
            logInfo3.append(") in database.");

        } else if (multiTestEnrichmentMethodName.equals("Benjamini & Yekutieli (2001)")) {
            // Adaptive Benjamini 2006 test
            adjustedPathwayMultipleTestingPValueHyst = MultipleTestingMethod.BenjaminiYekutieliFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueOrg[0]);
            logInfo1.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");

            adjustedPathwayMultipleTestingPValueHyper = MultipleTestingMethod.BenjaminiYekutieliFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueOrg[1]);
            logInfo2.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");

            adjustedPathwayMultipleTestingPValueWilcoxon = MultipleTestingMethod.BenjaminiYekutieliFDR(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueOrg[2]);
            logInfo3.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");
        } else if (multiTestEnrichmentMethodName.equals("Fixed p-value threshold")) {
            logInfo1.append("The fixed p value threshold ");
            logInfo2.append("The fixed p value threshold ");
            logInfo3.append("The fixed p value threshold ");
        } else if (multiTestEnrichmentMethodName.equals("Storey (2002)")) {
            // Storey, 2002 test
            adjustedPathwayMultipleTestingPValueHyst = MultipleTestingMethod.storeyFDRTest(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueOrg[0]);
            logInfo1.append("The significance level for Storey (2002) FDR test to control error rate ");

            adjustedPathwayMultipleTestingPValueHyper = MultipleTestingMethod.storeyFDRTest(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueOrg[1]);
            logInfo2.append("The significance level for Storey (2002) FDR test to control error rate ");

            adjustedPathwayMultipleTestingPValueWilcoxon = MultipleTestingMethod.storeyFDRTest(null, pathwayMultipleTestingEnrichmentPValue, pathwayPValueOrg[2]);
            logInfo3.append("The significance level for Storey (2002) FDR test to control error rate ");
        }

        if (!multiTestEnrichmentMethodName.equals("Standard Bonferroni")) {
            logInfo1.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo1.append(" for gene-sets is ");
            logInfo1.append(Util.formatPValue(adjustedPathwayMultipleTestingPValueHyst));
            logInfo1.append(".");

            logInfo2.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo2.append(" for gene-sets is ");
            logInfo2.append(Util.formatPValue(adjustedPathwayMultipleTestingPValueHyper));
            logInfo2.append(".");

            logInfo3.append(pathwayMultipleTestingEnrichmentPValue);
            logInfo3.append(" for gene-sets is ");
            logInfo3.append(Util.formatPValue(adjustedPathwayMultipleTestingPValueWilcoxon));
            logInfo3.append(".");
        }

        //Use Hypergenometirc or Wilcoxon tests to prune
        //by default checking
        boolean toUseWilcoxonTest = true;
        if (!pruningMethod.equals("LD-attenuating rank-sum test")) {
            toUseWilcoxonTest = false;
        }

        int significantPahtwaysNum = 0;

        significantPahtwaysNum = 0;
        int totalPahwayNum = pathwayList.size();
        Set<String> signifEnrichedPathwayGenes = new HashSet<String>();
        for (int i = 0; i < totalPahwayNum; i++) {
            Pathway pathway = pathwayList.get(i);
            if (toUseWilcoxonTest) {
                if (pathway.getWilcoxonPValue() <= adjustedPathwayMultipleTestingPValueWilcoxon) {
                    significantPahtwaysNum++;

                    signifEnrichedPathwayGenes.addAll(pathway.getGeneSymbols());
                }
            } else if (pathway.getEnrichedPValue() <= adjustedPathwayMultipleTestingPValueHyper) {
                significantPahtwaysNum++;

                signifEnrichedPathwayGenes.addAll(pathway.getGeneSymbols());
            }
        }

        inforSb.append('\n');

        logOutput.info(logInfo1);
        if (GlobalManager.aotcWindow != null) {
            SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GlobalManager.aotcWindow.insertMessage("INFO " + sdfData.format(new Date()) + logInfo1.toString());
        }
        if (runningResultTopComp != null) {
            runningResultTopComp.insertText(logInfo1.toString());
        }
        inforSb.append(logInfo1);
        inforSb.append('\n');
        logInfo1.delete(0, logInfo1.length());

        /*
         logOutput.info(logInfo2);
         if (GlobalManager.aotcWindow != null) {
         SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         GlobalManager.aotcWindow.insertMessage("INFO " + sdfData.format(new Date()) + logInfo2.toString());
         }
         if (runningResultTopComp != null) {
         runningResultTopComp.insertText(logInfo2.toString());
         }
         inforSb.append(logInfo2);
         inforSb.append('\n');
         logInfo2.delete(0, logInfo2.length());
         */
        logOutput.info(logInfo3);
        if (GlobalManager.aotcWindow != null) {
            SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GlobalManager.aotcWindow.insertMessage("\nINFO " + sdfData.format(new Date()) + logInfo3.toString());
        }
        if (runningResultTopComp != null) {
            runningResultTopComp.insertText(logInfo3.toString());
        }
        inforSb.append(logInfo3);
        inforSb.append('\n');
        logInfo3.delete(0, logInfo3.length());

        //logInfo1.append("\nAfter pruned by ").append(toUseWilcoxonTest ? "Wilcoxon" : "Hypergeometric").append(" test, ").append(pathwayList.size()).append(" genes sets were retained!");
        //calculate p-value cutoff for genes in signficant pathways
        // Unified Test Code
        // 0 Benjamini & Hochberg (1995)
        // 1 Standard Bonferroni
        // 2 Adaptive Benjamini (2006)
        // 3 Storey (2002)
        // 4 Hypergeometric Test
        // 5 Fisher Combination Test
        // 6 Simes Combination Test
        // 7 Fixed p-value threshold
        double pathwayGenePValueCutoff = pathwayBasedAssociation.getPathwayGeneMultipleTestingPValue();
        String pathwayGenePValueMultiTestMethod = pathwayBasedAssociation.getPathwayGeneMultipleTestingMethod();

        double adjustedPValue = pathwayGenePValueCutoff;
        if (!signifEnrichedPathwayGenes.isEmpty()) {
            // we need get all p-values to determin the threshlod to select
            // genes
            DoubleArrayList geneInSigPathwayspValues = new DoubleArrayList(signifEnrichedPathwayGenes.size());
            for (String geneS : signifEnrichedPathwayGenes) {
                Double pp = genePMap.get(geneS);
                if (pp != null) {
                    geneInSigPathwayspValues.add(pp);
                }
            }

            geneInSigPathwayspValues.quickSort();

            if (pathwayGenePValueMultiTestMethod.equals("Benjamini & Hochberg (1995)")) {
                // Benjamini & Hochberg, 1995 test
                adjustedPValue = MultipleTestingMethod.BenjaminiHochbergFDR("Genes in significant pathways", pathwayGenePValueCutoff, geneInSigPathwayspValues);

            } else if (pathwayGenePValueMultiTestMethod.equals("Standard Bonferroni")) {
                adjustedPValue = pathwayGenePValueCutoff / geneInSigPathwayspValues.size();

            } else if (pathwayGenePValueMultiTestMethod.equals("Benjamini & Yekutieli (2001)")) {
                // Adaptive Benjamini 2006 test
                adjustedPValue = MultipleTestingMethod.BenjaminiYekutieliFDR("Genes in significant pathways", pathwayGenePValueCutoff, geneInSigPathwayspValues);

            } else if (pathwayGenePValueMultiTestMethod.equals("Storey (2002)")) {
                // Storey, 2002 test
                adjustedPValue = MultipleTestingMethod.storeyFDRTest("Genes in significant pathways", pathwayGenePValueCutoff, geneInSigPathwayspValues);

            } else if (pathwayGenePValueMultiTestMethod.equals("Fixed p-value threshold")) {

            }
            geneInSigPathwayspValues.clear();
            if (pathwayGenePValueMultiTestMethod.equals("Standard Bonferroni") && pathwayGenePValueMultiTestMethod.equals("Fixed p-value threshold")) {

            }
        } else {
            //do not carry out hyper geometric test otherwise
            adjustedPValue = 0;
        }

        Set<String> signifGenes = new HashSet<String>();
        //get inter section of the genes 

        for (String geneS : signifEnrichedPathwayGenes) {
            Double pp = genePMap.get(geneS);
            if (pp != null) {
                if (pp <= adjustedPValue) {
                    signifGenes.add(geneS);
                }
            }
        }

        logInfo1.delete(0, logInfo1.length());
        logInfo1.append("\nIn all of significantly enriched gene sets (by ").append(toUseWilcoxonTest ? "Wilcoxon" : "Hypergeometric").append(" test), ").append(signifGenes.size()).append(" genes are significant (p-value cutoff ").append(Util.formatPValue(adjustedPValue)).append(" according to ").append(pathwayGenePValueMultiTestMethod).append(" test for the overall error rate ").append(pathwayGenePValueCutoff).append("\n").append(signifGenes.toString());
        logInfo1.append("\n****************************************************************************\n");

        logOutput.info(logInfo1);
        if (GlobalManager.aotcWindow != null) {
            SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GlobalManager.aotcWindow.insertMessage("INFO " + sdfData.format(new Date()) + logInfo1.toString());
        }

        if (runningResultTopComp != null) {
            runningResultTopComp.insertText(logInfo1.toString());
        }
        inforSb.append(logInfo1);
        logInfo1.delete(0, logInfo1.length());

        adjustePCutoffs[0] = adjustedPathwayMultipleTestingPValueHyst;
        adjustePCutoffs[1] = adjustedPathwayMultipleTestingPValueHyper;
        adjustePCutoffs[2] = adjustedPathwayMultipleTestingPValueWilcoxon;
        return adjustedPValue;

    }

    /**
     *
     * @param pathwaySet1
     * @param seedGenes
     * @param pathwayLink
     * @param addedName
     * @return
     * @throws Exception
     */
    public final String generatePathwayStringAnnoation(final List<Pathway> pathwaySet1,
            double thresholdPathwayP1, double thresholdPathwayP2, Map<String, PValueGene> genePValueMap,
            Map<String, int[]> geneGenomeIndexes, final Set<String> seedGenes, final String pathwayLink, final String addedName,
            boolean filterNonEriched) throws Exception {
        List<String[]> annotatedList = new ArrayList<String[]>();
        int exportPathwayNum = 0;
        if (noNeedSNP) {
            exportPathwayNum = exportPathwayGeneListNoSNP(pathwaySet1, thresholdPathwayP1, thresholdPathwayP2, genePValueMap, geneGenomeIndexes, pathwayLink, annotatedList, filterNonEriched);
        } else {
            // exportPathwayGeneListWithSNP(pathwaySet1, thresholdPathwayP1, pathwaySet2, thresholdPathwayP2, pathwayLink, annotatedList, filterNonEriched);
        }

        StringBuilder logInfo = new StringBuilder("In total ");
        String finalFileName = pathwayExportPath;
        if (pathwayExportFormat.equals("Text Format")) {
            if (!finalFileName.endsWith(".txt")) {
                if (addedName != null) {
                    finalFileName += ("." + addedName);
                }
                finalFileName = finalFileName + ".txt";
            }
            File saveFile = new File(finalFileName);
            LocalFile.writeData(saveFile.getCanonicalPath(), annotatedList,
                    "\t", false);
            logInfo.append(exportPathwayNum);
            logInfo.append(" gene-sets with p values <= ").append(pathwayBasedAssociation.getPathwayPValueExport()).append(" (by HYST)");
            if (filterNonEriched) {
                logInfo.append(" and significantly enriched (by Hypergeometric distribution test)");
            }
            logInfo.append(" are saved in ");
            logInfo.append(saveFile.getCanonicalPath());
            logInfo.append('\n');
        } else {
            if (!finalFileName.endsWith(".xls")) {
                if (addedName != null) {
                    finalFileName += ("." + addedName);
                }
                finalFileName = finalFileName + ".xls";
            }
            File saveFile = new File(finalFileName);
            LocalExcelFile.writeArray2ExcelFile(saveFile.getCanonicalPath(),
                    annotatedList, true);
            logInfo.append(pathwaySet1.size());
            logInfo.append(" gene-sets with p values <= ").append(pathwayBasedAssociation.getPathwayPValueExport()).append(" (by HYST) are saved in ");
            logInfo.append(saveFile.getCanonicalPath());
            logInfo.append('\n');
        }
        annotatedList = null;
        return logInfo.toString();
    }

    /**
     *
     * @param pathwaySet
     * @param pathwayLink
     * @param annotatedList
     * @throws Exception
     */
    public final int exportPathwayGeneListNoSNP(final List<Pathway> pathwaySet1,
            double thresholdPathwayP1, double thresholdPathwayP2, Map<String, PValueGene> genePValueMap, Map<String, int[]> geneGenomeIndexes,
            final String pathwayLink, final List<String[]> annotatedList, boolean filterNonEriched) throws Exception {
        Genome genome = pathwayBasedAssociation.getGeneScan().getGenome();

        // int SNPpValueTypeNum = testedSNPIndex.length;
        DecimalFormat df = new DecimalFormat("0.000E00");
        String[] fixedColumnNames = null;
        if (toWeightHyst) {
            fixedColumnNames = new String[]{"GeneSet_ID", "GeneSet_PValue_HYST",
                "IsSignificant_HYST", "GeneSet_PValue_Hypergeometric",
                "IsSignificant_Hypergeometric", "Total_GeneSet_Gene#", "GeneSet_Name", "GeneSet_URL", "Gene_Symbol", "Gene_PValue",
                "Entrez_GeneID", "Chromosome",
                "Start_Position", "Length", "SNP#", "GeneWeight"};
        } else {
            fixedColumnNames = new String[]{"GeneSet_ID", "GeneSet_PValue_HYST",
                "IsSignificant_HYST", "GeneSet_PValue_Hypergeometric",
                "IsSignificant_Hypergeometric", "Total_GeneSet_Gene#", "GeneSet_Name", "GeneSet_URL", "Gene_Symbol", "Gene_PValue",
                "Entrez_GeneID", "Chromosome",
                "Start_Position", "Length", "SNP#"};
        }
        //String[] columnNames = new String[fixedColumnNames.length + SNPpValueTypeNum];
        String[] columnNames = new String[fixedColumnNames.length];
        System.arraycopy(fixedColumnNames, 0, columnNames, 0, fixedColumnNames.length);
        annotatedList.add(columnNames);
        int colNum = columnNames.length;

        String[] values = null;
        int pathwayGeneCounter = 0;
        PValueGene p;

        //map genes on chromsomes so that each time only load one chromosome into memory
        List<Integer>[] chromItemIndexes = new List[CHROM_NAMES.length];
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            chromItemIndexes[i] = new ArrayList<Integer>();
        }

        int pathwayCounter = 0;
        //the first row is the head titles
        int itemNum = 1;
        for (Pathway curPath : pathwaySet1) {
            if (curPath == null) {
                continue;
            }
            // System.out.println(curPath.getID());
            pathwayGeneCounter = 0;
            Map<String, Float> geneSymbolWeightMap = curPath.getGeneSymbolWeightMap();

            if (geneSymbolWeightMap.isEmpty()) {
                values = new String[colNum];
                values[0] = curPath.getID();

                values[1] = df.format(curPath.getHystPValue());
                if (curPath.getHystPValue() <= thresholdPathwayP1) {
                    values[2] = "Y";
                } else {
                    values[2] = "N";
                }

                if (Double.isNaN(curPath.getEnrichedPValue())) {
                    values[3] = df.format(curPath.getEnrichedPValue());
                    if (curPath.getEnrichedPValue() <= thresholdPathwayP2) {
                        values[4] = "Y";
                        pathwayCounter++;
                    } else {
                        if (filterNonEriched) {
                            continue;
                        }
                        values[4] = "N";
                        pathwayCounter++;
                    }
                } else {
                    if (filterNonEriched) {
                        continue;
                    }
                    values[3] = "-";
                    values[4] = "-";
                    pathwayCounter++;
                }
                values[5] = String.valueOf(curPath.getTotalGeneNum());

                values[6] = curPath.getName();
                values[7] = pathwayLink + curPath.getURL();
                annotatedList.add(values);
                itemNum++;
                continue;
            }

            for (Map.Entry<String, Float> mGene : geneSymbolWeightMap.entrySet()) {
                String geneSymbol = mGene.getKey();
                int[] indexes = geneGenomeIndexes.get(geneSymbol);
                if (indexes == null) {
                    continue;
                }
                values = new String[colNum];
                if (pathwayGeneCounter == 0) {
                    values[0] = curPath.getID();
                    values[1] = df.format(curPath.getHystPValue());
                    if (curPath.getHystPValue() <= thresholdPathwayP1) {
                        values[2] = "Y";
                    } else {
                        values[2] = "N";
                    }
                    if (Double.isNaN(curPath.getEnrichedPValue())) {
                        values[3] = df.format(curPath.getEnrichedPValue());
                        if (curPath.getEnrichedPValue() <= thresholdPathwayP2) {
                            values[4] = "Y";
                            pathwayCounter++;
                        } else {
                            if (filterNonEriched) {
                                continue;
                            }
                            values[4] = "N";
                            pathwayCounter++;
                        }
                    } else {
                        if (filterNonEriched) {
                            continue;
                        }
                        values[3] = "-";
                        values[4] = "-";
                        pathwayCounter++;
                    }
                    values[5] = String.valueOf(curPath.getTotalGeneNum());

                    values[6] = curPath.getName();
                    values[7] = pathwayLink + curPath.getURL();
                }

                pathwayGeneCounter++;
                chromItemIndexes[indexes[0]].add(itemNum);

                values[8] = geneSymbol;
                p = genePValueMap.get(geneSymbol);
                values[9] = String.valueOf(p.pValue);
                values[11] = CHROM_NAMES[indexes[0]];
                if (toWeightHyst) {
                    Float weight = mGene.getValue();
                    values[15] = (weight == null) ? String.valueOf(1) + "NA" : weight.toString();
                }
                annotatedList.add(values);
                itemNum++;
            }
        }

        //read data and export chromosome by chromosome
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            if (chromItemIndexes[i].isEmpty()) {
                continue;
            }
            Chromosome chrom = genome.readChromosomefromDisk(i);
            if (chrom == null) {
                continue;
            }
            List<Integer> curChromGenes = chromItemIndexes[i];
            for (Integer itemIndex : curChromGenes) {
                values = annotatedList.get(itemIndex);
                int[] indexes = geneGenomeIndexes.get(values[8]);
                // if (indexes==null) continue;
                //System.out.println(values[5]);
                Gene gene = chrom.genes.get(indexes[1]);

                values[10] = String.valueOf(gene.getEntrezID());
                values[12] = String.valueOf(gene.start);
                values[13] = String.valueOf(gene.end - gene.start);
                values[14] = String.valueOf(gene.snps.size());
            }
            chrom = null;
        }
        return pathwayCounter;
    }

    /**
     *
     * @param geneList
     * @param enrichedPathwaysHypergeometric
     * @param databaseName
     * @return
     * @throws Exception
     */
    public final DoubleArrayList hypergeometricPathwayTest(
            final List<PValueGene> geneList,
            final List<Pathway> retrievedPathways, Set<String> seedGenes, int minSize)
            throws Exception {
        int geneNum = geneList.size();
        Set<String> sigGeneSymbs = new HashSet<String>();
        Set<String> allGeneSymbs = new HashSet<String>();

        //Collections.sort(pathwayList, new PathwayHystPValueComparator());
        //hyper geometric method
        // Unified Test Code
        // 0 Benjamini & Hochberg (1995)
        // 1 Standard Bonferroni
        // 2 Adaptive Benjamini (2006)
        // 3 Storey (2002)
        // 4 Hypergeometric Test
        // 5 Fisher Combination Test
        // 6 Simes Combination Test
        // 7 Fixed p-value threshold
        double geneHyperSelectionPValueCutoff = pathwayBasedAssociation.getGeneHyperSignifPValueCutoff();
        double adjustedGeneCutoff = geneHyperSelectionPValueCutoff;
        String geneHyperSelectionMultiTestMethod = pathwayBasedAssociation.getGeneHyperSignifMethod();

        // we need get all p-values to determin the threshlod to select
        // genes
        DoubleArrayList pValues = new DoubleArrayList(geneNum);
        for (int i = 0; i < geneNum; i++) {
            pValues.add(geneList.get(i).pValue);
        }
        pValues.quickSort();

        if (geneHyperSelectionMultiTestMethod.equals("Benjamini & Hochberg (1995)")) {
            // Benjamini & Hochberg, 1995 test
            adjustedGeneCutoff = MultipleTestingMethod.BenjaminiHochbergFDR("Genes in pathways for Hypergeometric test", geneHyperSelectionPValueCutoff, pValues);

        } else if (geneHyperSelectionMultiTestMethod.equals("Standard Bonferroni")) {
            adjustedGeneCutoff = geneHyperSelectionPValueCutoff / pValues.size();

        } else if (geneHyperSelectionMultiTestMethod.equals("Benjamini & Yekutieli (2001)")) {
            // Adaptive Benjamini 2006 test
            adjustedGeneCutoff = MultipleTestingMethod.BenjaminiYekutieliFDR("Genes in pathways for Hypergeometric test", geneHyperSelectionPValueCutoff, pValues);

        } else if (geneHyperSelectionMultiTestMethod.equals("Storey (2002)")) {
            // Storey, 2002 test
            adjustedGeneCutoff = MultipleTestingMethod.storeyFDRTest("Genes in pathways for Hypergeometric test", geneHyperSelectionPValueCutoff, pValues);

        } else if (geneHyperSelectionMultiTestMethod.equals("Fixed p-value threshold")) {

        }

        pValues.clear();

        StringBuilder logInfo = new StringBuilder();
        for (int i = 0; i < geneNum; i++) {
            if (geneList.get(i).pValue <= adjustedGeneCutoff) {
                sigGeneSymbs.add(geneList.get(i).getSymbol());
            } else {
                allGeneSymbs.add(geneList.get(i).getSymbol());
            }
        }

        // logInfo.append("\n--------------------------------------------------------------------------------------------");
        logInfo.append("\nOn the entire genome, ").append(sigGeneSymbs.size()).append(" genes are treated as risk genes according to the gene p value cutoff ")
                .append(adjustedGeneCutoff).append(" by ").append(geneHyperSelectionMultiTestMethod).append(" with overall error rate ").
                append(geneHyperSelectionPValueCutoff).append(" for Hypergeometric distribution test!\n");
        // logOutput.info(logInfo.toString());

        if (GlobalManager.aotcWindow != null) {
            //  SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //  GlobalManager.aotcWindow.insertMessage("INFO " + sdfData.format(new Date()) + logInfo.toString());
        }
        if (sigGeneSymbs.isEmpty()) {
            return pValues;
        }

        allGeneSymbs.addAll(sigGeneSymbs);

        pathExploer.searchEnrichedPathwaysby2Sets(allGeneSymbs, sigGeneSymbs, seedGenes, retrievedPathways, minSize);

        int pathwaySize = retrievedPathways.size();
        for (int i = 0; i < pathwaySize; i++) {
            if (retrievedPathways.get(i).getEnrichedPValue() != 1) {
                pValues.add(retrievedPathways.get(i).getEnrichedPValue());
            }
        }
        return pValues;
    }

    public DoubleArrayList wilcoxonPathwayTest(String sourcePValueName, Map<String, Pathway> dbPathwaySet, Map<String, PValueGene> genePValueMap,
            List<Pathway> pathwayList, int minSize, double minR2) throws Exception {
        Genome genome = pathwayBasedAssociation.getGeneScan().getGenome();
        Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();

        String geneTestMethod = pathwayBasedAssociation.getGeneScan().getTestedMethod();
        int geneScanMethod = 1;
        if (geneTestMethod != null) {
            if (geneTestMethod.equals("HYST")) {
                geneScanMethod = 0;
            }
        }

        Set<Integer>[] positionsSets = new Set[CHROM_NAMES.length];
        List<PValueGene>[] geneSetPG = new List[CHROM_NAMES.length];
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            positionsSets[chromIndex] = new HashSet<Integer>();
            geneSetPG[chromIndex] = new ArrayList<PValueGene>();
        }
        PValueGene gp = null;

        Set<String> uniquePathwayGenes = new HashSet<String>();
        //pickup positions of key SNPs

        for (Map.Entry<String, Pathway> mPath : dbPathwaySet.entrySet()) {
            Pathway curPath = mPath.getValue();

            Set<String> pGenes = curPath.getGeneSymbols();
            uniquePathwayGenes.addAll(pGenes);
            Iterator<String> pGeneIter = pGenes.iterator();
            while (pGeneIter.hasNext()) {
                String pGeneSymb = pGeneIter.next();

                int[] indexes1 = geneGenomeIndexes.get(pGeneSymb);
                if (indexes1 == null) {
                    continue;
                }
                gp = genePValueMap.get(pGeneSymb);
                if (gp == null) {
                    continue;
                }

                for (int i = 0; i < gp.keySNPPositions.size(); i++) {
                    positionsSets[indexes1[0]].add(gp.keySNPPositions.getQuick(i));
                }
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

        CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes = new CorrelationBasedByteLDSparseMatrix[CHROM_NAMES.length];
        info = "Reading LD information of Key SNPs";
        LOG.info(info);
        runningResultTopComp.insertText(info);
        GenotypeLDCalculator ldCaculator = new GenotypeLDCalculator();
        OUTER:
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (positionsSets[chromIndex] == null || positionsSets[chromIndex].isEmpty()) {
                continue;
            }
            switch (ldGenome.getLdSourceCode()) {
                case 0:
                    if (ldGenome.getPlinkSet() == null || !ldGenome.getPlinkSet().avaibleFiles()) {
                        String infor = "No Plink binary file to account for LD for SNPs! ";
                        runningResultTopComp.insertText(infor);
                        LOG.info(infor);
                        break OUTER;
                    }
                    ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyGenotypesPositions(ldGenome.getPlinkSet(), positionsSets[chromIndex], CHROM_NAMES[chromIndex], liftOverLDGenome2pValueFile);
                    break;
                case 1:
                    if (ldGenome.getChromLDFiles()[chromIndex] != null) {
                        File hapMapLDFile = new File(ldGenome.getChromLDFiles()[chromIndex]);
                        if (!hapMapLDFile.exists()) {
                            String infor = "No hapmap LD file on chromosome " + CHROM_NAMES[chromIndex];
                            runningResultTopComp.insertText(infor);
                            LOG.info(infor);
                            continue;
                        }
                    }
                    int[] indexesInLDFile = new int[]{0, 1, 6};
                    ldRsMatrixes[chromIndex] = readHapMapLDRSquareByPositions(ldGenome.getChromLDFiles()[chromIndex], chromIndex, positionsSets[chromIndex], indexesInLDFile, liftOverLDGenome2pValueFile);
                    break;
                case 2:
                    if (ldGenome.getHaploMapFilesList() == null) {
                        continue;
                    }
                    // ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyHaplotypeInGene(ldGenome.getHaploMapFilesList().get(chromIndex), CHROM_NAMES[chromIndex], positionsSets[chromIndex], liftOverLDGenome2pValueFile);
                    break;
                case 4:
                    if (ldGenome.getChromLDFiles()[chromIndex] != null) {
                        File hapMapLDFile = new File(ldGenome.getChromLDFiles()[chromIndex]);
                        if (!hapMapLDFile.exists()) {
                            continue;
                        }
                    }
                    ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyHaplotypeVCFByPositions(ldGenome.getChromLDFiles()[chromIndex], CHROM_NAMES[chromIndex], positionsSets[chromIndex], liftOverLDGenome2pValueFile);
                    break;
                default:
                    break;
            }
        }
        pathwayBasedAssociation.saveBetweenGeneLDToDisk(ldRsMatrixes, sourcePValueName);

        Map<String, Float> availableGWASGeneSet = new HashMap<String, Float>();
        boolean ingoreSNPNoLD = pathwayBasedAssociation.isIgnoreNoLDSNPs();
        float geneWeight = 1;

//note the selectedGWASGenes belongs to the allGWASGenes
        int subPopulationSize = 0;
        Map<String, Double> pValueListInPathway = null;
        DoubleArrayList pValueListOutPathway = new DoubleArrayList();
        DoubleArrayList allPValues = new DoubleArrayList(pathwayList.size());

        runningResultTopComp.insertText("Start to perform LD-attenuating rank-sum test ...");
        long start = System.currentTimeMillis();
        for (Map.Entry<String, Pathway> mPath : dbPathwaySet.entrySet()) {
            Pathway curPath = mPath.getValue();
            String pathID = mPath.getKey();
          
            long start1 = System.currentTimeMillis();
            Set<String> pGenes = curPath.getGeneSymbols();

            pValueListOutPathway.clear();
            availableGWASGeneSet.clear();

            Map<String, Float> geneSymbolWeightMap = curPath.getGeneSymbolWeightMap();
            for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
                geneSetPG[chromIndex].clear();
            }

            for (Map.Entry<String, Float> pGeneIter : geneSymbolWeightMap.entrySet()) {
                String pGeneSymb = pGeneIter.getKey();
                //may introduce biase
                if (!genePValueMap.containsKey(pGeneSymb)) {
                    continue;
                }

                int[] indexes1 = geneGenomeIndexes.get(pGeneSymb);
                if (indexes1 == null) {
                    continue;
                }

                if (toWeightHyst) {
                    geneWeight = pGeneIter.getValue();
                }
                availableGWASGeneSet.put(pGeneSymb, geneWeight);

                gp = genePValueMap.get(pGeneSymb);
                if (gp == null) {
                    continue;
                }
                geneSetPG[indexes1[0]].add(gp);
            }

            start = System.currentTimeMillis();
            pValueListInPathway = correctGeneLDInPathwayByEFS(geneSetPG, ldRsMatrixes, minR2, geneScanMethod);
            long end = System.currentTimeMillis();
            //   runningResultTopComp.insertText(pathID + 1 + " Time " + (end - start));
            /*
             if (pathID.equals("REACTOME_DEFENSINS")) {
             int sss = 0;
             System.out.println(pValueListInPathway.toString());
             }
             */
            Pathway newPathway = new Pathway(pathID, pathID, curPath.getURL());
            newPathway.getGeneSymbolWeightMap().putAll(availableGWASGeneSet);

            for (Map.Entry<String, PValueGene> mGene : genePValueMap.entrySet()) {
                if (!pGenes.contains(mGene.getKey())) {
                    if (!Double.isNaN(mGene.getValue().pValue)) {
                        pValueListOutPathway.add(mGene.getValue().pValue);
                    }
                }
            }

            subPopulationSize = pValueListInPathway.size();

            double[] pValuesInPathway = new double[subPopulationSize];
            int index = 0;
            for (Map.Entry<String, Double> pGeneIter : pValueListInPathway.entrySet()) {
                pValuesInPathway[index] = pGeneIter.getValue();
                index++;
            }
            double[] pValuesOutPathway = new double[pValueListOutPathway.size()];
            for (int t = 0; t < pValuesOutPathway.length; t++) {
                pValuesOutPathway[t] = pValueListOutPathway.getQuick(t);
            }

            if (subPopulationSize > minSize) {
                start = System.currentTimeMillis();
                MannWhitneyTest mt = new MannWhitneyTest(pValuesInPathway, pValuesOutPathway, H1.LESS_THAN);
                // WilcoxonTest wt = new WilcoxonTest(pValuesInPathway, median, H1.LESS_THAN);
                newPathway.setWilcoxonPValue(mt.getSP());
                allPValues.add(newPathway.getWilcoxonPValue());
                pathwayList.add(newPathway);
                end = System.currentTimeMillis();
                //  runningResultTopComp.insertText(pathID + 2 + " Time " + (end - start));
            } else {
                newPathway.setWilcoxonPValue(Double.NaN);
            }

        }

        return allPValues;
    }

    public Map<String, Double> correctGeneLDInPathwayByEFS(List<PValueGene>[] geneSetPG, CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes, double minR2, int geneTestMethodID) throws Exception {
        double p = 0;
        Map<String, Double> independentPVs = new HashMap<String, Double>();
        IntArrayList keySNPIDs = new IntArrayList();
        DoubleArrayList chiSquareStats = new DoubleArrayList();
        DoubleArrayList chiSquareDF = new DoubleArrayList();
        int chrNum = geneSetPG.length;

        double r;
        DoubleArrayList effectP = new DoubleArrayList();
        //List<PValueWeight> effectPW = new ArrayList<PValueWeight>();
        for (int curChrIDIndex = 0; curChrIDIndex < chrNum; curChrIDIndex++) {
            if (geneSetPG[curChrIDIndex].isEmpty()) {
                continue;
            } else if (geneSetPG[curChrIDIndex].size() == 1) {
                p = geneSetPG[curChrIDIndex].get(0).pValue;
                if (!Double.isNaN(p)) {
                    independentPVs.put(geneSetPG[curChrIDIndex].get(0).getSymbol(), p);
                }
                continue;
            }
            keySNPIDs.clear();
            chiSquareStats.clear();
            chiSquareDF.clear();

            Collections.sort(geneSetPG[curChrIDIndex], new PValueGeneComparator());
            int geneNum = geneSetPG[curChrIDIndex].size();
            for (int i = 0; i < geneNum; i++) {
                for (int j = 0; j < geneSetPG[curChrIDIndex].get(i).keySNPPositions.size(); j++) {
                    // System.out.println(geneSetPG[curChrIDIndex].get(s).keySNPPValues.getQuick(j));
                    //ingore SNPs with L information
                    if (geneSetPG[curChrIDIndex].get(i).keySNPPositions.getQuick(j) <= 0) {
                        continue;
                    }
                    keySNPIDs.add(geneSetPG[curChrIDIndex].get(i).keySNPPositions.getQuick(j));
                    chiSquareStats.add(geneSetPG[curChrIDIndex].get(i).keySNPPValues.getQuick(j) / 2);
                    chiSquareDF.add(1);
                }
            }

            int blockSize = keySNPIDs.size();

            MultipleTestingMethod.zScores(chiSquareStats);
            for (int k = 0; k < blockSize; k++) {
                chiSquareStats.setQuick(k, chiSquareStats.getQuick(k) * chiSquareStats.getQuick(k));
            }

            // DenseDoubleMatrix2D rLDMatrix = new DenseDoubleMatrix2D(blockSize, blockSize);
            // rLDMatrix.assign(0);
            for (int k = 0; k < blockSize; k++) {
                // rLDMatrix.setQuick(k, k, 1);
                for (int j = k + 1; j < blockSize; j++) {
                    //Note: this is an updated alogrithm to make the test insenstive the the LD cutoff
                    r = ldRsMatrixes[curChrIDIndex].getLDAt(keySNPIDs.get(k), keySNPIDs.get(j));
                    if (r < minR2) {
                        r = 0;
                    }

                    p = chiSquareStats.getQuick(j) - chiSquareStats.getQuick(k) * r;
                    if (p < 0) {
                        p = 0;
                    }
                    chiSquareStats.setQuick(j, p);

                    p = chiSquareDF.getQuick(j) - chiSquareDF.getQuick(k) * r;
                    if (p < 0) {
                        p = 0.1;
                    }
                    chiSquareDF.setQuick(j, p);
                }
            }
            int accuIndex = 0;
            boolean allSimesTest = false;
            int[] keyBlockIndex = new int[1];
            for (int i = 0; i < geneNum; i++) {
                int keySNPNum = geneSetPG[curChrIDIndex].get(i).keySNPPositions.size();
                //effectPW.clear();
                effectP.clear();
                double allDf = 0;
                double chiSquare = 0;

                for (int t = 0; t < keySNPNum; t++) {
                    if (geneSetPG[curChrIDIndex].get(i).keySNPPositions.getQuick(t) <= 0) {
                        continue;
                    }
                    allDf += chiSquareDF.getQuick(accuIndex + t);
                    chiSquare += chiSquareStats.getQuick(accuIndex + t);
                    if (chiSquareDF.getQuick(accuIndex + t) < 0.1) {
                        continue;
                    }
                    //chiSquareDF.getQuick(accuIndex + t)
                    //p = Probability.chiSquareComplemented(1, chiSquareStats.getQuick(accuIndex + t));
                    p = Gamma.incompleteGammaComplement(chiSquareDF.getQuick(accuIndex + t) / 2, chiSquareStats.getQuick(accuIndex + t) / 2);
                    p *= geneSetPG[curChrIDIndex].get(i).blockWeights.getQuick(t);
                    if (p > 1) {
                        p = 1;
                    }
                    if (!Double.isNaN(p)) {
                        effectP.add(p);
                    }
                    // PValueWeight pw = new PValueWeight();
                    // pw.pValue = p;
                    //  pw.weight = 1;
                    //  pw.physicalPos = accuIndex + t;
                    // effectPW.add(pw);
                }
                if (effectP.isEmpty()) {
                    continue;
                }

                double[] ps = new double[effectP.size()];
                for (int t = 0; t < ps.length; t++) {
                    ps[t] = effectP.getQuick(t);
                }

                if (geneTestMethodID == 1) {
                    p = SetBasedTest.combinePValuebySimeCombinationTest(ps);
                    // p1 = SetBasedTest.combineGATESPValuebyWeightedSimeCombinationTestMyMe(pvs, newCorrMatrix.copy());
                } else {
                    p = SetBasedTest.combinePValuebyFisherCombinationTest(ps);
                }

                /*
                 PValueWeight[] blockKeySNPs = new PValueWeight[effectPW.size()];
                 for (int s = 0; s < blockKeySNPs.length; s++) {
                 blockKeySNPs[s] = effectPW.get(s);
                 }

                 if (allSimesTest) {
                 p = SetBasedTest.combineGATESPValuebyWeightedSimeCombinationTestMyMe(blockKeySNPs, rLDMatrix, keyBlockIndex);
                 } else {
                 p = SetBasedTest.combinePValuebyScaleedFisherCombinationTestCovLogP(blockKeySNPs, rLDMatrix);
                 }
                 */
                accuIndex += keySNPNum;
                if (!Double.isNaN(p)) {
                    independentPVs.put(geneSetPG[curChrIDIndex].get(i).getSymbol(), p);
                }
            }
        }
        return independentPVs;
    }

    public DoubleArrayList wilcoxonTestExistingPathway(Map<String, Double> genePValueMap, List<Pathway> pathwayList, int minSize) throws Exception {
        //note the selectedGWASGenes belongs to the allGWASGenes
        int subPopulationSize = 0;
        DoubleArrayList pValueListInPathway = new DoubleArrayList();
        DoubleArrayList pValueListOutPathway = new DoubleArrayList();
        DoubleArrayList allPValues = new DoubleArrayList();

        Map<String, Float> availableGWASGeneSet = new HashMap<String, Float>();
        float geneWeight = 1;
        for (Pathway curPath : pathwayList) {
            Set<String> pGenes = curPath.getGeneSymbols();

            pValueListInPathway.clear();
            pValueListOutPathway.clear();
            Map<String, Float> geneSymbolWeightMap = curPath.getGeneSymbolWeightMap();

            for (Map.Entry<String, Float> pGeneIter : geneSymbolWeightMap.entrySet()) {
                String pGeneSymb = pGeneIter.getKey();
                //may introduce biase
                if (!genePValueMap.containsKey(pGeneSymb)) {
                    continue;
                }
                if (toWeightHyst) {
                    geneWeight = pGeneIter.getValue();
                }
                availableGWASGeneSet.put(pGeneSymb, geneWeight);
                Double p = genePValueMap.get(pGeneSymb);
                if (p != null) {
                    pValueListInPathway.add(p);
                }
            }

            for (Map.Entry<String, Double> mGene : genePValueMap.entrySet()) {
                if (!pGenes.contains(mGene.getKey())) {
                    pValueListOutPathway.add(mGene.getValue());
                }
            }

            subPopulationSize = pValueListInPathway.size();
            double[] pValuesInPathway = new double[subPopulationSize];
            for (int t = 0; t < subPopulationSize; t++) {
                pValuesInPathway[t] = pValueListInPathway.getQuick(t);
            }
            double[] pValuesOutPathway = new double[pValueListOutPathway.size()];
            for (int t = 0; t < pValuesOutPathway.length; t++) {
                pValuesOutPathway[t] = pValueListOutPathway.getQuick(t);
            }
            if (subPopulationSize > minSize) {
                MannWhitneyTest mt = new MannWhitneyTest(pValuesInPathway, pValuesOutPathway, H1.LESS_THAN);
                // WilcoxonTest wt = new WilcoxonTest(pValuesInPathway, median, H1.LESS_THAN);           
                curPath.setWilcoxonPValue(mt.getSP());
                allPValues.add(curPath.getWilcoxonPValue());
            } else {
                curPath.setWilcoxonPValue(Double.NaN);
            }

        }
        return allPValues;
    }

    public void pathwayScan() {
        //record the classification settings
        ScanPathwayBasedAssocSwingWorker worker = new ScanPathwayBasedAssocSwingWorker();
        // buildTask = buildTask.create(buildingThread); //the task is not started yet
        buildTask = RP.create(worker); //the task is not started yet
        buildTask.schedule(0); //start the task
    }

    class ScanPathwayBasedAssocSwingWorker extends SwingWorker<Void, String> {

        private final int NUM = 100;
        int runningThread = 0;
        boolean succeed = false;
        ProgressHandle ph = null;
        long time;

        public ScanPathwayBasedAssocSwingWorker() {
            ph = ProgressHandleFactory.createHandle("task thats shows progress", new Cancellable() {
                @Override
                public boolean cancel() {
                    return handleCancel();
                }
            });
            runningResultTopComp.setVisible(true);
            runningResultTopComp.newPane();
            time = System.nanoTime();
        }

        @Override
        protected Void doInBackground() {
            try {
                long startTime = System.currentTimeMillis();
                String prjName = GlobalManager.currentProject.getName();
                String prjWorkingPath = GlobalManager.currentProject.getWorkingPath();

                imgFolder = new File(prjWorkingPath + File.separator + prjName + File.separator + "image" + File.separator);
                if (!imgFolder.exists()) {
                    imgFolder.mkdirs();
                }
                String inforString = "Start to do pathway/gene-set scan...";
                runningResultTopComp.insertIcon(imgFolder, "Next.png", inforString);

                StatusDisplayer.getDefault().setStatusText("Scan gene-set-based association on the genome ...");
                Map<String, PValueGene> genePValueMap = new HashMap<String, PValueGene>();
                Map<String, Double> genePMap = new HashMap<String, Double>();
                pathExploer = new PathwayExplorer();
                pathExploer.loadGSEAPathways(pathwayBasedAssociation.getPathwayDBFile(), pathwayBasedAssociation.getMinGeneNumInPathway(), pathwayBasedAssociation.getMaxGeneNumInPathway());
                List<String> sourcePValueNames = pathwayBasedAssociation.getpValueSources();

                Genome genome = pathwayBasedAssociation.getGeneScan().getGenome();
                Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();

                CandidateGeneExtender candiExtender = new CandidateGeneExtender();
                Set<String> canidateGene = null;
                if (pathwayBasedAssociation.getCanidateGeneSetFile() != null) {
                    candiExtender.readSeedGenesFromFile(pathwayBasedAssociation.getCanidateGeneSetFile());
                    canidateGene = candiExtender.getSeedGeneSet();
                }

                for (String sourcePValueName : sourcePValueNames) {
                    ph.start(); //we must start the PH before we swith to determinate
                    ph.switchToIndeterminate();

                    genePValueMap.clear();
                    genePMap.clear();

                    // Calculate PPI associations
                    List<PValueGene> geneList = pathwayBasedAssociation.getGeneScan().loadGenePValuesfromDisk(sourcePValueName, null);
                    Collections.sort(geneList, new GenePValueComparator());
                    int geneNum = geneList.size();

                    DoubleArrayList allGenepValues = new DoubleArrayList();
                    for (int i = 0; i < geneNum; i++) {
                        genePValueMap.put(geneList.get(i).getSymbol(), geneList.get(i));
                        // allGenepValues.add(geneList.get(s).pValue);
                        genePMap.put(geneList.get(i).getSymbol(), geneList.get(i).pValue);
                    }
                    allGenepValues.quickSort();

                    MyTreeNode pathwayNode = new MyTreeNode("Pathways", "Enriched Pathways", "#");

                    // Calculate pathway associations
                    // String keggLink = "http://www.genome.ad.jp/kegg/pathway/map/map";
                    String prefixURLLink = "";
                    //------------------------------------------pathway association test by hyst
                    List<Pathway> pathwayList = new ArrayList<Pathway>();

                    //As hyst does not work for polygenic model, we give up it for pathway-based association analysis
                    //DoubleArrayList pathwayGenepValues = new DoubleArrayList();
                    //DoubleArrayList pathwayPValueHyst = setBasedTestByHYST(genePValueMap, pathwayList, pathExploer.getPathwaySet(), pathwayGenepValues, sourcePValueName);
                    // Collections.sort(pathwayList, new PathwayHystPValueComparator());
                    //pathwayPValueHyst.quickSort();
                    // pathwayGenepValues.quickSort();
                    DoubleArrayList pathwayGenePValues = new DoubleArrayList();

                    DoubleArrayList pathwayPValueWilcoxon = wilcoxonPathwayTest(sourcePValueName, pathExploer.getPathwaySet(), genePValueMap, pathwayList, pathwayBasedAssociation.getMinGeneNumInPathway(), pathwayBasedAssociation.getMinR2());
                    //   DoubleArrayList pathwayPValueWilcoxon = setBasedTestByWMW(genePValueMap, pathwayList, pathExploer.getPathwaySet(), pathwayGenePValues, sourcePValueName);

                    /*
                     DoubleArrayList pathwayChiSqWilcoxon = new DoubleArrayList(pathwayPValueWilcoxon.size());                  
                     int size = pathwayPValueWilcoxon.size();
                     double chiSquare = 0;
                     for (int i = 0; i < size; i++) {
                     chiSquare = MultipleTestingMethod.zScore(pathwayPValueWilcoxon.getQuick(i) / 2);// two tails to be one tail;
                     pathwayChiSqWilcoxon.add(chiSquare * chiSquare);
                     }

                     pathwayChiSqWilcoxon.quickSort();
                     double median = Descriptive.median(pathwayChiSqWilcoxon);
                     pathwayChiSqWilcoxon.clear();
                     StringBuilder tmpBuffer = new StringBuilder();
                     tmpBuffer.append("The inflation factor(λ) ");
                     tmpBuffer.append("of Wilcoxon ranked sign test ");
                     tmpBuffer.append(" is ");
                     median = median / 0.456;
                     tmpBuffer.append(Util.doubleToString(median, 4));
                     tmpBuffer.append("\n");
                     pathwayPValueWilcoxon.clear();
                     median=1;
                     for (Pathway pathway : pathwayList) {
                     chiSquare = pathway.getWilcoxonPValue();
                     chiSquare = MultipleTestingMethod.zScore(chiSquare / 2);// two tails to be one tail;
                     chiSquare = chiSquare * chiSquare;
                     chiSquare = Probability.chiSquareComplemented(1, chiSquare / median);
                     pathway.setWilcoxonPValue(chiSquare);
                     pathwayPValueWilcoxon.add(chiSquare);
                     }                   
                     */
                    pathwayPValueWilcoxon.quickSort();

                    DoubleArrayList pathwayPValueHyst = setBasedTestByHYST(genePValueMap, pathwayList, sourcePValueName);
                    pathwayPValueHyst.quickSort();

                    // for hypergeometricEnrichmentTest we can filer the genes and compute 
                    // p-values directly              
                    DoubleArrayList pathwayPValueHyper = hypergeometricPathwayTest(geneList, pathwayList, canidateGene, pathwayBasedAssociation.getMinGeneNumInPathway());
                    pathwayPValueHyper.quickSort();

                    File imgFile = new File(imgFolder.getCanonicalPath() + pathwayBasedAssociation.getName() + "." + "QQPlotGeneSetP" + ".png");
                    List<String> titles = new ArrayList<String>();
                    List<DoubleArrayList> pvalueList = new ArrayList<DoubleArrayList>();
                    //it seems that the Hypergeometric test is NOT suitable for QQ plot

                    if (pathwayList.size() > 500) {
                        // titles.add("Hypergeometric test");
                        // pvalueList.add(pathwayPValueHyper);
                    }
                    titles.add("Hypergeometric test");
                    pvalueList.add(pathwayPValueHyper);
                    titles.add("Wilcoxon test");
                    pvalueList.add(pathwayPValueWilcoxon);

                    // titles.add("All gene-based test");
                    // pvalueList.add(allGenepValues);
                    PValuePainter painter = new PValuePainter(600, 400);
                    painter.drawMultipleQQPlot(pvalueList, titles, null, imgFile.getCanonicalPath(), 1E-35);
                    runningResultTopComp.insertImage(imgFile);
                    // pathwayGenepValues.clear();

                    pathwayBasedAssociation.savePathwayAssociaion(sourcePValueName, pathwayList);
                    DoubleArrayList[] orgPList = new DoubleArrayList[3];
                    orgPList[0] = pathwayPValueHyst;
                    orgPList[1] = pathwayPValueHyper;
                    orgPList[2] = pathwayPValueWilcoxon;
                    double[] adjustePCutoffs = new double[3];
                    StringBuilder sbInfor = new StringBuilder();
                    significanceTest2(pathwayList, orgPList, genePMap, adjustePCutoffs, sbInfor);
                    List<Pathway> tmpPathwayList = new ArrayList<Pathway>();

                    Map<String, Float> exportedGeneSet = new HashMap<String, Float>();

                    double pathwayPValueExport = pathwayBasedAssociation.getPathwayPValueExport();
                    double genePValueExport = pathwayBasedAssociation.getGenePValueExport();

                    PValueGene gp = null;

                    int pathwayNumHyst = pathwayList.size();
                    // remove pathways with large p-values to export
                    for (int i = pathwayNumHyst - 1; i >= 0; i--) {
                        Pathway pathway = pathwayList.get(i);
                        if (pathway.getEnrichedPValue() <= pathwayPValueExport) {
                            tmpPathwayList.add(pathway);
                        }
                        exportedGeneSet.clear();

                        Map<String, Float> geneSymbolWeightMap = pathway.getGeneSymbolWeightMap();
                        for (Map.Entry<String, Float> mGene : geneSymbolWeightMap.entrySet()) {
                            gp = genePValueMap.get(mGene.getKey());
                            if (gp == null) {
                                continue;
                            }
                            double p = gp.pValue;
                            if (p > genePValueExport) {
                                continue;
                            }
                            exportedGeneSet.put(mGene.getKey(), mGene.getValue());
                        }
                        pathway.getGeneSymbolWeightMap().clear();
                        pathway.getGeneSymbolWeightMap().putAll(exportedGeneSet);
                    }

                    pathwayList.clear();
                    pathwayList.addAll(tmpPathwayList);

                    tmpPathwayList.clear();
                    // ph.progress(85);

                    String info = "";
                    if (pathwayList.isEmpty()) {
                        info = "No enriched pathways in gene-set database with association p-value <=" + pathwayPValueExport;
                        LOG.info(info);
                        runningResultTopComp.insertText(info);
                    } else {
                        String infor = null;
                        infor = generatePathwayStringAnnoation(pathwayList, adjustePCutoffs[0],
                                adjustePCutoffs[0], genePValueMap, geneGenomeIndexes, canidateGene,
                                prefixURLLink, null, pathwayBasedAssociation.isFilterNonSigEnriched());

                        LOG.info(info);
                        runningResultTopComp.insertText(info);
                        /*
                         generatePathwayTreeNodes(pathwayList, enrichedPathwaysHypergeometricMap, canidateGene,
                         prefixURLLink, pathwayTreeRoot, adjustedPathwayMultipleTestingPValueHypergeometric, filterNonEriched);
                         */
                    }

                    //GlobalManager.mainView.displayPathwayTree(pathwayTreeRoot);
                    // InterfaceUtil.saveTreeNode2XMLFile(pathwayTreeRoot, storagePath);
                    System.gc();
                }

                succeed = true;
            } catch (InterruptedException ex) {
                StatusDisplayer.getDefault().setStatusText("Scan PPI-based association task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

            } catch (Exception ex) {
                StatusDisplayer.getDefault().setStatusText("Scan PPI-based association task was CANCELLED!");
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
                    message = ("Geneset-based association scan failed!");
                    LOG.info(message);
                    return;
                }

                GlobalManager.currentProject.addPathwayBasedAssociationScan(pathwayBasedAssociation);
                GlobalManager.pathwayAssocSetModel.addElement(pathwayBasedAssociation);

                ProjectTopComponent projectTopComponent = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                projectTopComponent.showProject(GlobalManager.currentProject);

                message = ("Geneset-based association scan has been finished!");
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
                File ppiAssociationResultFilePath = new File(prjWorkingPath + File.separator + prjName + File.separator + pathwayBasedAssociation.getName() + ".html");
                runningResultTopComp.savePane(ppiAssociationResultFilePath);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
}
