/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.types;

import com.cgg.arielgeometry.model.types.XYZLocation;

/**
 *
 * @author jgrimsdale
 */
public class Receiver {

    public XYZLocation xyzlocation;
    public Long id;

    public Receiver(Long id,  float x, float y, float z) {
        this.xyzlocation = new XYZLocation(x, y, z);
        this.id = id;
    }

    public void print() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return String.format("Receiver:(id=%d xyzlocation=%s)", id, xyzlocation.toString());
    }
}
