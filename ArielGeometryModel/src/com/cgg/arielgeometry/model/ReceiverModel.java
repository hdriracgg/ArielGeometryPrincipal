/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.io.I_ReceiverScanner;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * contains receiver coordinates for each shot point these are real world
 * coordinates
 *
 * @author jgrimsdale
 */
public class ReceiverModel implements I_ReceiverModel {

    public boolean debug = false;
    public TreeMap<Integer, List<XYLocation>> receivermap;
    protected GeometryModel agm;
    // minimum and maximum x, y coordinates of recording nodes
    float minx = Float.POSITIVE_INFINITY;
    float maxx = Float.NEGATIVE_INFINITY;
    float miny = Float.POSITIVE_INFINITY;
    float maxy = Float.NEGATIVE_INFINITY;
    int receivergathers = 0;  // number of receiver gathers
    int locationsread = 0;

    public ReceiverModel(GeometryModel agm) {
        this.agm = agm;
        receivermap = new TreeMap<>();
    }

    // fixed recorder positions - no current
    void populate() {
        agm.updateregularparameters();
//        updatereceivers();
        updateagmparameters();
    }

    @Override
    public void updatereceivers() {
        // Source lines - i
        for (int i = 0; i < agm.ssls; i++) {
            // Sources in line j
            for (int j = 0; j < agm.sspl; j++) {
                // Receiver lines k
                for (int k = 0; k < agm.rls; k++) {
                    // Receivers in line l
                    for (int l = 0; l < agm.rpl; l++) {
                        updatereceiver(i, j, k, l);
                    }
                }
            }
        }
    }

    @Override
    public void updatereceiver(int sl, int s, int rl, int r) {

        if (debug) {
            System.out.println("ArielBinModel.updatebin sl s rl r " + sl + " " + s + " " + rl + " " + r);
        }

        int sn = s + (sl * agm.sspl);          // sequential shot number
        float rx = receiverx(rl, r, sl, s);
        float ry = receivery(rl, r, sl, s);
        addreceiverlocation(sn, rx, ry);   // ariel receiver map
    }

    void addreceiverlocation(int shot, float x, float y) {
        List<XYLocation> xylist = receivermap.get(shot);
        minmaxx(x);
        minmaxy(y);
        if (xylist == null) {
            xylist = new ArrayList<>();
            xylist.add(new XYLocation(x, y));
            receivermap.put(shot, xylist);
            receivergathers++;
        }
        else {
            xylist.add(new XYLocation(x, y));
        }
    }

    void updateagmparameters() {
        agm.rpwh = maxx - minx;
        agm.rpht = maxy - miny;
        agm.rbl = new XYLocation(minx, miny);
        agm.rtr = new XYLocation(maxx, maxy);
    }

    void calculateglobalvalues() {
    }

    // get all locations for a given shot within a given rectangle
    List<XYLocation> getreceiverlist(int shot, XYLocation bottomleft, XYLocation topright) {
        List<XYLocation> receiverlist = receivermap.get(shot);
        return getreceiverlist(receiverlist, bottomleft, topright);
    }

    // get all locations within a given rectangle
    List<XYLocation> getreceiverlist(List<XYLocation> receiverlist, XYLocation bottomleft, XYLocation topright) {
        List<XYLocation> result = new ArrayList<>();
        for (XYLocation xyl : receiverlist) {
            if (inside(xyl, bottomleft, topright)) {
                result.add(xyl);
            }
        }
        return result;
    }

    // return true if a is inside bottom-left and top-right
    boolean inside(XYLocation a, XYLocation bl, XYLocation tr) {
        if (a.x < bl.x) {
            return false;
        }
        if (a.y < bl.y) {
            return false;
        }
        if (a.x > tr.x) {
            return false;
        }
        if (a.y > tr.y) {
            return false;
        }
        return true;
    }

    float minmaxx(float x) {
        if (x < minx) {
            minx = x;
        }
        if (x > maxx) {
            maxx = x;
        }
        return x;
    }

    float minmaxy(float y) {
        if (y < miny) {
            miny = y;
        }
        if (y > maxy) {
            maxy = y;
        }
        return y;
    }

    // calculate receiver coordinates with bin origin
    float receiverx(int rl, int r, int sl, int s) {
        return r * agm.rils;
    }

    float receivery(int rl, int r, int sl, int s) {
        return rl * agm.rcls;
    }

    public List<XYLocation> getreceiverlocationspershot(int shot) {
        return receivermap.get(shot);
    }

    Collection<List<XYLocation>> getreceivercollection() {
        return receivermap.values();
    }

    TreeMap<Integer, List<XYLocation>> getreceivermap() {
        return receivermap;
    }

    @Override
    public List<XYLocation> getboundingrectangle() {
        List<XYLocation> br = new ArrayList<>();
        br.add(new XYLocation(minx, miny));  // bottom left
        br.add(new XYLocation(maxx, maxy));  // top right
        return br;
    }



    @Override
    public void generatereceivers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readfiles(I_ReceiverScanner scanner) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
