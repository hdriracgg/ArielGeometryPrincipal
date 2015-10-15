/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrentviewer;

import com.cgg.arielgeometrycurrents.MetOceanModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author jgrimsdale
 */
public class CurrentViewerPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    ArielGeometryCurrentViewerTopComponent agcvtc;
    BufferedImage background;
    boolean debug = false;
    int itlx = 0; // Image top left x
    int itly = 0; // Image top left y
    Point dragstartpoint;
    double scale = 1.0;
    double zoomfactor = 1.05;
    int xoffset;    // screen x coordinate of left edge of map 
    int yoffset;    // screen y coordinate of top edge of map
    int xorigin;    // real x coordinate of left edge of map
    int yorigin;    // real y coordinate of top edge of map
    double xscale;  // scale factor between screen and real in x direction
    double yscale;  // scale factor between screen and real in y direction
    boolean calibrating = false;
    boolean defaultcalibration = false;
    List<Point> calibrationpointlist;
    JDialog calibrationdialog;
    JButton jb4;
    JButton jb5;
    JSlider js1;
    Stroke widestroke;
    int currentphase = 0;
    MetOceanModel mom;

    public CurrentViewerPanel(ArielGeometryCurrentViewerTopComponent agcvtc) throws IOException, InvalidRangeException {
        this.agcvtc = agcvtc;
        setBackground(Color.white);
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);

        widestroke = new BasicStroke(2.5f);
        try {
            background = ImageIO.read(new File("C:\\Users\\jgrimsdale\\Desktop\\Moussafir A0 SURVEY map.png"));
        }
        catch (IOException e) {
            System.out.println("Cannot open file for background, choose a png in dialog");
            background = getbackground();
        }

        jb4 = new JButton("Load MO");
        jb4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadMO();
            }
        });
        add(jb4);

        JLabel jl1 = new JLabel("Min depth = ");
        jl1.setOpaque(true);
        jl1.setBackground(Color.LIGHT_GRAY);
        add(jl1);

        final JTextField jtf1 = new JTextField();
        jtf1.setText(Float.toString(mindepth));
        jtf1.setToolTipText("Hit Enter key to confirm");
        jtf1.setPreferredSize(new Dimension(50, 27));
        jtf1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mindepth = Float.parseFloat(jtf1.getText());
                System.out.println("Minimum depth changed to " + mindepth);
                classifybyphase();
                repaint();
            }
        });
        add(jtf1);

        JLabel jl2 = new JLabel("Max depth = ");
        jl2.setOpaque(true);
        jl2.setBackground(Color.LIGHT_GRAY);
        add(jl2);

        final JTextField jtf2 = new JTextField();
        jtf2.setText(Float.toString(maxdepth));
        jtf2.setToolTipText("Hit Enter key to confirm");
        jtf2.setPreferredSize(new Dimension(50, 27));
        jtf2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maxdepth = Float.parseFloat(jtf2.getText());
                System.out.println("max depth changed to " + maxdepth);
                classifybyphase();
                repaint();
            }
        });
        add(jtf2);

        JLabel jl4 = new JLabel("Current phase = ");
        jl4.setOpaque(true);
        jl4.setBackground(Color.LIGHT_GRAY);
        add(jl4);

        final JTextField jtf4 = new JTextField();
        jtf4.setText(Integer.toString(currentphase));
        jtf4.setToolTipText("Current phase");
        jtf4.setPreferredSize(new Dimension(50, 27));
        jtf4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentphase = Integer.parseInt(jtf4.getText());
                System.out.println("current phase changed to " + currentphase);
                repaint();
            }
        });
        add(jtf4);

        js1 = new JSlider(0, phaseclasses - 1);
        js1.setToolTipText("Slide to change tide phase");
        js1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                currentphase = js1.getValue();
                jtf4.setText(Integer.toString(currentphase));
                repaint();
            }
        });
        add(js1);

        calibrationpointlist = new ArrayList<>();
        setdefaultcalibration();
    }

    private BufferedImage getbackground() {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Select background image png file");
        jfc.setToolTipText("Select background image png file");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("png Files", "png");
        jfc.setFileFilter(filter);
        jfc.showOpenDialog(this);
        File file = jfc.getSelectedFile();
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        }
        catch (IOException e) {
            System.out.println("Cannot open file for background " + file.getAbsolutePath());
        }
        return bi;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 600);
    }

    // Handles the event of the user pressing down the mouse button.
    @Override
    public void mousePressed(MouseEvent e) {
        dragstartpoint = new Point(e.getX(), e.getY());
    }

    private void drawpoint(Graphics2D g, int x, int y) {
        g.fillOval(x - 2, y - 2, 5, 5);
    }

    private void drawline(Graphics2D g, Point p1, Point p2) {
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void moveimage(MouseEvent e) {
        int x = dragstartpoint.x;
        int y = dragstartpoint.y;
        int newx = e.getX();
        int newy = e.getY();
        itlx += (newx - x);
        itly += (newy - y);
        dragstartpoint = new Point(newx, newy);
    }

    private void zoom(int direction) {
        if (direction < 0) {
            scale *= zoomfactor;
        }
        else {
            scale *= (1 / zoomfactor);
        }
    }

    // Handles the event of a user dragging the mouse while holding
    // down the mouse button.
    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.isControlDown()) {
            moveimage(e);
        }
        repaint();
    }

    // Handles the event of a user releasing the mouse button.
    @Override
    public void mouseReleased(MouseEvent e) {
//        System.out.println("mouseReleased");
    }

    // This method is required by MouseListener.
    @Override
    public void mouseMoved(MouseEvent e) {
//        System.out.println("mouseMoved");
    }

    // These methods are required by MouseMotionListener.
    @Override
    public void mouseClicked(MouseEvent e) {
        if (calibrating) {
            setcalibrationpoint(e.getX(), e.getY());
        }
        else {
            if (e.getButton() == MouseEvent.BUTTON1) {
            }
        }
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
//        System.out.println("mouseExited");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
//        System.out.println("mouseEntered");
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoom(e.getWheelRotation());
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform at = g2.getTransform();
        g2.scale(scale, scale);
        g2.translate(itlx, itly);
        g2.drawImage(background, 0, 0, this);
        Stroke defaultstroke = g2.getStroke();
        Color defaultcolor = g2.getColor();
        g2.setStroke(defaultstroke);
        g2.setColor(defaultcolor);
        paintphaseclassvelocities(g2, currentphase);
        if (!calibrationpointlist.isEmpty()) {
            for (Point p : calibrationpointlist) {
                drawpoint(g2, p.x, p.y);
            }
        }
        g2.setTransform(at);
    }

    private Point screen2transformed(int x, int y) {
        return new Point((int) ((x / scale) - itlx), (int) ((y / scale) - itly));
    }

    private Point transformed2screen(int x, int y) {
        return new Point((int) ((itlx + x) * scale), (int) ((itly + y) / scale));
    }

    private Point transformed2screen(Point p) {
        return transformed2screen(p.x, p.y);
    }

    private Point transformed2real(Point p) {
        int x = xorigin + (int) (xscale * (p.x - xoffset));
        int y = yorigin - (int) (yscale * (p.y - yoffset));
        return new Point(x, y);
    }

    private Point real2transformed(Point p) {
        int x = (int) ((p.x - xorigin) / xscale) + xoffset;
        int y = (int) ((yorigin - p.y) / yscale) + yoffset;
        return new Point(x, y);
    }

    private Point real2screen(Point p) {
        Point transformed = real2transformed(p);
        return transformed2screen(transformed.x, transformed.y);
    }

    private void calibrate() {
        calibrating = true;
        calibrationpointlist = new ArrayList<>();
    }

    private void setcalibrationpoint(int x, int y) {
        calibrationpointlist.add(screen2transformed(x, y));
        repaint();
        if (calibrationpointlist.size() == 2) {
            finishcalibrating();
            calibrating = false;
        }
    }

    private void finishcalibrating() {
        if (!defaultcalibration) {
            JDialog jd = new JDialog((JDialog) null, "Calibration", true);
            calibrationdialog = jd;
            jd.setSize(200, 300);
//            jd.add(new CalibrateForm(this));
            jd.setVisible(true);
        }
    }

    void setcalibrationpoints(int tlx, int tly, int brx, int bry) {
        System.out.println(" calib points " + tlx + " " + tly + " " + brx + " " + bry);
        System.out.println(" points selected " + calibrationpointlist);
        Point tl = calibrationpointlist.get(0);
        Point br = calibrationpointlist.get(1);
        xscale = (float) (brx - tlx) / (float) (br.x - tl.x);
        yscale = (float) (tly - bry) / (float) (br.y - tl.y);
        xoffset = tl.x;
        yoffset = tl.y;
        xorigin = tlx;
        yorigin = tly;
    }

    private void setdefaultcalibration() {
        defaultcalibration = true;
        calibrate();
        setcalibrationpoint(58, 45);
        setcalibrationpoint(3890, 3248);
        setcalibrationpoints(122864, 3083951, 142331, 3067684);
        calibrationpointlist.add(new Point(58, 45));
        calibrationpointlist.add(new Point(3890, 3248));
        defaultcalibration = false;
    }
    //    Each buoy has a Map of date, record
    //   Map<buoyname, Map<javadate, MORrecord>>
    public Map<String, Map<Date, MetOceanModel.MORecord>> buoyMap = null;
    Map<Integer, List<MetOceanModel.MORecord>> phaseMap = null;
    List<MetOceanModel.MORecord> recordList = null;
    int phaseclasses = 6;     // number of tide phase classes
    float tideperiod = 12.41f; // period from high tide to high tide
//    float tideperiod = 12.53f; // period from high tide to high tide
//    float tideperiod = 24.8f; // period from high tide to high tide
    float linescale = 1000.0f;   // scale factor for current line
    float mindepth = 5.0f;
    float maxdepth = 30.0f;

    private void loadMO() {
        mom = new MetOceanModel();
        buoyMap = mom.buoyMap;
        classifybyphase();
//        TideModel tm = new TideModel();
//        agcvtc.updateTideModel(tm);
    }

    private void paintphaseclassvelocities(Graphics2D g2, Integer phaseclass) {
        if (phaseMap == null) {
            return;
        }
        recordList = phaseMap.get(phaseclass);
        if (recordList == null) {
            return;
        }
        InterpolatedColorMaker icm = new InterpolatedColorMaker(mom.mintime, mom.maxtime, mom.timerange);
        for (MetOceanModel.MORecord mor : recordList) {
            paintvelocity(g2, mor, icm);
        }
    }

    private void paintvelocity(Graphics2D g2, MetOceanModel.MORecord mor, InterpolatedColorMaker icm) {
        int realstartx = mor.position.x;
        int realstarty = mor.position.y;
        int realendx = realstartx + (int) (linescale * mor.current[0]);
        int realendy = realstarty + (int) (linescale * mor.current[1]);
        Point realstartpoint = new Point(realstartx, realstarty);
        Point realendpoint = new Point(realendx, realendy);
        Point transformedstartpoint = real2transformed(realstartpoint);
        Point transformedendpoint = real2transformed(realendpoint);

        Color oldcolor = g2.getColor();
        g2.setPaint(icm.getPaint(mor.javadate.getTime()));
        drawpoint(g2, transformedstartpoint.x, transformedstartpoint.y);
        drawline(g2, transformedstartpoint, transformedendpoint);
        g2.setColor(oldcolor);
    }

    private void classifybyphase() {
        phaseMap = new TreeMap<>();
        for (String buoy : buoyMap.keySet()) {
            for (Date d : buoyMap.get(buoy).keySet()) {
                MetOceanModel.MORecord mor = buoyMap.get(buoy).get(d);
                if (mor.depth < mindepth || mor.depth > maxdepth) {
                    continue;
                }
                Integer phaseclass = getphaseclass(d);
                if (phaseMap.containsKey(phaseclass)) {
                    recordList = phaseMap.get(phaseclass);
                }
                else {
                    recordList = new ArrayList<>();
                    phaseMap.put(phaseclass, recordList);
                }
                recordList.add(mor);
            }
        }
        for (Integer phaseclass : phaseMap.keySet()) {
            System.out.printf("Class %d has %d points\n", phaseclass, phaseMap.get(phaseclass).size());
        }
    }

    private int getphaseclass(Date d) {
        long secs = d.getTime();
        long secondsperphase = (long) (tideperiod * 3600.0f);
        long secondsperphaseclass = secondsperphase / phaseclasses;
        long exactphase = secs % secondsperphase;
        return (int) (exactphase / secondsperphaseclass);
    }
}
