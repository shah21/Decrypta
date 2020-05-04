package com.curiocodes.decrypta.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.curiocodes.decrypta.R;

public class ConfirmDialog extends DialogFragment {
    private String title, message, btnTxt;
    private ConfirmDialog.onSelected onSelected;

    public ConfirmDialog(String title, String message, String btnTxt) {
        this.title = title;
        this.message = message;
        this.btnTxt = btnTxt;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(btnTxt, new DialogInterface.OnClickListener() {
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
            onSelected = (ConfirmDialog.onSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "mst implement dialog   listener");
        }
    }

    public interface onSelected {
        void onDone(boolean isDone);
    }
}
