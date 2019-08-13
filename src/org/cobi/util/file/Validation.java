/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JProgressBar;

/**
 *
 * @author mxli
 */
public class Validation {

    /**
     *
     * @param file
     * @return
     */
    public static String getMD(File file, String hashType) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance(hashType);
            fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length = -1;
            System.out.println("Start to calculate...");
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            System.out.println("Finished!");
            return bytesToString(md.digest());
        } catch (IOException ex) {
            Logger.getLogger(Validation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Validation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(Validation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * 得到文件的MD5码,用于校验
     * @param file
     * @param jpb
     * @return
     */
    public static String getMD(File file, JProgressBar jpb, String hashType) {
        FileInputStream fis = null;
        jpb.setMaximum((int) file.length());
        jpb.setValue(0);
        jpb.setString("Calculating  the " + hashType + " of " + file.getName());
        try {
            MessageDigest md = MessageDigest.getInstance(hashType);
            fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length = -1;
            System.out.println("Start to calculate...");
            int value = 0;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
                value += length;
                jpb.setValue(value);
            }
            System.out.println("Finished!");
            return bytesToString(md.digest());
        } catch (IOException ex) {
            Logger.getLogger(Validation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Validation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(Validation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String bytesToString(byte[] data) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};
        char[] temp = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            temp[i * 2] = hexDigits[b >>> 4 & 0x0f];
            temp[i * 2 + 1] = hexDigits[b & 0x0f];
        }
        return new String(temp);
    }

    public static void main(String[] args) {
        try {
            File file = new File("resources/hapmapld/CEU/ld_chr1_CEU.txt");
            System.out.println(Validation.getMD(file, "MD5"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
