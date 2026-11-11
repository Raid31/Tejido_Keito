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

/**
 * Adaptador de la lista de tiendas. Muestra el nombre y la distancia al usuario (si hay ubicación).
 *
 * Tocar el item lo muestra en el mapa y el botón "Cómo llegar" abre la ruta.
 */
public class TiendaAdapter extends RecyclerView.Adapter<TiendaAdapter.ViewHolder> {

    /**
     * Avisa a la pantalla qué se tocó en un item.
     */
    public interface OnTiendaListener {
        /**
         * Se llama al tocar el item.
         *
         * @param tienda    tienda que se tocó
         */
        void onVerEnMapa(Tienda tienda);

        /**
         * Se llama al tocar el botón "Cómo llegar".
         *
         * @param tienda    tienda a la que se quiere llegar
         */
        void onComoLlegar(Tienda tienda);
    }

    private List<Tienda> tiendas = new ArrayList<>();
    private Location ubicacion;
    private final OnTiendaListener listener;

    /**
     * Crea el adaptador con una lista vacía.
     *
     * @param listener  pantalla que reacciona a los toques
     */
    public TiendaAdapter(OnTiendaListener listener) {
        this.listener = listener;
    }

    /**
     * Cambia las tiendas y la ubicación del usuario, y vuelve a dibujar la lista.
     *
     * @param tiendas   tiendas que se van a mostrar
     * @param ubicacion ubicación actual del usuario (puede ser null)
     */
    public void actualizar(List<Tienda> tiendas, Location ubicacion) {
        this.tiendas = tiendas;
        this.ubicacion = ubicacion;
        notifyDataSetChanged();
    }

    /**
     *Crea la vista de un item (item_tienda).
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return  ViewHolder con la vista del item
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tienda, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Llena un item con el nombre y la distancia, y define los toques.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tienda tienda = tiendas.get(position);
        holder.tvNombre.setText(tienda.getNombre());
        holder.tvDistancia.setText(textoDistancia(tienda));

        holder.itemView.setOnClickListener(v -> listener.onVerEnMapa(tienda));
        holder.btnComoLlegar.setOnClickListener(v -> listener.onComoLlegar(tienda));
    }

    /**
     * Cantidad de tiendas de la lista
     *
     * @return  cantidad de items
     */
    @Override
    public int getItemCount() {
        return tiendas.size();
    }

    /**
     * Arma el texto de la distancia: en metros si es menos de 1 km, y en km si es más
     *
     * @param tienda    tienda a la que se mide la distancia
     * @return  texto como "A 350 m de ti", o "Distancia no disponible" si no hay ubicación
     */
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

    /**
     * Guarda las vistas de un item
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvDistancia;
        Button btnComoLlegar;

        /**
         * Busca las vistas del item
         *
         * @param itemView  vista del item
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreTienda);
            tvDistancia = itemView.findViewById(R.id.tvDistanciaTienda);
            btnComoLlegar = itemView.findViewById(R.id.btnComoLlegar);
        }
    }
}