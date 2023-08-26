package com.sngtech.signconnect.models;

public class User {

    private String email;
    private String password;
    private boolean isDetBoxEnabled;

    public User() {}

    public User(String email, String password, boolean isDetBoxEnabled) {
        this.email = email;
        this.password = password;
        this.isDetBoxEnabled = isDetBoxEnabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDetBoxEnabled() {
        return isDetBoxEnabled;
    }

    public void setDetBoxEnabled(boolean detBoxEnabled) {
        isDetBoxEnabled = detBoxEnabled;
    }
}
