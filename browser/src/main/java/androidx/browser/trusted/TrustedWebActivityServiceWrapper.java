/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.browser.trusted;

import android.app.Notification;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.customtabs.trusted.ITrustedWebActivityService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

/**
 * TrustedWebActivityServiceWrapper is used by a Trusted Web Activity provider to wrap calls to
 * the {@link TrustedWebActivityService} in the client app.
 * All of these calls except {@link #getComponentName()} forward over IPC
 * to corresponding calls on {@link TrustedWebActivityService}, eg {@link #getSmallIconId()}
 * forwards to {@link TrustedWebActivityService#getSmallIconId()}.
 * <p>
 * These IPC calls are synchronous, though the {@link TrustedWebActivityService} method may hit the
 * disk. Therefore it is recommended to call them on a background thread (without StrictMode).
 */
public class TrustedWebActivityServiceWrapper {
    // Inputs.
    private static final String KEY_PLATFORM_TAG =
            "android.support.customtabs.trusted.PLATFORM_TAG";
    private static final String KEY_PLATFORM_ID =
            "android.support.customtabs.trusted.PLATFORM_ID";
    private static final String KEY_NOTIFICATION =
            "android.support.customtabs.trusted.NOTIFICATION";
    private static final String KEY_CHANNEL_NAME =
            "android.support.customtabs.trusted.CHANNEL_NAME";
    private static final String KEY_ACTIVE_NOTIFICATIONS =
            "android.support.customtabs.trusted.ACTIVE_NOTIFICATIONS";

    // Outputs.
    private static final String KEY_NOTIFICATION_SUCCESS =
            "android.support.customtabs.trusted.NOTIFICATION_SUCCESS";

    private static final String REMOTE_EXCEPTION_MESSAGE = "RemoteException while trying to "
            + "communicate with the TrustedWebActivityService, this is probably because the "
            + "service died while attempting to respond. Check to see if the service crashed for "
            + "some reason.";

    private final ITrustedWebActivityService mService;
    private final ComponentName mComponentName;

    TrustedWebActivityServiceWrapper(@NonNull ITrustedWebActivityService service,
            @NonNull ComponentName componentName) {
        mService = service;
        mComponentName = componentName;
    }

    /**
     * Checks whether notifications are enabled.
     * @param channelName The name of the channel to check enabled status. Only used on Android O+.
     * @return Whether notifications or the notification channel is blocked for the client app.
     */
    public boolean areNotificationsEnabled(@NonNull String channelName) {
        try {
            Bundle args = new NotificationsEnabledArgs(channelName).toBundle();
            return ResultArgs.fromBundle(mService.areNotificationsEnabled(args)).success;
        } catch (RemoteException e) {
            throw new RuntimeException("Failure when connecting to TrustedWebActivityService", e);
        }
    }

    /**
     * Requests a notification be shown.
     * @param platformTag The tag to identify the notification.
     * @param platformId The id to identify the notification.
     * @param notification The notification.
     * @param channel The name of the channel in the Trusted Web Activity client app to display the
     *                notification on.
     * @return Whether notifications or the notification channel are blocked for the client app.
     * @throws SecurityException If verification with the TrustedWebActivityService fails.
     */
    public boolean notify(@NonNull String platformTag, int platformId,
            @NonNull Notification notification, @NonNull String channel) {
        try {
            Bundle args = new NotifyNotificationArgs(platformTag, platformId, notification, channel)
                    .toBundle();
            return ResultArgs.fromBundle(mService.notifyNotificationWithChannel(args)).success;
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Requests a notification be cancelled.
     * @param platformTag The tag to identify the notification.
     * @param platformId The id to identify the notification.
     * @throws SecurityException If verification with the TrustedWebActivityService fails.
     */
    public void cancel(@NonNull String platformTag, int platformId) {
        try {
            Bundle args = new CancelNotificationArgs(platformTag, platformId).toBundle();
            mService.cancelNotification(args);
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Gets the notifications shown by the Trusted Web Activity client. Can only be called on
     * Android M and above.
     * @return An StatusBarNotification[] as a Parcelable[]. This is so this code can compile for
     *         Jellybean (even if it must not be called for pre-Marshmallow).
     * @throws SecurityException If verification with the TrustedWebActivityService fails.
     * @throws IllegalStateException If called on Android pre-M.
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @RequiresApi(Build.VERSION_CODES.M)
    public Parcelable[] getActiveNotifications() {
        try {
            Bundle notifications = mService.getActiveNotifications();
            return ActiveNotificationsArgs.fromBundle(notifications).notifications;
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Requests an Android resource id to be used for the notification small icon.
     * @return An Android resource id for the notification small icon. -1 if non found.
     * @throws SecurityException If verification with the TrustedWebActivityService fails.
     */
    public int getSmallIconId() {
        try {
            return mService.getSmallIconId();
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Requests a bitmap of a small icon to be used for the notification
     * small icon. The bitmap is decoded on the side of Trusted Web Activity client using
     * the resource id from {@link TrustedWebActivityService#getSmallIconId}.
     * @return A {@link Bitmap} to be used for the small icon.
     * @throws SecurityException If verification with the TrustedWebActivityService fails.
     */
    @Nullable
    public Bitmap getSmallIconBitmap() {
        try {
            return mService.getSmallIconBitmap()
                    .getParcelable(TrustedWebActivityService.KEY_SMALL_ICON_BITMAP);
        } catch (RemoteException e) {
            throw new RuntimeException(REMOTE_EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Gets the {@link ComponentName} of the connected Trusted Web Activity client app.
     * @return The Trusted Web Activity client app component name.
     */
    @NonNull
    public ComponentName getComponentName() {
        return mComponentName;
    }

    static class NotifyNotificationArgs {
        public final String platformTag;
        public final int platformId;
        public final Notification notification;
        public final String channelName;

        NotifyNotificationArgs(String platformTag, int platformId,
                Notification notification, String channelName) {
            this.platformTag = platformTag;
            this.platformId = platformId;
            this.notification = notification;
            this.channelName = channelName;
        }

        public static NotifyNotificationArgs fromBundle(Bundle bundle) {
            ensureBundleContains(bundle, KEY_PLATFORM_TAG);
            ensureBundleContains(bundle, KEY_PLATFORM_ID);
            ensureBundleContains(bundle, KEY_NOTIFICATION);
            ensureBundleContains(bundle, KEY_CHANNEL_NAME);

            return new NotifyNotificationArgs(bundle.getString(KEY_PLATFORM_TAG),
                    bundle.getInt(KEY_PLATFORM_ID),
                    (Notification) bundle.getParcelable(KEY_NOTIFICATION),
                    bundle.getString(KEY_CHANNEL_NAME));
        }

        public Bundle toBundle() {
            Bundle args = new Bundle();
            args.putString(KEY_PLATFORM_TAG, platformTag);
            args.putInt(KEY_PLATFORM_ID, platformId);
            args.putParcelable(KEY_NOTIFICATION, notification);
            args.putString(KEY_CHANNEL_NAME, channelName);
            return args;
        }
    }

    static class CancelNotificationArgs {
        public final String platformTag;
        public final int platformId;

        CancelNotificationArgs(String platformTag, int platformId) {
            this.platformTag = platformTag;
            this.platformId = platformId;
        }

        public static CancelNotificationArgs fromBundle(Bundle bundle) {
            ensureBundleContains(bundle, KEY_PLATFORM_TAG);
            ensureBundleContains(bundle, KEY_PLATFORM_ID);

            return new CancelNotificationArgs(bundle.getString(KEY_PLATFORM_TAG),
                    bundle.getInt(KEY_PLATFORM_ID));
        }

        public Bundle toBundle() {
            Bundle args = new Bundle();
            args.putString(KEY_PLATFORM_TAG, platformTag);
            args.putInt(KEY_PLATFORM_ID, platformId);
            return args;
        }
    }

    static class ResultArgs {
        public final boolean success;

        ResultArgs(boolean success) {
            this.success = success;
        }

        public static ResultArgs fromBundle(Bundle bundle) {
            ensureBundleContains(bundle, KEY_NOTIFICATION_SUCCESS);
            return new ResultArgs(bundle.getBoolean(KEY_NOTIFICATION_SUCCESS));
        }

        public Bundle toBundle() {
            Bundle args = new Bundle();
            args.putBoolean(KEY_NOTIFICATION_SUCCESS, success);
            return args;
        }
    }

    static class ActiveNotificationsArgs {
        public final Parcelable[] notifications;

        ActiveNotificationsArgs(Parcelable[] notifications) {
            this.notifications = notifications;
        }

        public static ActiveNotificationsArgs fromBundle(Bundle bundle) {
            ensureBundleContains(bundle, KEY_ACTIVE_NOTIFICATIONS);
            return new ActiveNotificationsArgs(bundle.getParcelableArray(KEY_ACTIVE_NOTIFICATIONS));
        }

        public Bundle toBundle() {
            Bundle args = new Bundle();
            args.putParcelableArray(KEY_ACTIVE_NOTIFICATIONS, notifications);
            return args;
        }
    }

    static class NotificationsEnabledArgs {
        public final String channelName;

        NotificationsEnabledArgs(String channelName) {
            this.channelName = channelName;
        }

        public static NotificationsEnabledArgs fromBundle(Bundle bundle) {
            ensureBundleContains(bundle, KEY_CHANNEL_NAME);
            return new NotificationsEnabledArgs(bundle.getString(KEY_CHANNEL_NAME));
        }

        public Bundle toBundle() {
            Bundle args = new Bundle();
            args.putString(KEY_CHANNEL_NAME, channelName);
            return args;
        }
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static void ensureBundleContains(Bundle args, String key) {
        if (args.containsKey(key)) return;
        throw new IllegalArgumentException("Bundle must contain " + key);
    }
}
