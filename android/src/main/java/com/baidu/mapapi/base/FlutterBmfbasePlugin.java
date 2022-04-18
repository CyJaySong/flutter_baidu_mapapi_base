package com.baidu.mapapi.base;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.VersionInfo;
import com.baidu.mapapi.common.BaiduMapSDKException;

/**
 * FlutterBmfbasePlugin
 */
public class FlutterBmfbasePlugin implements FlutterPlugin, MethodCallHandler {

    private static final String METHOD_SET_API_KEY = "flutter_bmfbase/sdk/setApiKey";
    private static final String METHOD_GET_NATIVE_SDK_VERSION = "flutter_bmfbase/sdk/getNativeBaseVersion";
    private static final String METHOD_SET_PRIVACY_API_KEY = "flutter_bmfbase/sdk/setAgreePrivacy";
    private Context applicationContext;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        applicationContext = flutterPluginBinding.getApplicationContext();
        final MethodChannel channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_bmfbase");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals(METHOD_GET_NATIVE_SDK_VERSION)) {
            Map<String, String> versionMap = new HashMap<>();
            versionMap.put("version", VersionInfo.getApiVersion());
            versionMap.put("platform", "Android");
            result.success(versionMap);
        } else if (call.method.equals(METHOD_SET_API_KEY) && call.hasArgument("BMF_COORD_TYPE")) {
            int nCoordType = call.argument("BMF_COORD_TYPE");
            // flutter端的CoordType比android多了一个WGS-84， 因此获取到的nCoordType要减1才能与android端匹配
            nCoordType = nCoordType - 1;
            if (nCoordType >= 0 && CoordType.values().length > nCoordType) {
                CoordType coordType = CoordType.values()[nCoordType];
                SDKInitializer.setCoordType(coordType);
            }
            result.success(null);
        } else if (call.method.equals(METHOD_SET_PRIVACY_API_KEY) && call.hasArgument("isAgree")) {
            boolean isAgree = call.argument("isAgree");
            try {
                SDKInitializer.setAgreePrivacy(applicationContext, isAgree);
                SDKInitializer.initialize(applicationContext);
            } catch (BaiduMapSDKException e) {
                e.getMessage();
            }
            result.success(null);
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        applicationContext = null;
    }
}
