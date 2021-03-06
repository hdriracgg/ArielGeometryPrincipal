/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.io;

import com.cgg.arielgeometry.model.GeometryModel;
import com.cgg.arielgeometry.model.I_ShotModel;
import com.cgg.arielgeometry.model.ReceiverModel;
import com.cgg.arielgeometry.model.ShotModel;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jgrimsdale
 */
public class UKOOAwriter {

    private GeometryModel agm;
    private String filename;
    private I_ShotModel asm;
    private ReceiverModel arm;
    private PrintStream ps;

    public UKOOAwriter(GeometryModel agm, String filename) {
        this.filename = filename;
        this.agm = agm;
        this.asm = agm.getasm();
        this.arm = agm.getarm();
    }

    public UKOOAwriter(String directory) {
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String date = sdf.format(today);
        filename = directory + "_source_waypoints_" + date;
        System.out.println("Attempting to write to "+filename);
        try {
            ps = new PrintStream(filename);
        }
        catch (FileNotFoundException ex) {
            System.out.println(directory + " not found");
        }
    }

    public void write() {
        System.out.println("Writing to file " + filename);
        try {
            ps = new PrintStream(filename);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found " + filename);
        }
        writeheader();
        Set<Integer> shotnumberset = asm.getshotnumberset();
        for (Integer shot : shotnumberset) {
            XYLocation shotlocation = asm.getshotlocation(shot);
            writeshot(shot, shotlocation);
            List<XYLocation> receiverlist = arm.getreceiverlocationspershot(shot);
            writereceivers(receiverlist);
        }

        ps.close();
        System.out.println("UKOOA file written " + filename);
    }

    void writeshot(Integer shot, XYLocation location) {
        ps.printf("S                  %6d                     %9.1f%9.1f 0.0\n",
                shot, location.x, location.y);
    }

    public void writeshot(Integer shot, double x, double y) {
        ps.printf("S                  %6d                     %9.1f%9.1f 0.0\n",
                shot, x, y);
    }

    void writereceivers(List<XYLocation> receivers) {
        int groupnumber = 1;
        int idx = 0;

        for (XYLocation location : receivers) {
//            System.out.println("Write receiver " + groupnumber + " " + location.x + " " + location.y);
            idx = groupnumber % 3;
            if (idx == 1) {
                ps.printf("R");
            }
            ps.printf("%4d%9.1f%9.1f    ", groupnumber, location.x, location.y);
            if (idx == 0) {
                ps.printf("1\n");
            }
            groupnumber++;
        }

        for (; idx >= 0; idx--) {
            ps.printf("%26s", "");
            if (idx == 0) {
                ps.printf("1\n");
            }
        }
    }
    
    public void close() {
        ps.close();
        System.out.println("UKOOA written to "+filename);
    }

    public void writeheader() {
        ps.printf("%-80s\n", "H0300CLIENT                     SAUDI ARAMCO");
        ps.printf("%-80s\n", "H0400GEOPHYSICAL CONTRACTOR     ARGAS");
        ps.printf("%-80s\n", "H0500POSITIONING CONTRACTOR     ????");
        ps.printf("%-80s\n", "H0600POSITIONING PROCESSING     ???");
        ps.printf("%-80s\n", "H0700POSITIONING SYSTEM         GATOR               ");
        ps.printf("%-80s\n", "H1800PROJECTION TYPE            001 UNIVERSAL TRANSVERSE MERCATOR");
        ps.printf("%-80s\n", "H1810TOWNSHIP COORDS (TYPE2)    N/A                               ");
        ps.printf("%-80s\n", "H1900PROJECTION ZONE            ZONE 37  NORTHERN HEMISPHERE      ");
        ps.printf("%-80s\n", "H1910PRINCIPLE MERID (TYPE2)    N/A                               ");
        ps.printf("%-80s\n", "H2000GRID UNITS                 1METRES                   1.000000000000");
        ps.printf("%-80s\n", "H2001HEIGHT UNITS               1METRES                   1.000000000000 ");
        ps.printf("%-80s\n", "H2002ANGULAR UNITS              1DEGREES  ");
    }
}
