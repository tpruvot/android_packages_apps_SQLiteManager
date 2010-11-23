package dk.andsen.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import dk.andsen.asqlitemanager.DBViewer;
import dk.andsen.asqlitemanager.R;

/**
 * @author andsen
 *
 */
public class NewFilePicker extends Activity implements OnClickListener {

	private List<String> item = null;
	private List<String> path = null;
	private String root="/";
	private TextView _myPath;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	private Context context = null;
	private TableLayout _aTable;
	private File[] _files;
	private ArrayList<Boolean> dir;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newfilepicker);
		_myPath = (TextView)findViewById(R.id.path);
		_aTable=(TableLayout)findViewById(R.id.filelist);
		context = this.getBaseContext();
		File path = null;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
			path = Environment.getExternalStorageDirectory();
			File programDirectory = new File(path.getAbsolutePath());
			// have the object build the directory structure, if needed.
			//programDirectory.mkdirs();
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
		_myPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		dir = new ArrayList<Boolean>();
		File f = new File(dirPath);
		_files = f.listFiles();
		if(!dirPath.equals(root))
		{
			item.add(root);
			path.add(root);
			dir.add(true);
			item.add("../");
			path.add(f.getParent());
			dir.add(true);
		}
		Arrays.sort(_files, new FileComparator());
		for(int i=0; i < _files.length; i++)
		{
			File file = _files[i];
			path.add(file.getPath());
			if(file.isDirectory()) {
				item.add(file.getName() + "/");
				dir.add(true);
			}
			else {
				item.add(file.getName());
				dir.add(false);
			}
		}
		appendRows();
	}


	private void appendRows() {
		_aTable.removeAllViews();
		for(int i=0; i < item.size(); i++)
		{
			TableRow row = new TableRow(this);
			if (i%2 == 1)
				row.setBackgroundColor(Color.DKGRAY);
			ImageView iV = new ImageView(this);
			TextView tV = new TextView(this);
			if(dir.get(i))
				iV.setImageResource(R.drawable.ic_folder);
			else
				iV.setImageResource(R.drawable.ic_document);
			iV.setPadding(3, 3, 3, 3);
			iV.setId(2000+1);
			tV.setText(item.get(i));
			tV.setPadding(3, 3, 3, 3);
			tV.setId(1000+i);
			tV.setOnClickListener(this);
			row.addView(iV);
			row.addView(tV);
			row.setId(i);
			row.setOnClickListener(this);
			_aTable.addView(row, new TableLayout.LayoutParams());
		}
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		final File file = new File(path.get(position));
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
			.setTitle("View [" + file.getAbsolutePath() + "]")
			.setPositiveButton("OK", 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(context, DBViewer.class);
					i.putExtra("db", ""+ file.getAbsolutePath());
					startActivity(i);
				}
			}).setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//dialog.dismiss();
				}
			}) .show();
		}
	}
	
	/**
	 * Sort files first directories then files	
	 * @author andsen
	 *
	 */
	class FileComparator implements Comparator<File> {
	   
	    public int compare(File file1, File file2){
	    	String f1 = ((File)file1).getName();
	    	String f2 = ((File)file2).getName();
	    	int f1Length = f1.length();
	    	int f2Length = f2.length();
	    	boolean f1Dir = (((File)file1).isDirectory()) ? true: false;
	    	boolean f2Dir = (((File)file2).isDirectory()) ? true: false;
	    	int shortest = (f1Length > f2Length) ? f2Length : f1Length;
	    	// one of the files is a directory
	    	if (f1Dir && !f2Dir)
	    		return -1;
	    	if (f2Dir && !f1Dir)
	    		return 1;
	    	// sort alphabetically
	    	for (int i = 0; i < shortest; i++) {
	    		if (f1.charAt(i) > f2.charAt(i))
	    			return 1;
	    		else if (f1.charAt(i) < f2.charAt(i))
	    			return -1;
	    	}
	    	if (f1Length > f2Length)
	    		return 1;
	    	else
	    		return 0; 
	    }
	}

	public void onClick(View v) {
		final int pos = v.getId();
		int row = pos;
		if (pos >= 1000) {
			row = pos - 1000;
		}
		Utils.logD("Table row: " + pos + " clicked");
		
		if (row < 2) {
			getDir(path.get(row));
		} else {
			if (_files[row-2].isDirectory())
			{
				if(_files[row-2].canRead())
					getDir(path.get(row));
				else
				{
					new AlertDialog.Builder(this)
					.setIcon(R.drawable.icon)
					.setTitle("[" + _files[row-2].getName() + "] folder can't be read!")
					.setPositiveButton("OK", 
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
				}
			} else {
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.icon)
				.setTitle("View [" + _files[row-2].getAbsolutePath() + "]")
				.setPositiveButton("OK", 
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(context, DBViewer.class);
						int row = pos;
						if (pos >= 1000)
							row = pos - 1000;
						i.putExtra("db", ""+ _files[row-2].getAbsolutePath());
						startActivity(i);
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//dialog.dismiss();
					}
				}) .show();
			}
		}
		//getDir(path.get(pos));
	}
}