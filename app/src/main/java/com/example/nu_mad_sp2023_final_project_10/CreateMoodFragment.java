package com.example.nu_mad_sp2023_final_project_10;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

import top.defaults.colorpicker.ColorWheelView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateMoodFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateMoodFragment extends Fragment {

    private ColorWheelView colorWheelView;
    private View pickedColor;

    private TextView textColorHex;
    private EditText editTextName;
    private RadioGroup radioGroup;
    private RadioButton radioButtonNeutral;
    private Button buttonDone;

    private int moodType;
    private String moodName;

    private int moodColor;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String email;

    private IFromCreateMood sendData;

    public CreateMoodFragment() {
        // Required empty public constructor
    }

    public static CreateMoodFragment newInstance() {
        CreateMoodFragment fragment = new CreateMoodFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        email = mUser.getEmail();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_mood, container, false);
        getActivity().setTitle("Create your own mood");

        colorWheelView = view.findViewById(R.id.colorPicker);
        pickedColor = view.findViewById(R.id.pickedColor);
        textColorHex = view.findViewById(R.id.textColorHex);
        radioGroup = view.findViewById(R.id.radioGroup);
        radioButtonNeutral = view.findViewById(R.id.radioNeutral);
        editTextName = view.findViewById(R.id.editTextMoodName);
        buttonDone = view.findViewById(R.id.buttonCreateMood);

        moodColor = colorWheelView.getColor();
        textColorHex.setText(colorHex(moodColor));

        colorWheelView.subscribe((color, fromUser, shouldPropogate) -> {
            moodColor = color;
            pickedColor.setBackgroundColor(color);
            textColorHex.setText(colorHex(moodColor));
        });

        radioButtonNeutral.setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioPositive) {
                    moodType = 1;
                } else if (i == R.id.radioNegative) {
                    moodType = -1;
                } else {
                    moodType = 0;
                }
            }
        });

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moodName = editTextName.getText().toString().trim();

                if (moodName.equals("")) {
                    editTextName.setError("Must enter name for mood");
                } else {
                    onClickDone();
                }

            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IFromCreateMood){
            sendData = (IFromCreateMood) context;
        } else {
            throw new RuntimeException(context + "must implement IFromCreateMood");
        }
    }

    private void onClickDone() {
        Mood mood = new Mood(moodColor, moodName, moodType);
        db.collection("users")
                .document(email)
                .collection("moods")
                .add(mood)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to create mood", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        sendData.onMoodCreation(documentReference);
                    }
                });
    }

    private String colorHex(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b);
    }

    public interface IFromCreateMood {
        void onMoodCreation(DocumentReference newMoodRef);
    }
}