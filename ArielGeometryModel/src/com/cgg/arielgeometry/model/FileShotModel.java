/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.io.ScannerMOOS;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jgrimsdale contains receiver coordinates for each shot point these
 * are real world coordinates
 */
public class FileShotModel extends ShotModel implements I_ShotModel {

    public boolean debug = false;
    private ShotReader asr;
    TreeMap<Integer, XYLocation> shotlocationmap;

    public FileShotModel(GeometryModel agm, ReceiverModel arm) {
        super(agm, arm);
        asr = new ShotReader(agm);
    }

    @Override
    public void populate() {
        asr.readshots(new ScannerMOOS());
        updatesources();
        updateagmparameters();
    }

    void updatesources() {
        shotlocationmap = asr.getshotlocationmap();
        for(int i=0; i<shotlocationmap.size(); i++) {
            XYLocation xyl = shotlocationmap.get(i);
            addshotlocation(i, xyl.x, xyl.y);
        }
    }
}
