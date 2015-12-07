package edu.wm.cs.cs301.ColinWeinshenkerMarissaMessner.falstad;

import android.os.Handler;
import android.os.Message;

public class MazeView extends DefaultViewer {

	Maze maze ; // need to know the maze model to check the state
	Handler handler;
	
	public MazeView(Maze m) {
		super() ;
		maze = m;
	}

	@Override
	public void redraw(GraphicsWrapper wrapper, int state, int px, int py, int view_dx,
			int view_dy, int walk_step, int view_offset, RangeSet rset, int ang) {
		switch (state) {
		case Constants.STATE_TITLE:
			// handled in AMazeActivity
			break;
		case Constants.STATE_GENERATING:
			// handled in GeneratingActivity
			break;
		case Constants.STATE_PLAY:
			// skip this one
			break;
		case Constants.STATE_FINISH:
			handler.sendMessage(Message.obtain(handler, Constants.STATE_FINISH));
			break;
		case Constants.STATE_FAILURE:
			handler.sendMessage(Message.obtain(handler, Constants.STATE_FAILURE));
			break;
		}
	}
	
	private void dbg(String str) {
		System.out.println("MazeView:" + str);
	}
	
	/**
	 * Set the handler used to notify the app about maze success or failure.
	 * @param handler
	 */
	public void setHandler(Handler handler){
		this.handler = handler;
	}

}
