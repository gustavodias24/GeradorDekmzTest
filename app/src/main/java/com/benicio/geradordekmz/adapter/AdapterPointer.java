package com.benicio.geradordekmz.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.benicio.geradordekmz.R;
import com.benicio.geradordekmz.model.PointerModel;

import java.util.List;

public class AdapterPointer extends RecyclerView.Adapter<AdapterPointer.MyViewHolder>{

    List<PointerModel> list;
    Context c;

    public AdapterPointer(List<PointerModel> list, Context c) {
        this.list = list;
        this.c = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pointer_layout, parent, false));
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PointerModel pointer = list.get(position);
        holder.title.setText(
                pointer.getTitle() +
                String.format("\nLat: %f Long: %f", pointer.getLat(), pointer.getLongi()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_pointer_text);
        }
    }
}
