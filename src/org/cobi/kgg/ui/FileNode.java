/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.cobi.kgg.ui.dialog.ProjectTopComponent;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 *
 * @author mxli
 */
public class FileNode extends AbstractNode {

    int nodeType;

    public FileNode(int nodeType, File card) {
        super(Children.LEAF, Lookups.singleton(card));
        this.nodeType = nodeType;
    }

    private File getFile() {
        return getLookup().lookup(File.class);
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/3dbarchart.png");
    }

    @Override
    public String getDisplayName() {
        File c = getFile();
        return c.getName();
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        File obj = getFile();

        try {
            if (obj.exists()) {
                Property<String> pathProp = new PropertySupport.Reflection<String>(obj, String.class, "getAbsolutePath", null);
                Property<Long> lengthProp = new PropertySupport.Reflection<Long>(obj, Long.class, "length", null);

                pathProp.setName("Path");
                lengthProp.setName("File Size");

                set.put(pathProp);
                set.put(lengthProp);
            } else {
                String notExist = "Not exist!";

                Property<String> pathProp = new PropertySupport.Reflection<String>(notExist, String.class, "toString", null);
                pathProp.setName("Path");
                set.put(pathProp);
            }


        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        sheet.put(set);
        return sheet;

    }

    @Override
    public Action[] getActions(boolean popup) {
        return new Action[]{new FileNode.DeleteNodeAction(getFile().getName())};
    }

    private class DeleteNodeAction extends AbstractAction {

        String nodeName;

        public DeleteNodeAction(String nodeName) {
            this.nodeName = nodeName;
            putValue(NAME, "Remove");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String info = "Are you sure to remove the selected file \'" + nodeName + "\' of the current project?";
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(info, NotifyDescriptor.YES_NO_OPTION);
                Object result = DialogDisplayer.getDefault().notify(nd);
                if (NotifyDescriptor.OK_OPTION.equals(result)) {
                    if (nodeType == 0) {
                        boolean succeed = GlobalManager.currentProject.removePValueFileByName(nodeName);
                        if (succeed) {
                            for (int j = GlobalManager.originalAssociationFilesModel.getSize() - 1; j >= 0; j--) {
                                if (GlobalManager.originalAssociationFilesModel.getElementAt(j).toString().equals(nodeName)) {
                                    GlobalManager.originalAssociationFilesModel.removeElementAt(j);
                                    break;
                                }
                            }
                            info = "The p-value file " + nodeName + " has been removed!";
                            StatusDisplayer.getDefault().setStatusText(info);
                            ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                            projTopComp.showProject(GlobalManager.currentProject);
                        }
                    } else if (nodeType == 1) {
                        boolean succeed = GlobalManager.currentProject.removeCandiGeneFileByName(nodeName);
                        if (succeed) {
                            for (int j = GlobalManager.candiGeneFilesModel.getSize() - 1; j >= 0; j--) {
                                if (GlobalManager.candiGeneFilesModel.getElementAt(j).toString().equals(nodeName)) {
                                    GlobalManager.candiGeneFilesModel.removeElementAt(j);
                                    break;
                                }
                            }
                            info = "The candiate gene file " + nodeName + " has been removed!";
                            StatusDisplayer.getDefault().setStatusText(info);
                            ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                            projTopComp.showProject(GlobalManager.currentProject);
                        }
                    }

                } else {
                    // don't do it
                }

            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
}
