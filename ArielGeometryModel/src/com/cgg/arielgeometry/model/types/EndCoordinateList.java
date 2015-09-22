/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.types;

import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.List;

/**
 *
 * @author jgrimsdale
 */
public class EndCoordinateList {
    public List<XYLocation> list;
    public float maxx = 0;
    public float maxy = 0;
    
    public EndCoordinateList(List<XYLocation> list) {
        this.list = list;
    }
    
    public EndCoordinateList(List<XYLocation> list, float x, float y) {
        this(list);
        maxx = x;
        maxy = y;
    }
}
