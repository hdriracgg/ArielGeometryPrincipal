/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.io.I_ShotScanner;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author jgrimsdale dummy shot model contains not shots
 */
public class EmptyShotModel implements I_ShotModel {

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

    public EmptyShotModel(GeometryModel agm, ReceiverModel arm) {
        shotmap = new TreeMap<>();
        this.agm = agm;
        this.arm = arm;
    }

    // shot positions - no current, one boat
    @Override
    public void populate() {
        System.out.println("DummyShotModel.populate: Does nothing");
    }

    @Override
    public void updateshots() {
        System.out.println("DummyShotModel.updateshots: Does nothing");
    }

    @Override
    public void updateshot(int sl, int s, int rl, int r) {
        System.out.println("DummyShotModel.updateshot: Does nothing");
    }

    void updateagmparameters() {
        System.out.println("DummyShotModel.updateparameters: Does nothing");
    }

    @Override
    public void calculateglobalvalues() {
        System.out.println("DummyShotModel.calculateglobalvalues: Does nothing");
    }

    // calculate source coordinates with bin origin
    @Override
    public float sourcex(int sl, int s, int rl, int r) {
        System.out.println("DummyShotModel.sourcex: Does nothing");
        float x = 0.0f;
        return x;
    }

    @Override
    public float sourcey(int sl, int s, int rl, int r) {
        System.out.println("DummyShotModel.sourcey: Does nothing");
        float x = 0.0f;
        return x;
    }

    void addshotlocation(int shot, float x, float y) {
        System.out.println("DummyShotModel.addshotlocation: Does nothing");
    }

    void checkshotlocation(float x, float y) {
        System.out.println("DummyShotModel.checkshotlocation: Does nothing");
    }

    public Set<Integer> getshotnumberset() {
        System.out.println("DummyShotModel.getshotnumberset: Returns null");
        return null;
    }

    @Override
    public XYLocation getshotlocation(int shot) {
        System.out.println("DummyShotModel.getshotlocation: Returns null");
        return null;
    }

    // get all shot numbers within a given rectangle
    @Override
    public List<Integer> getsourcelist(XYLocation bottomleft, XYLocation topright) {
        System.out.println("DummyShotModel.getsourcelist: returns null");
        return null;
    }

    // return true if a is inside bottom-left and top-right
    boolean inside(XYLocation a, XYLocation bl, XYLocation tr) {
        System.out.println("DummyShotModel.inside: Returns false");
        return false;
    }

    // get all of the source locations
    @Override
    public Collection<XYLocation> getsourcecollection() {
        System.out.println("DummyShotModel.getsourcecollection: Returns null");
        return null;
    }

    @Override
    public int getsize() {
        System.out.println("DummyShotModel.getsize: Returns 0");
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void readfiles(I_ShotScanner scanner) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
