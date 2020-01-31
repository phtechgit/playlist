package com.pheuture.playlists;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

// ResultType: Type for the Resource data.
// RequestType: Type for the API response.
public abstract class NetworkBoundResource<ResultType, RequestType> {

    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestType item);

    @MainThread
    protected abstract boolean shouldFetch(@Nullable ResultType data);

    @NonNull
    @MainThread
    protected abstract LiveData<ResultType> loadFromDb();

    /*@NonNull @MainThread
    protected abstract LiveData<ApiResponse<RequestType>> createCall();*/
}
