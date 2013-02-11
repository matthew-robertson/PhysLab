package gui.activities;

import global.StaticVariables;
import utils.file.AutosaveThread;
import utils.preferences.EnumDialogOptions;
import utils.preferences.NoticeDialogFragment;
import utils.preferences.SettingsFragment;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class PreferencesActivity extends PreferenceActivity implements
		NoticeDialogFragment.NoticeDialogListener {
	private SettingsFragment settingsFragment;
	private String activity;
	private Menu menu;

	public PreferencesActivity() {
		this.activity = "main_activity";
	}

	public PreferencesActivity(String activity) {
		this.activity = activity;
	}

	public void setActivity(String str) {
		this.activity = str;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.

		settingsFragment = new SettingsFragment(this);

		Bundle bundle = this.getIntent().getExtras();
		this.activity = bundle.getString(VideoActivity.MAIN_ACTIVITY);

		// THIS is the error

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, settingsFragment).commit();

		setPreferences();

	}
	EditTextPreference mEditTextPreference;
	ListPreference mListPreference;
    
	public void setPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor geted = prefs.edit();
		geted.putBoolean("preference_default_save_option", StaticVariables.mainProject.preferenceSaveInternally);
		geted.putBoolean("preference_auto_save_enabled", StaticVariables.mainProject.autosaveEnabled);
		geted.commit();

		if(StaticVariables.mainProject.autosaveEnabled && StaticVariables.autosaveThread == null)
		{
			StaticVariables.autosaveThread = new AutosaveThread(this);
			StaticVariables.autosaveThread.setDaemon(true);
			StaticVariables.autosaveThread.start();
		}
		
		/*
		Preference pref = settingsFragment.findPreference("preference_project_name");
		
	    if (pref instanceof EditTextPreference) {
	        EditTextPreference listPref = (EditTextPreference) pref;
	        ((EditTextPreference) pref).setText(StaticVariables.mainProject.projectName);
	        //pref.setSummary(listPref.getEntry());
	    }
	    else
	    {
	    	mEditTextPreference = (EditTextPreference)getPreferenceScreen().findPreference("preference_project_name");
	        mEditTextPreference.setText(StaticVariables.mainProject.projectName);
	    }
	    
	    Preference pref1 = settingsFragment.findPreference("preferences_measure1");

	    if (pref instanceof ListPreference) {
	        ListPreference listPref = (ListPreference) pref1;
	        ((ListPreference) pref1).setValueIndex(StaticVariables.mainProject.preferenceDistanceMeasure);
	    }
	    else{
	    mListPreference = (ListPreference)getPreferenceScreen().findPreference("preferences_measure1");
        mListPreference.setValueIndex(StaticVariables.mainProject.preferenceDistanceMeasure);
	    }
	    */
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_bar_close_preferences:
			swapBack();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public int parseInt(String str)
	{
		try
		{
			int i = Integer.parseInt(str);
			return i;
		}
		catch(Exception e)
		{
			return 1;
		}
	}
	
	public void swapBack()
    {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this); 
		String str = sp.getString("preferences_measure1","-1");
		boolean internal = sp.getBoolean("preference_default_save_option", false);		
		String name = sp.getString("preference_project_name", "0");
		int frameSkip = parseInt(sp.getString("preference_frame_skip", "0"));
		boolean save = sp.getBoolean("preference_auto_save_enabled", false);
		
		StaticVariables.mainProject.autosaveEnabled = save;
		StaticVariables.mainProject.preferenceDistanceMeasure = Integer.parseInt(str);
		StaticVariables.mainProject.preferenceSaveInternally = internal;
		StaticVariables.mainProject.projectName = name;
		StaticVariables.mainProject.frameSkip = frameSkip;
		
		if(StaticVariables.mainProject.autosaveEnabled && StaticVariables.autosaveThread == null)
		{
			StaticVariables.autosaveThread = new AutosaveThread(this);
			StaticVariables.autosaveThread.setDaemon(true);
			StaticVariables.autosaveThread.start();
		}
		
    	if(activity.equals(VideoActivity.NAME_ACTIVITY_MAIN))
    	{
    		Intent intent = new Intent(this, VideoActivity.class);
            startActivity(intent);
        }
    	else if(activity.equals(GraphingActivity.NAME_ACTIVITY_GRAPHING))
    	{
    		Intent intent = new Intent(this, GraphingActivity.class);
            startActivity(intent);
        }
    	else if(activity.equals(VideoSelect.NAME_ACTIVITY_VIDEO_SELECT))
    	{
    		Intent intent = new Intent(this, VideoSelect.class);
            startActivity(intent);
        }
    }

	public void clearAllData() {
		DialogFragment newFragment = new NoticeDialogFragment();
		newFragment.show(getFragmentManager(), "missiles");
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_preferences, menu);
		this.menu = menu;
		return true;
	}

	public void onDialogPositiveClick(DialogFragment dialog, String message) {
		if (message
				.equals(EnumDialogOptions.DIALOG_DELETE_POINTS_CONFIRM.value)) {
			try {
				// Clear all data
				StaticVariables.mainProject.clearAllPoints(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onDialogNegativeClick(DialogFragment dialog, String message) {

		// Do nothing
	}

}
