package dk.andsen.asqlitemanager;

import dk.andsen.utils.FilePicker;
import dk.andsen.utils.NewFilePicker;
import dk.andsen.utils.Utils;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * @author andsen
 *
 */
public class aSQLiteManager extends Activity implements OnClickListener {
	
	/**
	 * True to enable functions under test
	 */
	private final boolean testing = false;
	private static final int MENU_OPT = 1;
	private static final int MENU_HLP = 2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button open = (Button) this.findViewById(R.id.Open);
        open.setOnClickListener(this);
        Button about = (Button) this.findViewById(R.id.About);
        about.setOnClickListener(this);
        Button test = (Button) this.findViewById(R.id.Test);
        TextView tv = (TextView) this.findViewById(R.id.Version);
        tv.setText(getText(R.string.Version) + " " + getText(R.string.VersionNo));
        if (!testing) {
        	test.setVisibility(4);
        } else {
        	test.setOnClickListener(this);
        }
        Utils.logD("Created");
    }

		/* (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		public void onClick(View v) {
			int key = v.getId();
			if (key == R.id.Open) {
				Intent i = new Intent(this, FilePicker.class);
				//startActivityForResult(intent, requestCode)
				// onActivityResult() 
				startActivity(i);
			} else if (key == R.id.About) {
				Context mContext = this;
				Dialog dial = new Dialog(mContext);
				dial.setContentView(R.layout.about);
				dial.setTitle(getString(R.string.AboutHeader));
				TextView text = (TextView) dial.findViewById(R.id.text);
				text.setText(getString(R.string.AboutText));
				ImageView image = (ImageView) dial.findViewById(R.id.image);
				image.setImageResource(R.drawable.and);
				TextView mail = (TextView) dial.findViewById(R.id.mail);
				mail.setAutoLinkMask(Linkify.ALL);
				mail.setText(getString(R.string.MAIL));
				TextView www = (TextView) dial.findViewById(R.id.www);
				www.setAutoLinkMask(Linkify.ALL);
				www.setText(getString(R.string.WWW));
				dial.show();
			} else if (key == R.id.Test) {
				Intent i = new Intent(this, NewFilePicker.class);
				startActivity(i);
			}
      Utils.logD("Filepicker called");
		}
		
		/*
		 *  Creates the menu items
		 */
		public boolean onCreateOptionsMenu(Menu menu) {
			menu.add(0, MENU_OPT, 0, R.string.Option).setIcon(R.drawable.ic_menu_preferences);
			menu.add(0, MENU_HLP, 0, R.string.Help).setIcon(R.drawable.ic_menu_help);
			return true;
		}
		
		/* (non-Javadoc) Handles item selections
		 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
		 */
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
	    case MENU_OPT:
	      startActivity(new Intent(this, Prefs.class));
	      return true;
	    case MENU_HLP:
				Intent i = new Intent(this, Help.class);
				startActivity(i);
	      return true;
			}
			return false;
		}

}