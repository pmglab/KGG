/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.dialog;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.cobi.kgg.business.entity.Chromosome;
import org.cobi.kgg.business.entity.Constants;
import static org.cobi.kgg.business.entity.Constants.CHROM_NAMES;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.GenePosComparator;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.PValueGene;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.SNPPosiComparator;
import org.cobi.kgg.ui.ArrayListObjectArrayTableModel;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.action.BuildGenome;
import org.cobi.kgg.ui.action.ScanConditionalGeneBasedAssociation;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.text.LocalExcelFile;
import org.cobi.util.text.LocalFile;
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
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.cobi.kgg.ui.dialog//ConditionalGeneAssoc//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "ConditionalGeneAssocTopComponent",
    iconBase = "org/cobi/kgg/ui/png/16x16/Magic wand.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Gene", id = "org.cobi.kgg.ui.dialog.ConditionalGeneAssocTopComponent")
@ActionReference(path = "Menu/Gene" /*, position = 333*/)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_ConditionalGeneAssocAction",
    preferredID = "ConditionalGeneAssocTopComponent"
)
@Messages({
  "CTL_ConditionalGeneAssocAction=Conditional Association",
  "CTL_ConditionalGeneAssocTopComponent=ConditionalGeneAssoc",
  "HINT_ConditionalGeneAssocTopComponent=This is a ConditionalGeneAssoc window"
})
public final class ConditionalGeneAssocTopComponent extends TopComponent implements Constants {

  private List<Object[]> listPValueTableData = null;
  private ArrayListObjectArrayTableModel listPValueTableModel = null;
  DefaultComboBoxModel<String> pVlaueSoureModel = new DefaultComboBoxModel<String>();
  private ObjectArrayIntComparator oac = new ObjectArrayIntComparator(2);

  Map<String, Double> genePMap = null;
  Map<String, int[]> geneIndexMaps = null;

  Genome currGenome = null;
  int effectiveSNPPIndex = -1;
  IntArrayList effectiveChrIDs = new IntArrayList();
  String[] pValueTitles = null;
  private RequestProcessor.Task buildTask = null;
  private final static RequestProcessor RP = new RequestProcessor("Scan conditional gene", 1, true);

  public ConditionalGeneAssocTopComponent() {
    listPValueTableData = new ArrayList<Object[]>();
    listPValueTableModel = new ArrayListObjectArrayTableModel();
    pValueTitles = new String[9];
    pValueTitles[0] = "Group";
    pValueTitles[1] = "Gene";
    pValueTitles[2] = "Chromosome";
    pValueTitles[3] = "StartPosition";
    pValueTitles[4] = "OriginalP";
    pValueTitles[5] = "#SNP";
    pValueTitles[6] = "RankingScore";
    pValueTitles[7] = "Select";
    pValueTitles[8] = "Cond.P.";
    listPValueTableModel.setTitle(pValueTitles);

    initComponents();
    setName(Bundle.CTL_ConditionalGeneAssocTopComponent());
    setToolTipText(Bundle.HINT_ConditionalGeneAssocTopComponent());
    genePMap = new HashMap<String, Double>();
    associationScanSetComboBoxActionPerformed(null);
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

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane2 = new javax.swing.JScrollPane();
    geneListTable = new javax.swing.JTable();
    jPanel1 = new javax.swing.JPanel();
    jButton3 = new javax.swing.JButton();
    jButton4 = new javax.swing.JButton();
    condtionalAnalysisButton = new javax.swing.JButton();
    exportButton = new javax.swing.JButton();
    jTabbedPane1 = new javax.swing.JTabbedPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    geneSearchTextArea = new javax.swing.JTextArea();
    jScrollPane3 = new javax.swing.JScrollPane();
    regionsTextArea = new javax.swing.JTextArea();
    jPanel3 = new javax.swing.JPanel();
    jLabel3 = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    jTextField2 = new javax.swing.JTextField();
    multiMethodComboBox = new javax.swing.JComboBox();
    jLabel5 = new javax.swing.JLabel();
    distanceTextField = new javax.swing.JTextField();
    jLabel6 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    associationScanSetComboBox = new javax.swing.JComboBox();
    pvalueSoureComboBox = new javax.swing.JComboBox();
    jLabel2 = new javax.swing.JLabel();
    retrieveGeneButton = new javax.swing.JButton();
    updateRankButton = new javax.swing.JButton();
    resetButton = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();
    jScrollPane4 = new javax.swing.JScrollPane();
    jTextArea1 = new javax.swing.JTextArea();

    setMaximumSize(new java.awt.Dimension(3276, 3276));
    setPreferredSize(new java.awt.Dimension(573, 319));

    jScrollPane2.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jScrollPane2.toolTipText")); // NOI18N

    geneListTable.setModel(listPValueTableModel);
    geneListTable.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.geneListTable.toolTipText")); // NOI18N
    jScrollPane2.setViewportView(geneListTable);

    org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jButton3.text")); // NOI18N
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton3ActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jButton4.text")); // NOI18N
    jButton4.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton4ActionPerformed(evt);
      }
    });

    condtionalAnalysisButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    condtionalAnalysisButton.setForeground(new java.awt.Color(255, 0, 102));
    org.openide.awt.Mnemonics.setLocalizedText(condtionalAnalysisButton, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.condtionalAnalysisButton.text")); // NOI18N
    condtionalAnalysisButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        condtionalAnalysisButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(exportButton, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.exportButton.text")); // NOI18N
    exportButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(condtionalAnalysisButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(0, 0, Short.MAX_VALUE))
          .addComponent(exportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jButton3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jButton4)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(condtionalAnalysisButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(exportButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jScrollPane1.border.title"))); // NOI18N

    geneSearchTextArea.setColumns(20);
    geneSearchTextArea.setRows(5);
    geneSearchTextArea.setText(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.geneSearchTextArea.text")); // NOI18N
    jScrollPane1.setViewportView(geneSearchTextArea);

    jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jScrollPane1.TabConstraints.tabTitle"), jScrollPane1); // NOI18N

    regionsTextArea.setColumns(20);
    regionsTextArea.setRows(5);
    regionsTextArea.setText(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.regionsTextArea.text")); // NOI18N
    jScrollPane3.setViewportView(regionsTextArea);

    jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jScrollPane3.TabConstraints.tabTitle"), jScrollPane3); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jLabel3.text")); // NOI18N
    jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jLabel3.toolTipText")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jLabel4.text")); // NOI18N
    jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jLabel4.toolTipText")); // NOI18N

    jTextField2.setText(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jTextField2.text")); // NOI18N

    multiMethodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benjamini & Hochberg (1995)", "Benjamini & Yekutieli (2001)", "Standard Bonferroni", "Fixed p-value cutoff" }));

    org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jLabel5.text")); // NOI18N

    distanceTextField.setText(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.distanceTextField.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jLabel6.text")); // NOI18N

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addComponent(jLabel4)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jLabel5)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(distanceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel6))
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addComponent(jLabel3)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(multiMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(multiMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel5)
          .addComponent(distanceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel6))
        .addContainerGap())
    );

    jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jLabel1.text")); // NOI18N

    associationScanSetComboBox.setModel(GlobalManager.geneAssocSetModel);
    associationScanSetComboBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        associationScanSetComboBoxItemStateChanged(evt);
      }
    });
    associationScanSetComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        associationScanSetComboBoxActionPerformed(evt);
      }
    });

    pvalueSoureComboBox.setModel(pVlaueSoureModel);
    pvalueSoureComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        pvalueSoureComboBoxActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jLabel2.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(retrieveGeneButton, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.retrieveGeneButton.text")); // NOI18N
    retrieveGeneButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        retrieveGeneButtonActionPerformed(evt);
      }
    });

    updateRankButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    updateRankButton.setForeground(new java.awt.Color(255, 0, 0));
    org.openide.awt.Mnemonics.setLocalizedText(updateRankButton, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.updateRankButton.text")); // NOI18N
    updateRankButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        updateRankButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(resetButton, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.resetButton.text")); // NOI18N
    resetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jButton2.text")); // NOI18N
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton2ActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pvalueSoureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
              .addComponent(jLabel1)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(associationScanSetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
              .addComponent(retrieveGeneButton)
              .addGap(12, 12, 12)
              .addComponent(updateRankButton)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
              .addComponent(resetButton, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
              .addContainerGap()))))
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(associationScanSetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(pvalueSoureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel2)
          .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(retrieveGeneButton)
            .addComponent(updateRankButton))
          .addComponent(resetButton))
        .addContainerGap())
    );

    jTextArea1.setColumns(20);
    jTextArea1.setRows(5);
    jTextArea1.setText(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jTextArea1.text")); // NOI18N
    jTextArea1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConditionalGeneAssocTopComponent.class, "ConditionalGeneAssocTopComponent.jTextArea1.border.title"))); // NOI18N
    jScrollPane4.setViewportView(jTextArea1);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addComponent(jScrollPane2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jTabbedPane1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
          .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

    private void retrieveGeneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retrieveGeneButtonActionPerformed
      try {
        listPValueTableData.clear();
        //record the classification settings
        LoadGeneSwingWorker worker = new LoadGeneSwingWorker();
        // buildTask = buildTask.create(buildingThread); //the task is not started yet
        buildTask = RP.create(worker); //the task is not started yet
        buildTask.schedule(0); //start the task
      } catch (Exception ex) {
        ErrorManager.getDefault().notify(ex);
      }
    }//GEN-LAST:event_retrieveGeneButtonActionPerformed

  private boolean handleCancel() {
    if (null == buildTask) {
      return false;
    }
    return buildTask.cancel();
  }

  class LoadGeneSwingWorker extends SwingWorker<Void, String> {

    int runningThread = 0;
    boolean succeed = false;
    ProgressHandle ph = null;
    long time;

    public LoadGeneSwingWorker() {
      ph = ProgressHandleFactory.createHandle("task thats shows progress", new Cancellable() {
        @Override
        public boolean cancel() {
          return handleCancel();
        }
      });

      time = System.nanoTime();
    }

    @Override
    protected Void doInBackground() {
      try {
        long startTime = System.currentTimeMillis();
        String inforString = "Load gene-based association on the genome ...";

        StatusDisplayer.getDefault().setStatusText(inforString);

//pathwayBasedAssociation.getpValueSources();
        ph.start(); //we must start the PH before we swith to determinate
        ph.switchToIndeterminate();
        GeneBasedAssociation geneSet = (GeneBasedAssociation) associationScanSetComboBox.getSelectedItem();

        if (geneSet == null) {
          return null;
        }

        currGenome = geneSet.getGenome();

        boolean isMultP = geneSet.isMultVariateTest();
        String info = "This gene-based association is a multiple-phenotype analysis and is not supported for conditional gene-based analysis for the time being!";
        if (isMultP) {
          JOptionPane.showMessageDialog(GlobalManager.mainFrame, info, "Error", JOptionPane.ERROR_MESSAGE);
          return null;
        }

        String sourcePValueName = pVlaueSoureModel.getSelectedItem().toString();
        List<PValueGene> genePList = null;
        if (geneSet.isMultVariateTest()) {
          genePList = geneSet.loadGenePValuesfromDisk("MulVar", null);
        } else {
          genePList = geneSet.loadGenePValuesfromDisk(sourcePValueName, null);
        }
        String[] pValueNames = currGenome.getpValueNames();
        effectiveSNPPIndex = -1;
        for (int i = 0; i < pValueNames.length; i++) {
          if (pValueNames[i].equals(sourcePValueName)) {
            effectiveSNPPIndex = i;
            break;
          }
        }
        List<String> geneStringList = new ArrayList<String>();
        int[] gennIndex;
        int currChromIndex = -1;
        Chromosome currChrom = null;
        genePMap.clear();

        for (PValueGene pGene : genePList) {
          genePMap.put(pGene.getSymbol(), pGene.getpValue());
        }

        geneIndexMaps = currGenome.loadGeneGenomeIndexes2Buf();
        List<Object[]> listPValueTableDataTmp = new ArrayList<Object[]>();
        List<IntDoubleUnit> pValueOrders = new ArrayList<IntDoubleUnit>();
        effectiveChrIDs.clear();
        if (jTabbedPane1.getSelectedIndex() == 0) {
          String inutText = geneSearchTextArea.getText();

          StringTokenizer tokenizer = new StringTokenizer(inutText);
          while (tokenizer.hasMoreTokens()) {
            geneStringList.add(tokenizer.nextToken());
          }
          if (geneStringList.isEmpty()) {
            JOptionPane.showMessageDialog(GlobalManager.mainFrame, "No gene symboles are available!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
          }
          int groupID = 0;
          for (currChromIndex = 0; currChromIndex < CHROM_NAMES.length; currChromIndex++) {
            listPValueTableDataTmp.clear();
            for (String geneStr : geneStringList) {
              gennIndex = geneIndexMaps.get(geneStr);
              if (currChromIndex == gennIndex[0]) {
                Object[] cells = new Object[9];

                cells[1] = geneStr;
                cells[2] = CHROM_NAMES[gennIndex[0]];
                listPValueTableDataTmp.add(cells);
              }
            }
            if (listPValueTableDataTmp.isEmpty()) {
              continue;
            }
            currChrom = currGenome.readChromosomefromDisk(currChromIndex);

            groupID++;
            for (Object[] cells : listPValueTableDataTmp) {
              gennIndex = geneIndexMaps.get(cells[1].toString());
              if (gennIndex == null) {
                continue;
              }
              Gene gene1 = currChrom.genes.get(gennIndex[1]);
              cells[0] = String.valueOf(groupID);
              cells[3] = String.valueOf(gene1.start);
              double p = genePMap.get(cells[1].toString());
              cells[4] = Util.formatPValue(p);
              cells[5] = String.valueOf(gene1.snps.size());
              if (p == 0) {
                p = 1E-300;
              }
              cells[6] = String.format("%.2f", -Math.log10(p));
              cells[7] = true;
              cells[8] = "?";
            }
            /*
                         Collections.sort(listPValueTableDataTmp, oac);
                         int index = 0;
                         pValueOrders.clear();
                         for (Object[] cells1 : listPValueTableDataTmp) {
                         double p = Double.parseDouble(cells1[3].toString());
                         pValueOrders.add(new IntDoubleUnit(index, p));
                         index++;
                         }
                         Collections.sort(pValueOrders, new IntDoubleUnitComparator());
                         index = 0;
                         for (index = 0; index < pValueOrders.size(); index++) {
                         IntDoubleUnit pv = pValueOrders.get(index);
                         listPValueTableDataTmp.get(pv.index)[5] = String.valueOf(index + 1);
                         }
             */
            listPValueTableData.addAll(listPValueTableDataTmp);
            effectiveChrIDs.add(currChromIndex);
            publish("Chromosome " + CHROM_NAMES[currChromIndex] + " loaded!");
          }

        } else if (jTabbedPane1.getSelectedIndex() == 1) {
          String regDef = regionsTextArea.getText();

          String[] r1 = regDef.split("[,|\n]");
          Map<String, int[]> regionsIn = new HashMap<String, int[]>();

          for (int i = 0; i < r1.length; i++) {
            r1[i] = r1[i].trim();
            int index = r1[i].indexOf(':');
            if (index < 0) {
              String chrom = r1[i].substring(3);
              regionsIn.put(chrom, new int[]{-9, -9});
              continue;
            }
            String chrom = r1[i].substring(0, r1[i].indexOf(':'));
            chrom = chrom.substring(3);
            int[] localRegion = regionsIn.get(chrom);
            if (localRegion == null) {
              localRegion = new int[2];
            } else {
              int[] tmpLocalRegion = new int[localRegion.length];
              System.arraycopy(localRegion, 0, tmpLocalRegion, 0, localRegion.length);

              localRegion = new int[localRegion.length + 2];
              System.arraycopy(tmpLocalRegion, 0, localRegion, 0, tmpLocalRegion.length);
            }

            r1[i] = r1[i].substring(index + 1);
            index = r1[i].indexOf("-");
            if (index >= 0) {
              int pos1 = (int) Double.parseDouble(r1[i].substring(0, index));
              int pos2 = (int) Double.parseDouble(r1[i].substring(index + 1));
              localRegion[localRegion.length - 2] = pos1;
              localRegion[localRegion.length - 1] = pos2;
              regionsIn.put(chrom, localRegion);
            }
          }
          int[][] regionsInPos = new int[CHROM_NAMES.length][];
          StringBuilder regionsInStr = new StringBuilder();
          geneIndexMaps = currGenome.loadGeneGenomeIndexes2Buf();
          int groupID = 0;
          for (int t = 0; t < CHROM_NAMES.length; t++) {
            int[] physicalRegions = regionsIn.get(CHROM_NAMES[t]);
            if (physicalRegions == null) {
              continue;
            }
            int regionNum = physicalRegions.length / 2;

            regionsInPos[t] = physicalRegions;

            for (int j = 0; j < regionNum; j++) {
              if (physicalRegions[j * 2] != -9 || physicalRegions[j * 2 + 1] != -9) {
                if (physicalRegions[j * 2 + 1] == -9) {
                  physicalRegions[1] = Integer.MAX_VALUE;
                }
                regionsInStr.append(" chr");
                regionsInStr.append(CHROM_NAMES[t]);
                regionsInStr.append("[");
                regionsInStr.append(physicalRegions[j * 2]);
                regionsInStr.append(",");
                regionsInStr.append(physicalRegions[j * 2 + 1]);
                regionsInStr.append("]bp ");
              } else if (physicalRegions[j * 2] == -9 && physicalRegions[j * 2 + 1] == -9) {

                physicalRegions[1] = Integer.MAX_VALUE;
                regionsInStr.append("chr").append(CHROM_NAMES[t]).append(" ");
              }
            }

            currChromIndex = t;
            currChrom = currGenome.readChromosomefromDisk(currChromIndex);
            List<Gene> genes = currChrom.genes;
            boolean haveGotGene = false;
            listPValueTableDataTmp.clear();
            for (int j = 0; j < regionNum; j++) {
              groupID++;
              for (Gene gene : genes) {
                List<SNP> snps = gene.snps;
                haveGotGene = false;
                for (SNP snp : snps) {
                  {
                    if (snp.physicalPosition >= physicalRegions[j * 2] && snp.physicalPosition <= physicalRegions[j * 2 + 1]) {
                      gennIndex = geneIndexMaps.get(gene.getSymbol());
                      if (currChromIndex == gennIndex[0]) {
                        haveGotGene = true;
                        break;
                      }
                    }
                  }
                  if (haveGotGene) {
                    break;
                  }
                }
                if (haveGotGene) {
                  Object[] cells = new Object[9];
                  listPValueTableDataTmp.add(cells);
                  cells[0] = String.valueOf(groupID);
                  cells[1] = gene.getSymbol();
                  cells[2] = CHROM_NAMES[currChromIndex];
                }
              }
            }
            if (listPValueTableDataTmp.isEmpty()) {
              continue;
            }

            for (Object[] cells : listPValueTableDataTmp) {
              gennIndex = geneIndexMaps.get(cells[1].toString());
              Gene gene1 = currChrom.genes.get(gennIndex[1]);
              cells[3] = String.valueOf(gene1.start);
              double p = genePMap.get(cells[1].toString());
              cells[4] = Util.formatPValue(p);
              cells[5] = String.valueOf(gene1.snps.size());
              if (p == 0) {
                p = 1E-300;
              }
              cells[6] = String.format("%.2f", -Math.log10(p));
              cells[7] = true;
              cells[8] = "?";
            }
            /*
                         Collections.sort(listPValueTableDataTmp, oac);
                         int index = 0;
                         pValueOrders.clear();
                         for (Object[] cells1 : listPValueTableDataTmp) {
                         double p = Double.parseDouble(cells1[3].toString());
                         pValueOrders.add(new IntDoubleUnit(index, p));
                         index++;
                         }
                         Collections.sort(pValueOrders, new IntDoubleUnitComparator());
                         index = 0;
                         for (index = 0; index < pValueOrders.size(); index++) {
                         IntDoubleUnit pv = pValueOrders.get(index);
                         listPValueTableDataTmp.get(pv.index)[5] = String.valueOf(index + 1);
                         }
             */
            listPValueTableData.addAll(listPValueTableDataTmp);
            effectiveChrIDs.add(currChromIndex);
            publish("Chromosome " + CHROM_NAMES[t] + " loaded!");
          }
        } else {
          DoubleArrayList genePValues = new DoubleArrayList();
          for (PValueGene pGene : genePList) {
            if (!Double.isNaN(pGene.getpValue())) {
              genePValues.add(pGene.getpValue());
            }
          }
          genePValues.quickSort();
          int intFlag = multiMethodComboBox.getSelectedIndex();
          double dblER = Double.valueOf(jTextField2.getText());

          DoubleArrayList adjustedP = new DoubleArrayList();
          double dblCutoff = 0;
          switch (intFlag) {
            case 0:
              dblCutoff = MultipleTestingMethod.BenjaminiHochbergFDR("Genes", dblER, genePValues, adjustedP);
              break;
            case 1:
              dblCutoff = MultipleTestingMethod.BenjaminiYekutieliFDR("Genes", dblER, genePValues, adjustedP);
              break;
            case 2:
              dblCutoff = dblER / genePValues.size();
              MultipleTestingMethod.logInfo("Genes", "Standard Bonferron", dblCutoff);
              break;
            case 3:
              dblCutoff = dblER;
              break;
          }
          List<String>[] chrGenes = new List[CHROM_NAMES.length];
          for (PValueGene pGene : genePList) {
            if (!Double.isNaN(pGene.getpValue())) {
              if (pGene.getpValue() <= dblCutoff) {
                gennIndex = geneIndexMaps.get(pGene.getSymbol());
                if (gennIndex == null) {
                  continue;
                }
                if (chrGenes[gennIndex[0]] == null) {
                  chrGenes[gennIndex[0]] = new ArrayList<String>();
                }
                chrGenes[gennIndex[0]].add(pGene.getSymbol());
              }
            }
          }
          int groupID = 0;
          List<Gene> selGenes = new ArrayList<Gene>();

          //
          for (currChromIndex = 0; currChromIndex < CHROM_NAMES.length; currChromIndex++) {
            if (chrGenes[currChromIndex] == null) {
              continue;
            }

            currChrom = currGenome.readChromosomefromDisk(currChromIndex);
            List<Gene> genes = currChrom.genes;
            selGenes.clear();
            for (String gsm : chrGenes[currChromIndex]) {
              gennIndex = geneIndexMaps.get(gsm);
              if (gennIndex == null) {
                continue;
              }
              selGenes.add(genes.get(gennIndex[1]));
              Collections.sort(genes.get(gennIndex[1]).snps, new SNPPosiComparator());
            }

            Collections.sort(selGenes, new GenePosComparator());

            listPValueTableDataTmp.clear();
            int end0, start1;
            int index = 0;
            int minDistance = Integer.parseInt(distanceTextField.getText());
            boolean notTheEnd = true;
            int size = selGenes.size();

            groupID++;
            end0 = selGenes.get(index).snps.get(selGenes.get(index).snps.size() - 1).physicalPosition;
            Object[] cells = new Object[9];
            listPValueTableDataTmp.add(cells);
            cells[0] = String.valueOf(groupID);
            cells[1] = selGenes.get(index).getSymbol();
            cells[2] = CHROM_NAMES[currChromIndex];

            do {
              index++;
              if (index >= size) {
                notTheEnd = false;
                break;
              }
              start1 = selGenes.get(index).snps.get(0).physicalPosition;
              while (Math.abs(end0 - start1) <= minDistance) {
                cells = new Object[9];
                listPValueTableDataTmp.add(cells);
                cells[0] = String.valueOf(groupID);
                cells[1] = selGenes.get(index).getSymbol();
                cells[2] = CHROM_NAMES[currChromIndex];

                index++;
                if (index >= size) {
                  notTheEnd = false;
                  break;
                }
                end0 = start1;
                start1 = selGenes.get(index).snps.get(0).physicalPosition;
              }
              if (notTheEnd) {
                groupID++;
                cells = new Object[9];
                listPValueTableDataTmp.add(cells);
                cells[0] = String.valueOf(groupID);
                cells[1] = selGenes.get(index).getSymbol();
                cells[2] = CHROM_NAMES[currChromIndex];
                end0 = selGenes.get(index).snps.get(selGenes.get(index).snps.size() - 1).physicalPosition;

              }
            } while (notTheEnd);

            for (Object[] cells1 : listPValueTableDataTmp) {
              gennIndex = geneIndexMaps.get(cells1[1].toString());
              Gene gene1 = currChrom.genes.get(gennIndex[1]);
              cells1[3] = String.valueOf(gene1.start);
              double p = genePMap.get(cells1[1].toString());
              cells1[4] = Util.formatPValue(p);
              cells1[5] = String.valueOf(gene1.snps.size());
              if (p == 0) {
                p = 1E-300;
              }
              cells1[6] = String.format("%.2f", -Math.log10(p));
              cells1[7] = true;
              cells1[8] = "?";
            }
            publish("Chromosome " + CHROM_NAMES[currChromIndex] + " loaded!");
            listPValueTableData.addAll(listPValueTableDataTmp);
            effectiveChrIDs.add(currChromIndex);
          }
        }
        genePList.clear();

        //GlobalManager.mainView.displayPathwayTree(pathwayTreeRoot);
        // InterfaceUtil.saveTreeNode2XMLFile(pathwayTreeRoot, storagePath);
        System.gc();

        succeed = true;
      } catch (Exception ex) {
        StatusDisplayer.getDefault().setStatusText("Gene loading task was CANCELLED!");
        java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }

      return null;
    }

    @Override
    protected void process(List<String> chunks) {
      // TODO Auto-generated method stub  
      for (String message : chunks) {
        listPValueTableModel.setDataList(listPValueTableData);
        listPValueTableModel.fireTableDataChanged();
        StatusDisplayer.getDefault().setStatusText(message);
      }

    }

    @Override
    protected void done() {
      try {
        String message;
        if (!succeed) {
          message = ("Failed to load genes!");
          JOptionPane.showMessageDialog(GlobalManager.mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        ProjectTopComponent projectTopComponent = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
        projectTopComponent.showProject(GlobalManager.currentProject);

        message = ("Genes have been loaded!");
        JOptionPane.showMessageDialog(GlobalManager.mainFrame, message, "Information", JOptionPane.INFORMATION_MESSAGE);
        StatusDisplayer.getDefault().setStatusText(message);
        ph.finish();

        listPValueTableModel.setDataList(listPValueTableData);
        listPValueTableModel.fireTableDataChanged();
      } catch (Exception e) {
        ErrorManager.getDefault().notify(e);
      }
    }
  }


    private void condtionalAnalysisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_condtionalAnalysisButtonActionPerformed
      if (false) {
        JOptionPane.showMessageDialog(this, "Sorry, this function is still under development!", "Message", JOptionPane.INFORMATION_MESSAGE);
        return;
      }

      ScanConditionalGeneBasedAssociation scanConditionAssoc = new ScanConditionalGeneBasedAssociation(listPValueTableData);
      //   scanConditionAssoc.setCurrChrom(currChrom);        ;
      scanConditionAssoc.setGeneSet((GeneBasedAssociation) associationScanSetComboBox.getSelectedItem());
      scanConditionAssoc.setChromIDs(effectiveChrIDs);
      scanConditionAssoc.setGeneGenomeIndexes(geneIndexMaps);
      scanConditionAssoc.setGenome(currGenome);
      scanConditionAssoc.setSnpPIndex(effectiveSNPPIndex);
      scanConditionAssoc.conditionScan(listPValueTableModel);
      // listPValueTableModel.fireTableDataChanged();


    }//GEN-LAST:event_condtionalAnalysisButtonActionPerformed

    private void pvalueSoureComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pvalueSoureComboBoxActionPerformed

    }//GEN-LAST:event_pvalueSoureComboBoxActionPerformed

    private void associationScanSetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_associationScanSetComboBoxActionPerformed
      try {
        pVlaueSoureModel.removeAllElements();

        GeneBasedAssociation geneSet = (GeneBasedAssociation) associationScanSetComboBox.getSelectedItem();
        if (geneSet == null) {
          return;
        }

        if (!geneSet.getTestedMethod().equals("ECS")) {
          String msg = "This gene association set '" + geneSet.getName() + "' was produced by “GATES”\nand NOT suitable for conditional gene-based association analysis!";
          JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
        }

        List<String> pValueNames = geneSet.getPValueSources();
        if (pValueNames != null && !pValueNames.isEmpty()) {
          for (int j = 0; j < pValueNames.size(); j++) {
            pVlaueSoureModel.addElement(pValueNames.get(j));
          }
        }
      } catch (Exception ex) {
        ErrorManager.getDefault().notify(ex);
      }

    }//GEN-LAST:event_associationScanSetComboBoxActionPerformed

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

    private void associationScanSetComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_associationScanSetComboBoxItemStateChanged
      try {
        GeneBasedAssociation geneSet = (GeneBasedAssociation) associationScanSetComboBox.getSelectedItem();
        if (geneSet == null) {
          return;
        }
        pvalueSoureComboBox.removeAllItems();
        List<String> pSources = geneSet.getPValueSources();
        for (String pSource : pSources) {
          pvalueSoureComboBox.addItem(pSource);
        }

      } catch (Exception ex) {
        ErrorManager.getDefault().notify(ex);
      }
    }//GEN-LAST:event_associationScanSetComboBoxItemStateChanged

    private void updateRankButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateRankButtonActionPerformed
      String path = null;
      JFileChooser fDialog = null;
      if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
        fDialog = new JFileChooser(GlobalManager.lastAccessedPath);
      } else {
        fDialog = new JFileChooser();
      }
      fDialog.setDialogTitle(OPEN_CANDIDATE_GENE_PEDFILE);

      int result = fDialog.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        try {
          File file = fDialog.getSelectedFile();
          path = file.getCanonicalPath();

          // GlobalManager.addInforLog("Load candidate gene file: " + path);
          int[] indices = {0, 1};
          ArrayList<String[]> allGenes = new ArrayList<String[]>();
          LocalFile.retrieveData(path, allGenes, indices, "\t");

          Map<String, String> geneScore = new HashMap<String, String>();

          for (int i = 0; i < allGenes.size(); i++) {
            String[] row = allGenes.get(i);
            geneScore.put(row[0], row[1]);
          }
          allGenes.clear();
          int size = listPValueTableData.size();
          String geneSym = null;
          String socre;
          String chrNum;
          int socreCount = 0;
          List<Object[]> listPValueTableDataTmp = new ArrayList<Object[]>();

          for (int currChromIndex = 0; currChromIndex < CHROM_NAMES.length; currChromIndex++) {
            listPValueTableDataTmp.clear();
            for (int i = 0; i < size; i++) {
              Object[] cells = listPValueTableData.get(i);
              chrNum = cells[2].toString();
              if (!chrNum.equals(CHROM_NAMES[currChromIndex])) {
                continue;
              }
              listPValueTableDataTmp.add(cells);
            }
            socreCount = 0;
            boolean[] hasNoScores = new boolean[listPValueTableDataTmp.size()];
            int t = -1;
            for (Object[] cells : listPValueTableDataTmp) {
              t++;
              geneSym = cells[1].toString();
              socre = geneScore.get(geneSym);
              if (socre == null) {
                hasNoScores[t] = true;
                cells[6] = "-";
                continue;
              }
              hasNoScores[t] = false;
              cells[6] = socre;
              socreCount++;
            }
            if (socreCount > 0) {
              t = -1;
              for (Object[] cells : listPValueTableDataTmp) {
                t++;
                if (hasNoScores[t]) {
                  cells[6] = "-";
                }
              }
            }
          }

          for (Object[] cells : listPValueTableData) {
            cells[8] = "?";
          }

          listPValueTableModel.fireTableDataChanged();
          geneScore.clear();
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }

      } else {
        JOptionPane.showMessageDialog(this, FILE_OPEN_CANCELLED, "Message", JOptionPane.INFORMATION_MESSAGE);
      }
    }//GEN-LAST:event_updateRankButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
      double p;
      for (Object[] cells : listPValueTableData) {
        if (cells[4].equals("-")) {
          cells[6] = cells[4];
        } else {
          p = Double.parseDouble((String) cells[4]);
          if (p == 0) {
            p = 1E-300;
          }
          cells[6] = String.format("%.2f", -Math.log10(p));
        }
      }

      listPValueTableModel.fireTableDataChanged();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
      TopComponent outputWindow = WindowManager.getDefault().findTopComponent("AnalysisOutputTopComponent");
      //Determine if it is opened
      if (outputWindow != null && !outputWindow.isOpened()) {
        outputWindow.open();
      }
      List<String[]> lstOutput = new ArrayList();

      //String strFormat = jComboBox1.getSelectedItem().toString();
      JFileChooser jfcSave = null;
      if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
        jfcSave = new JFileChooser(GlobalManager.lastAccessedPath);
      } else {
        jfcSave = new JFileChooser();
      }
      try {
        jfcSave.setDialogTitle("Save conditional gene-based p-value!");
        FileNameExtensionFilter fnefFilter = null;
        fnefFilter = new FileNameExtensionFilter("*.xlsx", "xlsx");
        jfcSave.setFileFilter(fnefFilter);
        int intResult = jfcSave.showSaveDialog(this);
        File fleFile = jfcSave.getSelectedFile();
        if (intResult == JFileChooser.APPROVE_OPTION) {
          if (!fleFile.getPath().endsWith("xlsx")) {
            fleFile = new File(fleFile.getPath() + ".xlsx");
          }
          GlobalManager.lastAccessedPath = fleFile.getParent();
        } else {
          return;
        }
        lstOutput.add(pValueTitles);

        for (Object[] objs : listPValueTableData) {
          String[] cells = new String[objs.length];
          for (int t = 0; t < cells.length; t++) {
            cells[t] = objs[t].toString();
          }
          if (cells[cells.length - 1].equals("?")) {
            cells[cells.length - 1] = "-";
          }
          lstOutput.add(cells);
        }
        LocalExcelFile.WriteArray2XLSXFile(fleFile.getCanonicalPath(), lstOutput, true, 0);

        if (fleFile.exists()) {
          JOptionPane.showMessageDialog(this, "The data have has been saved in " + fleFile.getCanonicalPath() + ".", "File Infomation", JOptionPane.CLOSED_OPTION);
        }
      } catch (Exception ex) {
        Exceptions.printStackTrace(ex);
      }


    }//GEN-LAST:event_exportButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox associationScanSetComboBox;
  private javax.swing.JButton condtionalAnalysisButton;
  private javax.swing.JTextField distanceTextField;
  private javax.swing.JButton exportButton;
  private javax.swing.JTable geneListTable;
  private javax.swing.JTextArea geneSearchTextArea;
  private javax.swing.JButton jButton2;
  private javax.swing.JButton jButton3;
  private javax.swing.JButton jButton4;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JTextArea jTextArea1;
  private javax.swing.JTextField jTextField2;
  private javax.swing.JComboBox multiMethodComboBox;
  private javax.swing.JComboBox pvalueSoureComboBox;
  private javax.swing.JTextArea regionsTextArea;
  private javax.swing.JButton resetButton;
  private javax.swing.JButton retrieveGeneButton;
  private javax.swing.JButton updateRankButton;
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
