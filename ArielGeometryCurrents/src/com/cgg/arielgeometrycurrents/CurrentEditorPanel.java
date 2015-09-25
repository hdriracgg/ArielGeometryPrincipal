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
import java.awt.Rectangle;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author jgrimsdale
 */
public class CurrentEditorPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    NetcdfFile ncFile = null;
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
    float[] timearray;  // times = (time index in vtot)
    Variable vtot;      // contains x and y velocity vectors as a function of position, time and depth
    Variable vgrid3;    // contains grid node locations
    double cm2deg = 180.0 / (Math.PI * 637131500.0);
    double deg2cm = (Math.PI * 637131500.0) / 180.0;
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
    // Marina swarm paramters
    int nbNodePerGroup = 4;
    int nbNodeperLine = 4;
    int stepDuration = 10;
    int marinasDistance = 125;
    boolean writingmarinatrajectories = false;
    boolean writingsourcetrajectories = false;
    JButton jb4;
    JButton jb5;
    Color defaultfg;
    Color defaultbg;
    Stroke widestroke;

    public CurrentEditorPanel() {
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

//        JButton jb2 = new JButton("Print");
//        jb2.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                printpoints();
//                repaint();
//            }
//        });
//        add(jb2);

        JButton jb3 = new JButton("Calibrate");
        jb3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calibrate();
            }
        });
        add(jb3);

        jb4 = new JButton("Marinas");
        jb4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writingmarinatrajectories = true;
                repaint();
                writemarinatrajectories();
                writingmarinatrajectories = false;
                repaint();
            }
        });
        add(jb4);

        jb5 = new JButton("Sources");
        jb5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writingsourcetrajectories = true;
                repaint();
                writesourcetrajectories();
                writingsourcetrajectories = false;
                repaint();
            }
        });
        add(jb5);

        JLabel jl1 = new JLabel("Speed in cm/s = ");
        jl1.setOpaque(true);
        jl1.setBackground(Color.LIGHT_GRAY);
        add(jl1);

        final JTextField jtf1 = new JTextField();
        jtf1.setText(Integer.toString(speed));
        jtf1.setToolTipText("Hit Enter key to confirm");
        jtf1.setPreferredSize(new Dimension(50, 27));
        jtf1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speed = Integer.parseInt(jtf1.getText());
                System.out.println("Speed changed to " + speed + " cm/s");
            }
        });
        add(jtf1);

        pointList = new ArrayList<>();
        speedList = new ArrayList<>();
        timeList = new ArrayList<>();
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

    private void addpoint(int x, int y) {
        pointList.add(screen2transformed(x, y));
        speedList.add((float) speed);
        timeList.add(gettotaltime(pointList.size()));
    }

    private void recalculatetimes() {
        timeList = new ArrayList<>();
        for (int idx = 1; idx <= pointList.size(); idx++) {
            timeList.add(gettotaltime(idx));
        }
    }

    private void removepoint(int x, int y) {
        Point tmppoint = screen2transformed(x, y);
        Rectangle r = new Rectangle(tmppoint.x - 5, tmppoint.y - 5, 10, 10);
        Point pointtoremove = null;
        for (Point p : pointList) {
            if (r.contains(p)) {
                pointtoremove = p;
            }
        }
        int index = pointList.indexOf(pointtoremove);
        pointList.remove(index);
        speedList.remove(index);
        recalculatetimes();
    }

    private void movepoint(int x, int y, int tox, int toy) {
        Point tmporigin = screen2transformed(x, y);
        Point tmpdestination = screen2transformed(tox, toy);
        Rectangle r = new Rectangle(tmporigin.x - 5, tmporigin.y - 5, 10, 10);
        Point newpoint = new Point(tmpdestination.x, tmpdestination.y);
        for (Point p : pointList) {
            if (r.contains(p)) {
                int index = pointList.indexOf(p);
                pointList.set(index, newpoint);
                speedList.set(index, (float) speed);
                recalculatetimes();
            }
        }
    }

    private void clearpoints() {
        pointList = new ArrayList<>();
        speedList = new ArrayList<>();
        timeList = new ArrayList<>();
        marinapointList = null;
        marinaspeedList = null;
        marinatimeList = null;
        sourcepointList = null;
        sourcespeedList = null;
        sourcetimeList = null;
    }

    private void drawpoint(Graphics2D g, int x, int y) {
        g.fillOval(x - 2, y - 2, 5, 5);
    }

    private void drawline(Graphics2D g, Point p1, Point p2) {
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void movepoint(MouseEvent e) {
        int x = dragstartpoint.x;
        int y = dragstartpoint.y;
        int newx = e.getX();
        int newy = e.getY();
        movepoint(x, y, newx, newy);
        dragstartpoint = new Point(newx, newy);
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
        else {
            movepoint(e);
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
                addpoint(e.getX(), e.getY());
            }
            if (e.getButton() == MouseEvent.BUTTON3) {
                removepoint(e.getX(), e.getY());
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
        if (writingmarinatrajectories) {
            jb4.setBackground(Color.red);
            jb4.setForeground(Color.pink);
            jb4.setSelected(true);
        }
        jb5.setBackground(defaultbg);
        jb5.setForeground(defaultfg);
        jb5.setSelected(false);
        if (writingsourcetrajectories) {
            jb5.setBackground(Color.red);
            jb5.setForeground(Color.pink);
            jb5.setSelected(true);
        }
    }

    private void painttrajectories(Graphics2D g2) {
        Point previouspoint = null;
        float fontsize = (float) (11 / scale + 0.2);
        Font oldfont = g2.getFont();
        Font newfont = oldfont.deriveFont(fontsize);
        g2.setFont(newfont);
        for (Point p : pointList) {
            String s = getspeedfromp(p) + " " + gettimefromp(p);
            g2.drawString(s, 10 + p.x, p.y);
            drawpoint(g2, p.x, p.y);
            if (previouspoint != null) {
                drawline(g2, p, previouspoint);
            }
            previouspoint = p;
        }
        g2.setFont(oldfont);
    }

    private float gettotaltime(int size) {
        float t = 0.0f;
        int idx = size - 1;
        if (idx > 0) {
            Point thispoint = pointList.get(idx);
            Point previouspoint = pointList.get(idx - 1);
            double x2 = Math.pow(thispoint.x - previouspoint.x, 2.0);
            double y2 = Math.pow(thispoint.y - previouspoint.y, 2.0);
            float distance = (float) Math.sqrt(x2 + y2);
            float thisspeed = speedList.get(idx - 1);
            float thistime = distance / (thisspeed / 100);
            float previoustime = timeList.get(idx - 1);
            t = previoustime + thistime;
        }
        return t;
    }

    private String gettimefromp(Point p) {
        int idx = pointList.indexOf(p);
        return String.format("%.0fs", timeList.get(idx));
    }

    private String getspeedfromp(Point p) {
        int idx = pointList.indexOf(p);
        return String.format("%.0fcm/s", speedList.get(idx));
    }

    private Point screen2transformed(int x, int y) {
        return new Point((int) ((x / scale) - itlx), (int) ((y / scale) - itly));
    }

    private Point screen2real(Point p) {
        int x = xorigin + (int) (xscale * (p.x - xoffset));
        int y = yorigin - (int) (yscale * (p.y - yoffset));
        return new Point(x, y);
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
                System.out.println("Real point = " + screen2real(p));
            }
        }
        return pointList;
    }

    private void writemarinatrajectories() {
        // Choose and create directory if needed
        JFileChooser jfc = new JFileChooser("C:\\Users\\jgrimsdale\\Desktop");
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File od = null;
        while (od == null) {
            jfc.showSaveDialog(this);
            od = jfc.getSelectedFile();
            if (od == null) {
                System.out.println("Ordinary file exists with this name");
            }
        }
        if (!od.isDirectory()) {
            System.out.println("Create directory");
            try {
                Files.createDirectory(od.toPath());
            }
            catch (IOException ex) {
                Logger.getLogger(CurrentEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String outputdirectory = od.getAbsolutePath();
        System.out.println("Writing to directory " + outputdirectory);

        // Print waypoints to stdout
        if (debug) {
            printpoints();
        }

        // Prepare list of real points
        List<Point> realpointList = new ArrayList<>();
        for (Point p : pointList) {
            realpointList.add(screen2real(p));
        }

        // Test interpolation
        if (debug) {
            printmarinatrajectories();
        }

        // Run the interpolation
//        MarinasPositionsCalculator mpc = new MarinasPositionsCalculator(realpointList,
//                speedList, nbNodePerGroup, nbNodeperLine, stepDuration, marinasDistance);
//        List<Map<Integer, Node>> lmin = mpc.getMarinasPoints();

        // Output each Marina to a separate file in chosen directory
//        for (int i = 0; i < mpc.getNbMarinas(); i++) {
//            PrintStream ps = getps("Marina", i, od);
//            writetrajectories(ps, lmin, i);
//        }
        marinapointList = pointList;
        marinaspeedList = speedList;
        marinatimeList = timeList;
        pointList = new ArrayList<>();
        speedList = new ArrayList<>();
        timeList = new ArrayList<>();
        System.out.println("Writing Marinas complete");
    }

    private void writesourcetrajectories() {
        // Choose and create directory if needed
        JFileChooser jfc = new JFileChooser("C:\\Users\\jgrimsdale\\Desktop");
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File od = null;
        while (od == null) {
            jfc.showSaveDialog(this);
            od = jfc.getSelectedFile();
            if (od == null) {
                System.out.println("Ordinary file exists with this name");
            }
        }
        if (!od.isDirectory()) {
            System.out.println("Create directory");
            try {
                Files.createDirectory(od.toPath());
            }
            catch (IOException ex) {
                Logger.getLogger(CurrentEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String outputdirectory = od.getAbsolutePath();
        System.out.println("Writing to directory " + outputdirectory);

        // Print waypoints to stdout
        if (debug) {
            printpoints();
        }

        // Prepare list of real points
        List<Point> realpointList = new ArrayList<>();
        for (Point p : pointList) {
            realpointList.add(screen2real(p));
        }

        // Run the interpolation
//        MarinasPositionsCalculator mpc = new MarinasPositionsCalculator(realpointList,
//                speedList, 1, 1, stepDuration, marinasDistance);
//        List<Map<Integer, Node>> lmin = mpc.getMarinasPoints();

        // Output each shot to a separate file in chosen directory
//        for (int i = 0; i < mpc.getNbMarinas(); i++) {
//            PrintStream ps = getps("Source", i, od);
//            writetrajectories(ps, lmin, i);
//        }
//        System.out.println("Writing sources complete");
    }

//    private void writetrajectories(PrintStream ps, List<Map<Integer, Node>> lmin, int i) {
//        ps.println("time,x,y,depth,nominal_WH,propulsion_WH,communication_WH,total_WH,duration_estimation_DAYS");
//        for (Map m : lmin) {
//            Node n = (Node) m.get(i);
//            ps.printf("%.1f, %.1f, %.1f,,,,,,\n", (double) n.getTime(), (float) n.getCoodinates().x, (float) n.getCoodinates().y);
//        }
//        ps.close();
//    }

    // Prints the marina trajectories for debug purposes
    private void printmarinatrajectories() {
        // Prepare list of real points
        List<Point> realpointList = new ArrayList<>();
        for (Point p : pointList) {
            realpointList.add(screen2real(p));
        }

        // Run the interpolation
//        MarinasPositionsCalculator mpc = new MarinasPositionsCalculator(realpointList,
//                speedList, nbNodePerGroup, nbNodeperLine, stepDuration, marinasDistance);
//        List<Map<Integer, Node>> lmin = mpc.getMarinasPoints();

        // Print results
//        for (Map m : lmin) {
//            for (int i = 0; i < mpc.getNbMarinas(); i++) {
//                Node n = (Node) m.get(i);
//                System.out.println("Node = " + i + " t = " + n.getTime() + " x= " + n.getCoodinates().x + " y= " + n.getCoodinates().y);
//            }
//        }
    }

    private PrintStream getps(String name, int index, File directory) {
        String filename = name + index + ".csv";
        File f = new File(directory, filename);
        PrintStream ps;
        try {
            ps = new PrintStream(f);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(CurrentEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            ps = null;
        }
        return ps;
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

    void dowork() throws IOException, InvalidRangeException {
//        JFileChooser jfc = new JFileChooser();
//        jfc.showOpenDialog(jfc);
//        String filename = jfc.getSelectedFile().getAbsolutePath();
        try {
            ncFile = NetcdfFile.open("C:\\Users\\jgrimsdale\\Documents\\Ariel\\Currents\\MIT\\MIT_redsea_uvw_20min_geo_201412010000_201412011200.nc");
        }
        catch (IOException ex) {
            System.out.println("Can't open netCDF file: ");
        }
        System.out.println("ncFile " + ncFile);
        vgrid3 = ncFile.findVariable("vgrid3");
        vtot = ncFile.findVariable("vtot");
        Variable dxt = ncFile.findVariable("dxt");
        Variable dyt = ncFile.findVariable("dyt");
        Variable rlngd = ncFile.findVariable("rlngd");
        Variable rlatd = ncFile.findVariable("rlatd");
        Variable delx = ncFile.findVariable("delx");
        Variable dely = ncFile.findVariable("dely");
        Variable thetad = ncFile.findVariable("thetad");
        Variable outlev = ncFile.findVariable("outlev");
        Variable time = ncFile.findVariable("time");
        Variable coord = ncFile.findVariable("coord");


        System.out.println("vgrid3 = " + vgrid3);
        System.out.println("vtot = " + vtot);

        rlatdval = rlatd.readScalarFloat();
        rlngdval = rlngd.readScalarFloat();
        delxval = delx.readScalarFloat() / 100.0f;
        delyval = dely.readScalarFloat() / 100.0f;
        thetadval = thetad.readScalarFloat();
        coordval = coord.readScalarInt();
        System.out.println("rlatdval=" + rlatdval + " rlngdval=" + rlngdval + " delxval = " + delxval + " delyval=" + delyval + " thetadval=" + thetadval);

        Array dxtarray = dxt.read();
        float[] dxt1d = (float[]) dxtarray.copyTo1DJavaArray();
        gridxval = (float) (dxt1d[0] / 100.0f);
        Array dytarray = dyt.read();
        float[] dyt1d = (float[]) dytarray.copyTo1DJavaArray();
        gridyval = (float) (dyt1d[0] / 100.0f);
        System.out.println("gridxval=" + gridxval + " gridyval=" + gridyval);

        // outlev
        deptharray = get1dint(outlev);
//        for (int i = 0; i < deptharray.length; i++) {
//            System.out.println("depth " + i + " =" + deptharray[i]);
//        }

        // time
        timearray = get1dfloat(time);
//        for (int i = 0; i < timearray.length; i++) {
//            System.out.println("time " + i + " =" + timearray[i]);
//        }

        // store vtot dimension of lon and lat
        int[] vtotshape = vtot.getShape();
        nxval = vtotshape[1];
        nyval = vtotshape[2];

        testll2xy();

        // get some results
        float x0 = (float) (35.5f * deg2cm);
        float y0 = (float) (27.4f * deg2cm);
        float[] vxy;
        for (int i = 0; i < 10; i++) {
            float xm = x0 / 100.0f + i * 10.0f;
            float ym = y0 / 100.0f + i * 10.0f;
            float xdeg = (float) (xm * 100.0f * cm2deg);
            float ydeg = (float) (ym * 100.0f * cm2deg);
            vxy = getvv(xdeg, ydeg, 5000.0f, 50.0f);
            System.out.println("xdeg=" + xdeg + " ydeg=" + ydeg + "xm=" + xm + " ym=" + ym + " vxy=" + vxy[0] + "," + vxy[1]);
        }
    }

    // get a velocity vector from lon, lat, time, depth
    float[] getvv(float lon, float lat, float time, float depth) throws IOException, InvalidRangeException {
        float[] result = new float[]{0.0f, 0.0f};
        float[][][][] varray;
        // get indexes
        int xy[] = ll2xy(lon, lat);
        int x = xy[0];
        int y = xy[1];
        int t = gettimeindex(time);
        int d = getdepthindex(depth);
        if (t != previoustimeindex) {
            varray = getvelocityarrayfortimeindex(t);
        }
        else {
            varray = previous4darray;
        }
        result[0] = varray[x][y][d][0];
        result[1] = varray[x][y][d][1];
        previous4darray = varray;
        previoustimeindex = t;
        return result;
    }

    // get a 4d java array of velocities for a given time index
    float[][][][] getvelocityarrayfortimeindex(int t) throws IOException, InvalidRangeException {
        int[] shape = vtot.getShape();
        int[] origin = new int[]{t, 0, 0, 0, 0};
        int[] size = new int[]{1, shape[1], shape[2], shape[3], shape[4]};
        Array data5d = vtot.read(origin, size);
        ArrayFloat.D4 data4d = (ArrayFloat.D4) data5d.reduce();
        float[][][][] v = (float[][][][]) data4d.copyToNDJavaArray();
        return v;
    }

    // get depth index from a depth in metres
    int getdepthindex(float depth) {
        int d = (int) (depth * -1.0f);
        for (int i = 0; i < deptharray.length; i++) {
            if (d < deptharray[i]);
            return i;
        }
        System.out.println("Out of depth range " + depth);
        return -1;
    }

    // get time index from time
    int gettimeindex(int time) {
        int t = (int) time;
        for (int i = 0; i < timearray.length; i++) {
            if (t > timearray[i]);
            return i;
        }
        return -1;
    }

    int gettimeindex(float time) {
        return gettimeindex((int) time);
    }

    // convert lon, lat to x and y indices
    int[] ll2xy(float lon, float lat) {
        int coord = coordval;
        int nx = nxval;
        int ny = nyval;
        float gridx = gridxval;
        float gridy = gridyval;
        float rlngd = rlngdval;
        float rlatd = rlatdval;
        float delx = delxval;
        float dely = delyval;
        float thetad = thetadval;
        int[] result = new int[]{0, 0};
        double deg2rad = Math.PI / 180.0;
        double xc = ((float) nx + 1.0f) / 2.0f - (delx / gridx);
        double yc = ((float) ny + 1.0f) / 2.0f - (dely / gridy);

        if (coord != 0) {
            System.out.println("Error: Coordinate system of other type than 0 are not supported");
            return result;
        }

        double re_d2r = 6371315.0 * deg2rad;
        double f1x = re_d2r / gridx;
        double f1y = re_d2r / gridy;
        double f2x = f1x * Math.cos(rlatd * deg2rad);
        double f2y = f1y * Math.cos(rlatd * deg2rad);
        double sterm = Math.sin(thetad * deg2rad);
        double cterm = Math.cos(thetad * deg2rad);
        double dlat = lat - rlatd;
        double dlon = lon - rlngd;
        int x = (int) (xc + f1x * dlat * sterm + f2x * dlon * cterm);
        int y = (int) (yc + f1y * dlat * cterm - f2y * dlon * sterm);
        x = x - 1;
        y = y - 1;
        if (x < 0) {
            System.out.println("ll2xy x out of range - return 0 x = " + x);
            x = 0;
        }
        if (x >= nx) {
            System.out.println("ll2xy x out of range - return nx-1 x=" + x + " nx=" + nx);
            x = nx - 1;
        }
        if (y < 0) {
            System.out.println("ll2xy y out of range - return 0 y = " + y);
            y = 0;
        }
        if (y >= ny) {
            System.out.println("ll2xy y out of range - return ny-1 y=" + y + " ny=" + ny);
            y = ny - 1;
        }
        result[0] = x;
        result[1] = y;
        return result;
    }

    // test ll2xy
    void testll2xy() throws IOException, InvalidRangeException {
        // vgrid3
        float[][][][] array4d = get4dfloat(vgrid3);
//        int[] shape = vgrid3.getShape();
//        for (int i = 0; i < shape[0]; i++) {
//            for (int j = 0; j < shape[1]; j++) {
//                System.out.println("i=" + i + " j=" + j + " value=" + " array4d=" + array4d[i][j][0][0]);
//            }
//        }
        System.out.println("[0][0]=" + array4d[0][0][0][0] + "," + array4d[0][0][0][1]);
        System.out.println("[0][625]=" + array4d[0][625][0][0] + "," + array4d[0][625][0][1]);
        System.out.println("[625][0]=" + array4d[625][0][0][0] + "," + array4d[625][0][0][1]);
        System.out.println("[625][625]=" + array4d[625][625][0][0] + "," + array4d[625][625][0][1]);
        int[] l0l0 = ll2xy((float) 35.43578, (float) 27.310062);
        int[] l625l0 = ll2xy((float) 35.38007, (float) 27.39573);
        int[] l0l625 = ll2xy((float) 35.532272, (float) 27.359522);
        int[] l625l625 = ll2xy((float) 35.476562, (float) 27.445189);
        System.out.println("l0l0 =" + l0l0[0] + "," + l0l0[1]);
        System.out.println("l625l0 =" + l625l0[0] + "," + l625l0[1]);
        System.out.println("l0l625 =" + l0l625[0] + "," + l0l625[1]);
        System.out.println("l625l625 =" + l625l625[0] + "," + l625l625[1]);
    }

    int[] get1dint(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0};
        int[] shape = v.getShape();
        ArrayInt.D1 array1d = (ArrayInt.D1) v.read(origin, shape);
        int[] javaarray = (int[]) array1d.copyToNDJavaArray();
        return javaarray;
    }

    float[] get1dfloat(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0};
        int[] shape = v.getShape();
        ArrayFloat.D1 array1d = (ArrayFloat.D1) v.read(origin, shape);
        float[] javaarray = (float[]) array1d.copyToNDJavaArray();
        return javaarray;
    }

    float[][] get2dfloat(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0, 0};
        int[] shape = v.getShape();
        ArrayFloat.D2 array2d = (ArrayFloat.D2) v.read(origin, shape);
        float[][] javaarray = (float[][]) array2d.copyToNDJavaArray();
        return javaarray;
    }

    float[][][][] get4dfloat(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0, 0, 0, 0};
        int[] shape = v.getShape();
        ArrayFloat.D4 array4d = (ArrayFloat.D4) v.read(origin, shape);
        float[][][][] javaarray = (float[][][][]) array4d.copyToNDJavaArray();
        return javaarray;
    }

    float[][][][][] get5dfloat(Variable v) throws IOException, InvalidRangeException {
        int[] origin = new int[]{0, 0, 0, 0, 0};
        int[] shape = v.getShape();
        ArrayFloat.D5 array5d = (ArrayFloat.D5) v.read(origin, shape);
        float[][][][][] javaarray = (float[][][][][]) array5d.copyToNDJavaArray();
        return javaarray;
    }
}
