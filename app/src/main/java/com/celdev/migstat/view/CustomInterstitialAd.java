package com.celdev.migstat.view;


import android.content.Context;
import android.util.Log;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.R;
import com.celdev.migstat.controller.Controller;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class CustomInterstitialAd {

    private Context context;
    private InterstitialAd interstitialAd;
    private Controller controller;

    public CustomInterstitialAd(Controller controller, Context context) {
        this.context = context;
        this.controller = controller;
        this.interstitialAd = new InterstitialAd(context);
        initAd();
    }

    private void initAd() {
        interstitialAd.setAdUnitId(context.getString(R.string.ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.d(MainActivity.LOG_KEY, "ad onClose called saving enable themes");
                controller.saveEnableThemes();
                super.onAdClosed();
            }
        });
        AdRequest adRequest = new AdRequest.Builder().
                addTestDevice(AdRequest.DEVICE_ID_EMULATOR).
                build();
        interstitialAd.loadAd(adRequest);
    }



    public void show() {
        if (interstitialAd.isLoaded()) {
            try {
                interstitialAd.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
