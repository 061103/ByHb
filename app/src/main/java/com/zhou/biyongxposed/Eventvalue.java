package com.zhou.biyongxposed;

import java.io.Serializable;

public class Eventvalue implements Serializable {
    private String type;
    private String name;
    private int value;

    public Eventvalue(String type,String name, int value){
        this.type=type;
        this.name = name;
        this.value  = value;
    }
    public String getType(){return type;}
    public String getName(){
        return name;
    }
    public int getValue(){ return value; }
    public void setType(){this.type=type;}
    public void setName(String name){ this.name=name; }
    public void setValue(int value){ this.value=value; }
}
