
package dk.andsen.asqlitemanager;

import dk.andsen.utils.Utils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OpenOnClick extends Activity implements OnClickListener {
	Button _btOK;
	private String _file;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.onclickopen);
		TextView tvFileName = (TextView)this.findViewById(R.id.OpenClickFile);
		_btOK = (Button)this.findViewById(R.id.OpenClickOK);
		_btOK.setOnClickListener(this);
		_file = getIntent().getData().toString();
		if (_file.startsWith("file:///"))
			_file = _file.substring(7);
		Utils.logD("File clicked: " + _file);
		tvFileName.setText((CharSequence) _file);
		//TODO ask user i the file should be opened by qSQLiteManager
	}

	public void onClick(View v) {
		int key = v.getId();
		if (key == R.id.OpenClickOK) {
			Intent i = new Intent(this, DBViewer.class);
			i.putExtra("db", _file);
			startActivity(i);
			finish();
		}
	}

}
