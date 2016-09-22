package android.macys.com.filescanner;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FileScannerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FREQ_HEADER = 0;
    private static final int TYPE_FREQ_ITEM = 1;
    private static final int TYPE_BIG_FILE_HEADER = 2;
    private static final int TYPE_BIG_FILE_ITEM = 3;
    private static final int FREQ_HEADER_COUNT = 1;
    private static final int BIG_HEADER_COUNT = 1;

    List<FileInfo> mItems = new ArrayList<>();
    FileStats mFileStats;

    public void setData(FileStats stats) {
        if (stats != null) {
            mFileStats = stats;
            if (stats.fileInfoList != null && !stats.fileInfoList.isEmpty()) {
                mItems.addAll(stats.fileInfoList);
            }
            this.notifyDataSetChanged();
        }
    }

    /**
     * Clear the adapter and show no views
     */
    public void clearItems() {
        mItems.clear();
        mFileStats = null;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case TYPE_FREQ_HEADER:
            case TYPE_BIG_FILE_HEADER:
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false);
                viewHolder = new ViewHolderHeader(headerView);
                break;
            case TYPE_FREQ_ITEM:
            case TYPE_BIG_FILE_ITEM:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_view, parent, false);
                viewHolder = new ViewHolderItem(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_FREQ_HEADER:
                ViewHolderHeader headerItemHolder = (ViewHolderHeader) holder;
                headerItemHolder.header.setText("Extension Frequency");
                break;
            case TYPE_BIG_FILE_HEADER:
                ViewHolderHeader headerFileItemHolder = (ViewHolderHeader) holder;
                headerFileItemHolder.header.setText("10 Biggest Files");
                break;
            case TYPE_FREQ_ITEM:
                ViewHolderItem itemHolder = (ViewHolderItem) holder;
                int freqItemPosition = position - FREQ_HEADER_COUNT;
                itemHolder.fileNameView.setText(mFileStats.extFrequencies.get(freqItemPosition).first);
                itemHolder.sizeView.setText(String.valueOf(mFileStats.extFrequencies.get(freqItemPosition).second));
                break;
            case TYPE_BIG_FILE_ITEM:
                ViewHolderItem fileItemHolder = (ViewHolderItem) holder;
                int freqFileCount = mFileStats.extFrequencies != null ?
                        mFileStats.extFrequencies.size() + FREQ_HEADER_COUNT : 0;
                int bigItemPosition = position - BIG_HEADER_COUNT - freqFileCount;
                fileItemHolder.fileNameView.setText(mItems.get(bigItemPosition).name);
                fileItemHolder.sizeView.setText(String.valueOf(mItems.get(bigItemPosition).size));
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (TYPE_FREQ_HEADER == position) {
            return TYPE_FREQ_HEADER;
        } else if (mFileStats.extFrequencies != null &&
                position < mFileStats.extFrequencies.size() + FREQ_HEADER_COUNT) {
            return TYPE_FREQ_ITEM;
        } else if (mFileStats.extFrequencies != null &&
                position == mFileStats.extFrequencies.size() + FREQ_HEADER_COUNT) {
            return TYPE_BIG_FILE_HEADER;
        } else if (mFileStats.extFrequencies != null &&
                position > mFileStats.extFrequencies.size() + FREQ_HEADER_COUNT) {
            return TYPE_BIG_FILE_ITEM;
        }
        return 6;
    }

    @Override
    public int getItemCount() {
        int freqFileCount = mFileStats != null && mFileStats.extFrequencies != null ?
                mFileStats.extFrequencies.size() + FREQ_HEADER_COUNT : 0;
        int bigFileCount = !mItems.isEmpty() ? mItems.size() +BIG_HEADER_COUNT : 0;
        int totalItemCount = freqFileCount + bigFileCount;
        return totalItemCount;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolderItem extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView fileNameView;
        public TextView sizeView;
        public ViewHolderItem(View view) {
            super(view);
            fileNameView = (TextView) view.findViewById(R.id.fileNameTV);
            sizeView = (TextView) view.findViewById(R.id.file_size_tv);
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView header;
        public ViewHolderHeader(View view) {
            super(view);
            header = (TextView) view.findViewById(R.id.header);
        }
    }
}
