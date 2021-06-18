package org.bd.activitydemo;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import androidx.work.ListenableWorker;
import android.util.Log;
import com.google.common.util.concurrent.ListenableFuture;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer;

public class SampleWorker extends ListenableWorker implements Runnable {
    Thread pythonThread = null;
    Completer workCompleter = null;

    public SampleWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            workCompleter = completer;
            pythonThread = new Thread(this);
            pythonThread.start();
            Log.d("SampleWorker", "thread started");
            pythonThread.join();
            Log.d("SampleWorker", "thread finished");
            return "SampleWorker started";
        });
    }

    @Override
    public void run() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Log.d("SampleWorker", "interruped: " + e);
        }
        Log.d("SampleWorker", "HOHOHO");
        workCompleter.set(Result.success());
    }
}
