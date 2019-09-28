package com.multiple.runtime.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PermissionUtils
{
    private Context context;
    private Activity currentActivity;
    private PermissionResultCallback permissionResultCallback;

    private ArrayList<String> permissionArrayList = new ArrayList<>();
    private ArrayList<String> listPermissionsNeeded=new ArrayList<>();
    private int REQUEST_CODE;
    private String DIALOG_EXPLAIN_CONTENT_FOR_DENIED, DIALOG_EXPLAIN_CONTENT_FOR_NEVER_ASK_AGAIN;

    public PermissionUtils(Context context)
    {
        this.context = context;
        this.currentActivity = (Activity) context;
        this.permissionResultCallback = (PermissionResultCallback) context;
    }

    /**
     * Check the API Level & Permission
     *
     * @param permissionArrayList
     * @param REQUEST_CODE
     */

    public boolean check_permission(ArrayList<String> permissionArrayList, int REQUEST_CODE, String DIALOG_EXPLAIN_CONTENT_FOR_DENIED, String DIALOG_EXPLAIN_CONTENT_FOR_NEVER_ASK_AGAIN)
    {
        this.permissionArrayList = permissionArrayList;
        this.REQUEST_CODE = REQUEST_CODE;
        this.DIALOG_EXPLAIN_CONTENT_FOR_DENIED = DIALOG_EXPLAIN_CONTENT_FOR_DENIED;
        this.DIALOG_EXPLAIN_CONTENT_FOR_NEVER_ASK_AGAIN = DIALOG_EXPLAIN_CONTENT_FOR_NEVER_ASK_AGAIN;
        boolean permissionStatus = false;

        if (checkAndRequestPermissions(permissionArrayList, REQUEST_CODE))
        {
            permissionResultCallback.PermissionGranted(REQUEST_CODE);
            Log.i("all permissions", "granted");
            Log.i("proceed", "to callback");
            permissionStatus = true;
        }
        return  permissionStatus;
    }


    /**
     * Check and request the Permissions
     *
     * @param permissions
     * @param request_code
     * @return
     */
    private  boolean checkAndRequestPermissions(ArrayList<String> permissions,int request_code)
    {
        if(permissions.size()>0)
        {
            listPermissionsNeeded = new ArrayList<>();

            for(int i=0;i<permissions.size();i++)
            {
                int hasPermission = ContextCompat.checkSelfPermission(currentActivity,permissions.get(i));
                if (hasPermission != PackageManager.PERMISSION_GRANTED)
                {
                    listPermissionsNeeded.add(permissions.get(i));
                }
            }
            if (!listPermissionsNeeded.isEmpty())
            {
                ActivityCompat.requestPermissions(currentActivity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),request_code);
                return false;
            }
        }
        return true;
    }

    /**
     *
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
                 if(grantResults.length>0)
                 {
                     Map<String, Integer> perms = new HashMap<>();

                     for (int i = 0; i < permissions.length; i++)
                     {
                         perms.put(permissions[i], grantResults[i]);
                     }

                     final ArrayList<String> pending_permissions=new ArrayList<>();

                     for (int i = 0; i < listPermissionsNeeded.size(); i++)
                     {
                         if (perms.get(listPermissionsNeeded.get(i)) != PackageManager.PERMISSION_GRANTED)
                         {
                            if(ActivityCompat.shouldShowRequestPermissionRationale(currentActivity,listPermissionsNeeded.get(i)))
                            {
                                pending_permissions.add(listPermissionsNeeded.get(i));
                            }
                            else
                            {
                                Log.i("Go to settings","and enable permissions");
                                permissionResultCallback.NeverAskAgain(REQUEST_CODE);
                                dialogWhenNeverAskAgain();
                                return;
                            }
                         }
                     }

                     if(pending_permissions.size()>0)
                     {
                         dialogWhenDenied();
                     }
                     else
                     {
                        Log.i("all","permissions granted");
                        Log.i("proceed","to next step");
                        permissionResultCallback.PermissionGranted(REQUEST_CODE);
                     }
                 }
                 break;
        }
    }

    public void dialogWhenDenied()
    {
        ViewGroup viewGroup = currentActivity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(currentActivity).inflate(R.layout.permission_denied_then_alert_dialog, viewGroup, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setView(dialogView);
        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        TextView content = dialogView.findViewById(R.id.dialog_content);
        content.setText(DIALOG_EXPLAIN_CONTENT_FOR_DENIED);
        Button close = dialogView.findViewById(R.id.close_button);
        Button ok = dialogView.findViewById(R.id.ok_button);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                currentActivity.finish();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                check_permission(permissionArrayList,REQUEST_CODE,DIALOG_EXPLAIN_CONTENT_FOR_DENIED,DIALOG_EXPLAIN_CONTENT_FOR_NEVER_ASK_AGAIN);
            }
        });
    }

    public void dialogWhenNeverAskAgain()
    {
        ViewGroup viewGroup = currentActivity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(currentActivity).inflate(R.layout.permission_never_ask_again_alert_dialog, viewGroup, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setView(dialogView);
        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        TextView content = dialogView.findViewById(R.id.dialog_content);
        content.setText(DIALOG_EXPLAIN_CONTENT_FOR_NEVER_ASK_AGAIN);
        Button close = dialogView.findViewById(R.id.close_button);
        Button settings = dialogView.findViewById(R.id.settings_button);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                currentActivity.finish();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (Build.VERSION.SDK_INT >= 23)
                {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + currentActivity.getPackageName()));
                    currentActivity.startActivity(intent);
                    currentActivity.finish();
                }
            }
        });
    }
}
