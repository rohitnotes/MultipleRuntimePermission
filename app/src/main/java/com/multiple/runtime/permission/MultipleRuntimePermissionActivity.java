package com.multiple.runtime.permission;

import android.Manifest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.util.ArrayList;

public class MultipleRuntimePermissionActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,PermissionResultCallback{

    private static final String TAG = MultipleRuntimePermissionActivity.class.getSimpleName();
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private PermissionUtils permissionUtils;
    private Button buttonSingleRuntimePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_runtime_permission);
        initView();
        initObject();
        initEvent();
    }

    private void initView() {
        buttonSingleRuntimePermission = findViewById(R.id.multiple_runtime_permission);
    }

    private void initObject() {

        permissionUtils = new PermissionUtils(MultipleRuntimePermissionActivity.this);
    }

    private boolean permission()
    {
        ArrayList<String> permissionsArrayList=new ArrayList<>();
        String DENIED = "Kindly allow Permission, without these permission you could not access this app.";
        String NEVER_ASK_AGAIN = "Kindly allow Permission from Settings, without these permission you could not access this app";

        //********* Add required permissions to the permission request array **********
        permissionsArrayList.add(Manifest.permission.READ_CONTACTS);
        permissionsArrayList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionsArrayList.add(Manifest.permission.CALL_PHONE);
        permissionsArrayList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsArrayList.add(Manifest.permission.CAMERA);
        permissionsArrayList.add(Manifest.permission.RECORD_AUDIO);

        return permissionUtils.check_permission(permissionsArrayList, REQUEST_ID_MULTIPLE_PERMISSIONS, DENIED, NEVER_ASK_AGAIN);
    }

    private void initEvent() {

        buttonSingleRuntimePermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Marshmallow +
                 * Check whether the app is installed on Android 6.0 or higher.
                 * Check if the build version is greater than or equal to 23.
                 */
                if (Build.VERSION.SDK_INT >= 23)
                {
                    if(permission())
                    {
                        Log.e(TAG, "Permission Granted");
                        Intent intent = new Intent(MultipleRuntimePermissionActivity.this, PermissionGrantedActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                else
                {
                    /*
                     * Pre-Marshmallow
                     * If build version is less than or 23, then all permission is
                     * granted at install time in google play store.
                     */
                    Intent intent = new Intent(MultipleRuntimePermissionActivity.this, PermissionGrantedActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    //*************** Permission part start****************
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtils.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void PermissionGranted(int request_code) {
        Log.i("PERMISSION","GRANTED");
        Intent intent = new Intent(MultipleRuntimePermissionActivity.this, PermissionGrantedActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY","GRANTED");
    }

    @Override
    public void PermissionDenied(int request_code) {
        Log.i("PERMISSION","DENIED");
    }

    @Override
    public void NeverAskAgain(int request_code) {
        Log.i("PERMISSION","NEVER ASK AGAIN");
    }
    //*************** Permission part end ****************
}
