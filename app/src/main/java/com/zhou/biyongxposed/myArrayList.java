package com.zhou.biyongxposed;

public class myArrayList {

    private Object[] obj;
    private int index;//数组里实际装的元素个数

    /**
     * 默认长度为10
     */
    public myArrayList(){
        obj = new Object[10];
    }
    /**
     * 自定义长度
     * @param length
     */
    public myArrayList(int length){
        obj = new Object[length];
    }

    /**
     * 检查,扩大容量
     */
    public void expandArr(){
        Object[] temp;
        //检查容量（判断 数组里装的元素个数 与 数组长度）
        if(index >= obj.length){
            temp = new Object[obj.length * 2];//2倍大小扩展
            System.arraycopy(obj, 0, temp, 0, obj.length);//数组拷贝
            obj = temp;//将temp地址赋给obj
        }

    }

    /**
     * 添加操作
     * @param x
     * @param n
     */
    public void insert(Object x, Object n){
        expandArr();//每次添加先进行容量检查，如果不够则会自动增加2倍大小的容量，否则不执行任何操作
        obj[index++] = n;
    }

    /**
     * 数组置空
     */
    public void makeEmpty(){
        index = 0;
        obj = null;
    }

    /**
     * 获取数组第i位
     */
    public Object getArr(int i){
        return obj[i];
    }

    /**
     * 获取数组大小
     */
    public int getSize(){
        return index;
    }

    /**
     * 打印数组
     */
    public void printList(){
        for(int i = 0; i < obj.length; i++){
            System.out.println(obj[i]);
        }
    }
}
