package convoy.s3;

import calculation.ArrayDoubleTwo;
import clustering.DBSCAN;
import datetime.OneTimestamp;
import model.Trajectory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class S3NoStaticPoints extends S3{
    private double speedThreshold;// in knots per hour

    public S3NoStaticPoints(String fromTS, String toTS, int gapInSeconds, List<Trajectory> trajs, int m, int k, int w, boolean log, double speedThreshold, DBSCAN dbscan) {
        super(fromTS, toTS, gapInSeconds, trajs, m, k, w, log, dbscan);
        this.speedThreshold = speedThreshold;
    }

    @Override
    public List<Trajectory> filter(List<Trajectory> trajs, String timestamp) {
        List<Trajectory> filteredBySuper = super.filter(trajs, timestamp);

        List<Trajectory> answer = new ArrayList<>();
        for(Trajectory trajectory : filteredBySuper){
            String previousTS = OneTimestamp.add(timestamp, 0, 0, -1 * gapInSeconds, OneTimestamp.formatter1);
            if(trajectory.contains(previousTS)){
                double[] coord1 = trajectory.getVector2(previousTS);
                double[] coord2 = trajectory.getVector2(timestamp);
                double distance = ArrayDoubleTwo.euclidean(coord1, coord2);
                if(distance / gapInSeconds * 3.6 / 1.852 < speedThreshold)
                    continue;
            }
            answer.add(trajectory);
        }
        return answer;
    }
}
