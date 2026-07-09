package com.example.gameplus;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PersonaggioAdapter extends RecyclerView.Adapter<PersonaggioAdapter.ViewHolder> {

    public enum AdapterType {
        MERCATO, GESTIONE, MIA_SQUADRA
    }

    private final List<Personaggio> mData;
    private final AdapterType type;
    private final OnPersonaggioClickListener listener;

    // Configurazione Mercato
    private boolean isMercatoAperto = true;
    private String modalitaAcquisto = "standard";

    public interface OnPersonaggioClickListener {
        void onButtonClick(Personaggio p);
        void onItemClick(Personaggio p);
    }

    public PersonaggioAdapter(List<Personaggio> data, AdapterType type, OnPersonaggioClickListener listener) {
        this.mData = data;
        this.type = type;
        this.listener = listener;
    }

    public void setMercatoSettings(boolean isAperto, String modalita) {
        this.isMercatoAperto = isAperto;
        this.modalitaAcquisto = modalita;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✨ JAVA 17: Switch expression per assegnare il valore direttamente!
        int layoutId = switch (type) {
            case MERCATO -> R.layout.item_personaggio;
            case MIA_SQUADRA -> R.layout.item_mia_squadra;
            case GESTIONE -> android.R.layout.simple_list_item_2;
        };

        View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(v, type);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Personaggio p = mData.get(position);

        // ✨ JAVA 17: Pattern matching tramite Switch
        switch (type) {
            case MERCATO -> {
                holder.tvNome.setText(p.getNome());
                holder.tvPrezzo.setText(String.valueOf(p.getValore()));

                if (!isMercatoAperto) {
                    holder.tvStato.setText("Mercato Chiuso");
                    holder.tvStato.setTextColor(Color.RED);
                    holder.btnAzione.setVisibility(View.GONE);
                } else {
                    // ✨ JAVA 17: Switch pulito per le stringhe
                    switch (modalitaAcquisto) {
                        case "multiuso" -> {
                            holder.tvStato.setText("Disponibile");
                            holder.tvStato.setTextColor(Color.parseColor("#388E3C"));
                            holder.btnAzione.setVisibility(View.VISIBLE);
                            holder.btnAzione.setText("COMPRA");
                        }
                        case "asta" -> {
                            boolean hasOffer = p.getProprietario() != null && !p.getProprietario().isEmpty();
                            holder.tvStato.setText(hasOffer ? "Miglior offerente: " + p.getProprietario() : "Senza offerte");
                            holder.tvStato.setTextColor(hasOffer ? Color.parseColor("#F57C00") : Color.parseColor("#388E3C"));
                            holder.btnAzione.setVisibility(View.VISIBLE);
                            holder.btnAzione.setText("OFFRI");
                        }
                        default -> {
                            if (p.getProprietario() == null || p.getProprietario().isEmpty()) {
                                holder.tvStato.setText("Libero");
                                holder.tvStato.setTextColor(Color.parseColor("#388E3C"));
                                holder.btnAzione.setVisibility(View.VISIBLE);
                                holder.btnAzione.setText("COMPRA");
                            } else {
                                holder.tvStato.setText("Posseduto");
                                holder.tvStato.setTextColor(Color.RED);
                                holder.btnAzione.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                if (holder.btnAzione != null) {
                    holder.btnAzione.setOnClickListener(v -> { if (listener != null) listener.onButtonClick(p); });
                }
            }
            case MIA_SQUADRA -> {
                holder.tvNome.setText(p.getNome());
                holder.tvStato.setText("Valore: " + p.getValore() + " | Punti: " + p.getPunteggioTotale());
                if (holder.btnAzione != null) {
                    holder.btnAzione.setOnClickListener(v -> { if (listener != null) listener.onButtonClick(p); });
                }
            }
            case GESTIONE -> {
                holder.text1.setText(p.getNome() + " (" + p.getValore() + " crediti)");
                String stato = (p.getProprietario() == null || p.getProprietario().isEmpty()) ? "Libero" : "Assegnato";
                holder.text2.setText("Punti giornata: " + p.getPunteggioTotale() + " | Stato: " + stato);
                holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(p); });
            }
        }
    }

    @Override
    public int getItemCount() { return mData.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvStato, tvPrezzo;
        Button btnAzione;
        TextView text1, text2;

        public ViewHolder(View itemView, AdapterType type) {
            super(itemView);
            // ✨ JAVA 17: Più pulito di if/else
            switch (type) {
                case MERCATO -> {
                    tvNome = itemView.findViewById(R.id.tvNomeP);
                    tvStato = itemView.findViewById(R.id.tvStatoP);
                    tvPrezzo = itemView.findViewById(R.id.tvPrezzoP);
                    btnAzione = itemView.findViewById(R.id.btnCompraP);
                }
                case MIA_SQUADRA -> {
                    tvNome = itemView.findViewById(R.id.tvNomeMioP);
                    tvStato = itemView.findViewById(R.id.tvInfoMioP);
                    btnAzione = itemView.findViewById(R.id.btnSvincola);
                }
                case GESTIONE -> {
                    text1 = itemView.findViewById(android.R.id.text1);
                    text2 = itemView.findViewById(android.R.id.text2);
                }
            }
        }
    }
}