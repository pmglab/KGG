/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.dialog;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sf.picard.liftover.LiftOver;
import net.sf.picard.util.Interval;
import org.apache.log4j.Logger;
import org.cobi.kgg.business.entity.Chromosome;
import org.cobi.kgg.business.entity.Constants;
import static org.cobi.kgg.business.entity.Constants.VAR_FEATURE_NAMES;
import static org.cobi.kgg.business.entity.Constants.geneGroups;
import org.cobi.kgg.business.entity.CorrelationBasedByteLDSparseMatrix;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.PValueGene;
import org.cobi.kgg.business.entity.PValueGeneComparator;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.SNPPValueIndexComparator;
import org.cobi.kgg.ui.ArrayListStringArrayTableModel;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.file.Zipper;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.net.URLOpener;
import org.cobi.util.plot.ShowSNPChart;
import org.cobi.util.text.LocalExcelFile;
import org.cobi.util.text.LocalFile;
import org.cobi.util.text.Util;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.cobi.kgg.ui.dialog//ShowGeneResult//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "ShowGeneResultTopComponent",
        iconBase = "org/cobi/kgg/ui/png/16x16/Eye.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Gene", id = "org.cobi.kgg.ui.dialog.ShowGeneResultTopComponent")
@ActionReference(path = "Menu/Gene", position = 333)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ShowGeneResultAction",
        preferredID = "ShowGeneResultTopComponent")
@Messages({
    "CTL_ShowGeneResultAction=View Genes",
    "CTL_ShowGeneResultTopComponent=View Genes",
    "HINT_ShowGeneResultTopComponent=This is a ShowResult window"
})
public final class ShowGeneResultTopComponent extends TopComponent implements LookupListener, Constants, ChartMouseListener {

    private final static Logger LOG = Logger.getLogger(ShowGeneResultTopComponent.class.getName());
    private String name;
    private List<PValueGene> pValueGeneList;
    private Lookup.Result<GeneBasedAssociation> result = null;
    private String[] strGeneTerm = {"Symbol", "NominalP", "CorrectedP", "Chromosome", "Start_Position", "Group"};
    private final String[] strSNPTerm = {"SNP", "Gene_Feature", "P"};
    private final String[] strOutput = {"Symbol", "PValue", "IsSignificant", "Chromosome", "Start_Position", "SNP", "Gene_Feature", "P"};
    ArrayListStringArrayTableModel aomGeneTermModel = null;
    ArrayListStringArrayTableModel aomSNPTermModel = null;
    List<String[]> lstGeneTerm = null;
    List<String[]> lstSNPTerm = null;
    //List<Gene> altGeneSet = new ArrayList();
    DoubleArrayList dalPValues = new DoubleArrayList();
    CorrelationBasedByteLDSparseMatrix ldRsMatrix = null;
    GeneBasedAssociation event = null;
    Genome gmeTerm = null;
    Map<String, int[]> mapG2C = null;
    Chromosome[] chrTerms = null;
    ChartPanel cplCanvas = null;
    double[][] dblData2D = null;
    double[][] dblData = null;
    LiftOver liftOver = null;
    Gene gneTerm;
    boolean boolTest = false;
    Map<String, Gene> mapSymbol2Gene = null;
    private final static RequestProcessor RP = new RequestProcessor("Loading analysis genome tas", 1, true);
    private RequestProcessor.Task theTask = null;

    public ShowGeneResultTopComponent() {

        lstGeneTerm = new ArrayList<String[]>();
        aomGeneTermModel = new ArrayListStringArrayTableModel();
        aomGeneTermModel.setTitle(strGeneTerm);
        aomGeneTermModel.setDataList(lstGeneTerm);

        lstSNPTerm = new ArrayList<String[]>();
        aomSNPTermModel = new ArrayListStringArrayTableModel();
        aomSNPTermModel.setTitle(strSNPTerm);
        //aomSNPTermModel.setDataList(lstSNPTerm);
        mapSymbol2Gene = new HashMap<String, Gene>();

        initComponents();
        setName(Bundle.CTL_ShowGeneResultTopComponent());
        setToolTipText(Bundle.HINT_ShowGeneResultTopComponent());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        formatComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        contentComboBox = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        genePValueCutTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        variantPValueCutTextField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        multiMethodComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jButton3 = new javax.swing.JButton();

        jSplitPane1.setDividerLocation(286);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jPanel1.border.title"))); // NOI18N
        jPanel1.setToolTipText(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jPanel1.toolTipText")); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(979, 250));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel1MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 953, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jSplitPane1.setBottomComponent(jPanel1);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jScrollPane1.border.title"))); // NOI18N

        jTable1.setModel(aomGeneTermModel);
        jTable1.setToolTipText(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jTable1.toolTipText")); // NOI18N
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jScrollPane2.border.title"))); // NOI18N

        jTable2.setAutoCreateRowSorter(true);
        jTable2.setModel(aomSNPTermModel);
        jTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane2.setViewportView(jTable2);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jPanel2.border.title"))); // NOI18N

        formatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Excel(.xlsx)", "Excel(.xls)", "Text(.txt)" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel5.text")); // NOI18N

        contentComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Variants inside genes", "Only genes", "All variants + genes" }));
        contentComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.contentComboBox.toolTipText")); // NOI18N
        contentComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contentComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel6.text")); // NOI18N

        genePValueCutTextField.setText(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.genePValueCutTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel7.text")); // NOI18N

        variantPValueCutTextField.setText(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.variantPValueCutTextField.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(formatComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel6)
                                    .addGap(18, 18, 18)
                                    .addComponent(genePValueCutTextField))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel7)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(variantPValueCutTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(contentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 38, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(formatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(genePValueCutTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(variantPValueCutTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jPanel4.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel3.toolTipText")); // NOI18N

        multiMethodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benjamini & Hochberg (1995)", "Benjamini & Yekutieli (2001)", "Standard Bonferroni" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel4.text")); // NOI18N
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel4.toolTipText")); // NOI18N

        jTextField2.setText(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jTextField2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43)
                        .addComponent(jButton2))
                    .addComponent(multiMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(multiMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4))
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel3.AccessibleContext.accessibleName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jLabel1.text")); // NOI18N

        jTextPane1.setText(org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jTextPane1.text")); // NOI18N
        jScrollPane3.setViewportView(jTextPane1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(ShowGeneResultTopComponent.class, "ShowGeneResultTopComponent.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jButton3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 445, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 967, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
        int intRow = jTable1.getSelectedRow();
        if (intRow < 0) {

            return;
        }

        if (evt.getClickCount() == 1) {
            showSNPsofGene(intRow);
        } else {
            String strTarget = (String) jTable1.getValueAt(intRow, 0);
            URLOpener.openURL("http://www.genecards.org/cgi-bin/carddisp.pl?gene=" + strTarget);
        }

    }//GEN-LAST:event_jTable1MouseClicked

    private void showSNPsofGene(int intRow) {
        if (jTable1.getRowCount() <= 0) {
            String infor = "The selected gene has no SNP!";
            JOptionPane.showMessageDialog(this, infor, "Warnning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String pStr = (String) jTable1.getValueAt(intRow, 1);
        Double dblPValue = null;
        if (!pStr.equals("NA") && !pStr.equals("NaN")) {
            dblPValue = Double.valueOf(pStr);
        }
        String strTarget = (String) jTable1.getValueAt(intRow, 0);
        String chrom = (String) jTable1.getValueAt(intRow, 3);
        lstSNPTerm.clear();
        gneTerm = mapSymbol2Gene.get(strTarget);
        //gneTerm = altGeneSet.get(intRow);
        try {
            int intVary = 0;
            Iterator<SNP> itrSNP = gneTerm.snps.iterator();
            String[] names = gmeTerm.getpValueNames();
            List<String> vfns = gmeTerm.getVariantFeatureNames();
            String[] strVFNS = vfns.toArray(new String[vfns.size()]);
            int offSetValue = 3;

            Map<String, Double> tastPValues = event.loadTASTESNPPValuesfromDisk("TASTE");

            boolean hasMultipleTraits = false;
            if (tastPValues != null) {
                hasMultipleTraits = true;
                offSetValue++;
            }

            while (itrSNP.hasNext()) {
                SNP snpTerm = itrSNP.next();
                String[] strConst = null;
                if (hasMultipleTraits) {
                    Double tp = tastPValues.get(chrom + ":" + snpTerm.getPhysicalPosition());
                    strConst = new String[]{snpTerm.getRsID(), String.valueOf(snpTerm.getPhysicalPosition()), VAR_FEATURE_NAMES[snpTerm.getGeneFeature()], String.valueOf(tp)};
                } else {
                    strConst = new String[]{snpTerm.getRsID(), String.valueOf(snpTerm.getPhysicalPosition()), VAR_FEATURE_NAMES[snpTerm.getGeneFeature()]};
                }

                String[] strVary = Util.formatePValues(snpTerm.getpValues());
                String[] strRow = new String[strConst.length + strVary.length + strVFNS.length];
                System.arraycopy(strConst, 0, strRow, 0, strConst.length);

                System.arraycopy(strVary, 0, strRow, strConst.length, strVary.length);
                if (!vfns.isEmpty()) {
                    DoubleArrayList dalVF = snpTerm.getAFeatureValue();
                    double[] dblVF = dalVF.elements();
                    String[] strVF = new String[dblVF.length];
                    for (int i = 0; i < dblVF.length; i++) {
                        strVF[i] = String.valueOf(dblVF[i]);
                    }
                    System.arraycopy(strVF, 0, strRow, strConst.length + strVary.length, strVF.length);
                }
                //  if(Double.valueOf(strRow[2])<=Double.valueOf(variantPValueCutTextField.getText()))  
                lstSNPTerm.add(strRow);
            }

            intVary = names.length + strVFNS.length;
            String[] strTitles = new String[intVary + offSetValue];
            strTitles[0] = "SNP";
            strTitles[1] = "Position";
            strTitles[2] = "Gene_Feature";

            if (hasMultipleTraits) {
                strTitles[3] = "TATES_P";
            }

            if (lstSNPTerm.isEmpty()) {
                String strNULL[] = new String[intVary + offSetValue];
                for (int i = 0; i < intVary + offSetValue; i++) {
                    strNULL[i] = "null";
                }
                lstSNPTerm.add(strNULL);
            }

            System.arraycopy(names, 0, strTitles, offSetValue, names.length);
            if (!vfns.isEmpty()) {
                System.arraycopy(strVFNS, 0, strTitles, names.length + offSetValue, strVFNS.length);
            }

            aomSNPTermModel.setTitle(strTitles);
            aomSNPTermModel.setDataList(lstSNPTerm);
            aomSNPTermModel.fireTableStructureChanged();
            aomSNPTermModel.fireTableDataChanged();

            ShowSNPChart sscPlot = null;

            int[] posIndex = mapG2C.get(gneTerm.getSymbol());
            if (gmeTerm.getLdSourceCode() == -2) {
                ldRsMatrix = GlobalManager.currentProject.getGenomeByName(gmeTerm.getSameLDGenome()).readChromosomeLDfromDisk(posIndex[0]);
            } else {
                ldRsMatrix = gmeTerm.readChromosomeLDfromDisk(posIndex[0]);
            }

            sscPlot = new ShowSNPChart(gneTerm, ldRsMatrix, dblPValue);

            JFreeChart jfcPlot = sscPlot.getChart();
            cplCanvas = new ChartPanel(jfcPlot);
            cplCanvas.addChartMouseListener(this);

            jPanel1.removeAll();
            jPanel1.setLayout(new java.awt.BorderLayout());
            jPanel1.add(cplCanvas, BorderLayout.CENTER);
            jPanel1.validate();

            dblData2D = new double[2][gneTerm.snps.size()];
            dblData = sscPlot.getData();
            boolTest = false;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    private void jPanel1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseEntered
        // TODO add your handling code here:
        jPanel1.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jPanel1MouseEntered

    private void jPanel1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseExited
        // TODO add your handling code here:
        jPanel1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jPanel1MouseExited

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String strPValueInfo = multiMethodComboBox.getSelectedItem().toString();
        String strContent = contentComboBox.getSelectedItem().toString();
        String strFormat = formatComboBox.getSelectedItem().toString();
        String strErrorRate = jTextField2.getText();
        double genePCutOff = Double.parseDouble(genePValueCutTextField.getText());
        double snpPCutOff = Double.parseDouble(variantPValueCutTextField.getText());
        double geneP = 1;
        boolean noPass = false;

        int snpFeatureNum = gmeTerm.getVariantFeatureNames().size();
        int pNum = gmeTerm.getpValueNames().length;
        List<String[]> lstOutput = new ArrayList();
        List<String[]> lstOutput2 = new ArrayList();

        if (pValueGeneList == null) {
            return;
        }
        try {
            int geneNum = pValueGeneList.size();
            Map<String, IntArrayList> geneKeySNPPostMap = new HashMap<String, IntArrayList>();

            for (int i = 0; i < geneNum; i++) {
                PValueGene pvalueGene = pValueGeneList.get(i);
                if (pvalueGene.pValue <= genePCutOff) {
                    pvalueGene.keySNPPositions.quickSort();
                    geneKeySNPPostMap.put(pvalueGene.getSymbol(), pvalueGene.keySNPPositions);
                }
            }
            Map<String, Double> tastPValues = event.loadTASTESNPPValuesfromDisk("TASTE");
            int tastNum = 0;
            boolean hasMultipleTraits = false;
            if (tastPValues != null) {
                hasMultipleTraits = true;
                tastNum = 1;
            }

            SNPPValueIndexComparator snpPVCompar = new SNPPValueIndexComparator(0);
            int keySNPIndex = 0;
            if (contentComboBox.getSelectedIndex() == 0) {
                List<String[]> lstCurrentGene = aomGeneTermModel.getDataList();
                for (String[] strGenes : lstCurrentGene) {
                    gneTerm = mapSymbol2Gene.get(strGenes[0]);
                    if (!strGenes[1].equals("NA")) {
                        geneP = Double.parseDouble(strGenes[1]);
                        if (geneP > genePCutOff) {
                            continue;
                        }
                    }
                    String chrom = strGenes[3];

                    Collections.sort(gneTerm.snps, snpPVCompar);
                    Iterator<SNP> itrSNP = gneTerm.snps.iterator();

                    int snpNum = 0;
                    IntArrayList keySNPPoss = geneKeySNPPostMap.get(strGenes[0]);
                    while (itrSNP.hasNext()) {
                        SNP snpTerm = itrSNP.next();
                        String[] strSub = new String[snpFeatureNum + 4 + tastNum + pNum];
                        strSub[0] = snpTerm.getRsID();
                        strSub[1] = String.valueOf(snpTerm.getPhysicalPosition());
                        strSub[2] = VAR_FEATURE_NAMES[snpTerm.getGeneFeature()];
                        if (hasMultipleTraits) {
                            Double tp = tastPValues.get(chrom + ":" + snpTerm.getPhysicalPosition());
                            strSub[3] = String.valueOf(tp);
                        }

                        if (keySNPPoss != null) {
                            keySNPIndex = keySNPPoss.binarySearch(snpTerm.physicalPosition);
                            if (keySNPIndex >= 0) {
                                strSub[3 + tastNum] = "Y";
                            } else {
                                strSub[3 + tastNum] = "N";
                            }
                        } else {
                            strSub[3 + tastNum] = "-";
                        }

                        DoubleArrayList values = snpTerm.getAFeatureValue();// Has the function of outputing variant feature been finished?
                        for (int t = 0; t < snpFeatureNum; t++) {
                            strSub[t + 4 + tastNum] = String.valueOf(values.getQuick(t));
                        }
                        // strSub[snpFeatureNum + 2] = String.valueOf(snpTerm.getpValues()[0]);
                        noPass = true;
                        for (int t = 0; t < pNum; t++) {
                            if (snpPCutOff > snpTerm.getpValues()[t]) {
                                noPass = false;
                            }
                            strSub[snpFeatureNum + 4 + tastNum + t] = String.valueOf(snpTerm.getpValues()[t]);
                        }
                        if (noPass) {
                            continue;
                        }
                        String[] strRow = new String[strGenes.length + strSub.length];
                        if (snpNum == 0) {
                            System.arraycopy(strGenes, 0, strRow, 0, strGenes.length);
                            System.arraycopy(strSub, 0, strRow, strGenes.length, strSub.length);
                        } else {
                            System.arraycopy(strSub, 0, strRow, strGenes.length, strSub.length);
                        }
                        snpNum++;

                        lstOutput.add(strRow);
                    }
                }

                String[] strTitle = new String[strGeneTerm.length + snpFeatureNum + 4 + tastNum + pNum];
                System.arraycopy(strGeneTerm, 0, strTitle, 0, strGeneTerm.length);
                System.arraycopy(gmeTerm.getpValueNames(), 0, strTitle, snpFeatureNum + 4 + tastNum + strGeneTerm.length, pNum);

                List<String> vfns = gmeTerm.getVariantFeatureNames();
                String[] strVFNS = vfns.toArray(new String[vfns.size()]);
                strTitle[strGeneTerm.length] = "SNP";
                strTitle[strGeneTerm.length + 1] = "Position";
                strTitle[strGeneTerm.length + 2] = "GeneFeature";
                if (hasMultipleTraits) {
                    strTitle[strGeneTerm.length + 3] = "TATES_P";
                }

                strTitle[strGeneTerm.length + 3 + tastNum] = "IsGATESKeySNP";
                System.arraycopy(strVFNS, 0, strTitle, strGeneTerm.length + 4 + tastNum, strVFNS.length);

                lstOutput.add(0, strTitle);
            } else if (contentComboBox.getSelectedIndex() == 1) {
                lstOutput.add(strGeneTerm);
                lstOutput.addAll(aomGeneTermModel.getDataList());
            } else {
                List<String[]> lstCurrentGene = aomGeneTermModel.getDataList();
                int snpNum = 0;
                for (String[] strGenes : lstCurrentGene) {
                    gneTerm = mapSymbol2Gene.get(strGenes[0]);
                    if (!strGenes[1].equals("NA")) {
                        geneP = Double.parseDouble(strGenes[1]);
                        if (geneP > genePCutOff) {
                            continue;
                        }
                    }
                    String chrom = strGenes[3];
                    Collections.sort(gneTerm.snps, snpPVCompar);
                    Iterator<SNP> itrSNP = gneTerm.snps.iterator();

                    snpNum = 0;
                    IntArrayList keySNPPoss = geneKeySNPPostMap.get(strGenes[0]);
                    while (itrSNP.hasNext()) {
                        SNP snpTerm = itrSNP.next();
                        String[] strSub = new String[snpFeatureNum + 4 + tastNum + pNum];
                        strSub[0] = snpTerm.getRsID();
                        strSub[1] = String.valueOf(snpTerm.getPhysicalPosition());
                        strSub[2] = VAR_FEATURE_NAMES[snpTerm.getGeneFeature()];
                        if (hasMultipleTraits) {
                            Double tp = tastPValues.get(chrom + ":" + snpTerm.getPhysicalPosition());
                            strSub[3] = String.valueOf(tp);
                        }
                        if (keySNPPoss != null) {
                            keySNPIndex = keySNPPoss.binarySearch(snpTerm.physicalPosition);
                            if (keySNPIndex >= 0) {
                                strSub[3 + tastNum] = "Y";
                            } else {
                                strSub[3 + tastNum] = "N";
                            }
                        } else {
                            strSub[3 + tastNum] = "-";
                        }

                        DoubleArrayList values = snpTerm.getAFeatureValue();// Has the function of outputing variant feature been finished?
                        for (int t = 0; t < snpFeatureNum; t++) {
                            strSub[t + tastNum + 4] = String.valueOf(values.getQuick(t));
                        }
                        // strSub[snpFeatureNum + 2] = String.valueOf(snpTerm.getpValues()[0]);
                        noPass = true;
                        for (int t = 0; t < pNum; t++) {
                            if (snpPCutOff > snpTerm.getpValues()[t]) {
                                noPass = false;
                            }
                            strSub[snpFeatureNum + 4 + tastNum + t] = String.valueOf(snpTerm.getpValues()[t]);
                        }
                        if (noPass) {
                            continue;
                        }
                        String[] strRow = new String[strGenes.length + strSub.length];
                        if (snpNum == 0) {
                            System.arraycopy(strGenes, 0, strRow, 0, strGenes.length);
                            System.arraycopy(strSub, 0, strRow, strGenes.length, strSub.length);
                        } else {
                            System.arraycopy(strSub, 0, strRow, strGenes.length, strSub.length);
                        }
                        snpNum++;

                        lstOutput.add(strRow);
                    }
                }

                String[] strTitle = new String[strGeneTerm.length + snpFeatureNum + 4 + tastNum + pNum];
                System.arraycopy(strGeneTerm, 0, strTitle, 0, strGeneTerm.length);
                System.arraycopy(gmeTerm.getpValueNames(), 0, strTitle, snpFeatureNum + 4 + tastNum + strGeneTerm.length, pNum);

                List<String> vfns = gmeTerm.getVariantFeatureNames();
                String[] strVFNS = vfns.toArray(new String[vfns.size()]);
                strTitle[strGeneTerm.length] = "SNP";
                strTitle[strGeneTerm.length + 1] = "Position";
                strTitle[strGeneTerm.length + 2] = "GeneFeature";
                if (hasMultipleTraits) {
                    strTitle[strGeneTerm.length + 3] = "TASTE_P";
                }
                strTitle[strGeneTerm.length + 3 + tastNum] = "IsGATESKeySNP";

                System.arraycopy(strVFNS, 0, strTitle, strGeneTerm.length + 4 + tastNum, strVFNS.length);

                lstOutput.add(0, strTitle);

                for (Chromosome chrTerm : chrTerms) {
                    if (null == chrTerm) {
                        continue;
                    }
                    List lstSNP = chrTerm.snpsOutGenes;
                    Iterator<SNP> itrSNP = lstSNP.iterator();
                    while (itrSNP.hasNext()) {
                        SNP snpTerm = itrSNP.next();
                        String[] strSub = new String[snpFeatureNum + 3 + pNum];
                        strSub[0] = snpTerm.getRsID();
                        strSub[1] = String.valueOf(snpTerm.getPhysicalPosition());
                        strSub[2] = VAR_FEATURE_NAMES[snpTerm.getGeneFeature()];
                        DoubleArrayList values = snpTerm.getAFeatureValue();// Has the function of outputing variant feature been finished?
                        for (int t = 0; t < snpFeatureNum; t++) {
                            strSub[t + 3] = String.valueOf(values.getQuick(t));
                        }

                        noPass = true;
                        for (int t = 0; t < pNum; t++) {
                            if (snpPCutOff > snpTerm.getpValues()[t]) {
                                noPass = false;
                            }
                            strSub[snpFeatureNum + 3 + t] = String.valueOf(snpTerm.getpValues()[t]);
                        }
                        if (noPass) {
                            continue;
                        }

                        lstOutput2.add(strSub);
                    }
                }

                strTitle = new String[snpFeatureNum + 3 + pNum];
                System.arraycopy(gmeTerm.getpValueNames(), 0, strTitle, snpFeatureNum + 3, pNum);
                vfns = gmeTerm.getVariantFeatureNames();
                strVFNS = vfns.toArray(new String[vfns.size()]);
                strTitle[0] = "SNP";
                strTitle[1] = "Position";
                strTitle[2] = "GeneFeature";
                System.arraycopy(strVFNS, 0, strTitle, 3, strVFNS.length);
                lstOutput2.add(0, strTitle);
            }//Add title!!!!

            JFileChooser jfcSave = null;
            if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
                jfcSave = new JFileChooser(GlobalManager.lastAccessedPath);
            } else {
                jfcSave = new JFileChooser();
            }
            jfcSave.setDialogTitle("Save " + strContent);
            FileNameExtensionFilter fnefFilter = null;
            if (formatComboBox.getSelectedIndex() == 0) {
                fnefFilter = new FileNameExtensionFilter("*.xlsx", "xlsx");
            } else if (formatComboBox.getSelectedIndex() == 1) {
                fnefFilter = new FileNameExtensionFilter("*.xls", "xls");
            } else {
                fnefFilter = new FileNameExtensionFilter("*.txt", "txt");
            }
            jfcSave.setFileFilter(fnefFilter);
            int intResult = jfcSave.showSaveDialog(this);
            if (intResult == JFileChooser.APPROVE_OPTION) {
                File fleFile = jfcSave.getSelectedFile();
                if (formatComboBox.getSelectedIndex() == 0) {
                    if (!fleFile.getPath().endsWith("xlsx")) {
                        fleFile = new File(fleFile.getPath() + ".xlsx");
                    }
                    if (contentComboBox.getSelectedIndex() == 0) {
                        LocalExcelFile.WriteArray2XLSXFile(fleFile.getCanonicalPath(), null, lstOutput);
                    } else if (contentComboBox.getSelectedIndex() == 1) {
                        LocalExcelFile.WriteArray2XLSXFile(fleFile.getCanonicalPath(), null, lstOutput);
                    } else {
                        LocalExcelFile.WriteArray2XLSXFile(fleFile.getCanonicalPath(), null, lstOutput, lstOutput2);
                    }
                } else if (formatComboBox.getSelectedIndex() == 1) {
                    if (!fleFile.getPath().endsWith("xls")) {
                        fleFile = new File(fleFile.getPath() + ".xls");
                    }
                    if (contentComboBox.getSelectedIndex() == 0) {
                        LocalExcelFile.writeArray2ExcelFile(fleFile.getCanonicalPath(), null, lstOutput);
                    } else if (contentComboBox.getSelectedIndex() == 1) {
                        LocalExcelFile.writeArray2ExcelFile(fleFile.getCanonicalPath(), null, lstOutput);
                    } else {
                        LocalExcelFile.writeArray2ExcelFile(fleFile.getCanonicalPath(), null, lstOutput, lstOutput2);
                    }

                } else {
                    if (!fleFile.getPath().endsWith("txt")) {
                        fleFile = new File(fleFile.getPath() + ".txt");
                    }
                    if (contentComboBox.getSelectedIndex() == 0) {
                        LocalFile.writeData(fleFile.getCanonicalPath(), lstOutput, "\t", false);
                    } else if (contentComboBox.getSelectedIndex() == 1) {
                        LocalFile.writeData(fleFile.getCanonicalPath(), lstOutput, "\t", false);
                    } else {
                        LocalFile.writeData(fleFile.getCanonicalPath(), lstOutput, lstOutput2, "\t", false);
                    }
                }
                String info = "The output are stored in " + fleFile.getCanonicalPath();
                NotifyDescriptor nd = new NotifyDescriptor.Message(info, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
                StatusDisplayer.getDefault().setStatusText(info);

            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void contentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentComboBoxActionPerformed
        // TODO add your handling code here:
        contentComboBox.hidePopup();
    }//GEN-LAST:event_contentComboBoxActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            // TODO add your handling code here:
            int intFlag = multiMethodComboBox.getSelectedIndex();
            double dblER = Double.valueOf(jTextField2.getText());
            //List<String[]> lstCurrentGene = aomGeneTermModel.getDataList();
            //List<String[]> lstCurrentGene=lstGeneTerm;

            DoubleArrayList dalPVs = new DoubleArrayList();
            for (String[] strTerm : lstGeneTerm) {
                if (strTerm[1].equals("NA")) {
                    continue;
                }
                dalPVs.add(Double.valueOf(strTerm[1]));
            }

            DoubleArrayList adjustedP = new DoubleArrayList();
            double dblCutoff = 0;
            switch (intFlag) {
                case 0:
                    dblCutoff = MultipleTestingMethod.BenjaminiHochbergFDR("Genes", dblER, dalPVs, adjustedP);
                    break;
                case 1:
                    dblCutoff = MultipleTestingMethod.BenjaminiYekutieliFDR("Genes", dblER, dalPVs, adjustedP);
                    break;
                case 2:
                    dblCutoff = dblER / dalPVs.size();
                    MultipleTestingMethod.logInfo("Genes", "Standard Bonferron", dblCutoff);
                    break;
            }
            int sigGeneNum = 0;
            int i = 0;
            double p;
            for (String[] strTerm : lstGeneTerm) {
                if (strTerm[1].equals("NA")) {
                    continue;
                }
                p = Double.valueOf(strTerm[1]);
                boolean boolSignificant = p <= dblCutoff;
                if (boolSignificant) {
                    sigGeneNum++;
                }
                switch (intFlag) {
                    case 0:
                    case 1:
                        strTerm[2] = String.valueOf(adjustedP.getQuick(i));
                        break;
                    case 2:
                        p = p * dalPVs.size();
                        strTerm[2] = String.valueOf(p > 1 ? 1 : p);
                        break;
                    default:
                        strTerm[2] = "-";
                }
                i++;
            }

            Logger logOutput = Logger.getRootLogger();
            String strOutput1 = sigGeneNum + " genes pass the threshold!\n";
            logOutput.info(strOutput1);
            if (GlobalManager.aotcWindow != null) {
                GlobalManager.aotcWindow.insertMessage(strOutput1);
            }

            aomGeneTermModel.fireTableDataChanged();

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        String strText = jTextPane1.getText();
        StringTokenizer st = new StringTokenizer(strText);
        Set<String> strElements = new HashSet<String>();
        while (st.hasMoreTokens()) {
            strElements.add(st.nextToken().trim());
        }

        List<String[]> lstGeneSearchTerm = new ArrayList();
        List<Integer> lstIndex = new ArrayList();
        for (String strElement : strElements) {
            for (int j = 0; j < lstGeneTerm.size(); j++) {
                if (strElement.equals(lstGeneTerm.get(j)[0])) {
                    lstIndex.add(j);
                    break;
                }
            }
        }
        Collections.sort(lstIndex);
        for (Integer lstIndex1 : lstIndex) {
            lstGeneSearchTerm.add(lstGeneTerm.get(lstIndex1));
        }
        aomGeneTermModel.setDataList(lstGeneSearchTerm);
        aomGeneTermModel.fireTableDataChanged();
        if (lstGeneTerm.size() > 0) {
            showSNPsofGene(0);
        } else {
            lstSNPTerm.clear();
            aomSNPTermModel.setDataList(lstSNPTerm);
            aomSNPTermModel.fireTableDataChanged();
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox contentComboBox;
    private javax.swing.JComboBox formatComboBox;
    private javax.swing.JTextField genePValueCutTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JComboBox multiMethodComboBox;
    private javax.swing.JTextField variantPValueCutTextField;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        result = Utilities.actionsGlobalContext().lookupResult(GeneBasedAssociation.class);
        result.addLookupListener(this);

    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
        result.removeLookupListener(this);
        result = null;
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

    private boolean handleCancel() {

        if (null == theTask) {
            return false;
        }
        return theTask.cancel();
    }

    class LoadGenomeDataSwingWorker extends SwingWorker<Void, String> {

        GeneBasedAssociation event;

        boolean succeed = false;
        ProgressHandle ph = ProgressHandleFactory.createHandle("Loading analysis genome task", new Cancellable() {
            @Override
            public boolean cancel() {
                return handleCancel();
            }
        });

        public LoadGenomeDataSwingWorker(GeneBasedAssociation event) {
            this.event = event;
        }

        @Override
        protected Void doInBackground() {
            try {
                ph.start(); //we must start the PH before we swith to determinate
                ph.switchToIndeterminate();
                GlobalManager.isLoadingGenome = true;
                publish("Loading analysis genomes.... Please wait for a while!");
                List<String> varWeighNames = new ArrayList<String>();

                if (event.isMultVariateTest()) {
                    pValueGeneList = event.loadGenePValuesfromDisk("MulVar", null);
                } else {
                    pValueGeneList = event.loadGenePValuesfromDisk(event.getPValueSources().get(0), varWeighNames);
                }

                Collections.sort(pValueGeneList, new PValueGeneComparator());
                Iterator<PValueGene> itrTerm = pValueGeneList.iterator();
                gmeTerm = event.getGenome();
                mapG2C = gmeTerm.loadGeneGenomeIndexes2Buf();
                chrTerms = gmeTerm.loadAllChromosomes2Buf();

                int intNo = 0;
                //altGeneSet.clear();
                lstGeneTerm.clear();
                dalPValues.clear();
                mapSymbol2Gene.clear();
                String strRows[][] = new String[pValueGeneList.size()][];
                //Start with the second chrom
                int weightNum = varWeighNames.size() - 2;
                if (weightNum > 0) {
                    String[] newTitle = new String[6];
                    System.arraycopy(strGeneTerm, 0, newTitle, 0, newTitle.length);
                    strGeneTerm = new String[newTitle.length + weightNum];
                    System.arraycopy(newTitle, 0, strGeneTerm, 0, newTitle.length);

                    for (int i = 0; i < weightNum; i++) {
                        strGeneTerm[6 + i] = varWeighNames.get(i + 2);
                    }
                    aomGeneTermModel.setTitle(strGeneTerm);
                }
                while (itrTerm.hasNext()) {
                    PValueGene pvgTerm = itrTerm.next();
                    String strSymbol = pvgTerm.getSymbol();
                    double dblPValue = pvgTerm.getpValue();

                    int[] intPositions = mapG2C.get(strSymbol);
                    if (intPositions == null) {
                        continue;
                    }

                    Gene gneTerm1 = chrTerms[intPositions[0]].genes.get(intPositions[1]);
                    //Each weight has a different gene-based p-value
                    if (weightNum > 0) {
                        strRows[intNo] = new String[6 + weightNum];
                        strRows[intNo][0] = strSymbol;
                        strRows[intNo][1] = Util.formatPValue(dblPValue);
                        strRows[intNo][2] = String.valueOf(gneTerm1.getEntrezID());
                        strRows[intNo][3] = chrTerms[intPositions[0]].getName();
                        strRows[intNo][4] = String.valueOf(gneTerm1.getStart());
                        strRows[intNo][5] = geneGroups[gneTerm1.getGeneGroupID()];
                        for (int i = 0; i < weightNum; i++) {
                            strRows[intNo][6 + i] = String.valueOf(pvgTerm.pValues[i]);
                        }
                    } else {
                        strRows[intNo] = new String[]{strSymbol, Util.formatPValue(dblPValue), String.valueOf(gneTerm1.getEntrezID()), chrTerms[intPositions[0]].getName(), String.valueOf(gneTerm1.getStart()), geneGroups[gneTerm1.getGeneGroupID()]};
                    }
                    mapSymbol2Gene.put(strSymbol, gneTerm1);
                    //altGeneSet.add(gneTerm1);
                    dalPValues.add(dblPValue);
                    intNo++;
                }

                DoubleArrayList adjustedP = new DoubleArrayList();
                double dblPValueFDR = MultipleTestingMethod.BenjaminiHochbergFDR("Genes", Double.valueOf(jTextField2.getText()), dalPValues, adjustedP);
                for (int i = 0; i < dalPValues.size(); i++) {
                    // boolean boolSignificant = dalPValues.get(i) <= dblPValueFDR;
                    strRows[i][2] = String.valueOf(adjustedP.getQuick(i));
                    lstGeneTerm.add(strRows[i]);
                }

                String genomeVersion = gmeTerm.getFinalBuildGenomeVersion();
                if (!genomeVersion.equals("hg19")) {
                    File CHAIN_FILE = new File(GlobalManager.RESOURCE_PATH + "liftOver/" + genomeVersion + "ToHg19.over.chain.gz");
                    if (!CHAIN_FILE.exists()) {
                        if (!CHAIN_FILE.getParentFile().exists()) {
                            CHAIN_FILE.getParentFile().mkdirs();
                        }
                        String url = "http://hgdownload.cse.ucsc.edu/goldenPath/" + genomeVersion + "/liftOver/" + CHAIN_FILE.getName();
                        //HttpClient4API.downloadAFile(url, CHAIN_FILE);
                        HttpClient4API.simpleRetriever(url, CHAIN_FILE.getCanonicalPath(), GlobalManager.proxyBean);
                        Zipper ziper = new Zipper();
                        ziper.extractTarGz(CHAIN_FILE.getCanonicalPath(), CHAIN_FILE.getParent());
                    }
                    CHAIN_FILE = new File(GlobalManager.RESOURCE_PATH + "liftOver/" + genomeVersion + "ToHg19.over.chain");
                    liftOver = new LiftOver(CHAIN_FILE);
                }
                publish("Analysis genome have been loaded!");

            } catch (InterruptedException ex) {
                StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
                java.util.logging.Logger.getLogger(ShowGeneResultTopComponent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

            } catch (Exception ex) {
                ex.printStackTrace();
                StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
                java.util.logging.Logger.getLogger(ShowGeneResultTopComponent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

            succeed = true;

            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            // TODO Auto-generated method stub  
            for (String message : chunks) {
                LOG.info(message);
                StatusDisplayer.getDefault().setStatusText(message);
            }
        }

        @Override
        protected void done() {
            try {
                String message;
                if (succeed) {
                    aomGeneTermModel.setDataList(lstGeneTerm);
                    aomGeneTermModel.fireTableStructureChanged();
                    if (lstGeneTerm.size() > 0) {
                        showSNPsofGene(0);
                    }
                }
                ph.finish();
                GlobalManager.isLoadingGenome = false;
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends GeneBasedAssociation> allEvents = result.allInstances();
        try {
            if (!allEvents.isEmpty()) {
                event = allEvents.iterator().next();
                this.open();
                this.requestActive();
                if (event != null && event instanceof GeneBasedAssociation) {
                    if (GlobalManager.isLoadingGenome) {
                        String infor = "Be patient! I am loading the analysis genome!";
                        JOptionPane.showMessageDialog(this, infor, "Warnning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    TopComponent outputWindow = WindowManager.getDefault().findTopComponent("AnalysisOutputTopComponent");
                    //Determine if it is opened
                    if (outputWindow != null && !outputWindow.isOpened()) {
                        outputWindow.open();
                    }
                    LoadGenomeDataSwingWorker loader = new LoadGenomeDataSwingWorker(event);
                    theTask = RP.create(loader); //the task is not started yet
                    theTask.schedule(0); //start the task
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent cme) {
        try {
            testData2D();
            int intX = cme.getTrigger().getX();
            int intY = cme.getTrigger().getY();
            Point2D point2d = cplCanvas.translateScreenToJava2D(new Point(intX, intY));
            List<Integer> altCandidates = testOverlap(point2d);
            if (altCandidates.size() == 1) {
                callURL(Integer.valueOf(altCandidates.get(0).toString()));
            }
            if (altCandidates.size() > 1) {
                int intCandidate = testDistance(point2d, altCandidates);
                callURL(intCandidate);
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent cme) {
        jPanel1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        testData2D();
        int intX = cme.getTrigger().getX();
        int intY = cme.getTrigger().getY();
        Point2D point2d = cplCanvas.translateScreenToJava2D(new Point(intX, intY));
        List<Integer> altCandidates = testOverlap(point2d);
        if (altCandidates.size() == 1) {
            wowInfo(Integer.valueOf(altCandidates.get(0).toString()), intX, intY);
        }
        if (altCandidates.size() > 1) {
            int intCandidate = testDistance(point2d, altCandidates);
            wowInfo(intCandidate, intX, intY);
        }
    }

    public List<Integer> testOverlap(Point2D pnt) {

        List<Integer> altCandidates = new ArrayList<Integer>();
        for (int i = 0; i < dblData2D[0].length; i++) {
            double dblXDiff = Math.abs(pnt.getX() - dblData2D[0][i]);
            double dblYDiff = Math.abs(pnt.getY() - dblData2D[1][i]);
            if (dblXDiff <= 4 && dblYDiff <= 4) {
                altCandidates.add(i);
            }
        }
        return altCandidates;
    }

    public int testDistance(Point2D pnt, List<Integer> altCandidates) {

        double dblMin = Double.MAX_VALUE;
        int intMin = 0;
        for (int i = 0; i < altCandidates.size(); i++) {
            double dblDistance = Math.sqrt(Math.pow(pnt.getX() - dblData2D[0][Integer.valueOf(altCandidates.get(i).toString())], 2) + Math.pow(pnt.getY() - dblData2D[1][Integer.valueOf(altCandidates.get(i).toString())], 2));
            if (dblDistance < dblMin) {
                dblMin = dblDistance;
                intMin = altCandidates.get(i);
            }
        }
        return intMin;
    }

    public void callURL(int intIndex) throws URISyntaxException, IOException {

        String strURL = "http://jjwanglab.org/gwasrap/gwasrank/gwasrank/quickrap/";
        int longPosition = gneTerm.snps.get(intIndex).getPhysicalPosition();
        String strChr = chrTerms[mapG2C.get(gneTerm.getSymbol())[0]].getName();

        if (liftOver != null && longPosition > 0) {
            Interval interval = new Interval("chr" + strChr, longPosition, longPosition);
            Interval int2 = liftOver.liftOver(interval);
            if (int2 != null) {
                longPosition = int2.getStart();
            }
        }

        strURL = strURL + strChr + "/" + longPosition;
        URLOpener.openURL(strURL);
    }

    public void wowInfo(int intIndex, int intX, int intY) {
        if (intIndex >= gneTerm.snps.size()) {
            return;
        }
        int longPosition = gneTerm.snps.get(intIndex).getPhysicalPosition();
        String strChr = chrTerms[mapG2C.get(gneTerm.getSymbol())[0]].getName();
        String strID = gneTerm.snps.get(intIndex).getRsID();
        double dblPvalue = gneTerm.snps.get(intIndex).getpValues()[0];
        JPopupMenu jpmSNPInfo = new JPopupMenu();
        jpmSNPInfo.add("RsID: " + strID);
        jpmSNPInfo.add("Chr: " + strChr);
        jpmSNPInfo.add("Position: " + longPosition);
        jpmSNPInfo.add("p-value: " + dblPvalue);
        jpmSNPInfo.setBackground(new Color(248, 248, 255));
        jpmSNPInfo.setBorderPainted(false);
        jpmSNPInfo.setEnabled(false);
        jpmSNPInfo.show(jPanel1, intX + 15, intY + 15);
    }

    private void testData2D() {
        if (true == boolTest) {
            return;
        }
        ChartRenderingInfo criChart = cplCanvas.getChartRenderingInfo();
        java.awt.geom.Rectangle2D rectangle2d = criChart.getPlotInfo().getDataArea();
        XYPlot xyplot = cplCanvas.getChart().getXYPlot();
        ValueAxis vasX = xyplot.getDomainAxis();
        ValueAxis vasY = xyplot.getRangeAxis();
        for (int j = 0; j < dblData2D[0].length; j++) {
            double dblFlag = vasX.valueToJava2D(dblData[0][j], rectangle2d, xyplot.getDomainAxisEdge());
            if (dblFlag == dblData2D[0][j]) {
                return;
            }
            dblData2D[0][j] = dblFlag;
            dblData2D[1][j] = vasY.valueToJava2D(dblData[1][j], rectangle2d, xyplot.getRangeAxisEdge());
            boolTest = true;
        }
    }

}
