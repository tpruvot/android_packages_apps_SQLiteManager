package dk.andsen.types;

public class AField {
	private String fieldData;
	private FieldType fieldType;
	
  public enum FieldType {
    NULL,
    INTEGER,
    REAL,
    TEXT,
    BLOB,
    UNRESOLVED
  }
	
  public String toString() {
  	return "Type = " + fieldType + " Data = " + fieldData;
  }
  
	public String getFieldData() {
		return fieldData;
	}
	public void setFieldData(String fieldData) {
		this.fieldData = fieldData;
	}
	public FieldType getFieldType() {
		return fieldType;
	}
	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}
}
