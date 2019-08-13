/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.text.LocalString;

/**
 *
 * @author mxli
 */
public class Utils {

    public static void updateLocal(String sourceFoulder, String destFolder, String[] fileNames) throws Exception {
        for (int i = 0; i < fileNames.length; i++) {
            File copiedFile = new File(sourceFoulder + File.separator + fileNames[i]);
            File targetFile = new File(destFolder + File.separator + copiedFile.getName());
            //System.out.println(copiedFile.getCanonicalPath()+" -> "+targetFile.getCanonicalPath());
            //a file with size less than 1k is not normal
            if (copiedFile.length() > 1024 && copiedFile.length() != targetFile.length()) {
                copyFile(targetFile, copiedFile);
            }
        }
    }

    public static void copyFile(File targetFile, File sourceFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if (!sourceFile.exists()) {
                return;
            }

            if (targetFile.exists()) {
                if (!targetFile.delete()) {
                    targetFile.deleteOnExit();
                    //System.err.println("Cannot delete " + targetFile.getCanonicalPath());
                }
            }
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            in = new FileInputStream(sourceFile);
            out = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024 * 5];
            int size;
            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
                out.flush();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
    }

    public static boolean isConnected(String url) {
        URLConnection urlconn = null;
        try {
            URL u = new URL(url);
            urlconn = u.openConnection();
            urlconn.setConnectTimeout(1000);
            urlconn.connect();
            /*
             TimedUrlConnection timeoutconn = new TimedUrlConnection(urlconn, 5000);//time   out:   100seconds
             boolean bconnectok = timeoutconn.connect();
             if (bconnectok == false) {
             //urlconn   fails   to   connect   in   100seconds
             return false;
             } else {
             //connect   ok
             return true;
             }
             *
             */
            return true;
        } catch (SocketTimeoutException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (ConnectException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (MalformedURLException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
        }
    }

    public static boolean isLatestJavaVersion(String currentJavaVersion, String prefereedVersion) {
        String[] currentVers = currentJavaVersion.split("[.]");
        String[] refredVers = prefereedVersion.split("[.]");
        for (int i = 0; i < currentVers.length; i++) {
            String cv = currentVers[i].replaceAll("_", "");
            cv = cv.replaceAll("-", "");
            String rv = refredVers[i].replaceAll("_", "");
            int index = 0;
            while (index < cv.length() && cv.charAt(index) == '0') {
                index++;
            }
            cv = cv.substring(index);
            if (LocalString.isNumeric(cv)) {
                if (Integer.parseInt(cv) < Integer.parseInt(rv)) {
                    return false;
                } else if (Integer.parseInt(cv) > Integer.parseInt(rv)) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean needUpdateLib(String localFolder, String urlFolder, String[] fileNames, ProxyBean proxyB) throws Exception {
        for (String fileName : fileNames) {
            File newLibFile = new File(localFolder + File.separator + fileName);
            if (newLibFile.exists()) {
                long fileSize = newLibFile.length();
                String url = urlFolder + fileName;
                long netFileLen = HttpClient4API.getContentLength(url, proxyB);
                if (netFileLen <= 1024) {
                    return false;
                }
                if (fileSize != netFileLen) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /*
     //check previously downloaded resources
     public boolean updateShellVersion() throws Exception {
     File localFile = new File(LOCAL_COPY_FOLDER + File.separator + SHELL_NAME);
     String urlFile = URL_FOLDER + SHELL_NAME;
     if (localFile.exists()) {
     long fileSize = localFile.length();
     OriginalJavaTaskAssign taskAssign = new OriginalJavaTaskAssign();
     long netFileLen = taskAssign.getContentLength(urlFile);
     if (netFileLen <= 0) {
     return false;
     }
     if (fileSize == netFileLen) {
     return false;
     }
     }
    
     OriginalJavaTaskAssign downloadTask = new OriginalJavaTaskAssign();
     System.out.println("Updating Shell ...");
     OriginalJavaTaskBean taskBean = new OriginalJavaTaskBean();
    
     taskBean.setDownURL(urlFile);
     taskBean.setSectionCount(FILE_SEGEMENT_NUM);
     taskBean.setWorkerCount(FILE_SEGEMENT_NUM);
     taskBean.setBufferSize(128 * 1024);
     taskBean.setSaveFile(localFile.getCanonicalPath());
     downloadTask.work(taskBean);
    
     String infor = "The shell has been updated.\n1. Please exit now and remove " + localFile.getCanonicalPath()
     + "\n2. Rename \'" + localFile.getCanonicalPath() + ".r_save\' as \'" + localFile.getCanonicalPath() + "\'"
     + "\'.";
     System.out.println(infor);
     return true;
     }
     */
}
