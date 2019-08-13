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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mxli
 */
public class Chromosome implements Serializable {

    private static final long serialVersionUID = 3L;
    /** @pdOid ec29211f-ff0a-4f2d-bbd8-585cb5094b1b */
    private int id;
    /** @pdOid 2d5ae4e3-9217-4284-9d2e-2015123075fe */
    private String name;
    /** @pdRoleInfo migr=no name=Gene assc=association8 mult=0..* type=Aggregation */
    public List<Gene> genes;
    /** @pdRoleInfo migr=no name=SNP assc=association9 mult=0..* type=Aggregation */
    public List<SNP> snpsOutGenes;

    public int getId() {
        return id;
    }

    public Chromosome() {
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @param id
     * @param name
     */
    public Chromosome(int id, String name) {
        this.id = id;
        this.name = name;
        genes = new ArrayList<Gene>();
        snpsOutGenes = new ArrayList<SNP>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param gene
     */
    public void addGene(Gene gene) {
        genes.add(gene);
    }

    /**
     *
     * @param snp
     */
    public void addSNP(SNP snp) {
        snpsOutGenes.add(snp);
    }
}
