package dk.andsen.asqlitemanager;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_viewer);
		TextView tvDB = (TextView)this.findViewById(R.id.TableToView);
		Button bTab = (Button) this.findViewById(R.id.Fields);
		Button bVie = (Button) this.findViewById(R.id.Data);
		bTab.setOnClickListener(this);
		bVie.setOnClickListener(this);
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
		if (key == R.id.Fields) {
			_type = "Fields";
			buildList(_type);
		} else if (key == R.id.Data) {
			_type = "Data";
			//buildList(_type);
			Intent i = new Intent(this, DataGrid.class);
			i.putExtra("db", _dbPath);
			i.putExtra("Table", _table);
			startActivity(i);
		} else if (key == R.id.SQL) {
			
		}
	}
}