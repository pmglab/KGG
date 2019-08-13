/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.simu;

import cern.colt.bitvector.BitVector;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.stat.Descriptive;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cobi.kgg.business.entity.StatusGtySet;
import org.cobi.util.math.MultiNormalRandGenerator;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvarmulti.MultinormalCholeskyGen;
import umontreal.iro.lecuyer.randvarmulti.MultinormalGen;
import umontreal.iro.lecuyer.rng.MT19937;
import umontreal.iro.lecuyer.rng.WELL607;

/**
 *
 * @author mxli
 */
public class Population {

    List<Individual> allIndiv;
    // MultiNormalRandGenerator multiNormGenerator;
    Uniform uniformGenerator = null;
    Normal normalGenerator = null;
    MultiNormalRandGenerator multiNormGenerator;
    //RandomData randGenerator;

    Population(List<Individual> lstInputIndividual) {
        allIndiv = lstInputIndividual;
    }

    public synchronized List<Individual> getAllIndiv() {
        return allIndiv;
    }

    public void setAllIndiv(List<Individual> allIndiv) {
        this.allIndiv = allIndiv;
    }

    public Population(int indivSize) {
        allIndiv = new ArrayList<Individual>(indivSize);

        for (int i = 0; i < indivSize; i++) {
            Individual indi = new Individual();
            indi.setFamilyID(String.valueOf(i));
            indi.setIndividualID("0");
            indi.setDadID("0");
            indi.setMomID("0");
            indi.setGender(1);
            indi.markerGtySet = new StatusGtySet();
            indi.traitGtySet = new StatusGtySet();
            allIndiv.add(indi);
        }
        //   this.multiNormGenerator = new MultiNormalRandGenerator();
        //   this.uniformGenerator = new RandomDataImpl(new MersenneTwister());
        uniformGenerator = new Uniform(new MersenneTwister(new java.util.Date()));
        normalGenerator = new Normal(0, 1, new MersenneTwister(new java.util.Date()));
        multiNormGenerator = new MultiNormalRandGenerator();
    }

    public void calculateGenotypeCovarianceMatrix(DoubleMatrix2D jointProb) throws Exception {
        multiNormGenerator.setJointProb(jointProb);
        multiNormGenerator.exploreCovarMatrix();
    }

    public void allocateMarkerTraitGenotypeSpaces(int markerLociNum, int traitLociNum, int intRR) throws Exception {
        markerLociNum = markerLociNum * intRR;
        traitLociNum = traitLociNum * intRR;
        int markerLociNumLess = markerLociNum - 1;
        int traitLociNumLess = traitLociNum - 1;
        int size = allIndiv.size();

        for (int i = 0; i < size; i++) {
            Individual indi = allIndiv.get(i);

//            indi.markerGtySet.existence = new BitVector(markerLociNum);
//            indi.markerGtySet.existence.replaceFromToWith(0, markerLociNumLess, true);
            indi.markerGtySet.paternalChrom = new BitVector(markerLociNum);
            indi.markerGtySet.paternalChrom.replaceFromToWith(0, markerLociNumLess, false);
            indi.markerGtySet.maternalChrom = new BitVector(markerLociNum);
            indi.markerGtySet.maternalChrom.replaceFromToWith(0, markerLociNumLess, false);

//            indi.traitGtySet.existence = new BitVector(traitLociNum);
//            indi.traitGtySet.existence.replaceFromToWith(0, traitLociNumLess, true);
//            indi.traitGtySet.paternalChrom = new BitVector(traitLociNum);
//            indi.traitGtySet.paternalChrom.replaceFromToWith(0, traitLociNumLess, false);
//            indi.traitGtySet.maternalChrom = new BitVector(traitLociNum);
//            indi.traitGtySet.maternalChrom.replaceFromToWith(0, traitLociNumLess, false);
        }
    }

    public void simulateDependentGenotypes() throws Exception {
        DoubleMatrix2D covarianceMatrix = multiNormGenerator.getCovarianceMatrix();
        double[] quantileProb = multiNormGenerator.getQuantileProb();
        int lociNum = quantileProb.length;
        int lociNumLess = lociNum - 1;
        final double PRECISION = 1.0e-8;
        double[] mean = new double[4];//set 4
        Arrays.fill(mean, 0.0);
        int indivSize = allIndiv.size();
        int[] seeds = new int[19];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = (int) (Math.random() * 1000000);
            System.out.println(seeds[i]);
        }
        WELL607 we = new WELL607();
        we.setSeed(seeds);

        NormalGen ng = new NormalGen(new MT19937(we));

        double[][] temp = {{16, 4, 4, -4}, {4, 10, 4, 2}, {4, 4, 6, -2}, {-4, 2, -2, 4}};
        DoubleArrayList sd = new DoubleArrayList();
        for (int i = 0; i < 10000; i++) {
            sd.add(ng.nextDouble());
        }
        System.out.println(ng.getMu() + "\t" + Descriptive.mean(sd));
        System.out.println(ng.getSigma() + "\t" + Descriptive.sampleVariance(sd, Descriptive.mean(sd)));

        covarianceMatrix = new DenseDoubleMatrix2D(temp);
        //MultinormalGen sg = new MultinormalGen(mean, covarianceMatrix, PRECISION, new GaussianRandomGenerator(new MersenneTwister()));

        try {
            MultinormalCholeskyGen sg = new MultinormalCholeskyGen(ng, mean, covarianceMatrix); //How about the alternative one MultinormalPCAGen?

            for (int i = 0; i < indivSize; i++) {
                double[] samples = new double[lociNum];
                sg.nextPoint(samples);
                allIndiv.get(i).markerGtySet.paternalChrom.replaceFromToWith(0, lociNumLess, false);
                for (int j = 0; j < lociNum; j++) {
                    if (samples[j] <= quantileProb[j]) {
                        allIndiv.get(i).markerGtySet.paternalChrom.putQuick(j, true);
                    }
                }
                sg.nextPoint(samples);
                allIndiv.get(i).markerGtySet.maternalChrom.replaceFromToWith(0, lociNumLess, false);
                for (int j = 0; j < lociNum; j++) {
                    if (samples[j] <= quantileProb[j]) {
                        allIndiv.get(i).markerGtySet.maternalChrom.putQuick(j, true);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void simulateDependentGenotypes(int blockNum) throws Exception {
        DoubleMatrix2D covarianceMatrix = multiNormGenerator.getCovarianceMatrix();
        double[] quantileProb = multiNormGenerator.getQuantileProb();
        int lociNum = quantileProb.length;

        final double PRECISION = 1.0e-8;
        double[] mean = new double[lociNum];
        Arrays.fill(mean, 0.0);
        int indivSize = allIndiv.size();
        //NormalGen ng=new NormalACRGen(new WELL607());
        int[] seeds = new int[19];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = (int) (Math.random() * 1000000);
            System.out.println(seeds[i]);
        }
        WELL607 we = new WELL607();
        we.setSeed(seeds);

        NormalGen ng = new NormalGen(new MT19937(we));

        MultinormalGen sg = new MultinormalCholeskyGen(ng, mean, covarianceMatrix);
        for (int b = 0; b < blockNum; b++) {
            for (int i = 0; i < indivSize; i++) {
                double[] samples = new double[lociNum];
                sg.nextPoint(samples);
                allIndiv.get(i).markerGtySet.paternalChrom.replaceFromToWith(b * lociNum, (b + 1) * lociNum - 1, false);//The length of Chromosome is extended!
                for (int j = 0; j < lociNum; j++) {
                    if (samples[j] <= quantileProb[j]) {
                        allIndiv.get(i).markerGtySet.paternalChrom.putQuick(b * lociNum + j, true);
                    }
                }
                sg.nextPoint(samples);
                allIndiv.get(i).markerGtySet.maternalChrom.replaceFromToWith(b * lociNum, (b + 1) * lociNum - 1, false);
                for (int j = 0; j < lociNum; j++) {
                    if (samples[j] <= quantileProb[j]) {
                        allIndiv.get(i).markerGtySet.maternalChrom.putQuick(b * lociNum + j, true);
                    }
                }
            }
        }
    }

    public void simulateIndependentMarkerGenotypes(double[] markerAlleleFreqs, int intBlockNum) throws Exception {
        int lociNum = markerAlleleFreqs.length * intBlockNum;
        int lociNumLess = lociNum - 1;
        int indivSize = allIndiv.size();
        double prob;

        for (int i = 0; i < indivSize; i++) {
            allIndiv.get(i).markerGtySet.paternalChrom.replaceFromToWith(0, lociNumLess, false);
            for (int j = 0; j < lociNum; j++) {
                prob = uniformGenerator.nextDouble();
                if (prob <= markerAlleleFreqs[j % markerAlleleFreqs.length]) {
                    allIndiv.get(i).markerGtySet.paternalChrom.putQuick(j, true);
                }
            }

            allIndiv.get(i).markerGtySet.maternalChrom.replaceFromToWith(0, lociNumLess, false);
            for (int j = 0; j < lociNum; j++) {
                prob = uniformGenerator.nextDouble();
                if (prob <= markerAlleleFreqs[j % markerAlleleFreqs.length]) {
                    allIndiv.get(i).markerGtySet.maternalChrom.putQuick(j, true);
                }
            }
        }
    }

    public void simulatePhenotypRischMultipLociModel(RischMultipLociModel rischModel) throws Exception {
        int lociNum = rischModel.getLociNum();

        int indivSize = allIndiv.size();
        double prob;
        List<DiseaseSNP> liabiSNPs = rischModel.getDiseaseSNPs();
        int[] suscepLociID = new int[lociNum];
        for (int j = 0; j < lociNum; j++) {
            suscepLociID[j] = liabiSNPs.get(j).getLDMarkerPosition();
        }
        boolean[] suscepLociLabel = new boolean[lociNum];
        for (int j = 0; j < lociNum; j++) {
            suscepLociLabel[j] = liabiSNPs.get(j).riskAlleleLable;
        }
        double[] jointPenetrance = rischModel.getJointGenotypePenerances();
        int riskAlleleNum = 0;
        for (int i = 0; i < indivSize; i++) {
            prob = uniformGenerator.nextDouble();
            riskAlleleNum = 0;

            for (int j = 0; j < lociNum; j++) {
                //directly use the markerGtySet to decide the disease probaility
                if (allIndiv.get(i).markerGtySet.paternalChrom.getQuick(suscepLociID[j]) == suscepLociLabel[j]) {
                    riskAlleleNum++;
                }
                if (allIndiv.get(i).markerGtySet.maternalChrom.getQuick(suscepLociID[j]) == suscepLociLabel[j]) {
                    riskAlleleNum++;
                }
            }
            if (prob <= jointPenetrance[riskAlleleNum]) {
                allIndiv.get(i).setAffectedStatus(2);
            } else {
                allIndiv.get(i).setAffectedStatus(1);
            }
        }
    }

}
