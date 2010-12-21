/**
 * Part of one of andsens open source project (a41cv / aSQLiteManager) 
 *
 * @author andsen
 *
 */
package dk.andsen.utils;

import java.io.File;
import java.util.Arrays;
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
import dk.andsen.asqlitemanager.R;

public class NewFilePicker extends ListActivity {
  private EfficientAdapter adap;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	private TextView myPath;
//	private List<String> itemList = null;
//	private List<String> pathList = null;
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
			myPath.setText(currentPath);
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

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Toast.makeText(this, "Click " + String.valueOf(position), Toast.LENGTH_SHORT).show();
  }

  public static class EfficientAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private Context context;
    private FileHolder[] fileH;
    private String _path;
    private boolean rebuild = false;
    
    public EfficientAdapter(Context context, String path) {
      // Cache the LayoutInflate to avoid asking for a new one each time.
    	_path = path;
    	getFiles();
      mInflater = LayoutInflater.from(context);
      this.context = context;
    }

    private void getFiles() {
    	int noOfFiles;
    	Utils.logD(_path);
    	File f = new File(_path);
    	File[] files = f.listFiles();
    	Arrays.sort(files, new FileComparator());
    	noOfFiles = files.length;
    	if(!_path.equals(root)) {
    		noOfFiles += 2;
    	}
    	fileH = new FileHolder[noOfFiles];
    	int top = 0;
    	if(!_path.equals(root)) {
    		FileHolder h1 = new FileHolder();
    		h1.setDirectory(true);
    		h1.setFilePath(root);
    		h1.setFileName(root);
    		fileH[0] = h1;
    		FileHolder h2 = new FileHolder();
    		h2.setDirectory(true);
    		h2.setFilePath(f.getPath());
    		h2.setFileName("..");
    		fileH[1] = h2;
    		top = 2;
    	}
    	FileHolder h;
    	for (int i = 0; i < noOfFiles - top; i++) {
    		Utils.logD("FileNo " + i);
    		h = new FileHolder();
    		h.setDirectory(files[i].isDirectory());
    		h.setFileName(files[i].getName());
    		h.setFilePath(files[i].getAbsolutePath());
    		fileH[i+top] = h;
    	}
    }
    
    /**
     * Make a view to hold each row.
     * 
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(final int position, View convertView, ViewGroup parent) {
    	Utils.logD("getView " + position);
      // A ViewHolder keeps references to children views to avoid
      // unneccessary calls
      // to findViewById() on each row.
      ViewHolder holder;
      // When convertView is not null, we can reuse it directly, there is
      // no need
      // to reinflate it. We only inflate a new View when the convertView
      // supplied
      // by ListView is null.
      if (rebuild) {
      	Utils.logD("Rebuilding");
      	convertView.postInvalidate();
      	convertView.requestLayout();
      	//convertView.refreshDrawableState();
      	Utils.logD("_Path"  + _path);
      	getFiles();
      	if (position > fileH.length)
      		return convertView = mInflater.inflate(R.layout.adaptor_content, null);
      	//TODO
      }
      if (convertView == null || rebuild) {
      	rebuild = false;
        convertView = mInflater.inflate(R.layout.adaptor_content, null);
        // Creates a ViewHolder and store references to the two children
        // views
        // we want to bind data to.
        holder = new ViewHolder();
        holder.textLine = (TextView) convertView.findViewById(R.id.text);
        holder.iconLine = (ImageView) convertView.findViewById(R.id.icon);
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
            Utils.logD("pos " + pos);
          	if (fileH[pos].isDirectory()) {
            	_path = fileH[pos].getFilePath();  // TODO Skal dette være absolut path????
              Toast.makeText(context, "Open directory " + fileH[pos].getFileName(), Toast.LENGTH_SHORT).show();
              rebuild = true;
              //convertView.requestLayout();
              
          	} else {
          		Toast.makeText(context, "Open file " + fileH[pos].getFileName(), Toast.LENGTH_SHORT).show();
          	}
          }
        });
        convertView.setTag(holder);
      } else {
        // Get the ViewHolder back to get fast access to the TextView
        // and the ImageView.
        holder = (ViewHolder) convertView.getTag();
      }
      // Get flag name and id
      
      //int id = context.getResources().getIdentifier(filename, "drawable", "dk.andsen.sqlitemanager");
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