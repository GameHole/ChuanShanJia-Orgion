package com.bytedance.android;

import com.bytedance.sdk.openadsdk.TTCustomController;

/**
 * created by jijiachun on 2021/10/26
 */
public class CustomController extends TTCustomController {
    private String macAddress;
    private boolean isCanUseWifiState = true;

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void canUseWifiState(boolean canUseWifiState) {
        isCanUseWifiState = canUseWifiState;
    }

    @Override
    public boolean isCanUseWifiState() {
        return isCanUseWifiState;
    }

    @Override
    public String getMacAddress() {
        return macAddress != null ? macAddress : super.getMacAddress();
    }
}
