package com.sahana.geosmser;

public class Contact implements Comparable{
    private String Name;
    private String number;
 
    public String getName() {
        return Name;
    }
 
    public void setName(String Name) {
        this.Name = Name;
    }
 
    public String getNumber() {
        return number;
    }
 
    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public int compareTo(Object arg0) {
        // TODO Auto-generated method stub
        Contact newCont = (Contact)arg0;
        return this.Name.compareTo(newCont.getName());
    }

   
}
