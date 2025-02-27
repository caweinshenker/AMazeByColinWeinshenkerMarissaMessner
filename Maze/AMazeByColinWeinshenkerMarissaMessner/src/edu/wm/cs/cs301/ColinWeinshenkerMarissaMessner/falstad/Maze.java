package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Application;
import android.os.Handler;

/**
 * Class handles the user interaction for the maze. 
 * It implements a state-dependent behavior that controls the display and reacts to key board input from a user. 
 * After refactoring the original code from an applet into a panel, it is wrapped by a MazeApplication to be a java application 
 * and a MazeApp to be an applet for a web browser. At this point user keyboard input is first dealt with a key listener
 * and then handed over to a Maze object by way of the keyDown method.
 *
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
// MEMO: original code: public class Maze extends Applet {
//public class Maze extends Panel {
public class Maze{

	// Model View Controller pattern, the model needs to know the viewers
	// however, all viewers share the same graphics to draw on, such that the share graphics
	// are administered by the Maze object
	final private ArrayList<Viewer> views = new ArrayList<Viewer>() ;
	private GraphicsWrapper wrapper ; // graphics wrapper to draw on, shared by all views
		


	protected int state;			// keeps track of the current GUI state, one of STATE_TITLE,...,STATE_FINISH, mainly used in redraw()
	// possible values are defined in Constants
	// user can navigate 
	// title -> generating -(escape) -> title
	// title -> generation -> play -(escape)-> title
	// title -> generation -> play -> finish/failure -> title
	// STATE_PLAY is the main state where the user can navigate through the maze in a first person view

	private int percentdone = 0; // describes progress during generation phase
	private boolean showMaze;		 	// toggle switch to show overall maze on screen
	private boolean showSolution;		// toggle switch to show solution in overall maze on screen
	private boolean solving;			// toggle switch 
	private boolean mapMode; // true: display map of maze, false: do not display map of maze
	// map_mode is toggled by user keyboard input, causes a call to draw_map during play mode

	//static final int viewz = 50;    
	private int viewx, viewy, angle;
	private int dx, dy;  // current direction
	private int px, py ; // current position on maze grid (x,y)
	private int walkStep;
	private int viewdx, viewdy; // current view direction


	// debug stuff
	private boolean deepdebug = false;
	private boolean allVisible = false;
	private boolean newGame = false;

	// properties of the current maze
	private int mazew; // width of maze
	private int mazeh; // height of maze
	private int skill;
	private Cells mazecells ; // maze as a matrix of cells which keep track of the location of walls
	private Distance mazedists ; // a matrix with distance values for each cell towards the exit
	private Cells seencells ; // a matrix with cells to memorize which cells are visible from the current point of view
	// the FirstPersonDrawer obtains this information and the MapDrawer uses it for highlighting currently visible walls on the map
	private BSPNode rootnode ; // a binary tree type search data structure to quickly locate a subset of segments
	// a segment is a continuous sequence of walls in vertical or horizontal direction
	// a subset of segments need to be quickly identified for drawing
	// the BSP tree partitions the set of all segments and provides a binary search tree for the partitions
	

	// Mazebuilder is used to calculate a new maze together with a solution
	// The maze is computed in a separate thread. It is started in the local Build method.
	// The calculation communicates back by calling the local newMaze() method.
	MazeBuilder mazebuilder;

	
	// fixing a value matching the escape key
	final int ESCAPE = 27;

	// generation method used to compute a maze
	int method = 0 ; // 0 : default method, Falstad's original code
	// method == 1: Prim's algorithm

	int zscale = Constants.VIEW_HEIGHT/2;

	private RangeSet rset;
	protected int pathLength;
	protected float energyUsed;
	protected RobotDriver driver;
	protected MazeView mazeview;
	
	
	/**
	 * Constructor
	 */
	public Maze() {
		super() ;
	}
	/**
	 * Constructor that also selects a particular generation method
	 */
	public Maze(int method)
	{
		super() ;
		this.method = method;
	}
	
	/**
	 * Method to initialize internal attributes. Called separately from the constructor. 
	 */
	public void init() {
		state = Constants.STATE_PLAY;
		rset = new RangeSet();
		mazeview = new MazeView(this);
		addView(mazeview) ;
		notifyViewerRedraw() ;
	}
	
	/**
	 * Return the mazeview object
	 */
	public MazeView getMazeView(){
		return mazeview;
	}
	
	/**
	 * Assign the maze a graphics wrapper object
	 * @param wrapper -- the graphics wrapper to be assigned
	 */
	public void setGraphicsWrapper(GraphicsWrapper wrapper){
		this.wrapper = wrapper;
	}
	
	/**
	 * Set the maze skill level.
	 * @param skill The skill level
	 */
	public void setSkill(int skill) {
		this.skill = skill;
	}
	
	/**
	 * Set the maze builder.
	 * @param generationAlg The generatin algorithm to use
	 */
	public void setBuilder(String generationAlg) {
		switch(generationAlg){
		case "Prim":
			mazebuilder = new MazeBuilderPrim();
			break;
		case "Eller":
			mazebuilder = new MazeBuilderEller();
			break;
		case "Default":
			mazebuilder = new MazeBuilder();
			break;
		}	
	}
	
	/**
	 * Build the maze.
	 */
	public void build() {
		state = Constants.STATE_GENERATING;
		percentdone = 0;
		mazew = Constants.SKILL_X[skill];
		mazeh = Constants.SKILL_Y[skill];
		mazebuilder.build(this, mazew, mazeh, Constants.SKILL_ROOMS[skill], Constants.SKILL_PARTCT[skill]);
	}
	
	/**
	 * Call back method for MazeBuilder to communicate newly generated maze as reaction to a call to build()
	 * @param root node for traversals, used for the first person perspective
	 * @param cells encodes the maze with its walls and border
	 * @param dists encodes the solution by providing distances to the exit for each position in the maze
	 * @param startx current position, x coordinate
	 * @param starty current position, y coordinate
	 */
	public void newMaze(BSPNode root, Cells c, Distance dists, int startx, int starty) {
		if (Cells.deepdebugWall)
		{   // for debugging: dump the sequence of all deleted walls to a log file
			// This reveals how the maze was generated
			c.saveLogFile(Cells.deepedebugWallFileName);
		}
		// adjust internal state of maze model
		showMaze = showSolution = solving = false;
		showMaze = false;
		showSolution = false;
		mazecells = c ;
		mazedists = dists;
		seencells = new Cells(mazew+1,mazeh+1) ;
		rootnode = root ;
		MazeDataHolder.setRoot(rootnode);
		setCurrentDirection(1, 0) ;
		setCurrentPosition(startx,starty) ;
		walkStep = 0;
		viewdx = dx<<16; 
		viewdy = dy<<16;
		angle = 0;
		mapMode = false;
		// set the current state for the state-dependent behavior
		state = Constants.STATE_PLAY;
		MazeDataHolder.setCells(mazecells);
		MazeDataHolder.setDistance(mazedists.getDists());
		cleanViews() ;
		// register views for the new maze
		// mazew and mazeh have been set in build() method before mazebuider was called to generate a new maze.
		// reset map_scale in mapdrawer to a value of 10
		addView(new FirstPersonDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT,
				Constants.MAP_UNIT,Constants.STEP_SIZE, mazecells, seencells, 10, mazedists.getDists(), mazew, mazeh, root, this)) ;
		// order of registration matters, code executed in order of appearance!
		addView(new MapDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT,Constants.MAP_UNIT,Constants.STEP_SIZE, mazecells, seencells, 10, mazedists.getDists(), mazew, mazeh, this)) ;

		// notify viewers
	}

	/////////////////////////////// Methods for the Model-View-Controller Pattern /////////////////////////////
	/**
	 * Register a view
	 */
	public void addView(Viewer view) {
		views.add(view) ;
	}
	/**
	 * Unregister a view
	 */
	public void removeView(Viewer view) {
		views.remove(view) ;
	}
	/**
	 * Remove obsolete FirstPersonDrawer and MapDrawer
	 */
	private void cleanViews() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			if ((v instanceof FirstPersonDrawer)||(v instanceof MapDrawer))
			{
				//System.out.println("Removing " + v);
				it.remove() ;
			}
		}

	}
	
	
	/**
	 * Notify all registered viewers to redraw their graphics
	 */
	public void notifyViewerRedraw() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		wrapper.newGraphics();
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			// viewers draw on the buffer graphics
			v.redraw(wrapper, state, px, py, viewdx, viewdy, walkStep, Constants.VIEW_OFFSET, rset, angle);
		}
		// update the screen with the buffer graphics
		wrapper.invalidate();
	}
	/** 
	 * Notify all registered viewers to increment the map scale
	 */
	private void notifyViewerIncrementMapScale() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			v.incrementMapScale() ;
		}
		// update the screen with the buffer graphics
		wrapper.invalidate();
	}
	/** 
	 * Notify all registered viewers to decrement the map scale
	 */
	private void notifyViewerDecrementMapScale() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			v.decrementMapScale() ;
		}
		// update the screen with the buffer graphics
		wrapper.invalidate();
	}
	////////////////////////////// get methods ///////////////////////////////////////////////////////////////
	boolean isInMapMode() { 
		return mapMode ; 
	} 
	boolean isInShowMazeMode() { 
		return showMaze ; 
	} 
	boolean isInShowSolutionMode() { 
		return showSolution ; 
	} 
	public String getPercentDone(){
		return String.valueOf(percentdone) ;
	}
	
	/**
	 * Return the robot's battery level for the MazeView
	 * @return the amount of energy the robot has used
	 */
	protected float getEnergyUsed(){
		return driver.getEnergyConsumption();
	}
	
	/**
	 * Return the robot's path length for the MazeView
	 * @return path length
	 */
	protected int getPathLength(){
		return driver.getPathLength();
	}
	
	/**
	 * Return the Distance object
	 * @return mazedists -- the distance object.
	 */
	public Distance getMazeDists(){
		return mazedists;
	}
	
	/**
	 * Return the Cells object.
	 * @return mazecells the Cells object
	 */
	public Cells getMazeCells() {
		return mazecells;
	}
	
	/**
	 * Return the width of the maze.
	 * @return width
	 */
	public int getMazeWidth() {
		return mazew;
	}
	
	/**
	 * Return the height of the maze.
	 * @return height
	 */
	public int getMazeHeight() {
		return mazeh;
	}
	
	/**
	 * Return the MazeBuilder.
	 * @return mazebuilder
	 */
	public MazeBuilder getBuilder() {
		return mazebuilder;
	}
	////////////////////////////// set methods ///////////////////////////////////////////////////////////////
	////////////////////////////// Actions that can be performed on the maze model ///////////////////////////
	protected void setCurrentPosition(int x, int y) {
		px = x ;
		py = y ;
	}
	
	private void setCurrentDirection(int x, int y) {
		dx = x ;
		dy = y ;
	}
	
	/**
	 * Set the Cells object. This should only be used in tests.
	 * @param mazecells the Cells object
	 */
	protected void setMazeCells(Cells mazecells) {
		this.mazecells = mazecells;
	}
	
	/**
	 * Set the Distance object. This should only be used in tests.
	 * @param mazedists the Distance object
	 */
	protected void setMazeDists(Distance mazedists) {
		this.mazedists = mazedists;
	}
	
	/**
	 * Set the width. This should only be used in MazeStub and MazeApplication.
	 * @param width the width
	 */
	public void setMazeWidth(int width) {
		mazew = width;
	}
	
	/**
	 * Set the height. This should only be used in MazeStub and MazeApplication.
	 * @param height the height
	 */
	public void setMazeHeight(int height) {
		mazeh = height;
	}
	
	/////////////////////////////////////// end set methods ////////////////////////////////////////////
	
	
	void buildInterrupted() {
		state = Constants.STATE_TITLE;
		notifyViewerRedraw() ;
		mazebuilder = null;
	}

	final double radify(int x) {
		return x*Math.PI/180;
	}


	/**
	 * Allows external increase to percentage in generating mode with subsequence graphics update
	 * @param pc gives the new percentage on a range [0,100]
	 * @return true if percentage was updated, false otherwise
	 */
	public boolean increasePercentage(int pc) {
		if (percentdone < pc && pc < 100) {
			percentdone = pc;
			if (state == Constants.STATE_GENERATING)
			{
				notifyViewerRedraw() ;
			}
			else
				dbg("Warning: Receiving update request for increasePercentage while not in generating state, skip redraw.") ;
			return true ;
		}
		return false ;
	}

	



	/////////////////////// Methods for debugging ////////////////////////////////
	private void dbg(String str) {
		//System.out.println(str);
	}

	private void logPosition() {
		if (!deepdebug)
			return;
		dbg("x="+viewx/Constants.MAP_UNIT+" ("+
				viewx+") y="+viewy/Constants.MAP_UNIT+" ("+viewy+") ang="+
				angle+" dx="+dx+" dy="+dy+" "+viewdx+" "+viewdy);
	}
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Helper method for walk()
	 * @param dir
	 * @return true if there is no wall in this direction
	 */
	private boolean checkMove(int dir) {
		// obtain appropriate index for direction (CW_BOT, CW_TOP ...) 
		// for given direction parameter
		int a = angle/90;
		if (dir == -1)
			a = (a+2) & 3; // TODO: check why this works
		// check if cell has walls in this direction
		// returns true if there are no walls in this direction
		return mazecells.hasMaskedBitsFalse(px, py, Constants.MASKS[a]) ;
	}



	private void rotateStep() {
		angle = (angle+1800) % 360;
		viewdx = (int) (Math.cos(radify(angle))*(1<<16));
		viewdy = (int) (Math.sin(radify(angle))*(1<<16));
		moveStep();
	}

	private void moveStep() {
		notifyViewerRedraw() ;
		try {
			Thread.currentThread().sleep(25);
		} catch (Exception e) { }
	}

	private void rotateFinish() {
		setCurrentDirection((int) Math.cos(radify(angle)), (int) Math.sin(radify(angle))) ;
		logPosition();
	}

	private void walkFinish(int dir) {
		setCurrentPosition(px + dir*dx, py + dir*dy) ;
		
		if (isEndPosition(px,py)) {
			state = Constants.STATE_FINISH;
			notifyViewerRedraw() ;
		}
		walkStep = 0;
		logPosition();
	}

	/**
	 * checks if the given position is outside the maze
	 * @param x
	 * @param y
	 * @return true if position is outside, false otherwise
	 */
	private boolean isEndPosition(int x, int y) {
		return x < 0 || y < 0 || x >= mazew || y >= mazeh;
	}



	synchronized protected void walk(int dir) {
		if (!checkMove(dir))
			return;
		for (int step = 0; step != 4; step++) {
			walkStep += dir;
			moveStep();
		}
		walkFinish(dir);
		pathLength++;
	}

	synchronized protected void rotate(int dir) {
		final int originalAngle = angle;
		final int steps = 4;
		for (int i = 0; i != steps; i++) {
			angle = originalAngle + dir*(90*(i+1))/steps;
			rotateStep();
		}
		rotateFinish();
	}



	/**
	 * Method incorporates all reactions to keyboard input in original code, 
	 * after refactoring, Java Applet and Java Application wrapper call this method to communicate input.
	 */
	 public boolean keyDown(int key) {
		// possible inputs for key: unicode char value, 0-9, A-Z, Escape, 'k','j','h','l'
		switch (state) {
		// if we are currently generating a maze, recognize interrupt signal (ESCAPE key)
		// to stop generation of current maze
		case Constants.STATE_GENERATING:
			if (key == ESCAPE) {
				mazebuilder.interrupt();
				buildInterrupted();
			}
			break;
		// if user explores maze, 
		// react to input for directions and interrupt signal (ESCAPE key)	
		// react to input for displaying a map of the current path or of the overall maze (on/off toggle switch)
		// react to input to display solution (on/off toggle switch)
		// react to input to increase/reduce map scale
		case Constants.STATE_PLAY:
			switch (key) {
			case 'k': case '8':
				//walk(1);
				break;
			case 'h': case '4':
				//rotate(1);
				break;
			case 'l': case '6':
				//rotate(-1);
				break;
			case 'j': case '2':
				//walk(-1);
				break;
			case ESCAPE: case 65385:
				if (solving)
					solving = false;
				else
					state = Constants.STATE_TITLE;
				notifyViewerRedraw() ;
				break;
			case ('w' & 0x1f): 
			{ 
				setCurrentPosition(px + dx, py + dy) ;
				notifyViewerRedraw() ;
				break;
			}
			case '\t': case 'm':
				mapMode = !mapMode; 		
				notifyViewerRedraw() ; 
				break;
			case 'z':
				showMaze = !showMaze; 		
				notifyViewerRedraw() ; 
				break;
			case 's':
				showSolution = !showSolution; 		
				notifyViewerRedraw() ;
				break;
			case ('s' & 0x1f):
				if (solving)
					solving = false;
				else {
					solving = true;
				}
			break;
			case '+': case '=':
			{
				notifyViewerIncrementMapScale() ;
				notifyViewerRedraw() ; // seems useless but it is necessary to make the screen update
				break ;
			}
			case '-':
				notifyViewerDecrementMapScale() ;
				notifyViewerRedraw() ; // seems useless but it is necessary to make the screen update
				break ;
			}
			break;
		// if we are finished, return to initial state with title screen	
		case Constants.STATE_FINISH:
			state = Constants.STATE_TITLE;
			notifyViewerRedraw() ;
			break;
		case Constants.STATE_FAILURE:
			state = Constants.STATE_TITLE;
			notifyViewerRedraw();
		} 
		return true;
	}
}
