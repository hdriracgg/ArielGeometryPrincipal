/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrents;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author jgrimsdale
 */
public class KDTNearest {

    public static void main(String args[]) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader("input.txt"));
        int numpoints = 5;

        KDTree kdt = new KDTree(numpoints);
        double x[] = new double[2];

        x[0] = 2.1;
        x[1] = 4.3;
        kdt.add(x);

        x[0] = 3.3;
        x[1] = 1.5;
        kdt.add(x);

        x[0] = 4.7;
        x[1] = 11.1;
        kdt.add(x);

        x[0] = 5.0;
        x[1] = 12.3;
        kdt.add(x);

        x[0] = 5.1;
        x[1] = 1.2;
        kdt.add(x);

        System.out
                .println("Enter the co-ordinates of the point: (one after the other)");
        InputStreamReader reader = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(reader);
        double sx = Double.parseDouble(br.readLine());
        double sy = Double.parseDouble(br.readLine());

        double s[] = {sx, sy};
        KDNode kdn = kdt.find_nearest(s);
        System.out.println("The nearest neighbor is: ");
        System.out.println("(" + kdn.x[0] + " , " + kdn.x[1] + ")");
        in.close();
    }
}
