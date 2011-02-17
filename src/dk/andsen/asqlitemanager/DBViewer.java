/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import dk.andsen.utils.Types;
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
	private boolean _update = false;
	private final int MENU_EXPORT = 0;
	private final int MENU_RESTORE = 1;
	
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
				Utils.showException(_dbPath + getText(R.string.IsNotADatabase), _cont);
				try {
					finalize();
				} catch (Throwable e) {
					Utils.logD(e.getMessage());
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
	 * Build / rebuild the lists with tables, views and indexes
	 * @param type
	 */
  //TODO change type to private static final int DISPMODE_INDEX = 0 ...;
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
	 * Handle the the item clicked on
	 * @param type the type of list
	 * @param position Number of item in the list
	 */
	protected void selectRecord(String type, int position) {
		String name;
		name = toList[position];
		Utils.logD("Handle: " + type + " " + name);
		if (type.equals("Index")) {
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

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		int key = v.getId();
		if (key == R.id.Tables) {
			buildList("Tables");
		} else if (key == R.id.Views) {
			buildList("Views");
		} else if (key == R.id.Index) {
			buildList("Index");
		} else if (key == R.id.Query) {
			_update = true;
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
		if(hasFocus & _update) {
			_update = false;
			tables = _db.getTables();
			views = _db.getViews();
			indexes = _db.getIndex();
			buildList("Tables");
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_EXPORT, 0, getText(R.string.Export));
		menu.add(0, MENU_RESTORE, 0, getText(R.string.Restore));
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_EXPORT:
			showDialog(MENU_EXPORT);
			break;
		case MENU_RESTORE:
			showDialog(MENU_RESTORE);
			break;
		}
		return false;
	}
	
	protected Dialog onCreateDialog(int id) 
	{
		String title = "";
		switch (id) {
		case MENU_EXPORT:
			Utils.logD("Creating MENU_EXPORT");
			title = getText(R.string.Export).toString();
			Dialog export = new AlertDialog.Builder(this)
					.setTitle(title)
					.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler())
					.setNegativeButton(getText(R.string.Cancel), null)
					.create();
			return export;
		case MENU_RESTORE:
			// Utils.showMessage("Not Implemented",
			// "Restore of databases not implemented yet", _cont);
			title = getText(R.string.Restore).toString() + "\nnot implemented";
			Dialog restore = new AlertDialog.Builder(this).setTitle(title)
					.setPositiveButton(getText(R.string.OK), null)
					.setNegativeButton(getText(R.string.Cancel), null)
					// .setPositiveButton(getText(R.string.OK), new
					// DialogButtonClickHandler())
					.create();
			return restore;
		}
		return null;
	}

	public class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		public void onClick( DialogInterface dialog, int clicked )
		{
			Utils.logD("Dialog: " + dialog.getClass().getName());
			switch(clicked)
			{
			case DialogInterface.BUTTON_POSITIVE:
				Utils.logD("OK pressed");
				_db.exportDatabase();
				break;
			}
		}
	}


}