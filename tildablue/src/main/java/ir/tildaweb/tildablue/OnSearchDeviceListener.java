package ir.tildaweb.tildablue;

import java.util.List;

public interface OnSearchDeviceListener {

    void onSearchStart();

    void onSearchSearch();

    void onSearchFinish(List<BlueDevice> blueDevices);
}
