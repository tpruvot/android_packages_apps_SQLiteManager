package dk.andsen.asqlitemanager;

public class QueryResult {
	public String[] columnNames;
	public String [][] Data;

	public String[] getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	public String[][] getData() {
		return Data;
	}
	public void setData(String[][] data) {
		Data = data;
	}
}
