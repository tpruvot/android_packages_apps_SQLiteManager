package dk.andsen.asqlitemanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import dk.andsen.utils.Utils;

public class DataGrid extends Activity  {
	//private Context mContext;
	private ScrollView container;
	private float mx;
	private float my;
	private String _dbPath;
	private String _table;
	private Context _cont;
	private int _noOfCols;
	private Database _db = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.datagrid);
		final GridView switcherView = (GridView) this.findViewById(R.id.gridview);
		
		Bundle extras = getIntent().getExtras();
		if(extras ==null)
		{
			//throw Exception NoArgumentSupplied; 
		} else {
			_cont = switcherView.getContext();
			_dbPath = extras.getString("db");
			Utils.logD("Opening database");
			_table = extras.getString("Table");
			_db = new Database(_dbPath, _cont);
			Utils.logD("Database open");
			_noOfCols = _db.getNumCols(_table);
		}		
		switcherView.setAdapter(new TextViewAdapter(this, _dbPath, _table));
		switcherView.setNumColumns(_noOfCols);
		switcherView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Toast.makeText(DataGrid.this, "" + position, Toast.LENGTH_SHORT).show();
			}
		});
		// set up scrolling
		switcherView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent event) {
				float curX, curY;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
          mx = event.getX();
          my = event.getY();
          break;
      case MotionEvent.ACTION_MOVE:
          curX = event.getX();
          curY = event.getY();
          switcherView.scrollBy((int) (mx - curX), (int) (my - curY));
          mx = curX;
          my = curY;
          break;
      case MotionEvent.ACTION_UP:
          curX = event.getX();
          curY = event.getY();
          switcherView.scrollBy((int) (mx - curX), (int) (my - curY));
          break;
				}
				return true;
			}
		});

	}

private ViewGroup _createDataGrid() {
		LinearLayout panel = new LinearLayout(this);
		panel.setLayoutParams(
				new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, 
						LayoutParams.FILL_PARENT));
		panel.setOrientation(LinearLayout.VERTICAL);
		// Prøv med grid view hvor antal kolonner sættes
		ScrollView list = new ScrollView(this);
		//list.setOnTouchListener(this);
		container = list;
		list.setLayoutParams(new ScrollView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		list.setScrollbarFadingEnabled(false);
		list.setHorizontalScrollBarEnabled(true);
		list.setVerticalScrollBarEnabled(true);

		TableLayout tab = new TableLayout(this);
		tab.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		int RowPadding = 3;
		for (int i = 0; i < 50; i++) {
			TableRow row = new TableRow(this);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			row.setBackgroundColor(Color.argb(200, 51, 51, 51));
			TextView tvA = new TextView(this);
			tvA.setText("ABCDEFGHIJLMNOPQRSTUVXYZÆØÅ");
			tvA.setPadding(RowPadding, RowPadding, RowPadding, RowPadding);

			tvA.setBackgroundColor(R.color.hp_orange);
			tvA.setLayoutParams(new TableRow.LayoutParams(
					TableRow.LayoutParams.WRAP_CONTENT,
					TableRow.LayoutParams.WRAP_CONTENT
			));

			TextView tvB = new TextView(this);
			tvB.setText("abcdefghijklmnopqrstuvxyzæøå");
			tvB.setBackgroundColor(R.color.blue);
			tvB.setLayoutParams(new TableRow.LayoutParams(
					TableRow.LayoutParams.WRAP_CONTENT,
					TableRow.LayoutParams.WRAP_CONTENT
			));
			//tvB.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			row.addView(tvA);
			row.addView(tvB);
			tab.addView(row);
		}
		list.addView(tab);
		panel.addView(list);
		return panel;
	}
}
