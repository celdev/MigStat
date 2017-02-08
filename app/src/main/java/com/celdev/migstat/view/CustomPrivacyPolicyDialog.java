package com.celdev.migstat.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.celdev.migstat.R;
import com.celdev.migstat.controller.Controller;

public class CustomPrivacyPolicyDialog extends AlertDialog.Builder {

    private AlertDialog customPrivacyPolicyDialog;

    public CustomPrivacyPolicyDialog(@NonNull final Context context, final Controller controller) {
        super(context);
        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        controller.userOKPrivacyPolicy();
                        dialog.dismiss();
                    }
                }
        );
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(context);
        textView.setText(R.string.ok_privacypolicy);
        textView.setLinksClickable(true);
        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(textView);
        Linkify.addLinks(textView, Linkify.ALL);
        CheckBox checkBox = new CheckBox(context);
        checkBox.setText(R.string.agree_to_pp);
        linearLayout.addView(scrollView);
        linearLayout.addView(checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (customPrivacyPolicyDialog != null) {
                        customPrivacyPolicyDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    }
                } else {
                    if (customPrivacyPolicyDialog != null) {
                        customPrivacyPolicyDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            }
        });
        setView(linearLayout);
        setCancelable(false);

    }

    public void createAndShow() {
        customPrivacyPolicyDialog = this.create();
        customPrivacyPolicyDialog.show();
        disableOk();
    }

    public void disableOk() {
        customPrivacyPolicyDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        customPrivacyPolicyDialog.setCanceledOnTouchOutside(false);
    }
}
