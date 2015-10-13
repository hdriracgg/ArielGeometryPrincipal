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
    public String filename;
    boolean debug = false;
    public String date_time;
    public String buoyname;
    public String longitude;
    public String latitude;
    public String timestamp;
    public long javatimestamp;
    public float x;
    public float y;
    public float depth;
    public float vx;
    public float vy;
    public float v;
    String datetimepattern = "dd/MM/yy HH:mm:ss";
    SimpleDateFormat datetimeformat;
    public Date javadate;
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
            if(date_time.isEmpty()) {
                return false;
            }
            javatimestamp = getjavatimestamp(date_time);
            buoyname = nextstring(sc);
            longitude = nextstring(sc);
            latitude = nextstring(sc);
            timestamp = nextstring(sc);
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
        if(s.startsWith("x")) {
            return 0.0f;
        }
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
