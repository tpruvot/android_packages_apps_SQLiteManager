package dk.andsen.asqlitemanager;

import dk.andsen.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SQLViewer extends Activity implements OnClickListener {

	private String _dbPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sqlviewer);
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			_dbPath = extras.getString("db");
			Utils.logD("Opening SQL file " + _dbPath);
			
		}		
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}