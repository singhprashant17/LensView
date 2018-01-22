package com.exmaple.lensview;

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
        lensView.setAdapter(new AdapterClass(this, getApps()));
    }

    private ArrayList<App> getApps() {
        ArrayList<App> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            final App app = new App();
            list.add(app);
        }
        return list;
    }
}
