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
public class ReverseUserDefinedShotModel extends MovingShotModel {

    private boolean debug = false;

    public ReverseUserDefinedShotModel(GeometryModel agm, ReceiverModel arm) {
        super(agm, arm);
    }

    // shot patch dimensions use input parameters including crossline spacing
    // The shooting order is reversed
    // Shooting is centered around the complete receiver location map
    @Override
    void buildsourcemap() {
        int rs = agm.rs;                             // number of Receiver swaths
        int sspl = agm.sspl;                         // Source Shots Per Line
        int ssls = agm.ssls;                         // Source Shot Lines
        float sils = agm.sils;                       // Source InLine Spacing
        float scls = agm.scls;                       // Source CrossLine Spacing
        XYLocation sourcebottomleft = new XYLocation(0, 0);
        XYLocation sourcetopright = new XYLocation(0, 0);

        float sourcepatchwidth = scls * ((rs * ssls) - 1);
        float sourcepatchheight = sils * (sspl - 1);

        float receiverpatchwidth = topright.x - bottomleft.x;
        float receiverpatchheight = topright.y - bottomleft.y;

        sourcebottomleft.x = bottomleft.x - ((sourcepatchwidth - receiverpatchwidth) / 2);
        sourcebottomleft.y = bottomleft.y - ((sourcepatchheight - receiverpatchheight) / 2);
        sourcetopright.x = topright.x + ((sourcepatchwidth - receiverpatchwidth) / 2);
        sourcetopright.y = topright.y + ((sourcepatchheight - receiverpatchheight) / 2);

        float crosslinespacing = (ssls+1) * scls;      // distance between 2 shot lines in the same swath
        float crosslineincrement = scls;           // distance between 2 shot lines in consecutive swaths

        int sindex = 0;

        for (int swath = 0; (swath < rs) && (sindex < nbreceivercenters); swath++) {
            float lineoffset;
            int sourceswath = getsourceswathnumber(swath);
            float swathoffset = sourceswath * crosslineincrement;
            for (int sourceline = 0; sourceline < ssls; sourceline++) {
                lineoffset = sourceline * crosslinespacing;
                for (int sp = 0; sp < sspl; sp++) {
                    float x = sourcetopright.x - (lineoffset + swathoffset);
                    float y = sourcebottomleft.y + (sp * sils);
                    addshotlocation(sindex++, x, y);
                }
            }
        }
    }

    int getsourceswathnumber(int receiverswathnumber) {
        return receiverswathnumber;
    }
}
