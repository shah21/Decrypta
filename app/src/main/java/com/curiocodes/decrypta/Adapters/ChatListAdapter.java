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
import com.curiocodes.decrypta.Models.RecentChatModel;
import com.curiocodes.decrypta.Models.UserModel;
import com.curiocodes.decrypta.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private final OnClick onClick;
    private Context context;
    private List<RecentChatModel> arrayList;


    public ChatListAdapter(Context context, List<RecentChatModel> arrayList, OnClick onClick) {
        this.context = context;
        this.arrayList = arrayList;
        this.onClick = onClick;
    }


    public interface OnClick{
        void onItemClicked(int pos);
    }


    public void updateList(List<RecentChatModel> newList){
        arrayList = new ArrayList<>();
        arrayList.addAll(newList);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ViewHolder holder, final int position) {
        holder.mName.setText(arrayList.get(position).getName());
        if (!arrayList.get(position).getUri().isEmpty()) {
            Picasso.get().load(arrayList.get(position).getUri()).fit().placeholder(R.drawable.child_placeholder).into(holder.circleImageView);
        }
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mName;
        private RelativeLayout layout;
        private CircleImageView circleImageView;
        public ViewHolder(View v){
            super(v);
            layout = v.findViewById(R.id.layout);
            circleImageView = v.findViewById(R.id.profile_image);
            mName = v.findViewById(R.id.name);
        }
    }
}
