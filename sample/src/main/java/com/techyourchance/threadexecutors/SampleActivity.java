package com.techyourchance.threadexecutors;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.techyourchance.threadexecutor.UiThreadExecutor;
import com.techyourchance.threadposter.BackgroundThreadExecutor;

public class SampleActivity extends AppCompatActivity implements SampleWorker.SampleWorkerListener {

    private Button mBtnStart;
    private ProgressBar mProgressWorking;
    private TextView mTxtDone;

    /*
    IMPORTANT:
    Both BackgroundThreadExecutor and UiThreadExecutor should be global objects. Can be easily
    achieved using dependency injection framework that supports global objects ("singletons")
     */
    private final BackgroundThreadExecutor mBackgroundThreadExecutor = new BackgroundThreadExecutor();
    private final UiThreadExecutor mUiThreadExecutor = new UiThreadExecutor();

    private final SampleDataRetriever mSampleDataRetriever = new SampleDataRetriever();


    private SampleWorker mSampleWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        mBtnStart = (Button) findViewById(R.id.btn_start);
        mProgressWorking = (ProgressBar) findViewById(R.id.progress_working);
        mTxtDone = (TextView) findViewById(R.id.txt_done);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doWork();
            }
        });

        mSampleWorker = new SampleWorker(mSampleDataRetriever, mBackgroundThreadExecutor, mUiThreadExecutor);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSampleWorker.registerListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSampleWorker.unregisterListener(this);
    }

    private void doWork() {
        mBtnStart.setVisibility(View.GONE);
        mProgressWorking.setVisibility(View.VISIBLE);
        mSampleWorker.doWork();
    }

    @Override
    public void onWorkDone(String data) {
        mProgressWorking.setVisibility(View.GONE);
        mTxtDone.setVisibility(View.VISIBLE);
    }
}
