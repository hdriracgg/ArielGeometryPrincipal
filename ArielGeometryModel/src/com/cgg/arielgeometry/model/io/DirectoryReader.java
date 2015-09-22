/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model.io;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author jgrimsdale
 */
public class DirectoryReader {

    File directory;

    public DirectoryReader(String title) {
        chooseFile(title);
    }

    private void chooseFile(String title) {
        // Choose directory
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setDialogTitle(title);
        jfc.showOpenDialog(jfc);
        File id = jfc.getSelectedFile();
        String inputdirectory = id.getAbsolutePath();
        System.out.println("Reading from directory " + inputdirectory);
        directory = new File(inputdirectory);
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
