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
import com.example.walrus_manager.data.model.Event;
import com.example.walrus_manager.util.CountdownTimer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventAdapter extends ListAdapter<Event, EventAdapter.EventViewHolder> {
    private final EventActionListener listener;

    public interface EventActionListener {
        void onEditEvent(Event event);
        void onDeleteEvent(Event event);
    }

    public EventAdapter(EventActionListener listener) {
        super(new DiffUtil.ItemCallback<Event>() {
            @Override
            public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                        oldItem.getDescription().equals(newItem.getDescription()) &&
                        oldItem.getTargetDate() == newItem.getTargetDate();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull EventViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        CountdownTimer.getInstance().addListener(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull EventViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        CountdownTimer.getInstance().removeListener(holder);
    }

    static class EventViewHolder extends RecyclerView.ViewHolder implements CountdownTimer.CountdownListener {
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvTargetDate;
        private final TextView tvRemainingTime;
        private final ImageButton btnMore;
        private final SimpleDateFormat dateFormat;
        private Event currentEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTargetDate = itemView.findViewById(R.id.tvTargetDate);
            tvRemainingTime = itemView.findViewById(R.id.tvRemainingTime);
            btnMore = itemView.findViewById(R.id.btnMore);
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        }

        public void bind(Event event, EventActionListener listener) {
            currentEvent = event;
            tvTitle.setText(event.getTitle());
            tvDescription.setText(event.getDescription());
            tvTargetDate.setText("目标时间: " + dateFormat.format(new Date(event.getTargetDate())));
            updateRemainingTime();

            btnMore.setOnClickListener(v -> showPopupMenu(v, event, listener));
        }

        private void showPopupMenu(View view, Event event, EventActionListener listener) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenu().add("编辑");
            popup.getMenu().add("删除");
            
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("编辑")) {
                    listener.onEditEvent(event);
                    return true;
                } else if (item.getTitle().equals("删除")) {
                    listener.onDeleteEvent(event);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }

        @Override
        public void onCountdownTick() {
            if (currentEvent != null) {
                updateRemainingTime();
            }
        }

        private void updateRemainingTime() {
            long remainingTime = currentEvent.getTargetDate() - System.currentTimeMillis();
            tvRemainingTime.setText("剩余时间: " + formatRemainingTime(remainingTime));
        }

        private String formatRemainingTime(long milliseconds) {
            if (milliseconds < 0) {
                return "已过期";
            }

            long seconds = milliseconds / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + "天";
            } else if (hours > 0) {
                return hours + "小时";
            } else if (minutes > 0) {
                return minutes + "分钟";
            } else {
                return seconds + "秒";
            }
        }
    }
} 