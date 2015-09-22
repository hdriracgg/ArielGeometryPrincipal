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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author jgrimsdale
 */
public class ReceiverReader {

    private static boolean debug = false;
    private float spti;
    // Key is shot time, values are list of NameandLocation 
    private Map<Integer, List<NameandLocation>> nameandlocationmap = new TreeMap<>();
    // Key is shot time, values are list of XYLocation for all AUVs, after filtering/interpolation
    private Map<Integer, List<XYLocation>> receiverlocationmap = new TreeMap<>();
    private Set<String> nodenames = new TreeSet<>();
    private AbstractGeometryModel agm;
    private DirectoryReader directoryReader;
    private I_ReceiverScanner receiverScanner;
    private int filesread = 0;
    private int locationsread = 0;
    private int averagelocations = 0;

    public ReceiverReader(AbstractGeometryModel agm) {
        this.agm = agm;
    }

    private class NameandLocation {

        String name;
        XYLocation xylocation;

        private NameandLocation(String name, XYLocation xylocation) {
            this.name = name;
            this.xylocation = xylocation;
        }
    }

    /**
     * Reads receivers from all files in a directory averages or interpolates
     * (TBD) in order to have a single location for each node at each multiple
     * of nominal shot point time interval
     *
     * @param scr An instance of the ReceiverScanner to be used
     */
    public void readreceivers(I_ReceiverScanner scr) {
        receiverScanner = scr;
        directoryReader = new DirectoryReader("Receiver Trajectory Directory");
        spti = agm.spti;
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

    public void readFile(File file) {
        System.out.printf("ReceiverReader:readFile reading filename = %s\n", file.getName());
        receiverScanner.setFile(file);
        String name = receiverScanner.getrecname();
        nodenames.add(name);
        while (receiverScanner.hasNextReceiver()) {
            Receiver receiver = receiverScanner.getReceiver();
            XYZLocation xyzlocation = receiver.xyzlocation;
            if (debug) {
                System.out.println(name + " " + receiver.id + " " + xyzlocation.x + " " + xyzlocation.y);
            }
            updatemap(name, receiver.id, xyzlocation.x, xyzlocation.y);
            locationsread++;
        }
        filesread++;
    }

    /**
     * Called for every line in the file to populate nameandlocationmap there is
     * a list of namandlocations for every time interval (spid) if there is not
     * already a list for the given spid, a new one is created in the map
     *
     * @param name name of the node
     * @param t time read from file
     * @param x x position read from file
     * @param y y position read from file
     */
    private void updatemap(String name, long t, float x, float y) {
        int spid = (int) (t - Shot.timezero);
        // make spid a multiple of nominal shot interval. This means there may be
        // more than one location for a given AUV at a given spid
        spid = Shot.shottimeinterval * (spid / Shot.shottimeinterval);
        List<NameandLocation> l = nameandlocationmap.get(spid);
        if (l == null) {
            l = new ArrayList<>();
            nameandlocationmap.put(spid, l);
        }
        l.add(new NameandLocation(name, new XYLocation(x, y)));
    }

    /**
     * Called once, when the name and location map has been fully populated. It
     * reads the name and location map in shot id order and ensures there is
     * exactly one location for each AUV at each shot id
     */
    private void updatereceiverlocations() {
        for (Integer shotid : nameandlocationmap.keySet()) {
            updateareceiverlocation(shotid, nameandlocationmap.get(shotid));
        }
        // remove reference so it can be garbage collected
        nameandlocationmap = null;
    }

    /**
     * Called by updatereceiverlocations. For a given shotpoint it updates the
     * global receiverlocationmap, making sure there is exactly one location for
     * each AUV at the given shot id
     *
     * @param spid shot point id
     * @param nallist Name and location list
     */
    private void updateareceiverlocation(int spid, List<NameandLocation> nallist) {

        // Each node can have a list of locations corresponding to this time interval
        // the key in rmap is the AUV name
        Map<String, List<XYLocation>> rmap = new TreeMap<>();

        // fill rmap with name vs location list
        for (NameandLocation nal : nallist) {
            List<XYLocation> xylocations = rmap.get(nal.name);
            if (xylocations == null) {
                xylocations = new ArrayList<>();
                rmap.put(nal.name, xylocations);
            }
            xylocations.add(nal.xylocation);
        }

        System.out.printf("spid = %d", spid);

        // for this spid, store a unique location for each node name
        for (String name : rmap.keySet()) {
            // find the average location for this shot id and this AUV
            XYLocation xylocation = averagelocation(rmap.get(name));
            // get the receiverlocation map for this shot id
            List<XYLocation> rl = receiverlocationmap.get(spid);
            // if it doesn't exist yet, then create a new one and put it in the receiever location map
            if (rl == null) {
                rl = new ArrayList<>();
                receiverlocationmap.put(spid, rl);
            }
            rl.add(xylocation);
            System.out.printf(" name = %s x = %f y = %f", name, xylocation.x, xylocation.y);
        }

        System.out.println("");
        averagelocations++;
    }

    public Map<Integer, List<XYLocation>> getreceiverlocationmap() {
        return receiverlocationmap;
    }

    private class LocationFilter {

        int length;
        Queue<XYLocation> accumulator = new ArrayDeque<>();

        public LocationFilter(int length) {
            this.length = length;
        }

        public XYLocation getLocation(List<XYLocation> locationlist) {
            accumulator.addAll(locationlist);
            if (accumulator.size() < length) {
                return filter();
            }
            for (int i = 0; i < (accumulator.size() - length); i++) {
                accumulator.remove();
            }
            return filter();
        }

        private XYLocation filter() {
            XYLocation[] xyl = new XYLocation[1];
            return averagelocation(Arrays.asList(accumulator.toArray(xyl)));
        }
    }

    private XYLocation averagelocation(List<XYLocation> xylocations) {
        float x = 0;
        float y = 0;
        int s = xylocations.size();
        for (int i = 0; i < s; i++) {
            x += xylocations.get(i).x;
            y += xylocations.get(i).y;
        }
        return new XYLocation(x / s, y / s);
    }

    private float xydistance(XYLocation a, XYLocation b) {
        double xsqrd = Math.pow(a.x - b.x, 2);
        double ysqrd = Math.pow(a.y - b.y, 2);
        return (float) Math.pow(xsqrd + ysqrd, 0.5);
    }

    private float midxy(float a, float b) {
        return a + ((b - a) / 2);
    }
}
