/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.simu;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author JiangLi
 */
public class SKAT {

    RConnection rcon;
    int intParallel;

    public SKAT() throws RserveException {
        this.rcon = new RConnection();
        this.rcon.eval("pack=\"SKAT\"; if (!require(pack,character.only = TRUE)) { install.packages(pack,dep=TRUE,repos='http://cran.us.r-project.org');if(!require(pack,character.only = TRUE)) stop(\"Package not found\")}");
        this.rcon.eval("pack=\"snow\"; if (!require(pack,character.only = TRUE)) { install.packages(pack,dep=TRUE,repos='http://cran.us.r-project.org');if(!require(pack,character.only = TRUE)) stop(\"Package not found\")}");
        this.rcon.eval("library(SKAT)");
        this.rcon.eval("library(snow)");        
    }

    public double getPValue(int[][] Z, int[] y) throws REngineException, REXPMismatchException, InterruptedException {
        int[] intDim = new int[2];
        intDim[0] = Z.length;
        intDim[1] = Z[0].length;
        int[] intVector = new int[intDim[0] * intDim[1]];
        for (int i = 0; i < intDim[0]; i++) {
            for (int j = 0; j < intDim[1]; j++) {
                intVector[i * intDim[1] + j] = Z[i][j];
            }
        }
       
        rcon.assign("numVector", intVector);
        //rcon.assign("numDim", intDim);
        rcon.eval("Z<-matrix(numVector,nrow=" + intDim[0] + ",ncol=" + intDim[1] + ",byrow=T)");
        rcon.assign("y", y);
        rcon.eval("obj<-SKAT_Null_Model(y ~ 1, out_type=\"D\")");
        double p = rcon.eval("SKAT(Z, obj, kernel = \"linear.weighted\")$p.value").asDouble();//This step will cost a lot of time. 

        return p;
    }

    public int[] getPhenotype(int intCase, int intControl) {
        int[] intPhenotype = new int[intCase + intControl];
        for (int i = 0; i < intCase; i++) {
            intPhenotype[i] = 1;
        }
        for (int i = 0; i < intControl; i++) {
            intPhenotype[intCase + i] = 0;
        }
        return intPhenotype;
    }

    public int[][] getGenotype(List<Individual> lstIndividual) {
        int row = lstIndividual.size();
        int col = lstIndividual.get(0).markerGtySet.paternalChrom.size();
        int[][] Z = new int[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                Individual item = lstIndividual.get(i);
                int intM = item.markerGtySet.maternalChrom.getQuick(j) ? 1 : 0;
                int intF = item.markerGtySet.paternalChrom.getQuick(j) ? 1 : 0;
                Z[i][j] = intM + intF;
            }
        }

        return Z;
    }

    ArrayList<Double> getPValueParallel(ArrayList<ArrayList<int[][]>> altSKATs, int intParallel,int intStudy) throws REngineException, REXPMismatchException {
        if(intParallel!=this.intParallel & intParallel==1){
            this.closeRServe();
            this.startRServe(intParallel);
        }
        if(intParallel!=this.intParallel){
            this.closeRServe();
            this.startRServe(intParallel);
        }
        
        ArrayList<Double> altResult=new ArrayList();
        for(int j=0;j<intStudy;j++){
            ArrayList<int[][]> altZ=new ArrayList();
            for(int i=0;i<intParallel;i++) altZ.add(altSKATs.get(i).get(j));
            double[] dblResult=this.getPValue(altZ);//Time-consuming part. 
            for(int i=0;i<intParallel;i++)  altResult.add(dblResult[i]);
        }
        return altResult;        
    }

    double[] getPValue(ArrayList<int[][]> altZ) throws REngineException, REXPMismatchException {
        double[] dblResult=null;
        try {
            rcon.voidEval("lstZ<-list()");          
            for(int k=0;k<altZ.size();k++){
                int[][] Z=altZ.get(k);
                int[] intDim = new int[2];
                intDim[0] = Z.length;
                intDim[1] = Z[0].length;
                int[] intVector = new int[intDim[0] * intDim[1]];                
                for (int i = 0; i < intDim[0]; i++) 
                    for (int j = 0; j < intDim[1]; j++) 
                        intVector[i * intDim[1] + j] = Z[i][j];
                rcon.assign("numVector", intVector);
                rcon.voidEval("lstZ[["+(k+1)+"]]<-matrix(numVector,nrow=" + intDim[0] + ",ncol=" + intDim[1] + ",byrow=T)");
            }
            rcon.voidEval("lstResult<-parLapply(cl,lstZ,SKAT,obj)");
            //rcon.assign(".temp", "lstResult<-parLapply(cl,lstZ,SKAT,obj)");
            //REXP r=rcon.parseAndEval("try(eval(parse(text=.temp)),silent=T)");
            //if (r.inherits("try-error")) System.err.println("Error: "+r.toString());
            dblResult=rcon.eval("sapply(lstResult,fun<-function(x){x$p.value})").asDoubles();            
        } catch (RserveException ex) {
            ex.printStackTrace();
        }    
        return dblResult;
    }

    public void setPhenotype(int intCase, int intControl) {
        try {
            int[] intPhenotype=this.getPhenotype(intCase, intControl);
            rcon.assign("y", intPhenotype);
            rcon.voidEval("obj<-SKAT_Null_Model(y ~ 1, out_type=\"D\")");
        } catch (REngineException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void closeRServe(){
        try {
            this.intParallel=0;
            rcon.voidEval("stopCluster(cl)");
        } catch (RserveException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void startRServe(int intParallel){
        try {
            this.intParallel=intParallel;
            rcon.voidEval("cl<-makeCluster("+intParallel+")");
        } catch (RserveException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
