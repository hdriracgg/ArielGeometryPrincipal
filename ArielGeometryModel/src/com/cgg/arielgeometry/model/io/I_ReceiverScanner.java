/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.io;

import com.cgg.arielgeometry.model.types.Receiver;
import java.io.File;

/**
 *
 * @author jgrimsdale
 */
public interface I_ReceiverScanner {
    boolean hasNextReceiver();
    String getrecname();
    Receiver getReceiver();
    void setFile(File file);
}
