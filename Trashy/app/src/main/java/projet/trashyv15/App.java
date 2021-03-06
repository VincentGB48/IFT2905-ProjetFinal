package projet.trashyv15;

import android.app.Application;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class App extends Application {

    private static TrashyDBHelper mDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = new TrashyDBHelper(getApplicationContext());
    }

    public static TrashyDBHelper getDBHelper() {
        return mDBHelper;
    }
    public static void databasePutNotification(int notif) {

        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TrashyDBContract.TrashyDBTableNotification.COLUMN_NOTIF, notif);

        db.insert(TrashyDBContract.TrashyDBTableNotification.TABLE_NAME, null, values);

    }
    public static boolean databaseGetIsNotifActivated() {

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] projection = {
                TrashyDBContract.TrashyDBTableNotification.COLUMN_NOTIF
        };
        String selection = TrashyDBContract.TrashyDBTableNotification.COLUMN_NOTIF + " = ?";
        String[] selectionArgs = { "1" };

        Cursor cursor = db.query(
                TrashyDBContract.TrashyDBTableNotification.TABLE_NAME,
                projection, selection, selectionArgs,
                null, null, null
        );

        if (cursor.getCount() == 0) {
            System.out.println("Notif not activated");

            cursor.close();
            return false;
        }
        else {

        }
        cursor.close();

        return true;
    }
    public static long databasePutSchedule(String sWeekday,
                                           String sCycle,
                                           String sOnce,
                                           String sHourin,
                                           String sHourout,
                                           String sDatein,
                                           String sDateout,
                                           String type) {

        int weekday = Integer.parseInt(sWeekday);
        int cycle = Integer.parseInt(sCycle);
        boolean once = Boolean.parseBoolean(sOnce);
        int hourIn = Integer.parseInt(sHourin);
        int hourOut = Integer.parseInt(sHourout);

        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_WEEKDAY, weekday);
        values.put(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_CYCLE, cycle);
        values.put(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_ONCE, once);
        values.put(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_HOUR_IN, hourIn);
        values.put(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_HOUR_OUT, hourOut);
        values.put(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_DATE_IN, sDatein);
        values.put(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_DATE_OUT, sDateout);
        values.put(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_TYPE, type);

        long id = db.insert(TrashyDBContract.TrashyDBTableSchedules.TABLE_NAME, null, values);
        System.out.println("id=" + id + " type=" + type);

        return id;
    }

    public static long databasePutNeighbourhood(String name, boolean isCurrent) {

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String selection = TrashyDBContract.TrashyDBTableNeighbourhoods.COLUMN_NAME_NAME       + " = ? AND " +
                           TrashyDBContract.TrashyDBTableNeighbourhoods.COLUMN_NAME_IS_CURRENT + " = ?";
        String[] selectionArgs = { name, isCurrent ? "1" : "0" };

        Cursor cursor = db.query(
                TrashyDBContract.TrashyDBTableNeighbourhoods.TABLE_NAME,
                null,
                selection, selectionArgs,
                null, null, null
        );

        // If no rows match, then we must add it to the database. Else we just print but we could update.
        long neighbourdhoodID = 0;
        if (cursor.getCount() == 0) {

            db = mDBHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(TrashyDBContract.TrashyDBTableNeighbourhoods.COLUMN_NAME_NAME, name);
            values.put(TrashyDBContract.TrashyDBTableNeighbourhoods.COLUMN_NAME_IS_CURRENT, isCurrent);

            neighbourdhoodID = db.insert(TrashyDBContract.TrashyDBTableNeighbourhoods.TABLE_NAME, null, values);

            System.out.println("Inserted neighbourhood '" + name + "'");
        }
        else {

            cursor.moveToNext();
            neighbourdhoodID = cursor.getLong(cursor.getColumnIndexOrThrow(TrashyDBContract.TrashyDBTableNeighbourhoods._ID));

            System.out.println("Row already exists, id is " + neighbourdhoodID);
        }
        cursor.close();

        return neighbourdhoodID;
    }

    public static void databaseLinkNeighbourhoodNameAndSchedule(String neighbourhoodName, long scheduleID) {

        // Get the corresponding neighbourhood
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        String[] projection = {
                TrashyDBContract.TrashyDBTableNeighbourhoods._ID,
                TrashyDBContract.TrashyDBTableNeighbourhoods.COLUMN_NAME_NAME
        };
        String selection = TrashyDBContract.TrashyDBTableNeighbourhoods.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs = { neighbourhoodName };

        Cursor cursor = db.query(
                TrashyDBContract.TrashyDBTableNeighbourhoods.TABLE_NAME,
                projection, selection, selectionArgs,
                null, null, null
        );

        if (cursor.getCount() < 1) {
            System.out.println("No matching neighbourhood with name '" + neighbourhoodName + "' in database");
        }
        else {

            cursor.moveToNext();
            long neighbourhoodID = cursor.getLong(cursor.getColumnIndexOrThrow(
                    TrashyDBContract.TrashyDBTableNeighbourhoods._ID
            ));

            databaseLinkNeighbourhoodAndSchedule(neighbourhoodID, scheduleID);
        }
        cursor.close();
    }

    public static void databaseLinkNeighbourhoodAndSchedule(long neighbourhoodID, long scheduleID) {

        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TrashyDBContract.TrashyDBTableNeighbourhoodHasSchedule.COLUMN_NAME_NEIGHBOURHOOD_ID, neighbourhoodID);
        values.put(TrashyDBContract.TrashyDBTableNeighbourhoodHasSchedule.COLUMN_NAME_SCHEDULE_ID, scheduleID);

        db.insert(TrashyDBContract.TrashyDBTableNeighbourhoodHasSchedule.TABLE_NAME, null, values);
    }

    public static long databaseGetCurrentNeighbourhoodID() {

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] projection = {
                TrashyDBContract.TrashyDBTableNeighbourhoods._ID,
                TrashyDBContract.TrashyDBTableNeighbourhoods.COLUMN_NAME_IS_CURRENT
        };
        String selection = TrashyDBContract.TrashyDBTableNeighbourhoods.COLUMN_NAME_IS_CURRENT + " = ?";
        String[] selectionArgs = { "1" };

        Cursor cursor = db.query(
                TrashyDBContract.TrashyDBTableNeighbourhoods.TABLE_NAME,
                projection, selection, selectionArgs,
                null, null, null
        );

        long neighbourhoodID = -1;
        if (cursor.getCount() != 1) {
            System.out.println("No current neighbourhood");
        }
        else {

            cursor.moveToNext();
            neighbourhoodID = cursor.getLong(cursor.getColumnIndexOrThrow(
                    TrashyDBContract.TrashyDBTableNeighbourhoods._ID
            ));
        }
        cursor.close();

        return neighbourhoodID;
    }

    public static int[] databaseGetNextDay(char type) {

        int[] retval = new int[2];

        long currentNeighbourhoodID = databaseGetCurrentNeighbourhoodID();

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] projection = {
                TrashyDBContract.TrashyDBTableNeighbourhoodHasSchedule.COLUMN_NAME_NEIGHBOURHOOD_ID,
                TrashyDBContract.TrashyDBTableNeighbourhoodHasSchedule.COLUMN_NAME_SCHEDULE_ID
        };
        String selection = TrashyDBContract.TrashyDBTableNeighbourhoodHasSchedule.COLUMN_NAME_NEIGHBOURHOOD_ID + " = ?";
        String[] selectionArgs = { String.valueOf(currentNeighbourhoodID) };

        Cursor cursor = db.query(
                TrashyDBContract.TrashyDBTableNeighbourhoodHasSchedule.TABLE_NAME,
                projection, selection, selectionArgs,
                null, null, null
        );

        while (cursor.moveToNext()) {

            long scheduleID = cursor.getLong(cursor.getColumnIndexOrThrow(
                    TrashyDBContract.TrashyDBTableNeighbourhoodHasSchedule.COLUMN_NAME_SCHEDULE_ID
            ));

            SQLiteDatabase db2 = mDBHelper.getReadableDatabase();
            String[] proj2 = {
                    TrashyDBContract.TrashyDBTableSchedules._ID,
                    TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_WEEKDAY,
                    TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_HOUR_IN,
                    TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_TYPE
            };
            String sel2 = TrashyDBContract.TrashyDBTableSchedules._ID + " = ? AND " + TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_TYPE + " = ?";
            String[] selArgs2 = { String.valueOf(scheduleID), String.valueOf(type) };

            Cursor cur2 = db2.query(
                    TrashyDBContract.TrashyDBTableSchedules.TABLE_NAME,
                    proj2, sel2, selArgs2,
                    null, null, null
            );

            if (cur2.getCount() > 0) {

                cur2.moveToNext();

                retval[0] = cur2.getInt(cur2.getColumnIndexOrThrow(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_WEEKDAY));
                retval[1] = cur2.getInt(cur2.getColumnIndexOrThrow(TrashyDBContract.TrashyDBTableSchedules.COLUMN_NAME_HOUR_IN));
            }
            else {
                System.out.println("[SQL] No matching rows with scheduleID '" + scheduleID + "' and type '" + type + "'");
            }

            cur2.close();
        }
        cursor.close();
        return retval;
    }

    @Override
    public void onTerminate() {
        mDBHelper.close();
        super.onTerminate();
    }
}
