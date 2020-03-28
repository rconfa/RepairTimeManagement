package com.example.technobit.ui.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.technobit.R;

import java.util.ArrayList;
import java.util.List;

public class CardArrayAdapter  extends ArrayAdapter<Card> {
    private static final String TAG = "CardArrayAdapter";
    private List<Card> cardList = new ArrayList<Card>();
    private ArrayList<Card> CardChecked = new ArrayList<Card>();


    static class CardViewHolder {
        TextView line1;
        TextView line2;

    }

    // unused ma necessario per extends
    public CardArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public void add(Card object) {
        cardList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.cardList.size();
    }

    @Override
    public Card getItem(int index) {
        return this.cardList.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        CardViewHolder viewHolder;
        Card card = getItem(position);

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_item_card, parent, false);
            viewHolder = new CardViewHolder();
            viewHolder.line1 = (TextView) row.findViewById(R.id.line1);
            viewHolder.line2 = (TextView) row.findViewById(R.id.line2);
            row.setTag(viewHolder);
        } else {

            // metto il background corretto in base se la carta è stata selezionata o no
            if(card.isSelected())
                row.setBackgroundResource(R.drawable.card_background_selected);
            else
                row.setBackgroundResource(R.drawable.card_background);
            viewHolder = (CardViewHolder)row.getTag();
        }

        viewHolder.line1.setText(card.getLine1());
        viewHolder.line2.setText(card.getLine2());

        return row;
    }


    /* se la posizone non è presente nel vettore la salvo e ritorno true
       altrimenti se già presente la tolgo e ritorno false
    */
    public boolean savePositionToDelete(int pos){
        Card getCard = cardList.get(pos);

        // controllo se presente
        if(CardChecked.contains(getCard)){
            CardChecked.remove(getCard);
            getCard.setIsSelected(false);
            return false;
        }
        else{
            getCard.setIsSelected(true); // setto la card come selezionata
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
    }

}
