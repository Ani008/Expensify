package com.example.expensify;

public class UserHelperClass {
    String username, phoneNo, upiId;

    public UserHelperClass() {
        // Empty constructor required for Firebase
    }

    public UserHelperClass(String username, String phoneNo, String upiId) {
        this.username = username;
        this.phoneNo = phoneNo;
        this.upiId = upiId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
}