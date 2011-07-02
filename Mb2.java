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
	
	/**
	 * run: Mb1's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.blue,Color.red,Color.yellow); // body,gun,radar

		addCustomEvent(new RadarTurnCompleteCondition(this));
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		setTurnRadarRight(360);
 
		// Robot main loop
		while(true) {
			execute();
		}
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
		}
		
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		setBack(10);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		setBack(-40);
	}	

	public void onCustomEvent(CustomEvent e) {
	  	if (e.getCondition() instanceof RadarTurnCompleteCondition) 
			performScan();
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
				radarTurn=maxBearing+sign(maxBearing)*12; 
			}
			else
				radarTurn=maxBearing+sign(maxBearing)*1; 
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

/*
public class CircularIntercept extends Intercept {
	protected Coordinate getEstimatedPosition(double time) {
  		if (Math.abs(angularVelocity_rad_per_sec) <= Math.toRadians(0.1)) {
  			return super.getEstimatedPosition(time);
 		}

    	double initialTargetHeading = Math.toRadians(targetHeading);
    	double finalTargetHeading   = initialTargetHeading +  
     		angularVelocity_rad_per_sec * time;
    	double x = targetStartingPoint.x - targetVelocity /
     		angularVelocity_rad_per_sec *(Math.cos(finalTargetHeading) - 
     		Math.cos(initialTargetHeading));
    	double y = targetStartingPoint.y - targetVelocity / 
     		angularVelocity_rad_per_sec *
     		(Math.sin(initialTargetHeading) - 
     		Math.sin(finalTargetHeading));

    	return new Coordinate(x,y);
	}
}
*/




     
}


