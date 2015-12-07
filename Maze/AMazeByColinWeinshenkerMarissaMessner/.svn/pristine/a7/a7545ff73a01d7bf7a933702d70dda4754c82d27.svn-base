package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad;

import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Robot.Turn;

/**
 * This class implements a manual driver that feeds SimpleKeyListener input to a BasicRobot
 * 
 * Unlike the default first-person navigation mode, the manual driver does not allow the user to move backward.
 * He/she must turn around and step forward instead.
 * The manual driver also maintains the energy consumption of its robot and the path length from the starting position
 * to the current position. This information is then passed to the exit screen through the robot's maze object.
 * 
 * As ManualDriver implements the RobotDriver interface, it must have a drive2exit() method. However, this method does nothing
 * for this class, as all moves are determined by user input instead of an automated solver.
 *
 * Collaborators: SimpleKeyListener, Robot 
 * @author cweinshenker
 * @author mamessner
 */
public class ManualDriver implements RobotDriver {
	
	protected Robot robby;
	protected int width;
	protected int height;
	protected Distance distance;
	protected float initialEnergy;
	protected int pathLength;
	
	@Override
	public void setRobot(Robot r) {
		robby = r;
		initialEnergy = robby.getBatteryLevel();
	}

	@Override
	public void setDimensions(int width, int height) {
		this.width = width;
		this.height = height;

	}

	@Override
	public void setDistance(Distance distance) {
		this.distance = distance;

	}

	@Override
	public boolean drive2Exit() throws Exception {
		return false;
	}

	@Override
	public float getEnergyConsumption() {
		return initialEnergy - robby.getBatteryLevel();
	}

	@Override
	public int getPathLength() {
		return pathLength;
	}
	
	/**
	 * Move the robot forward one space and update the path length
	 * @throws Exception
	 */
	public void moveForward() throws Exception{
		robby.move(1);
		pathLength++;
	}
	
	/**
	 * Rotate the robot 90 degrees counterclockwise 
	 * @throws Exception
	 */
	public void rotateLeft() throws Exception{
		robby.rotate(Turn.LEFT);
	}
	
	/**
	 * Rotate the robot 90 degrees clockwise
	 * @throws Exception
	 */
	public void rotateRight() throws Exception{
		robby.rotate(Turn.RIGHT);
	}
	
	/**
	 * Rotate the robot 180 degrees and move forward one cell
	 * @throws Exception
	 */
	public void turnAround() throws Exception{
		robby.rotate(Turn.AROUND);
		robby.move(1);
		pathLength++;
	}

}
