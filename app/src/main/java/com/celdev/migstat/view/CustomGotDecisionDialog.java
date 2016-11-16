package com.celdev.migstat.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.celdev.migstat.R;

public class CustomGotDecisionDialog extends AlertDialog.Builder {


    public CustomGotDecisionDialog(Context context) {
        super(context);

        setMessage(R.string.congratulations_on_decision).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

}
