/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.Image;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mxli
 */
public class ResouceNode extends AbstractNode {

    // implements ChangeListener
    //private final InstanceContent content;
    /**
     * Creates a new instance of CategoryNode
     */
    /*
     * public ProjectNode(Project proj) { this(proj, new InstanceContent()); }
     *
     */
    public ResouceNode(List<GeneralNodeEntity> fileList) {
        super(new ResourceNodeChildren(fileList), Lookups.singleton(fileList));
        //super(new ProjectNodeChildren(pile), new AbstractLookup(content));
        //content.set(Arrays.asList(pile, pile.getpValueFiles(), this), null);
        //this.content = content;
        //pile.addChangeListener(WeakListeners.change(this, pile));
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    @Override
    public String getHtmlDisplayName() {
        return "<strong>Resources</strong>";
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/Hard disk.png");
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