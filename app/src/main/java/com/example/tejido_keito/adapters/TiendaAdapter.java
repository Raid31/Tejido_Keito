package com.example.tejido_keito.adapters;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.models.Tienda;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TiendaAdapter extends RecyclerView.Adapter<TiendaAdapter.ViewHolder> {

    public interface OnTiendaListener {
        void onVerEnMapa(Tienda tienda);
        void onComoLlegar(Tienda tienda);
    }

    private List<Tienda> tiendas = new ArrayList<>();
    private Location ubicacion;   // ubicación del usuario (puede ser null)
    private final OnTiendaListener listener;

    public TiendaAdapter(OnTiendaListener listener) {
        this.listener = listener;
    }

    public void actualizar(List<Tienda> tiendas, Location ubicacion) {
        this.tiendas = tiendas;
        this.ubicacion = ubicacion;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tienda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tienda tienda = tiendas.get(position);
        holder.tvNombre.setText(tienda.getNombre());
        holder.tvDistancia.setText(textoDistancia(tienda));

        holder.itemView.setOnClickListener(v -> listener.onVerEnMapa(tienda));
        holder.btnComoLlegar.setOnClickListener(v -> listener.onComoLlegar(tienda));
    }

    @Override
    public int getItemCount() {
        return tiendas.size();
    }

    private String textoDistancia(Tienda tienda) {
        if (ubicacion == null) {
            return "Distancia no disponible";
        }
        float[] resultado = new float[1];
        Location.distanceBetween(ubicacion.getLatitude(), ubicacion.getLongitude(),
                tienda.getLatitud(), tienda.getLongitud(), resultado);
        float metros = resultado[0];
        if (metros < 1000) {
            return "A " + Math.round(metros) + " m de ti";
        }
        return String.format(Locale.getDefault(), "A %.1f km de ti", metros / 1000);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvDistancia;
        Button btnComoLlegar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreTienda);
            tvDistancia = itemView.findViewById(R.id.tvDistanciaTienda);
            btnComoLlegar = itemView.findViewById(R.id.btnComoLlegar);
        }
    }
}