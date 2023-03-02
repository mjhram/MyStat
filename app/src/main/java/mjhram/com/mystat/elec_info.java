package mjhram.com.mystat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mohammad.haider on 2/16/2015.
 */
public class elec_info implements Parcelable {
    //used for minmax calc
    String date;
    float min, max;

    //private MainActivity theActivity;
    public Long id;
    public String time;//used for sqliteDB timestamp
    public Long prevDateInMilliSec;
    //public Long nextDateInMilliSec;
    public float prevReading;
    public String note;
    //public Long nextReading;
    //public String price;
    //public String calculationString;

    final private String TABLE_Units = databaseHandler.TABLE_DATA;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeString(time);
        dest.writeLong(prevDateInMilliSec);
        //dest.writeLong(nextDateInMilliSec);
        dest.writeFloat(prevReading);
        dest.writeString(note);
        //dest.writeLong(nextReading);
        //dest.writeString(price);
        //dest.writeString(calculationString);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        //time = in.readString();
        prevDateInMilliSec=in.readLong();
        //nextDateInMilliSec=in.readLong();
        prevReading=in.readFloat();
        note = in.readString();
        //nextReading=in.readLong();
        //price = in.readString();
        //calculationString = in.readString();
    }

    //private Activity theActivity;
    public elec_info() {
    }

    public elec_info(Parcel in){
        //theActivity = activity;
        readFromParcel(in);
    }

    public static final Creator<elec_info> CREATOR = new Creator<elec_info>() {

        @Override
        public elec_info createFromParcel(Parcel source) {
            return new elec_info(source);
        }

        @Override
        public elec_info[] newArray(int size) {
            return new elec_info[size];
        }
    };


    //aFormat example = "EEE MMM d HH:mm:ss zz yyyy"
    static public Date stringToDate(String aDate,String aFormat) {
        if(aDate==null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat(aFormat);
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;
    }

    static public elec_info getInfoFromRow(Cursor in, SortType sortType) {
        elec_info tmpMobInfo = new elec_info();
        if(sortType == SortType.PerDay) {
            tmpMobInfo.date = in.getString(in.getColumnIndexOrThrow("date1"));
            tmpMobInfo.min = in.getFloat(in.getColumnIndexOrThrow("min1"));
            tmpMobInfo.max = in.getFloat(in.getColumnIndexOrThrow("max1"));
        } else {
            tmpMobInfo.id = in.getLong(in.getColumnIndexOrThrow("No"));
            tmpMobInfo.time = in.getString(in.getColumnIndexOrThrow("time"));
            tmpMobInfo.note = in.getString(in.getColumnIndexOrThrow("note"));
            tmpMobInfo.prevDateInMilliSec = in.getLong(in.getColumnIndexOrThrow("prevDateInMilliSec"));
            tmpMobInfo.prevReading = in.getFloat(in.getColumnIndexOrThrow("prevReading"));
        }
        return tmpMobInfo;
    }

    public void addRecord2db(SQLiteDatabase db, int id)
    {
        // Tag used to cancel the request
        final String tag_string = "addRecord";
        ContentValues params = new ContentValues();
        String tmp = prevDateInMilliSec.toString();
        params.put("prevDateInMilliSec", tmp);
        //tmp = nextDateInMilliSec.toString();
        //params.put("nextDateInMilliSec", tmp);
        tmp = String.format("%2.2f", prevReading);
        params.put("prevReading", tmp);
        params.put("note", note);
        params.put("userId", id);
        //tmp = nextReading.toString();
        //params.put("nextReading", tmp);
        //params.put("price", price);
        //params.put("calcStr", calculationString);
        long tmpL =  db.insert(TABLE_Units, null, params);
        Log.d("Test", Long.toString(tmpL));
    }
}
