/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.dialog;

import org.openide.util.ImageUtilities;

/**
 *
 * @author mxli
 */
public class FormatShowingDialog extends javax.swing.JDialog {

  String content1 = "\n"
      + "Formats of the PPI dataset in a text file,  \n"
      + "[Column 1: Gene symbol of PPI partner 1]\n"
      + "[Column 2: Gene symbol of PPI partner 2]\n"
      + "[Column 3: Confidence score of PPI; optional].\n"
      + "\n"
      + "No title row is required!!!\n"
      + "\n"
      + "\n"
      + "e.g., \n"
      + "----------------------------------------------\n"
      + "ARF5	PLEK	0.904\n"
      + "ARF5	CYTH4	0.772\n"
      + "ARF5	ARFIP2	0.982\n"
      + "ARF5	ARFGEF1	0.723\n"
      + "ARF5	RAB11FIP3	0.921\n"
      + "...\n"
      + "...\n";
  String content2 = "Formats of the pathway dataset in a text file,  \n"
      + "[Column 1: Pathway ID]\n"
      + "[Column 2: Pathway URL]\n"
      + "[Column 3..N: Gene Symbols in the pathway; separated by spaces].\n"
      + "\n"
      + "No title row is required!!!\n"
      + "\n"
      + "\n"
      + "e.g., \n"
      + "----------------------------------------------\n"
      + "KEGG_GLYCOLYSIS_GLUCONEOGENESIS	http://www.broadinstitute.org/ KEGG_GLYCOLYSIS_GLUCONEOGENESIS.html	LDHC	LDHB	LDHA \n"
      + "KEGG_CITRATE_CYCLE_TCA_CYCLE	http://www.broadinstitute.org/KEGG_CITRATE_CYCLE_TCA_CYCLE.html	GJB1	OGDHL	OGDH	PDHB	IDH3G	LDHB \n"
      + "...\n"
      + "...";
  String content3 = "Please open your R and type the following commands to allow kgg to use it:\npack=\"Rserve\";\n"
      + "if (!require(pack,character.only = TRUE))   { install.packages(pack,dep=TRUE,repos=\'http://cran.us.r-project.org\');   if(!require(pack,character.only = TRUE)) stop(\"Package not found\")   }\n"
      + "library(\"Rserve\");\nRserve(debug = FALSE, port = 6311, args = NULL)\n";
  String content4 = "Formats of the customized gene regions in a text file,  \n"
      + "[Column 1: Gene ID/Symbols]\n"
      + "[Column 2: Regions]\n"
      + " \n"
      + "No title row is required!!!\n"
      + "Regions are speperated by commas.\n"
      + "e.g., \n"
      + "----------------------------------------------\n"
      + "CYLD chr16:50787463-50787473,chr16:50787483-50808726,chr16:50766127\n"
      + "SAG chr2:234223837-234247627\n"
      + "...";

  String content5 = "Formats of the variant weights in a text file,  \n"
      + "[Column 1: Chromosome]\n"
      + "[Column 2: Coordinate/Positions] it must be consistent with that in your p-value file.\n"
      + "[Column 3..N: Weights].\n"
      + "\n"
      + "\n"
      + "\n"
      + "e.g., \n"
      + "----------------------------------------------\n"
      + "chr	hg19	w1	w2	w3	w4	w5	w6\n"
      + "1	768448	0.369630396	0.369630396	.	0.369630396	0.369630396	0.369630396\n"
      + "1	1005806	.	0.369630396	0.862154841	0.849507987	0.701068699	0.369630396\n"
      + "1	1018704	0.369630396	.	0.369630396	0.369630396	0.369630396	0.369630396\n"
      + "1	1021415	0.369630396	0.369630396	0.369630396	0.369630396	0.369630396	.\n"
      + "1	1030565	0.369630396	0.369630396	0.369630396	0.369630396	0.369630396	0.369630396\n"
      + "1	1031540	.	0.369630396	0.369630396	0.369630396	0.369630396	0.369630396\n"
      + "1	1048955	0.897760808	0.922087491	0.48629421	0.369630396	0.369630396	0.485284001\n"
      + "1	1049950	0.608408928	0.948870718	0.487304538	0.479740083	0.369630396	0.682737172\n"
      + "1	1061166	0.369630396	0.369630396	0.369630396	0.369630396	0.723233521	0.369630396\n"
      + "1	1062638	0.369630396	0.369630396	0.369630396	0.369630396	0.413617402	0.369630396\n"
      + "1	1064979	.	0.369630396	0.369630396	0.369630396	.	0.369630396\n"
      + "1	1066029	0.369630396	0.369630396	0.369630396	0.369630396	0.686898708	0.369630396\n"
      + "1	1087683	0.369630396	0.369630396	0.369630396	0.369630396	0.369630396	0.369630396\n"
      + "1	1090557	0.369630396	0.369630396	0.369630396	0.369630396	0.369630396	0.369630396\n"
      + "1	1094738	0.369630396	0.369630396	.	0.369630396	0.369630396	0.369630396\n"
      + "1	1099342	0.369630396	0.369630396	0.369630396	0.369630396	0.369630396	0.92891258\n"
      + "1	1106473	0.369630396	0.727351964	0.369630396	0.369630396	0.369630396	0.369630396\n"
      + "1	1119858	0.369630396	0.369630396	0.369630396	0.369630396	0.727351964	0.369630396\n"
      + "1	1121794	0.369630396	.	0.369630396	0.369630396	0.369630396	.\n"
      + "1	1135242	0.369630396	0.369630396	0.369630396	0.369630396	0.62657547	0.369630396\n"
      + "1	1152631	0.369630396	0.76103586	0.369630396	0.369630396	0.768859923	0.369630396\n"
      + "1	1156131	0.772704124	0.812653244	0.369630396	0.369630396	0.757056296	0.608087718\n"
      + "1	1158277	0.812653244	0.983079314	0.916981459	0.908968985	0.805937469	0.369630396\n"
      + "...\n"
      + "...\n"
      + "...";

  String content6 = "Format of gene-variant map in a text file,  \n"
      + "[Column named 'gene': Ensemble Gene ID or HGNC Gene Symbol]\n"
      + "[Column named 'chr': chromosome]\n"
      + "[Column named 'pos': physical positions of a variant on the chromosome]\n"
      + "\n"
      + "\n"
      + "The above three title names are required!!! But the order can be arbitrary\n"
      + "\n"
      + "e.g., \n"
      + "----------------------------------------------\n"
      + "gene		chr	pos\n"
      + "ENSG00000131591.13	1	1027845\n"
      + "ENSG00000131591.13	1	1027846\n"
      + "NBN	1	1029805\n"
      + "A2M	1	1030565\n"
      + "ENSG00000131591.13	1	1030633\n"
      + "...\n"
      + "...\n"
      + "...";

  String expressionFormat = "Formats of the expression values in a text file,  \n"
      + "[Column 1: Gene symbol]: It can be gene symbol only or gene symbol:transcript ID\n"
      + "[Column 2: Name of tissue 1]: The averaged expression values of tissue 1\n"
      + "[Column 3: Name of tissue 1+\".SE\"]: The standard error of averaged expression values of tissue 1\n"
      + "[Column 4: Name of tissue 2]: The averaged expression values of tissue 2\n"
      + "[Column 5: Name of tissue 2+\".SE\"]: The standard error of averaged expression values of tissue 2\n"
      + "[Column 6: Name of tissue 3]: The averaged expression values of tissue 3\n"
      + "[Column 7: Name of tissue 3+\".SE\"]: The standard error of averaged expression values of tissue 3\n"
      + "[…]\n"
      + "\n"
      + "\n"
      + "e.g., \n"
      + "----------------------------------------------\n"
      + "Gene	Heart-AtrialAppendage	Heart-AtrialAppendage.SE	Ovary	Ovary.SE	Vagina	Vagina.SE	Adipose-Visceral(Omentum)	Adipose-Visceral(Omentum).SE	Uterus	Uterus.SE\n"
      + "PRPF19:ENST00000541371	0.135354	0.019945	1.17797	0.139247	1.229826	0.168586	0.458648	0.053846	1.723694	0.234283\n"
      + "SLC3A2:ENST00000541372	0.774209	0.049455	2.388647	0.173169	2.290696	0.184308	2.614282	0.110435	3.147568	0.224166\n"
      + "IL12RB2:ENST00000541374	0.003131	5.84E-04	0.008647	0.002579	0.012609	0.002737	0.075549	0.005937	0.010541	0.002491\n"
      + "MADD:ENST00000311027	0.730034	0.044154	0.006241	0.002944	0.007565	0.003146	0.00769	0.001813	0.009189	0.004613\n"
      + "NCALD:ENST00000311028	2.206566	0.088592	9.92188	0.526913	1.935565	0.237534	4.14031	0.155685	2.12036	0.162578\n"
      + "TAS2R63P:ENST00000541376	0.017778	0.001865	0.063759	0.006861	0.046957	0.007366	0.036	0.003072	0.040811	0.006366\n"
      + "CLASP1:ENST00000541377	1.656128	0.054217	4.377143	0.130577	3.657652	0.116301	3.342789	0.07723	4.487207	0.146652\n"
      + "RP11-25D3.1:ENST00000580975	0.003468	8.46E-04	0.029248	0.005196	0.008435	0.002604	0.013239	0.001858	0.005676	0.002006\n"
      + "RHOT1:ENST00000580976	0.003939	0.002467	NaN	NaN	0.003043	0.003043	0.006873	0.003707	0.01964	0.014333\n"
      + "C19orf47:ENST00000580977	0.610774	0.041243	0.214887	0.023969	0.358522	0.0369	0.252479	0.016071	0.343063	0.038473"
      + "...\n"
      + "...\n"
      + "...";

  /**
   * Creates new form FormatShowingDialog
   */
  public FormatShowingDialog(java.awt.Frame parent, boolean modal, String type) {
    super(parent, modal);
    initComponents();
    setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));
    this.setTitle(type + " example file!");
    if (type.equals("Gene-pair")) {
      contentTextArea.setText(content1);
    } else if (type.equals("Rserve")) {
      contentTextArea.setText(content3);
    } else if (type.equals("Pathway")) {
      contentTextArea.setText(content2);
    } else if (type.equals("VarWeight")) {
      contentTextArea.setText(content5);
    } else if (type.equals("GeneVarMap")) {
      contentTextArea.setText(content6);
    } else if (type.equals("GeneExpression")) {
      contentTextArea.setText(expressionFormat);
    } else {
      contentTextArea.setText(content4);
    }

  }

  public FormatShowingDialog(java.awt.Frame parent, boolean modal, String title, String content) {
    super(parent, modal);
    initComponents();
    setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));
    this.setTitle(title);
    contentTextArea.setText(content);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        contentTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        contentTextArea.setEditable(false);
        contentTextArea.setColumns(20);
        contentTextArea.setLineWrap(true);
        contentTextArea.setRows(5);
        jScrollPane1.setViewportView(contentTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(FormatShowingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(FormatShowingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(FormatShowingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(FormatShowingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the dialog */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        FormatShowingDialog dialog = new FormatShowingDialog(new javax.swing.JFrame(), true, null);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            System.exit(0);
          }
        });
        dialog.setVisible(true);
      }
    });
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea contentTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
