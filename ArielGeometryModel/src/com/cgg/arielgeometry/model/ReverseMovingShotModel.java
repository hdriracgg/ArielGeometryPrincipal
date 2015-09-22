/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.types.XYLocation;

/**
 *
 * @author jgrimsdale shot grid moves by following the centre of the receiver
 * patch the ReceiverModel must be populated first
 */
public class ReverseMovingShotModel extends MovingShotModel {

    private boolean debug = false;

    public ReverseMovingShotModel(GeometryModel agm, ReceiverModel arm) {
        super(agm, arm);
    }

    // shot patch dimensions use input parameters EXCEPT
    // that the crossline spacing is adjusted to match shot pattern to receivers
    // The shooting order is reversed
    @Override
    void buildsourcemap() {
        sourcedirection = sourcedirection*(-1);      // Reverse the source direction      
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
            int revindex = nbshots - sindex -1; // Run through receiver centers in reverse order
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
            float osfc = firstlineoffset - (spwh/2);               // shot patch offset from centre
            float xoffset = osfc + (lnnb * cscls);
            float yoffset = (spln * sils) - (spht / 2);
            XYLocation centerlocation = sourcecentermap.get(revindex);  // use reverse index
            float x = centerlocation.x + (sourcedirection * xoffset);
            float y = centerlocation.y + yoffset;
            if (debug) {
                System.out.printf("ArielReverseMovingShotModel.buildsourcemap:\n");
                System.out.printf(" agm.sspl=%d agm.spwh=%f nbshots=%d nblines=%d cscls=%f boat=%d\n", sspl, spwh, nbshots, nblines, cscls, boat);
                System.out.printf(" osfc=%f xoffset=%f yoffset=%f spln=%d spnb=%d lnnb=%d\n", osfc, xoffset, yoffset, spln, spnb, lnnb);
                System.out.printf(" x=%f y=%f xc=%f yc=%f\n", x, y, centerlocation.x, centerlocation.y);
            }
            addshotlocation(sindex, x, y);
        }
    }
}
