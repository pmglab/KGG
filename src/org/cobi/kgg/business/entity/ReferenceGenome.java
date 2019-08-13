/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author mxli
 */
public class ReferenceGenome implements Constants {

    private final static Logger LOG = Logger.getLogger(ReferenceGenome.class.getName());
    Map<String, Byte> fullchromNameIndexMap = new HashMap<String, Byte>();
    Map<String, Byte> chromNameIndexMap = new HashMap<String, Byte>();
    private List<List<RefmRNA>> chromosomes = null;
    private List<List<RNABoundaryIndex>> chromosomemRNABoundaryIndexList = null;
    private Map<String, int[]> geneChromPosMap = new HashMap<String, int[]>();
    boolean hasNotSorted = true;
    int upstreamDis = 1000;
    int donwstreamDis = 1000;
    int splicingDis = 2;
    RNABoundaryIndex searchedmRNABoundaryIndex = new RNABoundaryIndex(0);
    RNABoundaryIndexComparator rNAGeneBoundaryIndexComparator = new RNABoundaryIndexComparator();

    public ReferenceGenome() {
        chromosomes = new ArrayList<List<RefmRNA>>();
        chromosomemRNABoundaryIndexList = new ArrayList<List<RNABoundaryIndex>>();
        for (byte i = 0; i < CHROM_NAMES.length; i++) {
            fullchromNameIndexMap.put("chr" + CHROM_NAMES[i], i);
            chromNameIndexMap.put(CHROM_NAMES[i], i);
            chromosomes.add(null);
            chromosomemRNABoundaryIndexList.add(null);
        }

    }

    public ReferenceGenome(int spd, int usd, int dsd) {
        splicingDis = spd;
        upstreamDis = usd;
        donwstreamDis = dsd;
        chromosomes = new ArrayList<List<RefmRNA>>();
        chromosomemRNABoundaryIndexList = new ArrayList<List<RNABoundaryIndex>>();
        for (byte i = 0; i < CHROM_NAMES.length; i++) {
            fullchromNameIndexMap.put("chr" + CHROM_NAMES[i], i);
            chromNameIndexMap.put(CHROM_NAMES[i], i);
            chromosomes.add(null);
            chromosomemRNABoundaryIndexList.add(null);
        }
    }

    public int[] getGenePos(String syb) {
        return geneChromPosMap.get(syb);
    }

    public RefmRNA getmRNA(String syb) {
        int[] poss = geneChromPosMap.get(syb);
        if (poss == null) {
            return null;
        }
        return chromosomes.get(poss[0]).get(poss[1]);
    }

    public void exportGeneRegions(String outPath) throws Exception {
        int upstreamExtendLen = 5000;
        int downstreamExtendLen = 5000;
        BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
        bw.write("--regions  ");
        for (int iChrom = 0; iChrom < CHROM_NAMES.length; iChrom++) {
            List<RefmRNA> chrom = chromosomes.get(iChrom);
            if (chrom == null) {
                continue;
            }
            for (int iGene = 0; iGene < chrom.size(); iGene++) {
                RefmRNA mrna = chrom.get(iGene);

                if (mrna.codingStart == mrna.codingEnd) {
                    //   continue;
                }
                int start = (mrna.getStart() - upstreamExtendLen);
                if (start < 0) {
                    start = 0;
                }
                String mixID = "chr" + CHROM_NAMES[iChrom] + ":"
                        + start + "-" + (mrna.getEnd() + downstreamExtendLen) + ",";
                bw.write(mixID);
                // System.out.println(mrna.getRefID() + "\t" + mrna.getStart() + "\t" + mrna.getEnd());

            }
        }
        bw.close();

    }

    public int[] getmRNAPos(String syb) {
        return geneChromPosMap.get(syb);
    }

    public RefmRNA getmRNA(int[] poss) {
        return chromosomes.get(poss[0]).get(poss[1]);
    }
    //as RefmRNA can be mappled onto multiple chrosomes so it would be very difficult to use a mrna to orgnaize the RefmRNA

    public void addRefRNA(RefmRNA mrna, String chrom) {
        // System.out.println(mrna.getRefID());

        Byte chromID = chromNameIndexMap.get(chrom);
        if (chromosomes.get(chromID) == null) {
            chromosomes.set(chromID, new ArrayList<RefmRNA>());
            chromosomes.get(chromID).add(mrna);
        } else {
            chromosomes.get(chromID).add(mrna);
        }

        geneChromPosMap.put(mrna.getRefID() + ":" + chrom + ":" + mrna.codingStart + ":" + mrna.codingEnd, new int[]{chromID, chromosomes.get(chromID).size() - 1});
        hasNotSorted = true;
    }

    public void addRefRNANoPos(RefmRNA mrna, String chrom) {
        // System.out.println(mrna.getRefID());
        Byte chromID = chromNameIndexMap.get(chrom);
        if (chromosomes.get(chromID) == null) {
            chromosomes.set(chromID, new ArrayList<RefmRNA>());
            chromosomes.get(chromID).add(mrna);
        } else {
            chromosomes.get(chromID).add(mrna);
        }

        geneChromPosMap.put(mrna.getRefID(), new int[]{chromID, chromosomes.get(chromID).size() - 1});
        hasNotSorted = true;
    }

    /*
     * A reference from Annovar
     Feature	Value 	Explanation
     nonsynonymous	1	Variants result in a codon coding for a different amino acid (missense) and an amino acid codon to a stop codon (stopgain) and a stop codon to an amino acid codon (stoplos)
     synonymous	2	
     splicing	3	variant is within 2-bp of a splicing junction (use -splicing_threshold to change this) 
     ncRNA	4	variant overlaps a transcript without coding annotation in the region definition (see Notes below for more explanation) 
     UTR5	5	variant overlaps a 5' untranslated region 
     UTR3	6	variant overlaps a 3' untranslated region 
     intronic	7	variant overlaps an intron 
     upstream	8	variant overlaps 1-kb region upstream of transcription start site 
     downstream	9	variant overlaps 1-kb region downtream of transcription end site (use -neargene to change this) 
     intergenic	10	variant is in intergenic region 
    
     */
    public Set<String> getVarFeature(String chrom, SNP var) throws Exception {
        Byte chromID = chromNameIndexMap.get(chrom);
        if (chromID == null) {
            throw new Exception("Unknown chrosomsome ID " + chrom);
        }

        if (hasNotSorted) {
            throw new Exception("The genome has to been sorted before search the gene-feature of a variant.");
        }
        List<GeneFeature> featureList = new ArrayList<GeneFeature>();
        // System.out.println(chrom + " : " + var.physicalPosition);
        Set<String> geneSymbs = new HashSet<String>();

        if (chromosomemRNABoundaryIndexList.get(chromID) == null) {
            String info = "Warning: a variant at " + var.physicalPosition + " of chromosome " + chrom + " cannot be annotated!";
            //System.out.println(info);
            var.geneFeatureID = VarFeatureIDMap.get("unknown");
            return geneSymbs;
        }

        /*
         if (chrom.equals("6")){
         var.startPhysicalPosition=	31322303;
         var.setRefAllele('C');
         var.setAltAlleles(new String[]{"G"});
         }
         * 
         */
        //note for forward and reverse strand, the upstream and downsteam could be different
        int pos = var.physicalPosition;
        searchedmRNABoundaryIndex.position = pos;

        // System.out.println(pos);
        int headIndex = 0;
        int tailIndex = Collections.binarySearch(chromosomemRNABoundaryIndexList.get(chromID), searchedmRNABoundaryIndex, rNAGeneBoundaryIndexComparator);
        if (tailIndex < 0) {
            tailIndex = -tailIndex - 1;
            if (tailIndex == chromosomemRNABoundaryIndexList.get(chromID).size()) {
                var.geneFeatureID = VarFeatureIDMap.get("intergenic");
                return geneSymbs;
            }
            headIndex = tailIndex - 1;
            if (headIndex < 0) {
                var.geneFeatureID = VarFeatureIDMap.get("intergenic");
                return geneSymbs;
            }
            RNABoundaryIndex rbi1 = chromosomemRNABoundaryIndexList.get(chromID).get(headIndex);
            //impossible to be empty; it at least inlude the RefmRNA itselft
            /*
             if (rbi1.mRNAIndexList.isEmpty()) {
             var.geneFeatureID = GlobalManager.VarFeatureIDMap.get("intergenic");
             return geneSymbs;
             }
             * 
             */

            RNABoundaryIndex rbi2 = chromosomemRNABoundaryIndexList.get(chromID).get(tailIndex);
            //impossible to be empty; it at least inlude the RefmRNA itselft
            /*
             if (rbi2.mRNAIndexList.isEmpty()) {
             var.geneFeatureID = GlobalManager.VarFeatureIDMap.get("intergenic");
             return geneSymbs;
             }
             * 
             */
            int size1 = rbi1.mRNAIndexList.size();
            int size2 = rbi2.mRNAIndexList.size();
            if (size1 < size2) {
                for (int i = 0; i < size1; i++) {
                    int searchIndex = rbi1.mRNAIndexList.getQuick(i);
                    if (rbi2.mRNAIndexList.contains(searchIndex)) {
                        RefmRNA mRNA = chromosomes.get(chromID).get(searchIndex);
                        GeneFeature gf = mRNA.findCrudeFeature(pos, upstreamDis, donwstreamDis, splicingDis);
                        if (gf != null && gf.id < 14) {

                            //gf.setName(mRNA.geneSymb + ":" + gf.getName());
                            featureList.add(gf);
                            geneSymbs.add(mRNA.getRefID() + ":" + chrom + ":" + mRNA.codingStart + ":" + mRNA.codingEnd);
                        }
                    }
                }

            } else {
                for (int i = 0; i < size2; i++) {
                    int searchIndex = rbi2.mRNAIndexList.getQuick(i);
                    if (rbi1.mRNAIndexList.contains(searchIndex)) {
                        RefmRNA mRNA = chromosomes.get(chromID).get(searchIndex);
                        GeneFeature gf = mRNA.findCrudeFeature(pos, upstreamDis, donwstreamDis, splicingDis);
                        if (gf != null && gf.id < 14) {
                            //gf.setName(mRNA.geneSymb + ":" + gf.getName());
                            featureList.add(gf);
                            geneSymbs.add(mRNA.getRefID() + ":" + chrom + ":" + mRNA.codingStart + ":" + mRNA.codingEnd);
                        }
                    }
                }
            }

        } else {
            RNABoundaryIndex rbi = chromosomemRNABoundaryIndexList.get(chromID).get(tailIndex);
            if (rbi.mRNAIndexList.isEmpty()) {
                var.geneFeatureID = VarFeatureIDMap.get("intergenic");
                return geneSymbs;
            }
            for (int i = 0; i < rbi.mRNAIndexList.size(); i++) {
                int searchIndex = rbi.mRNAIndexList.getQuick(i);
                RefmRNA mRNA = chromosomes.get(chromID).get(searchIndex);
                GeneFeature gf = mRNA.findCrudeFeature(pos, upstreamDis, donwstreamDis, splicingDis);
                if (gf != null && gf.id < 14) {
                    // gf.setName(mRNA.geneSymb + ":" + gf.getName());
                    featureList.add(gf);
                    geneSymbs.add(mRNA.getRefID() + ":" + chrom + ":" + mRNA.codingStart + ":" + mRNA.codingEnd);
                }
            }
        }

        if (featureList.isEmpty()) {
            var.geneFeatureID = VarFeatureIDMap.get("intergenic");
        } else {
            int gfSize = featureList.size();
            if (gfSize == 1) {
                var.geneFeatureID = featureList.get(0).id;
            } else {
                Collections.sort(featureList, new GeneFeatureComparator());
                var.geneFeatureID = featureList.get(0).id;
            }
        }
        return geneSymbs;
    }

    public Set<String> getVarFeatureCustomGene(String chrom, SNP var) throws Exception {
        Byte chromID = chromNameIndexMap.get(chrom);
        if (chromID == null) {
            throw new Exception("Unknown chrosomsome ID " + chrom);
        }

        if (hasNotSorted) {
            throw new Exception("The genome has to been sorted before search the gene-feature of a variant.");
        }
        List<GeneFeature> featureList = new ArrayList<GeneFeature>();
        // System.out.println(chrom + " : " + var.physicalPosition);
        Set<String> geneSymbs = new HashSet<String>();
 
        if (chromosomemRNABoundaryIndexList.get(chromID) == null) {
            String info = "Warning: a variant at " + var.physicalPosition + " of chromosome " + chrom + " cannot be annotated!";
            //System.out.println(info);
            var.geneFeatureID = VarFeatureIDMap.get("unknown");
            return geneSymbs;
        }

        /*
         if (chrom.equals("6")){
         var.startPhysicalPosition=	31322303;
         var.setRefAllele('C');
         var.setAltAlleles(new String[]{"G"});
         }
         * 
         */
        //note for forward and reverse strand, the upstream and downsteam could be different
        int pos = var.physicalPosition;
        searchedmRNABoundaryIndex.position = pos;

        // System.out.println(pos);
        int headIndex = 0;
        int tailIndex = Collections.binarySearch(chromosomemRNABoundaryIndexList.get(chromID), searchedmRNABoundaryIndex, rNAGeneBoundaryIndexComparator);
        if (tailIndex < 0) {
            tailIndex = -tailIndex - 1;
            if (tailIndex == chromosomemRNABoundaryIndexList.get(chromID).size()) {
                var.geneFeatureID = VarFeatureIDMap.get("intergenic");
                return geneSymbs;
            }
            headIndex = tailIndex - 1;
            if (headIndex < 0) {
                var.geneFeatureID = VarFeatureIDMap.get("intergenic");
                return geneSymbs;
            }
            RNABoundaryIndex rbi1 = chromosomemRNABoundaryIndexList.get(chromID).get(headIndex);
            //impossible to be empty; it at least inlude the RefmRNA itselft
            /*
             if (rbi1.mRNAIndexList.isEmpty()) {
             var.geneFeatureID = GlobalManager.VarFeatureIDMap.get("intergenic");
             return geneSymbs;
             }
             * 
             */

            RNABoundaryIndex rbi2 = chromosomemRNABoundaryIndexList.get(chromID).get(tailIndex);
            //impossible to be empty; it at least inlude the RefmRNA itselft
            /*
             if (rbi2.mRNAIndexList.isEmpty()) {
             var.geneFeatureID = GlobalManager.VarFeatureIDMap.get("intergenic");
             return geneSymbs;
             }
             * 
             */
            int size1 = rbi1.mRNAIndexList.size();
            int size2 = rbi2.mRNAIndexList.size();
            if (size1 < size2) {
                for (int i = 0; i < size1; i++) {
                    int searchIndex = rbi1.mRNAIndexList.getQuick(i);
                    if (rbi2.mRNAIndexList.contains(searchIndex)) {
                        RefmRNA mRNA = chromosomes.get(chromID).get(searchIndex);
                        GeneFeature gf = mRNA.findCrudeFeatureCustomGene(pos);
                        if (gf != null && gf.id < 14) {

                            //gf.setName(mRNA.geneSymb + ":" + gf.getName());
                            featureList.add(gf);
                            geneSymbs.add(mRNA.getRefID());
                        }
                    }
                }
            } else {
                for (int i = 0; i < size2; i++) {
                    int searchIndex = rbi2.mRNAIndexList.getQuick(i);
                    if (rbi1.mRNAIndexList.contains(searchIndex)) {
                        RefmRNA mRNA = chromosomes.get(chromID).get(searchIndex);
                        GeneFeature gf = mRNA.findCrudeFeatureCustomGene(pos);
                        if (gf != null && gf.id < 14) {
                            //gf.setName(mRNA.geneSymb + ":" + gf.getName());
                            featureList.add(gf);
                            geneSymbs.add(mRNA.getRefID());
                        }
                    }
                }
            }
        } else {
            RNABoundaryIndex rbi = chromosomemRNABoundaryIndexList.get(chromID).get(tailIndex);
            if (rbi.mRNAIndexList.isEmpty()) {
                var.geneFeatureID = VarFeatureIDMap.get("intergenic");
                return geneSymbs;
            }
            for (int i = 0; i < rbi.mRNAIndexList.size(); i++) {
                int searchIndex = rbi.mRNAIndexList.getQuick(i);
                RefmRNA mRNA = chromosomes.get(chromID).get(searchIndex);
                GeneFeature gf = mRNA.findCrudeFeatureCustomGene(pos);
                if (gf != null && gf.id < 14) {
                    // gf.setName(mRNA.geneSymb + ":" + gf.getName());
                    featureList.add(gf);
                    geneSymbs.add(mRNA.getRefID() );
                }
            }
        }

        if (featureList.isEmpty()) {
            var.geneFeatureID = VarFeatureIDMap.get("intergenic");
        } else {
            int gfSize = featureList.size();
            if (gfSize == 1) {
                var.geneFeatureID = featureList.get(0).id;
            } else {
                Collections.sort(featureList, new GeneFeatureComparator());
                var.geneFeatureID = featureList.get(0).id;
            }
        }
        return geneSymbs;
    }

    private int binarySearchStartPos(int pos, int left, int right, List<RNASegment> mRNABounds) {
        if (left > right) {
            return -left - 1;
        }
        int middle = (left + right) / 2;
// the search is based on start posistion

        if ((mRNABounds.get(middle)).start == pos) {
            return middle;
        } else if ((mRNABounds.get(middle)).start > pos) {
            return binarySearchStartPos(pos, left, middle - 1, mRNABounds);
        } else {
            return binarySearchStartPos(pos, middle + 1, right, mRNABounds);
        }
    }

    class RNASegment {

        protected int start;
        protected int end;
        protected int mRNAIndex;
        protected char strand;

        public RNASegment(int start, int end, int mRNAIndex, char strand) {
            this.start = start;
            this.end = end;

            this.mRNAIndex = mRNAIndex;
            this.strand = strand;
        }

        public int getEnd() {
            return end;
        }

        public int getmRNAIndex() {
            return mRNAIndex;
        }

        public int getStart() {
            return start;
        }
    }

    class GeneRNASegmentComparator implements Comparator<RNASegment> {

        @Override
        public int compare(RNASegment arg0, RNASegment arg1) {
            int result = -1;
            if (arg0.start == arg1.start) {
                result = arg0.end - arg1.end;
            } else {
                result = arg0.start - arg1.start;
            }
            return result;
        }
    }

    public void sortmRNAMakeIndexonChromosomes() {
        geneChromPosMap.clear();
        int size = 0;
        List<RNASegment> rNAPositionIndexList = new ArrayList<RNASegment>();

        for (int i = 0; i < CHROM_NAMES.length; i++) {
            if (chromosomes.get(i) != null) {
                Collections.sort(chromosomes.get(i), new SeqSegmentComparator());
                size = chromosomes.get(i).size();
                rNAPositionIndexList.clear();
                if (chromosomemRNABoundaryIndexList.get(i) == null) {
                    chromosomemRNABoundaryIndexList.set(i, new ArrayList<RNABoundaryIndex>());
                } else {
                    chromosomemRNABoundaryIndexList.get(i).clear();
                }
                for (int j = 0; j < size; j++) {
                    RefmRNA mrna = chromosomes.get(i).get(j);

                    geneChromPosMap.put(mrna.getRefID() + ":" + CHROM_NAMES[i] + ":" + mrna.codingStart + ":" + mrna.codingEnd, new int[]{i, j});
                    if (mrna.getStrand() == '-') {
                        RNASegment gns = new RNASegment(mrna.start - donwstreamDis, mrna.end + upstreamDis, j, mrna.getStrand());
                        rNAPositionIndexList.add(gns);
                        chromosomemRNABoundaryIndexList.get(i).add(new RNABoundaryIndex(mrna.start - donwstreamDis));
                        chromosomemRNABoundaryIndexList.get(i).add(new RNABoundaryIndex(mrna.end + upstreamDis));
                    } else {
                        //default is +
                        RNASegment gns = new RNASegment(mrna.start - donwstreamDis, mrna.end + donwstreamDis, j, mrna.getStrand());
                        rNAPositionIndexList.add(gns);
                        chromosomemRNABoundaryIndexList.get(i).add(new RNABoundaryIndex(mrna.start - upstreamDis));
                        chromosomemRNABoundaryIndexList.get(i).add(new RNABoundaryIndex(mrna.end + donwstreamDis));
                    }

                }

                // the chromosomemRNABoundaryIndexList will be used to map a variant
                Collections.sort(rNAPositionIndexList, new GeneRNASegmentComparator());
                Collections.sort(chromosomemRNABoundaryIndexList.get(i), new RNABoundaryIndexComparator());
                int mrnaSize = rNAPositionIndexList.size();

                int boundSize = chromosomemRNABoundaryIndexList.get(i).size();
                for (int j = 0; j < boundSize; j++) {
                    RNABoundaryIndex rbi = chromosomemRNABoundaryIndexList.get(i).get(j);
                    int pos = rbi.position;
                    int genStartIndex = binarySearchStartPos(pos, 0, mrnaSize - 1, rNAPositionIndexList);
                    if (genStartIndex < 0) {
                        genStartIndex = -genStartIndex - 1;
                    }
                    if (genStartIndex >= mrnaSize) {
                        genStartIndex = mrnaSize - 1;
                    }
                    while (genStartIndex < mrnaSize && rNAPositionIndexList.get(genStartIndex).start <= pos) {
                        genStartIndex++;
                    }
                    if (genStartIndex >= mrnaSize) {
                        genStartIndex = mrnaSize - 1;
                    }
                    //consider the max distance. and it will further categrize this according to 
                    //strand information of transcripts
                    //there are overlapped genes 
                    //can you provide some tail information to avoid exploring always from the 0 index
                    for (int searchIndex = 0; searchIndex <= genStartIndex; searchIndex++) {
                        RNASegment region = rNAPositionIndexList.get(searchIndex);
                        if (region.strand == '-') {
                            if (region.start - donwstreamDis <= pos && region.end + upstreamDis >= pos) {
                                rbi.addIndexes(region.mRNAIndex);
                            }
                        } else //default is +
                        {
                            if (region.start - upstreamDis <= pos && region.end + donwstreamDis >= pos) {
                                rbi.addIndexes(region.mRNAIndex);
                            }
                        }
                    }
                }
            }
        }
        hasNotSorted = false;
    }

    public void sortmRNAMakeIndexonChromosomesCustomGene() {
        geneChromPosMap.clear();
        int size = 0;
        List<RNASegment> rNAPositionIndexList = new ArrayList<RNASegment>();
        SeqSegmentEndComparator seqEC = new SeqSegmentEndComparator();
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            if (chromosomes.get(i) != null) {
                Collections.sort(chromosomes.get(i), new SeqSegmentComparator());
                size = chromosomes.get(i).size();
                rNAPositionIndexList.clear();
                if (chromosomemRNABoundaryIndexList.get(i) == null) {
                    chromosomemRNABoundaryIndexList.set(i, new ArrayList<RNABoundaryIndex>());
                } else {
                    chromosomemRNABoundaryIndexList.get(i).clear();
                }
                for (int j = 0; j < size; j++) {
                    RefmRNA mrna = chromosomes.get(i).get(j);
                    Collections.sort(mrna.getExons(), seqEC);
                    geneChromPosMap.put(mrna.getRefID(), new int[]{i, j});
                    if (mrna.getStrand() == '-') {
                        RNASegment gns = new RNASegment(mrna.start - donwstreamDis, mrna.end + upstreamDis, j, mrna.getStrand());
                        rNAPositionIndexList.add(gns);
                        chromosomemRNABoundaryIndexList.get(i).add(new RNABoundaryIndex(mrna.start - donwstreamDis));
                        chromosomemRNABoundaryIndexList.get(i).add(new RNABoundaryIndex(mrna.end + upstreamDis));
                    } else {
                        //default is +
                        RNASegment gns = new RNASegment(mrna.start - donwstreamDis, mrna.end + donwstreamDis, j, mrna.getStrand());
                        rNAPositionIndexList.add(gns);
                        chromosomemRNABoundaryIndexList.get(i).add(new RNABoundaryIndex(mrna.start - upstreamDis));
                        chromosomemRNABoundaryIndexList.get(i).add(new RNABoundaryIndex(mrna.end + donwstreamDis));
                    }

                }

                // the chromosomemRNABoundaryIndexList will be used to map a variant
                Collections.sort(rNAPositionIndexList, new GeneRNASegmentComparator());
                Collections.sort(chromosomemRNABoundaryIndexList.get(i), new RNABoundaryIndexComparator());
                int mrnaSize = rNAPositionIndexList.size();

                int boundSize = chromosomemRNABoundaryIndexList.get(i).size();
                for (int j = 0; j < boundSize; j++) {
                    RNABoundaryIndex rbi = chromosomemRNABoundaryIndexList.get(i).get(j);
                    int pos = rbi.position;
                    int genStartIndex = binarySearchStartPos(pos, 0, mrnaSize - 1, rNAPositionIndexList);
                    if (genStartIndex < 0) {
                        genStartIndex = -genStartIndex - 1;
                    }
                    if (genStartIndex >= mrnaSize) {
                        genStartIndex = mrnaSize - 1;
                    }
                    while (genStartIndex < mrnaSize && rNAPositionIndexList.get(genStartIndex).start <= pos) {
                        genStartIndex++;
                    }
                    if (genStartIndex >= mrnaSize) {
                        genStartIndex = mrnaSize - 1;
                    }
                    //consider the max distance. and it will further categrize this according to 
                    //strand information of transcripts
                    //there are overlapped genes 
                    //can you provide some tail information to avoid exploring always from the 0 index
                    for (int searchIndex = 0; searchIndex <= genStartIndex; searchIndex++) {
                        RNASegment region = rNAPositionIndexList.get(searchIndex);
                        if (region.strand == '-') {
                            if (region.start - donwstreamDis <= pos && region.end + upstreamDis >= pos) {
                                rbi.addIndexes(region.mRNAIndex);
                            }
                        } else //default is +
                        {
                            if (region.start - upstreamDis <= pos && region.end + donwstreamDis >= pos) {
                                rbi.addIndexes(region.mRNAIndex);
                            }
                        }
                    }
                }
            }
        }
        hasNotSorted = false;
    }
}
