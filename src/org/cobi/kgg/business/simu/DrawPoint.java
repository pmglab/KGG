/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.kgg.business.simu;

/**
 *
 * @author Jiang Li
 */
public class DrawPoint {
    public double dblOdd;
    public double dblFreq;
    public int intRiskNum;
    public double dblLD;
    public double dblGATESPower;
    public double dblFISHERPower;
    public double dblHYTSPower;
    public double dblSKATPower;

    public DrawPoint(double dblOdd, double dblFreq, int intRiskNum, double dblLD, double dblGATESPower,double dblFISHERPower, double dblHYTSPower,double dblSKATPower) {
        this.dblOdd = dblOdd;
        this.dblFreq = dblFreq;
        this.intRiskNum = intRiskNum;
        this.dblLD = dblLD;
        this.dblGATESPower = dblGATESPower;
        this.dblFISHERPower=dblFISHERPower;
        this.dblHYTSPower = dblHYTSPower;
        this.dblSKATPower=dblSKATPower;
    }
    
}
