package net.ishero.road;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zhy.autolayout.AutoLayoutActivity;

import net.ishero.road.utils.Permission;

public class MainActivity extends AutoLayoutActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
    }

    private void requestPermission() {
        Permission.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Permission.requestPermission(this, Manifest.permission.CAMERA);
        Permission.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        Permission.requestPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

    }

}
