/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.List;

/**
 *
 * @author jgrimsdale shot grid moves by following the centre of the receiver
 * patch the ReceiverModel must be populated first
 */
public class OffsetReverseMovingShotModelTilted extends ReverseMovingShotModel {

    private boolean debug = false;
    private float rcvrpatchwidth;

    public OffsetReverseMovingShotModelTilted(GeometryModel agm, ReceiverModel arm) {
        super(agm, arm);
    }

    // shot patch dimensions use input parameters EXCEPT
    // that the crossline spacing is adjusted to match shot pattern to receivers
    // The shooting order is reversed
    @Override
    void buildsourcemap() {
        if (debug) {
            System.out.println("ArielOffsetReverseMovingShotModel.buildsourcemap");
        }
        sourcedirection = sourcedirection * (-1);      // Reverse the source direction      
        int sboats = agm.sboats;
        int sspl = agm.sspl;
        float spwh = agm.spwh;
        float sils = agm.sils;
        float spht = agm.spht;
        float nbswaths = agm.rs;
        int nbshots = sourcecentermap.size();        // number of shots is equal to number of receiver groups
        int nblines = nbshots / sspl;                // and we calculate the total number of shot lines
        int linesperboat = nblines / sboats;         // number of lines per boat
        nblines = linesperboat * sboats;             // nb lines is a multple of number of boats
        int nbgaps = nblines - 1;                    // number of shot line gaps
        float cscls = spwh / nbgaps;                 // and we calculate static crossline spacing
        float slinc = 2 * rcvrpatchwidth / (nbswaths * nbgaps);     // shot line increment that shifts shots in x direction for each pass
        nbshots = nblines * sspl;                    // shots stop at end of a line
        agm.ssls = nblines;                          // number of shot lines might have changed


        // for each swath
        for (int sourceswath = 0; sourceswath < nbswaths; sourceswath++) {
            int indexshift = sourceswath * nbshots;
            float swathshift = getswathshiftcm(slinc, sourceswath, nbswaths);
            // and for each shot in swath
            for (int sindex = 0; sindex < nbshots; sindex++) {
                int revindex = nbshots - sindex - 1; // Run through receiver centers in reverse order
                int boat = sindex % sboats;         // boat number, starts at 0
                int spnb = sindex / sboats;         // sp for this boat, starts at 0
                int lnnb = spnb / sspl;             // line number for this boat, starts at 0
                int spln = spnb % sspl;             // sp in this line, starts at 0
                if (boat % 2 == 0) {
                    spln = (lnnb % 2 != 0 ? sspl - spln - 1 : spln);   // even boat -> odd lines in reverse order
                }
                else {
                    spln = (lnnb % 2 == 0 ? sspl - spln - 1 : spln);   // odd boat -> even lines in reverse order
                }
                float firstlineoffset = boat * linesperboat * cscls;   // offset of first line for this boat
                float osfc = firstlineoffset - (spwh / 2);               // shot patch offset from centre
                float xoffset = osfc + (lnnb * cscls) + swathshift;
                float yoffset = (spln * sils) - (spht / 2);
                XYLocation centerlocation = sourcecentermap.get(revindex);  // use reverse index
                float x = centerlocation.x + (sourcedirection * xoffset);
                float y = centerlocation.y + yoffset;
                if (debug) {
                    System.out.printf("ArielOffsetReverseMovingShotModel.buildsourcemap:\n");
                    System.out.printf(" agm.sspl=%d agm.spwh=%f nbshots=%d nblines=%d cscls=%f boat=%d\n", sspl, spwh, nbshots, nblines, cscls, boat);
                    System.out.printf(" osfc=%f xoffset=%f yoffset=%f spln=%d spnb=%d lnnb=%d\n", osfc, xoffset, yoffset, spln, spnb, lnnb);
                    System.out.printf(" x=%f y=%f xc=%f yc=%f\n", x, y, centerlocation.x, centerlocation.y);
                }
                addshotlocation(sindex + indexshift, x, y);
            }
        }
        System.out.printf("ArielOffsetReverseMovingShotModel.buildsourcemap shotlocations = %d\n", shotlocations);
        System.out.printf("ArielOffsetReverseMovingShotModel.buildsourcemap nbshots = %d\n", nbshots);
    }

    // this function makes sure the shift for each swath is well distributed
    // interleaved version
    private float getswathshiftil(float slinc, int sourceswath, float nbswaths) {
        int middleswath = (int) nbswaths / 2;
        int a = sourceswath / 2;
        int b = sourceswath % 2;
        int sw;
        if (b == 0) {
            sw = middleswath + a;
        }
        else {
            sw = a;
        }
        return (slinc * sw) - (slinc * nbswaths / 2);
    }

    // this function makes sure the shift for each swath is well distributed
    // converge to middle version
    private float getswathshiftcm(float slinc, int sourceswath, float nbswaths) {
        int a = sourceswath / 2;
        int b = sourceswath % 2;
        int sw;
        if (b == 0) {
            sw = (int)nbswaths - (a - 1);
        }
        else {
            sw = a + 1;
        }
        return (slinc * sw) - (slinc * nbswaths / 2);
    }

    // find bounding rectangle of complete receiver patch
    // find coordinates of half the starting height and half the end height
    // here we are talking about the receiver swath height
    // do this by adding half the start swath height to bottom left
    // and subtracting half the end swath height from the top right
    // interpolate from start to end to fill the sourcecenter map
    @Override
    void findsourcecenters() {

        // First find out how many source centers we will need (nbshots)
        int nblines = agm.ssls;
        int linesperboat = nblines / agm.sboats;     // number of lines per boat
        nblines = linesperboat * agm.sboats;         // nb lines is a multple of number of boats
        int nbshots = nblines * agm.sspl;            // shots stop at end of a line
        agm.ssls = nblines;                          // number of shot lines might have changed

        // Determine bounding rectangle and end points
        List<XYLocation> boundingrectangle = arm.getboundingrectangle();
        XYLocation bl = boundingrectangle.get(0);  // bottom left
        XYLocation tr = boundingrectangle.get(1);  // top right
        float bottomy = bl.y + (agm.rstartswh / 2);   // bottom left + half the start receiver patch height
        float topy = tr.y - (agm.rendswh / 2);     // top right - half the end receiver patch height
        float leftx = bl.x;
        float rightx = tr.x;

        // update the sourcecentermap
        for (int i = 0; i < nbshots; i++) {
            sourcecentermap.put(i, xyinterpolate(i, nbshots, leftx, bottomy, rightx, topy));
        }

        // work out the shooting direction by looking at first and last positions
        List<XYLocation> firstlist = receiverlocationmap.get(0);
        List<XYLocation> lastlist = receiverlocationmap.get(receiverlocationmap.size() - 1);
        XYLocation firstlocation = averagelocation(firstlist);
        XYLocation lastlocation = averagelocation(lastlist);
        if (lastlocation.x > firstlocation.x) {
            sourcedirection = 1;
        }
        else {
            sourcedirection = -1;
        }

        // find total receiver patch width
        rcvrpatchwidth = sourcedirection * (lastlocation.x - firstlocation.x);

        if (debug) {
            int n = 0;
            for (XYLocation xyl : sourcecentermap.values()) {
                System.out.printf("ArielOffsetReverseMovingShotModel.findsourcecenters ");
                System.out.printf("X = %f Y = %f\n", xyl.x, xyl.y);
                n++;
            }
            System.out.printf("ArielOffsetReverseMovingShotModel.findsourcecenters ");
            System.out.printf("sourcedirection = %d n = %d\n", sourcedirection, n);
        }

        System.out.printf("ArielOffsetReverseMovingShotModel.findsourcecenters nbshots=%d\n", nbshots);
        System.out.printf("ArielOffsetReverseMovingShotModel.findsourcecenters sourcecentermap.size()=%d\n", sourcecentermap.size());

    }

    @Override
    public void calculateglobalvalues() {
        int nbshots = agm.ssls * agm.sspl * agm.rs;
        float timeinsecs = agm.spti * nbshots;
        agm.snbs = nbshots;
        agm.recordingtime = timeinsecs / (24.0f * 3600.0f);
        float boatspeedinmpersec = (nbshots * agm.sils) / timeinsecs;
        agm.boatspeed = boatspeedinmpersec;
    }

    private XYLocation xyinterpolate(int i, int nb, float x1, float y1, float x2, float y2) {
        float x;
        float y;
        x = x1 + ((float) i / (float) nb) * (x2 - x1);
        y = y1 + ((float) i / (float) nb) * (y2 - y1);
        return new XYLocation(x, y);
    }
}
