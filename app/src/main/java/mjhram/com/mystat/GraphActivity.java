package mjhram.com.mystat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GraphActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_graph);

        Bundle b = getIntent().getExtras();
        SortType s = SortType.None; // or other values
        if(b != null)
            s = (SortType) b.get("sortType");

        GraphView graph = (GraphView) findViewById(R.id.graph);
        switch (s) {
            case PerDay:
                initGraph(graph);
                break;
            case None:
                initGraph2(graph);
                break;
        }

    }

    Date getDateFromMillis(long msec) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(msec);
        return c.getTime();
    }

    public void initGraph2(final GraphView graph) {
        int mLabels =3;
        databaseHandler dbHandler = new databaseHandler(this);
        List<elec_info> values = dbHandler.getAllRecords(SortType.PrevDate, MainActivity.currentUserIdx);
        int mSize = values.size();
        DataPoint dp[] = new DataPoint[mSize];
        //DataPoint dpmin[] = new DataPoint[mSize];
        //Date d2 = elec_info.stringToDate(values.get(0).time,"yyyy-MM-dd hh:mm");
        Date d2 = getDateFromMillis(values.get(0).prevDateInMilliSec);
        Date d1 = getDateFromMillis(values.get(mSize-1).prevDateInMilliSec);

        for(int j=mSize-1, k=0; j>=0; j--, k++) {
            Date dd = getDateFromMillis(values.get(j).prevDateInMilliSec);
            /*if(k==0) {
                d1=dd;
            }*/
            dp[k] =    new DataPoint(dd, values.get(j).prevReading);
            //dpmin[k] = new DataPoint(dd, values.get(j).min);
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dp/*new DataPoint[] {

                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        }*/);
        // enable scrolling
        graph.getViewport().setScrollable(true);
        // enable scaling
        graph.getViewport().setScalable(true);

        series.setTitle("Values");
        series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        graph.addSeries(series);

        /*LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(dpmin);
        series2.setTitle("Min");
        series2.setColor(Color.RED);
        series2.setDrawDataPoints(true);
        graph.addSeries(series2);
        */
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Date d = new java.sql.Date((long) dataPoint.getX());
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                String formatted = format1.format(d.getTime());
                Toast.makeText(graph.getContext(), String.format("%s,   %.2f", formatted, dataPoint.getY()), Toast.LENGTH_LONG).show();
            }
        });
        /*series2.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Date d = new java.sql.Date((long) dataPoint.getX());
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                String formatted = format1.format(d.getTime());
                Toast.makeText(graph.getContext(), String.format("%s,   %.2f", formatted, dataPoint.getY()), Toast.LENGTH_LONG).show();
            }
        });*/

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        //nf.setMinimumIntegerDigits(2);
        //graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));

        graph.getGridLabelRenderer().setLabelFormatter(new myDateAsXAxisLabelFormatter(graph.getContext(), nf));
        graph.getGridLabelRenderer().setNumHorizontalLabels(mLabels);

        // set manual x bounds to have nice steps
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d2.getTime());
        graph.getViewport().setXAxisBoundsManual(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not nessecary
        graph.getGridLabelRenderer().setHumanRounding(false);
    }

    public void initGraph(final GraphView graph) {
        int mLabels =3;
        databaseHandler dbHandler = new databaseHandler(this);
        List<elec_info> values = dbHandler.getAllRecords(SortType.PerDay, MainActivity.currentUserIdx);
        int mSize = values.size();
        DataPoint dp[] = new DataPoint[mSize];
        DataPoint dpmin[] = new DataPoint[mSize];
        Date d2 = elec_info.stringToDate(values.get(0).date,"yyyy-MM-dd");
        Date d1 = elec_info.stringToDate(values.get(mSize-1).date,"yyyy-MM-dd");

        for(int j=mSize-1, k=0; j>=0; j--, k++) {
            Date dd = elec_info.stringToDate(values.get(j).date,"yyyy-MM-dd");
            if(k==0) {
                d1=dd;
            }
            dp[k] =    new DataPoint(dd, values.get(j).max);
            dpmin[k] = new DataPoint(dd, values.get(j).min);
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dp/*new DataPoint[] {

                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        }*/);
        // enable scrolling
        graph.getViewport().setScrollable(true);
        // enable scaling
        graph.getViewport().setScalable(true);

        series.setTitle("Max");
        series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        graph.addSeries(series);

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(dpmin);
        series2.setTitle("Min");
        series2.setColor(Color.RED);
        series2.setDrawDataPoints(true);
        graph.addSeries(series2);

        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Date d = new java.sql.Date((long) dataPoint.getX());
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                String formatted = format1.format(d.getTime());
                Toast.makeText(graph.getContext(), String.format("%s,   %.2f", formatted, dataPoint.getY()), Toast.LENGTH_LONG).show();
            }
        });
        series2.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Date d = new java.sql.Date((long) dataPoint.getX());
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                String formatted = format1.format(d.getTime());
                Toast.makeText(graph.getContext(), String.format("%s,   %.2f", formatted, dataPoint.getY()), Toast.LENGTH_LONG).show();
            }
        });

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        //nf.setMinimumIntegerDigits(2);
        //graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));

        graph.getGridLabelRenderer().setLabelFormatter(new myDateAsXAxisLabelFormatter(graph.getContext(), nf));
        graph.getGridLabelRenderer().setNumHorizontalLabels(mLabels);

        // set manual x bounds to have nice steps
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d2.getTime());
        graph.getViewport().setXAxisBoundsManual(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not nessecary
        graph.getGridLabelRenderer().setHumanRounding(false);
    }
}

class myDateAsXAxisLabelFormatter extends com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter {
    protected NumberFormat yNumberFormatter;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy@hh");

    public myDateAsXAxisLabelFormatter(Context context, NumberFormat yFormat) {
        super(context);
        yNumberFormatter = yFormat;
    }

    @Override
    public String formatLabel(double value, boolean isValueX) {
        if(isValueX) {
            //return super.formatLabel(value, isValueX);
            return sdf.format(new Date((long)value));
        } else {
            return yNumberFormatter.format(value);
        }
    }
}
