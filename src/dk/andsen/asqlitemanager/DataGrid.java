package dk.andsen.asqlitemanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import dk.andsen.utils.Utils;

public class DataGrid extends Activity  {
	private String _dbPath;
	private String _table;
	private Context _cont;
	private int _noOfCols;
	private Database _db = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.datagrid);
		TableLayout aTable=(TableLayout)findViewById(R.id.datagrid);
		Bundle extras = getIntent().getExtras();
		if(extras ==null)
		{
			//throw Exception NoArgumentSupplied; 
		} else {
			_dbPath = extras.getString("db");
			Utils.logD("Opening database");
			_table = extras.getString("Table");
			_db = new Database(_dbPath, _cont);
			Utils.logD("Database open");
			_noOfCols = _db.getNumCols(_table);
			String [] fieldNames = _db.getFieldsNames(_table);
			appendTitles(aTable, fieldNames);
			String [][] data = _db.getTableData(_table);
			appendRows(aTable, data);
		}		
	}
	
	private void appendRows(TableLayout table, String[][] amortization) {
		int rowSize=amortization.length;
		int colSize=(amortization.length>0)?amortization[0].length:0;
		for(int i=0; i<rowSize; i++){
			TableRow row = new TableRow(this);
			for(int j=0; j<colSize; j++){
				TextView c = new TextView(this);
				c.setText(amortization[i][j]);
				c.setPadding(3, 3, 3, 3);
				row.addView(c);
			}
			table.addView(row, new TableLayout.LayoutParams());
		}
	}

	private void appendTitles(TableLayout table, String[] amortization) {
		int rowSize=amortization.length;
		TableRow row = new TableRow(this);
		row.setBackgroundColor(Color.CYAN);
		for(int i=0; i<rowSize; i++){
				TextView c = new TextView(this);
				c.setText(amortization[i]);
				c.setPadding(3, 3, 3, 3);
				row.addView(c);
		}
		table.addView(row, new TableLayout.LayoutParams());
	}
}
