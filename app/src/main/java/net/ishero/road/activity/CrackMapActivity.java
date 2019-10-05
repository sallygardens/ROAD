package net.ishero.road.activity;

import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.zhy.autolayout.AutoLayoutActivity;

import net.ishero.road.R;

public class CrackMapActivity extends AutoLayoutActivity {
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crack_map);
        mapView = findViewById(R.id.crack_map);
        mapView.onCreate(savedInstanceState);
        AMap aMap = mapView.getMap();
        initView();
    }

    private void initView() {


    }


}
