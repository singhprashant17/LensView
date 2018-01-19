package com.exmaple.lensview;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LensView lensView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lensView = findViewById(R.id.lensView);
        lensView.setApps(getApps());
        lensView.setIconSize(20);
    }

    private ArrayList<App> getApps() {
        ArrayList<App> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final App app = new App();
            app.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable
                    .ic_3d_rotation_black_24dp));
            list.add(app);
        }
        return list;
    }
}
