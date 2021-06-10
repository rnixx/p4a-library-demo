package org.bd.activitydemo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker.Result;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class SampleWorker extends Worker {

    public SampleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        Log.d("Python Worker", "I am the god of hell fire. And it bring you FIRE");

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }
}