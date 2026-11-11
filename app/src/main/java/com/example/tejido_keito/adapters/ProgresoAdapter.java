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

/**
 * Adaptador de la lista de "mi progreso". Muestra imagen, nombre, la vuelta en que va y una barra de progreso.
 *
 * Tocar el item abre el contador y el botón Quitar borra el avance.
 */
public class ProgresoAdapter extends RecyclerView.Adapter<ProgresoAdapter.ViewHolder> {

    /**
     * Avisa a la pantalla qué se tocó en un item.
     */
    public interface OnProgresoListener {
        /**
         * Se llama al tocar el item (continuar en el contador).
         *
         * @param patron    patrón que se tocó
         */
        void onAbrir(Patron patron);

        /**
         * Se llama al tocar el botón Quitar.
         *
         * @param patron    patrón al que se le quiere borrar el avance
         */
        void onQuitar(Patron patron);
    }

    private List<Patron> patrones = new ArrayList<>();
    private Map<Integer, Integer> progresos = new HashMap<>();
    private final OnProgresoListener listener;

    /**
     * Crea el adaptador con una lista vacía
     *
     * @param listener  pantalla que reacciona a los toques
     */
    public ProgresoAdapter(OnProgresoListener listener) {
        this.listener = listener;
    }

    /**
     * Cambia los patrones y su progreso, y vuelve a dibujar la lista.
     *
     * @param patrones  patrones que el usuario ya empezó
     * @param progresos mapa con id del patrón y la vuelta actual
     */
    public void actualizar(List<Patron> patrones, Map<Integer, Integer> progresos) {
        this.patrones = patrones;
        this.progresos = progresos;
        notifyDataSetChanged();
    }

    /**
     * Crea la vista de un item (item_progreso).
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
                .inflate(R.layout.item_progreso, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Llena un item: nombre, "Vuelta X de Y", barra de progreso e imagen, y define los toques.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * Cantidad de patrones de la lista.
     *
     * @return  cantidad de items
     */
    @Override
    public int getItemCount() {
        return patrones.size();
    }

    /**
     * Guarda las vistas de un item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProgreso;
        TextView tvNombre;
        TextView tvVuelta;
        ProgressBar progress;
        Button btnQuitar;

        /**
         * Busca las vistas del item.
         *
         * @param itemView  vista del item
         */
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