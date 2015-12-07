package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad;
/**
 * This class implements the Robot interface
 * It receives instructions implements methods that act on a Maze object
 * 
 * This class should be able to receive input from a key listener as well as pathfinding algorithms in order to move the first-person
 * perspective of the maze solver around the maze.
 * To deal with the inconsistency between the representations of the maze in Cells and the GUI, a heading enum is introduced.
 * Headings represent the robot's current direction in terms of cardinal directions (from the top-down perspective) instead of dx and dy. This makes it easier to translate
 * between what needs to be done in cells and what needs to be done with the GUI. 
 * Headings also make detecting the distance to an obstacle much easier, as the number of cases that need to be evaluated is much smaller
 * when thinking in terms of cardinal directions.
 * 
 * Basic Robot should always have all its sensors.
 * 
 * 
 * Collaborators: a Maze and a RobotDriver that moves the robot through the maze
 *  
 * @author cweinshenker
 * @author mamessner
 *
 */
public class BasicRobot implements Robot {
	private static final int FORWARD_ENERGY = 5;
	private static final int ROTATE_ENERGY = 3;
	private static final int SENSE_ENERGY = 1;
	private Maze mz;
	private int x;
	private int y;
	private int exitX;
	private int exitY;
	private boolean stopped = false;
	private float batteryLevel;
	protected enum Heading {NORTH, EAST, SOUTH, WEST;
		protected Heading getRightDirection() {
			return values()[(ordinal() + 1) % values().length];
		}
		protected Heading getLeftDirection() {
			return values()[(ordinal() + 3) % values().length];
		}
		protected Heading getOppositeDirection() {
			return values()[(ordinal() + 2) % values().length];
		}
	};
	protected Heading curDirection = Heading.EAST;
	
	@Override
	public void rotate(Turn turn) throws Exception {
		if ((batteryLevel < ROTATE_ENERGY) || (hasStopped())){
			mz.state = Constants.STATE_FAILURE;
			mz.notifyViewerRedraw();
			throw new Exception();
		}
		switch(turn) {
		case RIGHT:
			curDirection = curDirection.getRightDirection();
			mz.rotate(-1);
			break;
		case LEFT:
			curDirection = curDirection.getLeftDirection();
			mz.rotate(1);
			break;
		case AROUND:
			curDirection = curDirection.getOppositeDirection();
			mz.rotate(1);
			mz.rotate(1);
			batteryLevel -= ROTATE_ENERGY;
		}
		batteryLevel -= ROTATE_ENERGY;
	}

	@Override
	public void move(int distance) throws Exception {
		if (hasStopped()){
			mz.state = Constants.STATE_FAILURE;
			mz.notifyViewerRedraw();
			throw new Exception();
		}
		while (!hasStopped() && (distance != 0)){
			if ((distanceToObstacle(Direction.FORWARD) == 0) || (batteryLevel < FORWARD_ENERGY)){
				stopped = true;
				// Restore energy used by distanceToObstacle
				batteryLevel += SENSE_ENERGY;
				move(1);
			}
			else{
				switch(curDirection){
				case NORTH:
					y++;
					break;
				case SOUTH:
					y--;
					break;
				case EAST:
					x++;
					break;
				case WEST:
					x--;
					break;
				}
				mz.walk(1);
				distance--;
				batteryLevel -= FORWARD_ENERGY;
				// Restore energy used by distanceToObstacle
				batteryLevel += SENSE_ENERGY;
			}
		}
	}

	@Override
	public int[] getCurrentPosition() throws Exception {
		int[] positionArray = {x, y};
		x = positionArray[0];
		y = positionArray[1];
		if ((x < 0) || (x > mz.getMazeWidth() - 1) || (y < 0) || (y > mz.getMazeHeight() - 1)){
			throw new Exception();
		}
		return positionArray;
	}

	@Override
	public void setMaze(Maze maze) {
		stopped = false;
		mz = maze;
		x = mz.getMazeDists().getStartPosition()[0];
		y = mz.getMazeDists().getStartPosition()[1];
		exitX = mz.getMazeDists().getExitPosition()[0];
		exitY = mz.getMazeDists().getExitPosition()[1];
		curDirection = Heading.EAST;
	}

	@Override
	public boolean isAtGoal() {
		return mz.getMazeCells().isExitPosition(x, y);
	}
	
	@Override
	public boolean canSeeGoal(Direction direction) throws UnsupportedOperationException {
		if (!hasDistanceSensor(direction)){
			throw new UnsupportedOperationException();
		}
		if ((exitX != x) && (exitY != y)){
			return false;
		}
		else if ((exitX == x) || (exitY == y)){
			return (distanceToObstacle(direction) == Integer.MAX_VALUE);
		}
		return false;
	}
	
	@Override
	public boolean isInsideRoom() throws UnsupportedOperationException {
		if (!hasRoomSensor()){
			throw new UnsupportedOperationException();
		}
		return mz.getMazeCells().isInRoom(x, y);	
	}

	@Override
	public boolean hasRoomSensor() {
		return true;
	}

	@Override
	public int[] getCurrentDirection() {
		int[] direction = {0, 0};
		switch(curDirection){
		case NORTH:
			direction[0] = 0;
			direction[1] = -1;
			break;
		case SOUTH:
			direction[0] = 0;
			direction[1] = 1;
			break;
		case WEST:
			direction[0] = -1;
			direction[1] = 0;
			break;
		case EAST:
			direction[0] = 1;
			direction[1] = 0;
			break;
		}
		return direction;
	}

	@Override
	public float getBatteryLevel() {
		return batteryLevel;
	}

	@Override
	public void setBatteryLevel(float level) {
		batteryLevel = level;

	}

	@Override
	public float getEnergyForFullRotation() {
		return (4 * ROTATE_ENERGY);
	}

	@Override
	public float getEnergyForStepForward() {
		return FORWARD_ENERGY;
	}

	@Override
	public boolean hasStopped() {
		return (batteryLevel <= 0) || (stopped == true);
	}

	@Override
	public int distanceToObstacle(Direction direction) throws UnsupportedOperationException {
		if ((!hasDistanceSensor(direction)) || (batteryLevel < 1)){
			mz.state = Constants.STATE_FAILURE;
			mz.notifyViewerRedraw();
			throw new UnsupportedOperationException();
		}
		int distance = 0;
		switch (direction){
		case FORWARD:
			distance = checkObstacleForward();
			break;
		case BACKWARD:
			distance = checkObstacleBackward();
			break;
		case LEFT:
			distance = checkObstacleLeft();
			break;
		case RIGHT:
			distance = checkObstacleRight();
			break;
		}
		batteryLevel -= SENSE_ENERGY;
		return distance;
	}

	@Override
	public boolean hasDistanceSensor(Direction direction) {
		return true;
	}
	
	/**
	 * Find the distance to the next obstacle in front of the robot.
	 * @return the distance or Integer.MAX_VALUE
	 */
	private int checkObstacleForward() {
		int distance = 0;
		if (curDirection == Heading.NORTH){
			distance = findObstacleIncreaseY();
		}
		else if (curDirection == Heading.SOUTH){
			distance = findObstacleDecreaseY();
		}
		else if (curDirection == Heading.EAST){
			distance = findObstacleIncreaseX();
		}
		else if (curDirection == Heading.WEST){
			distance = findObstacleDecreaseX();
		}
		return distance;
	}
	
	/**
	 * Find the distance to the next obstacle behind the robot.
	 * @return the distance or Integer.MAX_VALUE
	 */
	private int checkObstacleBackward() {
		int distance = 0;
		switch(curDirection) {
		case SOUTH: // facing down
			distance = findObstacleIncreaseY();
			break;
		case NORTH: // facing up
			distance = findObstacleDecreaseY();
			break;
		case EAST: // facing right
			distance = findObstacleDecreaseX();
			break;
		case WEST: // facing left
			distance = findObstacleIncreaseX();
			break;
		}
		return distance;
	}
	
	/**
	 * Find the distance to the next obstacle to the left of the robot.
	 * @return the distance or Integer.MAX_VALUE
	 */
	private int checkObstacleLeft() {
		int distance = 0;
		switch(curDirection) {
		case SOUTH: // look right if facing down
			distance = findObstacleIncreaseX();
			break;
		case NORTH: // look left if facing up
			distance = findObstacleDecreaseX();
			break;
		case EAST: // look up if facing right
			distance = findObstacleIncreaseY();
			break;
		case WEST: // look down if facing left
			distance = findObstacleDecreaseY();
			break;
		}
		return distance;
	}
	
	/**
	 * Find the distance to the next obstacle to the right of the robot.
	 * @return the distance or Integer.MAX_VALUE
	 */
	private int checkObstacleRight() {
		int distance = 0;
		switch(curDirection) {
		case SOUTH: // look left if facing down
			distance = findObstacleDecreaseX();
			break;
		case NORTH: // look right if facing up
			distance = findObstacleIncreaseX();
			break;
		case EAST: // look down if facing right
			distance = findObstacleDecreaseY();
			break;
		case WEST: // look up if facing left
			distance = findObstacleIncreaseY();
			break;
	}
		return distance;
	}
	
	/**
	 * Find the distance to the next obstacle by decreasing the y position.
	 * @return the distance or Integer.MAX_VALUE
	 */
	private int findObstacleDecreaseY() {
		for (int curY = y; curY >= 0; curY--){
			if (mz.getMazeCells().hasWallOnTop(x, curY)){
				return (y - curY);
			}
		}
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Find the distance to the next obstacle by increasing the y position.
	 * @return the distance or Integer.MAX_VALUE
	 */
	private int findObstacleIncreaseY() {
		for (int curY = y; curY < mz.getMazeHeight(); curY++){
			if ((mz.getMazeCells().hasWallOnBottom(x, curY))){
				return (curY - y);
			}
		}
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Find the distance to the next obstacle by increasing the x position.
	 * @return the distance or Integer.MAX_VALUE
	 */
	private int findObstacleIncreaseX() {
		for (int curX = x; curX < mz.getMazeWidth(); curX++){
			if (mz.getMazeCells().hasWallOnRight(curX, y)){
				return (curX - x);
			}
		}
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Find the distance to the next obstacle by decreasing the x position.
	 * @return the distance or Integer.MAX_VALUE
	 */
	private int findObstacleDecreaseX() {
		for (int curX = x; curX >= 0; curX--){
			if (mz.getMazeCells().hasWallOnLeft(curX, y)){
				return (x - curX);
			}
		}
		return Integer.MAX_VALUE;
	}
	
}
