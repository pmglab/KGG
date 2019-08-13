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

import cern.colt.bitvector.BitVector;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.picard.liftover.LiftOver;
import net.sf.picard.util.Interval;

/**
 *
 * @author mxli
 */
public class PlinkDataset implements Constants {

    private final static Logger LOG = Logger.getLogger(PlinkDataset.class.getName());
    protected String pedigreeFileName;
    protected String mapFileName;
    //if plinkBinaryFileName is not null, it must be a plink binary file
    protected String plinkBinaryFileName;

    @Override
    public String toString() {
        File file = new File(plinkBinaryFileName);
        return file.getName();
    }

    public PlinkDataset(String pedigreeFileName, String mapFileName, String plinkBinaryFileName) {
        this.pedigreeFileName = pedigreeFileName;
        this.mapFileName = mapFileName;
        this.plinkBinaryFileName = plinkBinaryFileName;
    }

    public boolean avaibleFiles() {
        File file = new File(pedigreeFileName);
        if (!file.exists()) {
            return false;
        }
        file = new File(mapFileName);
        if (!file.exists()) {
            return false;
        }
        file = new File(plinkBinaryFileName);
        if (!file.exists()) {
            return false;
        }
        return true;
    }
    // public void calcualte

    public boolean readPlinkBinaryGenotypeinPedigreeFile(List<SNP> snpList,
            Map<String, StatusGtySet> indivGtyMap) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(pedigreeFileName));
        TreeSet<String> unexpectedSNPSet = new TreeSet<String>();
        String line;
        String delimiter = "\t\" \",/";
        int totalSNPSize = snpList.size();
        int gtyStaringCol = -1;

        String indivLabel;

        int unexpectedGtyNum = 0;
        List<String> indiviIDInPed = new ArrayList<String>();
        List<Individual> indList = new ArrayList<Individual>();
        Collections.sort(snpList, new SNPGenotypeIndexComparator());
        //guess genotype starting column
        gtyStaringCol = 5 + 1;
        int snpOrder = 0;

        StringBuilder tmpBuffer = new StringBuilder();
        //long t3 = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
            line = line.toUpperCase();
            StringTokenizer tokenizer = new StringTokenizer(line, delimiter);

            Individual indiv = new Individual();
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setFamilyID(tmpBuffer.toString());
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setIndividualID(tmpBuffer.toString());
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setDadID(tmpBuffer.toString());
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setMomID(tmpBuffer.toString());
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setGender(Integer.valueOf(tmpBuffer.toString()));
            indiv.setLabelInChip(indiv.getFamilyID() + "@*@" + indiv.getIndividualID());

            //What's the meaning of this?
            for (int i = 5; i < gtyStaringCol; i++) {
                tmpBuffer.delete(0, tmpBuffer.length());
                tmpBuffer.append(tokenizer.nextToken().trim());
                indiv.addTrait(tmpBuffer.toString());
            }
            //System.out.println(indiv.getLabelInChip());
            indList.add(indiv);

            indivLabel = indiv.getLabelInChip();
            StatusGtySet sGty = indivGtyMap.get(indivLabel);
            if (sGty == null) {
                sGty = new StatusGtySet();
                //////////////////////////////
                // Allocate space for SNPs
                sGty.paternalChrom = new BitVector(totalSNPSize);
                sGty.maternalChrom = new BitVector(totalSNPSize);
                sGty.existence = new BitVector(totalSNPSize);
                indivGtyMap.put(indivLabel, sGty);
                indiviIDInPed.add(indivLabel);
            } else {
                String unexpectInfo = "Duplicated Individuals  in " + pedigreeFileName + " for PedID " + indiv.getFamilyID() + " IndivID " + indiv.getIndividualID();
                LOG.warning(unexpectInfo);
            }
            tokenizer = null;
            line = null;
        }
        br.close();

        //start to read binary genotypes
//http://pngu.mgh.harvard.edu/~purcell/plink/binary.shtml
        String info = ("Reading genotype bit-file from [ " + plinkBinaryFileName + " ] \n");

        LOG.info(info);

        boolean bfile_SNP_major = false;
        //openPlinkPedFormat
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(plinkBinaryFileName)));
        StringBuilder logInfor = new StringBuilder();
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

        // printBitSet(b);
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
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(plinkBinaryFileName)));
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
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(plinkBinaryFileName)));
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

        int indiviNum = indiviIDInPed.size();

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
                SNP snp = snpList.get(i);

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
                        StatusGtySet sGty = indivGtyMap.get(indiviIDInPed.get(k));
                        if (!b[s] && b[s + 1]) {
                            sGty.existence.putQuick(i, true);
                            sGty.paternalChrom.putQuick(i, true);
                        } else if (!b[s] && !b[s + 1]) {
                            sGty.existence.putQuick(i, true);
                        } else if (b[s] && b[s + 1]) {
                            sGty.existence.putQuick(i, true);
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
                StatusGtySet sGty = indivGtyMap.get(indiviIDInPed.get(k));
                for (int i = 0; i < totalSNPSize; i++) {
                    SNP snp = snpList.get(i);
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
                        sGty.existence.putQuick(i, true);
                        sGty.paternalChrom.putQuick(i, true);
                    } else if (!b[s] && !b[s + 1]) {
                        sGty.existence.putQuick(i, true);
                    } else if (b[s] && b[s + 1]) {
                        sGty.existence.putQuick(i, true);
                        sGty.maternalChrom.putQuick(i, true);
                        sGty.paternalChrom.putQuick(i, true);
                    }
                    snpOrder++;
                }
            }
            for (int i = 0; i < totalSNPSize; i++) {
                SNP snp = snpList.get(i);
                snp.genotypeOrder = i;
            }
        }

        in.close();
        if (unexpectedGtyNum > 0) {
            // GlobalManager.mainView.insertBriefRunningInfor((unexpectedGtyNum / 2) + " Unexpected genotypes in " + plinkBinaryFileName + " for " + unexpectedSNPSet.size() + " SNPs (detailed in the log).", true);

            LOG.log(Level.WARNING, "{0} SNPs have unexpected genotypes in {1}:\n{2}", new Object[]{unexpectedSNPSet.size(), plinkBinaryFileName, unexpectedSNPSet.toString()});

            unexpectedSNPSet.clear();
        }
        return false;
    }

    public List<SNP> readSNPsinPlinkBinaryMapFileByRSID(Chromosome chromosome, Map<String, int[]> snpGenomeIndexes, String chromName) throws Exception {
        File mapFile = new File(mapFileName);
        BufferedReader br = new BufferedReader(new FileReader(mapFile));
        String line = null;

        String delmilit = ", \t";
        int chromIndex = 0;
        int rsIndex = 1;
        int strandIndex = -1;
        int maxIndex = Math.max(chromIndex, rsIndex);

        List<Gene> genes = chromosome.genes;
        List<SNP> snpOutGenes = chromosome.snpsOutGenes;

        maxIndex = Math.max(strandIndex, rsIndex);

        int lineCounter = -1;
        boolean invalid = false;

        boolean diffChrom = false;
        String tmpStr;
        String rsID = null;
        StringBuilder tmpBuffer = new StringBuilder();
        int index;
        int effectiveSNPNum = 0;
        /*
         *
         * The autosomes should be coded 1 through 22. The following other codes can be used to specify other chromosome types:
        
         X    X chromosome                    -> 23
         Y    Y chromosome                    -> 24
         XY   Pseudo-autosomal region of X    -> 25
         MT   Mitochondrial                   -> 26
        
         */
        String endcodeChromName;
        if (chromName.equalsIgnoreCase("X")) {
            endcodeChromName = "23";
        } else if (chromName.equalsIgnoreCase("Y")) {
            endcodeChromName = "24";
        } else if (chromName.equalsIgnoreCase("XY")) {
            endcodeChromName = "25";
        } else if (chromName.equalsIgnoreCase("MT")) {
            endcodeChromName = "26";
        } else {
            endcodeChromName = chromName;
        }
        List<SNP> snpList = new ArrayList<SNP>();
        try {
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                lineCounter++;
                StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
                index = 0;
                diffChrom = false;
                invalid = false;

                while (tokenizer.hasMoreTokens()) {
                    tmpBuffer.delete(0, tmpBuffer.length());
                    tmpBuffer.append(tokenizer.nextToken().trim());
                    tmpStr = tmpBuffer.toString();
                    if (index == chromIndex) {
                        if (tmpStr.compareTo(endcodeChromName) != 0) {
                            diffChrom = true;
                            break;
                        }
                    } else if (index == rsIndex) {
                        rsID = tmpStr;
                    }

                    if (index == maxIndex) {
                        break;
                    }
                    index++;
                }

                if (diffChrom) {
                    continue;
                }
                if (invalid) {
                    continue;
                }
                effectiveSNPNum++;
                int[] poses = snpGenomeIndexes.get(rsID);
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

            StringBuilder runningInfo = new StringBuilder();
            runningInfo.append("The number of SNPs on chromosome ");
            runningInfo.append(chromName);

            runningInfo.append(" in map file ");
            runningInfo.append(mapFile.getName());
            runningInfo.append(" is ");
            runningInfo.append(effectiveSNPNum);
            runningInfo.append(".");
            // GlobalManager.mainView.insertBriefRunningInfor(runningInfo.toString(), true);
            LOG.info(runningInfo.toString());
            return (snpList);
        } finally {
            br.close();
        }
    }

    public List<SNP> readSNPsinPlinkBinaryMapFileByPositions(Chromosome chromosome, Map<String, int[]> positionGenomeIndexes, String chromName) throws Exception {
        File mapFile = new File(mapFileName);
        BufferedReader br = new BufferedReader(new FileReader(mapFile));
        String line = null;

        String delmilit = ", \t";
        int chromIndex = 0;
        int positionIndex = 3;
        int strandIndex = -1;
        int maxIndex = Math.max(chromIndex, positionIndex);

        List<Gene> genes = chromosome.genes;
        List<SNP> snpOutGenes = chromosome.snpsOutGenes;

        maxIndex = Math.max(strandIndex, positionIndex);

        int lineCounter = -1;
        boolean invalid = false;

        boolean diffChrom = false;
        String tmpStr;
        String chrom;
        String positionStr = null;
        StringBuilder tmpBuffer = new StringBuilder();
        int index;
        int effectiveSNPNum = 0;
        /*
         *
         * The autosomes should be coded 1 through 22. The following other codes can be used to specify other chromosome types:
        
         X    X chromosome                    -> 23
         Y    Y chromosome                    -> 24
         XY   Pseudo-autosomal region of X    -> 25
         MT   Mitochondrial                   -> 26
        
         */
        String endcodeChromName;
        if (chromName.equalsIgnoreCase("X")) {
            endcodeChromName = "23";
        } else if (chromName.equalsIgnoreCase("Y")) {
            endcodeChromName = "24";
        } else if (chromName.equalsIgnoreCase("XY")) {
            endcodeChromName = "25";
        } else if (chromName.equalsIgnoreCase("MT")) {
            endcodeChromName = "26";
        } else {
            endcodeChromName = chromName;
        }
        List<SNP> snpList = new ArrayList<SNP>();
        try {
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                lineCounter++;
                StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
                index = 0;
                diffChrom = false;
                invalid = false;

                while (tokenizer.hasMoreTokens()) {
                    tmpBuffer.delete(0, tmpBuffer.length());
                    tmpBuffer.append(tokenizer.nextToken().trim());
                    tmpStr = tmpBuffer.toString();
                    if (index == chromIndex) {
                        if (tmpStr.compareTo(endcodeChromName) != 0) {
                            diffChrom = true;
                            break;
                        }
                    } else if (index == positionIndex) {
                        positionStr = tmpStr;
                    }

                    if (index == maxIndex) {
                        break;
                    }
                    index++;
                }

                if (diffChrom) {
                    continue;
                }
                if (invalid) {
                    continue;
                }
                effectiveSNPNum++;
                int[] poses = positionGenomeIndexes.get(chromName + ":" + positionStr);
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

            StringBuilder runningInfo = new StringBuilder();
            runningInfo.append("The number of SNPs on chromosome ");
            runningInfo.append(chromName);

            runningInfo.append(" in map file ");
            runningInfo.append(mapFile.getName());
            runningInfo.append(" is ");
            runningInfo.append(effectiveSNPNum);
            runningInfo.append(".");
            //GlobalManager.mainView.insertBriefRunningInfor(runningInfo.toString(), true);
            LOG.info(runningInfo.toString());
            return (snpList);
        } finally {
            br.close();
        }
    }

    public List<SNP> readSNPsinPlinkBinaryMapFileByPositions(Set<Integer> positions, String chromName, LiftOver liftOver) throws Exception {
        File mapFile = new File(mapFileName);
        BufferedReader br = new BufferedReader(new FileReader(mapFile));
        String line = null;

        String delmilit = ", \t";
        int chromIndex = 0;
        int positionIndex = 3;
        int strandIndex = -1;
        int maxIndex = Math.max(chromIndex, positionIndex);

        maxIndex = Math.max(strandIndex, positionIndex);

        int lineCounter = -1;
        boolean invalid = false;

        boolean diffChrom = false;
        String tmpStr;

        int position = -1;
        StringBuilder tmpBuffer = new StringBuilder();
        int index;
        int effectiveSNPNum = 0;
        /*
         *
         * The autosomes should be coded 1 through 22. The following other codes can be used to specify other chromosome types:
        
         X    X chromosome                    -> 23
         Y    Y chromosome                    -> 24
         XY   Pseudo-autosomal region of X    -> 25
         MT   Mitochondrial                   -> 26
        
         */
        String endcodeChromName;
        if (chromName.equalsIgnoreCase("X")) {
            endcodeChromName = "23";
        } else if (chromName.equalsIgnoreCase("Y")) {
            endcodeChromName = "24";
        } else if (chromName.equalsIgnoreCase("XY")) {
            endcodeChromName = "25";
        } else if (chromName.equalsIgnoreCase("MT")) {
            endcodeChromName = "26";
        } else {
            endcodeChromName = chromName;
        }
        List<SNP> snpList = new ArrayList<SNP>();
        boolean needConvert = false;
        if (liftOver != null) {
            needConvert = true;
        }
        Interval interval = new Interval("chr" + chromName, 1, 2);
        try {
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                lineCounter++;
                StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
                index = 0;
                diffChrom = false;
                invalid = false;

                while (tokenizer.hasMoreTokens()) {
                    tmpBuffer.delete(0, tmpBuffer.length());
                    tmpBuffer.append(tokenizer.nextToken().trim());
                    tmpStr = tmpBuffer.toString();
                    if (index == chromIndex) {
                        if (tmpStr.compareTo(endcodeChromName) != 0) {
                            diffChrom = true;
                            break;
                        }
                    } else if (index == positionIndex) {
                        position = Integer.parseInt(tmpStr);
                        if (needConvert) {
                            interval.setStart(position);
                            interval.setEnd(position);
                            Interval int2 = liftOver.liftOver(interval);
                            if (int2 != null) {
                                position = int2.getStart();
                            }
                        }
                    }

                    if (index == maxIndex) {
                        break;
                    }
                    index++;
                }

                if (diffChrom) {
                    continue;
                }
                if (invalid) {
                    continue;
                }

                if (positions.contains(position)) {
                    SNP snp1 = new SNP(position);
                    snp1.genotypeOrder = lineCounter;
                    snpList.add(snp1);
                    effectiveSNPNum++;
                }
            }

            StringBuilder runningInfo = new StringBuilder();
            runningInfo.append("The number of SNPs on chromosome ");
            runningInfo.append(chromName);

            runningInfo.append(" in map file ");
            runningInfo.append(mapFile.getName());
            runningInfo.append(" is ");
            runningInfo.append(effectiveSNPNum);
            runningInfo.append(".");
            //GlobalManager.mainView.insertBriefRunningInfor(runningInfo.toString(), true);
            LOG.info(runningInfo.toString());
            return (snpList);
        } finally {
            br.close();
        }
    }

    public String getPedigreeFileName() {
        return pedigreeFileName;
    }

    public String getMapFileName() {
        return mapFileName;
    }

    public String getPlinkBinaryFileName() {
        return plinkBinaryFileName;
    }

}
