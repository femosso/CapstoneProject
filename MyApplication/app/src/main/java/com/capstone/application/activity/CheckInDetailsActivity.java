package com.capstone.application.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.capstone.application.R;
import com.capstone.application.model.Answer;
import com.capstone.application.model.CheckIn;
import com.capstone.application.model.Feedback;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;

public class CheckInDetailsActivity extends AppCompatActivity {
    private static final String TAG = CheckInDetailsActivity.class.getName();

    private CheckIn mCheckIn;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_details);

        mContext = getApplicationContext();

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check-in Details");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

                if (Constants.QuestionType.fromString(answer.getQuestion().getType())
                        .equals(Constants.QuestionType.TYPE1)) {
                    Button button = new Button(this);
                    button.setText("see history");

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] params = new String[2];
                            params[0] = mCheckIn.getUser().getEmail();
                            params[1] = answer.getQuestion().getType();

                            new RetrieveHistoricTask(CheckInDetailsActivity.this).execute(params);
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
        TextView checkInDate = (TextView) findViewById(R.id.checkInDate);
        checkInDate.setText("Date: " + new SimpleDateFormat(Constants.DATE_FORMAT).format(checkIn.getDate()));

        TextView checkInTime = (TextView) findViewById(R.id.checkInTime);
        checkInTime.setText("Time: " + new SimpleDateFormat(Constants.TIME_FORMAT).format(checkIn.getDate()));

        Button checkInPhoto = (Button) findViewById(R.id.checkInPhoto);
        checkInPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RetrievePhotoTask(CheckInDetailsActivity.this).execute(checkIn.getId());
            }
        });
    }

    private void initTeenInformation(User user) {
        TextView teenName = (TextView) findViewById(R.id.teenName);
        teenName.setText("Name: " + user.getFirstName() + " " + user.getLastName());

        TextView teenBirthday = (TextView) findViewById(R.id.teenBirthday);
        teenBirthday.setText("Birthday: " + user.getTeen().getBirthday());

        TextView teenMedicalNumber = (TextView) findViewById(R.id.teenMedicalNumber);
        teenMedicalNumber.setText("Medical Number: " + user.getTeen().getMedicalNumber());
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

        public RetrieveHistoricTask(Activity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Doing something, please wait.");
            dialog.show();
        }

        @Override
        protected FeedbackResponse doInBackground(String... params) {
            Log.d(TAG, "Contacting server to retrieve historic");

            String teenEmail = params[0];
            String type = params[1];

            Feedback feedback = null;
            try {
                // The URL for making the GET request
                final String url = Constants.SERVER_URL + "answer/historic?email=" + teenEmail
                        + "&type=" + type;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to CheckIn object
                feedback = restTemplate.getForObject(url, Feedback.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new FeedbackResponse(feedback, type);
        }

        @Override
        protected void onPostExecute(FeedbackResponse result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (result.feedback != null && result.feedback.getAnswerList() != null) {
                Intent intent = new Intent(mContext, LineChartActivity.class);
                intent.putExtra("feedback", result.feedback);
                intent.putExtra("type", result.type);

                startActivity(intent);
            } else {
                // TODO
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

    private class RetrievePhotoTask extends AsyncTask<Long, Void, FileSystemResource> {
        private ProgressDialog dialog;

        public RetrievePhotoTask(Activity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Doing something, please wait.");
            dialog.show();
        }

        @Override
        protected FileSystemResource doInBackground(Long... params) {
            Log.d(TAG, "Contacting server to retrieve check in photo");

            long checkInId = params[0];

            FileSystemResource result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.SERVER_URL + "checkIn/photo/" + checkInId;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to CheckIn object
                result = restTemplate.getForObject(url, FileSystemResource.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(FileSystemResource result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}