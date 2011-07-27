package dk.andsen.asqlitemanager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.TextView;
import dk.andsen.utils.Utils;

public class ProgressBarTest extends Activity {
	private ProgressBar myProgressBar;
	private int myProgress = 0;
	private TextView progressTable;

	//progress dialog should count from 0 to 100 for table def, data, idex, views
	// Should be incorporated in Dataabse.java
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.logD("Loading progress bar");
		setContentView(R.layout.progressbar);
		myProgressBar=(ProgressBar)findViewById(R.id.progressbar_Horizontal);
		progressTable = (TextView)findViewById(R.id.ProgressTable);
		Utils.logD("Statting progress bar");
		new Thread(myThread).start();
	}

	private Runnable myThread = new Runnable(){
		public void run() {
			while (myProgress<100){
				try{
					myHandle.sendMessage(myHandle.obtainMessage());
					Thread.sleep(1000);
				}
				catch(Throwable t){
				}
			}
			Utils.logD("Finish!!!");
			try {
				finish();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Handler myHandle = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				myProgress += 10;
				myProgressBar.setProgress(myProgress);
				progressTable.setText("Table " + myProgress);
			}
		};
		
	};
}