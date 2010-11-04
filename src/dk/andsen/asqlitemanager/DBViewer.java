package dk.andsen.asqlitemanager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DBViewer extends Activity {
	String dbPath;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dbviewer);
		TextView tvDB = (TextView)this.findViewById(R.id.DatabaseToView);

		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			dbPath = extras.getString("db");
			tvDB.setText("Viewing database: " + dbPath);

		}
	}
}