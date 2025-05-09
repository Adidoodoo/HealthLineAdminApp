package com.example.healthlineadminapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class departmentAdapter extends RecyclerView.Adapter<departmentAdapter.DepartmentViewHolder> {

    private List<Department> departments;
    private OnItemClickListener listener;

    public departmentAdapter(List<Department> departments) {
        this.departments = departments;
    }

    @Override
    public DepartmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_department, parent, false);
        return new DepartmentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DepartmentViewHolder holder, int position) {
        Department department = departments.get(position);
        holder.departmentNameTextView.setText(department.getDepartmentName());
        holder.departmentDoctorTextView.setText(department.getDoctorName());
        holder.departmentQueueNumberTextView.setText(String.valueOf(department.getCurrentQueue()));
    }

    @Override
    public int getItemCount() {
        return departments.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class DepartmentViewHolder extends RecyclerView.ViewHolder {
        public TextView departmentNameTextView;
        public TextView departmentDoctorTextView;
        public TextView departmentQueueNumberTextView;

        public DepartmentViewHolder(View itemView) {
            super(itemView);
            departmentNameTextView = itemView.findViewById(R.id.tvDepartmentName);
            departmentDoctorTextView = itemView.findViewById(R.id.tvDoctorName);
            departmentQueueNumberTextView = itemView.findViewById(R.id.tvQueueCount);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
