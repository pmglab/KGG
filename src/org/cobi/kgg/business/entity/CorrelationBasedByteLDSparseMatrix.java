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

import cern.colt.list.ByteArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mxli
 */
public class CorrelationBasedByteLDSparseMatrix extends LDSparseMatrix implements Serializable {

    // a specil matrix to store pair-wsie LD information
    // it is designed for the triganle matrix
    private static final long serialVersionUID = 100L;
    List<ByteArrayList> leadingSiteLDList = new ArrayList<ByteArrayList>();
    OpenIntIntHashMap lowerIndexMap = new OpenIntIntHashMap();
    public OpenIntIntHashMap positionIndexMap = null;
    int ldNum = 0;
    boolean hasSorted = false;
    //1/254
    double INTERVAL = 0.003937008;

    public CorrelationBasedByteLDSparseMatrix(OpenIntIntHashMap allIndexMap) {
        this.positionIndexMap = allIndexMap;
    }

    public boolean isEmpty() {
        return leadingSiteLDList.isEmpty();
    }

    public int size() {
        return ldNum;
    }

    public boolean addLDAt(final int pos1, final int pos2, final double ld) throws Exception {

        if (!positionIndexMap.containsKey(pos1) || !positionIndexMap.containsKey(pos2)) {
            //  throw new Exception("No need to store this LD!");
            System.err.println("No registered SNP position to store LD for " + pos1 + " " + pos2 + " " + ld);
            return false;
        }

        int index1 = positionIndexMap.get(pos1);
        int index2 = positionIndexMap.get(pos2);
        int acturalPos = -1;

        if (Math.abs(index1 - index2) > 100000) {
            //System.out.println("Warning!!! Too large distance for variants at " + pos1 + " and " + pos2 + "bp within a gene!");          
        }

        //ensure curIndex1 is less than index2 always
        if (index1 < index2) {
            if (lowerIndexMap.containsKey(index1)) {
                acturalPos = lowerIndexMap.get(index1);
            } else {
                acturalPos = leadingSiteLDList.size();
                lowerIndexMap.put(index1, acturalPos);
                leadingSiteLDList.add(new ByteArrayList());
            }
            int offSet = index2 - index1;
            ByteArrayList subLDList = leadingSiteLDList.get(acturalPos);
            if (subLDList == null) {
                subLDList = new ByteArrayList();
            }
            for (int i = subLDList.size(); i < offSet; i++) {
                subLDList.add((byte) - 128);
            }
            offSet--;
            subLDList.setQuick(offSet, (byte) (ld / INTERVAL - 127));
            ldNum++;
        } else if (index1 > index2) {
            if (lowerIndexMap.containsKey(index2)) {
                acturalPos = lowerIndexMap.get(index2);
            } else {
                acturalPos = leadingSiteLDList.size();
                lowerIndexMap.put(index2, acturalPos);
                leadingSiteLDList.add(new ByteArrayList());
            }

            int offSet = index1 - index2;
            ByteArrayList subLDList = leadingSiteLDList.get(acturalPos);
            if (subLDList == null) {
                subLDList = new ByteArrayList();
            }

            for (int i = subLDList.size(); i < offSet; i++) {
                subLDList.add((byte) - 128);
            }
            offSet--;
            subLDList.setQuick(offSet, (byte) (ld / INTERVAL - 127));
            ldNum++;
        } else {
            //  throw new Exception("No need to store this LD!");
            System.err.println("No need to store this LD for " + pos1 + " " + pos2 + " " + ld);
            return false;
        }
        return true;
    }

    @Override
    public DoubleMatrix2D subDenseLDMatrix(IntArrayList poss) throws Exception {
        int dim = poss.size();
        poss.quickSort();
        DoubleMatrix2D corrMat = new DenseDoubleMatrix2D(dim, dim);
        double x = 0;
        for (int i = 0; i < dim; i++) {
            corrMat.setQuick(i, i, 1);
            for (int j = i + 1; j < dim; j++) {
                x = getLDAt(poss.getQuick(i), poss.getQuick(j));
                corrMat.setQuick(i, j, x);
                corrMat.setQuick(j, i, x);
            }
        }
        // System.out.println(corrMat.toString());
        return corrMat;
    }

    @Override
    public void releaseLDData() {
        leadingSiteLDList.clear();
        lowerIndexMap.clear();
        positionIndexMap.clear();
        System.gc();
    }

    @Override
    public float getLDAt(int pos1, int pos2) throws Exception {

        if (!positionIndexMap.containsKey(pos1)) {
            //  throw new Exception("No need to store this LD!");
            //  System.err.println("No registered SNP position to get LD at " + pos1);
            return 0;
        }

        if (!positionIndexMap.containsKey(pos2)) {
            //  throw new Exception("No need to store this LD!");
            // System.err.println("No registered SNP position to get LD at " + pos2);
            return 0;
        }

        int index1 = positionIndexMap.get(pos1);
        int index2 = positionIndexMap.get(pos2);
        int acturalPos = -1;

        if (index1 < index2) {
            if (lowerIndexMap.containsKey(index1)) {
                acturalPos = lowerIndexMap.get(index1);
            } else {
                // System.err.println("No registered SNP position to get LD at " + pos1);
                return 0;
            }

            ByteArrayList subLDList = leadingSiteLDList.get(acturalPos);
            if (subLDList == null) {
                return 0;
            }

            int offSet = index2 - index1;
            if (offSet > subLDList.size()) {
                return 0;
            }
            offSet--;
            byte val = subLDList.get(offSet);
            //assume missing is 0
            if (val == -128) {
                return 0;
            } else if (val == -127) {
                return 0;
            } else if (val == 127) {
                return 1;
            }
            return (float) ((val + 127) * INTERVAL);
        } else if (index1 > index2) {
            if (lowerIndexMap.containsKey(index2)) {
                acturalPos = lowerIndexMap.get(index2);
            } else {
                // System.err.println("No registered SNP position to get LD at " + pos2);
                return 0;
            }

            ByteArrayList subLDList = leadingSiteLDList.get(acturalPos);
            if (subLDList == null) {
                return 0;
            }

            int offSet = index1 - index2;
            if (offSet > subLDList.size()) {
                return 0;
            }
            offSet--;
            byte val = subLDList.get(offSet);
            //assume missing is 0
            if (val == -128) {
                return 0;
            }
            if (val == -127) {
                return 0;
            } else if (val == 127) {
                return 1;
            }
            return (float) ((val + 127) * INTERVAL);
        }
        return 1;

    }
}
