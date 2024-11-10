package com.dsi.easy_audience_network;

import android.app.Activity;
import android.content.Context;
import com.facebook.ads.*;
import androidx.annotation.NonNull;
import java.util.HashMap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;

/**
 * FacebookAudienceNetworkPlugin
 */
public class FacebookAudienceNetworkPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private MethodChannel channel, interstitialAdChannel, rewardedAdChannel;
    private Activity _activity;
    private Context _context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), FacebookConstants.MAIN_CHANNEL);
        channel.setMethodCallHandler(this);
        _context = flutterPluginBinding.getApplicationContext();

        // Interstitial Ad channel
        interstitialAdChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(),
                FacebookConstants.INTERSTITIAL_AD_CHANNEL);
        interstitialAdChannel
                .setMethodCallHandler(new FacebookInterstitialAdPlugin(_context,
                        interstitialAdChannel));

        // Rewarded video Ad channel
        rewardedAdChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(),
                FacebookConstants.REWARDED_VIDEO_CHANNEL);
        rewardedAdChannel
                .setMethodCallHandler(new FacebookRewardedVideoAdPlugin(_context,
                        rewardedAdChannel));
        flutterPluginBinding.
                getPlatformViewRegistry().
                registerViewFactory(FacebookConstants.BANNER_AD_CHANNEL,
                        new FacebookBannerAdPlugin(flutterPluginBinding.getBinaryMessenger()));
        flutterPluginBinding.
                getPlatformViewRegistry().
                registerViewFactory(FacebookConstants.NATIVE_AD_CHANNEL,
                        new FacebookNativeAdPlugin(flutterPluginBinding.getBinaryMessenger()));
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        if (call.method.equals(FacebookConstants.INIT_METHOD))
            init((HashMap) call.arguments, result);
        else
            result.notImplemented();
    }

    private void init(HashMap initValues, final MethodChannel.Result result) {
        final String testingId = (String) initValues.get("testingId");
        final Boolean testMode = (Boolean) initValues.get("testMode");

        if (testingId != null) {
            AdSettings.addTestDevice(testingId);
        }
        if (testMode) {
            AdSettings.setTestMode(true);
        }
        AudienceNetworkAds
                .buildInitSettings(_activity.getApplicationContext())
                .withInitListener(new  AudienceNetworkAds.InitListener() {
                    @Override
                    public void onInitialized(AudienceNetworkAds.InitResult initResult) {
                        result.success(initResult.isSuccess());
                    }
                })
                .initialize();
    }
    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        interstitialAdChannel.setMethodCallHandler(null);
        rewardedAdChannel.setMethodCallHandler(null);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        _activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
    }
    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

}

