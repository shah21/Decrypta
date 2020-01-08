package com.curiocodes.decrypta.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.curiocodes.decrypta.AddOn.General;
import com.curiocodes.decrypta.Models.ChatModel;
import com.curiocodes.decrypta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHodler> {

    private Context context;
    private List<ChatModel> list;
    private OnItemClick onItemClick;

    public interface OnItemClick{
        void imgClick(int pos);
    }

    public ChatsAdapter(Context context, List<ChatModel> list,OnItemClick onItemClick) {
        this.context = context;
        this.list = list;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item,parent,false);
        return new ViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodler holder, final int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String message = list.get(position).getMessage();
        Date date = list.get(position).getTime();

        String pattern = "hh:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String time = simpleDateFormat.format(list.get(position).getTime());

        String userPhone = user.getPhoneNumber().replaceAll("\\s+","");
        if (list.get(position).getSender()!=null) {
            String sender = list.get(position).getSender();
            if (!sender.equals(userPhone)) {
                holder.l1.setVisibility(View.VISIBLE);
                if (message!=null) {
                    holder.m1.setText(message);
                    holder.m1.setVisibility(View.VISIBLE);
                }
                holder.d1.setText(time);
                if (list.get(position).getUri()!=null){
                    holder.img1.setVisibility(View.VISIBLE);
                    Picasso.get().load(list.get(position).getUri()).fit().placeholder(R.drawable.default_placeholder).into(holder.img1);
                }
            } else {
                holder.l2.setVisibility(View.VISIBLE);
                if (message!=null) {
                    holder.m2.setText(message);
                    holder.m2.setVisibility(View.VISIBLE);
                }
                holder.d2.setText(time);
                if (list.get(position).getUri()!=null){
                    holder.img2.setVisibility(View.VISIBLE);
                    Picasso.get().load(list.get(position).getUri()).fit().placeholder(R.drawable.default_placeholder).into(holder.img2);
                }
            }
        }

        holder.img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.imgClick(position);
            }
        });

        holder.img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.imgClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHodler extends RecyclerView.ViewHolder {
        private TextView m1,m2,d1,d2;
        private LinearLayout l1,l2;
        private ImageView img1,img2;
        public ViewHodler(@NonNull View itemView) {
            super(itemView);

            l1 = itemView.findViewById(R.id.block1);
            l2 = itemView.findViewById(R.id.block2);

            m1 = itemView.findViewById(R.id.m1);
            m2 = itemView.findViewById(R.id.m2);

            d1 = itemView.findViewById(R.id.d1);
            d2 = itemView.findViewById(R.id.d2);

            img1 = itemView.findViewById(R.id.img);
            img2 = itemView.findViewById(R.id.img2);
        }
    }

}
