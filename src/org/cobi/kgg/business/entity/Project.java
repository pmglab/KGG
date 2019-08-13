/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.cobi.util.file.LocalFileFunc;

import org.w3c.dom.*;

/**
 *
 * @author mxli
 */
public class Project {

    /**
     * @pdOid a93af019-8ba5-4f07-a4fb-41b7c8c75814
     */
    private String name;
    /**
     * @pdOid 1a4b3aa8-7597-419d-8c4c-118725c6924b
     */
    private String workingPath;
    private List<File> pValueFileList;
    private List<File> candiGeneFileList;
    private String description;
    private List<String> pPINetworkFiles;
    private List<String> enrichedPathwayFiles;
    public List<Genome> genomeSet;
    public List<GeneBasedAssociation> geneScans;
    public List<PPIBasedAssociation> ppiScans;
    public List<PathwayBasedAssociation> pathwayScans;

    public String getDescription() {
        return description;
    }

    /**
     *
     * @param setName
     * @return
     */
    public boolean isAvailableGenomeName(String setName) {
        if (genomeSet == null) {
            genomeSet = new ArrayList<Genome>();
            return false;
        }
        int fileNum = genomeSet.size();
        for (int i = 0; i < fileNum; i++) {
            if (genomeSet.get(i).getName().equals(setName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param setName
     * @return
     */
    public boolean isAvailableCandiGeneFile(String setName) {
        if (candiGeneFileList == null) {
            candiGeneFileList = new ArrayList<File>();
            return false;
        }
        int fileNum = candiGeneFileList.size();
        for (int i = 0; i < fileNum; i++) {
            if (candiGeneFileList.get(i).getName().equals(setName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param setName
     * @return
     */
    public boolean isAvailablePPIScanName(String setName) {
        if (ppiScans == null) {
            ppiScans = new ArrayList<PPIBasedAssociation>();
            return false;
        }
        int fileNum = ppiScans.size();
        for (int i = 0; i < fileNum; i++) {
            if (ppiScans.get(i).getName().equals(setName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param setName
     * @return
     */
    public boolean isAvailablePathwayScanName(String setName) {
        if (pathwayScans == null) {
            pathwayScans = new ArrayList<PathwayBasedAssociation>();
            return false;
        }
        int fileNum = pathwayScans.size();
        for (int i = 0; i < fileNum; i++) {
            if (pathwayScans.get(i).getName().equals(setName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param setName
     * @return
     */
    public boolean isAvailableGeneScanName(String setName) {
        if (geneScans == null) {
            geneScans = new ArrayList<GeneBasedAssociation>();
            return false;
        }
        int fileNum = geneScans.size();
        for (int i = 0; i < fileNum; i++) {
            if (geneScans.get(i).getName().equals(setName)) {
                return true;
            }
        }
        return false;
    }

    public boolean removeGenomeByName(String setName) throws Exception {
        if (genomeSet == null) {
            return true;
        }
        int fileNum = genomeSet.size();
        for (int i = 0; i < fileNum; i++) {
            if (genomeSet.get(i).getName().equals(setName)) {

                File folder = new File(workingPath + File.separator + genomeSet.get(i).getName());
                if (folder.exists()) {
                    LocalFileFunc.delAll(folder);
                }
                genomeSet.remove(i).removeStoredData();

                return true;
            }
        }
        return false;
    }

    public boolean removeGeneSetByName(String setName) throws Exception {
        if (geneScans == null) {
            return true;
        }
        int fileNum = geneScans.size();
        for (int i = 0; i < fileNum; i++) {
            if (geneScans.get(i).getName().equals(setName)) {
                List<String> sourceList = geneScans.get(i).getPValueSources();
                for (String sourceName : sourceList) {
                    File folder = new File(workingPath + File.separator + setName + "." + sourceName + ".obj");
                    if (folder.exists()) {
                        folder.delete();
                    }
                }
                geneScans.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removePValueFileByName(String setName) throws Exception {
        if (pValueFileList == null) {
            return true;

        }
        int fileNum = pValueFileList.size();
        for (int i = 0; i < fileNum; i++) {
            if (pValueFileList.get(i).getName().equals(setName)) {
                pValueFileList.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeCandiGeneFileByName(String setName) throws Exception {
        if (candiGeneFileList == null) {
            return true;

        }
        int fileNum = candiGeneFileList.size();
        for (int i = 0; i < fileNum; i++) {
            if (candiGeneFileList.get(i).getName().equals(setName)) {
                candiGeneFileList.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removePPIScanByName(String setName) throws Exception {
        if (ppiScans == null) {
            return true;
        }
        int fileNum = ppiScans.size();
        for (int i = 0; i < fileNum; i++) {
            if (ppiScans.get(i).getName().equals(setName)) {
                List<String> sourceList = ppiScans.get(i).getPValueSources();
                for (String sourceName : sourceList) {
                    File ppiAssociationFilePath = new File(workingPath + File.separator + setName + "." + sourceName + ".pvalue.obj");
                    if (ppiAssociationFilePath.exists()) {
                        ppiAssociationFilePath.delete();
                    }
                }
                ppiScans.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removePathwayScanByName(String setName) throws Exception {
        if (pathwayScans == null) {
            return true;
        }
        int fileNum = pathwayScans.size();
        for (int i = 0; i < fileNum; i++) {
            if (pathwayScans.get(i).getName().equals(setName)) {
                List<String> sourceList = pathwayScans.get(i).getpValueSources();
                for (String sourceName : sourceList) {
                    File ppiAssociationFilePath = new File(workingPath + File.separator + setName + "." + sourceName + ".pvalue.obj");
                    if (ppiAssociationFilePath.exists()) {
                        ppiAssociationFilePath.delete();
                    }
                }
                pathwayScans.remove(i);
                return true;
            }
        }
        return false;
    }

    public Genome getGenomeByName(String setName) {
        if (genomeSet == null) {
            return null;
        }
        int fileNum = genomeSet.size();
        for (int i = 0; i < fileNum; i++) {
            if (genomeSet.get(i).getName().equals(setName)) {
                return genomeSet.get(i);
            }
        }
        return null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     *
     * @param name
     * @param workingPath
     */
    public Project(String name, String workingPath) {
        this.name = name;
        this.workingPath = workingPath;
        pValueFileList = new ArrayList<File>();
        candiGeneFileList = new ArrayList<File>();
        geneScans = new ArrayList<GeneBasedAssociation>();
    }

    public Project(File settingFileName) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;

        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new Exception(pce.toString());
        }
        Document doc = null;
        try {
            doc = db.parse(settingFileName.getCanonicalPath());
        } catch (DOMException dom) {
            throw new Exception(dom.getMessage());
        } catch (IOException ioe) {
            throw new Exception(ioe.toString() + " No project in this folder!");
        }
        pValueFileList = new ArrayList<File>();
        candiGeneFileList = new ArrayList<File>();

        Element root = doc.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("name");
        Element subNode = (Element) nodes.item(0);
        Text t = (Text) subNode.getFirstChild();
        if (t != null) {
            name = (t.getNodeValue());
        }

        nodes = root.getElementsByTagName("workingPath");
        subNode = (Element) nodes.item(0);
        t = (Text) subNode.getFirstChild();
        if (t != null) {
            workingPath = (t.getNodeValue());
        }
        nodes = root.getElementsByTagName("orginalAssociationPValueFile");
        for (int i = 0; i < nodes.getLength(); i++) {
            subNode = (Element) nodes.item(i);
            t = (Text) subNode.getFirstChild();
            if (t != null) {
                pValueFileList.add(new File(t.getNodeValue()));
            }
        }

        nodes = root.getElementsByTagName("candidateGeneSetFile");
        for (int i = 0; i < nodes.getLength(); i++) {
            subNode = (Element) nodes.item(i);
            t = (Text) subNode.getFirstChild();
            if (t != null) {
                candiGeneFileList.add(new File(t.getNodeValue()));
            }
        }

        nodes = root.getElementsByTagName("genomeSet");
        genomeSet = new ArrayList<Genome>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element classSet = (Element) nodes.item(i);
            String settName = classSet.getAttribute("name");
            Genome setter = new Genome(settName);

            String storagePath = classSet.getAttribute("storagePath");
            String pValueSource = classSet.getAttribute("pValueSource");

            setter.setStoragePath(storagePath);
            setter.setExtendedGene5PLen(Double.parseDouble(classSet.getAttribute("extendedGene5PLen")));
            setter.setExtendedGene3PLen(Double.parseDouble(classSet.getAttribute("extendedGene3PLen")));
            setter.setToAdjustPValue(Boolean.parseBoolean(classSet.getAttribute("toAdjustPValue")));
            setter.setMappedByRSID(Boolean.parseBoolean(classSet.getAttribute("mappedByRSID")));
            setter.setFinalBuildGenomeVersion((classSet.getAttribute("referenceGenomeVersion") == null ? "hg19" : classSet.getAttribute("referenceGenomeVersion")));
            if (classSet.getAttribute("referenceGeneDB") != null && classSet.getAttribute("referenceGeneDB").trim().length() > 0) {
                setter.setGeneDB(classSet.getAttribute("referenceGeneDB"));
            }
            if (classSet.getAttribute("varGeneMapFile") != null && classSet.getAttribute("varGeneMapFile").trim().length() > 0) {
                setter.setGeneVarMapFile(classSet.getAttribute("varGeneMapFile"));
            }
            
            setter.setpValueSource(new File(pValueSource));
            setter.setLdMatrixStoragePath(classSet.getAttribute("ldMatrixStoragePath"));
            String geneSum = classSet.getAttribute("geneSumCount");
            if (geneSum != null && !geneSum.isEmpty()) {
                String[] cells = geneSum.split(",");
                int[] counts = new int[cells.length];
                for (int s = 0; s < counts.length; s++) {
                    counts[s] = Integer.parseInt(cells[s]);
                }
                setter.setGeneSumCount(counts);
            }

            NodeList subNodes = classSet.getElementsByTagName("pValueName");
            String[] pValueNames = new String[subNodes.getLength()];
            for (int j = 0; j < subNodes.getLength(); j++) {
                Element e = (Element) subNodes.item(j);
                pValueNames[j] = e.getAttribute("name");
            }
            setter.setpValueNames(pValueNames);
            setter.setMinEffectiveR2(Double.parseDouble(classSet.getAttribute("minEffectiveR2")));

            setter.setLdSourceCode(Integer.parseInt(classSet.getAttribute("LDSourceCode")));
            int ldcode = setter.getLdSourceCode();
            StringBuilder sb = new StringBuilder();

            if (ldcode == -2) {
                subNodes = classSet.getElementsByTagName("borrowLDGenome");
                setter.setSameLDGenome(((Element) subNodes.item(0)).getAttribute("LDGenome"));
            } else if (ldcode == 0) {
                subNodes = classSet.getElementsByTagName("plinkldfile");
                PlinkDataset plinkSet = new PlinkDataset(((Element) subNodes.item(0)).getAttribute("ped"),
                        ((Element) subNodes.item(0)).getAttribute("map"), ((Element) subNodes.item(0)).getAttribute("bin"));
                setter.setPlinkSet(plinkSet);

            } else if (ldcode == 1 || ldcode == 4) {
                subNodes = classSet.getElementsByTagName("hapmapldfile");
                setter.setLdFileGenomeVersion(((Element) subNodes.item(0)).getAttribute("cordinateVersion"));
                String[] ldPathes = ((Element) subNodes.item(0)).getAttribute("pathes").split(",");

                if (ldPathes != null) {
                    for (int j = 0; j < ldPathes.length; j++) {
                        if (ldPathes[j].equals("null")) {
                            ldPathes[j] = null;
                        }
                    }
                    setter.setChromLDFiles(ldPathes);
                }

            } else if (ldcode == 2) {
                /*
                 List<File[]> ldFiles = genome.getHaploMapFilesList();
                 if (ldFiles != null) {
                 Element ldFileVariable = doc.createElement("haploldfile");
                 genomeVariable.appendChild(ldFileVariable);
                 ldFileVariable.setAttribute("cordinateVersion", genome.ldFileGenomeVersion);
                 if (!ldFiles.isEmpty()) {
                 sb.append(ldFiles.get(0)[0].getAbsolutePath());
                 sb.append(',');
                 sb.append(ldFiles.get(0)[1].getAbsolutePath());
                 }
                 for (int j = 1; j < ldFiles.size(); j++) {
                 sb.append(',');
                 sb.append(ldFiles.get(j)[0].getAbsolutePath());
                 sb.append(',');
                 sb.append(ldFiles.get(j)[1].getAbsolutePath());
                 }
                 ldFileVariable.setAttribute("pathes", sb.toString());
                 }
                 */
            }
            genomeSet.add(setter);
        }

        nodes = root.getElementsByTagName("geneBasedAssociationScan");
        geneScans = new ArrayList<GeneBasedAssociation>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element classSet = (Element) nodes.item(i);
            String settName = classSet.getAttribute("name");
            String storageFloder = classSet.getAttribute("storageFloder");
            GeneBasedAssociation setter = new GeneBasedAssociation(settName, storageFloder);

            String genomeName = classSet.getAttribute("genome");
            int fileNum = genomeSet.size();
            for (int s = 0; s < fileNum; s++) {
                if (genomeSet.get(s).getName().equals(genomeName)) {
                    setter.setGenome(genomeSet.get(s));
                    break;
                }
            }
            String testMethod = classSet.getAttribute("geneTestMethodName");
            setter.setTestedMethod(testMethod);

            String pValueSource = classSet.getAttribute("pValueSource");
            if (!pValueSource.isEmpty()) {
                String[] sources = pValueSource.substring(1, pValueSource.length() - 1).split(", ");
                setter.setpValueSources(Arrays.asList(sources));
                String multVariateTest = classSet.getAttribute("multVariateTest");
                if (multVariateTest != null) {
                    if (multVariateTest.equals("true")) {
                        setter.setMultVariateTest(true);
                    }
                }

                String ingoreNOLDSNP = classSet.getAttribute("inoreNoLDSNP");
                if (ingoreNOLDSNP != null) {
                    if (ingoreNOLDSNP.equals("true")) {
                        setter.setIgnoreNoLDSNP(true);
                    }
                }
                geneScans.add(setter);
            }
        }

        nodes = root.getElementsByTagName("ppiBasedAssociationScan");
        ppiScans = new ArrayList<PPIBasedAssociation>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element classSet = (Element) nodes.item(i);
            String settName = classSet.getAttribute("name");
            String fileName = classSet.getAttribute("ppIDBFile");
            FileString[] ppiFiles = null;
            if (fileName != null) {
                String[] fileNames = fileName.split(",");
                ppiFiles = new FileString[fileNames.length];
                for (int s = 0; s < fileNames.length; s++) {
                    ppiFiles[s] = new FileString(fileNames[s]);
                }
            }

            String storageFloder = classSet.getAttribute("storageFloder");
            PPIBasedAssociation setter = new PPIBasedAssociation(settName, ppiFiles, storageFloder);
            String geneSetName = classSet.getAttribute("geneSetScan");
            int fileNum = geneScans.size();
            for (int s = 0; s < fileNum; s++) {
                if (geneScans.get(s).getName().equals(geneSetName)) {
                    setter.setGeneScan(geneScans.get(s));
                    break;
                }
            }
            String isMerege = classSet.getAttribute("isToMerge");
            if (isMerege != null) {
                if (isMerege.equals("N")) {
                    setter.setIsToMergePPISet(false);
                } else {
                    setter.setIsToMergePPISet(true);
                }
            }

            String pValueSources = classSet.getAttribute("pValueSources");
            String[] pValueSourcesArray = pValueSources.substring(1, pValueSources.length() - 1).split(",");
            List<String> sources = Arrays.asList(pValueSourcesArray);
            setter.setpValueSources(sources);
            String testMethod = classSet.getAttribute("ppIAssocTestedMethod");
            setter.setPpIAssocTestedMethod(testMethod);
            String confiScore = classSet.getAttribute("confidenceScore");
            if (confiScore != null && !confiScore.isEmpty()) {
                setter.setCofidenceScoreThreshold(Double.parseDouble(confiScore));
            }

            ppiScans.add(setter);
        }

        nodes = root.getElementsByTagName("pathwayBasedAssociationScan");
        pathwayScans = new ArrayList<PathwayBasedAssociation>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element classSet = (Element) nodes.item(i);
            String settName = classSet.getAttribute("name");
            File ppiFile = new File(classSet.getAttribute("pathwayDBFile"));
            String storageFloder = classSet.getAttribute("storageFloder");
            PathwayBasedAssociation setter = new PathwayBasedAssociation(settName, ppiFile, storageFloder);
            String geneSetName = classSet.getAttribute("geneSetScan");
            int fileNum = geneScans.size();
            for (int s = 0; s < fileNum; s++) {
                if (geneScans.get(s).getName().equals(geneSetName)) {
                    setter.setGeneScan(geneScans.get(s));
                    break;
                }
            }
            String candiGeneSetFile = classSet.getAttribute("candidateGeneSetName");
            setter.setCanidateGeneSetFile(new File(candiGeneSetFile));

            String pValueSources = classSet.getAttribute("pValueSources");
            String[] pValueSourcesArray = pValueSources.substring(1, pValueSources.length() - 1).split(",");
            List<String> sources = Arrays.asList(pValueSourcesArray);
            setter.setpValueSources(sources);
            String testMethod = classSet.getAttribute("pathwayAssocTestedMethod");
            setter.setPathwayAssocTestedMethod(testMethod);

            String value = classSet.getAttribute("maxGeneNumInPathway");
            if (value != null && value.trim().length() != 0) {
                setter.setMaxGeneNumInPathway(Integer.valueOf(value));
            }
            value = classSet.getAttribute("minGeneNumInPathway");
            if (value != null && value.trim().length() != 0) {
                setter.setMinGeneNumInPathway(Integer.valueOf(value));
            }
            value = classSet.getAttribute("minR2");
            if (value != null && value.trim().length() != 0) {
                setter.setMinR2(Double.valueOf(value));
            }
            pathwayScans.add(setter);
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkingPath() {
        return workingPath;
    }

    public void setWorkingPath(String workingPath) {
        this.workingPath = workingPath;
    }

    public List<File> getpValueFileList() {
        return pValueFileList;
    }

    public void setpValueFileList(List<File> pValueFileList) {
        this.pValueFileList = pValueFileList;
    }

    public List<File> getCandiGeneFileList() {
        return candiGeneFileList;
    }

    public void setCandiGeneFileList(List<File> candiGeneFileList) {
        this.candiGeneFileList = candiGeneFileList;
    }

    /**
     * @param filePath
     * @pdOid ab5124c4-0d42-4f27-ba70-0b93927418fa
     */
    public void addPValueFile(File filePath) {
        // TODO: implement
        pValueFileList.add(filePath);
    }

    public void addCandiGeneFile(File filePath) {
        // TODO: implement
        candiGeneFileList.add(filePath);
    }

    public void addGeneBasedAssociationScan(GeneBasedAssociation scan) {
        if (!isAvailableGeneScanName(scan.getName())) {
            geneScans.add(scan);
        }
    }

    public void addPPIBasedAssociationScan(PPIBasedAssociation scan) {
        if (!isAvailablePPIScanName(scan.getName())) {
            ppiScans.add(scan);
        }
    }

    public void addPathwayBasedAssociationScan(PathwayBasedAssociation scan) {
        if (!isAvailablePathwayScanName(scan.getName())) {
            pathwayScans.add(scan);
        }
    }

    /**
     *
     * @throws Exception
     */
    public void writeProjectVariables() throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new Exception(pce.toString());
        }

        Document doc = db.newDocument();
        Element root = doc.createElement("KGGProject");
        doc.appendChild(root);
        Element attriNode = doc.createElement("name");
        root.appendChild(attriNode);
        Text value = doc.createTextNode(name);
        attriNode.appendChild(value);

        attriNode = doc.createElement("workingPath");
        root.appendChild(attriNode);
        value = doc.createTextNode(workingPath);
        attriNode.appendChild(value);

        for (int i = 0; i < pValueFileList.size(); i++) {
            Element variable = doc.createElement("orginalAssociationPValueFile");
            root.appendChild(variable);
            value = doc.createTextNode(pValueFileList.get(i).getCanonicalPath());
            variable.appendChild(value);
        }

        for (int i = 0; i < candiGeneFileList.size(); i++) {
            Element variable = doc.createElement("candidateGeneSetFile");
            root.appendChild(variable);
            value = doc.createTextNode(candiGeneFileList.get(i).getCanonicalPath());
            variable.appendChild(value);
        }
        if (genomeSet != null) {
            for (int i = 0; i < genomeSet.size(); i++) {
                Element genomeVariable = doc.createElement("genomeSet");
                root.appendChild(genomeVariable);

                Genome genome = genomeSet.get(i);
                genomeVariable.setAttribute("name", genome.getName());
                genomeVariable.setAttribute("storagePath", genome.getStoragePath());
                genomeVariable.setAttribute("extendedGene5PLen", String.valueOf(genome.getExtendedGene5PLen()));
                genomeVariable.setAttribute("extendedGene3PLen", String.valueOf(genome.getExtendedGene3PLen()));
                genomeVariable.setAttribute("pValueSource", genome.getpValueSource().getCanonicalPath());
                genomeVariable.setAttribute("ldMatrixStoragePath", genome.getLdMatrixStoragePath());
                genomeVariable.setAttribute("referenceGenomeVersion", genome.getFinalBuildGenomeVersion());
                if (genome.getGeneVarMapFile() != null && genome.getGeneVarMapFile().trim().length() > 0) {
                    genomeVariable.setAttribute("varGeneMapFile", genome.getGeneVarMapFile());
                }
                if (genome.getGeneDB() != null && genome.getGeneDB().trim().length() > 0) {
                    genomeVariable.setAttribute("referenceGeneDB", genome.getGeneDB());
                }
  
                if (genome.isToAdjustPValue()) {
                    genomeVariable.setAttribute("toAdjustPValue", "true");
                } else {
                    genomeVariable.setAttribute("toAdjustPValue", "false");
                }

                if (genome.isMappedByRSID()) {
                    genomeVariable.setAttribute("mappedByRSID", "true");
                } else {
                    genomeVariable.setAttribute("mappedByRSID", "false");
                }

                genomeVariable.setAttribute("minEffectiveR2", String.valueOf(genome.getMinEffectiveR2()));

                /*
                 //ld source code
                 //-2 others LD
                 //-1 no ld 
                 //0 genotype plink binary file
                 //1 hapap ld
                 //2 1kG haplomap
                 //3 local LD calcualted by plink
                 //4 1kG haplomap vcf format
 
                 */
                int ldcode = genome.getLdSourceCode();
                StringBuilder sb = new StringBuilder();
                genomeVariable.setAttribute("LDSourceCode", String.valueOf(ldcode));
                if (ldcode == -2) {
                    Element ldFileVariable = doc.createElement("borrowLDGenome");
                    genomeVariable.appendChild(ldFileVariable);
                    ldFileVariable.setAttribute("LDGenome", genome.getSameLDGenome());
                } else if (ldcode == 0) {
                    Element ldFileVariable = doc.createElement("plinkldfile");
                    genomeVariable.appendChild(ldFileVariable);
                    if (genome.plinkSet != null) {
                        ldFileVariable.setAttribute("ped", genome.plinkSet.pedigreeFileName);
                        ldFileVariable.setAttribute("map", genome.plinkSet.mapFileName);
                        ldFileVariable.setAttribute("bin", genome.plinkSet.plinkBinaryFileName);
                    }
                } else if (ldcode == 1 || ldcode == 4) {
                    String[] ldFiles = genome.getChromLDFiles();
                    if (ldFiles != null) {
                        Element ldFileVariable = doc.createElement("hapmapldfile");
                        genomeVariable.appendChild(ldFileVariable);
                        ldFileVariable.setAttribute("cordinateVersion", genome.ldFileGenomeVersion);
                        if (ldFiles.length > 0) {
                            sb.append(ldFiles[0]);
                        }
                        for (int j = 1; j < ldFiles.length; j++) {
                            sb.append(',');
                            sb.append(ldFiles[j]);
                        }
                        ldFileVariable.setAttribute("pathes", sb.toString());
                    }
                } else if (ldcode == 2) {
                    List<File[]> ldFiles = genome.getHaploMapFilesList();
                    if (ldFiles != null) {
                        Element ldFileVariable = doc.createElement("haploldfile");
                        genomeVariable.appendChild(ldFileVariable);
                        ldFileVariable.setAttribute("cordinateVersion", genome.ldFileGenomeVersion);
                        if (!ldFiles.isEmpty()) {
                            sb.append(ldFiles.get(0)[0].getAbsolutePath());
                            sb.append(',');
                            sb.append(ldFiles.get(0)[1].getAbsolutePath());
                        }
                        for (int j = 1; j < ldFiles.size(); j++) {
                            sb.append(',');
                            sb.append(ldFiles.get(j)[0].getAbsolutePath());
                            sb.append(',');
                            sb.append(ldFiles.get(j)[1].getAbsolutePath());
                        }
                        ldFileVariable.setAttribute("pathes", sb.toString());
                    }
                }
                /*
                 PValueFileSetting pvSetting = genome.getpValueSourceSetting();
                 Element ldFileVariable = doc.createElement("pValueFile");
                 genomeVariable.appendChild(ldFileVariable);
                 ldFileVariable.setAttribute("genomeVersion", pvSetting.getPosGenomeVersion());
                 ldFileVariable.setAttribute("missingLabel", pvSetting.getMissingLabel());
                 ldFileVariable.setAttribute("testInputType", pvSetting.getTestInputType());
                 ldFileVariable.setAttribute("chiSquareDf", String.valueOf(pvSetting.getChiSquareDf()));
                 ldFileVariable.setAttribute("chromIndexInFile", String.valueOf(pvSetting.getChromIndexInFile()));
                 ldFileVariable.setAttribute("positionIndexInFile", String.valueOf(pvSetting.getPositionIndexInFile()));
                 ldFileVariable.setAttribute("markerIndexInFile", String.valueOf(pvSetting.getMarkerIndexInFile()));
                 ldFileVariable.setAttribute("hasTitleRow", pvSetting.isHasTitleRow() ? "Yes" : "No");
                 StringBuilder sb = new StringBuilder();
                 int[] sNPPValueIndexes = pvSetting.getsNPPValueIndexes();
                 if (sNPPValueIndexes != null) {
                 if (sNPPValueIndexes.length > 0) {
                 sb.append(sNPPValueIndexes[0]);
                 }
                 for (int j = 1; j < sNPPValueIndexes.length; j++) {
                 sb.append(',');
                 sb.append(sNPPValueIndexes[j]);
                 }
                 }
                 ldFileVariable.setAttribute("sNPPValueIndexes", sb.toString());
                 ldFileVariable.setAttribute("pvalueColType", String.valueOf(pvSetting.getPvalueColType()));
                 */
                String[] names = genome.getpValueNames();
                for (int j = 0; j < names.length; j++) {
                    Element type = doc.createElement("pValueName");

                    type.setAttribute("name", names[j]);
                    genomeVariable.appendChild(type);
                }

                int[] counts = genome.getGeneSumCount();
                if (counts != null) {
                    sb.delete(0, sb.length());
                    for (int j = 0; j < counts.length; j++) {
                        sb.append(counts[j]);
                        sb.append(',');
                    }
                    genomeVariable.setAttribute("geneSumCount", sb.substring(0, sb.length() - 1));
                }
            }
        }
        if (geneScans != null) {
            for (int t = 0; t < geneScans.size(); t++) {
                Element variable = doc.createElement("geneBasedAssociationScan");
                root.appendChild(variable);

                GeneBasedAssociation setter = geneScans.get(t);
                if (setter.getGenome() == null) {
                    continue;
                }
                variable.setAttribute("name", setter.getName());
                variable.setAttribute("storageFloder", setter.getStorageFolder());

                variable.setAttribute("genome", setter.getGenome().getName());
                variable.setAttribute("geneTestMethodName", setter.getTestedMethod());
                variable.setAttribute("pValueSource", setter.getPValueSources().toString());
                variable.setAttribute("multVariateTest", String.valueOf(setter.isMultVariateTest()));
                variable.setAttribute("inoreNoLDSNP", String.valueOf(setter.isIgnoreNoLDSNP()));
            }
        }

        if (pathwayScans != null) {
            for (int t = 0; t < pathwayScans.size(); t++) {
                Element variable = doc.createElement("pathwayBasedAssociationScan");
                root.appendChild(variable);

                PathwayBasedAssociation setter = pathwayScans.get(t);
                variable.setAttribute("name", setter.getName());
                if (setter.getCanidateGeneSetFile() != null) {
                    variable.setAttribute("candidateGeneSetName", setter.getCanidateGeneSetFile().getCanonicalPath());
                }
                if (setter.getGeneScan() != null) {
                    variable.setAttribute("geneSetScan", setter.getGeneScan().getName());
                }
                variable.setAttribute("storageFloder", setter.getStorageFolder());
                variable.setAttribute("pathwayDBFile", setter.getPathwayDBFile().getCanonicalPath());
                variable.setAttribute("pathwayAssocTestedMethod", setter.getPathwayAssocTestedMethod());
                variable.setAttribute("pValueSources", setter.getpValueSources().toString());
                variable.setAttribute("maxGeneNumInPathway", String.valueOf(setter.getMaxGeneNumInPathway()));
                variable.setAttribute("minGeneNumInPathway", String.valueOf(setter.getMinGeneNumInPathway()));
                variable.setAttribute("minR2", String.valueOf(setter.getMinR2()));
            }
        }

        if (ppiScans != null) {
            for (int t = 0; t < ppiScans.size(); t++) {
                Element variable = doc.createElement("ppiBasedAssociationScan");
                root.appendChild(variable);

                PPIBasedAssociation setter = ppiScans.get(t);
                variable.setAttribute("name", setter.getName());
                if (setter.getCanidateGeneSetFile() != null) {
                    variable.setAttribute("candidateGeneSetName", setter.getCanidateGeneSetFile().getCanonicalPath());
                }
                if (setter.getGeneScan() != null) {
                    variable.setAttribute("geneSetScan", setter.getGeneScan().getName());
                }
                variable.setAttribute("storageFloder", setter.getStorageFolder());
                StringBuilder sb = new StringBuilder();
                FileString[] ppiFiles = setter.getPpIDBFiles();
                if (ppiFiles != null) {
                    for (FileString ppS : ppiFiles) {
                        sb.append(ppS.getCanonicalPath());
                        sb.append(",");
                    }
                }
                if (setter.isIsToMergePPISet()) {
                    variable.setAttribute("isToMerge", "Y");
                } else {
                    variable.setAttribute("isToMerge", "N");
                }

                variable.setAttribute("ppIDBFile", sb.toString());
                variable.setAttribute("ppIAssocTestedMethod", setter.getPpIAssocTestedMethod());
                variable.setAttribute("pValueSources", setter.getPValueSources().toString());
                variable.setAttribute("confidenceScore", String.valueOf(setter.getCofidenceScoreThreshold()));

            }
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        FileOutputStream outStream = new FileOutputStream(workingPath + File.separator + name + ".xml");
        PrintWriter pw = new PrintWriter(outStream);
        StreamResult result = new StreamResult(pw);
        transformer.transform(new DOMSource(doc), result);
        pw.close();
        outStream.close();
    }
}
