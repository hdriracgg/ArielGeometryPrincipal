/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.io;

import com.cgg.arielgeometry.model.types.Shot;
import com.cgg.arielgeometry.model.types.Receiver;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jgrimsdale
 */
public class ScannerUKOOA implements I_ShotReceiverScanner, I_ReceiverScanner {

    Scanner scanner;
    String name;
    boolean debug = false;
    private Shot shot;
    private Queue<Receiver> receiverQueue;
    int receivergroupoffset = 26;
    int[] recordid = {1, 1};
    int[] linename = {2, 13};
    int[] sourceid = {18, 18};
    int[] pointnumber = {20, 25};
    int[] easting = {47, 55};
    int[] northing = {56, 64};
    int[] time = {74, 79};
    int[] recgrpnb = {1, 4};
    int[] receasting = {5, 13};
    int[] recnorthing = {14, 22};
    int[] recdepth = {23, 26};

    @Override
    public final void setFile(File file) {
        try {
            scanner = new Scanner(file);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(ScannerMOOS.class.getName()).log(Level.SEVERE, null, ex);
        }
        receiverQueue = new LinkedList<>();
        name = file.getName();
    }

//    
//    I_ShotReceiverScanner implementations
//    
    @Override
    public boolean hasNextShotReceiver() {
        String line;
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            if (line.startsWith("S")) {
                shot = parseshot(line);
                parsereceivers();
                return true;
            }
        }
        scanner.close();
        return false;
    }

    @Override
    public boolean hasMoreReceiversForShot() {
        if (receiverQueue.peek() != null) {
            return true;
        }
        return false;
    }

    @Override
    public String getshotname() {
        if (debug) {
            System.out.println("name = " + name);
        }
        return name;
    }

    @Override
    public Shot getShot() {
        return shot;
    }

//    
//    I_ReceiverScanner implementations
//            
    @Override
    public boolean hasNextReceiver() {
        if (receiverQueue.peek() != null) {
            return true;
        }
        if (hasNextShotReceiver()) {
            return true;
        }
        return false;
    }

    @Override
    public String getrecname() {
        return getshotname();
    }

    @Override
    public Receiver getReceiver() {
        Receiver r = receiverQueue.poll();
        return r;
    }

//    
//    Private methods
//    
    private Shot parseshot(String line) {
        long st;
        float sx;
        float sy;
        try {
            st = parseLong(line.substring(pointnumber[0], pointnumber[1]));
            sx = Float.parseFloat(line.substring(easting[0], easting[1]));
            sy = Float.parseFloat(line.substring(northing[0], northing[1]));
        }
        catch (Exception e) {
            System.out.println("line=" + line);
            throw e;
        }
        return new Shot(st, sx, sy);    // depth set to zero for now
    }

    private void parsereceivers() {
        String line;
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            if (line.startsWith("R")) {
                parsereceiverline(line);
            }
            else {
                break;
            }
        }
    }

    private void parsereceiverline(String line) {
        float x;
        float y;
        float z;
        for (int offset = 1; offset <= 54; offset += receivergroupoffset) {
            if (!isBlank(line.substring(offset + receasting[0], offset + receasting[1]))
                    || !isBlank(line.substring(offset + recnorthing[0], offset + recnorthing[1]))
                    || !isBlank(line.substring(offset + recdepth[0], offset + recdepth[1]))) {
                try {
                    x = parseFloat(line.substring(offset + receasting[0], offset + receasting[1]));
                    y = parseFloat(line.substring(offset + recnorthing[0], offset + recnorthing[1]));
                    z = parseFloat(line.substring(offset + recdepth[0], offset + recdepth[1]));
                }
                catch (Exception e) {
                    System.out.println("line=" + line);
                    throw e;
                }
                receiverQueue.add(new Receiver(shot.id, x, y, z));
            }
        }
    }

    private float parseFloat(String s) {
        if (!isBlank(s)) {
            return Float.parseFloat(s);
        }
        else {
            return 0.0f;
        }
    }

    private long parseLong(String s) {
        if (!isBlank(s)) {
            return Long.parseLong(s.substring(1 + s.lastIndexOf(" "), s.length()));
        }
        else {
            return 0;
        }
    }

    private boolean isBlank(String s) {
        if (s.matches("^\\s*$")) {
            return true;
        }
        return false;
    }
}
