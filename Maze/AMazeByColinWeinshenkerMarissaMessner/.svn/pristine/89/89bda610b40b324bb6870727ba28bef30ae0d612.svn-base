package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Robot.Direction;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Robot.Turn;

/**
 * CuriousMouse maze solver
 * Extends Wizard and implements RobotDriver
 * Curious Mouse uses a weighted random distribution to move toward the exit
 * This implementation chooses moves according to a random number generator, with added weight toward under-explored cells
 * Since the algorithm is random, the mouse is not expected to solve higher-level mazes.
 * To make the robot capable of solving at least level one mazes, however, the Wizard algorithm is deployed whenever the 
 * robot is in a room.
 * @author caweinshenker
 * @author mamessner
 *
 */
public class CuriousMouse extends Wizard implements RobotDriver {
	
	private int[] EAST = {1, 0};
	private int[] WEST = {-1, 0};
	private int[] NORTH = {0 , -1};
	private int[] SOUTH = {0, 1};
	Random random = new Random();

	@Override
	public boolean drive2Exit() throws Exception {
		int [][] visited = new int[width][height];
		if (!robby.isAtGoal()){
			// Check if the robot can already see the goal
			if (robby.canSeeGoal(Direction.FORWARD)){
				robby.move(1);
				pathLength++;
				return true;
			}
			else if (robby.canSeeGoal(Direction.RIGHT)){
				robby.rotate(Turn.RIGHT);
				return true;
			}
			else if (robby.canSeeGoal(Direction.LEFT)){
				robby.rotate(Turn.LEFT);
				return true;
			}
			else if (robby.canSeeGoal(Direction.BACKWARD)){
				robby.rotate(Turn.AROUND);
				return true;
			}
			// Deploy wizard if the mouse is in the room
			else if (robby.isInsideRoom()) {
				moveThroughRoom(visited);
				return true;
			}
			else {
				// Otherwise, make a random move
				int curX = robby.getCurrentPosition()[0];
				int curY = robby.getCurrentPosition()[1];
				visited[curX][curY]++;
				int[] curDirection = robby.getCurrentDirection();
				int[] weightsVector = getWeightsArray(curDirection, curX, curY, visited);
				ArrayList<Direction> sortedValidMoves = getSortedValidMoves(weightsVector);
				Direction dirToMove = getMove(sortedValidMoves);
				switch(dirToMove) {
				case FORWARD:
					robby.move(1);
					pathLength++;
					break;
				case BACKWARD:
					robby.rotate(Turn.AROUND);
					break;
				case RIGHT:
					robby.rotate(Turn.RIGHT);
					break;
				case LEFT:
					robby.rotate(Turn.LEFT);
					break;
				}
			}
		}
		// Turn to face the goal when in the exit cell
		else if (robby.canSeeGoal(Direction.RIGHT)){
			robby.rotate(Turn.RIGHT);
		}
		else if (robby.canSeeGoal(Direction.LEFT)){
			robby.rotate(Turn.LEFT);
		}
		else if (robby.canSeeGoal(Direction.BACKWARD)){
			robby.rotate(Turn.AROUND);
		}
		else {
			robby.move(1);
		}
		return true;
	}
	
	/**
	 * Determine how many times each adjacent cell has been visited.
	 * @param curDirection the direction the robot is facing
	 * @param curX the robot's x position
	 * @param curY the robot's y position
	 * @param visited a 2D array representing how many times each cell has been visited
	 * @return a 4-element array of how many times each cell (forward, right, left, back) has been visited
	 */
	private int[] getWeightsArray(int[] curDirection, int curX, int curY, int[][] visited) {
		int forwardWeight = 0;
		int rightWeight = 0;
		int leftWeight = 0;
		int backWeight = 0;
		if (Arrays.equals(curDirection, EAST)) {
			if (curX + 1 < width){
				forwardWeight = visited[curX + 1][curY];
			}
			if (curY - 1 >= 0){
				rightWeight = visited[curX][curY -1];
			}
			if (curY + 1 < height){
				leftWeight = visited[curX][curY + 1];
			}
			if (curX -1 >= 0){
				backWeight = visited[curX - 1][curY];
			}
		}
		else if (Arrays.equals(curDirection, WEST)) {
			if (curX - 1 >= 0){
				forwardWeight = visited[curX - 1][curY];
			}
			if (curY + 1 < height){
				rightWeight = visited[curX][curY + 1];
			}
			if (curY - 1 >= 0){
				leftWeight = visited[curX][curY - 1];
			}
			if (curX + 1 < width){
				backWeight = visited[curX + 1][curY];
			}
		}
		else if (Arrays.equals(curDirection, NORTH)) {
			if (curY + 1 < height){
				forwardWeight = visited[curX][curY + 1];
			}
			if (curX + 1 < width){
				rightWeight = visited[curX + 1][curY];
			}
			if (curX -1 >= 0){
				leftWeight = visited[curX - 1][curY];
			}
			if (curY - 1 >= 0){
				backWeight = visited[curX][curY - 1];
			}
		}
		else if (Arrays.equals(curDirection, SOUTH)) {
			if (curY - 1 >= 0){
				forwardWeight = visited[curX][curY - 1];
			}
			if (curX - 1 >= 0){
				rightWeight = visited[curX -1][curY];
			}
			if (curX + 1 < width){
				leftWeight = visited[curX + 1][curY];
			}
			if (curY + 1 < height){
				backWeight = visited[curX][curY + 1];
			}
		}
		int[] array = {forwardWeight, rightWeight, leftWeight, backWeight};
		return array;
	}
	
	/**
	 * Create a list of valid directions for the robot to move in, sorted starting with the least visited direction.
	 * @param weightArray
	 * @return
	 */
	private ArrayList<Direction> getSortedValidMoves(int[] weightArray) {
		int[] sortedWeightArray = weightArray.clone();
		Arrays.sort(sortedWeightArray);
		ArrayList<Direction> sortedValidMoves = new ArrayList<Direction>();
		for (int i = 0; i < sortedWeightArray.length; i++) {
			if ((sortedWeightArray[i] == weightArray[0]) && (robby.distanceToObstacle(Direction.FORWARD) != 0)
					&& (!sortedValidMoves.contains(Direction.FORWARD))) {
				sortedValidMoves.add(Direction.FORWARD);
			}
			else if ((sortedWeightArray[i] == weightArray[1]) && (robby.distanceToObstacle(Direction.RIGHT) != 0)
					&& (!sortedValidMoves.contains(Direction.RIGHT))) {
				sortedValidMoves.add(Direction.RIGHT);
			}
			else if ((sortedWeightArray[i] == weightArray[2]) && (robby.distanceToObstacle(Direction.LEFT) != 0)
					&& (!sortedValidMoves.contains(Direction.LEFT))) {
				sortedValidMoves.add(Direction.LEFT);
			}
			else if ((sortedWeightArray[i] == weightArray[3]) && (robby.distanceToObstacle(Direction.BACKWARD) != 0)
					&& (!sortedValidMoves.contains(Direction.BACKWARD))) {
				sortedValidMoves.add(Direction.BACKWARD);
			}
		}
		return sortedValidMoves;
	}
	
	/**
	 * Find the direction the robot should move in based on probability distribution weighted by the number of
	 * times cells have been visited.
	 * The probability distribution changes based on how many valid moves are possible.
	 * @param sortedValidMoves the list of valid moves sorted starting with the least-visited cell
	 * @return the direction to move in
	 */
	private Direction getMove(ArrayList<Direction> sortedValidMoves) {
		Direction direction = null;
		float probability = random.nextFloat();
		switch(sortedValidMoves.size()) {
		case 1:
			direction = sortedValidMoves.get(0);
			break;
		case 2:
			if (probability < 0.95) {
				direction = sortedValidMoves.get(0);
			}
			else {
				direction = sortedValidMoves.get(1);
			}
			break;
		case 3:
			if (probability < 0.85) {
				direction = sortedValidMoves.get(0);
			}
			else if (probability < 0.95) {
				direction = sortedValidMoves.get(1);
			}
			else {
				direction = sortedValidMoves.get(2);
			}
			break;
		case 4:
			if (probability < 0.8) {
				direction = sortedValidMoves.get(0);
			}
			else if (probability < 0.9) {
				direction = sortedValidMoves.get(1);
			}
			else if (probability < 0.95) {
				direction = sortedValidMoves.get(2);
			}
			else {
				direction = sortedValidMoves.get(3);
			}
		}
		return direction;
	}
	
	/**
	 * To make curious Mouse more effective, the wizard algorithm is used as long as the mouse is in the room
	 * @param visited -- a 2D array tracking how many times a given cell has been entered
	 * @return visited
	 * @throws Exception
	 */
	private int[][] moveThroughRoom(int[][] visited) throws Exception {
		int curX = robby.getCurrentPosition()[0];
		int curY = robby.getCurrentPosition()[1];
		visited[curX][curY]++;
		int[] curDirection = robby.getCurrentDirection();
		int distanceArray[] = getDistanceArray(curDirection, curX, curY);
		int move = getMove(distanceArray);
		switch(move){
		case 0:
			robby.move(1);
			pathLength++;
			break;
		case 1:
			robby.rotate(Turn.RIGHT);
			break;
		case 2:
			robby.rotate(Turn.LEFT);
			break;
		case 3:
			robby.rotate(Turn.AROUND);
			break;
		}
		return visited;
	}
	

}