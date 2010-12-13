/*
 * Part of android41cv (https://sourceforge.net/projects/android41cv/)
 * a HP41CV simulator by andsen (http://sourceforge.net/users/andsen)
 *
 * This class contains the preference functionalities.
 * 
 */
package dk.andsen.asqlitemanager;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {
   // Option names and default values
   private static final String OPT_PAGESIZE = "PageSize";
   private static final String OPT_PAGESIZE_DEF = "20";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
   }

   public static int getPageSize(Context context) {
     return new Integer( PreferenceManager.getDefaultSharedPreferences(context)
           .getString(OPT_PAGESIZE, OPT_PAGESIZE_DEF)).intValue();
  }

}
