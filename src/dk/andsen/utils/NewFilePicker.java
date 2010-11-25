package dk.andsen.utils;

import dk.andsen.asqlitemanager.R;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewFilePicker extends ListActivity {
  private EfficientAdapter adap;
  private static String[] data = new String[] { "0", "1", "2", "3", "4" };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.newfilepicker);
    adap = new EfficientAdapter(this);
    setListAdapter(adap);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Toast.makeText(this, "Click-" + String.valueOf(position), Toast.LENGTH_SHORT).show();
  }

  public static class EfficientAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private Bitmap mIcon1;
    private Context context;
    public EfficientAdapter(Context context) {
      // Cache the LayoutInflate to avoid asking for a new one each time.
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
        holder.textLine = (TextView) convertView.findViewById(R.id.textLine);
        holder.iconLine = (ImageView) convertView.findViewById(R.id.iconLine);
        holder.buttonLine = (Button) convertView.findViewById(R.id.buttonLine);
        convertView.setOnClickListener(new OnClickListener() {
          private int pos = position;
          public void onClick(View v) {
            Toast.makeText(context, "Click-" + String.valueOf(pos), Toast.LENGTH_SHORT).show();    
          }
        });
        holder.buttonLine.setOnClickListener(new OnClickListener() {
          private int pos = position;
          public void onClick(View v) {
            Toast.makeText(context, "Delete-" + String.valueOf(pos), Toast.LENGTH_SHORT).show();
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
      int id = context.getResources().getIdentifier(filename, "drawable", "dk.andsen.sqlitemanager");
         Utils.logD("Flag " + filename);
      // Icons bound to the rows.
      if (id != 0x0) {
        mIcon1 = BitmapFactory.decodeResource(context.getResources(), id);
      }
      // Bind the data efficiently with the holder.
      holder.iconLine.setImageBitmap(mIcon1);
      holder.textLine.setText("flag " + String.valueOf(position));
      return convertView;
    }
    static class ViewHolder {
      TextView textLine;
      ImageView iconLine;
      Button buttonLine;
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
      return data.length;
    }
    public Object getItem(int position) {
      // TODO Auto-generated method stub
      return data[position];
    }
  }
} 