/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.viewer;

import com.cgg.arielgeometry.model.ArielBinCharacteristics;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Color;
import java.util.List;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author jgrimsdale
 * 
 * All coordinates are in "bin coordinate system" in metres
 * i.e. 
 * distance between bin centres in x is bin size in x direction
 * distance between bin centres in y is bin size in y direction
 */
public class ArielGeometryViewPanel  {
    
    private SimpleUniverse universe;
    private BranchGroup branchgroup;
    private Canvas3D canvas3d;
    private TransformGroup maintg;
    private float scale = 500.0f; // x y scale
    private float cscale = 5.0f;  // covergae scale

    public ArielGeometryViewPanel() {
        canvas3d = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        canvas3d.setSize(100, 100);
        universe = new SimpleUniverse(canvas3d);
        branchgroup = null;
    }
    
    public void drawmap(AbstractArielGeometryModel agm) {
        if(branchgroup != null && branchgroup.isLive()) branchgroup.detach();
        branchgroup = new BranchGroup();
        branchgroup.setCapability(BranchGroup.ALLOW_DETACH);
        drawcoverage(agm);
//        drawbinmetric(agm);
        drawreceiverpoints(agm);
        drawsourcepoints(agm);
        display();
    }
    
    public Canvas3D getCanvas() {
        return canvas3d;
    }
    
    private Shape3D createXaxis(Color color) {
        return(createaxis(+1.0f, 0.0f, 0.0f, color));
    }
    
    private Shape3D createYaxis(Color color) {
        return(createaxis(0.0f, +1.0f, 0.0f, color));
    }
    
    private Shape3D createZaxis(Color color) {
        return(createaxis(0.0f, 0.0f, +1.0f, color));
    }
    
    Shape3D createaxis(float x, float y, float z, Color color) {
        Point3f[] coords = new Point3f[2];
        LineArray lines = new LineArray(coords.length, 
                GeometryArray.COORDINATES);
        coords[0] = new Point3f(0.0f, 0.0f, 0.0f);
        coords[1] = new Point3f(x, y, z);
        lines.setCoordinates(0, coords);
        Shape3D myshape = new Shape3D(lines, getAppearance(color));
        return(myshape);
    }
    
    public void drawcoverage(AbstractArielGeometryModel agm) {
        float boxx = agm.getBXS();
        float boxy = agm.getBYS();
        float x; // x position
        float y; // y position
        float c; // coverage
        List<ArielBinCharacteristics> list = agm.getcoverage();

        for(int i = 0; i < list.size(); i++) {
            ArielBinCharacteristics localabc = list.get(i);
            x = localabc.x*boxx; 
            y = localabc.y*boxy; 
            c = localabc.coverage*cscale;
            drawbox(x, y, boxx, boxy, c, Color.YELLOW);
        }
    }
    
    public void drawbinmetric(AbstractArielGeometryModel agm) {
        float boxx = agm.getBXS();
        float boxy = agm.getBYS();
        float x; // x position
        float y; // y position
        float c; // coverage
        List<ArielBinCharacteristics> list = agm.getcoverage();

        for(int i = 0; i < list.size(); i++) {
            ArielBinCharacteristics localabc = list.get(i);
            x = localabc.x*boxx; 
            y = localabc.y*boxy; 
            c = localabc.coverage*cscale;
            drawbox(x, y, boxx, boxy, c, Color.YELLOW);
        }
    }
    
    public void drawreceiverpoints(AbstractArielGeometryModel agm) {
//        float boxx = agm.getBXS();
//        float boxy = agm.getBYS();
//        float boxz = 2.0f;
//        List<Integer> list = agm.getreceivers();
//        for(int i = 0; i < list.size(); i += 2) {
//            drawbox(boxx*list.get(i), boxy*list.get(i+1), boxx/2, boxy/2, -boxz, Color.ORANGE);
//        }
    }
    
    public void drawsourcepoints(AbstractArielGeometryModel agm) {
//        float boxx = agm.getBXS();
//        float boxy = agm.getBYS();
//        float boxz = 4.0f;
//        List<Integer> list = agm.getsources();
//        for(int i = 0; i < list.size(); i += 2) {
//            drawbox(boxx*list.get(i), boxy*list.get(i+1), boxx/2, boxy/2, -boxz, Color.BLUE);
//        }
    }
    
    private void drawbox(float x, float y, float xl, float yl, float height, Color color) {
        maintg = new TransformGroup();
        MouseRotate rotatebehavior = new MouseRotate();
        MouseWheelZoom wheelzoombehavior = new MouseWheelZoom();
        MouseTranslate translatebehavior = new MouseTranslate();
        BoundingSphere bounds = new BoundingSphere(
            new Point3d(0.0,0.0,0.0), 10000.0);
        rotatebehavior.setSchedulingBounds(bounds);
        rotatebehavior.setTransformGroup(maintg);
        wheelzoombehavior.setSchedulingBounds(bounds);
        wheelzoombehavior.setTransformGroup(maintg);
        translatebehavior.setSchedulingBounds(bounds);
        translatebehavior.setTransformGroup(maintg);
        maintg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        maintg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        maintg.addChild(rotatebehavior);
        maintg.addChild(wheelzoombehavior);
        maintg.addChild(translatebehavior);
        branchgroup.addChild(maintg);
        TransformGroup tg = new TransformGroup();
        Transform3D transform = new Transform3D();
        Box box = new Box(xl/scale, yl/scale, height/scale, getAppearance(color));
        Vector3f vector = new Vector3f(x/scale, y/scale, height/scale);
        transform.setTranslation(vector);
        tg.setTransform(transform);
        tg.addChild(box);
        maintg.addChild(tg);
    }
    
    private void showshape(Shape3D shape3d) {
        TransformGroup tg = new TransformGroup();
        Transform3D transform = new Transform3D();
        Vector3f vector = new Vector3f( .0f, .0f, .0f);
        MouseRotate rotatebehavior = new MouseRotate();
        MouseWheelZoom wheelzoombehavior = new MouseWheelZoom();
        MouseTranslate translatebehavior = new MouseTranslate();
        BoundingSphere bounds = new BoundingSphere(
            new Point3d(0.0,0.0,0.0), 100.0);
        rotatebehavior.setSchedulingBounds(bounds);
        rotatebehavior.setTransformGroup(tg);
        wheelzoombehavior.setSchedulingBounds(bounds);
        wheelzoombehavior.setTransformGroup(tg);
        translatebehavior.setSchedulingBounds(bounds);
        translatebehavior.setTransformGroup(tg);
        transform.setTranslation(vector);
        tg.setTransform(transform);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.addChild(rotatebehavior);
        tg.addChild(wheelzoombehavior);
        tg.addChild(translatebehavior);
        tg.addChild(shape3d);
        branchgroup.addChild(tg);
    }
    
    final void display() {
        Color3f light1Color = new Color3f(1.0f, 1.0f, 1.0f); // white light
        // Color3f light1Color = new Color3f(.1f, 1.4f, .1f); // green light
        BoundingSphere bounds = new BoundingSphere(
                new Point3d(0.0,0.0,0.0), 100.0);
        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
        DirectionalLight light1 = new DirectionalLight(light1Color, 
                light1Direction);
        light1.setInfluencingBounds(bounds);
        // draw axes
        showshape(createXaxis(Color.blue));
        showshape(createYaxis(Color.green));
        showshape(createZaxis(Color.magenta));
        branchgroup.addChild(light1);
        universe.getViewingPlatform().setNominalViewingTransform();
        universe.getViewer().getView()
                .setProjectionPolicy(View.PERSPECTIVE_PROJECTION);

        // add the group of objects to the Universe
        branchgroup.compile();
        universe.addBranchGraph(branchgroup);
    }

    public static Appearance getAppearance(Color color) {
        return getAppearance(new Color3f(color));
    }
    
    public static Appearance getAppearance(Color3f color) {
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
        Appearance ap = new Appearance();
        Texture texture = new Texture2D();
        PointAttributes pa = new PointAttributes();
        LineAttributes la = new LineAttributes();
        pa.setPointSize(2.0f);
        la.setLineWidth(1.0f);
        la.setLineAntialiasingEnable(true);
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        texture.setBoundaryModeS(Texture.WRAP);
        texture.setBoundaryModeT(Texture.WRAP);
        texture.setBoundaryColor(new Color4f(0.0f, 1.0f, 0.0f, 0.0f));
        Material mat = new Material(color, black, color, white, 64.0f);
        ap.setTextureAttributes(texAttr);
        ap.setMaterial(mat);
        ap.setTexture(texture);	 
        ColoringAttributes ca = new ColoringAttributes(color,
                        ColoringAttributes.NICEST);
        ap.setColoringAttributes(ca);
        ap.setPointAttributes(pa);
        ap.setLineAttributes(la);
        return ap;
    }   
}
