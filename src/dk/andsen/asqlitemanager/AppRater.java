package dk.andsen.asqlitemanager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppRater {
	private static String APP_TITLE;
	private static String APP_PNAME = "dk.andsen.asqlitemanager";

	private final static int DAYS_UNTIL_PROMPT = 7;
	private final static int LAUNCHES_UNTIL_PROMPT = 10;

	public static void app_launched(Context mContext) {
		APP_TITLE = (String) mContext.getText(R.string.hello);
		SharedPreferences prefs = mContext.getSharedPreferences("aSQLiteManager", 0);
		if (prefs.getBoolean("dontshowagain", false)) {
			return;
		}
		SharedPreferences.Editor editor = prefs.edit();
		// Increment launch counter
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		editor.putLong("launch_count", launch_count);
		// Get date of first launch
		Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong("date_firstlaunch", date_firstLaunch);
		}
		// Wait at least LAUNCHES_UNTIL_PROMPT launches before prompting
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			// Wait at least DAYS_UNTIL_PROMPT days before opening
			if (System.currentTimeMillis() >= date_firstLaunch
					+ (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				showRateDialog(mContext, editor);
			}
		}
		editor.commit();
	}

	public static void showRateDialog(final Context mContext,
			final SharedPreferences.Editor editor) {
		final Dialog dialog = new Dialog(mContext);
		dialog.setTitle("Rate " + APP_TITLE);
		LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		TextView tv = new TextView(mContext);
		tv.setText(mContext.getText(R.string.RateMsg).toString());
		tv.setWidth(240);
		tv.setGravity(Gravity.CENTER);
		tv.setPadding(4, 0, 4, 10);
		ll.addView(tv);
		LinearLayout llStars = new LinearLayout(mContext);
		llStars.setOrientation(LinearLayout.HORIZONTAL);
		llStars.setGravity(Gravity.CENTER_HORIZONTAL);
		llStars.setPadding(0, 0, 0, 5);
		ImageView iv1 = new ImageView(mContext);
		iv1.setImageResource(R.drawable.star);
		ImageView iv2 = new ImageView(mContext);
		iv2.setImageResource(R.drawable.star);
		ImageView iv3 = new ImageView(mContext);
		iv3.setImageResource(R.drawable.star);
		ImageView iv4 = new ImageView(mContext);
		iv4.setImageResource(R.drawable.star);
		ImageView iv5 = new ImageView(mContext);
		iv5.setImageResource(R.drawable.star);
		llStars.addView(iv1);
		llStars.addView(iv2);
		llStars.addView(iv3);
		llStars.addView(iv4);
		llStars.addView(iv5);
		ll.addView(llStars);
		Button b1 = new Button(mContext);
		b1.setText(mContext.getText(R.string.Rate).toString() + " " + APP_TITLE);
		b1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id=" + APP_PNAME)));
				dialog.dismiss();
			}
		});
		ll.addView(b1);
		Button b2 = new Button(mContext);
		b2.setText(mContext.getText(R.string.Later).toString());
		b2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		ll.addView(b2);
		Button b3 = new Button(mContext);
		b3.setText(mContext.getText(R.string.NoThanks).toString());
		b3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean("dontshowagain", true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		ll.addView(b3);
		dialog.setContentView(ll);
		dialog.show();
	}
}