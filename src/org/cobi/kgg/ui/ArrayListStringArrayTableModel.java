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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Miaoxin Li
 */
public class ArrayListStringArrayTableModel extends AbstractTableModel {

    private String[] titles = null;
    private List<String[]> dataList = new ArrayList<String[]>();

    public void setTitle(String[] tt) {
        titles = tt;
    }

    public String[] getTitle() {
        return titles;
    }

    public void setDataList(List<String[]> dtList) {
        dataList = null;
        dataList = dtList;
    }

    public List<String[]> getDataList() {
        return dataList;
    }

    @Override
    public int getColumnCount() {
        if (titles == null) {
            return 0;
        } else {
            return titles.length;
        }
    }

    @Override
    public int getRowCount() {
        if (dataList == null) {
            return 0;
        } else {
            return dataList.size();
        }
    }

    @Override
    public String getValueAt(int row, int column) {
        if (!dataList.isEmpty()) {
            if (column >= dataList.get(row).length) {
                return "";
            } else {
                return dataList.get(row)[column];
            }
        } else {
            return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return titles[column];
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        dataList.get(row)[column] = (String) value;
        fireTableRowsUpdated(row, column);
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
