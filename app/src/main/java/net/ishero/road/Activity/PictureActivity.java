package net.ishero.road.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.ishero.road.Fragment.Camera2PictureFragment;
import net.ishero.road.R;

public class PictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container2
                    , Camera2PictureFragment.newInstance()).commit();

        }
    }
}
