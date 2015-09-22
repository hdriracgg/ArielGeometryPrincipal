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
        readFiles(dr, directory);
    }
    
    // Recursively read files 
    private void readFiles(I_FileReader dr, File d) {
        if (d.isFile()) {
            dr.readaFile(d);
        }
        if (d.isDirectory()) {
            for (File fileEntry : d.listFiles()) {
                readFiles(dr, fileEntry);
            }
        }
    }
}
