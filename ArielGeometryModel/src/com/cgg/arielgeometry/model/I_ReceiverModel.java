/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.io.I_ReceiverScanner;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.List;

/**
 *
 * @author jgrimsdale
 */
public interface I_ReceiverModel {

    void updatereceiver(int sl, int s, int rl, int r);
    void updatereceivers();
    List<XYLocation> getboundingrectangle();
    void generatereceivers();
    void readfiles(I_ReceiverScanner scanner);
    public boolean isEmpty();
}
