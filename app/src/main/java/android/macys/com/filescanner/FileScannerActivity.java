package android.macys.com.filescanner;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class FileScannerActivity extends AppCompatActivity {

    private FileScannerFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_scanner);

        FragmentManager manager = getFragmentManager();
        mFragment = (FileScannerFragment) manager.findFragmentByTag(FileScannerFragment.FRAGMENT_TAG);
        if (mFragment == null) {
            mFragment = FileScannerFragment.newInstance();
            manager.beginTransaction().add(mFragment, FileScannerFragment.FRAGMENT_TAG).commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Cancel the task when back button is pressed
        mFragment.stopProgress();
    }
}
