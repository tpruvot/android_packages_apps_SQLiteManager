package dk.andsen.asqlitemanager;

import dk.andsen.utils.Utils;
import android.content.Context;
import android.database.Cursor;
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

	public String[] getFieldsNames(String table) {
		testDB();
		String sql = "select * from " + table + " limit 1";
		sql = "pragma table_info(" + table + ")";
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
	public String[][] getTableData(String table) {
		testDB();
		String sql = "select * from " + table;
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
	public String[] getSQL(String table) {
		String [] res;
		testDB();
		String sql = "select sql from sqlite_master where tbl_name = '" + table +"'";
		Cursor cursor = _db.rawQuery(sql, null);
		int i = 0;
		res = new String[cursor.getCount()];
		// Split SQL in lines
		while(cursor.moveToNext()) {
				res[i] = cursor.getString(0);
			i++;
		}
		cursor.close();
		return res;
	}

}
