package com.example.gameplus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestioneLegaActivity extends AppCompatActivity {

    private String nomeLega;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText etNome, etValore;
    private RecyclerView rvPersonaggi;
    private PersonaggioAdapter adapter;
    private List<Personaggio> listaPersonaggi;

    private ListenerRegistration personaggiListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestione_lega);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        nomeLega = getIntent().getStringExtra("NOME_LEGA");

        TextView tvTitolo = findViewById(R.id.tvTitoloLega);
        tvTitolo.setText("Gestione: " + nomeLega);

        etNome = findViewById(R.id.etNomePersonaggio);
        etValore = findViewById(R.id.etValorePersonaggio);

        rvPersonaggi = findViewById(R.id.rvPersonaggi);
        rvPersonaggi.setLayoutManager(new LinearLayoutManager(this));
        listaPersonaggi = new ArrayList<>();

        adapter = new PersonaggioAdapter(listaPersonaggi, PersonaggioAdapter.AdapterType.GESTIONE, new PersonaggioAdapter.OnPersonaggioClickListener() {
            @Override
            public void onButtonClick(Personaggio p) {}

            @Override
            public void onItemClick(Personaggio p) {
                mostraDialogoPunteggio(p);
            }
        });
        rvPersonaggi.setAdapter(adapter);

        findViewById(R.id.btnAggiungiPersonaggio).setOnClickListener(v -> aggiungiPersonaggio());
        findViewById(R.id.btnVaiAlMercato).setOnClickListener(v -> {
            Intent intent = new Intent(this, DettaglioLegaActivity.class);
            intent.putExtra("NOME_LEGA", nomeLega);
            startActivity(intent);
        });
        findViewById(R.id.btnConcludiGiornata).setOnClickListener(v -> mostraDialogoConfermaGiornata());
        findViewById(R.id.btnEliminaLega).setOnClickListener(v -> confermaEliminazioneLega());

        ascoltaPersonaggi();
    }

    private void aggiungiPersonaggio() {
        String nomeP = etNome.getText().toString().trim();
        String valoreStr = etValore.getText().toString().trim();

        if (TextUtils.isEmpty(nomeP) || TextUtils.isEmpty(valoreStr)) {
            Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_SHORT).show();
            return;
        }

        int valoreP = Integer.parseInt(valoreStr);

        Map<String, Object> p = new HashMap<>();
        p.put("nome", nomeP);
        p.put("valore", valoreP);
        p.put("proprietario", "");
        p.put("punteggioTotale", 0);

        db.collection("leagues").document(nomeLega)
                .collection("personaggi").document(nomeP)
                .set(p)
                .addOnSuccessListener(aVoid -> {
                    etNome.setText("");
                    etValore.setText("");
                    Toast.makeText(this, "Personaggio aggiunto!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Errore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void ascoltaPersonaggi() {
        personaggiListener = db.collection("leagues").document(nomeLega)
                .collection("personaggi")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    listaPersonaggi.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        listaPersonaggi.add(doc.toObject(Personaggio.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @SuppressWarnings("unchecked")
    private void mostraDialogoPunteggio(Personaggio p) {
        db.collection("leagues").document(nomeLega).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Map<String, Long> regolamento = (Map<String, Long>) doc.get("regolamento");
                if (regolamento == null || regolamento.isEmpty()) {
                    Toast.makeText(this, "Nessun regolamento trovato.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> nomiRegole = new ArrayList<>(regolamento.keySet());
                String[] opzioni = nomiRegole.toArray(new String[0]);

                new AlertDialog.Builder(this)
                        .setTitle("Assegna punti a " + p.getNome())
                        .setItems(opzioni, (dialog, which) -> {
                            String regolaScelta = opzioni[which];
                            long valorePunti = regolamento.get(regolaScelta);
                            // ERRORE BATTITURA CORRETTO
                            applicaPunteggio(p.getNome(), valorePunti);
                        })
                        .show();
            }
        });
    }

    private void applicaPunteggio(String nomePersonaggio, long punti) {
        db.collection("leagues").document(nomeLega)
                .collection("personaggi").document(nomePersonaggio)
                .update("punteggioTotale", com.google.firebase.firestore.FieldValue.increment(punti))
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Punteggio aggiornato!", Toast.LENGTH_SHORT).show());
    }

    private void mostraDialogoConfermaGiornata() {
        new AlertDialog.Builder(this)
                .setTitle("Concludi Giornata")
                .setMessage("Vuoi salvare i punteggi attuali nella Classifica Generale? I punti dei personaggi verranno azzerati per la nuova giornata.")
                .setPositiveButton("Sì, Concludi", (dialog, which) -> concludiGiornata())
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void concludiGiornata() {
        db.collection("leagues").document(nomeLega).collection("personaggi")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Personaggio p = doc.toObject(Personaggio.class);

                        if (p.getProprietario() != null && !p.getProprietario().isEmpty() && p.getPunteggioTotale() != 0) {
                            DocumentReference refClassifica = db.collection("leagues").document(nomeLega)
                                    .collection("classifica").document(p.getProprietario());

                            batch.set(refClassifica, new HashMap<String, Object>() {{
                                put("punteggioTotale", com.google.firebase.firestore.FieldValue.increment(p.getPunteggioTotale()));
                            }}, com.google.firebase.firestore.SetOptions.merge());
                        }

                        if (p.getPunteggioTotale() != 0) {
                            DocumentReference refPersonaggio = db.collection("leagues").document(nomeLega)
                                    .collection("personaggi").document(p.getNome());
                            batch.update(refPersonaggio, "punteggioTotale", 0);
                        }
                    }

                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Giornata conclusa! Classifica aggiornata.", Toast.LENGTH_LONG).show();
                    });
                });
    }

    private void confermaEliminazioneLega() {
        new AlertDialog.Builder(this)
                .setTitle("Elimina Lega")
                .setMessage("Sei sicuro? Questa azione cancellerà tutti i dati in modo irreversibile.")
                .setPositiveButton("SÌ, ELIMINA", (dialog, which) -> eliminaTutto())
                .setNegativeButton("Annulla", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void eliminaTutto() {
        DocumentReference legaRef = db.collection("leagues").document(nomeLega);

        legaRef.collection("personaggi").get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                batch.delete(doc.getReference());
            }

            batch.delete(legaRef);

            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Lega eliminata", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HomeFantaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (personaggiListener != null) personaggiListener.remove();
    }
}