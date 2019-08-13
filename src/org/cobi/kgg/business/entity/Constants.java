/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author MXLi
 */
public interface Constants {

    public static final String PDATE = "July 1, 2019";
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final String KGG_VERSION = "4; Java/" + JAVA_VERSION;
    public static final String AUTHOR_STRING = "Miao-Xin Li; limx54@yahoo.com";
    public static final String KGG_WEBSITE_STRING = "http://grass.cgs.hku.hk/limx/kgg/";
    public static final String KGG_CITATION_STRING = "";
    public static final String OPEN_LINKAGE_PEDFILE = "Load Linkage Pedigree File";
    public static final String OPEN_CANDIDATE_GENE_PEDFILE = "Load candidate Gene File";
    public static final String FILE_OPEN_APPROVED = "File Open Approved";
    static final String FILE_OPEN_CANCELLED = "File Open Cancelled";
    static String ICON_PATH_16 = "/org/cobi/kgg/ui/png/16x16/";

    static final String KGG_RESOURCE_URL = "http://grass.cgs.hku.hk/limx/kgg/download/";
    static final double MISSING_DOUBLE = -99.999;
    public static final int GENOME_GENE_NUM = 20102;
    String[] CHROM_NAMES = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
        "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "XY", "M", "Unkown"};
    /*
     String[] resourceFiles = {"gene.zip", "mergedRSID.zip", "1.d.gz", "2.d.gz", "3.d.gz", "4.d.gz", "5.d.gz",
     "6.d.gz", "7.d.gz", "8.d.gz", "9.d.gz", "10.d.gz", "11.d.gz", "12.d.gz",
     "13.d.gz", "14.d.gz", "15.d.gz", "16.d.gz", "17.d.gz", "18.d.gz",
     "19.d.gz", "20.d.gz", "21.d.gz", "22.d.gz", "X.d.gz", "Y.d.gz", "MT.d.gz"
     };
     */

    String[] resourceFiles = {"PPIGeneAttribute.txt.gz", "TissueSpecificGenes.txt.gz", "OMIMGenes.txt.gz", "STRINGPPIV905.txt.gz", "c2.all.v4.0.symbols.gmt.gz", "c2.cp.v4.0.symbols.gmt.gz", "c4.all.v4.0.symbols.gmt.gz", "c6.all.v4.0.symbols.gmt.gz", "hg19_gencode.txt.gz","hg38_gencode.txt.gz"};
    /// String[] resourceFiles = {"gene.zip", "mergedRSID.zip"};
    static int FILE_SEGEMENT_NUM = 5;
    static int MAX_THREAD_NUM = 1;
    static String URL_FOLDER = KGG_RESOURCE_URL;
    //static String[] LOCAL_FILE_PATHES = {"KGG.jar", "UserManual.pdf", "lib/ant.jar", "lib/jcommon-1.0.16.jar", "lib/jfreechart-1.0.13.jar"};
    //"UserManual.pdf", "lib/ant.jar", "lib/jcommon-1.0.16.jar", "lib/jfreechart-1.0.13.jar"
  // static String[] URL_FILE_PATHES = {"kgg3/org-cobi-kgg.jar"};
    //download setting
    public static int MAX_TOTAL_CONNECTIONS = 1000;
    public static int MAX_PER_ROUTE_CONNECTIONS = 50;
    // "splice-5", "splice-3",
    String[] geneFeatureNames = {"frameshift", "nonframeshift", "stop-gain", "stop-loss", "missense",
        "splice-site", "coding-synonymous", "exonic", "utr-5", "utr-3", "intron", "near-gene-5", "near-gene-3", "ncRNA", "-", "unknown"};
    String[] geneGroups = {"protein-coding gene", "phenotype", "pseudogene", "non-coding RNA", "other","unknown"};
    public static String[] VAR_FEATURE_NAMES = new String[]{"frameshift", "nonframeshift", "stoploss",
        "stopgain", "missense", "splicing", "synonymous", "exonic", "5UTR", "3UTR", "intronic", "upstream", "downstream", "ncRNA", "intergenic", "unknown"};
    Map<String, Integer> PValueTestID = new HashMap<String, Integer>() {
        {
            put("Benjamini & Hochberg (1995)", 0);
            put("Standard Bonferroni", 1);
            //Adaptive Benjamini (2006) has been replaced with this
            put("Benjamini & Yekutieli (2001)", 2);
            put("Storey (2002)", 3);
            put("Hypergeometric Test", 4);
            put("Fisher Combination Test", 5);
            put("Simes Combination Test", 6);
            put("Fixed p-value threshold", 7);
        }
    };
    Map<String, Byte> VarFeatureIDMap = new HashMap<String, Byte>() {
        {
            byte i = 0;
            for (String feature : VAR_FEATURE_NAMES) {
                put(feature, i++);
            }

        }
    };
    public static final String HAPMAP_LD_URL = "http://hapmap.ncbi.nlm.nih.gov/downloads/ld_data/latest/";
    String[] HAPMAP_POPS = {"CHB", "YRI", "CEU", "JPT", "ASW", "CHD", "GIH", "LWK", "MEX", "MKK", "TSI"};
    //for CEU sample
    double[] propposedWeightsCEU = {1.037342587, 1.235082032, 1.415856361, 0.931913006, 1.339383505, 1.180190476, 1.253169546, 1.478160541, 1.234853168, 0.780377559, 0.883468835, 0.883468835, 0.883468835, 0.883468835};
    //for CHBJPT sample
    double[] propposedWeightsCHBJPT = {1.0235, 1.132, 1.336277574, 0.949093659, 1.291887826, 1.247293366, 1.196222939, 1.142280774, 1.220864589, 0.790195558, 0.935483871, 0.935483871, 0.935483871, 0.935483871};
    //for YRI sample
    double[] propposedWeightsYRI = {1.148184537, 1.263203091, 1.417477486, 0.912263124, 1.358629867, 1.023741282, 1.432304351, 1.085521442, 1.557138433, 1.165017668, 0.959302326, 0.959302326, 0.959302326, 0.959302326};

    //0 Benjamini & Hochberg (1995)
    //1 Standard Bonferroni
    //2 Benjamini & Yekutieli (2001)
    //3 Storey (2002)
    //4 Hypergeometric Test
    //5 Fisher Combination Test
    //6 Simes Combination Test
    //7 Fixed p-value threshold
    enum PValueTestCode {

        BenjaminiHochberg1995(0), StandardBonferroni(1), AdaptiveBenjamini2006(2), Storey2002(3),
        HypergeometricTest(4), FisherCombinationTest(5), SimesCombinationTest(6), FixedPValueThreshold(7);
        private int code;

        PValueTestCode(int codeID) {
            this.code = codeID;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public int getIndex() {
            return ordinal();
        }
    }
    public static char MISSING_ALLELE_NAME = 'X';
}
