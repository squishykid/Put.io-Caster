/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
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

package com.google.sample.cast.refplayer.utils;

import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.VideoBrowserActivity;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A collection of utility methods, all static.
 */
public class Utils {


    public final static String baseUrl = "https://api.put.io/v2/";

    /*
     * Making sure public utility methods remain static
     */
    private Utils() {
    }

    @SuppressWarnings("deprecation")
    /**
     * Returns the screen/display size
     *
     * @param ctx
     * @return
     */
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        return new Point(width, height);
    }

    /**
     * Shows an error dialog with a given text message.
     *
     * @param context
     * @param errorString
     */
    public static final void showErrorDialog(Context context, String errorString) {
        new AlertDialog.Builder(context).setTitle(R.string.error)
                .setMessage(errorString)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    /**
     * Shows an error dialog with a text provided by a resource ID
     *
     * @param context
     * @param resourceId
     */
    public static final void showErrorDialog(Context context, int resourceId) {
        new AlertDialog.Builder(context).setTitle(R.string.error)
                .setMessage(context.getString(resourceId))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    /**
     * Shows an "Oops" error dialog with a text provided by a resource ID
     *
     * @param context
     * @param resourceId
     */
    public static final void showOopsDialog(Context context, int resourceId) {
        new AlertDialog.Builder(context).setTitle(R.string.oops)
                .setMessage(context.getString(resourceId))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setIcon(R.drawable.ic_action_alerts_and_states_warning)
                .create()
                .show();
    }

    /**
     * A utility method to handle a few types of exceptions that are commonly thrown by the cast
     * APIs in this library. It has special treatments for
     * {@link TransientNetworkDisconnectionException}, {@link NoConnectionException} and shows an
     * "Oops" dialog conveying certain messages to the user. The following resource IDs can be used
     * to control the messages that are shown:
     * <p>
     * <ul>
     * <li><code>R.string.connection_lost_retry</code></li>
     * <li><code>R.string.connection_lost</code></li>
     * <li><code>R.string.failed_to_perfrom_action</code></li>
     * </ul>
     *
     * @param context
     * @param e
     */
    public static void handleException(Context context, Exception e) {
        int resourceId = 0;
        if (e instanceof TransientNetworkDisconnectionException) {
            // temporary loss of connectivity
            resourceId = R.string.connection_lost_retry;

        } else if (e instanceof NoConnectionException) {
            // connection gone
            resourceId = R.string.connection_lost;
        } else if (e instanceof RuntimeException ||
                e instanceof IOException ||
                e instanceof CastException) {
            // something more serious happened
            resourceId = R.string.failed_to_perfrom_action;
        } else {
            // well, who knows!
            resourceId = R.string.failed_to_perfrom_action;
        }
        if (resourceId > 0) {
            com.google.sample.cast.refplayer.utils.Utils.showOopsDialog(context, resourceId);
        }
    }

    /**
     * Gets the version of app.
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        String versionString = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0 /* basic info */);
            versionString = info.versionName;
        } catch (Exception e) {
            // do nothing
        }
        return versionString;
    }

    /**
     * Shows a (long) toast
     *
     * @param context
     * @param msg
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a (long) toast.
     *
     * @param context
     * @param resourceId
     */
    public static void showToast(Context context, int resourceId) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }

    /**
     * Converts an InputStream to a String
     *
     * @param inputStream
     * @return convertedString
     */

    public static String convertStreamToString(InputStream inputStream) {
        try {
            return new java.util.Scanner(inputStream).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    public static void addTransfersAsync(final Context context, final Intent retryIntent,
                                         final String urls, final String tokenWithStuff) {
        class AddTransfersTask extends AsyncTask<Void, Void, Void> {
            NotificationManager notifManager;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notifStart();
            }

            protected Void doInBackground(Void... nothing) {
                URL url = null;


                try {
                    url = new URL(baseUrl + "transfers/add" + tokenWithStuff);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                try {
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setDoOutput(true);

                    OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
                    output.write("url=" + urls);
                    output.flush();
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        notifSucceeded();
                    } else {
                        notifFailed();
                    }
                } catch (IOException e) {
                    notifFailed();
                    e.printStackTrace();
                }


                return null;
            }

            private void notifStart() {
                NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context);
                notifBuilder.setOngoing(true);
                notifBuilder.setContentTitle("Adding torrent");
                notifBuilder.setSmallIcon(R.drawable.ic_notificon_transfer);
                notifBuilder.setTicker("Adding torrent...");
                notifBuilder.setProgress(1, 0, true);
                Notification notif = notifBuilder.build();
                notif.ledARGB = Color.parseColor("#FFFFFF00");
                try {
                    notifManager.notify(1, notif);
                } catch (IllegalArgumentException e) {
                    notifBuilder.setContentIntent(PendingIntent.getActivity(
                            context, 0, new Intent(context, VideoBrowserActivity.class), 0));
                    notif = notifBuilder.build();
                    notif.ledARGB = Color.parseColor("#FFFFFF00");
                    notifManager.notify(1, notif);
                }
            }

            private void notifSucceeded() {
                NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context);
                notifBuilder.setOngoing(false);
                notifBuilder.setAutoCancel(true);
                notifBuilder.setContentTitle("Added torrent");
                notifBuilder.setContentText("Processing it now.");
                notifBuilder.setSmallIcon(R.drawable.ic_notificon_transfer);
                notifBuilder.setContentIntent(PendingIntent.getActivity(
                        context, 0, new Intent(context, VideoBrowserActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK));
//				notifBuilder.addAction(R.drawable.ic_notif_watch, "Watch", null);
                notifBuilder.setTicker("Added torrent");
                notifBuilder.setProgress(0, 0, false);
                Notification notif = notifBuilder.build();
                notif.ledARGB = Color.parseColor("#FFFFFF00");
                notifManager.notify(1, notif);
            }

            private void notifFailed() {
                NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context);
                notifBuilder.setOngoing(false);
                notifBuilder.setAutoCancel(true);
                notifBuilder.setContentTitle("Couldn't add torrent");
                notifBuilder.setContentText("Try again?");
                notifBuilder.setSmallIcon(R.drawable.ic_notificon_transfer);
                PendingIntent retryNotifIntent = PendingIntent.getActivity(
                        context, 0, retryIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
                //TODO notifBuilder.addAction(R.drawable.ic_notif_retry, "Retry", retryNotifIntent);
                notifBuilder.setContentIntent(retryNotifIntent);
                notifBuilder.setTicker("Couldn't add torrent");
                Notification notif = notifBuilder.build();
                notif.ledARGB = Color.parseColor("#FFFFFF00");
                notifManager.notify(1, notif);
            }
        }
        new AddTransfersTask().execute();
    }

}
