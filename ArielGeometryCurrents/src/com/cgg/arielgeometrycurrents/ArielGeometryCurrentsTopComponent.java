/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrents;

import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.JScrollPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import ucar.ma2.InvalidRangeException;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.cgg.arielgeometrycurrents//ArielGeometryCurrents//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "ArielGeometryCurrentsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "com.cgg.arielgeometrycurrents.ArielGeometryCurrentsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ArielGeometryCurrentsAction",
        preferredID = "ArielGeometryCurrentsTopComponent")
@Messages({
    "CTL_ArielGeometryCurrentsAction=ArielGeometryCurrents",
    "CTL_ArielGeometryCurrentsTopComponent=ArielGeometryCurrents Window",
    "HINT_ArielGeometryCurrentsTopComponent=This is a ArielGeometryCurrents window"
})
public final class ArielGeometryCurrentsTopComponent extends TopComponent {

    CurrentEditorPanel cep;

    public ArielGeometryCurrentsTopComponent() {
        initComponents();
        setName(Bundle.CTL_ArielGeometryCurrentsTopComponent());
        setToolTipText(Bundle.HINT_ArielGeometryCurrentsTopComponent());

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
        System.out.println("ArielGeometryCurrents opened");
        cep = new CurrentEditorPanel();
        JScrollPane scrollPane = new JScrollPane(cep);
        this.setLayout(new BorderLayout());
        this.add(scrollPane);
        //        result = org.openide.util.Utilities.actionsGlobalContext().lookupResult(AbstractGeometryModel.class);
        //        result.addLookupListener(this);
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        System.out.println("ArielGeometryCurrents Showing");

    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
        System.out.println("ArielGeometryCurrents Hidden");
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