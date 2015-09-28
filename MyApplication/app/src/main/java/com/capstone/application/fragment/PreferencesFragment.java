package com.capstone.application.fragment;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragmentCompat;

import com.capstone.application.R;

public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(final Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.preferences);
    }
}
