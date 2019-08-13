/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

;

/**
 *
 * @author Miaoxin Li
 */
public class Zipper {

    public void createZip(String baseDir, String zipFilePathAndName) throws Exception {
        //compress all files and folders under baseDir         
        List<File> fileList = getSubFiles(new File(baseDir));

        //zip file name
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePathAndName));

        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        int readLen = 0;
        for (int i = 0; i < fileList.size(); i++) {
            File f = fileList.get(i);
            System.out.println("Adding: " + f.getPath() + f.getName());

            //get an zip entry
            ze = new ZipEntry(getAbsFileName(baseDir, f));
            ze.setSize(f.length());
            ze.setTime(f.lastModified());

            //add ZipEntry into zos, and then output file content
            zos.putNextEntry(ze);
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                zos.write(buf, 0, readLen);
            }
            is.close();
            System.out.println("done...");
        }
        zos.close();
    }

    public String extractTarGz(String zipFilePathAndName, String baseDir) throws Exception {
        File srcFile = new File(zipFilePathAndName);
        File targetFile = null;
        if (zipFilePathAndName.endsWith(".tar.gz")) {
            //targetFile = new File(baseDir);
            //TarArchive srcTarFile = new TarArchive(new GZIPInputStream(new FileInputStream(srcFile)));
            // srcTarFile.extractContents(targetFile);
            // srcTarFile.closeArchive();
        } else if (zipFilePathAndName.endsWith(".gz")) {
            String fileName = srcFile.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            targetFile = new File(baseDir + File.separator + fileName);
            FileInputStream fin = new FileInputStream(zipFilePathAndName);
            GZIPInputStream gzin = new GZIPInputStream(fin);
            FileOutputStream fout = new FileOutputStream(targetFile);
            byte[] buf = new byte[4 * 1024];
            int num;
            while ((num = gzin.read(buf, 0, buf.length)) != -1) {
                fout.write(buf, 0, num);
            }
            gzin.close();
            fout.close();
            fin.close();
        }
        return targetFile.getCanonicalPath();
    }

    public void extractZip(String zipFilePathAndName, String baseDir) throws Exception {
        try {
            ZipFile zfile = new ZipFile(zipFilePathAndName);
            Enumeration zList = zfile.entries();
            ZipEntry ze = null;
            byte[] buf = new byte[1024];
            while (zList.hasMoreElements()) {
                // get a ZipFile from ZipEntry
                ze = (ZipEntry) zList.nextElement();
                if (ze.isDirectory()) {
                    // System.out.println("Directory: " + ze.getName() + " skipped..");
                    continue;
                }
                //System.out.println("Extracting: " + ze.getName() + "\t" + ze.getSize() + "\t" + ze.getCompressedSize());

                //get an InputStream from  ZipEntry and output the data
                OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(baseDir, ze.getName())));
                InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
                int readLen = 0;
                while ((readLen = is.read(buf, 0, 1024)) != -1) {
                    os.write(buf, 0, readLen);
                }
                is.close();
                os.close();

            }
        } catch (Exception ex) {
        }
    }

    private File getRealFileName(String baseDir, String absFileName) {
        String[] dirs = absFileName.split("/");
// System.out.println(dirs.length);
        File ret = new File(baseDir);
        //System.out.println(ret);
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                ret = new File(ret, dirs[i]);
            }
        }
        if (!ret.exists()) {
            ret.mkdirs();
        }
        ret = new File(ret, dirs[dirs.length - 1]);
        return ret;
    }

    private String getAbsFileName(String baseDir, File realFileName) {
        File real = realFileName;
        File base = new File(baseDir);
        String ret = real.getName();
        while (true) {
            real = real.getParentFile();
            if (real == null) {
                break;
            }
            if (real.equals(base)) {
                break;
            } else {
                ret = real.getName() + "/" + ret;
            }
        }
        System.out.println("File Name" + ret);
        return ret;
    }

    private List<File> getSubFiles(File baseDir) {
        List<File> ret = new ArrayList<File>();
// File base=new File(baseDir);
        File[] tmp = baseDir.listFiles();
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].isFile()) {
                ret.add(tmp[i]);
            }
            if (tmp[i].isDirectory()) {
                ret.addAll(getSubFiles(tmp[i]));
            }
        }
        return ret;
    }
}
