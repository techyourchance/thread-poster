package com.techyourchance.threadposters;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SampleActivity extends AppCompatActivity implements FetchDataUseCase.Listener {

    private Button mBtnFetchData;
    private ProgressBar mProgressWorking;
    private TextView mTxtData;
    private TextView mTxtError;

    private FetchDataUseCase mFetchDataUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        mFetchDataUseCase = ((SampleApplication)getApplication()).getFetchDataUseCase();

        mBtnFetchData = (Button) findViewById(R.id.btn_fetch_data);
        mProgressWorking = (ProgressBar) findViewById(R.id.progress_working);
        mTxtData = (TextView) findViewById(R.id.txt_data);
        mTxtError = (TextView) findViewById(R.id.txt_error);

        mBtnFetchData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mFetchDataUseCase.registerListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFetchDataUseCase.unregisterListener(this);
    }

    private void fetchData() {
        showProgressIndicationAndHideOthers();
        mFetchDataUseCase.fetchData();
    }

    @Override
    public void onDataFetched(String data) {
        hideProgressIndication();
        showData(data);
    }

    private void showData(String data) {
        mTxtData.setText(data);
        mTxtData.setVisibility(View.VISIBLE);
        mBtnFetchData.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDataFetchFailed() {
        hideProgressIndication();
        showError();
    }

    private void showError() {
        mTxtError.setVisibility(View.VISIBLE);
        mBtnFetchData.setVisibility(View.VISIBLE);
    }

    private void showProgressIndicationAndHideOthers() {
        mBtnFetchData.setVisibility(View.GONE);
        mTxtData.setVisibility(View.GONE);
        mTxtError.setVisibility(View.GONE);
        mProgressWorking.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndication() {
        mProgressWorking.setVisibility(View.GONE);
    }
}
