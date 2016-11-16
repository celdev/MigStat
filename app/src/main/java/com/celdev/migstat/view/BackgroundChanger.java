package com.celdev.migstat.view;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.celdev.migstat.R;
import com.celdev.migstat.ShowStatus;
import com.celdev.migstat.controller.Controller;
import com.celdev.migstat.controller.DataStorage;

public class BackgroundChanger {

    private final ShowStatus activity;
    private final RelativeLayout root;
    private final LinearLayout parent;
    private final Controller controller;

    public static final int[] drawables = new int[]{
            R.drawable.bg_path_wood,
            R.drawable.christmas_bulb,
            R.drawable.christmas_tree,
            R.drawable.couple_forest,
            R.drawable.couple_love,
            R.drawable.stockholm,
            R.drawable.santa_clause,
            R.drawable.palm_tree_waterfall,
            R.drawable.relationship_lake
    };

    private static int currentBG = 0;

    public BackgroundChanger(Controller controller,RelativeLayout root, LinearLayout parent, ShowStatus activity) {
        this.root = root;
        this.controller = controller;
        this.activity = activity;
        this.parent = parent;
        initView();
    }

    private void initView() {
        try {
            LayoutInflater layoutInflater = activity.getLayoutInflater();
            LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.layout_change_bg_mode, parent);

            ImageButton backBtn = (ImageButton) linearLayout.findViewById(R.id.change_bg_back);
            ImageButton nextBtn = (ImageButton) linearLayout.findViewById(R.id.change_bg_next);
            Button okBtn = (Button) linearLayout.findViewById(R.id.change_bg_ok);
            initButtons(backBtn, nextBtn, okBtn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void initButtons(ImageButton back, ImageButton next, Button ok) {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.setBackground(getPrefBackground());
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.setBackground(getNextBackground());
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewBackground();
                activity.removeSetBG();
            }
        });
    }

    private Drawable getPrefBackground() {
        if (currentBG == 0) {
            currentBG = drawables.length - 1;
        } else {
            currentBG--;
        }
        return activity.getDrawable(drawables[currentBG]);
    }

    private Drawable getNextBackground() {
        if (currentBG == drawables.length - 1) {
            currentBG = 0;
        } else {
            currentBG++;
        }
        return activity.getDrawable(drawables[currentBG]);
    }

    private void saveNewBackground() {
        controller.saveBackground(currentBG);
    }

}
