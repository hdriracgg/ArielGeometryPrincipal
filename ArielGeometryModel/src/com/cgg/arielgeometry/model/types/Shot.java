/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.types;

/**
 *
 * @author jgrimsdale
 */
public class Shot {

    public XYLocation xylocation;
    public Long id;

    public Shot(Long id,  float x, float y) {
        this.xylocation = new XYLocation(x, y);
        this.id = id;
    }

    public void print() {
        System.out.println(toString(this));
    }

    public String toString(Shot xyzl) {
        return String.format("Shot.print id=%l xylocation=%s", id, toString(xyzl));
    }
}
