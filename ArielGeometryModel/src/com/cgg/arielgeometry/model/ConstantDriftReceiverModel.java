/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

/**
 *
 * @author jgrimsdale
 * A receiver model with constant drift
 * Coordinates are real world
 */
public class ConstantDriftReceiverModel extends ReceiverModel {

    public ConstantDriftReceiverModel(GeometryModel agm) {
        super(agm);
    }

    // calculate receiver coordinates with origin 
    // bottom left of receiver patch at time zero
    @Override
    float receiverx(int rl, int r, int sl, int s) {
        return movex(sl, s) + (r * agm.rils);
    }

    @Override
    float receivery(int rl, int r, int sl, int s) {
        return movey(sl, s) + (rl * agm.rcls);
    }

    float movex(int sl, int s) {
        return agm.cspeedx * drifttime(sl, s);
    }

    float movey(int sl, int s) {
        return agm.cspeedy * drifttime(sl, s);
    }

    float drifttime(int sl, int s) {
        int sn = s + (sl * agm.sspl);          // sequential shot number
        return sn * agm.spti;
    }
}
