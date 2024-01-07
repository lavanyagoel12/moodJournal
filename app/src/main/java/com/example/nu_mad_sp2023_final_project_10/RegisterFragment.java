package com.example.nu_mad_sp2023_final_project_10;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterFragment extends Fragment {

    private Button buttonRegisterSubmit;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPasswordConfirm;
    private EditText editTextName;

    private String registerName;
    private String registerEmail;
    private String registerPassword;
    private String registerPasswordConfirm;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private IfromFragmentToActivity sendData;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        Initializing the interface......
        if (context instanceof IfromFragmentToActivity){
            sendData = (IfromFragmentToActivity) context;
        }else{
            throw new RuntimeException(context + "must implement IFromFragmentLogin");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        getActivity().setTitle("Register");
        buttonRegisterSubmit = view.findViewById(R.id.buttonRegisterSubmit);
        editTextName = view.findViewById(R.id.editTextRegisterName);
        editTextEmail = view.findViewById(R.id.editTextRegisterEmailAddress);
        editTextPassword = view.findViewById(R.id.editTextRegisterPassword);
        editTextPasswordConfirm = view.findViewById(R.id.editTextRegisterConfirmPassword);

        buttonRegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate input with built in features
                registerName = editTextName.getText().toString();
                registerEmail = editTextEmail.getText().toString();
                registerPassword = editTextPassword.getText().toString();
                registerPasswordConfirm = editTextPasswordConfirm.getText().toString();

                // make call to the api
                if(registerName.length() == 0) {
                    Toast.makeText(getActivity(), "Please enter a valid name.", Toast.LENGTH_LONG).show();
                } else if(registerEmail.length() == 0 && !Patterns.EMAIL_ADDRESS.matcher(registerEmail).matches()) {
                    Toast.makeText(getActivity(), "Please enter a valid email address.", Toast.LENGTH_LONG).show();
                } else if (registerPassword.length() > 0 && registerPassword.equals(registerPasswordConfirm)) {
                    User user = new User(registerName, registerEmail);

                    mAuth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                        mUser = task.getResult().getUser();



//                                    Adding name to the FirebaseUser...
                                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(registerName)
                                                .build();

                                        mUser.updateProfile(profileChangeRequest)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            sendData.registerDone(mUser, user);
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                                                    }
                                                })
                                        ;

                                    }

                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                                }
                            });



                } else {
                    Toast.makeText(getActivity(), "Please make sure passwords match", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }
}