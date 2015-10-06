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
public class MetOceanReader {

    Scanner sc;
    String filename;
    boolean debug = false;
    String date_time;
    String id;
    String longitude;
    String latitude;
    long timestamp;
    float x;
    float y;
    String name;
    float vx;
    float vy;
    float v;

    public MetOceanReader(File file) {
        try {
            sc = new Scanner(file);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(MetOceanReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        filename = file.getName();

        sc.nextLine();   // skip title line
    }

    public boolean hasNextRecord() {
        if (sc.hasNext()) {
            sc.useDelimiter(";");
            date_time = nextstring(sc);
            id = nextstring(sc);
            longitude = nextstring(sc);
            latitude = nextstring(sc);
            timestamp = nextlong(sc);
            x = nextfloat(sc);
            y = nextfloat(sc);
            name = nextstring(sc);
            vx = nextfloat(sc);
            vy = nextfloat(sc);
            sc.useDelimiter("\n");
            v = nextfloat(sc);
//            System.out.println("");
            sc.nextLine();
            return (true);
        }
        else {
            return false;
        }
    }

    private String nextstring(Scanner sc) {
        String s = sc.next();
//        System.out.printf("%s ", s);
        return s;
    }

    private long nextlong(Scanner sc) {
        String s = sc.next();
        long l = Long.parseLong(s);
//        System.out.printf("%d ", l);
        return l;
    }

    private float nextfloat(Scanner sc) {
        float f;
        String s = sc.next().replace(",", ".").replace(";", "").trim();
        if (s.isEmpty()) {
            f = 0.0f;
        }
        else {
            f = Float.parseFloat(s);
        }
//        System.out.printf("%f ", f);
        return f;
    }
}
