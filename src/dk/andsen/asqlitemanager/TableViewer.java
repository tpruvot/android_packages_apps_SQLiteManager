package dk.andsen.asqlitemanager;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
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
	private String[] toList;
	private String _table;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dbviewer);
		TextView tvDB = (TextView)this.findViewById(R.id.TableToView);
		Button bTab = (Button) this.findViewById(R.id.Fields);
		Button bVie = (Button) this.findViewById(R.id.Data);
		bTab.setOnClickListener(this);
		bVie.setOnClickListener(this);
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			Context cont = tvDB.getContext();
			_dbPath = extras.getString("db");
			Utils.logD("Opening database");
			_table = extras.getString("Table");
			tvDB.setText("Table: " + _table);
			_db = new Database(_dbPath, cont);
			Utils.logD("Database open");
			list = (ListView) findViewById(R.id.LVList);
			buildList(_table);
		}
	}

	private void buildList(String table) {
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;

		String sql = "select * from " + table;
		String[] fields = _db.getFields(table);
		
		int recs = 2;
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
				selectRecord("ddd", position);
			}
		});
	}

	protected void selectRecord(String type, int position) {
		String name;
		name = toList[position];
		Utils.logD("Handle: " + type + " " + name);
		if (type.equals("Index")) {

		}
		else if (type.equals("Views")) {
			
		}
		else if (type.equals("Tables")){
			
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
		}
	}
}