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
import javax.swing.JFileChooser;
import org.openide.util.Exceptions;

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
    String datetimepattern = "dd/MM/yy HH:mm";
    SimpleDateFormat datetimeformat;
    Date javadate;
    long javatimestamp;

    public TideReader(File file) {
                
        try {
            sc = new Scanner(file);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(TideReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        filename = file.getName();

        datetimeformat = new SimpleDateFormat(datetimepattern);
    }

    public boolean hasNextRecord() {
        if (sc.hasNext()) {
            sc.useDelimiter(" ");
            date = nextstring(sc);
            timeofday = nextstring(sc);
            String datestring = date + " " + timeofday;
            try {
                javadate = datetimeformat.parse(datestring);
            }
            catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            javatimestamp = (javadate.getTime()/1000);
            sc.useDelimiter("\n");
            h = nextfloat(sc);
//            System.out.printf("day=%s hour=%s javatimestamp=%d height=%f", date, timeofday, javatimestamp, h);
//            System.out.println("");
            if (sc.hasNext()) {
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
//        System.out.printf("%s ", s);
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
//        System.out.printf("%f ", f);
        return f;
    }
}
