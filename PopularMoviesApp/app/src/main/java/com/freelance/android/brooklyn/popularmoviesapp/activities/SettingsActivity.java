package com.freelance.android.brooklyn.popularmoviesapp.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.freelance.android.brooklyn.popularmoviesapp.R;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
