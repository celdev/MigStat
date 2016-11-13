package com.celdev.migstat.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.util.Linkify;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.celdev.migstat.R;

import java.util.Calendar;

public class CustomAboutDialog extends AlertDialog.Builder {

    public CustomAboutDialog(Context context) {
        super(context);
        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        setIcon(R.drawable.ic_help_outline_black_24dp);
        setTitle(R.string.about);

        ScrollView scrollView = new ScrollView(context);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(context);
        textView.setText(R.string.about_message);
        linearLayout.addView(textView);
        Linkify.addLinks(textView, Linkify.WEB_URLS);
        Linkify.addLinks(textView, Linkify.EMAIL_ADDRESSES);
        textView.setLinksClickable(true);
        scrollView.addView(linearLayout);
        setView(scrollView);
    }

    public void createAndShow() {
        create().show();
    }

}
