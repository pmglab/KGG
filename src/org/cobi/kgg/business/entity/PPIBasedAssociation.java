/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 *
 * @author mxli
 */
public class PPIBasedAssociation {
    
    private final static Logger LOG = Logger.getLogger(PPIBasedAssociation.class.getName());
    private String name;
    private GeneBasedAssociation geneScan;
    private File canidateGeneSetFile;
    private boolean useLocalPPIFile = false;
    private FileString[] ppIDBFiles;
    private String ppIAssocTestedMethod;
    private List<String> pValueSources;
    private String storageFolder;
    private double cofidenceScoreThreshold = 0.6;
    private boolean isToMergePPISet = false;
    private boolean ignoreNoLDSNPs = true;
    
    public List<String> getPValueSources() {
        return pValueSources;
    }
    
    public boolean isIsToMergePPISet() {
        return isToMergePPISet;
    }
    
    public void setIsToMergePPISet(boolean isToMergePPISet) {
        this.isToMergePPISet = isToMergePPISet;
    }
    
    public boolean isIgnoreNoLDSNPs() {
        return ignoreNoLDSNPs;
    }
    
    public void setIgnoreNoLDSNPs(boolean ignoreNoLDSNPs) {
        this.ignoreNoLDSNPs = ignoreNoLDSNPs;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public void setpValueSources(List<String> pValueSources) {
        this.pValueSources = pValueSources;
    }
    
    public String getStorageFolder() {
        return storageFolder;
    }
    
    public double getCofidenceScoreThreshold() {
        return cofidenceScoreThreshold;
    }
    
    public void setCofidenceScoreThreshold(double cofidenceScoreThreshold) {
        this.cofidenceScoreThreshold = cofidenceScoreThreshold;
    }
    
    public void removeStoredData() throws Exception {
        File f = new File(storageFolder + File.separator);
        if (f.exists() && f.isDirectory()) {
            File delFile[] = f.listFiles();
            int i = f.listFiles().length;
            for (int j = 0; j < i; j++) {
                if (delFile[j].isDirectory()) {
                    continue;
                }
                String fileName = delFile[j].getName();
                if (fileName.startsWith(name + ".") && fileName.endsWith(".pvalue.obj")) {
                    delFile[j].delete();
                }
                //check out out memeroy probl
                if (fileName.startsWith(name + ".") && fileName.contains(".NoLDSNPs.")) {
                    delFile[j].delete();
                }
            }
        }
    }
    
    public List<PPISet> loadPPIAssociation(String pvalueSource, String ppiSet) throws Exception {
        File ppiAssociationFilePath = new File(storageFolder + File.separator + name + "." + pvalueSource + "." + ppiSet + ".pvalue.obj");
        if (ppiAssociationFilePath.exists()) {
            FileInputStream objFIn = new FileInputStream(ppiAssociationFilePath);
            FSTObjectInput in = new FSTObjectInput(objFIn);
            List<PPISet> ppiList = new ArrayList<PPISet>();
            int size = in.readInt();
            Object ppi = null;
            for (int i = 0; i < size; i++) {
                ppi = in.readObject(PPISet.class);
                if (ppi != null) {
                    ppiList.add((PPISet) ppi);
                }
            }

            // List<PPISet> ppiList = (List<PPISet>) in.readObject(PPISet.class);
            in.close();

            /*
             BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
             ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
             List<PPISet> ppiList = (List<PPISet>) localObjIn.readObject();
             localObjIn.close();
             objIBfs.close();
             */
            objFIn.close();
            return ppiList;
        } else {
            return null;
        }
    }
    
    public void savePPIAssociaion(String pvalueSource, List<PPISet> ppiList, String ppiSet) throws Exception {
        File ppiAssociationFilePath = new File(storageFolder + File.separator + name + "." + pvalueSource + "." + ppiSet + ".pvalue.obj");
        
        FileOutputStream objFOut = new FileOutputStream(ppiAssociationFilePath);
        
        FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();
        FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
        out.writeInt(ppiList.size());
        for (PPISet ppiset : ppiList) {
            out.writeObject(ppiset, PPISet.class);
        }
        
        out.flush();
        out.close();
        objFOut.close();

        /*
         BufferedOutputStream objOBfs = new BufferedOutputStream(objFOut);
         ObjectOutputStream localObjOut = new ObjectOutputStream(objOBfs);

         localObjOut.writeObject(ppiList);
         localObjOut.flush();
         localObjOut.close();
         objOBfs.flush();
         objOBfs.close();
         */
        objFOut.close();
        
    }
    //multiple testing setting
    private String ppIAssocMultiTestedMethod = "Standard Bonferroni";
    private double ppIAssocNominalError = 0.05;
    private String ppIHetroTestedMethod = "Higgins I2<=";
    private double ppIHetroNominalError = 0.05;
    private String ppIKeepBothAssocMethod = "Standard Bonferroni";
    private double ppIKeepBothAssocNominalError = 0.05;
    
    public PPIBasedAssociation(String name, FileString[] pPIDBFile, GeneBasedAssociation geneScan, String storageFolder) {
        this.name = name;
        this.ppIDBFiles = pPIDBFile;
        this.geneScan = geneScan;
        this.storageFolder = storageFolder;
        
    }
    
    public PPIBasedAssociation(String name, FileString[] pPIDBFile, String storageFolder) {
        this.name = name;
        this.ppIDBFiles = pPIDBFile;
        this.storageFolder = storageFolder;
        
    }
    
    public boolean isUseLocalPPIFile() {
        return useLocalPPIFile;
    }
    
    public void setUseLocalPPIFile(boolean useLocalPPIFile) {
        this.useLocalPPIFile = useLocalPPIFile;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public GeneBasedAssociation getGeneScan() {
        return geneScan;
    }
    
    public void setGeneScan(GeneBasedAssociation geneScan) {
        this.geneScan = geneScan;
    }
    
    public File getCanidateGeneSetFile() {
        return canidateGeneSetFile;
    }
    
    public void setCanidateGeneSetFile(File canidateGeneSetFile) {
        this.canidateGeneSetFile = canidateGeneSetFile;
    }
    
    public FileString[] getPpIDBFiles() {
        return ppIDBFiles;
    }
    
    public void setPpIDBFiles(FileString[] ppIDBFiles) {
        this.ppIDBFiles = ppIDBFiles;
    }
    
    public String getPpIAssocTestedMethod() {
        return ppIAssocTestedMethod;
    }
    
    public void setPpIAssocTestedMethod(String ppIAssocTestedMethod) {
        this.ppIAssocTestedMethod = ppIAssocTestedMethod;
    }
    
    public String getPpIAssocMultiTestedMethod() {
        return ppIAssocMultiTestedMethod;
    }
    
    public void setPpIAssocMultiTestedMethod(String ppIAssocMultiTestedMethod) {
        this.ppIAssocMultiTestedMethod = ppIAssocMultiTestedMethod;
    }
    
    public double getPpIAssocNominalError() {
        return ppIAssocNominalError;
    }
    
    public void setPpIAssocNominalError(double ppIAssocNominalError) {
        this.ppIAssocNominalError = ppIAssocNominalError;
    }
    
    public String getPpIHetroTestedMethod() {
        return ppIHetroTestedMethod;
    }
    
    public void setPpIHetroTestedMethod(String ppIHetroTestedMethod) {
        this.ppIHetroTestedMethod = ppIHetroTestedMethod;
    }
    
    public double getPpIHetroNominalError() {
        return ppIHetroNominalError;
    }
    
    public void setPpIHetroNominalError(double ppIHetroNominalError) {
        this.ppIHetroNominalError = ppIHetroNominalError;
    }
    
    public String getPpIKeepBothAssocMethod() {
        return ppIKeepBothAssocMethod;
    }
    
    public void setPpIKeepBothAssocMethod(String ppIKeepBothAssocMethod) {
        this.ppIKeepBothAssocMethod = ppIKeepBothAssocMethod;
    }
    
    public double getPpIKeepBothAssocNominalError() {
        return ppIKeepBothAssocNominalError;
    }
    
    public void setPpIKeepBothAssocNominalError(double ppIKeepBothAssocNominalError) {
        this.ppIKeepBothAssocNominalError = ppIKeepBothAssocNominalError;
    }
}
