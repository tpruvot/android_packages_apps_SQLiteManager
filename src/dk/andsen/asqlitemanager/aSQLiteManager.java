package dk.andsen.asqlitemanager;

import dk.andsen.utils.FilePicker;
import dk.andsen.utils.NewFilePicker;
import dk.andsen.utils.Utils;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * @author andsen
 *
 */
public class aSQLiteManager extends Activity implements OnClickListener {
	
	/**
	 * True to enable functions under test
	 */
	private final boolean testing = false;
	private static final int MENU_OPT = 1;
	private static final int MENU_HLP = 2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button open = (Button) this.findViewById(R.id.Open);
        open.setOnClickListener(this);
        Button about = (Button) this.findViewById(R.id.About);
        about.setOnClickListener(this);
        Button newDatabase = (Button) this.findViewById(R.id.NewDB);
        newDatabase.setOnClickListener(this);
        Button test = (Button) this.findViewById(R.id.Test);
        TextView tv = (TextView) this.findViewById(R.id.Version);
        tv.setText(getText(R.string.Version) + " " + getText(R.string.VersionNo));
        if (!testing) {
        	test.setVisibility(4);
        } else {
        	test.setOnClickListener(this);
        }
        Utils.logD("Created");
    }

		/* (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		public void onClick(View v) {
			int key = v.getId();
			if (key == R.id.Open) {
				Intent i = new Intent(this, FilePicker.class);
				Utils.logD("Calling Filepicker");
				//startActivityForResult(intent, requestCode)
				// onActivityResult() 
				startActivity(i);
			} else if (key == R.id.About) {
				Context mContext = this;
				Dialog dial = new Dialog(mContext);
				dial.setContentView(R.layout.about);
				dial.setTitle(getString(R.string.AboutHeader));
				TextView text = (TextView) dial.findViewById(R.id.text);
				text.setText(getString(R.string.AboutText));
				ImageView image = (ImageView) dial.findViewById(R.id.image);
				image.setImageResource(R.drawable.and);
				TextView mail = (TextView) dial.findViewById(R.id.mail);
				mail.setAutoLinkMask(Linkify.ALL);
				mail.setText(getString(R.string.MAIL));
				TextView www = (TextView) dial.findViewById(R.id.www);
				www.setAutoLinkMask(Linkify.ALL);
				www.setText(getString(R.string.WWW));
				dial.show();
			}  else if (key == R.id.NewDB) {
				Utils.logD("Create new database");
				newDatabase();
			} else if (key == R.id.Test) {
				Intent i = new Intent(this, NewFilePicker.class);
				Utils.logD("Calling NewFilepicker");
				startActivity(i);
			}
      
		}
		
		private void newDatabase() {
			final Dialog newCountryDialog = new Dialog(this);
			newCountryDialog.setContentView(R.layout.new_database);
			newCountryDialog.setTitle(getText(R.string.NewDBSDCard));
			final EditText edNewDB = (EditText)newCountryDialog.findViewById(R.id.newCode);
			edNewDB.setHint(getText(R.string.NewDBPath));
			TextView tvMessage = (TextView) newCountryDialog.findViewById(R.id.newMessage);
			tvMessage.setText(getText(R.string.Database));
			newCountryDialog.show();
			final Button btnMOK = (Button) newCountryDialog.findViewById(R.id.btnMOK);
			btnMOK.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String path;
					if (v == btnMOK) {
						if (Utils.isSDAvailable()) {
							path = Environment.getExternalStorageDirectory().getAbsolutePath();
							path += "/" + edNewDB.getText();
							if (path.equals("")) {
								// Give error and do nothing???
							}
							if (!path.endsWith(".sqlite"))
								path += ".sqlite";
							SQLiteDatabase.openOrCreateDatabase(path, null);
							
							/*
							 * Get path to SDCard - OK
							 * If filename does not end with .sqlite add it OK
							 * If no name entered do nothing
							 * create database
							 * //db.newCountry(edNewCountry.getText().toString());
							 * ask if user wants to open it 
							 */
							
							
						} else {
							// Give error - no SDCard available
						}
						Utils.logD("Path: " + edNewDB.getText().toString());
						
						
						newCountryDialog.dismiss();
					}
				}
			});
		}

		/*
		 *  Creates the menu items
		 */
		public boolean onCreateOptionsMenu(Menu menu) {
			menu.add(0, MENU_OPT, 0, R.string.Option).setIcon(R.drawable.ic_menu_preferences);
			menu.add(0, MENU_HLP, 0, R.string.Help).setIcon(R.drawable.ic_menu_help);
			return true;
		}
		
		/* (non-Javadoc) Handles item selections
		 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
		 */
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
	    case MENU_OPT:
	      startActivity(new Intent(this, Prefs.class));
	      return true;
	    case MENU_HLP:
				Intent i = new Intent(this, Help.class);
				startActivity(i);
	      return true;
			}
			return false;
		}

}