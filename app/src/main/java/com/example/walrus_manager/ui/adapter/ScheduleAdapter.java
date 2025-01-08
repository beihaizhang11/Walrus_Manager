package com.example.walrus_manager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walrus_manager.R;
import com.example.walrus_manager.data.model.Schedule;

public class ScheduleAdapter extends ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder> {
    private final ScheduleActionListener listener;

    public interface ScheduleActionListener {
        void onScheduleClick(Schedule schedule);
        void onEditSchedule(Schedule schedule);
        void onDeleteSchedule(Schedule schedule);
    }

    public ScheduleAdapter(ScheduleActionListener listener) {
        super(new DiffUtil.ItemCallback<Schedule>() {
            @Override
            public boolean areItemsTheSame(@NonNull Schedule oldItem, @NonNull Schedule newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Schedule oldItem, @NonNull Schedule newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                        oldItem.getDescription().equals(newItem.getDescription());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final ImageButton btnMore;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        public void bind(Schedule schedule, ScheduleActionListener listener) {
            tvTitle.setText(schedule.getTitle());
            tvDescription.setText(schedule.getDescription());
            itemView.setOnClickListener(v -> listener.onScheduleClick(schedule));
            btnMore.setOnClickListener(v -> showPopupMenu(v, schedule, listener));
        }

        private void showPopupMenu(View view, Schedule schedule, ScheduleActionListener listener) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenu().add("编辑");
            popup.getMenu().add("删除");
            
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("编辑")) {
                    listener.onEditSchedule(schedule);
                    return true;
                } else if (item.getTitle().equals("删除")) {
                    listener.onDeleteSchedule(schedule);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }
    }
} 