/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.AbstractButton;
import javax.swing.JTextPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.UndoManager;

/**
 *
 * Modified from
 * http://developeriq.in/articles/2010/sep/10/document-editing-in-java/ Greatly
 * appreciate!
 */
public class JDocumentEditor extends JTextPane {

    UndoHandler uh = null;

    public JDocumentEditor() {
        this.setContentType("text/html");
        this.setDragEnabled(true);

    }

    public void setTextForeground(Color c) {
        MutableAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setForeground(att, c);
        this.setCharacterAttributes(att, false);
    }

    public void setTextBackground(Color c) {
        MutableAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setBackground(att, c);
        this.setCharacterAttributes(att, false);
    }

    public void setTextBold() {
        EditorKit styleEd = this.getEditorKit();
        if (!(styleEd instanceof StyledEditorKit)) {
            return;
        }
        MutableAttributeSet att = ((StyledEditorKit) styleEd).getInputAttributes();
        StyleConstants.setBold(att, !StyleConstants.isBold(att));
        this.setCharacterAttributes(att, false);
    }

    public void setTextItalic() {
        EditorKit styleEd = this.getEditorKit();
        if (!(styleEd instanceof StyledEditorKit)) {
            return;
        }
        MutableAttributeSet att = ((StyledEditorKit) styleEd).getInputAttributes();
        StyleConstants.setItalic(att, !StyleConstants.isItalic(att));
        this.setCharacterAttributes(att, false);
    }

    public void setTextUnderline() {
        EditorKit styleEd = this.getEditorKit();
        if (!(styleEd instanceof StyledEditorKit)) {
            return;
        }
        MutableAttributeSet att = ((StyledEditorKit) styleEd).getInputAttributes();
        StyleConstants.setUnderline(att, !StyleConstants.isUnderline(att));
        this.setCharacterAttributes(att, false);
    }

    public void setTextStrikeThrough() {
        EditorKit styleEd = this.getEditorKit();
        if (!(styleEd instanceof StyledEditorKit)) {
            return;
        }
        MutableAttributeSet att = ((StyledEditorKit) styleEd).getInputAttributes();
        StyleConstants.setStrikeThrough(att, !StyleConstants.isStrikeThrough(att));
        this.setCharacterAttributes(att, false);
    }

    public void setTextSuperscript() {
        EditorKit styleEd = this.getEditorKit();
        if (!(styleEd instanceof StyledEditorKit)) {
            return;
        }
        MutableAttributeSet att = ((StyledEditorKit) styleEd).getInputAttributes();
        StyleConstants.setSuperscript(att, !StyleConstants.isSuperscript(att));
        this.setCharacterAttributes(att, false);
    }

    public void setTextSubscript() {
        EditorKit styleEd = this.getEditorKit();
        if (!(styleEd instanceof StyledEditorKit)) {
            return;
        }
        MutableAttributeSet att = ((StyledEditorKit) styleEd).getInputAttributes();
        StyleConstants.setSubscript(att, !StyleConstants.isSubscript(att));
        this.setCharacterAttributes(att, false);
    }

    public void setTextFontFamily(String fnt) {
        MutableAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setFontFamily(att, fnt);
        this.setCharacterAttributes(att, false);
    }

    public void setTextFontSize(int size) {
        MutableAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setFontSize(att, size);
        this.setCharacterAttributes(att, false);
    }

    public void setTextAlignment(int align) {
        MutableAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setAlignment(att, align);
        this.setParagraphAttributes(att, false);
    }

    public void setTextIndent(float indent) {
        MutableAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setFirstLineIndent(att, indent);
        this.setParagraphAttributes(att, false);
    }

    public void setTextSpaceAbove(float space) {
        MutableAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setSpaceAbove(att, space);
        this.setParagraphAttributes(att, false);
    }

    public void setTextSpaceBelow(float space) {
        MutableAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setSpaceBelow(att, space);
        this.setParagraphAttributes(att, false);
    }

    public void setTextLineSpacing(float space) {
        MutableAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(att, space);
        this.setParagraphAttributes(att, false);
    }

    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    public void insertHTML(String s) {
        rwl.writeLock().lock();

        try {

             ((HTMLEditorKit) this.getEditorKit()).read(new java.io.StringReader(s), this.getDocument(), this.getSelectionStart());
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(JDocumentEditor.class.getName()).log(java.util.logging.Level.WARNING, null, ex);
        } finally {
            rwl.writeLock().unlock();
        }

    }

    public void insertImage(String fileURL) {
        // ((HTMLEditorKit) this.getEditorKit()).read(new java.io.StringReader("<img src=\"" + file + "\" />"), this.getDocument(), this.getSelectionStart());
        try {
            ((HTMLEditorKit) this.getEditorKit()).read(new java.io.StringReader(fileURL), this.getDocument(), this.getSelectionStart());
            // this.insertIcon(new ImageIcon(fileURL));
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(JDocumentEditor.class.getName()).log(java.util.logging.Level.WARNING, null, ex);
        }
    }

    public void insertImageHTML(String fileURL, String s) {
        // ((HTMLEditorKit) this.getEditorKit()).read(new java.io.StringReader("<img src=\"" + file + "\" />"), this.getDocument(), this.getSelectionStart());
        try {
            ((HTMLEditorKit) this.getEditorKit()).read(new java.io.StringReader(fileURL + s), this.getDocument(), this.getSelectionStart());
            // this.insertIcon(new ImageIcon(fileURL));

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(JDocumentEditor.class.getName()).log(java.util.logging.Level.WARNING, null, ex);
        }
    }

    public void insertTable(int r, int c) {
        try {
            StringBuilder sb = new StringBuilder("<table border=1>\n");
            for (int i = 0; i < r; i++) {
                sb.append("<tr>");
                for (int j = 0; j < c; j++) {
                    sb.append("<td></td>");
                }
                sb.append("</tr>\n");
            }
            sb.append("</table>");
            ((HTMLEditorKit) this.getEditorKit()).read(new java.io.StringReader(sb.toString()), this.getDocument(), this.getSelectionStart());
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(JDocumentEditor.class.getName()).log(java.util.logging.Level.WARNING, null, ex);
        }
    }

    public void setUndoRedo(AbstractButton undo, AbstractButton redo) {
        this.getDocument().addUndoableEditListener(uh = new UndoHandler(undo, redo));
    }

    public void undo() {
        uh.reverseEditing(false);
    }

    public void redo() {
        uh.reverseEditing(true);
    }

    public void resetUndo() {
        uh.resetUndo();
    }

    class UndoHandler implements UndoableEditListener, ActionListener {

        private UndoManager um = null;
        private AbstractButton undo = null, redo = null;

        public UndoHandler(AbstractButton umi, AbstractButton rmi) {
            um = new UndoManager();
            undo = umi;
            redo = rmi;
            JDocumentEditor.this.getDocument().addUndoableEditListener(this);
            undo.addActionListener(this);
            redo.addActionListener(this);
        }

        public void actionPerformed(ActionEvent ae) {
            reverseEditing(ae.getSource() == redo);
        }

        public void reverseEditing(boolean r) {
            try {
                if (r) {
                    um.redo();
                } else {
                    um.undo();
                }
                refreshState();
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Error in " + (r ? "redo " : "undo ") + ex.toString());
            }
        }

        public void resetUndo() {
            um.discardAllEdits();
            undo.setEnabled(false);
            redo.setEnabled(false);
            JDocumentEditor.this.getDocument().addUndoableEditListener(this);
        }

        public void refreshState() {
            undo.setEnabled(um.canUndo());
            redo.setEnabled(um.canRedo());
            undo.setText(undo.isEnabled() ? um.getUndoPresentationName() : "Undo");
            redo.setText(undo.isEnabled() ? um.getRedoPresentationName() : "Redo");
        }

        public void undoableEditHappened(UndoableEditEvent uee) {
            try {
                um.addEdit(uee.getEdit());
                this.refreshState();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
