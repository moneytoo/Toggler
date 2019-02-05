package com.brouken.wear.toggler;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class TogglerAccessibilityService extends AccessibilityService {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private View mCoverView;

    public static Boolean running = false;

    public static Boolean scroll = false;
    public static Boolean toggle = false;
    public static Boolean confirm = false;
    public static Boolean back = false;

    private static Boolean lastSwitchChecked;

    void log(final String text) {
        if (text != null)
            Log.d("Toggler", text);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        running = true;

        initCover();
        //coverUp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        running = false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        if (scroll || toggle || confirm || back) {

            coverUp();

            //log("onAccessibilityEvent");
            //log(accessibilityEvent.toString());

            if (accessibilityEvent.getSource() == null)
                return;

            final AccessibilityNodeInfo nodeInfo = getTopmostParent(accessibilityEvent.getSource());

            if (scroll) {
                scrollDown(nodeInfo);
            } else if (toggle) {
                goThroughHierarchy(accessibilityEvent.getSource());
            } else if (confirm) {
                if (accessibilityEvent.getClassName().toString().equals("android.support.wearable.view.AcceptDenyDialog")) {
                    clickButton("android:id/button1");
                    confirm = false;
                    back = true;
                }
            } else if (back) {
                if (accessibilityEvent.getClassName().toString().equals("android.widget.ListView")) {
                    back = false;
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    delayedCoverDown();
                }
            }

        }
    }

    void clickButton(final String viewId) {
        final List<AccessibilityNodeInfo> nodeInfos = getRootInActiveWindow().findAccessibilityNodeInfosByViewId(viewId);
        for (AccessibilityNodeInfo nodeInfo : nodeInfos)
            nodeInfo.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.getId());
    }

    private AccessibilityNodeInfo getTopmostParent(final AccessibilityNodeInfo node) {
        final AccessibilityNodeInfo parent = node.getParent();

        if (parent == null)
            return node;
        else
            return getTopmostParent(parent);
    }

    private String getTitle(final AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null)
            return null;

        final int count = nodeInfo.getChildCount();

        for (int i = 0; i < count; i++) {
            final AccessibilityNodeInfo child = nodeInfo.getChild(i);

            if (child == null)
                continue;

            if (child.getClassName().toString().equals("android.widget.TextView")) {
                return child.getText().toString();
            }

            String title = getTitle(child);
            if (title != null)
                return title;

        }

        return null;
    }

    private void scrollDown(final AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null)
            return;

        final int count = nodeInfo.getChildCount();

        for (int i = 0; i < count; i++) {
            final AccessibilityNodeInfo child = nodeInfo.getChild(i);

            if (child == null)
                continue;

            if (child.isScrollable()) {
                child.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                scroll = false;
                return;
            }

            scrollDown(child);
        }
    }

    private Boolean isParentSwitchUnchecked(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null)
            return false;

        final AccessibilityNodeInfo parent = nodeInfo.getParent();

        final int count = nodeInfo.getChildCount();

        for (int i = 0; i < count; i++) {
            final AccessibilityNodeInfo child = nodeInfo.getChild(i);
            //log(child.getClassName().toString());

            if (child.getClassName().toString().equals("android.widget.Switch")) {
                return !child.isChecked();
            }
        }

        return isParentSwitchUnchecked(parent);
    }

    private void goThroughHierarchy(final AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null)
            return;

        if (nodeInfo.getClassName().toString().equals("android.widget.Switch")) {
            lastSwitchChecked = nodeInfo.isChecked();
            //log("checked=" + lastSwitchChecked);
        }


        final int count = nodeInfo.getChildCount();

        for (int i = 0; i < count; i++) {
            final AccessibilityNodeInfo child = nodeInfo.getChild(i);

            if (child == null)
                continue;

            goThroughHierarchy(child);

            final CharSequence sequence = child.getText();
            if (sequence != null) {
                final String text = sequence.toString();

                if (text.equals(getString(R.string.pref_tiltToWake))) {
                    clickClickableParent(child);
                    toggle = false;
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    delayedCoverDown();
                } else if (text.equals(getString(R.string.pref_alwaysOnScreen))) {

                    clickClickableParent(child);
                    toggle = false;

                    if (lastSwitchChecked) {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        delayedCoverDown();
                    } else
                        confirm = true;
                }

            }
        }
    }

    private void clickClickableParent(final AccessibilityNodeInfo nodeInfo) {
        final AccessibilityNodeInfo parent = nodeInfo.getParent();

        if (parent == null)
            return;

        if (parent.isClickable())
            parent.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.getId());
        else
            clickClickableParent(parent);
    }

    @Override
    public void onInterrupt() {

    }

    //// Cover up

    private void initCover() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mCoverView = new View(this);

        mCoverView.setBackgroundColor(Color.argb(0xff, 0x00, 0x00, 0x00));

        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE),
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
    }

    private void coverUp() {
        try {
            if (mCoverView.getParent() == null)
                mWindowManager.addView(mCoverView, mParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void coverDown() {
        if (mWindowManager != null) {
            if (mCoverView != null && mCoverView.getParent() != null)
                mWindowManager.removeView(mCoverView);
        }
    }

    private void delayedCoverDown() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                coverDown();
            }
        }, 600);
    }

    private int getOverlayWidth() {
        final int EDGE_SIZE = 20; // dp
        final float density = getResources().getDisplayMetrics().density;
        return (int) (EDGE_SIZE * density + 0.5f);
    }

    //// Debug

    void dumpChildren(final AccessibilityNodeInfo nodeInfo) {
        dumpChildren(nodeInfo, 0);
    }

    void dumpChildren(final AccessibilityNodeInfo nodeInfo, int in) {
        if (nodeInfo == null)
            return;

        //log(indent(in) + nodeInfo.toString());

        final int count = nodeInfo.getChildCount();

        for (int i = 0; i < count; i++) {
            final AccessibilityNodeInfo child = nodeInfo.getChild(i);

            if (child == null)
                continue;

            in = in + 1;

            dumpChildren(child, in);
        }
    }

    String indent(int num) {
        String text = "";
        for(int i = 0; i < num; i++)
        {
            text += " ";
        }
        return text;
    }
}
