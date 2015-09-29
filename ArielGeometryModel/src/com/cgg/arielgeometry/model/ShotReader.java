/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.io.DirectoryReader;
import com.cgg.arielgeometry.model.io.I_FileReader;
import com.cgg.arielgeometry.model.io.I_ShotScanner;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author jgrimsdale
 */
public class ShotReader {

    private AbstractGeometryModel agm;
    private static boolean debug = false;
    TreeMap<Integer, XYLocation> shotlocationmap;
    // Key is shot time, values are list of XYLocation for all AUVs, after filtering/interpolation
    private DirectoryReader directoryReader;
    private I_ShotScanner shotScanner;
    private int filesread = 0;
    private int locationsread = 0;
    private int shotid;

    public ShotReader(AbstractGeometryModel agm) {
        shotlocationmap = new TreeMap<>();
        this.agm = agm;
    }

 /**
     * Reads shots from all files in a directory averages or interpolates
     * (TBD) in order to have a single location for each node at each multiple
     * of nominal shot point time interval
     *
     * @param scr An instance of the ShotScanner to be used
     */
    public void readshots(I_ShotScanner scr) {
        shotScanner = scr;
        directoryReader = new DirectoryReader("Shot Trajectory Directory");
        directoryReader.readFiles(new I_FileReader() {
            @Override
            public void readaFile(File file) {
                readFile(file);
            }
        });
        System.out.printf("ShotReader:readshots read %d files and %d locations\n",
                filesread, locationsread);
    }
    
    
    /**
     * Read a file. Can be called by a call back in DirectoryReader
     * Uses shot time for shotid
     *
     * @param file
     */
    public void readFile(File file) {
        System.out.printf("ShotReader:readFile reading filename = %s\n", file.getName());
        shotScanner.setFile(file);
        while (shotScanner.hasNextShot()) {
            com.cgg.arielgeometry.model.types.Shot shot = shotScanner.getShot();
            XYLocation xylocation = shot.xylocation;
            long time = shot.id;
            if (debug) {
                System.out.println("time = " + time + " " + xylocation.x + " " + xylocation.y);
            }
            shotlocationmap.put((int)time, xylocation);
            locationsread++;
        }
        filesread++;
    }
    
    /**
     * Read a file. Can be called by a call back in DirectoryReader
     * To be used if shot times are not available
     *
     * @param file
     */
    public void readFilenotime(File file) {
        System.out.printf("ShotReader:readFile reading filename = %s\n", file.getName());
        shotScanner.setFile(file);
        shotid = -1;
        while (shotScanner.hasNextShot()) {
            shotid++;
            com.cgg.arielgeometry.model.types.Shot shot = shotScanner.getShot();
            XYLocation xylocation = shot.xylocation;
            if (debug) {
                System.out.println("shotid = " + shotid + " " + xylocation.x + " " + xylocation.y);
            }
            shotlocationmap.put(shotid, xylocation);
            locationsread++;
        }
        filesread++;
    }
    
    public TreeMap<Integer, XYLocation> getshotlocationmap() {
        return shotlocationmap;
    }
}
