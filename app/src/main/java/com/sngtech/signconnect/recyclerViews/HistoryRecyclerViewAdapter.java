package com.sngtech.signconnect.recyclerViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sngtech.signconnect.R;
import com.sngtech.signconnect.models.HistoryItem;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {

    Context context;
    List<HistoryItem> historyItems;

    HistoryRecyclerViewListener itemListener;

    public HistoryRecyclerViewAdapter(Context context, List<HistoryItem> historyItems, HistoryRecyclerViewListener itemListener) {
        this.context = context;
        this.historyItems = historyItems;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public HistoryRecyclerViewAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_history_view, parent, false);
        return new HistoryRecyclerViewAdapter.HistoryViewHolder(view, itemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryRecyclerViewAdapter.HistoryViewHolder holder, int position) {
        holder.signType.setText(historyItems.get(position).getSignType().getLabel());
        holder.resultText.setText(WordUtils.capitalizeFully(historyItems.get(position).getResult().replace("-", " ")));
        holder.dateTimeText.setText(historyItems.get(position).getDateTimeLearnt());
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView signType;
        TextView resultText;
        TextView dateTimeText;

        public HistoryViewHolder(@NonNull View itemView, HistoryRecyclerViewListener itemListener) {
            super(itemView);

            signType = itemView.findViewById(R.id.sign_type);
            resultText = itemView.findViewById(R.id.result_text);
            dateTimeText = itemView.findViewById(R.id.time_learnt_text);

            itemView.setOnClickListener(ignore -> {
                if(itemListener == null)
                    return;

                int pos = getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION) {
                    itemListener.onItemClick(pos);
                }
            });
        }
    }
}
