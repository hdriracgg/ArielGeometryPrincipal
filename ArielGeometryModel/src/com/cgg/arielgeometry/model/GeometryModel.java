/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.types.EndCoordinateList;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jgrimsdale
 */
public class GeometryModel extends AbstractGeometryModel {

    public int dbinsx = 0;
    public int dbinsy = 0;
    public int dnbnonemptybins = 0;
    public int dmaxcov = 0;
    public float davecov = 0;
    public float dcovarea = 0;
    public float dboatspeed = 0;
    public float drecordingtime = 0;
    boolean debug = false;

    public GeometryModel(String armClassName, String asmClassName) {
        super();
        try {
            arm = (ReceiverModel) Class
                    .forName("com.cgg.arielgeometry.model." + armClassName)
                    .getConstructor(GeometryModel.class)
                    .newInstance(this);
            asm = (I_ShotModel) Class
                    .forName("com.cgg.arielgeometry.model." + asmClassName)
                    .getConstructor(GeometryModel.class, ReceiverModel.class)
                    .newInstance(this, arm);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        abm = new BinModel(this, asm, arm);
    }

    public void updateregularparameters() {
        rnbn = rpl * rls;
        rpwh = rils * (rpl - 1);
        rpht = rcls * (rls - 1);
        snbs = sspl * ssls;
        spwh = scls * (ssls - 1);
        spht = sils * (sspl - 1);
        rnbpos = rnbn;
    }

    @Override
    public void calculate() {
        arm.populate();
        asm.populate();
        abm.populate();
        calculateglobalvalues();
    }

    public void calculateglobalvalues() {
        arm.calculateglobalvalues();
        asm.calculateglobalvalues();
        abm.calculateglobalvalues();
    }

    @Override
    public void printbindetails() {
    }

    @Override
    public List<XYLocation> getsources() {
        List<XYLocation> xylist = new ArrayList<>(asm.getsourcecollection());
        return xylist;
    }

    @Override
    public List<List<XYLocation>> getreceivers() {
        List<List<XYLocation>> xylist = new ArrayList<>(arm.getreceivercollection());
        return xylist;
    }

    @Override
    public int[][] getbincoverage() {
        return abm.getcoveragearray();
    }
    
    @Override
    public int getbincoverage(float x, float y) {
        return abm.getbincoverage(x, y);
    }
    
     @Override
    public int[][] getshortoffsetcoverage() {
         return abm.getshortoffsetcoveragearray();
    }
    
    public I_ShotModel getasm() {
        return asm;
    }
    
    public ReceiverModel getarm() {
        return arm;
    }

    // get the shots and receivers that cover a given bin
    @Override
    public Map<Integer, List<XYLocation>> getshotsreceivers(XYLocation binlocation) {
        Map<Integer, List<XYLocation>> result = new TreeMap<>();
        float halfoffset = maxoffset / 2;
        XYLocation shotbottomleft = new XYLocation(binlocation.x - halfoffset, binlocation.y - halfoffset);
        XYLocation shottopright = new XYLocation(binlocation.x + halfoffset, binlocation.y + halfoffset);
        List<Integer> sourcelocations = asm.getsourcelist(shotbottomleft, shottopright);
        for (Integer shot : sourcelocations) {
            if (debug) {
                System.out.printf("ArielGeometryModel.getshotsreceivers: source number = %d\n", shot);
            }
            XYLocation recvrbottomleft = abm.getblshotprojection(shot, binlocation);
            XYLocation recvrtopright = abm.gettrshotprojection(shot, binlocation);
            result.put(shot, arm.getreceiverlist(shot, recvrbottomleft, recvrtopright));
        }
        return result;
    }

    @Override
    public EndCoordinateList getxyoffsets(XYLocation binlocation) {
        Map<Integer, List<XYLocation>> shotsreceivers = getshotsreceivers(binlocation);
        List<XYLocation> xylist = new ArrayList<>();
        for (Integer shot : shotsreceivers.keySet()) {
            XYLocation shotlocation = asm.getshotlocation(shot);
            List<XYLocation> receivers = shotsreceivers.get(shot);
            for (XYLocation rcvrlocation : receivers) {
                xylist.add(new XYLocation(rcvrlocation.x - shotlocation.x, rcvrlocation.y - shotlocation.y));
            }
        }
        return new EndCoordinateList(xylist);
    }

   
}
