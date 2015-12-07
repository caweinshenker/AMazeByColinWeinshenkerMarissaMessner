package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad;

import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Robot.Direction;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Robot.Turn;

/**
 * Wall follower maze solver
 * Extends ManualDriver and implements RobotDriver
 * Wall follower reaches the exit of the maze by following its lefthand wall until the exit is visible
 * If the exit is visible, Wall follower moves toward it
 * If the robot spawns in a room, it adjusts until it has a wall on its left
 * @author caweinshenker
 * @author mamessner
 *
 */
public class WallFollower extends ManualDriver implements RobotDriver {
	
	private boolean finishedInitialRoom = false;

	@Override
	public boolean drive2Exit() throws Exception {
		// Get the robot to a left wall if it starts in the middle of a room
		if (robby.isInsideRoom() && (robby.distanceToObstacle(Direction.LEFT) != 0)
				&& !finishedInitialRoom) {
			if (robby.distanceToObstacle(Direction.RIGHT) == 0) {
				robby.rotate(Turn.AROUND);
			}
			else {
				if (robby.distanceToObstacle(Direction.FORWARD) != 0) {
					robby.move(1);
				}
				
			}
		}
		else if (!robby.isAtGoal()){
			finishedInitialRoom = true;
			if (((robby.distanceToObstacle(Direction.LEFT) == 0) && 
			   (robby.distanceToObstacle(Direction.FORWARD) == 0)) && (robby.distanceToObstacle(Direction.RIGHT) == 0)){
				robby.rotate(Turn.AROUND);
			}
			else if ((robby.distanceToObstacle(Direction.LEFT) == 0) && (robby.distanceToObstacle(Direction.FORWARD) == 0)){
				robby.rotate(Turn.RIGHT);
			}
			else if (robby.distanceToObstacle(Direction.LEFT) != 0){
				robby.rotate(Turn.LEFT);
			}
			robby.move(1);
			pathLength++;
		}
		else if ((robby.isAtGoal()) && !(robby.canSeeGoal(Direction.FORWARD))){
			robby.rotate(Turn.LEFT);
		}
		else {
			robby.move(1);
		}
		return true;
	}

}
