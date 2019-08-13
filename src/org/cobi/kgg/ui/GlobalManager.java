/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import org.cobi.kgg.business.entity.Constants;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.PPIBasedAssociation;
import org.cobi.kgg.business.entity.PathwayBasedAssociation;
import org.cobi.kgg.business.entity.Project;
import org.cobi.kgg.ui.dialog.AnalysisOutputTopComponent;
import org.cobi.kgg.ui.dialog.BuildAnalysisGenomeByPositionDialog;
import org.cobi.kgg.ui.dialog.CorrelationMatrixDefJDialog;
import org.cobi.kgg.ui.dialog.CreateProjectDialog;
import org.cobi.kgg.ui.dialog.DefineCandidateGeneDialog;
import org.cobi.kgg.ui.dialog.GeneBasedScanDialogMultivariate;

import org.cobi.kgg.ui.dialog.GeneBasedScanDialogUnivariate;
import org.cobi.kgg.ui.dialog.GeneLDPlotFrame;
import org.cobi.kgg.ui.dialog.PPIBasedScanDialog;
import org.cobi.kgg.ui.dialog.PathwayBasedScanDialog;
import org.cobi.kgg.ui.dialog.PowerSimulationFrame;
import org.cobi.kgg.ui.dialog.SetMemoryDialog;

import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.net.ProxyBean;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author mxli
 */
public class GlobalManager implements Constants {

    private final static Logger LOG = Logger.getLogger(GlobalManager.class.getName());
    public static String lastAccessedPath = null;
    public static JFrame mainFrame = null;
    public static Project currentProject = null;
    public static ArrayList<String> latestAccessedProjects = new ArrayList<String>();
    public static boolean needStartHint = false;
    public static boolean hasSetMemorry = false;
    public static ProxyBean proxyBean;
    public static Map<String, String> resourceFileSize = new HashMap<String, String>(); //update resources
    // public static String localResourcePath = "resources/";
    public static DefaultComboBoxModel<FileTextNode> originalAssociationFilesModel = new DefaultComboBoxModel<FileTextNode>();
    public static DefaultComboBoxModel<FileTextNode> candiGeneFilesModel = new DefaultComboBoxModel<FileTextNode>();
    public static DefaultComboBoxModel<Genome> genomeSetModel = new DefaultComboBoxModel<Genome>();
    public static DefaultComboBoxModel<GeneBasedAssociation> geneAssocSetModel = new DefaultComboBoxModel<GeneBasedAssociation>();
    public static DefaultComboBoxModel<PPIBasedAssociation> ppiAssocSetModel = new DefaultComboBoxModel<PPIBasedAssociation>();
    public static DefaultComboBoxModel<PathwayBasedAssociation> pathwayAssocSetModel = new DefaultComboBoxModel<PathwayBasedAssociation>();
    public static DefaultComboBoxModel weightedSNPSetModel = new DefaultComboBoxModel();
    public static DefaultListModel<String> hapmapLDFileListModel = new DefaultListModel<String>();
    public static DefaultListModel<String> vcfHaplotypeFileListModel = new DefaultListModel<String>();
    public static DefaultComboBoxModel<String> pathwayGeneSetModel = new DefaultComboBoxModel<String>();
    public static DefaultComboBoxModel<String> pathwayGeneSetCustomModel = new DefaultComboBoxModel<String>();
    public static boolean canConnect2Website = false;
    public static int timeOut = 1000;
    public static HttpParams httpParams = null;
    public static BuildAnalysisGenomeByPositionDialog buildAnalysisGenomeByPositionDialog = null;
    public static CreateProjectDialog createProjectDialog = null;
    public static GeneBasedScanDialogUnivariate univarGeneBasedScanDialog = null;
    public static GeneBasedScanDialogMultivariate multivarGeneBasedScanDialog = null;

    public static PPIBasedScanDialog pPIBasedScanDialog = null;
    public static PathwayBasedScanDialog pathwayBasedScanDialog = null;
    public static DefineCandidateGeneDialog defineCandidateGeneDialog = null;
    public static CorrelationMatrixDefJDialog correlationMatrixDefJDialog = null;
    public static SetMemoryDialog smDialog = null;
    public static PowerSimulationFrame psDialog = null;
    public static GeneLDPlotFrame gldpDialog = null;
    public static String LOCAL_USER_FOLDER = "./";
    public static String LOCAL_LIB_FOLDER = "./";
    public static String RESOURCE_PATH = LOCAL_LIB_FOLDER + "resources/";
    public static String ETC_PATH = LOCAL_LIB_FOLDER + "etc/";
    public static String LOCAL_COPY_FOLDER = LOCAL_LIB_FOLDER + "/updated/";
    public static AnalysisOutputTopComponent aotcWindow = null;
    public static volatile boolean isLoadingGenome = false;

    public GlobalManager() {

    }

    public static void initialVariables(String customedResourcePath) throws Exception {
        File path = new File(GlobalManager.class.getResource("/org/cobi/kgg/ui").getFile());
        path = path.getParentFile();

        //System.out.println(userPath);
        // LOG.info(path.toString());
        //normally, it will be like this file:/D:/home/mxli/MyJava/KGGSeq/dist/kggseq.jar!/resource/
        try {
            String pathName = path.getAbsolutePath();
            //System.out.println(pathName);
            int index1 = pathName.indexOf("build\\cluster");
            if (index1 > 0) {
                path = path.getParentFile().getParentFile();
                if (path.exists()) {
                    LOCAL_LIB_FOLDER = path.getCanonicalPath();
                }
            } else {
                index1 = pathName.indexOf("build/cluster");
                if (index1 > 0) {
                    path = path.getParentFile().getParentFile();
                    if (path.exists()) {
                        LOCAL_LIB_FOLDER = path.getCanonicalPath();
                    }
                } else {
                    index1 = pathName.indexOf("/file:");
                    if (index1 >= 0) {
                        int index2 = pathName.lastIndexOf("kgg4");
                        pathName = pathName.substring(index1 + 6, index2);
                        path = new File(pathName);
                        if (path.exists()) {
                            LOCAL_LIB_FOLDER = pathName;
                        }
                    } else {
                        index1 = pathName.indexOf("\\file:");
                        int index2 = pathName.lastIndexOf("kgg4");
                        pathName = pathName.substring(index1 + 7, index2);
                        path = new File(pathName);
                        if (path.exists()) {
                            LOCAL_LIB_FOLDER = pathName;
                        }
                    }
                }
            }

            String userPath = System.getProperty("user.home");
            if (userPath != null) {
                LOCAL_USER_FOLDER = userPath + "/KGG/";
                File file = new File(LOCAL_USER_FOLDER);
                file.mkdirs();
            } else {
                LOCAL_USER_FOLDER = LOCAL_LIB_FOLDER;
            }

            if (customedResourcePath != null) {
                RESOURCE_PATH = customedResourcePath + "/";
            } else {
                RESOURCE_PATH = LOCAL_LIB_FOLDER + "resources/";
            }
            LOCAL_COPY_FOLDER = LOCAL_LIB_FOLDER + "updated/";
            ETC_PATH = LOCAL_LIB_FOLDER + "etc/";
            LOG.info(RESOURCE_PATH);
            LOG.info(LOCAL_LIB_FOLDER);
            LOG.info(ETC_PATH);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void initialInternetSettings() throws UnknownHostException, Exception {

        /*
         httpParams = new BasicHttpParams();
         ConnManagerParams.setMaxTotalConnections(httpParams, MAX_TOTAL_CONNECTIONS);
         ConnPerRouteBean connPerRoute = new ConnPerRouteBean(MAX_PER_ROUTE_CONNECTIONS);
         connPerRoute.setDefaultMaxPerRoute(MAX_PER_ROUTE_CONNECTIONS);
         ConnManagerParams.setMaxConnectionsPerRoute(httpParams, connPerRoute);
         ConnManagerParams.setTimeout(httpParams, 1000);
         connectionSchemeRegistry = new SchemeRegistry();
         connectionSchemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         connectionSchemeRegistry.register(new Scheme("https", PlainSocketFactory.getSocketFactory(), 80));
         */
        httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeOut);
        HttpConnectionParams.setSoTimeout(httpParams, timeOut);

        ProxySettings proxy = new ProxySettings();
        if (proxy.isManualSetProxy()) {
            proxyBean = new ProxyBean();
            proxyBean.setProxyHost(proxy.getHttpHost());
            proxyBean.setProxyPort(String.valueOf(proxy.getHttpPort()));
            if (proxy.hasAuth()) {
                proxyBean.setProxyUserName(proxy.getUsername());
                proxyBean.setProxyPassword(proxy.getPassword());
            }
        } else {

        }

        canConnect2Website = HttpClient4API.checkConnection(KGG_RESOURCE_URL + "kgg4/version.dat", proxyBean, httpParams);

        pathwayGeneSetCustomModel.addElement("C2: Canonical pathways from the pathway databases(1320 sets)");
        pathwayGeneSetCustomModel.addElement("C2: Curated gene sets from various sources (4722 gene sets)");
        pathwayGeneSetCustomModel.addElement("C4: Computational gene sets by cancer-oriented microarray data (858 sets)");
        pathwayGeneSetCustomModel.addElement("C6: Oncogenic signatures dis-regulated in cancer(189 sets)");
        pathwayGeneSetCustomModel.addElement("Customized Pathway DB in File");
    }

    public static void writeKGGSettings() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.newDocument();
            Element root = doc.createElement("KGGLocalVariables");
            doc.appendChild(root);

            Element variable = doc.createElement("LastAccessedPath");
            root.appendChild(variable);
            Text value = doc.createTextNode(lastAccessedPath == null ? "" : lastAccessedPath);
            variable.appendChild(value);

            variable = doc.createElement("NeedFileLoadHint");
            root.appendChild(variable);
            value = doc.createTextNode(needStartHint ? "Y" : "N");
            variable.appendChild(value);
            variable = doc.createElement("HasSetMemory");
            root.appendChild(variable);
            value = doc.createTextNode(hasSetMemorry ? "Y" : "N");
            variable.appendChild(value);

            if (proxyBean != null) {
                variable = doc.createElement("proxyHost");
                root.appendChild(variable);
                value = doc.createTextNode(proxyBean.getProxyHost() == null ? "N" : proxyBean.getProxyHost());
                variable.appendChild(value);
                variable = doc.createElement("proxyPort");
                root.appendChild(variable);
                value = doc.createTextNode(proxyBean.getProxyPort() == null ? "N" : proxyBean.getProxyPort());
                variable.appendChild(value);
                variable = doc.createElement("proxyUserName");
                root.appendChild(variable);
                value = doc.createTextNode(proxyBean.getProxyUserName() == null ? "N" : proxyBean.getProxyUserName());
                variable.appendChild(value);
            }

            int maxSize = 5;
            int startSize = 0;
            if (maxSize < latestAccessedProjects.size()) {
                startSize = latestAccessedProjects.size() - maxSize;
            }
            maxSize = latestAccessedProjects.size();
            for (int i = startSize; i < maxSize; i++) {
                variable = doc.createElement("LatestAccessedProjects");
                root.appendChild(variable);
                value = doc.createTextNode(latestAccessedProjects.get(i));
                variable.appendChild(value);
            }

            /*
             Element resourceFilesElement = doc.createElement("ResourceFiles");
             root.appendChild(resourceFilesElement);

             for (Entry<String, String> entry : resourceFileSize.entrySet()) {
             Element resourceFile = doc.createElement("ResourceFile");
             resourceFilesElement.appendChild(resourceFile);

             Element fileName = doc.createElement("File");
             resourceFile.appendChild(fileName);
             Text name = doc.createTextNode(entry.getKey());
             fileName.appendChild(name);

             Element fileSize = doc.createElement("Size");
             resourceFile.appendChild(fileSize);
             Text size = doc.createTextNode(entry.getValue());
             fileSize.appendChild(size);
             }
             */
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "GB2312");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            File xmlFile = new File(GlobalManager.LOCAL_USER_FOLDER + "kgg.ini.xml");
            FileOutputStream os = new FileOutputStream(xmlFile);
            PrintWriter pw = new PrintWriter(os);
            StreamResult result = new StreamResult(pw);
            transformer.transform(new DOMSource(doc), result);
            pw.close();
            os.close();

        } catch (ParserConfigurationException pce) {
            throw new Exception(pce.toString());
        } catch (DOMException dom) {
            ErrorManager.getDefault().notify(dom);

        } catch (IOException ioe) {
            // ErrorManager.getDefault().notify(ioe+ " No Initial file!");
            NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getMessage() + " No Initial file!",
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);

        }
    }

    public static void readKGGSettings() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc;
        File settingFile = new File(GlobalManager.LOCAL_USER_FOLDER + "kgg.ini.xml");

        try {
            if (!settingFile.exists()) {
                BufferedWriter bwWrite = new BufferedWriter(new FileWriter(settingFile));
                bwWrite.write("<?xml version=\"1.0\" encoding=\"GB2312\" standalone=\"no\"?>\n"
                        + "<KGGLocalVariables>\n"
                        + "<LastAccessedPath/>\n"
                        + "<NeedFileLoadHint>No</NeedFileLoadHint>\n"
                        + "<proxyHost>N</proxyHost>\n"
                        + "<proxyPort>N</proxyPort>\n"
                        + "<proxyUserName>N</proxyUserName>\n"
                        + "<ResourceFiles/>\n"
                        + "</KGGLocalVariables>");

                bwWrite.close();
            }
            File log4jFile = new File(GlobalManager.LOCAL_USER_FOLDER + "log4j.properties");
            if (!log4jFile.exists()) {
                BufferedWriter bwWrite = new BufferedWriter(new FileWriter(log4jFile));
                bwWrite.write("log4j.rootLogger=INFO,file\n"
                        + " \n"
                        + "#output configuration\n"
                        + "\n"
                        + "log4j.appender.file=org.apache.log4j.RollingFileAppender\n"
                        + "log4j.appender.file.File=" + GlobalManager.LOCAL_USER_FOLDER + "kgg.log\n"
                        + "log4j.appender.file.MaxFileSize=1000KB\n"
                        + "log4j.appender.file.MaxBackupIndex=10\n"
                        + "log4j.appender.file.layout=org.apache.log4j.PatternLayout\n"
                        + "log4j.appender.file.layout.ConversionPattern=%-5p %d{yyyy-MM-dd HH:mm:ss} - %m%n");

                bwWrite.close();
            }

            db = dbf.newDocumentBuilder();
            doc = db.parse(settingFile);

            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("LastAccessedPath");
            if (nodes.getLength() == 1) {
                Element e = (Element) nodes.item(0);
                Text t = (Text) e.getFirstChild();
                if (t == null) {
                    lastAccessedPath = "";
                } else {
                    lastAccessedPath = (t.getNodeValue());
                }
            }

            nodes = root.getElementsByTagName("NeedFileLoadHint");
            if (nodes.getLength() == 1) {
                Element e = (Element) nodes.item(0);
                Text t = (Text) e.getFirstChild();
                if (t != null) {
                    if (t.getTextContent().equals("Y")) {
                        needStartHint = true;
                    } else {
                        needStartHint = false;
                    }
                }
            }

            hasSetMemorry = true;
            /*
             nodes = root.getElementsByTagName("HasSetMemory");
             if (nodes.getLength() == 1) {
             Element e = (Element) nodes.item(0);
             Text t = (Text) e.getFirstChild();
             if (t != null) {
             if (t.getTextContent().equals("Y")) {
             hasSetMemorry = true;
             } else {
             hasSetMemorry = false;
             }
             }
             }
             */
 /*
             String proxyHost = null;
             String proxyPort = null;
             String proxyUserName = null;
             String proxyPassword = null;
             nodes = root.getElementsByTagName("proxyHost");
             if (nodes.getLength() == 1) {
             Element e = (Element) nodes.item(0);
             Text t = (Text) e.getFirstChild();
             if (t == null || t.getNodeValue().equals("N")) {
             proxyHost = null;
             } else {
             proxyHost = (t.getNodeValue());
             }
             }
             nodes = root.getElementsByTagName("proxyPort");
             if (nodes.getLength() == 1) {
             Element e = (Element) nodes.item(0);
             Text t = (Text) e.getFirstChild();
             if (t == null || t.getNodeValue().equals("N")) {
             proxyPort = null;
             } else {
             proxyPort = (t.getNodeValue());
             }
             }

             nodes = root.getElementsByTagName("proxyUserName");
             if (nodes.getLength() == 1) {
             Element e = (Element) nodes.item(0);
             Text t = (Text) e.getFirstChild();
             if (t == null || t.getNodeValue().equals("N")) {
             proxyUserName = null;
             } else {
             proxyUserName = (t.getNodeValue());
             }
             }
             proxyBean = new ProxyBean(proxyHost, proxyPort, proxyUserName, proxyPassword);
             */
            nodes = root.getElementsByTagName("LatestAccessedProjects");
            MRUFilesOptions opts = MRUFilesOptions.getInstance();

            for (int i = 0; i < nodes.getLength(); i++) {
                Element subNode = (Element) nodes.item(i);
                Text t = (Text) subNode.getFirstChild();
                File newFile = new File(t.getNodeValue());
                opts.addFile(newFile.getAbsolutePath());
                latestAccessedProjects.add(newFile.getCanonicalPath());
            }

            NodeList files = root.getElementsByTagName("ResourceFile");
            String fileName = null;
            String fileSize = null;

            for (int i = 0; i < files.getLength(); i++) {
                Element resourceFile = (Element) files.item(i);
                nodes = resourceFile.getElementsByTagName("File");
                if (nodes.getLength() == 1) {
                    Element e = (Element) nodes.item(0);
                    Text t = (Text) e.getFirstChild();
                    fileName = (t.getNodeValue());
                }

                nodes = resourceFile.getElementsByTagName("Size");
                if (nodes.getLength() == 1) {
                    Element e = (Element) nodes.item(0);
                    Text t = (Text) e.getFirstChild();
                    fileSize = (t.getNodeValue());
                }
                if (fileName != null) {
                    resourceFileSize.put(fileName, fileSize);
                }
            }
        } catch (ParserConfigurationException pce) {
            ErrorManager.getDefault().notify(pce);
        } catch (DOMException dom) {
            ErrorManager.getDefault().notify(dom);

        } catch (IOException ioe) {
            // ErrorManager.getDefault().notify(ioe+ " No Initial file!");
            NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getMessage() + " No initial file!",
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (SAXException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }

    }

    public static Project getCurrentProject() {
        return currentProject;
    }
}
