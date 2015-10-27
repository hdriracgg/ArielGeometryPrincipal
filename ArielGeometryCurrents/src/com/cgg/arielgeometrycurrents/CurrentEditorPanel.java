/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometrycurrents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.Exceptions;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author jgrimsdale
 */
public class CurrentEditorPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    ArielGeometryCurrentsTopComponent agctc;
    NetcdfFile ncFile = null;
    MetOceanModel mom = null;
    NetcdfReader netcdfreader = null;
    int coordval;       // coordinate flag 
    // 0 = tangent plane, 1 = unrotated spherical grid, 3 = rotated spherical grid.
    int nxval;          // number of grid points in x direction
    int nyval;          // number of grid points in y direction
    float gridxval;     // grid spacing in x direction
    float gridyval;     // grid spacing in y direction
    float rlatdval;     // Latitude of transformation centre
    float rlngdval;     // Longitude of transformation centre
    float delxval;      // Transformation grid offset in X
    float delyval;      // Transformation grid offset in Y
    float thetadval;    // Rotation angle
    float dxtval;       // Grid spacing in X (metres)
    float dytval;       // Grid spacing in Y (metres)
    int[] deptharray;   // output levels (outlev index in vtot)
    float[] timearray;  // times = (starttime index in vtot)
    Variable vtot;      // contains x and y velocity vectors as a function of position, starttime and depth
    Variable vgrid3;    // contains grid node locations
    double cm2deg = 180.0 / (Math.PI * 637131500.0);
    double deg2cm = (Math.PI * 637131500.0) / 180.0;
    double m2deg = 180.0 / (Math.PI * 6371315.0);
    double deg2m = (Math.PI * 6371315.0) / 180.0;
    int previoustimeindex = -1; // Time index of previous lookup
    float[][][][] previous4darray;
    BufferedImage background;
    boolean firstTime = true;
    boolean debug = false;
    int itlx = 0; // Image top left x
    int itly = 0; // Image top left y
    List<Point> pointList = null;
    List<Float> speedList = null;   // speed in m/s
    List<Float> timeList = null;    // list of times since start (0)
    List<Point> marinapointList = null;
    List<Float> marinaspeedList = null;
    List<Float> marinatimeList = null;
    List<Point> sourcepointList = null;
    List<Float> sourcespeedList = null;
    List<Float> sourcetimeList = null;
    List<Trajectory> trajectoryList = null;
    Trajectory currentTrajectory = null;
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
    int speed = 25;
    int starttime = 5000;
    JButton jb4;
    JButton jb5;
    Color defaultfg;
    Color defaultbg;
    Stroke widestroke;
    float timestep = 3600.0f;
    float depth = 50.0f;
    int steps = 10;
    int defaultdepth = 20;

    public CurrentEditorPanel(ArielGeometryCurrentsTopComponent agctc) throws IOException, InvalidRangeException {
        this.agctc = agctc;
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

        JButton jb1 = new JButton("Clear");
        jb1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearpoints();
                repaint();
            }
        });
        defaultbg = jb1.getBackground();
        defaultfg = jb1.getForeground();
        add(jb1);

        JButton jb3 = new JButton("Calibrate");
        jb3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calibrate();
            }
        });
        add(jb3);

        jb4 = new JButton("Load MO");
        jb4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadMO();
            }
        });
        add(jb4);

        JButton jb4a = new JButton("Load netcdf");
        jb4a.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadnetcdf();
            }
        });
        add(jb4a);

        JLabel jl1 = new JLabel("Start time in s = ");
        jl1.setOpaque(true);
        jl1.setBackground(Color.LIGHT_GRAY);
        add(jl1);

        final JTextField jtf1 = new JTextField();
        jtf1.setText(Integer.toString(starttime));
        jtf1.setToolTipText("Hit Enter key to confirm");
        jtf1.setPreferredSize(new Dimension(50, 27));
        jtf1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                starttime = Integer.parseInt(jtf1.getText());
                System.out.println("Start time changed to " + starttime + " s");
            }
        });
        add(jtf1);

        JLabel jl2 = new JLabel("Timestep in s = ");
        jl2.setOpaque(true);
        jl2.setBackground(Color.LIGHT_GRAY);
        add(jl2);

        final JTextField jtf2 = new JTextField();
        jtf2.setText(Float.toString(timestep));
        jtf2.setToolTipText("Hit Enter key to confirm");
        jtf2.setPreferredSize(new Dimension(50, 27));
        jtf2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timestep = Float.parseFloat(jtf2.getText());
                System.out.println("timestep changed to " + timestep + " s");
            }
        });
        add(jtf2);

        JLabel jl3 = new JLabel("Depth in m = ");
        jl3.setOpaque(true);
        jl3.setBackground(Color.LIGHT_GRAY);
        add(jl3);

        final JTextField jtf3 = new JTextField();
        jtf3.setText(Float.toString(depth));
        jtf3.setToolTipText("Hit Enter key to confirm");
        jtf3.setPreferredSize(new Dimension(50, 27));
        jtf3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                depth = Float.parseFloat(jtf3.getText());
                System.out.println("depth changed to " + depth + " s");
            }
        });
        add(jtf3);

        JLabel jl4 = new JLabel("Steps = ");
        jl4.setOpaque(true);
        jl4.setBackground(Color.LIGHT_GRAY);
        add(jl4);

        final JTextField jtf4 = new JTextField();
        jtf4.setText(Integer.toString(steps));
        jtf4.setToolTipText("Hit Enter key to confirm");
        jtf4.setPreferredSize(new Dimension(50, 27));
        jtf4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                steps = Integer.parseInt(jtf4.getText());
                System.out.println("steps changed to " + steps);
            }
        });
        add(jtf4);

        calibrationpointlist = new ArrayList<>();
        trajectoryList = new ArrayList<>();
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

    private void clearpoints() {
        trajectoryList = null;
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
            if (e.getButton() == MouseEvent.BUTTON1 && netcdfreader != null) {
                try {
                    System.out.println("Build netcdf trajectory");
                    buildnetcdftrajectory(e.getX(), e.getY());
                }
                catch (IOException | InvalidRangeException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (e.getButton() == MouseEvent.BUTTON1 && mom != null) {
                System.out.println("Build MetOceanModel trajectory");
                buildmomtrajectory(e.getX(), e.getY());
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3 && mom != null) {
            System.out.println("Get real buoy trajectory");
            buildbuoytrajectory(e.getX(), e.getY());
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
        paintbuttons();
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform at = g2.getTransform();
        g2.scale(scale, scale);
        g2.translate(itlx, itly);
        g2.drawImage(background, 0, 0, this);
        Stroke defaultstroke = g2.getStroke();
        Color defaultcolor = g2.getColor();
        if (marinapointList != null) {
            g2.setStroke(widestroke);
            g2.setColor(Color.RED);
            sourcepointList = pointList;
            sourcespeedList = speedList;
            sourcetimeList = timeList;
            pointList = marinapointList;
            speedList = marinaspeedList;
            timeList = marinatimeList;
            painttrajectories(g2);
            pointList = sourcepointList;
            speedList = sourcespeedList;
            timeList = sourcetimeList;
        }
        g2.setStroke(defaultstroke);
        g2.setColor(defaultcolor);
        painttrajectories(g2);
        if (!calibrationpointlist.isEmpty()) {
            for (Point p : calibrationpointlist) {
                drawpoint(g2, p.x, p.y);
            }
        }
        g2.setTransform(at);
    }

    private void paintbuttons() {
        jb4.setBackground(defaultbg);
        jb4.setForeground(defaultfg);
        jb4.setSelected(false);
//        if (writingmarinatrajectories) {
//            jb4.setBackground(Color.red);
//            jb4.setForeground(Color.pink);
//            jb4.setSelected(true);
//        }
    }

    private void painttrajectories(Graphics2D g2) {
        if (trajectoryList != null) {
            for (Trajectory t : trajectoryList) {
                painttrajectory(g2, t);
            }
        }
    }

    private void painttrajectory(Graphics2D g2, Trajectory t) {
        Point previouspoint = null;
        float fontsize = (float) (11 / scale + 0.2);
        Font oldfont = g2.getFont();
        Font newfont = oldfont.deriveFont(fontsize);
        g2.setFont(newfont);
        for (Point p : t.transformedPoints) {
            String s = getspeedfromp(p, t) + " " + gettimefromp(p, t);
            g2.drawString(s, 10 + p.x, p.y);
            drawpoint(g2, p.x, p.y);
            if (previouspoint != null) {
                drawline(g2, p, previouspoint);
            }
            previouspoint = p;
        }
        g2.setFont(oldfont);
    }

    private String gettimefromp(Point p, Trajectory t) {
        int idx = t.transformedPoints.indexOf(p);
        return String.format("%.0fs", t.times.get(idx));
    }

    private String getspeedfromp(Point p, Trajectory t) {
        int idx = t.transformedPoints.indexOf(p);
        if (idx >= t.speeds.size()) {
            idx = t.speeds.size() - 1;
        }
        float sp = 100.0f * t.speeds.get(idx);
        return String.format("%.2fcm/s", sp);
    }

    private Point screen2real(int x, int y) {
        return transformed2real(screen2transformed(x, y));
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

    private List<Point> printpoints() {
        if (pointList.isEmpty()) {
            System.out.println("No points clicked yet");
        }
        for (Point p : pointList) {
            System.out.println("Point = " + p);
        }
        if (!calibrationpointlist.isEmpty()) {
            for (Point p : pointList) {
                System.out.println("Real point = " + transformed2real(p));
            }
        }
        return pointList;
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

    double x2lon(int x) {
        return x * m2deg + 34.277944d;
    }

    double y2lat(int y) {
        return y * m2deg - 0.28087525d;
    }

    private void buildnetcdftrajectory(int x, int y) throws IOException, InvalidRangeException {

        if (trajectoryList == null) {
            trajectoryList = new ArrayList<>();
        }

        Trajectory ct = new Trajectory();
        trajectoryList.add(ct);

        ct.addPoint(x, y);

        int startx = ct.realx;
        int starty = ct.realy;
        double startlon = x2lon(startx);
        double startlat = y2lat(starty);
        float previouslon = (float) startlon;
        float previouslat = (float) startlat;
        int previousx = startx;
        int previousy = starty;
        for (int i = 0; i < steps; i++) {
            System.out.println("previouslon=" + previouslon + " previouslat=" + previouslat);
            int timenow = starttime + (int) (i * timestep);
            float[] current = netcdfreader.getvv(previouslon, previouslat, timenow, depth);
            System.out.println("Current = " + current[0] + "," + current[1]);
            int nextx = previousx + (int) (current[0] * timestep / 100.0f);
            int nexty = previousy + (int) (current[1] * timestep / 100.0f);
            ct.addrealPoint(nextx, nexty);
            previousx = nextx;
            previousy = nexty;
            previouslon = (float) x2lon(nextx);
            previouslat = (float) y2lat(nexty);
            System.out.println("nextx=" + nextx + " nexty=" + nexty);
        }
    }

    private void buildmomtrajectory(int x, int y) {

        if (trajectoryList == null) {
            trajectoryList = new ArrayList<>();
        }

        Trajectory ct = new Trajectory();
        trajectoryList.add(ct);

        ct.addPoint(x, y);

        int startx = ct.realx;
        int starty = ct.realy;
        int previousx = startx;
        int previousy = starty;
        for (int i = 0; i < steps; i++) {
            System.out.println("previousx=" + previousx + " previousy=" + previousy);
            int timenow = (starttime + (int) (i * timestep)) * 1000;
            float[] current = mom.findclosest(previousx, previousy, timenow, defaultdepth).current;
            System.out.println("Current = " + current[0] + "," + current[1]);
            int nextx = previousx + (int) (current[0] * timestep);
            int nexty = previousy + (int) (current[1] * timestep);
            ct.addrealPoint(nextx, nexty);
            previousx = nextx;
            previousy = nexty;
            System.out.println("nextx=" + nextx + " nexty=" + nexty);
        }
    }

    private void buildbuoytrajectory(int x, int y) {
        if (mom == null) {
            System.out.println("No MO Model is loaded");
            return;
        }
        if (trajectoryList == null) {
            trajectoryList = new ArrayList<>();
        }
        BuoyTrajectory ct = new BuoyTrajectory();
        Point startpoint = screen2real(x, y);

        // find the nearest buoy in the future
        MetOceanModel.MORecord mor = mom.findclosest(startpoint.x, startpoint.y, starttime * -1000, defaultdepth);
        String buoy = mom.getname(mor);
        System.out.println("buoy = " + buoy);
        System.out.println(mor);

        Map<Date, MetOceanModel.MORecord> recordmap = mom.getrecordsbybuoy(buoy);

        Date startdate = new Date(mom.getmomtime(starttime*1000));
        MetOceanModel.MORecord nextr;
        MetOceanModel.MORecord previousr = null;
        int pointsadded = 0;
        for (Date d : recordmap.keySet()) {
            if (d.after(startdate)) {
                nextr = recordmap.get(d);
                // Assume the buoy has been relaunched if time difference is more than 30 minutes
                System.out.println("nextr \n" + nextr);
                if (nextr.gettimedifference(previousr) > (30 * 60 * 1000)) {
                    break;
                }
                ct.addrealPoint(previousr, nextr);
                pointsadded++;
                previousr = nextr;
            }
        }
        if (pointsadded > 1) {
            System.out.println(pointsadded + " points added");
            trajectoryList.add(ct);
        }
        else {
            System.out.println("No points added");
        }
    }

    private float calculatespeed(int x, int y, int newx, int newy, float t) {
        double distance = Math.sqrt(Math.pow(newx - x, 2) + Math.pow(newy - y, 2));
        return (float) distance / t;
    }

    private class Trajectory {

        List<Point> screenPoints;
        List<Point> transformedPoints;
        List<Point> realPoints;
        List<Float> speeds;
        List<Float> times;
        int realx;
        int realy;

        public Trajectory() {
            screenPoints = new ArrayList<>();
            transformedPoints = new ArrayList<>();
            realPoints = new ArrayList<>();
            speeds = new ArrayList<>();
            times = new ArrayList<>();
        }

        void addPoint(int x, int y) {
            Point screenPoint = new Point(x, y);
            screenPoints.add(screenPoint);
            Point transformedPoint = screen2transformed(x, y);
            transformedPoints.add(transformedPoint);
            Point realPoint = transformed2real(transformedPoint);
            realPoints.add(realPoint);
            realx = realPoint.x;
            realy = realPoint.y;
            int idx = realPoints.size() - 1;
            if (idx > 0) {
                addSpeed(realPoints.get(idx), realPoints.get(idx - 1));
                addTime(timestep);
            }
        }

        void addrealPoint(int x, int y) {
            Point realPoint = new Point(x, y);
            realPoints.add(realPoint);
            Point transformedPoint = real2transformed(realPoint);
            transformedPoints.add(transformedPoint);
            Point screenPoint = transformed2screen(transformedPoint);
            screenPoints.add(screenPoint);
            realx = realPoint.x;
            realy = realPoint.y;
            int idx = realPoints.size() - 1;
            if (idx > 0) {
                addSpeed(realPoints.get(idx), realPoints.get(idx - 1));
                addTime(timestep);
            }
        }

        // speed is the speed between present point and next point
        // so its index is 1 behind the Point index
        void addSpeed(Point thisPoint, Point previousPoint) {
            float aspeed = calculatespeed(thisPoint.x, thisPoint.y, previousPoint.x, previousPoint.y, timestep);
            speeds.add(aspeed);
        }

        // Time starts at zero
        void addTime(float t) {
            if (times.isEmpty()) {
                times.add(0.0f);
            }
            int idx = times.size() - 1;
            times.add(times.get(idx) + t);
        }
    }

    private class BuoyTrajectory extends Trajectory {

        void addrealPoint(MetOceanModel.MORecord previous, MetOceanModel.MORecord next) {
            if (previous != null) {
                timestep = next.javadate.getTime() - previous.javadate.getTime();
            }
            else {
                timestep = 0;
            }
            addrealPoint(next.position.x, next.position.y);
        }

        // speed is the speed between present point and next point
        // so its index is 1 behind the Point index
        @Override
        void addSpeed(Point thisPoint, Point previousPoint) {
            float aspeed = calculatespeed(thisPoint.x, thisPoint.y, previousPoint.x, previousPoint.y, timestep);
            speeds.add(aspeed);
        }

        // Time starts at zero
        @Override
        void addTime(float t) {
            if (times.isEmpty()) {
                times.add(0.0f);
            }
            int idx = times.size() - 1;
            times.add(times.get(idx) + t);
        }
    }

    private void loadMO() {
        mom = new MetOceanModel();
        netcdfreader = null;
//        TideModel tm = new TideModel();
//        agctc.updateTideModel(tm);
    }

    private void loadnetcdf() {
        try {
            netcdfreader = new NetcdfReader();
            mom = null;
        }
        catch (IOException | InvalidRangeException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
