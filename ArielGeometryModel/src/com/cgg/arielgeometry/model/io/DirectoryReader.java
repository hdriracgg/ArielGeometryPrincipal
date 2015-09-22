/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.io;

import com.cgg.arielgeometry.model.io.I_FileReader;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

/**
 *
 * @author jgrimsdale
 */
public class DirectoryReader {

    String title;
    File directory;

    public DirectoryReader(String title) {
        this.title = title;
        chooseFile();
    }

    private void chooseFile() {
        FileDialog fd = new FileDialog(new Frame(), title, FileDialog.LOAD);
        fd.setVisible(true);
        directory = new File(fd.getDirectory());
    }

    public void readFiles(I_FileReader dr) {
        if (directory.isFile()) {
            dr.readaFile(directory);
        }
        if (directory.isDirectory()) {
            for (File fileEntry : directory.listFiles()) {
                dr.readaFile(fileEntry);
            }
        }
    }
}
