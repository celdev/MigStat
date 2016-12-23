package com.celdev.migstat.view;


import android.content.Context;
import android.util.Log;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.R;
import com.celdev.migstat.controller.Controller;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/*  This class contains methods for loading and showing
*   an interstitial ad.
*
*   when the ad is closes the background-changing functionality will be unlocked
* */
public class CustomInterstitialAd {

    private Context context;
    private InterstitialAd interstitialAd;
    private Controller controller;

    /*  initializes the ad
    * */
    public CustomInterstitialAd(Controller controller, Context context) {
        this.context = context;
        this.controller = controller;
        this.interstitialAd = new InterstitialAd(context);
        initAd();
    }

    /*  initializes the ad functionality
    *   when the "onClose"-method is called (when the ad is closed)
    *   the background-changing-functionality will be unlocked
    *   and a new ad will be loaded into the interstitial ad
    * */
    private void initAd() {
        interstitialAd.setAdUnitId(context.getString(R.string.ad_unit_id_interstitial_ad));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.d(MainActivity.LOG_KEY, "ad onClose called saving enable themes");
                controller.saveEnableThemes();
				doAdRequest();
                super.onAdClosed();
            }
        });
		doAdRequest();
    }

    /*  requests a new interstitial ad
    * */
	private void doAdRequest(){
        AdRequest adRequest = new AdRequest.Builder().
                build();
        interstitialAd.loadAd(adRequest);
	}

    /*  if the ad is loaded the ad will be shown
    *   otherwise the "change background"-functionality will be unlocked
    *   without showing an ad
    * */
    public void show() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            controller.saveEnableThemes();
        }
    }

}
