package net.ishero.road.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;

import net.ishero.road.R;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class BroadcastVideoActivity extends AppCompatActivity {

    JzvdStd myJzvdStd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_video);


        myJzvdStd=  findViewById(R.id.videoplayer);
        // rlvv=findViewById(R.id.rlvv);
        //设置视频标题
        myJzvdStd.setUp("http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4"
                , "饺子快长大", Jzvd.SCREEN_NORMAL);
        Log.d("sss", "onCreate: "+"wwss");
        //设置开场图片
        Glide.with(this).load("http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png").into(myJzvdStd.thumbImageView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }
}
