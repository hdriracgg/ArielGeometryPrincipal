/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrents;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;

/**
 *
 * @author jgrimsdale
 */
public class TideModel {
    
    Map<Date, Float> heightMap;

    public TideModel() {
        heightMap = new HashMap<>();
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
        jfc.setDialogTitle("TideFile");
        jfc.showOpenDialog(jfc);
        File file = jfc.getSelectedFile();
        TideReader tr = new TideReader(file);
        int records = 0;
        int duplicates = 0;
        while (tr.hasNextRecord()) {
            records++;
            Date javadate = tr.javadate;
            if(heightMap.containsKey(javadate)) {
                duplicates++;
                System.out.printf("Duplicate: Date=%s %s h1=%f h2=%f\n", tr.date, tr.timeofday, heightMap.get(javadate), tr.h);
            }
            heightMap.put(tr.javadate, tr.h);
        }
        System.out.printf("%d records of which %d duplicates read from file %s\n", records, duplicates, file.getAbsolutePath());
    }
}
