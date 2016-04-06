package me.jackweath.mycoach;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class DataManage extends SQLiteOpenHelper{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "RunData.db";

    // Constructor method
    public DataManage(Context context) {
        // Superclass constructor called to create database
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        onCreate(this.getWritableDatabase());
    }

    // On create the databases are created (if they don't exist)
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SUMMARY);
        db.execSQL(SQL_CREATE_DETAILED);
        db.execSQL(SQL_CREATE_LIVE);
    }

    // Currently no special treatment for upgrading or downgrading the schema
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {  }

    /* START OF NEW STUFF ====== TAKE OUT OF WRITE UP INITIAL */
    public void processData(long max) {
        prevDist = prevTime = prevSteps = 0; // Reset carrier variables
        long spacing = 10; // 5 seconds = 10 * 0.5 seconds

        for (int i = 1; i < max; i += spacing) {
            ArrayList<String> tableRow = readTableRow("live", i);
            HashMap<String, String> detailedFormat = formatToDetailed(tableRow);

            addDetailedData(detailedFormat);
        }

        // ALWAYS use the last point
        addDetailedData(
                formatToDetailed(readTableRow("live", max))
        );
    }

    double prevDist;
    long prevTime;
    int prevSteps;
    private HashMap<String, String> formatToDetailed(ArrayList<String> liveRow) {
        HashMap<String, String> detailedRow = new HashMap<>();
        CustomLocation locat = new CustomLocation(liveRow.get(5));
        detailedRow.put("location", locat.toString());

        // Getting time
        long time = Long.parseLong(liveRow.get(1));
        detailedRow.put("time", String.valueOf(time));

        // Interval
        detailedRow.put("interval", liveRow.get(3));

        // Level
        detailedRow.put("level", liveRow.get(6));

        // Elapsed values need steps and distance
        int steps = Integer.parseInt(liveRow.get(2));
        double distance = Double.parseDouble(liveRow.get(4));

        long elapsedTime = time - prevTime;
        int elapsedSteps = steps - prevSteps;
        double elapsedDistance = distance - prevDist;

        // Stride
        Calc.ValStat stride = Calc.strideLength(elapsedDistance, elapsedSteps);
        detailedRow.put("stride", stride.value.toString());

        // Cadence
        Calc.ValStat cadence = Calc.cadence(elapsedSteps, elapsedTime);
        detailedRow.put("cadence", cadence.value.toString());

        // Speed
        Calc.ValStat speed = Calc.speedMS(elapsedDistance, elapsedTime);
        detailedRow.put("speed", speed.value.toString());

        // Elevation
        double elevation = locat.getElevation();
        detailedRow.put("elevation", String.valueOf(elevation));

        return detailedRow;
    }
    /* END OF NEW STUFF ====== TAKE OUT OF WRITE UP INITIAL */

    /* ************************************************************** */
    /*               Shorthand methods for generic tasks              */
    // Add a single row of live data
    public long addLiveData(double time, int steps, int interval, double distance,
                            String position, int level) {
        // Get database that is readable
        SQLiteDatabase db = this.getWritableDatabase();

        // Define row values
        ContentValues values = new ContentValues();
        values.put(DataContract.LiveColumns.COLUMN_TIME, time);
        values.put(DataContract.LiveColumns.COLUMN_STEPS, steps);
        values.put(DataContract.LiveColumns.COLUMN_INT, interval);
        values.put(DataContract.LiveColumns.COLUMN_DIST, distance);
        // Value cannot be a Location object
        values.put(DataContract.LiveColumns.COLUMN_POS, position);
        values.put(DataContract.LiveColumns.COLUMN_LEVEL, level);

        // Insert values into the database, in the live data table
        long newRowId = db.insert(
                DataContract.LiveColumns.TABLE_NAME,
                null,
                values);

        // Return the row id that the data was inserted into
        return newRowId;
    }

    // Adds a row of summary data
    public long addSummaryData(String name, String mode, int intervals, String date,
                               double distance, double time, int steps, int level) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Defining the values and putting them into the ContentValues object
        ContentValues values = new ContentValues();
        values.put(DataContract.SummaryColumns.COLUMN_NAME, name);
        values.put(DataContract.SummaryColumns.COLUMN_MODE, mode);
        values.put(DataContract.SummaryColumns.COLUMN_INTS, intervals);
        values.put(DataContract.SummaryColumns.COLUMN_DATE, date);
        values.put(DataContract.SummaryColumns.COLUMN_DIST, distance);
        values.put(DataContract.SummaryColumns.COLUMN_TIME, time);
        values.put(DataContract.SummaryColumns.COLUMN_STEPS, steps);
        values.put(DataContract.SummaryColumns.COLUMN_LEVEL, level);

        // Insert value (row id is returned, can be used in details)
        long newRowId = db.insert(
                DataContract.SummaryColumns.TABLE_NAME,
                null,
                values);

        // Return row id
        return newRowId;
    }

    // Adds a row of more detailed data (for post-run display)
    public long addDetailedData(HashMap<String, String> row) {

        int runid = Integer.parseInt(row.get("runID"));
        double time = Double.parseDouble(row.get("time"));
        int interval = Integer.parseInt(row.get("interval"));
        double speed = Double.parseDouble(row.get("speed"));
        double cadence = Double.parseDouble(row.get("cadence"));
        double stride = Double.parseDouble(row.get("stride"));
        String position = row.get("position");
        double elevation = Double.parseDouble(row.get("elevation"));
        int level = Integer.parseInt(row.get("level"));

        // Getting the database that we can write to
        SQLiteDatabase db = this.getWritableDatabase();

        // Defining the row values, and inserting them into the object
        ContentValues values = new ContentValues();
        values.put(DataContract.DetailColumns.COLUMN_RUNID, runid);
        values.put(DataContract.DetailColumns.COLUMN_TIME, time);
        values.put(DataContract.DetailColumns.COLUMN_INT, interval);
        values.put(DataContract.DetailColumns.COLUMN_SPEED, speed);
        values.put(DataContract.DetailColumns.COLUMN_CADENCE, cadence);
        values.put(DataContract.DetailColumns.COLUMN_TIME, stride);
        values.put(DataContract.DetailColumns.COLUMN_POS, position);
        values.put(DataContract.DetailColumns.COLUMN_LEVEL, elevation);
        values.put(DataContract.DetailColumns.COLUMN_LEVEL, level);

        // Insert value (row id is returned, can be used to check for errors if necessary)
        long newRowId = db.insert(
                DataContract.SummaryColumns.TABLE_NAME,
                null,
                values);

        // Return row id
        return newRowId;
    }

    public void updateTitle(int id, String newTitle) {
        SQLiteDatabase db = this.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DataContract.SummaryColumns.COLUMN_NAME, newTitle);

        // Which row to update, based on the ID
        String selection = DataContract._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        // Updating the title of a run in the summary table
        int count = db.update(
                DataContract.SummaryColumns.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public void deleteRun(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define 'where' part of query.
        String selection = DataContract._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(id) };
        // Issue SQL statement.
        db.delete(DataContract.SummaryColumns.TABLE_NAME, selection, selectionArgs);

        // Repeat, but remove values with RUNID of parameter in the details table
        selection = DataContract.DetailColumns.COLUMN_RUNID + " LIKE ?";
        db.delete(DataContract.SummaryColumns.TABLE_NAME, selection, selectionArgs);
    }

    // Reading a row from one of 3 database tables
    public ArrayList<String> readTableRow(String table, long id) {
        // Getting readable database this time
        SQLiteDatabase db = this.getReadableDatabase();

        if (table == "summary" || table == "live" || table =="detailed") {
            String[] columns;
            String tableName;
            // Defining the columns that I want values from (all)
            switch(table){
                case "summary":
                    columns = new String[]{
                            DataContract._ID,
                            DataContract.SummaryColumns.COLUMN_NAME,
                            DataContract.SummaryColumns.COLUMN_MODE,
                            DataContract.SummaryColumns.COLUMN_INTS,
                            DataContract.SummaryColumns.COLUMN_DATE,
                            DataContract.SummaryColumns.COLUMN_DIST,
                            DataContract.SummaryColumns.COLUMN_TIME,
                            DataContract.SummaryColumns.COLUMN_STEPS,
                            DataContract.SummaryColumns.COLUMN_LEVEL
                    };
                    tableName = DataContract.SummaryColumns.TABLE_NAME;
                    break;
                case "live":
                    columns = new String[]{
                            DataContract._ID,
                            DataContract.LiveColumns.COLUMN_TIME,
                            DataContract.LiveColumns.COLUMN_STEPS,
                            DataContract.LiveColumns.COLUMN_INT,
                            DataContract.LiveColumns.COLUMN_DIST,
                            DataContract.LiveColumns.COLUMN_POS,
                            DataContract.LiveColumns.COLUMN_LEVEL
                    };

                    tableName = DataContract.LiveColumns.TABLE_NAME;
                    break;
                case "detailed":
                    // Array of columns
                    columns = new String[]{"0"};
                    tableName = DataContract.LiveColumns.TABLE_NAME;
                    break;
                default:
                    columns = new String[]{"0"};
                    tableName = DataContract.LiveColumns.TABLE_NAME;
            }

            // Cursor created, used to select data.
            Cursor cursor = db.query(tableName,     // Table name
                    columns, " id = ?",                         // WHERE
                    new String[]{String.valueOf(id)},           // criteria
                    null,                                       //
                    null,                                       //
                    null,                                       //
                    null);                                      //

            // Null arrayList initiated
            ArrayList<String> vals = new ArrayList<>();

            // If something was found
            if (cursor != null && cursor.moveToFirst()) {
                // Move the cursor to the first value in the selected data
                cursor.moveToFirst();

                // Iterate through columns, extracting data
                for (int i = 0; i < cursor.getColumnCount(); i++){
                    // Add the data (in string form), to the arrayList
                    vals.add(cursor.getString(i));
                }

                cursor.close(); // CURSOR MUST BE CLOSED!
            } else {
                // How to react when nothing is present!
                // Need to decide on this
            }

            // Return the array list
            return vals;
        } else {
            return new ArrayList<>();
        }
    }

    // Method to wipe collected data (tables)
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Execute SQL command to drop tables
        db.execSQL(SQL_DELETE_SUMMARY);
        db.execSQL(SQL_DELETE_DETAILED);
        db.execSQL(SQL_DELETE_LIVE);
    }

    // Clearing live data (done after summary data is created)
    public void freshLiveData() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Execute SQL command
        db.execSQL(SQL_DELETE_LIVE);
        db.execSQL(SQL_CREATE_LIVE);
    }


    /* ********************************************************* */
    /*                  Database schema is defined               */
    public final class DataContract {
        // To prevent someone from accidentally instantiating the contract class,
        // given it an empty constructor.
        public DataContract() {}

        public static final String _ID = "id";

        // Defining the schema for the past run summary data table
        public abstract class SummaryColumns implements BaseColumns {
            public static final String TABLE_NAME = "summaryPost";
            public static final String COLUMN_NAME = "name";
            public static final String COLUMN_MODE = "mode";
            public static final String COLUMN_INTS = "intervals";
            public static final String COLUMN_DATE = "date";
            public static final String COLUMN_DIST = "distance";
            public static final String COLUMN_TIME = "time";
            public static final String COLUMN_STEPS = "steps";
            public static final String COLUMN_LEVEL = "level";
        }

        // Defining the schema for the detailed past data table
        public abstract class DetailColumns implements BaseColumns {
            public static final String TABLE_NAME = "detailedPost";
            public static final String COLUMN_RUNID = "runID";
            public static final String COLUMN_TIME = "time";
            public static final String COLUMN_INT = "interval";
            public static final String COLUMN_SPEED = "speed";
            public static final String COLUMN_CADENCE = "cadence";
            public static final String COLUMN_STRIDE = "stride";
            public static final String COLUMN_POS = "position";
            public static final String COLUMN_ELEVA = "elevation";
            public static final String COLUMN_LEVEL = "level";
        }

        // Defining the schema for the live data table
        public abstract class LiveColumns implements BaseColumns {
            public static final String TABLE_NAME = "liveData";
            public static final String COLUMN_TIME = "time";
            public static final String COLUMN_STEPS = "steps";
            public static final String COLUMN_INT = "interval";
            public static final String COLUMN_DIST = "distance";
            public static final String COLUMN_POS = "position";
            public static final String COLUMN_LEVEL = "level";
        }
    }

    // All SQLite types
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String NULL_TYPE = " NULL";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ", ";

    // SQL command for creating past run summary table
    private static final String SQL_CREATE_SUMMARY =
            "CREATE TABLE IF NOT EXISTS " + DataContract.SummaryColumns.TABLE_NAME + " (" +
                    DataContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DataContract.SummaryColumns.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    DataContract.SummaryColumns.COLUMN_MODE + TEXT_TYPE + COMMA_SEP +
                    DataContract.SummaryColumns.COLUMN_INTS + INT_TYPE + COMMA_SEP +
                    DataContract.SummaryColumns.COLUMN_DATE + TEXT_TYPE + COMMA_SEP +
                    DataContract.SummaryColumns.COLUMN_DIST + REAL_TYPE + COMMA_SEP +
                    DataContract.SummaryColumns.COLUMN_TIME + REAL_TYPE + COMMA_SEP +
                    DataContract.SummaryColumns.COLUMN_STEPS + INT_TYPE + COMMA_SEP +
                    DataContract.SummaryColumns.COLUMN_LEVEL + INT_TYPE + " )";

    // SQL command for creating detauled past data table
    private static final String SQL_CREATE_DETAILED =
            "CREATE TABLE IF NOT EXISTS " + DataContract.DetailColumns.TABLE_NAME + " (" +
                    DataContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DataContract.DetailColumns.COLUMN_RUNID + INT_TYPE + COMMA_SEP +
                    DataContract.DetailColumns.COLUMN_TIME + REAL_TYPE + COMMA_SEP +
                    DataContract.DetailColumns.COLUMN_INT + INT_TYPE + COMMA_SEP +
                    DataContract.DetailColumns.COLUMN_SPEED + REAL_TYPE + COMMA_SEP +
                    DataContract.DetailColumns.COLUMN_CADENCE + REAL_TYPE + COMMA_SEP +
                    DataContract.DetailColumns.COLUMN_STRIDE + REAL_TYPE + COMMA_SEP +
                    DataContract.DetailColumns.COLUMN_POS + BLOB_TYPE +  COMMA_SEP +
                    DataContract.DetailColumns.COLUMN_ELEVA + REAL_TYPE + COMMA_SEP +
                    DataContract.DetailColumns.COLUMN_LEVEL + INT_TYPE + " )";

    // SQL command for creating the live data table
    private static final String SQL_CREATE_LIVE =
            "CREATE TABLE IF NOT EXISTS " + DataContract.LiveColumns.TABLE_NAME + " (" +
                    DataContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DataContract.LiveColumns.COLUMN_TIME + REAL_TYPE + COMMA_SEP +
                    DataContract.LiveColumns.COLUMN_STEPS + INT_TYPE + COMMA_SEP +
                    DataContract.LiveColumns.COLUMN_INT + INT_TYPE + COMMA_SEP +
                    DataContract.LiveColumns.COLUMN_DIST + REAL_TYPE + COMMA_SEP +
                    DataContract.LiveColumns.COLUMN_POS + BLOB_TYPE + COMMA_SEP +
                    DataContract.LiveColumns.COLUMN_LEVEL + INT_TYPE + " )";


    // More SQL commands to drop each of the tables
    private static final String SQL_DELETE_SUMMARY =
            "DROP TABLE IF EXISTS " + DataContract.SummaryColumns.TABLE_NAME;
    private static final String SQL_DELETE_DETAILED =
            "DROP TABLE IF EXISTS " + DataContract.DetailColumns.TABLE_NAME;
    private static final String SQL_DELETE_LIVE =
            "DROP TABLE IF EXISTS " + DataContract.LiveColumns.TABLE_NAME;

}
