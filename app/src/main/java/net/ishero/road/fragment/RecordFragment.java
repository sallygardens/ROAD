package net.ishero.road.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import net.ishero.road.Binder.CrackRecordItemViewBinder;
import net.ishero.road.Item.CrackRecordItem;
import net.ishero.road.R;
import net.ishero.road.activity.CrackDetailActivity;
import net.ishero.road.activity.CrackMapActivity;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;


public class RecordFragment extends Fragment {
    private View root;
    private RecyclerView recyclerView;
    private MultiTypeAdapter mAdapter;
    private Items mItems;
    private ImageButton openMapButton;

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
        initRecycleView();
        openMapButton = root.findViewById(R.id.ib_map);
        openMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(root.getContext(), CrackMapActivity.class));

            }
        });

    }
    private void initRecycleView(){
        recyclerView = root.findViewById(R.id.rv_record);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(CrackRecordItem.class, new CrackRecordItemViewBinder());
        recyclerView.setAdapter(mAdapter);
        mItems = new Items();
        for(int i = 0; i < 20; i++){
            mItems.add(new CrackRecordItem());
        }
        mAdapter.setItems(mItems);
        mAdapter.notifyDataSetChanged();

    }


}
