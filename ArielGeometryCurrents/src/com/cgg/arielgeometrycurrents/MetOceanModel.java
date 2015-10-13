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
import javax.swing.JFileChooser;

/**
 *
 * @author jgrimsdale
 */
public class MetOceanModel {

    //    Each buoy has a Map of date, record
    //   Map<buoyname, Map<javadate, MORrecord>>
    public Map<String, Map<Date, MORecord>> buoyMap;
    public long maxtime = Long.MIN_VALUE;
    public long mintime = Long.MAX_VALUE;
    public long timerange = 0;

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
                Map<Date, MORecord> m = new HashMap<>();
                m.put(javadate, record);
                buoyMap.put(buoyname, m);
            }
        }
        timerange = maxtime - mintime;
        System.out.printf("%d records of which %d duplicates read from file %s\n", records, duplicates, file.getAbsolutePath());
    }

    private void setminmax(Date d) {
        long t = d.getTime();
        if (t > maxtime) {
            maxtime = t;
        }
        if (t < mintime) {
            mintime = t;
        }
    }

    public class MORecord {

        public Date javadate;
        public float depth;
        public float[] current;
        public Point position;
    }
}
