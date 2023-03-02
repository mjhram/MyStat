package mjhram.com.mystat;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PerDayListActivity extends ListActivity {
    public databaseHandler dbHandler;

    /*public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        minmaxArrayAdapter adapter = (minmaxArrayAdapter)getListView().getAdapter();

        switch (item.getItemId()) {
            case R.id.menu_use:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                long id = this.getListView().getItemIdAtPosition(info.position);
                elec_info eInfo = adapter.mobInfoArray.get((int)id);
                Intent myIntent = new Intent(PerDayListActivity.this, MainActivity.class);
                myIntent.putExtra("eInfo", eInfo);
                setResult(2,myIntent);
                finish();//finishing activity
                break;
            case R.id.menu_orderbyprevdate:
                List<elec_info> values = dbHandler.getAllRecords(SortType.PrevDate);
                adapter.setValues(values);
                break;
            case R.id.menu_orderbysavedate:
                values = dbHandler.getAllRecords(SortType.SaveDate);
                adapter.setValues(values);
                break;
            case R.id.menu_none:
                values = dbHandler.getAllRecords(SortType.None);
                adapter.setValues(values);
                break;
        }
        return true;
    }*/

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHandler = new databaseHandler(this);
        List<elec_info> values = dbHandler.getAllRecords(SortType.PerDay, MainActivity.currentUserIdx);
        minmaxArrayAdapter adapter = new minmaxArrayAdapter(this, values);
        setListAdapter(adapter);
        final ListView listView = getListView();
        //registerForContextMenu(listView);
    }
}

class minmaxArrayAdapter extends ArrayAdapter<elec_info> {
    private final PerDayListActivity theListActivity;
    public List<elec_info> mobInfoArray;

    public minmaxArrayAdapter(PerDayListActivity aListActivity, List<elec_info> values) {
        super(aListActivity, R.layout.info_row_layout, values);
        this.theListActivity = aListActivity;
        this.mobInfoArray = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView txt_tmp;

        LayoutInflater inflater = (LayoutInflater) theListActivity
                .getSystemService(theListActivity.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.per_day_list_layout, parent, false);
        if(mobInfoArray.size() <= position) {
            showInfo(mobInfoArray.get(0), rowView);
            Toast.makeText(getContext(), "Test", Toast.LENGTH_SHORT).show();
        }else {
            showInfo(mobInfoArray.get(position), rowView);
        }
        return rowView;
    }

    private void showInfo(elec_info info, View rowView) {
        TextView txt_tmp;

        txt_tmp = (TextView) rowView.findViewById(R.id.tvDay);
        txt_tmp.setText(info.date);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvMin);
        txt_tmp.setText(String.format("%2.2f", info.min));
        txt_tmp = (TextView) rowView.findViewById(R.id.tvMax);
        txt_tmp.setText(String.format("%2.2f", info.max));
    }

    /*private String getDate(String time) {
        Timestamp timestamp = Timestamp.valueOf(time);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        TimeZone tz = TimeZone.getDefault();

        calendar.setTimeInMillis(timestamp.getTime());
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
        Date currenTimeZone = (Date)calendar.getTime();
        String date = sdf.format(currenTimeZone);

        return date;
    }*/

    public void setValues(List<elec_info> listValues){
        mobInfoArray.clear();
        mobInfoArray = null;
        mobInfoArray = listValues;
        clear();
        addAll(listValues);
        notifyDataSetChanged();
    }
}
