/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.dialog;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.annotations.AnnotatingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import org.cobi.kgg.business.entity.Constants;
import org.cobi.kgg.business.entity.FileString;
import org.cobi.kgg.business.entity.GenePValueComparator;
import org.cobi.kgg.business.entity.GeneVertex;
import org.cobi.kgg.business.entity.PPIBasedAssociation;
import org.cobi.kgg.business.entity.PPIEdge;
import org.cobi.kgg.business.entity.PPISet;
import org.cobi.kgg.business.entity.PValueGene;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.action.BuildGenome;
import org.cobi.kgg.ui.action.ScanPPIBasedAssociation;
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
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.cobi.kgg.ui.dialog//PPIResultViewer//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "PPIResultViewerTopComponent",
        iconBase = "org/cobi/kgg/ui/png/16x16/Target.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "BioModule", id = "org.cobi.kgg.ui.dialog.PPIResultViewerTopComponent")
@ActionReference(path = "Menu/BioModule", position = 333)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PPIResultViewerAction",
        preferredID = "PPIResultViewerTopComponent")
@Messages({
    "CTL_PPIResultViewerAction=View Gene-pairs",
    "CTL_PPIResultViewerTopComponent=Show gene pairs",
    "HINT_PPIResultViewerTopComponent=This is a Gene Pairs ResultViewer window"
})
public final class PPIResultViewerTopComponent extends TopComponent implements LookupListener, Constants {

    private final static Logger LOG = Logger.getLogger(PPIResultViewerTopComponent.class.getName());
    VisualizationViewer<GeneVertex, PPIEdge> graphViewer;
    List<edu.uci.ics.jung.algorithms.layout.Layout<GeneVertex, PPIEdge>> layouts;
    int width = 800;
    int height = 800;
    Graph<GeneVertex, PPIEdge> totalGraph;
    Graph<GeneVertex, PPIEdge> selectedGraph;
    DefaultListModel<GeneVertex> significantGeneListModel = new DefaultListModel<GeneVertex>();
    String[] layoutComb = {"KKLayout", "FRLayout", "FRLayout2", "CircleLayout", "SpringLayout", "SpringLayout2", "ISOMLayout"};
    AnnotatingModalGraphMouse<GeneVertex, PPIEdge> graphMouse;
    ScanPPIBasedAssociation scanPPI;
    private PPIBasedAssociation ppibaCurrent = null;
    private Lookup.Result<PPIBasedAssociation> ppibaResult = null;
    private PPIBasedAssociation ppibaEvent = null;
    private List<PPISet> lstPPISet = null;
    private List<PValueGene> lstPVG = null;
    private Map<String, PValueGene> mapSymbol2PVG = null;
    private double[] dblCutoffs = null;
    RunningResultViewerTopComponent runningResultTopComp = null;

    private final static RequestProcessor RP = new RequestProcessor("Loading analysis genome tas", 1, true);
    private RequestProcessor.Task theTask = null;

    private boolean handleCancel() {

        if (null == theTask) {
            return false;
        }
        return theTask.cancel();
    }

    class LoadGenomeDataSwingWorker extends SwingWorker<Void, String> {

        PPIBasedAssociation ppibaEvent;
        final ProgressHandle ph;
        boolean succeed = false;
        StringBuilder resultInfo = new StringBuilder();

        public LoadGenomeDataSwingWorker(PPIBasedAssociation event) {
            this.ppibaEvent = event;
            ph = ProgressHandleFactory.createHandle("Loading analysis genome task", new Cancellable() {
                @Override
                public boolean cancel() {
                    return handleCancel();
                }
            });
            ph.start(); //we must start the PH before we swith to determinate
            ph.switchToIndeterminate();
        }

        @Override
        protected Void doInBackground() {

            try {
                GlobalManager.isLoadingGenome = true;
                publish("Loading analysis genomes.... Please wait for a while!");
            
                if (pvalueSoureComboBox.getSelectedItem() == null) {
                    return null;
                }
                String pSourceName = pvalueSoureComboBox.getSelectedItem().toString();
                if (pSourceName == null) {
                    return null;
                }

                File ppiFile = (File) interactionComboBox.getSelectedItem();
                ppibaCurrent.setPpIAssocMultiTestedMethod(stageIPPIMethodConbomBox.getSelectedItem().toString());
                ppibaCurrent.setPpIAssocNominalError(Double.parseDouble(stageIPPPValueTextField.getText()));
                ppibaCurrent.setPpIHetroTestedMethod(heteroComboBox.getSelectedItem().toString());
                ppibaCurrent.setPpIHetroNominalError(Double.parseDouble(heterogeneityTestThresholdText.getText()));
                ppibaCurrent.setPpIKeepBothAssocMethod(ppiNetworkGenePvalueConbomBox.getSelectedItem().toString());
                ppibaCurrent.setPpIKeepBothAssocNominalError(Double.parseDouble(genePValueTextField.getText()));
                if (enableCandidateGeneCheckBox2.isSelected() && GlobalManager.candiGeneFilesModel.getSize() > 0) {
                    ppibaCurrent.setCanidateGeneSetFile(((File) candidateGeneComboBox2.getSelectedItem()));
                } else {
                    ppibaCurrent.setCanidateGeneSetFile(null);
                }
                lstPPISet = ppibaCurrent.loadPPIAssociation(pSourceName, ppiFile.getName());
                if (lstPPISet == null) {
                    String infor = "The Gene-pair-based association results of " + pSourceName + "@" + ppiFile.getName() + " are not availble!";
                    NotifyDescriptor nd = new NotifyDescriptor.Message(infor, NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                    return null;
                }

                lstPVG = ppibaCurrent.getGeneScan().loadGenePValuesfromDisk(pSourceName,null);
               
                Collections.sort(lstPVG, new GenePValueComparator());
                int geneNum = lstPVG.size();
                mapSymbol2PVG = new HashMap<String, PValueGene>();
                for (int i = 0; i < geneNum; i++) {
                    mapSymbol2PVG.put(lstPVG.get(i).getSymbol(), lstPVG.get(i));
                }

                scanPPI = new ScanPPIBasedAssociation(ppibaCurrent, runningResultTopComp);

                dblCutoffs = scanPPI.pPISetSigTest(lstPPISet, mapSymbol2PVG, true, true, resultInfo);
                scanPPI.setOnlyVisualizeSignificant(vsualizePPICheckBox.isSelected());
                totalGraph = new UndirectedSparseGraph<GeneVertex, PPIEdge>();
                String resInfo = scanPPI.extractPPISetNetworkSigPairHetFilterForShow(lstPPISet, mapSymbol2PVG, dblCutoffs[1], dblCutoffs[0], totalGraph);
                resultInfo.append("\n");
                resultInfo.append(resInfo);
                Collection<GeneVertex> vertex = totalGraph.getVertices();
                significantGeneListModel.clear();
                Iterator<GeneVertex> iter = vertex.iterator();
                while (iter.hasNext()) {
                    GeneVertex v = iter.next();
                    if (v.isIsSignificant()) {
                        significantGeneListModel.addElement(v);
                    }
                }
            
                layouts.clear();
                layouts.add(new KKLayout<GeneVertex, PPIEdge>(totalGraph));
                layouts.add(new FRLayout<GeneVertex, PPIEdge>(totalGraph));
                layouts.add(new FRLayout2<GeneVertex, PPIEdge>(totalGraph));
                layouts.add(new CircleLayout<GeneVertex, PPIEdge>(totalGraph));
                layouts.add(new SpringLayout<GeneVertex, PPIEdge>(totalGraph));
                layouts.add(new SpringLayout2<GeneVertex, PPIEdge>(totalGraph));
                layouts.add(new ISOMLayout<GeneVertex, PPIEdge>(totalGraph));

                publish("Analysis genome has been loaded!");
            

                /*
                //due to a gene with low p-values may save more interaction partners, there is a bias. It is not suitable to use this approach
                int observedEdgeNum = totalGraph.getEdgeCount();
                int observedVerNum = totalGraph.getVertexCount();
                int samplingNum = Integer.parseInt(sampleNumTextField.getText());
                double passingNum = 0;
                int allPPISize = lstPPISet.size();
                if (desityCheckBox.isSelected()) {
                 
                     RandomEngine randomGenerator = new cern.jet.random.engine.MersenneTwister(new java.util.Date());
                     Graph<String, Integer> allGraph = new UndirectedSparseGraph<String, Integer>();
                     for (int t = 0; t < allPPISize; t++) {
                     PPISet ppi = lstPPISet.get(t);
                     List<String> geneSymbs = ppi.getGeneSymbs();
                     //To be revised for Interaction sub-network
                     String symbolA = geneSymbs.get(0);
                     String symbolB = geneSymbs.get(1);
                     allGraph.addEdge(t, symbolA, symbolB);
                     }

                     List<String> allInovledGenes = new ArrayList(allGraph.getVertices());
                     long[] selectedIndexes = new long[observedVerNum];
                     int randomEdgeNum = 0;
                     for (int i = 0; i < samplingNum; i++) {
                     RandomSampler.sample(observedVerNum, allInovledGenes.size(), observedVerNum, 0, selectedIndexes, 0, randomGenerator);
                     randomEdgeNum = 0;
                     for (int t = 0; t < observedVerNum; t++) {
                     String symbolA = allInovledGenes.get((int) selectedIndexes[t]);
                     for (int s = t + 1; s < observedVerNum; s++) {
                     String symbolB = allInovledGenes.get((int) selectedIndexes[s]);
                     Integer a = allGraph.findEdge(symbolA, symbolB);
                     if (a != null) {
                     randomEdgeNum++;
                     }
                     }
                     }
                     if (randomEdgeNum >= observedEdgeNum) {
                     passingNum += 1.0;
                     }
                     }
                     String info = Util.doubleToString(passingNum / samplingNum * 100, 2) + "% networks have edges less than or equal to " + observedEdgeNum + "!";
                     resultInfo.append("\n");
                     resultInfo.append(info);
                     publish(info);
                    

                    RandomEngine randomGenerator = new cern.jet.random.engine.MersenneTwister(new java.util.Date());

                    long[] selectedIndexes = new long[observedEdgeNum];

                    Set<String> geneSymbos = new HashSet<String>();
                    for (int i = 0; i < samplingNum; i++) {
                        RandomSampler.sample(observedEdgeNum, allPPISize, observedEdgeNum, 0, selectedIndexes, 0, randomGenerator);
                        geneSymbos.clear();
                        for (int t = 0; t < observedEdgeNum; t++) {
                            PPISet symbols = lstPPISet.get((int) selectedIndexes[t]);
                            geneSymbos.addAll(symbols.getGeneSymbs());
                        }
                        if (geneSymbos.size() <= observedVerNum) {
                            passingNum += 1.0;
                        }
                    }
                    String info = Util.doubleToString(passingNum / samplingNum * 100, 2) + "% networks have edges less than or equal to " + observedEdgeNum + "!";
                    resultInfo.append("\n");
                    resultInfo.append(info);
                    publish(info);
                }
*/
               

            } /* catch (InterruptedException ex) {
             StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
             java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

             }*/ catch (Exception ex) {
                ex.printStackTrace();
                StatusDisplayer.getDefault().setStatusText("Building analysis genome task was CANCELLED!");
                java.util.logging.Logger.getLogger(BuildGenome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
           
            succeed = true;
            GlobalManager.isLoadingGenome = false;

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
                    Layout<GeneVertex, PPIEdge> l = layouts.get(layoutComboBox.getSelectedIndex());
                    l.setInitializer(graphViewer.getGraphLayout());
                    l.setSize(graphViewer.getSize());
                    LayoutTransition<GeneVertex, PPIEdge> lt = new LayoutTransition<GeneVertex, PPIEdge>(graphViewer, graphViewer.getGraphLayout(), l);
                    Animator animator = new Animator(lt);
                    animator.start();
                    graphViewer.getRenderContext().getMultiLayerTransformer().setToIdentity();
                    graphViewer.repaint();

                    FormatShowingDialog dialog = new FormatShowingDialog(new javax.swing.JFrame(), true, "Multiple testing results for Gene-pair-based association", resultInfo.toString());
                    dialog.setLocationRelativeTo(GlobalManager.mainFrame);
                    dialog.setVisible(true);
                }
                GlobalManager.isLoadingGenome = false;
                ph.finish();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends PPIBasedAssociation> allEvents = ppibaResult.allInstances();
        if (!allEvents.isEmpty()) {
            ppibaEvent = allEvents.iterator().next();
            if (ppibaEvent != null && ppibaEvent instanceof PPIBasedAssociation) {
                ppibaCurrent = ppibaEvent;
                List<String> pSources = ppibaCurrent.getPValueSources();
                pvalueSoureComboBox.removeAllItems();
                for (String pSource : pSources) {
                    pvalueSoureComboBox.addItem(pSource);
                }
                //pvalueSoureComboBoxActionPerformed(null);

                FileString[] ppiFiles = ppibaCurrent.getPpIDBFiles();
                interactionComboBox.removeAllItems();
                if (ppibaCurrent.isIsToMergePPISet()) {
                    FileString mgFile = new FileString("merged");
                    interactionComboBox.addItem(mgFile);
                } else {
                    for (FileString pSource : ppiFiles) {
                        interactionComboBox.addItem(pSource);
                    }
                }
                this.open();
                this.requestActive();

                // LoadGenomeDataSwingWorker loader = new LoadGenomeDataSwingWorker(ppibaCurrent);
                // loader.execute();
                try {
                    //  showPPIs();
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }

    /**
     * @author danyelf
     */
    public class MyVertexDrawPaintFunction<V> implements Transformer<V, Paint> {

        @Override
        public Paint transform(V v) {
            return Color.black;
        }
    }

    public class MyVertexFillPaintFunction<V> implements Transformer<V, Paint> {

        @Override
        public Paint transform(V v) {
            GeneVertex v1 = (GeneVertex) v;

            if (v1.isIsSignificant() && v1.isIsSeed()) {
                return Color.MAGENTA;
            } else if (v1.isIsSignificant()) {
                return Color.RED;
            } else if (v1.isIsSeed()) {
                return Color.GREEN;
            } else {
                return Color.GRAY;
            }
        }
    }

    public PPIResultViewerTopComponent() {
        totalGraph = new UndirectedSparseGraph<GeneVertex, PPIEdge>();
        layouts = new ArrayList<Layout<GeneVertex, PPIEdge>>();
        layouts.add(new KKLayout<GeneVertex, PPIEdge>(totalGraph));
        layouts.add(new FRLayout<GeneVertex, PPIEdge>(totalGraph));
        layouts.add(new FRLayout2<GeneVertex, PPIEdge>(totalGraph));
        layouts.add(new CircleLayout<GeneVertex, PPIEdge>(totalGraph));
        layouts.add(new SpringLayout<GeneVertex, PPIEdge>(totalGraph));
        layouts.add(new SpringLayout2<GeneVertex, PPIEdge>(totalGraph));
        layouts.add(new ISOMLayout<GeneVertex, PPIEdge>(totalGraph));

        graphViewer = new VisualizationViewer<GeneVertex, PPIEdge>(layouts.get(0), new Dimension(width, height));
        graphViewer.setBackground(Color.white);
        RenderContext<GeneVertex, PPIEdge> rc = graphViewer.getRenderContext();
        rc.setArrowFillPaintTransformer(new ConstantTransformer(Color.white));
        rc.setEdgeShapeTransformer(new edu.uci.ics.jung.visualization.decorators.EdgeShape.Line<GeneVertex, PPIEdge>());
        rc.setVertexDrawPaintTransformer(new MyVertexDrawPaintFunction<GeneVertex>());
        rc.setVertexFillPaintTransformer(new MyVertexFillPaintFunction<GeneVertex>());
        //rc.setVertexLabelTransformer(new ToStringLabeller());
        rc.setVertexLabelTransformer(new Transformer<GeneVertex, String>() {
            @Override
            public String transform(GeneVertex arg0) {
                String p = arg0.getAnnotation();
                if (p == null) {
                    return "NA";
                }
                p = p.substring(p.indexOf(":") + 2);
                String annot = arg0 + "(" + p + ")";
                return annot;
            }
        });
        runningResultTopComp = (RunningResultViewerTopComponent) WindowManager.getDefault().findTopComponent("RunningResultViewerTopComponent");
        /*
         rc.setEdgeLabelTransformer(new Transformer<PPIEdge, String>() {
        
         public String transform(PPIEdge arg0) {
         String p = String.valueOf(arg0.getScore());
         if (p == null) {
         return "NA";
         }
        
         String annot = arg0 + "(" + p + ")";
         return annot;
         }
         });
         */
        graphViewer.setEdgeToolTipTransformer(new Transformer<PPIEdge, String>() {
            @Override
            public String transform(PPIEdge arg0) {
                if (arg0.getpValue() == 1) {
                    return "NA";
                }
                String annot = "Gene-pair p-value: " + arg0.getpValue();
                if (arg0.getScore() > 0) {
                    annot += " confidence: " + arg0.getScore();
                }
                return annot;
            }
        });

        graphViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.E);
        graphViewer.setVertexToolTipTransformer(new Transformer<GeneVertex, String>() {
            @Override
            public String transform(GeneVertex arg0) {
                String annot = arg0.getSymbol();
                if (annot == null) {
                    return "NA";
                }
                return annot;
            }
        });

        graphViewer.setBackground(Color.white);
        AnnotatingGraphMousePlugin<GeneVertex, PPIEdge> annotatingPlugin = new AnnotatingGraphMousePlugin<GeneVertex, PPIEdge>(rc);
        graphMouse = new AnnotatingModalGraphMouse<GeneVertex, PPIEdge>(rc, annotatingPlugin);
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        graphViewer.setGraphMouse(graphMouse);
        graphViewer.addKeyListener(graphMouse.getModeKeyListener());
        graphViewer.setPreferredSize(new Dimension(width, height));
        //GraphZoomScrollPane gzsp = new GraphZoomScrollPane(graphViewer);
        initComponents();
        setName(Bundle.CTL_PPIResultViewerTopComponent());
        setToolTipText(Bundle.HINT_PPIResultViewerTopComponent());

        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        // setIconImage(InterfaceUtil.readImageIcon("logo1.png").getImage());
        int[] selectIndex = new int[significantGeneListModel.getSize()];
        for (int i = 0; i < selectIndex.length; i++) {
            selectIndex[i] = i;
        }
        testedGeneList.setSelectedIndices(selectIndex);
//        setName(Bundle.CTL_PPIResultViewerTopComponent());
//        setToolTipText(Bundle.HINT_PPIResultViewerTopComponent());
        //ppiScanComboBoxActionPerformed(null);     
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        modeBox = graphMouse.getModeComboBox();
        jLabel2 = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        hidePValueCheckBox = new javax.swing.JCheckBox();
        hideGeneSymbolsCheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        layoutComboBox = new javax.swing.JComboBox(layoutComb);
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        testedGeneList = new javax.swing.JList(significantGeneListModel);
        veiwPanel = new GraphZoomScrollPane(graphViewer);
        jPanel7 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        pvalueSoureComboBox = new javax.swing.JComboBox();
        enableCandidateGeneCheckBox2 = new javax.swing.JCheckBox();
        candidateGeneComboBox2 = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        interactionComboBox = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        stageIPPPValueTextField = new javax.swing.JTextField();
        stageIPPIMethodConbomBox = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        heteroComboBox = new javax.swing.JComboBox();
        heterogeneityTestThresholdText = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        ppiNetworkGenePvalueConbomBox = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        genePValueTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        onlyExportSignificantPPICheckBox = new javax.swing.JCheckBox();
        vsualizePPICheckBox = new javax.swing.JCheckBox();
        jButton3 = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(hidePValueCheckBox, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.hidePValueCheckBox.text")); // NOI18N
        hidePValueCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hidePValueCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(hideGeneSymbolsCheckBox, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.hideGeneSymbolsCheckBox.text")); // NOI18N
        hideGeneSymbolsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideGeneSymbolsCheckBoxActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jPanel3.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(zoomInButton, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.zoomInButton.text")); // NOI18N
        zoomInButton.setPreferredSize(new java.awt.Dimension(37, 23));
        zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(zoomOutButton, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.zoomOutButton.text")); // NOI18N
        zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(zoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(zoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zoomOutButton)))
        );

        layoutComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jLabel3.text")); // NOI18N

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jScrollPane2.border.title"))); // NOI18N

        testedGeneList.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jScrollPane2.setViewportView(testedGeneList);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modeBox, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(refreshButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hidePValueCheckBox)
                            .addComponent(layoutComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hideGeneSymbolsCheckBox)))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(refreshButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(modeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(layoutComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(hidePValueCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hideGeneSymbolsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        veiwPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jPanel5.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jLabel4.text")); // NOI18N

        pvalueSoureComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pvalueSoureComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(enableCandidateGeneCheckBox2, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.enableCandidateGeneCheckBox2.text")); // NOI18N
        enableCandidateGeneCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableCandidateGeneCheckBox2ActionPerformed(evt);
            }
        });

        candidateGeneComboBox2.setModel(GlobalManager.candiGeneFilesModel);
        candidateGeneComboBox2.setEnabled(false);
        candidateGeneComboBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                candidateGeneComboBox2ItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jLabel5.text")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enableCandidateGeneCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(candidateGeneComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pvalueSoureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(interactionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pvalueSoureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(interactionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(enableCandidateGeneCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(candidateGeneComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jPanel2.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jLabel6.text")); // NOI18N

        stageIPPPValueTextField.setText(org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.stageIPPPValueTextField.text")); // NOI18N

        stageIPPIMethodConbomBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Standard Bonferroni", "Benjamini & Hochberg (1995)", "Benjamini & Yekutieli (2001)", "Fixed p-value threshold" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stageIPPPValueTextField))
                    .addComponent(stageIPPIMethodConbomBox, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(stageIPPIMethodConbomBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel6)
                    .addComponent(stageIPPPValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jPanel4.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jLabel7.text")); // NOI18N

        heteroComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Higgins I2<=", "CochranQPValue>=" }));
        heteroComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heteroComboBoxActionPerformed(evt);
            }
        });

        heterogeneityTestThresholdText.setText(org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.heterogeneityTestThresholdText.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jLabel8.text")); // NOI18N

        ppiNetworkGenePvalueConbomBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Benjamini & Hochberg (1995)", "Benjamini & Yekutieli (2001)", "Standard Bonferroni", "Fixed p-value threshold" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jLabel11.text")); // NOI18N

        genePValueTextField.setText(org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.genePValueTextField.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(genePValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(heteroComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(heterogeneityTestThresholdText, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ppiNetworkGenePvalueConbomBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(heteroComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heterogeneityTestThresholdText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ppiNetworkGenePvalueConbomBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(genePValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jPanel6.border.title"))); // NOI18N

        onlyExportSignificantPPICheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(onlyExportSignificantPPICheckBox, "<html>Only export significant<br>gene pairs in file</html>");

        vsualizePPICheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(vsualizePPICheckBox, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.vsualizePPICheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(PPIResultViewerTopComponent.class, "PPIResultViewerTopComponent.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jButton3))
                    .addComponent(onlyExportSignificantPPICheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vsualizePPICheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(vsualizePPICheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlyExportSignificantPPICheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(49, 49, 49)
                                .addComponent(jButton1))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(veiwPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(veiwPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:

        pvalueSoureComboBoxActionPerformed(null);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        GeneVertex v;
        selectedGraph = new UndirectedSparseGraph<GeneVertex, PPIEdge>();
        Set<String> avaiblePPIs = new HashSet<String>();
        try {
            int[] selectedIx = testedGeneList.getSelectedIndices();
            for (int i = 0; i < selectedIx.length; i++) {
                v = significantGeneListModel.get(selectedIx[i]);
                extractSubGrph(v, avaiblePPIs);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        layouts.clear();
        layouts.add(new KKLayout<GeneVertex, PPIEdge>(selectedGraph));
        layouts.add(new FRLayout<GeneVertex, PPIEdge>(selectedGraph));
        layouts.add(new FRLayout2<GeneVertex, PPIEdge>(selectedGraph));
        layouts.add(new CircleLayout<GeneVertex, PPIEdge>(selectedGraph));
        layouts.add(new SpringLayout<GeneVertex, PPIEdge>(selectedGraph));
        layouts.add(new SpringLayout2<GeneVertex, PPIEdge>(selectedGraph));
        layouts.add(new ISOMLayout<GeneVertex, PPIEdge>(selectedGraph));
        Layout<GeneVertex, PPIEdge> l = layouts.get(layoutComboBox.getSelectedIndex());
        l.setInitializer(graphViewer.getGraphLayout());
        l.setSize(graphViewer.getSize());

        LayoutTransition<GeneVertex, PPIEdge> lt = new LayoutTransition<GeneVertex, PPIEdge>(graphViewer, graphViewer.getGraphLayout(), l);
        Animator animator = new Animator(lt);
        animator.start();
        graphViewer.getRenderContext().getMultiLayerTransformer().setToIdentity();
        graphViewer.repaint();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void hidePValueCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hidePValueCheckBoxActionPerformed
        RenderContext rc = graphViewer.getRenderContext();
        if (hideGeneSymbolsCheckBox.isSelected() && hidePValueCheckBox.isSelected()) {
            rc.setVertexLabelTransformer(new Transformer<GeneVertex, String>() {
                @Override
                public String transform(GeneVertex arg0) {
                    return "";
                }
            });
        } else if (!hideGeneSymbolsCheckBox.isSelected() && hidePValueCheckBox.isSelected()) {
            rc.setVertexLabelTransformer(new ToStringLabeller());
        } else if (hideGeneSymbolsCheckBox.isSelected() && !hidePValueCheckBox.isSelected()) {
            rc.setVertexLabelTransformer(new Transformer<GeneVertex, String>() {
                @Override
                public String transform(GeneVertex arg0) {
                    String p = arg0.getAnnotation();
                    if (p == null) {
                        return "NA";
                    }
                    p = p.substring(p.indexOf(":") + 2);
                    return p;
                }
            });
        } else {
            rc.setVertexLabelTransformer(new Transformer<GeneVertex, String>() {
                @Override
                public String transform(GeneVertex arg0) {
                    String p = arg0.getAnnotation();
                    if (p == null) {
                        return "NA";
                    }
                    p = p.substring(p.indexOf(":") + 2);
                    String annot = arg0 + "(" + p + ")";
                    return annot;
                }
            });
        }
        graphViewer.repaint();
    }//GEN-LAST:event_hidePValueCheckBoxActionPerformed

    private void extractSubGrph(GeneVertex seedV, Set<String> avaiblePPIs) {
        //to correct the code
        Collection<PPIEdge> edges = totalGraph.getIncidentEdges(seedV);
        Iterator<PPIEdge> iter = edges.iterator();
        if (!selectedGraph.containsVertex(seedV)) {
            selectedGraph.addVertex(seedV);
        }
        PPIEdge edge;
        String tmpPPI1, tmpPPI2;
        GeneVertex child;
        while (iter.hasNext()) {
            edge = iter.next();
            Pair<GeneVertex> paire = totalGraph.getEndpoints(edge);
            if (paire.getFirst().equals(seedV)) {
                child = paire.getSecond();
            } else {
                child = paire.getFirst();
            }

            //System.out.println(seedV.toString() + "--" + child.toString() + " " + child.getAnnotation());
            tmpPPI1 = child.getSymbol() + "&&" + seedV.getSymbol();
            tmpPPI2 = seedV.getSymbol() + "&&" + child.getSymbol();
            if (!avaiblePPIs.contains(tmpPPI1) && !avaiblePPIs.contains(tmpPPI2)) {
                if (!selectedGraph.containsVertex(child)) {
                    selectedGraph.addVertex(child);
                }
                selectedGraph.addEdge(edge, seedV, child);
                avaiblePPIs.add(tmpPPI1);
                avaiblePPIs.add(tmpPPI2);
                extractSubGrph(child, avaiblePPIs);
            }
        }
    }
    private void hideGeneSymbolsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideGeneSymbolsCheckBoxActionPerformed
        RenderContext rc = graphViewer.getRenderContext();
        if (hideGeneSymbolsCheckBox.isSelected() && hidePValueCheckBox.isSelected()) {
            rc.setVertexLabelTransformer(new Transformer<GeneVertex, String>() {
                @Override
                public String transform(GeneVertex arg0) {
                    return "";
                }
            });
        } else if (!hideGeneSymbolsCheckBox.isSelected() && hidePValueCheckBox.isSelected()) {
            rc.setVertexLabelTransformer(new ToStringLabeller());
        } else if (hideGeneSymbolsCheckBox.isSelected() && !hidePValueCheckBox.isSelected()) {
            rc.setVertexLabelTransformer(new Transformer<GeneVertex, String>() {
                @Override
                public String transform(GeneVertex arg0) {
                    String p = arg0.getAnnotation();
                    if (p == null) {
                        return "NA";
                    }
                    p = p.substring(p.indexOf(":") + 2);
                    return p;
                }
            });
        } else {
            rc.setVertexLabelTransformer(new Transformer<GeneVertex, String>() {
                @Override
                public String transform(GeneVertex arg0) {
                    String p = arg0.getAnnotation();
                    if (p == null) {
                        return "NA";
                    }
                    p = p.substring(p.indexOf(":") + 2);
                    String annot = arg0 + "(" + p + ")";
                    return annot;
                }
            });
        }
        graphViewer.repaint();
    }//GEN-LAST:event_hideGeneSymbolsCheckBoxActionPerformed

    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
        final ScalingControl scaler = new CrossoverScalingControl();
        scaler.scale(graphViewer, 1.1f, graphViewer.getCenter());
    }//GEN-LAST:event_zoomInButtonActionPerformed

    private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
        final ScalingControl scaler = new CrossoverScalingControl();
        scaler.scale(graphViewer, 0.9090909F, graphViewer.getCenter());
    }//GEN-LAST:event_zoomOutButtonActionPerformed

    private void layoutComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutComboBoxActionPerformed
        // TODO add your handling code here:
        try {
            Layout<GeneVertex, PPIEdge> l = layouts.get(layoutComboBox.getSelectedIndex());
            l.setInitializer(graphViewer.getGraphLayout());
            l.setSize(graphViewer.getSize());

            LayoutTransition<GeneVertex, PPIEdge> lt = new LayoutTransition<GeneVertex, PPIEdge>(graphViewer, graphViewer.getGraphLayout(), l);
            Animator animator = new Animator(lt);
            animator.start();
            graphViewer.getRenderContext().getMultiLayerTransformer().setToIdentity();
            graphViewer.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_layoutComboBoxActionPerformed

    private void heteroComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heteroComboBoxActionPerformed
        int index = heteroComboBox.getSelectedIndex();
        if (index == 0) {
            heterogeneityTestThresholdText.setText("0.5");
        } else {
            heterogeneityTestThresholdText.setText("0.05");
        }
    }//GEN-LAST:event_heteroComboBoxActionPerformed

    private void enableCandidateGeneCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableCandidateGeneCheckBox2ActionPerformed
        candidateGeneComboBox2.setEnabled(enableCandidateGeneCheckBox2.isSelected());

    }//GEN-LAST:event_enableCandidateGeneCheckBox2ActionPerformed

    private void pvalueSoureComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pvalueSoureComboBoxActionPerformed
        if (GlobalManager.isLoadingGenome) {
            String infor = "Be patient! I am loading the analysis genome!";
            JOptionPane.showMessageDialog(this, infor, "Warnning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TopComponent outputWindow = WindowManager.getDefault().findTopComponent("AnalysisOutputTopComponent");
        //Determine if it is opened
        if (outputWindow != null && outputWindow.isOpened()) {
            outputWindow.close();
        }
        LoadGenomeDataSwingWorker loader = new LoadGenomeDataSwingWorker(ppibaCurrent);
        theTask = RP.create(loader); //the task is not started yet
        theTask.schedule(0); //start the task
        try {
            // showPPIs();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }//GEN-LAST:event_pvalueSoureComboBoxActionPerformed

    private void candidateGeneComboBox2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_candidateGeneComboBox2ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_candidateGeneComboBox2ItemStateChanged

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        JFileChooser jfcSave = null;
        if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
            jfcSave = new JFileChooser(GlobalManager.lastAccessedPath);
        } else {
            jfcSave = new JFileChooser();
        }
        jfcSave.setDialogTitle("Save as");
        jfcSave.setAcceptAllFileFilterUsed(false);

        jfcSave.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        jfcSave.addChoosableFileFilter(new FileNameExtensionFilter("Excel 2007 Files (*.xlsx)", "xlsx"));
        jfcSave.addChoosableFileFilter(new FileNameExtensionFilter("Excel 2003 Files (*.xls)", "xls"));

        // jfcSave.setAcceptAllFileFilterUsed(true);
        short exportType = 0;
        int intResult = jfcSave.showSaveDialog(this);
        File ppiFile = (File) interactionComboBox.getSelectedItem();
        if (intResult == JFileChooser.APPROVE_OPTION) {
            try {
                File fleFile = jfcSave.getSelectedFile();

                FileFilter fileter = jfcSave.getFileFilter();
                if (fileter.getDescription().startsWith("Excel 2007")) {
                    if (!fleFile.getPath().endsWith("xlsx")) {
                        fleFile = new File(fleFile.getPath() + ".xlsx");
                    }
                    exportType = 0;
                } else if (fileter.getDescription().startsWith("Excel 2003")) {
                    if (!fleFile.getPath().endsWith("xls")) {
                        fleFile = new File(fleFile.getPath() + ".xls");
                    }
                    exportType = 1;
                } else {
                    if (!fleFile.getPath().endsWith("txt")) {
                        fleFile = new File(fleFile.getPath() + ".txt");
                    }
                    exportType = 2;
                }

                GlobalManager.lastAccessedPath = fleFile.getParent();

                scanPPI.setOnlyExportSignificant(onlyExportSignificantPPICheckBox.isSelected());
                scanPPI.setOnlyVisualizeSignificant(vsualizePPICheckBox.isSelected());
                scanPPI.setOutputPath(fleFile.getPath());
                scanPPI.setOutputFormatType(exportType);
                ppibaCurrent.setpValueSources(ppibaCurrent.getGeneScan().getPValueSources());
                String infor = scanPPI.exportPPISetListNoSNP(lstPPISet, mapSymbol2PVG, fleFile.getPath(), dblCutoffs, true, true, true);
                NotifyDescriptor nd = new NotifyDescriptor.Message(infor, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox candidateGeneComboBox2;
    private javax.swing.JCheckBox enableCandidateGeneCheckBox2;
    private javax.swing.JTextField genePValueTextField;
    private javax.swing.JComboBox heteroComboBox;
    private javax.swing.JTextField heterogeneityTestThresholdText;
    private javax.swing.JCheckBox hideGeneSymbolsCheckBox;
    private javax.swing.JCheckBox hidePValueCheckBox;
    private javax.swing.JComboBox interactionComboBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox layoutComboBox;
    private javax.swing.JComboBox modeBox;
    private javax.swing.JCheckBox onlyExportSignificantPPICheckBox;
    private javax.swing.JComboBox ppiNetworkGenePvalueConbomBox;
    private javax.swing.JComboBox pvalueSoureComboBox;
    private javax.swing.JButton refreshButton;
    private javax.swing.JComboBox stageIPPIMethodConbomBox;
    private javax.swing.JTextField stageIPPPValueTextField;
    private javax.swing.JList testedGeneList;
    private javax.swing.JPanel veiwPanel;
    private javax.swing.JCheckBox vsualizePPICheckBox;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        ppibaResult = Utilities.actionsGlobalContext().lookupResult(PPIBasedAssociation.class);
        ppibaResult.addLookupListener(this);

    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
        ppibaResult.removeLookupListener(this);
        ppibaResult = null;

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
