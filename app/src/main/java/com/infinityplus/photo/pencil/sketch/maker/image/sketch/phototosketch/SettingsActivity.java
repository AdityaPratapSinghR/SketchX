package com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageView backSettings = findViewById(R.id.back_settings);
        backSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        SharedPreferences sharedPreferences
                = getSharedPreferences(
                Constant.APP_NAME, MODE_PRIVATE);
        final SharedPreferences.Editor editorMode
                = sharedPreferences.edit();
        final boolean isDarkModeOn
                = sharedPreferences
                .getBoolean(
                        "isDarkModeOn", false);
        SwitchCompat dayNightSwitch = findViewById(R.id.day_night_switch);
        dayNightSwitch.setChecked(isDarkModeOn);
        dayNightSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (dayNightSwitch.isChecked()){
                AppCompatDelegate
                        .setDefaultNightMode(
                                AppCompatDelegate
                                        .MODE_NIGHT_YES);

                // it will set isDarkModeOn
                // boolean to true
                editorMode.putBoolean(
                        "isDarkModeOn", true);
                editorMode.apply();
            }else{
                // if dark mode is on it
                // will turn it off
                AppCompatDelegate
                        .setDefaultNightMode(
                                AppCompatDelegate
                                        .MODE_NIGHT_NO);
                // it will set isDarkModeOn
                // boolean to false
                editorMode.putBoolean(
                        "isDarkModeOn", false);
                editorMode.apply();

            }
        });

        //Privacy Policy
        String url ="https://sites.google.com/view/sketchx/";
       ConstraintLayout privacyPolicy = findViewById(R.id.privacy_policy);
        privacyPolicy.setOnClickListener(view -> {
            // initializing object for custom chrome tabs.
            CustomTabsIntent.Builder customIntent = new CustomTabsIntent.Builder();

            // below line is setting toolbar color
            // for our custom chrome tab.
            customIntent.setToolbarColor(ContextCompat.getColor(view.getContext(), R.color.blue));

            // we are calling below method after
            // setting our toolbar color.
            openCustomTab(SettingsActivity.this, customIntent.build(), Uri.parse(url), url);
        });

//        ConstraintLayout  help = findViewById(R.id.help);
//        help.setOnClickListener(view -> {
//            Intent intent = new Intent(SettingsActivity.this, HelpActivity.class);
//            startActivity(intent);
//        });
        ConstraintLayout  sendFeedback = findViewById(R.id.send_feedback);
        sendFeedback.setOnClickListener(view -> {
            String email = "help.infinityplus@gmail.com";
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name)+" Feedback");
            intent.putExtra(Intent.EXTRA_TEXT, "SketchX: \n");
            try {
                startActivity(intent);
            }catch (ActivityNotFoundException e){
                Toast.makeText(this, getString(R.string.no_app_found), Toast.LENGTH_SHORT).show();
            }

        });
        ConstraintLayout  rateUs = findViewById(R.id.rate_us);
        rateUs.setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } catch (ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });
        ConstraintLayout   ourApps = findViewById(R.id.our_apps);
        ourApps.setOnClickListener(view -> {
            String url1 = "https://play.google.com/store/apps/dev?id=4845091893080747602";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url1));
            startActivity(i);
        });

    }

    public void openCustomTab(Activity activity, CustomTabsIntent customTabsIntent, Uri uri, String url) {
        // package name is the default package
        // for our custom chrome tab
        String packageName = "com.android.chrome";

        // we are checking if the package name is not null
        // if package name is not null then we are calling
        // that custom chrome tab with intent by passing its
        // package name.
        customTabsIntent.intent.setPackage(packageName);

        // in that custom tab intent we are passing
        // our url which we have to browse.
        try {
            customTabsIntent.launchUrl(activity, uri);
        }catch (ActivityNotFoundException activityNotFoundException){
            Log.d("ACTIVITY","NOT FOUND");
            String url1 = url;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url1));
            startActivity(i);
        }
    }
}
