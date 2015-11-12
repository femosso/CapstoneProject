package com.capstone.application.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.application.R;
import com.capstone.application.model.Answer;
import com.capstone.application.model.CheckIn;
import com.capstone.application.model.Feedback;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.RestUriConstants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;

public class CheckInDetailsActivity extends AppCompatActivity {
    private static final String TAG = CheckInDetailsActivity.class.getName();

    private Context mContext;

    private CheckIn mCheckIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_details);

        mContext = getApplicationContext();

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.activity_name_check_in_details));
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // get the check in information that will be displayed
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mCheckIn = extras.getParcelable("checkIn");
        }

        if (mCheckIn != null) {
            initCheckInInformation(mCheckIn);
            initTeenInformation(mCheckIn.getUser());

            LinearLayout feedbackLayout = (LinearLayout) findViewById(R.id.feedbackLayout);

            int questionCount = 1;
            for (final Answer answer : mCheckIn.getAnswerList()) {
                String questionText = answer.getQuestion().getText();
                String answerText = answer.getText();

                // add the question text
                TextView questionTextView = new TextView(this);
                questionTextView.setText(questionCount + ". " + questionText);
                questionTextView.setTypeface(Typeface.DEFAULT_BOLD);

                feedbackLayout.addView(questionTextView);

                questionCount++;

                // add the answer text for this question
                TextView answerTextView = new TextView(this);
                answerTextView.setText(answerText);

                feedbackLayout.addView(answerTextView);

                // if the question is from this TYPE1, provide the possibility
                // to view the history of it in a line chart
                if (Constants.QuestionType.fromString(answer.getQuestion().getType())
                        .equals(Constants.QuestionType.TYPE1)) {
                    Button button = new Button(this);
                    button.setText("see history");

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String type = answer.getQuestion().getType();
                            String email = mCheckIn.getUser().getEmail();

                            new RetrieveHistoricTask(CheckInDetailsActivity.this, type).execute(email);
                        }
                    });

                    feedbackLayout.addView(button);
                } else if (Constants.QuestionType.fromString(answer.getQuestion().getType())
                        .equals(Constants.QuestionType.TYPE2)) {
                    Button button = new Button(this);
                    button.setText("see history 1");

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String type = answer.getQuestion().getType();
                            String email = mCheckIn.getUser().getEmail();

                            new RetrieveHistoricTask(CheckInDetailsActivity.this, type).execute(email);
                        }
                    });

                    feedbackLayout.addView(button);
                }

/*                // add line separator
                View separator = new View(this);
                LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);

                separator.setLayoutParams(layoutParams);
                separator.setBackgroundColor(Color.BLACK);

                feedbackLayout.addView(separator);*/
            }
        }
    }

    private void initCheckInInformation(final CheckIn checkIn) {
        TextView checkInDate = (TextView) findViewById(R.id.txtCheckInDate);
        checkInDate.setText(getString(R.string.check_in_date) +
                new SimpleDateFormat(Constants.DATE_FORMAT).format(checkIn.getDate()));

        TextView checkInTime = (TextView) findViewById(R.id.txtCheckInTime);
        checkInTime.setText(getString(R.string.check_in_time) +
                new SimpleDateFormat(Constants.TIME_FORMAT).format(checkIn.getDate()));

        Button checkInPhoto = (Button) findViewById(R.id.btnCheckInPhoto);
        checkInPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CheckInPhotoActivity.class);
                intent.putExtra("checkInId", checkIn.getId());

                startActivity(intent);
            }
        });
    }

    private void initTeenInformation(User user) {
        TextView teenName = (TextView) findViewById(R.id.txtTeenName);
        teenName.setText(getString(R.string.teen_name) +
                user.getFirstName() + " " + user.getLastName());

        TextView teenBirthday = (TextView) findViewById(R.id.txtTeenBirthday);
        teenBirthday.setText(getString(R.string.teen_birthday) +
                user.getTeen().getBirthday());

        TextView teenMedicalNumber = (TextView) findViewById(R.id.txtTeenMedicalNumber);
        teenMedicalNumber.setText(getString(R.string.teen_medical_number) +
                user.getTeen().getMedicalNumber());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class RetrieveHistoricTask extends AsyncTask<String, Void, FeedbackResponse> {
        private ProgressDialog dialog;
        private String mType;

        public RetrieveHistoricTask(Activity activity, String type) {
            dialog = new ProgressDialog(activity);
            mType = type;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.progress_dialog_loading));
            dialog.show();
        }

        @Override
        protected FeedbackResponse doInBackground(String... params) {
            Log.d(TAG, "Contacting server to retrieve historic");

            String teenEmail = params[0];

            Feedback feedback = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mContext) +
                        RestUriConstants.ANSWER_CONTROLLER + File.separator +
                        RestUriConstants.HISTORIC + "?" + RestUriConstants.PARAM_EMAIL + "=" +
                        teenEmail + "&" + RestUriConstants.PARAM_TYPE + "=" + mType;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to Feedback object
                feedback = restTemplate.getForObject(url, Feedback.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new FeedbackResponse(feedback, mType);
        }

        @Override
        protected void onPostExecute(FeedbackResponse result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (result.feedback != null && result.feedback.getAnswerList() != null) {
                Class graphActivity = null;
                if (Constants.QuestionType.fromString(mType).equals(Constants.QuestionType.TYPE1)) {
                    graphActivity = LineChartActivity.class;
                } else if (Constants.QuestionType.fromString(mType).equals(Constants.QuestionType.TYPE2)) {
                    graphActivity = PieChartActivity.class;
                }

                if (graphActivity != null) {
                    Intent intent = new Intent(mContext, graphActivity);
                    intent.putExtra("feedback", result.feedback);
                    intent.putExtra("type", result.type);

                    startActivity(intent);
                }
            } else {
                Toast.makeText(mContext, getString(R.string.check_in_history_not_loaded),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class FeedbackResponse {
        Feedback feedback;
        String type;

        public FeedbackResponse(Feedback feedback, String type) {
            this.feedback = feedback;
            this.type = type;
        }
    }
}