package com.example.technobit.contactdatas;

// this class represent the data of a single contact.

public class singleContact {
    private String company_name; // name of the company
    private String email; // email of the company

    public singleContact(String name, String email) {
        this.company_name = name;
        this.email = email;
    }

    public String getCompany_name() {
        return company_name;
    }

    public String getEmail() {
        return email;
    }

    public String toString(){
        if(this.email.equals("")) // if mail is empty I set it like a single space
            this.email = "";
        return this.company_name +";" + this.email;
    }

    public singleContact readFromString(String s_contact){
        // Check if the string contains the splitted character
        if(!s_contact.contains(";")) {
            String[] values = s_contact.split(";");
            if (values.length == 2) // string is well formed, has exactly one values for each field
                return new singleContact(values[0], values[1]);
        }

        return null;
    }
}
