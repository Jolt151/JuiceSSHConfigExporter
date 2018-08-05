package jolt151.juicesshconfigexporter;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sonelli.juicessh.pluginlibrary.PluginContract;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    String address;
    String port;
    String name;
    String nickname;

    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, "com.sonelli.juicessh.api.v1.permission.READ_CONNECTIONS"};


    String body = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button3 = (Button) findViewById(R.id.button3);

        final List<String> list = new ArrayList<>();
        list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        list.add("com.sonelli.juicessh.api.v1.permission.READ_CONNECTIONS");

        getPermissions(1);


        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, "com.sonelli.juicessh.api.v1.permission.READ_CONNECTIONS"};
                if(EasyPermissions.hasPermissions(MainActivity.this, perms)) {


                    ContentResolver resolver = getContentResolver();
                    String[] projection = new String[]{PluginContract.Connections.COLUMN_ID, PluginContract.Connections.COLUMN_NAME,
                            PluginContract.Connections.COLUMN_ADDRESS, PluginContract.Connections.COLUMN_PORT};
                    if (isAppInstalled(getApplicationContext(), "com.sonelli.juicessh")) {
                        final Cursor cursor =
                                resolver.query(Uri.parse("content://com.sonelli.juicessh.api.v1/connections"),
                                        PluginContract.Connections.PROJECTION,
                                        null,
                                        null,
                                        null);

                        //Log.d(" "+ cursor.getCount(), "message");
                        final int indexAddress = cursor.getColumnIndex(PluginContract.Connections.COLUMN_ADDRESS);
                        final int indexPort = cursor.getColumnIndex(PluginContract.Connections.COLUMN_PORT);
                        final int indexName = cursor.getColumnIndex(PluginContract.Connections.COLUMN_NAME);
                        final int indexNickname = cursor.getColumnIndex(PluginContract.Connections.COLUMN_NICKNAME);
                        if (cursor != null) {
                            cursor.moveToNext();
                            while (cursor.getPosition() < cursor.getCount() - 1) {
                                address = cursor.getString(indexAddress);
                                port = cursor.getString(indexPort);
                                name = cursor.getString(indexName);
                                nickname = cursor.getString(indexNickname);
                                cursor.moveToNext();

                                body += "Host " + name + "\n    " + "Hostname " + address + "\n    " + "Port " + port + "\n\n";

                            }
                        }
                        export(getApplicationContext(), "config", body);
                        body = "";
                    } else {
                        Toast.makeText(getApplicationContext(), "JuiceSSH not installed!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Error: You must grant permissions!", Toast.LENGTH_SHORT).show();
                    if (EasyPermissions.somePermissionPermanentlyDenied(MainActivity.this, list)) {
                        new AppSettingsDialog.Builder(MainActivity.this).build().show();
                    }
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("JuiceSSHConFigExporter", "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            //Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT)
              //      .show();
        }
    }

    public void getPermissions(int requestCode){
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, "com.sonelli.juicessh.api.v1.permission.READ_CONNECTIONS"};
        if(EasyPermissions.hasPermissions(this, perms)){

        }
        else{
            EasyPermissions.requestPermissions(this, "We need to be able to read your connections as well as write your files to " +
                    "the external storage. The app will not work without both of these permissions.", requestCode, perms);
        }
    }


    public void export(Context context, String sFileName, String sBody) {
        try {

            File root = new File(Environment.getExternalStorageDirectory(), "JuiceSSH Exports");

            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            gpxfile.delete();
            FileWriter writer = new FileWriter(gpxfile, false);
            writer.write(sBody);
            writer.flush();
            writer.close();


            Toast.makeText(context, "Saved to Internal Storage/JuiceSSH Exports/config", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
