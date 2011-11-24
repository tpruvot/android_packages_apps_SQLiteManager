package dk.andsen.RecordEditor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import dk.andsen.RecordEditor.types.TableField;
import dk.andsen.asqlitemanager.Database;
import dk.andsen.asqlitemanager.R;
import dk.andsen.utils.Utils;

/**
 * 
 * @author Andsen Due to the Android input interface Boolean fields can only be
 *         true / false (not null)
 */
public class RecordEditorBuilder {
	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;
	private ScrollView sv;
	private TableField[] _fields;
	private Context _cont;

	/**
	 * The base of id's of the edit fields linked to each of the fields
	 */
	public static final int idBase = 1000;
	/**
	 * The base of id's of the LinearLayout that holds info about each record
	 */
	public static final int lineIdBase = 2000;
	/**
	 * If this is set to true empty strings are treated as null
	 */
	private boolean treatEmptyFieldsAsNull = true;
	private int fieldNameWidth = 100;
	Database _db;
	private boolean useSelectLists = false;
	/**
	 * 
	 * @param fields
	 * @param cont
	 */
	public RecordEditorBuilder(TableField[] fields, Context cont, Database db) {
		_db = db;
		_cont = cont;
		_fields = fields;
		sv = new ScrollView(cont);
		sv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		sv.setPadding(5, 5, 5, 5);
		LinearLayout lmain = new LinearLayout(cont);
		lmain.setOrientation(LinearLayout.VERTICAL);
		lmain.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		// Add a linearLayout to hold the label and field to edit
		for (int i = 0; i < fields.length; i++) {
			Utils.logD("Field: " + fields[i].getName() + " fk: " + fields[i].getForeignKey());
			LinearLayout ll;
			if (fields[i].isUpdateable()) {
				ll = new LinearLayout(cont);
				// Use selection list for this field
				//TODO can't do this for all types of fields
				if (fields[i].getForeignKey() != null && useSelectLists) {
					Utils.logD("Uses list of FK for " + fields[i].getName());
					// how to handle this in validation and when reading data?
					ll = buildFKList(fields[i], lineIdBase +i, idBase + i);
				} else {
					// Normal input field based on type of field 
					ll = buildEditField(fields[i], lineIdBase + i, idBase + i);
				}
				lmain.addView(ll);
			}
		}
		//TODO add OK and cancel buttons here to have them on the scroll view??
		sv.addView(lmain);
	}

	private LinearLayout buildEditField(TableField field,int llId, int id) {
		LinearLayout ll = new LinearLayout(_cont);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		ll.setId(llId);
		
		// create the label and data field pair in a Linear Layout
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		// Id added to be able to find the line in the layout
		ll.setId(llId);
		// Add the label using display name if present else field name
		TextView tv = new TextView(_cont);
		tv.setLayoutParams((new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT)));
		if (field.getDisplayName() == null)
			tv.setText(field.getName());
		else
			tv.setText(field.getDisplayName());
		tv.setWidth(fieldNameWidth);
		tv.setPadding(5, 0, 5, 0);
		ll.addView(tv);
		/*
		 * Add edit fields to match the fields type
		 * 
		 * TODO in case of referential constraints show data in spinner (needs
		 * database in arguments to do that)
		 */
		switch (field.getType()) {
		case (TableField.TYPE_DATE):
			// change to button with DatePicker
			//DatePicker dp = new DatePicker(cont);
			//dp.setLayoutParams((new LayoutParams(LayoutParams.FILL_PARENT,
			//		LayoutParams.WRAP_CONTENT)));
			//dp.set
			//dp.setTag(fields[i].getValue());
			EditText etd = new EditText(_cont);
			etd.setLayoutParams((new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT)));
			etd.setText(field.getValue());
			etd.setId(id);
			etd.setInputType(InputType.TYPE_CLASS_DATETIME
					| InputType.TYPE_DATETIME_VARIATION_DATE);
			ll.addView(etd);
			break;
		case (TableField.TYPE_DATETIME):
			EditText etdt = new EditText(_cont);
			etdt.setLayoutParams((new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT)));
			etdt.setText(field.getValue());
			etdt.setInputType(InputType.TYPE_CLASS_DATETIME
					| InputType.TYPE_DATETIME_VARIATION_NORMAL);
			etdt.setId(id);
			ll.addView(etdt);
			break;
		case (TableField.TYPE_FLOAT):
			EditText etf = new EditText(_cont);
			etf.setLayoutParams((new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT)));
			etf.setText(field.getValue());
			etf.setInputType(InputType.TYPE_CLASS_NUMBER
					| InputType.TYPE_NUMBER_FLAG_SIGNED
					| InputType.TYPE_NUMBER_FLAG_DECIMAL);
			etf.setId(id);
			ll.addView(etf);
			break;
		case (TableField.TYPE_INTEGER):
			EditText eti = new EditText(_cont);
			eti.setLayoutParams((new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT)));
			eti.setText(field.getValue());
			eti.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_SIGNED);
			eti.setId(id);
			ll.addView(eti);
			break;
		case (TableField.TYPE_TIME):
			//TODO change to time picker
			EditText ett = new EditText(_cont);
			ett.setLayoutParams((new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT)));
			ett.setText(field.getValue());
			ett.setInputType(InputType.TYPE_CLASS_DATETIME
					| InputType.TYPE_DATETIME_VARIATION_TIME);
			ett.setId(id);
			ll.addView(ett);
			break;
		case (TableField.TYPE_BOOLEAN):
			CheckBox etb = new CheckBox(_cont);
			etb.setLayoutParams((new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT)));
			etb.setChecked((field.getValue() == null) ? false : int2boolean(field.getValue()));
			etb.setId(id);
			ll.addView(etb);
			break;
		case (TableField.TYPE_PHONENO):
			EditText etp = new EditText(_cont);
			etp.setLayoutParams((new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT)));
			etp.setText(field.getValue());
			etp.setHint(field.getHint());
			etp.setId(id);
			etp.setInputType(InputType.TYPE_CLASS_PHONE);
			ll.addView(etp);
			break;
		default: // treat the rest as Strings
			if (field.getForeignKey() != null) {
				Utils.logD("Should user list of FK");
			} else {
				Utils.logD("NO FK");
			}
			EditText ets = new EditText(_cont);
			ets.setLayoutParams((new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT)));
			ets.setText(field.getValue());
			ets.setHint(field.getHint());
			ets.setId(id);
			ll.addView(ets);
			break;
		}
		return ll;
	}

	
	/**
	 * Builds a Button with a dialog with fk's attached
	 * @param foreignKey
	 * @param notNull
	 * @param id
	 * @return
	 */
	private LinearLayout buildFKList(TableField field,int llId, int id) {
		LinearLayout ll = new LinearLayout(_cont);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		ll.setId(llId);
		final String[] fk = _db.getFKList(field.getForeignKey());
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(_cont,
		    android.R.layout.simple_spinner_dropdown_item, fk);
		final Button btn = new Button(_cont);
		btn.setText("Select from list");
		//TODO should store selected value in invisible TextView
		btn.setId(id);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			  new AlertDialog.Builder(_cont)
			  .setTitle("Select value")
			  .setAdapter(adapter, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {
			    	Utils.logD("Item pressed:" + which);
			    	String val = fk[which];
			    	Utils.logD("Value selected:" + val);
			    	btn.setText(val);
			      dialog.dismiss();
			    }
			  }).create().show();			}
		});
		ll.addView(btn);
		return ll;
	}

	/**
	 * Convert a SQLite 0 / 1 to their boolean values. All but 1 are
	 * treated as false
	 * @param val
	 * @return
	 */
	private boolean int2boolean(String val) {
		if (val.equals("1")) 
			return true;
		return false;
	}

	/**
	 * Build a ScrollView ready to edit a record. The input type are defined by
	 * the data type.
	 * 
	 * @return a ScrollView to edit all the fields of TableField list
	 */
	public ScrollView getScrollView() {
		return sv;
	}

	/**
	 * Check the edited data against the fields definition
	 * 
	 * @param sv
	 *          the ScrollView created by getScrollView containing the edited data
	 * @return a String with validation errors otherwise null
	 */
	public String checkInput(ScrollView sv) {
		String errorMsg = null;
		TableField[] fields = getEditedData(sv);
		for (int i = 0; i < _fields.length; i++) {
			// check for empty / null not null fields
			if (fields[i].getNotNull() != null && fields[i].getNotNull())
				if ((fields[i].getValue() == null) ||
						(fields[i].getValue().equals("") && treatEmptyFieldsAsNull)) {
					if (errorMsg != null)
						errorMsg += "\n" + _fields[i].getDisplayName() + " "
							+ _cont.getText(R.string.MustNotBeEmpty);
					else
						errorMsg = _fields[i].getDisplayName() + " "
							+ _cont.getText(R.string.MustNotBeEmpty);
				}
			// TODO should also check input data types(?) and constraints

		}
		// errorMsg =
		// "No validation yet :-(\nLong errormessages\ndoes works and can be\nused to give a report about\nbroken constraints";
		return errorMsg;
	}

	/**
	 * @param sv
	 *          the ScrollView created by getScrollView after editing
	 * @return a list of TableFields with the value field updated to reflect the
	 *         edited values form the ScrollView
	 */
	public TableField[] getEditedData(ScrollView sv) {
		String res = null;
		for (int i = 0; i < _fields.length; i++) {
			if (_fields[i].isUpdateable()) {
				switch (_fields[i].getType()) {
				case TableField.TYPE_BOOLEAN:
					CheckBox cb = (CheckBox) sv.findViewById(idBase + i);
					Boolean val = cb.isChecked();
					res = val.toString();
					break;
					// Now treated as text
//				case TableField.TYPE_DATE:
//					// TODO Move data from View to TableField list
//					break;
//				case TableField.TYPE_DATETIME:
//					// TODO Move data from View to TableField list
//					break;
//				case TableField.TYPE_TIME:
//					// TODO Move data from View to TableField list
//					break;
				case TableField.TYPE_FLOAT:
					TextView tvFloat = (TextView) sv.findViewById(idBase + i);
					res = tvFloat.getEditableText().toString();
					//if (treatEmptyFieldsAsNull)
						if (res.equals(""))
							res = null;
					break;
				case TableField.TYPE_INTEGER:
					TextView tvInteger = (TextView) sv.findViewById(idBase + i);
					res = tvInteger.getEditableText().toString();
					if (res.equals(""))
						res = null;
					break;
				default: // treat is as a String
					TextView tvString = (TextView) sv.findViewById(idBase + i);
					res = tvString.getEditableText().toString();
					if (treatEmptyFieldsAsNull)
						if (res.equals(""))
							res = null;
					break;
				}
				_fields[i].setValue(res);
			}
		}
		return _fields;
	}

	/**
	 * Set the with of the field name in pixels on the edit dialog
	 * 
	 * @param fieldNameWidth
	 */
	public void setFieldNameWidth(int fieldNameWidth) {
		this.fieldNameWidth = fieldNameWidth;
	}

	/**
	 * @return
	 */
	public int getFieldNameWidth() {
		return fieldNameWidth;
	}

	/**
	 * If this is set to true empty strings are treated as null
	 * 
	 * @param treatEmptyFieldsAsNull
	 */
	public void setTreatEmptyFieldsAsNull(boolean treatEmptyFieldsAsNull) {
		this.treatEmptyFieldsAsNull = treatEmptyFieldsAsNull;
	}

	/**
	 * @return the treatEmptyStringsAsNull
	 */
	public boolean isTreatEmptyFieldsAsNull() {
		return treatEmptyFieldsAsNull;
	}

}
