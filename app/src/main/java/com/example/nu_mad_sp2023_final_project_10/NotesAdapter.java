package com.example.nu_mad_sp2023_final_project_10;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    private ArrayList<String> notes = new ArrayList<>();

    NotesAdapter(ArrayList<String> notes) {
        this.notes = notes;
    }

    NotesAdapter() {
    }

    public ArrayList<String> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<String> notes) {
        this.notes = notes;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView noteText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.noteText = itemView.findViewById(R.id.textViewNote);
        }

        public TextView getNoteText(){return noteText;}
        public void setNoteText(String note) {
            noteText.setText(note);
        }

    }

    @NonNull
    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRecyclerView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.notes_row, parent, false);

        return new ViewHolder(itemRecyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.ViewHolder holder, int position) {
        String note = this.getNotes().get(position);

        holder.setNoteText(note);
    }

    @Override
    public int getItemCount() {
        return this.getNotes().size();
    }
}