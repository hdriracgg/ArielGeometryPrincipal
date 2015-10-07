/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrents;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JFileChooser;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.transform.TransformUtils;

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
            if (heightMap.containsKey(javadate)) {
                duplicates++;
                System.out.printf("Duplicate: Date=%s %s h1=%f h2=%f\n", tr.date, tr.timeofday, heightMap.get(javadate), tr.h);
            }
            heightMap.put(tr.javadate, tr.h);
        }
        System.out.printf("%d records of which %d duplicates read from file %s\n", records, duplicates, file.getAbsolutePath());
        fft();
    }

    void fft() {
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        int size = heightMap.size();
        int len = nextpowerof2(size);
        double[] values = new double[len];
        int i = 0;
        int indexofmax = 0;
        double max = 0;
        for (Date d : heightMap.keySet()) {
            values[i++] = heightMap.get(d);
        }
        while (i < len) {
            values[i++] = 0.0d;
        }
        System.out.println("before fft");
        Complex[] result = fft.transform(values, TransformType.FORWARD);
        System.out.println("afterfft");
        for (int j = 0; j < result.length; j++) {
            double v = result[j].abs();
            if (v > max) {
                max = v;
                indexofmax = j;
            }
//            System.out.printf("result %d %s\n", j, result[j].abs());
        }
        System.out.printf("max = %f index = %d", max, indexofmax);
    }

    int nextpowerof2(int i) {
        double logb2 = Math.log(i) / Math.log(2);
        int log2int = (int) Math.ceil(logb2);
        return (int) Math.pow(2.0d, log2int);
    }
}
