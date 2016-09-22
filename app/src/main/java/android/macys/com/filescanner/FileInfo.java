package android.macys.com.filescanner;

import java.util.Comparator;

public class FileInfo implements Comparable<FileInfo> {

    public String name;
    public double size;
    public String extension;

    public FileInfo(String name, long size, String extension) {
        this.name = name;
        this.size = size;
        this.extension = extension;
    }

    @Override
    public int compareTo(FileInfo fileInfo) {
        double diff = fileInfo.size - size;
        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        }
        return 0;
    }

    public static Comparator<FileInfo> FileInfoComparator = new Comparator<FileInfo>() {
        public int compare(FileInfo file1, FileInfo file2) {
            //descending order
            return file1.compareTo(file2);
        }
    };
}
