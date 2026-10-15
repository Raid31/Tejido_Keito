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

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

    public interface OnTutorialListener {
        void onAbrir(Tutorial tutorial);
    }

    private List<Tutorial> tutoriales = new ArrayList<>();
    private final OnTutorialListener listener;

    public TutorialAdapter(OnTutorialListener listener) {
        this.listener = listener;
    }

    public void actualizar(List<Tutorial> tutoriales) {
        this.tutoriales = tutoriales;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutorial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tutorial tutorial = tutoriales.get(position);
        holder.tvTitulo.setText(tutorial.getTitulo());
        holder.tvDescripcion.setText(tutorial.getDescripcion());
        holder.itemView.setOnClickListener(v -> listener.onAbrir(tutorial));
    }

    @Override
    public int getItemCount() {
        return tutoriales.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        TextView tvDescripcion;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloTutorial);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTutorial);
        }
    }
}