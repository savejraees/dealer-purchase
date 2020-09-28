package com.saifi.dealerpurchase.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.saifi.dealerpurchase.R;
import com.saifi.dealerpurchase.model.DetailsModel;

import java.util.ArrayList;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.HeroViewHolder> {


    private ArrayList<DetailsModel> heroList;
    private Context context;

    public DetailsAdapter(ArrayList<DetailsModel> heroList, Context context) {
        this.heroList = heroList;
        this.context = context;
    }

    @Override
    public HeroViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_details, parent, false);
        return new HeroViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final HeroViewHolder holder, final int position) {
        DetailsModel hero = heroList.get(position);
        holder.txtImei.setText(hero.getImei());
        holder.txtBarcode.setText(hero.getBarcode());
        holder.txtAmount.setText(hero.getPrice());
        holder.txtGB.setText(hero.getGb());
        holder.imgRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(position);
            }
        });
    }

    private void removeItem(int position) {
        heroList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return heroList.size();
    }

    class HeroViewHolder extends RecyclerView.ViewHolder {
        TextView txtImei,txtBarcode,txtAmount,txtGB;
        ImageView imgRemove;
        HeroViewHolder(View itemView) {
            super(itemView);
            txtImei = (TextView) itemView.findViewById(R.id.txtImei);
            txtBarcode = (TextView) itemView.findViewById(R.id.txtBarcode);
            txtAmount = (TextView) itemView.findViewById(R.id.txtAmount);
            txtGB = (TextView) itemView.findViewById(R.id.txtGB);
            imgRemove =  itemView.findViewById(R.id.imgRemove);

        }
    }
}