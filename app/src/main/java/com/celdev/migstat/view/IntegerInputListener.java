package com.celdev.migstat.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.R;

public class IntegerInputListener implements TextWatcher {

    private MainActivity mainActivity;
    private EditText applicationNumberField;

    public IntegerInputListener(MainActivity mainActivity, EditText applicationNumberField) {
        this.mainActivity = mainActivity;
        this.applicationNumberField = applicationNumberField;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (!editable.toString().isEmpty()) {
            try {
                Integer.parseInt(editable.toString());
                applicationNumberField.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.white));
            } catch (NumberFormatException e) {
                applicationNumberField.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.light_red));
            }
        }
        if (editable.toString().length() == 8) {
            hideInput();
        }
    }

    private void hideInput() {
        ((InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(applicationNumberField.getWindowToken(), 0);
    }
}
