package dk.andsen.asqlitemanager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
				// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		
	}

	public void run() {
		handler.sendEmptyMessage(0);
	}

}