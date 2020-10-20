package com.vva.androidopencbt;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import java.util.List;

public class SettingsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        SwitchPreferenceCompat[] prefs;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            prefs = new SwitchPreferenceCompat[7];
            prefs[0] = (SwitchPreferenceCompat)findPreference("enable_thoughts");
            prefs[1] = (SwitchPreferenceCompat)findPreference("enable_rational");
            prefs[2] = (SwitchPreferenceCompat)findPreference("enable_situation");
            prefs[3] = (SwitchPreferenceCompat)findPreference("enable_emotions");
            prefs[4] = (SwitchPreferenceCompat)findPreference("enable_intensity");
            prefs[5] = (SwitchPreferenceCompat)findPreference("enable_feelings");
            prefs[6] = (SwitchPreferenceCompat)findPreference("enable_actions");
            for(int i=0;i<6;i++)
                prefs[i].setOnPreferenceClickListener(lsnr);

        }

        Preference.OnPreferenceClickListener lsnr = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean flag = false;
                for(int i=0;i<6;i++)
                {
                    if(prefs[i].isChecked())
                    {
                        flag = true;
                        break;
                    }
                }
                if(!flag)
                {
                    Toast.makeText(getContext(), getString(R.string.pref_empty), Toast.LENGTH_SHORT).show();
                    ((SwitchPreferenceCompat)preference).setChecked(true);
                }
                //---
                return true;
            }
        };
    }
}