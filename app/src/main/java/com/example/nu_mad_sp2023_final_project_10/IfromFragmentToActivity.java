package com.example.nu_mad_sp2023_final_project_10;

import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.util.Date;

public interface IfromFragmentToActivity {
    void fromFragment(boolean toLoginPage);

    void populateMainFragment(FirebaseUser currentUser);

    void registerDone(FirebaseUser mUser, User user);

    void onDaySelected(LocalDate date);

    void onLogout();

    void onMemoryBox();

    void onCalendar();
}
