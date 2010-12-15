package dk.andsen.asqlitemanager;

import java.util.ArrayList;
import java.util.List;
import dk.andsen.utils.Utils;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author Andsen
 *
 */
public class Database {
	public boolean isDatabase = false;
	private SQLiteDatabase _db = null;
	private String _dbPath; 
	
	/**
	 * Open a existing database at the given path
	 * @param dbPath Path to the database
	 */
	public Database(String dbPath, Context cont) {
		_dbPath = dbPath;
		try {
			_db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
			isDatabase = true;
		} catch (Exception e) {
			// not a database
			isDatabase = false;
		}
	}
	
	/**
	 * Close the database
	 */
	public void close() {
		_db.close();
	}

	/**
	 * Test the database if not open open it
	 */
	private void testDB() {
		if (_db == null) {
			_db = SQLiteDatabase.openDatabase(_dbPath, null, SQLiteDatabase.OPEN_READWRITE);
		}
		if (!_db.isOpen()) {
			_db = SQLiteDatabase.openDatabase(_dbPath, null, SQLiteDatabase.OPEN_READWRITE);
		}
	}
	
	/**
	 * Retrieve all the table names of the database
	 * @return
	 */
	public String[] getTables() {
		testDB();
		String sql ="select name from sqlite_master where type = 'table'";
		Cursor res = _db.rawQuery(sql, null);
		int recs = res.getCount();
		String[] tables = new String[recs];
		int i = 0;
		Utils.logD("Views: " + recs);
		while(res.moveToNext()) {
			tables[i] = res.getString(0);
			i++;
		}
		res.close();
		return tables;
	}

	/**
	 * Retrieve all views from a database
	 * @return
	 */
	public String[] getViews() {
		testDB();
		String sql ="select name from sqlite_master where type = 'view'";
		Cursor res = _db.rawQuery(sql, null);
		int recs = res.getCount();
		String[] views = new String[recs];
		int i = 0;
		Utils.logD("Views: " + recs);
		while(res.moveToNext()) {
			views[i] = res.getString(0);
			i++;
		}
		res.close();
		return views;
	}

	/**
	 * Retrieve all views from a database
	 * @return
	 */
	public String[] getIndex() {
		testDB();
		String sql ="select name from sqlite_master where type = 'index'";
		Cursor res = _db.rawQuery(sql, null);
		int recs = res.getCount();
		String[] index = new String[recs];
		int i = 0;
		Utils.logD("Index: " + recs);
		while(res.moveToNext()) {
			index[i] = res.getString(0);
			i++;
		}
		res.close();
		return index;
	}

	/**
	 * Retrieve a list of field names from a table
	 * @param table
	 * @return
	 */
	public Field[] getFields(String table) {
		// Get field type
		// SELECT typeof(sql) FROM sqlite_master where typeof(sql) <> "null" limit 1
		testDB();
		String sql = "select * from " + table + " limit 1";
		sql = "pragma table_info(" + table + ")";
		Cursor res = _db.rawQuery(sql, null);
		int cols = res.getCount();
		Field[] fields = new Field[cols];
		int i = 0;
		// getting field names
		while(res.moveToNext()) {
			Field field = new Field();
			field.setFieldName(res.getString(1));
			field.setFieldType(res.getString(2));
			field.setNotNull(res.getInt(3));
			field.setDef(res.getString(4));
			field.setPk(res.getInt(5));
			fields[i] = field;
			i++;
		}
		res.close();
		return fields;
	}

	/** 
	 * Return a String list with all field names of the table
	 * @param table
	 * @return
	 */
	public String[] getFieldsNames(String table) {
		testDB();
		String sql = "pragma table_info(" + table + ")";
		Cursor res = _db.rawQuery(sql, null);
		int cols = res.getCount();
		String[] fields = new String[cols];
		int i = 0;
		// getting field names
		while(res.moveToNext()) {
			fields[i] = res.getString(1);
			i++;
		}
		res.close();
		return fields;
	}
	
	/**
	 * Retrieve the number of columns in a table
	 * @param table
	 * @return
	 */
	public int getNumCols(String table) {
		testDB();
		String sql = "select * from " + table + " limit 1";
		Cursor cursor = _db.rawQuery(sql, null);
		int cols = cursor.getColumnCount();
		cursor.close();
		return cols;
	}

	/**
	 * Retrieve all data form the tables and return it as two dimentional string list
	 * @param table
	 * @return
	 */
	public String[][] getTableData(String table, int offset, int limit) {
		testDB();
		String sql = "select * from " + table + " limit " + limit + " offset " + offset;
		Utils.logD("SQL = " + sql);
		Cursor cursor = _db.rawQuery(sql, null);
		int cols = cursor.getColumnCount();
		int rows = cursor.getCount();
		String[][] res = new String[rows][cols];
		int i = 0;
		//int j = 0;
		while(cursor.moveToNext()) {
			for (int k=0; k<cols; k++) {
				res[i][k] = cursor.getString(k);
			}
			i++;
		}
		return res;
	}

	/**
	 * Return the SQL that defines the table
	 * @param table
	 * @return a String[] with sql needed to create the table
	 */
	public String[][] getSQL(String table) {
		testDB();
		String sql = "select sql from sqlite_master where tbl_name = '" + table +"'";
		Cursor cursor = _db.rawQuery(sql, null);
		int i = 0;
		String[][] res = new String[cursor.getCount()][1];
		// Split SQL in lines
		while(cursor.moveToNext()) {
				res[i][0] = cursor.getString(0);
			i++;
		}
		cursor.close();
		return res;
	}

	/**
	 * Return the headings for a tables structure
	 * @param table
	 * @return
	 */
	public String[] getTableStructureHeadings(String table) {
		String[] ret = {"id", "name","type","notnull","dflt_value","pk"};
		return ret;
	}
	/**
	 * Return table structure i two dimentional string list
	 * @param table
	 * @return
	 */
	public String[][] getTableStructure(String table) {
		testDB();
		String sql = "pragma table_info ("+table+")";
		Cursor cursor = _db.rawQuery(sql, null);
		int cols = cursor.getColumnCount();
		int rows = cursor.getCount();
		String[][] res = new String[rows][cols];
		int i = 0;
		while(cursor.moveToNext()) {
			for (int k=0; k<cols; k++) {
				res[i][k] = cursor.getString(k);
			}
			i++;
		}
		return res;
	}

	/**
	 * Return the result of the query as a comma separated test in String list
	 * @param sql
	 * @return
	 */
	public String[] getSQLQuery(String sql) {
		testDB();
		String[] tables = {"No result"};
		try {
			Cursor res = _db.rawQuery(sql, null);
			int recs = res.getCount();
			tables = new String[recs];
			int i = 0;
			Utils.logD("Views: " + recs);
			while(res.moveToNext()) {
				for(int j = 0; j < res.getColumnCount(); j++) {
					if (j == 0)
						tables[i] = res.getString(j);
					else 
						tables[i] += ", " + res.getString(j);
				}
				i++;
			}
			res.close();
		} catch (Exception e) {
			tables = new String [] {"Error: " + e.toString()};
		}
		return tables;
	}

	public String[] getTablesFieldsNames(String[] tables) {
		testDB();
		Cursor res;
		List<String> tList = new ArrayList<String>();
		int i = 0;
		for (int j = 0; j < tables.length; j++) {
			String sql = "pragma table_info(" + tables[j] + ")";
			Utils.logD("getTablesFieldsNames: " + sql);
			res = _db.rawQuery(sql, null);
			i = 0;
			// getting field names
			while(res.moveToNext()) {
				tList.add(tables[j] + "." + res.getString(1));
				//fields[i] = res.getString(1);
				i++;
			}
			res.close();
		}
		String[] fieldList = new String[tList.size()];
		i = 0;
		for (String str: tList) {
			fieldList[i] = str;
			i++;
		}
		return fieldList;
	}

	/**
	 * Save a SQL statement in a aSQLiteManager table in the current database.
	 * @param saveSql statement to save
	 */
	public void saveSQL(String saveSql) {
		testDB();
		testHistoryTable();
		String sql = "insert into aSQLiteManager (sql) values (\"" + saveSql +"\")";
		try {
			_db.execSQL(sql);
			Utils.logD("SQL save");
		} catch (SQLException e) {
			Utils.logD(e.toString());
		}
	}

	/**
	 * Test for a aSQLiteManager table in the current database. If it does not
	 * exists create it.
	 */
	private void testHistoryTable() {
		testDB();
		Cursor res = _db.rawQuery("select name from sqlite_master where type = \"table\" and name = \"aSQLiteManager\"", null);
		int recs = res.getCount();
		if (recs > 0) {
			return;
		} else {
			// create the aSQLiteManager table
			String sql = "create table aSQLiteManager (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, sql TEXT NOT NULL)";
			_db.execSQL(sql);
			Utils.logD("aSQLiteManager table created");
		}
	}
}
