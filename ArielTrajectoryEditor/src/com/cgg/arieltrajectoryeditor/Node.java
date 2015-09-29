/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arieltrajectoryeditor;

import java.awt.Point;

/**
 * Marina transitional point
 * @author hdrira
 */
public class Node {
    
    // The coordinates
    private Point coodinates;
    
    // The time from the simulation beginning
    private long time;

    /**
     * 
     * @param coodinates Point coordinates
     * @param time passed time from the beginning to this point
     */
    public Node(Point coodinates, long time) {
        this.coodinates = coodinates;
        this.time = time;
    }
    
    /**
     * 
     * @return point coordinates
     */
    public Point getCoodinates() {
        return coodinates;
    }
    
    /**
     * 
     * @return time passed time from the beginning to this point
     */
    public long getTime() {
        return time;
    }

    /**
     * 
     * @param coodinates point coordinates
     */
    public void setCoodinates(Point coodinates) {
        this.coodinates = coodinates;
    }

    /**
     * 
     * @param time time passed time from the beginning to this point
     */
    public void setTime(long time) {
        this.time = time;
    }
    
    
    
    
    
    
    
    
    
}
