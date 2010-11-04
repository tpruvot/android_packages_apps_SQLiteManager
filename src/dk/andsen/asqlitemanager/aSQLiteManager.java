package dk.andsen.asqlitemanager;

import dk.andsen.utils.FilePicker;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class aSQLiteManager extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button open = (Button) this.findViewById(R.id.Open);
        open.setOnClickListener(this);
    }

		public void onClick(View v) {
			int key = v.getId();
			if (key == R.id.Open) {
				Intent i = new Intent(this, FilePicker.class);
				startActivity(i);
			}
		}
    
    
}