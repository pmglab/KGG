/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import cern.colt.list.DoubleArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.cobi.kgg.business.entity.Chromosome;
import org.cobi.kgg.business.entity.Constants;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.GenePValueComparator;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.PValueGene;
import org.cobi.kgg.business.entity.PlinkDataset;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.text.Util;

/**
 *
 * @author mxli
 */
public class GeneAssociationLDExtractor implements Constants {

    private final static Logger LOG = Logger.getLogger(GeneAssociationLDExtractor.class.getName());
    GeneBasedAssociation geneScan;
    HashSet<String> selectedGeneExport;
    int multipleTestingMethodID = 0;
    double familywiseErrorRate = 0.05;
    

    public GeneBasedAssociation getGeneScan() {
        return geneScan;
    }

    public void setGeneScan(GeneBasedAssociation geneScan) {
        this.geneScan = geneScan;
    }

    public HashSet<String> getSelectedGeneExport() {
        return selectedGeneExport;
    }

    public void setSelectedGeneExport(HashSet<String> selectedGeneExport) {
        this.selectedGeneExport = selectedGeneExport;
    }

    public int getGeneExpandBound5p() throws Exception {
        return (int) (geneScan.getGenome().getExtendedGene5PLen() * 1000);

    }

    public int getGeneExpandBound3p() throws Exception {
        return (int) (geneScan.getGenome().getExtendedGene3PLen() * 1000);

    }
    /**
     *
     * @param geneList
     * @param adjustedPValue
     * @param addedName
     * @return
     * @throws Exception
     */
    public List[] extractGeneSNPInfor() throws Exception {
       //List<PValueGene> geneList = geneScan.loadGenePValuesfromDisk();
        List<PValueGene> geneList=null;
       if (geneScan.isMultVariateTest()) {
           geneList = geneScan.loadGenePValuesfromDisk("MulVar",null);
       } else {
           geneList = geneScan.loadGenePValuesfromDisk(geneScan.getPValueSources().get(0).toString(),null);
        }
      
        StringBuilder logInfo = new StringBuilder();
        Collections.sort(geneList, new GenePValueComparator());
        Genome genome = geneScan.getGenome();
        int geneNum = geneList.size();

        Map<String, int[]> geneGenomeIndexes = genome.loadGeneGenomeIndexes2Buf();

        DoubleArrayList tempSNPPValues = new DoubleArrayList();
        //map genes on chromsomes so that each time only load one chromosome into memory
        Map<String, Double> genePValueMap = new HashMap<String, Double>();
        for (int i = 0; i < geneNum; i++) {
            PValueGene pValueGene = geneList.get(i);
            genePValueMap.put(pValueGene.getSymbol(), pValueGene.pValue);
            tempSNPPValues.add(pValueGene.pValue);
        }

        List[] geneSNPList = new List[CHROM_NAMES.length];
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            geneSNPList[i] = new ArrayList<Gene>();
        }
        List[] chromGenes = new List[CHROM_NAMES.length];
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            chromGenes[i] = new ArrayList<String>();
        }

        StringBuilder missedGenes = new StringBuilder();
        for (String gensmb : selectedGeneExport) {
            int[] indexes = geneGenomeIndexes.get(gensmb);
            if (indexes != null) {
                chromGenes[indexes[0]].add(gensmb);
            } else {
                missedGenes.append(" ");
                missedGenes.append(gensmb);
            }
        }

        if (missedGenes.length() > 0) {
            missedGenes.insert(0, "Genes do not exist in the current GWAS dataset:");
            missedGenes.append("\n\n");
            logInfo.insert(0, missedGenes);
        }
        //read data and export chromosome by chromosome
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            if (chromGenes[i].isEmpty()) {
                continue;
            }
            Chromosome chrom = genome.readChromosomefromDisk(i);
            List<String> curChromGenes = chromGenes[i];
            for (String gensmb : curChromGenes) {
                int[] indexes = geneGenomeIndexes.get(gensmb);
                Gene gene = chrom.genes.get(indexes[1]);
                gene.setOfficialName(Util.formatPValue(genePValueMap.get(gensmb)));
                geneSNPList[i].add(gene);
            }
            chrom = null;
            chromGenes[i].clear();
        }
        geneGenomeIndexes = null;
        genome.releaseMemory();


        // Unified Test Code
        // 0 Benjamini & Hochberg (1995)
        // 1 Standard Bonferroni
        // 2 Benjamini & Yekutieli (2001)
        // 3 Storey (2002)
        // 4 Hypergeometric Test
        // 5 Fisher Combination Test
        // 6 Simes Combination Test
        // 7 Fixed p-value threshold
        double adjustedPValue = 1;
        if (multipleTestingMethodID != 7) {
            switch (multipleTestingMethodID) {
                case 0:
                    // Benjamini & Hochberg, 1995 test
                    adjustedPValue = MultipleTestingMethod.BenjaminiHochbergFDR("Gene",familywiseErrorRate, tempSNPPValues);
                    logInfo.append("The significance level for Benjamini & Hochberg (1995) FDR test to control error rate ");
                    break;
                case 1:
                    adjustedPValue = familywiseErrorRate / tempSNPPValues.size();
                    logInfo.append("The significance level for Bonferroni correction  to control familywise error rate ");
                    logInfo.append(familywiseErrorRate);
                    logInfo.append(" in genome-wide gene set is ");
                    logInfo.append(Util.formatPValue(adjustedPValue));
                    logInfo.append("=(");
                    logInfo.append(familywiseErrorRate);
                    logInfo.append("/");
                    logInfo.append(tempSNPPValues.size());
                    logInfo.append(") .\n");
                    break;
                case 2:
                    // Adaptive Benjamini 2006 test
                    adjustedPValue = MultipleTestingMethod.BenjaminiYekutieliFDR("Gene",familywiseErrorRate, tempSNPPValues);
                    logInfo.append("The significance level for Addptive Benjamini (2006) FDR test to control error rate ");
                    break;
                case 3:
                    // Storey, 2002 test
                    adjustedPValue = MultipleTestingMethod.storeyFDRTest("Gene",familywiseErrorRate, tempSNPPValues);
                    logInfo.append("The significance level for Storey (2002) FDR test to control error rate ");
                    break;
                default:
                    break;
            }
            tempSNPPValues.clear();
        }
        if (multipleTestingMethodID != 1) {
            logInfo.append(familywiseErrorRate);
            logInfo.append(" in genome-wide gene set is ");
            logInfo.append(Util.formatPValue(adjustedPValue));
            logInfo.append(".\n");
        }
        //GlobalManager.mainView.insertBriefRunningInfor(logInfo.toString(), true);
        LOG.info(logInfo.toString());
        //GlobalManager.addInforLog(logInfo.toString());
        return geneSNPList;
    }
}
