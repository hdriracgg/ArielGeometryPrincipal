/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/**
 *
 * @author jgrimsdale shot grid moves by following the centre of the receiver
 * patch the ReceiverModel must be populated first
 */
public class MovingShotModel extends ShotModel {

    private boolean debug = false;
    Map<Integer, XYLocation> sourcecentermap = new TreeMap<>();
    TreeMap<Integer, List<XYLocation>> receiverlocationmap;
    XYLocation firstaveragelocation;
    XYLocation lastaveragelocation;
    XYLocation bottomleft;
    XYLocation topright;
    int sourcedirection;
    int nbreceivercenters = 0;

    public MovingShotModel(GeometryModel agm, ReceiverModel arm) {
        super(agm, arm);
        bottomleft = new XYLocation(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        topright = new XYLocation(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    }

    @Override
    public void populate() {
        receiverlocationmap = arm.getreceivermap();
        setreceiverbounds();
        findsourcecenters();
        buildsourcemap();
        updateagmparameters();
    }

    void setreceiverbounds() {
    }

    void findsourcecenters() {
        int numberofnodes = agm.rnbn;
        LocationFilter filter = new LocationFilter(numberofnodes);
        for (Integer i : receiverlocationmap.keySet()) {
            List<XYLocation> locationlist = receiverlocationmap.get(i);
            XYLocation avelocation = filter.getLocation(locationlist);
            updatelimits(avelocation);
            nbreceivercenters++;
            sourcecentermap.put(i, avelocation);
        }
        // work out the shooting direction by looking at first and last positions
        List<XYLocation> firstlist = receiverlocationmap.get(receiverlocationmap.firstKey());
        List<XYLocation> lastlist = receiverlocationmap.get(receiverlocationmap.lastKey());
        firstaveragelocation = averagelocation(firstlist);
        if(lastlist == null) {
            System.out.println("lastlist == null receiverlocationmap.size() = "+receiverlocationmap.size());
        }
        lastaveragelocation = averagelocation(lastlist);
        if (lastaveragelocation.x > firstaveragelocation.x) {
            sourcedirection = 1;
        }
        else {
            sourcedirection = -1;
        }
        if (debug) {
            int n = 0;
            for (XYLocation xyl : sourcecentermap.values()) {
                System.out.printf("ArielMovingShotModel.findsourcecenters ");
                System.out.printf("X = %f Y = %f\n", xyl.x, xyl.y);
                n++;
            }
            System.out.printf("ArielMovingShotModel.findsourcecenters ");
            System.out.printf("sourcedirection = %d n = %d\n", sourcedirection, n);
        }
    }

    void updatelimits(XYLocation xyl) {
        if (xyl.x > topright.x) {
            topright.x = xyl.x;
        }
        if (xyl.y > topright.y) {
            topright.y = xyl.y;
        }
        if (xyl.x < bottomleft.x) {
            bottomleft.x = xyl.x;
        }
        if (xyl.y < bottomleft.y) {
            bottomleft.y = xyl.y;
        }
    }

    // shot patch dimensions use input parameters EXCEPT
    // that the crossline spacing is adjusted to match shot pattern to receivers
    void buildsourcemap() {
        int sboats = agm.sboats;
        int sspl = agm.sspl;
        float spwh = agm.spwh;
        float sils = agm.sils;
        float spht = agm.spht;
        int nbshots = sourcecentermap.size();        // number of shots is equal to number of receiver groups
        int nblines = nbshots / sspl;                // and we calculate the total number of shot lines
        int linesperboat = nblines / sboats;         // number of lines per boat
        nblines = linesperboat * sboats;             // nb lines is a multple of number of boats
        int nbgaps = nblines - 1;                    // number of shot line gaps
        float cscls = spwh / nbgaps;                 // and we calculate static crossline spacing
        nbshots = nblines * sspl;                    // shots stop at end of a line
        agm.ssls = nblines;                          // number of shot lines might have changed

        for (int sindex = 0; sindex < nbshots; sindex++) {
            int boat = sindex % sboats;        // boat number, starts at 0
            int spnb = sindex / sboats;        // sp for this boat, starts at 0
            int lnnb = spnb / sspl;            // line number for this boat, starts at 0
            int spln = spnb % sspl;            // sp in this line, starts at 0
            if (boat % 2 == 0) {
                spln = (lnnb % 2 != 0 ? sspl - spln - 1 : spln);   // even boat -> odd lines in reverse order
            }
            else {
                spln = (lnnb % 2 == 0 ? sspl - spln - 1 : spln);   // odd boat -> even lines in reverse order
            }
            float firstlineoffset = boat * linesperboat * cscls;   // offset of first line for this boat
            float osfc = firstlineoffset - (spwh / 2);               // shot patch offset from centre
            float xoffset = osfc + (lnnb * cscls);
            float yoffset = (spln * sils) - (spht / 2);
            XYLocation centerlocation = sourcecentermap.get(sindex);
            float x = centerlocation.x + (sourcedirection * xoffset);
            float y = centerlocation.y + yoffset;
            if (debug && (spln == 0)) {
                System.out.printf("ArielMovingShotModel.buildsourcemap:\n");
                System.out.printf(" agm.sspl=%d agm.spwh=%f nbshots=%d nblines=%d cscls=%f boat=%d\n", sspl, spwh, nbshots, nblines, cscls, boat);
                System.out.printf(" osfc=%f xoffset=%f yoffset=%f spln=%d spnb=%d lnnb=%d\n", osfc, xoffset, yoffset, spln, spnb, lnnb);
                System.out.printf(" x=%f y=%f xc=%f yc=%f\n", x, y, centerlocation.x, centerlocation.y);
            }
            addshotlocation(sindex, x, y);
        }
    }

    class LocationFilter {

        int length;
        Queue<XYLocation> accumulator;

        public LocationFilter(int length) {
            this.length = length;
            accumulator = new ArrayDeque<>();
        }

        public XYLocation getLocation(List<XYLocation> locationlist) {
            accumulator.addAll(locationlist);
            int excess = accumulator.size() - length;
            if (excess > 0) {
                for (int i = 0; i < excess; i++) {
                    accumulator.remove();
                }
            }
            return filter();
        }

        private XYLocation filter() {
            XYLocation[] xyl = new XYLocation[length];
            return averagelocation(Arrays.asList(accumulator.toArray(xyl)));
        }
    }

    XYLocation averagelocation(List<XYLocation> xylocations) {
        float x = 0;
        float y = 0;
        int s = 0;
        for (XYLocation xyl : xylocations) {
            if (xyl == null) {
                System.out.println("ArielMovingShotModel.averagelocation xyl == null");
                continue;
            }
            x += xyl.x;
            y += xyl.y;
            s++;
        }
        return new XYLocation(x / s, y / s);
    }
}