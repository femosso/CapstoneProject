package com.capstone.application.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.capstone.application.R;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.RestUriConstants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;

public class CheckInPhotoActivity extends AppCompatActivity {
    private static final String TAG = CheckInPhotoActivity.class.getName();

    private Context mContext;

    private ImageView mImageView;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_photo);

        mContext = getApplicationContext();

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.activity_name_check_in_photo));
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mImageView = (ImageView) findViewById(R.id.imgCheckInPhoto);

        if (savedInstanceState != null) {
            mBitmap = savedInstanceState.getParcelable("bitmap");
            mImageView.setImageBitmap(mBitmap);
        } else {
            long checkInId = getIntent().getLongExtra("checkInId", -1);
            if (checkInId != -1) {
                new RetrievePhotoTask(CheckInPhotoActivity.this).execute(checkInId);
            }
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("bitmap", mBitmap);
    }

    private class RetrievePhotoTask extends AsyncTask<Long, Void, Bitmap> {
        private ProgressDialog dialog;

        public RetrievePhotoTask(Activity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.progress_dialog_loading));
            dialog.show();
        }

        @Override
        protected Bitmap doInBackground(Long... params) {
            Log.d(TAG, "Contacting server to retrieve check in photo");

            long checkInId = params[0];

            Bitmap result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mContext) +
                        RestUriConstants.CHECK_IN_CONTROLLER + File.separator +
                        RestUriConstants.PHOTO + "?" + RestUriConstants.PARAM_ID + "=" + checkInId;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to byte[]
                byte[] image = restTemplate.getForObject(url, byte[].class);

                // decode the image received to fit the device's screen
                if (image != null) {
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    int width = size.x;
                    int height = size.y;

                    result = decodeSampledBitmapFromByteArray(image, width, height);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            mBitmap = result;

            if (mBitmap != null) {
                mImageView.setImageBitmap(mBitmap);
            } else {
                Toast.makeText(mContext, getString(R.string.check_in_photo_not_loaded),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] array, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(array, 0, array.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(array, 0, array.length, options);
    }
}