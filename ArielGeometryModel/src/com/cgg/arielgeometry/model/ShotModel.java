/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.io.I_ShotScanner;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author jgrimsdale simple shot model contains coordinates for each shot point
 * shot grid is fixed and for one boat only
 */
public class ShotModel implements I_ShotModel {

    public boolean debug = false;
    protected Map<Integer, XYLocation> shotmap;
    protected ReceiverModel arm;
    protected GeometryModel agm;
    // minimum and maximum x, y coordinates of shots
    float minx = Float.POSITIVE_INFINITY;
    float maxx = Float.NEGATIVE_INFINITY;
    float miny = Float.POSITIVE_INFINITY;
    float maxy = Float.NEGATIVE_INFINITY;
    int shotlocations = 0;

    public ShotModel(GeometryModel agm, ReceiverModel arm) {
        shotmap = new TreeMap<>();
        this.agm = agm;
        this.arm = arm;
    }

    // shot positions - no current, one boat
    @Override
    public void populate() {
        agm.sboats = 1;
        updateshots();
        updateagmparameters();
    }

    @Override
    public void updateshots() {
        System.out.println("ArielShotModel.updateshots BINSX = " + agm.binsx + " BINSY = " + agm.binsy);
        // Source lines - i
        for (int i = 0; i < agm.ssls; i++) {
            // Sources in line j
            for (int j = 0; j < agm.sspl; j++) {
                // Receiver lines k
                for (int k = 0; k < agm.rls; k++) {
                    // Receivers in line l
                    for (int l = 0; l < agm.rpl; l++) {
                        updateshot(i, j, k, l);
                    }
                }
            }
        }
    }

    @Override
    public void updateshot(int sl, int s, int rl, int r) {

        if (debug) {
            System.out.println("ArielBinModel.updatebin sl s rl r " + sl + " " + s + " " + rl + " " + r);
        }

        int sn = s + (sl * agm.sspl);          // sequential shot number
        float sx = sourcex(sl, s, rl, r);
        float sy = sourcey(sl, s, rl, r);
        addshotlocation(sn, sx, sy);       // ariel shot map
    }

    void updateagmparameters() {
        agm.sbl = new XYLocation(minx, miny);
        agm.str = new XYLocation(maxx, maxy);
    }
    
    @Override
    public void calculateglobalvalues() {
        int nbshots = agm.ssls * agm.sspl;
        float timeinsecs = agm.spti * nbshots;
        agm.recordingtime = timeinsecs/(24.0f*3600.0f);
        float boatspeedinmpersec = (nbshots*agm.sils)/timeinsecs;
        agm.boatspeed = boatspeedinmpersec;
    }
    
    // calculate source coordinates with bin origin
    @Override
    public float sourcex(int sl, int s, int rl, int r) {
        return agm.binxos + sl * agm.scls;
    }

    @Override
    public float sourcey(int sl, int s, int rl, int r) {
        return agm.binyos + s * agm.sils;
    }

    void addshotlocation(int shot, float x, float y) {
        checkshotlocation(x, y);
        XYLocation xyl = shotmap.get(shot);
        if (xyl == null) {
            xyl = new XYLocation(x, y);
            shotmap.put(shot, xyl);
        }
        shotlocations++;
    }

    void checkshotlocation(float x, float y) {
        if (x < minx) {
            minx = x;
        }
        if (x > maxx) {
            maxx = x;
        }
        if (y < miny) {
            miny = y;
        }
        if (y > maxy) {
            maxy = y;
        }
    }

    public Set<Integer> getshotnumberset() {
        return shotmap.keySet();
    }
    
    @Override
    public XYLocation getshotlocation(int shot) {
        return shotmap.get(shot);
    }
    
    // get all shot numbers within a given rectangle
    @Override
    public List<Integer> getsourcelist(XYLocation bottomleft, XYLocation topright) {
        List<Integer> result = new ArrayList<>();
        for(Integer shot : shotmap.keySet()) {
            XYLocation xyl = shotmap.get(shot);
            if(inside(xyl, bottomleft, topright)) {
                result.add(shot);
            }
        }
        return result;
    }
    
    // return true if a is inside bottom-left and top-right
    boolean inside(XYLocation a, XYLocation bl, XYLocation tr) {
        if(a.x < bl.x) return false;
        if(a.y < bl.y) return false;
        if(a.x > tr.x) return false;
        if(a.y > tr.y) return false;
        return true;
    }
    
    /**
     * Get all of the source locations
     * @return
     */
    @Override
    public Collection<XYLocation> getsourcecollection() {
        return shotmap.values();
    }

    @Override
    public int getsize() {
        return shotlocations;
    }

    @Override
    public boolean isEmpty() {
        if( shotlocations == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void readfiles(I_ShotScanner scanner) {
        ShotReader sr = new ShotReader(agm);
    }
}
