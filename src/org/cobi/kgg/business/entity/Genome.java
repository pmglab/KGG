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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.cobi.kgg.business.entity.Constants.CHROM_NAMES;
import org.cobi.util.text.Util;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 *
 * @author mxli
 */
public class Genome implements Constants {

  /**
   * @pdOid 919fbd33-2725-4ec8-934c-52dbb4580123
   */
  private String name;
  /**
   * @pdOid 65e6e40e-de4a-4b64-9c2d-c71a70bc879e
   */
  private String storagePath;
  /**
   * @pdOid 8868dc35-e84b-42d2-aa76-77e1b0e4a652
   */
  private Map<String, int[]> snpGenomeIndexes;
  /**
   * @pdOid c95528e9-304b-4e47-bb13-d7f147cacf92
   */
  private Map<String, int[]> geneGenomeIndexes;
  /**
   * @pdRoleInfo migr=no name=Chromosome assc=association14 mult=0..*
   * type=Aggregation
   */
  private Chromosome[] chromosomes;
  private String[] pValueNames;
  private double extendedGene5PLen = 0;
  private double extendedGene3PLen = 0;
  private File pValueSource;
  private PValueFileSetting pValueSourceSetting;
  private boolean toAdjustPValue;
  private String ldMatrixStoragePath;
  private boolean mappedByRSID = true;
  //private String referenceGenomeVersion = "hg19";
  private String finalBuildGenomeVersion = "hg19";
  private String buildWay = "By RS ID";
  double minR2HighlyCorrelated = 0.9;
  private Map<String, IntArrayList> excludeRegionMap = null;
  private String[] chromLDFiles;
  private List<File[]> haploMapFilesList;
  private List<String> variantFeatureNames;
  private String geneDB = "";
  private String geneVarMapFile = "";
  PlinkDataset plinkSet;
  String ldFileGenomeVersion;
  boolean multiTraits = false;
  boolean exclusionModel = true;
  //ld source code
  //-2 others LD
  //0 genotype plink binary file
  //1 hapap ld
  //2 1kG haplomap
  //3 local LD calcualted by plink
  //4 1kG haplomap vcf format
  private int ldSourceCode = 0;
  private String sameLDGenome;
  double minEffectiveR2;
  private int[] geneSumCount;

  public List<String> getVariantFeatureNames() {
    return variantFeatureNames;
  }

  public boolean isExclusionModel() {
    return exclusionModel;
  }

  public void setExclusionModel(boolean exclusionModel) {
    this.exclusionModel = exclusionModel;
  }

  public int[] getGeneSumCount() {
    return geneSumCount;
  }

  public void setGeneVarMapFile(String geneVarMapFile) {
    this.geneVarMapFile = geneVarMapFile;
  }

  public String getGeneVarMapFile() {
    return geneVarMapFile;
  }

  public void setGeneSumCount(int[] geneSumCount) {
    this.geneSumCount = geneSumCount;
  }

  public void addAVariantFeatureNames(String name) {
    variantFeatureNames.add(name);
  }

  public String getGeneDB() {
    return geneDB;
  }

  public void setGeneDB(String geneDB) {
    this.geneDB = geneDB;
  }

  public double getMinEffectiveR2() {
    return minEffectiveR2;
  }

  public void setMinEffectiveR2(double minEffectiveR2) {
    this.minEffectiveR2 = minEffectiveR2;
  }

  public String getSameLDGenome() {
    return sameLDGenome;
  }

  public boolean isMultiTraits() {
    return multiTraits;
  }

  public void setMultiTraits(boolean multiTraits) {
    this.multiTraits = multiTraits;
  }

  public void setSameLDGenome(String sameLDGenome) {
    this.sameLDGenome = sameLDGenome;
  }

  public PValueFileSetting getpValueSourceSetting() {
    return pValueSourceSetting;
  }

  public void setpValueSourceSetting(PValueFileSetting pValueSourceSetting) {
    this.pValueSourceSetting = pValueSourceSetting;
  }

  public String getLdFileGenomeVersion() {
    return ldFileGenomeVersion;
  }

  public String getFinalBuildGenomeVersion() {
    return finalBuildGenomeVersion;
  }

  public void setFinalBuildGenomeVersion(String finalBuildGenomeVersion) {
    this.finalBuildGenomeVersion = finalBuildGenomeVersion;
  }

  public void setLdFileGenomeVersion(String ldFileGenomeVersion) {
    this.ldFileGenomeVersion = ldFileGenomeVersion;
  }

  public PlinkDataset getPlinkSet() {
    return plinkSet;
  }

  public void setPlinkSet(PlinkDataset plinkSet) {
    this.plinkSet = plinkSet;
  }

  public String getBuildWay() {
    return buildWay;
  }

  public void setBuildWay(String buildWay) {
    this.buildWay = buildWay;
  }

  public double getMinR2HighlyCorrelated() {
    return minR2HighlyCorrelated;
  }

  public int getLdSourceCode() {
    return ldSourceCode;
  }

  public void setLdSourceCode(int ldSourceCode) {
    this.ldSourceCode = ldSourceCode;
  }

  public void setMinR2HighlyCorrelated(double minR2HighlyCorrelated) {
    this.minR2HighlyCorrelated = minR2HighlyCorrelated;
  }

  public Map<String, IntArrayList> getExcludeRegionMap() {
    return excludeRegionMap;
  }

  public List<File[]> getHaploMapFilesList() {
    return haploMapFilesList;
  }

  public void setHaploMapFilesList(List<File[]> haploMapFilesList) {
    this.haploMapFilesList = haploMapFilesList;
  }

  public void setExcludeRegionMap(Map<String, IntArrayList> excludeRegionMap) {
    this.excludeRegionMap = excludeRegionMap;
  }

  public String[] getChromLDFiles() {
    return chromLDFiles;
  }

  public void setChromLDFiles(String[] chromLDFiles) {
    this.chromLDFiles = chromLDFiles;
  }

  public String getLdMatrixStoragePath() {
    return ldMatrixStoragePath;
  }

  public void setLdMatrixStoragePath(String ldMatrixStoragePath) {
    this.ldMatrixStoragePath = ldMatrixStoragePath;
  }

  public double getExtendedGene3PLen() {
    return extendedGene3PLen;
  }

  public void setExtendedGene3PLen(double extendedGene3PLen) {
    this.extendedGene3PLen = extendedGene3PLen;
  }

  public double getExtendedGene5PLen() {
    return extendedGene5PLen;
  }

  public void setExtendedGene5PLen(double extendedGene5PLen) {
    this.extendedGene5PLen = extendedGene5PLen;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   *
   * @return
   */
  public boolean isToAdjustPValue() {
    return toAdjustPValue;
  }

  /**
   *
   * @param toAdjustPValue
   */
  public void setToAdjustPValue(boolean toAdjustPValue) {
    this.toAdjustPValue = toAdjustPValue;
  }

  /**
   *
   * @return
   */
  public File getpValueSource() {
    return pValueSource;
  }

  /**
   *
   * @param pValueSource
   */
  public void setpValueSource(File pValueSource) {
    this.pValueSource = pValueSource;
  }

  /**
   *
   * @return
   */
  public Map<String, int[]> getGeneGenomeIndexes() {
    return geneGenomeIndexes;
  }

  /**
   *
   * @return
   */
  public Chromosome[] getChromosomes() {
    return chromosomes;
  }

  /**
   *
   * @param chromosomes
   */
  public void setChromosomes(Chromosome[] chromosomes) {
    this.chromosomes = chromosomes;
  }

  /**
   *
   * @param geneGenomeIndexes
   */
  public void setGeneGenomeIndexes(Map<String, int[]> geneGenomeIndexes) {
    this.geneGenomeIndexes = geneGenomeIndexes;
  }

  /**
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   *
   * @return
   */
  public String[] getpValueNames() {
    return pValueNames;
  }

  /**
   *
   * @param pValueNames
   */
  public void setpValueNames(String[] pValueNames) {
    this.pValueNames = pValueNames;
  }

  /**
   *
   * @return
   */
  public Map<String, int[]> getsNPGenomeIndexes() {
    return snpGenomeIndexes;
  }

  /**
   *
   * @param snpGenomeIndexes
   */
  public void setsNPGenomeIndexes(Map<String, int[]> sNPGenomeIndexes) {
    this.snpGenomeIndexes = sNPGenomeIndexes;
  }

  /**
   *
   * @return
   */
  public String getStoragePath() {
    return storagePath;
  }

  /**
   *
   * @param storagePath
   */
  public void setStoragePath(String storagePath) {
    this.storagePath = storagePath;
  }

  /**
   *
   * @param name
   */
  public Genome(String name) {
    this.name = name;
    variantFeatureNames = new ArrayList<String>();
  }

  /**
   *
   * @param name
   * @param storagePath
   */
  public Genome(String name, String storagePath) {
    this.name = name;
    this.storagePath = storagePath;
    variantFeatureNames = new ArrayList<String>();
  }

  /**
   *
   * @param rsID
   * @return
   */
  public Gene getGenebySNP(String rsID) {
    return null;

  }

  /**
   *
   * @return @throws Exception
   */
  public Chromosome[] loadAllChromosomes2Buf() throws Exception {
    if (chromosomes == null) {
      int chromLen = CHROM_NAMES.length;
      chromosomes = new Chromosome[chromLen];
      for (int i = 0; i < chromLen; i++) {
        chromosomes[i] = readChromosomefromDisk(i);
      }
    }
    return chromosomes;
  }

  /**
   *
   * @return @throws Exception
   */
  public Chromosome[] loadOneChromosomes2Buf(int definedIndex) throws Exception {
    if (chromosomes == null) {
      chromosomes = new Chromosome[CHROM_NAMES.length];
    }
    if (definedIndex == -1) {
      return chromosomes;
    } else {
      chromosomes[definedIndex] = readChromosomefromDisk(definedIndex);
      // System.out.println("Load chromosme " + CHROM_NAMES[definedIndex]);
    }
    return chromosomes;
  }

  /**
   *
   * @return @throws Exception
   */
  public Chromosome readChromosomefromDisk(int definedIndex) throws Exception {
    File fileName = new File(storagePath + File.separator + "Chromosome." + CHROM_NAMES[definedIndex] + ".obj");
    if (!fileName.exists()) {
      //throw (new Exception("Cannot find data in hard disk!"));
      return null;
    }
    FileInputStream objFIn = new FileInputStream(fileName);

    FSTObjectInput in = new FSTObjectInput(objFIn);
    Chromosome tmpChrom = (Chromosome) in.readObject(Chromosome.class, Gene.class, SNP.class);
    in.close();
    /*
         BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
         ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
         Chromosome tmpChrom = (Chromosome) localObjIn.readObject();
         localObjIn.close();
         objIBfs.close();
     */
    objFIn.close();
    return tmpChrom;
  }

  public CorrelationBasedByteLDSparseMatrix readChromosomeLDfromDisk(int definedIndex) throws Exception {
    File fileName = new File(ldMatrixStoragePath + File.separator + "LDrs." + CHROM_NAMES[definedIndex] + ".obj");
    if (!fileName.exists()) {
      //throw (new Exception("Cannot find data in hard disk!"));
      return null;
    }

    FileInputStream objFIn = new FileInputStream(fileName);

    FSTObjectInput in = new FSTObjectInput(objFIn);
    CorrelationBasedByteLDSparseMatrix ldRsMatrix = (CorrelationBasedByteLDSparseMatrix) in.readObject(CorrelationBasedByteLDSparseMatrix.class, ByteArrayList.class, OpenIntIntHashMap.class);
    in.close();

    /*
         BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
         ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
         CorrelationBasedByteLDSparseMatrix ldRsMatrix = (CorrelationBasedByteLDSparseMatrix) localObjIn.readObject();
         localObjIn.close();
         objIBfs.close();
     */
    objFIn.close();
    return ldRsMatrix;
  }

  public CorrelationBasedByteLDSparseMatrix readChromosomeCrossGeneLDfromDisk(int definedIndex) throws Exception {
    File fileName = new File(ldMatrixStoragePath + File.separator + "LDrs." + CHROM_NAMES[definedIndex] + "crossgene.obj");
    if (!fileName.exists()) {
      //throw (new Exception("Cannot find data in hard disk!"));
      return null;
    }

    FileInputStream objFIn = new FileInputStream(fileName);

    FSTObjectInput in = new FSTObjectInput(objFIn);
    CorrelationBasedByteLDSparseMatrix ldRsMatrix = (CorrelationBasedByteLDSparseMatrix) in.readObject(CorrelationBasedByteLDSparseMatrix.class, ByteArrayList.class, OpenIntIntHashMap.class);
    in.close();

    /*
         BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
         ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
         CorrelationBasedByteLDSparseMatrix ldRsMatrix = (CorrelationBasedByteLDSparseMatrix) localObjIn.readObject();
         localObjIn.close();
         objIBfs.close();
     */
    objFIn.close();
    return ldRsMatrix;
  }

  /**
   *
   * @return @throws Exception
   */
  public void writeChromosomeToDisk(Chromosome chrome) throws Exception {
    int definedIndex = chrome.getId();
    File fileName = new File(storagePath + File.separator + "Chromosome." + CHROM_NAMES[definedIndex] + ".obj");
    FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();
    FileOutputStream objFOut = new FileOutputStream(storagePath + File.separator + "Chromosome." + CHROM_NAMES[definedIndex] + ".obj");
    FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
    out.writeObject(chrome, Chromosome.class, Gene.class, SNP.class);

    out.flush();
    out.close();
    objFOut.close();

    /*
         BufferedOutputStream objOBfs = new BufferedOutputStream(objFOut);
         ObjectOutputStream localObjOut = new ObjectOutputStream(objOBfs);

         localObjOut.writeObject(chrome);
         localObjOut.flush();
         localObjOut.close();
         objOBfs.flush();
         objOBfs.close();
         objFOut.close();
     */
  }

  public void writeTraitCorrelationMatrixToDisk(List<Double> matrix) throws Exception {
    File fileName = new File(storagePath + File.separator + "TraitCorrelationMatrix.obj");
    FileOutputStream objFOut = new FileOutputStream(fileName);

    FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();
    FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
    out.writeObject(matrix);

    out.flush();
    out.close();
    objFOut.close();

    /*
         BufferedOutputStream objOBfs = new BufferedOutputStream(objFOut);
         ObjectOutputStream localObjOut = new ObjectOutputStream(objOBfs);

         localObjOut.writeObject(matrix);
         localObjOut.flush();
         localObjOut.close();
         objOBfs.flush();
         objOBfs.close();
         objFOut.close();
     */
  }

  public void removeStoredData() throws Exception {
    File fileName = new File(storagePath + File.separator);
    if (!fileName.exists()) {
      return;
    }
    Util.delAll(fileName);
  }

  public void removeTraitCorrelationMatrixInDisk() throws Exception {
    File fileName = new File(storagePath + File.separator + "TraitCorrelationMatrix.obj");
    if (!fileName.exists()) {
      return;
    }
    fileName.delete();
  }

  public double[][] loadTraitCorrelationMatrixFromDisk() throws Exception {
    File fileName = new File(storagePath + File.separator + "TraitCorrelationMatrix.obj");
    if (!fileName.exists()) {
      return null;
    }
    FileInputStream objFIn = new FileInputStream(fileName);

    FSTObjectInput in = new FSTObjectInput(objFIn);
    List<Double> existingObject = (List<Double>) in.readObject();
    in.close();

    /*
         BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
         ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
         List<Double> existingObject = (List<Double>) localObjIn.readObject();
         localObjIn.close();
         objIBfs.close();
     */
    objFIn.close();
    if (existingObject.isEmpty()) {
      return null;
    }
    int rowNum = (int) Math.sqrt(existingObject.size());
    double[][] corr = new double[rowNum][rowNum];
    for (int i = 0; i < rowNum; i++) {
      for (int j = 0; j < rowNum; j++) {
        corr[i][j] = existingObject.get(i * rowNum + j);
      }
    }
    return corr;
  }

  /**
   *
   * @return @throws Exception
   */
  public void writeChromosomeAndLDToDiskClean(Chromosome chrome, CorrelationBasedByteLDSparseMatrix ldRsMatrix, Map<String, int[]> snpChromIndex) throws Exception {
    int definedIndex = chrome.getId();
    File fileName = new File(storagePath + File.separator + "Chromosome." + CHROM_NAMES[definedIndex] + ".obj");
    // FSTConfiguration singletonConf =FSTConfiguration.createStructConfiguration();
    FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();

    FileOutputStream objFOut = new FileOutputStream(storagePath + File.separator + "Chromosome." + CHROM_NAMES[definedIndex] + ".obj");
    FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
    out.writeObject(chrome, Chromosome.class, Gene.class, SNP.class);

    out.flush();
    // out.close();
    objFOut.close();

    /*
         BufferedOutputStream objOBfs = new BufferedOutputStream(objFOut);
         ObjectOutputStream localObjOut = new ObjectOutputStream(objOBfs);

         localObjOut.writeObject(chrome);
         localObjOut.flush();
         localObjOut.close();
         objOBfs.flush();
         objOBfs.close();
         objFOut.close();
     */
    chrome.genes.clear();
    chrome.snpsOutGenes.clear();
    chrome = null;
    if (ldRsMatrix != null && !ldRsMatrix.isEmpty()) {
      objFOut = new FileOutputStream(ldMatrixStoragePath + File.separator + "LDrs." + CHROM_NAMES[definedIndex] + ".obj");

      out = singletonConf.getObjectOutput(objFOut);
      out.writeObject(ldRsMatrix, CorrelationBasedByteLDSparseMatrix.class, ByteArrayList.class, OpenIntIntHashMap.class);

      out.flush();
      //  out.close();
      objFOut.close();
      /*
             objOBfs = new BufferedOutputStream(objFOut);
             localObjOut = new ObjectOutputStream(objOBfs);

             localObjOut.writeObject(ldRsMatrix);
             localObjOut.flush();
             localObjOut.close();
             objOBfs.flush();
             objOBfs.close();
             objFOut.close();
       */
      ldRsMatrix.releaseLDData();
      //    ldRsMatrix.index1Map.clear();
      ldRsMatrix = null;
    }

    objFOut = new FileOutputStream(storagePath + File.separator + "SnpIndex." + CHROM_NAMES[definedIndex] + ".obj");
    out = singletonConf.getObjectOutput(objFOut);
    out.writeObject(snpChromIndex, Map.class);
    out.flush();
    out.close();
    objFOut.close();
    /*
         objOBfs = new BufferedOutputStream(objFOut);
         localObjOut = new ObjectOutputStream(objOBfs);

         localObjOut.writeObject(snpChromIndex);
         localObjOut.flush();
         localObjOut.close();
         objOBfs.flush();
         objOBfs.close();
         objFOut.close();
     */
    snpChromIndex.clear();
    System.gc();
  }

  public void writeCrossGeneLDMatrixToDisk(Chromosome chrome, CorrelationBasedByteLDSparseMatrix ldRsMatrix) throws Exception {
    int definedIndex = chrome.getId();
    FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();

    if (ldRsMatrix != null && !ldRsMatrix.isEmpty()) {
      FileOutputStream objFOut = new FileOutputStream(ldMatrixStoragePath + File.separator + "LDrs." + CHROM_NAMES[definedIndex] + "crossgene.obj");

      FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
      out.writeObject(ldRsMatrix, CorrelationBasedByteLDSparseMatrix.class, ByteArrayList.class, OpenIntIntHashMap.class);

      out.flush();
      //  out.close();
      objFOut.close();

      ldRsMatrix = null;
    }

  }

  /**
   *
   * @return @throws Exception
   */
  public void writeSNPGeneIndexToDisk(Map<String, int[]> tmpGeneGenomeIndexes) throws Exception {
    FileOutputStream objFOut = new FileOutputStream(storagePath + File.separator + "GeneGenomeIndexes.obj");

    FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();
    FSTObjectOutput out = singletonConf.getObjectOutput(objFOut);
    out.writeObject(tmpGeneGenomeIndexes);

    out.flush();
    out.close();
    objFOut.close();

    /*
         BufferedOutputStream objOBfs = new BufferedOutputStream(objFOut);
         ObjectOutputStream localObjOut = new ObjectOutputStream(objOBfs);

         //store  all index information
         localObjOut.writeObject(tmpGeneGenomeIndexes);
         localObjOut.flush();
         localObjOut.close();
         objOBfs.flush();
         objOBfs.close();
         objFOut.close();
     */
  }

  /**
   *
   * @throws Exception
   */
  public void releaseMemory() throws Exception {
    snpGenomeIndexes = null;
    geneGenomeIndexes = null;
    chromosomes = null;
    System.gc();
  }

  /**
   *
   * @return @throws Exception
   */
  @SuppressWarnings("unchecked")
  public Map<String, int[]> loadSNPGenomeIndexes2Buf() throws Exception {
    if (snpGenomeIndexes == null) {
      snpGenomeIndexes = new HashMap<String, int[]>();
      for (int i = 0; i < CHROM_NAMES.length; i++) {
        File fileName = new File(storagePath + File.separator + "SnpIndex." + CHROM_NAMES[i] + ".obj");
        if (!fileName.exists()) {
          //throw (new Exception("Cannot find data in hard disk!"));
          continue;
        }
        FileInputStream objFIn = new FileInputStream(fileName);

        FSTObjectInput in = new FSTObjectInput(objFIn);
        Object existingObject = in.readObject();
        if (existingObject != null) {
          snpGenomeIndexes.putAll((Map<String, int[]>) existingObject);
        }
        in.close();
        /*
                 BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
                 ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
                 Object existingObject = localObjIn.readObject();
                 if (existingObject != null) {
                 snpGenomeIndexes.putAll((Map<String, int[]>) existingObject);
                 }
                 localObjIn.close();
                 objIBfs.close();
         */
        objFIn.close();
      }
    }
    return snpGenomeIndexes;

  }

  /**
   *
   * @return @throws Exception
   */
  @SuppressWarnings("unchecked")
  public Map<String, int[]> loadGeneGenomeIndexes2Buf() throws Exception {
    if (geneGenomeIndexes == null) {
      File fileName = new File(storagePath + File.separator + "GeneGenomeIndexes.obj");
      if (!fileName.exists()) {
        throw (new Exception("Cannot find " + fileName.getCanonicalPath() + " in hard disk!"));
      }
      FileInputStream objFIn = new FileInputStream(fileName);
      FSTObjectInput in = new FSTObjectInput(objFIn);
      Object existingObject = in.readObject();
      if (existingObject != null) {
        geneGenomeIndexes = (Map<String, int[]>) existingObject;
      }
      in.close();

      /*
             BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
             ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
             Object existingObject = localObjIn.readObject();
             if (existingObject != null) {
             geneGenomeIndexes = (Map<String, int[]>) existingObject;
             }
             localObjIn.close();
             objIBfs.close();
       */
      objFIn.close();
    }
    return geneGenomeIndexes;
  }

  public boolean isMappedByRSID() {
    return mappedByRSID;
  }

  public void setMappedByRSID(boolean mappedByRSID) {
    this.mappedByRSID = mappedByRSID;
  }
}
