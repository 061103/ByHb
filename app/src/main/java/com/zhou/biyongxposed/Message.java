package com.zhou.biyongxposed;

public class Message {
    /*定义一个MessageEvent类
     *这个类是一个普通的Object，没有任何特殊，我们用它作为消息传递的载体
     */
    private  String msg;

    public Message(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
