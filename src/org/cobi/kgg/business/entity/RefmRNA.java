/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import cern.colt.list.IntArrayList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MX Li
 */
public class RefmRNA extends SeqSegment implements Constants {

    private char strand = '0';
    private String refID;
    private int length;
    private String mRnaSequence;
    private int mRnaSequenceStart;
    //Note: The end boundary of each exon is not included in refGene database
    private List<SeqSegment> exons;
    public int codingStart;
    //probably codingEnd is exclusive
    public int codingEnd;
    boolean noCodingExon = true;
    int exonNum = 0;
    int codingStartRelativeSiteInSequence = -1;
    int codingStartSiteExonID = 0;
    IntArrayList intronLength = new IntArrayList();

    String uniprotID = null;
    String geneSymb = null;
    boolean multipleMapping = false;

    public boolean isMultipleMapping() {
        return multipleMapping;
    }

    public void setMultipleMapping(boolean multipleMapping) {
        this.multipleMapping = multipleMapping;
    }

    public String getGeneSymb() {
        return geneSymb;
    }

    public List<SeqSegment> getExons() {
        return exons;
    }

    public void setExons(List<SeqSegment> exons) {
        this.exons = exons;
    }

    public void addExons(List<SeqSegment> exons) {
        this.exons.addAll(exons);
    }

    public void setGeneSymb(String geneSymb) {
        this.geneSymb = geneSymb;
    }

    public void setUniprotID(String uniprotID) {
        this.uniprotID = uniprotID;
    }

    public int getCodingEnd() {
        return codingEnd;
    }

    public int getCodingStart() {
        return codingStart;
    }

    public void setmRnaSequenceStart(int mRnaSequenceStart) {
        this.mRnaSequenceStart = mRnaSequenceStart;
    }

    public void setmRnaSequence(String mRnaSequence) {
        this.mRnaSequence = mRnaSequence;
    }

    public RefmRNA(String refID, int start, int end) {
        super(start, end);
        this.refID = refID;
        this.exons = new ArrayList<SeqSegment>();
    }

    public RefmRNA(String refID) {
        super(0, 0);
        this.refID = refID;
        this.exons = new ArrayList<SeqSegment>();
    }

    public String getRefID() {
        return refID;
    }

    public void setRefID(String refID) {
        this.refID = refID;
    }

    public RefmRNA(String refID, int start, int end, int codingStart, int codingEnd) {
        super(start, end);
        this.refID = refID;
        this.codingStart = codingStart;
        this.codingEnd = codingEnd;
        this.exons = new ArrayList<SeqSegment>();
        if (codingStart != codingEnd) {
            noCodingExon = false;
        }
    }

    public char getStrand() {
        return strand;
    }

    public void setStrand(char strand) {
        this.strand = strand;
    }

    public void addExon(SeqSegment exon) {
        exons.add(exon);
        exonNum++;
    }

    public void makeAccuIntronLength() throws Exception {
        if (exons == null || exons.isEmpty()) {
            return;
        }

        int accumIntronLen = 0;
        if (strand == '0') {
            throw new Exception("Unknown strand at " + refID + "; and cannot make AccuExonLength!");
        } else if (strand == '+') {
            SeqSegment exon = exons.get(0);
            //assume the boundary is not inclusive 
            if (codingStart >= exon.start && codingStart <= exon.end) {
                codingStartRelativeSiteInSequence = codingStart - exon.start;
                codingStartSiteExonID = 0;
            }
            accumIntronLen = 0;
            for (int i = 1; i < exonNum; i++) {
                exon = exons.get(i);
                //assume the boundary is not inclusive     
                intronLength.add(exons.get(i).start - exons.get(i - 1).end);
                accumIntronLen += intronLength.get(i - 1);
                if (codingStart >= exon.start && codingStart <= exon.end) {
                    codingStartRelativeSiteInSequence = codingStart - exons.get(0).start - accumIntronLen;
                    codingStartSiteExonID = i;
                }
            }
        } else {
            SeqSegment exon = exons.get(exonNum - 1);
            //assume the boundary is not inclusive 
            if (codingEnd >= exon.start && codingEnd <= exon.end) {
                codingStartRelativeSiteInSequence = exon.end - codingEnd;
                codingStartSiteExonID = exonNum - 1;
            }
            accumIntronLen = 0;
            for (int i = exonNum - 2; i >= 0; i--) {
                exon = exons.get(i);
                //assume the boundary is not inclusive              
                accumIntronLen += (exons.get(i + 1).start - exons.get(i).end);
                if (codingEnd >= exon.start && codingEnd <= exon.end) {
                    codingStartRelativeSiteInSequence = exons.get(exonNum - 1).end - codingEnd - accumIntronLen;
                    codingStartSiteExonID = i;
                }
            }
            for (int i = 1; i < exonNum; i++) {
                intronLength.add(exons.get(i).start - exons.get(i - 1).end);
            }
            //intronLength.reverse();
        }
    }

    public GeneFeature findCrudeFeature(int pos, int upstreamDis, int downstreamDis, int splicingDis) throws Exception {
        if (strand == '0') {
            throw new Exception("Unknown strand at " + refID);
        }
        int relativeCodingStartPos = -1;
        int exonIndex = binarySearch(pos, 0, exonNum - 1);
        //  System.out.println(pos);

        //note in the refgene database
        //the leftside boundaries of exon region are inclusive and rightside boundaries are exclusive 
        if (strand == '+') {
            if (exonIndex < 0) {
                exonIndex = -exonIndex - 1;
                if (exonIndex == exonNum) {
                    //after all exons                   
                    if (pos > end + downstreamDis) {
                        //intergenic
                        return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
                    } else {
                        //downstream	12	variant overlaps 1-kb region downtream of transcription end site (use -neargene to change this) 
                        return new GeneFeature(VarFeatureIDMap.get("downstream"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":downstream+" + (pos - end));
                    }
                } else if (exonIndex == 0) {
                    if (pos <= start - upstreamDis) {
                        //intergenic
                        return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
                    } else if (pos <= start) {
                        // upstream	11	variant overlaps 1-kb region upstream of transcription start site 
                        return new GeneFeature(VarFeatureIDMap.get("upstream"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":upstream-" + (start - pos));
                    } else if (noCodingExon) {
                        // ncRNA	7	variant overlaps a transcript without coding annotation in the gene definition (see Notes below for more explanation) 
                        return new GeneFeature(VarFeatureIDMap.get("ncRNA"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                    } else if (pos <= codingStart) {
                        //5UTR	8	variant overlaps a 5' untranslated region 
                        return new GeneFeature(VarFeatureIDMap.get("5UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR+" + (codingStart - pos));
                    } else if (pos <= codingEnd) {
                        //it must in the coiding region
                        // exonic   
                        //do not know why my input sample always have 1-base shift compared to the refGene coordinates on forward RefmRNA   

                        relativeCodingStartPos = pos - codingStart - 1;
                        //special coding for the exonic variantsl it will be parsed later on
                        return new GeneFeature(VarFeatureIDMap.get("exonic"), "1:" + relativeCodingStartPos);
                    } else {
                        // 3UTR	9	variant overlaps a 3' untranslated region 
                        return new GeneFeature(VarFeatureIDMap.get("3UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR+" + (pos - codingEnd));
                    }
                } else //  the index must be between 1 and exonIndex-1
                 if (noCodingExon) {
                        // ncRNA	7	variant overlaps a transcript without coding annotation in the gene definition (see Notes below for more explanation) 
                        return new GeneFeature(VarFeatureIDMap.get("ncRNA"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                    } else if (pos <= exons.get(exonIndex - 1).end + splicingDis) {
                        //splicing	6	variant is within 2-bp of a splicing junction (use -splicing_threshold to change this) 
                        return new GeneFeature(VarFeatureIDMap.get("splicing"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3splicing" + (exonIndex) + "+" + (exons.get(exonIndex - 1).end - pos));
                    } else if (pos <= exons.get(exonIndex).start - splicingDis) {
                        //5UTR	8	variant overlaps a 5' untranslated region 
                        return new GeneFeature(VarFeatureIDMap.get("intronic"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":intronic" + (exonIndex));
                    } else if (pos <= exons.get(exonIndex).start) {
                        //the 5' and 3' are relative to the closet exon
                        return new GeneFeature(VarFeatureIDMap.get("splicing"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5splicing" + (exonIndex + 1) + "-" + (exons.get(exonIndex).start - pos));
                    } else if (pos <= codingStart) {
                        //5UTR	8	variant overlaps a 5' untranslated region 
                        return new GeneFeature(VarFeatureIDMap.get("5UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR-" + (codingStart - pos));
                    } else if (pos <= codingEnd) {
                        //it must in the coiding region
                        // exonic   
                        //do not know why my input sample always have 1-base shift compared to the refGene coordinates on forward RefmRNA      
                        relativeCodingStartPos = pos - codingStart - 1;
                        for (int i = exonIndex; i > codingStartSiteExonID; i--) {
                            relativeCodingStartPos -= intronLength.getQuick(i - 1);
                        }
                        //special coding for the exonic variantsl it will be parsed later on
                        return new GeneFeature(VarFeatureIDMap.get("exonic"), (exonIndex + 1) + ":" + relativeCodingStartPos);
                    } else {
                        //
                        return new GeneFeature(VarFeatureIDMap.get("3UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR+" + (pos - codingEnd));
                    }
            } else //just at the  rightside boundary which is exclusive
             if (noCodingExon) {
                    return new GeneFeature(VarFeatureIDMap.get("ncRNA"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                } else if (pos <= codingStart) {
                    return new GeneFeature(VarFeatureIDMap.get("5UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR-" + (codingStart - pos));
                } else if (pos <= codingEnd) {
                    relativeCodingStartPos = pos - codingStart - 1;
                    for (int i = exonIndex; i > codingStartSiteExonID; i--) {
                        relativeCodingStartPos -= intronLength.getQuick(i - 1);
                    }
                    //special coding for the exonic variantsl it will be parsed later on
                    return new GeneFeature(VarFeatureIDMap.get("exonic"), (exonIndex + 1) + ":" + relativeCodingStartPos);
                    //  return new GeneFeature(VarFeatureIDMap.get("splicing"), refID+"("+exonNum+"Exons"  + (multipleMapping ? "MultiMap)" : ")") + ":3splicing" + (exonIndex + 1));
                } else {
                    return new GeneFeature(VarFeatureIDMap.get("3UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR+" + (pos - codingEnd));
                }
        } else if (strand == '-') {
            if (exonIndex < 0) {
                exonIndex = -exonIndex - 1;

                //after all exons
                if (exonIndex == exonNum) {
                    if (pos <= end + upstreamDis) {
                        // upstream 	11 	variant overlaps 1-kb region downtream of transcription end site (use -neargene to change this)
                        return new GeneFeature(VarFeatureIDMap.get("upstream"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":upstream-" + (pos - end));
                    } else {
                        //intergenic
                        return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
                    }
                } else if (exonIndex == 0) {
                    //the  leftside boundary is exclusive
                    if (pos <= start - downstreamDis) {
                        //intergenic
                        return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
                    } else if (pos <= start) {
                        // upstream 	12 	variant overlaps 1-kb region upstream of transcription start site
                        return new GeneFeature(VarFeatureIDMap.get("downstream"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":downstream+" + (start - pos));
                    } else if (noCodingExon) {
                        return new GeneFeature(VarFeatureIDMap.get("ncRNA"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                    } else if (pos <= codingStart) {
                        return new GeneFeature(VarFeatureIDMap.get("3UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR+" + (codingStart - pos));
                    } else if (pos <= codingEnd) {
                        //it must in the coiding region
                        // exonic    
                        relativeCodingStartPos = codingEnd - pos;
                        if (exonNum > 1) {
                            for (int i = exonIndex; i < codingStartSiteExonID; i++) {
                                relativeCodingStartPos -= intronLength.getQuick(i);
                            }
                        }

                        //special coding for the exonic variantsl it will be parsed later on
                        return new GeneFeature(VarFeatureIDMap.get("exonic"), exonNum + ":" + relativeCodingStartPos);

                    } else {
                        return new GeneFeature(VarFeatureIDMap.get("5UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR-" + (pos - codingEnd));
                    }
                } else //the index must be between 1 and exonIndex-1
                 if (noCodingExon) {
                        return new GeneFeature(VarFeatureIDMap.get("ncRNA"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                    } else if (pos <= exons.get(exonIndex - 1).end + splicingDis) {
                        //the 5' and 3' are relative to the closet exon
                        return new GeneFeature(VarFeatureIDMap.get("splicing"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5splicing" + (exonNum - exonIndex + 1) + "-" + (pos - exons.get(exonIndex - 1).end));
                    } else if (pos <= exons.get(exonIndex).start - splicingDis) {
                        // intronic 	4 	variant overlaps an intron 
                        return new GeneFeature(VarFeatureIDMap.get("intronic"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":intronic" + (exonNum - exonIndex));
                    } else if (pos <= exons.get(exonIndex).start) {
                        //the 5' and 3' are relative to the closet exon
                        return new GeneFeature(VarFeatureIDMap.get("splicing"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3splicing" + (exonNum - exonIndex) + "+" + (exons.get(exonIndex).start - pos));
                    } else if (pos <= codingStart) {
                        return new GeneFeature(VarFeatureIDMap.get("3UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR+" + (codingStart - pos));
                    } else if (pos <= codingEnd) {
                        //it must in the coiding region
                        // exonic   
                        //probably because codingEnd is exclusive, no -1 needed as in the forward strand

                        relativeCodingStartPos = codingEnd - pos;
                        for (int i = exonIndex; i < codingStartSiteExonID; i++) {
                            relativeCodingStartPos -= intronLength.getQuick(i);
                        }
                        // exonic  
                        //very important: usaully what we have in sample are alleles in forward strand
                        //but in the database RefmRNA on reverse strand will be on reverse strand before translated to amino accid

                        //special coding for the exonic variantsl it will be parsed later on
                        return new GeneFeature(VarFeatureIDMap.get("exonic"), (exonNum - exonIndex) + ":" + relativeCodingStartPos);

                    } else {
                        return new GeneFeature(VarFeatureIDMap.get("5UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR-" + (pos - codingEnd));
                    }
            } else //just at the  rightside boundary which is inclusive for reverse strand mRAN
             if (noCodingExon) {
                    return new GeneFeature(VarFeatureIDMap.get("ncRNA"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                } else if (pos <= codingStart) {
                    return new GeneFeature(VarFeatureIDMap.get("3UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR+" + (codingStart - pos));
                } else if (pos <= codingEnd) {
                    //it must in the coiding region
                    // exonic   
                    //probably because codingEnd is exclusive, no -1 needed as in the forward strand
                    relativeCodingStartPos = codingEnd - pos;
                    for (int i = exonIndex; i < codingStartSiteExonID; i++) {
                        relativeCodingStartPos -= intronLength.getQuick(i);
                    }
                    // exonic  
                    //very important: usaully what we have in sample are alleles in forward strand
                    //but in the database RefmRNA on reverse strand will be on reverse strand before translated to amino accid 

                    //special coding for the exonic variantsl it will be parsed later on
                    return new GeneFeature(VarFeatureIDMap.get("exonic"), (exonNum - exonIndex) + ":" + relativeCodingStartPos);
                } else {
                    return new GeneFeature(VarFeatureIDMap.get("5UTR"), refID + "(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR-" + (pos - codingEnd));
                }
        } else {
            //unrecognzed strand infor
            return new GeneFeature(VarFeatureIDMap.get("unknown"), "unknown");
        }
    }

    public GeneFeature findCrudeFeatureCustomGene(int pos) throws Exception {
        int relativeCodingStartPos = -1;
        int exonIndex = binarySearch(pos, 0, exonNum - 1);

        //  System.out.println(pos);
        //note in the refgene database
        //the leftside boundaries of exon region are inclusive and rightside boundaries are exclusive 
        if (exonIndex < 0) {
            exonIndex = -exonIndex - 1;
            if (exonIndex == exonNum) {
                //after all exons                   
                if (pos > end) {
                    //intergenic
                    return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
                } else {
                    //assume all regions are exominic
                    return new GeneFeature(VarFeatureIDMap.get("exonic"), refID);
                }
            } else if (exonIndex == 0) {
                if (pos < start) {
                    //intergenic
                    return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
                } else if (pos < codingEnd) {
                    //assume all regions are exominic
                    return new GeneFeature(VarFeatureIDMap.get("exonic"), refID);
                }
            } else //  the index must be between 1 and exonIndex-1
             if (pos < start) {
                    return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
                } else if (pos < exons.get(exonIndex).start) {
                    return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
                } else if (pos < end) {
                    //assume all regions are exominic
                    return new GeneFeature(VarFeatureIDMap.get("exonic"), refID);
                } else {
                    //
                    return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
                }
        } else //just at the  rightside boundary which is exclusive
        {
            if (pos < exons.get(exonIndex).end) {
                //assume all regions are exominic
                return new GeneFeature(VarFeatureIDMap.get("exonic"), refID);
            } else {
                return new GeneFeature(VarFeatureIDMap.get("intergenic"), "intergenic");
            }
        }
        return null;
    }

//we know that the exons are not overlapped and sorted . otherwise it is risk
    private int binarySearch(int pos, int left, int right) {
        if (left > right) {
            return -left - 1;
        }
        int middle = (left + right) / 2;

        if (exons.get(middle).end == pos) {
            return middle;
        } else if (exons.get(middle).end > pos) {
            return binarySearch(pos, left, middle - 1);
        } else {
            return binarySearch(pos, middle + 1, right);
        }
    }
}
