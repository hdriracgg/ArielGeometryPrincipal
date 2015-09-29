/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jgrimsdale
 */
public class BinModel implements I_BinModel {

    boolean debug = false;
    boolean empty = false;
    public GeometryModel agm;
    public ReceiverModel arm;
    public I_ShotModel asm;
    public int coverage[][];
    public int shortoffsetcoverage[][];

    public BinModel(GeometryModel agm, I_ShotModel asm, ReceiverModel arm) {
        this.asm = asm;
        this.arm = arm;
        this.agm = agm;
    }

    @Override
    public void populate() {
        if (!asm.isEmpty()) {
            updateagmparameters();
            fillabc();
            empty = false;
        }
    }

    private void fillabc() {
        coverage = new int[agm.binsx][agm.binsy];
        shortoffsetcoverage = new int[agm.binsx][agm.binsy];
        int shotswithoutreceivers = 0;
        for(Integer shot : asm.getshotnumberset()) {
            XYLocation shotlocation = asm.getshotlocation(shot);
            List<XYLocation> receiverlocationlist = arm.getreceiverlocationspershot(shot);
            if (receiverlocationlist == null) {
                shotswithoutreceivers++;
                continue;
            }
            for (Iterator<XYLocation> it = receiverlocationlist.iterator(); it.hasNext();) {
                XYLocation receiverlocation = it.next();
                int[] bin;
                float x = (shotlocation.x + receiverlocation.x) / 2;
                float y = (shotlocation.y + receiverlocation.y) / 2;
                bin = xy2bin(x, y);
                incrementcoverage(bin);
                if (binoffset(receiverlocation.x, receiverlocation.y, shotlocation.x, shotlocation.y) < agm.shortoffset) {
                    incrementshortoffsetcoverage(bin);
                }
            }
        }
        System.out.println("BinModel.fillabc: shotswithoutreceivers = " + shotswithoutreceivers);
    }

    void updateagmparameters() {
        float xrange = Math.max(agm.rtr.x, agm.str.x) - Math.min(agm.rbl.x, agm.sbl.x);
        float yrange = Math.max(agm.rtr.y, agm.str.y) - Math.min(agm.rbl.y, agm.sbl.y);
        agm.binsx = 1 + (int) (xrange / agm.bxs);
        agm.binsy = 1 + (int) (yrange / agm.bys);
        agm.binoriginx = Math.min(agm.rbl.x, agm.sbl.x);
        agm.binoriginy = Math.min(agm.rbl.y, agm.sbl.y);
    }

    void calculateglobalvalues() {
        if (empty) {
            return;
        }
        int binsnb = 0;   // number of non empty bins;
        int binhits = 0;  // running total of bin hits
        int bincmax = 0;  // current max coverage
        float covave;     // average coverage
        int covbins = 0;  // bins with higher than average coverage
        int covhits = 0;  // number of hits in bins with higher than average coverage
        int binsx = agm.binsx;
        int binsy = agm.binsy;

        float binarea = agm.bxs * agm.bys / (1000.0f * 1000.0f);  // area of a bin in km2

        for (int i = 0; i < binsx; i++) {
            for (int k = 0; k < binsy; k++) {
                int c = coverage[i][k];
                if (c != 0) {
                    binsnb++;
                    binhits += c;
                }
                if (c > bincmax) {
                    bincmax = c;
                }
            }
        }

        covave = binhits / binsnb;

        for (int i = 0; i < binsx; i++) {
            for (int k = 0; k < binsy; k++) {
                int c = coverage[i][k];
                if (c > covave) {
                    covbins++;
                    covhits += c;
                }
            }
        }

        agm.nbnonemptybins = binsnb;
        agm.covmax = bincmax;
        agm.covave = covave;
        agm.covarea = covbins * binarea;
        agm.tracedensity = (covhits / agm.covarea) / (1000.0f * 1000.0f);
        agm.totaltraces = binhits;
        System.out.println("ArielBinModel.calculateglobalvalues Number of traces recorded = " + agm.totaltraces);
        System.out.println("ArielBinModel.calculateglobalvalues Number of non empty bins = " + agm.nbnonemptybins);
        System.out.println("ArielBinModel.calculateglobalvalues Max coverage = " + agm.covmax);
        System.out.println("ArielBinModel.calculateglobalvalues Average coverage = " + agm.covave);
        System.out.println("ArielBinModel.calculateglobalvalues Coverage area in km2 = " + agm.covarea);
        System.out.println("ArielBinModel.calculateglobalvalues Trace density / m2 = " + agm.tracedensity);
    }

    @Override
    public XYLocation getblshotprojection(Integer shot, XYLocation binlocation) {
        float halfbinx = agm.bxs / 2;
        float halfbiny = agm.bys / 2;
        XYLocation shotlocation = asm.getshotlocation(shot);
        int[] bin = xy2bin(binlocation.x, binlocation.y);
        float[] bincenter = bin2xy(bin[0], bin[1]);
        float binblx = bincenter[0] - halfbinx;
        float binbly = bincenter[1] - halfbiny;
        float projblx = binblx + (binblx - shotlocation.x);
        float projbly = binbly + (binbly - shotlocation.y);
        return new XYLocation(projblx, projbly);
    }

    @Override
    public XYLocation gettrshotprojection(Integer shot, XYLocation binlocation) {
        float halfbinx = agm.bxs / 2;
        float halfbiny = agm.bys / 2;
        XYLocation shotlocation = asm.getshotlocation(shot);
        int[] bin = xy2bin(binlocation.x, binlocation.y);
        float[] bincenter = bin2xy(bin[0], bin[1]);
        float bintrx = bincenter[0] + halfbinx;
        float bintry = bincenter[1] + halfbiny;
        float projtrx = bintrx + (bintrx - shotlocation.x);
        float projtry = bintry + (bintry - shotlocation.y);
        return new XYLocation(projtrx, projtry);
    }

    private void incrementcoverage(int[] bin) {
        int bx = bin[0];
        int by = bin[1];
        try {
            coverage[bx][by]++;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("ArielBinModel.updatecoverage: Index into coverage out of bounds ");
            System.out.printf("bx=%d by=%d binsx=%d binsy=%d\n", bx, by, agm.binsx, agm.binsy);
        }
    }

    private void incrementshortoffsetcoverage(int[] bin) {
        int bx = bin[0];
        int by = bin[1];
        try {
            shortoffsetcoverage[bx][by]++;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("ArielBinModel.updatecoverage: Index into coverage out of bounds ");
            System.out.printf("bx=%d by=%d binsx=%d binsy=%d\n", bx, by, agm.binsx, agm.binsy);
        }
    }

    @Override
    public int[] xy2bin(float x, float y) {
        int bin[] = new int[2];
        bin[0] = (int) (x - agm.binoriginx) / (int) agm.bxs;
        bin[1] = (int) (y - agm.binoriginy) / (int) agm.bys;
        return bin;
    }

    @Override
    public float[] bin2xy(int x, int y) {
        float xy[] = new float[2];
        xy[0] = agm.binoriginx + ((x + 0.5f) * agm.bxs);
        xy[1] = agm.binoriginy + ((y + 0.5f) * agm.bys);
        return xy;
    }

    // find a list of x offset / y offset pairs that illuminate a bin
    // offset is negative if receiver ordinate is less than shotpoint ordinate
    @Override
    public List<XYLocation> getsources(int[] bin) {
        List<XYLocation> offsetlist = new ArrayList<>();
        return offsetlist;
    }

    // calculate offset from receiver and source numbers
    float binoffset(int rl, int r, int sl, int s) {
        return binoffset(
                arm.receiverx(rl, r, sl, s), arm.receivery(rl, r, sl, s),
                asm.sourcex(sl, s, rl, r), asm.sourcey(sl, s, rl, r));
    }

    // calculate offset from coordinates
    float binoffset(float rx, float ry, float sx, float sy) {
        return (float) Math.sqrt(Math.pow(sx - rx, 2) + Math.pow(sy - ry, 2));
    }

    // calculate azimuth from receiver and source numbers
    float binazimuth(int rl, int r, int sl, int s) {
        return binazimuth(
                arm.receiverx(rl, r, sl, s), arm.receivery(rl, r, sl, s),
                asm.sourcex(sl, s, rl, r), asm.sourcey(sl, s, rl, r));
    }

    // calculate azimuth from coordinates (0 to 2PI)
    float binazimuth(float rx, float ry, float sx, float sy) {
        float az = (float) Math.atan2((rx - sx), (ry - sy)) + (float) Math.PI;
        return az;
    }

    // calculate bin end coordinate relative to bin center, x always positive
    XYLocation binec(int rl, int r, int sl, int s) {
        return binec(
                arm.receiverx(rl, r, sl, s), arm.receivery(rl, r, sl, s),
                asm.sourcex(sl, s, rl, r), asm.sourcey(sl, s, rl, r));
    }

    XYLocation binec(float rx, float ry, float sx, float sy) {
        // coordinates of the receiver relative to bin centre
        float x = (rx - sx) / 2;
        float y = (ry - sy) / 2;
        if (Math.abs(x) > agm.maxxoffset) {
            agm.maxxoffset = Math.abs(x);
        }
        if (Math.abs(y) > agm.maxyoffset) {
            agm.maxyoffset = Math.abs(y);
        }
        XYLocation ec = new XYLocation(x, y);
        return ec;
    }

    @Override
    public int[][] getcoveragearray() {
        return coverage;
    }

    @Override
    public int getbincoverage(float x, float y) {
        int bin[] = xy2bin(x, y);
        int xbin = bin[0];
        int ybin = bin[1];
        return coverage[xbin][ybin];
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public int[][] getshortoffsetcoveragearray() {
        return shortoffsetcoverage;
    }
}
