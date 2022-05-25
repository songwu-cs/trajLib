package model;

import calculation.ListGeneric;
import datetime.TwoTimestamp;
import io.LoadTrajectories;
import io.bigdata.BatchFileReader;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Trajectory {
    private HashMap<String, List<Double>> doubleAttributes;
    private HashMap<String, List<Integer>> intAttributes;
    private HashMap<String, List<String>> strAttributes;
    private List<Point> points;
    private String trajID;

    public Trajectory(String id) {
        doubleAttributes = new HashMap<>();
        intAttributes = new HashMap<>();
        strAttributes = new HashMap<>();
        points = new ArrayList<>();
        trajID = id;
    }

    public void setPoints(List<Point> points){
        this.points = points;
    }

    public Trajectory subTrajectory(int start, int end, String id){
        Trajectory subTraj = new Trajectory(id);
        for(Point p : points.subList(start, end)){
            subTraj.points.add(p.clone());
        }
        for(String key : doubleAttributes.keySet()){
            List<Double> doubles = new ArrayList<>();
            doubles.addAll(doubleAttributes.get(key).subList(start, end));
            subTraj.doubleAttributes.put(key, doubles);
        }
        for(String key : intAttributes.keySet()){
            List<Integer> integers = new ArrayList<>();
            integers.addAll(intAttributes.get(key).subList(start, end));
            subTraj.intAttributes.put(key, integers);
        }
        for(String key : strAttributes.keySet()){
            List<String> strings = new ArrayList<>();
            strings.addAll(strAttributes.get(key).subList(start, end));
            subTraj.strAttributes.put(key, strings);
        }
        return subTraj;
    }

    public List<Double> getAttrsDouble(String attrName){
        return doubleAttributes.get(attrName);
    }

    public List<Integer> getAttrsInteger(String attrName){
        return intAttributes.get(attrName);
    }

    public List<String> getAttrsString(String attrName){
        return strAttributes.get(attrName);
    }

    public void putAttrsDouble(String attrName, List<Double> doubles){
        doubleAttributes.put(attrName, doubles);
    }

    public void putAttrsInteger(String attrName, List<Integer> integers){
        intAttributes.put(attrName, integers);
    }

    public void putAttrsString(String attrName, List<String> strings){
        strAttributes.put(attrName, strings);
    }

    public Point getPoint(int index){
        return points.get(index);
    }

    public String getID(){
        return trajID;
    }

    public int size(){
        return points.size();
    }

    public boolean contains(String timestamp){
        return points.get(0).getDatetimeStr().compareTo(timestamp) <= 0 &&
                points.get(points.size()-1).getDatetimeStr().compareTo(timestamp) >= 0;
    }

    //contains必须为true
    public double[] getVector2(String timestamp){
        int i = ListGeneric.firstIndex(points, e->e.getDatetimeStr().equals(timestamp));
        if(i < 0){
            int right = ListGeneric.firstIndex(points, e->e.getDatetimeStr().compareTo(timestamp)>0);
            int left = right - 1;
            double timeGapTotal = TwoTimestamp.diffInSeconds(points.get(right).getDatetimeStr(),
                    points.get(left).getDatetimeStr(), Point.formatter);
            double timeGapToLeft = TwoTimestamp.diffInSeconds(timestamp, points.get(left).getDatetimeStr(), Point.formatter);
            double leftCoef = (timeGapTotal - timeGapToLeft) / timeGapTotal;
            double rightCoef = 1 - leftCoef;
            return new double[]{points.get(left).getX() * leftCoef + points.get(right).getX() * rightCoef,
                                points.get(left).getY() * leftCoef + points.get(right).getY() * rightCoef};
        }else {
            return new double[]{points.get(i).getX(), points.get(i).getY()};
        }
    }

    public static List<Trajectory> load(LoadTrajectories loadConfig) throws IOException, ParseException {
        List<Trajectory> trajs = new ArrayList<>();
        try(BatchFileReader reader = new BatchFileReader(loadConfig.getFilePath(),
                loadConfig.getSplitter(), loadConfig.isWithHeader(), loadConfig.getTrajIndex())) {
            for(List<String> ss : reader){
                String trajID = ss.get(0).split(loadConfig.getSplitter())[loadConfig.getTrajIndex()];
                Trajectory trajectory = new Trajectory(trajID);
                trajs.add(trajectory);
                for(ExtraAttribute extraAttribute : loadConfig.getAttrs()){
                    if(extraAttribute.type == AttributeType.DoubleAttr)
                        trajectory.doubleAttributes.put(extraAttribute.attrName, new ArrayList<>());
                    else if (extraAttribute.type == AttributeType.IntAttr)
                        trajectory.intAttributes.put(extraAttribute.attrName, new ArrayList<>());
                    else if (extraAttribute.type == AttributeType.StrAttr)
                        trajectory.strAttributes.put(extraAttribute.attrName, new ArrayList<>());
                }

                String lastTimestamp = Point.defaultDateTime;
                for(String line : ss){
                    String[] parts = line.split(loadConfig.getSplitter());
                    String datetime = Point.defaultDateTime;
                    if(loadConfig.hasDateTime())
                        datetime = parts[loadConfig.getDatetimeIndex()];

                    if(TwoTimestamp.diffInSeconds(datetime, lastTimestamp, Point.formatter) < loadConfig.getSampleGap())
                        continue;
                    else
                        lastTimestamp = datetime;

                    trajectory.points.add(new Point(datetime,
                            Double.parseDouble(parts[loadConfig.getxIndex()]),
                            Double.parseDouble(parts[loadConfig.getyIndex()])));
                    for(ExtraAttribute extraAttribute : loadConfig.getAttrs()){
                        if(extraAttribute.type == AttributeType.DoubleAttr)
                            trajectory.doubleAttributes.get(extraAttribute.attrName).add(Double.parseDouble(parts[extraAttribute.attrIndex]));
                        else if (extraAttribute.type == AttributeType.IntAttr)
                            trajectory.intAttributes.get(extraAttribute.attrName).add(Integer.parseInt(parts[extraAttribute.attrIndex]));
                        else if (extraAttribute.type == AttributeType.StrAttr){
                            trajectory.strAttributes.get(extraAttribute.attrName).add(parts[extraAttribute.attrIndex]);
                        }
                    }
                }
            }
        }
        return trajs;
    }
}
