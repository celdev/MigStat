package com.celdev.migstat.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.celdev.migstat.R;

public class CustomSetWaitingTimeDialog extends AlertDialog.Builder {

    public CustomSetWaitingTimeDialog(Context context, final NumberPickerDialogReturn numberPickerDialogReturn) {
        super(context);
        final NumberPicker numberPicker = new NumberPicker(context);
        numberPicker.setMaxValue(36);
        numberPicker.setMinValue(1);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(context);
        textView.setText(R.string.choose_months);
        linearLayout.addView(textView);
        linearLayout.addView(numberPicker);

        setView(linearLayout);
        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numberPickerDialogReturn.returnOnOk(numberPicker.getValue());
            }
        });
        setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }


    public void createAndShow() {
        create().show();
    }
}
