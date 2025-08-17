package com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch.R;
import com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch.model.ConversionItem;

import java.util.List;

public class ConversionAdapter extends RecyclerView.Adapter<ConversionAdapter.ConversionViewHolder> {
    int currentPosition = -1;

    public interface OnConversionClickListener {
        void onConversionSelected(ConversionItem item, int position);
    }

    private final List<ConversionItem> dataList;
    private final OnConversionClickListener listener;

    public ConversionAdapter(List<ConversionItem> dataList, OnConversionClickListener listener) {
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversion, parent, false);
        return new ConversionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        final ConversionItem item = dataList.get(position);
      //  holder.conversionName.setText(item.getName());
        Glide.with(holder.itemView).load(item.getPreviewRes()).apply(new RequestOptions().override(400,400)).into(holder.conversionImage);
       // holder.conversionImage.setImageResource(item.getPreviewRes());
        // Show or hide checkmark based on whether this position is the selected item
        if (position == currentPosition) {
            holder.checked.setVisibility(View.VISIBLE);
        } else {
            holder.checked.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Update currentPosition to this position
                currentPosition = holder.getAdapterPosition();
                listener.onConversionSelected(item, currentPosition);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ConversionViewHolder extends RecyclerView.ViewHolder {
        ImageView conversionImage;
        TextView conversionName;
        ImageView checked;

        public ConversionViewHolder(@NonNull View itemView) {
            super(itemView);
            conversionImage = itemView.findViewById(R.id.conversionImage);
            checked = itemView.findViewById(R.id.checked);
           // conversionName = itemView.findViewById(R.id.conversionName);
        }
    }
}

