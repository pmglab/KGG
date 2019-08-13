/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.simu;

import cern.colt.list.IntArrayList;
import cern.jet.random.sampling.RandomSampler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author mxli
 */
public class Sampler {

    public List<Individual> sampleWithoutReplacement(List<Individual> allIndiv, int caseNum, int controlNum) {
        Collections.shuffle(allIndiv);
        List<Individual> sampledIndiv = new ArrayList<Individual>();
        Iterator<Individual> iter = allIndiv.iterator();
        while (iter.hasNext()) {
            Individual indiv = iter.next();
            if (caseNum > 0 && indiv.getAffectedStatus() == 2) {
                sampledIndiv.add(indiv);
                caseNum--;
            } else if (controlNum > 0 && indiv.getAffectedStatus() == 1) {
                sampledIndiv.add(indiv);
                controlNum--;
            }
            if (caseNum == 0 && controlNum == 0) {
                break;
            }
        }
        if (caseNum != 0) {
            System.out.println("Warning! The population has less case than " + caseNum);
        }

        if (controlNum != 0) {
            System.out.println("Warning! The population has less control than " + controlNum);
        }
        return sampledIndiv;
    }
    
     public List<Individual> sampleWithoutReplacement(List<Individual> allIndiv, int caseNum, int controlNum, IntArrayList caseIndexes, IntArrayList controlIndexes) {
        //very time consumming for large list
        //Collections.shuffle(allIndiv);
        if (caseNum >= caseIndexes.size()) {
            System.out.println("Warning! The population has less case than " + caseNum);
        }

        if (controlNum >= controlIndexes.size()) {
            System.out.println("Warning! The population has less control than " + controlNum);
        }
       
        long[] caseindexes = new long[caseNum];
          
        RandomSampler.sample(caseNum, caseIndexes.size(), caseNum, 0, caseindexes, 0, new cern.jet.random.engine.MersenneTwister(new java.util.Date()));
        List<Individual> sampledIndiv = new ArrayList<Individual>();
        for (int i = 0; i < caseNum; i++) {
            sampledIndiv.add(allIndiv.get(caseIndexes.getQuick((int) caseindexes[i])));
        }

        long[] controlindexes = new long[controlNum];
        RandomSampler.sample(controlNum, controlIndexes.size(), controlNum, 0, controlindexes, 0, new cern.jet.random.engine.MersenneTwister(new java.util.Date()));

        for (int i = 0; i < controlNum; i++) {
            sampledIndiv.add(allIndiv.get(controlIndexes.getQuick((int) controlindexes[i])));
        }

        return sampledIndiv;
    }

     
}
