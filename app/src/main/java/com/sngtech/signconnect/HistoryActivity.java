package com.sngtech.signconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sngtech.signconnect.databinding.ActivityHistoryBinding;
import com.sngtech.signconnect.recyclerViews.HistoryItem;
import com.sngtech.signconnect.recyclerViews.HistoryRecyclerViewAdapter;
import com.sngtech.signconnect.recyclerViews.HistoryRecyclerViewListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements HistoryRecyclerViewListener {

    public static List<HistoryItem> historyItemList = new ArrayList<>();

    ActivityHistoryBinding binding;
    HistoryRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //populateHistoryList();

        RecyclerView recyclerView = binding.historyRecyclerView;
        adapter = new HistoryRecyclerViewAdapter(getApplicationContext(), historyItemList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

//    private void populateHistoryList() {
//        String[] resultsArr = getResources().getStringArray(R.array.history_results_array);
//        String[] datetimeArr = getResources().getStringArray(R.array.history_datetime_array);
//
//        historyItemList.clear();
//        for(int i = 0; i < resultsArr.length; i++) {
//            HistoryItem item = new HistoryItem(resultsArr[i], datetimeArr[i], HistoryItem.SignType.WORD, null);
//            historyItemList.add(item);
//        }
//    }

    @Override
    public void onItemClick(int pos) {
        Intent newIntent = new Intent(getApplicationContext(), SignDetailsActivity.class);
        Bundle detailsBundle = new Bundle();
        detailsBundle.putString("signType", historyItemList.get(pos).getSignType().getLabel());
        detailsBundle.putString("result", historyItemList.get(pos).getResult());
        detailsBundle.putString("datetime", historyItemList.get(pos).getDateTimeLearnt());
        detailsBundle.putString("capturedPath", historyItemList.get(pos).getCapturedPath());

        newIntent.putExtras(detailsBundle);
        startActivity(newIntent);
    }
}