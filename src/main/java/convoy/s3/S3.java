package convoy.s3;

import calculation.ListGeneric;
import clustering.DBSCAN;
import datetime.OneTimestamp;
import model.Trajectory;

import java.util.*;

public class S3 {
    public String fromTS;
    public String toTS;
    public int gapInSeconds;
    public List<Trajectory> trajs;
    public int minpts;
    public double epsilon;
    public int m,k,w;
    public boolean log;

    public S3(String fromTS, String toTS, int gapInSeconds, List<Trajectory> trajs, int minpts, double epsilon, int m, int k, int w, boolean log) {
        this.fromTS = fromTS;
        this.toTS = toTS;
        this.gapInSeconds = gapInSeconds;
        this.trajs = trajs;
        this.minpts = minpts;
        this.epsilon = epsilon;
        this.m = m;
        this.k = k;
        this.w = w;
        this.log = log;
    }

    public List<EvolvingConvoy> go(){
        DBSCAN dbscan = new DBSCAN(minpts, epsilon);
        List<EvolvingConvoy> evolvingConvoysAnswer = new ArrayList<>();
        List<EvolvingConvoy> evolvingConvoysCurrent = new ArrayList<>();
        int counter = 1;
        for (String ts = fromTS; ts.compareTo(toTS) <= 0;){
            //获取快照坐标
            String tsCOPY = ts;
            List<Trajectory> snapshot = ListGeneric.filter(trajs, t -> t.contains(tsCOPY));
            Map<String, double[]> coords = new HashMap<>();
            for(Trajectory traj : snapshot)
                coords.put(traj.getID(), traj.getVector2(ts));

            //获取DBSCAN聚类结果
            List<Set<String>> clusters = dbscan.cluster(coords);
            List<EvolvingConvoy> newEvolvingConvoysCurrent = new ArrayList<>();

            boolean[] matched = new boolean[clusters.size()];
            for(int i = 0; i < evolvingConvoysCurrent.size(); i++){
                boolean extended = false;
                for(int j = 0; j < clusters.size(); j++){
                    Set<String> pmv = evolvingConvoysCurrent.get(i).permanentMembers();
                    Set<String> help = new HashSet<>();
                    help.addAll(pmv); help.addAll(clusters.get(j));
                    if(pmv.size() + clusters.get(j).size() - help.size() >= m){
                        newEvolvingConvoysCurrent.add(evolvingConvoysCurrent.get(i).extend(clusters.get(j), ts, counter));
                        extended = matched[j] = true;
                    }
                }
                if(! extended){
                    if(evolvingConvoysCurrent.get(i).size() >= w)
                        evolvingConvoysAnswer.add(evolvingConvoysCurrent.get(i));
                }
            }

            //检查matched
            for (int i = 0; i < matched.length; i++){
                if((! matched[i]) && clusters.get(i).size() >= m){
                    EvolvingConvoy newConvoy = new EvolvingConvoy(clusters.get(i), ts, counter, m, k, w);
                    newEvolvingConvoysCurrent.add(newConvoy);
                }
            }

            evolvingConvoysCurrent = newEvolvingConvoysCurrent;

            if(log)
                System.out.println(ts);
            ts = OneTimestamp.add(ts, 0, 0, gapInSeconds, OneTimestamp.formatter1);
            counter += 1;
        }

        //检查Vcur
        for (EvolvingConvoy convoy : evolvingConvoysCurrent){
            if(convoy.size() >= w)
                evolvingConvoysAnswer.add(convoy);
        }

        return evolvingConvoysAnswer;
    }
}
