// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.kgg.business.entity;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mxli
 */
public class CorrelationBasedLDSparseMatrix extends LDSparseMatrix implements Serializable {
    // a specil matrix to store pair-wsie LD information
    // it is designed for the triganle matrix
    private static final long serialVersionUID = 2L;
    List<SparseLDMatrixElement> ldList = new ArrayList<SparseLDMatrixElement>();
    SparseLDMatrixElementIndex1Comparator elementIndex1Comparator = new SparseLDMatrixElementIndex1Comparator();
    SparseLDMatrixElementIndex2Comparator elementIndex2Comparator = new SparseLDMatrixElementIndex2Comparator();
    SparseLDMatrixElement element = new SparseLDMatrixElement(0, 0, 0);
    OpenIntObjectHashMap index1Map = new OpenIntObjectHashMap();
    boolean hasSorted = false;

    public boolean isEmpty() {
        return ldList.isEmpty();
    }

    public int size() {
        return ldList.size();
    }

    public boolean addLDAt(int index1, int index2, float ld) throws Exception {
        //ensure curIndex1 is less than index2 always
        if (index1 > index2) {
            SparseLDMatrixElement ele = new SparseLDMatrixElement(index2, index1, ld);
            ldList.add(ele);
        } else if (index1 < index2) {
            SparseLDMatrixElement ele = new SparseLDMatrixElement(index1, index2, ld);
            ldList.add(ele);
        } else {
            //  throw new Exception("No need to store this LD!");
            System.err.println("No need to store this LD for " + index1 + " " + index2 + " " + ld);
            return false;
        }
        return true;
    }

    @Override
    public DoubleMatrix2D subDenseLDMatrix(IntArrayList indexes) throws Exception {
        int dim = indexes.size();
        indexes.quickSort();
        DoubleMatrix2D corrMat = new DenseDoubleMatrix2D(dim, dim);
        double x = 0;
        for (int i = 0; i < dim; i++) {
            corrMat.setQuick(i, i, 1);
            for (int j = i + 1; j < dim; j++) {
                x = getLDAt(indexes.getQuick(i), indexes.getQuick(j));
                corrMat.setQuick(i, j, x);
                corrMat.setQuick(j, i, x);
            }
        }
        // System.out.println(corrMat.toString());
        return corrMat;
    }

    public void sortIndex() {
        Collections.sort(ldList, elementIndex1Comparator);
        hasSorted = true;
        index1Map.clear();
        int size = ldList.size();
        if (size == 0) {
            return;
        }
        //twice sort
        List<SparseLDMatrixElement> tmpAllLdList = new ArrayList<SparseLDMatrixElement>();
        List<SparseLDMatrixElement> tmpLdList = new ArrayList<SparseLDMatrixElement>();
        tmpAllLdList.addAll(ldList);
        ldList.clear();
        int curIndex1 = tmpAllLdList.get(0).index1;
        int[] bound = new int[]{0, 0};
        for (int i = 1; i < size; i++) {
            if (tmpAllLdList.get(i).index1 != curIndex1) {
                bound[1] = i - 1;

                for (int j = bound[0]; j <= bound[1]; j++) {
                    tmpLdList.add(tmpAllLdList.get(j));
                }
                Collections.sort(tmpLdList, elementIndex2Comparator);
                ldList.addAll(tmpLdList);
                tmpLdList.clear();
                index1Map.put(curIndex1, bound);

                curIndex1 = tmpAllLdList.get(i).index1;
                bound = new int[]{i, i};
            }
        }
        //the last index
        bound[1] = size - 1;
        for (int j = bound[0]; j <= bound[1]; j++) {
            tmpLdList.add(tmpAllLdList.get(j));
        }
        Collections.sort(tmpLdList, elementIndex2Comparator);
        ldList.addAll(tmpLdList);
        tmpLdList.clear();
        index1Map.put(curIndex1, bound);
        tmpAllLdList.clear();
        System.gc();
    }

    float binarySearch(int tmpIndex2, int left, int right) {
        if (left > right) {
            return 0;
        }
        int middle = (left + right) / 2;

        if (ldList.get(middle).index2 == tmpIndex2) {
            return ldList.get(middle).ld;
        } else if (ldList.get(middle).index2 > tmpIndex2) {
            return binarySearch(tmpIndex2, left, middle - 1);
        } else {
            return binarySearch(tmpIndex2, middle + 1, right);
        }
    }

    boolean binarySearchOriginal(int tmpIndex2, int left, int right) {
        if (left > right) {
            return true;
        }
        int middle = (left + right) / 2;

        if (ldList.get(middle).index2 == tmpIndex2) {
            return ldList.get(middle).original;
        } else if (ldList.get(middle).index2 > tmpIndex2) {
            return binarySearchOriginal(tmpIndex2, left, middle - 1);
        } else {
            return binarySearchOriginal(tmpIndex2, middle + 1, right);
        }
    }

//Warning, this will change the r
    boolean binarySearchSet(int tmpIndex2, int left, int right, float r) {
        if (left > right) {
            return false;
        }
        int middle = (left + right) / 2;

        if (ldList.get(middle).index2 == tmpIndex2) {
            ldList.get(middle).ld = r;
            //later on, this.r is replaced by r
            ldList.get(middle).original = false;
        } else if (ldList.get(middle).index2 > tmpIndex2) {
            return binarySearchSet(tmpIndex2, left, middle - 1, r);
        } else {
            return binarySearchSet(tmpIndex2, middle + 1, right, r);
        }
        return true;
    }

    @Override
    public void releaseLDData() {
        //Do not need do anything for this class
    }

    @Override
    public float getLDAt(int index1, int index2) throws Exception {

        // if (!hasSorted) {
        // throw new Exception("Not a sorted LDSparseMatrix");
        // }

        //ensure curIndex1 is always less than index2, always
        int tmpIndex;
        if (index1 > index2) {
            tmpIndex = index1;
            index1 = index2;
            index2 = tmpIndex;
        } else if (index1 == index2) {
            return 1;
        }
        element.index1 = index1;

        int[] bound = (int[]) index1Map.get(index1);
        if (bound == null) {
            return 0;
        } else {
            return binarySearch(index2, bound[0], bound[1]);
        }
    }

    public boolean getIsOriginalAt(int index1, int index2) throws Exception {

        // if (!hasSorted) {
        // throw new Exception("Not a sorted LDSparseMatrix");
        // }

        //ensure curIndex1 is always less than index2, always
        int tmpIndex;
        if (index1 > index2) {
            tmpIndex = index1;
            index1 = index2;
            index2 = tmpIndex;
        }
        element.index1 = index1;

        int[] bound = (int[]) index1Map.get(index1);
        if (bound == null) {
            return true;
        } else {
            return binarySearchOriginal(index2, bound[0], bound[1]);
        }
    }

    public void setLDAt(int index1, int index2, float r) throws Exception {

        // if (!hasSorted) {
        // throw new Exception("Not a sorted LDSparseMatrix");
        // }

        //ensure curIndex1 is always less than index2, always
        int tmpIndex;
        if (index1 > index2) {
            tmpIndex = index1;
            index1 = index2;
            index2 = tmpIndex;
        }
        element.index1 = index1;

        int[] bound = (int[]) index1Map.get(index1);
        if (bound == null) {
            addLDAt(index1, index2, r);
        } else {
            if (!binarySearchSet(index2, bound[0], bound[1], r)) {
                addLDAt(index1, index2, r);
            }
        }
    }
}
