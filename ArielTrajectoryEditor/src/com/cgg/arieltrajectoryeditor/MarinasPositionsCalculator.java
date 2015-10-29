package com.cgg.arieltrajectoryeditor;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculate the marinas positions
 * @author hdrira
 */
public class MarinasPositionsCalculator {

    
    /**
     * Standard distance between marinas in meters 
     */
    public static final int DISTANCE_BETWEEN_MARINAS = 125; //Metre
    
    /**
     * Standard duration between each step in seconds 
     */
    public static final int STEP_DURATION = 10;
     
    /**
     * Standard number of nodes per group
     */
    public static final int NODE_PER_GROUP = 4;
    
    /**
     * Standard number of nodes per line
     */
    public static final int NODE_PER_LINE = 4;
    
    /**
     * Standard Initial Time
     */
    public static final long INITIAL_TIME = 0;
    
    /**
     * Standard Source Speed
     */
     public static final Float SOURCE_SPEED = (float) 200;
     
     /**
     * Standard Distance minimum without changing direction : 8km
     */
    final int MIN_SOURCE_STEP_DSITANCE = 1000;
            
    // Trajectory points
    final private List<Point> trajectoryPoints;
    
    // Speeds between trajectory points
    final private List<Float> trajectorySpeeds;
    
    // Target points
    final private List<Point> targetZonePoints;
    
    // Marinas  points to be calculated 
    final private List<Map<Integer,Node>> marinasPositions;
    
    //Source Marinas  points to be calculated 
    private List<Node> sourcePositions;
    
    //Source speed
    final private Float sourceSpeed;
    
    //Distance minimum without changing direction
    final int minSourceStepDsitance;
    
    // Target points to be calculated 
    final private List<Point> targetPoints;
    
    // Number of nodes per group
    final private int nbNodePerGroup;
            
    // Number of nodes per line
    final private int nbNodeperLine;
    
    // Duration between each step
    final private int marinaStepDuration;
    
    // Distance between marinas
    final private int marinasDistance;
    
    // Initial Time
    final private long initialTime;
    
    // Number of marina step during one Source Step
    final private int nbMarinaPostionsToSkip;
    
    // One source step duration
    final private int sourceStepDuration;
    
    //first postion flag : at hte first moment we have to choice the source direction between 0° and 360°, then between -45, 0 and 45° added to previous direction
    private boolean firstPositionFlag = true;
    
    
    /**
     * Calculate the marinas positions 
     * DISTANCE_BETWEEN_MARINAS = 125 
     * STEP_DURATION = 10 
     * NODE_PER_GROUP = 4
     * NODE_PER_LINE = 4
     * @param trajectoryPoints Trajectory points
     * @param trajectorySpeeds  Speeds between trajectory points
     * @param targetPoints Points define target zone 
     */
    public MarinasPositionsCalculator(List<Point> trajectoryPoints, List<Float> trajectorySpeeds, List<Point> targetPoints) {
        this.trajectoryPoints = trajectoryPoints;
        this.trajectorySpeeds = trajectorySpeeds;
        this.targetZonePoints = targetPoints;
        this.marinasPositions = new ArrayList<>();
        this.sourcePositions = new ArrayList<>();
        this.targetPoints = targetPoints;
        this.sourceSpeed = SOURCE_SPEED;
        this.nbNodePerGroup = NODE_PER_GROUP;
        this.nbNodeperLine = NODE_PER_LINE;
        this.marinaStepDuration = STEP_DURATION;
        this.marinasDistance = DISTANCE_BETWEEN_MARINAS;
        this.initialTime = INITIAL_TIME;
        this.minSourceStepDsitance = MIN_SOURCE_STEP_DSITANCE;
        // Because source have to make 8 km at least without changing direction (in one step). we caculate number of marina step during one Source Step
        this.sourceStepDuration = (new Double(minSourceStepDsitance / sourceSpeed)).intValue() * 1000;
        this.nbMarinaPostionsToSkip = (new Double(sourceStepDuration / marinaStepDuration)).intValue();
    }
    
    /**
     * Calculate the marinas positions. 
     * Number of marinas = nbNodePerGroup * nbNodeperLine
     * @param trajectoryPoints Trajectory points
     * @param trajectorySpeeds Speeds between trajectory points
     * @param targetPoints Points define target zone 
     * @param sourceSpeed Source Speed 
     * @param minSourceStepDsitance minimum Source Step Distance without changing direction
     * @param nbNodePerGroup  Number of nodes per group
     * @param nbNodeperLine Number of nodes per line
     * @param stepDuration Duration between each step
     * @param marinasDistance Distance between marinas
     * @param initialTime Initial Time
     */
    public MarinasPositionsCalculator(List<Point> trajectoryPoints, List<Float> trajectorySpeeds, List<Point> targetPoints, float sourceSpeed , int minSourceStepDsitance ,int nbNodePerGroup, int nbNodeperLine, int stepDuration, int marinasDistance, long initialTime) {
        this.trajectoryPoints = trajectoryPoints;
        this.trajectorySpeeds = trajectorySpeeds;
        this.targetZonePoints = targetPoints;
        this.marinasPositions = new ArrayList<>();
        this.sourcePositions = new ArrayList<>();
        this.targetPoints = targetPoints;
        this.sourceSpeed = sourceSpeed;
        this.nbNodePerGroup = nbNodePerGroup;
        this.nbNodeperLine = nbNodeperLine;
        this.marinaStepDuration = stepDuration;
        this.marinasDistance = marinasDistance;
        this.initialTime = initialTime;
        this.minSourceStepDsitance = minSourceStepDsitance;
        // Because source have to make 8 km at least without changing direction (in one step). we caculate number of marina step during one Source Step
        // Speed in cm/s and distance in metre => we multiply the distance by 100
        this.sourceStepDuration = (new Double(minSourceStepDsitance / sourceSpeed)).intValue() * 100;
        this.nbMarinaPostionsToSkip = (new Double(sourceStepDuration / marinaStepDuration)).intValue();
    }
    
    /**
     *
     * @return the number of marinas
     */
    public int getNbMarinas(){
        return nbNodePerGroup * nbNodeperLine;
    }
    

    /**
     * Calculate Marinas First positions for each step
     * @param refPoint current step trajectory Point
     * @param firstTime current step initial time
     * @return 
     */
    public Map<Integer,Node> initFirstMarinasPositions(Point refPoint, long firstTime){
        // Transitional Trajectory Marinas points : aloow tio record first Marina postion for each step
        Map<Integer,Node>  firstPoints = new HashMap<>();
        // formula calculating x coordinate using the number of marina per line
        Double referenceX = refPoint.getX() - (((nbNodePerGroup-1) * 0.5) * marinasDistance);
        // formula calculating y coordinate using the number of marina lines
        Double referenceY = refPoint.getY() - (((nbNodeperLine-1) * 0.5) * marinasDistance);

        for(int i = 0 ; i < nbNodePerGroup ; i++){
            for(int j = 0; j < nbNodeperLine; j++){
                int x = referenceX.intValue()+ (i * marinasDistance);
                int y = referenceY.intValue()+ (j * marinasDistance);
                Point newPoint = new Point(x,y);
                Node newNode = new Node(newPoint, firstTime);
                firstPoints.put(i + j * (nbNodeperLine), newNode);
            }
        }
        
        return firstPoints;
    }
    
    /**
     * Calculate source positions
     * Requirement : Marina position already calculated
     */
    public void calculateSourcePostions(){
        
        
        // Get target zone central point
        Point targetCentralPoint = getCentralPoint(targetPoints);
        
        // Init SourceTrajectory
        // Calculate first point in source trajectory with central symmetry
        Point firstMarinaPosition = marinasPositions.get(0).get(0).getCoodinates();
        Point firstSourcePosition = centralSymmetryPoint(firstMarinaPosition, targetCentralPoint);
        Node  firstSourceNode = new Node(firstSourcePosition, marinasPositions.get(0).get(0).getTime());
        List<Node> bestSourcePositions = new ArrayList<>();
        bestSourcePositions.add(firstSourceNode);
        List<Integer> nbSourcePositionsInTarget  = new ArrayList<>();
        nbSourcePositionsInTarget.add(1);
        int firstAngle = 0;
        SourceTrajectory sourceTrajectory = new SourceTrajectory(bestSourcePositions, nbSourcePositionsInTarget,firstAngle);
        
        
        // Calculate the remaining source positions: base on marines coordiantes already calculated
        int firstIndexMarinaPostion = 0;
        sourceTrajectory = bestSourceTrajectory(firstIndexMarinaPostion, sourceTrajectory);
        
        sourcePositions = sourceTrajectory.getSourcePositions();
    }
    
    /**
     * Retrieve best source trajectory : with max central point in the target zone 
     * Check the 8km vectors with variable directions : 0, 45', 90', 135', 180' :
     * @param currentMarinaPostionIndex
     * @param sourceTrajectory
     * @return 
     */
    SourceTrajectory bestSourceTrajectory(int currentMarinaPostionIndex, SourceTrajectory sourceTrajectory ){
        
        // Source possible positions and the number of the related central points int the arget zone
        List<SourceTrajectory> sourceTrajectoryCandidates = new ArrayList<>();
        
        int i = currentMarinaPostionIndex;
        while(i< marinasPositions.size()){
            
            //Current angle of source Trajectory
            int previousAngle = sourceTrajectory.getAngle();
            //Current Source Node
            Node currentSourceNode = sourceTrajectory.getSourcePositions().get(sourceTrajectory.getSourcePositions().size()-1);
            
            if(firstPositionFlag){
                firstPositionFlag = false;
                for(int angle = 0; angle < 360; angle = angle + 45){
                    SourceTrajectory sourceTrajectoryCandidate = copySourceTrajectory(sourceTrajectory);
                    //Calculate next source and marina positions and check if if the centralPoint between their is in the target zone
                    Double xToAddByStep = minSourceStepDsitance * Math.cos(angle);
                    Double yToAddByStep = minSourceStepDsitance * Math.sin(angle);

                    Point nextSourcePosition = new Point(currentSourceNode.getCoodinates().x + xToAddByStep.intValue() ,currentSourceNode.getCoodinates().y + yToAddByStep.intValue());

                    // Calculate number of points is in the target zone 
                    int nbCentralPointsInTargetZone = nbPointsInTargetZone(currentMarinaPostionIndex, nbMarinaPostionsToSkip, currentSourceNode.getCoodinates(), nextSourcePosition);

                    // Save the source position and the related number of central points int the arget zone
                    Node nextSourceNode = new Node(nextSourcePosition, currentSourceNode.getTime() + sourceStepDuration);
                    
                    sourceTrajectoryCandidate.getSourcePositions().add(nextSourceNode);
                    sourceTrajectoryCandidate.getNbSourcePositionsInTarget().add(nbCentralPointsInTargetZone);
                    //SourceTrajectory bestSourceTrajectory = bestSourceTrajectory(currentMarinaPostionIndex + nbMarinaPostionsToSkip ,sourceTrajectoryCandidate);
                    sourceTrajectoryCandidates.add(sourceTrajectoryCandidate);
                }
            }else{
                for(int angle = -90; angle < 91 ; angle = angle + 45){
                    SourceTrajectory sourceTrajectoryCandidate = copySourceTrajectory(sourceTrajectory);
                    //Calculate next source and marina positions and check if if the centralPoint between their is in the target zone
                    Double xToAddByStep = minSourceStepDsitance * Math.cos(previousAngle + angle);
                    Double yToAddByStep = minSourceStepDsitance * Math.sin(previousAngle + angle);

                    Point nextSourcePosition = new Point(currentSourceNode.getCoodinates().x + xToAddByStep.intValue() ,currentSourceNode.getCoodinates().y + yToAddByStep.intValue());

                    // Calculate number of points is in the target zone 
                    int nbCentralPointsInTargetZone = nbPointsInTargetZone(currentMarinaPostionIndex, nbMarinaPostionsToSkip, currentSourceNode.getCoodinates(), nextSourcePosition);

                    // Save the source position and the related number of central points int the arget zone
                    Node nextSourceNode = new Node(nextSourcePosition, currentSourceNode.getTime() + sourceStepDuration);
                    
                    sourceTrajectoryCandidate.getSourcePositions().add(nextSourceNode);
                    sourceTrajectoryCandidate.getNbSourcePositionsInTarget().add(nbCentralPointsInTargetZone);
                    //SourceTrajectory bestSourceTrajectory = bestSourceTrajectory(currentMarinaPostionIndex + nbMarinaPostionsToSkip ,sourceTrajectory);
                    sourceTrajectoryCandidates.add(sourceTrajectoryCandidate);
                }
            }
            
            
            // select the best source trajectory with maximum points in the target zone
            int nbMax = 0;
            int indiceMax = 0;
            for(int j = 0; j< sourceTrajectoryCandidates.size(); j++){
                int totalNbSourcePositionsInTarget = sourceTrajectoryCandidates.get(j).getTotalNbSourcePositionsInTarget();
                 if(totalNbSourcePositionsInTarget > nbMax){
                     nbMax = totalNbSourcePositionsInTarget;
                     indiceMax = j;
                 }
            }
            
            sourceTrajectory.setSourcePositions(sourceTrajectoryCandidates.get(indiceMax).getSourcePositions());
            sourceTrajectory.setNbSourcePositionsInTarget(sourceTrajectoryCandidates.get(indiceMax).getNbSourcePositionsInTarget());
            
            // Final thing : next marina will be the be the current for the next step
            i= i + nbMarinaPostionsToSkip;
            sourceTrajectoryCandidates.clear();
        }
        
        return sourceTrajectory;
    }
    
    /**
     * Calculate Marinas positions
     */
    public void calculateMarinasPostions(){
        
        // Calculate the remaining marinas transitional positions
        if(trajectoryPoints.size() >= 2){
            int i = 0;
            //Calculate Marinas initial positions
            Map<Integer,Node>  firstPoints = initFirstMarinasPositions(trajectoryPoints.get(0), initialTime);
            
            while(i+1 < trajectoryPoints.size()){
                Point p1 = trajectoryPoints.get(i);
                Point p2 = trajectoryPoints.get(i+1);
                Float speed = trajectorySpeeds.get(i);
                
                //Calculate the angle between the two points
                double xDiff = p2.x - p1.x; 
                double yDiff = p2.y - p1.y; 
                double angle = Math.atan2(yDiff, xDiff);
                
                //Calculate the number of transitional point between each segment
                double distance = Math.sqrt(Math.pow((p2.getX() - p1.getX()), 2) + Math.pow((p2.getY() - p1.getY()), 2));
                double step_distance = (marinaStepDuration * speed)/1000;
                Double xToAddByStep = step_distance * Math.cos(angle);
                Double yToAddByStep = step_distance * Math.sin(angle);
                
                //Then calculateMarinasPostions their positions
                Double nbOfTransitionalPoints = Math.floor(distance / step_distance);
                
                
                for(int p=0;p<nbOfTransitionalPoints;p++){
                
                    Double xToAdd = xToAddByStep * p;
                    Double yToAdd = yToAddByStep * p;
                    
                    // Marinas positions of the new step
                    Map<Integer,Node> newMarinasPositions = new HashMap<>();
                    
                    for(int j=0; j<getNbMarinas(); j++){
                        Node firstStepNode = firstPoints.get(j);
                        Point newPoint = new Point(firstStepNode.getCoodinates().x + xToAdd.intValue() ,firstStepNode.getCoodinates().y + yToAdd.intValue());
                        Node newNode = new Node(newPoint, firstStepNode.getTime() + (marinaStepDuration * p));
                        
                        newMarinasPositions.put(j, newNode);
                    }
                    
                    marinasPositions.add(newMarinasPositions);
                }
                // init first Marina postion for next step
                firstPoints = initFirstMarinasPositions(p2, firstPoints.get(0).getTime() + (marinaStepDuration * nbOfTransitionalPoints.longValue()));
                i++;
            }
        }
        
    }

    /**
     * 
     * @return the list of trajectory points
     */
    public List<Point> getTrajectoryPoints() {
        return trajectoryPoints;
    }

    /**
     * 
     * @return the list of marinas positions
     */
    public List<Map<Integer, Node>> getMarinasPoints() {
        calculateMarinasPostions();
        return marinasPositions;
    }
    
    public List<Node> getSourcePositions() {
        if(marinasPositions.isEmpty()){
            calculateMarinasPostions();
        }
        
        calculateSourcePostions();
        return sourcePositions;
    }
            
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Point p1 = new Point(127197, 3082138);
        Point p2 = new Point(127227,3081824);
        Point p3 = new Point(127740, 3081763);
        Point p4 = new Point(127771,3082128);
        //Point p3 = new Point(2000,0);
        
        List<Point> trajectoryPoints = new ArrayList<>();
        trajectoryPoints.add(p1);
        trajectoryPoints.add(p2);
        trajectoryPoints.add(p3);
        trajectoryPoints.add(p4);
        //trajectoryPoints.add(p3);
        
        List<Float> trajectorySpeeds = new ArrayList<>();
        trajectorySpeeds.add(25.0f);
        trajectorySpeeds.add(25.0f);
        trajectorySpeeds.add(25.0f);
        trajectorySpeeds.add(25.0f);
        //trajectorySpeeds.add(0.0f);
        
        int nbNodePerGroup = 4;
        int nbNodeperLine = 4;
        int stepDuration = 10;
        int marinasDistance = 125;
        long initialTime = 120;
        float sourceSpeed = 200;
        int minSourceStepDsitance = 1000;
        
        
        List<Point> targetPoints = new ArrayList<>();
        Point tp1 = new Point(130630, 3075250);
        Point tp2 = new Point(132297,3077148);
        targetPoints.add(tp1);
        targetPoints.add(tp2);
        
        MarinasPositionsCalculator marinasPositionsCalculator = new MarinasPositionsCalculator(trajectoryPoints, trajectorySpeeds, targetPoints, sourceSpeed, minSourceStepDsitance, nbNodePerGroup, nbNodeperLine, stepDuration, marinasDistance, initialTime);
        
        List<Map<Integer, Node>> marinasPositions = marinasPositionsCalculator.getMarinasPoints();
        
        List<Node> sourcesPostions = marinasPositionsCalculator.getSourcePositions();
        
        //// TESTER LES POSITIONS 
        /*
        int i = 0;
        for(Map<Integer, Node> mpi: marinasPositions){
            System.out.println(" *********************** Step number : " +(i+1) + " ***********************");
            
            // number of marinas
            int nbrMarinas = nbNodePerGroup * nbNodeperLine;
            
            for(int j = 0; j < nbrMarinas; j++){
                Node node = mpi.get(j);
                System.out.println("Marina no " + j +" : (x,y) = (" + node.getCoodinates().getX() + "," + node.getCoodinates().getY() +") : Time = " + node.getTime()) ;
            }
            i++;
        }
        */
        
        // TESTE LA VISTESSE
//        for(int k = 0 ; k< marinasPositionsCalculator.getNbMarinas(); k++){
//            for(int l =0; l<marinasPositions.size()-1;l++){
//                Map<Integer, Node> mpi1 = marinasPositions.get(l);
//                Map<Integer, Node> mpi2 = marinasPositions.get(l+1);
//                
//                Node node1 = mpi1.get(k);
//                Node node2 = mpi2.get(k);
//                
//                double distance = Math.sqrt(Math.pow((node2.getCoodinates().getX() - node1.getCoodinates().getX()), 2) + Math.pow((node2.getCoodinates().getY() - node1.getCoodinates().getY()), 2));
//                
//                double vitesseN1ToN2 = distance / ((double)stepDuration);
//                
//                System.out.println("Marina "+ k +" Step "+ l + " To Step " + (l+1)+ " - distance: " + distance +" - Vitesse : " + vitesseN1ToN2 + " : P1 = " +node1.getCoodinates()+ " : P2 = " +node2.getCoodinates());
//            }
//        }
        
        // TEST DES SOUCES POSITIONS
        System.out.println("*********************************************************************************************");
        System.out.println("*********************************************************************************************");
        for(int k = 0 ; k< sourcesPostions.size(); k++){
            System.out.println("Source "+ k + " : Coodinates = " +sourcesPostions.get(k).getCoodinates());
        }
        
        // TESTE CREATION DE FICHER SEPARER POUR CHAQUE MARINAS
        /*
        System.out.println("*********************************************************************************************");
        System.out.println("*********************************************************************************************");
        for(int k = 0 ; k< marinasPositionsCalculator.getNbMarinas(); k++){
            for(int l =0; l<marinasPositions.size();l++){
                Map<Integer, Node> mpi = marinasPositions.get(l);
                Node node = mpi.get(k);
                System.out.println("Marina "+ k +" Step "+ l  + " : Coodinates = " +node.getCoodinates());
            }
        }
        */
        
        // Run the interpolation
        /*
        for (int i = 0; i < marinasPositionsCalculator.getNbMarinas(); i++) {
            System.out.println("********************************************Marina numero " + i +" *************************************************");
            for (Map m : marinasPositions) {
                Node n = (Node) m.get(i);
                //ps.printf("%.1f, %.1f, %.1f,,,,,,\n", (double) n.getTime(), (float) n.getCoodinates().x, (float) n.getCoodinates().y);
                System.out.println("Time : " + n.getTime() + " - Coordinates : " + n.getCoodinates());
        }
        }
        */
        
        
        
    }
    
    
    /**
     * Calculate central point in the target zone
     * @param targetPoints
     * @return 
     */
    private Point getCentralPoint(List<Point> targetPoints) {
        
        Point p1 = targetPoints.get(0);
        Point p2 = targetPoints.get(1);
        
        int width = Math.abs(p1.x - p2.x);
        int height = Math.abs(p1.y - p2.y);
        
        Rectangle target = new Rectangle(p1.x, p1.y, width, height);
        return new Point(new Double(target.getCenterX()).intValue(), new Double(target.getCenterY()).intValue());
        
    }
    
        /**
     * Check if the point is inside the target zone
     * @param point the point to check
     * @return
     */
    private boolean isInsideTarget(Point point){
        if(targetZonePoints!=null){
            return true;
        }else{
            return false;
        }
    }

    /**
     * make a central Symmetry
     * @param point first point
     * @param centralPoint central point
     * @return Symmetry Point 
     */
    private Point centralSymmetryPoint(Point firstPoint, Point centralPoint) {
        return new Point((2 * centralPoint.x) - firstPoint.x,(2 * centralPoint.y) - firstPoint.y);
    }

    /**
     * Calculate the position of the middle point between two pont
     * @param nextMarinaPosition first point
     * @param nextSourcePosition second point
     * @return centralPoint
     */
    private Point centralPoint(Point p1, Point p2) {
        return new Point((p1.x + p2.x)/2 , (p1.y + p2.y)/2);
    }

    /**
     * Check central point is in the target zone
     * @param nextTargetPosition
     * @return true if the point is the target zone
     */
    private boolean isInTargetZone(Point pointToCheck) {
        Point p1 = targetPoints.get(0);
        Point p2 = targetPoints.get(1);
        
        int width = Math.abs(p1.x - p2.y);
        int height = Math.abs(p1.y - p2.y);
        
        Rectangle target = new Rectangle(p1.x, p1.y, width, height);
        
        return target.contains(pointToCheck);
        
    }
    
    /**
     * Calculate number of points is in the target zone 
     * @param currentMarinaPosition Current Marina Position
     * @param nextMarinaPosition Next Marina Position
     * @param currentSourcePosition Current Source Position
     * @param nextSourcePosition Next Source Position
     * @return number of points is in the target zone 
     */
    private int nbPointsInTargetZone(int currentMarinaPostionIndex, int nbMarinaPostionsToSkip, Point currentSourcePosition, Point nextSourcePosition) {
        
        // Prepare marina points
        List<Point> oneStepMarinaPoints = new ArrayList<>();
        int k= 0;
        while(k < nbMarinaPostionsToSkip && currentMarinaPostionIndex + k < marinasPositions.size()){
            oneStepMarinaPoints.add(marinasPositions.get(currentMarinaPostionIndex + k).get(0).getCoodinates());
            k++;
        }
        
        // Prepare source points
        List<Point> oneStepSourcePoints = new ArrayList<>();
        //Calculate the angle between the two points
        double xDiff = nextSourcePosition.x - currentSourcePosition.x; 
        double yDiff = nextSourcePosition.y - currentSourcePosition.y; 
        double angle = Math.atan2(yDiff, xDiff);
        Double xToAddByStep = xDiff / oneStepMarinaPoints.size() ;
        Double yToAddByStep = yDiff / oneStepMarinaPoints.size() ;
        for(int i= 0; i < oneStepMarinaPoints.size();i++){
            Double x =  currentSourcePosition.getX()+ xToAddByStep;
            Double y =  currentSourcePosition.getY()+ yToAddByStep;
            Point sourcePoint = new Point(x.intValue(), y.intValue());
            oneStepSourcePoints.add(sourcePoint);
        }
        
        // Check number of central points in the target zone
        int nbPointsInTargetZone = 0;
        for(int i= 0; i < oneStepMarinaPoints.size();i++){
            Point centralPoint = centralPoint(oneStepMarinaPoints.get(i), oneStepSourcePoints.get(i));
            if(isInTargetZone(centralPoint)){
                nbPointsInTargetZone++;
            }
        }
         return nbPointsInTargetZone;
    }
    
    /**
     * Cooy source Trajectory
     * @param sourceTrajectory
     * @return copy of the original sourceTrajectory
     */
    private SourceTrajectory copySourceTrajectory(SourceTrajectory sourceTrajectory){
        
        List<Node> copySourcePositions = new ArrayList<>(); 
        for(Node node:sourceTrajectory.getSourcePositions()){
            copySourcePositions.add(node);
        }
        
        List<Integer> copyNbSourcePositionsInTarget = new ArrayList<>(); 
        for(Integer nbSourcePositionsInTarget:sourceTrajectory.getNbSourcePositionsInTarget()){
            copyNbSourcePositionsInTarget.add(nbSourcePositionsInTarget);
        }
	
        SourceTrajectory copySourceTrajectory = new SourceTrajectory(copySourcePositions,copyNbSourcePositionsInTarget, sourceTrajectory.getAngle());
        
        return copySourceTrajectory;
    }

    
}
