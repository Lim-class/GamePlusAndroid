package com.example.gameplus;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MiaSquadraActivity extends AppCompatActivity {

    private String nomeLega;
    private FirebaseFirestore db;
    private TextView tvBudget;
    private RecyclerView rv;
    private PersonaggioAdapter adapter;
    private List<Personaggio> mieiPersonaggi;
    private long budgetIniziale = 0;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mia_squadra);

        db = FirebaseFirestore.getInstance();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        nomeLega = getIntent().getStringExtra("NOME_LEGA");

        tvBudget = findViewById(R.id.tvBudgetRimanente);
        rv = findViewById(R.id.rvMiaSquadra);

        rv.setLayoutManager(new LinearLayoutManager(this));
        mieiPersonaggi = new ArrayList<>();

        adapter = new PersonaggioAdapter(mieiPersonaggi, PersonaggioAdapter.AdapterType.MIA_SQUADRA, new PersonaggioAdapter.OnPersonaggioClickListener() {
            @Override
            public void onButtonClick(Personaggio p) {
                confermaSvincolo(p);
            }
            @Override
            public void onItemClick(Personaggio p) {}
        });
        rv.setAdapter(adapter);

        recuperaBudgetECaricaSquadra();
    }

    private void recuperaBudgetECaricaSquadra() {
        db.collection("leagues").document(nomeLega).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Long crediti = doc.getLong("creditiIniziali");
                budgetIniziale = (crediti != null) ? crediti : 0;
                ascoltaCambiamentiSquadra();
            }
        });
    }

    private void ascoltaCambiamentiSquadra() {
        db.collection("leagues").document(nomeLega)
                .collection("personaggi")
                .whereEqualTo("proprietario", myUid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        mieiPersonaggi.clear();
                        long spesaTotale = 0;

                        for (QueryDocumentSnapshot doc : value) {
                            Personaggio p = doc.toObject(Personaggio.class);
                            mieiPersonaggi.add(p);
                            spesaTotale += p.getValore();
                        }

                        adapter.notifyDataSetChanged();

                        long rimanente = budgetIniziale - spesaTotale;
                        tvBudget.setText("Crediti residui: " + rimanente);

                        if (rimanente < 20) {
                            tvBudget.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            tvBudget.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        }
                    }
                });
    }

    private void confermaSvincolo(Personaggio p) {
        new AlertDialog.Builder(this)
                .setTitle("Svincola " + p.getNome())
                .setMessage("Sei sicuro di voler svincolare questo personaggio? Recupererai " + p.getValore() + " crediti.")
                .setPositiveButton("Sì, svincola", (dialog, which) -> eseguiSvincolo(p))
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void eseguiSvincolo(Personaggio p) {
        db.collection("leagues").document(nomeLega)
                .collection("personaggi").document(p.getNome())
                .update("proprietario", "")
                .addOnSuccessListener(aVoid -> Toast.makeText(this, p.getNome() + " svincolato!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Errore durante lo svincolo", Toast.LENGTH_SHORT).show());
    }
}