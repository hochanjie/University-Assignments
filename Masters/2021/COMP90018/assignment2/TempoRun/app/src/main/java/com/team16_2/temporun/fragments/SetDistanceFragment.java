package com.team16_2.temporun.fragments;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.team16_2.temporun.ItemViewModel;
import com.team16_2.temporun.R;
import com.team16_2.temporun.SettingRunActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetDistanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetDistanceFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View view;
    EditText goalDistance;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SetDistanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment setDistanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetDistanceFragment newInstance(String param1, String param2) {
        SetDistanceFragment fragment = new SetDistanceFragment();
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
        view = inflater.inflate(R.layout.fragment_set_distance, container, false);
        goalDistance = view.findViewById(R.id.editDistanceNumber);

        goalDistance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    Bundle result = new Bundle();
                    result.putString("goalDistance", goalDistance.getText().toString());
                    getParentFragmentManager().setFragmentResult("requestDistance", result);
                }
            }
        });

        return view;
    }

}