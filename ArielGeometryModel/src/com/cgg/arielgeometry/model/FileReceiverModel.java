/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.io.I_ReceiverScanner;
import com.cgg.arielgeometry.model.io.ScannerMOOS;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jgrimsdale contains receiver coordinates for each shot point these
 * are real world coordinates
 */
public class FileReceiverModel extends ReceiverModel implements I_ReceiverModel {

    public boolean debug = false;
    private ReceiverReaderV3 arr;

    public FileReceiverModel(GeometryModel agm) {
        super(agm);
        arr = new ReceiverReaderV3(agm);
    }

    @Override
    void populate() {
        agm.updateregularparameters();
        readfiles(new ScannerMOOS());
        updatereceivers();
        updateagmparameters();
    }

    @Override
    public void updatereceivers() {
        Map<Integer, List<XYLocation>> receiverlocationmap = arr.getreceiverlocationmap();
        for (Integer shot : receiverlocationmap.keySet()) {
            List<XYLocation> xylist = receiverlocationmap.get(shot);
            for (XYLocation xyl : xylist) {
                addreceiverlocation(shot, xyl.x, xyl.y);
            }
        }
    }

    public void readfiles(I_ReceiverScanner scanner) {
        ReceiverReaderV3 rr = new ReceiverReaderV3(agm);
        rr.readreceiversfromsimulation(scanner);
        Map<Integer, List<XYLocation>> receiverlocationmap = rr.getreceiverlocationmap();
        for (Integer shot : receiverlocationmap.keySet()) {
            List<XYLocation> xylist = receiverlocationmap.get(shot);
            for (XYLocation xyl : xylist) {
                addreceiverlocation(shot, xyl.x, xyl.y);
                locationsread++;
            }
        }
        System.out.printf("ReceiverModel:readfiles locationsread = %d\n", locationsread);
    }
}
