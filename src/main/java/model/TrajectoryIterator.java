package model;

import datetime.OneTimestamp;
import datetime.TwoTimestamp;
import io.LoadTrajectories;
import io.bigdata.BatchFileReader;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrajectoryIterator implements Closeable,Iterable<Trajectory> {
    private LoadTrajectories loadConfig;
    private BatchFileReader batchFileReader;
    public TrajectoryIterator(LoadTrajectories loadConfig) throws IOException {
        this.loadConfig = loadConfig;
        batchFileReader = new BatchFileReader(loadConfig.getFilePath(),loadConfig.getSplitter(),loadConfig.isWithHeader(), loadConfig.getTrajIndex());
    }

    @Override
    public void close() throws IOException {
        batchFileReader.close();
    }

    @Override
    public Iterator<Trajectory> iterator() {
        return new BaseTrajIterator();
    }

    private class BaseTrajIterator implements Iterator<Trajectory>{
        List<String> lines;

        @Override
        public boolean hasNext() {
            try {
                lines = batchFileReader.readBatch();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lines != null;
        }

        @Override
        public Trajectory next() {
            String trajID = lines.get(0).split(loadConfig.getSplitter())[loadConfig.getTrajIndex()];
            Trajectory trajectory = new Trajectory(trajID);

            for(ExtraAttribute extraAttribute : loadConfig.getAttrs()){
                if(extraAttribute.type == AttributeType.DoubleAttr)
                    trajectory.doubleAttributes.put(extraAttribute.attrName, new ArrayList<>());
                else if (extraAttribute.type == AttributeType.IntAttr)
                    trajectory.intAttributes.put(extraAttribute.attrName, new ArrayList<>());
                else if (extraAttribute.type == AttributeType.StrAttr)
                    trajectory.strAttributes.put(extraAttribute.attrName, new ArrayList<>());
            }

            List<Point> points = new ArrayList<>();

            String lastTimestamp = Point.defaultDateTime;
            for(String line : lines){
                String[] parts = line.split(loadConfig.getSplitter());
                String datetime = Point.defaultDateTime;
                if(loadConfig.hasDateTime())
                    datetime = parts[loadConfig.getDatetimeIndex()];

                if(TwoTimestamp.diffInSeconds(datetime, lastTimestamp, Point.formatter) < loadConfig.getSampleGap())
                    continue;
                else
                    lastTimestamp = datetime;

                try {
                    points.add(new Point(datetime,
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
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            trajectory.setPoints(points);

            if(! loadConfig.getRegularFromTimestamp().equals("NONE")){
                trajectory.doubleAttributes.clear();
                trajectory.intAttributes.clear();
                trajectory.strAttributes.clear();

                List<Point> newPoints = new ArrayList<>();
                for(String s = loadConfig.getRegularFromTimestamp();
                    s.compareTo(loadConfig.getRegularToTimestamp()) <= 0;
                    s = OneTimestamp.add(s, 0, 0, loadConfig.getRegularGap(), Point.formatter)){
                    if(trajectory.contains(s)){
                        double[] coord = trajectory.getVector2(s);
                        try {
                            newPoints.add(new Point(s, coord[0], coord[1]));
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                points.clear();
                trajectory.setPoints(newPoints);
            }

            return trajectory;
        }
    }
}
