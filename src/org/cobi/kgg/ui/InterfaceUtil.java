// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.kgg.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author MX Li
 */
public class InterfaceUtil {

    public static ImageIcon readImageIcon(String filename) {
        URL url = InterfaceUtil.class.getResource("png/16x16/" + filename);
        return new ImageIcon(url);
    }

    //note the print function is imcomplete
    public static void converTreeNode2TextFile(DefaultMutableTreeNode pareNode, StringBuffer fw) {

        int num = 0;
        int leafNum = pareNode.getLeafCount();
        //System.out.println("There are " + leafNum + " pathes!");
        try {

            DefaultMutableTreeNode iterParentNode = pareNode.getFirstLeaf();
            do {
                TreeNode[] pathes = iterParentNode.getPath();
                num = pathes.length - 1;
                for (int j = 1; j < num; j++) {
                    fw.append(pathes[j].toString());
                    fw.append('\t');
                }
                fw.append(pathes[num].toString());
                fw.append('\n');
            } while ((iterParentNode = iterParentNode.getNextLeaf()) != null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //note the print function is imcomplete
    public static void converTreeNode2TextFile(DefaultMutableTreeNode pareNode, BufferedWriter bw) {

        int num = 0;
        int leafNum = pareNode.getLeafCount();
        //System.out.println("There are " + leafNum + " pathes!");
        try {

            DefaultMutableTreeNode iterParentNode = pareNode.getFirstLeaf();
            do {
                TreeNode[] pathes = iterParentNode.getPath();
                num = pathes.length - 1;
                for (int j = 1; j < num; j++) {
                    bw.write(pathes[j].toString());
                    bw.write("\t");
                }
                bw.write(pathes[num].toString());
                bw.write("\n");
            } while ((iterParentNode = iterParentNode.getNextLeaf()) != null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void saveTreeNode2XMLFile(DefaultMutableTreeNode treeRoot, String fileName) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");

        bw.close();
    }

    public static void expandTree(JTree tree) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), true);
    }

    private static void expandAll(JTree tree, TreePath parent, boolean expand) {
        //Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
                TreeNode n = e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        } // Expansion or collapse must be done bottom-up

        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
}
