package com.github.dmitrykersh.bugs.api.account;

/**
 * This class represents player's account and stores all information about it.
 */
public class Account {
    private String nickname;
    private String email;

    private int rating;

    public Account(String nickname, String email, int rating) {
        this.nickname = nickname;
        this.email = email;
        this.rating = rating;
    }

    public String getNickname() {
        return nickname;
    }

}
