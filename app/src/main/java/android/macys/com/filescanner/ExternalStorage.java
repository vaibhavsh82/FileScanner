package android.macys.com.filescanner;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Try to get the sd storage location for all devices above 4.0
 */
public class ExternalStorage {

    public static final String EXTERNAL_SD_CARD = "/mnt/sdcard";
    private static final String TAG = "FileScannerApp";
    private static final String EMULATED = "emulated";

    /**
     * @return True if the external storage is available. False otherwise.
     */
    public static boolean isAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static String getExternalStoragePath(Context context) {
        if (!isAvailable()) {
            Log.i(TAG, "Can't read the storage. Maybe check manifest permission or grant storage permission");
            return null;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            File[] downloadLocation = context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES);
            StringBuilder builder = new StringBuilder();
            for (File f : downloadLocation) {
                String filePath = f.getAbsolutePath();
                if (!filePath.contains(EMULATED)) {
                    Log.i(TAG, "File size: " + filePath + Environment.isExternalStorageEmulated());
                    String[] list = filePath.split("/");
                    builder.append("/").append(list[1]).append("/").append(list[2]);
                    Log.i(TAG, "Storage: " + builder.toString());
                }
            }
            if (!TextUtils.isEmpty(builder.toString())) {
                return builder.toString();
            }
        }

        // Before Kitkat Android doesn't provide any API to get the external sd card path if it
        // is being used as a media card and not as internal storage. Hardcoding the sdcard
        // mount but this could be different for different manufacturers and might fail.
        // Reference Android Framework Engineer response:
        // https://groups.google.com/forum/#!topic/android-platform/14VUiIgwUjY%5B1-25%5D
        File externalFile = new File(EXTERNAL_SD_CARD);
        if (externalFile.exists()) {
            return EXTERNAL_SD_CARD;
        }
        // Fall back to default external storage provided by Android.
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
}
