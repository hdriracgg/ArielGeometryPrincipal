/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arieltrajectoryeditor;

import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.cgg.arieltrajectoryeditor//ArielTrajectoryEditor//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "ArielTrajectoryEditorTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.cgg.arieltrajectoryeditor.ArielTrajectoryEditorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ArielTrajectoryEditorAction",
        preferredID = "ArielTrajectoryEditorTopComponent")
@Messages({
    "CTL_ArielTrajectoryEditorAction=ArielTrajectoryEditor",
    "CTL_ArielTrajectoryEditorTopComponent=ArielTrajectoryEditor Window",
    "HINT_ArielTrajectoryEditorTopComponent=This is a ArielTrajectoryEditor window"
})
public final class ArielTrajectoryEditorTopComponent extends TopComponent {

    private WayPointEditingPanel wpep;
    
    public ArielTrajectoryEditorTopComponent() {
        initComponents();
        setName(Bundle.CTL_ArielTrajectoryEditorTopComponent());
        setToolTipText(Bundle.HINT_ArielTrajectoryEditorTopComponent());

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
        System.out.println("ArielTrajectoryEditor opened");
        wpep = new WayPointEditingPanel();
        JScrollPane scrollPane = new JScrollPane(wpep);
        this.setLayout(new BorderLayout());
        this.add(scrollPane);
//        result = org.openide.util.Utilities.actionsGlobalContext().lookupResult(AbstractGeometryModel.class);
//        result.addLookupListener(this);
//        if(debug) System.out.println("ArielGeometryJFCViewerTopComponent.componentOpened");
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
