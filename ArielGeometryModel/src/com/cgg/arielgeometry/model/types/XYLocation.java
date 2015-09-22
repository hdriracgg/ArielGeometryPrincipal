/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.types;

/**
 *
 * @author jgrimsdale
 */
public class XYLocation {

    public float x;
    public float y;

    public XYLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void print() {
        System.out.println("XYLocation.print x=" + x + " y=" + y);
    }
}
