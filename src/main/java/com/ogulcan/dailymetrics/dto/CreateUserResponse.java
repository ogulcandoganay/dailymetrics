package com.ogulcan.dailymetrics.dto;

public class CreateUserResponse {

    private Long id;
    private String username;
    private String loginCode;

    public CreateUserResponse(Long id, String username, String loginCode) {
        this.id = id;
        this.username = username;
        this.loginCode = loginCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(String loginCode) {
        this.loginCode = loginCode;
    }
}