/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.cobi.kgg.business.entity.Chromosome;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.PValueGene;
import org.cobi.kgg.business.entity.Pathway;
import org.cobi.kgg.business.entity.PathwayBasedAssociation;
import org.cobi.kgg.ui.ArrayListObjectArrayTableModel;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.action.BuildGenome;
import org.cobi.kgg.ui.action.ScanPathwayBasedAssociation;
import org.cobi.util.text.ObjectArrayIntComparator;
import org.cobi.util.text.Util;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.cobi.kgg.ui.dialog//ConditionalGeneSetAssoc//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ConditionalGeneSetAssocTopComponent",
        iconBase = "org/cobi/kgg/ui/png/16x16/Top.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.cobi.kgg.ui.dialog.ConditionalGeneSetAssocTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ConditionalGeneSetAssocAction",
        preferredID = "ConditionalGeneSetAssocTopComponent"
)
@Messages({
    "CTL_ConditionalGeneSetAssocAction=Overlap-free Geneset Association",
    "CTL_ConditionalGeneSetAssocTopComponent=Overlap-free geneset association Window",
    "HINT_ConditionalGeneSetAssocTopComponent=This is an overlap-free geneset association window"
})
public final class ConditionalGeneSetAssocTopComponent extends TopComponent {

    private final static RequestProcessor RP = new RequestProcessor("Overlap-free geneset association", 1, true);
    private RequestProcessor.Task buildTask = null;

    private List<Object[]> listPValueTableData = null;
    private ArrayListObjectArrayTableModel listPValueTableModel = null;
    DefaultComboBoxModel<String> pVlaueSoureModel = new DefaultComboBoxModel<String>();
    private ObjectArrayIntComparator oac = new ObjectArrayIntComparator(3);

    Map<String, Double> genePMap = null;
    Map<String, int[]> geneIndexMaps = null;
    int currChromIndex = -1;
    Chromosome currChrom = null;
    Genome currGenome = null;
    int effectiveSNPPIndex = -1;
    PathwayBasedAssociation pathwayGeneSet;
    Map<String, Pathway> pathwayIDMap = new HashMap<String, Pathway>();
    Map<String, PValueGene> genePValueMap = new HashMap<String, PValueGene>();
    String sourcePValueName = null;

    public ConditionalGeneSetAssocTopComponent() {
        listPValueTableData = new ArrayList<Object[]>();
        listPValueTableModel = new ArrayListObjectArrayTableModel();

        String[] pValueTitles = new String[6];
        pValueTitles[0] = "GeneSet";
        pValueTitles[1] = "OriginalP";
        pValueTitles[2] = "#Gene";
        pValueTitles[3] = "Order";
        pValueTitles[4] = "Select";
        pValueTitles[5] = "OverlapfreeP";
        listPValueTableModel.setTitle(pValueTitles);
        initComponents();
        setName(Bundle.CTL_ConditionalGeneSetAssocTopComponent());
        setToolTipText(Bundle.HINT_ConditionalGeneSetAssocTopComponent());
    }

    class IntDoubleUnit {

        protected int index;
        protected double value;

        public IntDoubleUnit(int index, double value) {
            this.index = index;
            this.value = value;
        }

    }

    class IntDoubleUnitComparator implements Comparator<IntDoubleUnit> {

        public IntDoubleUnitComparator() {

        }

        @Override
        public int compare(IntDoubleUnit o1, IntDoubleUnit o2) {
            return Double.compare(o1.value, o2.value);
        }
    }

    class IntIntUnit {

        protected int index;
        protected int index1;

        public IntIntUnit(int index, int index1) {
            this.index = index;
            this.index1 = index1;
        }

    }

    class IntIntUnitComparator implements Comparator<IntIntUnit> {

        @Override
        public int compare(IntIntUnit o1, IntIntUnit o2) {
            return Integer.compare(o1.index1, o2.index1);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        pathwaySearchTextArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        associationScanSetComboBox = new javax.swing.JComboBox();
        retrieveGeneButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        geneListTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        condtionalAnalysisButton = new javax.swing.JButton();

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConditionalGeneSetAssocTopComponent.class, "ConditionalGeneSetAssocTopComponent.jScrollPane1.border.title"))); // NOI18N

        pathwaySearchTextArea.setColumns(20);
        pathwaySearchTextArea.setRows(5);
        pathwaySearchTextArea.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionalGeneSetAssocTopComponent.class, "ConditionalGeneSetAssocTopComponent.pathwaySearchTextArea.toolTipText")); // NOI18N
        jScrollPane1.setViewportView(pathwaySearchTextArea);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ConditionalGeneSetAssocTopComponent.class, "ConditionalGeneSetAssocTopComponent.jLabel1.text")); // NOI18N

        associationScanSetComboBox.setModel(GlobalManager.pathwayAssocSetModel);
        associationScanSetComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                associationScanSetComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(retrieveGeneButton, org.openide.util.NbBundle.getMessage(ConditionalGeneSetAssocTopComponent.class, "ConditionalGeneSetAssocTopComponent.retrieveGeneButton.text")); // NOI18N
        retrieveGeneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retrieveGeneButtonActionPerformed(evt);
            }
        });

        geneListTable.setModel(listPValueTableModel);
        jScrollPane2.setViewportView(geneListTable);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(ConditionalGeneSetAssocTopComponent.class, "ConditionalGeneSetAssocTopComponent.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(ConditionalGeneSetAssocTopComponent.class, "ConditionalGeneSetAssocTopComponent.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(ConditionalGeneSetAssocTopComponent.class, "ConditionalGeneSetAssocTopComponent.jButton4.text")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        condtionalAnalysisButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        condtionalAnalysisButton.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(condtionalAnalysisButton, org.openide.util.NbBundle.getMessage(ConditionalGeneSetAssocTopComponent.class, "ConditionalGeneSetAssocTopComponent.condtionalAnalysisButton.text")); // NOI18N
        condtionalAnalysisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                condtionalAnalysisButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(condtionalAnalysisButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(condtionalAnalysisButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(associationScanSetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(retrieveGeneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(associationScanSetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(retrieveGeneButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 160, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void associationScanSetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_associationScanSetComboBoxActionPerformed

    }//GEN-LAST:event_associationScanSetComboBoxActionPerformed

    private void retrieveGeneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retrieveGeneButtonActionPerformed
        try {
            listPValueTableData.clear();
            pathwayGeneSet = (PathwayBasedAssociation) associationScanSetComboBox.getSelectedItem();

            if (pathwayGeneSet == null) {
                return;
            }

            GeneBasedAssociation geneAss = pathwayGeneSet.getGeneScan();
            currGenome = geneAss.getGenome();

            boolean isMultP = geneAss.isMultVariateTest();
            String info = "This gene-based association is a multiple-phenotype analysis and is not supported for conditional gene-based analysis for the time being!";
            if (isMultP) {
                JOptionPane.showMessageDialog(this, info, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sourcePValueName = geneAss.getPValueSources().get(0);
            List<PValueGene> genePList = null;
            if (geneAss.isMultVariateTest()) {
                genePList = geneAss.loadGenePValuesfromDisk("MulVar", null);
            } else {
                genePList = geneAss.loadGenePValuesfromDisk(sourcePValueName, null);
            }
            String[] pValueNames = currGenome.getpValueNames();
            effectiveSNPPIndex = -1;
            for (int i = 0; i < pValueNames.length; i++) {
                if (pValueNames[i].equals(sourcePValueName)) {
                    effectiveSNPPIndex = i;
                    break;
                }
            }
            genePValueMap.clear();
            for (PValueGene pg : genePList) {
                genePValueMap.put(pg.getSymbol(), pg);
            }
            genePList.clear();
            List<Pathway> pathwayList = pathwayGeneSet.loadPathwayAssociation(sourcePValueName);
            pathwayIDMap.clear();
            for (Pathway pathway : pathwayList) {
                pathwayIDMap.put(pathway.getID(), pathway);
            }

            List<String> genesetStringList = new ArrayList<String>();
            String inutText = pathwaySearchTextArea.getText();

            StringTokenizer tokenizer = new StringTokenizer(inutText);
            while (tokenizer.hasMoreTokens()) {
                genesetStringList.add(tokenizer.nextToken());
            }
            if (genesetStringList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No gene symboles are available!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int ii = 0;

            for (String geneSetStr : genesetStringList) {
                Pathway pathway = pathwayIDMap.get(geneSetStr);
                Object[] cells = new Object[6];
                listPValueTableData.add(cells);
                cells[0] = geneSetStr;
                cells[1] = Util.formatPValue(pathway.getWilcoxonPValue());
                cells[2] = String.valueOf(pathway.getGeneSymbolWeightMap().size());
                cells[3] = String.valueOf(ii);
                cells[4] = true;
                cells[5] = "?";
                ii++;
            }

            List<IntDoubleUnit> pValueOrders = new ArrayList<IntDoubleUnit>();
            Collections.sort(listPValueTableData, oac);
            int index = 0;
            for (Object[] cells : listPValueTableData) {
                double p = Double.parseDouble(cells[1].toString());
                pValueOrders.add(new IntDoubleUnit(index, p));
                index++;
            }
            Collections.sort(pValueOrders, new IntDoubleUnitComparator());
            index = 0;
            for (index = 0; index < pValueOrders.size(); index++) {
                IntDoubleUnit pv = pValueOrders.get(index);
                listPValueTableData.get(pv.index)[3] = String.valueOf(index + 1);
            }
            listPValueTableModel.setDataList(listPValueTableData);
            listPValueTableModel.fireTableStructureChanged();

        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }//GEN-LAST:event_retrieveGeneButtonActionPerformed

    private void condtionalAnalysisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_condtionalAnalysisButtonActionPerformed
        ScanPathwayBasedConditionalAssocSwingWorker ss = new ScanPathwayBasedConditionalAssocSwingWorker();
        // buildTask = buildTask.create(buildingThread); //the task is not started yet
        buildTask = RP.create(ss); //the task is not started yet
        buildTask.schedule(0); //start the task
    }//GEN-LAST:event_condtionalAnalysisButtonActionPerformed

    class ScanPathwayBasedConditionalAssocSwingWorker extends SwingWorker<Void, String> {

        Map<String, Double> pathwayNewPMap = new HashMap<String, Double>();
        private final int NUM = 100;
        int runningThread = 0;
        boolean succeed = false;
        ProgressHandle ph = null;
        long time;

        public ScanPathwayBasedConditionalAssocSwingWorker() {
            ph = ProgressHandleFactory.createHandle("task thats shows progress", new Cancellable() {
                @Override
                public boolean cancel() {
                    // return handleCancel();
                    return false;
                }
            });

            time = System.nanoTime();
        }

        @Override
        protected Void doInBackground() {
            try {
                ScanPathwayBasedAssociation scanPathway = new ScanPathwayBasedAssociation(pathwayGeneSet);
                List< Pathway> selectedPathways = new ArrayList< Pathway>();
                List<IntIntUnit> settedOrders = new ArrayList<IntIntUnit>();

                int index = 0;
                for (Object[] cells : listPValueTableData) {
                    int index1 = Integer.parseInt(cells[3].toString());
                    settedOrders.add(new IntIntUnit(index, index1));
                    index++;
                }
                Collections.sort(settedOrders, new IntIntUnitComparator());

                for (IntIntUnit intunit : settedOrders) {
                    Object[] objs = listPValueTableData.get(intunit.index);
                    Boolean selected = (Boolean) objs[4];
                    if (selected) {
                        String pathID = (String) objs[0];
                        Pathway pathway = pathwayIDMap.get(pathID);
                        selectedPathways.add(pathway);
                    }
                }
                if (selectedPathways.isEmpty()) {
                    return null;
                }

                scanPathway.setBasedTestByWilcoxWhileRemovingRedundantGenes(genePValueMap, selectedPathways, pathwayNewPMap, sourcePValueName, pathwayGeneSet.getMinR2(), pathwayGeneSet.getMinGeneNumInPathway());

                succeed = true;
            } catch (InterruptedException ex) {
                StatusDisplayer.getDefault().setStatusText("Scan gene-pair-based association task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

            } catch (Exception ex) {
                StatusDisplayer.getDefault().setStatusText("Scan gene-pair-based association task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            ph.progress(100);

            return null;
        }

        @Override
        protected void done() {
            try {
                String message;
                if (!succeed) {
                    message = ("Overlap-free geneset association scan failed!");
                    return;
                }

                for (Object[] objs : listPValueTableData) {
                    String pathID = (String) objs[0];
                    Double pp = pathwayNewPMap.get(pathID);
                    if (pp != null) {
                        objs[5] = Util.formatPValue(pp);;
                    } else {
                        objs[5] = "?";
                    }
                }
                listPValueTableModel.fireTableDataChanged();

                JOptionPane.showMessageDialog(GlobalManager.mainFrame, "Finished!", "Message", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }


    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        listPValueTableData.clear();
        listPValueTableModel.fireTableDataChanged();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        for (Object[] cells : listPValueTableData) {
            cells[4] = true;
        }
        listPValueTableModel.fireTableDataChanged();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        for (Object[] cells : listPValueTableData) {
            cells[4] = false;
        }
        listPValueTableModel.fireTableDataChanged();
    }//GEN-LAST:event_jButton4ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox associationScanSetComboBox;
    private javax.swing.JButton condtionalAnalysisButton;
    private javax.swing.JTable geneListTable;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea pathwaySearchTextArea;
    private javax.swing.JButton retrieveGeneButton;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
