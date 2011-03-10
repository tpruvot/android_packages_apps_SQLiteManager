package dk.andsen.asqlitemanager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import dk.andsen.utils.Utils;

public class SQLViewer extends Activity implements OnClickListener, Runnable {

	private String _dbPath;
	private ProgressDialog _pd;
	private FileReader _f;
	private BufferedReader _in;
	private ListView _lv;
	private static final int MENU_RUN = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sqlviewer);
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			_dbPath = extras.getString("db");
			Utils.logD("Opening SQL file " + _dbPath);
			_pd = ProgressDialog.show(this, getString(R.string.Working),
					getString(R.string.ReadingScript), true, false);
			Utils.logD("Fetching SQLListView");
	    _lv = (ListView)findViewById(R.id.SQLListView);
			ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> map;
	    try {
				_f = new FileReader(_dbPath);
				_in = new BufferedReader(_f);
				Utils.logD("Importing from; " + _dbPath);
				String nline;
				while ((nline = _in.readLine()) != null) {
					map = new HashMap<String, String>();
					map.put("Sql", nline);
					mylist.add(map);
				}
		    _in.close();
		    _f.close();
	    }  catch (Exception e) {
	    	Utils.logD("Exception!");
	    }
			Utils.logD("All lines read");
			SimpleAdapter mSchedule = new SimpleAdapter(this, mylist,
					R.layout.sql_line, new String[] {"Sql"},
					new int[] { R.id.Sql});
			Utils.logD("Adapter finished");
			
			try {
				_lv.setAdapter(mSchedule);
			} catch (Exception e) {
				e.printStackTrace();
				Utils.logE(e.toString());
			}
			Thread thread = new Thread(this);
			thread.start();
		}		
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			_pd.dismiss();
		}
	};

	
	public void onClick(View v) {
		// TODO Execute current line
		
	}

	public void run() {
		handler.sendEmptyMessage(0);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_RUN, 0, R.string.Run);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) 
	{
		String title = "";
		Dialog test = null;
		switch (id) {
		case MENU_RUN:
			Utils.logD("Creating MENU_TABLES");
			title = getText(R.string.Run).toString();

			test = new AlertDialog.Builder(this)
			.setTitle(title)
			.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler())
			.create();
		}
		return test;
	}
	
	/**
	 * @author os
	 *
	 */
	public class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		public void onClick( DialogInterface dialog, int clicked )
		{
			Utils.logD("Dialog: " + dialog.getClass().getName());
			switch(clicked)
			{
			case DialogInterface.BUTTON_POSITIVE:
				//TODO Run script
				Utils.logD("Running " + _dbPath);
				break;
			}
		}
	}

	
}