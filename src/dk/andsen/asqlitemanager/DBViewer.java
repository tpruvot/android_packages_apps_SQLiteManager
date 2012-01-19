/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * Show tables, views, and index from the current database
 * 
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import dk.andsen.types.Types;
import dk.andsen.utils.NewFilePicker;
import dk.andsen.utils.Recently;
import dk.andsen.utils.Utils;

public class DBViewer extends Activity implements OnClickListener {
	private String _dbPath;
	public static Database database = null;
//	private String[] tables;
//	private String[] views;
	private String[] indexes;
	private ListView list;
	private LinearLayout query;
	private String[] toList;
	private Context _cont;
	private boolean _update = false;
	private final int MENU_EXPORT = 0;
	private final int MENU_RESTORE = 1;
	private final int MENU_SQL = 2;
	private final int MENU_INFO = 3;
	private final int MENU_CREATETABLE = 4;
	private int _dialogClicked;
	private boolean logging = false;
	private boolean newFeatures = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.logD("DBViewer onCreate", logging);
		setContentView(R.layout.dbviewer);
		logging = Prefs.getLogging(this);
		TextView tvDB = (TextView)this.findViewById(R.id.DatabaseToView);
		Button bTab = (Button) this.findViewById(R.id.Tables);
		Button bVie = (Button) this.findViewById(R.id.Views);
		Button bInd = (Button) this.findViewById(R.id.Index);
		Button bQue = (Button) this.findViewById(R.id.Query);
		query = (LinearLayout) this.findViewById(R.id.QueryFrame);
		query.setVisibility(View.GONE);
		bTab.setOnClickListener(this);
		bVie.setOnClickListener(this);
		bInd.setOnClickListener(this);
		bQue.setOnClickListener(this);
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			_cont = tvDB.getContext();
			_dbPath = extras.getString("db");
			tvDB.setText(getText(R.string.Database) + ": " + _dbPath);
			Utils.logD("Opening database " + _dbPath, logging);
			database = new Database(_dbPath, _cont);
			//_SQLiteDb = SQLiteDatabase.openDatabase(_dbPath, null, SQLiteDatabase.OPEN_READWRITE);
			if (!database.isDatabase) {
				Utils.logD("Not a database!", logging);
				Utils.showMessage(getText(R.string.Error).toString(),
						getText(R.string.IsNotADatabase).toString(), _cont);
			} else {
				// database is a database and is opened
				// Test if database is working and not corrupt
				try {
					database.getTables();
					// Store recently opened files
					if (Prefs.getEnableFK(_cont)) {
						database.FKOn();
					}
					int noOfFiles = Prefs.getNoOfFiles(_cont);
					SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
					String files = settings.getString("Recently", null);
					files = Recently.updateList(files, _dbPath, noOfFiles);
					Editor edt = settings.edit();
					edt.putString("Recently", files);
					edt.commit();
//					tables = _db.getTables();
//					views = _db.getViews();
					indexes = database.getIndex();
//					for(String str: tables) {
//						Utils.logD("Table: " + str);
//					}
//					for(String str: views) {
//						Utils.logD("View: " + str);
//					}
					list = (ListView) findViewById(R.id.LVList);
					buildList("Tables");
				} catch (Exception e) {
					Utils.showException(e.getLocalizedMessage(), _cont);
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		Utils.logD("DBViewer onDestroy", logging);
		database.close();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Utils.logD("DBViewer onPause", logging);
		super.onPause();
	}

	@Override
	protected void onRestart() {
		Utils.logD("DBViewer onRestart", logging);
		if (database == null)
			database = new Database(_dbPath, _cont);
		super.onRestart();
	}

	/**
	 * Build / rebuild the lists with tables, views and indexes
	 * @param type
	 */
  //TODO change type to private static final int DISPMODE_INDEX = 0 ...
	// and change to case
	private void buildList(final String type) {  
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		if (type.equals("Clear"))
			toList = new String [] {};
		else if (type.equals("Index"))
			toList = database.getIndex();
		else if (type.equals("Views")) 
			toList = database.getViews();
		else 
			toList = database.getTables();
		int recs = toList.length;
		for (int i = 0; i < recs; i++) {
			map = new HashMap<String, String>();
			map.put("name", toList[i]);
			mylist.add(map);
		}
		SimpleAdapter mSchedule = new SimpleAdapter(this, mylist, R.layout.row,
				new String[] {"name"}, new int[] {R.id.rowtext});
		if (list != null) {
			list.setAdapter(mSchedule);  //2.5 null pointer exception here. Don't know why
			list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position,
						long id) {
					// Do something with the table / view / index clicked on
					selectRecord(type, position);
				}
			});
			list.setOnItemLongClickListener(new OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> parent, View v,
						int position, long arg3) {
					Utils.logD("Long click on list", logging);
					dropSelected(type, position);
					return false;
				}
			});
		} else {
			Utils.showMessage(_cont.getText(R.string.Error).toString(),
					_cont.getText(R.string.StrangeErr).toString(), _cont);
		}
	}

	private void dropSelected(final String type, int position) {
		String name;
		final String sql;
		String msg = "";
		name = toList[position];
		//Utils.logD("Handle: " + type + " " + name);
		if (type.equals("Index")) {
			if (indexes[position].startsWith("sqlite_autoindex_")) {
				sql = "";
				msg = getText(R.string.CannotDeleteAutoIndex) + " " + name;
			}
			else {
				sql = "drop index [" + name + "]";
				msg = getText(R.string.DeleteIndex) + " " + name +"?";
			}
		}
		else if (type.equals("Views")) {
			sql = "drop view [" + name + "]";
			msg = getText(R.string.DeleteView) + " "  + name +"?";
		}
		else if (type.equals("Tables")){
			if (name.equalsIgnoreCase("sqlite_master")
					|| name.equalsIgnoreCase("sqlite_sequence")
					|| name.equalsIgnoreCase("android_metadata")) {
				sql = "";
				msg = getText(R.string.CannotDeleteSysTable) + name;
			} else {
				sql = "drop table [" + name + "]";
				msg = getText(R.string.DeleteTable) + " "  + name +"?";
			}
		}
		else {
			sql = "";
			msg = "This is not happening ;-)";
		}
		Utils.logD(msg, logging);
		if (sql.equals("")) {
			Utils.showMessage(getText(R.string.Error).toString(), msg, _cont);
		} else {
			final Builder yesNoDialog = new AlertDialog.Builder(_cont);
			yesNoDialog.setTitle(getText(R.string.DropItem));
			yesNoDialog.setMessage(msg);
			yesNoDialog.setNegativeButton(getText(R.string.No),
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing
				}});
			yesNoDialog.setPositiveButton(getText(R.string.Yes),
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					// Delete it
					database.executeStatement(sql, _cont);
					buildList(type);
				}});
			yesNoDialog.show();
		}
	}

	/**
	 * Handle the the item clicked on
	 * @param type the type of list
	 * @param position Number of item in the list
	 */
	protected void selectRecord(String type, int position) {
		String name;
		name = toList[position];
		//Utils.logD("Handle: " + type + " " + name);
		if (type.equals("Index")) {
			String indexDef = "";
			if (indexes[position].startsWith("sqlite_autoindex_"))  //2.5 null pointer ex. here
				indexDef = (String) this.getText(R.string.AutoIndex);
			else
				indexDef = database.getIndexDef(indexes[position]);
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(indexDef);
			Utils.showMessage(this.getString(R.string.Message), indexDef, _cont);
			Utils.toastMsg(_cont, this.getString(R.string.IndexDefCopied));
		}
		else if (type.equals("Views")) {
			Intent i = new Intent(this, TableViewer.class);
			i.putExtra("db", _dbPath);
			i.putExtra("Table", name);
			i.putExtra("type", Types.VIEW);
			try {
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in TableViewer showing a view)", logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of hov to generate it", _cont);
			}
		}
		else if (type.equals("Tables")){
			Intent i = new Intent(this, TableViewer.class);
			i.putExtra("db", _dbPath);
			i.putExtra("Table", name);
			i.putExtra("type", Types.TABLE);
			try {
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in TableViewer showing a table)", logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of hov to generate it", _cont);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		Utils.logD("DBViewer OnCLick", logging);
		int key = v.getId();
		if (key == R.id.Tables) {
			buildList("Tables");
		} else if (key == R.id.Views) {
			buildList("Views");
		} else if (key == R.id.Index) {
			buildList("Index");
		} else if (key == R.id.Query) {
			_update = true;
			Intent i = new Intent(this, QueryViewer.class);
			i.putExtra("db", _dbPath);
			try {
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in QueryViewer", logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of how to generate it", _cont);
			}
		} 
	}
	
	/* (non-Javadoc)
	 * Update the lists to ensure new tables (created in query mode) and indexes
	 * are retrieved
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	public void onWindowFocusChanged(boolean hasFocus) {
		Utils.logD("DBViewer onWindowFocusChanged: " + hasFocus, logging);
		if(hasFocus & _update) {
			_update = false;
//			tables = _db.getTables();
//			views = _db.getViews();
			indexes = database.getIndex();
			buildList("Tables");
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_EXPORT, 0, getText(R.string.Export));
		menu.add(0, MENU_RESTORE, 0, getText(R.string.Restore));
		// Open files with SQL scripts, execute one or all commands 		
		menu.add(0, MENU_SQL, 0, getText(R.string.OpenSQL));
		menu.add(0, MENU_INFO, 0, getText(R.string.DBInfo));
		if (newFeatures )
			menu.add(0, MENU_CREATETABLE, 0, getText(R.string.CreateTable));
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_EXPORT:
			_dialogClicked = MENU_EXPORT; 
			showDialog(MENU_EXPORT);
			break;
		case MENU_RESTORE:
			_dialogClicked = MENU_RESTORE;
			showDialog(MENU_RESTORE);
			break;
		case MENU_SQL:
			_dialogClicked = MENU_SQL; 
			showDialog(MENU_SQL);
			break;
		case MENU_INFO:
			String versionStr = database.getVersionInfo();
			Utils.showMessage(getText(R.string.DatabaseInfo).toString(), versionStr, _cont);
			break;
		case MENU_CREATETABLE:
			createTableDialog();
			break;
		}
		return false;
	}
	
	/**
	 * Open a create table dialog where the user can define the table
	 * by adding fields
	 */
	private void createTableDialog() {
		Button newTabNewField;
		Button newTabCancel;
		Button newTabOk;
		final EditText newTabTabName;
		final List<String> fldList = new ArrayList<String>();
		final List<String> fkList = new ArrayList<String>();
		final LinearLayout newTabSV;
		final Dialog createTab = new Dialog(_cont);
		createTab.setContentView(R.layout.create_table);
		createTab.setTitle(getText(R.string.CreateTable));
		newTabNewField = (Button) createTab.findViewById(R.id.newTabAddField);
		newTabCancel = (Button) createTab.findViewById(R.id.newTabCancel);
		newTabOk = (Button) createTab.findViewById(R.id.newTabOK);
		newTabSV = (LinearLayout) createTab.findViewById(R.id.newTabSV);
		newTabTabName = (EditText) createTab.findViewById(R.id.newTabTabName);
		createTab.setTitle(getText(R.string.CreateTable));
		newTabNewField.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				newField();
			}
			private void newField() {
				Button newFieldCancel;
				Button newFieldOk;
				final EditText fName;
				final EditText fDef;
				final CheckBox fNotNull;
				final CheckBox fPK;
				final CheckBox fUnique;
				final CheckBox fAutoInc;
				final CheckBox fDesc;
				final EditText fFKTab;
				final EditText fFKFie;
				final Spinner fSPType;
				final Dialog createField = new Dialog(_cont);
				// data types to be selectable from create field
				final String[] type = { 
						"INTEGER",
						"REAL",
						"TEXT",
						"BLOB",
						"DATE",
						"TIMESTAMP",
						"TIME",
						"INTEGER (strict)",
						"REAL (strict)",
						"TEXT (strict)"
						};
				ArrayAdapter<String> adapterType = new ArrayAdapter<String>(_cont,
						android.R.layout.simple_spinner_item, type);
				createField.setContentView(R.layout.create_field);
				createField.setTitle(getText(R.string.CreateField));
				newFieldCancel = (Button) createField.findViewById(R.id.newFieldCancel);
				newFieldOk = (Button) createField.findViewById(R.id.newFieldOK);
				fName = (EditText) createField.findViewById(R.id.newFldName);
				fNotNull = (CheckBox) createField.findViewById(R.id.newFldNull);
				fPK = (CheckBox) createField.findViewById(R.id.newFldPK);
				fUnique = (CheckBox) createField.findViewById(R.id.newFldUnique);
				fAutoInc = (CheckBox) createField.findViewById(R.id.newFldAutoInc);
				fDesc = (CheckBox) createField.findViewById(R.id.newFldDesc);
				fDef = (EditText) createField.findViewById(R.id.newFldDef);
				fFKTab = (EditText) createField.findViewById(R.id.newFldFKTab);
				fFKFie = (EditText) createField.findViewById(R.id.newFldFKFie);
				fSPType = (Spinner) createField.findViewById(R.id.newFldSpType);
				adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				fSPType.setAdapter(adapterType);
				newFieldCancel.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						createField.dismiss();
					}
				});
				fPK.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Utils.logD("Turning autoinc on / off", logging);
						if (isChecked) {
							//Only turn AutoInc if field is INTEGER
							int iType = fSPType.getSelectedItemPosition();
							String stype = type[iType];
							if (stype.startsWith("INTEGER"))
								fAutoInc.setEnabled(true);
							fDesc.setEnabled(true);
						} else {
							fAutoInc.setEnabled(false);
							fDesc.setEnabled(false);
						}
					}
				});
				// OK clicked on new field
				newFieldOk.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						int iType = fSPType.getSelectedItemPosition();
						String stype = type[iType];
						Utils.logD("Field type = " + stype, logging);
						//Check for name and type not null
						if (!fName.getEditableText().toString().trim().equals("") 
								&& (!(fAutoInc.isChecked() && fDesc.isChecked()))) {
							boolean forceType = false;
							// Build the sql for the field
							String fld = "[";
							String fk = "";
							fld += fName.getEditableText().toString();
							// shod it use forced types?
							if (stype.endsWith("(strict)")) {
								forceType = true;
								fld += "] " + stype.substring(0, stype.indexOf(" "));
							} else {
								fld += "] " + stype;
							}
							if (fPK.isChecked()) {
								fld += " PRIMARY KEY";
								// Sort descending?
								if (fDesc.isChecked()) {
									fld += " DESC";
								} else {
									fld += " ASC";
								}
								// Add order here ASC / DESC
								if (fAutoInc.isChecked()) {
									fld += " AUTOINCREMENT";
								}
							}
							if (fNotNull.isChecked()) 
								fld += " NOT NULL";
							if (fUnique.isChecked()) 
								fld += " UNIQUE";
							// Handle forced type for INTEGER, REAL and TEXT fields
							if (forceType) {
								if (stype.startsWith("INTEGER")) {
									fld += " check(typeof(" + fName.getEditableText().toString() +") = 'integer')";
								} else if (stype.startsWith("REAL")) {
									fld += " check(typeof(" + fName.getEditableText().toString() +") = 'real' " +
											"or typeof(" + fName.getEditableText().toString() +") = 'integer')";
								} else if (stype.startsWith("TEXT")) {
									fld += " typeof(" + fName.getEditableText().toString() +") = 'text')";
								} else {
									//Ups
								}
							}
							if (!fDef.getEditableText().toString().equals("")) {
								fld += " DEFAULT " + fDef.getEditableText().toString();
							}
							if (!fFKFie.getEditableText().toString().trim().equals("") &&
									!fFKTab.getEditableText().toString().trim().equals("")) {
								//Foreign key constraints
								fk += " FOREIGN KEY(["
									+ fName.getEditableText().toString() + "]) REFERENCES ["
									+ fFKTab.getEditableText().toString() + "]([" 
									+ fFKFie.getEditableText().toString() + "])";
								Utils.logD("FK " + fk , logging);
							}
							LinearLayout ll = new LinearLayout(_cont);
							ll.setOrientation(LinearLayout.HORIZONTAL);
							TextView tw = new TextView(_cont);
							tw.setText(fld);
							ll.addView(tw);
							ll.setPadding(5, 5, 5, 5);
							newTabSV.addView(ll);
							fldList.add(fld);
							if (!fk.trim().equals("")) {
								LinearLayout llfk = new LinearLayout(_cont);
								llfk.setOrientation(LinearLayout.HORIZONTAL);
								TextView twfk = new TextView(_cont);
								twfk.setText(fk);
								llfk.addView(twfk);
								llfk.setPadding(5, 5, 5, 5);
								newTabSV.addView(llfk);
							}
							if (!fk.trim().equals(""))
								fkList.add(fk);
							createField.dismiss();
						} else {
							String msg = "";
							if (fName.getEditableText().toString().trim().equals("")) {
								Utils.logD("No field name", logging);
								msg = getText(R.string.MustEnterFieldName).toString();
							}
							if ((fAutoInc.isChecked() && fDesc.isChecked())) {
								Utils.logD("DESC & AutoInc", logging);
								getText(R.string.DescAutoIncError).toString();
								msg += "\n" + getText(R.string.DescAutoIncError).toString();
							}
							Utils.showMessage(getText(R.string.Error).toString(),
									msg, _cont);
						}
					}
				});
				createField.show();
			}
		});
		newTabCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				createTab.dismiss();
			}
		});
		newTabOk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// build create table SQL if enough informations
				if (!(newTabTabName.getEditableText().toString().equals(""))
						&& (fldList.size() > 0)) {
					String sql = "create table ["
						+ newTabTabName.getEditableText().toString()
						+ "] (";
					Iterator<String> it = fldList.iterator();
					while (it.hasNext()) {
						sql += it.next();
						if (it.hasNext())
							 sql += ", ";
					}
					if (fkList.size() > 0) {
						sql += " ,";
						it = fkList.iterator();
						while (it.hasNext()) {
							sql += it.next();
							if (it.hasNext())
								 sql += ", ";
							else
								sql += ")";
						}
					} else
						sql += ")";
					//Utils.showMessage("SQL", sql, _cont);
					//Execute sql
					Utils.logD("Executing " + sql, logging);
					database.executeStatement(sql, _cont);
					createTab.dismiss();
					//Refresh list of tables
					buildList("Tables");   
				} else {
					// not enough inf.
					Utils.showMessage(getText(R.string.Error).toString(),
							getText(R.string.MustEnterTableNameAndOneField).toString(), _cont);
				}
			}
		}); 
		createTab.show();
	}

	protected Dialog onCreateDialog(int id) 
	{
		switch (id) {
		case MENU_EXPORT:
			//Utils.logD("Creating MENU_EXPORT");
			Dialog export = new AlertDialog.Builder(this)
					.setTitle(getText(R.string.Export))
					.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler())
					.setNegativeButton(getText(R.string.Cancel), null)
					.create();
			return export;
		case MENU_RESTORE:
			//Utils.logD("Creating MENU_RESTORE");
			Dialog restore = new AlertDialog.Builder(this)
					.setTitle(getText(R.string.Restore))
					.setMessage(getString(R.string.Patience))
					.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler())
					.setNegativeButton(getText(R.string.Cancel), null)
					.create();
			return restore;
		case MENU_SQL:
			Dialog sql = new AlertDialog.Builder(this).setTitle(getText(R.string.OpenSQL))
			.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler())
			.setNegativeButton(getText(R.string.Cancel), null)
			.create();
			return sql;
			
		}
		return null;
	}

	/**
	 * Click handler for the Export and Restore menus  
	 * @author Andsen
	 */
	public class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int clicked) {
			//Utils.logD("Dialog: " + dialog.getClass().getName());
			switch (clicked) {
			// OK button clicked
			case DialogInterface.BUTTON_POSITIVE:
				//Utils.logD("OK pressed");
				// Find the menu from which the OK button was clicked
				switch (_dialogClicked) {
				case MENU_EXPORT:
					database.exportDatabase();
					Utils.toastMsg(_cont, getString(R.string.DataBaseExported));
					break;
				case MENU_RESTORE:
					database.restoreDatabase();
					Utils.toastMsg(_cont, getString(R.string.DataBaseRestored));
					break;
				case MENU_SQL:
					//Utils.logD("Open SQL file");
					Intent i = new Intent(_cont, NewFilePicker.class);
					i.putExtra("SQLtype", true);
					i.putExtra("dbPath", _dbPath);
					try {
						startActivity(i);
					} catch (Exception e) {
						Utils.logE("Error in NewFilePicker", logging);
						e.printStackTrace();
						Utils.showException("Plase report this error with descriptions of how to generate it", _cont);
					}
					//TODO call NewFIlePicker with db(?) = true
					
					break;
				}
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				finish();
				break;
			}
		}
	}
}