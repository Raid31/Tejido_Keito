package com.example.tejido_keito.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.activities.DetallePatronActivity;
import com.example.tejido_keito.models.Patron;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatronAdapter extends RecyclerView.Adapter<PatronAdapter.PatronViewHolder> {

    // Avisa a la pantalla cuando el usuario toca el corazón
    public interface OnFavoritoListener {
        void onFavoritoCambiado(Patron patron, boolean esFavorito);
    }

    private List<Patron> listaPatrones;
    private Set<Integer> favoritos = new HashSet<>();   // ids de los patrones favoritos
    private OnFavoritoListener favoritoListener;

    public PatronAdapter(List<Patron> listaPatrones){
        this.listaPatrones = listaPatrones;
    }

    public void setOnFavoritoListener(OnFavoritoListener listener) {
        this.favoritoListener = listener;
    }

    public void setFavoritos(Set<Integer> favoritos) {
        this.favoritos = new HashSet<>(favoritos);
        notifyDataSetChanged();
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

        // Corazón: lleno si es favorito, vacío si no
        boolean esFavorito = favoritos.contains(patronActual.getId());
        holder.btnFavorito.setImageResource(esFavorito
                ? R.drawable.ic_favorito_lleno
                : R.drawable.ic_favorito_vacio);

        holder.btnFavorito.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            Integer id = patronActual.getId();
            boolean ahoraEsFavorito = !favoritos.contains(id);
            if (ahoraEsFavorito) {
                favoritos.add(id);
            } else {
                favoritos.remove(id);
            }
            notifyItemChanged(pos);
            if (favoritoListener != null) {
                favoritoListener.onFavoritoCambiado(patronActual, ahoraEsFavorito);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetallePatronActivity.class);
            intent.putExtra("id", patronActual.getId());
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
        ImageButton btnFavorito;

        public PatronViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPatron = itemView.findViewById(R.id.ivPatron);
            tvNombrePatron = itemView.findViewById(R.id.tvNombrePatron);
            tvDificultadPatron = itemView.findViewById(R.id.tvDificultadPatron);
            btnFavorito = itemView.findViewById(R.id.btnFavorito);
        }
    }
}