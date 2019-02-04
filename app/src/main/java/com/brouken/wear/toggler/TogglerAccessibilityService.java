package com.brouken.wear.toggler;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TogglerAccessibilityService extends AccessibilityService {
    public static Boolean run = false;

    void log(final String text) {
        if (text != null)
            Log.d("Toggler", text);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        if (!run)
            return;

        log("onAccessibilityEvent");
        log(accessibilityEvent.toString());

        if (accessibilityEvent.getSource() == null)
            return;

        run = false;

        final AccessibilityNodeInfo nodeInfo = getTopmostParent(accessibilityEvent.getSource());

        //log(getTitle(nodeInfo));
        scrollDown(nodeInfo);


        //accessibilityEvent.setScrollY(400);


        dumpChildren(accessibilityEvent.getSource());


        //goThroughHierarchy(accessibilityEvent.getSource());
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
                log("will scroll");
                child.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                log("scrolled");
                return;
            }

            scrollDown(child);
        }
    }

    private void goThroughHierarchy(final AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null)
            return;

        final int count = nodeInfo.getChildCount();

        for (int i = 0; i < count; i++) {
            final AccessibilityNodeInfo child = nodeInfo.getChild(i);

            if (child == null)
                continue;

            goThroughHierarchy(child);

            final CharSequence sequence = child.getText();
            if (sequence != null) {
                final String text = sequence.toString();

                if (text.equals("Tilt-to-wake")) {
                    clickClickableParent(child);
                    run = false;
                    performGlobalAction(GLOBAL_ACTION_BACK);
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

    //// Debug

    void dumpChildren(final AccessibilityNodeInfo nodeInfo) {
        dumpChildren(nodeInfo, 0);
    }

    void dumpChildren(final AccessibilityNodeInfo nodeInfo, int in) {
        if (nodeInfo == null)
            return;

        log(indent(in) + nodeInfo.toString());

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
