// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.util.download.stable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import org.cobi.util.file.Zipper;
import org.cobi.util.net.ProxyBean;

/**
 *
 * @author MX Li
 */
public class HttpClient4API {

    public static long getContentLength(String curUrl, ProxyBean proxyB)  {
        HttpClient curHttpClient = new DefaultHttpClient();

        //Set proxy host and port   
        if (proxyB != null) {
            if (proxyB.getProxyHost() != null && proxyB.getProxyPort() != null) {
                HttpHost proxy = new HttpHost(proxyB.getProxyHost(), Integer.parseInt(proxyB.getProxyPort()));
                if (proxyB.getProxyUserName() != null && proxyB.getProxyPort() != null) {
                    // Set Credentials
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    //Set auhorizaiton name and password 
                    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(proxyB.getProxyUserName(), proxyB.getProxyPort());
                    //create Credentials
                    credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
                    curHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                    ((DefaultHttpClient) curHttpClient).setCredentialsProvider(credsProvider);
                } else {
                    curHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                }
            }
        }

        HttpHead httpHead = new HttpHead(curUrl);
        try {
            HttpResponse response = curHttpClient.execute(httpHead);
            long length = -1;
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception("Failed to connect " + curUrl);
            }
            /*
             if (getDebug()) {
             for (Header header : response.getAllHeaders()) {
             System.out.println(header.getName() + ":" + header.getValue());
             }
             }
             *
             */

            //Content-Length
            Header[] headers = response.getHeaders("Content-Length");
            if (headers.length > 0) {
                length = Long.valueOf(headers[0].getValue());
            }
            return length;
        } catch (Exception e) {
            System.err.println(e.getMessage() + " for " + curUrl);
            return -1;
        } finally {
            httpHead.abort();
            curHttpClient.getConnectionManager().shutdown();
        }
    }

    public static String getContent(String curUrl, ProxyBean proxyB) throws IOException, ClientProtocolException, Exception {
        String content = null;
        HttpClient curHttpClient = new DefaultHttpClient();
        //Set proxy host and port   
        if (proxyB != null) {
            if (proxyB.getProxyHost() != null && proxyB.getProxyPort() != null) {
                HttpHost proxy = new HttpHost(proxyB.getProxyHost(), Integer.parseInt(proxyB.getProxyPort()));
                if (proxyB.getProxyUserName() != null && proxyB.getProxyPort() != null) {
                    // Set Credentials
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    //Set auhorizaiton name and password 
                    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(proxyB.getProxyUserName(), proxyB.getProxyPort());
                    //create Credentials
                    credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
                    curHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                    ((DefaultHttpClient) curHttpClient).setCredentialsProvider(credsProvider);
                } else {
                    curHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                }
            }
        }
        HttpGet httpGet = new HttpGet(curUrl);

        try {
            HttpResponse response = curHttpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                //throw new Exception("��Դ������!");
                return null;
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                content = EntityUtils.toString(entity, "utf-8");
                //System.out.println(content);
            }
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            throw e;
        } finally {
            curHttpClient.getConnectionManager().shutdown();
        }
        return content;
    }

    public static boolean checkConnection(String url, ProxyBean proxyB, HttpParams httpParams) throws UnknownHostException, Exception {
        HttpClient curHttpClient = new DefaultHttpClient(httpParams);
        boolean sucess = false;
        //Set proxy host and port   
        if (proxyB != null) {
            if (proxyB.getProxyHost() != null && proxyB.getProxyPort() != null) {
                HttpHost proxy = new HttpHost(proxyB.getProxyHost(), Integer.parseInt(proxyB.getProxyPort()));
                if (proxyB.getProxyUserName() != null && proxyB.getProxyPort() != null) {
                    // Set Credentials
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    //Set auhorizaiton name and password 
                    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(proxyB.getProxyUserName(), proxyB.getProxyPort());
                    //create Credentials
                    credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
                    curHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                    ((DefaultHttpClient) curHttpClient).setCredentialsProvider(credsProvider);
                } else {
                    curHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                }
            }
        }
        HttpHead httpHead = new HttpHead(url);
        try {
            HttpResponse response = curHttpClient.execute(httpHead);
            int statusCode = response.getStatusLine().getStatusCode();
            sucess = statusCode == 200;
        } catch (Exception ex) {
            sucess = false;
        } finally {
            httpHead.abort();
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            curHttpClient.getConnectionManager().shutdown();

        }
        //note please donot put return into the finally block, otherwise, the exception will be not throw out.
        return sucess;
    }

    public static void downloadAFile(String url, File destFile, ProxyBean proxy) throws Exception {
        File parentFolder = destFile.getParentFile();
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }
        ExecutorService exec = Executors.newFixedThreadPool(1);
        CompletionService<String> serv = new ExecutorCompletionService<String>(exec);
        final HttpClient4DownloadTask task = new HttpClient4DownloadTask(url, 9, proxy);
        task.setLocalPath(destFile.getCanonicalPath());
        task.addTaskListener(new DownloadTaskListener() {
            @Override
            public void autoCallback(DownloadTaskEvent event) {
            }

            @Override
            public void taskCompleted() throws Exception {

                Zipper ziper = new Zipper();
                File savedFile = new File(task.getLocalPath());
                if (task.getLocalPath().endsWith(".gz")) {
                    ziper.extractTarGz(task.getLocalPath(), savedFile.getParent());
                    savedFile.delete();
                }
            }
        });
        //   task.call();
        exec.submit(task);
        Future<String> task1 = serv.take();
        task1.get();
        exec.shutdown();

    }

    public static void simpleRetriever(String url, String outPath, ProxyBean proxyB) throws Exception {
        // url = "http://www.genenames.org/cgi-bin/hgnc_downloads?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_prev_sym&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_eg_id&col=md_eg_id&col=md_mim_id&col=gd_locus_group&status=Approved&status_opt=2&where=&order_by=gd_hgnc_id&format=text&limit=&hgnc_dbtag=on&submit=submit";
        HttpClient curHttpClient = new DefaultHttpClient();
        //Set proxy host and port   
        if (proxyB != null) {
            if (proxyB.getProxyHost() != null && proxyB.getProxyPort() != null) {
                HttpHost proxy = new HttpHost(proxyB.getProxyHost(), Integer.parseInt(proxyB.getProxyPort()));
                if (proxyB.getProxyUserName() != null && proxyB.getProxyPort() != null) {
                    // Set Credentials
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    //Set auhorizaiton name and password 
                    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(proxyB.getProxyUserName(), proxyB.getProxyPort());
                    //create Credentials
                    credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
                    curHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                    ((DefaultHttpClient) curHttpClient).setCredentialsProvider(credsProvider);
                } else {
                    curHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                }
            }
        }


        try {
            HttpGet httpGet = new HttpGet(url);

            HttpResponse response = curHttpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpClient4DownloadTask.getDebug()) {
                for (Header header : response.getAllHeaders()) {
                    System.out.println(header.getName() + ":" + header.getValue());
                }
                System.out.println("statusCode:" + statusCode);
            }
            if (statusCode == 206 || (statusCode == 200)) {
                InputStream inputStream = response.getEntity().getContent();
                // BufferedInputStream bis = new BufferedInputStream(is, temp.length);

                RandomAccessFile outputStream = new RandomAccessFile(outPath, "rw");


                int count = 0;
                byte[] buffer = new byte[10 * 1024];
                while ((count = inputStream.read(buffer, 0, buffer.length)) > 0) {
                    outputStream.write(buffer, 0, count);
                }
                outputStream.close();
                EntityUtils.consume(response.getEntity());
            }
        } finally {
            curHttpClient.getConnectionManager().shutdown();
        }
    }
}
