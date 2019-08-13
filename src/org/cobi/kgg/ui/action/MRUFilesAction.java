/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.filechooser.FileFilter;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.PPIBasedAssociation;
import org.cobi.kgg.business.entity.PathwayBasedAssociation;
import org.cobi.kgg.business.entity.Project;
import org.cobi.kgg.ui.ExtensionFileFilter;
import org.cobi.kgg.ui.FileTextNode;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.KGGJFileChooser;
import org.cobi.kgg.ui.MRUFilesOptions;
import org.cobi.kgg.ui.dialog.ProjectTopComponent;
import org.cobi.kgg.ui.dialog.TopComponentFactory;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "org.cobi.kgg.ui.action.MRUFilesAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Goforward.png",
        displayName = "#CTL_MRUFilesAction")
@ActionReference(path = "Menu/Project", position = 1300, separatorBefore = 1250)
@Messages("CTL_MRUFilesAction=Open Project")
public final class MRUFilesAction extends CallableSystemAction {

    public static TopComponentFactory tcfDetect = null;

    //, lazy = false
    /**
     * {@inheritDoc} do nothing
     */
    @Override
    public void performAction() {
        // do nothing
    }

    @Override
    protected String iconResource() {
        //Replace org/nvarun/tat with your path/to/icon
        return "org/cobi/kgg/ui/png/16x16/Goforward.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(MRUFilesAction.class, "CTL_MRUFilesAction");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected ImageIcon createImageIcon(String path,
            String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        setIcon(createImageIcon(iconResource(), "test"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * {@inheritDoc} Overide to provide SubMenu for MRUFiles (Most Recently Used
     * Files)
     */
    @Override
    public JMenuItem getMenuPresenter() {
        JMenu menu = new MRUFilesMenu(getName());
        return menu;
    }

    class MRUFilesMenu extends JMenu implements DynamicMenuContent {

        public MRUFilesMenu(String s) {
            super(s);

            MRUFilesOptions opts = MRUFilesOptions.getInstance();
            opts.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (!evt.getPropertyName().equals(MRUFilesOptions.MRU_FILE_LIST_PROPERTY)) {
                        return;
                    }
                    updateMenu();
                }
            });

            updateMenu();
        }

        @Override
        public JComponent[] getMenuPresenters() {
            return new JComponent[]{this};
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

        private void updateMenu() {
            removeAll();
            Action action = createAction("Browse a project ...");
            action.putValue(Action.NAME, "Browse a project ...");
            JMenuItem menuItem = new JMenuItem(action);
            add(menuItem);
            JSeparator sp = new JSeparator();
            add(sp);
            MRUFilesOptions opts = MRUFilesOptions.getInstance();
            List<String> list = opts.getMRUFileList();
            for (String name : list) {
                action = createAction(name);
                action.putValue(Action.NAME, name);
                menuItem = new JMenuItem(action);
                add(menuItem);
            }
        }

        private Action createAction(String actionCommand) {
            Action action = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    menuItemActionPerformed(e);
                }
            };

            action.putValue(Action.ACTION_COMMAND_KEY, actionCommand);
            return action;
        }

        private void menuItemActionPerformed(ActionEvent evt) {
            String command = evt.getActionCommand();
            File file;

            try {
                if (GlobalManager.currentProject != null) {
                    GlobalManager.currentProject.writeProjectVariables();
                    GlobalManager.currentProject = null;
                    GlobalManager.originalAssociationFilesModel.removeAllElements();
                    GlobalManager.candiGeneFilesModel.removeAllElements();
                    GlobalManager.genomeSetModel.removeAllElements();
                    GlobalManager.geneAssocSetModel.removeAllElements();
                    GlobalManager.ppiAssocSetModel.removeAllElements();
                    GlobalManager.pathwayAssocSetModel.removeAllElements();
                    ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                    projTopComp.showProject(GlobalManager.currentProject);
                }

                if (command.equals("Browse a project ...")) {
                    JFileChooser fDialog = new KGGJFileChooser();
                    FileFilter filter1 = new ExtensionFileFilter("KGG project file (*.xml)", "xml");
                    fDialog.setFileFilter(filter1);
                    fDialog.setDialogTitle("Browse a KGG project file");
                    int result = fDialog.showOpenDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        file = fDialog.getSelectedFile();
                    } else {
                        return;
                    }
                } else {
                    file = new File(command);
                }

                String filePath = file.getCanonicalPath();
                if (!GlobalManager.latestAccessedProjects.contains(filePath)) {
                    GlobalManager.latestAccessedProjects.add(filePath);
                }

                //closeCurrentProject(); 
                GlobalManager.currentProject = new Project(file);
                GlobalManager.lastAccessedPath = file.getParent();
                //GlobalManager.addInforLog(msg);
                //addProject2Tree(openProject);

                /*
                 * DataObject data =
                 * DataObject.find(FileUtil.toFileObject(file)); OpenCookie
                 * cookie = data.getCookie(OpenCookie.class); cookie.open();
                 *
                 */
                String msg = GlobalManager.currentProject.toString() + " has been opened!";

                CallableSystemAction.get(ShowPathwayBasedAssocScanDialogAction.class).setEnabled(true);
                CallableSystemAction.get(ShowPPIBasedAssocScanDialogAction.class).setEnabled(true);
                CallableSystemAction.get(ShowGeneBasedAssocScanDialogAction.class).setEnabled(true);
                CallableSystemAction.get(ShowMultivarGenebasedScanAction.class).setEnabled(true);

                CallableSystemAction.get(ShowBuildAnalysisGenomeDialogAction.class).setEnabled(true);
                CallableSystemAction.get(LoadPValueAction.class).setEnabled(true);
                CallableSystemAction.get(DefineSeedGeneDialogAction.class).setEnabled(true);

                List<File> pVFiles = GlobalManager.currentProject.getpValueFileList();
                for (int i = 0; i < pVFiles.size(); i++) {
                    GlobalManager.originalAssociationFilesModel.addElement(new FileTextNode(pVFiles.get(i).getCanonicalPath()));
                }

                List<File> geneSetVFiles = GlobalManager.currentProject.getCandiGeneFileList();
                for (int i = 0; i < geneSetVFiles.size(); i++) {
                    GlobalManager.candiGeneFilesModel.addElement(new FileTextNode(geneSetVFiles.get(i).getCanonicalPath()));
                }
                List<Genome> genomeList = GlobalManager.currentProject.genomeSet;
                for (int i = 0; i < genomeList.size(); i++) {
                    GlobalManager.genomeSetModel.addElement(genomeList.get(i));
                }
                List<GeneBasedAssociation> geneScanList = GlobalManager.currentProject.geneScans;
                for (int i = 0; i < geneScanList.size(); i++) {
                    GlobalManager.geneAssocSetModel.addElement(geneScanList.get(i));
                }

                List<PPIBasedAssociation> ppiScanList = GlobalManager.currentProject.ppiScans;
                for (int i = 0; i < ppiScanList.size(); i++) {
                    GlobalManager.ppiAssocSetModel.addElement(ppiScanList.get(i));
                }

                List<PathwayBasedAssociation> pathwayScanList = GlobalManager.currentProject.pathwayScans;
                for (int i = 0; i < pathwayScanList.size(); i++) {
                    GlobalManager.pathwayAssocSetModel.addElement(pathwayScanList.get(i));
                }

                ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                projTopComp.showProject(GlobalManager.currentProject);
                projTopComp.openAtTabPosition(0);
                projTopComp.requestActive();
                StatusDisplayer.getDefault().setStatusText(msg);

                // PathwayInfoTopComponent pitc=(PathwayInfoTopComponent) WindowManager.getDefault().findTopComponent("PathwayInfoTopComponent");
                //pitc.open();
                //pitc.setVisible(false);
                //pitc.openAtTabPosition(-1);
                tcfDetect = new TopComponentFactory();

            } catch (OutOfMemoryError ex) {
                /*
                 * String msg =
                 * Application.getMessage("MSG_OutOfMemoryError.Text");
                 * NotifyDescriptor nd = new NotifyDescriptor.Message(msg,
                 * NotifyDescriptor.ERROR_MESSAGE);
                 * DialogDisplayer.getDefault().notify(nd);
                 *
                 */
                ErrorManager.getDefault().notify(ex);
            } catch (Exception ex) {
                /*
                 * NotifyDescriptor nd = new
                 * NotifyDescriptor.Message(ex.getMessage(),
                 * NotifyDescriptor.ERROR_MESSAGE);
                 * DialogDisplayer.getDefault().notify(nd);
                 *
                 */
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
}
