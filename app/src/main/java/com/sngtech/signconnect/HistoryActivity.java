package com.sngtech.signconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sngtech.signconnect.databinding.ActivityHistoryBinding;
import com.sngtech.signconnect.models.HistoryItem;
import com.sngtech.signconnect.recyclerViews.HistoryRecyclerViewAdapter;
import com.sngtech.signconnect.recyclerViews.HistoryRecyclerViewListener;
import com.sngtech.signconnect.models.HistoryModel;
import com.sngtech.signconnect.models.HistoryQueryListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements HistoryRecyclerViewListener, HistoryQueryListener {

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

        populateHistoryList();
    }

    private void populateHistoryList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HistoryModel.queryHistoryItems(FirebaseAuth.getInstance().getCurrentUser(), db, this);
    }

    @Override
    public void onItemClick(int pos) {
        Intent newIntent = new Intent(getApplicationContext(), SignDetailsActivity.class);
        Bundle detailsBundle = new Bundle();
        detailsBundle.putString("signType", historyItemList.get(pos).getSignType().getLabel());
        detailsBundle.putString("result", historyItemList.get(pos).getResult());
        detailsBundle.putString("datetime", historyItemList.get(pos).getDateTimeLearnt());
        detailsBundle.putString("capturedPath", historyItemList.get(pos).getCapturedPath());
        detailsBundle.putInt("facing", historyItemList.get(pos).getFacing());

        newIntent.putExtras(detailsBundle);
        startActivity(newIntent);
    }

    @Override
    public void onQuerySuccess(List<HistoryItem> queriedItems) {
        historyItemList.clear();
        historyItemList.addAll(queriedItems);

        // Show Recycler View
        RecyclerView recyclerView = binding.historyRecyclerView;
        adapter = new HistoryRecyclerViewAdapter(getApplicationContext(), historyItemList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}