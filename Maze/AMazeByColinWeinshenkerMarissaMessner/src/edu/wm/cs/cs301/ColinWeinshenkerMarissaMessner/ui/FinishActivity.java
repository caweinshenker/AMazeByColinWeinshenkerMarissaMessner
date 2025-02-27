package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.ui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.MazeDataHolder;
import edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad.MazeFileWriter;


/**
 * This class supports functionality for the finish state of the maze
 * Textviews show the user the condition under which the play state terminated
 * They should also show the robot's path length and remaining battery level
 * A button allows the user to save the maze for future use
 * Exiting this activity returns the user to the title state
 * @author cweinshenker
 * @author mamessner
 */
public class FinishActivity extends Activity {

	private static final String LOG_TAG = "FinishActivity";
	private boolean foundExit;
	private float energyConsumption;
	private int pathLength;
	private Intent previousIntent;
	private MediaPlayer mediaPlayer;
	RelativeLayout relativeLayout; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finish);
		relativeLayout = (RelativeLayout) findViewById(R.id.finishLayout);
		previousIntent = getIntent();
		setFinishText();
		setBatteryLevelText();
		setPathLengthText();
	}
	
	/**
	 * Return the user to the title screen if the back button is pressed.
	 */
	@Override
	public void onBackPressed() {
		releasePlayer();
		Intent intent = new Intent(this, AMazeActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * Handle action bar item clicks here. The action bar will
	 * automatically handle clicks on the Home/Up button, so long
	 * as you specify a parent activity in AndroidManifest.xml.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Inflate the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.finish, menu);
		return true;
	}
//	
//	/**
//	 * Pause the music when the app isn't visible.
//	 */
//	@Override
//	public void onPause() {
//		super.onPause();
//		if (mediaPlayer.isPlaying()) {
//			mediaPlayer.pause();
//		}
//	}
	
//	/**
//	 * Start the music when the app comes back into view.
//	 */
//	@Override
//	public void onResume() {
//		super.onResume();
//		mediaPlayer.start();
//	}
//	
	
	/**
	 * Release the MediaPlayer.
	 */
	private void releasePlayer() {
		try {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
			mediaPlayer.stop();
			mediaPlayer.release();
		} catch (Exception e) {
			Log.v(LOG_TAG, "Reached exception when releasing media player");
		}
	}
	/**
	 * Save the current maze to disk.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws IllegalStateException 
	 * @throws IllegalArgumentException 
	 * @throws TransformerException 
	 */
	public void saveMaze(View view) {
		// Run this in a separate thread to keep the UI responsive
		new Thread(new Runnable() {
			@Override
			public void run() {
				String filename = "Maze_level_" + MazeDataHolder.getSkill() + "_" + MazeDataHolder.getGenerationAlgorithm() + ".xml";
				Log.v(LOG_TAG, "Writing to file " + filename);
				FileOutputStream fos = null;
				try {
					fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				try {
					MazeFileWriter.store(filename, MazeDataHolder.getWidth(), MazeDataHolder.getHeight(), MazeDataHolder.getRooms(),
										MazeDataHolder.getExpectedPartiters(), MazeDataHolder.getRoot(), MazeDataHolder.getCells(),
										MazeDataHolder.getDistance(), MazeDataHolder.getStartX(), MazeDataHolder.getStartY(), fos);
				} catch (IllegalArgumentException | IllegalStateException | IOException | ParserConfigurationException
						| TransformerException e) {
					e.printStackTrace();
				}
			}
		}).start();
		Log.v(LOG_TAG, "Maze saved");
		Toast.makeText(getApplicationContext(), "Maze saved", Toast.LENGTH_SHORT).show();				
	}
	
	/**
	 * Set the main TextView based on whether or not the exit was found.
	 */
	public void setFinishText() {
		foundExit = previousIntent.getBooleanExtra("foundExit", false);
		TextView finishText = (TextView) findViewById(R.id.finishText);
		Log.v(LOG_TAG, "Exit found: " + foundExit);
		if (foundExit) {
			finishText.setText("Congratulations!");
			relativeLayout.setBackgroundResource(R.drawable.fallout_vaultboy_24dp);
			mediaPlayer = MediaPlayer.create(this, R.raw.mus_success);
			mediaPlayer.start();
		}
		else {
			finishText.setText("You failed!");
			relativeLayout.setBackgroundResource(R.drawable.fallout4_vaultboy_failure_24dp);
			mediaPlayer = MediaPlayer.create(this, R.raw.death);
			mediaPlayer.start();
		}
	}
	
	/**
	 * Set the text view displaying the robot's remaining battery.
	 */
	public void setBatteryLevelText() {
		energyConsumption = previousIntent.getFloatExtra("Energy Consumption", 0);
		Log.v(LOG_TAG, "Energy consumption: " + energyConsumption);
		TextView batteryLevelText = (TextView) findViewById(R.id.batteryLevel);
		batteryLevelText.setText("Energy Consumption: " + energyConsumption);
	}
	
	/**
	 * Set the text view displaying the robot's path length
	 */
	public void setPathLengthText() {
		pathLength = previousIntent.getIntExtra("Path Length", 0);
		Log.v(LOG_TAG, "Path Length: " + pathLength);
		TextView pathLengthText = (TextView) findViewById(R.id.pathLength);
		pathLengthText.setText("Path length: " + pathLength);
	}
}
