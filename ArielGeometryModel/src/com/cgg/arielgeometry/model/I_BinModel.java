/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.List;

/**
 *
 * @author jgrimsdale
 */
public interface I_BinModel {

    float[] bin2xy(int x, int y);

    XYLocation getblshotprojection(Integer shot, XYLocation binlocation);

    // find a list of x offset / y offset pairs that illuminate a bin
    // offset is negative if receiver ordinate is less than shotpoint ordinate
    List<XYLocation> getsources(int[] bin);

    XYLocation gettrshotprojection(Integer shot, XYLocation binlocation);
    
    int getbincoverage(float x, float y);
    
    int[][] getcoveragearray();
    
    int[][] getshortoffsetcoveragearray();

    void populate();

    int[] xy2bin(float x, float y);
    
    boolean isEmpty();
}
