package io;

import model.AttributeType;
import model.ExtraAttribute;
import model.Trajectory;

import java.util.ArrayList;
import java.util.List;

public class LoadTrajectories {
    private String filePath;
    private String splitter = ",";
    private boolean withHeader;
    private int sampleGap = Integer.MIN_VALUE;
    private int xIndex, yIndex, datetimeIndex, trajIndex;
    private List<ExtraAttribute> attrs = new ArrayList<>();

    public LoadTrajectories filePath(String filePath){
        this.filePath = filePath;
        return this;
    }

    public LoadTrajectories splitter(String splitter){
        this.splitter = splitter;
        return this;
    }

    public LoadTrajectories xIndex(int xIndex){
        this.xIndex = xIndex;
        return this;
    }

    public LoadTrajectories sampleGap(int seconds){
        sampleGap = seconds;
        return this;
    }

    public LoadTrajectories yIndex(int yIndex){
        this.yIndex = yIndex;
        return this;
    }

    public LoadTrajectories datetimeIndex(int datetimeIndex){
        this.datetimeIndex = datetimeIndex;
        return this;
    }

    public LoadTrajectories trajIndex(int trajIndex){
        this.trajIndex = trajIndex;
        return this;
    }

    public LoadTrajectories withHeader(boolean bool){
        this.withHeader = bool;
        return this;
    }

    public LoadTrajectories addAttr(AttributeType type, String attrName, int attrIndex){
        attrs.add(new ExtraAttribute(type, attrName, attrIndex));
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getSplitter(){
        return splitter;
    }

    public boolean isWithHeader() {
        return withHeader;
    }

    public int getSampleGap(){
        return sampleGap;
    }

    public int getxIndex() {
        return xIndex;
    }

    public int getyIndex() {
        return yIndex;
    }

    public int getDatetimeIndex() {
        return datetimeIndex;
    }

    public int getTrajIndex() {
        return trajIndex;
    }

    public List<ExtraAttribute> getAttrs() {
        return attrs;
    }

    public boolean hasDateTime(){
        return datetimeIndex >= 0;
    }
}
