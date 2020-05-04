package com.curiocodes.decrypta.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.curiocodes.decrypta.Adapters.ContactsAdapter;
import com.curiocodes.decrypta.R;

public class AddDialog extends DialogFragment {

    private String title, message;
    private onSelected onSelected;

    public AddDialog(String title, String message) {
        this.title = title;
        this.message = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onSelected.onDone(true);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onSelected.onDone(false);
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onSelected = (onSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "mst implement dialog   listener");
        }
    }

    public interface onSelected {
        void onDone(boolean isDone);
    }
}
