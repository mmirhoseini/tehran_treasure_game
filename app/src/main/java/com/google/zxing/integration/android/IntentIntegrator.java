/*
 * Copyright 2009 ZXing authors
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

package com.google.zxing.integration.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import com.tehran.treasure.DownloadScanner;
import com.tehran.treasure.R;
import com.tehran.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IntentIntegrator {
    public static final int REQUEST_CODE = 0x0000c0de; // Only use bottom 16
    // supported barcode formats
    // public static final Collection<String> PRODUCT_CODE_TYPES = list("UPC_A",
    // "UPC_E", "EAN_8", "EAN_13", "RSS_14");
    // public static final Collection<String> ONE_D_CODE_TYPES =
    // list("UPC_A", "UPC_E", "EAN_8", "EAN_13", "CODE_39", "CODE_93",
    // "CODE_128",
    // "ITF", "RSS_14", "RSS_EXPANDED");
    public static final Collection<String> QR_CODE_TYPES = Collections
            .singleton("QR_CODE");
    // bits

    // private static final String tag = IntentIntegrator.class.getSimpleName();
    public static final Collection<String> ALL_CODE_TYPES = null;
    private static final String BS_PACKAGE = "com.google.zxing.client.android";
    public static final List<String> TARGET_BARCODE_SCANNER_ONLY = Collections
            .singletonList(BS_PACKAGE);
    // public static final Collection<String> DATA_MATRIX_TYPES =
    // Collections.singleton("DATA_MATRIX");
    private static final String BSPLUS_PACKAGE = "com.srowen.bs.android";
    public static final List<String> TARGET_ALL_KNOWN = list(BS_PACKAGE, // Barcode
            // Scanner
            BSPLUS_PACKAGE, // Barcode Scanner+
            BSPLUS_PACKAGE + ".simple" // Barcode Scanner+ Simple
            // What else supports this intent?
    );
    private final Activity activity;
    private final Map<String, Object> moreExtras;
    private Context context;
    private String title;
    private String message;
    private String buttonYes;
    private String buttonNo;
    private List<String> targetApplications;

    public IntentIntegrator(Activity activity, String title, String message,
                            String buttonYes, String buttonNo) {

        this.activity = activity;
        this.context = (Context) activity;
        this.title = title;
        this.message = message;
        this.buttonYes = buttonYes;
        this.buttonNo = buttonNo;
        targetApplications = TARGET_ALL_KNOWN;
        moreExtras = new HashMap<String, Object>(3);
    }

    /**
     * <p>
     * Call this from your {@link Activity}'s
     * {@link Activity#onActivityResult(int, int, Intent)} method.
     * </p>
     *
     * @return null if the event handled here was not related to this class, or
     * else an {@link IntentResult} containing the result of the scan.
     * If the user cancelled scanning, the fields will be null.
     */
    public static IntentResult parseActivityResult(int requestCode,
                                                   int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
                byte[] rawBytes = intent.getByteArrayExtra("SCAN_RESULT_BYTES");
                int intentOrientation = intent.getIntExtra(
                        "SCAN_RESULT_ORIENTATION", Integer.MIN_VALUE);
                Integer orientation = intentOrientation == Integer.MIN_VALUE ? null
                        : intentOrientation;
                String errorCorrectionLevel = intent
                        .getStringExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL");
                return new IntentResult(contents, formatName, rawBytes,
                        orientation, errorCorrectionLevel);
            }
            return new IntentResult();
        }
        return null;
    }

    private static List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleByID(int titleID) {
        title = activity.getString(titleID);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageByID(int messageID) {
        message = activity.getString(messageID);
    }

    public String getButtonYes() {
        return buttonYes;
    }

    public void setButtonYes(String buttonYes) {
        this.buttonYes = buttonYes;
    }

    public void setButtonYesByID(int buttonYesID) {
        buttonYes = activity.getString(buttonYesID);
    }

    public String getButtonNo() {
        return buttonNo;
    }

    public void setButtonNo(String buttonNo) {
        this.buttonNo = buttonNo;
    }

    public void setButtonNoByID(int buttonNoID) {
        buttonNo = activity.getString(buttonNoID);
    }

    public Collection<String> getTargetApplications() {
        return targetApplications;
    }

    public void setTargetApplications(List<String> targetApplications) {
        if (targetApplications.isEmpty()) {
            throw new IllegalArgumentException("No target applications");
        }
        this.targetApplications = targetApplications;
    }

    /**
     * @deprecated call {@link #setTargetApplications(List)}
     */
    @Deprecated
    public void setTargetApplications(Collection<String> targetApplications) {
        List<String> list = new ArrayList<String>(targetApplications.size());
        for (String app : targetApplications) {
            list.add(app);
        }
        setTargetApplications(list);
    }

    public void setSingleTargetApplication(String targetApplication) {
        this.targetApplications = Collections.singletonList(targetApplication);
    }

    public Map<String, ?> getMoreExtras() {
        return moreExtras;
    }

    public void addExtra(String key, Object value) {
        moreExtras.put(key, value);
    }

    /**
     * Initiates a scan for all known barcode types.
     */
    public AlertDialog initiateScan() {
        return initiateScan(ALL_CODE_TYPES);
    }

    public AlertDialog initiateScan(Collection<String> desiredBarcodeFormats) {
        Intent intentScan = new Intent(BS_PACKAGE + ".SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);

        // check which types of codes to scan for
        if (desiredBarcodeFormats != null) {
            // set the desired barcode types
            StringBuilder joinedByComma = new StringBuilder();
            for (String format : desiredBarcodeFormats) {
                if (joinedByComma.length() > 0) {
                    joinedByComma.append(',');
                }
                joinedByComma.append(format);
            }
            intentScan.putExtra("SCAN_FORMATS", joinedByComma.toString());
        }

        String targetAppPackage = findTargetAppPackage(intentScan);
        if (targetAppPackage == null) {
            return showDownloadDialog();
        }
        intentScan.setPackage(targetAppPackage);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        attachMoreExtras(intentScan);
        startActivityForResult(intentScan, REQUEST_CODE);
        return null;
    }

    /**
     * Start an activity.<br>
     * This method is defined to allow different methods of activity starting
     * for newer versions of Android and for compatibility library.
     *
     * @param intent Intent to start.
     * @param code   Request code for the activity
     * @see android.app.Activity#startActivityForResult(Intent, int)
     * @see android.app.Fragment#startActivityForResult(Intent, int)
     */
    protected void startActivityForResult(Intent intent, int code) {
        activity.startActivityForResult(intent, code);
    }

    private String findTargetAppPackage(Intent intent) {
        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> availableApps = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (availableApps != null) {
            for (ResolveInfo availableApp : availableApps) {
                String packageName = availableApp.activityInfo.packageName;
                if (targetApplications.contains(packageName)) {
                    return packageName;
                }
            }
        }
        return null;
    }

    private AlertDialog showDownloadDialog() {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // String packageName = targetApplications.get(0);
                        // Uri uri = Uri.parse("market://details?id="
                        // + packageName);
                        // Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        // try {
                        // activity.startActivity(intent);
                        // } catch (ActivityNotFoundException anfe) {
                        // // Hmm, market is not installed
                        // Log.w(TAG,
                        // "Google Play is not installed; cannot install "
                        // + packageName);
                        // }

                        DownloadScanner dScanner = new DownloadScanner(context);
                        dScanner.execute(context.getResources().getString(
                                R.string.download_scanner_link));
                    }
                });
        downloadDialog.setNegativeButton(buttonNo,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utils.exit(activity);
                    }
                });
        downloadDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                Utils.exit(activity);
            }
        });
        return downloadDialog.show();
    }

    /**
     * Defaults to type "TEXT_TYPE".
     *
     * @see #shareText(CharSequence, CharSequence)
     */
    public AlertDialog shareText(CharSequence text) {
        return shareText(text, "TEXT_TYPE");
    }

    /**
     * Shares the given text by encoding it as a barcode, such that another user
     * can scan the text off the screen of the device.
     *
     * @param text the text string to encode as a barcode
     * @param type type of data to encode. See
     *             {@code com.google.zxing.client.android.Contents.Type}
     *             constants.
     * @return the {@link AlertDialog} that was shown to the user prompting them
     * to download the app if a prompt was needed, or null otherwise
     */
    public AlertDialog shareText(CharSequence text, CharSequence type) {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(BS_PACKAGE + ".ENCODE");
        intent.putExtra("ENCODE_TYPE", type);
        intent.putExtra("ENCODE_DATA", text);
        String targetAppPackage = findTargetAppPackage(intent);
        if (targetAppPackage == null) {
            return showDownloadDialog();
        }
        intent.setPackage(targetAppPackage);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        attachMoreExtras(intent);
        activity.startActivity(intent);
        return null;
    }

    private void attachMoreExtras(Intent intent) {
        for (Map.Entry<String, Object> entry : moreExtras.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // Kind of hacky
            if (value instanceof Integer) {
                intent.putExtra(key, (Integer) value);
            } else if (value instanceof Long) {
                intent.putExtra(key, (Long) value);
            } else if (value instanceof Boolean) {
                intent.putExtra(key, (Boolean) value);
            } else if (value instanceof Double) {
                intent.putExtra(key, (Double) value);
            } else if (value instanceof Float) {
                intent.putExtra(key, (Float) value);
            } else if (value instanceof Bundle) {
                intent.putExtra(key, (Bundle) value);
            } else {
                intent.putExtra(key, value.toString());
            }
        }
    }

}