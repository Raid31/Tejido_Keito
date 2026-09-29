package com.example.tejido_keito.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tejido_keito.models.Patron;
import com.example.tejido_keito.adapters.PatronAdapter;
import com.example.tejido_keito.R;

import java.util.ArrayList;
import java.util.List;

public class BibliotecaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_biblioteca);

        List<Patron> listaPatrones = new ArrayList<>();
        listaPatrones.add(new Patron("Bufanda basica", "Principiante", 40));
        listaPatrones.add(new Patron("Gorro punto arroz", "Intermedio", 60));
        listaPatrones.add(new Patron("Chaleco trenzado", "Avanzado", 120));

        RecyclerView rvPatrones = findViewById(R.id.rvPatrones);
        rvPatrones.setLayoutManager(new LinearLayoutManager(this));
        rvPatrones.setAdapter(new PatronAdapter(listaPatrones));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}