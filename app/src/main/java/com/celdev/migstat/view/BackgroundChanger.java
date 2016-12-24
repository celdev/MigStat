package com.celdev.migstat.view;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.celdev.migstat.R;
import com.celdev.migstat.ShowStatus;
import com.celdev.migstat.controller.Controller;

/*  This class handles the changing of the background in the ShowStatus activity
*
*   It stores all the available background resource integers in the
*   drawables array and allows the user to step through each of the
*   backgrounds and then "lock" a background using the ok button
* */
public class BackgroundChanger {

    private final ShowStatus activity;
    private final RelativeLayout root;
    private final LinearLayout parent;
    private final Controller controller;

    //the background resource integers
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

    //the current background index (0 = bg_path_wood)
    private static int currentBG = 0;

    public BackgroundChanger(Controller controller,RelativeLayout root, LinearLayout parent, ShowStatus activity) {
        this.root = root;
        this.controller = controller;
        this.activity = activity;
        this.parent = parent;
        initView();
    }

    /*  initialises the "change background"-layout using the layout_change_bg_mode file
    *
    *   the layout contains 3 buttons
    *       <-      shows the previous background
    *       ->      shows the next background
    *       ok      locks the background
    * */
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

    /*  initializes the button functionality
    *
    *   uses old API for low API phones
    * */
    private void initButtons(ImageButton back, ImageButton next, Button ok) {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    root.setBackground(getPrevBackground());
                } else {
                    root.setBackgroundDrawable(getPrevBackground());
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    root.setBackground(getNextBackground());
                }else {
                    root.setBackgroundDrawable(getPrevBackground());
                }
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

    //returns the previous background
    private Drawable getPrevBackground() {
        if (currentBG == 0) {
            currentBG = drawables.length - 1;
        } else {
            currentBG--;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return activity.getDrawable(drawables[currentBG]);
        } else {
            return activity.getResources().getDrawable(drawables[currentBG]);
        }
    }

    //returns the next background
    private Drawable getNextBackground() {
        if (currentBG == drawables.length - 1) {
            currentBG = 0;
        } else {
            currentBG++;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return activity.getDrawable(drawables[currentBG]);
        } else {
            return activity.getResources().getDrawable(drawables[currentBG]);
        }
    }

    //makes the controller save the "locked" background (index)
    private void saveNewBackground() {
        controller.saveBackground(currentBG);
    }

}
