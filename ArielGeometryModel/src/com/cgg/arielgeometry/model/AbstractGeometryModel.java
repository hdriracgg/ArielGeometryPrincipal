/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.types.EndCoordinateList;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jgrimsdale
 */
public abstract class AbstractGeometryModel {

    // input parameters
    float rils;             // Receiver InLine Spacing
    float rcls;             // Receiver CrossLine Spacing
    int rpl;                // Receivers Per Line
    int rls;                // Receiver LineS
    public float rss;       // Receiver Swath Spacing
    public int rs;          // number of Receiver swaths
    float sils;             // Source InLine Spacing
    float scls;             // Source CrossLine Spacing
    int sspl;               // Source Shots Per Line
    int ssls;               // Source Shot Lines   
    float bxs;              // Bin X Size
    float bys;              // Bin Y Size
    public float spti;      // Shot interval in seconds
    public int sboats;      // Number of source boats
    public float cspeedx;   // current speed in X direction
    public float cspeedy;   // current speed in Y direction
    public float maxoffset; // Maximum offset used in bin metric calculation
    public float shortoffset; // Maximum offset for the short offset coverage map
    
    // calculated values for receivers
    public int rnbn;            // Receiver NumBer of Nodes (per shot)
    public int rnbpos;          // Total number of receiver positions
    public float rpwh;          // Receiver Patch WidtH
    public float rpht;          // Receiver Patch HeighT
    public XYLocation rbl;      // Receiver Bottom Left
    public XYLocation rtr;      // Receiver Top Right
    public float rstartswh;     // Receiver start of survey SWath Height
    public float rendswh;       // Receiver end of survey SWath Height;
    
    // calculated values for shots
    public int snbs;            // Source NumBer of Shots
    public float spwh;          // Source Patch WidtH
    public float spht;          // Source Patch HeighT
    public XYLocation sbl;      // Source Bottom Left
    public XYLocation str;      // Source Top Right
    
    // calculated values for bins
    public int binsx;                // BINS in X direction
    public int binsy;                // BINS in Y direction
    public float binoriginx;         // x coordinate of the centre of bin[0][0]
    public float binoriginy;         // x coordinate of the centre of bin[0][0]
    public int   covmax;             // maximum coverage
    public float covave;             // average coverage
    public float covarea;            // area covered in km2 at >= average coverage
    public float tracedensity;       // average traces per m2 in bins with greater than average coverage
    public float maxxoffset;         // max offset over whole survey in x direction
    public float maxyoffset;         // max offset over whole survey in y direction
    public float maxrealoffset;      // max real offset actually found (not updatedted yet)
    public int   nbnonemptybins;     // number of bins with coverage >= 1
    public int   totaltraces;        // total number of traces recorded
    float binxor;               // Bin x coordinate of first receiver
    float binyor;               // Bin y coordinate of first receiver line
    float binxos;               // Bin x coordinate of first source line
    float binyos;               // Bin y coordinate of first source
    int binxornb;               // Bin number x  of first receiver
    int binyornb;               // Bin number y  of first receiver line
    int binxosnb;               // Bin number x  of first source line
    int binyosnb;               // Bin number y  of first source
    int azimuthsectors;         // number of sectors over 2pi radians
    public float boatspeed;     // Approximate source vessel speed in m/s
    public float recordingtime; // recording time in days
    
    // models
    public I_ShotModel asm;
    public ReceiverModel arm;
    public BinModel abm;

    // methods
    public abstract void printbindetails();

    public abstract void calculate();

    public abstract int[][] getbincoverage();
    
    public abstract int[][] getshortoffsetcoverage();
    
    public abstract int getbincoverage(float x, float y);

    public abstract List<XYLocation> getsources();

    public abstract List<List<XYLocation>> getreceivers();
    
    public abstract Map<Integer, List<XYLocation>> getshotsreceivers(XYLocation binlocation);
    
    public abstract EndCoordinateList getxyoffsets(XYLocation binlocation);

    // setters and getters
    public int getBINSX() {
        return binsx;
    }

    public int getBINSY() {
        return binsy;
    }

    public void setRCLS(float rcls) {
        this.rcls = rcls;
    }

    public void setRPL(int rpl) {
        this.rpl = rpl;
    }

    public void setRLS(int rls) {
        this.rls = rls;
    }

    public float getRILS() {
        return rils;
    }

    public void setRILS(float rils) {
        this.rils = rils;
    }

    public float getRCLS() {
        return rcls;
    }

    public int getRPL() {
        return rpl;
    }

    public int getRLS() {
        return rls;
    }

    public void setSILS(float sils) {
        this.sils = sils;
    }

    public void setSCLS(float scls) {
        this.scls = scls;
    }

    public void setSSPL(int sspl) {
        this.sspl = sspl;
    }

    public void setSSLS(int ssls) {
        this.ssls = ssls;
    }

    public float getSILS() {
        return sils;
    }

    public float getSCLS() {
        return scls;
    }

    public int getSSPL() {
        return sspl;
    }

    public int getSSLS() {
        return ssls;
    }

    public void setBXS(float bxs) {
        this.bxs = bxs;
    }

    public void setBXY(float bys) {
        this.bys = bys;
    }

    public float getBXS() {
        return bxs;
    }

    public float getBYS() {
        return bys;
    }

    public void setMAXOFFSET(float maxoffset) {
        this.maxoffset = maxoffset;
    }

    public float getMAXOFFSET() {
        return maxoffset;
    }

    public int getRNBN() {
        return rnbn;
    }

    public float getRPWH() {
        return rpwh;
    }

    public float getRPHT() {
        return rpht;
    }

    public int getRNBPOS() {
        return rnbpos;
    }

    public int getSNBS() {
        return snbs;
    }

    public float getSPWH() {
        return spwh;
    }

    public float getSPHT() {
        return spht;
    }
}
