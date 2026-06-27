package com.lafid.rentaja.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String role; // "renter" or "owner"
    private String photoUrl;

    // Firestore requires empty constructor
    public User() {}

    public User(String uid, String name, String email, String role) {
        this.uid   = uid;
        this.name  = name;
        this.email = email;
        this.role  = role;
    }

    public User(String uid, String name, String email, String role, String photoUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.photoUrl = photoUrl;
    }

    // Getters & setters
    public String getUid()              { return uid; }
    public void   setUid(String uid)    { this.uid = uid; }

    public String getName()             { return name; }
    public void   setName(String name)  { this.name = name; }

    public String getEmail()              { return email; }
    public void   setEmail(String email)  { this.email = email; }

    public String getRole()             { return role; }
    public void   setRole(String role)  { this.role = role; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isOwner()  { return "owner".equals(role); }
    public boolean isRenter() { return "renter".equals(role); }
}
