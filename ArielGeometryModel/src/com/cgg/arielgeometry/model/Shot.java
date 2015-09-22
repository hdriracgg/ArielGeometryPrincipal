/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometry.model;

import com.cgg.arielgeometry.model.types.XYLocation;

/**
 *
 * @author jgrimsdale
 */
public class Shot {

    public Integer shotid;  // is the time of the shot to nearest second since start of survey
    public XYLocation xylocation;
    public static Integer timezero;     // unix time at start of survey
    public static int dailystarttime;   // time in seconds after midnight that shooting is scheduled to start
    public static int dailyendtime;     // time in seconds after midnight that shooting is scheduled to end
    public static int shottimeinterval; // nominal time between shots

    /**
     * Creates an Integer id from day number and sp number for the day. id
     * corresponds to the unix time of the shot to nearest second
     *
     * @param day shooting day number
     * @param sp sp number in day
     */
    public void setid(int day, int sp) {
        shotid = timezero + day * 3600 * 24 + dailystarttime + sp * shottimeinterval;
    }

    /**
     * Returns the unix time of the shot given its id
     *
     * @param id The shot id (number starting from shot 0)
     * @return
     */
    public static int id2time(Integer id) {
        int shotsperday = (dailyendtime - dailystarttime) / shottimeinterval;
        int shootingday = id / shotsperday;
        int shotinday = id - (shootingday * shotsperday);
        int time = timezero + (shootingday * 3600 * 24) + dailystarttime + (shotinday * shottimeinterval);
        return time;
    }

    public static Integer time2id(int time) {
        int shotsperday = (dailyendtime - dailystarttime) / shottimeinterval;
        int timefromstart = time - timezero;
        int shootingday = timefromstart / (3600 * 24);
        int timefrommidnight = timefromstart % (3600 * 24);
        int shootingtimeinday = timefrommidnight - dailystarttime;
        int shotinday = shootingtimeinday / shottimeinterval;
        Integer id = shootingday * shotsperday + shotinday;
        return id;
    }

    public static void test() {
        System.out.println("id2time(0)" + id2time(0));
        System.out.println("id2time(3600)" + id2time(3600));
        System.out.println("id2time(6000)" + id2time(6000));

        System.out.println("time2id(id2time(0))" + time2id(id2time(0)));
        System.out.println("time2id(id2time(3600))" + time2id(id2time(3600)));
        System.out.println("time2id(id2time(6000))" + time2id(id2time(6000)));
        System.out.println("time2id(id2time(6000)+4)" + time2id(id2time(6000)+4));
    }
}
