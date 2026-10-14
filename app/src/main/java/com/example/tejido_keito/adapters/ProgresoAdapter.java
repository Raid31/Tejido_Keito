package com.example.tejido_keito.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.models.Patron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgresoAdapter extends RecyclerView.Adapter<ProgresoAdapter.ViewHolder> {

    public interface OnProgresoListener {
        void onAbrir(Patron patron);
        void onQuitar(Patron patron);
    }

    private List<Patron> patrones = new ArrayList<>();
    private Map<Integer, Integer> progresos = new HashMap<>();
    private final OnProgresoListener listener;

    public ProgresoAdapter(OnProgresoListener listener) {
        this.listener = listener;
    }

    public void actualizar(List<Patron> patrones, Map<Integer, Integer> progresos) {
        this.patrones = patrones;
        this.progresos = progresos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_progreso, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Patron patron = patrones.get(position);
        Integer vuelta = progresos.get(patron.getId());
        int vueltaActual = vuelta != null ? vuelta : 0;

        holder.tvNombre.setText(patron.getNombre());
        holder.tvVuelta.setText("Vuelta " + vueltaActual + " de " + patron.getTotalVueltas());
        holder.progress.setMax(patron.getTotalVueltas());
        holder.progress.setProgress(vueltaActual);
        holder.ivProgreso.setImageResource(patron.getImagenResId());

        holder.itemView.setOnClickListener(v -> listener.onAbrir(patron));
        holder.btnQuitar.setOnClickListener(v -> listener.onQuitar(patron));
    }

    @Override
    public int getItemCount() {
        return patrones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProgreso;
        TextView tvNombre;
        TextView tvVuelta;
        ProgressBar progress;
        Button btnQuitar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProgreso = itemView.findViewById(R.id.ivProgreso);
            tvNombre = itemView.findViewById(R.id.tvNombreProgreso);
            tvVuelta = itemView.findViewById(R.id.tvVueltaProgreso);
            progress = itemView.findViewById(R.id.progressItem);
            btnQuitar = itemView.findViewById(R.id.btnQuitar);
        }
    }
}