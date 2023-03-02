package mjhram.com.mystat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohammad.haider on 5/16/2016.
 */
public class databaseHandler extends SQLiteOpenHelper {
        // All Static variables
        // Database
        private static final int DATABASE_VERSION = 5;
        public static final String DATABASE_NAME = "myData.db";
        private static final String KEY_ID = "No";

        // Contacts table name
        public static final String TABLE_DATA = "dataTable";
        public static final String TABLE_USERS = "usersTable";

        public databaseHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Creating Tables
        @Override
        public void onCreate(SQLiteDatabase db) {
             {
                String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_DATA + "("
                        + "No INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,  time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + " userId int(4) DEFAULT 0,"
                        + " userName varchar(100) DEFAULT 'User0',"
                        + " prevDateInMilliSec bigint  DEFAULT 0,"
                        //+ " nextDateInMilliSec bigint  DEFAULT 0,"
                        + " prevReading real  DEFAULT 0.0"
                        //+ " nextReading int(11)  DEFAULT 0,"
                        //+ " price varchar(15)  DEFAULT 0,"
                        //+ " calcStr varchar(100)  DEFAULT NULL"
                        + ")";
                Log.d("Test", CREATE_TABLE);
                db.execSQL(CREATE_TABLE);
                CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                         + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                         + " name varchar(100) DEFAULT 'User0',"
                         + " storedReading REAL DEFAULT 50.0"
                        + ")";
                 Log.d("Test", CREATE_TABLE);
                 db.execSQL(CREATE_TABLE);
                 String sql = "INSERT INTO " + TABLE_USERS + " (id, name,storedReading) VALUES(0, 'User0', 50.0)";
                 db.execSQL(sql);
            }
        }

        // Upgrading database
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(oldVersion<4) {
                db.execSQL("ALTER TABLE " + TABLE_DATA + "  ADD userId int(4) DEFAULT 0");
                String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                        + " name varchar(100) DEFAULT 'User0'"
                        + ")";
                Log.d("Test", CREATE_TABLE);
                db.execSQL(CREATE_TABLE);
                db.execSQL("ALTER TABLE " + TABLE_USERS + "  ADD storedReading REAL DEFAULT 50.0");
                String sql = "INSERT INTO " + TABLE_USERS + " (id, name) VALUES(0, 'User0')";
                db.execSQL(sql);
            } else if(oldVersion==4) {
                db.execSQL("ALTER TABLE " + TABLE_USERS + "  ADD storedReading REAL DEFAULT 50.0");
            } else {
                // Drop older table if existed
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
                // Create tables again
                onCreate(db);
            }
        }

        /**
         * All CRUD(Create, Read, Update, Delete) Operations
         */

        // Adding new record
        void addRecord(elec_info mobInfo,int id) {
            SQLiteDatabase db = this.getWritableDatabase();
            mobInfo.addRecord2db(db, id);
            db.close();
        }

    // Deleting single Record
    public void delRecord(elec_info eInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        String tmp = "No = "+eInfo.id;
        int a = db.delete(TABLE_DATA, tmp, null);
        db.close();
    }

    // Deleting single Record
    public long addUser(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("storedReading", 50.0);
        long id = db.insert(TABLE_USERS, null, cv);
        db.close();
        return  id;
    }

    public void storeSettings(user_info usr) {
        SQLiteDatabase db = this.getWritableDatabase();
        String tmp = "id = "+usr.id;
        ContentValues cv = new ContentValues();
        cv.put("storedReading",usr.storedReading); //These Fields should be your String values of actual column names
        int a = db.update(TABLE_USERS, cv, tmp, null);
        db.close();
    }

    public user_info loadSettings(String usrName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String tmp = "name = '"+usrName+"'";
        String[] columns = {"id", "name", "storedReading"};
        Cursor cursor = db.query(TABLE_USERS, columns, tmp, null, null, null, null, null);
        user_info usr = new user_info();
        if (cursor.moveToFirst()) {
            usr.id = cursor.getInt(cursor.getColumnIndex("id"));
            usr.name = cursor.getString(cursor.getColumnIndex("name"));
            usr.storedReading = cursor.getFloat(cursor.getColumnIndex("storedReading"));
        }
        db.close();
        return usr;
    }

    // Getting All records
    public List<elec_info> getAllRecords(SortType sortType, int id) {
        List<elec_info> cInfoList = new ArrayList<elec_info>();
        // Select All Query
        String orderStr ="";
        if(sortType == SortType.PrevDate) {
            orderStr = " ORDER BY prevDateInMilliSec DESC";
        } else if(sortType == SortType.SaveDate) {
            orderStr = " ORDER BY time DESC";
        } else {
            orderStr = " ORDER BY No DESC";
        }
        String whereStr = " WHERE userId="+id;
        String selectQuery;
        if(sortType == SortType.PerDay) {
            selectQuery = "SELECT  date(prevDateInMilliSec/1000,'unixepoch') as date1, min(prevReading) as min1, max(prevReading) as max1 FROM " + TABLE_DATA +whereStr+ " group by date1  ORDER BY date1 DESC";
        //select date(prevDateInMilliSec/1000, 'unixepoch')as date1, min(nextReading), max(nextReading) from elecUnits group by date1
        } else {
            selectQuery = "SELECT  * FROM " + TABLE_DATA + whereStr + orderStr;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                elec_info tmpMobInfo = elec_info.getInfoFromRow(cursor, sortType);
                // Adding cInfo to list
                cInfoList.add(tmpMobInfo);
            } while (cursor.moveToNext());
        }
        // return 3gTests list
        return cInfoList;
    }

    // Getting contacts Count
    public int getRecordsCount(int id) {
        String whereStr = " WHERE userId="+id;
        String countQuery = "SELECT  * FROM " + TABLE_DATA+whereStr;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public ArrayList<user_info> getUsers() {
        String sql = "SELECT  * FROM " + TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        ArrayList list =new ArrayList<String>();
        if (cursor.moveToFirst()) {
            do {
                user_info user = new user_info();
                user.name = cursor.getString(cursor.getColumnIndex("name"));
                user.id = cursor.getInt(cursor.getColumnIndex("id"));
                user.storedReading = cursor.getFloat(cursor.getColumnIndex("storedReading"));
                list.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
