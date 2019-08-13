/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.picard;

/**
 *
 * @author MX Li
 */
public class PicardException extends RuntimeException {
    public javax.swing.JTextField projectNameTextField2;
    public javax.swing.JTextField projectNameTextField1;

    public PicardException() {
    }

    public PicardException(final String s) {
        super(s);
    }

    public PicardException(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public PicardException(final Throwable throwable) {
        super(throwable);
    }
}
