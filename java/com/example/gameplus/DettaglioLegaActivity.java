package com.example.gameplus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DettaglioLegaActivity extends AppCompatActivity {

    private String nomeLega;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvNome, tvCrediti, tvStatoMercato, tvInfoCalcolo;
    private RecyclerView rvPersonaggi;
    private PersonaggioAdapter adapter;
    private List<Personaggio> listaPersonaggi;
    private Lega legaCorrente;

    private boolean isMercatoAperto = false;
    private ListenerRegistration legaListener;
    private ListenerRegistration personaggiListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettaglio_lega);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        nomeLega = getIntent().getStringExtra("NOME_LEGA");

        tvNome = findViewById(R.id.tvNomeLegaDettaglio);
        tvCrediti = findViewById(R.id.tvCreditiLegaDettaglio);
        tvStatoMercato = findViewById(R.id.tvStatoMercato);
        tvInfoCalcolo = findViewById(R.id.tvInfoCalcolo);
        rvPersonaggi = findViewById(R.id.rvPersonaggiMercato);

        tvNome.setText(nomeLega);

        rvPersonaggi.setLayoutManager(new LinearLayoutManager(this));
        listaPersonaggi = new ArrayList<>();

        adapter = new PersonaggioAdapter(listaPersonaggi, PersonaggioAdapter.AdapterType.MERCATO, new PersonaggioAdapter.OnPersonaggioClickListener() {
            @Override
            public void onButtonClick(Personaggio p) {
                String modalita = (legaCorrente != null && legaCorrente.getModalitaAcquisto() != null) ? legaCorrente.getModalitaAcquisto() : "standard";
                if ("asta".equals(modalita)) {
                    apriFinestraAsta(p);
                } else {
                    acquistaPersonaggio(p);
                }
            }
            @Override
            public void onItemClick(Personaggio p) {}
        });
        rvPersonaggi.setAdapter(adapter);

        findViewById(R.id.btnVediClassifica).setOnClickListener(v -> {
            Intent i = new Intent(this, ClassificaActivity.class);
            i.putExtra("NOME_LEGA", nomeLega);
            startActivity(i);
        });

        FloatingActionButton fab = findViewById(R.id.fabMiaSquadra);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, MiaSquadraActivity.class);
            i.putExtra("NOME_LEGA", nomeLega);
            startActivity(i);
        });

        ascoltaDatiLega();
    }

    private void ascoltaDatiLega() {
        legaListener = db.collection("leagues").document(nomeLega)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    legaCorrente = snapshot.toObject(Lega.class);
                    if (legaCorrente != null) {
                        tvCrediti.setText("Budget Iniziale: " + legaCorrente.getCreditiIniziali());

                        isMercatoAperto = calcolaSeMercatoAperto(
                                legaCorrente.getGiornoInizioMercato(), legaCorrente.getOraInizioMercato(),
                                legaCorrente.getGiornoFineMercato(), legaCorrente.getOraFineMercato()
                        );

                        if (tvStatoMercato != null) {
                            if (isMercatoAperto) {
                                tvStatoMercato.setText("🟢 MERCATO APERTO (Chiude " + legaCorrente.getGiornoFineMercato() + " " + legaCorrente.getOraFineMercato() + ")\nModalità: " + legaCorrente.getModalitaAcquisto().toUpperCase());
                                tvStatoMercato.setTextColor(Color.parseColor("#388E3C"));
                            } else {
                                tvStatoMercato.setText("🔴 MERCATO CHIUSO (Apre " + legaCorrente.getGiornoInizioMercato() + " " + legaCorrente.getOraInizioMercato() + ")");
                                tvStatoMercato.setTextColor(Color.parseColor("#D32F2F"));
                            }
                            tvInfoCalcolo.setText("🏆 Risultati attesi: " + legaCorrente.getGiornoCalcolo() + " alle " + legaCorrente.getOraCalcolo());
                        }

                        if (adapter != null) {
                            adapter.setMercatoSettings(isMercatoAperto, legaCorrente.getModalitaAcquisto());
                        }
                        ascoltaPersonaggi();
                    }
                });
    }

    private boolean calcolaSeMercatoAperto(String gInizio, String oInizio, String gFine, String oFine) {
        if (gInizio == null || oInizio == null) return true;
        try {
            int startDay = getGiornoInt(gInizio);
            int endDay = getGiornoInt(gFine);
            int startHour = Integer.parseInt(oInizio.split(":")[0]);
            int startMin = Integer.parseInt(oInizio.split(":")[1]);
            int endHour = Integer.parseInt(oFine.split(":")[0]);
            int endMin = Integer.parseInt(oFine.split(":")[1]);

            java.util.Calendar c = java.util.Calendar.getInstance();
            int currentDay = c.get(java.util.Calendar.DAY_OF_WEEK);
            currentDay = (currentDay == java.util.Calendar.SUNDAY) ? 7 : currentDay - 1;
            int currentHour = c.get(java.util.Calendar.HOUR_OF_DAY);
            int currentMin = c.get(java.util.Calendar.MINUTE);

            int currentTotal = currentDay * 24 * 60 + currentHour * 60 + currentMin;
            int startTotal = startDay * 24 * 60 + startHour * 60 + startMin;
            int endTotal = endDay * 24 * 60 + endHour * 60 + endMin;

            if (startTotal <= endTotal) {
                return currentTotal >= startTotal && currentTotal <= endTotal;
            } else {
                return currentTotal >= startTotal || currentTotal <= endTotal;
            }
        } catch (Exception e) {
            return false;
        }
    }

    // ✨ JAVA 17: Switch Expression che restituisce l'int in modo compatto!
    private int getGiornoInt(String giorno) {
        return switch (giorno) {
            case "Lunedì" -> 1;
            case "Martedì" -> 2;
            case "Mercoledì" -> 3;
            case "Giovedì" -> 4;
            case "Venerdì" -> 5;
            case "Sabato" -> 6;
            case "Domenica" -> 7;
            default -> 1;
        };
    }

    private void ascoltaPersonaggi() {
        if (personaggiListener != null) personaggiListener.remove();
        personaggiListener = db.collection("leagues").document(nomeLega)
                .collection("personaggi")
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) return;
                    listaPersonaggi.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        listaPersonaggi.add(doc.toObject(Personaggio.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void acquistaPersonaggio(Personaggio p) {
        String uid = mAuth.getCurrentUser().getUid();
        String modalita = (legaCorrente != null && legaCorrente.getModalitaAcquisto() != null) ? legaCorrente.getModalitaAcquisto() : "standard";

        if ("multiuso".equals(modalita)) {
            Map<String, Object> acquisto = new HashMap<>();
            acquisto.put("nome", p.getNome());
            acquisto.put("valore", p.getValore());
            acquisto.put("proprietario", uid);
            acquisto.put("punteggioTotale", 0);

            db.collection("leagues").document(nomeLega).collection("personaggi_acquistati")
                    .add(acquisto).addOnSuccessListener(aVoid -> Toast.makeText(this, "Acquistato!", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("leagues").document(nomeLega).collection("personaggi").document(p.getNome())
                    .update("proprietario", uid).addOnSuccessListener(aVoid -> Toast.makeText(this, "Acquisto effettuato!", Toast.LENGTH_SHORT).show());
        }
    }

    private void apriFinestraAsta(Personaggio p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fai un'offerta per " + p.getNome());
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Offerta (Min: " + p.getValore() + ")");
        builder.setView(input);

        builder.setPositiveButton("Offri", (dialog, which) -> {
            String offertaStr = input.getText().toString();
            if (!offertaStr.isEmpty()) {
                int offerta = Integer.parseInt(offertaStr);
                if (offerta >= p.getValore()) {
                    db.collection("leagues").document(nomeLega).collection("personaggi").document(p.getNome())
                            .update("valore", offerta, "proprietario", mAuth.getCurrentUser().getUid())
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Offerta inviata!", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, "Offerta troppo bassa!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (legaListener != null) legaListener.remove();
        if (personaggiListener != null) personaggiListener.remove();
    }
}