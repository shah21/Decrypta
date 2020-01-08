package com.curiocodes.decrypta.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.curiocodes.decrypta.Models.SaveModel;
import com.curiocodes.decrypta.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SavedAdapter extends BaseAdapter {

    private Context context;
    private List<SaveModel> list;
    private Onclick onclick;

    public SavedAdapter(Context context, List<SaveModel> list,Onclick onclick) {
        this.onclick = onclick;
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            convertView= LayoutInflater.from(context).inflate(R.layout.photos_layout,parent,false);
        }

        ImageView imageView = convertView.findViewById(R.id.image);
        Picasso.get().load(list.get(position).getUri()).fit().into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclick.onClickItem(position);
            }
        });

        return convertView;
    }

    public interface Onclick{
        void onClickItem(int pos);
    }
}
