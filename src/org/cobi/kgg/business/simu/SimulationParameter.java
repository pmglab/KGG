/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.simu;

import java.util.ArrayList;
import org.cobi.kgg.ui.dialog.PowerSimulationFrame;

/**
 *
 * @author Jiang Li
 */
public class SimulationParameter {

    //self-define

    public int intTotalSNP;
    public int intBlockNum;
    public int intRR1;

    public double dblMAFStart;
    public double dblMAFEnd;
    public double dblMAFStep;

    public boolean boolSNPIndependenceFlag1;

    public double dblLDStart;
    public double dblLDEnd;
    public double dblLDStep;

    //real data-plink
    public String strFamilyFile;
    public String strMapFile;
    public String strBEDFile;

    public int intVariant2;
    public int intRR2;
    public boolean boolSNPIndependenceFlag2;

    //real data-vcf
    public String strVCFFile;
    public int intVariant3;
    public int intRR3;
    public boolean boolSNPIndependenceFlag3;

    //Risk Variants      
    public int intRiskSNPStart;
    public int intRiskSNPEnd;
    public int intRiskSNPStep;

    public double dblOddsStart;
    public double dblOddsEnd;
    public double dblOddsStep;

    public double dblDiseasePrevalence;
    public int intGeneticModel;
    public double dblPValueThreshold;
    public ArrayList<Integer> altRiskSNPPosition;

    //Population & Sample   
    public int intPopulation;
    public int intCase;
    public int intControl;

    //Simulation
    public int intSimulationTimes;
    public int intSamplingTimes;
    public int intGeneBasedMethod;
    public int intMetaAnalysis;
    public int intNumOfStudies;
    public boolean boolShuffle;
    public int parellelRunNum;
    
    //SKAT
    public boolean boolSKAT;
    public SKAT skatOne;   
    public int intParallel;
    //Others
    public int intApplicationFlag;
    public PowerSimulationFrame jdOne;


}
