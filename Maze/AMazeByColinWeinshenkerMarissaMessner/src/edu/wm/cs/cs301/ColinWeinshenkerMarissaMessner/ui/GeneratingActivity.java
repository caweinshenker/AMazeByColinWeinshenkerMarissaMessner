package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.ui;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Distance;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.Maze;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.MazeDataHolder;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.MazeFileReader;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * This Activity represents the second screen of the app, where the maze gets generated and a driver
 * algorithm is chosen.
 * 
 * A progress bar shows how much of the maze has been generated. While it's generating, the user can
 * select the RobotDriver they want to use. They can also click toggle switches to show the maze's
 * solution, the walls, and the top-down view of the maze. When the progress bar reaches 100%,
 * a start button appears and the user can click it to start playing the maze.
 * 
 * @author caweinshenker
 * @author mamessner
 *
 */
public class GeneratingActivity extends AppCompatActivity {
	
	private static final String LOG_TAG = "GeneratingActivity";
	private ProgressBar progressBar;
	private TextView loadingText;
	private int progressBarStatus = 0;
	private Switch solutionSwitch;
	private Switch wallsSwitch;
	private Switch topDownSwitch;
	private Spinner driver;
	private Button playButton;
	private boolean loadMaze;
	private MediaPlayer attackPlayer;
	private MazeFileReader reader;
	protected static Maze maze = new Maze();
	private int mazeLevel;
	private String generationAlg;
	private Handler progressHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			progressBarStatus = msg.what;
		}
	};
	
	
	/**
	 * Set the content view, get the maze level and generation algorithm from the
	 * Intent, set the variables, and start the progress bar.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generating);
		Intent previousIntent = getIntent();
		loadMaze = previousIntent.getBooleanExtra("loadMaze", false);
		generationAlg = previousIntent.getStringExtra("generationAlgorithm");
		attackPlayer = MediaPlayer.create(this, R.raw.deathclaw_attack);
		attackPlayer.setVolume(1.5f, 1.5f);
		attackPlayer.start();
		if (loadMaze){
			Log.v(LOG_TAG, "Generating maze from file");
			String filename = previousIntent.getStringExtra("filename");
			setUpVariables();
			addSwitchListeners();
			generateMazeFromFile(filename);
		}
		// mazeLevel and generationAlgorithmh will be used later when we generate mazes
		else {
			mazeLevel = previousIntent.getIntExtra("level", 0);
			setUpVariables();
			addSwitchListeners();
			updateProgressBar();
			generateMaze();
		}
	}
	
	/**
	 * Use the MazeFileReader to construct a maze.
	 * @param filename the name of the maze file
	 */
	private void generateMazeFromFile(final String filename) {
		// Run this in a new thread to keep UI responsive
		new Thread(new Runnable() {
			@Override
			public void run() {
				reader = new MazeFileReader(filename, getApplicationContext());
				Log.v(LOG_TAG, "Reader width: " + reader.getWidth());
				Log.v(LOG_TAG, "Reader height: " + reader.getHeight());
				maze.setMazeWidth(reader.getWidth());
				maze.setMazeHeight(reader.getHeight());
				maze.setBuilder(generationAlg);
				maze.getBuilder().setDeterministic();
				maze.getBuilder().setHandler(progressHandler);
				Distance dists = new Distance(reader.getDistances());
				maze.newMaze(reader.getRootNode(), reader.getCells(), dists,
						reader.getStartX(), reader.getStartY());
				updateProgressBar();
				progressBarStatus = 100;
//				updateProgressBar();
//				maze.getBuilder().build(maze, reader.getWidth(), reader.getHeight(),
//						reader.getRooms(), reader.getExpectedPartiters());
			}
		}).start();
	}
	
	/**
	 * Create the options menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.generating, menu);
		return true;
	}

	/**
	 * Handle what happens when option items are selected. The only options item
	 * is settings.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Go back to the title screen when the back button is pressed.
	 */
	@Override
	public void onBackPressed() {
		 releasePlayer(attackPlayer);
		 Intent intent = new Intent(this, AMazeActivity.class);
		 startActivity(intent);
		 finish();
	}
	
	/**
	 * Add a listener for the driver selection Spinner.
	 */
	public void addListenerOnDriverSelection() {
		driver.setOnItemSelectedListener(new OnItemSelectedListener(){

			/**
			 * Log when a driver is selected.
			 */
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id){
				Log.v(LOG_TAG, "Driver selection: " + driver.getSelectedItem().toString());
			}

			/**
			 * Don't do anything when nothing is selected.
			 */
			@Override
			public void onNothingSelected(AdapterView<?> parentView){
				// do nothing
			}
		});
	}
	
	/**
	 * Initialize variables for the progress bars, the switches, the button, and the spinner.
	 */
	public void setUpVariables() {
		progressBar = (ProgressBar) findViewById(R.id.batteryLevelBar);
		loadingText = (TextView) findViewById(R.id.loadingText);
		solutionSwitch = (Switch) findViewById(R.id.switch3);
		wallsSwitch = (Switch) findViewById(R.id.switch2);
		topDownSwitch = (Switch) findViewById(R.id.switch1);
		driver = (Spinner) findViewById(R.id.driver);
		playButton = (Button) findViewById(R.id.play);
		playButton.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Start an Intent to move on to PlayActivity and pass it the selected RobotDriver
	 * and the values of all toggle switches. This should only be called by the play button.
	 * @param view The item that called the method
	 */
	public void toPlay(View view){
		Log.v(LOG_TAG, "Play button clicked");
		releasePlayer(attackPlayer);
		Intent intent = new Intent(this, PlayActivity.class);
		String driverChoice = driver.getSelectedItem().toString();
		boolean showSolution = solutionSwitch.isChecked();
		boolean showWalls = wallsSwitch.isChecked();
		boolean showMaze = topDownSwitch.isChecked();
		intent.putExtra("driver", driverChoice);
		intent.putExtra("showSolution", showSolution);
		intent.putExtra("showWalls", showWalls);
		intent.putExtra("showMaze", showMaze);
		updateDataHolder();
		startActivity(intent);
		finish();
	}
	
	/**
	 * Give the switches listeners so their status can be logged.
	 */
	public void addSwitchListeners(){
		solutionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.v(LOG_TAG, "Show solution: " + isChecked);
			}
		});
		
		wallsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.v(LOG_TAG, "Show walls: " + isChecked);
			}
		});
		
		topDownSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.v(LOG_TAG, "Show top-down view: " + isChecked);
			}
		});
	}
	
	
	/**
	 * Update the progress bar until it hits 100%.
	 */
	public void updateProgressBar() {
		new AsyncTask<Void, Integer, Void>(){

			/**
			 * Update the progress bar by 10% every second until it reaches 100%.
			 */
			@Override
			public Void doInBackground(Void...params){
				while (progressBarStatus < 100){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Log.v(LOG_TAG, "Progress: "+ progressBarStatus);
					publishProgress(progressBarStatus);
				}
				return null;
			}

			/**
			 * Set the progress bar's progress to the given value.
			 */
			@Override
			public void onProgressUpdate(Integer... progress) {
				progressBar.setProgress(progress[0]);
				loadingText.setText("Loading: " + progress[0] + "/100");
			}


			/**
			 * Initialize the progress bar to 0%.
			 */
			@Override
			public void onPreExecute() {
				progressBar.setProgress(0);
			}
			
			/**
			 * After doInBackground() finishes, let the user know the maze is ready and
			 * display the play button.
			 */
			@Override
			public void onPostExecute(Void result){
				progressBar.setProgress(100);
				loadingText.setText("Now run.");
				Toast.makeText(getApplicationContext(), "Maze ready", Toast.LENGTH_SHORT).show();
				playButton.setVisibility(View.VISIBLE);
			}

		}.execute();
	}
	
	/**
	 * Put maze data in the MazeDataHolder before moving on to PlayActivity.
	 */
	private void updateDataHolder() {
		MazeDataHolder.setCells(maze.getMazeCells());
		MazeDataHolder.setDistance(maze.getMazeDists().getDists());
		MazeDataHolder.setWidth(maze.getMazeWidth());
		MazeDataHolder.setHeight(maze.getMazeHeight());
		MazeDataHolder.setStartX(maze.getMazeDists().getStartPosition()[0]);
		MazeDataHolder.setStartY(maze.getMazeDists().getStartPosition()[1]);
	}
	
	/**
	 * Generate a maze using the data received from AMazeActivity.
	 */
	private void generateMaze() {
		maze.setSkill(mazeLevel);
		maze.setBuilder(generationAlg);
		maze.getBuilder().setHandler(progressHandler);
		maze.build();
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
