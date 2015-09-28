package com.capstone.application.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.capstone.application.R;

public class DialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        Bundle extras = getIntent().getExtras();
        int value = -1;
        if (extras != null) {
            value = extras.getInt("type");
        }

        Log.d("Felipe", "value = " + value);

        /*if (value == 1) {
            new MaterialDialog.Builder(this)
                    .title("title")
                    .items(R.array.nav_drawer_labels)
                    .theme(Theme.DARK)
                    .positiveText("agree")
                    .negativeText("disagree")
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .show();
        } else {
            new MaterialDialog.Builder(this)
                    .title("title")
                    .items(R.array.nav_drawer_labels)
                    .theme(Theme.LIGHT)
                    .positiveText("agree")
                    .negativeText("disagree")
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .show();
        }*/

    }

}