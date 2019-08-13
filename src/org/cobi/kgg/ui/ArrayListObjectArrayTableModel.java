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
 * @author MX Li
 */
public class ArrayListObjectArrayTableModel extends AbstractTableModel {

    private String[] titles = null;
    private List<Object[]> dataList = new ArrayList<Object[]>();

    public void setTitle(String[] tt) {
        titles = tt;
    }

    public void setDataList(List<Object[]> dtList) {
        dataList = null;
        dataList = dtList;
    }

    public List<Object[]> getDataList() {
        return dataList;
    }

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
    public Object getValueAt(int row, int column) {
        if (!dataList.isEmpty()) {
            return dataList.get(row)[column];
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
        dataList.get(row)[column] = value;
        fireTableRowsUpdated(row, column);
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }
}
