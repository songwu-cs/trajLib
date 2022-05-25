package convoy.s3;

import calculation.ListGeneric;
import calculation.MapGeneric;
import calculation.SetGeneric;

import java.io.PrintWriter;
import java.util.*;

public class EvolvingConvoy {
    public int startT;//从1开始计数
    public List<String> timestamps;

    public List<Integer> clusterID;
    public List<Set<String>> clusters;
    public List<Map<String, Integer>> trackers;
    public int m, k, w;

    public EvolvingConvoy(int m, int k, int w) {
        this.m = m;
        this.k = k;
        this.w = w;
    }

    public EvolvingConvoy(Set<String> cluster, String startTimestamp, int startT, int m, int k, int w, int cluID) {
        timestamps = new ArrayList<>();
        clusterID = new ArrayList<>();
        clusters = new ArrayList<>();
        trackers = new ArrayList<>();

        this.startT = startT;
        timestamps.add(startTimestamp);
        clusterID.add(cluID);
        clusters.add(cluster);
        Map<String, Integer> firstTracker = new HashMap<>();
        for(String s : cluster)
            firstTracker.put(s, 1);
        trackers.add(firstTracker);

        this.m = m;
        this.k = k;
        this.w = w;
    }

    //ecID, t, tID, shipID, count, role, stageID, clusterID
    public List<String> toString(String ecID) {
        List<String> answer = new ArrayList<>();

        int[] stageID = new int[size()];
        stageID[w - 1] = 1;
        int nextStageID = 2;
        Set<String> previousMembers = members(timestamps.get(w-1));
        for(int i = w; i < size(); i++){
            Set<String> nowMembers = members(timestamps.get(i));
            if(SetGeneric.equals(previousMembers, nowMembers)){
                stageID[i] = nextStageID - 1;
            }else {
                stageID[i] = nextStageID++;
                previousMembers = nowMembers;
            }
        }

        int tID = startT;
        int arrayIndex = 0;
        for(String ts : timestamps){
            Set<String> permM = permanentMembers(ts);
            Set<String> dynaM = dynamicMembers(ts);
            Map<String, Integer> counter = trackers.get(arrayIndex);
            for(String perm : permM)
                answer.add(String.join(",", ecID, ts, tID+"", perm, counter.get(perm)+"", "Static", stageID[arrayIndex]+"", clusterID.get(arrayIndex)+""));
            for(String dyna : dynaM)
                answer.add(String.join(",", ecID, ts, tID+"", dyna, counter.get(dyna)+"", "Dynamic", stageID[arrayIndex]+"", clusterID.get(arrayIndex)+""));

            tID++;
            arrayIndex++;
        }

        return answer;
    }

    public void setTimestamps(List<String> timestamps){
        this.timestamps = timestamps;
    }

    public void setClusterID(List<Integer> clusterID) {
        this.clusterID = clusterID;
    }

    public void setClusters(List<Set<String>> clusters) {
        this.clusters = clusters;
    }

    public void setTracker(List<Map<String, Integer>> tracker) {
        this.trackers = tracker;
    }

    public int size(){
        return timestamps.size();
    }

    public EvolvingConvoy copy(){
        EvolvingConvoy evolvingConvoy = new EvolvingConvoy(m, k, w);

        evolvingConvoy.startT = startT;

        evolvingConvoy.setTimestamps(ListGeneric.copy(timestamps));
        evolvingConvoy.setClusterID(ListGeneric.copy(clusterID));

        List<Set<String>> lss = new ArrayList<>();
        lss.addAll(clusters);
//        for(Set<String> ss : clusters)
//            lss.add(SetGeneric.copy(ss));
        evolvingConvoy.setClusters(lss);

        List<Map<String, Integer>> lmsi = new ArrayList<>();
        lmsi.addAll(trackers);
//        for(Map<String, Integer> msi : trackers)
//            lmsi.add(MapGeneric.copy(msi));
        evolvingConvoy.setTracker(lmsi);

        return evolvingConvoy;
    }

    public Set<String> members(){
        Set<String> both = new HashSet<>();
        both.addAll(permanentMembers());
        both.addAll(dynamicMembers());
        return both;
    }

    public Set<String> members(String timestamp){
        Set<String> both = new HashSet<>();
        both.addAll(permanentMembers(timestamp));
        both.addAll(dynamicMembers(timestamp));
        return both;
    }

    public Set<String> permanentMembers(){
        return permanentHelper(trackers.get(trackers.size() - 1), trackers.size());
    }

    public Set<String> permanentMembers(String timestamp){
        int i = ListGeneric.firstIndex(timestamps, t -> t.equals(timestamp));
        return permanentHelper(trackers.get(i), i+1);
    }

    private Set<String> permanentHelper(Map<String, Integer> counter, int index){
        Set<String> answer = new HashSet<>();
        for(String s : counter.keySet()){
            if(counter.get(s) == w || (index < w && counter.get(s) == index))
                answer.add(s);
        }
        return answer;
    }

    public Set<String> dynamicMembers(){
        return dynamicHelper(trackers.get(trackers.size() - 1), trackers.size());
    }

    public Set<String> dynamicMembers(String timestamp){
        int i = ListGeneric.firstIndex(timestamps, t -> t.equals(timestamp));
        return dynamicHelper(trackers.get(i), i+1);
    }

    private Set<String> dynamicHelper(Map<String, Integer> counter, int index){
        Set<String> answer = new HashSet<>();
        if(index < w)
            return answer;
        for(String s : counter.keySet()){
            if(counter.get(s) < w && counter.get(s) >= k)
                answer.add(s);
        }
        return answer;
    }

    //id为数据集中的id
    public EvolvingConvoy extend(Set<String> cluster, String timestamp, int id, int cluID){
        EvolvingConvoy answer = copy();
        answer.clusters.add(cluster);
        answer.timestamps.add(timestamp);
        answer.clusterID.add(cluID);
        if(id - startT + 1 <= w){
            Map<String, Integer> lastTracker = MapGeneric.copy(trackers.get(trackers.size()-1));
            for(String s : cluster)
                lastTracker.put(s, lastTracker.getOrDefault(s,0) + 1);
            answer.trackers.add(lastTracker);
        }else {
            Set<String> expiredCluster = clusters.get(id - w - startT);
            Map<String, Integer> lastTracker = MapGeneric.copy(trackers.get(trackers.size()-1));
            for(String s : expiredCluster){
                lastTracker.put(s, lastTracker.get(s) - 1);
            }
            for(String s : cluster)
                lastTracker.put(s, lastTracker.getOrDefault(s, 0) + 1);
            answer.trackers.add(lastTracker);
        }
        return answer;
    }
}
