package edu.fusion.common.model;

public class User {

    private String username;
    private String password;
    private Role role;
    private String college;

    public User() {
    }

    public User(String username, String password, Role role, String college) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.college = college;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }
}
