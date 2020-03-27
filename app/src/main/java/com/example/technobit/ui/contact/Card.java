package com.example.technobit.ui.contact;

public class Card {
    private String company_name;
    private String email;

    public Card(String line1, String line2) {
        this.company_name = line1;
        this.email = line2;
    }

    public String getLine1() {
        return company_name;
    }

    public String getLine2() {
        return email;
    }

}
