/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bluewalrus;

import java.awt.Color;
import java.util.List;

/**
 *
 * @author jgrimsdale
 */
public class PSegment extends Segment{
    public PSegment(int level, Segment parent, double magnitude, String name, Color color) {
        super(level, parent, magnitude, name, color);
    }
    
    public Color getColor() {
        return color;
    }
    
    public List<Segment> getChildren() {
        return children;
    }
}
