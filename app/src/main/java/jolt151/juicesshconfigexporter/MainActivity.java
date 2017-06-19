package jolt151.juicesshconfigexporter;

import android.content.ContentResolver;
import android.content.Context;
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

public class MainActivity extends AppCompatActivity {

    String address;
    String port;
    String name;
    String nickname;

    String body = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button3 = (Button) findViewById(R.id.button3);


        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                ContentResolver resolver = getContentResolver();
                String[] projection = new String[]{PluginContract.Connections.COLUMN_ID, PluginContract.Connections.COLUMN_NAME,
                        PluginContract.Connections.COLUMN_ADDRESS, PluginContract.Connections.COLUMN_PORT};
                if (isAppInstalled(getApplicationContext(),"com.sonelli.juicessh")) {
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
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"JuiceSSH not installed!", Toast.LENGTH_SHORT).show();
                }
            }
        });


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


            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
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
