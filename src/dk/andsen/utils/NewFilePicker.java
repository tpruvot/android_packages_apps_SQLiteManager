package dk.andsen.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import dk.andsen.asqlitemanager.FileHolder;
import dk.andsen.asqlitemanager.R;
import dk.andsen.utils.FilePicker.FileComparator;

public class NewFilePicker extends ListActivity {
  private EfficientAdapter adap;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	private TextView myPath;
	private List<String> itemList = null;
	private List<String> pathList = null;
	private static String root="/";
	private String currentPath;
	
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.newfilepicker);
		myPath = (TextView)findViewById(R.id.path);

		String state = Environment.getExternalStorageState();
		File path = null;
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
			path = Environment.getExternalStorageDirectory();
			File programDirectory = new File(path.getAbsolutePath());
			currentPath = programDirectory.getAbsolutePath();
			//getDir(programDirectory.getAbsolutePath());
			//getDir(currentPath);
	    adap = new EfficientAdapter(this, currentPath);
	    setListAdapter(adap);
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

  private void getDir(String dirPath) {
		myPath.setText("Location: " + dirPath);
		itemList = new ArrayList<String>();
		pathList = new ArrayList<String>();
		File f = new File(dirPath);
		File[] files = f.listFiles();
		if(!dirPath.equals(root))
		{
			itemList.add(root);
			pathList.add(root);
			itemList.add("../");
			pathList.add(f.getParent());
		}
		Arrays.sort(files, new FileComparator());
		for(int i=0; i < files.length; i++)
		{
			File file = files[i];
			pathList.add(file.getPath());
			if(file.isDirectory())
				itemList.add(file.getName() + "/");
			else
				itemList.add(file.getName());
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
  
	@Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Toast.makeText(this, "Click " + String.valueOf(position), Toast.LENGTH_SHORT).show();
  }

  public static class EfficientAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private Context context;
    private FileHolder[] fileH;
    
    
    
    public EfficientAdapter(Context context, String path) {
      // Cache the LayoutInflate to avoid asking for a new one each time.
    	int noOfFiles;
    	File f = new File(path);
    	File[] files = f.listFiles();
    	//TODO Arrays.sort(files, new FileComparator());
    	noOfFiles = files.length;
    	if(!path.equals(root)) {
    		noOfFiles += 2;
    	}
    	fileH = new FileHolder[noOfFiles];
    	int top = 0;
    	if(!path.equals(root)) {
    		FileHolder h1 = new FileHolder();
    		h1.setDirectory(true);
    		h1.setFileName(root);
    		fileH[0] = h1;
    		FileHolder h2 = new FileHolder();
    		h2.setDirectory(true);
    		h2.setFileName("..");
    		fileH[1] = h2;
    		top = 2;
    	}
    	for (int i = 0; i < noOfFiles - top; i++) {
    		Utils.logD("FileNo " + i);
    		FileHolder h = new FileHolder();
    		h.setDirectory(files[i].isDirectory());
    		h.setFileName(files[i].getName());
    		fileH[i+top] = h;
    	}
      mInflater = LayoutInflater.from(context);
      this.context = context;
    }

    /**
     * Make a view to hold each row.
     * 
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(final int position, View convertView, ViewGroup parent) {
      // A ViewHolder keeps references to children views to avoid
      // unneccessary calls
      // to findViewById() on each row.
      ViewHolder holder;
      // When convertView is not null, we can reuse it directly, there is
      // no need
      // to reinflate it. We only inflate a new View when the convertView
      // supplied
      // by ListView is null.
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.adaptor_content, null);
        // Creates a ViewHolder and store references to the two children
        // views
        // we want to bind data to.
        holder = new ViewHolder();
        holder.textLine = (TextView) convertView.findViewById(R.id.text);
        holder.iconLine = (ImageView) convertView.findViewById(R.id.icon);
        Utils.logD("Image0: " + R.drawable.flag_0);
        if (fileH[position].isDirectory()) {
          holder.iconLine.setImageResource(R.drawable.ic_folder);
        } else {
        	holder.iconLine.setImageResource(R.drawable.ic_document);
        }
        String path = fileH[position].getFileName();
        holder.textLine.setText(path);
        convertView.setOnClickListener(new OnClickListener() {
          private int pos = position;
          public void onClick(View v) {
            Toast.makeText(context, "Click " + pos, Toast.LENGTH_SHORT).show();    
          }
        });
        convertView.setTag(holder);
      } else {
        // Get the ViewHolder back to get fast access to the TextView
        // and the ImageView.
        holder = (ViewHolder) convertView.getTag();
      }
      // Get flag name and id
      String filename = "flag_" + String.valueOf(position);
      
      //int id = context.getResources().getIdentifier(filename, "drawable", "dk.andsen.sqlitemanager");
         Utils.logD("Flag " + filename);
      // Icons bound to the rows.
      //if (id != 0x0) {
      //  mIcon1 = BitmapFactory.decodeResource(context.getResources(), id);
      //}
      // Bind the data efficiently with the holder.
      //holder.iconLine.setImageResource(R.drawable.flag_1);
      holder.textLine.setText(fileH[position].getFileName());
      return convertView;
    }
    static class ViewHolder {
      TextView textLine;
      ImageView iconLine;
    }
    public Filter getFilter() {
      // TODO Auto-generated method stub
      return null;
    }
    public long getItemId(int position) {
      // TODO Auto-generated method stub
      return 0;
    }
    public int getCount() {
      // TODO Auto-generated method stub
      return fileH.length;
    }
    public Object getItem(int position) {
      // TODO Auto-generated method stub
      return fileH[position];
    }
  }
} 