/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.math;

import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author mxli
 */
public class MarixDensity {

    public double arrRho[];

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        double threshold = 0.05;
        int columnNum = 25;
        double scale = 10;
        double pairThreshold = 0.3;
        int maxBlockSize = 200;

        // List<int[]> sss=new ArrayList<int[]>();
        // ArrayList<Integer[]> blockCluster = new
        // MarixDensity().getCluster(threshold, columnNum, scale,
        // pairThreshold);
        DoubleMatrix2D ldRMatrix = new DenseDoubleMatrix2D(columnNum, columnNum);

        // -------------------- put your file here ----------------------
        BufferedReader br = new BufferedReader(new FileReader(new File("E:\\home\\mxli\\MyJava\\GenetSimulator\\corr1.txt")));
        String string;
        int row = 0;
        while (br.ready()) {
            string = br.readLine();
            String[] cells = string.trim().split("\\s+");
            ldRMatrix.set(row, row, 1);
            for (int i = 0; i < cells.length; i++) {
                ldRMatrix.set(row, i, Float.parseFloat(cells[i]));
                ldRMatrix.set(i, row, Float.parseFloat(cells[i]));
            }
            row += 1;
        }
        List<IntArrayList> blockCluster = new MarixDensity().getCluster(ldRMatrix, threshold, maxBlockSize, scale, pairThreshold);
    }

    public List<IntArrayList> getCluster(DoubleMatrix2D ldRMatrix, double threshold, int maxBlockSize, double scale, double pairThreshold) {

        // -------------------- put your file here ----------------------
        long startTime = System.currentTimeMillis();
        int columnNum = ldRMatrix.columns();
        double[] arrRho1 = new double[columnNum];
        int[] arrNeighborSize = new int[columnNum];
        int[] arrIndex = new int[columnNum];
        int[] arrIndexTmp = new int[columnNum];
        double k = 0.001;
        double[] sigma = new double[columnNum];

        for (int s = 0; s < columnNum; s++) {
            arrNeighborSize[s] = -1;
            for (int i = 0; i < columnNum; i++) {
                double tmp = ldRMatrix.getQuick(s, i);
                if (tmp >= threshold) {
                    arrRho1[s] += tmp;
                    arrNeighborSize[s] += 1;
                }
            }
            arrIndex[s] = s;
        }

        this.arrRho = arrRho1;

        quickSort(arrIndex, 0, arrIndex.length - 1);

        for (int i = 0; i < sigma.length; i++) {
            if (i == 0) {
                for (int j = 0; j < columnNum; j++) {
                    arrIndexTmp[j] = j;
                }
                this.arrRho = new double[columnNum];
                for (int j = 0; j < ldRMatrix.columns(); j++) {
                    this.arrRho[j] = ldRMatrix.getQuick(arrIndex[i], j);
                }
                quickSort(arrIndexTmp, 0, arrIndex.length - 1);
                sigma[arrIndex[i]] = ldRMatrix.getQuick(arrIndex[i], arrIndexTmp[arrIndexTmp.length - 1]) + k;
            } else if (i == 1) {
                sigma[arrIndex[i]] = ldRMatrix.getQuick(arrIndex[i], arrIndex[i - 1]) + k;
            } else {
                this.arrRho = new double[i];
                arrIndexTmp = new int[i];
                for (int j = 0; j < i; j++) {
                    this.arrRho[j] = ldRMatrix.getQuick(arrIndex[i], arrIndex[j]);
                    arrIndexTmp[j] = j;
                }
                quickSort(arrIndexTmp, 0, i - 1);
                sigma[arrIndex[i]] = ldRMatrix.getQuick(arrIndex[i], arrIndex[arrIndexTmp[0]]) + k;
            }
        }
        double[] combineCriteria = new double[sigma.length];

        int[] combineCriteriaFromLargestToSmallestIndex = new int[sigma.length];
        for (int i = 0; i < sigma.length; i++) {
            combineCriteria[i] = arrRho1[i] / arrRho1[arrIndex[0]] * k / sigma[i];
            combineCriteriaFromLargestToSmallestIndex[i] = i;
            // System.out.println(arrRho1[s] + "\t" + k / sigma[s] + "\t" +
            // arrNeighborSize[s] + "\t" + (s + 1));
        }
        this.arrRho = combineCriteria;
        quickSort(combineCriteriaFromLargestToSmallestIndex, 0, sigma.length - 1);
        /*
        for (int i = 0; i < combineCriteria.length; i++) {
            System.out.println(combineCriteria[combineCriteriaFromLargestToSmallestIndex[i]]);
        }*/
        int centerNums = 0;
        int blockSizeMax = arrNeighborSize[combineCriteriaFromLargestToSmallestIndex[0]];
        int blockSizeMin = columnNum + 999;
        int readElements = 1;

        IntArrayList centerList = new IntArrayList();

        while ((blockSizeMax + 0.0) / blockSizeMin < scale) {
            centerNums++;
            centerList.add(combineCriteriaFromLargestToSmallestIndex[centerNums - 1]);
            boolean stop = false;
            if (readElements >= columnNum) {
                stop = true;
            }
            if (!stop) {
                for (int i = 0; i < centerList.size(); i++) {
                    if (ldRMatrix.get(centerList.get(i), combineCriteriaFromLargestToSmallestIndex[readElements]) >= threshold) {
                        stop = true;
                        break;
                    }
                }
            }
            if (stop) {
                break;
            }
            int size = arrNeighborSize[combineCriteriaFromLargestToSmallestIndex[readElements]];
            if (blockSizeMax < size) {
                blockSizeMax = size;
            } else {
                if (blockSizeMin > size) {
                    blockSizeMin = size;
                }
            }
            readElements++;
        }

        LinkedList<IntArrayList> blockList = new LinkedList<IntArrayList>();

        for (int i = 0; i < centerNums; i++) {
            IntArrayList tmpArrayList = new IntArrayList();
            tmpArrayList.add(combineCriteriaFromLargestToSmallestIndex[i]);
            blockList.add(tmpArrayList);
        }

        for (int i = centerNums; i < combineCriteriaFromLargestToSmallestIndex.length; i++) {
            int maxIndex = 0;
            double one = 0, two = 0;
            one = ldRMatrix.getQuick(combineCriteriaFromLargestToSmallestIndex[0], combineCriteriaFromLargestToSmallestIndex[i]);
            for (int j1 = 1; j1 < centerNums; j1++) {
                two = ldRMatrix.getQuick(combineCriteriaFromLargestToSmallestIndex[j1], combineCriteriaFromLargestToSmallestIndex[i]);
                if (one < two) {
                    one = two;
                    maxIndex = j1;
                }
            }
            if (one == 0) {
                one = 0;
                two = 0;
                for (int l = 0; l < blockList.get(0).size(); l++) {
                    one += ldRMatrix.getQuick(blockList.get(0).get(l), combineCriteriaFromLargestToSmallestIndex[i]);
                }
                one /= blockList.get(0).size();
                for (int j = 1; j < centerNums; j++) {
                    for (int j2 = 0; j2 < blockList.get(j).size(); j2++) {
                        two += ldRMatrix.getQuick(blockList.get(j).get(j2), combineCriteriaFromLargestToSmallestIndex[i]);
                    }
                    two /= blockList.get(j).size();
                    if (one < two) {
                        one = two;
                        maxIndex = j;
                    }
                }
            }
            blockList.get(maxIndex).add(combineCriteriaFromLargestToSmallestIndex[i]);
        }
        //System.out.println("Initial Block Size:" + blockList.size());
        List<IntArrayList> blockCluster = new ArrayList<IntArrayList>();
        if (blockList.size() > maxBlockSize) {
            int pairMorethanThreshold[][] = new int[blockList.size() * (blockList.size() - 1) / 2][3];
            int index = 0;
            for (int i = 0; i < blockList.size(); i++) {
                if (i == blockList.size() - 1) {
                    break;
                }
                for (int j = i + 1; j < blockList.size(); j++) {
                    int count = 0;
                    for (int l = 0; l < blockList.get(i).size(); l++) {
                        for (int l2 = 0; l2 < blockList.get(j).size(); l2++) {
                            if (ldRMatrix.getQuick(blockList.get(i).get(l), blockList.get(j).get(l2)) >= pairThreshold) {
                                count += 1;
                            }
                        }
                    }
                    pairMorethanThreshold[index][0] = i;
                    pairMorethanThreshold[index][1] = j;
                    pairMorethanThreshold[index][2] = count;
                    index += 1;
                }
            }
            arrIndexTmp = new int[pairMorethanThreshold.length];
            this.arrRho = new double[pairMorethanThreshold.length];
            for (int i = 0; i < pairMorethanThreshold.length; i++) {
                arrIndexTmp[i] = i;
                this.arrRho[i] = pairMorethanThreshold[i][2];
            }
            quickSort(arrIndexTmp, 0, arrIndexTmp.length - 1);
            ArrayList<Set<Integer>> setArrayList = new ArrayList<Set<Integer>>();
            Set<Integer> tmpSet = new HashSet<Integer>();
            tmpSet.add(pairMorethanThreshold[arrIndexTmp[0]][0]);
            tmpSet.add(pairMorethanThreshold[arrIndexTmp[0]][1]);
            setArrayList.add(tmpSet);
            // System.out.println(pairMorethanThreshold[arrIndexTmp[0]][0] +
            // "\t" + pairMorethanThreshold[arrIndexTmp[0]][1] + "\t" +
            // pairMorethanThreshold[arrIndexTmp[0]][2]);
            for (int i = 1; i < arrIndexTmp.length; i++) {
                // System.out.println(pairMorethanThreshold[arrIndexTmp[s]][0] +
                // "\t" + pairMorethanThreshold[arrIndexTmp[s]][1] + "\t" +
                // pairMorethanThreshold[arrIndexTmp[s]][2]);
                boolean creatNewSet = true;
                int blockSizeCount = blockList.size();
                int iterblockArray = 0;

                int preContain = -1;
                int lastContain = -1;
                while (iterblockArray < setArrayList.size()) {
                    if (setArrayList.get(iterblockArray).contains(pairMorethanThreshold[arrIndexTmp[i]][0])
                            || setArrayList.get(iterblockArray).contains(pairMorethanThreshold[arrIndexTmp[i]][1])) {
                        if (preContain != -1) {
                            lastContain = iterblockArray;
                        } else {
                            setArrayList.get(iterblockArray).add(pairMorethanThreshold[arrIndexTmp[i]][0]);
                            setArrayList.get(iterblockArray).add(pairMorethanThreshold[arrIndexTmp[i]][1]);
                            preContain = iterblockArray;
                        }
                        creatNewSet = false;
                        if (preContain != -1 && lastContain != -1) {
                            setArrayList.get(preContain).addAll(setArrayList.get(lastContain));
                            setArrayList.remove(lastContain);
                            lastContain = -1;
                            iterblockArray--;
                        }
                    }
                    iterblockArray++;
                }
                for (int l = 0; l < setArrayList.size(); l++) {
                    blockSizeCount = blockSizeCount - setArrayList.get(l).size() + 1;
                }
                if (creatNewSet) {
                    tmpSet = new HashSet<Integer>();
                    tmpSet.add(pairMorethanThreshold[arrIndexTmp[i]][0]);
                    tmpSet.add(pairMorethanThreshold[arrIndexTmp[i]][1]);
                    setArrayList.add(tmpSet);
                    blockSizeCount -= 1;
                }
                if (blockSizeCount <= maxBlockSize) {
                    // System.out.println(pairMorethanThreshold[arrIndexTmp[s]][0]
                    // +
                    // ","+pairMorethanThreshold[arrIndexTmp[s]][1]+" => "+pairMorethanThreshold[arrIndexTmp[s]][2]);
                    break;
                }
            }
            Set<Integer> allClusterIndex = new HashSet<Integer>();

            for (int i = 0; i < setArrayList.size(); i++) {
                Iterator iterator = setArrayList.get(i).iterator();
                ArrayList<Integer> subCluster = new ArrayList<Integer>();
                while (iterator.hasNext()) {
                    Integer element = (Integer) iterator.next();
                    subCluster.add(element);
                    allClusterIndex.add(element);
                }
                IntArrayList elementsIndexList = new IntArrayList();
                for (int j = 0; j < subCluster.size(); j++) {
                    for (int j2 = 0; j2 < blockList.get(subCluster.get(j)).size(); j2++) {
                        elementsIndexList.add(blockList.get(subCluster.get(j)).get(j2));
                    }
                }
                blockCluster.add(elementsIndexList);

            }

            for (int i = 0; i < blockList.size(); i++) {
                if (!allClusterIndex.contains(i)) {
                    IntArrayList elementsIndexList = new IntArrayList();
                    for (int j2 = 0; j2 < blockList.get(i).size(); j2++) {
                        elementsIndexList.add(blockList.get(i).get(j2));
                    }
                    blockCluster.add(elementsIndexList);
                }
            }
        } else {
            for (int i = 0; i < blockList.size(); i++) {
                IntArrayList elementsIndexList = new IntArrayList();
                for (int j = 0; j < blockList.get(i).size(); j++) {
                    elementsIndexList.add(blockList.get(i).get(j));
                }

                blockCluster.add(elementsIndexList);
            }
        }
        /*
         for (int i = 0; i < blockCluster.size(); i++) {
         System.out.print("block " + (i + 1) + ":" + blockCluster.get(i).size() + " => elements: ");
         for (int j = 0; j < blockCluster.get(i).size(); j++) {
         System.out.print(blockCluster.get(i).get(j) + "\t");
         }
         System.out.println();
         }
         */
         // ---------------- Initial ---------------------------

       //  long endTime = System.currentTimeMillis();
        // long duration = (endTime - startTime);
       //  System.out.println("That took " + duration / 1000.0 + " seconds");
         
        return blockCluster;
    }

    int partition(int arr[], int left, int right) {
        int i = left, j = right;
        int tmp;
        double pivot = arrRho[arr[(left + right) / 2]];

        while (i <= j) {
            while (arrRho[arr[i]] > pivot) {
                i++;
            }
            while (arrRho[arr[j]] < pivot) {
                j--;
            }
            if (i <= j) {
                tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i++;
                j--;
            }
        }
        return i;
    }

    void quickSort(int arr[], int left, int right) {
        int index = partition(arr, left, right);
        if (left < index - 1) {
            quickSort(arr, left, index - 1);
        }
        if (index < right) {
            quickSort(arr, index, right);
        }
    }
}
