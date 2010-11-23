package dk.andsen.asqlitemanager;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import dk.andsen.utils.Utils;

public class TableViewer extends Activity implements OnClickListener {
	private String _dbPath;
	private Database _db = null;
	private ListView list;
	private String _table;
	Context _cont;
	private String _type = "Fields";
	private TableLayout _aTable;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_viewer);
		TextView tvDB = (TextView)this.findViewById(R.id.TableToView);
		Button bTab = (Button) this.findViewById(R.id.Fields);
		Button bVie = (Button) this.findViewById(R.id.Data);
		Button sVie = (Button) this.findViewById(R.id.SQL);
		bTab.setOnClickListener(this);
		bVie.setOnClickListener(this);
		sVie.setOnClickListener(this);
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			_cont = tvDB.getContext();
			_dbPath = extras.getString("db");
			Utils.logD("Opening database");
			_table = extras.getString("Table");
			tvDB.setText("Table: " + _table);
			_db = new Database(_dbPath, _cont);
			Utils.logD("Database open");
			list = (ListView) findViewById(R.id.LVList);
			buildList(_type);
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
	
	private void buildList(final String viewType) {
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		// show the fields of the table
		if (viewType.equals("Fields")) {
			Field[] fields = _db.getFields(_table);
			int recs = fields.length;
			for (int i = 0; i < recs; i++) {
				String notNull = " - null ";
				if (fields[i].getNotNull() == 1)
					notNull = " - not null ";
				map = new HashMap<String, String>();
				map.put("name", fields[i].getFieldName()
						+ " - " + fields[i].getFieldType()
						+ notNull
						+ " - (pk) " + fields[i].getPk()
						+ " - (def) " + fields[i].getDef());
				mylist.add(map);
			} 
			SimpleAdapter mSchedule = new SimpleAdapter(this, mylist, R.layout.row,
					new String[] {"name"}, new int[] {R.id.rowtext});
			list.setAdapter(mSchedule);
		} else if (viewType.equals("Data")) {
			SimpleAdapter mSchedule = new SimpleAdapter(this, mylist, R.layout.row,
					new String[] {"name"}, new int[] {R.id.rowtext});
			list.setAdapter(mSchedule);			
		} else if (viewType.equals("SQL")) {

			// show the fields of the table
				String[] sql = _db.getSQL(_table);
				int recs = sql.length;
				for (int i = 0; i < recs; i++) {
					map = new HashMap<String, String>();
					map.put("name", sql[i]);
					mylist.add(map);
				} 
				SimpleAdapter mSchedule = new SimpleAdapter(this, mylist, R.layout.row,
						new String[] {"name"}, new int[] {R.id.rowtext});
				list.setAdapter(mSchedule);
			
			
			
			
		}
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// Do something with the table / view / index clicked on
				selectRecord(viewType, position);
			}
		});
	}

	protected void selectRecord(String type, int position) {
		//TODO what?
	}

	public void onClick(View v) {
		int key = v.getId();
		_aTable=(TableLayout)findViewById(R.id.datagrid);
		if (key == R.id.Fields) {
			_aTable.removeAllViews();
			_type = "Fields";
			buildList(_type);
		} else if (key == R.id.Data) {
			//list.
			_type = "Data";
			buildList(_type); // clears the list
			String [] fieldNames = _db.getFieldsNames(_table);
			setTitles(_aTable, fieldNames);
			String [][] data = _db.getTableData(_table);
			appendRows(_aTable, data);

			//buildList(_type);
//			Intent i = new Intent(this, DataGrid.class);
//			i.putExtra("db", _dbPath);
//			i.putExtra("Table", _table);
//			startActivity(i);
		} else if (key == R.id.SQL) {
			_aTable.removeAllViews();
			_type = "SQL";
			buildList(_type);
		}
	}
	private void appendRows(TableLayout table, String[][] data) {
		int rowSize=data.length;
		int colSize=(data.length>0)?data[0].length:0;
		for(int i=0; i<rowSize; i++){
			TableRow row = new TableRow(this);
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
				row.addView(c);
			}
			table.addView(row, new TableLayout.LayoutParams());
		}
	}

	private void setTitles(TableLayout table, String[] amortization) {
		int rowSize=amortization.length;
		table.removeAllViews();
		TableRow row = new TableRow(this);
		row.setBackgroundColor(Color.BLUE);
		for(int i=0; i<rowSize; i++){
				TextView c = new TextView(this);
				c.setText(amortization[i]);
				c.setPadding(3, 3, 3, 3);
				row.addView(c);
		}
		table.addView(row, new TableLayout.LayoutParams());
	}

}