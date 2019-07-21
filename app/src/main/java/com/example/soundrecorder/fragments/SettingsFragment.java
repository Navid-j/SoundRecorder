package com.example.soundrecorder.fragments;


import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.example.soundrecorder.BuildConfig;
import com.example.soundrecorder.MySharedPreferences;
import com.example.soundrecorder.R;
import com.example.soundrecorder.activities.MainActivity;
import com.example.soundrecorder.activities.SettingsActivity;

import java.util.Locale;


public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        CheckBoxPreference highQualityPref = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_high_quality_key));
        highQualityPref.setChecked(MySharedPreferences.getPrefHighQuality(getActivity()));
        highQualityPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MySharedPreferences.setPrefHighQuality(getActivity(), (boolean) newValue);
                return true;
            }
        });

        Preference aboutPref = findPreference(getString(R.string.pref_about_key));
        aboutPref.setSummary(getString(R.string.pref_about_desc, BuildConfig.VERSION_NAME));
        aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AboutFragment aboutFragment = new AboutFragment();
                aboutFragment.show(((SettingsActivity)getActivity()).getSupportFragmentManager().beginTransaction(), "dialog_licenses");
                return true;
            }
        });

         final Preference lang = findPreference(getString(R.string.lang_key));
            lang.setSummary(getString(R.string.language_config));
            lang.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {


                    if ("fa".equals(Locale.getDefault().getLanguage())){

                        Resources resources = getResources();
                        Configuration configuration = resources.getConfiguration();
                        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
                        configuration.setLocale(Locale.forLanguageTag("en"));
                        resources.updateConfiguration(configuration, displayMetrics);
                        Locale.setDefault(new Locale("en"));
                        getActivity().startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));

                    }else {

                        Resources resources = getResources();
                        Configuration configuration = resources.getConfiguration();
                        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
                        configuration.setLocale(Locale.forLanguageTag("fa"));
                        resources.updateConfiguration(configuration, displayMetrics);
                        Locale.setDefault(new Locale("fa"));
                        getActivity().startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));

                    }

                    getActivity().finish();
                    return true;

                }
            });
    }
}
