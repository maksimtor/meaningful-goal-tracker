package org.maksimtoropygin.meaningfulgoaltracker.adapters;


import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.maksimtoropygin.meaningfulgoaltracker.R;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Daytime;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Time;

import java.util.ArrayList;


public class DaytimeListAdapter extends RecyclerView.Adapter<DaytimeListAdapter.ContactHolder> {

    private ArrayList<Daytime> daytimeList;

    public DaytimeListAdapter(ArrayList<Daytime> daytimeList) {
        this.daytimeList = daytimeList;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.recycler_day_time, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public int getItemCount() {
        return daytimeList == null? 0: daytimeList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactHolder holder, final int position) {
        final Daytime daytime = daytimeList.get(position);

        holder.setDay(daytime.dayToInt());
        holder.setStart(daytime.getStart().timeToString());
        holder.setFinish(daytime.getFinish().timeToString());

        holder.day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String item = holder.day.getSelectedItem().toString();
                daytime.setDay(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        holder.start.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ( s.length()==5 && isNumeric(s.charAt(0)+ "" +s.charAt(1)) && isNumeric(s.charAt(3)+ "" +s.charAt(4))){
                    daytime.setStart(new Time(Integer.parseInt(s.charAt(0)+ "" +s.charAt(1)), Integer.parseInt(s.charAt(3)+ "" +s.charAt(4))));
                }
            }
        });

        holder.finish.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ( s.length()==5 && isNumeric(s.charAt(0)+ "" +s.charAt(1)) && isNumeric(s.charAt(3)+ "" +s.charAt(4))){
                    daytime.setFinish(new Time(Integer.parseInt(s.charAt(0)+ "" +s.charAt(1)), Integer.parseInt(s.charAt(3)+ "" +s.charAt(4))));
                }
            }
        });
    }

    public static class ContactHolder extends RecyclerView.ViewHolder {

        private Spinner day;
        private EditText start;
        private EditText finish;

        public ContactHolder(View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.recycler_daytime_day);
            start = itemView.findViewById(R.id.recycler_daytime_start);
            finish = itemView.findViewById(R.id.recycler_daytime_finish);
        }

        public void setDay(int dayNumber) {
            day.setSelection(dayNumber);
        }

        public void setStart(String startValue) {
            start.setText(startValue);
        }

        public void setFinish(String finishValue) {
            finish.setText(finishValue);
        }
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}