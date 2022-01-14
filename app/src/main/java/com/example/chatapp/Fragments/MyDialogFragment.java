package com.example.chatapp.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class MyDialogFragment extends DialogFragment {

    public static final String TITLE = "dataKey";
    private OnYesNoClick yesNoClick;

    public static MyDialogFragment newInstance(String dataToShow ) {
        MyDialogFragment frag = new MyDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, dataToShow);
        frag.setArguments(args);
        return frag;
    }

    public void setOnYesNoClick(OnYesNoClick yesNoClick) {
        this.yesNoClick = yesNoClick;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String mDataRecieved = getArguments().getString(TITLE,"defaultTitle");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder
                .setMessage(
                        "Do you want to send the recording?")
                .setNegativeButton(
                        "Is not", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if(yesNoClick != null)
                            yesNoClick.onNoClicked();
                    }
                })

                .setPositiveButton(
                        "Have", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if(yesNoClick != null)
                            yesNoClick.onYesClicked();
                    }
                });
        Dialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.WHITE));

        return dialog;

    }

    public interface OnYesNoClick{
        void onYesClicked();
        void onNoClicked();
    }
}