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
    private ArrayList<Card> cardList = new ArrayList<>(); // arrayList contains all contact
    private ArrayList<Card> cardChecked = new ArrayList<>(); // arrayList contains all contact checked
    private ItemLongClickListener longClickListener; // long press listener

    // class for the recycle view
    static class CardViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private TextView line1;
        private TextView line2;
        private ItemLongClickListener longClickListener;


        private CardViewHolder(@NonNull View itemView, ItemLongClickListener lcl) {
            super(itemView);
            // setting the textview
            line1 = itemView.findViewById(R.id.tv_item_title);
            line2 = itemView.findViewById(R.id.tv_item_email);

            // setting the listener
            longClickListener = lcl;

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            longClickListener.onItemLongClick(v, getAdapterPosition());
            return true;
        }
    }

    // set the long click listener
    void mySetLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.longClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // take the view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card, parent, false);

        // create the new view holder
        return new CardViewHolder(v,longClickListener);

    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        // if the card is selected I change the transparent background
        if(cardList.get(position).isCardSelected())
            holder.itemView.setBackgroundResource(R.drawable.card_background_selected);
        else
            holder.itemView.setBackgroundResource(R.drawable.card_background);

        // setting the lines values
        holder.line1.setText(cardList.get(position).getCompanyName());
        holder.line2.setText(cardList.get(position).getEmail());
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
       I add the position if is not in the arrayList yet and return true
       else I delete the position from the arrayList and return false
    */
    public boolean savePositionToDelete(int pos){
        Card getCard = cardList.get(pos);

        // Check if the card is in arrayList yet
        if(cardChecked.contains(getCard)){
            cardChecked.remove(getCard); // remove the card
            getCard.setCardSelection(false); // set the checked value as false
            return false;
        }
        else{
            getCard.setCardSelection(true); // Setting the card as checked
            cardChecked.add(getCard); // add the card to the arrayList
        }
        return true;
    }

    // remove from the arraylist all the element
    public void removeSelected(){
        // remove the deleted item from the list
        for(Card c : cardChecked) {
            cardList.remove(c);
        }

        cardChecked.clear(); // clear the checked arrayList
        notifyDataSetChanged(); // notify the change
    }



}
