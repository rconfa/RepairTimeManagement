package com.example.technobit.utils.contact;

// this class represent the data of a single contact.

public class Contact {
    private String company_name; // name of the company
    private String email; // email of the company

    public Contact(String name, String email) {
        this.company_name = name;
        this.email = email;
    }

    public Contact(Contact contact) {
        this.company_name = contact.company_name;
        this.email = contact.email;
    }

    public Contact(){}

    public String getCompany_name() {
        return company_name;
    }

    public String getEmail() {
        return email;
    }

    public String toString(){
        return this.company_name +";" + this.email;
    }

    public Contact readFromString(String s_contact){
        // Check if the string contains the splitted character
        if(s_contact.contains(";")) {
            String[] values = s_contact.split(";");
            if (values.length == 2) // string is well formed, has exactly one values for each field
                return new Contact(values[0], values[1]);
            else if(values.length == 1) // string contains only the company name
                return new Contact(values[0], "");
        }

        return null;
    }

    // method to check if two contact are equals
    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object instanceof Contact)
        {
            sameSame = this.company_name.equals(((Contact) object).company_name);
        }

        return sameSame;
    }
}
