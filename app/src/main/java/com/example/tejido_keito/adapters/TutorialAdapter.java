package com.example.tejido_keito.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.models.Tutorial;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de la lista de tutoriales. Muestra título y descripción, y al tocar uno avisa a la pantalla.
 */
public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

    /**
     * Avisa a la pantalla qué tutorial se tocó
     */
    public interface OnTutorialListener {
        /**
         * Se llama al tocar un tutorial
         *
         * @param tutorial  tutorial que se tocó
         */
        void onAbrir(Tutorial tutorial);
    }

    private List<Tutorial> tutoriales = new ArrayList<>();
    private final OnTutorialListener listener;

    /**
     * Crea el adaptador con una lista vacía
     *
     * @param listener  pantalla que reacciona al toque
     */
    public TutorialAdapter(OnTutorialListener listener) {
        this.listener = listener;
    }

    /**
     * Cambia los tutoriales y vuelve a dibujar la lista
     *
     * @param tutoriales    tutoriales que se van a mostrar
     */
    public void actualizar(List<Tutorial> tutoriales) {
        this.tutoriales = tutoriales;
        notifyDataSetChanged();
    }

    /**
     * Crea la vista de un item (item_tutorial)
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
                .inflate(R.layout.item_tutorial, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Llena un item con el título y la descripción, y define el toque.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tutorial tutorial = tutoriales.get(position);
        holder.tvTitulo.setText(tutorial.getTitulo());
        holder.tvDescripcion.setText(tutorial.getDescripcion());
        holder.itemView.setOnClickListener(v -> listener.onAbrir(tutorial));
    }

    /**
     * Cantidad de tutoriales de la lista
     *
     * @return  cantidad de items
     */
    @Override
    public int getItemCount() {
        return tutoriales.size();
    }

    /**
     * Guarda las vistas de un item
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        TextView tvDescripcion;

        /**
         * Busca las vistas del item
         *
         * @param itemView  vista del item
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloTutorial);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTutorial);
        }
    }
}