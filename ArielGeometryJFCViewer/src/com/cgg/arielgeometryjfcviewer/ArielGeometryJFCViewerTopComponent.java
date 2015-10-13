/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometryjfcviewer;

import com.cgg.arielgeometry.model.AbstractGeometryModel;
import java.awt.BorderLayout;
import java.util.Collection;
import javax.swing.JScrollPane;
import javax.swing.RepaintManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.cgg.arielgeometryjfcviewer//ArielGeometryJFCViewer//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "ArielGeometryJFCViewerTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.cgg.arielgeometryjfcviewer.ArielGeometryJFCViewerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ArielGeometryJFCViewerAction",
        preferredID = "ArielGeometryJFCViewerTopComponent")
@Messages({
    "CTL_ArielGeometryJFCViewerAction=ArielGeometryJFCViewer",
    "CTL_ArielGeometryJFCViewerTopComponent=ArielGeometryJFCViewer Window",
    "HINT_ArielGeometryJFCViewerTopComponent=This is a ArielGeometryJFCViewer window"
})

public final class ArielGeometryJFCViewerTopComponent extends TopComponent implements LookupListener {

    private AbstractGeometryModel agm;
    private InstanceContent content;
    
    private org.openide.util.Lookup.Result<AbstractGeometryModel> result;
    private ArielGeometryJFCXYPlot chartpanel;
    
    private boolean debug = false;

    public ArielGeometryJFCViewerTopComponent() {
        initComponents();
        setName(Bundle.CTL_ArielGeometryJFCViewerTopComponent());
        setToolTipText(Bundle.HINT_ArielGeometryJFCViewerTopComponent());
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
        chartpanel = new ArielGeometryJFCXYPlot("Plot title", content);
        JScrollPane scrollPane = new JScrollPane(chartpanel);
        this.setLayout(new BorderLayout());
        this.add(scrollPane);
        result = org.openide.util.Utilities.actionsGlobalContext().lookupResult(AbstractGeometryModel.class);
        result.addLookupListener(this);
        if(debug) System.out.println("ArielGeometryJFCViewerTopComponent.componentOpened");
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }
    
    @Override
    protected void componentShowing() {
        super.componentShowing();
        System.out.println("JFCXYPlot Showing");
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
        System.out.println("JFCXYPlot Hidden");
    }
    
    @Override
    protected void componentActivated() {
        super.componentActivated();
        if(debug) System.out.println("ArielGeometryJFCViewerTopComponent.componentActivated");
        RepaintManager.currentManager(this).addInvalidComponent(this);
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        if(debug) System.out.println("ArielGeometryJFCViewerTopComponent.componentDeactivated");
        RepaintManager.currentManager(this).addInvalidComponent(this);
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

    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends AbstractGeometryModel> allagms = result.allInstances();
        if(debug) System.out.println("ArielGeometryJFCViewerTopComponent.resultChanged");
        for(AbstractGeometryModel agml : allagms) {
            chartpanel.drawmaps(agml);
        }
    }
}
