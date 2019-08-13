/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import cern.colt.list.ByteArrayList;
import cern.colt.map.OpenIntIntHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import static org.cobi.kgg.business.entity.Constants.CHROM_NAMES;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 *
 * @author mxli
 */
public class PathwayBasedAssociation {

    private final static Logger LOG = Logger.getLogger(PathwayBasedAssociation.class.getName());
    private String name;
    private GeneBasedAssociation geneScan;
    private File canidateGeneSetFile;
    private boolean useLocalPathwayFile = false;
    private File pathwayDBFile;
    private String storageFolder;
    private int maxGeneNumInPathway;
    private int minGeneNumInPathway;
    private String pathwayAssocTestedMethod;
    private List<String> pValueSources;
    //multiple testing

    private String pathwayMultipleTestingMethod = "Benjamini & Hochberg (1995)";
    private double pathwayMultipleTestingPValue = 0.05;

    private String geneHyperSignifMethod = "Fixed p-value threshold";
    private double geneHyperSignifPValueCutoff = 0.05;

    private String pathwayEnrichmentMultipleTestingMethod = "Benjamini & Hochberg (1995)";
    private double pathwayEnrichmentMultipleTestingPValue = 0.05;

    private String pathwayGeneMultipleTestingMethod = "Benjamini & Hochberg (1995)";
    private double pathwayGeneMultipleTestingPValue = 0.05;
    private boolean filterNonSigEnriched = true;
    private double pathwayPValueExport = 0.05;
    private double genePValueExport = 0.05;
    private boolean ignoreNoLDSNPs = true;
    private double minR2=0.5;

    public double getMinR2() {
        return minR2;
    }

    public void setMinR2(double minR2) {
        this.minR2 = minR2;
    }
    
    
    public boolean isIgnoreNoLDSNPs() {
        return ignoreNoLDSNPs;
    }

    public void setIgnoreNoLDSNPs(boolean ignoreNoLDSNPs) {
        this.ignoreNoLDSNPs = ignoreNoLDSNPs;
    }

    public String getPathwayEnrichmentMultipleTestingMethod() {
        return pathwayEnrichmentMultipleTestingMethod;
    }

    public void setPathwayEnrichmentMultipleTestingMethod(String pathwayEnrichmentMultipleTestingMethod) {
        this.pathwayEnrichmentMultipleTestingMethod = pathwayEnrichmentMultipleTestingMethod;
    }

    public double getPathwayEnrichmentMultipleTestingPValue() {
        return pathwayEnrichmentMultipleTestingPValue;
    }

    public void setPathwayEnrichmentMultipleTestingPValue(double pathwayEnrichmentMultipleTestingPValue) {
        this.pathwayEnrichmentMultipleTestingPValue = pathwayEnrichmentMultipleTestingPValue;
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
            }
        }
    }

    /**
     *
     * @return @throws Exception
     */
    public void saveBetweenGeneLDToDisk(CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes, String pvalueSource) throws Exception {

        for (int i = 0; i < CHROM_NAMES.length; i++) {
            CorrelationBasedByteLDSparseMatrix ldRsMatrix = ldRsMatrixes[i];
            if (ldRsMatrix != null && !ldRsMatrix.isEmpty()) {
                FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();
                FileOutputStream objFOut = new FileOutputStream(storageFolder + File.separator + name + "." + pvalueSource + "betweenGeneLD." + CHROM_NAMES[i] + ".obj");
                FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
                out.writeObject(ldRsMatrix, CorrelationBasedByteLDSparseMatrix.class, ByteArrayList.class, OpenIntIntHashMap.class);
                out.close();
                out.flush();
                objFOut.close();
                /*
                 BufferedOutputStream objOBfs = new BufferedOutputStream(objFOut);
                 ObjectOutputStream localObjOut = new ObjectOutputStream(objOBfs);
                 localObjOut.writeObject(ldRsMatrix);
                 localObjOut.flush();
                 localObjOut.close();
                 objOBfs.flush();
                 objOBfs.close();
                 objFOut.close();
                 */
            }
        }
    }

    public CorrelationBasedByteLDSparseMatrix[] loadBetweenGeneLDToDisk(String pvalueSource) throws Exception {
        CorrelationBasedByteLDSparseMatrix[] ldRsMatrixes = new CorrelationBasedByteLDSparseMatrix[CHROM_NAMES.length];
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            File fileName = new File(storageFolder + File.separator + name + "." + pvalueSource + "betweenGeneLD." + CHROM_NAMES[i] + ".obj");
            if (!fileName.exists()) {
                //throw (new Exception("Cannot find data in hard disk!"));
                continue;
            }

            FileInputStream objFIn = new FileInputStream(fileName);

            FSTObjectInput in = new FSTObjectInput(objFIn);
            ldRsMatrixes[i] = (CorrelationBasedByteLDSparseMatrix) in.readObject(CorrelationBasedByteLDSparseMatrix.class, ByteArrayList.class, OpenIntIntHashMap.class);
            in.close();

            /*
             BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
             ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
             ldRsMatrixes[i] = (CorrelationBasedByteLDSparseMatrix) localObjIn.readObject();
             localObjIn.close();
             objIBfs.close();
             */
            objFIn.close();
        }
        return ldRsMatrixes;
    }

    public List<Pathway> loadPathwayAssociation(String pvalueSource) throws Exception {
        File pathwayAssociationFilePath = new File(storageFolder + File.separator + name + "." + pvalueSource + ".pvalue.obj");
        if (pathwayAssociationFilePath.exists()) {
            FileInputStream objFIn = new FileInputStream(pathwayAssociationFilePath);

            FSTObjectInput in = new FSTObjectInput(objFIn);
            List<Pathway> pathwayList = new ArrayList<Pathway>();
            int size = in.readInt();
            Object ppi = null;
            for (int i = 0; i < size; i++) {
                ppi = in.readObject(Pathway.class);
                if (ppi != null) {
                    pathwayList.add((Pathway) ppi);
                } else {
                    break;
                }
            }

            in.close();

            /*
             BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
             ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
             List<Pathway> pathwayList = (List<Pathway>) localObjIn.readObject();
             localObjIn.close();
             objIBfs.close();
             */
            objFIn.close();
            return pathwayList;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public String getStorageFolder() {
        return storageFolder;
    }

    public void savePathwayAssociaion(String pvalueSource, List<Pathway> ppiList) throws Exception {
        File ppiAssociationFilePath = new File(storageFolder + File.separator + name + "." + pvalueSource + ".pvalue.obj");

        FileOutputStream objFOut = new FileOutputStream(ppiAssociationFilePath);

        FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();
        FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
        out.writeInt(ppiList.size());
        for (Pathway paw : ppiList) {
            out.writeObject(paw, Pathway.class);
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
         objFOut.close();
         */
    }

    public int getMaxGeneNumInPathway() {
        return maxGeneNumInPathway;
    }

    public void setMaxGeneNumInPathway(int maxGeneNumInPathway) {
        this.maxGeneNumInPathway = maxGeneNumInPathway;
    }

    public int getMinGeneNumInPathway() {
        return minGeneNumInPathway;
    }

    public void setMinGeneNumInPathway(int minGeneNumInPathway) {
        this.minGeneNumInPathway = minGeneNumInPathway;
    }

    public boolean isFilterNonSigEnriched() {
        return filterNonSigEnriched;
    }

    public void setFilterNonSigEnriched(boolean filterNonSigEnriched) {
        this.filterNonSigEnriched = filterNonSigEnriched;
    }

    public double getGenePValueExport() {
        return genePValueExport;
    }

    public void setGenePValueExport(double genePValueExport) {
        this.genePValueExport = genePValueExport;
    }

    public double getPathwayPValueExport() {
        return pathwayPValueExport;
    }

    public void setPathwayPValueExport(double pathwayPValueExport) {
        this.pathwayPValueExport = pathwayPValueExport;
    }

    public String getPathwayGeneMultipleTestingMethod() {
        return pathwayGeneMultipleTestingMethod;
    }

    public void setPathwayGeneMultipleTestingMethod(String pathwayGeneMultipleTestingMethod) {
        this.pathwayGeneMultipleTestingMethod = pathwayGeneMultipleTestingMethod;
    }

    public double getPathwayGeneMultipleTestingPValue() {
        return pathwayGeneMultipleTestingPValue;
    }

    public void setPathwayGeneMultipleTestingPValue(double pathwayGeneMultipleTestingPValue) {
        this.pathwayGeneMultipleTestingPValue = pathwayGeneMultipleTestingPValue;
    }

    public String getGeneHyperSignifMethod() {
        return geneHyperSignifMethod;
    }

    public void setGeneHyperSignifMethod(String geneHyperSignifMethod) {
        this.geneHyperSignifMethod = geneHyperSignifMethod;
    }

    public double getGeneHyperSignifPValueCutoff() {
        return geneHyperSignifPValueCutoff;
    }

    public void setGeneHyperSignifPValueCutoff(double geneHyperSignifPValueCutoff) {
        this.geneHyperSignifPValueCutoff = geneHyperSignifPValueCutoff;
    }

    public String getPathwayMultipleTestingMethod() {
        return pathwayMultipleTestingMethod;
    }

    public void setPathwayMultipleTestingMethod(String pathwayMultipleTestingMethod) {
        this.pathwayMultipleTestingMethod = pathwayMultipleTestingMethod;
    }

    public double getPathwayMultipleTestingPValue() {
        return pathwayMultipleTestingPValue;
    }

    public void setPathwayMultipleTestingPValue(double pathwayMultipleTestingPValue) {
        this.pathwayMultipleTestingPValue = pathwayMultipleTestingPValue;
    }

    public PathwayBasedAssociation(String name, File pathwayDBFile, GeneBasedAssociation geneScan, String storageFolder) {
        this.name = name;
        this.pathwayDBFile = pathwayDBFile;
        this.geneScan = geneScan;
        this.storageFolder = storageFolder;

    }

    public PathwayBasedAssociation(String name, File pathwayDBFile, String storeageFolder) {
        this.name = name;
        this.pathwayDBFile = pathwayDBFile;
        this.storageFolder = storeageFolder;

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

    public boolean isUseLocalPathwayFile() {
        return useLocalPathwayFile;
    }

    public void setUseLocalPathwayFile(boolean useLocalPathwayFile) {
        this.useLocalPathwayFile = useLocalPathwayFile;
    }

    public File getPathwayDBFile() {
        return pathwayDBFile;
    }

    public void setPathwayDBFile(File pathwayDBFile) {
        this.pathwayDBFile = pathwayDBFile;
    }

    public String getPathwayAssocTestedMethod() {
        return pathwayAssocTestedMethod;
    }

    public void setPathwayAssocTestedMethod(String pathwayAssocTestedMethod) {
        this.pathwayAssocTestedMethod = pathwayAssocTestedMethod;
    }

    public List<String> getpValueSources() {
        return pValueSources;
    }

    public void setpValueSources(List<String> pValueSources) {
        this.pValueSources = pValueSources;
    }
}
