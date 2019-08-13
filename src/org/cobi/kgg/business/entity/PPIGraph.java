/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import cern.colt.list.DoubleArrayList;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import net.sf.samtools.util.AsciiLineReader;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.text.LocalFile;
import org.cobi.util.text.StringArrayStringComparator;
import org.cobi.util.text.StringArrayStringFinder;
import org.apache.commons.collections15.Factory;

/**
 *
 * @author mxli
 */
public class PPIGraph {

    private List<String[]> forwardInteractionItems;
    private List<String[]> backwardInteractionItems;
    private int uniqueGeneNum = 0;

    public int getUniqueGeneNum() {
        return uniqueGeneNum;
    }

    public void setUniqueGeneNum(int uniqueGeneNum) {
        this.uniqueGeneNum = uniqueGeneNum;
    }

    public List<String[]> getBackwardInteractionItems() {
        return backwardInteractionItems;
    }

    public void setBackwardInteractionItems(List<String[]> backwardInteractionItems) {
        this.backwardInteractionItems = backwardInteractionItems;
    }

    public List<String[]> getForwardInteractionItems() {
        return forwardInteractionItems;
    }

    public void setForwardInteractionItems(List<String[]> forwardInteractionItems) {
        this.forwardInteractionItems = forwardInteractionItems;
    }

    /**
     *
     * @param startGraph
     * @param startLabel
     * @param level
     * @param totalLevel
     */
    public void buildGraph(Graph<String, Integer> startGraph, String startLabel, int level, int totalLevel) {
        int listLen = forwardInteractionItems.size();
        String[] cells = null;

        int firstOccurPos = 0;
        StringArrayStringFinder forwardFinder = new StringArrayStringFinder(0);
        StringArrayStringFinder backwardFinder = new StringArrayStringFinder(1);

        if ((level < totalLevel) && (startGraph != null)) {
            //forward search 
            firstOccurPos = Collections.binarySearch(forwardInteractionItems, startLabel, forwardFinder);
            if (firstOccurPos >= 0) {
                //make sure the index is at the start of this label
                if (firstOccurPos > 0) {
                    --firstOccurPos;
                    while (forwardInteractionItems.get(firstOccurPos)[0].equals(startLabel)) {
                        --firstOccurPos;
                        if (firstOccurPos < 0) {
                            break;
                        }
                    }
                    firstOccurPos++;
                }

                cells = forwardInteractionItems.get(firstOccurPos);
                do {
                    Integer ppiID = new Integer(cells[2]);
                    //this can remove close loop
                    if (!startGraph.containsVertex(cells[1])) {
                        startGraph.addVertex(cells[1]);
                    }
                    if (!startGraph.containsEdge(ppiID)) {
                        startGraph.addEdge(ppiID, startLabel, cells[1]);
                    }
                    firstOccurPos++;
                    if (firstOccurPos == listLen) {
                        break;
                    }
                    cells = forwardInteractionItems.get(firstOccurPos);
                } while (cells[0].equals(startLabel));
            }

            //backward search
            firstOccurPos = Collections.binarySearch(backwardInteractionItems, startLabel, backwardFinder);
            if (firstOccurPos >= 0) {
                //make sure the index is at the start of this label
                if (firstOccurPos > 0) {
                    --firstOccurPos;
                    while (backwardInteractionItems.get(firstOccurPos)[1].equals(startLabel)) {
                        --firstOccurPos;
                        if (firstOccurPos < 0) {
                            break;
                        }
                    }
                    firstOccurPos++;
                }

                cells = backwardInteractionItems.get(firstOccurPos);
                do {
                    Integer ppiID = new Integer(cells[2]);
                    if (!startGraph.containsVertex(cells[0])) {
                        startGraph.addVertex(cells[0]);
                    }
                    if (!startGraph.containsEdge(ppiID)) {
                        startGraph.addEdge(ppiID, startLabel, cells[0]);
                    }
                    firstOccurPos++;
                    if (firstOccurPos == listLen) {
                        break;
                    }
                    cells = backwardInteractionItems.get(firstOccurPos);
                } while (cells[1].equals(startLabel));
            }

            Collection<String> neighbers = startGraph.getNeighbors(startLabel);
            if (neighbers == null) {
                return;
            }
            Iterator<String> neighberIter = neighbers.iterator();
            level++;
            while (neighberIter.hasNext()) {
                buildGraph(startGraph, neighberIter.next(), level, totalLevel);
            }
        }
    }

    public void readPPIConfidenceScore(List<PPISet> ppiSetList1, File fileName, int gene1Index, int gene2Index, int scoreIndex) throws Exception {
        int size = ppiSetList1.size();
        if (size == 0) {
            return;
        }
        Set<String> invovledGeneSymbols = new HashSet<String>();
        for (int i = 0; i < size; i++) {
            invovledGeneSymbols.addAll(ppiSetList1.get(i).getGeneSymbs());
        }
        Graph<String, PPIEdge> graph = buildGraphWithConfidenceScore(invovledGeneSymbols, fileName, gene1Index, gene2Index, scoreIndex);
        for (int i = 0; i < size; i++) {
            PPISet ppi = ppiSetList1.get(i);
            List<String> symbs = ppi.getGeneSymbs();
            PPIEdge edgi = graph.findEdge(symbs.get(0), symbs.get(1));
            if (edgi != null) {
                ppi.setConfidenceScore(edgi.getScore() / 1000);
            }
        }
        graph = null;
    }

    public void readPPIConfidenceScore(List<PPISet> ppiSetList1, List<String[]> interPairs) throws Exception {
        int size = ppiSetList1.size();
        if (size == 0) {
            return;
        }

        Graph<String, PPIEdge> graph = new UndirectedSparseGraph<String, PPIEdge>();
        int listLen = interPairs.size();
        String[] cells = null;
        //initiate the edge vetic otherwise it  will throw exception

        for (int i = 0; i < listLen; i++) {
            cells = interPairs.get(i);
            graph.addVertex(cells[0]);
            graph.addVertex(cells[1]);
            graph.addEdge(new PPIEdge(i, Double.parseDouble(cells[2])), cells[0], cells[1]);
        }

        for (int i = 0; i < size; i++) {
            PPISet ppi = ppiSetList1.get(i);
            List<String> symbs = ppi.getGeneSymbs();
            PPIEdge edgi = graph.findEdge(symbs.get(0), symbs.get(1));
            if (edgi != null) {
                ppi.setConfidenceScore(edgi.getScore());
            }
        }
        graph = null;
    }

    public Graph<String, Integer> buildGraph(boolean toPermute, String fileName, int gene1Index, int gene2Index, int scoreIndex, int minScore) throws Exception {
        forwardInteractionItems = new ArrayList<String[]>();
        //read PPI genes

        File fileFolder = new File(fileName);
        if (!fileFolder.exists()) {
            throw new Exception("File " + fileName + " doese not exists!");
        }

        int[] indices = new int[3];
        indices[0] = gene1Index;
        indices[1] = gene2Index;
        indices[2] = scoreIndex;
        List<String[]> tmpArrays = new ArrayList<String[]>();
        LocalFile.retrieveData(fileName, tmpArrays, indices, "\t");
        forwardInteractionItems = new ArrayList<String[]>();
        Set<String> uniuqeGenes = new HashSet<String>();
        int ppiSize = tmpArrays.size();

        for (int i = 0; i < ppiSize; i++) {
            String[] items = new String[3];
            items[0] = tmpArrays.get(i)[0];
            items[1] = tmpArrays.get(i)[1];
            if (items[0].startsWith("UNKNOWN") || items[1].startsWith("UNKNOWN")) {
                continue;
            }
            uniuqeGenes.add(items[0]);
            uniuqeGenes.add(items[1]);
            //remove self loop
            if (items[0].equals(items[1])) {
                continue;
            }
            items[2] = String.valueOf(i);
            forwardInteractionItems.add(items);
        }
        uniqueGeneNum = uniuqeGenes.size();
        uniuqeGenes.clear();
        //for a quick test only
        if (toPermute) {
            permutePPI(forwardInteractionItems);
        }

        Graph<String, Integer> graph = new UndirectedSparseGraph<String, Integer>();

        int listLen = forwardInteractionItems.size();
        String[] cells = null;
        int edgeID = 0;
        //initiate the edge vetic otherwise it  will throw exception
        cells = forwardInteractionItems.get(0);

        for (int i = 0; i < listLen; i++) {
            cells = forwardInteractionItems.get(i);
            graph.addVertex(cells[0]);
            graph.addVertex(cells[1]);
            graph.addEdge(i, cells[0], cells[1]);
        }

        forwardInteractionItems.clear();

        return graph;
    }

    public List<String[]> readPPIPairs(Set<String> selectedGenes, File fileFolder,
            int gene1Index, int gene2Index, int socreInde, double minConfiScore, StringBuilder infors) throws Exception {
        //read PPI genes         
        if (!fileFolder.exists()) {
            throw new Exception("File " + fileFolder.getCanonicalPath() + " doese not exists!");
        }
        List<String[]> readPairs = new ArrayList<String[]>();

        Set<String> uniuqeGenes = new HashSet<String>();

        LineReader br = null;
        if (fileFolder.getName().endsWith(".zip") || fileFolder.getName().endsWith(".gz") || fileFolder.getName().endsWith(".tar.gz")) {
            br = new CompressedFileReader(fileFolder);
        } else {
            br = new AsciiLineReader(fileFolder);
        }

        String line = null;
        String delmilit = "\t\" \"\n";

        int colID = -1;
        String[] items = null;
        int arrayIndex = 0;
        int maxIndex = Math.max(gene1Index, gene2Index);
        maxIndex = Math.max(maxIndex, socreInde);
        StringBuilder tmpStr = new StringBuilder();

        double score = 0;
        String[] scoreStrs;
        int passNum = 0;
        double maxScore = 0;
        while ((line = br.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            colID = 0;

            StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
            items = new String[3];

            while (tokenizer.hasMoreTokens()) {
                tmpStr.append(tokenizer.nextToken().trim());
                if (colID == gene1Index) {
                    items[0] = tmpStr.toString();

                } else if (colID == gene2Index) {
                    items[1] = tmpStr.toString();

                } else if (colID == socreInde) {
                    items[2] = tmpStr.toString();

                }
                colID++;
                tmpStr.delete(0, tmpStr.length());
                if (colID > maxIndex) {
                    break;
                }
            }

            if (items[0] == null || items[1] == null || items[0].startsWith("UNKNOWN") || items[1].startsWith("UNKNOWN")
                    || !selectedGenes.contains(items[0]) || !selectedGenes.contains(items[1])) {
                continue;
            }
            score = 0;
            scoreStrs = items[2].split(",");
            passNum = 0;
            maxScore = 0;
            for (String conf : scoreStrs) {
                //if (org.cobi.util.text.Util.isNumeric(items[2])) 
                {
                    score = Double.parseDouble(conf);
                    //this is the default scale in STRING database
                    if (score > 1) {
                        score = score / 1000;
                    }
                    if (maxScore < score) {
                        maxScore = score;
                    }
                    if (Math.abs(score) >= minConfiScore) {
                        passNum++;
                    }
                }
            }

            if (passNum == 0) {
                continue;
            }
            if (items[0].equals(items[1])) {
                continue;
            }
            uniuqeGenes.add(items[0]);
            uniuqeGenes.add(items[1]);
            //remove self loop

            items[2] = String.valueOf(maxScore);
            readPairs.add(items);
        }
        br.close();

        String info = "In the " + fileFolder.getCanonicalPath() + ", there are " + uniuqeGenes.size() + " uniuqe genes and " + readPairs.size() + " pairs.";
        infors.append(info);
        uniuqeGenes.clear();
        return readPairs;
    }

    public List<String[]> generateRandomInteractions(List<String> backGroundGenes, int netSize, Map<String, String> latestGenesSymbMap) throws Exception {
        List<String[]> allPPIPairs = new ArrayList<String[]>();
        int count = 0;
        int geneSize = backGroundGenes.size();
        Set<String> ppiSet = new HashSet<String>();
        while (count < netSize) {
            Collections.shuffle(backGroundGenes);
            for (int i = 0; i < geneSize; i += 2) {
                String[] pair = new String[3];
                if (backGroundGenes.get(i).compareTo(backGroundGenes.get(i + 1)) > 0) {
                    pair[0] = backGroundGenes.get(i + 1);
                    pair[1] = backGroundGenes.get(i);
                } else {
                    pair[0] = backGroundGenes.get(i);
                    pair[1] = backGroundGenes.get(i + 1);
                }
                if (latestGenesSymbMap.containsKey(pair[0])) {
                    pair[0] = latestGenesSymbMap.get(pair[0]);
                }
                if (latestGenesSymbMap.containsKey(pair[1])) {
                    pair[1] = latestGenesSymbMap.get(pair[1]);
                }
                if (pair[0].compareTo(pair[1]) == 0) {
                    continue;
                }
                pair[2] = "0.9";
                String item = pair[0] + ":::" + pair[1];
                if (!ppiSet.contains(item)) {
                    ppiSet.add(item);
                    allPPIPairs.add(pair);
                    count++;
                    if (count >= netSize) {
                        break;
                    }
                }
            }
        }
        // System.out.print("The number of unique pairs :" + allPPIPairs.size());

        ppiSet.clear();
        return allPPIPairs;
    }

    public Graph<String, PPIEdge> buildGraph(List<String[]> items, boolean toPermute, StringBuilder infor) throws Exception {
        //for a quick test only
        if (toPermute) {
            permutePPI(items);
        }

        Graph<String, PPIEdge> graph = new UndirectedSparseGraph<String, PPIEdge>();

        int listLen = items.size();
        String[] cells = null;
        for (int i = 0; i < listLen; i++) {
            cells = items.get(i);
            graph.addVertex(cells[0]);
            graph.addVertex(cells[1]);
            graph.addEdge(new PPIEdge(i, Double.parseDouble(cells[2])), cells[0], cells[1]);
        }

        String info = "The gene network has " + graph.getVertexCount() + " uniuqe vertexes and " + graph.getEdgeCount(EdgeType.UNDIRECTED) + " uniuqe edges.";
        infor.append(info);
        infor.append("\n");
        return graph;
    }

    public Graph<String, PPIEdge> buildGraphWithConfidenceScore(Set<String> selectedGenes,
            File fileFolder, int gene1Index, int gene2Index, int scoreIndex) throws Exception {
        forwardInteractionItems = new ArrayList<String[]>();
        //read PPI genes

        if (!fileFolder.exists()) {
            throw new Exception("File " + fileFolder.getCanonicalPath() + " doese not exists!");
        }

        Set<Integer> indices = new HashSet<Integer>();
        indices.add(gene1Index);
        indices.add(gene2Index);
        indices.add(scoreIndex);

        List<String[]> tmpArrays = new ArrayList<String[]>();
        LocalFile.retrieveData(fileFolder, tmpArrays, null, indices);
        forwardInteractionItems = new ArrayList<String[]>();

        int ppiSize = tmpArrays.size();

        for (int i = 0; i < ppiSize; i++) {
            String[] items = new String[3];
            items[0] = tmpArrays.get(i)[0];
            items[1] = tmpArrays.get(i)[1];
            if (items[0].startsWith("UNKNOWN") || items[1].startsWith("UNKNOWN")
                    || !selectedGenes.contains(items[0]) || !selectedGenes.contains(items[1])) {
                continue;
            }

            //remove self loop
            if (items[0].equals(items[1])) {
                continue;
            }
            items[2] = tmpArrays.get(i)[2];
            forwardInteractionItems.add(items);
        }

        Graph<String, PPIEdge> graph = new UndirectedSparseGraph<String, PPIEdge>();

        int listLen = forwardInteractionItems.size();
        String[] cells = null;
        //initiate the edge vetic otherwise it  will throw exception
        cells = forwardInteractionItems.get(0);

        for (int i = 0; i < listLen; i++) {
            cells = forwardInteractionItems.get(i);
            graph.addVertex(cells[0]);
            graph.addVertex(cells[1]);
            graph.addEdge(new PPIEdge(i, Double.parseDouble(cells[2])), cells[0], cells[1]);
        }
        forwardInteractionItems.clear();
        return graph;
    }

    public void constructPPITree(DefaultMutableTreeNode parentNode, Map<String, Double> genePValueMap,
            int geneSelectionMethodID, double pValueThreshold, int multipleTestnum) throws Exception {
        String nodeLabel;
        int listLen = forwardInteractionItems.size();
        String[] cells = null;

        int occurIndex = 0;
        int num = 0;
        TreeNode[] pathes = null;
        boolean hasContained = false;
        StringArrayStringFinder forwardFinder = new StringArrayStringFinder(0);
        StringArrayStringFinder backwardFinder = new StringArrayStringFinder(1);
        double adjustedPValue = 1;
        int itemNum;
        DoubleArrayList pValues = new DoubleArrayList();
        List<String> symbolList = new ArrayList<String>();

        if (parentNode != null) {
            pathes = parentNode.getPath();
            num = pathes.length - 1;
            //forward search
            nodeLabel = (String) parentNode.getUserObject();
            pValues.clear();
            symbolList.clear();

            occurIndex = Collections.binarySearch(forwardInteractionItems, nodeLabel, forwardFinder);
            if (occurIndex >= 0) {
                //make sure the index is at the start of this label
                if (occurIndex > 0) {
                    --occurIndex;
                    while (occurIndex >= 0 && forwardInteractionItems.get(occurIndex)[0].equals(nodeLabel)) {
                        --occurIndex;
                    }
                    occurIndex++;
                }

                cells = forwardInteractionItems.get(occurIndex);
                do {
                    hasContained = false;
                    for (int j = 0; j < num; j++) {
                        if (cells[1].equals(pathes[j].toString())) {
                            hasContained = true;
                            break;
                        }
                    }
                    //this can remove close loop
                    if (!hasContained) {
                        if (genePValueMap.get(cells[1]) != null) {
                            pValues.add(genePValueMap.get(cells[1]));
                            symbolList.add(cells[1]);
                        }
                    }
                    occurIndex++;
                    if (occurIndex == listLen) {
                        break;
                    }
                    cells = forwardInteractionItems.get(occurIndex);
                } while (cells[0].equals(nodeLabel));
            }

            //backward search
            occurIndex = Collections.binarySearch(backwardInteractionItems, nodeLabel, backwardFinder);
            if (occurIndex >= 0) {
                //make sure the index is at the start of this label
                if (occurIndex > 0) {
                    --occurIndex;
                    while (occurIndex >= 0 && backwardInteractionItems.get(occurIndex)[1].equals(nodeLabel)) {
                        --occurIndex;
                    }
                    occurIndex++;
                }

                cells = backwardInteractionItems.get(occurIndex);
                do {
                    hasContained = false;
                    for (int j = 0; j < num; j++) {
                        if (cells[0].equals(pathes[j].toString())) {
                            hasContained = true;
                            break;
                        }
                    }

                    //this can remove close loop
                    if (!hasContained) {
                        if (genePValueMap.get(cells[0]) != null) {
                            pValues.add(genePValueMap.get(cells[0]));
                            symbolList.add(cells[0]);
                        }
                    }
                    occurIndex++;
                    if (occurIndex == listLen) {
                        break;
                    }
                    cells = backwardInteractionItems.get(occurIndex);
                } while (cells[1].equals(nodeLabel));
            }
            itemNum = pValues.size();
            if (geneSelectionMethodID != 7) {
                switch (geneSelectionMethodID) {
                    case 0:
                        // Benjamini & Hochberg, 1995 test
                        adjustedPValue = MultipleTestingMethod.BenjaminiHochbergFDR("PPI", pValueThreshold, pValues);
                        //adjustedPValue = MultipleTestingMethod.BenjaminiHochbergFDR(pValueThreshold / multipleTestnum, pValues);
                        break;
                    case 1:
                        //adjustedPValue = pValueThreshold / (itemNum);
                        adjustedPValue = pValueThreshold / (itemNum * multipleTestnum);
                        break;
                    case 2:
                        //Adaptive Benjamini 2006 test
                        adjustedPValue = MultipleTestingMethod.BenjaminiYekutieliFDR("PPI", pValueThreshold, pValues);
                        //adjustedPValue = MultipleTestingMethod.BenjaminiYekutieliFDR(pValueThreshold / multipleTestnum, pValues);
                        break;
                    case 3:
                        // Storey, 2002 test
                        adjustedPValue = MultipleTestingMethod.storeyFDRTest("PPI", pValueThreshold, pValues);
                        //adjustedPValue = MultipleTestingMethod.storeyFDRTest(pValueThreshold / multipleTestnum, pValues);
                        break;
                    default:
                        break;
                }
            } else {
                adjustedPValue = pValueThreshold;
            }
            for (int i = 0; i < itemNum; i++) {
                if (genePValueMap.get(symbolList.get(i)) <= adjustedPValue) {
                    parentNode.add(new DefaultMutableTreeNode(symbolList.get(i)));
                }
            }

            //the expansion can inflate type 1 error because of multiple testing issue
            /*
             num = parentNode.getChildCount();
             for (int i = 0; i < num; i++) {
             constructPPITree((DefaultMutableTreeNode) parentNode.getChildAt(i), genePValueMap, geneSelectionMethodID, pValueThreshold, multipleTestnum);
             }
             *
             */
        }
    }

    /**
     * Construct a n-depth network based-on PPI pairs by recursive algorithm
     *
     * @param parentNode the parent node
     * @param curLevel the current network level
     * @param maxLevel the maximal network level
     */
    public void constructPPITree(DefaultMutableTreeNode parentNode, int curLevel, int maxLevel) {
        String nodeLabel;
        int listLen = forwardInteractionItems.size();
        String[] cells = null;

        int occurIndex = 0;
        int num = 0;
        TreeNode[] pathes = null;
        boolean hasContained = false;
        StringArrayStringFinder forwardFinder = new StringArrayStringFinder(0);
        StringArrayStringFinder backwardFinder = new StringArrayStringFinder(1);
        if ((curLevel < maxLevel) && (parentNode != null)) {
            pathes = parentNode.getPath();
            num = pathes.length - 1;
            //forward search
            nodeLabel = (String) parentNode.getUserObject();

            occurIndex = Collections.binarySearch(forwardInteractionItems, nodeLabel, forwardFinder);
            if (occurIndex >= 0) {
                //make sure the index is at the start of this label
                if (occurIndex > 0) {
                    --occurIndex;
                    while (occurIndex >= 0 && forwardInteractionItems.get(occurIndex)[0].equals(nodeLabel)) {
                        --occurIndex;
                    }
                    occurIndex++;
                }
                cells = forwardInteractionItems.get(occurIndex);
                do {
                    hasContained = false;
                    for (int j = 0; j < num; j++) {
                        if (cells[1].equals(pathes[j].toString())) {
                            hasContained = true;
                            break;
                        }
                    }
                    //this can remove close loop
                    if (!hasContained) {
                        parentNode.add(new DefaultMutableTreeNode(cells[1]));
                    }
                    occurIndex++;
                    if (occurIndex == listLen) {
                        break;
                    }
                    cells = forwardInteractionItems.get(occurIndex);
                } while (cells[0].equals(nodeLabel));
            }

            //backward search
            occurIndex = Collections.binarySearch(backwardInteractionItems, nodeLabel, backwardFinder);
            if (occurIndex >= 0) {
                //make sure the index is at the start of this label
                if (occurIndex > 0) {
                    --occurIndex;
                    while (occurIndex >= 0 && backwardInteractionItems.get(occurIndex)[1].equals(nodeLabel)) {
                        --occurIndex;
                    }
                    occurIndex++;
                }

                cells = backwardInteractionItems.get(occurIndex);
                do {
                    hasContained = false;
                    for (int j = 0; j < num; j++) {
                        if (cells[0].equals(pathes[j].toString())) {
                            hasContained = true;
                            break;
                        }
                    }
                    //this can remove close loop
                    if (!hasContained) {
                        parentNode.add(new DefaultMutableTreeNode(cells[0]));
                    }
                    occurIndex++;
                    if (occurIndex == listLen) {
                        break;
                    }
                    cells = backwardInteractionItems.get(occurIndex);
                } while (cells[1].equals(nodeLabel));
            }
            num = parentNode.getChildCount();
            curLevel++;
            for (int i = 0; i < num; i++) {
                constructPPITree((DefaultMutableTreeNode) parentNode.getChildAt(i), curLevel, maxLevel);
            }
        }
    }

    /**
     * Trim network by seed candidate gene through recursive algorithm
     *
     * @param currentTreeNode the current node to trim
     * @param involvedGenes the seed candidate gene set
     */
    public void trimPPITree(DefaultMutableTreeNode currentTreeNode, HashSet<String> candidateGenes) throws Exception {
        String currentGene = (String) currentTreeNode.getUserObject();
        if (currentTreeNode.getChildCount() == 0) {
            //if the current node is not in the candidate gene set, then remove it
            if (!candidateGenes.contains(currentGene)) {
                currentTreeNode.removeFromParent();
            }
        } else {
            // after removed, the index will be changed so we have to access the node in reverse order
            for (int i = currentTreeNode.getChildCount() - 1; i >= 0; i--) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) currentTreeNode.getChildAt(i);
                trimPPITree(childNode, candidateGenes);
            }
            if (currentTreeNode.getChildCount() == 0) {
                if (!candidateGenes.contains(currentGene)) {
                    currentTreeNode.removeFromParent();
                }
            }
        }
    }

    public void savePPITreeAsXML(DefaultMutableTreeNode treeRoot, String filePath) throws Exception {
        int spaceNum = 2;
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
        savePPITree2XMLFile(treeRoot, spaceNum, bw);
        bw.close();

    }

    public void savePPITree2XMLFile(DefaultMutableTreeNode treeRoot, int spaceNum, BufferedWriter fw) throws Exception {

        spaceNum += 2;
        String nodeName = (String) treeRoot.getUserObject();
        if (treeRoot.isLeaf()) {
            for (int i = 0; i < spaceNum; i++) {
                fw.write(" ");
            }
            fw.write("<Gene name=\"" + nodeName + "\"/>\n");
        } else {
            for (int i = 0; i < spaceNum; i++) {
                fw.write(" ");
            }
            fw.write("<Gene name=\"" + nodeName + "\">\n");
            for (int i = 0; i < treeRoot.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
                savePPITree2XMLFile(childNode, spaceNum, fw);
            }
            for (int i = 0; i < spaceNum; i++) {
                fw.write(" ");
            }
            fw.write("</Gene>\n");
        }
    }

    /**
     *
     * @throws Exception
     */
    public void readPPIItems(String fileName, int gene1Index, int gene2Index) throws Exception {

        forwardInteractionItems = new ArrayList<String[]>();
        backwardInteractionItems = new ArrayList<String[]>();
        //read PPI genes

        File fileFolder = new File(fileName);
        if (!fileFolder.exists()) {
            throw new Exception("File " + fileName + " doese not exists!");
        }

        int[] indices = new int[2];
        indices[0] = gene1Index;
        indices[1] = gene2Index;
        List<String[]> tmpArrays = new ArrayList<String[]>();
        LocalFile.retrieveData(fileName, tmpArrays, indices, "\t");
        forwardInteractionItems = new ArrayList<String[]>();
        Set<String> uniuqeGenes = new HashSet<String>();
        int ppiSize = tmpArrays.size();
        for (int i = 0; i < ppiSize; i++) {
            String[] items = new String[3];
            items[0] = tmpArrays.get(i)[0];
            items[1] = tmpArrays.get(i)[1];
            uniuqeGenes.add(items[0]);
            uniuqeGenes.add(items[1]);
            //remove self loop
            if (items[0].equals(items[1])) {
                continue;
            }
            items[2] = String.valueOf(i);
            forwardInteractionItems.add(items);
        }
        uniqueGeneNum = uniuqeGenes.size();
        uniuqeGenes.clear();
        // System.out.println("Unique Gene Number: "+uniqueGeneNum); 
        backwardInteractionItems = new ArrayList<String[]>();
        backwardInteractionItems.addAll(forwardInteractionItems);

        Collections.sort(forwardInteractionItems, new StringArrayStringComparator(0));
        Collections.sort(backwardInteractionItems, new StringArrayStringComparator(1));
    }

    /**
     *
     * @param treeRoot
     * @param rootName
     * @param endNodes
     * @param startGraph
     * @throws Exception
     */
    public void convertGraph(DefaultMutableTreeNode treeRoot, String rootName, Set<String> endNodes, Graph<String, Integer> startGraph) throws Exception {
        // after removed, the index will be changed so we have to access the node in reverse order
        if (treeRoot.getChildCount() == 0) {
            endNodes.add(rootName);
        }
        for (int i = treeRoot.getChildCount() - 1; i >= 0; i--) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
            String currentGene2 = childNode.toString();

            if (!startGraph.containsVertex(currentGene2)) {
                startGraph.addVertex(currentGene2);
            }

            startGraph.addEdge(edgeFactory.create(), rootName, currentGene2);

            if (childNode.getChildCount() > 0) {
                convertGraph(childNode, currentGene2, endNodes, startGraph);
            } else {
                endNodes.add(currentGene2);
            }
        }
    }

    /**
     *
     * @param treeRoot
     * @param rootName
     * @param endNodes
     * @param startGraph
     * @throws Exception
     */
    public void convertGraph(DefaultMutableTreeNode treeRoot, String rootName, Graph<String, Integer> startGraph) throws Exception {

        for (int i = treeRoot.getChildCount() - 1; i >= 0; i--) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
            String currentGene2 = childNode.toString();

            if (!startGraph.containsVertex(currentGene2)) {
                startGraph.addVertex(currentGene2);
            }

            startGraph.addEdge(edgeFactory.create(), rootName, currentGene2);

            if (childNode.getChildCount() > 0) {
                convertGraph(childNode, currentGene2, startGraph);
            }
        }
    }

    public void convertTree2ArrayList(DefaultMutableTreeNode treeRoot, List<String> list) throws Exception {
        String currentGene1 = (String) treeRoot.getUserObject();
        // after removed, the index will be changed so we have to access the node in reverse order
        for (int i = treeRoot.getChildCount() - 1; i >= 0; i--) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
            String currentGene2 = (String) childNode.getUserObject();
            list.add(currentGene1);
            list.add(currentGene2);
            if (childNode.getChildCount() > 0) {
                convertTree2ArrayList(childNode, list);
            }
        }
    }

    public void getAllLeaves(DefaultMutableTreeNode treeRoot, HashSet<String> genes) throws Exception {
        String currentGene = (String) treeRoot.getUserObject();
        if (treeRoot.getChildCount() == 0) {
            genes.add(currentGene);
        } else {
            // after removed, the index will be changed so we have to access the node in reverse order
            for (int i = treeRoot.getChildCount() - 1; i >= 0; i--) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
                getAllLeaves(childNode, genes);
            }

            if (treeRoot.getChildCount() == 0) {
                genes.add(currentGene);
            }
        }
    }

    public void permutePPI(List<String[]> interactionItems) {
        int listLen = interactionItems.size();
        int N = listLen - 1;
        int s;
        int allowedRepeatTime = 1000;
        int r = 0;
        RandomEngine twister = new MersenneTwister(new java.util.Date());
        Uniform unifor = new Uniform(twister);
        String geneSymb = null;
        Set<String> avaiblePPIs = new HashSet<String>();
        String tmpPPI1, tmpPPI2, tmpPPI3, tmpPPI4;
        int noChangedInteraction = 0;
        for (int i = 0; i < listLen; i++) {
            s = i + unifor.nextIntFromTo(0, N - i);   // between i and N-1
            tmpPPI1 = interactionItems.get(i)[0] + "&&" + interactionItems.get(s)[1];
            tmpPPI2 = interactionItems.get(s)[1] + "&&" + interactionItems.get(i)[0];
            tmpPPI3 = interactionItems.get(i)[1] + "&&" + interactionItems.get(s)[0];
            tmpPPI4 = interactionItems.get(s)[0] + "&&" + interactionItems.get(i)[1];
            r = 0;
            while (interactionItems.get(i)[0].equals(interactionItems.get(s)[1])
                    || avaiblePPIs.contains(tmpPPI1) || avaiblePPIs.contains(tmpPPI2)
                    || avaiblePPIs.contains(tmpPPI3) || avaiblePPIs.contains(tmpPPI4)) {
                s = i + unifor.nextIntFromTo(0, N - i);   // between i and N-1
                tmpPPI1 = interactionItems.get(i)[0] + "&&" + interactionItems.get(s)[1];
                tmpPPI2 = interactionItems.get(s)[1] + "&&" + interactionItems.get(i)[0];
                tmpPPI3 = interactionItems.get(i)[1] + "&&" + interactionItems.get(s)[0];
                tmpPPI4 = interactionItems.get(s)[0] + "&&" + interactionItems.get(i)[1];
                r++;
                if (r > allowedRepeatTime) {
                    break;
                }
            }

            if (r > allowedRepeatTime) {
                noChangedInteraction++;
                continue;
            }
            geneSymb = interactionItems.get(i)[1];
            interactionItems.get(i)[1] = interactionItems.get(s)[1];
            interactionItems.get(s)[1] = geneSymb;
            avaiblePPIs.add(tmpPPI1);
            avaiblePPIs.add(tmpPPI2);
            avaiblePPIs.add(tmpPPI3);
            avaiblePPIs.add(tmpPPI4);
        }
        avaiblePPIs.clear();
        System.out.println(noChangedInteraction + " interactions are not randomized!");
    }

    public double[] evaluateTree(DefaultMutableTreeNode[] pareNode, int permuTime) throws Exception {
        int listLen = forwardInteractionItems.size();
        int treeNum = pareNode.length;

        int startIndex, endIndex;
        ArrayList<ArrayList<String>> interactList = new ArrayList<ArrayList<String>>();
        ArrayList<String[]> tmpItemList = new ArrayList<String[]>();
        double[] pValues = new double[treeNum];
        Arrays.fill(pValues, 0.0);

        RandomEngine twister = new MersenneTwister(new java.util.Date());
        Uniform unifor = new Uniform(twister);
        String[] allGenes = new String[listLen * 2];
        String[] tmpAllGenes = new String[listLen * 2];
        int N = allGenes.length - 1;
        int r;
        String swap;
        StringArrayStringFinder finder = new StringArrayStringFinder(0);

        // convert ppi pairs into arrays
        for (int i = 0; i < listLen; i++) {
            tmpAllGenes[i * 2] = forwardInteractionItems.get(i)[0];
            tmpAllGenes[i * 2 + 1] = forwardInteractionItems.get(i)[1];
        }
        // convert trees into arraylists
        for (int i = 0; i < treeNum; i++) {
            ArrayList<String> inters = new ArrayList<String>();
            if (pareNode[i] != null) {
                convertTree2ArrayList(pareNode[i], inters);
                interactList.add(inters);
            }
        }

        for (int n = 0; n < permuTime; n++) {
            tmpItemList.clear();
            //shuffle connections
            allGenes = Arrays.copyOf(tmpAllGenes, listLen * 2);

            for (int i = 0; i <= N; i++) {
                r = i + unifor.nextIntFromTo(0, N - i);   // between i and N-1
                //exclude self connection

                if ((i % 2 == 1) && (!allGenes[r].equals(allGenes[i - 1]))) {
                    swap = allGenes[i];
                    allGenes[i] = allGenes[r];
                    allGenes[r] = swap;
                }
            }

            //store the ppi into an arrayList for sorting and binnary searching
            for (int i = 0; i < listLen; i++) {
                if (allGenes[i * 2].compareTo(allGenes[i * 2 + 1]) > 0) {
                    tmpItemList.add(new String[]{allGenes[i * 2 + 1], allGenes[i * 2]});
                } else {
                    tmpItemList.add(new String[]{allGenes[i * 2], allGenes[i * 2 + 1]});
                }
            }
            Collections.sort(tmpItemList, new StringArrayStringComparator(0));

            for (int i = 0; i < treeNum; i++) {
                ArrayList<String> tmpNet = interactList.get(i);
                int tmpNetSize = tmpNet.size();
                if (tmpNetSize == 1) {
                    pValues[i] = pValues[i] + 1;
                    continue;
                }
                boolean hasFound = false;

                for (int j = 0; j < tmpNetSize; j += 2) {
                    String item1 = tmpNet.get(j);
                    String item2 = tmpNet.get(j + 1);
                    if (item1.compareTo(item2) > 0) {
                        int pos = Collections.binarySearch(tmpItemList, item2, finder);

                        if (pos >= 0) {
                            startIndex = pos - 1;
                            while ((startIndex >= 0) && (tmpItemList.get(startIndex)[0].equals(item2))) {
                                startIndex--;
                            }
                            startIndex++;

                            endIndex = pos + 1;
                            while ((endIndex < listLen) && (tmpItemList.get(endIndex)[0].equals(item2))) {
                                endIndex++;
                            }
                            endIndex--;
                            hasFound = false;
                            for (int k = startIndex; k <= endIndex; k++) {
                                if (tmpItemList.get(k)[1].equals(item1)) {
                                    hasFound = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        int pos = Collections.binarySearch(tmpItemList, item1, finder);

                        if (pos >= 0) {
                            startIndex = pos - 1;
                            while ((startIndex >= 0) && (tmpItemList.get(startIndex)[0].equals(item1))) {
                                startIndex--;
                            }
                            startIndex++;

                            endIndex = pos + 1;
                            while ((endIndex < listLen) && (tmpItemList.get(endIndex)[0].equals(item1))) {
                                endIndex++;
                            }
                            endIndex--;
                            hasFound = false;
                            for (int k = startIndex; k <= endIndex; k++) {
                                if (tmpItemList.get(k)[1].equals(item2)) {
                                    hasFound = true;
                                    break;
                                }
                            }
                        }
                    }
                    // there are must be at least one interaction not existing at the network
                    if (!hasFound) {
                        break;
                    }
                }
                if (hasFound) {
                    pValues[i] = pValues[i] + 1;
                }
            }
        }

        for (int i = 0; i < treeNum; i++) {
            pValues[i] /= permuTime;
        }
        return pValues;
    }
    Factory<Integer> edgeFactory = new Factory<Integer>() {

        int i = 0;

        @Override
        public Integer create() {
            return Integer.valueOf(i++);
        }
    };
}
