package com.example.healthlineadminapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class queueAdapter extends RecyclerView.Adapter<queueAdapter.QueueViewHolder> {

    private List<queueItem> queueList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(queueItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public queueAdapter(List<queueItem> queueList) {
        this.queueList = queueList;
    }

    @Override
    public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QueueViewHolder holder, int position) {
        queueItem item = queueList.get(position);
        holder.patientName.setText(item.getPatientName());
        holder.queueNumber.setText(String.valueOf(item.getQueueNumber()));
    }

    @Override
    public int getItemCount() {
        return queueList.size();
    }

    public class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView patientName, queueNumber;

        public QueueViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.tvPatientName);
            queueNumber = itemView.findViewById(R.id.tvQueueNumber);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(queueList.get(pos));
                }
            });
        }
    }
}