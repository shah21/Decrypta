package com.curiocodes.decrypta.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.curiocodes.decrypta.Adapters.ScreensAdapter;
import com.curiocodes.decrypta.Models.ScreenItem;
import com.curiocodes.decrypta.R;

import java.util.ArrayList;
import java.util.List;

public class GetstartActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ScreensAdapter screensAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_start);

        viewPager = findViewById(R.id.frame);
        //list
        List<ScreenItem> list = new ArrayList<>();
        list.add(new ScreenItem("Communicate with anyone", "", R.drawable.clip_unsubscribed));
        list.add(new ScreenItem("Communicate with anywhere", "", R.drawable.pluto_no_comments));
        list.add(new ScreenItem("Communicate with security", "", R.drawable.marginalia_information_security));
        screensAdapter = new ScreensAdapter(this, list);

        viewPager.setAdapter(screensAdapter);
    }
}
