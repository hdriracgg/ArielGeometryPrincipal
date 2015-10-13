/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrentviewer;

import java.awt.Color;
import java.awt.Paint;

/**
 *
 * @author jgrimsdale
 */
public class InterpolatedColorMaker {

    private float max;
    private float upper;
    private float lower;
    private static boolean debug = false;

    public InterpolatedColorMaker(long lower, long upper, long range) {
//        this.max = 0.1f + (float) range / 3.0f;
        this.max = (float) range / 3.0f;
        this.lower = (float) lower;
        this.upper = (float) upper;
    }

    public Paint getPaint(long value) {
        float a = 0;
        float b = 0;
        float c = 0;
        double d = (double) value - lower;
        Paint paint = null;

        if (d < max) {
            a = (float) d / max;
        }
        if (d >= max && d < max * 2) {
            a = (float) 1.0;
            b = ((float) d - max) / max;
        }
        if (d >= max * 2) {
            a = (float) 1.0;
            b = (float) 1.0;
            c = ((float) d - 2 * max) / max;
            if (c > 1.0f) {
                c = 1.0f;
            }
        }

        try {
            paint = new Color(a, b, c);
        }
        catch (Exception e) {
            System.out.println("InterpolatedColorMaker.getPaint Exception: " + e);
            System.out.println("InterpolatedColorMaker.getPaint: A color component is out of bounds a b c d max: " + a + " " + b + " " + c + " " + d + " " + max);
            throw (e);
        }
        return paint;
    }
}
