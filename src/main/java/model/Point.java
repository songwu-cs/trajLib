package model;

import datetime.SimpleDateFormatExt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Point implements Cloneable{
    protected static String defaultDateTime = "2000-01-01 00:00:00";
    protected static SimpleDateFormatExt formatter = new SimpleDateFormatExt("yyyy-MM-dd HH:mm:ss");
    private String datetimeStr;
    private double x;
    private double y;

    public static void setDateTimeFormatter(String format) throws ParseException {
        SimpleDateFormatExt formatterNew = new SimpleDateFormatExt(format);
        defaultDateTime = formatterNew.formatExt(formatter.parse(defaultDateTime));
        formatter = formatterNew;
    }

    public static String getDefaultDateTime(){
        return defaultDateTime;
    }

    public Point(String datetime, double x, double y) throws ParseException {
        this.datetimeStr = datetime;
        this.x = x;
        this.y = y;
    }

    public String getDatetimeStr() {
        return datetimeStr;
    }


    public void setDatetime(String datetime) throws ParseException {
        this.datetimeStr = datetime;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    protected Point clone() {
        try {
            return (Point) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws ParseException, CloneNotSupportedException {

    }
}
