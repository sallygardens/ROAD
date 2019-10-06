package net.ishero.road;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.ishero.road.Activity.BroadcastVideoActivity;
import net.ishero.road.Activity.PictureActivity;
import net.ishero.road.Activity.VideoActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    /**
     *  拍照按钮
     */
    Button picture;

    /**
     * 视频按钮
     */
    Button video;

    /**
     * 视频播放按钮
     */
    Button bcvideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //动态请求权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},1);

        }

        picture=findViewById(R.id.picture);
        video = findViewById(R.id.videotest);
        bcvideo=findViewById(R.id.bc_video);

        picture.setOnClickListener(this);
        video.setOnClickListener(this);
        bcvideo.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.picture){
            Intent intent = new Intent(MainActivity.this,PictureActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.videotest) {
            Intent intent = new Intent(MainActivity.this, VideoActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.bc_video){
            Intent intent = new Intent(MainActivity.this, BroadcastVideoActivity.class);
            startActivity(intent);
        }
    }

}
