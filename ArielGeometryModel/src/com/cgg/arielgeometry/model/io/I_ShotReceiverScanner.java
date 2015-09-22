/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.io;

import com.cgg.arielgeometry.model.types.Receiver;
import com.cgg.arielgeometry.model.types.Shot;

/**
 *
 * @author jgrimsdale
 */
public interface I_ShotReceiverScanner {
    
    boolean hasNextShotReceiver();
    boolean hasMoreReceiversForShot();
    String getshotname();
    Shot getShot();
}
