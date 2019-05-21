package com.zhou.biyongxposed;

import java.io.Serializable;

public class eventvalue implements Serializable {
    private int value;
    private String name;

    public eventvalue(String name,int value){
        this.name = name;
        this.value  = value;
    }
    public String getName(){
        return name;
    }
    public int getValue(){ return value; }
    public void setName(String name){ this.name=name; }
    public void setValue(int value){ this.value=value; }
}
