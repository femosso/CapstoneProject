package com.capstone.application.activity;

import android.graphics.Color;
import android.graphics.Typeface;
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
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PieChartActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    private static final String TAG = PieChartActivity.class.getName();

    private PieChart mChart;

    private Typeface tf;

    private String mInformationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piechart);

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
        mChart = (PieChart) findViewById(R.id.pieChart);
        mChart.setUsePercentValues(true);
        mChart.setDescription("");
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        mChart.setCenterTextTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        mChart.setCenterText("text");

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setData(answerList);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        Legend l = mChart.getLegend();
        l.setPosition(LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
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

    private Set<String> convertStringToSet(String value) {
        if (value != null) {
            return new HashSet<>(Arrays.asList(value.split(",")));
        }

        return null;
    }

    private void setData(List<Answer> answerList) {
        HashMap<String, Integer> optionsCounter = new HashMap<>();

        Set<String> optionsChecked;
        int counter;
        for (Answer answer : answerList) {
            optionsChecked = convertStringToSet(answer.getText());

            for (String item : optionsChecked) {
                // if already initialized, increment counter, else initialize it with 1
                if (optionsCounter.containsKey(item)) {
                    counter = optionsCounter.get(item);
                    counter++;
                    optionsCounter.put(item, counter);
                } else {
                    optionsCounter.put(item, 1);
                }
            }
        }

        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yVals = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Integer> entry : optionsCounter.entrySet()) {
            yVals.add(new Entry((float) entry.getValue(), i));
            xVals.add(entry.getKey());
            i++;
        }

        PieDataSet dataSet = new PieDataSet(yVals, "Election Results");
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

/*        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);*/

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

/*        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);*/

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(tf);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getVal() + ", xIndex: " + e.getXIndex()
                        + ", DataSet index: " + dataSetIndex);
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }
}
