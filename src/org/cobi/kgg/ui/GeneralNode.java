/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.cobi.kgg.ui.dialog.ProjectTopComponent;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 *
 * @author mxli
 */
public class GeneralNode extends AbstractNode{

    File sumFile;
    String nodeType;
    String name;

    // implements ChangeListener
    //private final InstanceContent content;
    /**
     * Creates a new instance of CategoryNode
     */
    /*
     * public ProjectNode(Project proj) { this(proj, new InstanceContent()); }
     *
     */
    public GeneralNode(String type, String name, List<GeneralLeaf> propList) {
        super(new GeneralNodeChildren(propList));
        this.nodeType = type;
        this.name = name;
        
        


        //super(new ProjectNodeChildren(pile), new AbstractLookup(content));
        //content.set(Arrays.asList(pile, pile.getpValueFiles(), this), null);
        //this.content = content;
        //pile.addChangeListener(WeakListeners.change(this, pile));
    }

    public GeneralNode(String type, String name, List<GeneralLeaf> propList, File sumFile) {
        super(new GeneralNodeChildren(propList), Lookups.singleton(sumFile));
        this.nodeType = type;
        this.name = name;

       

        //super(new ProjectNodeChildren(pile), new AbstractLookup(content));
        //content.set(Arrays.asList(pile, pile.getpValueFiles(), this), null);
        //this.content = content;
        //pile.addChangeListener(WeakListeners.change(this, pile));
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    private class DeleteNodeAction extends AbstractAction  {

        String nodeName;

        public DeleteNodeAction(String nodeName) {
            this.nodeName = nodeName;
            putValue(NAME, "Remove");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (nodeType.equals("Genome")) {
                    String info = "Are you sure to delete the genome \'" + nodeName + "\' of the current projec?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(info, NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (NotifyDescriptor.OK_OPTION.equals(result)) {
                        boolean succeed = GlobalManager.currentProject.removeGenomeByName(nodeName);
                        if (succeed) {
                            for (int j = GlobalManager.genomeSetModel.getSize() - 1; j >= 0; j--) {
                                if (GlobalManager.genomeSetModel.getElementAt(j).toString().equals(nodeName)) {
                                    GlobalManager.genomeSetModel.removeElementAt(j);
                                    break;
                                }
                            }
                            info = "The genome " + nodeName + " has been removed!";
                            StatusDisplayer.getDefault().setStatusText(info);
                            ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                            projTopComp.showProject(GlobalManager.currentProject);
                        }
                    } else {
                        // don't do it
                    }
                } else if (nodeType.equals("Gene Scan")) {
                    String info = "Are you sure to delete the gene set \'" + nodeName + "\' of the current projec?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(info, NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (NotifyDescriptor.OK_OPTION.equals(result)) {
                        boolean succeed = GlobalManager.currentProject.removeGeneSetByName(nodeName);
                        if (succeed) {
                            for (int j = GlobalManager.geneAssocSetModel.getSize() - 1; j >= 0; j--) {
                                if (GlobalManager.geneAssocSetModel.getElementAt(j).toString().equals(nodeName)) {
                                    GlobalManager.geneAssocSetModel.removeElementAt(j);
                                    break;
                                }
                            }
                            info = "The gene set '" + nodeName + "' has been removed!";
                            StatusDisplayer.getDefault().setStatusText(info);
                            ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                            projTopComp.showProject(GlobalManager.currentProject);
                        }
                    } else {
                        // don't do it
                    }
                } else if (nodeType.equals("Gene-pair Scan")) {
                    String info = "Are you sure to delete the Gene-pair scan set \'" + nodeName + "\' of the current projec?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(info, NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (NotifyDescriptor.OK_OPTION.equals(result)) {
                        boolean succeed = GlobalManager.currentProject.removePPIScanByName(nodeName);
                        if (succeed) {
                            for (int j = GlobalManager.ppiAssocSetModel.getSize() - 1; j >= 0; j--) {
                                if (GlobalManager.ppiAssocSetModel.getElementAt(j).toString().equals(nodeName)) {
                                    GlobalManager.ppiAssocSetModel.removeElementAt(j);
                                    break;
                                }
                            }
                            info = "The PPI set '" + nodeName + "' has been removed!";
                            StatusDisplayer.getDefault().setStatusText(info);
                            ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                            projTopComp.showProject(GlobalManager.currentProject);
                        }
                    } else {
                        // don't do it
                    }
                } else if (nodeType.equals("Pathway/GeneSet Scan")) {
                    String info = "Are you sure to delete the Pathway/GeneSet scan set \'" + nodeName + "\' of the current projec?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(info, NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (NotifyDescriptor.OK_OPTION.equals(result)) {
                        boolean succeed = GlobalManager.currentProject.removePathwayScanByName(nodeName);
                        if (succeed) {
                            for (int j = GlobalManager.pathwayGeneSetModel.getSize() - 1; j >= 0; j--) {
                                if (GlobalManager.pathwayGeneSetModel.getElementAt(j).toString().equals(nodeName)) {
                                    GlobalManager.pathwayGeneSetModel.removeElementAt(j);
                                    break;
                                }
                            }
                            info = "The Pathway/GeneSet set '" + nodeName + "' has been removed!";
                            StatusDisplayer.getDefault().setStatusText(info);
                            ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                            projTopComp.showProject(GlobalManager.currentProject);
                        }
                    } else {
                        // don't do it
                    }
                }

            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    @Override
    public Action[] getActions(boolean popup) {
        return new Action[]{new DeleteNodeAction(name)};
    }

    @Override
    public String getHtmlDisplayName() {

        return "<strong>" + nodeType + ":</strong> " + name;
    }

    @Override
    public Image getIcon(int type) {
        if (nodeType.equals("Genome")) {
            return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/chips.jpg");
        } else if (nodeType.equals("Gene Scan")) {
            return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/Component.png");
        } else if (nodeType.equals("Gene-pair Scan")) {
            return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/Radiation.png");
        } else if (nodeType.equals("P-value Files")) {
            return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/Text.png");
        } else if (nodeType.equals("Candi. Gene Files")) {
            return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/Euro.png");
        } else {
            return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/Diagram.png");
        }
    }

    /*
     * @Override public void stateChanged(ChangeEvent e) { Set newContent = new
     * HashSet(); newContent.add(getProject()); List<File> card =
     * getProject().getpValueFiles(); if (card != null) { //the pile could be
     * empty newContent.add(card); } //content.set(newContent, null); }
     *
     */
    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();


        sheet.put(set);
        return sheet;

    }
   
}