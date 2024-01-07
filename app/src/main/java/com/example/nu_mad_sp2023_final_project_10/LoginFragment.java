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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginFragment extends Fragment {
    private Button buttonLoginSubmit;
    private EditText editTextEmail;
    private EditText editTextPassword;

    private String loginEmail;
    private String loginPassword;
    private FirebaseAuth mAuth;

    private IfromFragmentToActivity sendData;


    public LoginFragment() {
        // Required empty public constructor
    }


    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        getActivity().setTitle("Login");
        buttonLoginSubmit = view.findViewById(R.id.buttonLoginSubmit);
        editTextEmail = view.findViewById(R.id.editTextLoginEmailAddress);
        editTextPassword = view.findViewById(R.id.editTextLoginPassword);

        buttonLoginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate input with built in features
                loginEmail = editTextEmail.getText().toString();
                loginPassword = editTextPassword.getText().toString();

                // make call to the api
                if (loginEmail.length() == 0 || !Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) {
                    Toast.makeText(getActivity(), "Please enter a valid email address", Toast.LENGTH_LONG).show();
                } else if (loginPassword.length() == 0) {
                    Toast.makeText(getActivity(), "Please enter password", Toast.LENGTH_LONG).show();
                } else {
                    mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                                }
                            })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Login Failed! " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        sendData.populateMainFragment(mAuth.getCurrentUser());
                                    }
                                }
                            });
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        Initializing the interface......
        if (context instanceof IfromFragmentToActivity){
            sendData = (IfromFragmentToActivity) context;
        }else{
            throw new RuntimeException(context + "must implement IFromFragmentLoginSuccess");
        }
    }

}