/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.types;

/**
 *
 * @author jgrimsdale
 */
public class XYZLocation {

    public float x;
    public float y;
    public float z;

    public XYZLocation(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void print() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return String.format("XYZLocation:(x=%f y=%f z=%f)", x, y, z);
    }
}
