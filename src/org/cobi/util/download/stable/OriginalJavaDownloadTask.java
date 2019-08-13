/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.download.stable;

import java.util.concurrent.Callable;

/**
 *
 * @author Miaoxin Li
 */
public class OriginalJavaDownloadTask extends DownloadTask implements Callable <String>{

    private static int count = 0;
    private int num = count;
 
   /**
     * ��ʼ����
     * @throws Exception
     */
    @Override
    public String call() throws Exception {
        return null;
    }
}

