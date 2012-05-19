/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * The mail class of the aSQLiteManager
 *
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import dk.andsen.utils.FilePickerMode;
import dk.andsen.utils.NewFilePicker;
import dk.andsen.utils.RootFilePicker;
import dk.andsen.utils.Utils;

public class aSQLiteManager extends Activity implements OnClickListener {
	/**
	 * True to enable functions under test
	 */
	private static final int MENU_OPT = 1;
	private static final int MENU_HLP = 2;
	private static final int MENU_RESET = 3;
	final String WelcomeId = "ShowWelcome3.1";
	// change this to delete preferences 
	final String vers = "3.1";
	private Context _cont;
	private String _recentFiles;
	private boolean testRoot = false;
	private boolean _logging = false;
	private boolean loadSettings = false;

	private Dialog newDatabaseDialog;

	/** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.logD("onCreate", _logging);
		_logging = Prefs.getLogging(this);
		testRoot = Prefs.getTestRoot(this);
		if (Prefs.getMainVertical(this))
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.main);
		Button open = (Button) this.findViewById(R.id.Open);
		open.setOnClickListener(this);
		Button about = (Button) this.findViewById(R.id.About);
		about.setOnClickListener(this);
		Button newDatabase = (Button) this.findViewById(R.id.NewDB);
		newDatabase.setOnClickListener(this);
		Button recently = (Button) this.findViewById(R.id.Recently);
		recently.setOnClickListener(this);
		TextView tv = (TextView) this.findViewById(R.id.Version);
		tv.setText(getText(R.string.Version) + " " + getText(R.string.VersionNo));
		_cont = this;
		final SharedPreferences settings = getSharedPreferences("aSQLiteManager",
				MODE_PRIVATE);
		// Show welcome screen if not disabled
		// Perhaps change how the welcome screen is displayed. Store version no in
		// in "VersionNo" and show welcome if versionNo has changed
		//SharedPreferences prefs = getSharedPreferences(
		//		"dk.andsen.asqlitemanager_preferences", MODE_PRIVATE);
		// Should preferences be deleted?
		if (!settings.getString(vers, "").equals(vers)) {
			// Clear to many preferences???
			// Editor editor = prefs.edit();
			// editor.clear();
			// editor.commit();
		}
		Utils.logD("Show Tip	" + 1, _logging);
		Utils.showTip(getText(R.string.Tip1), 1, _cont);
		Editor ed = settings.edit();
		ed.putString(vers, vers);
		ed.commit();
		if (settings.getBoolean(WelcomeId, true)) {
			final Dialog dial = new Dialog(this);
			dial.setContentView(R.layout.new_welcome);
			dial.setTitle(R.string.Welcome);
			Button _btOK = (Button) dial.findViewById(R.id.OK);
			_btOK.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					CheckBox _remember = (CheckBox) dial.findViewById(R.id.ShowAtStartUp);
					android.content.SharedPreferences.Editor edt = settings.edit();
					edt.putBoolean(WelcomeId, _remember.isChecked());
					edt.commit();
					dial.dismiss();
				}
			});
			dial.show();
		}
		AppRater.app_launched(_cont);
		// if aSQLiteManager is started from other app vith a name of a database open it
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String _dbPath = extras.getString("Database");
			Intent i = new Intent(_cont, DBViewer.class);
			i.putExtra("db", _dbPath);
			startActivity(i);
		} else {

		}
  }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	Utils.logD("onConfigurationChanged", _logging);
    	//TODO handle change of orientations here?
    	
    }

		/* (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		public void onClick(View v) {
			int key = v.getId();
			if (key == R.id.Open) {
				Intent i = null;
				if (testRoot) {
					Utils.logD("Calling RootFilepicker", _logging);
					i = new Intent(this, RootFilePicker.class);
				} else {
					Utils.logD("Calling NewFilepicker", _logging);
					i = new Intent(this, NewFilePicker.class);
				}
//				Utils.logD("Calling NewFilepicker for result");
//				startActivityForResult(i, 1);
				try {
					startActivity(i);
				} catch (Exception e) {
					Utils.logE("Error in file picker (root " + testRoot + ")", _logging);
					e.printStackTrace();
					Utils.showException("Plase report this error with descriptions of how to generate it", _cont);
				}
			} else if (key == R.id.About) {
				showAboutDialog();
			}  else if (key == R.id.NewDB) {
				Utils.logD("Create new database", _logging);
				newDatabase();
			} else if (key == R.id.Recently) {
				// Retrieve recently opened files
				SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
				_recentFiles = settings.getString("Recently", null);
				if (_recentFiles == null) {
					Utils.showMessage(getText(R.string.Error).toString(),
							getText(R.string.NoRecentFiles).toString(), _cont);
				} else {
					// Special handling for databases in Dropbox (cut part of path)
					String recentTest = _recentFiles.replaceAll("/mnt/sdcard/Android/data/com.dropbox.android/files/scratch", "[Dropbox]");
					String[] resently = recentTest.split(";");
					//TODO test this!!!!!	
					// String[] resently = _recentFiles.split(";");
					Utils.logD(_recentFiles, _logging);
					AlertDialog dial = new AlertDialog.Builder(this)
					.setTitle(getString(R.string.Recently)) 
					.setSingleChoiceItems(resently, 0, new ResentFileOnClickHandler() )
					.create();
					dial.show();
				}
			}
		}
		
//		@Override
//		protected void onPause() {
//		Utils.logD("aSQLiteManager onPause", logging);
//			super.onPause();
//		}
//
//		@Override
//		protected void onStop() {
//			Utils.logD("aSQLiteManager onStop", logging);
//			super.onStop();
//		}
//
//		
//		protected void onResume() {
//			super.onResume();
//			Utils.logD("aSQLiteManager onResume", logging);
//		}
		
		public void onWindowFocusChanged(boolean hasFocus) {
			// Works only need to refresh the screen
			Utils.logD("Focus changed: " + hasFocus, _logging);
			if(hasFocus) {
				if (loadSettings  ) {
	        _logging = Prefs.getLogging(this);
	        testRoot = Prefs.getTestRoot(this);
					
				}
			}
		}
		
		/**
		 * Open a the database clicked on from the recently opened file menu
		 */
		public class ResentFileOnClickHandler implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
				String[] files = settings.getString("Recently", null).split(";");
				String database = files[which];
				//Utils.toastMsg(_cont, database);
				dialog.dismiss();
				Intent i = new Intent(_cont, DBViewer.class);
				i.putExtra("db", database);
				try {
					startActivity(i);
				} catch (Exception e) {
					Utils.logE("Error in DBViewer", _logging);
					e.printStackTrace();
					Utils.showException("Plase report this error with descriptions of how to generate it", _cont);
				}
			}
		}

		/**
		 * Display the about dialog
		 */
		private void showAboutDialog() {
			Dialog dial = new Dialog(this);
			dial.setTitle(getText(R.string.About) + " " + getText(R.string.hello));
			dial.setContentView(R.layout.about);
			dial.show();
			//Intent i = new Intent(this, ProgressBarTest.class);
			//Utils.logD("Calling ProgressBar");
			//startActivity(i);
		}

		protected void onActivityResult(int requestCode, int resultCode, Intent data)
		{
			Utils.logD("Main-activity got result from sub-activity", _logging);
			if (resultCode == Activity.RESULT_CANCELED) {
				Utils.logD("WidgetActivity was cancelled or encountered an error. resultcode == result_cancelled", _logging);
				Utils.logD("WidgetActivity was cancelled - data =" + data, _logging);
			} else
				switch (requestCode) {
				case 1:
					 String msg = data.getStringExtra("returnedData");
					 Utils.showMessage("Returned file", msg, _cont);
					break;
				case 2:
					if (newDatabaseDialog != null) {
						String folderName = Utils.addSlashIfNotEnding(data
								.getStringExtra("RESULT"));
						final TextView newFolder = (TextView) newDatabaseDialog
								.findViewById(R.id.newFolder);
						newFolder.setText(folderName);
					}
					break;
				}
			Utils.logD("Main-activity got result from sub-activity", _logging);
		}		
		
		/**
		 * Create a new empty database
		 */
		private void newDatabase() {
			newDatabaseDialog = new Dialog(this);
			newDatabaseDialog.setContentView(R.layout.new_database);
			newDatabaseDialog.setTitle(getText(R.string.NewDBSDCard));
			final EditText edNewDB = (EditText) newDatabaseDialog
					.findViewById(R.id.newCode);
			edNewDB.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_NORMAL);
			// TODO Change to filename only!!!
			edNewDB.setHint(getText(R.string.NewDBPath));
			final TextView newFolder = (TextView) newDatabaseDialog
					.findViewById(R.id.newFolder);
			newFolder.setText(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/");
			final Button newFolderSelectButton = (Button) newDatabaseDialog
					.findViewById(R.id.newFolderSelectButton);
			newFolderSelectButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(_cont, NewFilePicker.class);
					i.putExtra("MODE", FilePickerMode.SELECTFOLDER.name());
					try {
						startActivityForResult(i, 2);
					} catch (Exception e) {
						Utils.logE("Error in file picker (root " + testRoot + ")", _logging);
						e.printStackTrace();
						Utils.showException(
								"Plase report this error with descriptions of how to generate it",
								_cont);
					}
				}
			});
			TextView tvMessage = (TextView) newDatabaseDialog
					.findViewById(R.id.newMessage);
			tvMessage.setText(getText(R.string.Database));
			newDatabaseDialog.show();
			final Button btnMOK = (Button) newDatabaseDialog.findViewById(R.id.btnMOK);
			btnMOK.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					boolean error = false;
					String path;
					if (v == btnMOK) {
						if (Utils.isSDAvailable()) {
							String folderName = Utils.addSlashIfNotEnding(newFolder.getText()
									.toString());
							String fileName = edNewDB.getEditableText().toString();
							path = folderName + fileName;
							if (fileName.trim().equals("")) {
								Utils.showMessage((String) getText(R.string.Error),
										(String) getText(R.string.NoFileName), _cont);
							} else {
								if (!path.endsWith(".sqlite"))
									path += ".sqlite";
								try {
									// check to see if it exists
									File f = new File(path);
									if (f.exists()) {
										error = true;
										Utils.showMessage(getString(R.string.Error), path + " "
												+ getString(R.string.DatabaseExists), _cont);
									} else {
										SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path,
												null);
										db.close();
									}
								} catch (Exception e) {
									error = true;
									Utils.showMessage(getString(R.string.Error),
											getString(R.string.CouldNotCreate) + " " + path, _cont);
									e.printStackTrace();
								}
								// Ask before??
								if (!error) {
									Intent i = new Intent(_cont, DBViewer.class);
									i.putExtra("db", path);
									newDatabaseDialog.dismiss();
									newDatabaseDialog = null;
									try {
										startActivity(i);
									} catch (Exception e) {
										Utils.logE("Error in DBViewer", _logging);
										e.printStackTrace();
										Utils
												.showException(
														"Plase report this error with descriptions of how to generate it",
														_cont);
									}
								}
							}
						}
						Utils.logD("Path: " + edNewDB.getText().toString(), _logging);
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
			menu.add(0, MENU_RESET, 0, R.string.Reset).setIcon(R.drawable.ic_menu_close_clear_cancel);
			return true;
		}
		
		/* (non-Javadoc) Handles item selections
		 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
		 */
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
	    case MENU_OPT:
	    	loadSettings = true;
	      startActivity(new Intent(this, Prefs.class));
	      return true;
	    case MENU_HLP:
				Intent i = new Intent(this, Help.class);
				startActivity(i);
	      return true;
	    case MENU_RESET:
	    	// Reset all settings to default
	    	resetAllPreferences();
				return false;
			}
			return false;
		}

		/**
		 * Clear all preferences. Preferences, different choices, recently opened
		 * and tip history
		 */
		private void resetAllPreferences() {
			//TODO ask before doing this!
			//Clear different choices, recently opened, number of times used, day of first use
    	SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.clear();
			editor.commit();
			// Clear preferences
			settings = getSharedPreferences("dk.andsen.asqlitemanager_preferences", MODE_PRIVATE);
			editor = settings.edit();
			editor.clear();
			editor.commit();
			// Clear tip history
			settings = _cont.getSharedPreferences("dk.andsen.asqlitemanager_tips", MODE_PRIVATE);
			editor = settings.edit();
			editor.clear();
			editor.commit();
		}
}