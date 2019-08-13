/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business;

import cern.colt.function.DoubleProcedure;
import cern.colt.list.DoubleArrayList;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import net.sf.samtools.util.AsciiLineReader;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import org.apache.log4j.Logger;

import org.cobi.kgg.business.entity.Constants;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.Pathway;
import org.cobi.kgg.business.entity.PathwayEnrichedPValueComparator;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.text.LocalFile;

/**
 *
 * @author mxli
 */
public class PathwayExplorer implements Constants {

    private final static Logger LOG = Logger.getLogger(PathwayExplorer.class.getName());
    private Map<String, Pathway> dbPathwaySet;
    private int uniqueTotalPathwayGeneNum = 0;
    File pathwayFile = null;
    Set<String> uniquePathwayGenes = new HashSet<String>();

    public Set<String> getUniquePathwayGenes() {
        return uniquePathwayGenes;
    }

    public int getUniqueTotalPathwayGeneNum() {
        return uniqueTotalPathwayGeneNum;
    }

    public void setUniqueTotalPathwayGeneNum(int uniqueTotalPathwayGeneNum) {
        this.uniqueTotalPathwayGeneNum = uniqueTotalPathwayGeneNum;
    }

    /**
     *
     * @throws Exception
     */
    public PathwayExplorer() throws Exception {
        dbPathwaySet = new HashMap<String, Pathway>();
    }

    public Map<String, Pathway> getPathwaySet() {
        return dbPathwaySet;
    }

    public void setPathwaySet(Map<String, Pathway> pathwaySet) {
        this.dbPathwaySet = pathwaySet;
    }

    /**
     *
     * @param kggPathwayFileName
     * @param biocartaPathwayFileName
     * @throws Exception
     */
    public String loadGSEAPathways(File pathwayFile1, int minGene, int maxGene) throws Exception {
        String line = null;

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

        dbPathwaySet.clear();
        HashSet<String> smalllPathIDToRemove = new HashSet<String>();
        HashSet<String> largePathIDToRemove = new HashSet<String>();

        pathwayFile = pathwayFile1;
        LineReader br = null;
        if (pathwayFile.exists() && pathwayFile.getName().endsWith(".zip")) {
            br = new CompressedFileReader(pathwayFile);
        } else {
            if (pathwayFile.exists() && pathwayFile.getName().endsWith(".gz")) {
                br = new CompressedFileReader(pathwayFile);
            } else {
                if (pathwayFile.exists()) {
                    br = new AsciiLineReader(pathwayFile);
                } else {
                    throw new Exception("No input file: " + pathwayFile.getCanonicalPath());
                }
            }
        }
        int index = -1;
        float weight = 1;
        while ((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            //skip gene ID
            String pathID = st.nextToken().trim();
            String url = st.nextToken().trim();

            Pathway pathway = dbPathwaySet.get(pathID);
            if (pathway == null) {
                pathway = new Pathway(pathID, pathID, url);
            }
            while (st.hasMoreTokens()) {
                String geneSymb = st.nextToken().trim();
                if (latestGenesSymbMap.containsKey(geneSymb)) {
                    geneSymb = latestGenesSymbMap.get(geneSymb);
                }
                index = geneSymb.indexOf('|');
                if (index >= 0) {
                    weight = Float.parseFloat(geneSymb.substring(index + 1));
                    pathway.addGeneSymbolWeight(geneSymb.substring(0, index), weight);
                } else {
                    pathway.addGeneSymbolWeight(geneSymb, 1);
                }
            }

            if (pathway.getGeneSymbols().size() < minGene) {
                smalllPathIDToRemove.add(pathway.getID());
            } else if (pathway.getGeneSymbols().size() > maxGene) {
                largePathIDToRemove.add(pathway.getID());
            } else {
                dbPathwaySet.put(pathID, pathway);
                uniquePathwayGenes.addAll(pathway.getGeneSymbols());
            }
        }
        br.close();

        StringBuilder info = new StringBuilder();
        Iterator<String> iter;
        if (largePathIDToRemove.size() > 0) {
            info.append("The following pathways are excluded due to their gene number is over ");
            info.append(maxGene);
            info.append("\n: [");
            iter = largePathIDToRemove.iterator();
            while (iter.hasNext()) {
                info.append(iter.next());
                info.append("; ");
            }
            largePathIDToRemove.clear();
            info.append("]\n\n");
        }

        if (smalllPathIDToRemove.size() > 0) {
            info.append("The following pathways are excluded due to their gene number is less than ");
            info.append(minGene);
            info.append("\n: [");
            iter = smalllPathIDToRemove.iterator();
            while (iter.hasNext()) {
                String id = iter.next();
                info.append(id);
                info.append("; ");
            }
            largePathIDToRemove.clear();
            info.append("]\n");
        }

        uniqueTotalPathwayGeneNum = uniquePathwayGenes.size();

        return info.toString();

    }

    /**
     *
     * @param totalGWASGenes
     * @param seedGenes
     * @param sigGenes
     * @param searchedPathwayList
     * @throws Exception
     */
    public void searchEnrichedPathwaysby2Sets(HashSet<String> totalGWASGenes, Map<String, Gene> seedGenes, Map<String, Gene> sigGenes, List<Pathway> searchedPathwayList) throws Exception {
        int seedGeneInPathNum = 0;
        int sigGeneInPathNum = 0;
        int subPopulationSize = 0;

        Map<String, Pathway> searchedPathways = new HashMap<String, Pathway>();
        for (Map.Entry<String, Pathway> mPath : dbPathwaySet.entrySet()) {
            Set<String> pGenes = mPath.getValue().getGeneSymbols();
            String pathID = mPath.getKey();
            Pathway curPath = searchedPathways.get(pathID);
            if (curPath == null) {
                curPath = new Pathway(pathID, mPath.getValue().getName(), mPath.getValue().getURL());
                curPath.setURL(mPath.getValue().getURL());
            }
            seedGeneInPathNum = 0;
            sigGeneInPathNum = 0;
            subPopulationSize = 0;
            Iterator<String> it = pGenes.iterator();
            while (it.hasNext()) {
                String geneSym = it.next();
                if (totalGWASGenes.contains(geneSym)) {
                    subPopulationSize++;
                }
                if (seedGenes.containsKey(geneSym)) {
                    curPath.addGeneSymbolWeight(geneSym, 1);
                    seedGeneInPathNum++;
                }
                if (sigGenes.containsKey(geneSym)) {
                    curPath.addGeneSymbolWeight(geneSym, 1);
                    sigGeneInPathNum++;
                }
            }

            if (seedGeneInPathNum > 0 && sigGeneInPathNum > 0) {
                curPath.setEnrichedPValue(MultipleTestingMethod.hypergeometricEnrichmentTest(GENOME_GENE_NUM, pGenes.size(), seedGenes.size(), seedGeneInPathNum)
                        * MultipleTestingMethod.hypergeometricEnrichmentTest(totalGWASGenes.size(), subPopulationSize, sigGenes.size(), sigGeneInPathNum));
                searchedPathways.put(pathID, curPath);
            }
        }
        searchedPathwayList.addAll(searchedPathways.values());
        Collections.sort(searchedPathwayList, new PathwayEnrichedPValueComparator());
    }

    /**
     *
     * @param totalGWASGenes
     * @param sigGenes
     * @param pathwayList
     * @throws Exception
     */
    public void searchEnrichedPathways(HashSet<String> totalGWASGenes, Map<String, Gene> sigGenes, List<Pathway> pathwayList) throws Exception {
        Map<String, Pathway> searchedPathways = new HashMap<String, Pathway>();

        int sigGeneInPathNum = 0;
        int subPopulationSize = 0;
        for (Map.Entry<String, Pathway> mPath : dbPathwaySet.entrySet()) {
            Set<String> pGenes = mPath.getValue().getGeneSymbols();
            String pathID = mPath.getKey();
            Pathway curPath = searchedPathways.get(pathID);
            if (curPath == null) {
                curPath = new Pathway(pathID, mPath.getValue().getName(), mPath.getValue().getURL());
                curPath.setURL(mPath.getValue().getURL());
            }

            sigGeneInPathNum = 0;
            subPopulationSize = 0;
            Iterator<String> it = pGenes.iterator();
            while (it.hasNext()) {
                String geneSym = it.next();
                if (totalGWASGenes.contains(geneSym)) {
                    subPopulationSize++;
                }
                if (sigGenes.containsKey(geneSym)) {
                    curPath.addGeneSymbolWeight(geneSym, 1);
                    sigGeneInPathNum++;
                }
            }

            if (sigGeneInPathNum > 0) {
                curPath.setEnrichedPValue(MultipleTestingMethod.hypergeometricEnrichmentTest(totalGWASGenes.size(),
                        subPopulationSize, sigGenes.size(), sigGeneInPathNum));
                searchedPathways.put(pathID, curPath);
            }
        }
        pathwayList.addAll(searchedPathways.values());
        Collections.sort(pathwayList, new PathwayEnrichedPValueComparator());
    }

    /**
     *
     * @param allGWASGenes
     * @param selectedGWASGenes
     * @param pathwayList
     * @throws Exception
     */
    public void searchEnrichedPathwaysby2Sets(Set<String> allGWASGenes,
            Set<String> selectedGWASGenes, Set<String> seedGenes, List<Pathway> pathwayList, int minSize) throws Exception {
        //note the selectedGWASGenes belongs to the allGWASGenes

        int sigGeneInPathNum = 0;
        int subPopulationSize = 0;
        int allGWASGeneNum = allGWASGenes.size();
        int selectedGWASGeneNum = selectedGWASGenes.size();

        for (Pathway curPath : pathwayList) {
            //Note this will lead to a bias to the multiple comparsions for pathway p values
            //if (!pathwayIDSet.contains(mPath.getValue().getID())) continue;\

            Set<String> pGenes = curPath.getGeneSymbols();
            String pathID = curPath.getID();

            sigGeneInPathNum = 0;
            subPopulationSize = 0;
            Iterator<String> pGeneIter = pGenes.iterator();
            if (seedGenes != null && seedGenes.size() > 0) {
                while (pGeneIter.hasNext()) {
                    String pGeneSymb = pGeneIter.next();
                    if (allGWASGenes.contains(pGeneSymb)) {
                        if (selectedGWASGenes.contains(pGeneSymb)) {
                            //curPath.addGeneSymbolWeight(pGeneSymb, 1);
                            sigGeneInPathNum++;
                        }
                        //include the seed gene into the pathway anyway
                        if (seedGenes.contains(pGeneSymb)) {
                            //curPath.addGeneSymbolWeight(pGeneSymb, 1);
                            sigGeneInPathNum++;
                        }
                        subPopulationSize++;
                    }
                }
            } else {
                while (pGeneIter.hasNext()) {
                    String pGeneSymb = pGeneIter.next();
                    if (allGWASGenes.contains(pGeneSymb)) {
                        if (selectedGWASGenes.contains(pGeneSymb)) {
                            //curPath.addGeneSymbolWeight(pGeneSymb, 1);
                            sigGeneInPathNum++;
                        }
                        subPopulationSize++;
                    }
                }
            }

            curPath.setTotalGeneNum(subPopulationSize);
            if (sigGeneInPathNum > 0 && subPopulationSize > minSize) {
               // System.out.println(curPath.getID() + " " + allGWASGeneNum + " " + subPopulationSize + " " + selectedGWASGeneNum + " " + sigGeneInPathNum);
                curPath.setEnrichedPValue(MultipleTestingMethod.hypergeometricEnrichmentTest(allGWASGeneNum, subPopulationSize, selectedGWASGeneNum, sigGeneInPathNum));
            } else if (sigGeneInPathNum == 0 && subPopulationSize > minSize) {
                curPath.setEnrichedPValue(1);
            } else {
                curPath.setEnrichedPValue(Double.NaN);
            }
        }

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
    public DoubleArrayList searchSignificantPathwaysSimesTest(Map<String, Double> genePValues,
            double presentationPathwayPThreshold, double presetationGenePThreshold, List<Pathway> pathwayList) throws Exception {
        Map<String, Pathway> searchedPathways = new HashMap<String, Pathway>();
        DoubleArrayList pValues = new DoubleArrayList();
        int geneNum;
        double pMin;
        DoubleArrayList pathwayPValues = new DoubleArrayList();
        HashSet<String> genesKept = new HashSet<String>();
        for (Map.Entry<String, Pathway> mPath : dbPathwaySet.entrySet()) {
            Pathway curPath = mPath.getValue();
            String pathID = mPath.getKey();
            pValues.clear();
            genesKept.clear();
            Set<String> pGenes = curPath.getGeneSymbols();
            Iterator<String> pGeneIter = pGenes.iterator();
            while (pGeneIter.hasNext()) {
                String pGeneSymb = pGeneIter.next();
                Double pV = genePValues.get(pGeneSymb);
                if (pV != null) {
                    //note all avaible genes are involved in the test
                    pValues.add(pV);
                    if (pV <= presetationGenePThreshold) {
                        genesKept.add(pGeneSymb);
                    }
                }
            }
            pValues.quickSort();
            //use Simes test to combine p-values
            geneNum = pValues.size();
            if (geneNum == 0) {
                continue;
            }

            Pathway newPathway = new Pathway(pathID, pathID, curPath.getURL());
            newPathway.getGeneSymbols().addAll(genesKept);

            pMin = pValues.get(0) * geneNum;
            if (geneNum > 1) {
                for (int i = 1; i < geneNum; i++) {
                    if ((pValues.getQuick(i) * geneNum / (i + 1)) < pMin) {
                        pMin = pValues.getQuick(i) * geneNum / (i + 1);
                    }
                }
            }
            newPathway.setTotalGeneNum(geneNum);
            pathwayPValues.add(pMin);
            if (pMin <= presentationPathwayPThreshold) {
                newPathway.setEnrichedPValue(pMin);
                searchedPathways.put(pathID, newPathway);
            }
        }
        pathwayList.addAll(searchedPathways.values());
        return pathwayPValues;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        int size = 100;
        int[] buff = new int[size];
        long start = System.currentTimeMillis();
        DoubleArrayList pValues = new DoubleArrayList();
        for (int i = 0; i < size; i++) {
            buff[i] = 1;
            pValues.add(1);
        }

        System.out.println(System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        for (int i = size - 1; i <= 0; i--) {
            buff[i] = 1;
        }
        System.out.println(System.currentTimeMillis() - start);
        try {
            //calcluate pvalues
            int allGeneSize = 15300;
            DoubleProcedure dp = new DoubleProcedure() {
                @Override
                public boolean apply(double a) {
                    System.out.print(a);
                    return true;
                }
            };

        } catch (Exception ex) {
        }
    }
}
