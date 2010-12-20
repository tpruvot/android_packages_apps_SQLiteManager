package dk.andsen.asqlitemanager;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import dk.andsen.utils.Utils;

public class DBViewer extends Activity implements OnClickListener {
	private String _dbPath;
	private Database _db = null;
	private String[] tables;
	private String[] views;
	private String[] indexes;
	private ListView list;
	private LinearLayout query;
	private String[] toList;
	private Context _cont;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dbviewer);
		TextView tvDB = (TextView)this.findViewById(R.id.DatabaseToView);
		Button bTab = (Button) this.findViewById(R.id.Tables);
		Button bVie = (Button) this.findViewById(R.id.Views);
		Button bInd = (Button) this.findViewById(R.id.Index);
		Button bQue = (Button) this.findViewById(R.id.Query);
		query = (LinearLayout) this.findViewById(R.id.QueryFrame);
		query.setVisibility(View.GONE);
		bTab.setOnClickListener(this);
		bVie.setOnClickListener(this);
		bInd.setOnClickListener(this);
		bQue.setOnClickListener(this);
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			_cont = tvDB.getContext();
			_dbPath = extras.getString("db");
			tvDB.setText(getText(R.string.Database) + ": " + _dbPath);
			Utils.logD("Opening database");
			_db = new Database(_dbPath, _cont);
			if (!_db.isDatabase) {
				Utils.logD("Not a database!");
				Utils.showException(_dbPath + " is not a database!", _cont);
				try {
					finalize();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				Utils.logD("Database open");
				tables = _db.getTables();
				views = _db.getViews();
				indexes = _db.getIndex();
				for(String str: tables) {
					Utils.logD("Table: " + str);
				}
				for(String str: views) {
					Utils.logD("View: " + str);
				}
				list = (ListView) findViewById(R.id.LVList);
				buildList("Tables");
			}
		}
	}
	
	@Override
	protected void onPause() {
		_db.close();
		super.onPause();
	}

	@Override
	protected void onRestart() {
		_db = new Database(_dbPath, _cont);
		super.onRestart();
	}

	/**
	 * @param type
	 */
	private void buildList(final String type) {
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		if (type.equals("Clear"))
			toList = new String [] {};
		else if (type.equals("Index"))
			toList = _db.getIndex();
		else if (type.equals("Views")) 
			toList = _db.getViews();
		else 
			toList = _db.getTables();
		int recs = toList.length;
		for (int i = 0; i < recs; i++) {
			map = new HashMap<String, String>();
			map.put("name", toList[i]);
			mylist.add(map);
		}
		SimpleAdapter mSchedule = new SimpleAdapter(this, mylist, R.layout.row,
				new String[] {"name"}, new int[] {R.id.rowtext});
		list.setAdapter(mSchedule);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// Do something with the table / view / index clicked on
				selectRecord(type, position);
			}
		});
	}

	/**
	 * @param type
	 * @param position
	 */
	protected void selectRecord(String type, int position) {
		String name;
		name = toList[position];
		Utils.logD("Handle: " + type + " " + name);
		if (type.equals("Index")) {
			// TODO do not work with sqlite_autoindex
			String indexDef = "";
			if (indexes[position].startsWith("sqlite_autoindex_"))
				indexDef = (String) this.getText(R.string.AutoIndex);
			else
			  indexDef = _db.getIndexDef(indexes[position]);
	  	ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	  	clipboard.setText(indexDef);
	  	Utils.showMessage(this.getString(R.string.Message), indexDef, _cont);
	  	Utils.toastMsg(_cont, "Index definition copied to clip board");
			Utils.logD("IndexDef; " + indexDef);
		}
		else if (type.equals("Views")) {
			Intent i = new Intent(this, TableViewer.class);
			i.putExtra("db", _dbPath);
			i.putExtra("Table", name);
			i.putExtra("type", Types.VIEW);
			startActivity(i);
		}
		else if (type.equals("Tables")){
			Intent i = new Intent(this, TableViewer.class);
			i.putExtra("db", _dbPath);
			i.putExtra("Table", name);
			i.putExtra("type", Types.TABLE);
			startActivity(i);
		}
	}

	public void onClick(View v) {
		int key = v.getId();
		if (key == R.id.Tables) {
			buildList("Tables");
		} else if (key == R.id.Views) {
			buildList("Views");
		} else if (key == R.id.Index) {
			buildList("Index");
		} else if (key == R.id.Query) {
			Intent i = new Intent(this, QueryViewer.class);
			i.putExtra("db", _dbPath);
			startActivity(i);
		} 
	}
	
	/* (non-Javadoc)
	 * Update the lists to ensure new tables (created in query mode) and indexes
	 * are retrieved
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	public void onWindowFocusChanged(boolean hasFocus) {
		Utils.logD("Focus changed: " + hasFocus);
		if(hasFocus) {
			tables = _db.getTables();
			views = _db.getViews();
			indexes = _db.getIndex();
			buildList("Tables");
		}
	}
}