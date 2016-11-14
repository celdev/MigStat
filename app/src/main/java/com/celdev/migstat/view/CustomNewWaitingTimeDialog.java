package com.celdev.migstat.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.celdev.migstat.R;
import com.celdev.migstat.model.Application;

public class CustomNewWaitingTimeDialog extends AlertDialog.Builder {

    public CustomNewWaitingTimeDialog(Context context) {
        super(context);
        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        setMessage(R.string.new_waiting_time_message);
    }
}
