package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.ui;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.MazeDataHolder;

/**
 * This activity represents the title screen of the maze app. It has welcome text, a SeekBar to
 * select the skill level for the maze, and a Spinner to select the maze generation method.
 * 
 * The user has the option to select any of the maze generation methods (default, Eller, or Prim)
 * or to load the maze from a file. One maze file is saved per skill level. If there is no maze file
 * for that skill level, one will be generated and saved.
 * 
 * The user presses a start button to start generating the maze and move on to GeneratingActivity.
 * 
 * @author caweinshenker
 * @author mamessner
 *
 */
public class AMazeActivity extends AppCompatActivity {

	private SeekBar seekBar;
	private TextView textView;
	private Spinner spinner;
	private MediaPlayer mediaPlayer;
	private static final String LOG_TAG = "AMazeActivity";

	/**
	 * Set the content view and initialize class variables.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_amaze);
		initializeVariables();
		mediaPlayer = MediaPlayer.create(this, R.raw.fo3ending_segment03);
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}

	/**
	 * Create the options menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.amaze, menu);
		return true;
	}

	/**
	 * Check to see which option was selected. The only option is settings.
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
	 * Stop playing music when the back button is pressed.
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		releasePlayer();
	}
	
//	/**
//	 * Stop playing music when the app isn't visible.
//	 */
//	@Override
//	public void onPause() {
//		super.onPause();
//		if (mediaPlayer.isPlaying()) {
//			mediaPlayer.pause();
//		}
//	}
//	
	/**
	 * Start playing music when the app is visible.
	 */
	@Override
	public void onResume() {
		mediaPlayer.start();
		super.onResume();
	}
	
	/**
	 * Access the file system via android and read in a maze from XML using MazeFileWriter
	 * Then create an intent and pass the specification to the the GeneratingActivity
	 */
	public void loadMaze(View view){
		int mazeLevel = seekBar.getProgress();
		String generationAlgorithm = spinner.getSelectedItem().toString();
		String filename = "Maze_level_" + mazeLevel + "_" +generationAlgorithm + ".xml";
		File file = getApplicationContext().getFileStreamPath(filename);
		if (!file.exists()) {
			Toast.makeText(getApplicationContext(), "Maze file doesn't exist", Toast.LENGTH_SHORT).show();
		}
		else {
			Log.v(LOG_TAG, "Reading from file " + filename);
			Intent intent = new Intent(this, GeneratingActivity.class);
			intent.putExtra("loadMaze", true);
			intent.putExtra("filename", filename);
			intent.putExtra("generationAlgorithm", generationAlgorithm);
			startActivity(intent);
			releasePlayer();
			finish();
		}
	}
	
	/**
	 * Create an intent to move to GenerationActivity and give it the selected
	 * skill level and maze generation algorithm.
	 * Set the appropriate fields in the MazeDataHolder
	 * @param view The item that called the method
	 */
	public void generateMaze(View view){
		Log.v(LOG_TAG, "Start button clicked");
		Intent intent = new Intent(this, GeneratingActivity.class);
		intent.putExtra("loadMaze", false);
		int mazeLevel = seekBar.getProgress();
		intent.putExtra("level", mazeLevel);
		MazeDataHolder.setSkill(mazeLevel);
		String generationAlgorithm = spinner.getSelectedItem().toString();
		MazeDataHolder.setGenerationAlgorithm(generationAlgorithm);
		intent.putExtra("generationAlgorithm", generationAlgorithm);
		releasePlayer();
		startActivity(intent);
		finish();
	}
	
	/**
	 * Release the media player.
	 */
	private void releasePlayer() {
		try {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		catch (Exception e) {
			Log.v(LOG_TAG, "Reached exception when releasing media player");
		}
	}
	
	/**
	 * Initialize the SeekBar and TextView objects.
	 * The SeekBar has a listener that prints to 
	 */
	private void initializeVariables() {
		seekBar = (SeekBar) findViewById(R.id.seekBar1);
		textView = (TextView) findViewById(R.id.finishText);
		spinner = (Spinner) findViewById(R.id.algorithm);
		setSeekBarListener();
		addListenerOnAlgorithmSelection();
	}
	
	/**
	 * Add a listener for the skill level SeekBar. When the SeekBar's progress changes,
	 * a message is written to Log.v.
	 */
	private void setSeekBarListener() {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			int progress = 0;

			/**
			 * When the SeekBar's progress changes, log it and print a Toast saying that the progress changed.
			 */
			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser){
				progress = progressValue;
				textView.setText("Skill Level: " + progress + "/" + seekBar.getMax());
			}

			/**
			 * When the user touches the SeekBar, print a Toast.
			 */
			@Override
			public void onStartTrackingTouch(SeekBar seekBar){
				// do nothing
			}

			/**
			 * When the user stops touching the SeekBar, change the text above it to show the selected value.
			 */
			@Override
			public void onStopTrackingTouch(SeekBar seekBar){
				Log.v(LOG_TAG, "Selected skill level " + progress);
			}
		});
	}
	
	/**
	 * Add a listener for the maze generation algorithm Spinner.
	 * Write to the log file when an algorithm is selected.
	 */
	public void addListenerOnAlgorithmSelection() {
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			/**
			 * Log the selected algorithm.
			 */
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id){
				Log.v(LOG_TAG, "Maze generation algorithm selection: " + spinner.getSelectedItem().toString());
			}

			/**
			 * Do nothing if nothing is selected.
			 */
			@Override
			public void onNothingSelected(AdapterView<?> parentView){
				// do nothing
			}
		});
	}
}
