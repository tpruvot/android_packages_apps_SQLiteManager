package dk.andsen.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import dk.andsen.asqlitemanager.R;

/**
 * @author andsen
 *
 */
public class FilePicker extends ListActivity {

	private List<String> item = null;
	private List<String> path = null;
	private String root="/";
	private TextView myPath;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filepicker);
		myPath = (TextView)findViewById(R.id.path);
		File path = null;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
			path = Environment.getExternalStorageDirectory();
			File programDirectory = new File(path.getAbsolutePath() + "/a41/");
			// have the object build the directory structure, if needed.
			programDirectory.mkdirs();
			getDir(programDirectory.getAbsolutePath());
		} else {
			// No SDCard
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon)
			.setTitle("No SDCard available")
			.setPositiveButton("OK", 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			}).show();
		}
	}

	private void getDir(String dirPath)
	{
		myPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		File f = new File(dirPath);
		File[] files = f.listFiles();
		if(!dirPath.equals(root))
		{
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}
		for(int i=0; i < files.length; i++)
		{
			File file = files[i];
			path.add(file.getPath());
			if(file.isDirectory())
				item.add(file.getName() + "/");
			else
				item.add(file.getName());
		}
		ArrayAdapter<String> fileList =
			new ArrayAdapter<String>(this, R.layout.filerow, item);
		setListAdapter(fileList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(path.get(position));
		if (file.isDirectory())
		{
			if(file.canRead())
				getDir(path.get(position));
			else
			{
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.icon)
				.setTitle("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK", 
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
			}
		} else {
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon)
			.setTitle("[" + file.getAbsolutePath() + "]")
			.setPositiveButton("OK", 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			}).show();
		}
	}
}