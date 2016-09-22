package android.macys.com.filescanner;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class FileStats {

    public FileStats() {
        fileInfoList = new ArrayList<>();
    }

    /**
     * List of top 10 bug files
     */
    public List<FileInfo> fileInfoList;
    /**
     * Frequency of extensions
     */
    public List<Pair<String, Integer>> extFrequencies;
    public long averageFileSize;
    public long medianFileSize;
}
