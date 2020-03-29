package com.example.technobit.ui.contact;

import com.example.technobit.contactdatas.SingleContact;

// Class that represent a single contact like a card to be added in an listview
public class Card extends SingleContact {
    //private singleContact client;
    private Boolean isCardSelected; // salvo se la card è stata selezionata, se true background = green

    public Card(String company_name, String email) {
        super(company_name,email);

        this.isCardSelected = false;
    }

    public Card(SingleContact contact) {
        super(contact);

        this.isCardSelected = false;
    }

    public String getLine1() {
        return super.getCompany_name();
    }

    public String getLine2() {
        return super.getEmail();
    }

    public boolean isCardSelected(){
        return isCardSelected;
    }

    public void setCardSelection(boolean sel){
        this.isCardSelected = sel;
    }
}
