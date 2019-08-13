/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import org.cobi.util.net.URLOpener;

/**
 *
 * @author MXLi
 */
public class LinkLabel extends JLabel {

    private String text, url;
    private boolean isSupported;

    public LinkLabel(String text, String url) {
        this.text = text;
        this.url = url;
        try {
            this.isSupported = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
        } catch (Exception e) {
            this.isSupported = false;
        }
        setText(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setText(isSupported);
                if (isSupported) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setText(false);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    URLOpener.openURL(LinkLabel.this.url);
                } catch (Exception ex) {
                }
            }
        });
    }

    private void setText(boolean b) {
        if (!b) {
            setText("<html><font color=blue><u>" + text);
        } else {
            setText("<html><font color=red><u>" + text);
        }
    }
}
