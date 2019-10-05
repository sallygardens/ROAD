package net.ishero.road.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.ishero.road.R;
import net.ishero.road.activity.CrackMapActivity;


public class RecordFragment extends Fragment {
    private View root;
    private Button startMapActivityButton;

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        root = inflater.inflate(R.layout.fragment_record, container, false);
        initView();
        return root;
    }

    private void initView(){
        startMapActivityButton = root.findViewById(R.id.bt_record_start_map_activity);
        startMapActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(root.getContext(), CrackMapActivity.class));
            }
        });

    }


}
