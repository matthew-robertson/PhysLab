package utils.preferences;

import gui.activities.PreferencesActivity;
import gui.activities.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment 
{
	private PreferencesActivity preferences;
	
	public SettingsFragment()
	{
	}
	
	public SettingsFragment(final PreferencesActivity pref)
	{
		this.preferences = pref;
	}
	
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_menu);
        registerMouseListener("return_button", null, preferences);
    }

    public void registerMouseListener(String key, OnPreferenceClickListener listener, final PreferencesActivity pref)
    {
    	/*
    	Preference myPref = (Preference) findPreference("preference_close_preferences");
    	myPref.setOnPreferenceClickListener(    	
    	new OnPreferenceClickListener() 
		{
            public boolean onPreferenceClick(Preference preference) 
            {
            	pref.swapBack();
				return false;
            }
    	});
    	*/
    	
    	Preference clearDataPref = (Preference) findPreference("preference_clear_all_data");
    	clearDataPref.setOnPreferenceClickListener(    	
    	new OnPreferenceClickListener() 
		{
            public boolean onPreferenceClick(Preference preference) 
            {
            	pref.clearAllData();
				return false;
            }
    	});
    } 
    
    
  
	
}
