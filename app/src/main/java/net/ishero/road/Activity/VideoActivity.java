package net.ishero.road.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.ishero.road.Fragment.Camera2VideoFragment;
import net.ishero.road.R;

public class VideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container
                    , Camera2VideoFragment.newInstance()).commit();
        }
    }
}
