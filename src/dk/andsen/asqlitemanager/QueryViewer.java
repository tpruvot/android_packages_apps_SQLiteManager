/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import dk.andsen.utils.Utils;

public class QueryViewer extends Activity implements OnClickListener{

	private static final int MENU_TABLES = 0;
	private static final int MENU_FIELDS = 1;
	private static final int MENU_QUERYTYPE = 2;
	private static final int QUERYTYPE_SELECT = 0;
	private static final int QUERYTYPE_CREATEVIEW = 1;
	private static final int QUERYTYPE_CREATETABLE = 2;
	private static final int QUERYTYPE_DROPTABLE = 3;
	private static final int QUERYTYPE_DROPVIEW = 5;
	private static final int QUERYTYPE_DELETE = 6;
	private String[] _queryTypes = new String[]
    {"Select", "Create view" ,"Create table", "Drop table", "Drop view", "Delete from"};
	private EditText _tvQ;
	private Button _btR;
	private Context _cont;
	private String _dbPath;
	private Database _db;
	private int _offset = 0;
	private int _limit;
	private TableLayout _aTable;
	private boolean _save;
	private boolean[] listOfTables_selected;
	private String[] listOfTables;
	private boolean[] listOfFields_selected;
	private String[] listOfFields;
	private Button bUp;
	private Button bDwn;
	private int _queryType = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.query_viewer);
		_tvQ = (EditText) this.findViewById(R.id.SQLStm);
		_btR = (Button) this.findViewById(R.id.Run);
		_btR.setOnClickListener(this);
		_cont = _tvQ.getContext();
		_save = Prefs.getSaveSQL(_cont);
		_limit = Prefs.getPageSize(_cont);
		bUp = (Button) this.findViewById(R.id.PgUp);
		bDwn = (Button) this.findViewById(R.id.PgDwn);
		bUp.setOnClickListener(this);
		bDwn.setOnClickListener(this);
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			_cont = _tvQ.getContext();
			_dbPath = extras.getString("db");
			Utils.logD("Opening database");
			_db = new Database(_dbPath, _cont);
			if (!_db.isDatabase) {
				Utils.logD("Not a database!");
				Utils.showException(_dbPath + " is not a database!", _cont);
				finish();
			} else {
				Utils.logD("Database open");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		int key = v.getId();
		String sql = _tvQ.getText().toString();
		Utils.logD("Offset: " + _offset);
		Utils.logD("Limit: " + _limit);
		if (!sql.equals(""))
		if (key == R.id.Run) {
			QueryResult result = _db.getSQLQueryPage(sql, _offset, _limit);
			if (_save)
				_db.saveSQL(_tvQ.getText().toString());
			_aTable=(TableLayout)findViewById(R.id.datagrid);
			setTitles(_aTable, result.getColumnNames());
			appendRows(_aTable, result.getData());			
		}  else if (key == R.id.PgDwn) {
			int childs = _aTable.getChildCount();
			Utils.logD("Table childs: " + childs);
			if (childs >= _limit) {  //  No more data on to display - no need to PgDwn
				_offset += _limit;
				String [] nn = {};
				setTitles(_aTable, nn);
				QueryResult result = _db.getSQLQueryPage(sql, _offset, _limit);
				setTitles(_aTable, result.getColumnNames());
				appendRows(_aTable, result.getData());
			}
			Utils.logD("PgDwn:" + _offset);
		} else if (key == R.id.PgUp) {
			_offset -= _limit;
			if (_offset < 0)
				_offset = 0;
			QueryResult result = _db.getSQLQueryPage(sql, _offset, _limit);
			setTitles(_aTable, result.getColumnNames());
			appendRows(_aTable, result.getData());
			Utils.logD("PgUp: " + _offset);
		}
	}

	/**
	 * Add a String[] as titles
	 * @param table
	 * @param titles
	 */
	private void setTitles(TableLayout table, String[] titles) {
		int rowSize=titles.length;
		table.removeAllViews();
		TableRow row = new TableRow(this);
		row.setBackgroundColor(Color.BLUE);
		for(int i=0; i<rowSize; i++){
			TextView c = new TextView(this);
			c.setText(titles[i]);
			c.setPadding(3, 3, 3, 3);
			row.addView(c);
		}
		table.addView(row, new TableLayout.LayoutParams());
	}

	/**
	 * Add a String[][] list to the table layout as rows
	 * @param table
	 * @param data
	 */
	private void appendRows(TableLayout table, String[][] data) {
		if (data == null)
			return;
		int rowSize=data.length;
		int colSize=(data.length>0)?data[0].length:0;
		for(int i=0; i<rowSize; i++){
			TableRow row = new TableRow(this);
			row.setOnClickListener(new OnClickListener() {
			   public void onClick(View v) {
			      // button 1 was clicked!
			  	 Utils.logD("OnClick: " + v.getId());
			   }
			  });

			if (i%2 == 1)
				row.setBackgroundColor(Color.DKGRAY);
			for(int j=0; j<colSize; j++){
				TextView c = new TextView(this);
				c.setText(data[i][j]);
				c.setPadding(3, 3, 3, 3);
				//				if (j%2 == 1)
				//					if (i%2 == 1)
				//						c.setBackgroundColor(Color.BLUE);
				//					else
				//						c.setBackgroundColor(Color.BLUE & Color.GRAY);
				c.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
				      // button 1 was clicked!
				  	 Utils.logD("OnClick: " + v.getId());
				  	 String text = (String)((TextView)v).getText();
				  	 ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				  	 clipboard.setText(text);
				  	 Utils.toastMsg(_cont, "Text copied to clip board");
				   }
				  });
				row.addView(c);
			}
			table.addView(row, new TableLayout.LayoutParams());
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_TABLES:
			showDialog( MENU_TABLES );
			break;
		case MENU_FIELDS:
			showDialog( MENU_FIELDS );
			break;
		case MENU_QUERYTYPE:
			showDialog( MENU_QUERYTYPE );
			break;
		}
		return false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		//menu.clear();
		//menu.
		// kan menuen slettes og genoprettes når der f.eks. kommer nye tabeller?
		menu.add(0, MENU_TABLES, 0, R.string.DBTables);
		menu.add(0, MENU_FIELDS, 0, R.string.DBFields);
		menu.add(0, MENU_QUERYTYPE, 0, R.string.DBQueryType);
		//menu.add(0, MENU_LOAD, 0, R.string.Load).setIcon(R.drawable.ic_menu_load);
		//menu.add(0, MENU_OPT, 0, R.string.Option).setIcon(R.drawable.ic_menu_preferences);
		//menu.add(0, MENU_PRGS, 0, R.string.Progs).setIcon(R.drawable.ic_menu_compose);
		//menu.add(0, MENU_RESET, 0, R.string.Reset).setIcon(R.drawable.ic_menu_close_clear_cancel);
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog( int id ) 
	{
		CharSequence[] posts = null;
		boolean[] post_selected = null;
		String title = "";
		switch (id) {
		case MENU_TABLES:
			title = getText(R.string.DBTables).toString();
			listOfTables = _db.getTables();
			listOfTables_selected = new boolean[listOfTables.length];
			posts = listOfTables; 
			post_selected = listOfTables_selected;
			return 
			new AlertDialog.Builder( this )
			.setTitle(title)
			.setMultiChoiceItems( posts, post_selected, new DialogSelectionClickHandler() )
			.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler() )
			.create();
		case MENU_FIELDS:
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
			return 
			new AlertDialog.Builder( this )
			.setTitle(title)
			.setMultiChoiceItems( posts, post_selected, new DialogSelectionClickHandler() )
			.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler() )
			.create();
		case MENU_QUERYTYPE:
			posts = _queryTypes; 
			return 
			new AlertDialog.Builder( this )
			.setTitle(title)
			.setSingleChoiceItems(posts, 0, new QueryTypeOnClickHandler() )
			//.setMultiChoiceItems( posts, post_selected, new DialogSelectionClickHandler() )
			//.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler() )
			.create();
		}
		return 
		new AlertDialog.Builder( this )
		.setTitle(title)
		.setMultiChoiceItems( posts, post_selected, new DialogSelectionClickHandler() )
		.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler() )
		.create();
	}

	/**
	 * @author os
	 *
	 */
	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
		public void onClick( DialogInterface dialog, int clicked, boolean selected )
		{
			//Log.d( "aWine", _grapes[ clicked ] + " selected: " + selected );
		}
	}
	
	/**
	 * @author os
	 *
	 */
	public class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		public void onClick( DialogInterface dialog, int clicked )
		{
			switch( clicked )
			{
			case DialogInterface.BUTTON_POSITIVE:
				String sql = buildSQL();
				_tvQ.setText(sql);
				//printSelectedGrapes();
				break;
			}
		}
	}
	
	/**
	 * Handles the click on the query type dialog
	 * @author andsen
	 *
	 */
	public class QueryTypeOnClickHandler implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			_queryType = which;
			//Utils.showMessage("qtype", "" + _queryType, _cont);
			dialog.dismiss();
			String sql = buildSQL();
			_tvQ.setText(sql);
		}
	}
	
	/**
	 * Build the SQL statement
	 * @return
	 */
	private String buildSQL() {
		String sql = "";
		switch (_queryType) {
		case QUERYTYPE_SELECT:
			sql = buildSelectSQL();
			break;
		case QUERYTYPE_CREATETABLE:
			sql = buildCreateTableSQL();
			//TODO does this update the menu??
			//TODO
			onCreateDialog(MENU_TABLES);
			break;
		case QUERYTYPE_CREATEVIEW:
			sql = buildCreateViewSQL();
			break;
		case QUERYTYPE_DELETE:
			sql = buildDeleteSQL();
			break;
		case QUERYTYPE_DROPTABLE:
			sql = buildDropTableSQL();
			break;
		case QUERYTYPE_DROPVIEW:
			sql = buildDropViewSQL();
			break;
		default:
			break;
		}
		return sql;
	}


	private String buildSelectSQL() {
		int i = 0;
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

	private String buildDropTableSQL() {
		String sql = "Drop table ";
		// Drop first of the selected tables
		for (int i= 0; i < listOfTables.length; i++) {
			if (listOfTables_selected[i]) {
					sql += listOfTables[i];
					break;
			}
		}
		return sql;
	}

	private String buildDropViewSQL() {
		String sql = "Drop view viewName";
		return sql;
	}

	private String buildDeleteSQL() {
		String sql = "Delete from  ";
		for (int i= 0; i < listOfTables.length; i++) {
			if (listOfTables_selected[i]) {
					sql += listOfTables[i] + " where ";
					break;
			}
		}
		for (int i= 0; i < listOfFields.length; i++) {
			if (listOfFields_selected[i]) {
					sql += listOfFields[i]+ " = 'xxx'";
					break;
			}
		}
		return sql;
	}

	private String buildCreateViewSQL() {
		String sql = "Create view ViewName as ";
		sql += buildSelectSQL();
		return sql;
	}

	private String buildCreateTableSQL() {
		String sql = "Create tables TableName (feild1 f1type, feild2 f2type)";
		return sql;
	}

}
