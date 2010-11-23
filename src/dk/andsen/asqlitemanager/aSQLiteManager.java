package dk.andsen.asqlitemanager;

import dk.andsen.utils.FilePicker;
import dk.andsen.utils.Utils;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author andsen
 *
 */
public class aSQLiteManager extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button open = (Button) this.findViewById(R.id.Open);
        open.setText(getString(R.string.OpdenDB));
        open.setOnClickListener(this);
        Button about = (Button) this.findViewById(R.id.About);
        about.setText(getString(R.string.About));
        about.setOnClickListener(this);
        Utils.logD("Created");
    }

		public void onClick(View v) {
			int key = v.getId();
			if (key == R.id.Open) {
				Intent i = new Intent(this, FilePicker.class);
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
			}
      Utils.logD("Filepicker called");
		}
}