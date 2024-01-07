package com.example.nu_mad_sp2023_final_project_10;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String email;

    public User(){
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

        public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

   @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        User other = (User) obj;
        return getEmail().equals(other.getEmail());
    }

    @Override
    public int hashCode() {
        return getEmail().hashCode();
    }


}
