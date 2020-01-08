package com.curiocodes.decrypta.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Models.ContactsModel;
import com.curiocodes.decrypta.R;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private Context context;
    private List<ContactsModel> arrayList;
    private OnClick onClick;

    public Adapter(Context context, List<ContactsModel> arrayList, OnClick onClick) {
        this.context = context;
        this.arrayList = arrayList;
        this.onClick = onClick;
    }

    public void updateList(List<ContactsModel> newList){
        arrayList = new ArrayList<>();
        arrayList.addAll(newList);
        notifyDataSetChanged();
    }

    public interface OnClick{
        void onItemClicked(String name,String number);
    }



    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contacts_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, final int position) {
        holder.mName.setText(arrayList.get(position).getName());
        holder.mPhone.setText(arrayList.get(position).getPhone());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClicked(arrayList.get(position).getName(),arrayList.get(position).getPhone());
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mPhone;
        private TextView mName;
        private LinearLayout linearLayout;
        public ViewHolder(View v){
            super(v);
            mName = v.findViewById(R.id.name);
            mPhone = v.findViewById(R.id.phone);
            linearLayout = v.findViewById(R.id.layout);
        }
    }
}
