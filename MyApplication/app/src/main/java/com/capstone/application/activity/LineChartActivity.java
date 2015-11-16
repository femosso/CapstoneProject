package com.capstone.application.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.capstone.application.R;
import com.capstone.application.model.Answer;
import com.capstone.application.model.Feedback;
import com.capstone.application.utils.Constants;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LineChartActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    private static final String TAG = LineChartActivity.class.getName();

    private LineChart mChart;

    private String mInformationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linechart);

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.activity_name_graph_view));
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Feedback feedback = extras.getParcelable("feedback");
            mInformationType = extras.getString("type");

            if (feedback != null) {
                initOverallInformation(feedback);
                initChart(feedback.getAnswerList());
            }
        }
    }

    /**
     * Sets overall information about the information being display, such as teen name, type
     * of information and period.
     */
    private void initOverallInformation(Feedback feedback) {
        List<Answer> answerList = feedback.getAnswerList();

        long start = answerList.get(0).getCheckIn().getDate();
        long end = answerList.get(answerList.size() - 1).getCheckIn().getDate();

        String startDate = new SimpleDateFormat(Constants.DATE_FORMAT).format(start);
        String endDate = new SimpleDateFormat(Constants.DATE_FORMAT).format(end);

        TextView informationType = (TextView) findViewById(R.id.txtInformationType);
        informationType.setText(Html.fromHtml("<b>" + feedback.getUser().getFirstName() +
                "</b>'s " + mInformationType + " data from " + startDate + " to " + endDate));
    }

    private void initChart(List<Answer> answerList) {
        mChart = (LineChart) findViewById(R.id.lineChart);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription(getString(R.string.chart_no_data));

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        //mChart.setBackgroundColor(Color.GRAY);

        // add data
        setData(answerList);

        mChart.animateX(2500);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend
        //l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.LINE);
        l.setTextSize(11f);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        //l.setYOffset(11f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setSpaceBetweenLabels(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);

        mChart.getAxisRight().setEnabled(false);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void setData(List<Answer> answerList) {
        ArrayList<String> xValues = new ArrayList<>();
        ArrayList<Entry> yValues = new ArrayList<>();

        String date; int count = 0;
        for (Answer answer : answerList) {
            try {
                date = new SimpleDateFormat(Constants.LINE_CHART_DATE_TIME_FORMAT)
                        .format(answer.getCheckIn().getDate());
                xValues.add(date);

                yValues.add(new Entry(Float.valueOf(answer.getText()), count));
                count++;
            } catch (NumberFormatException e) {
                Log.d(TAG, "Invalid entry " + answer.getText());
            }
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yValues, mInformationType);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(2f);
        set1.setCircleSize(3f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);
        //set1.setFillFormatter(new MyFillFormatter(0f));
        //set1.setDrawHorizontalHighlightIndicator(false);
        //set1.setVisible(false);
        //set1.setCircleHoleColor(Color.WHITE);

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xValues, dataSets);
        data.setValueTextSize(9f);

        // set data
        mChart.setData(data);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }
}
