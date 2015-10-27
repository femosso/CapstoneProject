package com.capstone.application.fragment;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragmentCompat;

import com.capstone.application.R;

public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.preferences);

        //fetch the item where you wish to insert the CheckBoxPreference, in this case a PreferenceCategory with key "targetCategory"
        /*MultiSelectListPreference multiSelectListPreference =
                (MultiSelectListPreference) findPreference("sharedData");

        String[] entries = Constants.QuestionType.names();
        if (entries.length > 0) {
            CharSequence[] entryValues = new CharSequence[entries.length];
            for (int i = 0; i < entryValues.length; i++) {
                entryValues[i] = String.valueOf(i);
            }

            multiSelectListPreference.setEntries(entries);
            multiSelectListPreference.setEntryValues(entryValues);
            multiSelectListPreference.setDefaultValue(entryValues);

            PreferenceManager.setDefaultValues(getActivity(), "sharedData", Context.MODE_PRIVATE, R.xml.preferences, true);
        }*/
    }
}
