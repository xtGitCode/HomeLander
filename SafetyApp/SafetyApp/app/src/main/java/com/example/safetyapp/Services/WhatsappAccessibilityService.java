package com.example.safetyapp.Services;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.List;

public class WhatsappAccessibilityService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v("YourTag", "Accessibility event received");
        if (getRootInActiveWindow()==null){
            return;
        }

//        getting root node
        AccessibilityNodeInfoCompat rootNodeInfo = AccessibilityNodeInfoCompat.wrap(getRootInActiveWindow());

        List<AccessibilityNodeInfoCompat> messageNodeList = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry");
        if(messageNodeList == null || messageNodeList.isEmpty())
            return;

        AccessibilityNodeInfoCompat messageField = messageNodeList.get(0);
        if(messageField == null || messageField.getText().length() == 0)
            return;

//        whatsapp send message button node list
        List<AccessibilityNodeInfoCompat> sendMessageNodeList = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send");
        if(sendMessageNodeList == null)
            return;

        AccessibilityNodeInfoCompat sendMessage = sendMessageNodeList.get(0);
        if(!sendMessage.isVisibleToUser())
            return;

//        automatically fire send button
        sendMessage.performAction(AccessibilityNodeInfo.ACTION_CLICK);

//        go back to app by clicking back button twice
        try{
            Thread.sleep(2000); //some device can't handle instant back click
            performGlobalAction(GLOBAL_ACTION_BACK);
            Thread.sleep(2000);
        }catch (InterruptedException ignored){}

        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    @Override
    public void onInterrupt() {

    }
}
