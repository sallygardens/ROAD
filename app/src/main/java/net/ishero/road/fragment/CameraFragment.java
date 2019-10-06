package net.ishero.road.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.ishero.road.R;


public class CameraFragment extends Fragment implements View.OnClickListener {
    private View root;
    private FloatingActionMenu menuRed;
    private Spinner spinner;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;


    public CameraFragment() {
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
        root = inflater.inflate(R.layout.fragment_camera, container, false);
        initView();
        return root;
    }

    private void initView() {
        menuRed = root.findViewById(R.id.menu_red);
        spinner = root.findViewById(R.id.top_spiner);
        fab1 = root.findViewById(R.id.fab1);
        fab2 = root.findViewById(R.id.fab2);
        fab3 = root.findViewById(R.id.fab3);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.fab1:
                break;
            case R.id.fab2:
                break;
            case R.id.fab3:
                break;
        }
    }
}
