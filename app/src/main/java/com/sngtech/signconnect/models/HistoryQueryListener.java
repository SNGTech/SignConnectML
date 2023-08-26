package com.sngtech.signconnect.models;

import java.util.List;

public interface HistoryQueryListener {

    void onQuerySuccess(List<HistoryItem> queriedItems);

    void onQueryFailed();
}
