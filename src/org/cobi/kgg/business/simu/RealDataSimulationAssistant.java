/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.kgg.business.simu;

import cern.colt.bitvector.BitVector;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import net.sf.samtools.util.AsciiLineReader;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import org.cobi.kgg.business.entity.PlinkDataset;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.StatusGtySet;
import org.cobi.kgg.business.entity.StatusGtySetExtension;

/**
 *
 * @author Jiang Li
 */
public class RealDataSimulationAssistant { 
    
    private final static Logger LOG = Logger.getLogger(PlinkDataset.class.getName());
    
    PlinkDataset pdsRealData;
    List<SNP> lstInputSNP;
    List<Individual> lstInputIndividual;
    List<StatusGtySet> lstInputGenotype;
    List<Double> lstMAF;
    double[][] dblLD;
    int intMarkerNum;
    String strVCF;
    

    public RealDataSimulationAssistant(PlinkDataset pdsRealData, int intMarkerNum) {
        this.pdsRealData = pdsRealData;
        this.intMarkerNum=intMarkerNum;
        lstInputSNP=new ArrayList<SNP>();
        lstInputIndividual=new ArrayList<Individual>();
        lstInputGenotype=new ArrayList<StatusGtySet>();
        lstMAF=new ArrayList<Double>();
        
    }

    public RealDataSimulationAssistant(String strVCF, int intMarkerNum) {
        this.strVCF = strVCF;
        this.intMarkerNum=intMarkerNum;
        lstInputSNP=new ArrayList<SNP>();
        lstInputIndividual=new ArrayList<Individual>();
        lstInputGenotype=new ArrayList<StatusGtySet>();
        lstMAF=new ArrayList<Double>();        
    }
    
    public void readSNPFromBimFileByPositions() throws Exception {
        //Set inputstream. 
        File fileMap = new File(pdsRealData.getMapFileName());
        BufferedReader br = new BufferedReader(new FileReader(fileMap));
        
        //Set delimitation using regex. 
        String delimit = "\\,+| +|\t+";
        
        //Start to parse .bim file line by line. 
        String strLine;
        int lineCounter = 0;
        while ((strLine = br.readLine()) != null) {
            String[] strItems=strLine.split(delimit);
            SNP snpOne=new SNP();
            snpOne.setPhysicalPosition(Integer.parseInt(strItems[3]));
            snpOne.setGenotypeOrder(lineCounter);
            snpOne.setRsID(strItems[1]);
            snpOne.setStrChr(strItems[0]);
            lstInputSNP.add(snpOne);
            lineCounter++;
            }
        br.close();
    }
    
    public void readIndividualFromFamFile() throws Exception{
        //Set inputstream. 
        File fileFam = new File(pdsRealData.getPedigreeFileName());
        BufferedReader br = new BufferedReader(new FileReader(fileFam));
        
        //Set delimitation using regex. 
        String delimit = "\\,+| +|\t+";
        
        //Start to parse .bim file line by line. 
        String strLine;
        while ((strLine = br.readLine()) != null) {
            String[] strItems=strLine.split(delimit);
            Individual idvOne=new Individual();
            idvOne.setFamilyID(strItems[0]);
            idvOne.setIndividualID(strItems[1]);
            idvOne.setMomID(strItems[2]);
            idvOne.setDadID(strItems[3]);
            idvOne.setGender(Integer.parseInt(strItems[4]));
            idvOne.setAffectedStatus(Integer.parseInt(strItems[5]));
            idvOne.setLabelInChip(strItems[0]+ "@*@" +strItems[1]);
            lstInputIndividual.add(idvOne);
        }
        br.close();       
    }
    
    public boolean readGenotypeFromBedFile() throws Exception{
        
        //Set asistant varibles. 
        StringBuilder logInfor = new StringBuilder();
        int totalSNPSize = lstInputSNP.size();       
        int snpOrder = 0;
        
        //Initiate genotype according to idividuals. 
        for(int i=0;i<lstInputIndividual.size();i++){
            StatusGtySet sgsOne=new StatusGtySet();
            sgsOne.paternalChrom=new BitVector(lstInputSNP.size());
            sgsOne.maternalChrom=new BitVector(lstInputSNP.size());
//            sgsOne.existence=new BitVector(lstInputSNP.size());
            lstInputGenotype.add(sgsOne);
        }
        
        //Set input stream. 
        boolean bfile_SNP_major = false;
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(pdsRealData.getPlinkBinaryFileName())));
        
        //Judge the vertion. 
        byte bt = in.readByte();
        boolean v1_bfile = true;
        boolean[] b = new boolean[8];
        for (int i = 7; i >= 0; i--) {
            if (((1 << i) & bt) != 0) {
                b[i] = true;
            } else {
                b[i] = false;
            }
        }        
        
        // If v1.00 file format
        // Magic numbers for .bed file: 00110110 11011000 = v1.00 bed file
        if ((b[2] && b[3] && b[5] && b[6]) && !(b[0] || b[1] || b[4] || b[7])) {
            // Next number
            bt = in.readByte();
            for (int i = 7; i >= 0; i--) {
                if (((1 << i) & bt) != 0) {
                    b[i] = true;
                } else {
                    b[i] = false;
                }
            }
            // printBitSet(b);
            if ((b[0] && b[1] && b[3] && b[4]) && !(b[2] || b[5] || b[6] || b[7])) {
                // Read SNP/Ind major coding
                bt = in.readByte();
                for (int i = 7; i >= 0; i--) {
                    if (((1 << i) & bt) != 0) {
                        b[i] = true;
                    } else {
                        b[i] = false;
                    }
                }
                if (b[0]) {
                    bfile_SNP_major = true;
                } else {
                    bfile_SNP_major = false;
                }

                if (bfile_SNP_major) {
                    logInfor.append("Detected that Plink binary PED file is v1.00 SNP-major mode\n");
                } else {
                    logInfor.append("Detected that Plink binary PED file is v1.00 individual-major mode\n");
                    logInfor.append("KGG now does not support  individual-major mode");
                    LOG.warning(logInfor.toString());
                    return false;
                }
            } else {
                v1_bfile = false;
            }
        } else {
            v1_bfile = false;
        }
       
        // Reset file if < v1
        if (!bfile_SNP_major) {
            logInfor.append("Warning, old BED file <v1.00 : will try to recover...\n");
            logInfor.append("  bs.get(t you should --make-bs.get(d from PED )\n");
            in.close();
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(pdsRealData.getPlinkBinaryFileName())));
            bt = in.readByte();
            for (int i = 7; i >= 0; i--) {
                if (((1 << i) & bt) != 0) {
                    b[i] = true;
                } else {
                    b[i] = false;
                }
            }
        }

        // If 0.99 file format
        if ((!v1_bfile) && (b[1] || b[2] || b[3] || b[4] || b[5] || b[6] || b[7])) {
            logInfor.append("\n *** Possibs.get(e probs.get(em: guessing that BED is < v0.99      *** \n");
            logInfor.append(" *** High chance of data corruption, spurious results    *** \n");
            logInfor.append(" *** Unles you are _sure_ this really is an old BED file *** \n");
            logInfor.append(" *** you should recreate PED -> BED                      *** \n\n");

            bfile_SNP_major = false;
            in.close();
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(pdsRealData.getPlinkBinaryFileName())));
        } else if (!v1_bfile) {
            if (b[0]) {
                bfile_SNP_major = true;
            } else {
                bfile_SNP_major = false;
            }

            logInfor.append("Binary PED file is v0.99\n");
            if (bfile_SNP_major) {
                logInfor.append("Detected that binary PED file is in SNP-major mode\n");
            } else {
                logInfor.append("Detected that binary PED file is in individual-major mode\n");
            }
        }
        //System.out.println(logInfor);
        // GlobalManager.mainView.insertBriefRunningInfor(logInfor.toString(), true);
        LOG.info(logInfor.toString());

        int indiviNum = lstInputIndividual.size();
        ///////////////////////////
        // SNP-major mode
        if (bfile_SNP_major) {
            snpOrder = 0;
            int byteNumPerSNP = indiviNum / 4;
            if (indiviNum % 4 > 0) {
                byteNumPerSNP++;
            }
            int currentBytePoint = 0;
            int shouldBytePoint = 0;
            for (int i = 0; i < totalSNPSize; i++) {
                SNP snp = lstInputSNP.get(i);

                shouldBytePoint = snp.genotypeOrder * byteNumPerSNP;
                if (currentBytePoint < shouldBytePoint) {
                    in.skipBytes(shouldBytePoint - currentBytePoint);
                    currentBytePoint = shouldBytePoint;
                }
                snp.genotypeOrder = snpOrder;
                int k = 0;
                while (k < indiviNum) {
                    bt = in.readByte();
                    currentBytePoint++;
                    for (int s = 7; s >= 0; s--) {
                        if (((1 << s) & bt) != 0) {
                            b[s] = true;
                        } else {
                            b[s] = false;
                        }
                    }

                    // printBitSet(b);
                    for (int s = 0; s < 8; s += 2) {
                        StatusGtySet sGty = lstInputGenotype.get(k);
                        if (!b[s] && b[s + 1]) {
//                            sGty.existence.putQuick(i, true);
                            sGty.paternalChrom.putQuick(i, true);
                        } else if (!b[s] && !b[s + 1]) {
//                            sGty.existence.putQuick(i, true);
                        } else if (b[s] && b[s + 1]) {
//                            sGty.existence.putQuick(i, true);
                            sGty.maternalChrom.putQuick(i, true);
                            sGty.paternalChrom.putQuick(i, true);
                        }
                        k++;
                        if (k == indiviNum) {
                            break;
                        }
                    }
                }
                snpOrder++;
            }
        } else {

            int j = -1;
            int currentSNPNum = 0;

            snpOrder = 0;
            int s = 0;

            //This code has not been tested yet because the latest version generate SNP-major model
            ///////////////////////////
            // individual-major mode
            for (int k = 0; k < indiviNum; k++) {
                currentSNPNum = 0;
                StatusGtySet sGty = lstInputGenotype.get(k);
                for (int i = 0; i < totalSNPSize; i++) {
                    SNP snp = lstInputSNP.get(i);
                    while (currentSNPNum < snp.genotypeOrder) {
                        bt = in.readByte();
                        for (int t = 7; t >= 0; t--) {
                            if (((1 << t) & bt) != 0) {
                                b[t] = true;
                            } else {
                                b[t] = false;
                            }
                        }
                        currentSNPNum += 4;
                    }
                    s = (snp.genotypeOrder % 4) * 2;
                    if (!b[s] && b[s + 1]) {
//                        sGty.existence.putQuick(i, true);
                        sGty.paternalChrom.putQuick(i, true);
                    } else if (!b[s] && !b[s + 1]) {
//                        sGty.existence.putQuick(i, true);
                    } else if (b[s] && b[s + 1]) {
//                        sGty.existence.putQuick(i, true);
                        sGty.maternalChrom.putQuick(i, true);
                        sGty.paternalChrom.putQuick(i, true);
                    }
                    snpOrder++;
                }
            }
            for (int i = 0; i < totalSNPSize; i++) {
                SNP snp = lstInputSNP.get(i);
                snp.genotypeOrder = i;
            }
        }
        in.close();
        return false;        
    }
    
    public void calculateMAF(){
        //Read all the information, but use part of that. 
        if(intMarkerNum>lstInputSNP.size())    return;
        
        int[] intAlleleCount=new int[intMarkerNum];
        int[] intIndividualCount=new int[intMarkerNum];
        Arrays.fill(intAlleleCount, 0);
        Arrays.fill(intIndividualCount,0);
        
        for(int i=0;i<lstInputGenotype.size();i++){
            StatusGtySet sgsOne=lstInputGenotype.get(i);
            for(int j=0;j<intMarkerNum;j++){
//                if(sgsOne.existence.getQuick(j)){
                    if(sgsOne.paternalChrom.getQuick(j))    intAlleleCount[j]++;
                    if(sgsOne.maternalChrom.getQuick(j))    intAlleleCount[j]++;
                    intIndividualCount[j]++;
//                }                  
            }
        }
        
        for(int i=0;i<intMarkerNum;i++){
            double dblFrequence=(double)intAlleleCount[i]/(intIndividualCount[i]*2);
            if(dblFrequence>0.5)    dblFrequence=1-dblFrequence;
            lstMAF.add(dblFrequence);
        }        
    }
    
    public void calculateRSquareMatrix(){
        //Read all the information, but use part of that.
        if(intMarkerNum>lstInputSNP.size())    return;
        
        dblLD=new double[intMarkerNum][intMarkerNum];
        DoubleArrayList dalSNP1=new DoubleArrayList(intMarkerNum);
        DoubleArrayList dalSNP2=new DoubleArrayList(intMarkerNum);
        double mean1, mean2, sd1, sd2, r;
        
        for(int i=0;i<intMarkerNum;i++){
            
            SNP snp1=lstInputSNP.get(i);
            if(snp1.genotypeOrder<0)    continue;
            
            for(int j=i+1;j<intMarkerNum;j++){  
                
                SNP snp2=lstInputSNP.get(j);
                if(snp2.genotypeOrder<0)    continue; 
                
                dalSNP1.clear();
                dalSNP2.clear();
                
                if (snp1.physicalPosition == snp2.physicalPosition)     System.out.println(snp1.rsID + " " + snp2.rsID + " " + snp1.physicalPosition + " same");
                
                for(int k=0;k<lstInputGenotype.size();k++){
                    StatusGtySet sgsOne=lstInputGenotype.get(k);
//                    if (sgsOne.existence.getQuick(snp1.genotypeOrder) && sgsOne.existence.getQuick(snp2.genotypeOrder)) {
                        if (!sgsOne.paternalChrom.getQuick(snp1.genotypeOrder) && !sgsOne.maternalChrom.getQuick(snp1.genotypeOrder)) {
                            dalSNP1.add(0);
                        } else if (sgsOne.paternalChrom.getQuick(snp1.genotypeOrder) && sgsOne.maternalChrom.getQuick(snp1.genotypeOrder)) {
                            dalSNP1.add(2);
                        } else {
                            dalSNP1.add(1);
                        }

                        if (!sgsOne.paternalChrom.getQuick(snp2.genotypeOrder) && !sgsOne.maternalChrom.getQuick(snp2.genotypeOrder)) {
                            dalSNP2.add(0);
                        } else if (sgsOne.paternalChrom.getQuick(snp2.genotypeOrder) && sgsOne.maternalChrom.getQuick(snp2.genotypeOrder)) {
                            dalSNP2.add(2);
                        } else {
                            dalSNP2.add(1);
                        }
//                    }                    
                }
                
                if (dalSNP1.isEmpty() || dalSNP2.isEmpty())     continue;
                
                mean1 = Descriptive.mean(dalSNP1);
                mean2 = Descriptive.mean(dalSNP2);
                sd1 = Descriptive.sampleVariance(dalSNP1, mean1);
                sd2 = Descriptive.sampleVariance(dalSNP2, mean2);
                r = Descriptive.correlation(dalSNP1, Math.sqrt(sd1), dalSNP2, Math.sqrt(sd2));                
                
                if (Double.isNaN(r))    continue;
                r = r * r; 
                dblLD[i][j]=dblLD[j][i]=r;
            }   
            dblLD[i][i]=1;
        }
    }
    
    public String[] getSNPNameSet(){
        String[] strNames=new String[intMarkerNum];
        for(int i=0;i<intMarkerNum;i++)    strNames[i]=lstInputSNP.get(i).getRsID();
        return strNames;
    }

    public double[][] getDblLD() {
        return dblLD;
    }

    public List<Double> getLstMAF() {
        return lstMAF;
    }
    
    public void readEverythingFromVcfFile() throws Exception {
        //Set input vcf file. 
        File vcfFile = new File(strVCF);
        if (!vcfFile.exists()) {
            return;
        }
        
        //Set string reading buffer. 
        StringBuilder runningInfo = new StringBuilder();
        StringBuilder tmpBuffer = new StringBuilder();

//        boolean needConvert = false;
//        if (liftOver != null) {
//            needConvert = true;
//        }

        int indexCHROM = -1;
        int indexPOS = -1;
        int indexID = -1;
        int indexREF = -1;
        int indexALT = -1;
        int indexQUAL = -1;
        int indexFILTER = -1;
        int indexFORMAT = -1;
        int indexINFO = -1;

//        int availabeSNPSpace = positionSet.size();

        LineReader br = null;
        if (vcfFile.exists() && vcfFile.getName().endsWith(".zip")) {
            br = new CompressedFileReader(vcfFile);
        } else {
            if (vcfFile.exists() && vcfFile.getName().endsWith(".gz")) {
                br = new CompressedFileReader(vcfFile);
            } else {
                if (vcfFile.exists()) {
                    br = new AsciiLineReader(vcfFile);
                } else {
                    throw new Exception("No input file: " + vcfFile.getCanonicalPath());
                }
            }
        }

        //skip the first line
        // br.readLine();
        int curSNPNum = 0;
        String line;
        int posIndex = 2;
        int maxIndex = posIndex;
        int index = 0;

        List<SNP> mappedSNPList = new ArrayList<SNP>();
        String delmilit = "\t ,";
        //start to read genotypes
        int effectiveSNPGtyNum = 0;

        //skip to the head line 
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#CHROM")) {
                break;
            }
        }
        if (line == null) {
            br.close();
            return;
        }

        //parse head line
        StringTokenizer st = new StringTokenizer(line.trim());

        int iCol = 0;
        int indivNum = 0;
        double[] gtyQuality = null;
        String[] gtys = null;
        int[] gtyDepth = null;
        float[] altReadRatio = null;

        //#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  
//chr1    109     .       A       T       237.97  PASS    AC=21;AF=0.328;AN=64;DP=47;Dels=0.02;HRun=0;HaplotypeScore=1.9147;MQ=44.81;MQ0=48;QD=5.53;SB=-28.76;sumGLbyD=9.00       GT:AD:DP:GQ:PL  0/1:6,1:3:15.67:16,0,64 0/0:3,0:1:3.01:0,3,33 
        while (st.hasMoreTokens()) {
            // System.out.println(st);
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(st.nextToken().trim());
            //  System.out.println(tmpBuffer); 
            if (tmpBuffer.toString().equals("#CHROM")) {
                indexCHROM = iCol;
            } else if (tmpBuffer.toString().equals("POS")) {
                indexPOS = iCol;
            } else if (tmpBuffer.toString().equals("ID")) {
                indexID = iCol;
            } else if (tmpBuffer.toString().equals("REF")) {
                indexREF = iCol;
            } else if (tmpBuffer.toString().equals("ALT")) {
                indexALT = iCol;
            } else if (tmpBuffer.toString().equals("QUAL")) {
                indexQUAL = iCol;
            } else if (tmpBuffer.toString().equals("FILTER")) {
                indexFILTER = iCol;
            } else if (tmpBuffer.toString().equals("INFO")) {
                indexINFO = iCol;
            } else if (tmpBuffer.toString().equals("FORMAT")) { 
                //warning: assume the FORMAT is the last meta column
                indexFORMAT = iCol;
                indivNum = 0;

                while (st.hasMoreTokens()) {
                    tmpBuffer.delete(0, tmpBuffer.length());
                    tmpBuffer.append(st.nextToken().trim());

                    StatusGtySet sGty = new StatusGtySetExtension();
                    //The individual information seems unnecessary! If not, add this here. 
                    lstInputGenotype.add(sGty);
                    indivNum++;
                }
                break;
            }
            iCol++;
        }

        if (indivNum > 0) {
            gtyQuality = new double[indivNum];
            gtys = new String[indivNum];
            gtyDepth = new int[indivNum];
            altReadRatio = new float[indivNum];
            Arrays.fill(gtys, null);
            Arrays.fill(gtyQuality, 0);
            Arrays.fill(gtyDepth, 0);
            Arrays.fill(altReadRatio, 0);
        }

        int indexA, indexB;
        int maxColNum = indexCHROM;
        maxColNum = Math.max(maxColNum, indexPOS);
        maxColNum = Math.max(maxColNum, indexID);
        maxColNum = Math.max(maxColNum, indexALT);
        maxColNum = Math.max(maxColNum, indexREF);
        maxColNum = Math.max(maxColNum, indexFORMAT);
        String delimiter = "\t";
        int alleleNum = 0;
        String tmpStr = null;
        boolean isPhased = false;
        boolean incomplete = false;
        String currChr = null;
        int makerPostion = -1;
        String ref = null;
        String alt = null;
        String strSNPID=null;

        int gtyIndexInInfor = -1;
        int gtyQualIndexInInfor = -1;
        int gtyDepthIndexInInfor = -1;
        int gtyAlleleDepthIndexInInfor = -1;
        int gtyAltAlleleFracIndexInInfor = -1;

        boolean hasIndexGT = false;
        boolean hasIndexGQ = false;
        boolean hasIndexDP = false;
        boolean hasIndexAD = false;
        boolean hasIndexFA = false;

        int ignoredLowQualGtyNum = 0;
        double avgSeqQuality = 0;
        int ignoredBadAltFracGtyNum = 0;

        //temp variables
        int iGty = 0;
        int index1 = 0;
        int index2 = 0;
        int colonNum = 0;
        int len = tmpBuffer.length();
        int ignoredLowDepthGtyNum = 0;

        double gtyQualityThrehsold = 0;
        int minSeqDepth = 0;
        double altAlleleFracHomRefThrehsold = 1;
        double altAlleleFractAltHetHomThrehsold = 0;

        String depA = null;
        String depB = null;

        if ((line = br.readLine()) != null) {
            //  bw.write(line);                bw.write("\n");
            //decide the whether genotypes are phased or not //at most consider 3 alternative alleles
            if (line.contains("0|0") || line.contains("0|0") || line.contains("0|1") || line.contains("1|0") || line.contains("0|2") || line.contains("2|0") || line.contains("0|3") || line.contains("3|0")
                    || line.contains("1|1") || line.contains("1|2") || line.contains("2|1") || line.contains("1|3") || line.contains("3|1")
                    || line.contains("2|2") || line.contains("2|3") || line.contains("3|2")
                    || line.contains("3|3")) {
                isPhased = true;
            }

            do {
                // System.out.println(line); 
                st = new StringTokenizer(line.trim(), delimiter);
                //initialize varaibles
                incomplete = true;
                currChr = null;
                makerPostion = -1;
                ref = null;

                alt = null;
                gtyIndexInInfor = -1;
                gtyQualIndexInInfor = -1;
                gtyDepthIndexInInfor = -1;
                gtyAlleleDepthIndexInInfor = -1;
                gtyAltAlleleFracIndexInInfor = -1;

                hasIndexGT = false;
                hasIndexGQ = false;
                hasIndexDP = false;
                hasIndexAD = false;
                hasIndexFA = false;
                if (indivNum > 0) {
                    Arrays.fill(gtys, null);
                    Arrays.fill(gtyQuality, 0);
                    Arrays.fill(gtyDepth, 0);
                    Arrays.fill(altReadRatio, 0);
                }

//#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  
//chr1    109     .       A       T       237.97  PASS    AC=21;AF=0.328;AN=64;DP=47;Dels=0.02;HRun=0;HaplotypeScore=1.9147;MQ=44.81;MQ0=48;QD=5.53;SB=-28.76;sumGLbyD=9.00       GT:AD:DP:GQ:PL  0/1:6,1:3:15.67:16,0,64 0/0:3,0:1:3.01:0,3,33 
                for (iCol = 0; iCol <= maxColNum; iCol++) {
                    if (st.hasMoreTokens()) {
                        tmpBuffer.delete(0, tmpBuffer.length());
                        tmpBuffer.append(st.nextToken().trim());

                        if (iCol == indexCHROM) {
                            if (tmpBuffer.charAt(tmpBuffer.length() - 1) == 'T' || tmpBuffer.charAt(tmpBuffer.length() - 1) == 't') {
                                currChr = tmpBuffer.substring(0, tmpBuffer.length() - 1);
                            } else {
                                currChr = tmpBuffer.toString();
                            }
                        } else if (iCol == indexPOS) {
                            makerPostion = Integer.parseInt(tmpBuffer.toString());
                            // System.out.println(makerPostion); 
                        } else if (iCol == indexALT) {
                            alt = tmpBuffer.toString();
                        }else if (iCol==indexID) {
                            strSNPID = tmpBuffer.toString();
                        } else if (iCol == indexREF) {
                            ref = tmpBuffer.toString();
                        } else if (iCol == indexFORMAT) {
                            //the meta order maybe not consistent across rows this is troublesome and maybe 
                            // if (gtyIndexInInfor < 0 || gtyQualIndexInInfor < 0)
                            {
                                StringTokenizer st1 = new StringTokenizer(tmpBuffer.toString(), ":");
                                int i = 0;
                                while (st1.hasMoreTokens()) {
                                    String label = st1.nextToken();
                                    if (label.equals("GT")) {
                                        gtyIndexInInfor = i;
                                        hasIndexGT = true;
                                    } else if (label.equals("GQ")) {
                                        gtyQualIndexInInfor = i;
                                        hasIndexGQ = true;
                                    } else if (label.equals("DP")) {
                                        gtyDepthIndexInInfor = i;
                                        hasIndexDP = true;
                                    } else if (label.equals("AD")) {
                                        gtyAlleleDepthIndexInInfor = i;
                                        hasIndexAD = true;
                                    } else if (label.equals("FA")) {
                                        gtyAltAlleleFracIndexInInfor = i;
                                        hasIndexFA = true;
                                    }

                                    /*
                                     if (gtyIndexInInfor >= 0 && gtyQualIndexInInfor >= 0 && gtyDepthIndexInInfor >= 0) {
                                     break;
                                     }
                                     * 
                                     */
                                    i++;
                                }
                            }

                            if (indivNum <= 0) {
                                indivNum = st.countTokens() - iCol;
                                gtyQuality = new double[indivNum];
                                gtys = new String[indivNum];
                                gtyDepth = new int[indivNum];
                                altReadRatio = new float[indivNum];
                                Arrays.fill(gtys, null);
                                Arrays.fill(gtyQuality, 0);
                                Arrays.fill(gtyDepth, 0);
                                Arrays.fill(altReadRatio, 0);

                                StatusGtySet sGty = new StatusGtySetExtension();
                                lstInputGenotype.add(sGty);
                            }
                            //1/1:0,2:2:6.02:70,6,0	./.
                            iGty = 0;
                            while (st.hasMoreTokens()) {
                                tmpBuffer.delete(0, tmpBuffer.length());
                                tmpBuffer.append(st.nextToken().trim());
                                if (tmpBuffer.charAt(0) == '"') {
                                    tmpStr = tmpBuffer.substring(1, tmpBuffer.length() - 1);
                                    tmpBuffer.delete(0, tmpBuffer.length());
                                    tmpBuffer.append(tmpStr);
                                }
                                if (tmpBuffer.toString().equals("./.") || tmpBuffer.toString().equals(".|.")) {
                                    gtys[iGty] = null;
                                    iGty++;
                                    continue;
                                }
                                index1 = 0;
                                index2 = 0;
                                colonNum = 0;
                                len = tmpBuffer.length();
                                while (index2 < len) {
                                    if (tmpBuffer.charAt(index2) == ':') {
                                        if (colonNum == gtyIndexInInfor) {
                                            gtys[iGty] = tmpBuffer.substring(index1, index2);

                                        } else if (colonNum == gtyQualIndexInInfor) {
                                            if (tmpBuffer.charAt(index1) == '.') {
                                                gtyQuality[iGty] = 0;
                                            } else {
                                                gtyQuality[iGty] = Double.parseDouble(tmpBuffer.substring(index1, index2));
                                            }
                                        } else if (colonNum == gtyDepthIndexInInfor) {
                                            if (tmpBuffer.charAt(index1) == '.') {
                                                gtyDepth[iGty] = 0;
                                            } else {
                                                gtyDepth[iGty] = Integer.parseInt(tmpBuffer.substring(index1, index2));
                                            }
                                        } else if (!hasIndexFA && colonNum == gtyAlleleDepthIndexInInfor) {
                                            if (tmpBuffer.charAt(index1) == '.') {
                                                altReadRatio[iGty] = Float.NaN;
                                            } else {
                                                String allRead = tmpBuffer.substring(index1, index2);
                                                indexA = allRead.indexOf(',');
                                                indexB = allRead.lastIndexOf(',');
                                                //when more than 2 alleles only consider the first ahd the last allele
                                                depA = allRead.substring(0, indexA);
                                                depB = allRead.substring(indexB + 1);
                                                if (depA.equals(".")) {
                                                    depA = "0";
                                                }
                                                if (depB.equals(".")) {
                                                    depB = "0";
                                                }
                                                altReadRatio[iGty] = Float.parseFloat(depB) / (Float.parseFloat(depB) + Float.parseFloat(depA));
                                            }
                                        } else if (colonNum == gtyAltAlleleFracIndexInInfor) {
                                            if (tmpBuffer.charAt(index1) == '.') {
                                                altReadRatio[iGty] = Float.NaN;
                                            } else {
                                                altReadRatio[iGty] = Float.parseFloat(tmpBuffer.substring(index1, index2));
                                            }
                                        }
                                        index1 = index2 + 1;
                                        colonNum++;
                                    }
                                    index2++;
                                }
                                //the last column
                                if (colonNum == gtyIndexInInfor) {
                                    gtys[iGty] = tmpBuffer.substring(index1, index2);
                                } else if (colonNum == gtyQualIndexInInfor) {
                                    if (tmpBuffer.charAt(index1) == '.') {
                                        gtyQuality[iGty] = 0;
                                    } else {
                                        gtyQuality[iGty] = Double.parseDouble(tmpBuffer.substring(index1, index2));
                                    }
                                } else if (colonNum == gtyDepthIndexInInfor) {
                                    if (tmpBuffer.charAt(index1) == '.') {
                                        gtyDepth[iGty] = 0;
                                    } else {
                                        gtyDepth[iGty] = Integer.parseInt(tmpBuffer.substring(index1, index2));
                                    }
                                } else if (!hasIndexFA && colonNum == gtyAlleleDepthIndexInInfor) {
                                    if (tmpBuffer.charAt(index1) == '.') {
                                        altReadRatio[iGty] = Float.NaN;
                                    } else {
                                        String allRead = tmpBuffer.substring(index1, index2);
                                        indexA = allRead.indexOf(',');
                                        indexB = allRead.lastIndexOf(',');
                                        //when more than 2 alleles only consider the first ahd the last allele
                                        depA = allRead.substring(0, indexA);
                                        depB = allRead.substring(indexB + 1);
                                        if (depA.equals(".")) {
                                            depA = "0";
                                        }
                                        if (depB.equals(".")) {
                                            depB = "0";
                                        }
                                        altReadRatio[iGty] = Float.parseFloat(depB) / (Float.parseFloat(depB) + Float.parseFloat(depA));
                                    }
                                } else if (colonNum == gtyAltAlleleFracIndexInInfor) {
                                    if (tmpBuffer.charAt(index1) == '.') {
                                        altReadRatio[iGty] = Float.NaN;
                                    } else {
                                        altReadRatio[iGty] = Float.parseFloat(tmpBuffer.substring(index1, index2));
                                    }
                                }
                                iGty++;
                            }
                        }
                        //simply remove the indel as the snv and indel can have the same position
                        if (ref != null && ref.length() > 1) {
                            break;
                        }
                        if (alt != null && alt.length() > 1) {
                            break;
                        }
                    } else {
                        break;
                    }
                    if (iCol >= maxColNum) {
                        incomplete = false;
                    }

                }
                if (incomplete) {
                    continue;
                }
                if (currChr.startsWith("chr")) {
                    currChr = currChr.substring(3);
                }

                int invalidGtyNum = 0;
                //check if there are variants and genotypes with low quality 
                for (index = indivNum - 1; index >= 0; index--) {
                    //ignore variants with missing genotypes
                    if (gtys[index] == null || gtys[index].charAt(0) == '.' || gtys[index].charAt(2) == '.') {
                        invalidGtyNum++;
                        continue;
                    }

                    if (hasIndexGQ && gtyQuality[index] < gtyQualityThrehsold) {
                        ignoredLowQualGtyNum++;
                        gtys[index] = null;
                        invalidGtyNum++;
                        continue;
                    }
                    if (hasIndexDP && gtyDepth[index] < minSeqDepth) {
                        ignoredLowDepthGtyNum++;
                        gtys[index] = null;
                        invalidGtyNum++;
                        continue;
                    }

                    if (hasIndexAD || hasIndexFA) {
                        if (Float.isNaN(altReadRatio[index])) {
                            ignoredBadAltFracGtyNum++;
                            gtys[index] = null;
                            invalidGtyNum++;
                            continue;
                        } else {
                            if (gtys[index].charAt(0) == '0' && gtys[index].charAt(2) == '0') {
                                if (altReadRatio[index] > altAlleleFracHomRefThrehsold) {
                                    ignoredBadAltFracGtyNum++;
                                    gtys[index] = null;
                                    invalidGtyNum++;
                                    continue;
                                }
                            } else {
                                if (altReadRatio[index] < altAlleleFractAltHetHomThrehsold) {
                                    ignoredBadAltFracGtyNum++;
                                    gtys[index] = null;
                                    invalidGtyNum++;
                                    continue;
                                }
                            }
                        }
                    }
                }

                if (invalidGtyNum == indivNum) {
                    continue;
                }

                //ignore the variants with multiple alleles 
                String[] alts = alt.split(",");                
                if (alts.length > 1) { 
                    continue;   //This situation should be take care of. 
                }
                
                SNP snp1 = new SNP(makerPostion);
                snp1.genotypeOrder = effectiveSNPGtyNum;
                snp1.rsID=strSNPID;
                lstInputSNP.add(snp1);


//                if (availabeSNPSpace <= effectiveSNPGtyNum) {
//                    availabeSNPSpace += 50000;
//                    for (StatusGtySet gtySet : subjectGtyList) {
//                        gtySet.existence.setSize(availabeSNPSpace);
//                        gtySet.paternalChrom.setSize(availabeSNPSpace);
//                        gtySet.maternalChrom.setSize(availabeSNPSpace);
//                    }
//                }
                // assume all only have two alleles. 
                for (index = 0; index < indivNum; index++) {
                    StatusGtySetExtension sGty = (StatusGtySetExtension)lstInputGenotype.get(index);
                    if (gtys[index] == null) {
                        //sGty.existence.putQuick(effectiveSNPGtyNum, false);
                        sGty.lstExistence.add(effectiveSNPGtyNum, false);
                        sGty.lstPaternalGty.add(effectiveSNPGtyNum, false);//Is this necessary?
                        sGty.lstMaternalGty.add(effectiveSNPGtyNum, false);                        
                    } else if (gtys[index].equals("0|0")) {
//                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
//                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
//                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        sGty.lstExistence.add(effectiveSNPGtyNum, true);
                        sGty.lstPaternalGty.add(effectiveSNPGtyNum, false);
                        sGty.lstMaternalGty.add(effectiveSNPGtyNum, false);
                    } else if (gtys[index].equals("0|1")) {
//                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
//                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
//                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        sGty.lstExistence.add(effectiveSNPGtyNum, true);
                        sGty.lstPaternalGty.add(effectiveSNPGtyNum, false);
                        sGty.lstMaternalGty.add(effectiveSNPGtyNum, true);
                    } else if (gtys[index].equals("1|0")) {
//                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
//                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
//                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        sGty.lstExistence.add(effectiveSNPGtyNum, true);
                        sGty.lstPaternalGty.add(effectiveSNPGtyNum, true);
                        sGty.lstMaternalGty.add(effectiveSNPGtyNum, false);                        
                    } else if (gtys[index].equals("1|1")) {
//                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
//                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
//                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        sGty.lstExistence.add(effectiveSNPGtyNum, true);
                        sGty.lstPaternalGty.add(effectiveSNPGtyNum, true);
                        sGty.lstMaternalGty.add(effectiveSNPGtyNum, true);                          
                    }                    
                }
                effectiveSNPGtyNum++;
            } while ((line = br.readLine()) != null);
            br.close();
        }
        
        for(int i=0;i<lstInputGenotype.size();i++){
            StatusGtySetExtension sGty = (StatusGtySetExtension)lstInputGenotype.get(i);
            sGty.mergeBack();
        }
    }   
    
}
