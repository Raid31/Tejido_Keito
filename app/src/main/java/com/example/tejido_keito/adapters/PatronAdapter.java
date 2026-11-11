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

/**
 * Adaptador de la lista de patrones (RecyclerView). Lo usan la Biblioteca y Favoritos.
 *
 * Muestra imagen, nombre, dificultad y el corazón de favorito, y al tocar un patrón abre su detalle
 * mandando por Intent id, nombre, dificultad, totalVueltas, imagenResId, abreviaciones e instrucciones.
 */
public class PatronAdapter extends RecyclerView.Adapter<PatronAdapter.PatronViewHolder> {

    /**
     *     Avisa a la pantalla cuando el usuario toca el corazón
      */
    public interface OnFavoritoListener {
        /**
         * Se llama después de tocar el corazón.
         * @param patron que se toco
         * @param esFavorito true si quedo como favorito, false si se quito
         */
        void onFavoritoCambiado(Patron patron, boolean esFavorito);
    }

    private List<Patron> listaPatrones;
    private Set<Integer> favoritos = new HashSet<>();
    private OnFavoritoListener favoritoListener;

    /**
     * Crea el adaptador con la lista inicial.
     *
     * @param listaPatrones patrones que se van a mostrar
     */
    public PatronAdapter(List<Patron> listaPatrones){
        this.listaPatrones = listaPatrones;
    }

    /**
     * Define quién se entera cuando cambia un favorito.
     *
     * @param listener  pantalla que guarda el cambio en la base de datos
     */
    public void setOnFavoritoListener(OnFavoritoListener listener) {
        this.favoritoListener = listener;
    }

    /**
     * Actualiza cuáles patrones son favoritos y vuelve a dibujar la lista
     *
     * @param favoritos  ids de los patrones favoritos del usuario
     */
    public void setFavoritos(Set<Integer> favoritos) {
        this.favoritos = new HashSet<>(favoritos);
        notifyDataSetChanged();
    }

    /**
     * Crea la vista de un item (item_patron).
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return  ViewHolder con la vista del item
     */
    @NonNull
    @Override
    public PatronViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patron, parent, false);
        return new PatronViewHolder(vista);
    }

    /**
     * Llena un item con los datos del patrón: textos, imagen y corazón (lleno si es favorito).
     * También define qué pasa al tocar el corazón y al tocar el item
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * Cantidad de patrones de la lista.
     *
     * @return cantidad de items
     */
    @Override
    public int getItemCount() {
        return listaPatrones.size();
    }

    /**
     * Cambia la lista que se muestra (por ejemplo después de filtrar).
     *
     * @param nuevaLista    patrones que se van a mostrar
     */
    public void actualizarLista(List<Patron> nuevaLista) {
        this.listaPatrones = nuevaLista;
        notifyDataSetChanged();
    }

    /**
     * Guarda las vistas de un item para no buscarlas cada vez.
     */
    public static class PatronViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPatron;
        TextView tvNombrePatron;
        TextView tvDificultadPatron;
        ImageButton btnFavorito;

        /**
         * Busca las vistas del item
         *
         * @param itemView  vista del item
         */
        public PatronViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPatron = itemView.findViewById(R.id.ivPatron);
            tvNombrePatron = itemView.findViewById(R.id.tvNombrePatron);
            tvDificultadPatron = itemView.findViewById(R.id.tvDificultadPatron);
            btnFavorito = itemView.findViewById(R.id.btnFavorito);
        }
    }
}