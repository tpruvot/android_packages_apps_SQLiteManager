/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a Android SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 *	This activity is called when clicked on a .sqlite / .db file from file managers
 *
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import dk.andsen.utils.Utils;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class OpenOnClick2 extends Activity implements OnClickListener {
	Button _btOK;
	CheckBox _remember;
	private String _file;
	private boolean logging;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logging = Prefs.getLogging(this);
		SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
		_file = getIntent().getData().toString();
		if (_file.startsWith("file:///"))
			_file = _file.substring(7);
		Utils.logD("File clicked: " + _file, logging);
		if(!settings.getBoolean("JustOpen", false))
		{
			setContentView(R.layout.onclickopen);
			TextView tvFileName = (TextView)this.findViewById(R.id.OpenClickFile);
			_btOK = (Button)this.findViewById(R.id.OpenClickOK);
			_btOK.setOnClickListener(this);
			_remember = (CheckBox) this.findViewById(R.id.RememberDecision);
			tvFileName.setText((CharSequence) _file);
		} else {
			Intent i = new Intent(this, DBViewer.class);
			i.putExtra("db", _file);
			try {
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in DBViewer", logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of how to generate it", this);
			}
			finish();
		}
	}

	public void onClick(View v) {
		int key = v.getId();
		if (key == R.id.OpenClickOK) {
			if (_remember.isChecked()) {
				SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("JustOpen", true);
				editor.commit();
			}
			Intent i = new Intent(this, DBViewer.class);
			i.putExtra("db", _file);
			try {
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in DBViewer", logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of how to generate it", this);
			}
			finish();
		}
	}

}
