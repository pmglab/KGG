/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 *
 * @author mxli
 */
public class GeneBasedAssociation {

    private final static Logger LOG = Logger.getLogger(GeneBasedAssociation.class.getName());
    private String name;
    private Genome genome;
    private List<String> pValueSources;
    private String testedMethod;
    private String storageFolder;
    private boolean multVariateTest = false;
    private boolean ignoreNoLDSNP = false;

    public boolean isIgnoreNoLDSNP() {
        return ignoreNoLDSNP;
    }

    public void setIgnoreNoLDSNP(boolean ignoreNoLDSNP) {
        this.ignoreNoLDSNP = ignoreNoLDSNP;
    }

    public GeneBasedAssociation(String name, String storageFolder) {
        this.name = name;
        this.storageFolder = storageFolder;
        pValueSources = new ArrayList<String>();
    }

    public boolean isMultVariateTest() {
        return multVariateTest;
    }

    public void setMultVariateTest(boolean multVariateTest) {
        this.multVariateTest = multVariateTest;
    }

    public List<String> getPValueSources() {
        return pValueSources;
    }

    public void setpValueSources(List<String> pValueSources) {
        this.pValueSources = pValueSources;
    }

    public String getStorageFolder() {
        return storageFolder;
    }

    public String getTestedMethod() {
        return testedMethod;
    }

    public void setTestedMethod(String testedMethod) {
        this.testedMethod = testedMethod;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Genome getGenome() {
        return genome;
    }

    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public List<PValueGene> loadGenePValuesfromDisk(String pValueName, List<String> weightNames) throws Exception {
        File geneAssociationFilePath = new File(storageFolder + File.separator + name + "." + pValueName + ".obj");
        //System.out.println(geneAssociationFilePath.toString());
        if (!geneAssociationFilePath.exists()) {
            throw new Exception("Cannot find file " + geneAssociationFilePath.getAbsolutePath() + " in hard disk!");
        }
        List<PValueGene> pValueGenes;
        try (FileInputStream objFIn = new FileInputStream(geneAssociationFilePath); FSTObjectInput in = new FSTObjectInput(objFIn)) {
            pValueGenes = (List<PValueGene>) in.readObject(IntArrayList.class, DoubleArrayList.class, PValueGene.class);
            /*
            BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
            ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
            List<PValueGene> pValueGenes = (List<PValueGene>) localObjIn.readObject();
            localObjIn.close();
            objIBfs.close();
             */
        }

        if (weightNames != null) {
            geneAssociationFilePath = new File(storageFolder + File.separator + name + "." + pValueName + "wns.obj");
            //System.out.println(geneAssociationFilePath.toString());
            if (geneAssociationFilePath.exists()) {
                try (FileInputStream objFIn = new FileInputStream(geneAssociationFilePath); FSTObjectInput in = new FSTObjectInput(objFIn)) {
                    List<String> names = (List<String>) in.readObject(String.class);
                    weightNames.addAll(names);
                }
            }
        }

        return pValueGenes;
    }

    public Map<String, Double> loadTASTESNPPValuesfromDisk(String pValueName) throws Exception {
        File geneAssociationFilePath = new File(storageFolder + File.separator + name + "." + pValueName + ".obj");
        //System.out.println(geneAssociationFilePath.toString());
        if (!geneAssociationFilePath.exists()) {
            return null;
            // throw new Exception("Cannot find file " + geneAssociationFilePath.getAbsolutePath() + " in hard disk!");
        }
        FileInputStream objFIn = new FileInputStream(geneAssociationFilePath);
        FSTObjectInput in = new FSTObjectInput(objFIn);
        Map<String, Double> tastPValue = (Map<String, Double>) in.readObject();
        in.close();
        objFIn.close();
        /*
         BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
         ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
         Map<String, Double> tastPValue = (Map<String, Double>) localObjIn.readObject();
         localObjIn.close();
         objIBfs.close();
         objFIn.close();
         */
        return tastPValue;
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
                if (fileName.startsWith(name + ".") && fileName.endsWith(".obj")) {
                    delFile[j].delete();
                }
            }
        }
    }

    public void saveGenePValuestoDisk(String pValueName, List<String> weightNames, List<PValueGene> pValueGenes) throws Exception {
        File geneAssociationFilePath = new File(storageFolder + File.separator + name + "." + pValueName + ".obj");
        FileOutputStream objFOut = new FileOutputStream(geneAssociationFilePath);

        FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();
        FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
        out.writeObject(pValueGenes, IntArrayList.class, DoubleArrayList.class, PValueGene.class);

        out.flush();
        out.close();
        objFOut.close();

        if (weightNames != null && !weightNames.isEmpty()) {
            geneAssociationFilePath = new File(storageFolder + File.separator + name + "." + pValueName + "wns.obj");
            objFOut = new FileOutputStream(geneAssociationFilePath);

            singletonConf = FSTConfiguration.createDefaultConfiguration();
            out = singletonConf.getObjectOutput(objFOut);
            out.writeObject(weightNames, String.class);

            out.flush();
            out.close();
            objFOut.close();
        }
        /*
         BufferedOutputStream objOBfs = new BufferedOutputStream(objFOut);
         ObjectOutputStream localObjOut = new ObjectOutputStream(objOBfs);
         //Serialization starts here.
         localObjOut.writeObject(pValueGenes);
         localObjOut.flush();
         localObjOut.close();
         objOBfs.flush();
         objOBfs.close();
         */

    }

    public void saveTASTESNPPValuestoDisk(String pValueName, Map<String, Double> pValueGenes) throws Exception {
        File geneAssociationFilePath = new File(storageFolder + File.separator + name + "." + pValueName + ".obj");
        FileOutputStream objFOut = new FileOutputStream(geneAssociationFilePath);

        FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();
        FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
        out.writeObject(pValueGenes);

        out.flush();
        out.close();
        objFOut.close();

        /*
         BufferedOutputStream objOBfs = new BufferedOutputStream(objFOut);
         ObjectOutputStream localObjOut = new ObjectOutputStream(objOBfs);
         //Serialization starts here.
         localObjOut.writeObject(pValueGenes);
         localObjOut.flush();
         localObjOut.close();
         objOBfs.flush();
         objOBfs.close();
         */
        objFOut.close();
    }
}
