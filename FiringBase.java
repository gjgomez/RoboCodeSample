package R2R;
import robocode.*;
import java.util.*;
import robocode.util.*;
import java.awt.geom.Point2D;
/**
 * MyClass - a class by (your name here)
 */
public class FiringBase
{

	protected double getBulletPower(AdvancedRobot sourceRobot, TargetData targetData)
	{
		double distance = targetData.getDistance();
		double power;
		if (distance > 300 || (sourceRobot.getEnergy() < 40 && distance > 100))
		{
			power = 1;
		}
		else if (distance < 50)
		{
			power = 3;
		}
		else
		{
			power = 3 - ((distance-50) * .008);  // Provides a linear increase of power from 1 to 3 as the distance goes from 299 to 51
		}

		//System.out.print("Distance: " + distance + " Power: " + power + "\n");
		return power;							
	}

														
}
