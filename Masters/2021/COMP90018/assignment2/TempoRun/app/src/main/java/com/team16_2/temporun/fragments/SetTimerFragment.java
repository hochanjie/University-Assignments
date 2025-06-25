package com.team16_2.temporun.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.team16_2.temporun.R;
import com.team16_2.temporun.SettingRunActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetTimerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetTimerFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;
    EditText hours;
    EditText minutes;

    public SetTimerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment setTimerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetTimerFragment newInstance(String param1, String param2) {
        SetTimerFragment fragment = new SetTimerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_set_timer, container, false);
        hours = view.findViewById(R.id.hours);
        minutes = view.findViewById(R.id.minutes);

        minutes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    String StartMinute = minutes.getText().toString();

                    //Format single number
                    if (StartMinute != null && StartMinute.length() < 2){
                        StartMinute = "0" + StartMinute;
                    }
                    minutes.setText(StartMinute);
                    Bundle result = new Bundle();
                    result.putString("minutes", minutes.getText().toString());
                    getParentFragmentManager().setFragmentResult("requestMinutes", result);
                }
            }
        });

        hours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    Bundle result = new Bundle();
                    result.putString("hours", hours.getText().toString());
                    getParentFragmentManager().setFragmentResult("requestHours", result);
                }
            }
        });

        return view;
    }

    public void send(EditText h, EditText m){
        Intent intent = new Intent(getActivity().getBaseContext(), SettingRunActivity.class);
        intent.putExtra("hours", h.getText().toString()).putExtra("minutes", m.getText().toString()).putExtra("option","Time");
        getActivity().startActivity(intent);
    }

}