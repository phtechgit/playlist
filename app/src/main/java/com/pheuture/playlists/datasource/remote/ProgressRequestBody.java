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
        int lastProgress = 0;

        try (FileInputStream in = new FileInputStream(mFile)) {
            long uploaded = 0;
            int read;
            Handler handler = new Handler(Looper.getMainLooper());

            while ((read = in.read(buffer)) != -1) {
                uploaded += read;
                sink.write(buffer, 0, read);

                int progress = (int) ((uploaded*100)/fileLength);
                if (progress!=lastProgress){
                    lastProgress = progress;
                    // update progress on UI thread
                    handler.post(new ProgressUpdater(uploaded));
                }
            }
        }
    }

    private class ProgressUpdater implements Runnable {
        private int percentageCompleted;
        private long mUploadedInBytes;
        private long mTotalInBytes;

        ProgressUpdater(long uploaded) {
            mUploadedInBytes = uploaded;
        }

        @Override
        public void run() {
            if (mListener!=null) {
                mListener.onProgressUpdate(mUploadedInBytes);
            }
        }
    }

    public interface UploadCallbacks {
        void onProgressUpdate(long uploadedInBytes);
    }

}


