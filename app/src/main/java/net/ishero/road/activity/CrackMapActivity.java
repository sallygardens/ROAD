package net.ishero.road.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.zhy.autolayout.AutoLayoutActivity;

import net.ishero.road.R;

public class CrackMapActivity extends AutoLayoutActivity implements View.OnClickListener {
    private MapView mapView;
    private ImageButton backButton, localMeButton;

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
        backButton = findViewById(R.id.ib_back_map);
        localMeButton = findViewById(R.id.ib_local_me);
        backButton.setOnClickListener(this);
        localMeButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_back_map:
                this.finish();
                break;
            case R.id.ib_local_me:
                break;
        }
    }
}
