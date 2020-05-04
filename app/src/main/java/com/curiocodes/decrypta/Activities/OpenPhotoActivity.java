package com.curiocodes.decrypta.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.curiocodes.decrypta.R;
import com.squareup.picasso.Picasso;
import com.zolad.zoominimageview.ZoomInImageViewAttacher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenPhotoActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_open_photo);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("From " + getIntent().getStringExtra("sender"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.image);
        //set zoomable
        ZoomInImageViewAttacher mIvAttacter = new ZoomInImageViewAttacher();
        mIvAttacter.attachImageView(imageView);
        //load image
        Picasso.get().load(getIntent().getStringExtra("uri")).fit().into(imageView);
        /*date = findViewById(R.id.date);

        String pattern = "MM-dd-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String dateFormat = simpleDateFormat.format(new Date());
        System.out.println(date);
        date.setText(dateFormat);*/


    }
}
