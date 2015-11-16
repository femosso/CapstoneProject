package com.capstone.application.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.capstone.application.R;
import com.capstone.application.model.Alternative;
import com.capstone.application.model.Answer;
import com.capstone.application.model.CheckIn;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.model.Question;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.Constants.QuestionFormat;
import com.capstone.application.utils.RestUriConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.tech.freak.wizardpager.model.AbstractWizardModel;
import com.tech.freak.wizardpager.model.ModelCallbacks;
import com.tech.freak.wizardpager.model.MultipleFixedChoicePage;
import com.tech.freak.wizardpager.model.NumberPage;
import com.tech.freak.wizardpager.model.Page;
import com.tech.freak.wizardpager.model.PageList;
import com.tech.freak.wizardpager.model.TextPage;
import com.tech.freak.wizardpager.ui.PageFragmentCallbacks;
import com.tech.freak.wizardpager.ui.ReviewFragment;
import com.tech.freak.wizardpager.ui.StepPagerStrip;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CheckInWizardActivity extends FragmentActivity implements
        PageFragmentCallbacks, ReviewFragment.Callbacks, ModelCallbacks {

    private static final String TAG = RegisterActivity.class.getName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Context mContext;

    private ViewPager mPager;

    private MyPagerAdapter mPagerAdapter;

    private AbstractWizardModel mWizardModel;

    private boolean mEditingAfterReview;
    private boolean mConsumePageSelectedEvent;

    private long mPendingCheckInId;

    private Button mNextButton;
    private Button mPrevButton;

    private List<Page> mCurrentPageSequence;
    private List<Question> mQuestions;

    private StepPagerStrip mStepPagerStrip;

    private String mImageName;

    /**
     * Stores the list of {@link Page} keys to be retrieved when reading results
     */
    private List<String> mKeyList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_wizard);

        mContext = getApplicationContext();

        // dismiss the question notification
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.QUESTION_NOTIFICATION_ID);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mQuestions = extras.getParcelableArrayList("questions");
            mPendingCheckInId = extras.getLong("pendingCheckInId");
        }

        if (mQuestions != null && !mQuestions.isEmpty()) {
            initViews(savedInstanceState);
        } else {
            Toast.makeText(mContext, getString(R.string.check_in_wizard_not_initialized), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews(Bundle savedInstanceState) {
        mWizardModel = new QuestionsWizardModel(mContext);

        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle("model");
            if (bundle != null) {
                mWizardModel.load(bundle);
            }
        }

        mWizardModel.registerListener(this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(mPagerAdapter.getCount() - 1, position);
                if (mPager.getCurrentItem() != position) {
                    mPager.setCurrentItem(position);
                }
            }
        });

        mNextButton = (Button) findViewById(R.id.btnNext);
        mPrevButton = (Button) findViewById(R.id.btnPrev);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {
                    DialogFragment dialogFragment = new DialogFragment() {
                        @NonNull
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            return new AlertDialog.Builder(getActivity())
                                    .setMessage(getString(R.string.check_in_confirm_alert_title))
                                    .setNegativeButton(getString(R.string.check_in_confirm_alert_negative), onNegativeButton)
                                    .setPositiveButton(getString(R.string.check_in_confirm_alert_positive), onPositiveButton)
                                    .setNeutralButton(getString(R.string.check_in_confirm_alert_neutral), null).create();
                        }
                    };
                    dialogFragment.show(getSupportFragmentManager(), "place_order_dialog");
                } else {
                    if (mEditingAfterReview) {
                        mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                    } else {
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    }
                }
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });

        onPageTreeChanged();
        updateBottomBar();
    }

    private DialogInterface.OnClickListener onNegativeButton = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                String timeStamp = String.valueOf(System.currentTimeMillis());

                File imagesFolder = new File(Constants.SAVE_IMAGES_PATH);
                imagesFolder.mkdirs();

                mImageName = "Image_" + timeStamp + ".jpg";

                File image = new File(imagesFolder, mImageName);
                Uri saveImage = Uri.fromFile(image);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, saveImage);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            sendDataToServer();
        }
    }

    private DialogInterface.OnClickListener onPositiveButton = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            sendDataToServer();
        }
    };

    private void sendDataToServer() {
        int i = 0; boolean hasUnanswered = false;

        Answer answer; Question question; String text;
        List<Answer> answers = new ArrayList<>();

        Page page;

        // fulfill list of questions with respective answers to be sent to the server
        for (String key : mKeyList) {
            page = mWizardModel.findByKey(key);

            if (page instanceof MultipleFixedChoicePage) {
                ArrayList<String> values = page.getData().getStringArrayList(Page.SIMPLE_DATA_KEY);

                // convert array list to comma-separated string
                StringBuilder sb = new StringBuilder();
                if (values != null) {
                    for (String s : values) {
                        sb.append(s);
                        sb.append(",");
                    }
                }
                text = sb.toString();
            } else {
                text = page.getData().getString(Page.SIMPLE_DATA_KEY);
            }

            if (text == null || text.isEmpty()) {
                hasUnanswered = true;
                break;
            }

            // get only the id of the question to associate with the answer
            question = new Question();
            question.setId(mQuestions.get(i).getId());

            answer = new Answer();
            answer.setText(text);
            answer.setQuestion(question);

            answers.add(answer);
            i++;
        }

        // if teen has answered all the questions
        if (!hasUnanswered) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            User loggedUser = new User();
            loggedUser.setEmail(loggedEmail);

            CheckIn checkIn = new CheckIn();
            checkIn.setUser(loggedUser);
            checkIn.setAnswerList(answers);
            checkIn.setDate(System.currentTimeMillis());

            if (mImageName != null) {
                checkIn.setPhotoPath(Constants.SAVE_IMAGES_PATH + mImageName);
                mImageName = null;
            }

            new SendCheckInTask(CheckInWizardActivity.this).execute(checkIn);
        } else {
            Toast.makeText(mContext, getString(R.string.check_in_wizard_not_answered), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1);
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == mCurrentPageSequence.size()) {
            mNextButton.setText(getString(R.string.check_in_wizard_finish));
            mNextButton.setBackgroundResource(R.drawable.wizard_finish_background);
            mNextButton.setTextColor(Color.WHITE);
        } else {
            mNextButton.setText(mEditingAfterReview ? getString(R.string.check_in_wizard_review) : getString(R.string.check_in_wizard_next));
            mNextButton.setBackgroundResource(R.drawable.wizard_selectable_item_background);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
            mNextButton.setTextColor(Color.BLACK);
        }

        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mWizardModel != null) {
            mWizardModel.unregisterListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    @Override
    public void onPageDataChanged(Page page) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private Fragment mPrimaryItem;
        private int mCutOffPage;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }

            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
            return Math.min(mCutOffPage + 1, mCurrentPageSequence == null ? 1
                    : mCurrentPageSequence.size() + 1);
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }

    private class QuestionsWizardModel extends AbstractWizardModel {

        public QuestionsWizardModel(Context context) {
            super(context);
        }

        @Override
        protected PageList onNewRootPageList() {
            Page[] wizardPages = new Page[mQuestions.size()];

            mKeyList = new ArrayList<>();

            int k, i = 0;
            for (Question question : mQuestions) {
                if (question.getFormat().equals(QuestionFormat.FORMAT1.getValue())) {
                    String[] alternatives = new String[question.getAlternativeList().size()];
                    k = 0;
                    for (Alternative item : question.getAlternativeList()) {
                        alternatives[k] = item.getText();
                        k++;
                    }

                    MultipleFixedChoicePage choicePage = new MultipleFixedChoicePage(this, question.getText());
                    choicePage.setRequired(true);
                    choicePage.setChoices((alternatives));

                    wizardPages[i] = choicePage;
                } else if (question.getFormat().equals(QuestionFormat.FORMAT2.getValue())) {
                    wizardPages[i] = new NumberPage(this, question.getText()).setRequired(true);
                } else {
                    wizardPages[i] = new TextPage(this, question.getText()).setRequired(true);
                }

                mKeyList.add(wizardPages[i].getKey());
                i++;
            }

            return new PageList(wizardPages);
        }
    }

    private class SendCheckInTask extends AsyncTask<CheckIn, Void, JsonResponse> {
        private ProgressDialog dialog;

        public SendCheckInTask(CheckInWizardActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.progress_dialog_sending));
            dialog.show();
        }

        @Override
        protected JsonResponse doInBackground(CheckIn... params) {
            CheckIn checkInData = params[0];

            Log.d(TAG, checkInData.getUser().getEmail() + " is sending check in data to server");

            JsonResponse result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mContext) +
                        RestUriConstants.CHECK_IN_CONTROLLER + File.separator + RestUriConstants.SEND;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // all the data to be sent to server will be in this hash
                MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

                if (checkInData.getPhotoPath() != null) {
                    map.add(RestUriConstants.PARAM_PHOTO, new FileSystemResource(checkInData.getPhotoPath()));
                }

                // get the Check In data as Json String
                ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String checkInJson = objectWriter.writeValueAsString(checkInData);

                HttpHeaders header = new HttpHeaders();
                header.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> checkInParam = new HttpEntity<>(checkInJson, header);
                map.add(RestUriConstants.PARAM_CHECK_IN, checkInParam);

                // Make the HTTP POST request, marshaling the response to JsonResponse object
                result = restTemplate.postForObject(url, map, JsonResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (checkInData.getPhotoPath() != null) {
                File file = new File(checkInData.getPhotoPath());
                file.delete();
            }

            return result;
        }

        @Override
        protected void onPostExecute(JsonResponse result) {
            if (dialog.isShowing()) {
                dialog.dismiss();

                if (result != null && result.getStatus() == HttpStatus.OK) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("pendingCheckInId", mPendingCheckInId);

                    setResult(RESULT_OK, returnIntent);
                } else {
                    Toast.makeText(mContext, "Could not send check in data to server",
                            Toast.LENGTH_LONG).show();
                }

                finish();
            }
        }
    }
}