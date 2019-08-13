/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.kgg.business.entity;

import cern.colt.bitvector.BitVector;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jiang Li
 */
public class StatusGtySetExtension extends StatusGtySet {
    public List<Boolean> lstPaternalGty;
    public List<Boolean> lstMaternalGty;
    public List<Boolean> lstExistence;

    public StatusGtySetExtension() {
        lstPaternalGty=new ArrayList();
        lstMaternalGty=new ArrayList();
        lstExistence=new ArrayList();
    }
    
    public void addPaternalGty(Boolean boolGty){
        lstPaternalGty.add(boolGty);
    }
    
    public void addMaternalGty(Boolean boolGty){
        lstMaternalGty.add(boolGty);
    }
    
    public void addExistence(Boolean boolGty){
        lstExistence.add(boolGty);
    }
    
    public void mergeBack(){
        
        this.existence=new BitVector(lstExistence.size());
        this.paternalChrom=new BitVector(lstExistence.size());
        this.maternalChrom=new BitVector(lstExistence.size());
        
        for(int i=0;i<lstExistence.size();i++){
            this.existence.put(i, lstExistence.get(i));
            this.paternalChrom.put(i, lstPaternalGty.get(i));
            this.maternalChrom.put(i, lstMaternalGty.get(i));
        } 
        
        lstExistence=null;
        lstPaternalGty=null;
        lstMaternalGty=null;
    }
    
    
    
    
}
