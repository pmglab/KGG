/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.dialog;

import cern.colt.list.IntArrayList;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.cobi.kgg.business.entity.Constants;
import static org.cobi.kgg.business.entity.Constants.CHROM_NAMES;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.PValueFileSetting;
import org.cobi.kgg.business.entity.PlinkDataset;

import org.cobi.kgg.ui.ArrayListStringArrayTableModel;
import org.cobi.kgg.ui.FileTextNode;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.LinkLabel;
import org.cobi.kgg.ui.action.BuildGenome;
import org.cobi.kgg.ui.action.ShowGeneBasedAssocScanDialogAction;
import org.cobi.kgg.ui.action.ShowMultivarGenebasedScanAction;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.text.LocalFile;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author MXLi
 */
public class BuildAnalysisGenomeByPositionDialog extends javax.swing.JDialog implements Constants {

    private ArrayList<String[]> listTableData = null;
    private ArrayListStringArrayTableModel listTableModel = null;
    List<Integer> pvalueColum = null;
    DefaultListModel<String> selectedRegionListModel = new DefaultListModel<String>();
    DefaultComboBoxModel<String> regionsModel = new DefaultComboBoxModel<String>();
    DefaultComboBoxModel<String> pvalueTitleBox = new DefaultComboBoxModel<String>();
    Map<String, IntArrayList> selectedRegionMap = new HashMap<String, IntArrayList>();
    List<Double> corrList = new ArrayList<Double>();
    private final static Logger LOG = Logger.getLogger(BuildAnalysisGenomeByPositionDialog.class.getName());

    /**
     * Creates new form BuildAnalysisGenomeByPositionDialog
     */
    public BuildAnalysisGenomeByPositionDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        listTableData = new ArrayList<String[]>();
        listTableModel = new ArrayListStringArrayTableModel();
        pvalueColum = new ArrayList<Integer>();
        for (String CHROM_NAMES1 : CHROM_NAMES) {
            regionsModel.addElement(CHROM_NAMES1);
        }

        initComponents();
        setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));

        infoLogicComboBox.setVisible(false);
        infoCutTextField.setVisible(false);

    }

    class OpenFileSwingWorker extends SwingWorker<Void, String> {

        private final int NUM = 100;
        int runningThread = 0;
        boolean finished = false;
        String[] inFileTitles = null;

        public OpenFileSwingWorker() {

        }

        @Override
        protected Void doInBackground() {
            try {
                listTableData.clear();
                int selectedIndex = originalAssociationFilesComboBox.getSelectedIndex();
                File f = (File) originalAssociationFilesComboBox.getItemAt(selectedIndex);
                if (f != null) {
                    ArrayList<String[]> content = new ArrayList<String[]>();
                    int rowNum = 8;
                    LocalFile.retrieveData(f.getCanonicalPath(), content, rowNum, null, null, true, 1024);
                    if (content.isEmpty()) {
                        return null;
                    }
                    listTableModel.setTitle(content.get(0));
                    for (int i = 1; i < content.size(); i++) {
                        listTableData.add(content.get(i));
                    }
                    inFileTitles = content.get(0);
                    listTableModel.setDataList(listTableData);
                    finished = true;

                }
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
            return null;
        }

        @Override
        protected void process(List<String> chunks) {

        }

        @Override
        protected void done() {
            try {
                if (finished) {

                    listTableModel.fireTableStructureChanged();
                    chromComboBox.removeAllItems();
                    markerIDComboBox.removeAllItems();
                    markerPositionComboBox.removeAllItems();
                    testNameComboBox.removeAllItems();
                    pvalueTitleBox.removeAllElements();
                    imputationQualComboBox.removeAllItems();
                    imputationQualComboBox.addItem("(Optional)");
                    for (String inFileTitle : inFileTitles) {
                        chromComboBox.addItem(inFileTitle);
                        markerIDComboBox.addItem(inFileTitle);
                        testNameComboBox.addItem(inFileTitle);
                        markerPositionComboBox.addItem(inFileTitle);
                        pvalueTitleBox.addElement(inFileTitle);
                        imputationQualComboBox.addItem(inFileTitle);
                    }
                    markerIDComboBox.setSelectedIndex(1);
                    markerPositionComboBox.setSelectedIndex(2);

                    formatComboBoxActionPerformed(null);
                    inputTypeComboBoxActionPerformed(null);
                }

            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelButton = new javax.swing.JButton();
        buildGenomeButton = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        markerFileTable = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        titleList = new javax.swing.JList<>();
        jLabel16 = new javax.swing.JLabel();
        genomeNameTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        toGCCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        lowLDR2TextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        customGeneFilePath = new javax.swing.JTextField();
        geneDBComboBox = new javax.swing.JComboBox();
        exampleFormatButton = new javax.swing.JButton();
        customGeneButton = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        exampleFormatButton1 = new javax.swing.JButton();
        variantFilePath = new javax.swing.JTextField();
        gvMapButton1 = new javax.swing.JButton();
        geneDBCheckBox = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        length5PTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        p3LenTextField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        originalAssociationFilesComboBox = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        regionsComboBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        fromTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        toTextField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        addRegionButton = new javax.swing.JButton();
        removeRegionButton = new javax.swing.JButton();
        excludeWholeChrButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        selectedRegionList = new javax.swing.JList<>();
        jLabel8 = new javax.swing.JLabel();
        regiontypeComboBox = new javax.swing.JComboBox();
        jPanel9 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        chromComboBox = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        markerPositionComboBox = new javax.swing.JComboBox<>();
        inputTypeComboBox = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        formatComboBox = new javax.swing.JComboBox<>();
        testNameComboBox = new javax.swing.JComboBox<>();
        testNameLabel = new javax.swing.JLabel();
        dfLabel = new javax.swing.JLabel();
        dfTextField = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        missingValueTextField = new javax.swing.JTextField();
        hasTitleLineCheckBox = new javax.swing.JCheckBox();
        jLabel28 = new javax.swing.JLabel();
        pValueGenomeVersionComboBox = new javax.swing.JComboBox<>();
        jLabel31 = new javax.swing.JLabel();
        markerIDComboBox = new javax.swing.JComboBox<>();
        jLabel2 = new LinkLabel ("No positions?! Get positions of SNPs by SNPTracker", "http://grass.cgs.hku.hk/snptracker/");
        jLabel7 = new javax.swing.JLabel();
        imputationQualComboBox = new javax.swing.JComboBox<>();
        infoLogicComboBox = new javax.swing.JComboBox();
        infoCutTextField = new javax.swing.JTextField();
        ldfilesTabbedPane = new javax.swing.JTabbedPane();
        machHapVCFPanel = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        machHaplotypeGenomeVersionComboBox = new javax.swing.JComboBox<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        ldMachVCFDataFileList = new javax.swing.JList<>();
        jButton6 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel40 = new LinkLabel ("Download", "http://grass.cgs.hku.hk/limx/kgg/phasedgty.html");
        plinkGtyPanel = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        pedigreeFilePathTextField = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        mapFilePathTextField = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        bedFilePathTextField = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        defineCorrCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.title")); // NOI18N
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buildGenomeButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        buildGenomeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/24x24/arrow-right-4.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildGenomeButton, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.buildGenomeButton.text")); // NOI18N
        buildGenomeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildGenomeButtonActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel10.text")); // NOI18N

        jPanel8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        markerFileTable.setModel(listTableModel);
        markerFileTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(markerFileTable);

        titleList.setModel(pvalueTitleBox);
        jScrollPane3.setViewportView(titleList);

        jLabel16.setBackground(new java.awt.Color(204, 255, 204));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel16.text")); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 829, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        genomeNameTextField.setText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.genomeNameTextField.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        org.openide.awt.Mnemonics.setLocalizedText(toGCCheckBox, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.toGCCheckBox.text")); // NOI18N
        toGCCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        toGCCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel1.text")); // NOI18N

        lowLDR2TextField.setText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.lowLDR2TextField.text")); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jPanel2.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel3.text")); // NOI18N

        customGeneFilePath.setEditable(false);
        customGeneFilePath.setText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.customGeneFilePath.text")); // NOI18N

        geneDBComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "RefGene", "GEncode", "Customized" }));
        geneDBComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geneDBComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(exampleFormatButton, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.exampleFormatButton.text")); // NOI18N
        exampleFormatButton.setEnabled(false);
        exampleFormatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exampleFormatButtonActionPerformed(evt);
            }
        });

        customGeneButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Folder.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(customGeneButton, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.customGeneButton.text")); // NOI18N
        customGeneButton.setEnabled(false);
        customGeneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customGeneButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel13.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(exampleFormatButton1, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.exampleFormatButton1.text")); // NOI18N
        exampleFormatButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exampleFormatButton1ActionPerformed(evt);
            }
        });

        variantFilePath.setText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.variantFilePath.text")); // NOI18N

        gvMapButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Folder.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(gvMapButton1, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.gvMapButton1.text")); // NOI18N
        gvMapButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gvMapButton1ActionPerformed(evt);
            }
        });

        geneDBCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(geneDBCheckBox, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.geneDBCheckBox.text")); // NOI18N
        geneDBCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geneDBCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel11.text")); // NOI18N

        length5PTextField.setText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.length5PTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel12.text")); // NOI18N

        p3LenTextField.setText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.p3LenTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel14.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(geneDBCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addGap(3, 3, 3)
                                .addComponent(geneDBComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(exampleFormatButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(customGeneFilePath))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(exampleFormatButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(variantFilePath, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(customGeneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gvMapButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(length5PTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(p3LenTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(jLabel14)))
                .addGap(20, 20, 20))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(geneDBComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(exampleFormatButton)
                        .addComponent(customGeneFilePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(geneDBCheckBox)
                    .addComponent(customGeneButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(length5PTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(p3LenTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exampleFormatButton1)
                    .addComponent(variantFilePath, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gvMapButton1)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lowLDR2TextField, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(toGCCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lowLDR2TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toGCCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel4.text")); // NOI18N

        originalAssociationFilesComboBox.setModel(GlobalManager.originalAssociationFilesModel);
        originalAssociationFilesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                originalAssociationFilesComboBoxActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jPanel5.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel5.text")); // NOI18N

        regionsComboBox.setEditable(true);
        regionsComboBox.setModel(regionsModel);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel17.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel18, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel18.text")); // NOI18N

        addRegionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Create.png"))); // NOI18N
        addRegionButton.setToolTipText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.addRegionButton.toolTipText")); // NOI18N
        addRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRegionButtonActionPerformed(evt);
            }
        });

        removeRegionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Cancel.png"))); // NOI18N
        removeRegionButton.setToolTipText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.removeRegionButton.toolTipText")); // NOI18N
        removeRegionButton.setFocusPainted(false);
        removeRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRegionButtonActionPerformed(evt);
            }
        });

        excludeWholeChrButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Radiation.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(excludeWholeChrButton, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.excludeWholeChrButton.text")); // NOI18N
        excludeWholeChrButton.setToolTipText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.excludeWholeChrButton.toolTipText")); // NOI18N
        excludeWholeChrButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excludeWholeChrButtonActionPerformed(evt);
            }
        });

        selectedRegionList.setModel(selectedRegionListModel);
        jScrollPane2.setViewportView(selectedRegionList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel8.text")); // NOI18N

        regiontypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Exclusion", "Inclusion" }));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(regiontypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(addRegionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(removeRegionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(fromTextField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(toTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(jLabel9)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel5)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(regionsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(excludeWholeChrButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8)
                    .addComponent(regiontypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(regionsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(excludeWholeChrButton))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jLabel6))
                            .addComponent(jLabel9)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(fromTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(toTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel17))))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(removeRegionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addRegionButton)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jPanel9.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel15.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel19, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel19.text")); // NOI18N

        inputTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "p-values", "z-scores", "chi-square" }));
        inputTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputTypeComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel20, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel20.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel21, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel21.text")); // NOI18N

        formatComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Single test per column", "Multiple tests per column" }));
        formatComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(testNameLabel, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.testNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dfLabel, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.dfLabel.text")); // NOI18N

        dfTextField.setText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.dfTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel26.text")); // NOI18N

        missingValueTextField.setText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.missingValueTextField.text")); // NOI18N

        hasTitleLineCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(hasTitleLineCheckBox, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.hasTitleLineCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel28, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel28.text")); // NOI18N

        pValueGenomeVersionComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "hg19", "hg38", "hg18", "hg17" }));
        pValueGenomeVersionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pValueGenomeVersionComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel31, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel31.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel7.text")); // NOI18N

        imputationQualComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imputationQualComboBoxActionPerformed(evt);
            }
        });

        infoLogicComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ">=", "<=" }));

        infoCutTextField.setText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.infoCutTextField.text")); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inputTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dfLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dfTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(formatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(testNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(testNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(missingValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(hasTitleLineCheckBox))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imputationQualComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(infoLogicComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(infoCutTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel9Layout.createSequentialGroup()
                            .addComponent(jLabel15)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(chromComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel19)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(markerPositionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel9Layout.createSequentialGroup()
                            .addComponent(jLabel31)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(markerIDComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel28)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pValueGenomeVersionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(chromComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(markerPositionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel28)
                        .addComponent(pValueGenomeVersionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel31)
                        .addComponent(markerIDComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(imputationQualComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(infoLogicComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(infoCutTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(dfLabel)
                    .addComponent(dfTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(formatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel26)
                    .addComponent(missingValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hasTitleLineCheckBox))
                .addContainerGap())
        );

        ldfilesTabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.ldfilesTabbedPane.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel38, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel38.text")); // NOI18N

        machHaplotypeGenomeVersionComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "hg19" }));

        ldMachVCFDataFileList.setModel(GlobalManager.vcfHaplotypeFileListModel);
        ldMachVCFDataFileList.setToolTipText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.ldMachVCFDataFileList.toolTipText")); // NOI18N
        jScrollPane4.setViewportView(ldMachVCFDataFileList);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Cancel.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton6, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jButton6.text")); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Folder.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jButton3.text")); // NOI18N
        jButton3.setToolTipText(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jButton3.toolTipText")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel40.setFont(new java.awt.Font("Book Antiqua 12 粗体 12", 0, 12)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel40, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel40.text")); // NOI18N

        javax.swing.GroupLayout machHapVCFPanelLayout = new javax.swing.GroupLayout(machHapVCFPanel);
        machHapVCFPanel.setLayout(machHapVCFPanelLayout);
        machHapVCFPanelLayout.setHorizontalGroup(
            machHapVCFPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(machHapVCFPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(machHapVCFPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(machHapVCFPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel38)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(machHaplotypeGenomeVersionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
        );
        machHapVCFPanelLayout.setVerticalGroup(
            machHapVCFPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(machHapVCFPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(machHapVCFPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(machHaplotypeGenomeVersionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(machHapVCFPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(machHapVCFPanelLayout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton6))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        ldfilesTabbedPane.addTab(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.machHapVCFPanel.TabConstraints.tabTitle"), machHapVCFPanel); // NOI18N

        plinkGtyPanel.setName("plinkGtyPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel22, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel22.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel23, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel23.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel24, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jLabel24.text")); // NOI18N

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Folder.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jButton4.text")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Folder.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton5, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jButton5.text")); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/cobi/kgg/ui/png/16x16/Folder.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton9, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.jButton9.text")); // NOI18N
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout plinkGtyPanelLayout = new javax.swing.GroupLayout(plinkGtyPanel);
        plinkGtyPanel.setLayout(plinkGtyPanelLayout);
        plinkGtyPanelLayout.setHorizontalGroup(
            plinkGtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plinkGtyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(plinkGtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(plinkGtyPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel23))
                    .addGroup(plinkGtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel22)
                        .addComponent(jLabel24)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(plinkGtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pedigreeFilePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mapFilePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bedFilePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(plinkGtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4))
        );
        plinkGtyPanelLayout.setVerticalGroup(
            plinkGtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, plinkGtyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(plinkGtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel22)
                    .addComponent(pedigreeFilePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(plinkGtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel23)
                    .addComponent(jButton5)
                    .addComponent(mapFilePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(plinkGtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel24)
                    .addComponent(bedFilePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9))
                .addGap(100, 100, 100))
        );

        ldfilesTabbedPane.addTab(org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.plinkGtyPanel.TabConstraints.tabTitle"), plinkGtyPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(defineCorrCheckBox, org.openide.util.NbBundle.getMessage(BuildAnalysisGenomeByPositionDialog.class, "BuildAnalysisGenomeByPositionDialog.defineCorrCheckBox.text")); // NOI18N
        defineCorrCheckBox.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        defineCorrCheckBox.setBorderPainted(true);
        defineCorrCheckBox.setBorderPaintedFlat(true);
        defineCorrCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defineCorrCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ldfilesTabbedPane))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(buildGenomeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addComponent(defineCorrCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(genomeNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(originalAssociationFilesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel10)
                    .addComponent(genomeNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(originalAssociationFilesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(defineCorrCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addComponent(buildGenomeButton)
                                .addGap(41, 41, 41)
                                .addComponent(cancelButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ldfilesTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void buildGenomeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildGenomeButtonActionPerformed
        pvalueColum.clear();
        int[] selectedIndexes = titleList.getSelectedIndices();

        for (int i = 0; i < selectedIndexes.length; i++) {
            pvalueColum.add(selectedIndexes[i]);
        }

        if (pvalueColum.isEmpty()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Please choose p-value or statistic columns to build!!", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            return;
        }

        String genomeName = genomeNameTextField.getText();
        if (GlobalManager.currentProject.isAvailableGenomeName(genomeName)) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("The genome name \"" + genomeName + "\" is available!", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            return;
        }
        int ldSourceCode = -1;

        Genome ldGenome = null;
        List<File[]> mapHaploFileList = new ArrayList<File[]>();
        String[] chromLDFiles = null;
        /*
         if (useOtheLDMatrixCheckBox.isSelected()) {
         //ld source code
         //-2 others LD
         //-1 no LD information
         //0 genotype plink binary file
         //1 hapap ld
         //2 1kG haplomap
         //3 local LD calcualted by plink
         //4 1kG haplomap vcf format
         ldSourceCode = -2;
         ldGenome = (Genome) genometSetComboBox.getSelectedItem();

         int ldTypeCode = ldGenome.getLdSourceCode();
         if (ldTypeCode == -2) {
         String infor = "The analysis genome \"" + ldGenome.getName() + "\" also also borrows the LD data of another analysis genome \"" + ldGenome.getSameLDGenome() + "\"."
         + "  Do you want to the LD of \"" + ldGenome.getSameLDGenome() + "\"?";
         Object[] options = {"Yes", "No"};
         int response = JOptionPane.showOptionDialog(this, infor, "Message", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
         null, options, options[0]);
         if (response == 1) {
         return;
         }
         ldGenome = GlobalManager.currentProject.getGenomeByName(ldGenome.getSameLDGenome());
         if (ldGenome == null) {
         infor = "Sorry, the analysis genome \"" + ldGenome.getSameLDGenome() + "\" does not exist!!";
         JOptionPane.showMessageDialog(this, infor, "Warnning", JOptionPane.WARNING_MESSAGE);
         return;
         }
         }

         if (ldTypeCode == 0) {
         if (ldGenome.getPlinkSet() == null) {
         String infor = "No genotype data to calculate linkadge disequiblibrium data! SNPs will be assume independent on these chromsomes. Do you want to continue?";
         Object[] options = {"Yes", "No"};
         int response = JOptionPane.showOptionDialog(this, infor, "Message", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
         null, options, options[0]);
         if (response == 1) {
         return;
         }
         }
         } else if (ldTypeCode == 1) {
         chromLDFiles = ldGenome.getChromLDFiles();
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < CHROM_NAMES.length - 1; i++) {
         if (chromLDFiles[i] == null) {
         sb.append(" ").append(CHROM_NAMES[i]);
         }
         }
         if (sb.length() > 0) {
         String infor = "The following chromosomes do not have the linkadge disequiblibrium data:\n" + sb.toString()
         + "\nSNPs will be assume independent on these chromsomes. Do you want to continue?";
         Object[] options = {"Yes", "No"};
         int response = JOptionPane.showOptionDialog(this, infor, "Message",
         JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
         null, options, options[0]);
         if (response == 1) {
         return;
         }
         }

         } else if (ldTypeCode == 2) {
         mapHaploFileList = ldGenome.getHaploMapFilesList();

         if (mapHaploFileList == null || mapHaploFileList.isEmpty()) {
         String infor = "No Haplotype datasets spcified to derive linkadge disequiblibrium! SNPs will be assume independent on these chromsomes. Do you want to continue?";
         Object[] options = {"Yes", "No"};
         int response = JOptionPane.showOptionDialog(this, infor, "Message",
         JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
         null, options, options[0]);
         if (response == 1) {
         return;
         }

         } else {
         //ld source code
         //0 genotype plink binary file
         //1 hapap ld
         //2 1kG haplomap
         //3 local LD calcualted by plink

         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < CHROM_NAMES.length - 1; i++) {
         if (mapHaploFileList.get(i) == null) {
         sb.append(" ").append(CHROM_NAMES[i]);
         }
         }
         if (sb.length() > 0) {
         String infor = "The following chromosomes do not have the linkadge disequiblibrium data:\n" + sb.toString()
         + "\nSNPs will be assume independent on these chromsomes. Do you want to continue?";
         Object[] options = {"Yes", "No"};
         int response = JOptionPane.showOptionDialog(this, infor, "Message",
         JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
         null, options, options[0]);
         if (response == 1) {
         return;
         }
         }

         }
         } else if (ldTypeCode == 4) {
         chromLDFiles = ldGenome.getChromLDFiles();
         if (chromLDFiles == null) {
         String infor = "No halotype data! SNPs will be assumed independent on these chromsomes. Do you want to continue?";
         Object[] options = {"Yes", "No"};
         int response = JOptionPane.showOptionDialog(this, infor, "Message",
         JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
         null, options, options[0]);
         if (response == 1) {
         return;
         }

         } else {
         //ld source code
         //-2 others LD
         //0 genotype plink binary file
         //1 hapap ld
         //2 1kG haplomap
         //3 local LD calcualted by plink
         //4 1kG haplomap vcf format

         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < CHROM_NAMES.length - 1; i++) {
         if (chromLDFiles[i] == null) {
         sb.append(" ").append(CHROM_NAMES[i]);
         }
         }
         if (sb.length() > 0) {
         String infor = "The following chromosomes have no haplotype data:\n" + sb.toString()
         + "\nSNPs will be assume independent on these chromsomes. Do you want to continue?";
         Object[] options = {"Yes", "No"};
         int response = JOptionPane.showOptionDialog(this, infor, "Message",
         JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
         null, options, options[0]);
         if (response == 1) {
         return;
         }
         }
         }
         }

         } else */

        if (ldfilesTabbedPane.getSelectedIndex() == 1) {
            if (bedFilePathTextField.getText().trim().length() == 0) {
                String infor = "No genotype data to calculate linkadge disequiblibrium data! SNPs will be assume independent on these chromosomes. Do you want to continue?";
                NotifyDescriptor nd = new NotifyDescriptor(
                        this, // instance of your panel
                        infor, // title of the dialog
                        NotifyDescriptor.YES_NO_OPTION, // it is Yes/No dialog ...
                        NotifyDescriptor.QUESTION_MESSAGE, // ... of a question type => a question mark icon
                        null, // we have specified YES_NO_OPTION => can be null, options specified by L&F,
                        // otherwise specify options as:
                        //     new Object[] { NotifyDescriptor.YES_OPTION, ... etc. },
                        NotifyDescriptor.YES_OPTION // default option is "Yes"
                );
                if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.NO_OPTION) {
                    return;
                }
                ldSourceCode = -1;
            } else {
                //ld source code
                //-2 others LD
                //0 genotype plink binary file
                //1 hapap ld
                //2 1kG haplomap
                //3 local LD calcualted by plink
                //4 1kG haplomap vcf format
                ldSourceCode = 0;
            }
        } else if (ldfilesTabbedPane.getSelectedIndex() == 2) {
            if (GlobalManager.hapmapLDFileListModel.isEmpty()) {
                String infor = "No linkadge disequiblibrium data! SNPs will be assumed independent on these chromosomes. Do you want to continue?";
                Object[] options = {"Yes", "No"};
                int response = JOptionPane.showOptionDialog(this, infor, "Message",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, options, options[0]);
                if (response == 1) {
                    return;
                }
                chromLDFiles = new String[CHROM_NAMES.length];
                ldSourceCode = -1;
            } else {
                //ld source code
                //-2 others LD
                //0 genotype plink binary file
                //1 hapap ld
                //2 1kG haplomap
                //3 local LD calcualted by plink
                //4 1kG haplomap vcf format
                ldSourceCode = 1;
                chromLDFiles = new String[CHROM_NAMES.length];
                int fileSize = GlobalManager.hapmapLDFileListModel.getSize();

                for (int i = 0; i < CHROM_NAMES.length; i++) {
                    String chroName = "_chr" + CHROM_NAMES[i] + "_";
                    //attemp to guess the name of chromsomes
                    for (int j = 0; j < fileSize; j++) {
                        if (GlobalManager.hapmapLDFileListModel.get(j).contains(chroName)) {
                            chromLDFiles[i] = GlobalManager.hapmapLDFileListModel.get(j);
                            break;
                        }
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < CHROM_NAMES.length - 1; i++) {
                    if (chromLDFiles[i] == null) {
                        sb.append(" ").append(CHROM_NAMES[i]);
                    }
                }
                if (sb.length() > 0) {
                    String infor = "The following chromosomes do not have the linkadge disequiblibrium data:\n" + sb.toString()
                            + "\nSNPs will be assume independent on these chromosomes. Do you want to continue?";
                    Object[] options = {"Yes", "No"};
                    int response = JOptionPane.showOptionDialog(this, infor, "Message",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0]);
                    if (response == 1) {
                        return;
                    }
                }
            }
        } else if (ldfilesTabbedPane.getSelectedIndex() == 0) {
            if (GlobalManager.vcfHaplotypeFileListModel.isEmpty()) {
                String infor = "No linkadge disequiblibrium data! SNPs will be assumed independent on these chromosomes. Do you want to continue?";
                Object[] options = {"Yes", "No"};
                int response = JOptionPane.showOptionDialog(this, infor, "Message",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, options, options[0]);
                if (response == 1) {
                    return;
                }
                chromLDFiles = new String[CHROM_NAMES.length];
                ldSourceCode = -1;
            } else {
                //ld source code
                //-2 others LD
                //0 genotype plink binary file
                //1 hapap ld
                //2 1kG haplomap
                //3 local LD calcualted by plink
                //4 1kG haplomap vcf format
                ldSourceCode = 4;
                chromLDFiles = new String[CHROM_NAMES.length];
                int fileSize = GlobalManager.vcfHaplotypeFileListModel.getSize();

                for (int i = 0; i < CHROM_NAMES.length; i++) {
                    String chroName = "chr" + CHROM_NAMES[i] + ".";
                    //attemp to guess the name of chromsomes
                    for (int j = 0; j < fileSize; j++) {
                        if (GlobalManager.vcfHaplotypeFileListModel.get(j).contains(chroName)) {
                            chromLDFiles[i] = GlobalManager.vcfHaplotypeFileListModel.get(j);
                            break;
                        }
                    }
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < CHROM_NAMES.length - 1; i++) {
                    if (chromLDFiles[i] == null) {
                        sb.append(" ").append(CHROM_NAMES[i]);
                    }
                }
                if (sb.length() > 0) {
                    String infor = "The following chromosomes have no haplotype data:\n" + sb.toString()
                            + "\nSNPs will be assume independent on these chromosomes. Do you want to continue?";
                    Object[] options = {"Yes", "No"};
                    int response = JOptionPane.showOptionDialog(this, infor, "Message",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0]);
                    if (response == 1) {
                        return;
                    }
                }
            }
        }
        /*
         String info = "Please make sure you select the correct \'Marker Position Version\' (Reference Genome of Coordinates)!";
         Object[] options = {"Yes", "No, not yet"};
         int response = JOptionPane.showOptionDialog(this, info, "Message",
         JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
         null, options, options[0]);
         if (response == 1) {
         return;
         }
         */
        try {
            RunningResultViewerTopComponent runningResultTopComp = (RunningResultViewerTopComponent) WindowManager.getDefault().findTopComponent("RunningResultViewerTopComponent");

            String prjWorkingPath = GlobalManager.currentProject.getWorkingPath();
            File storagePath = new File(prjWorkingPath + File.separator + GlobalManager.currentProject.getName() + File.separator + genomeName + File.separator);
            Genome genome = new Genome(genomeName, storagePath.getCanonicalPath());
            genome.setMappedByRSID(false);
            genome.setLdMatrixStoragePath(storagePath.getCanonicalPath());
            if (geneDBCheckBox.isSelected()) {
                genome.setExtendedGene5PLen(Double.parseDouble(length5PTextField.getText()));
                genome.setExtendedGene3PLen(Double.parseDouble(p3LenTextField.getText()));
            }
             
            genome.setpValueSource((FileTextNode) GlobalManager.originalAssociationFilesModel.getSelectedItem());
            LocalFileFunc.makeStorageLoc(storagePath.getCanonicalPath());
            genome.setToAdjustPValue(toGCCheckBox.isSelected());
            // genome.setMinR2HighlyCorrelated(Double.parseDouble(highLDRSquareThredTextField.getText()));
            genome.setMinR2HighlyCorrelated(0.98);
            genome.setMappedByRSID(false);

            String dbName = geneDBComboBox.getSelectedItem().toString();
            if (!geneDBCheckBox.isSelected()) {
                genome.setGeneDB(null);
            } else if (dbName.equals("Customized")) {
                dbName = customGeneFilePath.getText();
                File f = new File(dbName);
                if (!f.exists()) {
                    JOptionPane.showMessageDialog(this, "The customized gene file does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                genome.setGeneDB(f.getCanonicalPath());
            } else {
                genome.setGeneDB(dbName);                  
            }

            String geneVarMapFile = variantFilePath.getText().trim();
            if (geneVarMapFile.length() > 0) {
                File f = new File(geneVarMapFile);
                if (!f.exists()) {
                    JOptionPane.showMessageDialog(this, "The gene-variant map file does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                genome.setGeneVarMapFile(f.getCanonicalPath());
            }

            //genome.setReferenceGenomeVersion(refGenomeComboBox.getSelectedItem().toString());
            genome.setMinEffectiveR2(Double.parseDouble(lowLDR2TextField.getText()));
            genome.setExcludeRegionMap(selectedRegionMap);
            if (!regiontypeComboBox.getSelectedItem().toString().equals("Exclusion")) {
                if (selectedRegionMap == null || selectedRegionMap.isEmpty()) {
                    String info = "You are selecting an exclusively inclusion model and must specify the region(s) to be analyzed!";
                    JOptionPane.showMessageDialog(this, "Error", info, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                genome.setExclusionModel(false);
            }

            genome.setLdSourceCode(ldSourceCode);
            genome.setFinalBuildGenomeVersion(pValueGenomeVersionComboBox.getSelectedItem().toString());
            if (corrList != null && !corrList.isEmpty()) {
                genome.writeTraitCorrelationMatrixToDisk(corrList);
            }
            boolean isImputInfoLarger = infoLogicComboBox.getSelectedIndex() <= 0;
            float imputInfoCutoff = Float.parseFloat(infoCutTextField.getText());

            PValueFileSetting pvSetting = new PValueFileSetting(chromComboBox.getSelectedIndex(), markerPositionComboBox.getSelectedIndex(), markerIDComboBox.getSelectedIndex(),
                    imputationQualComboBox.getSelectedIndex() - 1, isImputInfoLarger, imputInfoCutoff, titleList.getSelectedIndices());

            pvSetting.setMissingLabel(missingValueTextField.getText());
            pvSetting.setTestInputType(inputTypeComboBox.getSelectedItem().toString());
            pvSetting.setTestNameIndex(testNameComboBox.getSelectedIndex());
            pvSetting.setChiSquareDf(Integer.parseInt(dfTextField.getText()));
            pvSetting.setHasTitleRow(hasTitleLineCheckBox.isSelected());
            pvSetting.setPvalueColType(formatComboBox.getSelectedIndex());
            pvSetting.setPosGenomeVersion(pValueGenomeVersionComboBox.getSelectedItem().toString());
            genome.setpValueSourceSetting(pvSetting);

            BuildGenome bg = new BuildGenome(genome, GlobalManager.currentProject, runningResultTopComp);
            if (ldSourceCode == -2) {
                genome.setSameLDGenome(ldGenome.getName());
            } else if (ldSourceCode == 0) {
                PlinkDataset plinkSet = new PlinkDataset(pedigreeFilePathTextField.getText(), mapFilePathTextField.getText(), bedFilePathTextField.getText());
                genome.setPlinkSet(plinkSet);
            } else if (genome.getLdSourceCode() == 1) {
                // genome.setLdFileGenomeVersion(hapLDGenomeVersionComboBox.getSelectedItem().toString());
                //genome.setChromLDFiles(chromLDFiles);
            } else if (genome.getLdSourceCode() == 2) {
                //genome.setLdFileGenomeVersion(haplotyeMapLDGenomeVersionComboBox.getSelectedItem().toString());
                //genome.setHaploMapFilesList(mapHaploFileList);
            } else if (genome.getLdSourceCode() == 3) {
            } else if (genome.getLdSourceCode() == 4) {
                genome.setLdFileGenomeVersion(machHaplotypeGenomeVersionComboBox.getSelectedItem().toString());
                genome.setChromLDFiles(chromLDFiles);
            }

            setVisible(false);
            bg.actionPerformed(evt);
            CallableSystemAction.get(ShowGeneBasedAssocScanDialogAction.class).setEnabled(true);
            CallableSystemAction.get(ShowMultivarGenebasedScanAction.class).setEnabled(true);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(BuildAnalysisGenomeByPositionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buildGenomeButtonActionPerformed

    private void originalAssociationFilesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_originalAssociationFilesComboBoxActionPerformed
        // TODO add your handling code here:
        try {
            listTableData.clear();
            int selectedIndex = originalAssociationFilesComboBox.getSelectedIndex();
            File f = (File) originalAssociationFilesComboBox.getItemAt(selectedIndex);
            if (f != null) {
                ArrayList<String[]> content = new ArrayList<String[]>();
                int rowNum = 8;
                LocalFile.retrieveData(f.getCanonicalPath(), content, rowNum, null, null, true, 1024);
                if (content.isEmpty()) {
                    return;
                }
                listTableModel.setTitle(content.get(0));
                for (int i = 1; i < content.size(); i++) {
                    listTableData.add(content.get(i));
                }

                String[] inFileTitles = content.get(0);
                listTableModel.setDataList(listTableData);
                listTableModel.fireTableStructureChanged();
                chromComboBox.removeAllItems();
                markerIDComboBox.removeAllItems();
                markerPositionComboBox.removeAllItems();
                testNameComboBox.removeAllItems();
                pvalueTitleBox.removeAllElements();
                imputationQualComboBox.removeAllItems();
                imputationQualComboBox.addItem("(Optional)");
                for (String inFileTitle : inFileTitles) {
                    chromComboBox.addItem(inFileTitle);
                    markerIDComboBox.addItem(inFileTitle);
                    testNameComboBox.addItem(inFileTitle);
                    markerPositionComboBox.addItem(inFileTitle);
                    pvalueTitleBox.addElement(inFileTitle);
                    imputationQualComboBox.addItem(inFileTitle);
                }
                markerIDComboBox.setSelectedIndex(1);
                markerPositionComboBox.setSelectedIndex(2);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }//GEN-LAST:event_originalAssociationFilesComboBoxActionPerformed

    private void addRegionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRegionButtonActionPerformed
        // TODO add your handling code here:
        String chroInfo = regionsComboBox.getSelectedItem().toString();
        if (fromTextField.getText().trim().isEmpty()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("The \"from\" position cannot be empty", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            return;
        }
        if (toTextField.getText().trim().isEmpty()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("The \"to\" position cannot be empty", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            return;
        }
        int fromPos = Integer.parseInt(fromTextField.getText().trim());
        int toPos = Integer.parseInt(toTextField.getText().trim());
        if (fromPos > toPos) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("The \"from\" position cannot be smaller than the \"to\" position", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            return;
        }

        IntArrayList region = selectedRegionMap.get(chroInfo);
        if (region == null) {
            region = new IntArrayList();
            selectedRegionMap.put(chroInfo, region);
        }
        region.add(fromPos);
        region.add(toPos);
        selectedRegionListModel.addElement("Chr" + chroInfo + " [" + fromPos + "," + toPos + "]bp");
        fromTextField.setText("");
    }//GEN-LAST:event_addRegionButtonActionPerformed

    private void removeRegionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRegionButtonActionPerformed
        // TODO add your handling code here:
        int[] selectedIx = selectedRegionList.getSelectedIndices();
        for (int i = selectedIx.length - 1; i >= 0; i--) {
            String allInfo = selectedRegionListModel.remove(selectedIx[i]);
            String chroInfo = allInfo.substring(3, allInfo.indexOf(' '));
            if (allInfo.contains("All available SNPs")) {
                IntArrayList region = selectedRegionMap.get(chroInfo);
                if (region != null) {
                    int rgSize = region.size() / 2 - 1;
                    for (int j = rgSize; j >= 0; j--) {
                        if (region.get(j * 2) == -9 && region.get(j * 2 + 1) == Integer.MAX_VALUE) {
                            region.remove(j * 2 + 1);
                            region.remove(j * 2);
                        }
                    }
                    if (region.isEmpty()) {
                        selectedRegionMap.remove(chroInfo);
                    }
                }
            } else {
                long fromPos = Long.parseLong(allInfo.substring(allInfo.indexOf('[') + 1, allInfo.indexOf(',')).trim());
                long toPos = Long.parseLong(allInfo.substring(allInfo.indexOf(',') + 1, allInfo.indexOf(']')).trim());
                IntArrayList region = selectedRegionMap.get(chroInfo);
                if (region != null) {
                    int rgSize = region.size() / 2 - 1;
                    for (int j = rgSize; j >= 0; j--) {
                        if (region.get(j * 2) == fromPos && region.get(j * 2 + 1) == toPos) {
                            region.remove(j * 2 + 1);
                            region.remove(j * 2);
                        }
                    }
                    if (region.isEmpty()) {
                        selectedRegionMap.remove(chroInfo);
                    }
                }
            }
        }
    }//GEN-LAST:event_removeRegionButtonActionPerformed

    private void excludeWholeChrButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excludeWholeChrButtonActionPerformed
        // TODO add your handling code here:
        String chroInfo = regionsComboBox.getSelectedItem().toString();
        IntArrayList region = selectedRegionMap.get(chroInfo);
        if (region == null) {
            region = new IntArrayList();
            selectedRegionMap.put(chroInfo, region);
        }
        region.add(-9);
        region.add(Integer.MAX_VALUE);
        selectedRegionListModel.addElement("Chr" + chroInfo + " All available SNPs");
    }//GEN-LAST:event_excludeWholeChrButtonActionPerformed

    private void inputTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputTypeComboBoxActionPerformed
        // TODO add your handling code here:
        if (inputTypeComboBox.getSelectedIndex() == 2) {
            dfLabel.setVisible(true);
            dfTextField.setVisible(true);
        } else {
            dfLabel.setVisible(false);
            dfTextField.setVisible(false);
        }
    }//GEN-LAST:event_inputTypeComboBoxActionPerformed

    private void formatComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatComboBoxActionPerformed

        // TODO add your handling code here:
        if (formatComboBox.getSelectedIndex() == 1) {
            testNameLabel.setVisible(true);
            testNameComboBox.setVisible(true);
        } else {
            testNameLabel.setVisible(false);
            testNameComboBox.setVisible(false);
        }
    }//GEN-LAST:event_formatComboBoxActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        int[] selectedIx = ldMachVCFDataFileList.getSelectedIndices();
        for (int i = selectedIx.length - 1; i >= 0; i--) {
            GlobalManager.vcfHaplotypeFileListModel.removeElementAt(selectedIx[i]);
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:

        JFileChooser fDialog = new JFileChooser(GlobalManager.RESOURCE_PATH);
        //  JFileChooser fDialog = new JFileChooser("E:\\home\\mxli\\MyJava\\PI\\resources\\EUR");
        fDialog.setMultiSelectionEnabled(true);
        fDialog.setDialogTitle("Load 1KG Haplotype files");

        int result = fDialog.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // GlobalManager.lastAccessedPath = fDialog.getSelectedFile().getPath();
            //  GlobalManager.lastAccessedPath = GlobalManager.lastAccessedPath.substring(0, GlobalManager.lastAccessedPath.lastIndexOf(File.separator) + 1);
            try {
                final File[] files = fDialog.getSelectedFiles();
                int fileLength = files.length;
                for (int i = 0; i < fileLength; i++) {
                    if (!files[i].getName().endsWith(".md5")) {
                        GlobalManager.vcfHaplotypeFileListModel.addElement(files[i].getCanonicalPath());
                    }
                }

            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message("File loading Cancelled!", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
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
                pedigreeFilePathTextField.setText(path);
                if (mapFilePathTextField.getText().trim().length() == 0) {
                    int extPos = path.lastIndexOf(".");
                    String prefixName = "";
                    if (extPos >= 0) {
                        prefixName = path.substring(0, extPos);
                    }
                    File tmpFile = new File(prefixName + ".bim");
                    if (tmpFile.exists()) {
                        mapFilePathTextField.setText(prefixName + ".bim");
                    }
                }
                if (bedFilePathTextField.getText().trim().length() == 0) {
                    int extPos = path.lastIndexOf(".");
                    String prefixName = "";
                    if (extPos >= 0) {
                        prefixName = path.substring(0, extPos);
                    }
                    File tmpFile = new File(prefixName + ".bed");
                    if (tmpFile.exists()) {
                        bedFilePathTextField.setText(prefixName + ".bed");
                    }
                }

            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }

        } else {
            // JOptionPane.showMessageDialog(this, FILE_OPEN_CANCELLED, "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        String path;
        JFileChooser fDialog;
        if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
            fDialog = new JFileChooser(GlobalManager.lastAccessedPath);
        } else {
            fDialog = new JFileChooser();
        }
        fDialog.setDialogTitle("Choose a Map File for SNPs in the Pedigree File");

        int result = fDialog.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            GlobalManager.lastAccessedPath = fDialog.getSelectedFile().getPath();
            GlobalManager.lastAccessedPath = GlobalManager.lastAccessedPath.substring(0, GlobalManager.lastAccessedPath.lastIndexOf(File.separator) + 1);

            try {
                File file = fDialog.getSelectedFile();
                path = file.getCanonicalPath();
                mapFilePathTextField.setText(path);

                if (pedigreeFilePathTextField.getText().trim().length() == 0) {
                    int extPos = path.lastIndexOf(".");
                    String prefixName = "";
                    if (extPos >= 0) {
                        prefixName = path.substring(0, extPos);
                    }
                    File tmpFile = new File(prefixName + ".fam");
                    if (tmpFile.exists()) {
                        pedigreeFilePathTextField.setText(prefixName + ".fam");
                    }
                }
                if (bedFilePathTextField.getText().trim().length() == 0) {
                    int extPos = path.lastIndexOf(".");
                    String prefixName = "";
                    if (extPos >= 0) {
                        prefixName = path.substring(0, extPos);
                    }
                    File tmpFile = new File(prefixName + ".bed");
                    if (tmpFile.exists()) {
                        bedFilePathTextField.setText(prefixName + ".bed");
                    }
                }

                //JOptionPane.showMessageDialog(this, FILE_OPEN_APPROVED, "Message", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        } else {
            //JOptionPane.showMessageDialog(this, FILE_OPEN_CANCELLED, "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
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
                bedFilePathTextField.setText(path);
                if (mapFilePathTextField.getText().trim().length() == 0) {
                    int extPos = path.lastIndexOf(".");
                    String prefixName = "";
                    if (extPos >= 0) {
                        prefixName = path.substring(0, extPos);
                    }
                    File tmpFile = new File(prefixName + ".bim");
                    if (tmpFile.exists()) {
                        mapFilePathTextField.setText(prefixName + ".bim");
                    }
                }
                if (pedigreeFilePathTextField.getText().trim().length() == 0) {
                    int extPos = path.lastIndexOf(".");
                    String prefixName = "";
                    if (extPos >= 0) {
                        prefixName = path.substring(0, extPos);
                    }
                    File tmpFile = new File(prefixName + ".fam");
                    if (tmpFile.exists()) {
                        pedigreeFilePathTextField.setText(prefixName + ".fam");
                    }
                }

            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }

        } else {
            // JOptionPane.showMessageDialog(this, FILE_OPEN_CANCELLED, "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void defineCorrCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defineCorrCheckBoxActionPerformed
        if (defineCorrCheckBox.isSelected()) {
            if (GlobalManager.correlationMatrixDefJDialog == null) {
                GlobalManager.correlationMatrixDefJDialog = new CorrelationMatrixDefJDialog(GlobalManager.mainFrame, true, titleList, corrList);
            }
            GlobalManager.correlationMatrixDefJDialog.updateTiteBox(pvalueTitleBox);
            GlobalManager.correlationMatrixDefJDialog.setLocationRelativeTo(GlobalManager.mainFrame);
            GlobalManager.correlationMatrixDefJDialog.setVisible(true);
        } else {
            String info = "Do you want to remove the phenotype-correlation information?";
            Object[] options = {"Yes", "No!"};
            int response = JOptionPane.showOptionDialog(GlobalManager.mainFrame, info, "Message",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);
            if (response == 1) {
                return;
            }
            if (corrList != null) {
                corrList.clear();
            }

        }


    }//GEN-LAST:event_defineCorrCheckBoxActionPerformed

    private void geneDBComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geneDBComboBoxActionPerformed
        // TODO add your handling code here:
        String item = geneDBComboBox.getSelectedItem().toString();
        if (item.equals("RefGene")) {
            pValueGenomeVersionComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{"hg19", "hg18", "hg17"}));
            customGeneFilePath.setEnabled(false);
            customGeneButton.setEnabled(false);
            exampleFormatButton.setEnabled(false);
        } else if (item.equals("GEncode")) {
            pValueGenomeVersionComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{"hg19", "hg18"}));
            customGeneFilePath.setEnabled(false);
            customGeneButton.setEnabled(false);
            exampleFormatButton.setEnabled(false);
        } else {
            pValueGenomeVersionComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{"hg19", "hg18", "hg17"}));
            customGeneFilePath.setEnabled(true);
            customGeneFilePath.setEditable(true);
            customGeneButton.setEnabled(true);
            exampleFormatButton.setEnabled(true);
        }
    }//GEN-LAST:event_geneDBComboBoxActionPerformed

    private void pValueGenomeVersionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pValueGenomeVersionComboBoxActionPerformed
        // TODO add your handling code here:
        String item = pValueGenomeVersionComboBox.getSelectedItem().toString();
        String item1 = geneDBComboBox.getSelectedItem().toString();
        if ((item.equals("hg18") || item.equals("hg17")) && item1.equals("GEncode")) {
            JOptionPane.showMessageDialog(this, FILE_OPEN_CANCELLED, "Sorry, GEncode does not support " + item + "!", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_pValueGenomeVersionComboBoxActionPerformed

    private void imputationQualComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imputationQualComboBoxActionPerformed
        if (imputationQualComboBox.getSelectedIndex() > 0) {
            infoLogicComboBox.setVisible(true);
            infoCutTextField.setVisible(true);
        } else {
            infoLogicComboBox.setVisible(false);
            infoCutTextField.setVisible(false);
        }

    }//GEN-LAST:event_imputationQualComboBoxActionPerformed

    static boolean hasNotLoadP = true;
    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
        if (hasNotLoadP) {
            hasNotLoadP = false;
            OpenFileSwingWorker worker = new OpenFileSwingWorker();
            worker.execute();
        }
    }//GEN-LAST:event_formMouseEntered

    private void customGeneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customGeneButtonActionPerformed
        JFileChooser fDialog = new JFileChooser(GlobalManager.RESOURCE_PATH);
        //  JFileChooser fDialog = new JFileChooser("E:\\home\\mxli\\MyJava\\PI\\resources\\EUR");
        fDialog.setMultiSelectionEnabled(true);
        fDialog.setDialogTitle("Load a gene definition file");

        int result = fDialog.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // GlobalManager.lastAccessedPath = fDialog.getSelectedFile().getPath();
            //  GlobalManager.lastAccessedPath = GlobalManager.lastAccessedPath.substring(0, GlobalManager.lastAccessedPath.lastIndexOf(File.separator) + 1);
            try {
                final File files = fDialog.getSelectedFile();
                customGeneFilePath.setText(files.getCanonicalPath());
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message("File loading Cancelled!", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        }

    }//GEN-LAST:event_customGeneButtonActionPerformed

    private void exampleFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exampleFormatButtonActionPerformed

        FormatShowingDialog dialog = new FormatShowingDialog(new javax.swing.JFrame(), true, "Gene");
        dialog.setLocationRelativeTo(GlobalManager.mainFrame);
        dialog.setVisible(true);
    }//GEN-LAST:event_exampleFormatButtonActionPerformed

    private void gvMapButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gvMapButton1ActionPerformed

        JFileChooser fDialog = new JFileChooser(GlobalManager.RESOURCE_PATH);
        //  JFileChooser fDialog = new JFileChooser("E:\\home\\mxli\\MyJava\\PI\\resources\\EUR");
        fDialog.setMultiSelectionEnabled(true);
        fDialog.setDialogTitle("Load Gene-Variant Map File");

        int result = fDialog.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // GlobalManager.lastAccessedPath = fDialog.getSelectedFile().getPath();
            //  GlobalManager.lastAccessedPath = GlobalManager.lastAccessedPath.substring(0, GlobalManager.lastAccessedPath.lastIndexOf(File.separator) + 1);
            try {
                final File files = fDialog.getSelectedFile();
                variantFilePath.setText(files.getCanonicalPath());
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message("File loading Cancelled!", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        }

    }//GEN-LAST:event_gvMapButton1ActionPerformed

    private void geneDBCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geneDBCheckBoxActionPerformed
        // TODO add your handling code here:
        geneDBComboBox.setEnabled(geneDBCheckBox.isSelected());
        length5PTextField.setEnabled(geneDBCheckBox.isSelected());
        p3LenTextField.setEnabled(geneDBCheckBox.isSelected());


    }//GEN-LAST:event_geneDBCheckBoxActionPerformed

    private void exampleFormatButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exampleFormatButton1ActionPerformed
        // TODO add your handling code here:
        FormatShowingDialog dialog = new FormatShowingDialog(new javax.swing.JFrame(), true, "GeneVarMap");
        dialog.setLocationRelativeTo(GlobalManager.mainFrame);
        dialog.setVisible(true);
    }//GEN-LAST:event_exampleFormatButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(BuildAnalysisGenomeByPositionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BuildAnalysisGenomeByPositionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BuildAnalysisGenomeByPositionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BuildAnalysisGenomeByPositionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                BuildAnalysisGenomeByPositionDialog dialog = new BuildAnalysisGenomeByPositionDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton addRegionButton;
    private javax.swing.JTextField bedFilePathTextField;
    private javax.swing.JButton buildGenomeButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox<String> chromComboBox;
    private javax.swing.JButton customGeneButton;
    private javax.swing.JTextField customGeneFilePath;
    private javax.swing.JCheckBox defineCorrCheckBox;
    private javax.swing.JLabel dfLabel;
    private javax.swing.JTextField dfTextField;
    private javax.swing.JButton exampleFormatButton;
    private javax.swing.JButton exampleFormatButton1;
    private javax.swing.JButton excludeWholeChrButton;
    private javax.swing.JComboBox<String> formatComboBox;
    private javax.swing.JTextField fromTextField;
    private javax.swing.JCheckBox geneDBCheckBox;
    private javax.swing.JComboBox geneDBComboBox;
    private javax.swing.JTextField genomeNameTextField;
    private javax.swing.JButton gvMapButton1;
    private javax.swing.JCheckBox hasTitleLineCheckBox;
    private javax.swing.JComboBox<String> imputationQualComboBox;
    private javax.swing.JTextField infoCutTextField;
    private javax.swing.JComboBox infoLogicComboBox;
    private javax.swing.JComboBox<String> inputTypeComboBox;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JList<String> ldMachVCFDataFileList;
    private javax.swing.JTabbedPane ldfilesTabbedPane;
    private javax.swing.JTextField length5PTextField;
    private javax.swing.JTextField lowLDR2TextField;
    private javax.swing.JPanel machHapVCFPanel;
    private javax.swing.JComboBox<String> machHaplotypeGenomeVersionComboBox;
    private javax.swing.JTextField mapFilePathTextField;
    private javax.swing.JTable markerFileTable;
    private javax.swing.JComboBox<String> markerIDComboBox;
    private javax.swing.JComboBox<String> markerPositionComboBox;
    private javax.swing.JTextField missingValueTextField;
    private javax.swing.JComboBox<FileTextNode> originalAssociationFilesComboBox;
    private javax.swing.JTextField p3LenTextField;
    private javax.swing.JComboBox<String> pValueGenomeVersionComboBox;
    private javax.swing.JTextField pedigreeFilePathTextField;
    private javax.swing.JPanel plinkGtyPanel;
    private javax.swing.JComboBox<String> regionsComboBox;
    private javax.swing.JComboBox regiontypeComboBox;
    private javax.swing.JButton removeRegionButton;
    private javax.swing.JList<String> selectedRegionList;
    private javax.swing.JComboBox<String> testNameComboBox;
    private javax.swing.JLabel testNameLabel;
    private javax.swing.JList<String> titleList;
    private javax.swing.JCheckBox toGCCheckBox;
    private javax.swing.JTextField toTextField;
    private javax.swing.JTextField variantFilePath;
    // End of variables declaration//GEN-END:variables
}
