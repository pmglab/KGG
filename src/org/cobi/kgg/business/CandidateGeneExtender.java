/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.cobi.kgg.business.entity.PPIGraph;
import org.cobi.kgg.business.entity.Pathway;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.util.text.LocalFile;
import org.cobi.util.text.Util;

/**
 *
 * @author mxli
 */
public class CandidateGeneExtender {

    private Set<String> seedGeneSet;
    private Set<String> otherInterestingGeneSymbols;
    private Set<String> ppiExtendedGeneSet;
    private Set<String> pathwayExtendedGeneSet;
    private PPIGraph ppiNetwork;
    private PathwayExplorer pathExplorer;

    /**
     * 
     * @throws Exception
     */
    public CandidateGeneExtender(String pathwayDB, int minPathwayGene, int maxPathwayGene,
            String ppifileName, int gene1Index, int gene2Index) throws Exception {
        seedGeneSet = new HashSet<String>();
        otherInterestingGeneSymbols = new HashSet<String>();
        ppiExtendedGeneSet = new HashSet<String>();
        pathwayExtendedGeneSet = new HashSet<String>();
        // build ppi network
        ppiNetwork = new PPIGraph();
        ppiNetwork.readPPIItems(ppifileName,gene1Index,gene2Index);
        pathExplorer = new PathwayExplorer();
        pathExplorer.loadGSEAPathways(new File(pathwayDB),minPathwayGene, maxPathwayGene);
    }

    public String countCoverages(List<String> gwasGeneList, int ppiLevel) {
        List<String[]> forwardInteractionItems = ppiNetwork.getForwardInteractionItems();
        HashSet<String> ppiGeneSet = new HashSet<String>();
        int geneSize = forwardInteractionItems.size();
        for (int i = 0; i < geneSize; i++) {
            ppiGeneSet.add(forwardInteractionItems.get(i)[0]);
            ppiGeneSet.add(forwardInteractionItems.get(i)[1]);
        }

        HashSet<String> pathwayGeneSet = new HashSet<String>();
        Map<String, Pathway> pathways = pathExplorer.getPathwaySet();

        for (Map.Entry<String, Pathway> mPathway : pathways.entrySet()) { 
            pathwayGeneSet.addAll(mPathway.getValue().getGeneSymbols());
        }

        geneSize = gwasGeneList.size();
        double geneNumPPI = 0;
        double geneNumPathway = 0;
        double geneNumAll = 0;
        double geneNumEither = 0;
        for (int i = 0; i < geneSize; i++) {
            String geneSymb = gwasGeneList.get(i);
            if (ppiGeneSet.contains(geneSymb)) {
                geneNumPPI += 1;
            }
            if (pathwayGeneSet.contains(geneSymb)) {
                geneNumPathway += 1;
            }
            if (ppiGeneSet.contains(geneSymb) || pathwayGeneSet.contains(geneSymb)) {
                geneNumEither += 1;
            } else if (ppiGeneSet.contains(geneSymb) && pathwayGeneSet.contains(geneSymb)) {
                geneNumAll += 1;
            }
        }
        StringBuilder infor = new StringBuilder();
        infor.append("There are ").append(geneSize).append(" genes in your GWAS dataset; ");
        infor.append((int) geneNumPPI);
        infor.append("(");
        double percentage = 100 * geneNumPPI / geneSize;
        infor.append(Util.doubleToString(percentage, 3));
        infor.append("%) genes have registry in our PPI dataset; ");
        infor.append((int) geneNumPathway);
        infor.append("(");
        percentage = 100 * geneNumPathway / geneSize;
        infor.append(Util.doubleToString(percentage, 3));
        infor.append("%) genes have registry in our pathway dataset.\n    ");
        infor.append((int) geneNumEither);
        infor.append("(");
        percentage = 100 * geneNumEither / geneSize;
        infor.append(Util.doubleToString(percentage, 3));
        infor.append("%) genes have registry in either of datasets; ");
        infor.append((int) geneNumAll);
        infor.append("(");
        percentage = 100 * geneNumAll / geneSize;
        infor.append(Util.doubleToString(percentage, 3));
        infor.append("%) genes have registry in both datasets simultaneously.\n");

        geneNumPPI = 0;
        geneNumAll = 0;
        geneNumPathway = 0;
        geneNumEither = 0;
        for (int i = 0; i < geneSize; i++) {
            String geneSymb = gwasGeneList.get(i);
            if (ppiExtendedGeneSet.contains(geneSymb)) {
                geneNumPPI += 1;
            }
            if (pathwayExtendedGeneSet.contains(geneSymb)) {
                geneNumPathway += 1;
            }
            if (ppiExtendedGeneSet.contains(geneSymb) || pathwayExtendedGeneSet.contains(geneSymb)) {
                geneNumEither += 1;
            } else if (ppiExtendedGeneSet.contains(geneSymb) && pathwayExtendedGeneSet.contains(geneSymb)) {
                geneNumAll += 1;
            }
        }

        infor.append((int) geneNumPPI);
        infor.append("(");
        percentage = 100 * geneNumPPI / geneSize;
        infor.append(Util.doubleToString(percentage, 3));
        infor.append("%) genes have ").append(ppiLevel).append(" - (or less-) level funtional gene links with seed candidate genes;");
        infor.append((int) geneNumPathway);
        infor.append("(");
        percentage = 100 * geneNumPathway / geneSize;
        infor.append(Util.doubleToString(percentage, 3));
        infor.append("%) genes share same pathways with seed candidate genes.\n    ");
        infor.append((int) geneNumEither);
        infor.append("(");
        percentage = 100 * geneNumEither / geneSize;
        infor.append(Util.doubleToString(percentage, 3));
        infor.append("%) genes meet either of conditions; ");
        infor.append((int) geneNumAll);
        infor.append("(");
        percentage = 100 * geneNumAll / geneSize;
        infor.append(Util.doubleToString(percentage, 3));
        infor.append("%) genes meet both of conditions.");

        return infor.toString();
    }

    public Set<String> getSeedGeneSet() {
        return seedGeneSet;
    }

    public void setSeedGeneSet(Set<String> seedGeneSet) {
        this.seedGeneSet = seedGeneSet;
    }

    /**
     *
     * @throws Exception
     */
    public CandidateGeneExtender() throws Exception {
        seedGeneSet = new HashSet<String>();
        otherInterestingGeneSymbols = new HashSet<String>();
        ppiExtendedGeneSet = new HashSet<String>();
        pathwayExtendedGeneSet = new HashSet<String>();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public final Set<String> getAllGeneSymbs() throws Exception {
        Set<String> geneSet = new HashSet<String>();
        geneSet.addAll(seedGeneSet);
        geneSet.addAll(ppiExtendedGeneSet);
        geneSet.addAll(pathwayExtendedGeneSet);
        geneSet.addAll(otherInterestingGeneSymbols);
        return geneSet;
    }

    /**
     *
     * @param fileName
     * @throws Exception
     */
    public String readSeedGenesFromFile(final File fileName)
            throws Exception {
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
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        StringBuilder tmpBuf = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, "\t");
                // System.out.println(line);
                // source
                st.nextToken();
                tmpBuf.append(st.nextToken().trim());
                String symbol = tmpBuf.toString();
                 if (latestGenesSymbMap.containsKey(symbol)) {
                    symbol = latestGenesSymbMap.get(symbol);
                }
                tmpBuf.delete(0, tmpBuf.length());
                st.nextToken();
                st.nextToken();
                st.nextToken();
                String asSeed = st.nextToken().trim();
                if (asSeed.equals("true")) {
                    seedGeneSet.add(symbol);
                } else {
                    otherInterestingGeneSymbols.add(symbol);
                }
            }
            StringBuilder inforString = new StringBuilder();
            inforString.append(seedGeneSet.size());
            inforString.append(" seed candidate genes, ");
            inforString.append(otherInterestingGeneSymbols.size());
            inforString.append(" non-seed candidate genes are read.");

            return inforString.toString();           
        } finally {
            br.close();
        }
    }

    /**
     *
     * @throws Exception
     */
    public final void extendPathwayGenes() throws Exception {
        Map<String, Pathway> allPathways = pathExplorer.getPathwaySet();
        pathwayExtendedGeneSet.clear();
        for (Map.Entry<String, Pathway> mPathway : allPathways.entrySet()) {
            Set<String> pathwayGenes = mPathway.getValue().getGeneSymbols();
            Iterator<String> itSeed = seedGeneSet.iterator();
            while (itSeed.hasNext()) {
                if (pathwayGenes.contains(itSeed.next())) {
                    pathwayExtendedGeneSet.addAll(pathwayGenes);
                    break;
                }
            }
        }

    }

    /**
     *
     * @param level
     * @throws Exception
     */
    public final void extendPPIGenes(final int level) throws Exception {
        ppiExtendedGeneSet.clear();
        Iterator<String> itSeed = seedGeneSet.iterator();
        Graph<String, Integer> ppiGraph = new UndirectedSparseGraph<String, Integer>();

        while (itSeed.hasNext()) {
            String geneSymb = itSeed.next();
            ppiGraph.addVertex(geneSymb);
            ppiNetwork.buildGraph(ppiGraph, geneSymb, 0, level);
        }

        Collection<String> genes = ppiGraph.getVertices();
        Iterator<String> ite = genes.iterator();
        String geneSyb = null;
        while (ite.hasNext()) {
            geneSyb = ite.next();
            if (!seedGeneSet.contains(geneSyb)) {
                ppiExtendedGeneSet.add(geneSyb);
            }
        }

    }

    /**
     *
     * @param seedSet
     * @throws Exception
     */
    public final void replaceSeedGenes(final HashSet<String> seedSet)
            throws Exception {
        seedGeneSet.clear();
        seedGeneSet.addAll(seedSet);
    }
}
