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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.cobi.kgg.business.entity.Chromosome;
import static org.cobi.kgg.business.entity.Constants.CHROM_NAMES;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.GenePosComparator;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.PValueGene;
import org.cobi.kgg.business.entity.SNPPosiComparator;
import org.cobi.kgg.ui.ArrayListObjectArrayTableModel;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.LinkLabel;
import org.cobi.kgg.ui.action.BuildGenome;
import org.cobi.kgg.ui.action.ScanConditionalGeneBasedAssociation;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.text.LocalExcelFile;
import org.cobi.util.text.ObjectArrayIntComparator;
import org.cobi.util.text.Util;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
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
    dtd = "-//org.cobi.kgg.ui.dialog//DriverTissue//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "DriverTissueTopComponent",
    iconBase = "org/cobi/kgg/ui/png/16x16/Taxi.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.cobi.kgg.ui.dialog.DriverTissueTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_DriverTissueAction",
    preferredID = "DriverTissueTopComponent"
)
@Messages({
  "CTL_DriverTissueAction=DriverTissue",
  "CTL_DriverTissueTopComponent=Driver Tissue (DESE)",
  "HINT_DriverTissueTopComponent=This is a DriverTissue window"
})
public final class DriverTissueTopComponent extends TopComponent {

  private List<Object[]> listPValueTableData = null;
  private List<Object[]> driverTissueTableData = null;
  private ArrayListObjectArrayTableModel listPValueTableModel = null;
  private ArrayListObjectArrayTableModel driverTissueTableModel = null;
  DefaultComboBoxModel<String> pVlaueSoureModel = new DefaultComboBoxModel<String>();
  private ObjectArrayIntComparator oac = new ObjectArrayIntComparator(2);

  Map<String, Double> genePMap = null;
  Map<String, int[]> geneIndexMaps = null;

  Genome currGenome = null;
  int effectiveSNPPIndex = -1;
  IntArrayList effectiveChrIDs = new IntArrayList();
  String[] pValueTitles = null;
  String[] driverTissueTitles = null;
  private RequestProcessor.Task buildTask = null;
  private final static RequestProcessor RP = new RequestProcessor("Scan conditional gene", 1, true);
  private double pValueCutoff = 0.05;

  public DriverTissueTopComponent() {
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
    pValueTitles[8] = "Cond.P";
    listPValueTableModel.setTitle(pValueTitles);

    driverTissueTableData = new ArrayList<Object[]>();
    driverTissueTableModel = new ArrayListObjectArrayTableModel();
    driverTissueTitles = new String[6];
    driverTissueTitles[0] = "TissueName";
    driverTissueTitles[1] = "RobustRegressionZ";
    driverTissueTitles[2] = "ConventionalZ";
    driverTissueTitles[3] = "MADZ";
    driverTissueTitles[4] = "RatioOfProjection";
    driverTissueTitles[5] = "AveragedLog(p)";

    driverTissueTableModel.setTitle(driverTissueTitles);

    initComponents();
    setName(Bundle.CTL_ConditionalGeneAssocTopComponent());
    setToolTipText(Bundle.HINT_ConditionalGeneAssocTopComponent());
    genePMap = new HashMap<String, Double>();
    associationScanSetComboBoxActionPerformed(null);
    initComponents();
    setName(Bundle.CTL_DriverTissueTopComponent());
    setToolTipText(Bundle.HINT_DriverTissueTopComponent());

  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jPanel1 = new javax.swing.JPanel();
    jButton3 = new javax.swing.JButton();
    jButton4 = new javax.swing.JButton();
    condtionalAnalysisButton = new javax.swing.JButton();
    exportButton = new javax.swing.JButton();
    removeButton = new javax.swing.JButton();
    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel4 = new javax.swing.JPanel();
    jLabel7 = new javax.swing.JLabel();
    filePathTextField = new javax.swing.JTextField();
    jLabel8 = new javax.swing.JLabel();
    jButton1 = new javax.swing.JButton();
    tpmTextField = new javax.swing.JTextField();
    jButton6 = new javax.swing.JButton();
    jLabel9 = new javax.swing.JLabel();
    jLabel40 = new LinkLabel ("Download", "http://grass.cgs.hku.hk/limx/rez/download/");
    jPanel2 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    associationScanSetComboBox = new javax.swing.JComboBox();
    pvalueSoureComboBox = new javax.swing.JComboBox();
    jLabel2 = new javax.swing.JLabel();
    jButton2 = new javax.swing.JButton();
    jLabel3 = new javax.swing.JLabel();
    multiMethodComboBox = new javax.swing.JComboBox();
    jLabel4 = new javax.swing.JLabel();
    jTextField2 = new javax.swing.JTextField();
    jLabel5 = new javax.swing.JLabel();
    distanceTextField = new javax.swing.JTextField();
    jLabel6 = new javax.swing.JLabel();
    retrieveGeneButton = new javax.swing.JButton();
    jScrollPane4 = new javax.swing.JScrollPane();
    jTextArea1 = new javax.swing.JTextArea();
    jTabbedPane2 = new javax.swing.JTabbedPane();
    jScrollPane2 = new javax.swing.JScrollPane();
    geneListTable = new javax.swing.JTable();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTable1 = new javax.swing.JTable();

    org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jButton3.text_1")); // NOI18N
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton3ActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jButton4.text_1")); // NOI18N
    jButton4.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton4ActionPerformed(evt);
      }
    });

    condtionalAnalysisButton.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
    condtionalAnalysisButton.setForeground(new java.awt.Color(255, 0, 102));
    org.openide.awt.Mnemonics.setLocalizedText(condtionalAnalysisButton, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.condtionalAnalysisButton.text_1")); // NOI18N
    condtionalAnalysisButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        condtionalAnalysisButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(exportButton, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.exportButton.text_1")); // NOI18N
    exportButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.removeButton.text")); // NOI18N
    removeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        removeButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(exportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(condtionalAnalysisButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(0, 0, Short.MAX_VALUE))
          .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
        .addComponent(removeButton)
        .addContainerGap())
    );

    org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel7.text")); // NOI18N

    filePathTextField.setText(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.filePathTextField.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel8.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jButton1.text")); // NOI18N
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    tpmTextField.setText(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.tpmTextField.text")); // NOI18N

    jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Folder.png"))); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(jButton6, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jButton6.text")); // NOI18N
    jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton6MouseClicked(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel9.text")); // NOI18N

    jLabel40.setFont(new java.awt.Font("Book Antiqua 12 粗体 12", 0, 12)); // NOI18N
    jLabel40.setForeground(new java.awt.Color(255, 0, 0));
    org.openide.awt.Mnemonics.setLocalizedText(jLabel40, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel40.text")); // NOI18N

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel4Layout.createSequentialGroup()
            .addComponent(jLabel7)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jButton1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tpmTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addGap(0, 6, Short.MAX_VALUE))
              .addComponent(filePathTextField))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(16, 16, 16))))
    );
    jPanel4Layout.setVerticalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel7)
          .addComponent(jButton1)
          .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(filePathTextField))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel8)
          .addComponent(tpmTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel9))
        .addContainerGap(21, Short.MAX_VALUE))
    );

    jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel1.text_1")); // NOI18N

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

    org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel2.text_1")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jButton2.text_1")); // NOI18N
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton2ActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel3.text_1")); // NOI18N
    jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel3.toolTipText_1")); // NOI18N

    multiMethodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benjamini & Hochberg (1995)", "Benjamini & Yekutieli (2001)", "Standard Bonferroni", "Fixed p-value cutoff" }));

    org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel4.text_1")); // NOI18N
    jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel4.toolTipText_1")); // NOI18N

    jTextField2.setText(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jTextField2.text_1")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel5.text_1")); // NOI18N

    distanceTextField.setText(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.distanceTextField.text_1")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jLabel6.text_1")); // NOI18N

    retrieveGeneButton.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
    retrieveGeneButton.setForeground(new java.awt.Color(255, 102, 51));
    org.openide.awt.Mnemonics.setLocalizedText(retrieveGeneButton, org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.retrieveGeneButton.text_1")); // NOI18N
    retrieveGeneButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        retrieveGeneButtonActionPerformed(evt);
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
            .addComponent(jLabel3)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(multiMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(retrieveGeneButton))
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pvalueSoureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(associationScanSetComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addContainerGap())
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addComponent(jLabel4)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(jLabel5)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(distanceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel6)
        .addGap(0, 0, Short.MAX_VALUE))
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
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(multiMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(retrieveGeneButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel5)
          .addComponent(distanceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel6))
        .addContainerGap())
    );

    jTextArea1.setColumns(20);
    jTextArea1.setRows(5);
    jTextArea1.setText(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jTextArea1.text_1")); // NOI18N
    jTextArea1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jTextArea1.border.title_1"))); // NOI18N
    jScrollPane4.setViewportView(jTextArea1);

    jScrollPane2.setToolTipText(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jScrollPane2.toolTipText_1")); // NOI18N

    geneListTable.setModel(listPValueTableModel);
    geneListTable.setToolTipText(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.geneListTable.toolTipText_1")); // NOI18N
    jScrollPane2.setViewportView(geneListTable);

    jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N

    jTable1.setModel(driverTissueTableModel);
    jScrollPane1.setViewportView(jTable1);

    jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(DriverTissueTopComponent.class, "DriverTissueTopComponent.jScrollPane1.TabConstraints.tabTitle_1"), jScrollPane1); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jTabbedPane1))
          .addComponent(jScrollPane4)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jTabbedPane2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jTabbedPane1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    for (Object[] cells : listPValueTableData) {
      cells[7] = true;
    }
    listPValueTableModel.fireTableDataChanged();
  }//GEN-LAST:event_jButton3ActionPerformed

  private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    for (Object[] cells : listPValueTableData) {
      cells[7] = false;
    }
    listPValueTableModel.fireTableDataChanged();
  }//GEN-LAST:event_jButton4ActionPerformed

  private void condtionalAnalysisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_condtionalAnalysisButtonActionPerformed
    if (filePathTextField.getText().trim().length() == 0) {
      JOptionPane.showMessageDialog(this, "Please specify the path of expression file!", "Message", JOptionPane.ERROR_MESSAGE);
      return;
    }
    File file = new File(filePathTextField.getText().trim());
    if (!file.exists()) {
      JOptionPane.showMessageDialog(this, file + " does not exist!", "Message", JOptionPane.ERROR_MESSAGE);
      return;
    }
    ScanConditionalGeneBasedAssociation scanConditionAssoc = new ScanConditionalGeneBasedAssociation(listPValueTableData);
    //   scanConditionAssoc.setCurrChrom(currChrom);        ;
    scanConditionAssoc.setGeneSet((GeneBasedAssociation) associationScanSetComboBox.getSelectedItem());
    scanConditionAssoc.setChromIDs(effectiveChrIDs);
    scanConditionAssoc.setGeneGenomeIndexes(geneIndexMaps);
    scanConditionAssoc.setGenome(currGenome);
    scanConditionAssoc.setSnpPIndex(effectiveSNPPIndex);
    double minExp = Double.parseDouble(tpmTextField.getText());

    scanConditionAssoc.setMinExp(minExp);
    scanConditionAssoc.setExpressionPath(filePathTextField.getText());
    scanConditionAssoc.setpValueCutoff(pValueCutoff);
    scanConditionAssoc.setTissueTable(driverTissueTableData);

    scanConditionAssoc.conditionScan(listPValueTableModel, driverTissueTableModel, this);

    // listPValueTableModel.fireTableDataChanged();

  }//GEN-LAST:event_condtionalAnalysisButtonActionPerformed

  private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

    List<String[]> lstOutput = new ArrayList();

    //String strFormat = jComboBox1.getSelectedItem().toString();
    JFileChooser jfcSave = null;
    if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
      jfcSave = new JFileChooser(GlobalManager.lastAccessedPath);
    } else {
      jfcSave = new JFileChooser();
    }
    try {
      if (jTabbedPane2.getSelectedIndex() == 0) {
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
      } else {
        jfcSave.setDialogTitle("Save driver tissue p-value!");
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

        lstOutput.add(driverTissueTitles);

        for (Object[] objs : driverTissueTableData) {
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
      }

    } catch (Exception ex) {
      Exceptions.printStackTrace(ex);
    }

  }//GEN-LAST:event_exportButtonActionPerformed

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

  public void showTissueTab() {
    jTabbedPane2.setSelectedIndex(1);
  }

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

  private void pvalueSoureComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pvalueSoureComboBoxActionPerformed

  }//GEN-LAST:event_pvalueSoureComboBoxActionPerformed

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
        List<ConditionalGeneAssocTopComponent.IntDoubleUnit> pValueOrders = new ArrayList<ConditionalGeneAssocTopComponent.IntDoubleUnit>();
        effectiveChrIDs.clear();
        {
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

          pValueCutoff = dblCutoff;
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


  private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    listPValueTableData.clear();
    listPValueTableModel.fireTableDataChanged();
  }//GEN-LAST:event_jButton2ActionPerformed

  private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
    // TODO add your handling code here:
    String path;
    JFileChooser fDialog;
    if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
      fDialog = new JFileChooser(GlobalManager.lastAccessedPath);
    } else {
      fDialog = new JFileChooser();
    }
    fDialog.setDialogTitle("Choose a Pedigree File with Genotypes");

    int result = fDialog.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      GlobalManager.lastAccessedPath = fDialog.getSelectedFile().getPath();
      GlobalManager.lastAccessedPath = GlobalManager.lastAccessedPath.substring(0, GlobalManager.lastAccessedPath.lastIndexOf(File.separator) + 1);

      try {
        File file = fDialog.getSelectedFile();
        path = file.getCanonicalPath();
        filePathTextField.setText(path);

      } catch (Exception ex) {
        ErrorManager.getDefault().notify(ex);
      }

    } else {
      // JOptionPane.showMessageDialog(this, FILE_OPEN_CANCELLED, "Message", JOptionPane.INFORMATION_MESSAGE);
    }
  }//GEN-LAST:event_jButton6MouseClicked

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    // TODO add your handling code here:
    FormatShowingDialog dialog = new FormatShowingDialog(new javax.swing.JFrame(), true, "GeneExpression");
    dialog.setLocationRelativeTo(GlobalManager.mainFrame);
    dialog.setVisible(true);
  }//GEN-LAST:event_jButton1ActionPerformed

  private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    // TODO add your handling code here:
    if (jTabbedPane2.getSelectedIndex() == 0) {
      String info = "Are you sure to remove all genes in the table?";
      NotifyDescriptor nd = new NotifyDescriptor.Confirmation(info, NotifyDescriptor.YES_NO_OPTION);
      Object result = DialogDisplayer.getDefault().notify(nd);
      if (NotifyDescriptor.OK_OPTION.equals(result)) {
        listPValueTableData.clear();
        listPValueTableModel.fireTableDataChanged();

      } else {
        // don't do it
      }

    } else {
      String info = "Are you sure to remove all tissues in the table?";
      NotifyDescriptor nd = new NotifyDescriptor.Confirmation(info, NotifyDescriptor.YES_NO_OPTION);
      Object result = DialogDisplayer.getDefault().notify(nd);
      if (NotifyDescriptor.OK_OPTION.equals(result)) {
        driverTissueTableData.clear();
        driverTissueTableModel.fireTableDataChanged();

      } else {
        // don't do it
      }
    }
  }//GEN-LAST:event_removeButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox associationScanSetComboBox;
  private javax.swing.JButton condtionalAnalysisButton;
  private javax.swing.JTextField distanceTextField;
  private javax.swing.JButton exportButton;
  private javax.swing.JTextField filePathTextField;
  private javax.swing.JTable geneListTable;
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JButton jButton3;
  private javax.swing.JButton jButton4;
  private javax.swing.JButton jButton6;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel40;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabel9;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JTabbedPane jTabbedPane2;
  private javax.swing.JTable jTable1;
  private javax.swing.JTextArea jTextArea1;
  private javax.swing.JTextField jTextField2;
  private javax.swing.JComboBox multiMethodComboBox;
  private javax.swing.JComboBox pvalueSoureComboBox;
  private javax.swing.JButton removeButton;
  private javax.swing.JButton retrieveGeneButton;
  private javax.swing.JTextField tpmTextField;
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
