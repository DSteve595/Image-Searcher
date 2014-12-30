package com.stevenschoen.imagesearchernew;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        View backButton = findViewById(R.id.about_back);
        Utils.setupFab(backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(AboutActivity.this);
            }
        });

        View rateButton = findViewById(R.id.about_actions_rate_holder);
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            rateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent rateIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(rateIntent);
                }
            });
        } else {
            rateButton.setVisibility(View.GONE);
        }

        View githubButton = findViewById(R.id.about_actions_github_holder);
        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent githubIntent = new Intent(Intent.ACTION_VIEW);
                githubIntent.setData(Uri.parse("https://github.com/DSteve595/Image-Searcher"));
                startActivity(githubIntent);
            }
        });

        TextView versionText = (TextView) findViewById(R.id.about_version);
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(getString(R.string.version_x, version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
