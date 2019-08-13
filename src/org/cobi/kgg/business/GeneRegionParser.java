/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.picard.liftover.LiftOver;
import net.sf.picard.util.Interval;

import net.sf.samtools.util.AsciiLineReader;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import org.cobi.kgg.business.entity.Constants;
import org.cobi.kgg.business.entity.ReferenceGenome;
import org.cobi.kgg.business.entity.RefmRNA;
import org.cobi.kgg.business.entity.SeqSegment;

/**
 *
 * @author mxli
 */
public class GeneRegionParser implements Constants {

    private final static Logger LOG = Logger.getLogger(GeneRegionParser.class.getName());

    /**
     * @return @throws Exception
     * @pdOid f0621cff-9d97-421e-a77a-765bd0938dfb
     */
    public ReferenceGenome readRefGene(String vAFile, int splicing,
            int upstream, int downStream, LiftOver liftOver, String covertHGInfor) throws Exception {
        int indexmRNAName = 1;
        int indexChom = 2;
        int indexStrand = 3;
        int indexTxStart = 4;
        int indexTxEnd = 5;
        int indexCdsStart = 6;
        int indexCdsEnd = 7;
        int indexExonCount = 8;
        int indexExonStarts = 9;
        int indexExonEnds = 10;
        int indexName2 = 12;
        int maxColNum = indexmRNAName;
        maxColNum = Math.max(maxColNum, indexChom);
        maxColNum = Math.max(maxColNum, indexStrand);
        maxColNum = Math.max(maxColNum, indexTxStart);
        maxColNum = Math.max(maxColNum, indexTxEnd);
        maxColNum = Math.max(maxColNum, indexCdsStart);
        maxColNum = Math.max(maxColNum, indexCdsEnd);
        maxColNum = Math.max(maxColNum, indexExonCount);
        maxColNum = Math.max(maxColNum, indexExonStarts);
        maxColNum = Math.max(maxColNum, indexExonEnds);
        maxColNum = Math.max(maxColNum, indexName2);
        int faildedConvertNum = 0;

        ReferenceGenome genome = new ReferenceGenome(splicing, upstream, downStream);
        String currentLine = null;
        String currChr = null;
        StringBuilder tmpBuffer = new StringBuilder();
        long lineCounter = 0;

        File dataFile = new File(vAFile);
        LineReader br = null;
        if (dataFile.exists() && dataFile.getName().endsWith(".zip")) {
            br = new CompressedFileReader(dataFile);
        } else if (dataFile.exists() && dataFile.getName().endsWith(".gz")) {
            br = new CompressedFileReader(dataFile);
        } else if (dataFile.exists()) {
            br = new AsciiLineReader(dataFile);
        } else {
            throw new Exception("No input file: " + dataFile.getCanonicalPath());
        }
        boolean incomplete = true;
        System.out.print("Parse ");
        String mRNAName = null;
        char strand = '0';
        int cdsStart = -1;
        int cdsEnd = -1;
        int txStart = -1;
        int txEnd = -1;
        String exonStarts = null;
        String exonEnds = null;
        String geneSym = null;

        int transcriptNum = 0;
        boolean needConvert = false;
        Set<String> mRNAIDSet = new HashSet<String>();

        if (liftOver != null) {
            needConvert = true;
        }
        try {
            /*
             //skip to the head line 
             while ((currentLine = br.readLine()) != null) {
             lineCounter++;
             if (currentLine.startsWith("VAR")) {
             break;
             }
             }
             * 
             */
            currentLine = br.readLine();
            do {
                if (currentLine == null) {
                    break;
                }
                if (currentLine.startsWith("##")) {
                    continue;
                }
                //System.out.println(currentLine);
                lineCounter++;
                StringTokenizer st = new StringTokenizer(currentLine.trim());
                //initialize varaibles
                incomplete = true;
                currChr = null;
                mRNAName = null;
                strand = '0';
                cdsStart = -1;
                cdsEnd = -1;
                txStart = -1;
                txEnd = -1;
                exonStarts = null;
                exonEnds = null;
                geneSym = null;
                for (int iCol = 0; iCol <= maxColNum; iCol++) {
                    if (st.hasMoreTokens()) {
                        tmpBuffer.delete(0, tmpBuffer.length());
                        tmpBuffer.append(st.nextToken().trim());
                        if (iCol == indexmRNAName) {
                            mRNAName = tmpBuffer.toString();
                        } else if (iCol == indexStrand) {
                            strand = tmpBuffer.charAt(0);
                        } else if (iCol == indexChom) {
                            currChr = tmpBuffer.toString();
                        } else if (iCol == indexTxStart) {
                            txStart = Integer.parseInt(tmpBuffer.toString());
                        } else if (iCol == indexTxEnd) {
                            txEnd = Integer.parseInt(tmpBuffer.toString());
                        } else if (iCol == indexCdsStart) {
                            cdsStart = Integer.parseInt(tmpBuffer.toString());
                        } else if (iCol == indexCdsEnd) {
                            cdsEnd = Integer.parseInt(tmpBuffer.toString());
                        } else if (iCol == indexExonStarts) {
                            exonStarts = tmpBuffer.toString();
                        } else if (iCol == indexExonEnds) {
                            exonEnds = tmpBuffer.toString();
                        } else if (iCol == indexName2) {
                            geneSym = tmpBuffer.toString();
                        }
                    } else {
                        break;
                    }
                    if (iCol == maxColNum) {
                        incomplete = false;
                    }
                }

                if (incomplete) {
                    continue;
                }

                int index = currChr.indexOf('_');
                if (index >= 0) {
                    // ingnore these uncertain genes like chr1_random
                    continue;
                    //currChr = currChr.substring(0, currChr.indexOf('_'));
                }

                index = currChr.indexOf('.');
                if (index >= 0) {
                    // ingnore these uncertain genes like chr1_random
                    continue;
                    //currChr = currChr.substring(0, currChr.indexOf('_'));
                }
                
                if (needConvert) {
                    /*
                     Interval interval = new Interval(currChr, txStart, txStart);
                     Interval int2 = liftOver.liftOver(interval);
                     if (int2 != null) {
                     txStart = int2.getStart();
                     }
                     interval.setStart(txEnd);
                     interval.setEnd(txEnd);
                     int2 = liftOver.liftOver(interval);
                     if (int2 != null) {
                     txEnd = int2.getEnd();
                     }
                     if (cdsStart > 0) {
                     interval.setStart(cdsStart);
                     interval.setEnd(cdsStart);
                     int2 = liftOver.liftOver(interval);
                     if (int2 != null) {
                     cdsStart = int2.getStart();
                     }
                    
                     interval.setStart(cdsEnd);
                     interval.setEnd(cdsEnd);
                     int2 = liftOver.liftOver(interval);
                     if (int2 != null) {
                     cdsEnd = int2.getEnd();
                     }
                     }
                     * 
                     */
                    Interval interval = new Interval(currChr, txStart, txEnd);
                    Interval int2 = liftOver.liftOver(interval);
                    if (int2 != null) {
                        txStart = int2.getStart();
                        txEnd = int2.getEnd();
                    } else {
                        faildedConvertNum++;
                    }
                    if (cdsStart > 0) {
                        interval.setStart(cdsStart);
                        interval.setEnd(cdsEnd);
                        int2 = liftOver.liftOver(interval);
                        if (int2 != null) {
                            cdsStart = int2.getStart();
                            cdsEnd = int2.getEnd();
                        }
                    }
                }
                RefmRNA mrna = new RefmRNA(mRNAName, txStart, txEnd, cdsStart, cdsEnd);
                mrna.setStrand(strand);

                String[] bounderStarts = exonStarts.split(",");
                String[] bounderEnds = exonEnds.split(",");
                // System.out.println(mRNAName);
                for (int i = 0; i < bounderStarts.length; i++) {
                    int start = Integer.parseInt(bounderStarts[i]);
                    int end = Integer.parseInt(bounderEnds[i]);
                    if (needConvert) {
                        /*
                         Interval interval = new Interval(currChr, start, start);
                         Interval int2 = liftOver.liftOver(interval);
                         if (int2 != null) {
                         start = int2.getStart();
                         }
                         interval = new Interval("chr" + currChr, end, end);
                         int2 = liftOver.liftOver(interval);
                         if (int2 != null) {
                         end = int2.getStart();
                         }
                         * 
                         */
                        Interval interval = new Interval(currChr, start, end);
                        Interval int2 = liftOver.liftOver(interval);
                        if (int2 != null) {
                            start = int2.getStart();
                            end = int2.getEnd();
                        }

                    }
                    SeqSegment exon = new SeqSegment(start, end);
                    mrna.addExon(exon);
                }
                mrna.makeAccuIntronLength();
                int[] poss = genome.getmRNAPos(mrna.getRefID() + ":" + currChr + ":" + mrna.getCodingStart() + ":" + mrna.getCodingEnd());
                if (mRNAIDSet.contains(mrna.getRefID())) {
                    // mrna.setMultipleMapping(true);
                } else {
                    mRNAIDSet.add(mrna.getRefID());
                }

                if (geneSym.startsWith("ENSG")) {
                    String[] cells = geneSym.split(";");
                    geneSym = cells[1];
                }

                mrna.setGeneSymb(geneSym);
                if (poss != null) {
                    mrna = genome.getmRNA(poss);
                    if (!currChr.substring(3).equals(CHROM_NAMES[poss[0]])) {
                        //note a transcript can be mapped onto multiple locations
                        String info = "Duplicated refGene items: " + mRNAName;
                        System.out.println(info);
                        continue;
                    }
                } else {
                    transcriptNum++;
                    genome.addRefRNA(mrna, currChr.substring(3));
                }

                // System.out.println(currentLine);
            } while ((currentLine = br.readLine()) != null);
            String info = transcriptNum + " transcripts of genes have been read!";
            if (faildedConvertNum > 0) {
                info += "\n Warning: " + faildedConvertNum + " transcripts's boundaries failed to convert from " + covertHGInfor;
            }
            LOG.info(info);

        } catch (NumberFormatException nex) {
            String info = nex.toString() + " when parsing at line " + lineCounter + ": " + currentLine;
            // LOG.error(nex, info);
            throw new Exception(info);
        }
        br.close();
        genome.sortmRNAMakeIndexonChromosomes();
        return genome;
    }

    public ReferenceGenome readCustomGene(String vAFile, LiftOver liftOver, String covertHGInfor) throws Exception {
        int faildedConvertNum = 0;
        ReferenceGenome genome = new ReferenceGenome(0, 0, 0);
        String currentLine = null;
        String currChr = null;
        StringBuilder tmpBuffer = new StringBuilder();
        long lineCounter = 0;

        File dataFile = new File(vAFile);
        LineReader br = null;
        if (dataFile.exists() && dataFile.getName().endsWith(".zip")) {
            br = new CompressedFileReader(dataFile);
        } else if (dataFile.exists() && dataFile.getName().endsWith(".gz")) {
            br = new CompressedFileReader(dataFile);
        } else if (dataFile.exists()) {
            br = new AsciiLineReader(dataFile);
        } else {
            throw new Exception("No input file: " + dataFile.getCanonicalPath());
        }

        int txStart = -1;
        int txEnd = -1;

        String geneSym = null;
        String region;
        int minStart;
        int maxEnd;
        int transcriptNum = 0;
        RefmRNA mrna;
        boolean needConvert = false;
        Set<String> mRNAIDSet = new HashSet<String>();
        int index, index1;
        if (liftOver != null) {
            needConvert = true;
        }
        List<SeqSegment> inervals = new ArrayList<SeqSegment>();
        try {
            while ((currentLine = br.readLine()) != null) {
                lineCounter++;
                StringTokenizer st = new StringTokenizer(currentLine.trim(), " ,");
                //initialize varaibles 
                currChr = null;
                txStart = -1;
                txEnd = -1;
                geneSym = st.nextToken();
                minStart = Integer.MAX_VALUE;
                maxEnd = -1;

                while (st.hasMoreTokens()) {
                    region = st.nextToken();
                    index = region.indexOf(":");
                    currChr = region.substring(0, index);
                    index1 = region.indexOf("-");
                    if (index1 < 0) {
                        txStart = Integer.parseInt(region.substring(index + 1));
                        if (needConvert) {
                            Interval interval = new Interval(currChr, txStart, txStart);
                            Interval int2 = liftOver.liftOver(interval);
                            if (int2 != null) {
                                txStart = int2.getStart();
                            } else {
                                faildedConvertNum++;
                            }
                        }

                        txEnd = txStart + 1;
                    } else {
                        txStart = Integer.parseInt(region.substring(index + 1, index1));
                        txEnd = Integer.parseInt(region.substring(index1 + 1)) + 1;
                        if (needConvert) {
                            Interval interval = new Interval(currChr, txStart, txStart);
                            Interval int2 = liftOver.liftOver(interval);
                            if (int2 != null) {
                                txStart = int2.getStart();
                            } else {
                                faildedConvertNum++;
                            }
                            if (txStart > 0) {
                                interval.setStart(txEnd);
                                interval.setEnd(txEnd);
                                int2 = liftOver.liftOver(interval);
                                if (int2 != null) {
                                    txEnd = int2.getStart();
                                }
                            }
                        }
                    }
                    if (txStart > txEnd) {
                        index = txStart;
                        txStart = txEnd;
                        txEnd = index;
                    }
                    if (txStart < minStart) {
                        minStart = txStart;
                    }
                    if (txEnd > maxEnd) {
                        maxEnd = txEnd;
                    }
                    SeqSegment exon = new SeqSegment(txStart, txEnd);
                    inervals.add(exon);
                }

                int[] poss = genome.getmRNAPos(geneSym);
                if (mRNAIDSet.contains(geneSym)) {
                    mrna = genome.getmRNA(poss);
                    if (minStart < mrna.getStart()) {
                        mrna.setStart(minStart);
                    }
                    if (maxEnd > mrna.getEnd()) {
                        mrna.setEnd(maxEnd);
                    }
                    mrna.addExons(inervals);
                } else {
                    mrna = new RefmRNA(geneSym);
                    mrna.setStart(minStart);
                    mrna.setEnd(maxEnd);
                    mrna.addExons(inervals);
                    mrna.setGeneSymb(geneSym);
                    mRNAIDSet.add(mrna.getRefID());
                    transcriptNum++;
                    genome.addRefRNANoPos(mrna, currChr.substring(3));
                }
                inervals.clear();
            }
            String info = transcriptNum + " genes have been read!";
            if (faildedConvertNum > 0) {
                info += "\n Warning: " + faildedConvertNum + " transcripts's boundaries failed to convert from " + covertHGInfor;
            }
            LOG.info(info);

        } catch (NumberFormatException nex) {
            String info = nex.toString() + " when parsing at line " + lineCounter + ": " + currentLine;
            // LOG.error(nex, info);
            throw new Exception(info);
        }
        br.close();
        genome.sortmRNAMakeIndexonChromosomesCustomGene();
        return genome;
    }

    public static void main(String[] args) {
        GeneRegionParser gpp = new GeneRegionParser();
        try {
            // ReferenceGenome genom = gpp.readRefGene("resources/hg18/refGene.txt", 2, 1000, 1000, null, null);
            ReferenceGenome genom = gpp.readCustomGene("resources/myGene.txt", null, null);
            genom.exportGeneRegions("regions.txt");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
