/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.Image;

import org.cobi.kgg.business.entity.Project;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;


import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mxli
 */
public class ProjectNode extends AbstractNode {

    // implements ChangeListener
    //private final InstanceContent content;
    /**
     * Creates a new instance of CategoryNode
     */
    /*
     * public ProjectNode(Project proj) { this(proj, new InstanceContent()); }
     *
     */
    public ProjectNode(Project prj) {
        super(new ProjectNodeChildren(prj), Lookups.singleton(prj));
        //super(new ProjectNodeChildren(prj), new AbstractLookup(content));
        //content.set(Arrays.asList(prj, prj.getpValueFiles(), this), null);
        //this.content = content;
        //pile.addChangeListener(WeakListeners.change(this, prj));
    }

    private Project getProject() {
        return getLookup().lookup(Project.class);
    }

    @Override
    public String getHtmlDisplayName() {
        Project c = getProject();
        return "<strong>" + c.getName() + "</strong>";
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png");
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
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
        Project obj = getProject();

        try {
            Property<String> pathProp = new PropertySupport.Reflection<String>(obj, String.class, "getWorkingPath", null);
            Property<String> descripProp = new PropertySupport.Reflection<String>(obj, String.class, "getDescription", null);
            pathProp.setName("WorkingPath");
            descripProp.setName("Description");
            set.put(pathProp);
            set.put(descripProp);

        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        sheet.put(set);
        return sheet;

    }
}
