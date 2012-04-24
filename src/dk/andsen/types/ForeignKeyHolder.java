/**
 * @author Andsen
 * 
 * This class is used to hold a list of foreign keys for foreign key selection lists
 * in the editor
 *
 */
package dk.andsen.types;

public class ForeignKeyHolder {
	private String ids[];
	private String texts[];
	
	/**
	 * Retrieve the foreign key values
	 * @return a String [] representation of the foreign keys
	 */
	public String[] getIds() {
		return ids;
	}
	/**
	 * Set the foreign key values
	 * @param ids - the values represented as a String []
	 */
	public void setId(String[] ids) {
		this.ids = ids;
	}
	
	/**
	 * Get the text describing the values associated to the foreign key 
	 * @return A String describing the record
	 */
	public String[] getText() {
		return texts;
	}
	
	/**
	 * Set the text that describes the the values associated to the foreign key
	 * @param text A String describing the record
	 */
	public void setText(String[] texts) {
		this.texts = texts;
	}
	

}
