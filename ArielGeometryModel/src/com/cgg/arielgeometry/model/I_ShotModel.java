/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.io.I_ShotScanner;
import com.cgg.arielgeometry.model.types.XYLocation;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jgrimsdale
 */
public interface I_ShotModel {

    void readfiles(I_ShotScanner scanner);
    public void updateshot(int sl, int s, int rl, int r);
    public void updateshots();
    public int getsize();
    public XYLocation getshotlocation(int shot);
    public float sourcex(int sl, int s, int rl, int r);
    public float sourcey(int sl, int s, int rl, int r);
    public void calculateglobalvalues();
    public Collection<XYLocation> getsourcecollection();
    public List<Integer> getsourcelist(XYLocation bottomleft, XYLocation topright);
    public Set<Integer> getshotnumberset();
    public boolean isEmpty();
    public void populate();
}
