/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrents;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jgrimsdale
 */
public class TideReader {

    Scanner sc;
    String filename;
    boolean debug = false;
    String date;
    String timeofday;
    float h;

    public TideReader(File file) {
        try {
            sc = new Scanner(file);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(TideReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        filename = file.getName();

        sc.nextLine();   // skip title line
    }

    public boolean hasNextRecord() {
        if (sc.hasNext()) {
            sc.useDelimiter(" ");
            date = nextstring(sc);
            timeofday = nextstring(sc);
            sc.useDelimiter("\n");
            h = nextfloat(sc);
            System.out.println("");
            if(sc.hasNext()) {
                sc.nextLine();
            }
            return (true);
        }
        else {
            return false;
        }
    }

    private String nextstring(Scanner sc) {
        String s = sc.next();
        System.out.printf("%s ", s);
        return s;
    }

    private float nextfloat(Scanner sc) {
        float f;
        String s = sc.next();
        if (s.isEmpty()) {
            f = 0.0f;
        }
        else {
            f = Float.parseFloat(s);
        }
        System.out.printf("%f ", f);
        return f;
    }
}
