package com.flipkart.chatheads.config;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import com.flipkart.chatheads.utils.ChatHeadUtils;

/**
 * Created by kiran.kumar on 06/05/15.
 */
public class ChatHeadDefaultConfig extends ChatHeadConfig {
    public ChatHeadDefaultConfig(Context context) {
        int diameter = 100;
        Log.v(getClass().getSimpleName(), "ChatHeadDefaultConfig setConfig");

        setHeadHeight(ChatHeadUtils.dpToPx(context,diameter));
        setHeadWidth(ChatHeadUtils.dpToPx(context, diameter));
        setHeadHorizontalSpacing(ChatHeadUtils.dpToPx(context, 10));
        setHeadVerticalSpacing(ChatHeadUtils.dpToPx(context, 5));
        setInitialPosition(new Point(10,ChatHeadUtils.dpToPx(context,10)));
        setCloseButtonHidden(false);
        setCloseButtonWidth(ChatHeadUtils.dpToPx(context, 62));
        setCloseButtonHeight(ChatHeadUtils.dpToPx(context, 62));
        setCloseButtonBottomMargin(ChatHeadUtils.dpToPx(context, 50));
        setCircularRingWidth(ChatHeadUtils.dpToPx(context,diameter+5));
        setCircularRingHeight(ChatHeadUtils.dpToPx(context,diameter+5));
        setMaxChatHeads(5);
    }

    @Override
    public int getCircularFanOutRadius(int maxWidth, int maxHeight) {
        return (int) (maxWidth/2.5f);
    }
}
