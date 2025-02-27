package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.ui;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.BasicRobot;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Constants;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.CuriousMouse;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.GraphicsWrapper;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.ManualDriver;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Maze;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Robot;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.RobotDriver;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.SingleRandom;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.WallFollower;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Wizard;

/**
 * This class supports functionality for the play state of the maze
 * The class should handle user input on two different toolbars
 * The first toolbar allows the user to toggle the mazeview options
 * The second toolbar (footer) allows the user to drive the robot in manual mode
 * If not in manual mode, the second toolbar simply has a pause button to stop a robot
 * There should also be two buttons to move to the finish activity in success and failure states, respectively
 * @author cweinshenker 
 * @author mamessner
 */
public class PlayActivity extends AppCompatActivity{
	
	private RelativeLayout layout;
	private GraphicsWrapper tupac;
	private ProgressBar batteryBar;
	private String driverName;
	private boolean showSolution;
	private boolean showWalls;
	private boolean showMaze;
	private ToggleButton wallsToggle;
	private ToggleButton solutionToggle;
	private ToggleButton mazeToggle;
	private float batteryLevel;
	private int pathLength;
	private Maze maze;
	private Robot robby;
	private RobotDriver driver;
	private MediaPlayer mediaPlayer;
	private MediaPlayer backgroundPlayer;
	private MediaPlayer attackPlayer;
	private SingleRandom random = SingleRandom.getRandom();
	private Button forward;
	private Button back;
	private Button left;
	private Button right;
	private boolean gamePaused;
	private ToggleButton pause;
	private TextView pauseText;
	private long time;
	private int roar = 0;
	private static final int DELAY = 15;
	private static final String LOG_TAG = "PlayActivity";
	private Handler robotHandler = new Handler();
	private Handler stateHandler = new Handler(){
		@Override
		public void handleMessage (Message msg){
			int state = msg.what;
			switch(state){
			case Constants.STATE_FAILURE:
				toFinishFailure(new View(getApplicationContext()));
				break;
			case Constants.STATE_FINISH:
				toFinishSuccess(new View(getApplicationContext()));
				break;
			}
		}
	};
	private Runnable moveRobot = new Runnable() {
		@Override
		public void run() {
			if (!gamePaused) {
				try {
					// drive2Exit() makes a single move
					driver.drive2Exit();
				} catch (Exception e) {
					e.printStackTrace();
				}
				tupac.invalidate();
				updateBattery();
			}
			robotHandler.postDelayed(this, DELAY);
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		batteryBar = (ProgressBar) findViewById(R.id.batteryLevelBar);
		getIntentExtras();
		setUpButtons();
		maze = GeneratingActivity.maze;
		setUpRobotAndDriver();
		setUpGraphicsWrapper();
		setUpAudio();
		time = System.currentTimeMillis();
	}
	
	/**
	 * Set the visibility of the D-pad and pause buttons based on the driver 
	 */
	public void setUpButtons(){
		forward = (Button) findViewById(R.id.button1);
		left = (Button) findViewById(R.id.button2);
		right = (Button) findViewById(R.id.button4);
		back = (Button) findViewById(R.id.button3);
		pause = (ToggleButton) findViewById(R.id.toggleButton4);
		pauseText = (TextView) findViewById(R.id.textView4);
		if (!driverName.equals("Manual")){
			forward.setVisibility(View.GONE);
			left.setVisibility(View.GONE);
			right.setVisibility(View.GONE);
			back.setVisibility(View.GONE);
		}
		else{
			pause.setVisibility(View.GONE);
			pauseText.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Return to the title screen if the back button is pressed
	 */
	@Override
	public void onBackPressed(){
		Log.v(LOG_TAG, "Returning to title screen");
		Intent intent = new Intent(this, AMazeActivity.class);
		// Stop the driver
		robotHandler.removeCallbacks(moveRobot);
		releaseAllPlayers();
		startActivity(intent);
		finish();
	}
	
	/**
	 * Pause the music when the app isn't visible.
	 */
	@Override
	public void onPause() {
		super.onPause();
//		mediaPlayer.pause();
//		mediaPlayer.stop();
//		mediaPlayer.release();
//		mediaPlayer.pause();
//		mediaPlayer.stop();
//		mediaPlayer.release();
//		backgroundPlayer.pause();
//		mediaPlayer.release();
//		backgroundPlayer.stop();
	}
	
	/**
	 * Restart the background music when the app comes back into view.
	 */
	@Override
	public void onResume() {
		super.onResume();
//		backgroundPlayer.start();
	}
	
	/**
	 * Set up the audio players.
	 */
	private void setUpAudio() {
		mediaPlayer = MediaPlayer.create(this, R.raw.running_gravel);
		mediaPlayer.setLooping(true);
		backgroundPlayer = MediaPlayer.create(this, R.raw.maintitle);
		backgroundPlayer.setLooping(true);
		backgroundPlayer.setVolume(0.3f, 0.3f);
		backgroundPlayer.start();
		attackPlayer = MediaPlayer.create(this, R.raw.deathclaw_attack);
		attackPlayer.setVolume(1.5f, 1.5f);
		//attackPlayer.setLooping(true);
	}

	/**
	 * Set listeners for the toggle switches.
	 */
	private void setToggles(){
		wallsToggle = (ToggleButton) findViewById(R.id.toggleButton1);
		wallsToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	Log.v(LOG_TAG, "Top-down button clicked");
		    		maze.keyDown('z');
		    		mazeToggle.setChecked(true);
		            // The toggle is enabled
		        } else {
		        	Log.v(LOG_TAG, "Top-down button unclicked");
		    		maze.keyDown('z');
		            // The toggle is disabled
		        }
		    }
		});
		solutionToggle = (ToggleButton) findViewById(R.id.toggleButton2);
		solutionToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	Log.v(LOG_TAG, "Show solution button clicked");
		    		maze.keyDown('s');
		    		mazeToggle.setChecked(true);
		            // The toggle is enabled
		        } else {
		        	Log.v(LOG_TAG, "Show solution button unclicked");
		    		maze.keyDown('s');
		            // The toggle is disabled
		        }
		    }
		});
		mazeToggle = (ToggleButton) findViewById(R.id.toggleButton3);
		mazeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	Log.v(LOG_TAG, "Top-down button clicked");
		    		maze.keyDown('m');
		            // The toggle is enabled
		        } else {
		        	Log.v(LOG_TAG, "Top-down button unclicked");
		    		maze.keyDown('m');
		    		solutionToggle.setChecked(false);
		    		wallsToggle.setChecked(false);
		            // The toggle is disabled
		        }
		    }
		});
		if (showSolution){
			solutionToggle.setChecked(true);
			mazeToggle.setChecked(true);
		}
		if (showWalls){
			wallsToggle.setChecked(true);
			mazeToggle.setChecked(true);
		}
		if (showMaze){
			mazeToggle.setChecked(true);
		}
		pause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				if (isChecked){
					Log.v(LOG_TAG, "Pausing");
					gamePaused = true;
				}
				else{
					Log.v(LOG_TAG, "Unpausing");
					gamePaused = false;
				}
			}
		});
	}
	
	
	/**
	 * Get the data from the previous intent.
	 */
	private void getIntentExtras() {
		Intent intent = getIntent();
		driverName = intent.getStringExtra("driver");
		showSolution = intent.getBooleanExtra("showSolution", false);
		showWalls = intent.getBooleanExtra("showWalls", false);
		showMaze = intent.getBooleanExtra("showMaze", false);
	}
	
	/**
	 * Set up the GraphicsWrapper and maze once the layout is created.
	 */
	private void setUpGraphicsWrapper() {
		layout = (RelativeLayout) findViewById(R.id.playLayout);
		layout.post(new Runnable() {
			@Override
			public void run() {
				tupac = (GraphicsWrapper) findViewById(R.id.graphicsWrapper);
				tupac.measureDimensions();
				maze.setGraphicsWrapper(tupac);
				maze.init();
				maze.getMazeView().setHandler(stateHandler);
				setToggles();
				if (!driverName.equals("Manual")) {
					robotHandler.postDelayed(moveRobot, DELAY);
				}
			}
		});
	}
	
	/**
	 * Set up the robot and driver.
	 */
	private void setUpRobotAndDriver() {
		Log.v(LOG_TAG, "Driver: " + driverName);
		robby = new BasicRobot();
		robby.setBatteryLevel(2500);
		batteryBar.setMax(2500);
		batteryBar.setProgress(2500);
		robby.setMaze(maze);
		switch (driverName){
		case "Manual":
			driver = new ManualDriver();
			break;
		case "Wall Follower":
			driver = new WallFollower();
			break;
		case "Wizard":
			driver = new Wizard();
			break;
		case "Curious Mouse":
			driver = new CuriousMouse();
			break;
		}
		driver.setRobot(robby);
		driver.setDistance(maze.getMazeDists());
		driver.setDimensions(maze.getMazeWidth(), maze.getMazeHeight());
	}
	
	/**
	 * Move to the finish screen with a success condition
	 * Pass on the battery level, path length, and finish condition in the intent
	 * @param view
	 */
	public void toFinishSuccess(View view){
		Log.v(LOG_TAG, "Moving to success screen");
		Intent intent = new Intent(this, FinishActivity.class);
		batteryLevel = driver.getEnergyConsumption();
		pathLength = driver.getPathLength();
		intent.putExtra("Energy Consumption", batteryLevel);
		intent.putExtra("Path Length", pathLength);
		intent.putExtra("foundExit", true);
		releaseAllPlayers();
		robotHandler.removeCallbacks(moveRobot);
		startActivity(intent);
		finish();
	}

	/**
	 * Move to the finish screen with a failure condition
	 * Pass on the battery level, path length, and finish condition in the intent
	 * @param view
	 */
	public void toFinishFailure(View view){
		Log.v(LOG_TAG, "Moving to fail screen");
		Intent intent = new Intent(this, FinishActivity.class);
		batteryLevel = driver.getEnergyConsumption();
		pathLength = driver.getPathLength();
		intent.putExtra("Energy Consumption", batteryLevel);
		intent.putExtra("Path Length", pathLength);
		intent.putExtra("foundExit", false);
		releaseAllPlayers();
		robotHandler.removeCallbacks(moveRobot);
		startActivity(intent);
		finish();
	}
	
	/**
	 * Update the battery bar.
	 */
	private void updateBattery() {
		int batteryLevel = (int) (batteryBar.getMax() - driver.getEnergyConsumption());
		batteryBar.setProgress(batteryLevel);
	}
	
	/**
	 * Move the robot forward.
	 * @param view the View that called the method
	 */
	public void moveForward(View view){
		final ManualDriver tempDriver = (ManualDriver) driver;
		Log.v(LOG_TAG, "Move forward");
		try {
			roar = random.nextIntWithinInterval(0, 20);
			if ((roar == 1) && (System.currentTimeMillis() - time > attackPlayer.getDuration())){
					Log.v(LOG_TAG, "Roaring");
					attackPlayer.start();
					time = System.currentTimeMillis();
			}
			mediaPlayer.start();
			tempDriver.moveForward();
			tupac.invalidate();
			updateBattery();
			mediaPlayer.pause();
			if (roar == 1){
				attackPlayer.pause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Move the robot backward.
	 * @param view the View that called the method
	 */
	public void moveBack(View view){
		final ManualDriver tempDriver = (ManualDriver) driver;
		Log.v(LOG_TAG, "Move back");
		try {
			roar = random.nextIntWithinInterval(0, 20);
			if ((roar == 1) && (System.currentTimeMillis() - time > attackPlayer.getDuration())){
					Log.v(LOG_TAG, "Roaring");
					attackPlayer.start();
					time = System.currentTimeMillis();
			}
			mediaPlayer.start();
			tempDriver.turnAround();
			tupac.invalidate();
			updateBattery();
			mediaPlayer.pause();
			if (roar == 1){
				attackPlayer.pause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Turn the robot right.
	 * @param view the View that called the method
	 */
	public void moveRight(View view){
		final ManualDriver tempDriver = (ManualDriver) driver;
		Log.v(LOG_TAG, "Turn right");
		try {
			roar = random.nextIntWithinInterval(0, 20);
			if ((roar == 1) && (System.currentTimeMillis() - time > attackPlayer.getDuration())){
				Log.v(LOG_TAG, "Roaring");
				attackPlayer.start();
				time = System.currentTimeMillis();
			}
			mediaPlayer.start();
			tempDriver.rotateRight();
			tupac.invalidate();
			updateBattery();
			mediaPlayer.pause();
			if (roar == 1){
				attackPlayer.pause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Move the robot left.
	 * @param view the View that called the method
	 */
	public void moveLeft(View view){
		final ManualDriver tempDriver = (ManualDriver) driver;
		Log.v(LOG_TAG, "Turn left");
		try {
			roar = random.nextIntWithinInterval(0, 20);
			if ((roar == 1) && (System.currentTimeMillis() - time > attackPlayer.getDuration())){
				Log.v(LOG_TAG, "Roaring");
				attackPlayer.start();
				time = System.currentTimeMillis();
			}
			mediaPlayer.start();
			tempDriver.rotateLeft();
			tupac.invalidate();
			updateBattery();
			mediaPlayer.pause();
			if (roar == 1){
				attackPlayer.pause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Release all of the MediaPlayers.
	 */
	private void releaseAllPlayers() {
		releasePlayer(mediaPlayer);
		releasePlayer(backgroundPlayer);
		releasePlayer(attackPlayer);
	}
	
	/**
	 * Release the MediaPlayer if it's in use.
	 * @param player the MediaPlayer
	 */
	private void releasePlayer(MediaPlayer player) {
		try {
			if (player.isPlaying()){
				player.pause();
				player.stop();
				player.release();
			}
		}
		catch (Exception e) {
			Log.v(LOG_TAG, "Exception thrown when releasing media player");
		}
	}
}


