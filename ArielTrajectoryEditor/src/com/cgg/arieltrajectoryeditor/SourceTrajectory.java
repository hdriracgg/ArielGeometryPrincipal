package com.cgg.arieltrajectoryeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Define a source trajectory
 * @author hdrira
 */
public class SourceTrajectory implements Cloneable{
    
    /**
     * list of trajectory points
     */
    private List<Node> sourcePositions;
    
    /**
     * Number of source position with symmetry in the target
     */
    private List<Integer> nbSourcePositionsInTarget;
    
    /**
     * Last angle of the trajectory
     */
    private int angle;

    public SourceTrajectory(List<Node> sourcePositions, List<Integer> nbSourcePositionsInTarget, int angle) {
        this.sourcePositions = sourcePositions;
        this.nbSourcePositionsInTarget = nbSourcePositionsInTarget;
        this.angle = angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getAngle() {
        return angle;
    }

    public List<Node> getSourcePositions() {
        return sourcePositions;
    }

    public void setSourcePositions(List<Node> sourcePositions) {
        this.sourcePositions = sourcePositions;
    }

    public List<Integer> getNbSourcePositionsInTarget() {
        return nbSourcePositionsInTarget;
    }

    public void setNbSourcePositionsInTarget(List<Integer> nbSourcePositionsInTarget) {
        this.nbSourcePositionsInTarget = nbSourcePositionsInTarget;
    }
    
    public int getTotalNbSourcePositionsInTarget() {
        int compteur = 0;
        for(Integer nb: nbSourcePositionsInTarget){
            compteur = compteur + nb;
        }
        return compteur;
    }
    
}
