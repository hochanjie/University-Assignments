package com.team16_2.temporun.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.team16_2.temporun.R;
import com.team16_2.temporun.RunDetail;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MonthRunFragment extends Fragment {

    private View view;
    private ArrayList<RunDetail> monthRun;
    private final static Integer numDays = 30 - 1;
    private Button timeBtn;
    private Button distanceBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_month_run, container, false);

        timeBtn = view.findViewById(R.id.timeBtn);
        distanceBtn = view.findViewById(R.id.distanceBtn);

        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeBtn.setBackgroundColor(Color.parseColor("#3168D8"));
                timeBtn.setTextColor(Color.parseColor("#FFFFFF"));
                distanceBtn.setBackgroundColor(Color.parseColor("#FFFFFF"));
                distanceBtn.setTextColor(Color.parseColor("#3168D8"));
                replaceFragment(new TimeFragment());
            }
        });

        distanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                distanceBtn.setBackgroundColor(Color.parseColor("#3168D8"));
                distanceBtn.setTextColor(Color.parseColor("#FFFFFF"));
                timeBtn.setBackgroundColor(Color.parseColor("#FFFFFF"));
                timeBtn.setTextColor(Color.parseColor("#3168D8"));
                replaceFragment(new DistanceFragment());
            }
        });

        monthRun = getArguments().getParcelableArrayList("monthRun");
        replaceFragment(new TimeFragment());
        //replaceFragment(new DistanceFragment());

        return view;
    }

    private void replaceFragment(Fragment fragment) {

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("runDetails", monthRun);
        bundle.putInt("numDays", numDays);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.month_frame_layout, fragment);
        fragmentTransaction.commit();

    }

}