package com.meedamian.info;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;

import permissions.dispatcher.PermissionUtils;

public class SimChecker {

    public static final String PERMISSION = Manifest.permission.READ_PHONE_STATE;

    private static final int PHONE_CHANGED_NOTIFICATION_ID      = 1;
    private static final int PERMISSION_MISSING_NOTIFICATION_ID = 2;

    private BasicData bd;

    public SimChecker(Context c) {
        bd = BasicData.getInstance(c);

        if (!PermissionUtils.hasSelfPermissions(c, PERMISSION)) {
            showPermissionNotification(c);
            return;

        } else
            cancelPermissionNotification(c);

        // There's no SIM present - ignore
        String currentSubscriber = getCurrentSubscriberId(c);
        if (currentSubscriber == null)
            return;

        // That's the first read - just save
        String cachedSubscriber = getCachedSubscriberId(c);
        if (cachedSubscriber == null) {
            cacheNewSubscriber(c, currentSubscriber);
            return;
        }

        // SIM changed - notify
        if (!cachedSubscriber.equals(currentSubscriber)) {
            cacheNewSubscriber(c, currentSubscriber);
            showSimChangedNotification(c);
        }
    }

    private String getCachedSubscriberId(Context c) {
        return bd.getString(BasicData.SUBSCRIBER_ID);
    }
    private void cacheNewSubscriber(Context c, String newSubscriberId) {
        bd.cacheString(BasicData.SUBSCRIBER_ID, newSubscriberId);
    }

    private String getCurrentSubscriberId(Context c) {
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }

    private PendingIntent getAppPendingIntent(Context c) {
        return PendingIntent.getActivity(c, 0,
            new Intent(
                c,
                MainActivity.class
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
    private PendingIntent getSettingsPendingIntent(Context c) {
        return PendingIntent.getActivity(c, 0,
            new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", c.getPackageName(), null)
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private NotificationManager getNotificationManager(Context c) {
        return (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void showSimChangedNotification(Context c) {
        String phoneNo = bd.getString(BasicData.PHONE);

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(c)
                .setSmallIcon(R.drawable.ic_sim_card_black_24dp)
                .setContentTitle("SIM card changed")
                .setContentText(String.format("Is %s your current phone number?", phoneNo))
                .addAction(R.drawable.ic_edit_black_24dp, "Change", getAppPendingIntent(c))
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setContentIntent(getAppPendingIntent(c));

        getNotificationManager(c).notify(PHONE_CHANGED_NOTIFICATION_ID, mBuilder.build());
    }

    private void showPermissionNotification(Context c) {
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(c)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setSmallIcon(R.drawable.ic_sim_card_black_24dp)
                .setContentTitle("Permission missing")
                .setContentText("Phone number change notifications disabled")
                .addAction(R.drawable.ic_lightbulb_outline_24dp, "Grant", getSettingsPendingIntent(c))
                .setContentIntent(getAppPendingIntent(c));

        getNotificationManager(c).notify(PERMISSION_MISSING_NOTIFICATION_ID, mBuilder.build());
    }
    private void cancelPermissionNotification(Context c) {
        getNotificationManager(c).cancel(PERMISSION_MISSING_NOTIFICATION_ID);
    }
}
