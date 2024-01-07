package com.example.nu_mad_sp2023_final_project_10;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PopUpSendNote {
    //PopupWindow display method
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ArrayList<String> friends = new ArrayList<>();

    public void showPopupWindow(final View view) {

        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.sendnotepopup_layout, null);

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // add UI elements here + handlers
        Spinner emailSpinner = popupView.findViewById(R.id.spinnerEmail);
        EditText noteMessage = popupView.findViewById(R.id.editTextNote);
        Button sendButton = popupView.findViewById(R.id.buttonSendNoteSubmit);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        db.collection("users")
                .document(mUser.getEmail())
                .collection("friends")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                String friend = documentSnapshot.getId();
                                addF(friend);
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                                        popupView.getContext(), android.R.layout.simple_spinner_item,
                                        friends);
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                emailSpinner.setAdapter(spinnerArrayAdapter);
                            }
                        } else {
                            Toast.makeText(popupView.getContext(), "Could not retrieve friends.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(popupView.getContext(), "Could not retrieve friends.", Toast.LENGTH_SHORT).show();
                    }
                });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int noteLength = noteMessage.getText().toString().trim().length();
                if(noteLength > 0 &&
                        emailSpinner.getSelectedItem() != null) {
                    String message = noteMessage.getText().toString();
                    String emailSendTo = emailSpinner.getSelectedItem().toString();
                    addNoteToDatabase(message, emailSendTo);
                    popupWindow.dismiss();
                } else {
                    Toast.makeText(popupView.getContext(), "Can not leave fields empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // clicking on the inactive zone of the window
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void addF(String f) {
        friends.add(f);
    }

    private void addNoteToDatabase(String message, String email) {
        Map<String, Object> nt = new HashMap<>();
        nt.put("note", message);
        db.collection("users")
                .document(email)
                .collection("notes")
                .document(message).set(nt);
    }
}
