/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrents;

import java.awt.Point;
import java.io.IOException;
import java.util.Random;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author jgrimsdale
 */
public class NetcdfReader {

    NetcdfFile ncFile = null;
    int coordval;       // coordinate flag 
    // 0 = tangent plane, 1 = unrotated spherical grid, 3 = rotated spherical grid.
    int nxval;          // number of grid points in x direction
    int nyval;          // number of grid points in y direction
    float gridxval;     // grid spacing in x direction
    float gridyval;     // grid spacing in y direction
    float rlatdval;     // Latitude of transformation centre
    float rlngdval;     // Longitude of transformation centre
    float delxval;      // Transformation grid offset in X
    float delyval;      // Transformation grid offset in Y
    float thetadval;    // Rotation angle
    float dxtval;       // Grid spacing in X (metres)
    float dytval;       // Grid spacing in Y (metres)
    int[] deptharray;   // output levels (outlev index in vtot)
    float[] timearray;  // times = (time index in vtot)
    Variable vtot;      // contains x and y velocity vectors as a function of position, time and depth
    Variable vgrid3;    // contains grid node locations
    double cm2deg = 180.0 / (Math.PI * 637131500.0);
    double deg2cm = (Math.PI * 637131500.0) / 180.0;
    double m2deg = 180.0 / (Math.PI * 6371315.0);
    double deg2m = (Math.PI * 6371315.0) / 180.0;
    int previoustimeindex = -1; // Time index of previous lookup
    float[][][][] previous4darray;
    Point surveyB;
    Point surveyR;
    Point surveyT;
    Point surveyL;

    public NetcdfReader() throws IOException, InvalidRangeException {
        surveyB = new Point(132165, 3069553);
        surveyR = new Point(137962, 3075628);
        surveyT = new Point(132186, 3081153);
        surveyL = new Point(126384, 3075084);
//        JFileChooser jfc = new JFileChooser();
//        jfc.showOpenDialog(jfc);
//        String filename = jfc.getSelectedFile().getAbsolutePath();
        try {
            ncFile = NetcdfFile.open("C:\\Users\\jgrimsdale\\Documents\\Ariel\\Currents\\MIT\\MIT_redsea_uvw_20min_geo_201412010000_201412011200.nc");
        }
        catch (IOException ex) {
            System.out.println("Can't open netCDF file: ");
        }
        System.out.println("ncFile " + ncFile);
        vgrid3 = ncFile.findVariable("vgrid3");
        vtot = ncFile.findVariable("vtot");
        Variable dxt = ncFile.findVariable("dxt");
        Variable dyt = ncFile.findVariable("dyt");
        Variable rlngd = ncFile.findVariable("rlngd");
        Variable rlatd = ncFile.findVariable("rlatd");
        Variable delx = ncFile.findVariable("delx");
        Variable dely = ncFile.findVariable("dely");
        Variable thetad = ncFile.findVariable("thetad");
        Variable outlev = ncFile.findVariable("outlev");
        Variable time = ncFile.findVariable("time");
        Variable coord = ncFile.findVariable("coord");


        System.out.println("vgrid3 = " + vgrid3);
        System.out.println("vtot = " + vtot);

        rlatdval = rlatd.readScalarFloat();
        rlngdval = rlngd.readScalarFloat();
        delxval = delx.readScalarFloat() / 100.0f;
        delyval = dely.readScalarFloat() / 100.0f;
        thetadval = thetad.readScalarFloat();
        coordval = coord.readScalarInt();
        System.out.println("rlatdval=" + rlatdval + " rlngdval=" + rlngdval + " delxval = " + delxval + " delyval=" + delyval + " thetadval=" + thetadval);

        Array dxtarray = dxt.read();
        float[] dxt1d = (float[]) dxtarray.copyTo1DJavaArray();
        gridxval = (float) (dxt1d[0] / 100.0f);
        Array dytarray = dyt.read();
        float[] dyt1d = (float[]) dytarray.copyTo1DJavaArray();
        gridyval = (float) (dyt1d[0] / 100.0f);
        System.out.println("gridxval=" + gridxval + " gridyval=" + gridyval);

        // outlev
        deptharray = get1dint(outlev);
        for (int i = 0; i < deptharray.length; i++) {
            System.out.println("depth " + i + " =" + deptharray[i]);
        }

        // time
        timearray = get1dfloat(time);
        for (int i = 0; i < timearray.length; i++) {
            System.out.println("time " + i + " =" + timearray[i]);
        }

        // store vtot dimension of lon and lat
        int[] vtotshape = vtot.getShape();
        nxval = vtotshape[1];
        nyval = vtotshape[2];

        testll2xy();
    }

    void dowork() throws IOException, InvalidRangeException {

        testll2xy();

//        System.out.println("SurveyT=" + x2lon(surveyT.x) + "," + y2lat(surveyT.y));
//        System.out.println("SurveyB=" + x2lon(surveyB.x) + "," + y2lat(surveyB.y));
//        System.out.println("SurveyL=" + x2lon(surveyL.x) + "," + y2lat(surveyL.y));
//        System.out.println("SurveyR=" + x2lon(surveyR.x) + "," + y2lat(surveyR.y));


        // get some results
//        float x0 = (float) (35.5f * deg2cm);
//        float y0 = (float) (27.4f * deg2cm);
//        float[] vxy;
//        for (int i = 0; i < 10; i++) {
//            float xm = x0 / 100.0f + i * 10.0f;
//            float ym = y0 / 100.0f + i * 10.0f;
//            float xdeg = (float) (xm * 100.0f * cm2deg);
//            float ydeg = (float) (ym * 100.0f * cm2deg);
//            vxy = getvv(xdeg, ydeg, 5000.0f, 50.0f);
//            System.out.println("xdeg=" + xdeg + " ydeg=" + ydeg + "xm=" + xm + " ym=" + ym + " vxy=" + vxy[0] + "," + vxy[1]);
//        }

        // test performance
        int nbreads = 100000;
        float lons[] = new float[nbreads];
        float lats[] = new float[nbreads];
        float times[] = new float[nbreads];
        float depths[] = new float[nbreads];
        float result[];
        Random r = new Random();
        for (int i = 0; i < nbreads; i++) {
            lats[i] = 27.359522f + (float) (r.nextFloat() * (27.39573f - 27.359522f));
            lons[i] = 35.43578f + (float) (r.nextFloat() * (35.476562f - 35.43578f));
            times[i] = 42000.0f * 0.5f;
            depths[i] = 140.0f * r.nextFloat();
        }
        long tstart = System.currentTimeMillis();
        System.out.println("start reading " + System.currentTimeMillis());
        for (int i = 0; i < nbreads; i++) {
            result = getvv(lons[i], lats[i], times[i], depths[i]);
        }
        long tend = System.currentTimeMillis();
        System.out.println("stop reading " + System.currentTimeMillis());
        float mspc = (float) (tend - tstart) / (float) nbreads;
        System.out.println(nbreads + " reads, ms per call = " + mspc);
    }

    // get a velocity vector from lon, lat, time, depth
    float[] getvv(float lon, float lat, float time, float depth) throws IOException, InvalidRangeException {
        float[] result = new float[2];
        // get indexes
        int xy[] = ll2xy(lon, lat);
        int x = xy[0];
        int y = xy[1];
        int t = gettimeindex(time);
        int d = getdepthindex(depth);
        int origin[] = new int[5];
        origin[0] = t;
        origin[1] = x;
        origin[2] = y;
        origin[3] = d;
        origin[4] = 0;
        int size[] = new int[]{1, 1, 1, 1, 2};
        Array data5d = vtot.read(origin, size);
        Array data1d = data5d.reduce();
        result[0] = data1d.getFloat(0);
        result[1] = data1d.getFloat(1);
        return result;
    }

    // get a velocity vector from lon, lat, time, depth
    float[] getcachedvv(float lon, float lat, float time, float depth) throws IOException, InvalidRangeException {
        float[] result = new float[]{0.0f, 0.0f};
        float[][][][] varray;
        // get indexes
        int xy[] = ll2xy(lon, lat);
        int x = xy[0];
        int y = xy[1];
        int t = gettimeindex(time);
        int d = getdepthindex(depth);
        if (t != previoustimeindex) {
            varray = getvelocityarrayfortimeindex(t);
        }
        else {
            varray = previous4darray;
        }
        result[0] = varray[x][y][d][0];
        result[1] = varray[x][y][d][1];
        previous4darray = varray;
        previoustimeindex = t;
        return result;
    }

    // get a 4d java array of velocities for a given time index
    float[][][][] getvelocityarrayfortimeindex(int t) throws IOException, InvalidRangeException {
        int[] shape = vtot.getShape();
        int[] origin = new int[]{t, 0, 0, 0, 0};
        int[] size = new int[]{1, shape[1], shape[2], shape[3], shape[4]};
        Array data5d = vtot.read(origin, size);
        ArrayFloat.D4 data4d = (ArrayFloat.D4) data5d.reduce();
        float[][][][] v = (float[][][][]) data4d.copyToNDJavaArray();
        return v;
    }

    // get depth index from a depth in metres
    int getdepthindex(float depth) {
        int d = (int) (depth * -1.0f);
        for (int i = 0; i < deptharray.length; i++) {
            if (d < deptharray[i]);
            return i;
        }
        System.out.println("Out of depth range " + depth);
        return -1;
    }

    // get time index from time
    int gettimeindex(int time) {
        int t = (int) time;
        for (int i = 0; i < timearray.length; i++) {
            if (t > timearray[i]);
            return i;
        }
        return -1;
    }

    int gettimeindex(float time) {
        return gettimeindex((int) time);
    }
    // convert lon, lat to x and y indices
    double deg2rad = Math.PI / 180.0;

    int[] ll2xy(float lon, float lat) {
        int coord = coordval;
        int nx = nxval;
        int ny = nyval;
        float gridx = gridxval;
        float gridy = gridyval;
        float rlngd = rlngdval;
        float rlatd = rlatdval;
        float delx = delxval;
        float dely = delyval;
        float thetad = thetadval;
        int[] result = new int[]{0, 0};
        double xc = ((float) nx + 1.0f) / 2.0f - (delx / gridx);
        double yc = ((float) ny + 1.0f) / 2.0f - (dely / gridy);

        if (coord != 0) {
            System.out.println("Error: Coordinate system of other type than 0 are not supported");
            return result;
        }

        double re_d2r = 6371315.0 * deg2rad;
        double f1x = re_d2r / gridx;
        double f1y = re_d2r / gridy;
        double f2x = f1x * Math.cos(rlatd * deg2rad);
        double f2y = f1y * Math.cos(rlatd * deg2rad);
        double sterm = Math.sin(thetad * deg2rad);
        double cterm = Math.cos(thetad * deg2rad);
        double dlat = lat - rlatd;
        double dlon = lon - rlngd;
        int x = (int) (xc + f1x * dlat * sterm + f2x * dlon * cterm);
        int y = (int) (yc + f1y * dlat * cterm - f2y * dlon * sterm);
        x = x - 1;
        y = y - 1;
        if (x < 0) {
            System.out.println("ll2xy x out of range - return 0 x = " + x);
            x = 0;
        }
        if (x >= nx) {
            System.out.println("ll2xy x out of range - return nx-1 x=" + x + " nx=" + nx);
            x = nx - 1;
        }
        if (y < 0) {
            System.out.println("ll2xy y out of range - return 0 y = " + y);
            y = 0;
        }
        if (y >= ny) {
            System.out.println("ll2xy y out of range - return ny-1 y=" + y + " ny=" + ny);
            y = ny - 1;
        }
        result[0] = x;
        result[1] = y;
        return result;
    }

    // test ll2xy
    void testll2xy() throws IOException, InvalidRangeException {
        // vgrid3
        float[][][][] array4d = get4dfloat(vgrid3);
//        int[] shape = vgrid3.getShape();
//        for (int i = 0; i < shape[0]; i++) {
//            for (int j = 0; j < shape[1]; j++) {
//                System.out.println("i=" + i + " j=" + j + " value=" + " array4d=" + array4d[i][j][0][0]);
//            }
//        }
        System.out.println("[0][0]=" + array4d[0][0][0][0] + "," + array4d[0][0][0][1]);
        System.out.println("[0][625]=" + array4d[0][625][0][0] + "," + array4d[0][625][0][1]);
        System.out.println("[625][0]=" + array4d[625][0][0][0] + "," + array4d[625][0][0][1]);
        System.out.println("[625][625]=" + array4d[625][625][0][0] + "," + array4d[625][625][0][1]);
        int[] l0l0 = ll2xy((float) 35.43578, (float) 27.310062);
        int[] l625l0 = ll2xy((float) 35.38007, (float) 27.39573);
        int[] l0l625 = ll2xy((float) 35.532272, (float) 27.359522);
        int[] l625l625 = ll2xy((float) 35.476562, (float) 27.445189);
        System.out.println("l0l0 =" + l0l0[0] + "," + l0l0[1]);
        System.out.println("l625l0 =" + l625l0[0] + "," + l625l0[1]);
        System.out.println("l0l625 =" + l0l625[0] + "," + l0l625[1]);
        System.out.println("l625l625 =" + l625l625[0] + "," + l625l625[1]);
    }

    final int[] get1dint(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0};
        int[] shape = v.getShape();
        ArrayInt.D1 array1d = (ArrayInt.D1) v.read(origin, shape);
        int[] javaarray = (int[]) array1d.copyToNDJavaArray();
        return javaarray;
    }

    final float[] get1dfloat(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0};
        int[] shape = v.getShape();
        ArrayFloat.D1 array1d = (ArrayFloat.D1) v.read(origin, shape);
        float[] javaarray = (float[]) array1d.copyToNDJavaArray();
        return javaarray;
    }

    float[][] get2dfloat(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0, 0};
        int[] shape = v.getShape();
        ArrayFloat.D2 array2d = (ArrayFloat.D2) v.read(origin, shape);
        float[][] javaarray = (float[][]) array2d.copyToNDJavaArray();
        return javaarray;
    }

    float[][][][] get4dfloat(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0, 0, 0, 0};
        int[] shape = v.getShape();
        ArrayFloat.D4 array4d = (ArrayFloat.D4) v.read(origin, shape);
        float[][][][] javaarray = (float[][][][]) array4d.copyToNDJavaArray();
        return javaarray;
    }

    float[][][][][] get5dfloat(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0, 0, 0, 0, 0};
        int[] shape = v.getShape();
        ArrayFloat.D5 array5d = (ArrayFloat.D5) v.read(origin, shape);
        float[][][][][] javaarray = (float[][][][][]) array5d.copyToNDJavaArray();
        return javaarray;
    }
}