package com.example.nu_mad_sp2023_final_project_10;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    private ArrayList<CalendarDay> days = new ArrayList<>();
    private IfromFragmentToActivity listener;

    CalendarAdapter(Context context) {
        if(context instanceof IfromFragmentToActivity){
            this.listener = (IfromFragmentToActivity) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IfromFragmentToActivity");
        }
    }

    CalendarAdapter(){}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRecyclerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_day_item, parent, false);

        return new ViewHolder(itemRecyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDay day = this.getDays().get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalDate localDate = LocalDate.of(day.getYear(), day.getMonth(), day.getDay());
                listener.onDaySelected(localDate);
            }
        });

        holder.setDayText(day);

        if(Objects.nonNull(day.getColor())) {
            holder.setConstraintLayout(day.getColor());
        }
    }

    @Override
    public int getItemCount() {
        return this.getDays().size();
    }

    public ArrayList<CalendarDay> getDays() {
        return this.days;
    }

    public void setDays(ArrayList<CalendarDay> days) {
        this.days = days;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView dayText;
        private final CardView cardView;
        private final ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.dayText = itemView.findViewById(R.id.dayTextView);
            this.cardView = itemView.findViewById(R.id.CardViewDay);
            this.constraintLayout = itemView.findViewById(R.id.dayConstraintLayout);
        }


        public TextView getDayText() {
            return dayText;
        }

        public CardView getCardView() {
            return cardView;
        }

        public void setCardView(Color color) {
            cardView.setCardBackgroundColor(color.toArgb());
        }

        public ConstraintLayout getConstraintLayout() {
            return constraintLayout;
        }

        public void setConstraintLayout(Color color) {
            constraintLayout.setBackgroundColor(color.toArgb());
        }

        public void setDayText(CalendarDay day) {
            dayText.setText(String.valueOf(day.getDay()));
        }
    }


}
