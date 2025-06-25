package com.team16_2.temporun.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.team16_2.temporun.R;
import com.team16_2.temporun.RunDetail;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TodayRunFragment extends Fragment {

    private TextView hrs;
    private TextView mins;
    private TextView kms;
    private TextView metres;
    private int totalDist = 0;
    private int totalTime = 0;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_today_run, container, false);
        hrs = view.findViewById(R.id.hrView);
        mins = view.findViewById(R.id.minView);
        kms = view.findViewById(R.id.kmView);
        metres = view.findViewById(R.id.metresView);

        ArrayList<RunDetail> todayRun = getArguments().getParcelableArrayList("todayRun");
        for (RunDetail rd : todayRun) {
            totalDist = totalDist + rd.getDistance();
            totalTime = totalTime + (int) (rd.getStartTime().until(rd.getEndTime(), ChronoUnit.MINUTES ));
        }

        double distRawData = (double) totalDist/1000;
        int distKm = (int) Math.floor(distRawData);
        double distM = distRawData - (double) distKm;

        String kmString = Integer.toString(distKm);
        String metresString = String.format("%.2f", distM).substring(2);

        if (kmString != null && kmString.length() < 2){
            kmString = "0" + kmString;
        }
        if (metresString != null && metresString.length() < 2){
            metresString = "0" + metresString;
        }

        kms.setText(kmString);
        metres.setText(metresString);

        double timeRawData = (double) totalTime/60;
        int timeHrs = (int) Math.floor(timeRawData);
        int timeMin = totalTime % 60;

        String hrsString = Integer.toString(timeHrs);
        String minsString = Integer.toString(timeMin);

        if (hrsString != null && hrsString.length() < 2){
            hrsString = "0" + hrsString;
        }
        if (minsString != null && minsString.length() < 2){
            minsString = "0" + minsString;
        }

        hrs.setText(hrsString);
        mins.setText(minsString);


        // Inflate the layout for this fragment
        return view;
    }
}