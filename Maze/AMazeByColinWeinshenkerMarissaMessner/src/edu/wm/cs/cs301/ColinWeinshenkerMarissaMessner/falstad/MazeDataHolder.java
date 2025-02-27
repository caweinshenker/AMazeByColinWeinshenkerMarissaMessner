package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad;

/**
 * Static class for relaying data from constructed Maze to MazeGeneration and 
 * Stateplay activities
 * 
 * @author mamessner
 * @author cweinshenker
 *
 */
public class MazeDataHolder {
	public static Cells cells;
	public static int[][] dists;
	public static int skill;
	public static BSPNode root;
	public static int width;
	public static int height;
	public static int startX;
	public static int startY;
	public static int rooms;
	public static int expectedPartiters;
	public static String generationAlgorithm;
	
	/**
	 * Set the distances matrix
	 * @param distanceSetter -- the distance matrix to set the class member to
	 */
	public static void setDistance(int[][] distanceSetter){
		dists = distanceSetter;
	}
	
	/**
	 * Get the distances matrix
	 * @return distance -- the distance matrix
	 */
	public static int[][] getDistance(){
		return dists;
	}
	
	/**
	 * Get the cells object
	 * @return cells -- the cells object to be passed
	 */
	public static Cells getCells(){
		return cells;
	}
	
	/**
	 * Set the cells object
	 * @param cellsSetter -- the cells object to set the class member to 
	 */
	public static void setCells(Cells cellsSetter){
		cells = cellsSetter;
	}
	
	/**
	 * Set the skill level
	 * @param skillSetter -- the skill level
	 */
	public static void setSkill(int skillSetter){
		skill = skillSetter;
	}
	
	/**
	 * Get the skill level
	 * @return skill -- the skill level
	 */
	public static int getSkill(){
		return skill;
	}
	
	/**
	 * Get the number of rooms
	 * @return rooms -- the number of rooms
	 */
	public static int getRooms(){
		return Constants.SKILL_ROOMS[skill];
	}
	
	/**
	 * Get the number of expected partiters
	 * @return expectedPartiters
	 */
	public static int getExpectedPartiters(){
		return Constants.SKILL_PARTCT[skill];
	}
	
	/**
	 * Set the root node
	 * @param rootSetter -- the root
	 */
	public static void setRoot(BSPNode rootSetter){
		root = rootSetter;
	}
	
	/**
	 * Get the root node
	 */
	public static BSPNode getRoot(){
		return root;
	}
	
	/**
	 * Set the maze width
	 * @param widthSetter -- the width
	 */
	public static void setWidth(int widthSetter){
		width = widthSetter;
	}
	
	/**
	 * Get the maze width
	 * @return width -- the width
	 */
	public static int getWidth(){
		return width;
	}
	
	/**
	 * Set the maze height
	 * @param heightSetter -- the height
	 */
	public static void setHeight(int heightSetter){
		height = heightSetter;
	}
	
	/**
	 * Get the maze height
	 * @return height -- the height
	 */
	public static int getHeight(){
		return height;
	}
	
	/**
	 * Set the starting X position
	 * @param startXSetter -- the x position
	 */
	public static void setStartX(int startXSetter){
		startX = startXSetter;
	}
	
	/**
	 * @return startX -- the starting X position
	 */
	public static int getStartX(){
		return startX;
	}
	
	/**
	 * @param startYSetter -- the starting Y position
	 */
	 public static void setStartY(int startYSetter){
		 startY = startYSetter;
	 }
	 
	 /**
	  * @return startY -- the starting Y position
	  */
	 public static int getStartY(){
		 return startY;
	 }
	 
	 /**
	  * Set the generation Algorithm
	  * @param generationAlgorithmSetter -- the algorithm
	  */
	 public static void setGenerationAlgorithm(String generationAlgorithmSetter){
		 generationAlgorithm = generationAlgorithmSetter;		 
	 }
	 
	 /**
	  * Return the generation algorithm
	  * @return generationAlgorithm
	  */
	 public static String getGenerationAlgorithm(){
		 return generationAlgorithm;
	 }
	 
	 /**
	  * Set the number of rooms
	  * @param roomsSetter -- number of rooms
	  */
	 public static void setRooms(int roomsSetter){
		 rooms = roomsSetter;
	 }
	 
	 /**
	  * Set the number of partiters
	  * @param partitersSetter
	  */
	 public static void setExpectedPartiters(int partitersSetter){
		 expectedPartiters = partitersSetter;
	 }
	 

}
