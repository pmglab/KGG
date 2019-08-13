/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import cern.colt.bitvector.BitVector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import net.sf.picard.liftover.LiftOver;
import net.sf.picard.util.Interval;
import net.sf.samtools.util.AsciiLineReader;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import org.cobi.util.text.Util;

/**
 *
 * @author mxli
 */
public class HaplotypeDataset {

    private final static Logger LOG = Logger.getLogger(HaplotypeDataset.class.getName());
    String haplotypeMapFilePath;
    String haplotypeGenotypeFilePath;
    public static char MISSING_ALLELE_NAME = 'X';
    public static char MISSING_STRAND_NAME = '0';

    public HaplotypeDataset() {
    }

    public List<SNP> readSNPMapFileBySNPList(Chromosome chromosome, File[] mapHapFiles, String chromName,
            Map<String, int[]> positionGenomeIndexes, LiftOver liftOver) throws Exception {

        File mapFile = mapHapFiles[0];
        if (!mapFile.exists()) {
            return null;
        }
        StringBuilder runningInfo = new StringBuilder();
        StringBuilder tmpBuffer = new StringBuilder();

        List<Gene> genes = chromosome.genes;
        List<SNP> snpOutGenes = chromosome.snpsOutGenes;

        boolean needConvert = false;
        if (liftOver != null) {
            needConvert = true;
        }

        BufferedReader brMap = new BufferedReader(new FileReader(mapFile));
        //skip the first line
        // br.readLine();
        int lineCounter = -1;
        boolean invalid = true;
        int curSNPNum = 0;
        String line;
        int posIndex = 2;
        int maxIndex = posIndex;
        int index = 0;
        int pos = 0;
        String delmilit = "\t ,";
        //start to read genotypes
        int effectiveSNPNum = 0;
        List<SNP> snpList = new ArrayList<SNP>();
        while ((line = brMap.readLine()) != null) {
            lineCounter++;
            StringTokenizer tokenizer = new StringTokenizer(line);
            invalid = false;
            index = 0;
            while (tokenizer.hasMoreTokens()) {
                tmpBuffer.delete(0, tmpBuffer.length());
                tmpBuffer.append(tokenizer.nextToken().trim());

                if (index == posIndex) {
                    //filter physical region
                    pos = Integer.parseInt(tmpBuffer.toString());

                    if (needConvert && pos > 0) {
                        Interval interval = new Interval("chr" + chromName, pos, pos);
                        Interval int2 = liftOver.liftOver(interval);
                        if (int2 != null) {
                            pos = int2.getStart();
                        }
                    }

                } else if (index > maxIndex) {
                    break;
                }
                index++;
            }
            if (invalid) {
                continue;
            }

            effectiveSNPNum++;
            int[] poses = positionGenomeIndexes.get(chromName + ":" + pos);
            SNP snp1;
            if (poses != null) {
                if (poses[1] == -1) {
                    //a snp outside of gene
                    snp1 = snpOutGenes.get(poses[2]);
                } else {
                    snp1 = genes.get(poses[1]).snps.get(poses[2]);
                }
                snp1.genotypeOrder = lineCounter;
                snpList.add(snp1);
            }
        }
        brMap.close();

        runningInfo.append("The number of SNPs on chromosome ");
        runningInfo.append(chromName);
        runningInfo.append(" in map file ");
        runningInfo.append(mapFile.getName());
        runningInfo.append(" is ");
        runningInfo.append(snpList.size());
        runningInfo.append(".");
        // GlobalManager.mainView.insertBriefRunningInfor(runningInfo.toString(), true);
        LOG.info(runningInfo.toString());
        return snpList;
    }

    public Set<Integer> readSNPMapHapVCFFileBySNPList(Chromosome chromosome, File vcfFile, String chromName,
            Map<String, int[]> positionGenomeIndexes, LiftOver liftOver, List<StatusGtySet> subjectGtyList, boolean[] isPhased) throws Exception {
        if (!vcfFile.exists()) {
            return null;
        }
        StringBuilder runningInfo = new StringBuilder();
        StringBuilder tmpBuffer = new StringBuilder();

        Set<Integer> variantsWithLD = new HashSet<Integer>();
        List<Gene> genes = chromosome.genes;
        //List<SNP> snpOutGenes = chromosome.snpsOutGenes;

        boolean needConvert = false;
        if (liftOver != null) {
            needConvert = true;
        }

        int indexCHROM = -1;
        int indexPOS = -1;
        int indexID = -1;
        int indexREF = -1;
        int indexALT = -1;
        int indexQUAL = -1;
        int indexFILTER = -1;
        int indexFORMAT = -1;
        int indexINFO = -1;

        int availabeSNPSpace = 100;

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
            return variantsWithLD;
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

                    StatusGtySet sGty = new StatusGtySet();
                    sGty.existence = new BitVector(availabeSNPSpace);
                    sGty.paternalChrom = new BitVector(availabeSNPSpace);
                    sGty.maternalChrom = new BitVector(availabeSNPSpace);
                    subjectGtyList.add(sGty);
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

        boolean incomplete = false;
        String currChr = null;
        int makerPostion = -1;
        String ref = null;
        String alt = null;

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
        SNP snp1;
        //int haplotypeSNPInGenes = 0;

        if ((line = br.readLine()) != null) {
            //  bw.write(line);                bw.write("\n");
            //decide the whether genotypes are phased or not //at most consider 3 alternative alleles
            if (line.contains("0|0") || line.contains("0|0") || line.contains("0|1") || line.contains("1|0") || line.contains("0|2") || line.contains("2|0") || line.contains("0|3") || line.contains("3|0")
                    || line.contains("1|1") || line.contains("1|2") || line.contains("2|1") || line.contains("1|3") || line.contains("3|1")
                    || line.contains("2|2") || line.contains("2|3") || line.contains("3|2")
                    || line.contains("3|3")) {
                isPhased[0] = true;
            }

            String[] cells = null;
            String[] cells1 = null;
            do {
                // System.out.println(line); 
                // st = new StringTokenizer(line.trim(), delimiter);
                cells = Util.tokenize(line, '\t', indexPOS);
                /*
                 currChr = cells[indexCHROM];
                 if (currChr.charAt(currChr.length() - 1) == 'T' || currChr.charAt(currChr.length() - 1) == 't') {
                 currChr = currChr.substring(0, currChr.length() - 1);
                 }*/

                makerPostion = Integer.parseInt(cells[indexPOS]);

                // System.out.println(makerPostion); 
                if (needConvert && makerPostion > 0) {
                    Interval interval = new Interval("chr" + chromName, makerPostion, makerPostion);
                    Interval int2 = liftOver.liftOver(interval);
                    if (int2 != null) {
                        makerPostion = int2.getStart();
                    }
                }
                //the indel will distored the index
                int[] poses = positionGenomeIndexes.get(chromName + ":" + makerPostion);

                /*
                 //sometimes the 1kg data has a mixture of 1-based or 0-based postions
                 if (poses == null) {
                 poses = positionGenomeIndexes.get(chromName + ":" + (makerPostion + 1));
                 if (poses == null) {
                 poses = positionGenomeIndexes.get(chromName + ":" + (makerPostion - 1));
                 }
                 }
                 */
                if (poses != null) {
                    if (poses[1] == -1) {
                        //a snp outside of gene
                        //snp1 = snpOutGenes.get(poses[2]);
                        continue;
                    } else {
                        snp1 = genes.get(poses[1]).snps.get(poses[2]);
                        variantsWithLD.add(makerPostion);
                    }
                } else {
                    continue;
                }

                cells = Util.tokenize(line, '\t');

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

                alt = cells[indexALT];
                ref = cells[indexREF];
                if (ref != null && ref.length() > 1) {
                    continue;
                }
                if (alt != null && alt.length() > 1) {
                    continue;
                }
                cells1 = Util.tokenize(cells[indexFORMAT], ':');

                for (int i = 0; i < cells1.length; i++) {
                    if (cells1[i].equals("GT")) {
                        gtyIndexInInfor = i;
                        hasIndexGT = true;
                    } else if (cells1[i].equals("GQ")) {
                        gtyQualIndexInInfor = i;
                        hasIndexGQ = true;
                    } else if (cells1[i].equals("DP")) {
                        gtyDepthIndexInInfor = i;
                        hasIndexDP = true;
                    } else if (cells1[i].equals("AD")) {
                        gtyAlleleDepthIndexInInfor = i;
                        hasIndexAD = true;
                    } else if (cells1[i].equals("FA")) {
                        gtyAltAlleleFracIndexInInfor = i;
                        hasIndexFA = true;
                    }
                }
                if (indivNum <= 0) {
                    indivNum = cells.length - indexFORMAT - 1;
                    gtyQuality = new double[indivNum];
                    gtys = new String[indivNum];
                    gtyDepth = new int[indivNum];
                    altReadRatio = new float[indivNum];
                    Arrays.fill(gtys, null);
                    Arrays.fill(gtyQuality, 0);
                    Arrays.fill(gtyDepth, 0);
                    Arrays.fill(altReadRatio, 0);

                    StatusGtySet sGty = new StatusGtySet();
                    sGty.existence = new BitVector(availabeSNPSpace);
                    sGty.paternalChrom = new BitVector(availabeSNPSpace);
                    sGty.maternalChrom = new BitVector(availabeSNPSpace);
                    subjectGtyList.add(sGty);
                }

//#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  
//chr1    109     .       A       T       237.97  PASS    AC=21;AF=0.328;AN=64;DP=47;Dels=0.02;HRun=0;HaplotypeScore=1.9147;MQ=44.81;MQ0=48;QD=5.53;SB=-28.76;sumGLbyD=9.00       GT:AD:DP:GQ:PL  0/1:6,1:3:15.67:16,0,64 0/0:3,0:1:3.01:0,3,33 
                iGty = 0;
                for (iCol = indexFORMAT + 1; iCol < cells.length; iCol++) {
                    tmpBuffer.delete(0, tmpBuffer.length());
                    tmpBuffer.append(cells[iCol]);
                    //1/1:0,2:2:6.02:70,6,0	./.

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

                /*
                 if (currChr.startsWith("chr")) {
                 currChr = currChr.substring(3);
                 }*/
                int invalidGtyNum = 0;
                //check if there are variants and genotypes with low quality 
                for (index = indivNum - 1; index >= 0; index--) {
                    //ignore variants with missing genotypes
                    if (gtys[index] == null || gtys[index].charAt(0) == '.') {
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
                        } else {
                            if (gtys[index].charAt(0) == '0' && gtys[index].charAt(2) == '0') {
                                if (altReadRatio[index] > altAlleleFracHomRefThrehsold) {
                                    ignoredBadAltFracGtyNum++;
                                    gtys[index] = null;
                                    invalidGtyNum++;
                                }
                            } else {
                                if (altReadRatio[index] < altAlleleFractAltHetHomThrehsold) {
                                    ignoredBadAltFracGtyNum++;
                                    gtys[index] = null;
                                    invalidGtyNum++;
                                }
                            }
                        }
                    }
                }

                if (invalidGtyNum == indivNum) {
                    continue;
                }

                String[] alts = alt.split(",");
                //ignore the variants with multiple alleles 
                if (alts.length > 1) {
                    continue;
                }
                if (availabeSNPSpace <= effectiveSNPGtyNum) {
                    availabeSNPSpace += 10000;
                    for (StatusGtySet gtySet : subjectGtyList) {
                        gtySet.existence.setSize(availabeSNPSpace);
                        gtySet.paternalChrom.setSize(availabeSNPSpace);
                        gtySet.maternalChrom.setSize(availabeSNPSpace);
                    }
                }

                // assume all only have two alleles. 
                if (isPhased[0]) {
                    for (index = 0; index < indivNum; index++) {
                        StatusGtySet sGty = subjectGtyList.get(index);
                        if (gtys[index] == null) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("0|0") || gtys[index].equals("0")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("0|1")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        } else if (gtys[index].equals("1|0")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("1|1") || gtys[index].equals("1")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        }
                    }
                } else {
                    for (index = 0; index < indivNum; index++) {
                        StatusGtySet sGty = subjectGtyList.get(index);
                        if (gtys[index] == null) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("0/0") || gtys[index].equals("0")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("0/1")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        } else if (gtys[index].equals("1/0")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("1/1") || gtys[index].equals("1")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        }
                    }
                }
                snp1.genotypeOrder = effectiveSNPGtyNum;
                effectiveSNPGtyNum++;
            } while ((line = br.readLine()) != null);

            br.close();

            runningInfo.append("The number of SNPs within genes on chromosome ");
            runningInfo.append(chromName);
            runningInfo.append(" in the VCF haloptype file ");
            runningInfo.append(vcfFile.getName());
            runningInfo.append(" is ");
            runningInfo.append(variantsWithLD.size());
            runningInfo.append(".");
            //GlobalManager.mainView.insertBriefRunningInfor(runningInfo.toString(), true);
            LOG.info(runningInfo.toString());
        }

        return variantsWithLD;
    }

    public Set<Integer> readSNPMapHapVCFFileBySNPList0(Chromosome chromosome, File vcfFile, String chromName,
            Map<String, int[]> positionGenomeIndexes, LiftOver liftOver, List<StatusGtySet> subjectGtyList, boolean[] isPhased) throws Exception {
        if (!vcfFile.exists()) {
            return null;
        }
        StringBuilder runningInfo = new StringBuilder();
        StringBuilder tmpBuffer = new StringBuilder();

        Set<Integer> variantsWithLD = new HashSet<Integer>();
        List<Gene> genes = chromosome.genes;
        //List<SNP> snpOutGenes = chromosome.snpsOutGenes;

        boolean needConvert = false;
        if (liftOver != null) {
            needConvert = true;
        }

        int indexCHROM = -1;
        int indexPOS = -1;
        int indexID = -1;
        int indexREF = -1;
        int indexALT = -1;
        int indexQUAL = -1;
        int indexFILTER = -1;
        int indexFORMAT = -1;
        int indexINFO = -1;

        int availabeSNPSpace = 100;

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
            return variantsWithLD;
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

                    StatusGtySet sGty = new StatusGtySet();
                    sGty.existence = new BitVector(availabeSNPSpace);
                    sGty.paternalChrom = new BitVector(availabeSNPSpace);
                    sGty.maternalChrom = new BitVector(availabeSNPSpace);
                    subjectGtyList.add(sGty);
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

        boolean incomplete = false;
        String currChr = null;
        int makerPostion = -1;
        String ref = null;
        String alt = null;

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
        SNP snp1;
        //int haplotypeSNPInGenes = 0;

        if ((line = br.readLine()) != null) {
            //  bw.write(line);                bw.write("\n");
            //decide the whether genotypes are phased or not //at most consider 3 alternative alleles
            if (line.contains("0|0") || line.contains("0|0") || line.contains("0|1") || line.contains("1|0") || line.contains("0|2") || line.contains("2|0") || line.contains("0|3") || line.contains("3|0")
                    || line.contains("1|1") || line.contains("1|2") || line.contains("2|1") || line.contains("1|3") || line.contains("3|1")
                    || line.contains("2|2") || line.contains("2|3") || line.contains("3|2")
                    || line.contains("3|3")) {
                isPhased[0] = true;
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

                snp1 = null;
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
                            if (needConvert && makerPostion > 0) {
                                Interval interval = new Interval("chr" + chromName, makerPostion, makerPostion);
                                Interval int2 = liftOver.liftOver(interval);
                                if (int2 != null) {
                                    makerPostion = int2.getStart();
                                }
                            }

                            //the indel will distored the index
                            int[] poses = positionGenomeIndexes.get(chromName + ":" + makerPostion);

                            if (poses != null) {
                                if (poses[1] == -1) {
                                    //a snp outside of gene
                                    //snp1 = snpOutGenes.get(poses[2]);
                                    break;
                                } else {
                                    snp1 = genes.get(poses[1]).snps.get(poses[2]);
                                    variantsWithLD.add(makerPostion);
                                }
                            } else {
                                break;
                            }

                        } else if (iCol == indexALT) {
                            alt = tmpBuffer.toString();
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

                                StatusGtySet sGty = new StatusGtySet();
                                sGty.existence = new BitVector(availabeSNPSpace);
                                sGty.paternalChrom = new BitVector(availabeSNPSpace);
                                sGty.maternalChrom = new BitVector(availabeSNPSpace);
                                subjectGtyList.add(sGty);
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
                    if (gtys[index] == null || gtys[index].charAt(0) == '.') {
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

                String[] alts = alt.split(",");
                //ignore the variants with multiple alleles 
                if (alts.length > 1) {
                    continue;
                }
                if (availabeSNPSpace <= effectiveSNPGtyNum) {
                    availabeSNPSpace += 10000;
                    for (StatusGtySet gtySet : subjectGtyList) {
                        gtySet.existence.setSize(availabeSNPSpace);
                        gtySet.paternalChrom.setSize(availabeSNPSpace);
                        gtySet.maternalChrom.setSize(availabeSNPSpace);
                    }
                }

                // assume all only have two alleles. 
                if (isPhased[0]) {
                    for (index = 0; index < indivNum; index++) {
                        StatusGtySet sGty = subjectGtyList.get(index);
                        if (gtys[index] == null) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("0|0") || gtys[index].equals("0")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("0|1")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        } else if (gtys[index].equals("1|0")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("1|1") || gtys[index].equals("1")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        }
                    }
                } else {
                    for (index = 0; index < indivNum; index++) {
                        StatusGtySet sGty = subjectGtyList.get(index);
                        if (gtys[index] == null) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("0/0") || gtys[index].equals("0")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("0/1")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        } else if (gtys[index].equals("1/0")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                        } else if (gtys[index].equals("1/1") || gtys[index].equals("1")) {
                            sGty.existence.putQuick(effectiveSNPGtyNum, true);
                            sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                            sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                        }
                    }
                }
                snp1.genotypeOrder = effectiveSNPGtyNum;
                effectiveSNPGtyNum++;
            } while ((line = br.readLine()) != null);

            br.close();

            runningInfo.append("The number of SNPs within genes on chromosome ");
            runningInfo.append(chromName);
            runningInfo.append(" in the VCF haloptype file ");
            runningInfo.append(vcfFile.getName());
            runningInfo.append(" is ");
            runningInfo.append(variantsWithLD.size());
            runningInfo.append(".");
            //GlobalManager.mainView.insertBriefRunningInfor(runningInfo.toString(), true);
            LOG.info(runningInfo.toString());
        }

        return variantsWithLD;
    }

    public List<SNP> readSNPMapHapVCFFileByPositions(File vcfFile, String chromName,
            Set<Integer> positionSet, LiftOver liftOver, List<StatusGtySet> subjectGtyList) throws Exception {
        if (!vcfFile.exists()) {
            return null;
        }
        StringBuilder runningInfo = new StringBuilder();
        StringBuilder tmpBuffer = new StringBuilder();

        boolean needConvert = false;
        if (liftOver != null) {
            needConvert = true;
        }

        int indexCHROM = -1;
        int indexPOS = -1;
        int indexID = -1;
        int indexREF = -1;
        int indexALT = -1;
        int indexQUAL = -1;
        int indexFILTER = -1;
        int indexFORMAT = -1;
        int indexINFO = -1;

        int availabeSNPSpace = positionSet.size();

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
            return null;
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

                    StatusGtySet sGty = new StatusGtySet();
                    sGty.existence = new BitVector(availabeSNPSpace);
                    sGty.paternalChrom = new BitVector(availabeSNPSpace);
                    sGty.maternalChrom = new BitVector(availabeSNPSpace);
                    subjectGtyList.add(sGty);
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

        String tmpStr = null;
        boolean isPhased = false;

        int makerPostion = -1;
        String ref = null;
        String alt = null;

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
            String[] cells = null;
            String[] cells1 = null;
            do {
                // System.out.println(line); 
                // st = new StringTokenizer(line.trim(), delimiter);
                cells = Util.tokenize(line, '\t', indexPOS);
                /*
                 currChr = cells[indexCHROM];
                 if (currChr.charAt(currChr.length() - 1) == 'T' || currChr.charAt(currChr.length() - 1) == 't') {
                 currChr = currChr.substring(0, currChr.length() - 1);
                 }*/

                makerPostion = Integer.parseInt(cells[indexPOS]);

                // System.out.println(makerPostion); 
                if (needConvert && makerPostion > 0) {
                    Interval interval = new Interval("chr" + chromName, makerPostion, makerPostion);
                    Interval int2 = liftOver.liftOver(interval);
                    if (int2 != null) {
                        makerPostion = int2.getStart();
                    }
                }

                //sometimes the 1kg data has a mixture of 1-based or 0-based postions              
                if (!positionSet.contains(makerPostion) //|| !positionSet.contains(makerPostion + 1) || !positionSet.contains(makerPostion - 1)
                        ) {
                    continue;
                }

                cells = Util.tokenize(line, '\t');

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

                alt = cells[indexALT];
                ref = cells[indexREF];
                if (ref != null && ref.length() > 1) {
                    continue;
                }
                if (alt != null && alt.length() > 1) {
                    continue;
                }
                cells1 = Util.tokenize(cells[indexFORMAT], ':');

                for (int i = 0; i < cells1.length; i++) {
                    if (cells1[i].equals("GT")) {
                        gtyIndexInInfor = i;
                        hasIndexGT = true;
                    } else if (cells1[i].equals("GQ")) {
                        gtyQualIndexInInfor = i;
                        hasIndexGQ = true;
                    } else if (cells1[i].equals("DP")) {
                        gtyDepthIndexInInfor = i;
                        hasIndexDP = true;
                    } else if (cells1[i].equals("AD")) {
                        gtyAlleleDepthIndexInInfor = i;
                        hasIndexAD = true;
                    } else if (cells1[i].equals("FA")) {
                        gtyAltAlleleFracIndexInInfor = i;
                        hasIndexFA = true;
                    }
                }
                if (indivNum <= 0) {
                    indivNum = cells.length - indexFORMAT - 1;
                    gtyQuality = new double[indivNum];
                    gtys = new String[indivNum];
                    gtyDepth = new int[indivNum];
                    altReadRatio = new float[indivNum];
                    Arrays.fill(gtys, null);
                    Arrays.fill(gtyQuality, 0);
                    Arrays.fill(gtyDepth, 0);
                    Arrays.fill(altReadRatio, 0);

                    StatusGtySet sGty = new StatusGtySet();
                    sGty.existence = new BitVector(availabeSNPSpace);
                    sGty.paternalChrom = new BitVector(availabeSNPSpace);
                    sGty.maternalChrom = new BitVector(availabeSNPSpace);
                    subjectGtyList.add(sGty);
                }

//#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  
//chr1    109     .       A       T       237.97  PASS    AC=21;AF=0.328;AN=64;DP=47;Dels=0.02;HRun=0;HaplotypeScore=1.9147;MQ=44.81;MQ0=48;QD=5.53;SB=-28.76;sumGLbyD=9.00       GT:AD:DP:GQ:PL  0/1:6,1:3:15.67:16,0,64 0/0:3,0:1:3.01:0,3,33 
                iGty = 0;
                for (iCol = indexFORMAT + 1; iCol < cells.length; iCol++) {
                    tmpBuffer.delete(0, tmpBuffer.length());
                    tmpBuffer.append(cells[iCol]);
                    //1/1:0,2:2:6.02:70,6,0	./.

                    if (tmpBuffer.charAt(0) == '"') {
                        tmpStr = tmpBuffer.substring(1, tmpBuffer.length() - 1);
                        tmpBuffer.delete(0, tmpBuffer.length());
                        tmpBuffer.append(tmpStr);
                    }
                    if (tmpBuffer.toString().equals(".|.") || tmpBuffer.toString().equals("./.") || tmpBuffer.toString().equals(".")) {
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

                /*
                 if (currChr.startsWith("chr")) {
                 currChr = currChr.substring(3);
                 }*/
                int invalidGtyNum = 0;
                //check if there are variants and genotypes with low quality
                for (index = indivNum - 1; index >= 0; index--) {
                    //ignore variants with missing genotypes
                    if (gtys[index] == null || gtys[index].charAt(0) == '.') {
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

                String[] alts = alt.split(",");
                //ignore the variants with multiple alleles 
                if (alts.length > 1) {
                    continue;
                }
                if (availabeSNPSpace <= effectiveSNPGtyNum) {
                    availabeSNPSpace += 50000;
                    for (StatusGtySet gtySet : subjectGtyList) {
                        gtySet.existence.setSize(availabeSNPSpace);
                        gtySet.paternalChrom.setSize(availabeSNPSpace);
                        gtySet.maternalChrom.setSize(availabeSNPSpace);
                    }
                }
                // assume all only have two alleles. 
                for (index = 0; index < indivNum; index++) {
                    StatusGtySet sGty = subjectGtyList.get(index);
                    if (gtys[index] == null) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, false);
                    } else if (gtys[index].equals("0|0") || gtys[index].equals("0")) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                    } else if (gtys[index].equals("0|1")) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                    } else if (gtys[index].equals("1|0")) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                    } else if (gtys[index].equals("1|1") || gtys[index].equals("1")) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                    }
                }
                SNP snp1 = new SNP(makerPostion);
                snp1.genotypeOrder = effectiveSNPGtyNum;
                mappedSNPList.add(snp1);
                effectiveSNPGtyNum++;
            } while ((line = br.readLine()) != null);

            br.close();
        }
        return mappedSNPList;
    }

    public List<SNP> readSNPMapHapVCFFileByPositions0(File vcfFile, String chromName,
            Set<Integer> positionSet, LiftOver liftOver, List<StatusGtySet> subjectGtyList) throws Exception {
        if (!vcfFile.exists()) {
            return null;
        }
        StringBuilder runningInfo = new StringBuilder();
        StringBuilder tmpBuffer = new StringBuilder();

        boolean needConvert = false;
        if (liftOver != null) {
            needConvert = true;
        }

        int indexCHROM = -1;
        int indexPOS = -1;
        int indexID = -1;
        int indexREF = -1;
        int indexALT = -1;
        int indexQUAL = -1;
        int indexFILTER = -1;
        int indexFORMAT = -1;
        int indexINFO = -1;

        int availabeSNPSpace = positionSet.size();

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
            return null;
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

                    StatusGtySet sGty = new StatusGtySet();
                    sGty.existence = new BitVector(availabeSNPSpace);
                    sGty.paternalChrom = new BitVector(availabeSNPSpace);
                    sGty.maternalChrom = new BitVector(availabeSNPSpace);
                    subjectGtyList.add(sGty);
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
                            if (needConvert && makerPostion > 0) {
                                Interval interval = new Interval("chr" + chromName, makerPostion, makerPostion);
                                Interval int2 = liftOver.liftOver(interval);
                                if (int2 != null) {
                                    makerPostion = int2.getStart();
                                }
                            }
                            if (!positionSet.contains(makerPostion)) {
                                break;
                            }

                        } else if (iCol == indexALT) {
                            alt = tmpBuffer.toString();
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

                                StatusGtySet sGty = new StatusGtySet();
                                sGty.existence = new BitVector(availabeSNPSpace);
                                sGty.paternalChrom = new BitVector(availabeSNPSpace);
                                sGty.maternalChrom = new BitVector(availabeSNPSpace);
                                subjectGtyList.add(sGty);
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
                    if (gtys[index] == null || gtys[index].charAt(0) == '.') {
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

                String[] alts = alt.split(",");
                //ignore the variants with multiple alleles 
                if (alts.length > 1) {
                    continue;
                }
                if (availabeSNPSpace <= effectiveSNPGtyNum) {
                    availabeSNPSpace += 50000;
                    for (StatusGtySet gtySet : subjectGtyList) {
                        gtySet.existence.setSize(availabeSNPSpace);
                        gtySet.paternalChrom.setSize(availabeSNPSpace);
                        gtySet.maternalChrom.setSize(availabeSNPSpace);
                    }
                }
                // assume all only have two alleles. 
                for (index = 0; index < indivNum; index++) {
                    StatusGtySet sGty = subjectGtyList.get(index);
                    if (gtys[index] == null) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, false);
                    } else if (gtys[index].equals("0|0") || gtys[index].equals("0")) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                    } else if (gtys[index].equals("0|1")) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, false);
                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                    } else if (gtys[index].equals("1|0")) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, false);
                    } else if (gtys[index].equals("1|1") || gtys[index].equals("1")) {
                        sGty.existence.putQuick(effectiveSNPGtyNum, true);
                        sGty.paternalChrom.putQuick(effectiveSNPGtyNum, true);
                        sGty.maternalChrom.putQuick(effectiveSNPGtyNum, true);
                    }
                }
                SNP snp1 = new SNP(makerPostion);
                snp1.genotypeOrder = effectiveSNPGtyNum;
                mappedSNPList.add(snp1);
                effectiveSNPGtyNum++;
            } while ((line = br.readLine()) != null);

            br.close();
        }
        return mappedSNPList;
    }

    public List<SNP> readSNPMapFileByPositions(File[] mapHapFiles, String chromName,
            Set<Integer> positionSet, LiftOver liftOver) throws Exception {
        File mapFile = mapHapFiles[0];
        if (!mapFile.exists()) {
            return null;
        }
        StringBuilder runningInfo = new StringBuilder();
        StringBuilder tmpBuffer = new StringBuilder();

        boolean needConvert = false;
        if (liftOver != null) {
            needConvert = true;
        }

        BufferedReader brMap = new BufferedReader(new FileReader(mapFile));
        //skip the first line
        // br.readLine();
        int lineCounter = -1;
        boolean invalid = true;
        int curSNPNum = 0;
        String line;
        int posIndex = 2;
        int maxIndex = posIndex;
        int index = 0;
        int pos = 0;
        String delmilit = "\t ,";
        //start to read genotypes
        int effectiveSNPNum = 0;
        List<SNP> snpList = new ArrayList<SNP>();
        while ((line = brMap.readLine()) != null) {
            lineCounter++;
            StringTokenizer tokenizer = new StringTokenizer(line);
            invalid = false;
            index = 0;
            while (tokenizer.hasMoreTokens()) {
                tmpBuffer.delete(0, tmpBuffer.length());
                tmpBuffer.append(tokenizer.nextToken().trim());

                if (index == posIndex) {
                    //filter physical region
                    pos = Integer.parseInt(tmpBuffer.toString());

                    if (needConvert && pos > 0) {
                        Interval interval = new Interval("chr" + chromName, pos, pos);
                        Interval int2 = liftOver.liftOver(interval);
                        if (int2 != null) {
                            pos = int2.getStart();
                        }
                    }

                } else if (index > maxIndex) {
                    break;
                }
                index++;
            }
            if (invalid) {
                continue;
            }
            effectiveSNPNum++;

            if (positionSet.contains(pos)) {
                SNP snp1 = new SNP(pos);
                snp1.genotypeOrder = lineCounter;
                snpList.add(snp1);
            }
        }
        brMap.close();

        runningInfo.append("The number of SNPs on chromosome ");
        runningInfo.append(chromName);
        runningInfo.append(" in map file ");
        runningInfo.append(mapFile.getName());
        runningInfo.append(" is ");
        runningInfo.append(snpList.size());
        runningInfo.append(".");
        //GlobalManager.mainView.insertBriefRunningInfor(runningInfo.toString(), true);
        LOG.info(runningInfo.toString());
        return snpList;
    }

    //note: a unique function to read haplotype provided by Mach
    public void readHaplotypesBySNPList(String chromName, List<SNP> chromSNPs, File[] mapHapFiles,
            List<StatusGtySet> indList) throws Exception {
        String line = null;

        int availabeSNPSpace = chromSNPs.size();
        String phase1, phase2;

        //encode genotypes
        int totalSNPSize = chromSNPs.size();
        Collections.sort(chromSNPs, new SNPGenotypeIndexComparator());

        char[] aAlleles = new char[totalSNPSize];
        char[] bAlleles = new char[totalSNPSize];
        boolean[] needAlleleNames = new boolean[totalSNPSize];
        int[] snpOders = new int[totalSNPSize];
        Arrays.fill(aAlleles, MISSING_ALLELE_NAME);
        Arrays.fill(bAlleles, MISSING_ALLELE_NAME);
        Arrays.fill(needAlleleNames, true);
        Arrays.fill(snpOders, -1);
        int unexpectedGtyNum = 0;
        File phaseFile = mapHapFiles[1];
        int index1 = 0, index2 = 0;
        char DEFAULT_MISSING_ALLELE = MISSING_ALLELE_NAME;
        int currentHaploIndex = 0;
        BufferedReader brPhase = new BufferedReader(new FileReader(phaseFile));
        while ((line = brPhase.readLine()) != null) {
            index1 = 0;
            index2 = index1;
            while (line.charAt(index2) != ' ') {
                index2++;
            }

            index2++;
            //skip haplo label
            index1 = index2;
            while (line.charAt(index2) != ' ') {
                index2++;
            }
            //get the haplotype
            phase1 = line.substring(index2 + 1);

            // System.out.println(indiv.getLabelInChip());
            line = brPhase.readLine();

            //skip the individual lable and haplo labels as they are identical
            index1 = 0;
            index2 = index1;
            while (line.charAt(index2) != ' ') {
                index2++;
            }
            index2++;
            index1 = index2;
            while (line.charAt(index2) != ' ') {
                index2++;
            }
            phase2 = line.substring(index2 + 1);

            StatusGtySet sGty = new StatusGtySet();
            sGty.existence = new BitVector(availabeSNPSpace);
            sGty.paternalChrom = new BitVector(availabeSNPSpace);
            sGty.maternalChrom = new BitVector(availabeSNPSpace);

            //System.out.println(indiv.getLabelInChip());
            currentHaploIndex = 0;

            for (int snpOrder = 0; snpOrder < totalSNPSize; snpOrder++) {
                int snpFileIndex = chromSNPs.get(snpOrder).genotypeOrder;
                do {
                    if (snpFileIndex > currentHaploIndex) {
                        currentHaploIndex++;
                    }
                } while (snpFileIndex > currentHaploIndex);

                if (needAlleleNames[snpOrder]) {
                    //decide the allele names
                    if (aAlleles[snpOrder] != MISSING_ALLELE_NAME && bAlleles[snpOrder] == MISSING_ALLELE_NAME) {
                        //decide the allele names
                        if (phase1.charAt(currentHaploIndex) != aAlleles[snpOrder] && phase1.charAt(currentHaploIndex) != DEFAULT_MISSING_ALLELE) {
                            bAlleles[snpOrder] = phase1.charAt(currentHaploIndex);
                            needAlleleNames[snpOrder] = false;
                        } else if (phase2.charAt(currentHaploIndex) != aAlleles[snpOrder] && phase2.charAt(currentHaploIndex) != DEFAULT_MISSING_ALLELE) {
                            bAlleles[snpOrder] = phase2.charAt(currentHaploIndex);
                            needAlleleNames[snpOrder] = false;
                        }
                    } else if (aAlleles[snpOrder] == MISSING_ALLELE_NAME && bAlleles[snpOrder] == MISSING_ALLELE_NAME) {
                        //decide the allele names
                        if (phase1.charAt(currentHaploIndex) != DEFAULT_MISSING_ALLELE) {
                            aAlleles[snpOrder] = phase1.charAt(currentHaploIndex);

                            if (phase2.charAt(currentHaploIndex) != DEFAULT_MISSING_ALLELE && phase2.charAt(currentHaploIndex) != aAlleles[snpOrder]) {
                                bAlleles[snpOrder] = phase2.charAt(currentHaploIndex);
                                needAlleleNames[snpOrder] = false;
                            }
                        } else if (phase2.charAt(currentHaploIndex) != DEFAULT_MISSING_ALLELE) {
                            aAlleles[snpOrder] = phase2.charAt(currentHaploIndex);

                        }
                    } else if (aAlleles[snpOrder] == MISSING_ALLELE_NAME && bAlleles[snpOrder] != MISSING_ALLELE_NAME) {
                        //decide the allele names
                        if (phase1.charAt(currentHaploIndex) != bAlleles[snpOrder] && phase1.charAt(currentHaploIndex) != DEFAULT_MISSING_ALLELE) {
                            aAlleles[snpOrder] = phase1.charAt(currentHaploIndex);

                            needAlleleNames[snpOrder] = false;
                        } else if (phase2.charAt(currentHaploIndex) != bAlleles[snpOrder] && phase2.charAt(currentHaploIndex) != DEFAULT_MISSING_ALLELE) {
                            aAlleles[snpOrder] = phase2.charAt(currentHaploIndex);
                            needAlleleNames[snpOrder] = false;
                        }
                    }
                }

                // anyhow the  aAllele and bAllele must be known
                //re-code: A->0; B->1;
                if (phase1.charAt(currentHaploIndex) == bAlleles[snpOrder]) {
                    sGty.existence.putQuick(snpOrder, true);
                    sGty.paternalChrom.putQuick(snpOrder, true);
                } else if (phase1.charAt(currentHaploIndex) == aAlleles[snpOrder]) {
                    sGty.existence.putQuick(snpOrder, true);
                } else if (phase1.charAt(currentHaploIndex) != DEFAULT_MISSING_ALLELE) {
                    //throw new Exception("Unexpected genotype in " + filePath + " for " + probID);
                    // String unexpectInfo = "Unexpected allele " + phase1.charAt(currentHaploIndex) + " in " + phaseFile.getName() + " for IndivID " + indiv.getLabelInChip() + " of phase 1.";

                    //GlobalVariables.mainView.setBriefRunningInfor(unexpectInfo);
                    //System.out.println(unexpectInfo);
                    unexpectedGtyNum++;
                }

                if (phase2.charAt(currentHaploIndex) == bAlleles[snpOrder]) {
                    sGty.maternalChrom.putQuick(snpOrder, true);
                } else if (phase2.charAt(currentHaploIndex) != aAlleles[snpOrder] && phase2.charAt(currentHaploIndex) != DEFAULT_MISSING_ALLELE) {
                    //throw new Exception("Unexpected genotype in " + filePath + " for " + probID);
                    // String unexpectInfo = "Unexpected allele " + phase1.charAt(currentHaploIndex) + " in " + phaseFile.getName() + " for IndivID " + indiv.getLabelInChip() + " of phase 2.";
                    //GlobalVariables.mainView.setBriefRunningInfor(unexpectInfo);
                    // System.out.println(unexpectInfo);
                    unexpectedGtyNum++;
                }
                currentHaploIndex++;
            }

            indList.add(sGty);
            line = null;
        }
        brPhase.close();
        if (unexpectedGtyNum > 0) {
            System.out.println(unexpectedGtyNum + " SNPs have unexpected genotypes in " + phaseFile.getName() + "in total.");
        }
        /*
         OpenIntIntHashMap indexGenotypePosMap = new OpenIntIntHashMap();
        
         for (int snpOrder = 0; snpOrder < totalSNPSize; snpOrder++) {
         indexGenotypePosMap.put(chromSNPs.get(snpOrder).physicalPosition, snpOrder);
         }
        
         * 
         */

        for (int snpOrder = 0; snpOrder < totalSNPSize; snpOrder++) {
            chromSNPs.get(snpOrder).genotypeOrder = snpOrder;
        }

    }
}
