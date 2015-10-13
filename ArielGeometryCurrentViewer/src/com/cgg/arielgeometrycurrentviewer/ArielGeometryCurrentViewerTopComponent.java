/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrentviewer;

import com.cgg.arielgeometrycurrents.TideModel;
import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.JScrollPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import ucar.ma2.InvalidRangeException;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.cgg.arielgeometrycurrentviewer//ArielGeometryCurrentViewer//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "ArielGeometryCurrentViewerTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.cgg.arielgeometrycurrentviewer.ArielGeometryCurrentViewerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ArielGeometryCurrentViewerAction",
        preferredID = "ArielGeometryCurrentViewerTopComponent")
@Messages({
    "CTL_ArielGeometryCurrentViewerAction=ArielGeometryCurrentViewer",
    "CTL_ArielGeometryCurrentViewerTopComponent=ArielGeometryCurrentViewer Window",
    "HINT_ArielGeometryCurrentViewerTopComponent=This is a ArielGeometryCurrentViewer window"
})
public final class ArielGeometryCurrentViewerTopComponent extends TopComponent {

    CurrentViewerPanel cvp;
    private InstanceContent content = null;

    public ArielGeometryCurrentViewerTopComponent() {
        initComponents();
        setName(Bundle.CTL_ArielGeometryCurrentViewerTopComponent());
        setToolTipText(Bundle.HINT_ArielGeometryCurrentViewerTopComponent());
        content = new InstanceContent();
        associateLookup(new AbstractLookup(content));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        System.out.println("ArielGeometryCurrentViewer opened");
        try {
            cvp = new CurrentViewerPanel(this);
        }
        catch (IOException | InvalidRangeException ex) {
            Exceptions.printStackTrace(ex);
        }
        JScrollPane scrollPane = new JScrollPane(cvp);
        this.setLayout(new BorderLayout());
        this.add(scrollPane);
    }

    void updateTideModel(TideModel tm) {
        System.out.println("agcvtc update");
        content.add(tm);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}