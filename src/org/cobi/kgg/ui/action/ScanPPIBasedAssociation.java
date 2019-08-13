package org.cobi.kgg.ui.action;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.uci.ics.jung.graph.Graph;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.cobi.kgg.business.CandidateGeneExtender;
import org.cobi.kgg.business.GenotypeLDCalculator;
import org.cobi.kgg.business.GenotypeSetUtil;
import org.cobi.kgg.business.PValuePainter;
import org.cobi.kgg.business.SetBasedTest;
import org.cobi.kgg.business.entity.Chromosome;
import org.cobi.kgg.business.entity.Constants;
import static org.cobi.kgg.business.entity.Constants.CHROM_NAMES;
import org.cobi.kgg.business.entity.CorrelationBasedByteLDSparseMatrix;
import org.cobi.kgg.business.entity.FileString;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.GenePValueComparator;
import org.cobi.kgg.business.entity.GeneVertex;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.HaplotypeDataset;
import org.cobi.kgg.business.entity.PPIBasedAssociation;
import org.cobi.kgg.business.entity.PPIEdge;
import org.cobi.kgg.business.entity.PPIGraph;
import org.cobi.kgg.business.entity.PPISet;
import org.cobi.kgg.business.entity.PPISetPValueComparator;
import org.cobi.kgg.business.entity.PValueGene;
import org.cobi.kgg.business.entity.PValueWeight;
import org.cobi.kgg.business.entity.PValueWeightComparatorIndex;
import org.cobi.kgg.business.entity.PlinkDataset;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.StatusGtySet;
import org.cobi.kgg.ui.GlobalManager;
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

/**
 *
 * @author mxli
 */
public class ScanPPIBasedAssociation implements Constants {

    private final static Logger LOG = Logger.getLogger(ScanPPIBasedAssociation.class.getName());
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private RequestProcessor.Task buildTask = null;

    private PPIBasedAssociation ppIBasedAssociation;
    //output setting
    private boolean onlyExportSignificant;
    private boolean onlyVisualizeSignificant;
    short outputFormatType;
    private String outputPath;
    RunningResultViewerTopComponent runningResultTopComp;
    private File imgFolder;

    public boolean isOnlyExportSignificant() {
        return onlyExportSignificant;
    }

    public void setOnlyExportSignificant(boolean onlyExportSignificant) {
        this.onlyExportSignificant = onlyExportSignificant;
    }

    public boolean isOnlyVisualizeSignificant() {
        return onlyVisualizeSignificant;
    }

    public void setOnlyVisualizeSignificant(boolean onlyVisualizeSignificant) {
        this.onlyVisualizeSignificant = onlyVisualizeSignificant;
    }

    public short getOutputFormatType() {
        return outputFormatType;
    }

    public void setOutputFormatType(short outputFormatType) {
        this.outputFormatType = outputFormatType;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public ScanPPIBasedAssociation(PPIBasedAssociation ppIBasedAssociation, RunningResultViewerTopComponent runningResultTopComp) {
        this.ppIBasedAssociation = ppIBasedAssociation;
        this.runningResultTopComp = runningResultTopComp;

    }

    private boolean handleCancel() {
        if (null == buildTask) {
            return false;
        }
        return buildTask.cancel();
    }

    private GeneVertex getGeneVertex(String symbol, Graph<GeneVertex, PPIEdge> significantGraph) {
        GeneVertex vertex = null;
        Collection<GeneVertex> vertexes = significantGraph.getVertices();
        Iterator<GeneVertex> iter = vertexes.iterator();
        while (iter.hasNext()) {
            vertex = iter.next();
            if (vertex.getSymbol().equals(symbol)) {
                return vertex;
            }
        }
        return null;
    }

    public String extractPPISetNetworkSigPairHetFilterForShow(List<PPISet> ppiList, Map<String, PValueGene> genePValueMap,
            double adjustedPpiPValueThreshold, double adjustedGenePValueThreshold, Graph<GeneVertex, PPIEdge> significantGraph) throws Exception {
        Set<String> significantGenes = new HashSet<String>();
        Set<String> nonSignificantGenes = new HashSet<String>();

        int ppiNum = ppiList.size();
        if (ppiNum == 0) {
            String logInfo = "There is no significant gene-pairs!";

            LOG.info(logInfo);
            runningResultTopComp.insertText(logInfo);
            return logInfo;
        }

        Set<String> candiateGeneSymbols = new HashSet<String>();
        if (ppIBasedAssociation.getCanidateGeneSetFile() != null) {
            CandidateGeneExtender candiExtender = new CandidateGeneExtender();
            candiExtender.readSeedGenesFromFile(ppIBasedAssociation.getCanidateGeneSetFile());
            candiateGeneSymbols.addAll(candiExtender.getSeedGeneSet());
        }
        boolean isHigginsI2 = false;
        double heterogeneityTestThreshold = ppIBasedAssociation.getPpIHetroNominalError();
        if (ppIBasedAssociation.getPpIHetroTestedMethod().startsWith("Higgins I2")) {
            isHigginsI2 = true;
        }

        //prepare annotation
        int sigPairNum = 0;
        for (int i = 0; i < ppiNum; i++) {
            PPISet ppi = ppiList.get(i);
            List<String> geneSymbs = ppi.getGeneSymbs();
            //To be revised for Interaction sub-network
            String symbolA = geneSymbs.get(0);
            String symbolB = geneSymbs.get(1);

            PPIEdge edge = new PPIEdge(i, ppi.getAssocicationPValue(), ppi.getConfidenceScore());

            //the ppi detected by the neighboor method has no associcationPValue heterogeneityI2 heterogeneityPValue
            if (!Double.isNaN(ppi.associcationPValue) && ppi.associcationPValue > adjustedPpiPValueThreshold) {
                break;
            }

            if (genePValueMap.get(symbolA).pValue > adjustedGenePValueThreshold || genePValueMap.get(symbolB).pValue > adjustedGenePValueThreshold) {
                //for Higgins I2, the larger the worse
                if (isHigginsI2) {
                    if (!Double.isNaN(ppi.heterogeneityI2) && ppi.heterogeneityI2 > heterogeneityTestThreshold) {
                        continue;
                    }
                    //for Chrons Q
                } else {
                    if (!Double.isNaN(ppi.heterogeneityPValue) && ppi.heterogeneityPValue < heterogeneityTestThreshold) {
                        continue;
                    }
                }
            }

            GeneVertex geneA = getGeneVertex(symbolA, significantGraph);
            if (geneA == null) {
                geneA = new GeneVertex(symbolA);
                geneA.setAnnotation("Gene p-value: " + Util.formatPValue(genePValueMap.get(symbolA).pValue));
                if (genePValueMap.get(symbolA).pValue <= adjustedGenePValueThreshold) {
                    geneA.setIsSignificant(true);
                    significantGenes.add(symbolA);
                } else {
                    nonSignificantGenes.add(symbolA);
                }
                if (candiateGeneSymbols.contains(symbolA)) {
                    geneA.setIsSeed(true);
                }
                significantGraph.addVertex(geneA);
            }

            GeneVertex geneB = getGeneVertex(symbolB, significantGraph);
            if (geneB == null) {
                geneB = new GeneVertex(symbolB);
                geneB.setAnnotation("Gene p-value: " + Util.formatPValue(genePValueMap.get(symbolB).pValue));
                if (genePValueMap.get(symbolB).pValue <= adjustedGenePValueThreshold) {
                    geneB.setIsSignificant(true);
                    significantGenes.add(symbolB);
                } else {
                    nonSignificantGenes.add(symbolB);
                }
                if (candiateGeneSymbols.contains(symbolB)) {
                    geneB.setIsSeed(true);
                }
                significantGraph.addVertex(geneB);
            }
            sigPairNum++;
            significantGraph.addEdge(edge, geneA, geneB);
        }
        String info = "There are " + sigPairNum + " significant gene pairs!";
        if (significantGenes.size() > 0) {
            info += "\nThese " + significantGenes.size() + " genes are significant according to their own gene-wise p-values:\n" + significantGenes.toString();
            LOG.info(info);
            if (runningResultTopComp != null) {
                runningResultTopComp.insertText(info);
            }

        }

        if (nonSignificantGenes.size() > 0) {
            info += "\nThese " + nonSignificantGenes.size() + " genes are NOT significant according to their OWN gene-wise p-values:\n" + nonSignificantGenes.toString();
            LOG.info(info);
            if (runningResultTopComp != null) {
                runningResultTopComp.insertText(info);
            }

        }
        return info;
    }

    public double[] pPISetSigTestAndQQPlot(List<PPISet> ppiList, Map<String, PValueGene> genePValueMap,
            String ppiSetName, boolean hetFilter, boolean bothGeneSigFilter) throws Exception {
        DoubleArrayList ppiValueArrayLD = new DoubleArrayList();
        DoubleArrayList geneValueArray = new DoubleArrayList();
        DoubleArrayList ppiValueArrayNoWeight = new DoubleArrayList();
        for (Map.Entry<String, PValueGene> m : genePValueMap.entrySet()) {
            if (Double.isNaN(m.getValue().pValue)) {
                continue;
            }
            geneValueArray.add(m.getValue().pValue);
        }
        int ppiNum = ppiList.size();
        for (int i = 0; i < ppiNum; i++) {
            PPISet ppi = ppiList.get(i);
            ppiValueArrayLD.add(ppi.associcationPValue);
        }
        geneValueArray.quickSort();
        ppiValueArrayLD.quickSort();
        //cutoffs of genes and ppis
        double[] pvalueCutoffs = significanceCheckInPPIPairs(ppiValueArrayLD, geneValueArray);
        ppiValueArrayLD.clear();

        boolean allSignificant = true;

        boolean isHigginsI2 = false;
        double heterogeneityTestThreshold = ppIBasedAssociation.getPpIHetroNominalError();
        if (ppIBasedAssociation.getPpIHetroTestedMethod().startsWith("Higgins I2")) {
            isHigginsI2 = true;
        }

        for (int i = 0; i < ppiNum; i++) {
            PPISet ppi = ppiList.get(i);

            allSignificant = true;
            if (bothGeneSigFilter) {
                List<String> geneList = ppi.getGeneSymbs();
                for (String g1 : geneList) {
                    if (genePValueMap.get(g1).pValue > pvalueCutoffs[0]) {
                        allSignificant = false;
                        break;
                    }
                }
            } else {
                //to force heterogeity test 
                allSignificant = false;
            }

            //other wise it is not necessary for heterogeity test 
            if (!allSignificant) {
                if (hetFilter) {
                    //for Higgins I2, the larger the worse
                    if (isHigginsI2) {
                        if (ppi.heterogeneityI2 > heterogeneityTestThreshold) {
                            continue;
                        }

                        //for Chrons Q
                    } else {
                        if (ppi.heterogeneityPValue < heterogeneityTestThreshold) {
                            continue;
                        }
                    }
                }
            }
            ppiValueArrayLD.add(ppi.associcationPValue);
            // ppiValueArrayNoWeight.add(ppi.getConfidenceScore());
        }

        File imgFile = new File(imgFolder.getCanonicalPath() + File.separator + ppIBasedAssociation.getName() + "." + ppiSetName + ".QQPlotGeneP" + ".png");
        List<String> titles = new ArrayList<String>();
        List<DoubleArrayList> pvalueList = new ArrayList<DoubleArrayList>();
        ppiValueArrayLD.quickSort();
        //titles.add("Interaction p-values (Weight)");
        titles.add("Gene-pair p-values");
        pvalueList.add(ppiValueArrayLD);

        geneValueArray.quickSort();
        titles.add("Gene p-values");
        pvalueList.add(geneValueArray);

        PValuePainter painter = new PValuePainter(600, 400);
        painter.drawMultipleQQPlot(pvalueList, titles, null, imgFile.getCanonicalPath(), 1E-35);
        runningResultTopComp.insertImage(imgFile);

        geneValueArray.clear();
        ppiValueArrayLD.clear();
        return pvalueCutoffs;
    }

    public double[] pPISetSigTest(List<PPISet> ppiList, Map<String, PValueGene> genePValueMap, boolean hetFilter, boolean bothGeneSigFilter, StringBuilder textInfor) throws Exception {
        DoubleArrayList ppiValueArrayLD = new DoubleArrayList();
        DoubleArrayList geneValueArray = new DoubleArrayList();
        DoubleArrayList ppiValueArrayNoWeight = new DoubleArrayList();
        for (Map.Entry<String, PValueGene> m : genePValueMap.entrySet()) {
            geneValueArray.add(m.getValue().pValue);
        }
        int ppiNum = ppiList.size();
        for (int i = 0; i < ppiNum; i++) {
            PPISet ppi = ppiList.get(i);
            ppiValueArrayLD.add(ppi.associcationPValue);
        }
        geneValueArray.quickSort();
        ppiValueArrayLD.quickSort();
        //cutoffs of genes and ppis
        StringBuilder logInfo1 = new StringBuilder();
        StringBuilder logInfo2 = new StringBuilder();
        double[] pvalueCutoffs = significanceCheckInPPIPairs(ppiValueArrayLD, geneValueArray, logInfo1, logInfo2);
        textInfor.append(logInfo1);
        textInfor.append(logInfo2);
        ppiValueArrayLD.clear();

        boolean allSignificant = true;

        boolean isHigginsI2 = false;
        double heterogeneityTestThreshold = ppIBasedAssociation.getPpIHetroNominalError();
        if (ppIBasedAssociation.getPpIHetroTestedMethod().startsWith("Higgins I2")) {
            isHigginsI2 = true;
        }

        for (int i = 0; i < ppiNum; i++) {
            PPISet ppi = ppiList.get(i);

            allSignificant = true;
            if (bothGeneSigFilter) {
                List<String> geneList = ppi.getGeneSymbs();
                for (String g1 : geneList) {
                    if (genePValueMap.get(g1).pValue > pvalueCutoffs[0]) {
                        allSignificant = false;
                        break;
                    }
                }
            } else {
                //to force heterogeity test 
                allSignificant = false;
            }

            //other wise it is not necessary for heterogeity test 
            if (!allSignificant) {
                if (hetFilter) {
                    //for Higgins I2, the larger the worse
                    if (isHigginsI2) {
                        if (ppi.heterogeneityI2 > heterogeneityTestThreshold) {
                            continue;
                        }

                        //for Chrons Q
                    } else {
                        if (ppi.heterogeneityPValue < heterogeneityTestThreshold) {
                            continue;
                        }
                    }
                }
            }
            ppiValueArrayLD.add(ppi.associcationPValue);
            // ppiValueArrayNoWeight.add(ppi.getConfidenceScore());
        }

        return pvalueCutoffs;
    }

    private double[] significanceCheckInPPIPairs(DoubleArrayList ppiValueArray, DoubleArrayList geneValueArray, StringBuilder logInfo1, StringBuilder logInfo2) throws Exception {
        // Unified Test Code
        // 0 Benjamini & Hochberg (1995)
        // 1 Standard Bonferroni
        // 2 Benjamini & Yekutieli (2001)
        // 3 Storey (2002)
        // 4 Hypergeometric Test
        // 5 Fisher Combination Test
        // 6 Simes Combination Test
        // 7 Fixed p-value threshold
        int geneNum = geneValueArray.size();
        int ppiNum = ppiValueArray.size();

        double adjustedGenePValue = ppIBasedAssociation.getPpIAssocNominalError();
        double adjustedPPIPValue = ppIBasedAssociation.getPpIAssocNominalError();
        if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Fixed p-value threshold")) {
        } else {
            if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Benjamini & Hochberg (1995)")) {
                // Benjamini & Hochberg, 1995 test
                adjustedGenePValue = MultipleTestingMethod.BenjaminiHochbergFDR("Gene-pair", adjustedGenePValue, geneValueArray);
                logInfo1.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate ");

                // Benjamini & Hochberg, 1995 test
                adjustedPPIPValue = MultipleTestingMethod.BenjaminiHochbergFDR("Gene-pair", adjustedPPIPValue, ppiValueArray);
                logInfo2.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate ");

            } else if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Standard Bonferroni")) {
                adjustedGenePValue = adjustedGenePValue / geneNum;
                logInfo1.append("The significance level for Bonferroni correction  to control familywise error rate ");
                logInfo1.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo1.append(" on the on whole genome is ");
                logInfo1.append(Util.formatPValue(adjustedGenePValue));
                logInfo1.append("=(");
                logInfo1.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo1.append("/");
                logInfo1.append(geneNum);
                logInfo1.append(") for ppi genes.\n");

                adjustedPPIPValue = adjustedPPIPValue / ppiNum;
                logInfo2.append("The significance level for Bonferroni correction  to control familywise error rate ");
                logInfo2.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo2.append(" on the on whole genome is ");
                logInfo2.append(Util.formatPValue(adjustedPPIPValue));
                logInfo2.append("=(");
                logInfo2.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo2.append("/");
                logInfo2.append(ppiNum);
                logInfo2.append(") for all gene pairs.\n");

            } else if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Benjamini & Yekutieli (2001)")) {
                // Adaptive Benjamini 2006 test
                adjustedGenePValue = MultipleTestingMethod.BenjaminiYekutieliFDR("Gene-pair", adjustedGenePValue, geneValueArray);
                logInfo1.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");

                // Adaptive Benjamini 2006 test
                adjustedPPIPValue = MultipleTestingMethod.BenjaminiYekutieliFDR("Gene-pair", adjustedPPIPValue, ppiValueArray);
                logInfo2.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");

            } else if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Storey (2002)")) {
                // Storey, 2002 test
                adjustedGenePValue = MultipleTestingMethod.storeyFDRTest("Gene-pair", adjustedGenePValue, geneValueArray);
                logInfo1.append("The significance level for Storey (2002) FDR test to control error rate ");

                // Storey, 2002 test
                adjustedPPIPValue = MultipleTestingMethod.storeyFDRTest("Gene-pair", adjustedPPIPValue, ppiValueArray);
                logInfo1.append("The significance level for Storey (2002) FDR test to control error rate ");
            }
            // geneValueArray.clear();
            if (!ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Standard Bonferroni")) {
                logInfo1.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo1.append(" on the on whole genome is ");
                logInfo1.append(Util.formatPValue(adjustedGenePValue));
                logInfo1.append(" for ").append(geneNum).append(" ppi genes.\n");

                logInfo2.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo2.append(" on the on whole genome is ");
                logInfo2.append(Util.formatPValue(adjustedPPIPValue));
                logInfo2.append(" for all ").append(ppiNum).append(" all gene pairs.\n");
            }

        }
        /*
         if (runningResultTopComp != null) {
         runningResultTopComp.insertText(logInfo1.toString());
         runningResultTopComp.insertText(logInfo2.toString());
         }
         LOG.info(logInfo1.toString());
         LOG.info(logInfo2.toString());
         */

        return new double[]{
            adjustedGenePValue, adjustedPPIPValue
        };
    }

    private double[] significanceCheckInPPIPairs(DoubleArrayList ppiValueArray, DoubleArrayList geneValueArray) throws Exception {
        // Unified Test Code
        // 0 Benjamini & Hochberg (1995)
        // 1 Standard Bonferroni
        // 2 Adaptive Benjamini (2006)
        // 3 Storey (2002)
        // 4 Hypergeometric Test
        // 5 Fisher Combination Test
        // 6 Simes Combination Test
        // 7 Fixed p-value threshold
        int geneNum = geneValueArray.size();
        int ppiNum = ppiValueArray.size();
        StringBuilder logInfo1 = new StringBuilder();
        StringBuilder logInfo2 = new StringBuilder();
        double adjustedGenePValue = ppIBasedAssociation.getPpIAssocNominalError();
        double adjustedPPIPValue = ppIBasedAssociation.getPpIAssocNominalError();
        if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Fixed p-value threshold")) {
        } else {
            if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Benjamini & Hochberg (1995)")) {
                // Benjamini & Hochberg, 1995 test
                adjustedGenePValue = MultipleTestingMethod.BenjaminiHochbergFDR("Gene-pair", adjustedGenePValue, geneValueArray);
                logInfo1.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate ");

                // Benjamini & Hochberg, 1995 test
                adjustedPPIPValue = MultipleTestingMethod.BenjaminiHochbergFDR("Gene-pair", adjustedPPIPValue, ppiValueArray);
                logInfo2.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate ");

            } else if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Standard Bonferroni")) {
                adjustedGenePValue = adjustedGenePValue / geneNum;
                logInfo1.append("The significance level for Bonferroni correction  to control familywise error rate ");
                logInfo1.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo1.append(" on the on whole genome is ");
                logInfo1.append(Util.formatPValue(adjustedGenePValue));
                logInfo1.append("=(");
                logInfo1.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo1.append("/");
                logInfo1.append(geneNum);
                logInfo1.append(") for ppi genes.\n");

                adjustedPPIPValue = adjustedPPIPValue / ppiNum;
                logInfo2.append("The significance level for Bonferroni correction  to control familywise error rate ");
                logInfo2.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo2.append(" on the on whole genome is ");
                logInfo2.append(Util.formatPValue(adjustedPPIPValue));
                logInfo2.append("=(");
                logInfo2.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo2.append("/");
                logInfo2.append(ppiNum);
                logInfo2.append(") for all gene pairs.\n");

            } else if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Benjamini & Yekutieli (2001)")) {
                // Adaptive Benjamini 2006 test
                adjustedGenePValue = MultipleTestingMethod.BenjaminiYekutieliFDR("Gene-pair", adjustedGenePValue, geneValueArray);
                logInfo1.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");

                // Adaptive Benjamini 2006 test
                adjustedPPIPValue = MultipleTestingMethod.BenjaminiYekutieliFDR("Gene-pair", adjustedPPIPValue, ppiValueArray);
                logInfo2.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");

            } else if (ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Storey (2002)")) {
                // Storey, 2002 test
                adjustedGenePValue = MultipleTestingMethod.storeyFDRTest("Gene-pair", adjustedGenePValue, geneValueArray);
                logInfo1.append("The significance level for Storey (2002) FDR test to control error rate ");

                // Storey, 2002 test
                adjustedPPIPValue = MultipleTestingMethod.storeyFDRTest("Gene-pair", adjustedPPIPValue, ppiValueArray);
                logInfo1.append("The significance level for Storey (2002) FDR test to control error rate ");
            }
            // geneValueArray.clear();
            if (!ppIBasedAssociation.getPpIAssocMultiTestedMethod().equals("Standard Bonferroni")) {
                logInfo1.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo1.append(" on the on whole genome is ");
                logInfo1.append(Util.formatPValue(adjustedGenePValue));
                logInfo1.append(" for ").append(geneNum).append(" ppi genes.\n");

                logInfo2.append(ppIBasedAssociation.getPpIAssocNominalError());
                logInfo2.append(" on the on whole genome is ");
                logInfo2.append(Util.formatPValue(adjustedGenePValue));
                logInfo2.append(" for all ").append(ppiNum).append(" all gene pairs.\n");
            }

        }

        if (runningResultTopComp != null) {
            runningResultTopComp.insertText(logInfo1.toString());
            runningResultTopComp.insertText(logInfo2.toString());
        }
        LOG.info(logInfo1.toString());
        LOG.info(logInfo2.toString());

        return new double[]{
            adjustedGenePValue, adjustedPPIPValue
        };
    }

    public String exportPPISetListNoSNP(List<PPISet> ppiList, Graph<String, Integer> graph, Map<String, PValueGene> genePValueMap, String outPath,
            double[] pvalueCutts, boolean sigFilter, boolean hetFilter, boolean bothGeneSigFilter) throws Exception {
        StringBuilder logInfo = new StringBuilder();
        List<String[]> annotatedList = new ArrayList<String[]>();

        List<String> columnNameList = new ArrayList<String>();
        columnNameList.add("Gene-pair_ID");
        columnNameList.add("Gene-pair_P_Value");
        columnNameList.add("Gene-pair_Cochran_Q_P");
        columnNameList.add("Gene-pair_Higgins_I2");
        columnNameList.add("Gene-pair_Confidence");

        String[] values = null;
        int validGeneNum = 0;
        int ppiNum = ppiList.size();
        int subGraphSize = 0;
        if (ppiNum > 0) {
            subGraphSize = ppiList.get(0).getGeneSymbs().size();
            for (int i = 0; i < subGraphSize; i++) {
                columnNameList.add("Gene" + (i + 1) + "_Symbol");
                columnNameList.add("Gene" + (i + 1) + "_P_Value");
                columnNameList.add("Gene" + (i + 1) + "_Chromosome");
                columnNameList.add("Gene" + (i + 1) + "_Start_Position");
                columnNameList.add("Gene" + (i + 1) + "_End_Position");
            }
        }
        String[] columnNames = new String[columnNameList.size()];
        columnNameList.toArray(columnNames);
        annotatedList.add(columnNames);
        int colNum = columnNames.length;
        int exPpiNum = 0;

        Set<String> avaiblePairs = new HashSet<String>();
        StringBuilder avaibleGenePairInString = new StringBuilder();
        Genome genome = ppIBasedAssociation.getGeneScan().getGenome();

        Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();
        //map gene1s on chromsomes so that each time only load one chromosome into memory
        List<int[]>[] chromGenes = new List[CHROM_NAMES.length];
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            chromGenes[i] = new ArrayList<int[]>();
        }

        DoubleArrayList geneValueArray = new DoubleArrayList();

        for (Map.Entry<String, PValueGene> m : genePValueMap.entrySet()) {
            geneValueArray.add(m.getValue().pValue);
        }
        geneValueArray.quickSort();
        boolean isHigginsI2 = false;
        double heterogeneityTestThreshold = ppIBasedAssociation.getPpIHetroNominalError();
        if (ppIBasedAssociation.getPpIHetroTestedMethod().startsWith("Higgins I2")) {
            isHigginsI2 = true;
        }

        if (onlyExportSignificant) {
            boolean allSignificant = true;
            for (int i = 0; i < ppiNum; i++) {
                PPISet ppi = ppiList.get(i);
                if (sigFilter) {
                    if (ppi.associcationPValue > pvalueCutts[1]) {
                        break;
                    }
                }
                allSignificant = true;
                if (bothGeneSigFilter) {
                    List<String> geneList = ppi.getGeneSymbs();
                    for (String g1 : geneList) {
                        if (genePValueMap.get(g1).pValue > pvalueCutts[0]) {
                            allSignificant = false;
                            break;
                        }
                    }
                } else {
                    //to force heterogeity test 
                    allSignificant = false;
                }

                //other wise it is not necessary for heterogeity test 
                if (!allSignificant) {
                    if (hetFilter) {
                        //for Higgins I2, the larger the worse
                        if (isHigginsI2) {
                            if (ppi.heterogeneityI2 > heterogeneityTestThreshold) {
                                continue;
                            }

                            //for Chrons Q
                        } else {
                            if (ppi.heterogeneityPValue < heterogeneityTestThreshold) {
                                continue;
                            }
                        }
                    }
                }

                List<String> genes = ppi.getGeneSymbs();
                int geneSize = genes.size();
                if (geneSize == 0) {
                    continue;
                }
                List<PValueGene> pValueGenes = new ArrayList<PValueGene>();
                //sort gene1s according to gp1-values
                avaibleGenePairInString.delete(0, avaibleGenePairInString.length());
                for (String gene : genes) {
                    PValueGene pg = new PValueGene(gene);
                    pg.setpValue(genePValueMap.get(gene).pValue);
                    pValueGenes.add(pg);
                    avaibleGenePairInString.append(gene);
                }
                if (avaiblePairs.contains(avaibleGenePairInString.toString())) {
                    continue;
                } else {
                    avaiblePairs.add(avaibleGenePairInString.toString());
                }

                avaibleGenePairInString.delete(0, avaibleGenePairInString.length());
                for (int k = geneSize - 1; k >= 0; k--) {
                    avaibleGenePairInString.append(genes.get(k));
                }
                if (avaiblePairs.contains(avaibleGenePairInString.toString())) {
                    continue;
                } else {
                    avaiblePairs.add(avaibleGenePairInString.toString());
                }

                Collections.sort(pValueGenes, new GenePValueComparator());
                values = new String[colNum];
                exPpiNum++;

                //a strategy to save memory
                values[0] = String.valueOf(exPpiNum);
                values[1] = String.valueOf(ppi.associcationPValue);
                values[2] = String.valueOf(ppi.heterogeneityPValue);
                values[3] = String.valueOf(ppi.heterogeneityI2);
                values[4] = String.valueOf(ppi.getConfidenceScore());

                for (int k = 0; k < geneSize; k++) {
                    String geneSymbol = pValueGenes.get(k).getSymbol();
                    int[] indexes = geneGenomeIndexes.get(geneSymbol);
                    // System.out.println(geneSymbol);
                    chromGenes[indexes[0]].add(new int[]{exPpiNum, indexes[1], 5 + 5 * k + 3});
                    values[5 + 5 * k] = geneSymbol;
                    values[5 + 5 * k + 1] = String.valueOf(pValueGenes.get(k).pValue);
                    values[5 + 5 * k + 2] = CHROM_NAMES[indexes[0]];
                }

                annotatedList.add(values);
                validGeneNum++;
            }
        } else {
            for (int i = 0; i < ppiNum; i++) {
                PPISet ppi = ppiList.get(i);
                values = new String[colNum];

                //a strategy to save memory
                values[0] = String.valueOf(i + 1);
                values[1] = String.valueOf(ppi.associcationPValue);
                values[2] = String.valueOf(ppi.heterogeneityPValue);
                values[3] = String.valueOf(ppi.heterogeneityI2);
                values[4] = String.valueOf(ppi.getConfidenceScore());
                List<String> genes = ppi.getGeneSymbs();
                avaibleGenePairInString.delete(0, avaibleGenePairInString.length());

                for (int k = 0; k < genes.size(); k++) {
                    int[] indexes = geneGenomeIndexes.get(genes.get(k));
                    // System.out.println(geneSymbol);
                    //has the head line
                    chromGenes[indexes[0]].add(new int[]{i + 1, indexes[1], 5 + 5 * k + 3});
                    values[5 + 5 * k] = genes.get(k);
                    values[5 + 5 * k + 1] = String.valueOf(genePValueMap.get(genes.get(k)).pValue);
                    values[5 + 5 * k + 2] = CHROM_NAMES[indexes[0]];
                    avaibleGenePairInString.append(genes.get(k));
                }
                avaibleGenePairInString.delete(0, avaibleGenePairInString.length());
                for (int k = genes.size() - 1; k >= 0; k--) {
                    avaibleGenePairInString.append(genes.get(k));
                }
                if (avaiblePairs.contains(avaibleGenePairInString.toString())) {
                    continue;
                } else {
                    avaiblePairs.add(avaibleGenePairInString.toString());
                }
                annotatedList.add(values);
                validGeneNum++;
            }
        }
        avaiblePairs.clear();

        //read data and export chromosome by chromosome
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            if (chromGenes[i].isEmpty()) {
                continue;
            }
            Chromosome chrom = genome.readChromosomefromDisk(i);
            List<Gene> geneList = chrom.genes;
            List<int[]> curChromGenes = chromGenes[i];
            for (int[] gensmbIndex : curChromGenes) {
                values = annotatedList.get(gensmbIndex[0]);
                Gene gene = geneList.get(gensmbIndex[1]);
                values[gensmbIndex[2]] = String.valueOf(gene.getStart());
                values[gensmbIndex[2] + 1] = String.valueOf(gene.getEnd());
            }
            chromGenes[i].clear();
        }

        if (outputFormatType == 0) {
            File saveFile = new File(outPath);
            LocalExcelFile.WriteArray2XLSXFile(saveFile.getCanonicalPath(), annotatedList, true);
            logInfo.append(validGeneNum);
            logInfo.append(" significant gene pairs (Family-wise error rate<=").append(ppIBasedAssociation.getPpIAssocNominalError()).append(") after heterogeneity test are saved in ");
            logInfo.append(saveFile.getCanonicalPath());
            logInfo.append('\n');

        } else if (outputFormatType == 1) {

            File saveFile = new File(outPath);
            LocalExcelFile.writeArray2ExcelFile(saveFile.getCanonicalPath(), annotatedList, true);
            logInfo.append(validGeneNum);
            logInfo.append(" significant gene pairs (Family-wise error rate<=").append(ppIBasedAssociation.getPpIAssocNominalError()).append(") after heterogeneity test are saved in ");
            logInfo.append(saveFile.getCanonicalPath());
            logInfo.append('\n');
        } else {
            File saveFile = new File(outPath);
            LocalFile.writeData(saveFile.getCanonicalPath(), annotatedList, "\t", false);
            logInfo.append(validGeneNum);
            logInfo.append(" significant gene pairs (Family-wise error rate<=").append(ppIBasedAssociation.getPpIAssocNominalError()).append(") after heterogeneity test are saved in ");
            logInfo.append(saveFile.getCanonicalPath());
            logInfo.append('\n');
        }
        annotatedList = null;

        LOG.info(logInfo.toString());
        runningResultTopComp.insertText(logInfo.toString());
        return logInfo.toString();
    }

    public String exportPPISetListNoSNP(List<PPISet> ppiList, Map<String, PValueGene> genePValueMap, String outPath,
            double[] pvalueCutts, boolean sigFilter, boolean hetFilter, boolean bothGeneSigFilter) throws Exception {
        StringBuilder logInfo = new StringBuilder();
        List<String[]> annotatedList = new ArrayList<String[]>();

        List<String> columnNameList = new ArrayList<String>();
        columnNameList.add("Gene-pair_ID");
        columnNameList.add("Gene-pair_P_Value_HYST");
        columnNameList.add("Gene-pair_Cochran_Q_P");
        columnNameList.add("Gene-pair_Higgins_I2");
        columnNameList.add("Gene-pair_Confidence");

        String[] values = null;
        int validGeneNum = 0;
        int ppiNum = ppiList.size();
        int subGraphSize = 0;
        if (ppiNum > 0) {
            subGraphSize = ppiList.get(0).getGeneSymbs().size();
            for (int i = 0; i < subGraphSize; i++) {
                columnNameList.add("Gene" + (i + 1) + "_Symbol");
                columnNameList.add("Gene" + (i + 1) + "_P_Value");
                columnNameList.add("Gene" + (i + 1) + "_Chromosome");
                columnNameList.add("Gene" + (i + 1) + "_Start_Position");
                columnNameList.add("Gene" + (i + 1) + "_End_Position");
            }
        }
        String[] columnNames = new String[columnNameList.size()];
        columnNameList.toArray(columnNames);
        annotatedList.add(columnNames);
        int colNum = columnNames.length;
        int exPpiNum = 0;

        Set<String> avaiblePairs = new HashSet<String>();
        StringBuilder avaibleGenePairInString = new StringBuilder();
        Genome genome = ppIBasedAssociation.getGeneScan().getGenome();

        Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();
        //map gene1s on chromsomes so that each time only load one chromosome into memory
        List<int[]>[] chromGenes = new List[CHROM_NAMES.length];
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            chromGenes[i] = new ArrayList<int[]>();
        }

        DoubleArrayList geneValueArray = new DoubleArrayList();

        for (Map.Entry<String, PValueGene> m : genePValueMap.entrySet()) {
            geneValueArray.add(m.getValue().pValue);
        }
        geneValueArray.quickSort();
        boolean isHigginsI2 = false;
        double heterogeneityTestThreshold = ppIBasedAssociation.getPpIHetroNominalError();
        if (ppIBasedAssociation.getPpIHetroTestedMethod().startsWith("Higgins I2")) {
            isHigginsI2 = true;
        }

        if (onlyExportSignificant) {
            boolean allSignificant = true;
            for (int i = 0; i < ppiNum; i++) {
                PPISet ppi = ppiList.get(i);
                if (sigFilter) {
                    if (ppi.associcationPValue > pvalueCutts[1]) {
                        break;
                    }
                }
                allSignificant = true;
                if (bothGeneSigFilter) {
                    List<String> geneList = ppi.getGeneSymbs();
                    for (String g1 : geneList) {
                        if (genePValueMap.get(g1).pValue > pvalueCutts[0]) {
                            allSignificant = false;
                            break;
                        }
                    }
                } else {
                    //to force heterogeity test 
                    allSignificant = false;
                }

                //other wise it is not necessary for heterogeity test 
                if (!allSignificant) {
                    if (hetFilter) {
                        //for Higgins I2, the larger the worse
                        if (isHigginsI2) {
                            if (ppi.heterogeneityI2 > heterogeneityTestThreshold) {
                                continue;
                            }

                            //for Chrons Q
                        } else {
                            if (ppi.heterogeneityPValue < heterogeneityTestThreshold) {
                                continue;
                            }
                        }
                    }
                }

                List<String> genes = ppi.getGeneSymbs();
                int geneSize = genes.size();
                if (geneSize == 0) {
                    continue;
                }
                List<PValueGene> pValueGenes = new ArrayList<PValueGene>();
                //sort gene1s according to gp1-values
                avaibleGenePairInString.delete(0, avaibleGenePairInString.length());
                for (String gene : genes) {
                    PValueGene pg = new PValueGene(gene);
                    pg.setpValue(genePValueMap.get(gene).pValue);
                    pValueGenes.add(pg);
                    avaibleGenePairInString.append(gene);
                }
                if (avaiblePairs.contains(avaibleGenePairInString.toString())) {
                    continue;
                } else {
                    avaiblePairs.add(avaibleGenePairInString.toString());
                }

                avaibleGenePairInString.delete(0, avaibleGenePairInString.length());
                for (int k = geneSize - 1; k >= 0; k--) {
                    avaibleGenePairInString.append(genes.get(k));
                }
                if (avaiblePairs.contains(avaibleGenePairInString.toString())) {
                    continue;
                } else {
                    avaiblePairs.add(avaibleGenePairInString.toString());
                }

                Collections.sort(pValueGenes, new GenePValueComparator());
                values = new String[colNum];
                exPpiNum++;

                //a strategy to save memory                
                values[0] = String.valueOf(ppi.associcationPValue);
                values[1] = String.valueOf(ppi.heterogeneityPValue);
                values[2] = String.valueOf(ppi.heterogeneityI2);
                values[3] = String.valueOf(ppi.getConfidenceScore());

                for (int k = 0; k < geneSize; k++) {
                    String geneSymbol = pValueGenes.get(k).getSymbol();
                    int[] indexes = geneGenomeIndexes.get(geneSymbol);
                    // System.out.println(geneSymbol);
                    chromGenes[indexes[0]].add(new int[]{exPpiNum, indexes[1], 4 + 5 * k + 3});
                    values[4 + 5 * k] = geneSymbol;
                    values[4 + 5 * k + 1] = String.valueOf(pValueGenes.get(k).pValue);
                    values[4 + 5 * k + 2] = CHROM_NAMES[indexes[0]];
                }

                annotatedList.add(values);
                validGeneNum++;
            }
        } else {
            for (int i = 0; i < ppiNum; i++) {
                PPISet ppi = ppiList.get(i);
                values = new String[colNum];

                //a strategy to save memory           
                values[0] = String.valueOf(ppi.associcationPValue);
                values[1] = String.valueOf(ppi.heterogeneityPValue);
                values[2] = String.valueOf(ppi.heterogeneityI2);
                values[3] = String.valueOf(ppi.getConfidenceScore());
                List<String> genes = ppi.getGeneSymbs();
                avaibleGenePairInString.delete(0, avaibleGenePairInString.length());

                for (int k = 0; k < genes.size(); k++) {
                    int[] indexes = geneGenomeIndexes.get(genes.get(k));
                    // System.out.println(geneSymbol);
                    //has the head line
                    chromGenes[indexes[0]].add(new int[]{i + 1, indexes[1], 4 + 5 * k + 3});
                    values[4 + 5 * k] = genes.get(k);
                    values[4 + 5 * k + 1] = String.valueOf(genePValueMap.get(genes.get(k)).pValue);
                    values[4 + 5 * k + 2] = CHROM_NAMES[indexes[0]];
                    avaibleGenePairInString.append(genes.get(k));
                }
                avaibleGenePairInString.delete(0, avaibleGenePairInString.length());
                for (int k = genes.size() - 1; k >= 0; k--) {
                    avaibleGenePairInString.append(genes.get(k));
                }
                if (avaiblePairs.contains(avaibleGenePairInString.toString())) {
                    continue;
                } else {
                    avaiblePairs.add(avaibleGenePairInString.toString());
                }
                annotatedList.add(values);
                validGeneNum++;
            }
        }
        avaiblePairs.clear();

        //read data and export chromosome by chromosome
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            if (chromGenes[i].isEmpty()) {
                continue;
            }
            Chromosome chrom = genome.readChromosomefromDisk(i);
            List<Gene> geneList = chrom.genes;
            List<int[]> curChromGenes = chromGenes[i];
            for (int[] gensmbIndex : curChromGenes) {
                values = annotatedList.get(gensmbIndex[0]);
                Gene gene = geneList.get(gensmbIndex[1]);
                values[gensmbIndex[2]] = String.valueOf(gene.getStart());
                values[gensmbIndex[2] + 1] = String.valueOf(gene.getEnd());
            }
            chromGenes[i].clear();
        }

        if (outputFormatType == 0) {
            File saveFile = new File(outPath);
            LocalExcelFile.WriteArray2XLSXFile(saveFile.getCanonicalPath(), annotatedList, true);
            logInfo.append(validGeneNum);
            logInfo.append(" significant gene pairs (Family-wise error rate<=").append(ppIBasedAssociation.getPpIAssocNominalError()).append(") after heterogeneity test are saved in ");
            logInfo.append(saveFile.getCanonicalPath());
            logInfo.append('\n');

        } else if (outputFormatType == 1) {

            File saveFile = new File(outPath);
            LocalExcelFile.writeArray2ExcelFile(saveFile.getCanonicalPath(), annotatedList, true);
            logInfo.append(validGeneNum);
            logInfo.append(" significant gene pairs (Family-wise error rate<=").append(ppIBasedAssociation.getPpIAssocNominalError()).append(") after heterogeneity test are saved in ");
            logInfo.append(saveFile.getCanonicalPath());
            logInfo.append('\n');
        } else {
            File saveFile = new File(outPath);
            LocalFile.writeData(saveFile.getCanonicalPath(), annotatedList, "\t", false);
            logInfo.append(validGeneNum);
            logInfo.append(" significant gene pairs (Family-wise error rate<=").append(ppIBasedAssociation.getPpIAssocNominalError()).append(") after heterogeneity test are saved in ");
            logInfo.append(saveFile.getCanonicalPath());
            logInfo.append('\n');
        }

        annotatedList = null;

        LOG.info(logInfo.toString());
        runningResultTopComp.insertText(logInfo.toString());
        return logInfo.toString();
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
        if (!GenotypeSetUtil.calculateGenotypeCorrelationSquareFastBit(mappedSNP, chromGtys, ldRsMatrix, ppIBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2())) {
            infor = "Using slow function due to mssing genotypes!!!";
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
            GenotypeSetUtil.calculateGenotypeCorrelationSquareFast(mappedSNP, chromGtys, ldRsMatrix, ppIBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2());
        }

        return ldRsMatrix;
    }

    //read all involved positions and the LDs
    public CorrelationBasedByteLDSparseMatrix readHapMapLDRSquareByPositions(String hapMapFilePath, String ppiSetName, int chromIndex, Set<Integer> positionsSet,
            int[] indexesInLDFile, LiftOver liftOver) throws Exception {
        double minR2InGene = 0.01;

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
        sb.append("Position\n");
        IntArrayList snpPositionList = new IntArrayList();
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
            File outPath = new File(prjWorkingPath + File.separator + prjName + File.separator + ppIBasedAssociation.getName() + "." + ppiSetName + ".NoLDSNPs." + chromName + ".txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
            bw.write(sb.toString());
            bw.close();

            sb.delete(0, sb.length());
            infor = "Warning!!! " + unmappedSNPinGeneNum + " variants within genes out of " + totalSNPinGeneNum + " on chromosome " + chromName + " have NO haplotype information and will be assumed to be independent of others or will be ingored! Detailed information of these variants is saved in " + outPath.getCanonicalPath() + ".";
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
        }
        addedPositionSet.clear();
        infor = ldRsMatrix.size() + " LD pairs have been read.";
        runningResultTopComp.insertText(infor);
        LOG.info(infor);

        return ldRsMatrix;
    }

    /*
     // this function attempts to calculate SNP LD only within genes because it is too slow to do others
     public CorrelationBasedByteLDSparseMatrix calculateLocalLDRSquarebyHaplotypeInGene(File[] mapHapFiles,
     String chromName, Set<Integer> positionsSet,
     LiftOver liftOver) throws Exception {
     CorrelationBasedByteLDSparseMatrix ldRsMatrix = new CorrelationBasedByteLDSparseMatrix();

     if (mapHapFiles == null) {
     return ldRsMatrix;
     }
     HaplotypeDataset haplotypeDataset = new HaplotypeDataset();
     if (!mapHapFiles[0].exists()) {
     String infor = "No map file for chromosome " + chromName;
     runningResultTopComp.insertText(infor);
     LOG.info(infor);
     return ldRsMatrix;
     }

     if (!mapHapFiles[1].exists()) {
     String infor = "No haplotype file for chromosome " + chromName;
     runningResultTopComp.insertText(infor);
     LOG.info(infor);
     return ldRsMatrix;
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
     GenotypeSetUtil.calculateLDRSequareByHaplotype(mappedSNP, chromGtys, ldRsMatrix, ppIBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2());

     return ldRsMatrix;
     }*/
    // this function attempts to calculate SNP LD only within genes because it is too slow to do others
    public CorrelationBasedByteLDSparseMatrix calculateLocalLDRSquarebyHaplotypeVCFByPositions(
            String vcfFilePath, String ppiSetName, String chromName, Set<Integer> positionsSet, LiftOver liftOver) throws Exception {

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
        IntArrayList snpPositionList = new IntArrayList();

        int totalSNPinGeneNum = positionsSet.size();
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
            File outPath = new File(prjWorkingPath + File.separator + prjName + File.separator + ppIBasedAssociation.getName() + "." + ppiSetName + ".NoLDSNPs." + chromName + ".txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
            bw.write(sb.toString());
            bw.close();

            sb.delete(0, sb.length());
            infor = "Warning!!! " + unmappedSNPinGeneNum + " variants within genes out of " + totalSNPinGeneNum + " on chromosome " + chromName + " have NO haplotype information and will be assumed to be independent of others or will be ingored! Detailed information of these variants is saved in " + outPath.getCanonicalPath() + ".";
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
        }
        snpPositionList.quickSort();
        int ldSNPNum = snpPositionList.size();
        OpenIntIntHashMap allIndexMap = new OpenIntIntHashMap(ldSNPNum);
        for (int i = 0; i < ldSNPNum; i++) {
            allIndexMap.put(snpPositionList.getQuick(i), i);
        }
        snpPositionList.clear();
        CorrelationBasedByteLDSparseMatrix ldRsMatrix = new CorrelationBasedByteLDSparseMatrix(allIndexMap);

        infor = "Calculating local pair-wise LD of SNP within genes on chromosome " + chromName + " ...";
        runningResultTopComp.insertText(infor);
        LOG.info(infor);
        //GenotypeSetUtil.calculateGenotypeCorrelationSquare(mappedSNP, chromGtys, ldRsMatrix);

        if (!GenotypeSetUtil.calculateLDRSequareByHaplotypeFast(mappedSNP, chromGtys, ldRsMatrix, ppIBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2())) {
            infor = "Using slow function due to mssing genotypes!!!";
            runningResultTopComp.insertText(infor);
            LOG.info(infor);
            GenotypeSetUtil.calculateLDRSequareByHaplotype(mappedSNP, chromGtys, ldRsMatrix, ppIBasedAssociation.getGeneScan().getGenome().getMinEffectiveR2());
        }

        mappedSNP.clear();
        chromGtys.clear();
        addedPositionSet.clear();

        return ldRsMatrix;
    }

    public double calculatePPIPValues(int chrID1, int chrID2, List<PValueWeight> gene1TmpBlocks, List<PValueWeight> gene2TmpBlocks,
            CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes, double[] maxGeneLDR2, String marker) throws Exception {
        double MIN_R2 = 1E-3;

        int blockSize = gene1TmpBlocks.size() + gene2TmpBlocks.size();
        DenseDoubleMatrix2D rLDMatrix = new DenseDoubleMatrix2D(blockSize, blockSize);
        rLDMatrix.assign(0);
        PValueWeight[] geneTmpBlocks = new PValueWeight[blockSize];
        int blockSize1 = gene1TmpBlocks.size();
        int blockSize2 = gene2TmpBlocks.size();
        for (int i = 0; i < blockSize1; i++) {
            geneTmpBlocks[i] = gene1TmpBlocks.get(i);
        }
        for (int i = 0; i < blockSize2; i++) {
            geneTmpBlocks[i + blockSize1] = gene2TmpBlocks.get(i);
        }

        IntArrayList orginalPositions = new IntArrayList(blockSize);

        maxGeneLDR2[0] = 0;

        if (chrID1 == chrID2) {
            Arrays.sort(geneTmpBlocks, new PValueWeightComparatorIndex());
            for (int k = 0; k < blockSize; k++) {
                rLDMatrix.setQuick(k, k, 1);
                if (ldRsMatrixes[chrID1] != null) {
                    for (int j = k + 1; j < blockSize; j++) {

                        double x = ldRsMatrixes[chrID1].getLDAt(geneTmpBlocks[k].physicalPos, geneTmpBlocks[j].physicalPos);
                        if (k < blockSize1 && j >= blockSize1 && maxGeneLDR2[0] < x) {
                            maxGeneLDR2[0] = x;
                        }
                        if (x > MIN_R2) {
                            // ldCorr.setLDAt(pvalueWeightList.get(k).physicalPos, pvalueWeightList.get(j).physicalPos, (float) x);
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
                orginalPositions.add(geneTmpBlocks[k].physicalPos);
                geneTmpBlocks[k].physicalPos = k;
            }
        } else {
            //the two genes are on different chromsomes 
            for (int k = 0; k < blockSize1; k++) {
                rLDMatrix.setQuick(k, k, 1);
                if (ldRsMatrixes[chrID1] != null) {
                    for (int j = k + 1; j < blockSize1; j++) {

                        double x = ldRsMatrixes[chrID1].getLDAt(gene1TmpBlocks.get(k).physicalPos, gene1TmpBlocks.get(j).physicalPos);

                        if (x > MIN_R2) {
                            // ldCorr.setLDAt(pvalueWeightList.get(k).physicalPos, pvalueWeightList.get(j).physicalPos, (float) x);
                            rLDMatrix.setQuick(k, j, x);
                            rLDMatrix.setQuick(j, k, x);
                        } else {
                            rLDMatrix.setQuick(k, j, 0);
                            rLDMatrix.setQuick(j, k, 0);
                        }

                    }
                } else {
                    for (int j = k + 1; j < blockSize1; j++) {
                        rLDMatrix.setQuick(k, j, 0);
                        rLDMatrix.setQuick(j, k, 0);
                    }
                }
                orginalPositions.add(geneTmpBlocks[k].physicalPos);
                geneTmpBlocks[k].physicalPos = k;
            }

            for (int k = 0; k < blockSize2; k++) {
                rLDMatrix.setQuick(blockSize1 + k, blockSize1 + k, 1);
                if (ldRsMatrixes[chrID2] != null) {
                    for (int j = k + 1; j < blockSize2; j++) {

                        double x = ldRsMatrixes[chrID2].getLDAt(gene2TmpBlocks.get(k).physicalPos, gene2TmpBlocks.get(j).physicalPos);

                        if (x > MIN_R2) {
                            // ldCorr.setLDAt(pvalueWeightList.get(k).physicalPos, pvalueWeightList.get(j).physicalPos, (float) x);
                            rLDMatrix.setQuick(blockSize1 + k, blockSize1 + j, x);
                            rLDMatrix.setQuick(blockSize1 + j, blockSize1 + k, x);
                        } else {
                            rLDMatrix.setQuick(blockSize1 + k, blockSize1 + j, 0);
                            rLDMatrix.setQuick(blockSize1 + j, blockSize1 + k, 0);
                        }

                    }
                } else {
                    for (int j = k + 1; j < blockSize2; j++) {
                        rLDMatrix.setQuick(blockSize1 + k, blockSize1 + j, 0);
                        rLDMatrix.setQuick(blockSize1 + j, blockSize1 + k, 0);
                    }
                }
                orginalPositions.add(geneTmpBlocks[blockSize1 + k].physicalPos);
                geneTmpBlocks[blockSize1 + k].physicalPos = blockSize1 + k;
            }
        }

        /*
         if (marker != null && marker.equals("AKT3SRPK2")) {
         System.out.println(rLDMatrix.toString());
         int sss = 0;
         }
         */
        double v = SetBasedTest.combinePValuebyScaleedFisherCombinationTestCovLogP(geneTmpBlocks, rLDMatrix);

        /*
         PValueWeight[] tempBlocks = new PValueWeight[geneTmpBlocks.size()];
         for (int i = 0; i < geneTmpBlocks.size(); i++) {
         tempBlocks[i] = geneTmpBlocks.get(i);
         }
        
         double v =  MultipleTestingMethod.combinePValuebyWeightedSimeCombinationTestMyMe(tempBlocks, rLDMatrix);
         */
        //restore the postions
        for (int k = 0; k < blockSize; k++) {
            geneTmpBlocks[k].physicalPos = orginalPositions.getQuick(k);
        }
        return v;
    }

    public void ppIScan() {
        //record the classification settings
        ScanPPIBasedAssocSwingWorker worker = new ScanPPIBasedAssocSwingWorker();
        // buildTask = buildTask.create(buildingThread); //the task is not started yet
        buildTask = RP.create(worker); //the task is not started yet
        buildTask.schedule(0); //start the task
    }

    public List<PPISet> ppiComplexAssociationTestWithGraph(Map<String, PValueGene> genePValueMap, Map<String, int[]> geneGenomeIndexes,
            String setName, Graph<String, PPIEdge> graph, boolean filter) throws Exception {
        String symbol;
        PValueGene gp1 = null;
        DoubleArrayList tmpGeneValueArray = new DoubleArrayList();

        Map<String, Double> neighberNum = new HashMap<String, Double>();
        // Map<String, Double> neighberThreshold = new HashMap<String, Double>();
        Map<String, List<String>> uniqueNeighberPPIs = new HashMap<String, List<String>>();
        double[] maxGeneLD = new double[1];

        //construct Interaction set including pairs, triangle and rectangle ... 
        Collection<String> vertex = graph.getVertices();
        Iterator<String> iter = vertex.iterator();
//construct the neighberhood pairs to construct the set
        while (iter.hasNext()) {
            symbol = iter.next();
            gp1 = genePValueMap.get(symbol);
            if (gp1 == null) {
                continue;
            }

            List<String> neighbers = new ArrayList<String>(graph.getNeighbors(symbol));
            Collections.sort(neighbers);
            int length = neighbers.size();
            neighberNum.put(symbol, (double) length);
            tmpGeneValueArray.clear();
            //keep this for filtration
            for (int i = 0; i < length; i++) {
                gp1 = genePValueMap.get(symbol);
                if (gp1 == null) {
                    continue;
                }
                tmpGeneValueArray.add(gp1.pValue);
            }

            tmpGeneValueArray.quickSort();

            //obsolete codes
            // double pT = significanceGeneCheck(tmpGeneValueArray);
            // neighberThreshold.put(symbol, pT);
            int index = Collections.binarySearch(neighbers, symbol);
            if (index < 0) {
                //discard gene symbols less than symbol which could avoid redundent triangle 
                uniqueNeighberPPIs.put(symbol, neighbers.subList(-index - 1, length));
            }
        }

        String info = "There are " + graph.getEdgeCount() + " gene-pairs; " + vertex.size() + " genes in the local dataset have at least one gene pair.\n";
        info += " gene pairs scan on the genome ...";
        LOG.info(info);
        runningResultTopComp.insertText(info);

        Set<Integer>[] positionsSets = new Set[CHROM_NAMES.length];
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            positionsSets[chromIndex] = new HashSet<Integer>();
        }
        String geneSymb1 = null;
        String geneSymb2 = null;
        double[] pvalues = new double[2];
        //select key SNP positions
        for (Map.Entry<String, List<String>> m : uniqueNeighberPPIs.entrySet()) {
            geneSymb1 = m.getKey();
            // this can be easily implemented for Interaction triangles and Interaction rectangles
            int[] indexes1 = geneGenomeIndexes.get(geneSymb1);
            if (indexes1 == null) {
                continue;
            }
            gp1 = genePValueMap.get(geneSymb1);
            for (int i = 0; i < gp1.keySNPPositions.size(); i++) {
                positionsSets[indexes1[0]].add(gp1.keySNPPositions.getQuick(i));
            }

            List<String> ppiGenes = uniqueNeighberPPIs.get(geneSymb1);
            pvalues[0] = gp1.pValue;

            int ppiNum = ppiGenes.size();
            for (int i = 0; i < ppiNum; i++) {
                geneSymb2 = ppiGenes.get(i);

                int[] indexes2 = geneGenomeIndexes.get(geneSymb2);
                if (indexes2 == null) {
                    continue;
                }
                //consider the gene on the same  chromosome 
                PValueGene gp2 = genePValueMap.get(geneSymb2);
                for (int j = 0; j < gp2.keySNPPositions.size(); j++) {
                    positionsSets[indexes2[0]].add(gp2.keySNPPositions.getQuick(j));
                }
            }
        }

        File CHAIN_FILE = null;
        LiftOver liftOverLDGenome2pValueFile = null;
        Zipper ziper = new Zipper();
        Genome genome = ppIBasedAssociation.getGeneScan().getGenome();
        String pValueFileGenomeVersion = genome.getFinalBuildGenomeVersion();

        Genome ldGenome;
        if (genome.getLdSourceCode() == -2) {
            ldGenome = GlobalManager.currentProject.getGenomeByName(genome.getSameLDGenome());
        } else {
            ldGenome = genome;
        }

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
                ldRsMatrixes[chromIndex] = readHapMapLDRSquareByPositions(ldGenome.getChromLDFiles()[chromIndex], setName, chromIndex, positionsSets[chromIndex], indexesInLDFile, liftOverLDGenome2pValueFile);

            } else if (ldGenome.getLdSourceCode() == 2) {
                if (ldGenome.getHaploMapFilesList() == null) {
                    continue;
                }
                //ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyHaplotypeInGene(ldGenome.getHaploMapFilesList().get(chromIndex), CHROM_NAMES[chromIndex], positionsSets[chromIndex], liftOverLDGenome2pValueFile);
            } else if (ldGenome.getLdSourceCode() == 4) {
                if (ldGenome.getChromLDFiles()[chromIndex] != null) {
                    File hapMapLDFile = new File(ldGenome.getChromLDFiles()[chromIndex]);
                    if (!hapMapLDFile.exists()) {
                        continue;
                    }
                }
                ldRsMatrixes[chromIndex] = calculateLocalLDRSquarebyHaplotypeVCFByPositions(ldGenome.getChromLDFiles()[chromIndex], setName, CHROM_NAMES[chromIndex], positionsSets[chromIndex], liftOverLDGenome2pValueFile);
            }
        }

        //  Map<String, double[]> genePPIAtts = readPPIGeneAttribute();
        // double weightAdjustFactor = 1;
        int subGNum = 2;
        pvalues = new double[subGNum];
        String[] geneSymbs = new String[subGNum];

        int counting = 0;
        List<PPISet> ppiSetList = new ArrayList<PPISet>();
        boolean toDebug = false;

        List<PValueWeight> gene1TmpBlocks = new ArrayList<PValueWeight>();
        List<PValueWeight> gene2TmpBlocks = new ArrayList<PValueWeight>();

        double tmpP = 0;
        //force to ingore snps without LD information
        boolean ingorNoLDSNP = ppIBasedAssociation.isIgnoreNoLDSNPs();
        for (Map.Entry<String, List<String>> m : uniqueNeighberPPIs.entrySet()) {
            geneSymbs[0] = m.getKey();
          //  System.out.println(geneSymbs[0]);

            // this can be easily implemented for Interaction triangles and Interaction rectangles
            int[] indexes1 = geneGenomeIndexes.get(geneSymbs[0]);
            if (indexes1 == null) {
                continue;
            }

            gene1TmpBlocks.clear();
            // double[] atts = genePPIAtts.get(geneSymbs[0]);
            double geneWeight1 = 1;
            /*
             if (weightPPIGene && atts != null) {
             geneWeight1 = -3.035 + atts[0] * (-2.330e-02) + atts[1] * (-5.354e-06)
             + atts[2] * (-2.595) + atts[3] * (1.561) + atts[4] * (-8.567e-01);
             // geneWeight1 = 1 / (1 + Math.exp(-geneWeight1));
             //use the inverse the weight as the weight is to down-weight a more likely gene
             geneWeight1 = (1 + Math.exp(-geneWeight1));
             // geneWeight1 = Math.pow(geneWeight1, weightAdjustFactor);
             }
             * 
             */

            gp1 = genePValueMap.get(geneSymbs[0]);
            if (gp1.keySNPPositions.size() == 0) {
                continue;
            }
            for (int i = 0; i < gp1.keySNPPositions.size(); i++) {
                if (ingorNoLDSNP) {
                    if (ldRsMatrixes[indexes1[0]] == null || ldRsMatrixes[indexes1[0]].isEmpty()) {
                        continue;
                    }
                    if (!ldRsMatrixes[indexes1[0]].positionIndexMap.containsKey(gp1.keySNPPositions.getQuick(i))) {
                        continue;
                    }
                }
                PValueWeight gpb = new PValueWeight();
                gpb.pValue = gp1.blockPValues.getQuick(i);
                gpb.weight = geneWeight1;
                gpb.physicalPos = gp1.keySNPPositions.getQuick(i);
                gene1TmpBlocks.add(gpb);
            }
            if (gene1TmpBlocks.isEmpty()) {
                continue;
            }

            // To change it and use HYST to calculate the pvalues[0]
            //  DoubleMatrix2D dm=new DoubleMatrix2D();
            // pvalues[0]= MultipleTestingMethod.combinePValuebyScaleedFisherCombinationTestCovLogP(gene1TmpBlocks, ldRsMatrixes);
            List<String> ppiGenes = uniqueNeighberPPIs.get(geneSymbs[0]);
            pvalues[0] = gp1.pValue;

            int ppiNum = ppiGenes.size();
            for (int i = 0; i < ppiNum; i++) {
                geneSymbs[1] = ppiGenes.get(i);

                int[] indexes2 = geneGenomeIndexes.get(geneSymbs[1]);
                if (indexes2 == null) {
                    continue;
                }

                //consider the gene on the same  chromosome 
                PValueGene gp2 = genePValueMap.get(geneSymbs[1]);
                if (gp2.keySNPPositions.size() == 0) {
                    continue;
                }
                pvalues[1] = gp2.pValue;
                gene2TmpBlocks.clear();

                double geneWeight2 = 1;
                /*
                 atts = genePPIAtts.get(geneSymbs[1]);              
                 if (weightPPIGene && atts != null) {
                 geneWeight2 = -3.035 + atts[0] * (-2.330e-02) + atts[1] * (-5.354e-06)
                 + atts[2] * (-2.595) + atts[3] * (1.561) + atts[4] * (-8.567e-01);
                 // geneWeight2 = 1 / (1 + Math.exp(-geneWeight2));
                 geneWeight2 = (1 + Math.exp(-geneWeight2));
                 // geneWeight2 = Math.pow(geneWeight2, weightAdjustFactor);
                 }
                 * 
                 */

                for (int j = 0; j < gp2.keySNPPositions.size(); j++) {
                    if (ingorNoLDSNP) {
                        if (ldRsMatrixes[indexes2[0]] == null || ldRsMatrixes[indexes2[0]].isEmpty() || !ldRsMatrixes[indexes2[0]].positionIndexMap.containsKey(gp2.keySNPPositions.getQuick(j))) {
                            continue;
                        }
                    }
                    PValueWeight gpb = new PValueWeight();
                    gpb.pValue = gp2.blockPValues.getQuick(j);
                    gpb.weight = geneWeight2;
                    gpb.physicalPos = gp2.keySNPPositions.getQuick(j);
                    gene2TmpBlocks.add(gpb);
                }
                if (gene2TmpBlocks.isEmpty()) {
                    continue;
                }

                /*
                 if (geneSymbs[0].equals("AKT3") && geneSymbs[1].equals("SRPK2") || geneSymbs[0].equals("AKT3") && geneSymbs[1].equals("SRPK2")) {
                 int sss = 0;
                 System.out.println(geneSymbs[0]);
                 for (int j = 0; j < gp1.keySNPPositions.size(); j++) {
                 System.out.println(gene1TmpBlocks.get(j).physicalPos + "\t" + gene1TmpBlocks.get(j).pValue);
                 }
                 System.out.println(geneSymbs[1]);
                 for (int j = 0; j < gp2.keySNPPositions.size(); j++) {
                 System.out.println(gene2TmpBlocks.get(j).physicalPos + "\t" + gene2TmpBlocks.get(j).pValue);
                 }
                 }
                 */
                double v = -1;
                //temperally set an unreasonable value which will be chanted later
                PPISet ppi = new PPISet();

                List<String> ppiSymbs = new ArrayList<String>();
                ppiSymbs.add(geneSymbs[0]);
                ppiSymbs.add(geneSymbs[1]);
                ppi.setGeneSymbs(ppiSymbs);
                PPIEdge pe = graph.findEdge(geneSymbs[0], geneSymbs[1]);
                ppi.setConfidenceScore(pe.getScore());

                v = calculatePPIPValues(indexes1[0], indexes2[0], gene1TmpBlocks, gene2TmpBlocks, ldRsMatrixes, maxGeneLD, geneSymbs[0] + geneSymbs[1]);

                if (maxGeneLD[0] > 0.01) {
                    //when v is too small  equivalentChi is infinit

                    // double as=Probability.normalInverse(1-v);
                    //a new alogrithm to adjust LD between dependent gene based p values
                    double equivalentChi = MultipleTestingMethod.inverseChisquareCumulativeProbability("Gene-pair", 4, v);
                    double equalChiqAtB = equivalentChi + 2 * Math.log(pvalues[0]);
                    equalChiqAtB = -equalChiqAtB / 2;
                    tmpP = Math.exp(equalChiqAtB);
                    // the tmp cannot be smaller than the pvalues[1]; otherwise it may has some problem.
                    if (tmpP > pvalues[1]) {
                        pvalues[1] = tmpP;
                    }
                    if (pvalues[1] > 1) {
                        pvalues[1] = 1;
                    }
                }

                ppi.associcationPValue = v;

                // System.out.println(newPPISetList.toString());
                v = MultipleTestingMethod.combinationHeterogeneityCochranQTest("Gene-pair", pvalues);
                //observedPpiValueArray.add(v);
                ppi.setHeterogeneityPValue(v);
                v = MultipleTestingMethod.combinationHeterogeneityI2("Gene-pair", pvalues);
                ppi.setHeterogeneityI2(v);

                if (toDebug) {
                    for (PValueWeight gb : gene1TmpBlocks) {
                        gb.weight = 1;
                    }

                    for (PValueWeight gb : gene2TmpBlocks) {
                        gb.weight = 1;
                    }
                    v = calculatePPIPValues(indexes1[0], indexes2[0], gene1TmpBlocks, gene2TmpBlocks, ldRsMatrixes, maxGeneLD, null);
                    ppi.setConfidenceScore(v);
                } else {
                   // ppi.setConfidenceScore(0);
                }

                ppiSetList.add(ppi);
                counting++;

                /*
                 v = MultipleTestingMethod.combinePValuebyFisherCombinationTest(pvalues);
                 ppiValueArrayNoLD.add(v);
                 * 
                 */
            }
        }

        ldRsMatrixes = null;
        System.gc();

        /*
         double mid = Descriptive.median(ppiValueArrayLD);
         double lamda = mid / 0.456;
         System.out.print("IF " + lamda);
         for (int j = 0; j < ppiValueArrayLD.size(); j++) {
         ppiValueArrayLD.setQuick(j, Probability.chiSquareComplemented(1, ppiValueArrayLD.getQuick(j) / lamda));
         }
         */
        info = "Sub-network size: " + (subGNum) + "; Number of Sub-networks: " + counting;
        LOG.info(info);
        runningResultTopComp.insertText(info);

        info = "Finished gene-network scan on the genome!";
        LOG.info(info);
        runningResultTopComp.insertText(info);
        return ppiSetList;
    }

    class ScanPPIBasedAssocSwingWorker extends SwingWorker<Void, String> {

        private final int NUM = 100;
        int runningThread = 0;
        boolean succeed = false;
        long time;
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Gene-pair association scan task", new Cancellable() {
            @Override
            public boolean cancel() {
                return handleCancel();
            }
        });

        public ScanPPIBasedAssocSwingWorker() {
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

                ///This is to make a control test.
                boolean permutePPINextwork = false;
                String inforString = "Start to do Gene-pair scan...";
                runningResultTopComp.insertIcon(imgFolder, "Next.png", inforString);
                LOG.info(inforString);

                StatusDisplayer.getDefault().setStatusText("Scan interaction-based association on the genome ...");
                Map<String, PValueGene> genePValueMap = new HashMap<String, PValueGene>();

                List<String> sourcePValueNames = ppIBasedAssociation.getPValueSources();
                Genome genome = ppIBasedAssociation.getGeneScan().getGenome();
                Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();
                FileString[] ppiSets = ppIBasedAssociation.getPpIDBFiles();
                boolean isMeregePPISet = ppIBasedAssociation.isIsToMergePPISet();
                PPIGraph ppiGraph = new PPIGraph();
                ph.start(); //we must start the PH before we swith to determinate
                ph.switchToIndeterminate();
                //better to develped a alorithme purely using the graphas
                //unfortunately this Graph class is much slower than my alogrithm, probabliy it did not use the binary search function
                // but later I found UndirectedSparseGraph made it as fast as my alogrithm
                int totalProcessNum = sourcePValueNames.size();
                //only local files have multiple ppi set
                if (ppIBasedAssociation.isUseLocalPPIFile() && !isMeregePPISet) {
                    totalProcessNum *= ppiSets.length;
                }
                double unitNum = 5;
                double basicUit = NUM / (totalProcessNum * unitNum);
                Graph<String, PPIEdge> allGeneGraph = null;
                int pVresourceNum = 0;
                int[] indices = {1, 3};
                ArrayList<String[]> allGenes = new ArrayList<String[]>();
                LocalFile.retrieveData(GlobalManager.RESOURCE_PATH + "HgncGene.txt.gz", allGenes, indices, "\t");
                Map<String, String> latestGenesSymbMap = new HashMap<String, String>();
                for (String[] item : allGenes) {
                    if (item[1] == null || item[1].isEmpty()) {
                        continue;
                    }
                    String[] cells = item[1].split(",");
                    for (String cell : cells) {
                        latestGenesSymbMap.put(cell.trim(), item[0]);
                    }
                }
                List<String> allGWASGenes = new ArrayList<String>();
                for (String sourcePValueName : sourcePValueNames) {
                    genePValueMap.clear();
                    StringBuilder infors = new StringBuilder();
                    // Calculate Gene-pair associations
                    List<PValueGene> geneList = ppIBasedAssociation.getGeneScan().loadGenePValuesfromDisk(sourcePValueName,null);
                    Collections.sort(geneList, new GenePValueComparator());
                    int geneNum = geneList.size();

                    for (int i = 0; i < geneNum; i++) {
                        genePValueMap.put(geneList.get(i).getSymbol(), geneList.get(i));
                        allGWASGenes.add(geneList.get(i).getSymbol());
                    }

                    Set<String> geneSymbs = new HashSet<String>(genePValueMap.keySet());
                    if (ppIBasedAssociation.isUseLocalPPIFile() && isMeregePPISet) {
                        List<String[]> allPairs = new ArrayList<String[]>();

                        for (FileString file : ppiSets) {
                            List<String[]> pairs = ppiGraph.readPPIPairs(geneSymbs, file, 0, 1, 2, (ppIBasedAssociation.getCofidenceScoreThreshold()), infors);
                            if (pairs != null) {
                                for (String[] pair : pairs) {
                                    if (latestGenesSymbMap.containsKey(pair[0])) {
                                        pair[0] = latestGenesSymbMap.get(pair[0]);
                                    }
                                    if (latestGenesSymbMap.containsKey(pair[1])) {
                                        pair[1] = latestGenesSymbMap.get(pair[1]);
                                    }
                                }
                                allPairs.addAll(pairs);
                                infors.append("\n");
                            }
                        }

                        allGeneGraph = ppiGraph.buildGraph(allPairs, permutePPINextwork, infors);
                        allPairs.clear();
                        publish(infors.toString());

                       
                        List<PPISet> ppiList = ppiComplexAssociationTestWithGraph(genePValueMap, geneGenomeIndexes, "merged", allGeneGraph, true);
                        Collections.sort(ppiList, new PPISetPValueComparator());
                        

                        double[] pvalueCutoffs = pPISetSigTestAndQQPlot(ppiList, genePValueMap, "merged", true, true);

                        ppIBasedAssociation.savePPIAssociaion(sourcePValueName, ppiList, "merged");
                       
                        publish(infors.toString());
                        infors.delete(0, infors.length());
                        //recordPhysicalDistancePPIPairs(ppiList, adjustedPpiPValueThreshold, 200000);
                        // exportPPISetListNoSNP(ppiList, allGeneGraph, genePValueMap, "ppi.heterogeneity.filter", pvalueCutoffs, true, true, true);
                        System.gc();

                    } else {
                        int iVresourceNum = 0;

                        for (FileString file : ppiSets) {
                            List<String[]> pairs = null;
                            if (ppIBasedAssociation.isUseLocalPPIFile()) {
                                pairs = ppiGraph.readPPIPairs(geneSymbs, file, 0, 1, 2, (ppIBasedAssociation.getCofidenceScoreThreshold()), infors);
                            } else {
                                pairs = ppiGraph.readPPIPairs(geneSymbs, file, 4, 5, 6, (ppIBasedAssociation.getCofidenceScoreThreshold()), infors);
                            }
                            System.gc();
                            int allPPINum = 0;
                            if (pairs != null) {
                                allPPINum = pairs.size();
                                allGeneGraph = ppiGraph.buildGraph(pairs, permutePPINextwork, infors);
                                pairs.clear();
                            }
                            publish(infors.toString());
                            infors.delete(0, infors.length());

                             
                            List<PPISet> ppiList = ppiComplexAssociationTestWithGraph(genePValueMap, geneGenomeIndexes, file.getName(), allGeneGraph, true);
                            Collections.sort(ppiList, new PPISetPValueComparator());
                            

                            double[] pvalueCutoffs = pPISetSigTestAndQQPlot(ppiList, genePValueMap, file.getName(), true, true);

                            ppIBasedAssociation.savePPIAssociaion(sourcePValueName, ppiList, file.getName());
                           

                            //recordPhysicalDistancePPIPairs(ppiList, adjustedPpiPValueThreshold, 200000);
                            // exportPPISetListNoSNP(ppiList, allGeneGraph, genePValueMap, "ppi.heterogeneity.filter", pvalueCutoffs, true, true, true);
                            System.gc();
                            iVresourceNum++;
                            if (false) {
                                //codes to write paper
                                Set<String> set1 = new HashSet<String>(Arrays.asList(new String[]{"TCF4", "TLE1", "BCL11B", "CACNA1C", "CACNA1I", "CACNB2", "CSMD1", "NLGN4X", "IMMP2L", "SNAP91", "PJA1", "RIMS1", "TBC1D5", "CNKSR2", "GALNT10", "CYP26B1", "SLC39A8", "SNX19", "SATB2", "ZNF804A", "CNTN4", "TMTC1", "EPC2", "C12orf42", "CUL3", "TSNARE1", "TRANK1", "ZNF536", "PLCH2", "GRAMD1B", "ZSWIM6", "DPYD", "DRD2", "IGSF9B", "C11orf87", "FUT9", "GPM6A", "GRIN2A", "GRM3", "HCN1", "MAD1L1", "MAN2A1", "MMP16", "ATP2A2", "PRKD1", "BRINP2"}));
                                Set<String> set2 = new HashSet<String>(Arrays.asList(new String[]{"CNNM2", "SF3B1", "STAG1", "TCF4", "GIGYF2", "VRK2", "CACNA1C", "CACNA1I", "CACNB2", "IMMP2L", "PPP1R13B", "PPP1R16B", "AS3MT", "CHRNA3", "CHRNA5", "SOX2-OT", "SFMBT1", "APOPT1", "ZFYVE21", "PITPNM2", "PCGF6", "ZNF804A", "KCTD13", "WBP1L", "C10orf32", "SPATS2L", "TMEM219", "COQ10B", "OGFOD2", "CYP17A1", "AMBRA1", "C2orf47", "TSNARE1", "TYW5", "C12orf65", "C2orf69", "LINC00637", "TRANK1", "ZSWIM6", "DPYD", "ZC3H7B", "IGSF9B", "C2orf82", "FOXP1", "CACNA1C-AS4", "FXR1", "CACNA1C-IT3", "MIR137HG", "FTCDNL1", "C10orf32-ASMT", "ABCB9", "EP300-AS1", "HSPD1", "ITIH3", "KLC1", "MAD1L1", "MPHOSPH9", "NGEF", "NT5C2", "BAG5", "PTPRF"}));
                                Set<String> sigGenes1 = new HashSet<String>();
                                Set<String> sigGenes2 = new HashSet<String>();
                                int samplingTime = 100;
                                StringBuilder textInfor = new StringBuilder();
                                double heterogeneityTestThreshold = ppIBasedAssociation.getPpIHetroNominalError();

                                for (int t = 0; t < ppiList.size(); t++) {
                                    PPISet ppi = ppiList.get(t);
                                    if (ppi.heterogeneityI2 > heterogeneityTestThreshold) {
                                        continue;
                                    }
                                    if (ppi.associcationPValue > pvalueCutoffs[1]) {
                                        break;
                                    }
                                    if (genePValueMap.get(ppi.getGeneSymbs().get(0)).pValue > pvalueCutoffs[0]) {
                                        sigGenes1.add(ppi.getGeneSymbs().get(0));
                                    }
                                    if (genePValueMap.get(ppi.getGeneSymbs().get(1)).pValue > pvalueCutoffs[0]) {
                                        sigGenes1.add(ppi.getGeneSymbs().get(1));
                                    }
                                }
                                sigGenes2.addAll(sigGenes1);
                                sigGenes1.retainAll(set1);
                                sigGenes2.retainAll(set2);
                                int obs1 = sigGenes1.size();
                                int obs2 = sigGenes2.size();
                                double pass1 = 0;
                                double pass2 = 0;
                                StringBuilder record1 = new StringBuilder();
                                StringBuilder record2 = new StringBuilder();
                                for (int i = 0; i < samplingTime; i++) {
                                    pairs = ppiGraph.generateRandomInteractions(allGWASGenes, allPPINum, latestGenesSymbMap);
                                    allGeneGraph = ppiGraph.buildGraph(pairs, permutePPINextwork, infors);
                                    pairs.clear();
                                    ppiList = ppiComplexAssociationTestWithGraph(genePValueMap, geneGenomeIndexes, file.getName(), allGeneGraph, true);
                                    Collections.sort(ppiList, new PPISetPValueComparator());
                                    pvalueCutoffs = pPISetSigTest(ppiList, genePValueMap, true, true, textInfor);
                                    sigGenes1.clear();
                                    sigGenes2.clear();
                                    for (PPISet ppi : ppiList) {
                                        if (ppi.associcationPValue > pvalueCutoffs[1]) {
                                            break;
                                        }
                                        if (ppi.heterogeneityI2 > heterogeneityTestThreshold) {
                                            continue;
                                        }
                                        if (genePValueMap.get(ppi.getGeneSymbs().get(0)).pValue > pvalueCutoffs[0]) {
                                            sigGenes1.add(ppi.getGeneSymbs().get(0));
                                        }
                                        if (genePValueMap.get(ppi.getGeneSymbs().get(1)).pValue > pvalueCutoffs[0]) {
                                            sigGenes1.add(ppi.getGeneSymbs().get(1));
                                        }
                                    }
                                    sigGenes2.addAll(sigGenes1);
                                    sigGenes1.retainAll(set1);
                                    sigGenes2.retainAll(set2);
                                    if (obs1 < sigGenes1.size()) {
                                        pass1 += 1;
                                    }
                                    if (obs2 < sigGenes2.size()) {
                                        pass2 += 1;
                                    }
                                    record1.append(sigGenes1.size()).append(",");
                                    record2.append(sigGenes2.size()).append(",");
                                    System.gc();
                                }
                                System.out.println(file.getName() + " Error rate: " + (pass1 / samplingTime) + " " + (pass2 / samplingTime));
                                System.out.println(record1);
                                System.out.println(record2);
                            }
                        }
                    }
                    pVresourceNum++;

                }

                succeed = true;
            } catch (InterruptedException ex) {
                StatusDisplayer.getDefault().setStatusText("Scan Gene-pair-based association task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

            } catch (Exception ex) {
                StatusDisplayer.getDefault().setStatusText("Scan Gene-pair-based association task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            // TODO Auto-generated method stub  
            try {
                for (String message : chunks) {
                    LOG.info(message);
                    StatusDisplayer.getDefault().setStatusText(message);
                    runningResultTopComp.insertText(message);
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        @Override
        protected void done() {
            try {
                String message;
                if (!succeed) {
                    message = ("Gene-pair-based association scan failed!");
                    LOG.info(message);
                    return;
                }
                GlobalManager.currentProject.addPPIBasedAssociationScan(ppIBasedAssociation);
                GlobalManager.ppiAssocSetModel.addElement(ppIBasedAssociation);

                ProjectTopComponent projectTopComponent = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                projectTopComponent.showProject(GlobalManager.currentProject);

                message = ("Gene-pair-based association scan has been finished!");
                LOG.info(message);
                StatusDisplayer.getDefault().setStatusText(message);

                String prjName = GlobalManager.currentProject.getName();
                String prjWorkingPath = GlobalManager.currentProject.getWorkingPath();

                //ppiNetworkBasedScan.ppiAssociationScanSimutationOnly();
                if (onlyExportSignificant) {
                    /*
                     GraphViewerFrame gf = new GraphViewerFrame(fileNode.getCanonicalPath());
                     gf.setLocationRelativeTo(GlobalManager.currentApplication.getMainFrame());
                     KGG2App.getApplication().show(gf);
                     */
                }
              
                ph.finish();
                time = System.nanoTime() - time;
                time = time / 1000000000;
                long min = time / 60;
                long sec = time % 60;
                String info = ("Elapsed time: " + min + " min. " + sec + " sec.");
                runningResultTopComp.insertText(info);
                File ppiAssociationResultFilePath = new File(prjWorkingPath + File.separator + prjName + File.separator + ppIBasedAssociation.getName() + ".html");
                runningResultTopComp.savePane(ppiAssociationResultFilePath);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
}
