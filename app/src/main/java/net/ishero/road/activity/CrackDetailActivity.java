package net.ishero.road.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.bumptech.glide.Glide;

import net.ishero.road.R;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class CrackDetailActivity extends AppCompatActivity {
    private JzvdStd jzvdStd;
    private MapView mapView;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crack_detail);
        mapView = findViewById(R.id.detail_map);
        mapView.onCreate(savedInstanceState);
        AMap aMap = mapView.getMap();
        initView();
    }

    private void initView() {
        backButton = findViewById(R.id.ib_back_map2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        jzvdStd = findViewById(R.id.jz_video);
        Glide.with(this).load("http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png").into(jzvdStd.thumbImageView);

        jzvdStd.setUp("http://47.95.7.169:8000/live/index.m3u8", "路面回放",
                JzvdStd.SCREEN_NORMAL);

    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }
}
