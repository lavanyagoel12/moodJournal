package com.example.nu_mad_sp2023_final_project_10;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChooseMoodAdapter extends RecyclerView.Adapter<ChooseMoodAdapter.ViewHolder>{

    private ArrayList<Mood> moods = new ArrayList<>();
    private int currSelected = -1;

    private IFromChooseMoodAdapter mListener;

    ChooseMoodAdapter() {
        moods.add(new Mood(Color.LTGRAY, "Create new mood", 0));
        moods.add(new Mood(Color.GREEN, "happy", 1));
        moods.add(new Mood(Color.RED, "angry", -1));
        moods.add(new Mood(Color.BLUE, "sad", -1));
        moods.add(new Mood(Color.YELLOW, "calm", 0));
        moods.add(new Mood(Color.MAGENTA, "nervous", -1));
    }

    ChooseMoodAdapter(ArrayList<Mood> moods, Context context) {
        this.moods = moods;
        if (context instanceof IFromChooseMoodAdapter) {
            this.mListener = (IFromChooseMoodAdapter) context;
        } else {
            throw new RuntimeException(context.toString()+ "must implement IFromChooseMoodAdapter");
        }
    }

    public ArrayList<Mood> getMoods() {
        return moods;
    }

    public void setMoods(ArrayList<Mood> moods) {
        this.moods = moods;
    }

    public int getCurrSelected() {
        return currSelected;
    }

    public void setCurrSelected(int currSelected) {
        this.currSelected = currSelected;
        notifyItemChanged(currSelected);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final View moodSquare;
        private final TextView moodName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.moodSquare = itemView.findViewById(R.id.moodSquare);
            this.moodName = itemView.findViewById(R.id.moodName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        notifyItemChanged(currSelected);
                        if (getAdapterPosition() == 0) {
                            mListener.onCreateMood();
                        } else {
                            currSelected = getAdapterPosition();
                            notifyItemChanged(currSelected);
                            mListener.onChooseMood(currSelected);
                        }
                    }
                }
            });
        }

        public View getMoodSquare() {
            return moodSquare;
        }

        public TextView getMoodName() {
            return moodName;
        }
    }

    @NonNull
    @Override
    public ChooseMoodAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRecyclerView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.select_mood_item, parent, false);

        return new ViewHolder(itemRecyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseMoodAdapter.ViewHolder holder, int position) {
        Mood mood = this.getMoods().get(position);

        holder.getMoodSquare().setBackgroundColor(mood.getColor());
        holder.getMoodName().setText(mood.getName());

        holder.itemView.setBackgroundColor(currSelected == position ? Color.BLACK : Color.TRANSPARENT);

    }

    @Override
    public int getItemCount() {
        return this.getMoods().size();
    }


    public interface IFromChooseMoodAdapter {
        void onChooseMood(int position);
        void onCreateMood();
    }
}
