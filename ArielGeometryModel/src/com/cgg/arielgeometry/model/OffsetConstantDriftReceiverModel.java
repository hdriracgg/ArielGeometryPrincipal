/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

/**
 *
 * @author jgrimsdale A receiver model with constant drift and offset in Y
 * direction Coordinates are real world
 */
public class OffsetConstantDriftReceiverModel extends ConstantDriftReceiverModel {

    private float offset;
    private int swath;
    private int sourcepointsperswath;

    public OffsetConstantDriftReceiverModel(GeometryModel agm) {
        super(agm);
    }

    // moving recorder positions with multiple swaths
    @Override
    void populate() {
        agm.rstartswh = (agm.rcls * (agm.rpl-1)) + (agm.rss*(agm.rs-1));
        agm.rendswh = agm.rstartswh;
        sourcepointsperswath = agm.ssls * agm.sspl;
        float rcvrswathspacing = agm.rss;
        int rcvrswaths = agm.rs;
        agm.updateregularparameters();
        for (swath = 0; swath < rcvrswaths; swath++) {
            offset = swath * rcvrswathspacing;
            updatereceivers();
        }
        updateagmparameters();
        System.out.printf("ArielOffsetConstantDriftReceiverModel.populate receivergathers = %d\n", receivergathers);
    }

    @Override
    public void updatereceiver(int sl, int s, int rl, int r) {

        if (debug) {
            System.out.println("ArielBinModel.updatebin sl s rl r " + sl + " " + s + " " + rl + " " + r);
        }

        float rx = receiverx(rl, r, sl, s);
        float ry = receivery(rl, r, sl, s);
        int sn = s + (sl * agm.sspl) + (swath * sourcepointsperswath);   // sequential shot number
        addreceiverlocation(sn, rx, ry);   // ariel receiver map
    }

    // Offset the receiver Y
    @Override
    float receivery(int rl, int r, int sl, int s) {
        return movey(sl, s) + (rl * agm.rcls) + offset;
    }
}
