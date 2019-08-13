/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.cobi.kgg.business.entity.FileString;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.PPIBasedAssociation;
import org.cobi.kgg.business.entity.PathwayBasedAssociation;
import org.cobi.kgg.business.entity.Project;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author mxli
 */
public class ProjectNodeChildren extends Children.Keys<GeneralNodeEntity> implements ChangeListener {

    private Project project;
    String prjWorkingPath;
    String prjName;
    // private final List<PValueFileList> tmpPValueFileList = new ArrayList<PValueFileList>();
    //private final List<CandiGeneFileList> tmpCandiGeneFileList = new ArrayList<CandiGeneFileList>();
    private final List<GeneralNodeEntity> tmpList = new ArrayList<GeneralNodeEntity>();

    public ProjectNodeChildren(Project project) {
        this.project = project;
        prjWorkingPath = project.getWorkingPath();
        prjName = project.getName();
    }

    @Override
    protected void addNotify() {
        // tmpPValueFileList.add(project.getpValueFileList());
        //tmpCandiGeneFileList.add(project.getCandiGeneFileList());
        tmpList.add(new GeneralNodeEntity("P-value Files", project.getpValueFileList()));
        tmpList.add(new GeneralNodeEntity("Candi. Gene Files", project.getCandiGeneFileList()));

        if (project.genomeSet != null) {
            tmpList.add(new GeneralNodeEntity("Genome Set", project.genomeSet));
        }
        if (project.geneScans != null) {
            tmpList.add(new GeneralNodeEntity("Gene Scan", project.geneScans));
        }
        if (project.ppiScans != null) {
            tmpList.add(new GeneralNodeEntity("Gene-pair Scan", project.ppiScans));
        }
        if (project.pathwayScans != null) {
            tmpList.add(new GeneralNodeEntity("Pathway/GeneSet Scan", project.pathwayScans));
        }

        setKeys(tmpList);
        // setKeys(project.getCandiGeneFiles());
    }

    @Override
    protected Node[] createNodes(GeneralNodeEntity key) {
        if (key.typeName.equals("P-value Files")) {
            List<File> files = (List<File>) key.value;
            if (files != null && !files.isEmpty()) {
                List<GeneralLeaf> leafs = new ArrayList<GeneralLeaf>();
                for (File file : files) {
                    GeneralLeaf gl = new GeneralLeaf(0, null, file);
                    leafs.add(gl);
                }
                return new Node[]{new GeneralNode("P-value Files", "", leafs)};
            }
        } else if (key.typeName.equals("Candi. Gene Files")) {
            List<File> files = (List<File>) key.value;
            if (files != null && !files.isEmpty()) {
                List<GeneralLeaf> leafs = new ArrayList<GeneralLeaf>();
                for (File file : files) {
                    //category id 0: String 1: File
                    GeneralLeaf gl = new GeneralLeaf(1, null, file);
                    leafs.add(gl);
                }
                return new Node[]{new GeneralNode("Candi. Gene Files", "", leafs)};
            }

        } else if (key.typeName.equals("Genome Set")) {
            List<Genome> genomeList = (List<Genome>) key.value;
            if (!genomeList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                Node[] genomeNodes = new Node[genomeList.size()];
                int i = 0;

                for (Genome genme : genomeList) {
                    List<GeneralLeaf> leafs = new ArrayList<GeneralLeaf>();
                    //category id 0: String 1: File
                    leafs.add(new GeneralLeaf(2, "Source@%@" + genme.getpValueSource().getName()));
                    leafs.add(new GeneralLeaf(2, "Version@%@" + genme.getFinalBuildGenomeVersion()));
                    sb.delete(0, sb.length());
                    String[] pNames = genme.getpValueNames();
                    if (pNames != null) {
                        for (String str : pNames) {
                            sb.append(str);
                            sb.append(',');
                        }
                    }
                    if (genme.getGeneDB() != null && !genme.getGeneDB().trim().isEmpty()) {
                        File f = new File(genme.getGeneDB());
                        if (f.exists()) {
                            leafs.add(new GeneralLeaf(2, "Gene database@%@" + f.getName()));
                        } else {
                            leafs.add(new GeneralLeaf(2, "Gene database@%@" + genme.getGeneDB()));
                        }
                    }
                    if (genme.getGeneVarMapFile() != null && !genme.getGeneVarMapFile().trim().isEmpty()) {
                        File f = new File(genme.getGeneVarMapFile());
                        leafs.add(new GeneralLeaf(2, "Gene-Var map file@%@" + f.getName()));
                    }
                    leafs.add(new GeneralLeaf(2, "Used columns@%@[" + sb.substring(0, sb.length() - 1) + "]"));
                    leafs.add(new GeneralLeaf(2, "Gene 5' extension@%@" + genme.getExtendedGene5PLen()));
                    leafs.add(new GeneralLeaf(2, "Gene 3' extension@%@" + genme.getExtendedGene3PLen()));
                    //ld source code
                    //-2 others LD
                    //-1 no LD information
                    //0 genotype plink binary file
                    //1 hapap ld
                    //2 1kG haplomap
                    //3 local LD calcualted by plink
                    //4 1kG haplomap vcf format
                    if (genme.getLdSourceCode() == -2) {
                        leafs.add(new GeneralLeaf(2, "SNP LD@%@Same as \'" + genme.getSameLDGenome() + "\'"));
                    } else if (genme.getLdSourceCode() == -1) {
                        leafs.add(new GeneralLeaf(2, "SNP LD@%@No"));
                    } else if (genme.getLdSourceCode() == 0) {
                        leafs.add(new GeneralLeaf(2, "SNP LD@%@" + genme.getPlinkSet().toString()));
                    } else if (genme.getLdSourceCode() == 1) {
                        leafs.add(new GeneralLeaf(2, "SNP LD@%@Hapmap"));
                    } else if (genme.getLdSourceCode() == 2) {
                        leafs.add(new GeneralLeaf(2, "SNP LD@%@1KG Haplotypes"));
                    } else if (genme.getLdSourceCode() == 4) {
                        leafs.add(new GeneralLeaf(2, "SNP LD@%@1KG Haplotypes VCF"));
                    }
                    leafs.add(new GeneralLeaf(2, "Adjust P Value@%@" + (genme.isToAdjustPValue() ? "Yes" : "No")));

                    File filePath = new File(prjWorkingPath + File.separator + prjName + File.separator
                            + genme.getName() + ".html");

                    genomeNodes[i] = new GeneralNode("Genome", genme.getName(), leafs, filePath);
                    i++;
                }
                return genomeNodes;
            }
        } else if (key.typeName.equals("Gene Scan")) {
            List<GeneBasedAssociation> scanList = (List<GeneBasedAssociation>) key.value;
            if (!scanList.isEmpty()) {
                Node[] genomeNodes = new Node[scanList.size()];
                int i = 0;
                for (GeneBasedAssociation genme : scanList) {
                    if (genme.getGenome() == null) {
                        continue;
                    }
                    List<GeneralLeaf> leafs = new ArrayList<GeneralLeaf>();
                    leafs.add(new GeneralLeaf(3, "Genome@%@" + genme.getGenome().getName()));
                    leafs.add(new GeneralLeaf(3, "Multivariate Test@%@" + (genme.isMultVariateTest() ? "Yes" : "No")));
                    leafs.add(new GeneralLeaf(3, "P value sources@%@" + genme.getPValueSources().toString()));
                    leafs.add(new GeneralLeaf(3, "Test@%@" + genme.getTestedMethod()));
                    leafs.add(new GeneralLeaf(3, "Show@%@" + "Detailed Results", genme));
                    File filePath = new File(prjWorkingPath + File.separator + prjName + File.separator
                            + genme.getName() + ".html");

                    genomeNodes[i] = new GeneralNode("Gene Scan", genme.getName(), leafs, filePath);
                    i++;
                }
                return genomeNodes;
            }
        } else if (key.typeName.equals("Gene-pair Scan")) {
            List<PPIBasedAssociation> scanList = (List<PPIBasedAssociation>) key.value;
            if (!scanList.isEmpty()) {
                Node[] genomeNodes = new Node[scanList.size()];
                int i = 0;
                for (PPIBasedAssociation genme : scanList) {
                    List<GeneralLeaf> leafs = new ArrayList<GeneralLeaf>();
                    leafs.add(new GeneralLeaf(4, "Gene scan@%@" + genme.getGeneScan().getName()));
                    leafs.add(new GeneralLeaf(4, "P value sources@%@" + genme.getPValueSources().toString()));
                    leafs.add(new GeneralLeaf(4, "Test@%@" + genme.getPpIAssocTestedMethod()));
                    FileString[] files = genme.getPpIDBFiles();
                    for (File file : files) {
                        leafs.add(new GeneralLeaf(4, "Gene-pair@%@" + file.getName()));
                    }

                    leafs.add(new GeneralLeaf(4, "Confidence@%@" + genme.getCofidenceScoreThreshold()));
                    if (genme.getCanidateGeneSetFile() != null) {
                        leafs.add(new GeneralLeaf(4, "Candidate Gene@%@" + genme.getCanidateGeneSetFile().getName()));
                    }
                    leafs.add(new GeneralLeaf(4, "Show@%@" + "Detailed Results", genme));
                    File filePath = new File(prjWorkingPath + File.separator + prjName + File.separator
                            + genme.getName() + ".html");

                    genomeNodes[i] = new GeneralNode("Gene-pair Scan", genme.getName(), leafs, filePath);
                    i++;
                }
                return genomeNodes;
            }
        } else if (key.typeName.equals("Pathway/GeneSet Scan")) {
            List<PathwayBasedAssociation> scanList = (List<PathwayBasedAssociation>) key.value;
            if (!scanList.isEmpty()) {
                Node[] genomeNodes = new Node[scanList.size()];
                int i = 0;
                for (PathwayBasedAssociation genme : scanList) {
                    List<GeneralLeaf> leafs = new ArrayList<GeneralLeaf>();
                    leafs.add(new GeneralLeaf(5, "Gene scan@%@" + genme.getGeneScan().getName()));
                    leafs.add(new GeneralLeaf(5, "P value sources@%@" + genme.getpValueSources().toString()));
                    leafs.add(new GeneralLeaf(5, "Test@%@" + genme.getPathwayAssocTestedMethod()));
                    leafs.add(new GeneralLeaf(5, "Pathway DB@%@" + genme.getPathwayDBFile().getName()));
                    if (genme.getCanidateGeneSetFile() != null) {
                        leafs.add(new GeneralLeaf(5, "Candidate Gene@%@" + genme.getCanidateGeneSetFile().getName()));
                    }
                    leafs.add(new GeneralLeaf(5, "Show@%@" + "Detailed Results", genme));
                    File filePath = new File(prjWorkingPath + File.separator + prjName + File.separator
                            + genme.getName() + ".html");

                    genomeNodes[i] = new GeneralNode("Pathway/GeneSet Scan", genme.getName(), leafs, filePath);
                    i++;
                }
                return genomeNodes;
            }
        }

        return null;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        setKeys(tmpList);
    }
}
