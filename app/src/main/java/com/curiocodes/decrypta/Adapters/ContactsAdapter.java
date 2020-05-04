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

import com.curiocodes.decrypta.Models.ContactsModel;
import com.curiocodes.decrypta.R;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private Context context;
    private List<ContactsModel> arrayList;
    private OnClick onClick;

    public ContactsAdapter(Context context, List<ContactsModel> arrayList, OnClick onClick) {
        this.context = context;
        this.arrayList = arrayList;
        this.onClick = onClick;
    }

    public interface OnClick {
        void onItemClicked(int pos);

        void onItemDelete(int pos);
    }


    @NonNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, final int position) {
        holder.mName.setText(arrayList.get(position).getName());
        holder.mPhone.setText(arrayList.get(position).getPhone());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClicked(position);
            }
        });
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemDelete(position);
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
        private ImageView mDelete;
        private RelativeLayout linearLayout;

        public ViewHolder(View v) {
            super(v);
            mDelete = v.findViewById(R.id.delete);
            mName = v.findViewById(R.id.name);
            mPhone = v.findViewById(R.id.phone);
            linearLayout = v.findViewById(R.id.layout);
        }
    }

}
