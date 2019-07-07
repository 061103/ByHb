package com.zhou.biyongxposed;

import java.io.Serializable;

public class Eventvalue implements Serializable {
    private int id;
    private String name;
    private int value;

    public Eventvalue(int id,String name, int value){
        this.id=id;
        this.name = name;
        this.value  = value;
    }
    public int getType(){return id;}
    public String getName(){
        return name;
    }
    public int getValue(){ return value; }
    public void setType(){this.id=id;}
    public void setName(String name){ this.name=name; }
    public void setValue(int value){ this.value=value; }
}