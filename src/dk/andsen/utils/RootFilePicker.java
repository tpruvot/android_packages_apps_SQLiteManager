/**
 * Part of aShell (http://aaa.andsen.dk/)
 * A SQLite Manager by Andsen
 *
 * The root enabled FilePicker used by aShell
 *
 * @author Andsen
 *
 */

package dk.andsen.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import dk.andsen.asqlitemanager.DBViewer;
import dk.andsen.asqlitemanager.Prefs;
import dk.andsen.asqlitemanager.R;

public class RootFilePicker extends ListActivity {
	private List<String> item = null;
	private String currentShell = "sh";
	private String shell = "sh";
	private String suShell = "su";
	private String currPath = "/";
	private boolean _exitNextTime = false;
	private TextView tvHeader;
	private TextView tvMode;
	private boolean useRoot = false;
	private static final int MENU_FILTER = 0;
	private static final int MENU_ROOT = 1;
	private String filter = "";
	private boolean inSuPath = false;
	protected boolean editingDatabase;
	protected String databasePath;
	private String databaseTemp;
	private final String tempDir = "/aSQLiteManager";
	//private final int pause = 500;
	private int delay;
	private boolean _log;
	private Context _cont;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_cont = this;
		setContentView(R.layout.rootfilepicker);
		tvHeader = (TextView) this.findViewById(R.id.path);
		tvHeader.setText(getText(R.string.Path) + ": " + currPath);
		tvMode = (TextView) this.findViewById(R.id.Mode);
//		Bundle extras = getIntent().getExtras();

		delay = Prefs.getPause(_cont);
		_log = Prefs.getLogging(_cont);
		// Right now isRoot always true here as RootFilePicker only 
		// launched if isRoot is true. Code prepared for skipping old
		// file picker
		useRoot = Prefs.getTestRoot(_cont);
		Utils.logD("Try to use root " + useRoot, _log);
		if(useRoot) {
			//Show warning
  		final SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
  		if(!settings.getBoolean("RootWarning", false)) {
  			showRootWarning();
  		}
			suShell = Prefs.getSuLocation(_cont);
			// shell not defined in configuration
			if (suShell == null || suShell.equals("")) {
				// try to locate su shell
				suShell = findSu();
			}
			if (suShell == null || suShell.equals("")) {
				// No root shell found use sh
				// and save it to preferences
				suShell = "sh";
				//final SharedPreferences settings = getSharedPreferences("dk.andsen.asqlitemanager_preferences", MODE_PRIVATE);
				Editor edt = settings.edit();
				edt.putString("SuShell", suShell);
				edt.commit();
				Utils.logD("Root not available", _log);
				Utils.showMessage(getText(R.string.Warning).toString(), 
						getText(R.string.RootNotFound).toString(), _cont);
				currentShell = shell;
				tvMode.setText(R.string.Normal);

			}
			if (suShell.equals("sh")) {
				Utils.logD("No root available", _log);
				currentShell = suShell;
				tvMode.setText(R.string.Normal);
			} else {
				Utils.logD("Root available", _log);
				currentShell = suShell;
				tvMode.setText(R.string.Root);
			}
		} else {
			Utils.logD("No using root", _log);
			currentShell = "sh";
			tvMode.setText(R.string.Normal);
		}
		AShellCommandInterface sh = new AShellCommandInterface(currentShell, this);
		String res = sh.runCommandOut("ls -l -a " + currPath);
		item = string2Items(res);
		String[] filetypes = {".sqlite", ".db"};
		MyRootArrayAdapter mlist = new MyRootArrayAdapter(this, item, filetypes); 
		setListAdapter(mlist);
	}

	private void showRootWarning() {
		final Dialog warningDial = new Dialog(_cont);
		warningDial.setTitle(getText(R.string.Warning));
		warningDial.setContentView(R.layout.root_warning);
		Button _btOK = (Button)warningDial.findViewById(R.id.OK);
		_btOK.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox _remember = (CheckBox) warningDial.findViewById(R.id.ShowAtStartUp);
    		final SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
				android.content.SharedPreferences.Editor edt = settings.edit();
				edt.putBoolean("RootWarning", _remember.isChecked()); 
				edt.commit();
				warningDial.dismiss();
			} });
		warningDial.show();


	}

	/**
	 * Try to find su
	 * @return the location of su or null if su not found
	 */
	private String findSu() {
		String results = "Results:";
		for (String suShell : SU_COMMANDS) {
			Utils.logD("Trying " + suShell, _log);
			results += "\n" + suShell;
			AShellCommandInterface sh = new AShellCommandInterface(suShell, this);
			String out = null;
			// Trying to execute id
			for (String command : TEST_COMMANDS) {
				//Utils.logD("Testnig " + command);
	      out = sh.runCommandOut(command);
	      results += " " + out;
	      //Utils.logD(" result '" + out + "'");
	      if (out != null && out.length() > 0)
	      	break;
	    }
	    if (out == null || out.length() == 0) {
	    	
	    } else {
	    	Utils.logD("Got a result " + out, _log);
		    Matcher matcher = UID_PATTERN.matcher(out.replace('\n', ' ').trim());
		    if (matcher.matches()) {
		    	Utils.logD("Match", _log);
		      if ("0".equals(matcher.group(1))) {
		      	Utils.logD("su found", _log);
		        return suShell;
		      }
		    }
	    }
	    sh.close();
		}
		//Utils.showMessage("Results", results, this);
		return null;
	}

	/**
	 * To check if id returns 0 root 
	 * uid=0(root) gid=0(root)
	 */
	private static final Pattern UID_PATTERN = Pattern.compile("^uid=(\\d+).*?");
	
  /**
   * Holds the different locations of su to try
   */
  private static final String[] SU_COMMANDS = new String[]{
    "/system/bin/su",
    "/system/xbin/su",
    "su"
  };

  /**
   * Holds the different locations of id to try
   */
  private static final String[] TEST_COMMANDS = new String[]{
    "/system/bin/id",
    "/system/xbin/id",
    "id"
  };

	/**
	 * Convert a String with the output from ls -l -a to a List
	 * @param filesString
	 * @return a List of files
	 */
	private List<String> string2Items(String filesString) {
		//TODO format not always the same. Can't use positions!!!!
		List<String> item = null;
		item = new ArrayList<String>();
		if (filesString == null)
			item.add("..");
		else {
			String[] files = filesString.split("\n");
			if (!currPath.equals("/"))
				item.add("..");
			if (filesString.trim().equals(""))
				return item;
			// -rwxr-xr-x  1 mh None  792 Aug  5 13:18 .profile
			// lrwxrwxrwx  1 mh root       6 Aug  5 13:17 xzegrep -> xzgrep
			// drwxr-xr-x+ 1 mh root    0 Aug  5 13:29 bin
			for (String str: files) {
				String attr = str.substring(0, str.indexOf(' '));
				String name;
				// doesn't work if spaces in file name
				// is the : in time a god fix point??
				if (attr.startsWith("l")) {
					//links starts from third last ' ' if no spaces in file name
					String [] fields = str.split(" ");
					name = fields[fields.length -3] + " " + fields[fields.length -2] + " "
						+ fields[fields.length-1];
				} else {
					//Filename starts from last ' ' if no spaces in file name
					name = str.substring(str.lastIndexOf(' ') + 1);
				}
//				String fileDescr = "";
//				fileDescr = str.substring(55) + "\n";
//				fileDescr += str.substring(0, str.indexOf(" "));
				String fileDescr = name + "\n" + attr;
				item.add(fileDescr);
			}
		}
		Collections.sort(item, new byFileName());
		return item;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String oldPath = currPath;
		Utils.logD("pos: " + position, _log);
		String newPath = item.get(position);
		if (newPath.equals(".."))
			onBackPressed();
		else {
			//TODO the check for readability not good enough
			Utils.logD(newPath, _log);
			int nl = newPath.indexOf("\n");
			String dir = newPath.substring(nl + 1, nl + 2);
			newPath = newPath.substring(0, newPath.indexOf("\n")).trim();
			//AShellCommandInterface sh = new AShellCommandInterface(shell);
			// directory clicked move into it
			//TODO not working quite as it should here (messages about root dir)
			if (dir.equals("d")) {
				if (currPath.length() > 1) {
					currPath += "/" + newPath.trim() ;
					Utils.logD("Entering " + currPath, _log);
				} else { 
					currPath += newPath.trim();
					Utils.logD("In " + currPath, _log);
				}
				// IF file cannot be redden in this way we are in a su dir
				if (!(new File(currPath).canRead())) {
					if (!useRoot) {	
						// Are we not in root mode step back
						// this also makes cd /data impossible
						Utils.showMessage(getText(R.string.RootWarning).toString(),
								getText(R.string.RootNeeded).toString()+ ":\n" + currPath, this);
						//						Utils.toastMsg(this, getText(R.string.RootNeeded).toString()
						//								+ ": " + currPath);
						currPath = oldPath;
					} else {
						// user has root permission and is allowed into the the dir
						
					}
				} else {
					//Path changed clear filter
					filter = "";
				}
				tvHeader.setText(getText(R.string.Path) + ": " + currPath);
				//use if(file.canRead()) to check if file / dir is in a roor dir.
				if (!(new File(currPath).canRead())) {
					// only warn if going from non root to root dir.
					if (!inSuPath)
						Utils.toastMsg(this, getText(R.string.EnterRoot) + " " + currPath);
					inSuPath = true;
					//currPath = oldPath;
				} else {
					if (inSuPath)
						Utils.toastMsg(this, getText(R.string.LeavingRoot) + " " + currPath);
					inSuPath = false;
				}
				updateList();
			} else {
				// If it is a .db / .sqlite file and non root access to currPath
				// open database
				if (newPath.endsWith(".sqlite") || newPath.endsWith(".db")) {
					File f = new File(currPath);
					final String dbPath = currPath;
					final String database = newPath;
					// test if it a root dir
					if (!f.canRead()) {
						// A root dir
						//toastMsg("A root dir!");
						AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
						alt_bld.setMessage(getText(R.string.RootEdit1) + " ["
								+ dbPath + "/" + database +"] " + getText(R.string.RootEdit2))
								.setCancelable(false)
								.setPositiveButton(getText(R.string.Yes), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										// Action for 'Yes' Button
										Utils.logD("Open root file", _log);
										openRootFile(dbPath, database);
										dialog.dismiss();
									}
								})
								.setNegativeButton(getText(R.string.No), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										// Action for 'NO' Button
										//dialog.cancel();
										dialog.dismiss();
									}
								});
						AlertDialog alert = alt_bld.create();
						// Title for AlertDialog
						alert.setTitle(getText(R.string.Warning));
						// Icon for AlertDialog
						alert.setIcon(R.drawable.sqlite_icon);
						alert.show();

					} else {
						// Not a root dir
						openFile(currPath, newPath);
					}
				}
			}
		}
	}

	/**
	 * Open the file clicked on. This is used directly for the non root files
	 * rooted files are opened after being copied to temperary location
	 * @param path to the file
	 * @param file name of the file
	 */
	private void openFile(String path, String file) {
		boolean treatAllFilesAsDatabases = true;
		
		if (treatAllFilesAsDatabases) {
			//Open database directly		
			Utils.logD("File clicked " + file, _log);
			Intent iDBViewer = new Intent(_cont, DBViewer.class);
			iDBViewer.putExtra("db", ""+ path + "/" + file);
			try {
				startActivity(iDBViewer);
			} catch (Exception e) {
				Utils.logE("Error in file DBViewer", _log);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of hov to generate it", _cont);
			}
		} else {
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
			Utils.logD("File: " + path + "/" + file, _log);
			Uri data = Uri.fromFile(new File(path + "/" + file));
			//    Utils.toastMsg(this, "Uri: " + data.getEncodedPath());
			//    Utils.toastMsg(this, "Type: " + "*/*");
			intent.setDataAndType(data, "*/*");
			try {
				startActivity(intent); 
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this, "No app to open this file ;-(", Toast.LENGTH_SHORT).show();
			};
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (editingDatabase) {
			AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
			alt_bld.setMessage(getText(R.string.CommitChangesTo) + " ["
					+ databasePath +"]")
					.setCancelable(false)
					.setPositiveButton(getText(R.string.Yes), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Action for 'Yes' Button
							String cmd = "cat " + databaseTemp + " > " + databasePath;
							AShellInterface shc = new AShellInterface(suShell, delay, _cont);
							shc.runCommand(cmd);
							try {
								Thread.sleep(500);
							}
							catch (InterruptedException e) {
								e.printStackTrace();
							}
							dialog.dismiss();
						}
					})
					.setNegativeButton(getText(R.string.No), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Action for 'NO' Button
							dialog.cancel();
						}
					});
			AlertDialog alert = alt_bld.create();
			// Title for AlertDialog
			alert.setTitle(getText(R.string.CommitChanges));
			// Icon for AlertDialog
			alert.setIcon(R.drawable.sqlite_icon);
			alert.show();
			editingDatabase = false;
		}
	}

	/**
	 * Open a copy file from the root part of the phone. The file is copied to 
	 * /mnt/sccard/aSQLiteManager (this method of opening the file did not
	 * work with catalogs containing "."
	 * @param dbPath Path to the file
	 * @param file The name of the file
	 */
	private void openRootFile(String dbPath, String file) {
		testTempDir();
		// Does not work with "." in temp path (.aSQLiteManager)
		String tmpPath = Environment.getExternalStorageDirectory().toString() + tempDir; 
		AShellInterface shc = new AShellInterface(suShell, delay, _cont);
		String cmd = "cat " + dbPath + "/" + file + " > " + tmpPath + "/" + file + ".bck";
		shc.runCommand(cmd);
		cmd = "cat " + dbPath  + "/" + file + " > " + tmpPath + "/" + file;
		shc.runCommand(cmd);
		editingDatabase = true;
		databasePath = dbPath + "/" + file;
		databaseTemp = tmpPath + "/" + file;
		try {
			Thread.sleep(delay);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		openFile(tmpPath, file);
	}

	/**
	 * Catch the back button. The back button must be pressed twice to leave the
	 * file picker
	 */
	public void onBackPressed() {
		if (_exitNextTime && currPath.equals("/")) {
			this.finish();
		} else {
			if (currPath.equals("/")) {
				Utils.toastMsg(this, getText(R.string.OneMoreToExit).toString());
				_exitNextTime = true;
			} else
				_exitNextTime = false;
			Utils.logD("onBackPressed Called", _log);
			currPath = currPath.substring(0, currPath.lastIndexOf("/"));
			if (!currPath.startsWith("/"))
				currPath = "/" + currPath;
			if (currPath.trim() == "")
				currPath = "/";
			tvHeader.setText(getText(R.string.Path) + ": " + currPath);
			updateList();
		}
	}

	/**
	 * Update the List holding the files
	 * Perhaps open new cataloges in a new intent so that the program return
	 * to the same place in the list?
	 */
	private void updateList() {
		Utils.logD("Current shell: " +currentShell, _log);
		AShellCommandInterface sh = new AShellCommandInterface(currentShell, this);
		String cmd = "ls -l -a " + currPath;
		Utils.logD("Cmd: " + cmd, _log);  
		if (!filter.equals(""))
			cmd = "ls -l -a -d " + currPath + "/" + filter.trim(); 
		String res = sh.runCommandOut(cmd);
		//ShellInterface.setShell(shell);
		//String res = MyShellInterface.getProcessOutputStdout("ls -l " + currPath);
		item = string2Items(res);
		String[] filetypes = {".sqlite", ".db"};
		MyRootArrayAdapter mlist = new MyRootArrayAdapter(this, item, filetypes); 
		setListAdapter(mlist);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_FILTER , 0, getText(R.string.Filter));
		menu.add(0, MENU_ROOT , 1, getText(R.string.Root));
		//menu.add(0, MENU_RESET, 0, R.string.Reset).setIcon(R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_FILTER:
			final Dialog dial = new Dialog(this);
			dial.setContentView(R.layout.filter);
			dial.setTitle(getText(R.string.EnterFilter));
			final EditText ed = (EditText)dial.findViewById(R.id.Filter);
			Button btnOk = (Button)dial.findViewById(R.id.OK);
			btnOk.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					//OK
					filter = ed.getText().toString();
					dial.dismiss();
					updateList();
				}
			});
			Button btnClear = (Button)dial.findViewById(R.id.Clear);
			btnClear.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					//Clear filter
					filter = "";
					dial.dismiss();
					updateList();
				}
			});
			dial.show();
			return true;
		case MENU_ROOT:
			//TODO Warn about root, test for root presence and perhaps turn it on
			
			
			return true;
		}
		return false;
	}

	/**
	 * This just check if the temporary cataloge for files to be edited is pressent
	 * if not it is created
	 */
	private void testTempDir() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			//Utils.toastMsg(this, "sdcard ok");
			//mExternalStorageAvailable = mExternalStorageWriteable = true;
			String tmpPath = Environment.getExternalStorageDirectory().toString() + tempDir; 
			if( new File(tmpPath).canRead()) {
				//Utils.toastMsg(this, "tmpdir ok");
			} else {
				//Utils.toastMsg(this, "create dir: " + tmpPath);
				if ( (new File(tmpPath)).mkdir()) {
					//Utils.toastMsg(this, "tmpdir created");
				} else {
					// in troubles
					//Utils.toastMsg(this, "in troubles");
				}
			}
		}
	}

	/**
	 * @author Andsen
	 * Sort the list of file names and attributes. Must be on this format: 
	 *  filename\ndrwxrwxrwx
	 *  Dosn't handle other types of files than '-', 'd' and 'l'
	 */
	public class byFileName implements java.util.Comparator<String> {
		public int compare(String a, String b) {
			if (a.equals(".."))
				return -1;
			if (b.equals(".."))
				return 1;
			int aIdx = a.indexOf('\n');
			int bIdx = b.indexOf('\n');
			String aName = a.substring(0, aIdx);
			String aType = a.substring(aIdx +1, aIdx + 2);
			if (aType.equals("l"))
				aType = "c";
			String bName = b.substring(0, bIdx);
			String bType = b.substring(bIdx + 1, bIdx + 2);
			if (bType.equals("l"))
				bType = "c";

			if(!aType.equals(bType))
				return bType.compareTo(aType);
			else
				return aName.compareTo(bName);
		}
	} 

}