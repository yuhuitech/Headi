package com.example.headi.ui.stats;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.headi.R;
import com.example.headi.db.DiaryStats;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private View view;
    private EditText fromDate;
    private EditText toDate;
    private String fromDateFilter;
    private String toDateFilter;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;
    private DiaryStats diaryStats;
    private PieChart piePainDurationRatio;
    private BarChart barCountStrengthRatio;
    private LineChart lineDurationOverTime;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Context context = getActivity();
        view = inflater.inflate(R.layout.fragment_stats, container, false);

        setHasOptionsMenu(true);
        setupCharts();

        try {
            readFromDB(null, null);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_stats_filter:  {
                openFilterDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupCharts() {
        Context context = getActivity();

        // Pie Chart: Pain - Duration ratio
        piePainDurationRatio = view.findViewById(R.id.stats_pain_duration_ratio);
        piePainDurationRatio.getDescription().setEnabled(false);
        piePainDurationRatio.setUsePercentValues(true);
        piePainDurationRatio.setCenterText("Ratio of pains");
        piePainDurationRatio.getLegend().setEnabled(false);
        piePainDurationRatio.setRotationEnabled(false);
        piePainDurationRatio.setHighlightPerTapEnabled(false);
        piePainDurationRatio.setExtraOffsets(30.f, 0.f, 30.f, 0.f);
        piePainDurationRatio.setHoleRadius(35f);
        piePainDurationRatio.setTransparentCircleRadius(44f);
        piePainDurationRatio.animateXY(1000, 1000);

        // Bar Chart: Count - Strength ratio
        barCountStrengthRatio = view.findViewById(R.id.stats_count_strength_ratio);
        barCountStrengthRatio.getDescription().setEnabled(false);
        barCountStrengthRatio.setScaleEnabled(false);
        barCountStrengthRatio.setDrawBarShadow(false);
        barCountStrengthRatio.setDrawGridBackground(false);
        barCountStrengthRatio.getLegend().setEnabled(false);
        barCountStrengthRatio.setHighlightPerTapEnabled(false);
        barCountStrengthRatio.animateXY(1000, 1000);
        barCountStrengthRatio.setTouchEnabled(false);

        XAxis xAxis = barCountStrengthRatio.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getPrimaryTextColor());
        xAxis.setLabelCount(10);

        YAxis yAxis = barCountStrengthRatio.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setTextColor(getPrimaryTextColor());
        yAxis.setGranularity(1.0f);
        yAxis.setGranularityEnabled(true); // Required to enable granularity
        barCountStrengthRatio.getAxisRight().setDrawLabels(false);

        // Line Chart: Pain minutes over time
        lineDurationOverTime = view.findViewById(R.id.stats_duration_over_time);
        lineDurationOverTime.getDescription().setEnabled(false);
        lineDurationOverTime.setScaleEnabled(false);
        lineDurationOverTime.setDragEnabled(false);
        lineDurationOverTime.setTouchEnabled(false);
        lineDurationOverTime.setPinchZoom(false);
        lineDurationOverTime.animateXY(1000, 1000);
        lineDurationOverTime.getLegend().setEnabled(false);
        lineDurationOverTime.setHighlightPerTapEnabled(false);
        lineDurationOverTime.getXAxis().setValueFormatter(new DiaryStats.LineChartXAxisValueFormatter());

        xAxis = lineDurationOverTime.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getPrimaryTextColor());

        yAxis = lineDurationOverTime.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setTextColor(getPrimaryTextColor());
        lineDurationOverTime.getAxisRight().setDrawLabels(false);

    }

    private void openFilterDialog() {
        Context context = requireActivity();

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.stats_set_date_title));

        // set the save layout
        final View saveView = getLayoutInflater().inflate(R.layout.fragment_stats_filter_dialog, null);
        builder.setView(saveView);

        registerDatePickerListeners(saveView);

        // add apply button
        builder.setPositiveButton(context.getString(R.string.apply_button), (dialog, which) -> {
            String timeSelection = "";
            ArrayList<String> timeArgs = new ArrayList<>();
            String selection = "";
            String[] selectionArgs = new String[0];

            // time filter is set
            if (fromDateFilter != null && toDateFilter != null) {
                timeSelection = HeadiDBContract.Diary.COLUMN_START_DATE + " >= ? AND " + HeadiDBContract.Diary.COLUMN_END_DATE + " <= ?";
                timeArgs.add(fromDateFilter);
                timeArgs.add(toDateFilter);
            }

            if (!timeSelection.isEmpty()) {
                selection =  timeSelection;
                selectionArgs = timeArgs.toArray(new String[0]);
            }

            if (!selection.isEmpty()) {
                try {
                    readFromDB(selection, selectionArgs);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> { });

        // add delete filter button
        builder.setNeutralButton(context.getString(R.string.remove_filter_button), (dialog, which) -> {
            try {
                readFromDB(null, null);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void registerDatePickerListeners(View view) {
        Context context = requireActivity();
        SimpleDateFormat date_formatter = new SimpleDateFormat("E dd. MMM yyyy", Locale.getDefault());
        fromDate = (EditText) view.findViewById(R.id.filter_from_date);
        toDate = (EditText) view.findViewById(R.id.filter_to_date);

        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth, 0,0,0);
                fromDate.setText(date_formatter.format(newDate.getTime()));
                fromDateFilter = Long.toString(newDate.getTimeInMillis());
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth, 23, 59, 59);
                toDate.setText(date_formatter.format(newDate.getTime()));
                toDateFilter = Long.toString(newDate.getTimeInMillis());
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        fromDate.setOnClickListener(v -> fromDatePickerDialog.show());
        toDate.setOnClickListener(v -> toDatePickerDialog.show());
    }

    private void readFromDB(String selection, String[] selectionArgs) throws ParseException {
        Context context = getActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        diaryStats = helper.readDiaryStatsFromDB(context, selection, selectionArgs);

        populateCharts();
        setStatsFromAndToDate();
    }

    private void setStatsFromAndToDate() {
        TextView fromAndTo = (TextView) view.findViewById(R.id.stats_date_from_to);
        String from = diaryStats.getStatsFromDate();
        String to = diaryStats.getStatsToDate();
        fromAndTo.setText(from + " - " + to);
    }

    private void populateCharts() throws ParseException {
        piePainDurationRatio.setData(diaryStats.getPainAndDurationRatio(piePainDurationRatio));
        piePainDurationRatio.invalidate();

        barCountStrengthRatio.setData(diaryStats.getCountAndStrengthRatio());
        barCountStrengthRatio.setFitBars(true);
        barCountStrengthRatio.invalidate();

        lineDurationOverTime.setData(diaryStats.getDurationOverTime());
        lineDurationOverTime.invalidate();
    }

    private int getPrimaryTextColor() {
        Context context = getActivity();
        // Get the primary text color of the theme
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
        int primaryColor = arr.getColor(0, -1);
        return primaryColor;
    }


}