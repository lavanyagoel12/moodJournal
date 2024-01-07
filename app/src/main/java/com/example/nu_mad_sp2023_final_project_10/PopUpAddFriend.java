package com.example.nu_mad_sp2023_final_project_10;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class PopUpAddFriend {
    //PopupWindow display method

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private ArrayList<String> users = new ArrayList<>();

    public void showPopupWindow(final View view) {

        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.addfriendpopup_layout, null);

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // add UI elements here + handlers

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        Button doneButton;
        Spinner spinnerEmailAdd;

        doneButton = popupView.findViewById(R.id.buttonAddFriendSubmit);
        spinnerEmailAdd = popupView.findViewById(R.id.spinnerEmailAdd);

        db.collection("users")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                String user = documentSnapshot.getId();
                                checkFriendInDatabase(user, popupView, spinnerEmailAdd);
                            }
                        } else {
                            Toast.makeText(popupView.getContext(), "Could not retrieve users.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(popupView.getContext(), "Could not retrieve friends.", Toast.LENGTH_SHORT).show();
                    }
                });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAdd = spinnerEmailAdd.getSelectedItem().toString();
                addFriendToDatabase(emailAdd);
                popupWindow.dismiss();
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

    private void addU(String u, View popupView, Spinner spinnerEmailAdd) {
        users.add(u);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                popupView.getContext(), android.R.layout.simple_spinner_item,
                users);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmailAdd.setAdapter(spinnerArrayAdapter);
    }

    private void checkFriendInDatabase(String u, View popupView, Spinner spinnerEmailAdd) {
        db.collection("users")
                .document(mUser.getEmail())
                .collection("friends")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Boolean found = false;
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                String friend = documentSnapshot.getId();
                                if(u.equals(friend)) {
                                    found = true;
                                }
                            }
                            if(!found) {
                                if(!u.equals(mUser.getEmail())) {
                                    addU(u, popupView, spinnerEmailAdd);
                                }
                            }
                        } else {
                            Toast.makeText(popupView.getContext(), "Could not check if friend exists.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(popupView.getContext(), "Could not check if friend exists.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addFriendToDatabase(String emailAdd) {
        Map<String, Object> em = new HashMap<>();
        em.put("email", emailAdd);

        db.collection("users")
                .document(mUser.getEmail())
                .collection("friends")
                .document(emailAdd).set(em);
    }
}
