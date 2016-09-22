package android.macys.com.filescanner;

import android.Manifest;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.id;

/**
 * Fragment which is responsible for main scanning part.
 */
public class FileScannerFragment extends Fragment {

    public static final String FRAGMENT_TAG = "sd_card_scanner_fragment";
    private static final String TAG = "FileScannerApp";
    private static final String LOCATION_KEY = "SD_card_location";
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 352;
    private static final String PROGRESS_PENDING_KEY = "progress_pending";
    private static final String AVERAGE_SIZE_KEY = "average_file_size";

    private String mSdCardLocation;
    private FileScannerViewAdapter mAdapter;
    private FloatingActionButton mStartButton;
    private ProgressDialog mProgressDialog;
    private AsyncTask<String, Integer, FileStats> mTask;
    private FileStats mFileStats;
    private boolean mIsProgressPending;
    private NotificationManager mNotifyManager;
    private TextView mAverageTextView;
    private TextView mAverageHeaderView;
    private FloatingActionButton mShareButton;

    public static FileScannerFragment newInstance() {
        return new FileScannerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate all the views.
        View rootView = inflater.inflate(R.layout.fragment_file_scanner, container, false);
        mAverageTextView = (TextView) rootView.findViewById(R.id.average_size_text);
        mAverageHeaderView = (TextView) rootView.findViewById(R.id.average_size_header);
        mStartButton = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mShareButton = (FloatingActionButton) rootView.findViewById(R.id.share);
        mNotifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        mAdapter = new FileScannerViewAdapter();
        mProgressDialog = new ProgressDialog(inflater.getContext());

        //Retain this fragment for configuration change
        setRetainInstance(true);
        recyclerView.setAdapter(mAdapter);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireShareIntent();
            }
        });

        // Try to restore the views if activity is being recreated
        if (savedInstanceState != null) {
            mSdCardLocation = savedInstanceState.getString(LOCATION_KEY);
            mIsProgressPending = savedInstanceState.getBoolean(PROGRESS_PENDING_KEY, false);
            long averageSize = savedInstanceState.getLong(AVERAGE_SIZE_KEY);
            if (averageSize != 0) {
                showAverageUI(averageSize);
            }
        }
        if (mFileStats == null) {
            mFileStats = new FileStats();
        }
        mAdapter.setData(mFileStats);

        if (mIsProgressPending) {
            showProgressDialog();
        }

        //Button to start the scan
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestPermission()) {
                    startScan(view.getContext());
                }
            }
        });
        return rootView;
    }

    /**
     * Fetch the sdcard location and start the scan if location valid.
     * @param context
     */
    private void startScan(Context context) {
        mSdCardLocation = ExternalStorage.getExternalStoragePath(context);
        if (mSdCardLocation != null) {
            executeScanTask(mSdCardLocation);
            mShareButton.setVisibility(View.GONE);
            showNotification();
            showProgressDialog();
            Log.i(TAG, "Scanning location: " + mSdCardLocation);
            Snackbar.make(mStartButton, "Scanning the SD card", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            Snackbar.make(mStartButton, "Couldn't find any SD card", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    /**
     * Show notification to notification bar while scanning is active.
     */
    private void showNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());
        mBuilder.setContentTitle("SD Card Scanner")
                .setContentText("Scanning in progress")
                .setSmallIcon(android.R.drawable.ic_media_play);
        mBuilder.setProgress(0, 0, true);
        Intent resultIntent = new Intent(getActivity(), FileScannerActivity.class);
        //Pending intent so that when user click on notification, it takes them back to the app.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(getActivity(), 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        // Issues the notification
        mNotifyManager.notify(id, mBuilder.build());
    }

    /**
     * Cancel the task and clear up the state if needed.
     */
    public void stopProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mIsProgressPending = false;
        }
        if (mTask != null) {
            mTask.cancel(true);
        }
        if (mNotifyManager != null) {
            mNotifyManager.cancel(id);
        }
    }

    public void updateViews(FileStats fileStats) {
        mFileStats = fileStats;
        showAverageUI(mFileStats.averageFileSize);
        mAdapter.setData(fileStats);
        stopProgress();
    }

    private void showAverageUI(long average) {
        mAverageTextView.setText("" + average);
        mAverageHeaderView.setVisibility(View.VISIBLE);
        mShareButton.setVisibility(View.VISIBLE);
    }

    // Call to fire the share intent
    private void fireShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Scan Stats");
        intent.putExtra(Intent.EXTRA_TEXT,"Average File Size: " + mFileStats.averageFileSize);
        startActivity(Intent.createChooser(intent, "Share File Stats"));
    }

    private void executeScanTask(String sdCardLocation) {
        mTask = new AsyncTask<String, Integer, FileStats>() {
            FileStats mFileStats;
            private double totalSpaceOccupied;
            private Map<String, Integer> extFrequencyMap;

            private FileStats startScanner(String sdCardLocation) {
                mFileStats = new FileStats();
                if (!TextUtils.isEmpty(sdCardLocation)) {
                    extFrequencyMap = new HashMap<>();
                    totalSpaceOccupied = 0;
                    scanRecursive(sdCardLocation);
                    if (!mFileStats.fileInfoList.isEmpty() && !isCancelled()) {
                        //TODO: Can use custom sorting algo to make it run faster.
                        Collections.sort(mFileStats.fileInfoList, FileInfo.FileInfoComparator);
                        int totalNumFiles = mFileStats.fileInfoList.size();
                        mFileStats.averageFileSize = (long) totalSpaceOccupied / totalNumFiles;
                        mFileStats.extFrequencies = ScanUtil.sortByValue(extFrequencyMap);
                        //just get the top 10 files.
                        int endIdx = mFileStats.fileInfoList.size() > 10 ? 10 : mFileStats.fileInfoList.size();
                        mFileStats.fileInfoList = mFileStats.fileInfoList.subList(0, endIdx);
                    }
                }
                return mFileStats;
            }

            public void scanRecursive(String path) {
                final File root = new File(path);
                if (!isCancelled() && root.listFiles() != null && root.listFiles().length > 0) {
                    for (File f : root.listFiles()) {
                        if (f.isFile()) {
                            // Store in KB
                            long sizeInKB = f.length() / 1024;
                            totalSpaceOccupied += sizeInKB;
                            String extension = ScanUtil.getFileExtension(f.getName());
                            if (!extension.isEmpty()) {
                                if (extFrequencyMap.containsKey(extension)) {
                                    int freq = extFrequencyMap.get(extension);
                                    extFrequencyMap.remove(extension);
                                    ++freq;
                                    extFrequencyMap.put(extension, freq);
                                } else {
                                    extFrequencyMap.put(extension, 1);
                                }
                            }
                            mProgressDialog.incrementProgressBy(1);
                            mFileStats.fileInfoList.add(new FileInfo(f.getName(), sizeInKB, extension));
                        } else {
                            scanRecursive(f.getAbsolutePath());
                        }
                    }
                }
            }

            @Override
            protected void onPostExecute(FileStats fileStats) {
                super.onPostExecute(fileStats);
                updateViews(fileStats);
            }

            @Override
            protected FileStats doInBackground(String... params) {
                String sdCardLocation = params[0];
                return startScanner(sdCardLocation);
            }
        };

        //start the file scanning task in background thread.
        mTask.execute(sdCardLocation);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(mSdCardLocation)) {
            //save all the states for configuration change
            outState.putString(LOCATION_KEY, mSdCardLocation);
            outState.putBoolean(PROGRESS_PENDING_KEY, mIsProgressPending);
            outState.putLong(AVERAGE_SIZE_KEY, mFileStats.averageFileSize);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Show the progress dialog while task is active
     */
    private void showProgressDialog() {
        mProgressDialog.setTitle("Scanning SD Card");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mAdapter.clearItems();
                        stopProgress();
                    }
                });
        mProgressDialog.show();
        mIsProgressPending = true;
    }

    /**
     * Request the permission for storage if the device os is Lollipop and above
     * @return
     */
    private boolean requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_STORAGE);
            }
            return true;
        } else {
            Log.i(TAG, "Permission already granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Try to start scanning files
                    startScan(getActivity());
                } else {
                    Toast.makeText(getActivity(), "Permission Denied! Can't scan SD card.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}

