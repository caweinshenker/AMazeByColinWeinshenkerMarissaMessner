package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad;

import java.util.Arrays;

import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Robot.Direction;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Robot.Turn;

/**
 * Wizard solver method
 * Extends manual driver and implements robot driver
 * Wizard knows the distance matrix and therefore the best path out of the maze
 * Should be able to access the maze's distance array and rank the distances to the exit for each legal move
 * From there, the wizard simply chooses the move that places it closest to the exit.
 * @author caweinshenker
 * @author mamessner
 */
public class Wizard extends ManualDriver implements RobotDriver {

	private int[] EAST = {1, 0};
	private int[] WEST = {-1, 0};
	private int[] NORTH = {0 , -1};
	private int[] SOUTH = {0, 1};

	@Override
	public boolean drive2Exit() throws Exception {
		if (!robby.isAtGoal()){
			int curX = robby.getCurrentPosition()[0];
			int curY = robby.getCurrentPosition()[1];
			int[] curDirection = robby.getCurrentDirection();
			int distanceArray[] = getDistanceArray(curDirection, curX, curY);
			int move = getMove(distanceArray);
			switch(move){
			case 0:
				robby.move(1);
				break;
			case 1:
				robby.rotate(Turn.RIGHT);
				robby.move(1);
				break;
			case 2:
				robby.rotate(Turn.LEFT);
				robby.move(1);
				break;
			case 3:
				robby.rotate(Turn.AROUND);
				robby.move(1);
				break;
			}
			pathLength++;
		}
		else if (!robby.canSeeGoal(Direction.FORWARD)){
			robby.rotate(Turn.LEFT);
		}
		else {
			robby.move(1);
		}
		return true;
	}
	
	/**
	 * Determine which direction the robot should move in to get closer to the exit.
	 * @param distanceArray an array of distance values for the adjacent cells
	 * @return an int (0-3) representing the direction
	 */
	protected int getMove(int[] distanceArray){
		int minDist = Integer.MAX_VALUE;
		int minIndex = 0;
		for (int i = 0; i < 4; i++){
			if (distanceArray[i] < minDist){
				switch(i){
				case 0:
					if ((robby.distanceToObstacle(Direction.FORWARD) != 0)){
						minDist = distanceArray[i];
						minIndex = i;
					}
					break;
				case 1:
					if (robby.distanceToObstacle(Direction.RIGHT) != 0){
						minDist = distanceArray[i];
						minIndex = i;
					}
					break;
				case 2:
					if ((robby.distanceToObstacle(Direction.LEFT) != 0)){
						minDist = distanceArray[i];
						minIndex = i;
					}
					break;
				case 3:
					if (robby.distanceToObstacle(Direction.BACKWARD) != 0){
						minDist = distanceArray[i];
						minIndex = i;
					}
					break;
				}
			}			
		}
		return minIndex;
	}
	
	/**
	 * Get the distance to the exit of each adjacent cell.
	 * @param curDirection the direction the robot is facing
	 * @param curX the current x position
	 * @param curY the current y position
	 * @return an array of distance values
	 */
	protected int[] getDistanceArray(int[] curDirection, int curX, int curY) {
		int forwardDist = Integer.MAX_VALUE;
		int rightDist = Integer.MAX_VALUE;
		int leftDist = Integer.MAX_VALUE;
		int backDist = Integer.MAX_VALUE;
		if (Arrays.equals(curDirection, EAST)) {
			if (curX + 1 < width){
				forwardDist = distance.getDistance(curX + 1, curY);
			}
			if (curY - 1 >= 0){
				rightDist = distance.getDistance(curX, curY - 1);
			}
			if (curY + 1 < height){
				leftDist = distance.getDistance(curX, curY + 1);
			}
			if (curX -1 >= 0){
				backDist = distance.getDistance(curX - 1, curY);
			}
		}
		else if (Arrays.equals(curDirection, WEST)) {
			if (curX - 1 >= 0){
				forwardDist = distance.getDistance(curX - 1, curY);
			}
			if (curY + 1 < height){
				rightDist = distance.getDistance(curX, curY + 1);
			}
			if (curY - 1 >= 0){
				leftDist = distance.getDistance(curX, curY - 1);
			}
			if (curX + 1 < width){
				backDist = distance.getDistance(curX + 1, curY);
			}
		}
		else if (Arrays.equals(curDirection, NORTH)) {
			if (curY + 1 < height){
				forwardDist = distance.getDistance(curX, curY + 1);
			}
			if (curX + 1 < width){
				rightDist = distance.getDistance(curX + 1, curY);
			}
			if (curX -1 >= 0){
				leftDist = distance.getDistance(curX - 1, curY);
			}
			if (curY - 1 >= 0){
				backDist = distance.getDistance(curX, curY - 1);
			}
		}
		else if (Arrays.equals(curDirection, SOUTH)) {
			if (curY - 1 >= 0){
				forwardDist = distance.getDistance(curX, curY - 1);
			}
			if (curX - 1 >= 0){
				rightDist = distance.getDistance(curX -1 , curY);
			}
			if (curX + 1 < width){
				leftDist = distance.getDistance(curX + 1, curY);
			}
			if (curY + 1 < height){
				backDist = distance.getDistance(curX, curY + 1);
			}
		}
		int[] array = {forwardDist, rightDist, leftDist, backDist};
		return array;
	}

}
