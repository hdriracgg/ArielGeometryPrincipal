/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrents;

import java.awt.Point;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFileChooser;

/**
 *
 * @author jgrimsdale
 */
public class MetOceanModel {

    //    Each buoy has a Map of date, record
    //   Map<buoyname, Map<javadate, MORrecord>>
    public Map<String, Map<Date, MORecord>> buoyMap;
    public Date maxdate = new Date(Long.MIN_VALUE);
    public Date mindate = new Date(Long.MAX_VALUE);
    public long mintime;
    public long maxtime;
    public long timerange;
    // converts seconds to metres in distance calculation
    private float timefactor = 0.000000001f;
    // converts depth to pseudo metres in distance calculation
    private float depthfactor = 100.0f;

    public MetOceanModel() {
        buoyMap = new HashMap<>();
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
        jfc.setDialogTitle("MetOceanFile");
        jfc.showOpenDialog(jfc);
        File file = jfc.getSelectedFile();
        MetOceanReader mor = new MetOceanReader(file);
        int records = 0;
        int duplicates = 0;
        while (mor.hasNextRecord()) {
            records++;
            MORecord record = new MORecord();
            Date javadate = mor.javadate;
            setminmax(javadate);
            record.javadate = javadate;
            record.depth = mor.depth;
            float[] current = new float[2];
            current[0] = mor.vx;
            current[1] = mor.vy;
            record.current = current;
            Point position = new Point((int) mor.x, (int) mor.y);
            record.position = position;
            String buoyname = mor.buoyname;
            record.buoy = buoyname;
            if (buoyMap.containsKey(buoyname)) {
                if (buoyMap.get(buoyname).containsKey(javadate)) {
                    duplicates++;
                    System.out.printf("duplicate: buoyname=%s datetime=%s x1=%d x2=%f\n", buoyname, mor.date_time,
                            buoyMap.get(buoyname).get(javadate).position.x,
                            mor.x);
                }
                buoyMap.get(buoyname).put(javadate, record);
            }
            else {
                Map<Date, MORecord> m = new TreeMap<>();
                m.put(javadate, record);
                buoyMap.put(buoyname, m);
            }
        }
        mintime = mindate.getTime();
        maxtime = maxdate.getTime();
        timerange = maxtime - mintime;
        System.out.printf("%d records of which %d duplicates read from file %s\n", records, duplicates, file.getAbsolutePath());
    }

    private void setminmax(Date d) {
        if (d.before(mindate)) {
            mindate = d;
        }
        if (d.after(maxdate)) {
            maxdate = d;
        }
    }

    // t is MetOceanModel time
    // negative means select only in the future
    // positive means select only in the past
    public MORecord findclosest(int x, int y, long t, float depth) {
        MORecord result = null;
        MORecord r;
        float min = Float.MAX_VALUE;
        for (String buoy : buoyMap.keySet()) {
            for (Date d : buoyMap.get(buoy).keySet()) {
                r = buoyMap.get(buoy).get(d);
                float distance = getpseudodistance(r, x, y, t, depth);
                if (distance < min) {
                    min = distance;
                    result = r;
                }
            }
        }
        return (result);
    }

    float[] getvv(int x, int y, long t, float depth) {
        return findclosest(x, y, t, depth).current;
    }
    
    public Map<Date, MORecord> getrecordsbybuoy(String name) {
        return buoyMap.get(name);
    }

    public Date getmomDate(long timefromstart) {
        return new Date(timefromstart + mindate.getTime());
    }

    private float getpseudodistance(MORecord r, int x, int y, long t, float depth) {

        float xdistance = r.position.x - x;
        float ydistance = r.position.y - y;
        float result = (float) Math.hypot(xdistance, ydistance);

        boolean future = false;
        if (t < 0) {
            future = true;
            t = t * -1;
        }

        // process time pseudodistance
        long timediff = r.javadate.getTime() - t;
        timediff = timediff / 1000;  // convert to seconds
        // only look in MetOcean past
        if (!future && timediff > 0) {
            return Float.MAX_VALUE;
        }
        // or only look in MetOcean future
        if (future && timediff < 0) {
            return Float.MAX_VALUE;
        }
        float timedistance = timediff * timefactor;
        result = (float) Math.hypot(result, timedistance);

        // process depth pseudodistance
        float depthdistance = depthfactor * (depth - r.depth);
        result = (float) Math.hypot(result, depthdistance);

//        System.out.printf("xd=%f yd=%f td=%f dd=%f result=%f\n",
//                xdistance, ydistance, timedistance, depthdistance, result);
        return result;
    }

    public class MORecord {

        public String buoy;
        public Date javadate;
        public float depth;
        public float[] current;
        public Point position;

        // get apparent speed in m/s
        public double getapparentspeed(MORecord b) {
            if (b == null) {
                return Double.MAX_VALUE;
            }
            double distance = Math.hypot(position.x - b.position.x, position.y - b.position.y);
            long timedifference = (long) (Math.abs(javadate.getTime() - b.javadate.getTime()) / 1000.0);
            return distance / timedifference;
        }

        public long getTimeDifference(MORecord b) {
            if (b == null) {
                return 0;
            }
            else {
                return javadate.getTime() - b.javadate.getTime();
            }
        }

        public float getPseudoDistance(MORecord b) {
            return getpseudodistance(b, position.x, position.y, javadate.getTime(), depth);
        }

        public double getDistance(MORecord b) {
            double x = b.position.x - position.x;
            double y = b.position.y - position.y;
            return Math.hypot(x, y);
        }

        public double getDistance(int otherx, int othery) {
            double x = otherx - position.x;
            double y = othery - position.y;
            return Math.hypot(x, y);
        }

        public String getDifference(MORecord b) {
            if(b == null) {
                return "Difference with null\n";
            }
            long t = getTimeDifference(b);
            float pd = getPseudoDistance(b);
            double d = getDistance(b);
            return String.format("Time difference=%d Pseudodistance=%g Distance=%g\n", t, pd, d);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("buoy name = %s\n", buoy));
            sb.append(String.format("javatime = %d\n", javadate.getTime()));
            sb.append(String.format("depth    = %f\n", depth));
            sb.append(String.format("current  = %05.4f,%05.4f\n", current[0], current[1]));
            sb.append(String.format("position = %d,%d\n", position.x, position.y));
            return sb.toString();
        }
    }
}
