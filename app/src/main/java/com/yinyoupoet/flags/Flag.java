package com.yinyoupoet.flags;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by hasee on 2018/2/14.
 */

public class Flag implements Serializable,Comparable<Flag>{
    int id = -1;             //flag的id
    String state;       //完成状态
    String color;       //item的颜色
    String title;       //显示的标题
    String content;     //内容
    Date date;          //时间

    public Flag(){
        this("待完成","#000000","","",new Date());
    }

    public Flag(String state, String color, String title, String content,Date date){
        setState(state);
        setColor(color);
        setTitle(title);
        setContent(content);
        setDate(date);
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(@NonNull Flag flag) {
        if(getDate().getTime() > flag.getDate().getTime()){
            return -1;
        }else if(getDate().getTime() < flag.getDate().getTime()){
            return 1;
        }
        return 0;
    }
}
