/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.io;

import com.cgg.arielgeometry.model.types.Receiver;
import com.cgg.arielgeometry.model.types.Shot;
import java.io.File;

/**
 *
 * @author jgrimsdale
 */
public interface I_ShotScanner {
    void setFile(File file);
    boolean hasNextShot();
    Shot getShot();
}
