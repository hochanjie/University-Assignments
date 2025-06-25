package com.team16_2.temporun.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.team16_2.temporun.R;
import com.team16_2.temporun.RunDetail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DistanceFragment extends Fragment {

    private View view;
    private ArrayList<RunDetail> runDetails;
    private LocalDateTime lt = LocalDateTime.now();
    private LineChart lineChart;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
    private Integer numDays;

    private ArrayList<Entry> dataSet = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_distance, container, false);

        runDetails = getArguments().getParcelableArrayList("runDetails");
        numDays = getArguments().getInt("numDays");
        lineChart = view.findViewById(R.id.distance_line_chart);

        //        ArrayList for x-axis
        ArrayList<String> keys = new ArrayList<>();
        for (int i = numDays; i >= 0; i--) {
            keys.add(lt.minusDays(i).format(formatter));
        }

        HashMap<String, Integer> timeDict = new HashMap<>();
        for (String key: keys) {
            timeDict.put(key, 0);
        }

        for (RunDetail rd : runDetails) {
            String curDate = rd.getDate().format(formatter);
            timeDict.put(curDate, timeDict.get(curDate) + rd.getDistance());
        }

        for (int i = 0; i < keys.size(); i++) {
            dataSet.add(new Entry(i, timeDict.get(keys.get(i))));
        }

        LineDataSet lineDataSet = new LineDataSet(dataSet, "Distance");
        lineDataSet.setDrawValues(false);
        lineDataSet.setFillColor(Color.WHITE);
        lineDataSet.setFillAlpha(30);
        lineDataSet.setDrawFilled(true);
        LineData lineData = new LineData(lineDataSet);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(keys));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(lineData.getXMin() - 0.2f);
        xAxis.setAxisMaximum(lineData.getXMax() + 0.2f);
        xAxis.setTextColor(getResources().getColor(R.color.white));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        lineChart.getAxisRight().setEnabled(false);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setTextColor(getResources().getColor(R.color.white));
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value/1000 + " km";
            }
        });

        lineChart.setData(lineData);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);

        return view;
    }
}