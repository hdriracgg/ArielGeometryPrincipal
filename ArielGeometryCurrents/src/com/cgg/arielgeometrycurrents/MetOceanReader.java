/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrents;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author jgrimsdale
 */
public class MetOceanReader {

    Scanner sc;
    String filename;
    boolean debug = false;
    String date_time;
    String buoyname;
    String longitude;
    String latitude;
    long timestamp;
    long javatimestamp;
    float x;
    float y;
    float depth;
    float vx;
    float vy;
    float v;
    String datetimepattern = "dd/MM/yy HH:mm:ss";
    SimpleDateFormat datetimeformat;
    Date javadate;
    long calculatedtimestamp;

    public MetOceanReader(File file) {
        try {
            sc = new Scanner(file);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(MetOceanReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        filename = file.getName();
        datetimeformat = new SimpleDateFormat(datetimepattern);
        sc.nextLine();   // skip title line
    }

    public boolean hasNextRecord() {
        if (sc.hasNext()) {
            sc.useDelimiter(";");
            date_time = nextstring(sc);
            javatimestamp = getjavatimestamp(date_time);
            buoyname = nextstring(sc);
            longitude = nextstring(sc);
            latitude = nextstring(sc);
            timestamp = nextlong(sc);
            x = nextfloat(sc);
            y = nextfloat(sc);
            depth = nextfloat(sc);
            vx = nextfloat(sc);
            vy = nextfloat(sc);
            sc.useDelimiter("\n");
            v = nextfloat(sc);
            sc.nextLine();
            return (true);
        }
        else {
            return false;
        }
    }

    long getjavatimestamp(String s) {
        try {
            javadate = datetimeformat.parse(s);
        }
        catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return (javadate.getTime() / 1000);
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
