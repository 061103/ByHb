package com.zhou.biyongxposed;

import java.io.Serializable;

public class Eventvalue implements Serializable {
    private Integer id;
    private String name;
    private int value;
    private String coincount;
    Eventvalue(Integer id, String name, int value, String coincount ){
        this.id=id;
        this.name = name;
        this.value  = value;
        this.coincount=coincount;
    }

    Eventvalue() {

    }

    public Integer getId(){return id;}
    public String getName(){ return name; }
    int getValue(){ return value; }
    String getCoincount(){return coincount;}
    public void setId(Integer id){this.id=id;}
    public void setName(String name){ this.name=name; }
    void setValue(int value){ this.value=value; }
    void setCoincount(String coincount){this.coincount=coincount;}
}
