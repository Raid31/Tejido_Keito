package com.example.tejido_keito.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.activities.DetallePatronActivity;
import com.example.tejido_keito.models.Patron;

import java.util.List;

public class PatronAdapter extends RecyclerView.Adapter<PatronAdapter.PatronViewHolder> {

    private List<Patron> listaPatrones;

    public PatronAdapter(List<Patron> listaPatrones){
        this.listaPatrones = listaPatrones;
    }

    @NonNull
    @Override
    public PatronViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patron, parent, false);
        return new PatronViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PatronViewHolder holder, int position) {
        Patron patronActual = listaPatrones.get(position);
        holder.tvNombrePatron.setText(patronActual.getNombre());
        holder.tvDificultadPatron.setText(patronActual.getDificultad());
        holder.ivPatron.setImageResource(patronActual.getImagenResId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetallePatronActivity.class);
            intent.putExtra("nombre", patronActual.getNombre());
            intent.putExtra("dificultad", patronActual.getDificultad());
            intent.putExtra("totalVueltas", patronActual.getTotalVueltas());
            intent.putExtra("imagenResId", patronActual.getImagenResId());
            intent.putExtra("abreviaciones", patronActual.getAbreviaciones());
            intent.putExtra("instrucciones", patronActual.getInstrucciones());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaPatrones.size();
    }

    public void actualizarLista(List<Patron> nuevaLista) {
        this.listaPatrones = nuevaLista;
        notifyDataSetChanged();
    }

    public static class PatronViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPatron;
        TextView tvNombrePatron;
        TextView tvDificultadPatron;

        public PatronViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPatron = itemView.findViewById(R.id.ivPatron);
            tvNombrePatron = itemView.findViewById(R.id.tvNombrePatron);
            tvDificultadPatron = itemView.findViewById(R.id.tvDificultadPatron);
        }
    }
}
