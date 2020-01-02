package com.pheuture.playlists.datasource.remote;


import android.os.Handler;
import android.os.Looper;

import com.pheuture.playlists.utils.Logger;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {
    private static final String TAG = ProgressRequestBody.class.getSimpleName();
    private File mFile;
    private UploadCallbacks mListener;
    private MediaType mContentType;
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    public ProgressRequestBody(final File file, MediaType contentType, final UploadCallbacks listener) {
        mContentType = contentType;
        mFile = file;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return mContentType;
    }

    @Override
    public long contentLength() {
        return mFile.length();
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        try (FileInputStream in = new FileInputStream(mFile)) {
            long uploaded = 0;
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                handler.post(new ProgressUpdater(uploaded, fileLength));

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploadedInBytes;
        private long mTotalInBytes;

        ProgressUpdater(long uploaded, long total) {
            mUploadedInBytes = uploaded;
            mTotalInBytes = total;
        }

        @Override
        public void run() {
            Logger.e(TAG, "uploaded:" + mUploadedInBytes + "total:" + mTotalInBytes);
            if (mListener!=null) {
                mListener.onProgressUpdate(mUploadedInBytes, mTotalInBytes);
            }
        }
    }

    public interface UploadCallbacks {
        void onProgressUpdate(long uploadedInBytes, long totalInBytes);
    }

}


