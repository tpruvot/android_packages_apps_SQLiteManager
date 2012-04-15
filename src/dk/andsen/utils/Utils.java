/**
 * Part of one of Andsen's open source project (a41cv / aSQLiteManager) 
 *
 * @author andsen
 *
 */
package dk.andsen.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Utils {
	// public static boolean logging = true;

	private static String app = "aSQLMan";

	/**
	 * Write a debug message to the log
	 * 
	 * @param msg
	 *          Message
	 */
	public static void logD(String msg, boolean logging) {
		if (logging)
			Log.d(app, msg);
	}

	/**
	 * Write an error message to the log
	 * 
	 * @param msg
	 *          Message
	 */
	public static void logE(String msg, boolean logging) {
		if (logging)
			Log.e(app, msg);
	}

	/**
	 * Show a dialog with an error message
	 * 
	 * @param e
	 *          the message
	 * @param cont
	 *          the programmes content
	 */
	public static void showException(String e, Context cont) {
		AlertDialog alertDialog = new AlertDialog.Builder(cont).create();
		alertDialog.setTitle("Error");
		alertDialog.setMessage(e);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alertDialog.show();
	}

	public static void showMessage(String title, String msg, Context cont) {
		showMessage(title, msg, null, "OK", cont);
	}

	public static void showMessage(String title, String msg, String btnText,
			Context cont) {

		showMessage(title, msg, null, btnText, cont);
	}

	public static void showMessage(String title, String msg, Integer icon,
			String btnText, Context cont) {
		AlertDialog alertDialog = new AlertDialog.Builder(cont).create();
		if (icon != null) {
			alertDialog.setIcon(icon);
		}
		alertDialog.setTitle(title);
		if (msg != null) {
			alertDialog.setMessage(msg);
		}
		alertDialog.setButton(btnText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alertDialog.show();
	}

	public static void showModalDialog(String title, String msg, String posText,
			String negText, Context cont) {
		showModalDialog(title, msg, null, posText, null, negText, null, cont);
	}

	public static void showModalDialog(String title, String msg, String posText,
			DialogInterface.OnClickListener posAction, String negText, Context cont) {
		showModalDialog(title, msg, null, posText, posAction, negText, null, cont);
	}

	public static void showModalDialog(String title, String msg, String posText,
			String negText, DialogInterface.OnClickListener negAction, Context cont) {
		showModalDialog(title, msg, null, posText, null, negText, negAction, cont);
	}

	public static void showModalDialog(String title, String msg, Integer icon,
			String posText, String negText, Context cont) {
		showModalDialog(title, msg, icon, posText, null, negText, null, cont);
	}

	public static void showModalDialog(String title, String msg, Integer icon,
			String posText, DialogInterface.OnClickListener posAction,
			String negText, Context cont) {
		showModalDialog(title, msg, icon, posText, posAction, negText, null, cont);
	}

	public static void showModalDialog(String title, String msg, Integer icon,
			String posText, String negText,
			DialogInterface.OnClickListener negAction, Context cont) {
		showModalDialog(title, msg, icon, posText, null, negText, negAction, cont);
	}

	public static void showModalDialog(String title, String msg, Integer icon,
			String posText, DialogInterface.OnClickListener posAction,
			String negText, DialogInterface.OnClickListener negAction, Context cont) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);
		if (icon != null) {
			dialog.setIcon(icon);
		}
		dialog.setTitle(title);
		dialog.setMessage(msg);
		dialog.setPositiveButton(posText, posAction);
		dialog.setNegativeButton(negText, negAction);
		dialog.show();
	}

	/**
	 * Display the message as a short toast message
	 * 
	 * @param context
	 * @param msg
	 *          the message to display
	 */
	public static void toastMsg(Context context, String msg) {
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**
	 * Test if a SDCard is available
	 * 
	 * @return true if a external SDCard is available
	 */
	public static boolean isSDAvailable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static void printStackTrace(Exception e, boolean logging) {
		if (logging)
			e.printStackTrace();
	}
	
	public static String addSlashIfNotEnding(String str) {
		if (str != null) {
		if (!str.substring(str.length() - 1).equalsIgnoreCase("/")) {
			str += "/";
		}
		}
		return str;
	}

}
