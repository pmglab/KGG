/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.text;

import java.io.*;
import java.util.*;
import net.sf.samtools.util.AsciiLineReader;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import org.cobi.util.file.LocalFileFunc;

/**
 *
 * @author MX Li
 */
public class LocalFile {

    /**
     * retrieve data from a text file whith limited rows
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, List<String[]> arry,
            int limitedRowNumber, String delimi, String startLabel, boolean useTokenizer) throws Exception {
        File file = new File(fileName);
        LineReader br = null;
        if (file.getName().endsWith(".zip") || file.getName().endsWith(".gz") || file.getName().endsWith(".tar.gz")) {
            br = new CompressedFileReader(file);
        } else {
            br = new AsciiLineReader(file);
        }

        String line = "";
        String[] row = null;
        int lineNumber = 0;
        String delmilit = "\t\" \"\n,";
        if (delimi != null) {
            delmilit = delimi;
            //usually some files donot start with data but with breif annoation, so we need filter the latter.
        }
        if (startLabel != null) {
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith(startLabel)) {
                    break;
                }
            }
        }

        if (useTokenizer) {
            int colNum = -1;
            int i;
            StringBuilder tmpStr = new StringBuilder();
            do {
                if (line.trim().length() == 0) {
                    continue;
                }
                StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
                if (colNum < 0) {
                    colNum = tokenizer.countTokens();
                }
                row = new String[colNum];
                for (i = 0; i < colNum; i++) {
                    //sometimes tokenizer.nextToken() can not release memory
                    row[i] = tmpStr.append(tokenizer.nextToken().trim()).toString();
                    tmpStr.delete(0, tmpStr.length());
                }
                arry.add(row);

                lineNumber++;
                if (lineNumber > limitedRowNumber) {
                    break;
                }
            } while ((line = br.readLine()) != null);
        } else {
            if (delmilit.equals("\t\" \"\n")) {
                delmilit = "[" + delmilit + "]";
            }
            do {
                if (line.trim().length() == 0) {
                    continue;
                }
                arry.add(line.split(delmilit, -1));
                lineNumber++;
                if (lineNumber > limitedRowNumber) {
                    break;
                }
            } while ((line = br.readLine()) != null);
        }
        br.close();
        return true;
    }

    static public boolean retrieveData(String fileName, List<String[]> arry,
            int limitedRowNumber, String delimi, String startLabel, boolean useTokenizer, int size) throws Exception {
        File file = new File(fileName);
        LineReader br = null;
        if (file.getName().endsWith(".zip") || file.getName().endsWith(".gz") || file.getName().endsWith(".tar.gz")) {
            br = new CompressedFileReader(file, size);
        } else {
            br = new AsciiLineReader(file, size);
        }

        String line = "";
        String[] row = null;
        int lineNumber = 0;
        String delmilit = "\t\" \"\n,";
        if (delimi != null) {
            delmilit = delimi;
            //usually some files donot start with data but with breif annoation, so we need filter the latter.
        }
        if (startLabel != null) {
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith(startLabel)) {
                    break;
                }
            }
        }

        if (useTokenizer) {
            int colNum = -1;
            int i;
            StringBuilder tmpStr = new StringBuilder();
            do {
                if (line.trim().length() == 0) {
                    continue;
                }
                StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
                if (colNum < 0) {
                    colNum = tokenizer.countTokens();
                }
                row = new String[colNum];
                for (i = 0; i < colNum; i++) {
                    //sometimes tokenizer.nextToken() can not release memory
                    row[i] = tmpStr.append(tokenizer.nextToken().trim()).toString();
                    tmpStr.delete(0, tmpStr.length());
                }
                arry.add(row);

                lineNumber++;
                if (lineNumber > limitedRowNumber) {
                    break;
                }
            } while ((line = br.readLine()) != null);
        } else {
            if (delmilit.equals("\t\" \"\n")) {
                delmilit = "[" + delmilit + "]";
            }
            do {
                if (line.trim().length() == 0) {
                    continue;
                }
                arry.add(line.split(delmilit, -1));
                lineNumber++;
                if (lineNumber > limitedRowNumber) {
                    break;
                }
            } while ((line = br.readLine()) != null);
        }
        br.close();
        return true;
    }

    static public boolean retrieveData(String fileName, List<String[]> arry, int limitedRowNumber) throws Exception {
        File file = new File(fileName);
        LineReader br;
        if (file.getName().endsWith(".gz")) {
            br = new CompressedFileReader(file);
        } else {
            br = new AsciiLineReader(file);
        }

        String line;
        String[] row;
        int lineNumber = 0;
        String delmilit = "\t\" \"\n,";
        List<String> rowCells = new ArrayList<String>();

        StringBuilder tmpStr = new StringBuilder();
        while ((line = br.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
            while (tokenizer.hasMoreTokens()) {
                //sometimes tokenizer.nextToken() can not release memory
                rowCells.add(tmpStr.append(tokenizer.nextToken().trim()).toString());
                tmpStr.delete(0, tmpStr.length());
            }

            row = new String[rowCells.size()];
            rowCells.toArray(row);
            arry.add(row);
            rowCells.clear();
            lineNumber++;
            if (lineNumber > limitedRowNumber) {
                break;
            }
        }

        br.close();
        return true;
    }

    /**
     * simply retrieve data from a file
     *
     * @param FileName
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, List<String[]> arry, String delimiter) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        String delmilit = "\t\" \"\n";
        if (delimiter != null) {
            delmilit = delimiter;        //usually some files donot start with data but with breif annoation, so we need filter the latter.

        }
        int colNum = -1;
        String[] row = null;
        StringBuilder tmpStr = new StringBuilder();
        while ((line = br.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
            if (colNum < 0) {
                colNum = tokenizer.countTokens();
            }
            row = new String[colNum];
            for (int i = 0; i < colNum; i++) {
                //sometimes tokenizer.nextToken() can not release memory
                row[i] = tmpStr.append(tokenizer.nextToken().trim()).toString();
                tmpStr.delete(0, tmpStr.length());
            }
            arry.add(row);
        }
        br.close();
        return true;
    }

    /**
     * simply retrieve data from a file
     *
     * @param FileName
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, StringBuilder tmpBf) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            tmpBf.append(line);
            tmpBf.append('\n');
        }
        br.close();
        return true;
    }

    /**
     * simply retrieve data from a file
     *
     * @param FileName
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, HashSet<String> arry) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() > 1) {
                arry.add(line);
            }
        }
        br.close();
        return true;
    }

    /**
     * simply retrieve data from a file
     *
     * @param FileName
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, HashSet<String> arry, int index) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        int i = 0;
        while ((line = br.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            for (i = 0; i < index; i++) {
                tokenizer.nextToken();
                // System.out.println(tokenizer.nextToken());
            }
            arry.add(tokenizer.nextToken());
        }
        br.close();
        return true;
    }

    /**
     * retrieve data from a text file it is based on split,
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, List<String[]> arry, int[] orgIndices,
            HashSet<String> refList, int refIndex, String delimiter) throws Exception {
        File file = new File(fileName);
        LineReader br = null;
        if (file.getName().endsWith(".zip") || file.getName().endsWith(".gz") || file.getName().endsWith(".tar.gz")) {
            br = new CompressedFileReader(file);
        } else {
            br = new AsciiLineReader(file);
        }

        String line = null;
        String[] cells = null;
        String[] row = null;
        int selectedColNum = orgIndices.length;

        int i, pos;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.trim().length() == 0) {
                continue;
            }
            cells = line.split(delimiter, -1);
            if (refList.contains(cells[refIndex])) {
                row = new String[selectedColNum];
                for (i = 0; i < selectedColNum; i++) {
                    row[i] = cells[orgIndices[i]];
                }
                arry.add(row);
            }
        }
        br.close();
        return true;
    }

    /**
     * retrieve data from a text file it is based on tokenizor, but the order in
     * the indices will be changed to the order of the files. it is a
     * consideration of speed.
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, List<String[]> arry, int[] orgIndices,
            String delimiter) throws Exception {
        BufferedReader br = LocalFileFunc.getBufferedReader(fileName);
                 
        String line = null;
        String[] cells = null;
        String[] row = null;
        int selectedColNum = orgIndices.length;
        int i;

        while ((line = br.readLine()) != null) {
            //line = line.trim();
            if (line.trim().length() == 0) {
                continue;
            }
            // System.out.println(line);
            cells = line.split(delimiter, -1);
           
            row = new String[selectedColNum];
            for (i = 0; i < selectedColNum; i++) {
                row[i] = cells[orgIndices[i]];
            }
            arry.add(row);
        }
        br.close();
        return true;
    }

    /**
     * simply retrieve data from a file
     *
     * @param FileName
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    //Note: this must be in sequencial order
    static public boolean retrieveData(File fileName, List<String[]> arry, String delimiter, Set<Integer> indexes) throws Exception {
        LineReader br = null;
        if (fileName.getName().endsWith(".zip") || fileName.getName().endsWith(".gz") || fileName.getName().endsWith(".tar.gz")) {
            br = new CompressedFileReader(fileName);
        } else {
            br = new AsciiLineReader(fileName);
        }

        String line = null;
        String delmilit = "\t\" \"\n";
        if (delimiter != null) {
            delmilit = delimiter;        //usually some files donot start with data but with breif annoation, so we need filter the latter.

        }
        int colID = -1;
        String[] row = null;
        int arrayIndex = 0;
        StringBuilder tmpStr = new StringBuilder();
        while ((line = br.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            colID = 0;
            arrayIndex = 0;
            StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
            row = new String[indexes.size()];
            while (tokenizer.hasMoreTokens()) {
                tmpStr.append(tokenizer.nextToken().trim());
                if (indexes.contains(colID)) {
                    row[arrayIndex] = tmpStr.toString();
                    arrayIndex++;
                }
                colID++;
                tmpStr.delete(0, tmpStr.length());
            }
            arry.add(row);
        }
        br.close();
        return true;
    }

    /**
     * retrieve data from a text file it is based on split,
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, ArrayList<String[]> arry, int[] orgIndices,
            String[] refList, int refIndex, String delimiter) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        String[] cells = null;
        String[] row = null;
        int selectedColNum = orgIndices.length;
        int i, pos;
        Arrays.sort(refList);
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.trim().length() == 0) {
                continue;
            }
            cells = line.split(delimiter, -1);
            pos = Arrays.binarySearch(refList, cells[refIndex]);
            if (pos >= 0) {
                row = new String[selectedColNum];
                for (i = 0; i < selectedColNum; i++) {
                    row[i] = cells[orgIndices[i]];
                }
                arry.add(row);
            }
        }
        br.close();
        return true;
    }

    /**
     * write data to a text file
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean writeObject2Text(String fileName, List<Object[]> arry, String delmilit) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        Object[] linecells = null;
        int linenumber = arry.size();
        int cols = 0;
        for (int i = 0; i < linenumber; i++) {
            linecells = arry.get(i);
            cols = linecells.length - 1;
            for (int j = 0; j < cols; j++) {
                if (linecells[j] == null) {
                    bw.write(" ");
                } else {
                    bw.write(linecells[j].toString());
                }
                bw.write("\t");
            }
            if (linecells[cols] == null) {
                bw.write(" ");
            } else {
                bw.write(linecells[cols].toString());
            }
            bw.write("\n");

        }
        bw.flush();
        bw.close();
        return true;
    }

    /**
     * write data to a text file
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean writeData(String fileName, List<String[]> arry, String delmilit, boolean append) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, append));
        String[] linecells = null;
        int linenumber = arry.size();
        if (linenumber == 0) {
            return false;
        }
        int cols = arry.get(0).length - 1;
        for (int i = 0; i < linenumber; i++) {
            linecells = arry.get(i);

            for (int j = 0; j < cols; j++) {
                if (linecells[j] == null) {
                    bw.write("-");
                } else {
                    bw.write(linecells[j]);
                }
                bw.write(delmilit);
            }

            if (linecells[cols] == null) {
                bw.write("-");
            } else {
                bw.write(linecells[cols]);
            }
            bw.write("\n");
        }
        bw.close();
        return true;
    }

    static public boolean writeData(String fileName, String[] head, List<String[]> arry, String delmilit, boolean append) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, append));
        String[] linecells = null;
        int linenumber = arry.size();
        if (linenumber == 0) {
            return false;
        }
        int cols = head.length - 1;
        bw.write(head[0]);
        for (int i = 1; i < cols; i++) {
            bw.write(delmilit);
            bw.write(head[i]);
        }
        bw.write("\n");
        for (int i = 0; i < linenumber; i++) {
            linecells = arry.get(i);

            for (int j = 0; j < cols; j++) {
                if (linecells[j] == null) {
                    bw.write("-");
                } else {
                    bw.write(linecells[j]);
                }
                bw.write(delmilit);
            }

            if (linecells[cols] == null) {
                bw.write("-");
            } else {
                bw.write(linecells[cols]);
            }
            bw.write("\n");
        }
        bw.close();
        return true;
    }

    public static void main(String[] args) {
        String fileName = "kggweb.log";
        HashSet<String> ips = new HashSet<String>();
        try {
            LocalFile.retrieveData(fileName, ips, 3);
            System.out.println(ips.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean writeObject2Text(File fleFile, List<String[]> lstOutput, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void writeData(String fileName, List<String[]> arry, List<String[]> arry2, String delmilit, boolean append) throws IOException, Exception {

        writeData(fileName, arry, delmilit, append);
        String strFileName = fileName.replaceAll(".txt$", "_outside.txt");
        writeData(strFileName, arry2, delmilit, append);
    }

    public static void writeData(String fileName, String[] heads, List<String[]> arry, List<String[]> arry2, String delmilit, boolean append) throws IOException, Exception {

        writeData(fileName, heads, arry, delmilit, append);
        String strFileName = fileName.replaceAll(".txt$", "_outside.txt");
        writeData(strFileName, heads, arry2, delmilit, append);
    }
}
