package net.ishero.road.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ishero.road.Item.MineTopItem;
import net.ishero.road.Binder.MineTopItemViewBinder;
import net.ishero.road.Item.MineRecordItem;
import net.ishero.road.Binder.MineRecordItemViewBinder;
import net.ishero.road.R;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;


public class MineFragment extends Fragment {
    private View root;
    private RecyclerView recyclerView;

    private MultiTypeAdapter mAdapter;
    private Items mItems;

    public MineFragment() {
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
        root = inflater.inflate(R.layout.fragment_mine, container, false);
        initView();
        return root;
    }

    private void initView() {
        recyclerView = root.findViewById(R.id.rv_mine);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(MineTopItem.class, new MineTopItemViewBinder());
        mAdapter.register(MineRecordItem.class, new MineRecordItemViewBinder());
        recyclerView.setAdapter(mAdapter);
        mItems = new Items();
        mItems.add(new MineTopItem());
        for (int i = 0; i < 10; i++){
            mItems.add(new MineRecordItem());
        }




        mAdapter.setItems(mItems);
        mAdapter.notifyDataSetChanged();


    }


}
