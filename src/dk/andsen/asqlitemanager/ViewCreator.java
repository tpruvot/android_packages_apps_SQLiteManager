package dk.andsen.asqlitemanager;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class ViewCreator {
  private Context _context;
  
	ViewCreator(Context context) {
  	_context = context;
  }
	public ViewGroup tableView() {
		LinearLayout panel = new LinearLayout(_context);
		panel.setLayoutParams(
				new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, 
						LayoutParams.FILL_PARENT));
		panel.setOrientation(LinearLayout.VERTICAL);	
		
		return panel;
	}
}
