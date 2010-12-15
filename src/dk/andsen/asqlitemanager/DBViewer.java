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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import dk.andsen.utils.Utils;

public class DBViewer extends Activity implements OnClickListener {
	private static final int MENU_TABLES = 0;
	private static final int MENU_FIELDS = 1;
	private String _dbPath;
	private Database _db = null;
	private String[] tables;
	private String[] views;
	private ListView list;
	private LinearLayout query;
	private EditText tvQ;
	private Button btR;
	private String[] toList;
	private Context _cont;
	private boolean[] listOfTables_selected;
	private String[] listOfTables;
	private boolean[] listOfFields_selected;
	private String[] listOfFields;
//	private int offset = 0;
//	private int limit = 20;
	
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
		tvQ = (EditText) this.findViewById(R.id.SQLStm);
		btR = (Button) this.findViewById(R.id.Run);
		btR.setOnClickListener(this);
		// Hide query panel
		tvQ.setVisibility(View.GONE);
		btR.setVisibility(View.GONE);
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
			} else {
				Utils.logD("Database open");
				tables = _db.getTables();
				views = _db.getViews();
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
		if (type.equals("SQL")) {
			toList = _db.getSQLQuery(tvQ.getText().toString());
			boolean save = Prefs.getSaveSQL(_cont);
			if (save)
				_db.saveSQL(tvQ.getText().toString());
		}
		else if (type.equals("Clear"))
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
			setDisplay("List");
			buildList("Tables");
		} else if (key == R.id.Views) {
			setDisplay("List");
			buildList("Views");
		} else if (key == R.id.Index) {
			setDisplay("List");
			buildList("Index");
		} else if (key == R.id.Query) {
			setDisplay("Query");
			buildList("Clear");
		} else if (key == R.id.Run) {
			buildList("SQL");
		}
	}
	
	/**
	 * Toggle display mode
	 * @param type
	 */
	private void setDisplay(String type) {
		if (type.equals("Query")) {
			tvQ.setVisibility(View.VISIBLE);
			btR.setVisibility(View.VISIBLE);
			query.setVisibility(View.VISIBLE);
		} else {
			tvQ.setVisibility(View.GONE);
			btR.setVisibility(View.GONE);
			query.setVisibility(View.GONE);
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_TABLES, 0, R.string.DBTables);
		menu.add(0, MENU_FIELDS, 0, R.string.DBFields);
		//menu.add(0, MENU_LOAD, 0, R.string.Load).setIcon(R.drawable.ic_menu_load);
		//menu.add(0, MENU_OPT, 0, R.string.Option).setIcon(R.drawable.ic_menu_preferences);
		//menu.add(0, MENU_PRGS, 0, R.string.Progs).setIcon(R.drawable.ic_menu_compose);
		//menu.add(0, MENU_RESET, 0, R.string.Reset).setIcon(R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_TABLES:
			showDialog( 0 );
			break;
		case MENU_FIELDS:
			showDialog( 1 );
			break;
			
		}
		return false;
	}
		
	@Override
	protected Dialog onCreateDialog( int id ) 
	{
		CharSequence[] posts = null;
		boolean[] post_selected = null;
		String title = "";
		switch (id) {
		case 0:
			title = getText(R.string.DBTables).toString();
			listOfTables = _db.getTables();
			listOfTables_selected = new boolean[listOfTables.length];
			posts = listOfTables; 
			post_selected = listOfTables_selected;
			
			break;
		case 1:
			title = getText(R.string.DBViews).toString();
			//count selected tables
			int selTables = 0;
			for (boolean sel: listOfTables_selected) {
				if (sel)
				  selTables++;
			}
			String[] tables = new String[selTables];
			selTables = 0;
			for (int i = 0; i < listOfTables.length; i++) {
				if (listOfTables_selected[i]) {
					tables[selTables] = listOfTables[i];
				  selTables++;
				}
			}
			listOfFields = _db.getTablesFieldsNames(listOfTables);
			listOfFields_selected = new boolean[listOfFields.length];
			posts = listOfFields; 
			post_selected = listOfFields_selected;
			break;
		}
		return 
		new AlertDialog.Builder( this )
		.setTitle(title)
		.setMultiChoiceItems( posts, post_selected, new DialogSelectionClickHandler() )
		.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler() )
		.create();
	}
	
	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
		public void onClick( DialogInterface dialog, int clicked, boolean selected )
		{
			//Log.d( "aWine", _grapes[ clicked ] + " selected: " + selected );
		}
	}
	
	public class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		public void onClick( DialogInterface dialog, int clicked )
		{
			switch( clicked )
			{
			case DialogInterface.BUTTON_POSITIVE:
				String sql = buildSQL();
				tvQ.setText(sql);
				//printSelectedGrapes();
				break;
			}
		}
		int i = 0;
		private String buildSQL() {
			String sql = "";
			if (listOfFields == null)
				sql = "select * \nfrom ";
			else {
				sql = "select ";
				for (i= 0; i < listOfFields.length; i++) {
					if (listOfFields_selected[i]) {
							sql += listOfFields[i]+ ", ";
					}
				}
				sql = sql.substring(0, sql.length() - 2);
				sql += "\nfrom ";
			}
			if (listOfTables != null) {
				for (i = 0; i < listOfTables.length; i++) {
					if (listOfTables_selected[i]) {
						sql += listOfTables[i] + ", ";
					}
				}
				sql = sql.substring(0, sql.length() - 2);
			}
			return sql;
		}
	}

}