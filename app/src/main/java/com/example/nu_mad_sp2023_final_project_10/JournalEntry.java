package com.example.nu_mad_sp2023_final_project_10;

import com.google.firebase.firestore.DocumentReference;

public class JournalEntry {

    private String entryBody;
    private DocumentReference moodRef;
    private String location;

    public JournalEntry() {}

    public JournalEntry(String entryBody, DocumentReference moodRef, String location) {
        this.entryBody = entryBody;
        this.moodRef = moodRef;
        this.location = location;
    }

    public String getEntryBody() {
        return entryBody;
    }

    public DocumentReference getMoodRef() {
        return moodRef;
    }

    public String getLocation() {
        return location;
    }
}
