package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

public class MazeBuilderEller extends MazeBuilder {
	protected SingleRandom random = SingleRandom.getRandom();
	protected Random defaultRandom= new Random();
	protected HashMap<Integer, Integer> cellsToSet = new HashMap<Integer, Integer>();
	protected HashMap<Integer, ArrayList<Integer>> setNumberToSet = new HashMap<Integer, ArrayList<Integer>>();
	
	
	public MazeBuilderEller(){
		super();
	}
	
	public MazeBuilderEller(boolean deterministic){
		super(deterministic);
	}
		
	/**
	 * Main override function for MazeBuilderEller
	 * Generates pathways for the maze
	 * Iterate across each row
	 * For each row but the last, add each cell index to a hashmap of cell indices to set numbers
	 * Also add each cell index to a hashmap of set numbers to lists of cell indices
	 * These two hashmaps comprise the worklist
	 * For each cell in a row (except the last one), randomly decide whether to knock its right wall down
	 * If a wall is knocked down, update the hashmaps to reflect the joined sets
	 * After horizontal walls have been knocked down, add the sets in the row to a stack
	 * Pop each item in the stack and perform at least one vertical knockdown. 
	 * Update the hashmaps to reflect joining.
	 * For the last row, join all remaining sets and update the hashmaps.
	 */
	@Override
	protected void generatePathways() {
		roomsPass();
		// Do this for all but the last row
		for (int y = 0; y < (height - 1); y++) {
			// Randomly decide to knock down walls between neighbors in the same row
			rowKnockdowns(y);
			// Get all of the sets in the given row
			Stack<ArrayList<Integer>> setStack = generateSetStack(y);
			//Knock down a bottom wall for each set in the row
			verticalKnockdowns(setStack, y);
		}
		bottomRow();
	}
	
	/**
	 * Generate a stack of sets that border the frontier of the explored maze.
	 * This will be used to ensure that each set in a row has a vertical connection to the row below it
	 * @param y -- the current row
	 * @return setStack -- the stack of sets on the frontier
	 */
	private Stack<ArrayList<Integer>> generateSetStack(int y){
		Stack<ArrayList<Integer>> setStack = new Stack<ArrayList<Integer>>();
		int rowStart = y * width;
		int rowEnd = (width + (y * width));
		List<Integer> setList = new ArrayList<Integer>();
		for (int x = rowStart; x < rowEnd; x++){
			int setNumber = cellsToSet.get(x);
			if (!setList.contains(setNumber)){
				setList.add(setNumber);
				setStack.push(setNumberToSet.get(setNumber));
			}
		}
		return setStack;
		
	}
	
	
	/**
	 * Make a pass over the cells array to account for added rooms
	 * First put each cell in its own set
	 * Then pass over the cells again and update the sets to reflect rooms
	 */
	public void roomsPass(){
		//Put each cell in its own sets
		for (int i = 0; i < (width * height); i++){
			putCellInNewSet(i);
		}
		//Join sets according to rooms
		for (int i = 0; i < (width * height); i++){
			int xPosition = i % width;
			// TODO: figure out why Math.floorDiv() is undefined
			int yPosition = (int) Math.floor(i / width);
//			int yPosition = Math.floorDiv(i, width);
			if (xPosition != width - 1) {
				if (cells.hasWallOnRight(xPosition, yPosition) == false){
				updateSets(i, i + 1);
				}
			}
			if (yPosition != height - 1){	
				if (cells.hasWallOnBottom(xPosition, yPosition) == false){
				updateSets(i, i + width);
				}
			}
		}
	}

	
	
	/**
	 * Iterate across the row, randomly deciding whether to knock down the right wall of each cell
	 * @param y -- the current row
	 */
	private void rowKnockdowns (int y){
		for (int x = 0; x < width - 1; x ++) {
			int knockdown = random.nextIntWithinInterval(0, 1);
			if (knockdown == 1) {
				knockDownRightWall(x, y);
				}
			}
		}
	
	/**
	 * Iterate across the bottom row
	 * For each cell but the rightmost, obtain its set number and its right neighbor's set number
	 * If the set numbers are different, join the sets.
	 * Update the hashmaps to reflect the changes
	 */
	private void bottomRow(){
		for (int position = width * (height - 1); position < (width * height) - 1; position++) {
			int currentSet = cellsToSet.get(position);
			int neighborSet = cellsToSet.get(position + 1);
			int x = position % width;
			int y = height - 1;
			if (currentSet != neighborSet) {
				knockDownRightWall(x, y);
				currentSet = cellsToSet.get(position);
				neighborSet = cellsToSet.get(position + 1);
			}
		}
	}

	
	/**
	 * Knock down the right wall of position (x, y) if the two cells aren't in the same set and are not separated by a border.
	 * @param x The x position of the current cell
	 * @param y The y position of the current cell
	 * @return -- true if a right wall was successfully knocked down
	 */
	private boolean knockDownRightWall(int x, int y) {
		int dx = 1;
		int dy = 0;
		int cell1Position = x + (width * y);
		int cell2Position = (x + dx) + width * (y + dy);
		if ((cellsToSet.get(cell1Position) != cellsToSet.get(cell2Position)) && (noBorder(x, y, dx, dy))){
			cells.deleteWall(x, y, dx, dy);
			setAsVisited(x, y, dx, dy);
			if (cellsToSet.get(cell1Position) != cellsToSet.get(cell2Position)) {
				updateSets(cell1Position, cell2Position);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Knock down the bottom wall of position (x, y).
	 * Only knock down wall if cell is not in same set as cell below it and the two are not separated by a border
	 * @param x The x position of the current cell
	 * @param y The y position of the current cell
	 * @return -- true if a bottom wall was successfully knocked down
	 */
	private boolean knockDownBottomWall(int x, int y) {
		int dx = 0;
		int dy = 1;
		int cell1Position = x + width*y;
		int cell2Position =  (x + dx) + width*(y + dy);
		if ((cellsToSet.get(cell1Position) != cellsToSet.get(cell2Position)) && (noBorder(x, y, dx, dy))){
			//System.out.println("Knocking down bottom wall of (" + x + ", " + y + ")");
			cells.deleteWall(x, y, dx, dy);
			setAsVisited(x, y, dx, dy);
			updateSets(cell1Position, cell2Position);
			return true;
		}
		return false;
	}
	
	
	/**
	 * Knock down at least one bottom wall for each set in the row
	 * @param y -- the row index
	 * @param setStack -- a stack containing lists of the cells in each set in the row
	 */
	private void verticalKnockdowns(Stack<ArrayList<Integer>> setStack, int y){
		while (!setStack.isEmpty()) {
			ArrayList<Integer> set = setStack.pop();
			// Remove cells from the previous rows from the set
			ArrayList<Integer> oldCells = getOldCells(set, y);
			set.removeAll(oldCells);
			ArrayList<Integer> tempSet = new ArrayList<Integer>(set); //A copy of set
			ArrayList<Integer> triedCells = new ArrayList<Integer>(); //A list of cell indices already tried for vertical knockdown
			// Knock down at least one lower wall
			boolean success = false;
			while ((success == false) && (tempSet.size() != 0)){
				int upperBound = tempSet.size() == 1 ? 1 : tempSet.size() - 1;
				int numberOfKnockdowns = random.nextIntWithinInterval(1, upperBound);
				for (int i = 1; i <= numberOfKnockdowns; i++) {
					// Determine which index should get its wall knocked down
					int knockDownIndex = random.nextIntWithinInterval(0, tempSet.size() - 1);
					int knockDownPosition = tempSet.get(knockDownIndex);
					int x = knockDownPosition % width;
					success = knockDownBottomWall(x, y);
					if (success == false){
						triedCells.add(knockDownPosition);
					}
				}
				//Check to be sure that the set has at least one bottom wall missing
				success = noDisjoint(tempSet, y);
				//Take out any cells that have already been tried
				tempSet.removeAll(triedCells);
				}
			//If all possible bottom walls have been exhausted, override the border rule
			if (tempSet.size() == 0){
				while (success == false){
					success = overrideKnockDown(set, y);	
					}
				}
			}
		}
	/**
	 * Call this method only if none of the bottom walls in a row's set can be knocked down without violating the border rule
	 * Join two cells if they are not already in the same set, ignoring the border constraint
	 * @param set -- the cell indices in a set along the current border frontier
	 * @param y -- the row index
	 * @return true if a bottom wall has been knocked down
	 */
	private boolean overrideKnockDown(ArrayList<Integer> set, int y){
		int knockDownIndex = random.nextIntWithinInterval(0, set.size() - 1);
		int knockDownPosition = set.get(knockDownIndex);
		int x = knockDownPosition % width;
		int dx = 0;
		int dy = 1;
		int cell1Position = x + width*y;
		int cell2Position =  (x + dx) + width*(y + dy);
		if ((cellsToSet.get(cell1Position) != cellsToSet.get(cell2Position))){
			cells.deleteWall(x, y, dx, dy);
			setAsVisited(x, y, dx, dy);
			updateSets(cell1Position, cell2Position);
			return true;
		}
		return false;
	}
	
	/**
	 * Check to see that a given set has a vertical connection to the next row below the frontier of the explored maze
	 * @param set -- the cell indices within a given set and along the frontier of the explored maze
	 * @param y -- the row index
	 * @return -- true if the given set has at least one vertical connection
	 */
	private boolean noDisjoint(ArrayList<Integer> set, int y){
		boolean setHasOpenBottom = false;
		for (int elem: set){
			int x = elem % width;
			if (cells.hasNoWallOnBottom(x, y)){
				setHasOpenBottom = true;
			}
		}
		return setHasOpenBottom;
	}
	
	/**
	 * Mark the given cell and its neighbor as visited.
	 * @param x The current x position
	 * @param y The current y position
	 * @param dx The horizontal change in direction; neighbor's x position = x + dx
	 * @param dy The vertical change in direction; neighbor's y position = y + dy
	 */
	private void setAsVisited(int x, int y, int dx, int dy) {
		cells.setCellAsVisited(x, y);
		cells.setCellAsVisited(x + dx, y + dy);
	}
	
	/**
	 * Add cell2 to cell1's set and remove cell2's set from the set-number-to-set map.
	 * @param cell1Position cell1's overall position
	 * @param cell2Position cell2's overall position
	 */
	private void updateSets(int cell1Position, int cell2Position) {
		if (cellsToSet.get(cell1Position) != cellsToSet.get(cell2Position)){
			int cell1SetNumber = cellsToSet.get(cell1Position);
			int cell2SetNumber = cellsToSet.get(cell2Position);
			ArrayList<Integer> cell1Set = setNumberToSet.get(cell1SetNumber);
			Set<Integer> keys = cellsToSet.keySet();
			for (int key : keys) {
				if ((cellsToSet.get(key) == cell2SetNumber) && !(setNumberToSet.get(cell1SetNumber).contains(key))){
					// Update cell-to-set-number map to map k to cell1's set
					cellsToSet.put(key, cell1SetNumber);
					// Update set-number-to-set map to include k in cell1's set
					cell1Set.add(key);
				}
			}
			setNumberToSet.put(cell1SetNumber, cell1Set);
			// Cell 2's set doesn't exist anymore
			if (cell2SetNumber != cell1SetNumber) {
				setNumberToSet.remove(cell2SetNumber);
			}
		}
	}	
	
	/**
	 * Check to see whether a given cell's indicated neighbor has no border
	 * @param x  -- x position of given cell
	 * @param y -- y position of given cell
	 * @param dx  -- direction of neighbor cell
	 * @param dy -- direction of neighbor cell
	 * @return boolean -- true if neighbor does not have a border
	 */
	private boolean noBorder(int x, int y, int dx, int dy){
		if (cells.hasMaskedBitsTrue(x, y, (cells.getBit(dx, dy) << Constants.CW_BOUND_SHIFT))){
			return false;
		}
		return true;
	}
	
	/**
	 * Put a cell in its own set in both maps.
	 * @param position -- the current position in cells
	 */
	private void putCellInNewSet(int position) {
		cellsToSet.put(position, position);
		ArrayList<Integer> newSet = new ArrayList<Integer>();
		newSet.add(position);
		setNumberToSet.put(position, newSet);
	}
	
	/**
	 * Find cells from the previous row in a given set.
	 * @param set -- The set to look at
	 * @return ArrayList -- an ArrayList of previous cells
	 */
	private ArrayList<Integer> getOldCells(ArrayList<Integer> set, int y) {
		ArrayList<Integer> oldCells = new ArrayList<Integer>();
		for (int position : set) {
			if (position < (y * width)) {
				oldCells.add(position);
			}
		}
		return oldCells;
	}
}
