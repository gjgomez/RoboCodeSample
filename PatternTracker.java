package R2R;
import robocode.*;
import java.util.*;
import robocode.util.*;
import java.awt.geom.Point2D;
/**
 * MyClass - a class by (your name here)
 */
public class PatternTracker
{
    static final long MOVIE_LENGTH = 7000;
    static final char BREAK_KEY = (char)0;
    static final int NO_BEARING = -1000;

    static StringBuffer pattern = new StringBuffer((int)MOVIE_LENGTH);
    static ArrayList<Frame> movie = new ArrayList<Frame>((int)MOVIE_LENGTH);
    static boolean movieIsFull = false;
    static long movieSize = 0;

	int searches;
	double _targetHeading;
	long _time;

	public PatternTracker()
	{
		recordBreak(1);
	}
	
	public void feedData(AdvancedRobot sourceRobot, TargetRobot targetRobot)
	{
		TargetData targetData = targetRobot.getCurrentTargetData();
		
		double headingDelta = Math.toRadians(targetData.getHeading()) - _targetHeading;
		_targetHeading += headingDelta;
		
        long timeDelta = sourceRobot.getTime() - _time;
        _time += timeDelta;
		
		record(Utils.normalRelativeAngle(headingDelta), targetData.getVelocity(), timeDelta);

	}

    public double calcProjectedBearing(AdvancedRobot sourceRobot, TargetRobot targetRobot, double bulletPower, long index) {
		Point2D.Double sourceLocation = new Point2D.Double(sourceRobot.getX(), sourceRobot.getY());
		TargetData targetData = targetRobot.getCurrentTargetData();
        double newX = targetData.getLocation().getX();
        double newY = targetData.getLocation().getY();
		double bulletVelocity = 20 - 3 * bulletPower;

        long travelTime = 0;
        long bulletTravelTime = 0;
        if (index > 0) {
            double frameHeading = _targetHeading;
            for (int i = (int)index; i < movieSize && travelTime <= bulletTravelTime; i++) {
                Frame frame = (Frame)movie.get(i);
				double deltaX = Math.sin(frameHeading) * frame.getVelocity();
				double deltaY = Math.cos(frameHeading) * frame.getVelocity();
                newX += deltaX;
                newY += deltaY;
                bulletTravelTime = (long)(sourceLocation.distance(newX, newY) / bulletVelocity);
                travelTime++;
				frameHeading += frame.getHeadingDelta();
            }
        }
        return absoluteBearing(sourceLocation, new Point2D.Double(newX, newY));
    }

    private double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    public long findSimilarPeriodEndIndex(AdvancedRobot sourceRobot, TargetData targetData, double bulletPower) {
        long index = -1;
        long matchIndex = -1;
        long matchLength = 0;
        long maxTryLength = Math.min(sourceRobot.getTime(), 600);
		double bulletVelocity = 20 - 3 * bulletPower;
		long maxBulletTravelTime = calcMaxBulletTravelTime(targetData.getDistance(), bulletVelocity);
        searches++;
        if (maxTryLength > 1 && movieSize > maxTryLength + 1 + maxBulletTravelTime) {
            long patternLength = movieSize - maxBulletTravelTime;
            String patternString = pattern.substring(0, (int)patternLength);
            String searchString  = pattern.substring((int)(movieSize - maxTryLength));
            long tryLength = maxTryLength;
            long upper = maxTryLength;
            do {
                boolean foundMatch = false;
                if (_time > 600) {
                    index = patternString.lastIndexOf(searchString.substring((int)(maxTryLength - tryLength)));
                }
                else {
                    index = patternString.indexOf(searchString.substring((int)(maxTryLength - tryLength)));
                }
                if (index >= 0) {
                    long endIndex = index + tryLength;
                    if (patternLength > endIndex + maxBulletTravelTime + 1 && 
                            patternString.substring((int)endIndex,
                                (int)(endIndex + maxBulletTravelTime + 1)).indexOf(BREAK_KEY) < 0) {
                        foundMatch = true;
                        matchIndex = index;
                        matchLength = tryLength;
                        if (tryLength == maxTryLength) {
                            break;
                        }
                        tryLength += (upper - tryLength) / 2;
                    }
                }
                if (!foundMatch) {
                    upper = tryLength;
                    tryLength -= (tryLength - matchLength) / 2;
                }
            }
            while (tryLength > 1 && tryLength < upper - 1);
        }
        if (matchIndex >= 0) {
            return matchIndex + matchLength;
        }
        return matchIndex;
    }


    long calcMaxBulletTravelTime(double enemyDistance, double bulletVelocity) 
	{
        return (long)(enemyDistance * 1.3 / bulletVelocity);
    }

    void record(double eHeadingDelta, double eVelocity, long timeDelta) 
	{
        eHeadingDelta /= timeDelta;
        eVelocity /= timeDelta;
        for (int i = 0; i < timeDelta; i++) {
            record(new Frame(eHeadingDelta, eVelocity));
        }
    }

    void recordBreak(long timeDelta) 
	{
        Frame breakFrame = new BreakFrame();
        for (int i = 0; i < timeDelta; i++) {
            record(breakFrame);
        }
    }

    void record(Frame frame) 
	{
        movie.add(frame);
        pattern.append((char)(frame.getKey()));
        if (movieIsFull) {
            pattern.deleteCharAt(0);
            movie.remove(0);
        }
        else {
            movieSize++;
            movieIsFull = movieSize >= MOVIE_LENGTH;
        }
    }


	// Nested Classes
	class Frame 
	{
	    double headingDelta;
	    double velocity;

	    Frame() 
		{
	    }

	    Frame(double headingDelta, double velocity) 
		{
	        this.headingDelta = headingDelta;
	        this.velocity = velocity;
	    }

	    char getKey() 
		{
	        int key = 3;
	        key = key + 11 * (int)((10.0 + Math.toDegrees(headingDelta)) * 3);
	        key = key + (int)((8.0 + velocity));
			return (char)(key);
    	}

		double getVelocity()
		{
			return velocity;
		}
	
		double getHeadingDelta()
		{
			return headingDelta;
		}
	}

	
	class BreakFrame extends Frame 
	{
	    char getKey() 
		{
	        return BREAK_KEY;
	    }

	    double deltaX() 
		{
	        return 0;
	    }

	    double deltaY() 
		{
	        return 0;
	    }
	}

}
	