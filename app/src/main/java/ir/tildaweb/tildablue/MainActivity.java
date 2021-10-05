package ir.tildaweb.tildablue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            req();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            req();
        }
    }

    private void req() {
        TildaBlueUtils tildaBlueUtils = new TildaBlueUtils(this);
        Log.d(TAG, "onCreate: " + tildaBlueUtils.isEnable());
        tildaBlueUtils.enable();
        Log.d(TAG, "onCreate: " + tildaBlueUtils.isEnable());
        Log.d(TAG, "onCreate: " + tildaBlueUtils.getBondedDevices().size());

        tildaBlueUtils.search(7, new OnSearchDeviceListener() {
            @Override
            public void onSearchStart() {
                Log.d(TAG, "onSearchStart:");
            }

            @Override
            public void onSearchSearch() {
                Log.d(TAG, "onSearchSearch:");
            }

            @Override
            public void onSearchFinish(List<BlueDevice> blueDevices) {
                Log.d(TAG, "onSearchFinish: ---> " + blueDevices.size());
                for (BlueDevice blueDevice : blueDevices) {
                    Log.d(TAG, "onSearchFinish: " + blueDevice.getName());
                    tildaBlueUtils.connect(blueDevice.getMacAddress(), new OnMessageReceiveListener() {
                        @Override
                        public void onMessageReceived(String message) {
                            Log.d(TAG, "onMessageReceived: " + message);
                        }
                    });

                    tildaBlueUtils.sendData("a");

                    break;
                }
            }
        });
    }
}