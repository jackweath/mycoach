package me.jackweath.mycoach;

import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.lang.Number;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Class to hold:
 *      - all functions for calculations
 *      - string rounding
 *      - value and status class
 */


public class Calc {
    // An object and constructor to return a double val, and a status (i.e, if an error occurred)
    public static class ValStat {
        Number value;
        boolean err;
        public ValStat (Number val, boolean error) {
            value = val;
            err = error;
        }

        // Checks for a zero in the denominator (parameter, badVal)
        public void zeroCheck (double badVal) {
            // Multiply by 1.0 to allow comparison to double for any number type
            if (badVal * 1.0  == 0.0) {
                err = true;
            } else {
                err = false;
            }
        }

        /* ************************************************** */
        /*             Rounding for String display            */
        public String roundDp(int dp, boolean floor) {
            double powerTen = Math.pow(10, dp);
            double roundNum;

            if (floor) {
                roundNum = (double) Math.floor(this.value.doubleValue() * powerTen ) / powerTen;
            } else {
                roundNum = Double.parseDouble(roundDp(dp));
            }

            return String.valueOf(roundNum);
        }

        public String roundDp(int dp) {
            double powerTen = Math.pow(10, dp);
            double roundNum = (double) Math.round(this.value.doubleValue() * powerTen ) / powerTen;

            return String.valueOf(roundNum);
        }

        public String round() {
            return String.valueOf(Math.round(this.value.doubleValue()));
        }
    }

    /* *********************************************** */
    /*            Calculations for Distance            */
    /*
    public static float distance(Location start, Location end) {
        double startLat = start.getLatitude();
        double startLon = start.getLongitude();
        double endLat = end.getLatitude();
        double endLon = end.getLongitude();

        float[] result = new float[1];

        Location.distanceBetween(startLat, startLon, endLat, endLon, result);
        double orig =  distanceOrg(start, end);
        float dist = result[0];

        Log.d("DIST_DEBUG", "Orig, Dist: " + (orig * 1000) + ", " + dist);
        Log.d("DIST_DEBUG", dist + "");
        return dist / 1000;
    }
    */

    public static double distance(Location start, Location end) {
        final double RADIUS = 6372.8; // Radius of the earth

        // Convert to radians, as required for formula
        double lat1 = Math.toRadians(start.getLatitude());
        double lon1 = Math.toRadians(start.getLongitude());

        double lat2 = Math.toRadians(end.getLatitude());
        double lon2 = Math.toRadians(end.getLongitude());

        // Calculate difference
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // Apply the formula
        double a = hav(dLat) + Math.cos(lat1) * Math.cos(lat2) * hav(dLon);
        double c = 2 * Math.asin(Math.sqrt(a));

        // Return the distance
        return RADIUS * c;
    }
    // Haversine formula method itself
    private static double hav(double l) {
        return Math.pow(Math.sin(l / 2), 2);
    }


    public static double milliToSeconds(long milli) {
        return ((double) milli) / 1000.0;
    }

    public static double milliToMinutes(long milli) {
        return ((double) milli) / 60000.0;
    }




    public static String roundSf(double doub, int sf) {
        BigDecimal bigDecNum = BigDecimal.valueOf(doub);
        BigDecimal roundedNum = bigDecNum.round(new MathContext(sf, RoundingMode.HALF_UP));

        return String.valueOf(roundedNum);
    }

    public static double msToKmH(double msVal) {
        return msVal * 3600 / 1000;
    }

    /* ********************************************* */
    /*             Secondary Calculations            */
    // ValStat objects are returned to give values and an error status
    public static ValStat strideLength(double dist, int steps) {
        ValStat valStat = new ValStat(0.0, false);
        valStat.zeroCheck(steps);

        if (!valStat.err) {
            valStat.value = dist * 1000 / steps;
        }

        return valStat;
    }

    // Speed given in metres per second
    public static ValStat speedMS(double dist, long time) {
        ValStat valStat = new ValStat(0.0, false);
        valStat.zeroCheck(time);
        if (!valStat.err) {
            valStat.value = dist / milliToSeconds(time);
        }

        return valStat;
    }

    // Speed given in minutes per kilometre
    public static ValStat speedMK(double dist, long time) {
        ValStat valStat = new ValStat(0.0, false);

        valStat.zeroCheck(dist);
        if (!valStat.err) {
            valStat.value = milliToMinutes(time) / dist;
        }

        return valStat;
    }

    public static ValStat cadence(int steps, long time) {
        ValStat valStat = new ValStat(0.0, false);
        valStat.zeroCheck(time);

        if (!valStat.err) {
            valStat.value = steps / milliToMinutes(time);
        }

        Log.d("DATADEBUG", steps + "/ " + milliToMinutes(time) + " = " + valStat.value);

        return valStat;
    }

    public static double calories(ValStat speed, long time, double weight) {
        double speedVal = msToKmH(speed.value.doubleValue());

        Log.d("CAL_DEBUG", speed.value + "m/s == " + speedVal + "km/h");
        double MET;

        if (speedVal <= 3) {
            MET = 0;
        } else if (speedVal <= 6) {
            MET = 6;
        } else if (speedVal < 8) {
            MET = 8;
        } else if (speedVal <= 8.4) {
            MET = 9;
        } else if (speedVal <= 9.7) {
            MET = 10.0;
        } else if (speedVal <= 10.8) {
            MET = 11.0;
        } else if (speedVal <= 11.3) {
            MET = 11.5;
        } else if (speedVal <= 12.1) {
            MET = 12.5;
        } else if (speedVal <= 12.9) {
            MET = 13.5;
        } else if (speedVal <= 13.8) {
            MET = 14;
        } else if (speedVal <= 14.5) {
            MET = 15;
        } else if (speedVal <= 16.1) {
            MET = 16;
        } else if (speedVal <= 17.5) {
            MET = 18;
        } else {
            MET = 0; // USER CANNOT BE RUNNING RIGHT?
        }

        double cals = weight * MET * (milliToMinutes(time) / 60);
        Log.d("CAL_DEBUG", cals + " cals burnt");
        return cals;
    }

    // Returns the milliseconds since the system clock was initiated
    public static long timeMilli() {
        return SystemClock.elapsedRealtime();
    }

    public static String formatTime(long time) {
        String formattedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1));

        return  formattedTime;
    }
}
