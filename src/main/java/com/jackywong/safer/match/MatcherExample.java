package com.jackywong.safer.match;

/**
 * Created by huangziqi on 2019/7/16
 */
public class MatcherExample {

    interface Car {
        String runOnRoad();
    }
    static class Track implements Car{
        String name = "Track";
        @Override
        public String runOnRoad() {
            return "bubu";
        }
    }
    static class Bike implements Car {
        String name = "Bike";
        @Override
        public String runOnRoad() {
            return "dingding";
        }
    }

    public static Car factory(String key) {
        if(key.equals("bike")) {
            return new Bike();
        } else if(key.equals("track")){
            return new Track();
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        Car obj = factory("trac");

        //Use matcher
        long s = System.currentTimeMillis();
        String res = Matcher.<String>match(obj)
                .matchCases(Bike.class,b -> "My name is "+b.name+" and I "+b.runOnRoad())
                .matchCases(Track.class, t -> "My name is "+t.name+" and I "+t.runOnRoad())
                .matchDefault(() -> "I don't know what I am")
                .build()
                .get();
        long e = System.currentTimeMillis();
        System.out.println(res+ " ms:"+(e-s));

        //use normal
        s = System.currentTimeMillis();
        if(obj instanceof Bike){
            Bike b = (Bike) obj;
            res = "My name is "+b.name+" and I "+b.runOnRoad();
        } else if(obj instanceof Track) {
            Track t = (Track) obj;
            res = "My name is "+t.name+" and I "+t.runOnRoad();
        } else {
            res = "I don't know what I am";
        }
        e = System.currentTimeMillis();
        System.out.println(res+ " ms:"+(e-s));
    }
}
