/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.io.DirectoryReader;
import com.cgg.arielgeometry.model.io.I_ReceiverScanner;
import com.cgg.arielgeometry.model.io.I_FileReader;
import com.cgg.arielgeometry.model.types.Receiver;
import com.cgg.arielgeometry.model.types.XYZLocation;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author jgrimsdale
 */
public class ReceiverReaderV3 {

    private static boolean debug = false;
    // Key is node name, values are list of TimeandLocation 
    private TreeMap<String, TreeSet<TimeandLocation>> timeandlocationmap = new TreeMap<>();
    // Key is shot time, values are list of XYLocation for all AUVs, after filtering/interpolation
    private Map<Integer, List<XYLocation>> receiverlocationmap = new TreeMap<>();
    // Set of the names of the nodes
    private Set<String> nodenames = new TreeSet<>();
    private AbstractGeometryModel agm;
    private DirectoryReader directoryReader;
    private I_ReceiverScanner receiverScanner;
    private int filesread = 0;
    private int locationsread = 0;
    private int averagelocations = 0;
    private int mintime = Integer.MAX_VALUE;
    private int maxtime = Integer.MIN_VALUE;

    public ReceiverReaderV3(AbstractGeometryModel agm) {
        this.agm = agm;
    }

    private class TimeandLocation {

        int time;
        XYLocation xylocation;

        private TimeandLocation(int time, XYLocation xylocation) {
            this.time = time;
            this.xylocation = xylocation;
        }
    }

    private class TimeandLocationComparator implements Comparator<TimeandLocation> {

        @Override
        public int compare(TimeandLocation o1, TimeandLocation o2) {
            if (o1.time < o2.time) {
                return -1;
            }
            if (o1.time > o2.time) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * Reads receivers from all files in a directory then interpolates in order
     * to have a single location for each node at each multiple of nominal shot
     * point time interval (spid).
     *
     * The files come from a marina trajectory simulation, so there are no shot
     * point numbers in the files
     *
     * @param scr An instance of the ReceiverScanner to be used
     */
    public void readreceiversfromsimulation(I_ReceiverScanner scr) {
        receiverScanner = scr;
        directoryReader = new DirectoryReader("Receiver Trajectory Directory");
        directoryReader.readFiles(new I_FileReader() {
            @Override
            public void readaFile(File file) {
                readFile(file);
            }
        });
        updatereceiverlocations();
        System.out.printf("ReceiverReader:readreceivers read %d files and %d locations %d averagelocations\n",
                filesread, locationsread, averagelocations);
        agm.rnbn = nodenames.size();
    }

    /**
     * Reads receivers from all files in a directory for a real acquisition.
     *
     * The files contain real trajectories, so each marina position is
     * associated with a real shot point number
     *
     * @param scr An instance of the ReceiverScanner to be used
     */
    public void readreceiversfromreal(I_ReceiverScanner scr) {
        // TBD
    }

    /**
     * Read a file. Can be called by a call back in DirectoryReader
     *
     *
     * @param file
     */
    public void readFile(File file) {
        System.out.printf("ReceiverReaderV3:readFile reading filename = %s\n", file.getName());
        receiverScanner.setFile(file);
        String name = receiverScanner.getrecname();
        nodenames.add(name);
        while (receiverScanner.hasNextReceiver()) {
            Receiver receiver = receiverScanner.getReceiver();
            XYZLocation xyzlocation = receiver.xyzlocation;
            if (debug) {
                System.out.println(name + " " + receiver.id + " " + xyzlocation.x + " " + xyzlocation.y);
            }
            updatetalmap(name, receiver.id, xyzlocation.x, xyzlocation.y);
            locationsread++;
        }
        filesread++;
    }

    /**
     * Called for every line in the file to populate the "timeandlocationmap".
     * There is a set of TimeandLocations for every node. If there is not
     * already a set for the given node, a new one is created in the map.
     *
     * @param name name of the node
     * @param t time read from file
     * @param x x position read from file
     * @param y y position read from file
     */
    private void updatetalmap(String name, long time, float x, float y) {
        if (time > maxtime) {
            maxtime = (int) time;
        }
        if (time < mintime) {
            mintime = (int) time;
        }
        TreeSet<TimeandLocation> talts = timeandlocationmap.get(name);
        if (talts == null) {
            talts = new TreeSet<>(new TimeandLocationComparator());
            timeandlocationmap.put(name, talts);
        }
        talts.add(new TimeandLocation((int) time, new XYLocation(x, y)));
    }

    /**
     * Called once, when the time and location map has been fully populated. It
     * reads the time and location map in node name order and ensures there is
     * exactly one location for each AUV at each shot id
     */
    private void updatereceiverlocations() {
        for (String name : timeandlocationmap.keySet()) {
            updateanode(timeandlocationmap.get(name));
        }
        // remove reference so it can be garbage collected
        timeandlocationmap = null;
    }

    /**
     * Called by updatereceiverlocations. For a given node it updates the global
     * receiverlocationmap, making sure there is exactly one location for each
     * AUV for each shot id
     *
     * @param tall Time and location Set
     */
    private void updateanode(TreeSet<TimeandLocation> tall) {

        // will just be used for comparisons
        TimeandLocation comparisonelement = new TimeandLocation(0, null);

        // global times
        int firsttime = spid2time(time2spid(mintime));
        int lasttime = spid2time(time2spid(maxtime));

        // times for this node
        int thisnodefirsttime = tall.first().time;
        int thisnodelasttime = tall.last().time;

        // locations for this node
        XYLocation firstxylocation = tall.first().xylocation;
        XYLocation lastxylocation = tall.last().xylocation;

        // extrapolate ends
        if (thisnodefirsttime > firsttime) {
            tall.add(new TimeandLocation(firsttime, firstxylocation));
        }
        if (thisnodelasttime < lasttime) {
            tall.add(new TimeandLocation(lasttime, lastxylocation));
        }

        // interpolate to get location at each spid
        for (int time = firsttime; time <= lasttime; time += Shot.shottimeinterval) {
            int spid = time2spid(time);
            int sptime = spid2time(spid);
            comparisonelement.time = sptime;

            // find the necessary elements each side of this time
            TimeandLocation lowertal = tall.floor(comparisonelement);
            TimeandLocation uppertal = tall.ceiling(comparisonelement);

            // get the receiverlocation map for this spid
            List<XYLocation> xyll = receiverlocationmap.get(spid);
            // if it doesn't exist yet, then create a new one and put it in the receiever location map
            if (xyll == null) {
                xyll = new ArrayList<>();
                receiverlocationmap.put(spid, xyll);
            }

            // calculate the location and add to the map
            XYLocation interpolatedlocation = calculatelocation(sptime, lowertal, uppertal);
            xyll.add(interpolatedlocation);
        }
    }

    /**
     * Interpolate between two locations before and after desired time
     * 
     * @param thistime desired time
     * @param lowertal time and location of sample immediately before
     * @param uppertal time and location of sample immediately after
     * @return 
     */
    private XYLocation calculatelocation(int thistime, TimeandLocation lowertal, TimeandLocation uppertal) {

        int lowertime = lowertal.time;
        int uppertime = uppertal.time;

        XYLocation result = interpolatelocation(
                lowertime,
                thistime,
                uppertime,
                lowertal.xylocation,
                uppertal.xylocation);

        return result;
    }

    public Map<Integer, List<XYLocation>> getreceiverlocationmap() {
        return receiverlocationmap;
    }

    /**
     * Interpolate between two locations for a desired time
     * 
     * @param a time before
     * @param b time desired
     * @param c time after
     * @param lower Location at time before
     * @param higher Location at time after
     * @return 
     */
    private XYLocation interpolatelocation(int a, int b, int c, XYLocation lower, XYLocation higher) {
        double ratio;
        if (a == c) {
            ratio = 0;
        }
        else {
            ratio = ((double) b - (double) a) / ((double) c - (double) a);
        }
        double x = lower.x + ratio * (higher.x - lower.x);
        double y = lower.y + ratio * (higher.y - lower.y);
        return new XYLocation((float) x, (float) y);
    }

    /**
     * Make spid a multiple of nominal shot interval. This means there may be
     * more than one location for a given AUV at a given spid.
     *
     * @param t unix time
     * @return the spid
     */
    private int time2spid(int t) {
        int spid = t - Shot.timezero;
        spid = spid / Shot.shottimeinterval;
        return spid;
    }

    /**
     * Convert a spid back to time
     * 
     * @param spid spid to be converted
     * @return unix time
     */
    private int spid2time(int spid) {
        int time = Shot.timezero + (spid * Shot.shottimeinterval);
        return time;
    }
}
