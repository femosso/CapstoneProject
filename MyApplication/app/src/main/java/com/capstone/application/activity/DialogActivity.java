package com.capstone.application.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.capstone.application.R;
import com.capstone.application.model.Question;

public class DialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        Bundle extras = getIntent().getExtras();
        Question question = null;
        if (extras != null) {
            question = extras.getParcelable("question");
        }

        if (question != null) {
            Log.d("Felipe", "question = " + question.getText());

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
        }

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