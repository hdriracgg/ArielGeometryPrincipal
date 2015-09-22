/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.types.XYLocation;

/**
 *
 * @author jgrimsdale
 */
public class SREndCoordinate extends XYLocation {
    public int type;
    
    public SREndCoordinate(float x, float y, int type) {
        super(x, y);
        this.type = type;
    }   
}
