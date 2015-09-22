/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.io;

import com.cgg.arielgeometry.model.types.Receiver;
import com.cgg.arielgeometry.model.types.Shot;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jgrimsdale
 */
public class ScannerMOOS implements I_ReceiverScanner, I_ShotScanner {

    Scanner sc;
    String name;
    boolean debug = false;
    private Long t;
    private float x;
    private float y;
    private float z;

    @Override
    public final void setFile(File file) {
        try {
            sc = new Scanner(file);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(ScannerMOOS.class.getName()).log(Level.SEVERE, null, ex);
        }
        name = file.getName();
        sc.useDelimiter(",");
        sc.nextLine();   // skip title line
    }

    @Override
    public String getrecname() {
        if (debug) {
            System.out.println("name = " + name);
        }
        return name;
    }

    @Override
    public boolean hasNextReceiver() {
        if (sc.hasNext()) {
            t = new Long((long) Double.parseDouble(sc.next()));
            x = (float) Double.parseDouble(sc.next());
            y = (float) Double.parseDouble(sc.next());
            z = 0.0f;
            sc.nextLine();
            return (true);
        }
        else {
            return false;
        }
    }

    @Override
    public Receiver getReceiver() {
        return new Receiver(t, x, y, 0);
    }

    @Override
    public boolean hasNextShot() {
        return hasNextReceiver();
    }

    public String getshotname() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Shot getShot() {
        return new Shot(t, x, y);
    }
}
