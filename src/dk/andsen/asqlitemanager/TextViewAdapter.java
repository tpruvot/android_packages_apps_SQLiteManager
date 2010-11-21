package dk.andsen.asqlitemanager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import dk.andsen.utils.Utils;

public class TextViewAdapter extends BaseAdapter  {
  private Context _context;
  private String _table;
	private String _dbPath;
	private Database _db = null;

  public TextViewAdapter(Context c, String dbPath, String table) {
      _context = c;
      _table = table;
      //_db = new Database(_dbPath, _context);
      //String[][] tableData = _db.getTableData(table);
      
      
  }

	public int getCount() {
		// TODO Auto-generated method stub
		// return numbers of rows * cols
		return 200;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
    TextView tView;
    if (convertView == null) {  // if it's not recycled, initialize some attributes
        tView = new TextView(_context);
//        tView.setLayoutParams(new LayoutParams( 
//            LayoutParams.WRAP_CONTENT, 
//            LayoutParams.WRAP_CONTENT)); 
        //tView.setTextColor(R.color.blue);
        tView.setPadding(2, 1, 2, 1);
    } else {
        tView = (TextView) convertView;
    }
    // set text to right cell value
    Utils.logD("Text = Text " +position);
    tView.setText("Text" + position);
    return tView;
	}

}
