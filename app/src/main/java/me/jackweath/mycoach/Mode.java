package me.jackweath.mycoach;

import android.util.Log;
import java.util.HashMap;

/**
 * Class is the structure of a mode (required at the start of every run)
 */

public class Mode {
    public final int level;
    public final String mode;   // Mode cannot be changed after initialisation
    public HashMap<String, Double> targets;

    public Mode(int pLevel, String pMode) {
        targets = new HashMap<>();

        this.level = pLevel;
        this.mode = pMode.toLowerCase();

        switch (this.mode) {
            case "open":
                // No requirements are set
                break;
            case "sprint":
                // Requirements set
                targets.put("time", 30 * Math.pow(1.1, this.level+1));
                targets.put("speed", Math.pow(7, 1-(this.level-1)*0.01));
                targets.put("break", 60.0);
                targets.put("intervals", Math.ceil(600.0 / targets.get("time")));
                break;
            case "pace":
                // Setting requirements
                targets.put("speed", Math.pow(8, 1-(this.level-1)*0.01));
                targets.put("distance", 0.7 * Math.pow(1.04, this.level - 1));
                targets.put("intervals", 3.0);
                targets.put("break", 60.0);
                break;
            default:
                // Log an error
                Log.e("Mode Error", "No valid mode (" + this.mode + ") given on mode object creation.");
        }
    }

    public HashMap<String, Double> compare(HashMap<String, Double> userVals) {
        HashMap<String, Double> comparison = new HashMap<>();

        // Loop through target keys (should be the same in userVals)
        for (String targName : targets.keySet()) {
            // If the target was given...
            if (userVals.containsKey(targName)) {
                // Calculate the percentage achieved
                double perc = userVals.get(targName) / targets.get(targName) * 100.0;
                comparison.put(targName, perc);     // Put the comparison into the HashMap
            } else {
                // Log an error if a target was not found
                if (targName != "intervals" && targName != "distance" && targName != "break" && targName != "time") {
                    Log.e("Target Error", "Given targets do not include " + targName +".");
                }
            }
        }

        return comparison;
    }

    public double compareAll(HashMap<String, Double> userVals) {
        double sum = 0.0;

        // Get a HashMap of all the comparisons
        HashMap<String, Double> comparison = compare(userVals);
        for (Double val: comparison.values()){
            sum += val;
        }

        // Return the mean percentage
        return sum / comparison.size();

    }
}
