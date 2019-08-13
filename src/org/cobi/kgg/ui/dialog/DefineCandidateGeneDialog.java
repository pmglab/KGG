/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import java.util.logging.Logger;
import org.cobi.kgg.business.entity.Constants;
import static org.cobi.kgg.business.entity.Constants.OPEN_CANDIDATE_GENE_PEDFILE;

import org.cobi.kgg.ui.ArrayListObjectArrayTableModel;
import org.cobi.kgg.ui.FileTextNode;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.text.LocalFile;
import org.cobi.util.text.StringArrayStringComparator;
import org.cobi.util.text.StringArrayStringFinder;
import org.openide.util.ImageUtilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jiang Li
 */
public class DefineCandidateGeneDialog extends javax.swing.JDialog implements Constants {

    /**
     * Creates new form DefineCandidateGeneDialog
     */
    List<Object[]> candidateGeneTableData = null;
    ArrayListObjectArrayTableModel candidateGeneTableModel = null;
    List<Object[]> inputGeneTableData = null;
    ArrayListObjectArrayTableModel inputGeneTableModel = null;
    List<Object[]> tissueGeneTableData = null;
    ArrayListObjectArrayTableModel tissueGeneTableModel = null;
    List<Object[]> omimGeneTableData = null;
    ArrayListObjectArrayTableModel omimGeneTableModel = null;
    String[] geneTabTitle = {"Source", "Symbol", "EntrezID", "Name", "Chromosome", "As Seed"};
    String[] tissueTabTitle = {"ID", "Name", "As Seed"};
    String[] omimTabTitle = {"OMIMID", "OMIM Description", "Source", "Symbol", "EntrezID", "Name", "Chromosome", "As Seed"};
    String[] keggTabTitle = {"KEGG Pathway", "Source", "Symbol", "EntrezID", "Name", "Chromosome", "As Seed"};
    String[] biocartaTabTitle = {"Biocarta Pathway", "Source", "Symbol", "EntrezID", "Name", "Chromosome", "ToInclude"};
    String[] ppiTabTitle = {"Interacting EntrezID", "Source", "Symbol", "EntrezID", "Name", "Chromosome", "ToInclude"};
    String[] tissueNames = {"721-BLymphoblasts", "Adipocytes", "Adrenal Cortex", "Adrenal Gland",
        "Amygdala", "Appendix", "Atrio Ventricular Node", "BM-CD105 Endothelial", "BM-CD33 Myeloid",
        "BM-CD34", "BM-CD71 Early Erythroid", "Bone Marrow", "Bronchial Epitelia", "Cardiac Myocytes",
        "Caudate Nucleus", "Cerebellum", "Cerebellum Peduncles", "Ciliary Ganglion",
        "Cingulate Cortex", "Dorsal Root Ganglia", "Extraocular Muscle", "Fetal Brain",
        "Fetal Heart", "Fetal Liver", "Fetal Lung", "Fetal Thyroid", "Globus Pallidus",
        "Human Bronchial Epithelial Cell", "Heart", "Hippocampus", "Hypothalamus", "Islet", "Kidney",
        "Limb Muscle", "Liver", "Lung", "Lymph Node", "Medulla Oblongata", "Occipital Lobe", "Olfactory Bulb",
        "Ovary", "Pancreas", "Parietal Lobe", "PB-BDCA4 Dentritic Cells", "PB-CD14 Monocytes",
        "PB-CD19 BCells", "PB-CD4 TCells", "PB-CD56N KCells", "PB-CD8 TCells", "Pituitary Gland",
        "Placenta", "Pons", "Prefrontal Cortex", "Prostate", "Psoas Muscle", "Salivary Gland", "Skin",
        "Smooth Muscle", "Spinal Cord", "Subthalamic Nucleus", "Temporal Lobe", "Testi Seminiferous Tubule",
        "Testis", "Testis Germ Cell", "Testis Intersitial", "Testis Leydig Cell", "Thalamus", "Thymus",
        "Thyroid", "Tongue", "Tonsil", "Trachea", "Trigeminal Ganglion", "Uterus", "Uterus Corpus",
        "Whole Blood", "Whole Brain"
    };
    ArrayList tissueTableData = null;
    ArrayListObjectArrayTableModel tissueTableModel = null;
    private final static Logger LOG = Logger.getLogger(DefineCandidateGeneDialog.class.getName());

    public DefineCandidateGeneDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        candidateGeneTableData = new ArrayList();
        candidateGeneTableModel = new ArrayListObjectArrayTableModel();
        candidateGeneTableModel.setTitle(geneTabTitle);
        candidateGeneTableModel.setDataList(candidateGeneTableData);

        inputGeneTableData = new ArrayList();
        inputGeneTableModel = new ArrayListObjectArrayTableModel();
        inputGeneTableModel.setTitle(geneTabTitle);
        inputGeneTableModel.setDataList(inputGeneTableData);

        tissueGeneTableData = new ArrayList();
        tissueGeneTableModel = new ArrayListObjectArrayTableModel();
        tissueGeneTableModel.setTitle(geneTabTitle);
        tissueGeneTableModel.setDataList(tissueGeneTableData);

        tissueTableData = new ArrayList();
        tissueTableModel = new ArrayListObjectArrayTableModel();
        tissueTableModel.setTitle(tissueTabTitle);
        tissueTableModel.setDataList(tissueTableData);

        omimGeneTableData = new ArrayList();
        omimGeneTableModel = new ArrayListObjectArrayTableModel();
        omimGeneTableModel.setTitle(omimTabTitle);
        omimGeneTableModel.setDataList(omimGeneTableData);
        initComponents();
        setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));
        for (int i = 0; i < tissueNames.length; i++) {
            Object[] row = {String.valueOf(i + 1), tissueNames[i], new Boolean(false)};
            tissueTableData.add(row);
        }

        jTable1.getColumnModel().getColumn(0).setPreferredWidth(30);
        tissueTableModel.fireTableDataChanged();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane1 = new javax.swing.JScrollPane();
    jTable1 = new javax.swing.JTable();
    jButton1 = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();
    candidateGeneSetNameTextField = new javax.swing.JTextField();
    jButton4 = new javax.swing.JButton();
    jButton3 = new javax.swing.JButton();
    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel1 = new javax.swing.JPanel();
    jPanel4 = new javax.swing.JPanel();
    geneFilePathTextField = new javax.swing.JTextField();
    jButton6 = new javax.swing.JButton();
    jButton7 = new javax.swing.JButton();
    geneTypeComboBox = new javax.swing.JComboBox();
    jScrollPane3 = new javax.swing.JScrollPane();
    inputGeneTextArea = new javax.swing.JTextArea();
    jScrollPane4 = new javax.swing.JScrollPane();
    jTable2 = new javax.swing.JTable();
    jButton8 = new javax.swing.JButton();
    jButton9 = new javax.swing.JButton();
    jButton11 = new javax.swing.JButton();
    jButton10 = new javax.swing.JButton();
    jPanel3 = new javax.swing.JPanel();
    jPanel6 = new javax.swing.JPanel();
    jScrollPane6 = new javax.swing.JScrollPane();
    jTable4 = new javax.swing.JTable();
    jButton17 = new javax.swing.JButton();
    jScrollPane7 = new javax.swing.JScrollPane();
    jTable5 = new javax.swing.JTable();
    jButton18 = new javax.swing.JButton();
    jButton19 = new javax.swing.JButton();
    jButton20 = new javax.swing.JButton();
    jButton21 = new javax.swing.JButton();
    jPanel2 = new javax.swing.JPanel();
    jPanel5 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    omimIDTextField = new javax.swing.JTextField();
    jLabel2 = new javax.swing.JLabel();
    omimNameTextField = new javax.swing.JTextField();
    jButton12 = new javax.swing.JButton();
    jScrollPane5 = new javax.swing.JScrollPane();
    jTable3 = new javax.swing.JTable();
    jButton13 = new javax.swing.JButton();
    jButton14 = new javax.swing.JButton();
    jButton15 = new javax.swing.JButton();
    jButton16 = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.title")); // NOI18N
    setPreferredSize(new java.awt.Dimension(925, 667));

    jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jScrollPane1.border.title"))); // NOI18N
    jScrollPane1.setViewportBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

    jTable1.setModel(candidateGeneTableModel);
    jScrollPane1.setViewportView(jTable1);

    jButton1.setLabel(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton1.label")); // NOI18N
    jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton1MouseClicked(evt);
      }
    });

    jButton2.setLabel(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton2.label")); // NOI18N
    jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton2MouseClicked(evt);
      }
    });
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton2ActionPerformed(evt);
      }
    });

    candidateGeneSetNameTextField.setText(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.candidateGeneSetNameTextField.text")); // NOI18N
    candidateGeneSetNameTextField.setName("candidateGeneSetNameTextField"); // NOI18N

    jButton4.setLabel(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton4.label")); // NOI18N
    jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton4MouseClicked(evt);
      }
    });

    jButton3.setLabel(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton3.label")); // NOI18N
    jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton3MouseClicked(evt);
      }
    });

    jTabbedPane1.setToolTipText(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jTabbedPane1.toolTipText")); // NOI18N
    jTabbedPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

    jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

    jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

    geneFilePathTextField.setText(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.geneFilePathTextField.text")); // NOI18N
    geneFilePathTextField.setName("geneFilePathTextField"); // NOI18N

    jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Folder.png"))); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(jButton6, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton6.text")); // NOI18N
    jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton6MouseClicked(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton7, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton7.text")); // NOI18N
    jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton7MouseClicked(evt);
      }
    });

    geneTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Gene Symbol", "Entrez Gene ID" }));
    geneTypeComboBox.setName("geneTypeComboBox"); // NOI18N

    jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jScrollPane3.border.title"))); // NOI18N

    inputGeneTextArea.setColumns(20);
    inputGeneTextArea.setRows(5);
    inputGeneTextArea.setName("inputGeneTextArea"); // NOI18N
    jScrollPane3.setViewportView(inputGeneTextArea);

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel4Layout.createSequentialGroup()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(jButton7))
              .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(geneFilePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addComponent(geneTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(0, 0, Short.MAX_VALUE))
          .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );
    jPanel4Layout.setVerticalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(geneTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(geneFilePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jButton6))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jButton7)
        .addContainerGap())
    );

    jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jScrollPane4.border.title"))); // NOI18N

    jTable2.setModel(inputGeneTableModel);
    jScrollPane4.setViewportView(jTable2);

    org.openide.awt.Mnemonics.setLocalizedText(jButton8, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton8.text")); // NOI18N
    jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton8MouseClicked(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton9, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton9.text")); // NOI18N
    jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton9MouseClicked(evt);
      }
    });
    jButton9.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton9ActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton11, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton11.text")); // NOI18N
    jButton11.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton11MouseClicked(evt);
      }
    });
    jButton11.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton11ActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton10, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton10.text")); // NOI18N
    jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton10MouseClicked(evt);
      }
    });
    jButton10.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton10ActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(jButton8)
            .addGap(94, 94, 94)
            .addComponent(jButton9)
            .addGap(93, 93, 93)
            .addComponent(jButton10)
            .addGap(114, 114, 114)
            .addComponent(jButton11)
            .addGap(70, 70, 70))))
    );

    jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton10, jButton11, jButton8, jButton9});

    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addGap(0, 0, Short.MAX_VALUE)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
              .addComponent(jButton8)
              .addComponent(jButton9)
              .addComponent(jButton10)
              .addComponent(jButton11))))
        .addContainerGap())
    );

    jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

    jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    jPanel3.setPreferredSize(new java.awt.Dimension(800, 358));

    jPanel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

    jScrollPane6.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jScrollPane6.border.title"))); // NOI18N

    jTable4.setModel(tissueTableModel);
    jScrollPane6.setViewportView(jTable4);

    org.openide.awt.Mnemonics.setLocalizedText(jButton17, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton17.text")); // NOI18N
    jButton17.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton17MouseClicked(evt);
      }
    });

    javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
    jPanel6.setLayout(jPanel6Layout);
    jPanel6Layout.setHorizontalGroup(
      jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel6Layout.createSequentialGroup()
        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel6Layout.createSequentialGroup()
            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(jButton17)))
        .addContainerGap())
    );
    jPanel6Layout.setVerticalGroup(
      jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel6Layout.createSequentialGroup()
        .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jButton17)
        .addContainerGap())
    );

    jScrollPane7.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jScrollPane7.border.title"))); // NOI18N

    jTable5.setModel(tissueGeneTableModel);
    jScrollPane7.setViewportView(jTable5);

    org.openide.awt.Mnemonics.setLocalizedText(jButton18, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton18.text")); // NOI18N
    jButton18.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton18MouseClicked(evt);
      }
    });
    jButton18.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton18ActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton19, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton19.text")); // NOI18N
    jButton19.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton19MouseClicked(evt);
      }
    });
    jButton19.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton19ActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton20, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton20.text")); // NOI18N
    jButton20.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton20MouseClicked(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton21, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton21.text")); // NOI18N
    jButton21.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton21MouseClicked(evt);
      }
    });

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 548, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(jButton18)
          .addComponent(jButton19)
          .addComponent(jButton20)
          .addComponent(jButton21))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton18, jButton19, jButton20, jButton21});

    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
              .addContainerGap()
              .addComponent(jButton18)
              .addGap(78, 78, 78)
              .addComponent(jButton19)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(jButton20)
              .addGap(81, 81, 81)
              .addComponent(jButton21))
            .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

    jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

    jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jPanel5.border.title"))); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jLabel1.text")); // NOI18N

    omimIDTextField.setText(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.omimIDTextField.text")); // NOI18N
    omimIDTextField.setName("omimIDTextField"); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jLabel2.text")); // NOI18N

    omimNameTextField.setText(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.omimNameTextField.text")); // NOI18N
    omimNameTextField.setName("omimNameTextField"); // NOI18N
    omimNameTextField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        omimNameTextFieldActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton12, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton12.text")); // NOI18N
    jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton12MouseClicked(evt);
      }
    });

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(omimIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(omimNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(48, 48, 48)
        .addComponent(jButton12)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel5Layout.setVerticalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jButton12)
          .addComponent(omimNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel2)
          .addComponent(omimIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(0, 4, Short.MAX_VALUE))
    );

    jScrollPane5.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jScrollPane5.border.title"))); // NOI18N

    jTable3.setModel(omimGeneTableModel);
    jScrollPane5.setViewportView(jTable3);

    org.openide.awt.Mnemonics.setLocalizedText(jButton13, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton13.text")); // NOI18N
    jButton13.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton13MouseClicked(evt);
      }
    });
    jButton13.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton13ActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton14, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton14.text")); // NOI18N
    jButton14.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton14MouseClicked(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton15, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton15.text")); // NOI18N
    jButton15.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton15MouseClicked(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButton16, org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jButton16.text")); // NOI18N
    jButton16.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jButton16MouseClicked(evt);
      }
    });

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 782, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
          .addComponent(jButton14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jButton15)
          .addComponent(jButton16))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addContainerGap())
    );

    jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton13, jButton14, jButton15, jButton16});

    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addGap(41, 41, 41)
            .addComponent(jButton13)
            .addGap(18, 18, 18)
            .addComponent(jButton14)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
            .addComponent(jButton15)
            .addGap(18, 18, 18)
            .addComponent(jButton16)
            .addContainerGap())
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
    );

    jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DefineCandidateGeneDialog.class, "DefineCandidateGeneDialog.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(37, 37, 37)
        .addComponent(jButton1)
        .addGap(48, 48, 48)
        .addComponent(jButton2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(candidateGeneSetNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(100, 100, 100)
        .addComponent(jButton3)
        .addGap(67, 67, 67)
        .addComponent(jButton4)
        .addGap(90, 90, 90))
      .addComponent(jScrollPane1)
      .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jButton1)
          .addComponent(jButton2)
          .addComponent(candidateGeneSetNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jButton3)
          .addComponent(jButton4))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO add your handling code here:
        removeCandidateGene();
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        // TODO add your handling code here:
        setOkay();
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
        // TODO add your handling code here:
        allAsSeedGene();
    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        // TODO add your handling code here:
        nonAsSeedCandidateGene();
    }//GEN-LAST:event_jButton3MouseClicked

    private void omimNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_omimNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_omimNameTextFieldActionPerformed

    private void jButton12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton12MouseClicked
        // TODO add your handling code here:
        searchOMIMGenes();
    }//GEN-LAST:event_jButton12MouseClicked

    private void jButton13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton13MouseClicked
        // TODO add your handling code here:
        nonAsSeedInputGene();
    }//GEN-LAST:event_jButton13MouseClicked

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton14MouseClicked
        // TODO add your handling code here:
        allAsSeedOmimGenes();
    }//GEN-LAST:event_jButton14MouseClicked

    private void jButton15MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton15MouseClicked
        // TODO add your handling code here:
        removeOMIMGene();
    }//GEN-LAST:event_jButton15MouseClicked

    private void jButton16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton16MouseClicked
        // TODO add your handling code here:
        addOMIMCandidateGenes();
    }//GEN-LAST:event_jButton16MouseClicked

    private void jButton17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton17MouseClicked
        // TODO add your handling code here:
        selectTissueGenes();
    }//GEN-LAST:event_jButton17MouseClicked

    private void jButton18MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton18MouseClicked
        // TODO add your handling code here:
        nonAsSeedTissueGenes();
    }//GEN-LAST:event_jButton18MouseClicked

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jButton19MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton19MouseClicked
        // TODO add your handling code here:
        allAsSeedTissueGenes();
    }//GEN-LAST:event_jButton19MouseClicked

    private void jButton20MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton20MouseClicked
        // TODO add your handling code here:
        removeTissueSpecificGene();
    }//GEN-LAST:event_jButton20MouseClicked

    private void jButton21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton21MouseClicked
        // TODO add your handling code here:
        addTissueCandidateGenes();
    }//GEN-LAST:event_jButton21MouseClicked

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton11MouseClicked
        // TODO add your handling code here:
        addInputCandidateGenes();
    }//GEN-LAST:event_jButton11MouseClicked

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton10MouseClicked
        // TODO add your handling code here:
        removeInputtedGene();
    }//GEN-LAST:event_jButton10MouseClicked

    private void jButton9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton9MouseClicked
        // TODO add your handling code here:
        allAsSeedInputGenes();
    }//GEN-LAST:event_jButton9MouseClicked

    private void jButton8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton8MouseClicked
        // TODO add your handling code here:
        nonAsSeedInputGene();
    }//GEN-LAST:event_jButton8MouseClicked

    private void jButton7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton7MouseClicked
        // TODO add your handling code here:
        addSelectGene();
    }//GEN-LAST:event_jButton7MouseClicked

    private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
        // TODO add your handling code here:
        loadGenes();
    }//GEN-LAST:event_jButton6MouseClicked

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton19ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    public void removeOMIMGene() {
        int[] slectedIndex = jTable3.getSelectedRows();
        if (slectedIndex.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select the items you want to remove", "Message", JOptionPane.WARNING_MESSAGE);
            return;
        }
        //candidateGeneTableData
        for (int i = slectedIndex.length - 1; i >= 0; i--) {
            omimGeneTableData.remove(slectedIndex[i]);
        }
        omimGeneTableModel.fireTableDataChanged();
    }

    public void removeTissueSpecificGene() {
        int[] slectedIndex = jTable4.getSelectedRows();
        if (slectedIndex.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select the items you want to remove", "Message", JOptionPane.WARNING_MESSAGE);
            return;
        }
        //candidateGeneTableData
        for (int i = slectedIndex.length - 1; i >= 0; i--) {
            tissueGeneTableData.remove(slectedIndex[i]);
        }
        tissueGeneTableModel.fireTableDataChanged();
    }

    public void removeCandidateGene() {
        int[] slectedIndex = jTable1.getSelectedRows();
        if (slectedIndex.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select the items you want to remove", "Message", JOptionPane.WARNING_MESSAGE);
            return;
        }
        //candidateGeneTableData
        for (int i = slectedIndex.length - 1; i >= 0; i--) {
            candidateGeneTableData.remove(slectedIndex[i]);
        }
        candidateGeneTableModel.fireTableDataChanged();
    }

    public void removeInputtedGene() {
        int[] selectedIndex = jTable2.getSelectedRows();
        if (selectedIndex.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select the items you want to remove", "Message", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (int i = selectedIndex.length - 1; i >= 0; i--) {
            inputGeneTableData.remove(selectedIndex[i]);
        }
        inputGeneTableModel.fireTableDataChanged();
    }

    public void nonAsSeedInputGene() {
        for (int i = inputGeneTableData.size() - 1; i >= 0; i--) {
            inputGeneTableData.get(i)[5] = new Boolean(false);
        }
        inputGeneTableModel.fireTableDataChanged();
    }

    public void nonAsSeedOmimGenes() {
        for (int i = omimGeneTableData.size() - 1; i >= 0; i--) {
            omimGeneTableData.get(i)[7] = new Boolean(false);
        }
        omimGeneTableModel.fireTableDataChanged();
    }

    public void nonAsSeedTissueGenes() {
        for (int i = tissueGeneTableData.size() - 1; i >= 0; i--) {
            tissueGeneTableData.get(i)[5] = new Boolean(false);
        }
        tissueGeneTableModel.fireTableDataChanged();
    }

    public void nonAsSeedCandidateGene() {
        for (int i = candidateGeneTableData.size() - 1; i >= 0; i--) {
            candidateGeneTableData.get(i)[5] = false;
        }
        candidateGeneTableModel.fireTableDataChanged();
    }

    public void allAsSeedInputGenes() {
        for (int i = inputGeneTableData.size() - 1; i >= 0; i--) {
            inputGeneTableData.get(i)[5] = new Boolean(true);
        }
        inputGeneTableModel.fireTableDataChanged();
    }

    public void allAsSeedOmimGenes() {
        for (int i = omimGeneTableData.size() - 1; i >= 0; i--) {
            omimGeneTableData.get(i)[7] = new Boolean(true);
        }
        omimGeneTableModel.fireTableDataChanged();
    }

    public void allAsSeedTissueGenes() {
        for (int i = tissueGeneTableData.size() - 1; i >= 0; i--) {
            tissueGeneTableData.get(i)[5] = true;
        }
        tissueGeneTableModel.fireTableDataChanged();
    }

    public void allAsSeedGene() {
        for (int i = candidateGeneTableData.size() - 1; i >= 0; i--) {
            candidateGeneTableData.get(i)[5] = true;
        }
        candidateGeneTableModel.fireTableDataChanged();
    }

    public void addInputCandidateGenes() {
        int geneNum = inputGeneTableData.size();
        int i;
        Object[] row = null;
        for (i = 0; i < geneNum; i++) {
            row = (Object[]) inputGeneTableData.get(i);
            candidateGeneTableData.add(row);
        }
        candidateGeneTableModel.fireTableDataChanged();

        for (i = geneNum - 1; i >= 0; i--) {
            row = (Object[]) inputGeneTableData.get(i);
            inputGeneTableData.remove(i);
        }
        inputGeneTableModel.fireTableDataChanged();
    }

    public void addOMIMCandidateGenes() {
        int geneNum = omimGeneTableData.size();
        int i;
        Object[] row = null;
        Object[] row1 = null;
        for (i = 0; i < geneNum; i++) {
            row = (Object[]) omimGeneTableData.get(i);

            row1 = Arrays.copyOfRange(row, 2, 8);
            candidateGeneTableData.add(row1);

        }
        candidateGeneTableModel.fireTableDataChanged();

        for (i = geneNum - 1; i >= 0; i--) {
            row = (Object[]) omimGeneTableData.get(i);
            omimGeneTableData.remove(i);
        }
        omimGeneTableModel.fireTableDataChanged();
    }

    public void addTissueCandidateGenes() {
        int geneNum = tissueGeneTableData.size();
        int i;
        Object[] row = null;
        for (i = 0; i < geneNum; i++) {
            row = (Object[]) tissueGeneTableData.get(i);
            candidateGeneTableData.add(row);
        }
        candidateGeneTableModel.fireTableDataChanged();

        for (i = geneNum - 1; i >= 0; i--) {
            row = (Object[]) tissueGeneTableData.get(i);
            tissueGeneTableData.remove(i);
        }
        tissueGeneTableModel.fireTableDataChanged();
    }

    public void loadGenes() {
        String path = null;
        JFileChooser fDialog = null;
        if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
            fDialog = new JFileChooser(GlobalManager.lastAccessedPath);
        } else {
            fDialog = new JFileChooser();
        }
        fDialog.setDialogTitle(OPEN_CANDIDATE_GENE_PEDFILE);
        boolean inputSymbol = true;
        if (((String) geneTypeComboBox.getSelectedItem()).equals("Entrez Gene ID")) {
            inputSymbol = false;
        }

        int result = fDialog.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fDialog.getSelectedFile();
                path = file.getCanonicalPath();
                geneFilePathTextField.setText(path);
                LOG.info("Load candidate gene file: " + path);
                // GlobalManager.addInforLog("Load candidate gene file: " + path);
                int[] indices = {1, 7, 2, 4};
                HashSet<String> loadedGeneList = new HashSet<String>();
                LocalFile.retrieveData(path, loadedGeneList);
                ArrayList<String[]> allGenes = new ArrayList<String[]>();
                if (inputSymbol) {
                    LocalFile.retrieveData(GlobalManager.RESOURCE_PATH + "HgncGene.txt.gz",
                            allGenes, indices, loadedGeneList, 1, "\t");
                } else {
                    LocalFile.retrieveData(GlobalManager.RESOURCE_PATH + "HgncGene.txt.gz",
                            allGenes, indices, loadedGeneList, 7, "\t");
                }
                for (int i = 0; i < allGenes.size(); i++) {
                    Object[] row = new Object[6];
                    row[0] = "Input";
                    for (int j = 1; j < 5; j++) {
                        row[j] = allGenes.get(i)[j - 1];
                    }
                    row[5] = new Boolean(true);
                    inputGeneTableData.add(row);
                }
                inputGeneTableModel.fireTableDataChanged();

                //GlobalManager.mainView.insertBriefRunningInfor(allGenes.size() + " genes loaded", true);
                //GlobalManager.addInforLog(allGenes.size() + " genes loaded");
                LOG.info(allGenes.size() + " genes loaded");

                boolean hasNoApproved = false;
                if (loadedGeneList.size() != allGenes.size()) {
                    StringBuilder info = new StringBuilder();
                    if (inputSymbol) {
                        info.append("The following symbol(s) may not be officially approved, please check on \"http://www.genenames.org/\":\n");
                        //check genes have no official symbols
                        Collections.sort(allGenes, new StringArrayStringComparator(0));
                        StringArrayStringFinder finder = new StringArrayStringFinder(0);
                        Iterator<String> iter = loadedGeneList.iterator();
                        while (iter.hasNext()) {
                            String tmpStr = iter.next();
                            int pos = Collections.binarySearch(allGenes, tmpStr, finder);
                            if (pos < 0) {
                                hasNoApproved = true;
                                info.append(tmpStr);
                                info.append(", ");
                            }
                        }
                    } else {
                        info.append("The following Entrez Gene ID may not blong to human!, please check on \"http://www.ncbi.nlm.nih.gov\":\n");
                        //check genes have no official symbols
                        Collections.sort(allGenes, new StringArrayStringComparator(1));
                        StringArrayStringFinder finder = new StringArrayStringFinder(1);
                        Iterator<String> iter = loadedGeneList.iterator();
                        while (iter.hasNext()) {
                            String tmpStr = iter.next();
                            int pos = Collections.binarySearch(allGenes, tmpStr, finder);
                            if (pos < 0) {
                                hasNoApproved = true;
                                info.append(tmpStr);
                                info.append(", ");
                            }
                        }
                    }

                    if (hasNoApproved) {
                        info.delete(info.length() - 2, info.length());
                        //info.append('\n');
                        // GlobalManager.mainView.insertBriefRunningInfor(info.toString(), true);
                        //GlobalManager.addInforLog(info.toString());
                        LOG.info(info.toString());
                        JOptionPane.showMessageDialog(this, info.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(this, FILE_OPEN_CANCELLED, "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void addSelectGene() {
        inputGeneTableData.clear();
        String geneText = inputGeneTextArea.getText();
        String[] inputGenes = geneText.split("\n");
        if (inputGenes.length <= 0) {
            return;
        }

        for (int i = 0; i < inputGenes.length; i++) {
            inputGenes[i] = inputGenes[i].trim();
        }
        boolean inputSymbol = true;
        if (((String) geneTypeComboBox.getSelectedItem()).equals("Entrez Gene ID")) {
            inputSymbol = false;
        }
        try {
            int[] indices = {1, 7, 2, 4};
            ArrayList<String[]> allGenes = new ArrayList<String[]>();
            if (inputSymbol) {
                LocalFile.retrieveData(GlobalManager.RESOURCE_PATH + "HgncGene.txt.gz", allGenes, indices, inputGenes, 1, "\t");
            } else {
                LocalFile.retrieveData(GlobalManager.RESOURCE_PATH + "HgncGene.txt.gz", allGenes, indices, inputGenes, 7, "\t");
            }
            for (int i = 0; i < allGenes.size(); i++) {
                Object[] row = new Object[6];
                row[0] = "Input";
                for (int j = 1; j < 5; j++) {
                    row[j] = allGenes.get(i)[j - 1];
                }
                row[5] = new Boolean(true);
                inputGeneTableData.add(row);
            }
            inputGeneTableModel.fireTableDataChanged();

            //GlobalManager.mainView.insertBriefRunningInfor(allGenes.size() + " Genes Loaded", true);
            // GlobalManager.addInforLog(allGenes.size() + " Genes Loaded");
            LOG.info(allGenes.size() + " Genes Loaded");
            boolean hasNoApproved = false;

            if (inputGenes.length != allGenes.size()) {
                StringBuffer info = new StringBuffer();
                if (inputSymbol) {
                    info.append("The following symbol(s) may not be officially approved, please check on \"http://www.genenames.org/\":\n");
                    //check genes have no official symbols
                    Collections.sort(allGenes, new StringArrayStringComparator(0));
                    StringArrayStringFinder finder = new StringArrayStringFinder(0);
                    for (int i = 0; i < inputGenes.length; i++) {
                        int pos = Collections.binarySearch(allGenes, inputGenes[i], finder);
                        if (pos < 0) {
                            hasNoApproved = true;
                            info.append(inputGenes[i]);
                            info.append(", ");
                        }
                    }
                } else {
                    info.append("The following Entrez Gene ID may not blong to human!, please check on \"http://www.ncbi.nlm.nih.gov\":\n");
                    //check genes have no official symbols
                    Collections.sort(allGenes, new StringArrayStringComparator(1));
                    StringArrayStringFinder finder = new StringArrayStringFinder(1);
                    for (int i = 0; i < inputGenes.length; i++) {
                        int pos = Collections.binarySearch(allGenes, inputGenes[i], finder);
                        if (pos < 0) {
                            hasNoApproved = true;
                            info.append(inputGenes[i]);
                            info.append(", ");
                        }
                    }
                }
                if (hasNoApproved) {
                    info.delete(info.length() - 2, info.length());
                    //info.append('\n');
                    //GlobalManager.mainView.insertBriefRunningInfor(info.toString(), true);
                    //GlobalManager.addInforLog(info.toString());
                    LOG.info(info.toString());
                    JOptionPane.showMessageDialog(this, info.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
                }

            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void searchOMIMGenes() {
        try {
            omimGeneTableData.clear();
            String idSearchItems = omimIDTextField.getText();
            HashSet<String> idKeywords = new HashSet<String>();
            StringTokenizer st = new StringTokenizer(idSearchItems, ", ");
            while (st.hasMoreTokens()) {
                idKeywords.add(st.nextToken().trim());
            }
            String nameSearchItems = omimNameTextField.getText();
            List<String> nameKeywords = new ArrayList<String>();

            nameSearchItems = nameSearchItems.toUpperCase();
            //ignor the insensitive word
            nameSearchItems = nameSearchItems.replaceAll("DISEASE", "");

            st = new StringTokenizer(nameSearchItems, ", ");
            while (st.hasMoreTokens()) {
                nameKeywords.add(st.nextToken().trim());
            }
            int nameKeywordsLen = nameKeywords.size();

            // gene information
            int[] indices = {1, 7, 2, 4};
            ArrayList<String[]> allGenes = new ArrayList<String[]>();

            LocalFile.retrieveData(GlobalManager.RESOURCE_PATH + "/HgncGene.txt.gz", allGenes, indices, "\t");
            Collections.sort(allGenes, new StringArrayStringComparator(0));
            StringArrayStringFinder geneIDFinder = new StringArrayStringFinder(0);

            LineReader br = new CompressedFileReader(new File(GlobalManager.RESOURCE_PATH + "/OMIMGenes.txt.gz"));
            String line = null;
            String[] cells = null;


            int pos;
            boolean hasHit = false;
            while ((line = br.readLine()) != null) {
                //line = line.trim();
                if (line.trim().length() == 0) {
                    continue;
                }
                hasHit = false;
                cells = line.split("\t", -1);
                if (idKeywords.contains(cells[2])) {
                    hasHit = true;
                } else {
                    String upperCaseName = cells[3].toUpperCase();
                    for (int i = 0; i < nameKeywordsLen; i++) {
                        if (upperCaseName.indexOf(nameKeywords.get(i)) >= 0) {
                            hasHit = true;
                            break;
                        }
                    }
                }

                if (hasHit) {
                    pos = Collections.binarySearch(allGenes, cells[1], geneIDFinder);
                    if (pos >= 0) {
                        Object[] row = new Object[8];
                        row[0] = cells[2];
                        row[1] = cells[3];
                        row[2] = "OMIM";
                        for (int j = 3; j < 7; j++) {
                            row[j] = allGenes.get(pos)[j - 3];
                        }
                        row[7] = new Boolean(true);
                        omimGeneTableData.add(row);
                    }
                }
            }
            omimGeneTableModel.fireTableDataChanged();
            JOptionPane.showMessageDialog(this, omimGeneTableData.size() + " OMIM genes selected", "Message", JOptionPane.INFORMATION_MESSAGE);
            br.close();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setOkay() {
        try {
            String setName = candidateGeneSetNameTextField.getText();
            if (GlobalManager.currentProject.isAvailableCandiGeneFile(setName)) {
                JOptionPane.showMessageDialog(this, "Gene Set Name is available!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            setVisible(false);
            String prjName = GlobalManager.getCurrentProject().getName();
            String prjWorkingPath = GlobalManager.getCurrentProject().getWorkingPath();
            File candidateGenePath = new File(prjWorkingPath + File.separator + prjName + File.separator
                    + "candidate_genes" + File.separator);
            LocalFileFunc.makeStorageLoc(candidateGenePath.getAbsolutePath());
            String setFileName = candidateGeneSetNameTextField.getText();
            FileTextNode candidateGeneFile = new FileTextNode(candidateGenePath.getCanonicalPath() + File.separator + setFileName);
            LocalFile.writeObject2Text(candidateGeneFile.getCanonicalPath(), candidateGeneTableData, "\t");

            if (candidateGeneFile.exists()) {
                GlobalManager.currentProject.addCandiGeneFile(candidateGeneFile);
                //GlobalManager.originalAssociationFilesModel.addElement(new FileTextNode(file.getCanonicalPath()));
                // GlobalManager.mainFrame.addCandidateGenesNode(candidateGeneFile);//mainView->mainFrame
                GlobalManager.candiGeneFilesModel.addElement(candidateGeneFile);
            }

            String info = candidateGeneTableData.size() + " candidate genes saved in ";
            //JOptionPane.showMessageDialog(GlobalManager.currentApplication.getMainFrame(), info, "Message", JOptionPane.INFORMATION_MESSAGE);
            //GlobalManager.mainView.insertBriefRunningInfor(info + candidateGeneFile.toString(), true);
            //GlobalManager.addInforLog(info + candidateGeneFile.toString());
            JOptionPane.showMessageDialog(this, info, "Message", JOptionPane.INFORMATION_MESSAGE);
            LOG.info(info + candidateGeneFile.toString());

            candidateGeneTableData.clear();
            candidateGeneTableModel.fireTableDataChanged();
            ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
            projTopComp.showProject(GlobalManager.currentProject);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void selectTissueGenes() {
        Object[] row = null;

        int i, tissueNum = tissueTableData.size();
        int geneNum = 0;
        HashSet<String> selectedTissue = new HashSet<String>();
        //get selected tissue
        for (i = 0; i < tissueNum; i++) {
            row = (Object[]) tissueTableData.get(i);
            if (((Boolean) row[2]).booleanValue()) {
                selectedTissue.add((String) row[1]);
            }
        }

        if (selectedTissue.size() == 0) {
            return;
        }

        try {
            //get genes of selected tissue
            int[] tissueIndices = {1};
            List<String[]> selectedGenes = new ArrayList<String[]>();
            LocalFile.retrieveData(GlobalManager.RESOURCE_PATH + "/TissueSpecificGenes.txt.gz",
                    selectedGenes, tissueIndices, selectedTissue, 0, "\t");
            geneNum = selectedGenes.size();
            HashSet<String> selectedGene = new HashSet<String>();
            for (i = 0; i < geneNum; i++) {
                selectedGene.add(selectedGenes.get(i)[0]);
            }

            //get gene information in detail
            int[] indices = {1, 7, 2, 4};
            List<String[]> allGenes = new ArrayList<String[]>();

            LocalFile.retrieveData(GlobalManager.RESOURCE_PATH + "HgncGene.txt.gz",
                    allGenes, indices, selectedGene, 1, "\t");
            geneNum = allGenes.size();
            tissueGeneTableData.clear();
            for (i = 0; i < geneNum; i++) {
                row = new Object[6];
                row[0] = "Tissue Specific";
                for (int j = 1; j < 5; j++) {
                    row[j] = allGenes.get(i)[j - 1];
                }

                row[5] = new Boolean(false);
                tissueGeneTableData.add(row);
            }
            tissueGeneTableModel.fireTableDataChanged();
            JOptionPane.showMessageDialog(this, tissueGeneTableData.size() + " Tissue Specific genes selected", "Message", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DefineCandidateGeneDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DefineCandidateGeneDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DefineCandidateGeneDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DefineCandidateGeneDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DefineCandidateGeneDialog dialog = new DefineCandidateGeneDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextField candidateGeneSetNameTextField;
  private javax.swing.JTextField geneFilePathTextField;
  private javax.swing.JComboBox geneTypeComboBox;
  private javax.swing.JTextArea inputGeneTextArea;
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton10;
  private javax.swing.JButton jButton11;
  private javax.swing.JButton jButton12;
  private javax.swing.JButton jButton13;
  private javax.swing.JButton jButton14;
  private javax.swing.JButton jButton15;
  private javax.swing.JButton jButton16;
  private javax.swing.JButton jButton17;
  private javax.swing.JButton jButton18;
  private javax.swing.JButton jButton19;
  private javax.swing.JButton jButton2;
  private javax.swing.JButton jButton20;
  private javax.swing.JButton jButton21;
  private javax.swing.JButton jButton3;
  private javax.swing.JButton jButton4;
  private javax.swing.JButton jButton6;
  private javax.swing.JButton jButton7;
  private javax.swing.JButton jButton8;
  private javax.swing.JButton jButton9;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JPanel jPanel6;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JScrollPane jScrollPane5;
  private javax.swing.JScrollPane jScrollPane6;
  private javax.swing.JScrollPane jScrollPane7;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JTable jTable1;
  private javax.swing.JTable jTable2;
  private javax.swing.JTable jTable3;
  private javax.swing.JTable jTable4;
  private javax.swing.JTable jTable5;
  private javax.swing.JTextField omimIDTextField;
  private javax.swing.JTextField omimNameTextField;
  // End of variables declaration//GEN-END:variables
}
