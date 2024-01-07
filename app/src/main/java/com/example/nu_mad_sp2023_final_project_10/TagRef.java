package com.example.nu_mad_sp2023_final_project_10;

import com.google.firebase.firestore.DocumentReference;

public class TagRef {
    private DocumentReference tagRef;

    public TagRef() {}

    public TagRef(DocumentReference tagRef) {
        this.tagRef = tagRef;
    }

    public DocumentReference getTagRef() {
        return tagRef;
    }
}
