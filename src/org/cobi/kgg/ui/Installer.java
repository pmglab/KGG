/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.Toolkit;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.log4j.PropertyConfigurator;
import org.cobi.kgg.business.entity.Constants;
import static org.cobi.kgg.business.entity.Constants.ICON_PATH_16;
import static org.cobi.kgg.business.entity.Constants.PDATE;
import org.cobi.kgg.ui.action.DefineSeedGeneDialogAction;
import org.cobi.kgg.ui.action.LoadPValueAction;
import org.cobi.kgg.ui.action.ShowBuildAnalysisGenomeDialogAction;
import org.cobi.kgg.ui.action.ShowGeneBasedAssocScanDialogAction;
import org.cobi.kgg.ui.action.ShowMultivarGenebasedScanAction;
import org.cobi.kgg.ui.action.ShowPPIBasedAssocScanDialogAction;
import org.cobi.kgg.ui.action.ShowPathwayBasedAssocScanDialogAction;
import org.cobi.kgg.ui.dialog.SetMemoryDialog;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall implements Constants {

    @Override
    public void restored() {
        try {
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    GlobalManager.mainFrame = (JFrame) WindowManager.getDefault().getMainWindow();
                    String vInfor = "KGG(V4) - A systematic biological Knowledge-based mining system for Genome-wide Genetic studies (" + PDATE + ")";
                    GlobalManager.mainFrame.setTitle(vInfor);
                }
            });

            //System.setProperty("netbeans.winsys.menu_bar.path", "LookAndFeel/MenuBar.instance");      
            GlobalManager.initialVariables(null);
            GlobalManager.readKGGSettings();
            GlobalManager.initialInternetSettings();
            if (!GlobalManager.hasSetMemorry) {
                GlobalManager.smDialog = new SetMemoryDialog(new javax.swing.JFrame(), true);
                // GlobalManager.smDialog = new SetMemoryDialog((JFrame) WindowManager.getDefault().getMainWindow(), true);
                GlobalManager.smDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        // System.exit(0);
                    }
                });

                //JOptionPane.showConfirmDialog(null, "The program has run here--->1!", "SM", JOptionPane.OK_CANCEL_OPTION);
                //Get x and y by geometry relationship
                double x = 0.5 * GlobalManager.smDialog.getWidth();
                double y = 0.5 * GlobalManager.smDialog.getHeight();

                int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
                int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
                GlobalManager.smDialog.setLocation((int) (screenWidth / 2 - x), (int) (screenHeight / 2 - y));
                GlobalManager.smDialog.setVisible(true);
            }

            //JOptionPane.showConfirmDialog(null, "The program has run here--->2!", "SM", JOptionPane.OK_CANCEL_OPTION);
            File cfgFile = new File(GlobalManager.LOCAL_USER_FOLDER + "/log4j.properties");
            if (cfgFile.exists()) {
                PropertyConfigurator.configure(cfgFile.getCanonicalPath());
            } else {
                System.err.println(cfgFile.getCanonicalPath() + " does not exist!");
            }

//                    try {
//                         
//                        NormalGen ng=new NormalACRGen(new  MT19937(new  WELL607()));
//                        double[] mean = new double[10];
//                        Arrays.fill(mean, 0.0);
////                        int[] rowIndexes={0,1,2,3,4,5,6,7,8,9};
////                        int[] columnIndexes={0,1,2,3,4,5,6,7,8,9};
//                        double[][] temp=new double[10][10];
//                        for(int i=0;i<10;i++)
//                            for(int j=i;j<10;j++){
//                                temp[i][j]=Math.random();
//                                temp[j][i]=temp[i][j];
//                            }
//                                                           
//                        DoubleMatrix2D covarianceMatrix=new DenseDoubleMatrix2D(temp);
//                        JOptionPane.showConfirmDialog(null, "The program has run here--->7!", "SM", JOptionPane.OK_CANCEL_OPTION);
//                        MultinormalCholeskyGen sg=new MultinormalCholeskyGen(ng,mean,covarianceMatrix);
//                        JOptionPane.showConfirmDialog(null, "The program has run here--->8!", "SM", JOptionPane.OK_CANCEL_OPTION);
//                        //   simulator.simulatedGenotypeGeneAssociationRischMuliLociModelSimple();
//                        //  LocalExcelFile.test(); 
// RelativeRiskConverter.oddsRation2RelativeRisk(0.107, 0.36, 0.16, 0.0378);
//                        // RelativeRiskConverter.oddsRation2RelativeRisk(0.1013, 1.58, 2.63, 0.1);
//                    } catch (Exception ex) {
//                        ErrorManager.getDefault().notify(ex);
//                    }
            //JOptionPane.showConfirmDialog(null, "The program has run here--->3!", "SM", JOptionPane.OK_CANCEL_OPTION);
            if (GlobalManager.canConnect2Website) {
                /*
                 //Sometimes updating a jar file is not sufficient
                 if (Utils.needUpdateLib(GlobalManager.LOCAL_COPY_FOLDER, URL_FOLDER, URL_FILE_PATHES, GlobalManager.proxyBean)) {
                     
                 //JOptionPane.showConfirmDialog(null, "The program has run here--->4!", "SM", JOptionPane.OK_CANCEL_OPTION);
                 UpdateItselfDialog dialog = new UpdateItselfDialog(new javax.swing.JFrame(), true);
                 // UpdateItselfDialog dialog = new UpdateItselfDialog((JFrame) WindowManager.getDefault().getMainWindow(), true);
                 dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                 @Override
                 public void windowClosing(java.awt.event.WindowEvent e) {
                 System.exit(0);
                 }
                 });

                 //Get x and y by geometry relationship
                 double x = 0.5 * dialog.getWidth();
                 double y = 0.5 * dialog.getHeight();
                 //JOptionPane.showConfirmDialog(null, "The program has run here--->5!", "SM", JOptionPane.OK_CANCEL_OPTION);
                 int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
                 int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
                 dialog.setLocation((int) (screenWidth / 2 - x), (int) (screenHeight / 2 - y));
                 dialog.setVisible(true);
                    
                 }
                 */
                //JOptionPane.showConfirmDialog(null, "The program has run here--->6!", "SM", JOptionPane.OK_CANCEL_OPTION);
                /*
                 view.insertBriefRunningInfor("Checking Resources...", true);
                 GlobalManager.addInforLog("Checking Resources...");
                 downloadResource();
                 */

            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    //Simulator simulator = new Simulator();

                    try {
                        //   simulator.simulatedGenotypeGeneAssociationRischMuliLociModelSimple();
                        //  LocalExcelFile.test(); 
                        // RelativeRiskConverter.oddsRation2RelativeRisk(0.103, 0.357, 0.164, 0.1);
                        // RelativeRiskConverter.oddsRation2RelativeRisk(0.1013, 1.58, 2.63, 0.1);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }

                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }

        CallableSystemAction.get(ShowPathwayBasedAssocScanDialogAction.class).setIcon(new ImageIcon(Installer.class.getResource(ICON_PATH_16 + "Help.png")));
        CallableSystemAction.get(ShowPathwayBasedAssocScanDialogAction.class).setEnabled(false);

        CallableSystemAction.get(ShowPPIBasedAssocScanDialogAction.class).setIcon(new ImageIcon(Installer.class.getResource(ICON_PATH_16 + "Networkconnection.png")));
        CallableSystemAction.get(ShowPPIBasedAssocScanDialogAction.class).setEnabled(false);

        CallableSystemAction.get(ShowGeneBasedAssocScanDialogAction.class).setIcon(new ImageIcon(Installer.class.getResource(ICON_PATH_16 + "Pinion.png")));
        CallableSystemAction.get(ShowGeneBasedAssocScanDialogAction.class).setEnabled(false);

        CallableSystemAction.get(ShowBuildAnalysisGenomeDialogAction.class).setIcon(new ImageIcon(Installer.class.getResource(ICON_PATH_16 + "Earth.png")));
        CallableSystemAction.get(ShowBuildAnalysisGenomeDialogAction.class).setEnabled(false);

        CallableSystemAction.get(LoadPValueAction.class).setIcon(new ImageIcon(Installer.class.getResource(ICON_PATH_16 + "Newdocument.png")));
        CallableSystemAction.get(LoadPValueAction.class).setEnabled(false);

        CallableSystemAction.get(DefineSeedGeneDialogAction.class).setIcon(new ImageIcon(Installer.class.getResource(ICON_PATH_16 + "Bee.png")));
        CallableSystemAction.get(DefineSeedGeneDialogAction.class).setEnabled(false);

        CallableSystemAction.get(ShowMultivarGenebasedScanAction.class).setIcon(new ImageIcon(Installer.class.getResource(ICON_PATH_16 + "Bubble.png")));
        CallableSystemAction.get(ShowMultivarGenebasedScanAction.class).setEnabled(false);

    }
}
