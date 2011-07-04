package R2R;
import robocode.*;
import java.awt.*;
import java.util.*;
import robocode.util.*;
import java.awt.geom.Point2D;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * Mb1 - a robot by (your name here)
 */
public class Mb2 extends AdvancedRobot
{
	private long timeSinceLastScan;
	private PatternTracker _tracker = new PatternTracker();
	private int _radarDirection=1;
	private HashMap<String,TargetRobot> _targetMap=new HashMap<String,TargetRobot>();
	private double _minDistanceToEnemy = 450;
	private double _minWallDistance = 100;
	int _moveDirection = 1;
	int _moveSeqCount = 0;
	int _moveSeqTurnCount = 0;
	double _moveSeqHeading;
	double _moveSeqTurnIncr;
	
	
	/**
	 * run: Mb1's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.blue,Color.red,Color.yellow); // body,gun,radar

		addCustomEvent(new RadarTurnCompleteCondition(this));
		addCustomEvent(new Condition("close_to_walls") {
			public boolean test() {
				return (
					// we're too close to the left wall
					(getX() <= _minWallDistance ||
					 // or we're too close to the right wall
					 getX() >= getBattleFieldWidth() - _minWallDistance ||
					 // or we're too close to the bottom wall
					 getY() <= _minWallDistance ||
					 // or we're too close to the top wall
					 getY() >= getBattleFieldHeight() - _minWallDistance)
					);
				}
			});
				
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		setTurnRadarRight(360);
 
		// Robot main loop
		while(true) {
			execute();
		}
	}

	private void doMove(TargetRobot targetRobot) {
		
		if (_moveSeqCount > 0)
		{
			_moveSeqCount--;
			if (_moveSeqTurnCount > 0)
			{
				_moveSeqTurnCount--;
				setTurnRight(_moveSeqTurnIncr);
			}
			setAhead(8 * _moveDirection);
			return;
		}
		// TODO Auto-generated method stub
		double targetBearing = targetRobot.getCurrentTargetData().getBearing();
		double targetDistance = targetRobot.getCurrentTargetData().getDistance();
		double targetHeading = targetRobot.getCurrentTargetData().getHeading();
		
//		if (targetDistance <= _minDistanceToEnemy)
//			_moveDirection *= -1;
		
		/*
		System.out.println("Bearing: " + targetBearing);
		System.out.println("Heading: " + targetHeading);
		System.out.println("Distance: " + targetDistance);
		System.out.println("Direction: " + _moveDirection);
		*/
		//setTurnRight(_targetRobot.getCurrentTargetData().getBearing() + 120);
//		if (targetDistance <= 100)
//			setTurnRight(targetBearing + 90 - (10 * moveDirection));
//		else if (targetDistance <= 200)
//			setTurnRight(targetBearing - 60);
//		else if (targetDistance <= 300)
//			setTurnRight(targetBearing - 30);
//		else if (targetDistance <= 400)
//			setTurnRight(targetBearing + 90);
//		else if (targetDistance <= 500)
//			setTurnRight(targetBearing + 110);
//		else if (targetDistance <= 600)
//			setTurnRight(targetBearing + 130);
//		else
//			setTurnRight(targetBearing + 140);
			
		
		if (targetDistance <= _minDistanceToEnemy)
		{
			setTurnRight(targetBearing - 90 - (10 * _moveDirection));
			//System.out.println("Within distance ");
		}
		else
		{
			setTurnRight(targetBearing + 90 - (10 * _moveDirection));
			//System.out.println("Not Within distance ");
		}		
		
		setAhead(8 * _moveDirection);			
		
		//System.out.println("My bearing: " + getHeading());
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		boolean foundRobot = false;
		Iterator iterator = _targetMap.values().
    		iterator();

	  	while(iterator.hasNext()) {
    		TargetRobot tRobot = (TargetRobot)iterator.next();		
			if (tRobot.getName() == e.getName())
			{
				tRobot.recordScanEvent(e, calcTargetLocation(e));
				foundRobot = true;
				//System.out.print("found robot " + tmp.getName() + "\n");
			}
		}
		
		if (!foundRobot)
		{
			TargetRobot tRobot = new TargetRobot(e.getName());
			tRobot.recordScanEvent(e, calcTargetLocation(e));
			_targetMap.put(tRobot.getName(), tRobot);
		}
		
		iterator = _targetMap.values().iterator();
		//System.out.print("Has found robot: " + iterator.hasNext() + "\n");
		if (iterator.hasNext())
		{
			TargetRobot target = (TargetRobot)iterator.next();
			_tracker.feedData(this, target);
			//LinearPredictiveFiring firing = new LinearPredictiveFiring();
			//firing.performFiringLogic(this, target);
			PatternPredictiveFiring firing = new PatternPredictiveFiring(_tracker);
			firing.performFiringLogic(this, target);
			doMove(target);
		}
		
	}

	public void onCustomEvent(CustomEvent e) {
	  	if (e.getCondition() instanceof RadarTurnCompleteCondition) 
			performScan();
			
	  	if (e.getCondition().getName().equals("close_to_walls"))
		{
			// switch directions and move away	
	  		System.out.println("WALL!!");
	
			double newHeading = calcWallAvoidanceHeading();
			if (_moveSeqHeading != newHeading || _moveSeqCount == 0)
			{
				_moveSeqHeading = newHeading;
				double bearing = calcWallAvoidanceBearing(newHeading);
				_moveSeqTurnIncr = bearing / 5;
				_moveSeqTurnCount = 4;
				setTurnRight(_moveSeqTurnIncr);
	
				_moveDirection = 1;
				_moveSeqCount = 20;
		  		ahead(8 * _moveDirection);
			}
			else
			{
				System.out.println("Wall avoidance - Heading: " + getHeading() + ", New Heading: " + 
					_moveSeqHeading + "\n");
			}
		}
			
	}

	/**
	 * onHitWall: What to do when you hit a wall
	 */

	public void onHitWall(HitWallEvent e) {
		System.out.println("Hit wall");
		double newHeading = calcWallAvoidanceHeading();
		_moveSeqHeading = newHeading;
		double bearing = calcWallAvoidanceBearing(newHeading);
		_moveSeqTurnIncr = 0;
		setTurnRight(bearing);
		_moveSeqCount = 20;
		ahead(8 * _moveDirection);
	}

	private Point2D.Double calcTargetLocation(ScannedRobotEvent scanEvent)
	{
		double angle = scanEvent.getBearingRadians() + this.getHeadingRadians();
		
		double cos = Math.cos(angle);
		double targetY = (cos * scanEvent.getDistance()) + this.getY();
		
		double sin = Math.sin(angle);
		double targetX = (sin * scanEvent.getDistance()) + this.getX();
		
		return new Point2D.Double(targetX, targetY);
	}
 
	private double calcWallAvoidanceHeading()
	{
		double robotX = getX();
		double robotY = getY();
		double newHeading;
		if (robotX < _minWallDistance * 2)
		{
			if (robotY < _minWallDistance * 2)
				newHeading = 45;
			else if (robotY > (getBattleFieldHeight() - (_minWallDistance * 2)))
				newHeading = 135;
			else
				newHeading = 90;
		}
		else if (robotX > (getBattleFieldWidth() - (_minWallDistance * 2)))
		{
			if (robotY < _minWallDistance * 2)
				newHeading = 315;
			else if (robotY > (getBattleFieldHeight() - (_minWallDistance * 2)))
				newHeading = 225;
			else
				newHeading = 270;
		}
		else if (robotY < _minWallDistance * 2)
			newHeading = 0;
		else
			newHeading = 180;
			
		return newHeading;		
	}

	private double calcWallAvoidanceBearing(double newHeading)
	{
		double turnDegrees = newHeading - getHeading();
		if (Math.abs(turnDegrees) > 180)
			turnDegrees = (Math.abs(turnDegrees) - 360) * sign(turnDegrees);
		System.out.println("Wall avoidance - Heading: " + getHeading() + ", New Heading: " + 
			newHeading + ", Turn Degrees: " + turnDegrees + "\n");

		return turnDegrees;				
	}			

	private void performScan() {
  		double maxBearingAbs=0, maxBearing=0, minDistance=1000;
  		int scannedBots=0;
  		Iterator iterator = _targetMap.values().iterator();

  		while(iterator.hasNext()) 
		{
    		TargetRobot tRobot = (TargetRobot)iterator.next();
			TargetData tData = tRobot.getCurrentTargetData();

    		if (tRobot!=null && tRobot.isUpdated(this.getTime())) 
			{
      			double bearing = Utils.normalRelativeAngleDegrees
        			(getHeading() + tData.getBearing() - getRadarHeading());
      			if (Math.abs(bearing)>maxBearingAbs) 
				{ 
        			maxBearingAbs = Math.abs(bearing); 
        			maxBearing = bearing; 
      			}

				double distance = tData.getDistance();
				if (minDistance > distance)
					minDistance = distance;
				
      			scannedBots++;
    		}
  		}

  		double radarTurn=180*_radarDirection;
  		if (scannedBots==getOthers()) 
		{
			//System.out.print("Min Distance: "+minDistance+"\n");
					
			if (minDistance < 250)
			{
				radarTurn=maxBearing+sign(maxBearing)*20; 
			}
			else
				radarTurn=maxBearing+sign(maxBearing)*10; 
		}	

  		setTurnRadarRight(radarTurn);
  		_radarDirection=sign(radarTurn);

	}

	private int sign(double n)
	{
		if (n > 0)
			return 1;
		else
			return -1;
	} 
}


