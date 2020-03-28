package com.example.technobit.ui.contact;

public class Card {
    private String company_name;
    private String email;
    private Boolean isSelected; // salvo se la card è stata selezionata, se true background = green

    public Card(String line1, String line2) {
        this.company_name = line1;
        this.email = line2;
        this.isSelected = false;
    }

    public String getLine1() {
        return company_name;
    }

    public String getLine2() {
        return email;
    }

    public boolean isSelected(){
        return isSelected;
    }

    public void setIsSelected(boolean sel){
        this.isSelected = sel;
    }
}
