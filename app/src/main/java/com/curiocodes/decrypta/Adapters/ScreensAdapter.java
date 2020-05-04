package com.curiocodes.decrypta.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.curiocodes.decrypta.Models.ScreenItem;
import com.curiocodes.decrypta.R;

import java.util.List;

public class ScreensAdapter extends PagerAdapter {

    private Context context;
    List<ScreenItem> list;

    public ScreensAdapter(Context context, List<ScreenItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutScreen = inflater.inflate(R.layout.layout_screen, null);

        ImageView imageView = layoutScreen.findViewById(R.id.image);
        TextView title = layoutScreen.findViewById(R.id.title);
        TextView desc = layoutScreen.findViewById(R.id.desc);

        title.setText(list.get(position).getTitle());
        desc.setText(list.get(position).getDesc());
        imageView.setImageResource(list.get(position).getScreenImg());

        container.addView(layoutScreen);

        return layoutScreen;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
