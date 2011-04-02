/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * Show informations and data for tables and views
 *
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import dk.andsen.types.Types;
import dk.andsen.utils.Utils;

public class TableViewer extends Activity implements OnClickListener {
	private String _dbPath;
	private Database _db = null;
	private String _table;
	Context _cont;
	//private String _type = "Fields";
	private TableLayout _aTable;
	private int offset = 0;
	private int limit = 15;
	Button bUp;
	Button bDwn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_viewer);
		TextView tvDB = (TextView)this.findViewById(R.id.TableToView);
		Button bTab = (Button) this.findViewById(R.id.Fields);
		Button bVie = (Button) this.findViewById(R.id.Data);
		Button sVie = (Button) this.findViewById(R.id.SQL);
		bUp = (Button) this.findViewById(R.id.PgUp);
		bDwn = (Button) this.findViewById(R.id.PgDwn);
		bUp.setOnClickListener(this);
		bDwn.setOnClickListener(this);
		bUp.setVisibility(View.GONE);
		bDwn.setVisibility(View.GONE);
		_cont = this;
		limit = Prefs.getPageSize(this);
		bTab.setOnClickListener(this);
		bVie.setOnClickListener(this);
		sVie.setOnClickListener(this);
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			_cont = tvDB.getContext();
			int sourceType = extras.getInt("type");
			_dbPath = extras.getString("db");
			Utils.logD("Opening database");
			_table = extras.getString("Table");
			if (sourceType == Types.TABLE)
				tvDB.setText(getString(R.string.DBTable) + " " + _table);
			else if (sourceType == Types.VIEW)
				tvDB.setText(getString(R.string.DBView) + " " + _table);
			_db = new Database(_dbPath, _cont);
			Utils.logD("Database open");
			onClick(bTab);
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

	protected void selectRecord(String type, int position) {
		//TODO edit record
		Utils.logD("TableViewer selectRecord edit record here");
	}

	public void onClick(View v) {
		int key = v.getId();
		_aTable=(TableLayout)findViewById(R.id.datagrid);
		if (key == R.id.Fields) {
			offset = 0;
			String[] fieldNames = _db.getTableStructureHeadings(_table);
			setTitles(_aTable, fieldNames);
			String [][] data = _db.getTableStructure(_table);
			updateButtons(false);
			appendRows(_aTable, data);
		} else if (key == R.id.Data) {
			//list.
			offset = 0;
			String [] fieldNames = _db.getFieldsNames(_table);
			setTitles(_aTable, fieldNames);
			String [][] data = _db.getTableData(_table, offset, limit);
			updateButtons(true);
			appendRows(_aTable, data);
			//buildList(_type);
			//			Intent i = new Intent(this, DataGrid.class);
			//			i.putExtra("db", _dbPath);
			//			i.putExtra("Table", _table);
			//			startActivity(i);
		} else if (key == R.id.SQL) {
			offset = 0;
			String [] fieldNames = {"SQL"};
			setTitles(_aTable, fieldNames);
			String [][] data = _db.getSQL(_table);
			updateButtons(false);
			appendRows(_aTable, data);
		} else if (key == R.id.PgDwn) {
			int childs = _aTable.getChildCount();
			Utils.logD("Table childs: " + childs);
			if (childs >= limit) {  //  No more data on to display - no need to PgDwn
				offset += limit;
				String [] fieldNames = _db.getFieldsNames(_table);
				setTitles(_aTable, fieldNames);
				String [][] data = _db.getTableData(_table, offset, limit);
				appendRows(_aTable, data);
			}
			Utils.logD("PgDwn:" + offset);
		} else if (key == R.id.PgUp) {
			offset -= limit;
			if (offset < 0)
				offset = 0;
			String [] fieldNames = _db.getFieldsNames(_table);
			setTitles(_aTable, fieldNames);
			String [][] data = _db.getTableData(_table, offset, limit);
			appendRows(_aTable, data);
			Utils.logD("PgUp: " + offset);
		}
		
	}
	/**
	 * If paging = true show paging buttons otherwise not
	 * @param paging
	 */
	private void updateButtons(boolean paging) {
		if (paging) {
			bUp.setVisibility(View.VISIBLE);
			bDwn.setVisibility(View.VISIBLE);
		} else {
			bUp.setVisibility(View.GONE);
			bDwn.setVisibility(View.GONE);
		}
	}

	/**
	 * Add a row to the table
	 * @param table
	 * @param data
	 */
	private void appendRows(TableLayout table, String[][] data) {
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
			// TODO change rows to ConvertView
			// Adding all columns as TextView's should be changed to a ConvertView
			// as described here:
			// http://android-er.blogspot.com/2010/06/using-convertview-in-getview-to-make.html
			// or in android41cv dk.andsen.utils.MyArrayAdapter
			
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

	/**
	 * Add titles to the columns
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

}