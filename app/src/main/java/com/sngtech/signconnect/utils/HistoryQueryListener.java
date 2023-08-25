package com.sngtech.signconnect.utils;

import com.sngtech.signconnect.recyclerViews.HistoryItem;

import java.util.List;

public interface HistoryQueryListener {

    void onQuerySuccess(List<HistoryItem> queriedItems);
}
