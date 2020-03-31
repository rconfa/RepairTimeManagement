package com.example.technobit.ui.contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.technobit.R;
import com.example.technobit.contactdatas.SingleContact;

import java.util.ArrayList;


public class CardArrayAdapter extends RecyclerView.Adapter<CardArrayAdapter.CardViewHolder> {
    private ArrayList<Card> cardList = new ArrayList<Card>();
    private ArrayList<Card> CardChecked = new ArrayList<Card>();


    static class CardViewHolder extends RecyclerView.ViewHolder{
        private TextView line1;
        private TextView line2;

        public CardViewHolder(@NonNull View itemView, TextView l1, TextView l2) {
            super(itemView);
            line1 = l1;
            line2 = l2;
        }
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // take the view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card, parent, false);

        // create the new view holder
        TextView name = v.findViewById(R.id.tv_item_title);
        TextView email = v.findViewById(R.id.tv_item_email);
        CardViewHolder viewHolder = new CardViewHolder(v,name, email);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        // if the card is selected I change the transparent background
        if(cardList.get(position).isCardSelected())
            holder.itemView.setBackgroundResource(R.drawable.card_background_selected);
        else
            holder.itemView.setBackgroundResource(R.drawable.card_background);

        // setting the lines values
        holder.line1.setText(cardList.get(position).getLine1());
        holder.line2.setText(cardList.get(position).getLine2());
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    // add new card
    public void add(Card object) {
        cardList.add(object); // add the obj to list
        notifyDataSetChanged(); // notify the change
    }

    // Add multiple card
    public void add(ArrayList<SingleContact> list_card){
        if(list_card == null)
            return;
        Card temp;
        for(SingleContact s:list_card){
            temp = new Card(s);
            cardList.add(temp);
        }
    }

    /*
       se la posizone non è presente nel vettore la salvo e ritorno true
       altrimenti se già presente la tolgo e ritorno false
    */
    public boolean savePositionToDelete(int pos){
        Card getCard = cardList.get(pos);

        // controllo se presente
        if(CardChecked.contains(getCard)){
            CardChecked.remove(getCard);
            getCard.setCardSelection(false);
            return false;
        }
        else{
            getCard.setCardSelection(true); // setto la card come selezionata
            CardChecked.add(getCard);
        }
        return true;
    }

    // rimuove dalla lista tutti gli elementi selezionati
    public void removeSelected(){
        for(Card c : CardChecked) {

            cardList.remove(c);
        }

        CardChecked.clear();
        notifyDataSetChanged(); // notify the change
    }

}
