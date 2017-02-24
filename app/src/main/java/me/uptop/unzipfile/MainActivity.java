package me.uptop.unzipfile;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    String mainZipFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkPermissionsAndRequestIfNotGranted(new String[]{READ_EXTERNAL_STORAGE}, 3000)) takeZipFromGallery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onActivityResults(requestCode, resultCode, data);
    }

    private void onActivityResults(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String photoUrl = data.getData().toString();
                updateZip(photoUrl);
            }
        }
    }

    private void updateZip(String path) {
        String newPath = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            newPath = LocalStorageZip.getPath(getApplicationContext(), Uri.parse(path));
        }
        mainZipFile = newPath.substring(29);
        newPath = newPath.replace(mainZipFile,"");
        final Decompress d = new Decompress(newPath+mainZipFile, newPath+"unzipped/app/");
        Runnable myRunnable = new Runnable(){
            public void run() {
                d.unzip();
            }
        };
        Thread thread = new Thread(myRunnable);
        thread.start();
    }

    private void takeZipFromGallery() {
        Intent intent = new Intent();
        intent.setType("application/zip");
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        startActivityForResult(intent, 1001);
    }

    public boolean checkPermissionsAndRequestIfNotGranted(@NonNull String[] permissions, int requestCode) {
        boolean allGranted = true;
        for (String permission : permissions) {
            int selfPermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (selfPermission != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, requestCode);
            }
            return false;
        }
        return allGranted;
    }
}
