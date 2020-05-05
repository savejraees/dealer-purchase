package com.saifi.dealerpurchase.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.saifi.dealerpurchase.R;
import com.saifi.dealerpurchase.retrofitModel.dealer.DealerDatum;


import java.util.ArrayList;
import java.util.List;

public class DealerAdapter extends ArrayAdapter<DealerDatum> {

    Context context;
    int resource, textViewResourceId;
    List<DealerDatum> items, tempItems, suggestions;

    public DealerAdapter(Context context, int resource, int textViewResourceId, List<DealerDatum> items) {
        super(context, resource, textViewResourceId, items);
        this.context = context;
        this.resource = resource;
        this.textViewResourceId = textViewResourceId;
        this.items = items;
        tempItems = new ArrayList<DealerDatum>(items); // this makes the difference.
        suggestions = new ArrayList<DealerDatum>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_autocomplete, parent, false);
        }
        DealerDatum people = items.get(position);
        if (people != null) {
            TextView lblName = (TextView) view.findViewById(R.id.lbl_name);
            if (lblName != null)
                lblName.setText(people.getDealerName());
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = ((DealerDatum) resultValue).getDealerName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (DealerDatum people : tempItems) {
                    if (people.getDealerName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(people);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<DealerDatum> filterList = (ArrayList<DealerDatum>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (DealerDatum people : filterList) {
                    add(people);
                    notifyDataSetChanged();
                }
            }
        }
    };
}


