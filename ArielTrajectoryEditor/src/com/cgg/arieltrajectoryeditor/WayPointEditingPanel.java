/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arieltrajectoryeditor;

import com.cgg.arielgeometry.model.io.UKOOAwriter;
import com.cgg.arielgeometrycurrents.MetOceanReader;
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
import java.util.Date;
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

/**
 *
 * @author jgrimsdale
 */
public class WayPointEditingPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

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
    long starttime = 0;
    // Marina swarm paramters
    int nbNodePerGroup = 4;
    int nbNodeperLine = 4;
    int stepDuration = 10;
    int marinasDistance = 125;
    boolean writingmarinatrajectories = false;
    boolean writingsourcetrajectories = false;
    JButton jb4;
    JButton jb5;
    JButton jb6;
    JTextField jtf1;
    Color defaultfg;
    Color defaultbg;
    Stroke widestroke;

    public WayPointEditingPanel() {
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

        jb6 = new JButton("Load Buoy");
        jb6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadbuoy();
            }
        });
        add(jb6);

        JLabel jl1 = new JLabel("Start Time = ");
        jl1.setOpaque(true);
        jl1.setBackground(Color.LIGHT_GRAY);
        add(jl1);

        jtf1 = new JTextField();
        jtf1.setText(Long.toString(starttime));
        jtf1.setToolTipText("Hit Enter key to confirm");
        jtf1.setPreferredSize(new Dimension(50, 27));
        jtf1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                starttime = Long.parseLong(jtf1.getText());
                System.out.println("Start time changed to " + starttime + " s");
            }
        });
        add(jtf1);

        JLabel jl2 = new JLabel("Speed in cm/s = ");
        jl2.setOpaque(true);
        jl2.setBackground(Color.LIGHT_GRAY);
        add(jl2);

        final JTextField jtf2 = new JTextField();
        jtf2.setText(Integer.toString(speed));
        jtf2.setToolTipText("Hit Enter key to confirm");
        jtf2.setPreferredSize(new Dimension(50, 27));
        jtf2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speed = Integer.parseInt(jtf2.getText());
                System.out.println("Speed changed to " + speed + " cm/s");
            }
        });
        add(jtf2);

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
            Point thispoint = screen2real(pointList.get(idx));
            Point previouspoint = screen2real(pointList.get(idx - 1));
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
        float flt = timeList.get(idx);
        int timeinsecs = (int) flt;
        int hours = timeinsecs / 3600;
        int minutes = (timeinsecs % 3600) / 60;
        int secs = timeinsecs % 60;
        return String.format("%d:%d:%d", hours, minutes, secs);
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

    private Point real2transformed(Point p) {
        int x = (int) ((p.x - xorigin) / xscale) + xoffset;
        int y = (int) ((yorigin - p.y) / yscale) + yoffset;
        return new Point(x, y);
    }

    private Point real2screen(Point p) {
        Point transformed = real2transformed(p);
        return transformed2screen(transformed.x, transformed.y);
    }

    private Point transformed2screen(int x, int y) {
        return new Point((int) ((itlx + x) * scale), (int) ((itly + y) / scale));
    }

    private Point transformed2screen(Point p) {
        return transformed2screen(p.x, p.y);
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

    private void writeshots2UKOOA(String directory) {
        if (pointList.isEmpty()) {
            System.out.println("No points clicked yet");
            return;
        }
        UKOOAwriter writer = new UKOOAwriter(directory);
        writer.writeheader();
        int shotnb = 1;
        Point realPoint;
        for (Point p : pointList) {
            realPoint = screen2real(p);
            System.out.println("Point = " + realPoint);
            writer.writeshot(shotnb++, (double) realPoint.x, (double) realPoint.y);
        }
        writer.close();
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
                Logger.getLogger(WayPointEditingPanel.class.getName()).log(Level.SEVERE, null, ex);
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
        MarinasPositionsCalculator mpc = new MarinasPositionsCalculator(realpointList,
                speedList, nbNodePerGroup, nbNodeperLine, stepDuration, marinasDistance, starttime);
        List<Map<Integer, Node>> lmin = mpc.getMarinasPoints();

        // Output each Marina to a separate file in chosen directory
        for (int i = 0; i < mpc.getNbMarinas(); i++) {
            PrintStream ps = getps("Marina", i, od);
            writetrajectories(ps, lmin, i);
        }
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
                Logger.getLogger(WayPointEditingPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String outputdirectory = od.getAbsolutePath();
        System.out.println("Writing to directory " + outputdirectory);

        // Print waypoints to stdout
        if (debug) {
            printpoints();
        }

        writeshots2UKOOA(outputdirectory);

        // Prepare list of real points
        List<Point> realpointList = new ArrayList<>();
        for (Point p : pointList) {
            realpointList.add(screen2real(p));
        }

        // Run the interpolation
        MarinasPositionsCalculator mpc = new MarinasPositionsCalculator(realpointList,
                speedList, 1, 1, stepDuration, marinasDistance, starttime);
        List<Map<Integer, Node>> lmin = mpc.getMarinasPoints();

        // Output each shot to a separate file in chosen directory
        for (int i = 0; i < mpc.getNbMarinas(); i++) {
            PrintStream ps = getps("Source", i, od);
            writetrajectories(ps, lmin, i);
        }
        System.out.println("Writing sources complete");
    }

    private void writetrajectories(PrintStream ps, List<Map<Integer, Node>> lmin, int i) {
        ps.println("time,x,y,depth,nominal_WH,propulsion_WH,communication_WH,total_WH,duration_estimation_DAYS");
        for (Map m : lmin) {
            Node n = (Node) m.get(i);
            ps.printf("%.1f, %.1f, %.1f,,,,,,\n", (double) n.getTime(), (float) n.getCoodinates().x, (float) n.getCoodinates().y);
        }
        ps.close();
    }

    // Prints the marina trajectories for debug purposes
    private void printmarinatrajectories() {
        // Prepare list of real points
        List<Point> realpointList = new ArrayList<>();
        for (Point p : pointList) {
            realpointList.add(screen2real(p));
        }

        // Run the interpolation
        MarinasPositionsCalculator mpc = new MarinasPositionsCalculator(realpointList,
                speedList, nbNodePerGroup, nbNodeperLine, stepDuration, marinasDistance, starttime);
        List<Map<Integer, Node>> lmin = mpc.getMarinasPoints();

        // Print results
        for (Map m : lmin) {
            for (int i = 0; i < mpc.getNbMarinas(); i++) {
                Node n = (Node) m.get(i);
                System.out.println("Node = " + i + " t = " + n.getTime() + " x= " + n.getCoodinates().x + " y= " + n.getCoodinates().y);
            }
        }
    }

    private PrintStream getps(String name, int index, File directory) {
        String filename = name + index + ".csv";
        File f = new File(directory, filename);
        PrintStream ps;
        try {
            ps = new PrintStream(f);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(WayPointEditingPanel.class.getName()).log(Level.SEVERE, null, ex);
            ps = null;
        }
        return ps;
    }

    private void loadbuoy() {
        JFileChooser jfc = new JFileChooser("C:\\Users\\jgrimsdale\\Desktop");
        jfc.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
        jfc.setDialogTitle("Select Buoy trajectory file");
        jfc.showOpenDialog(this);
        File file = jfc.getSelectedFile();
        MetOceanReader mor = new MetOceanReader(file);
        Date startdate = null;
        while (mor.hasNextRecord()) {
            if (startdate == null) {
                startdate = mor.javadate;
            }
            Point p = real2screen(new Point((int) mor.x, (int) mor.y));
            speed = (int) (100.0f * Math.hypot(mor.vx, mor.vy));
            addpoint(p.x, p.y);
        }
        if (startdate != null) {
            starttime = (startdate.getTime() / 1000) - 1440000000;
        }
        jtf1.setText(Long.toString(starttime));
        System.out.println("startime=" + starttime);
        repaint();
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
            jd.add(new CalibrateForm(this));
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
}
