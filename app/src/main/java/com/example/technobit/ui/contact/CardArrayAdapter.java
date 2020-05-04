package com.example.technobit.ui.contact;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.technobit.R;
import com.example.technobit.utils.contact.Contact;

import java.util.ArrayList;
import java.util.Collections;


public class CardArrayAdapter extends RecyclerView.Adapter<CardArrayAdapter.CardViewHolder> {
    private ArrayList<Card> mCardList = new ArrayList<>(); // arrayList contains all contact
    private ArrayList<Integer> mCardChecked = new ArrayList<>(); // arrayList contains all contact checked
    private ItemLongClickListener mLongClickListener; // long press listener
    private ItemClickListener mClickListener; // on click listener

    // class for the recycle view that implements longclick and click listener
    class CardViewHolder extends RecyclerView.ViewHolder
            implements View.OnLongClickListener, View.OnClickListener
    {
        private TextView mTextViewName; // first line of the view (company name)
        private TextView mTextViewEmail; // second line of the view (email)
        private ItemLongClickListener mLongClickListener;
        private ItemClickListener mClickListener;
        private CardView mCard;

        // constructor
        private CardViewHolder(@NonNull View itemView, ItemLongClickListener lcl,
                               ItemClickListener cl) {
            super(itemView);
            // get the textview item from ui
            this.mTextViewName = itemView.findViewById(R.id.tv_item_title);
            this.mTextViewEmail = itemView.findViewById(R.id.tv_item_email);
            this.mCard = itemView.findViewById(R.id.card_contact);

            // setting the listener
            this.mLongClickListener = lcl;
            this.mClickListener = cl;
            mCard.setOnClickListener(this);
            mCard.setOnLongClickListener(this);
        }

        // action to be performed on long click
        @Override
        public boolean onLongClick(View v) {
            mLongClickListener.onItemLongClick(v, getAdapterPosition());
            return true;
        }

        // action to be performed on click only if the card is not selected
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            //if(!mCardList.get(position).isCardSelected()) // if the card is not selected
            if(mCardChecked.isEmpty()) // if there are no card selected
                mClickListener.onItemClick(v, position , mTextViewName.getText().toString(),
                        mTextViewEmail.getText().toString());
        }

        public void setCardBackgroundColor(boolean selected){
            if (selected)
                mCard.setCardBackgroundColor(Color.parseColor("#dedede"));
            else
                mCard.setCardBackgroundColor(Color.WHITE);
        }
    }

    // set the long click listener
    void mySetLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // set the click listener
    void mySetClickListener(ItemClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    // parent activity will implement this method to respond to long click events
    public interface ItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, String name, String email);
    }


    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // take the view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card, parent, false);

        // create the new view holder
        return new CardViewHolder(v, mLongClickListener, mClickListener);

    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        // if the card is selected I change the transparent background
        holder.setCardBackgroundColor(mCardList.get(position).isCardSelected());


        // setting the lines values
        holder.mTextViewName.setText(mCardList.get(position).getCompanyName());
        holder.mTextViewEmail.setText(mCardList.get(position).getEmail());
    }

    @Override
    public int getItemCount() {
        return mCardList.size();
    }

    // add new card only if it's not duplicate
    public void add(Card object) {
        if(!mCardList.contains(object)) {
            mCardList.add(object); // add the obj to list
            notifyDataSetChanged(); // notify the change
        }
    }

    // add new card
    public void modify(Card object, int position) {
        mCardList.set(position, object);
        notifyDataSetChanged(); // notify the change
    }


    // Add multiple card
    public void add(ArrayList<Contact> list_card){
        if(list_card == null)
            return;
        Card temp;
        for(Contact s:list_card){
            temp = new Card(s);
            mCardList.add(temp);
        }
    }

    /*
       I add the position if is not in the arrayList yet
       else I delete the position from the arrayList
    */
    public void savePositionToDelete(int pos){
        Card getCard = mCardList.get(pos);

        // Check if the card is in arrayList yet
        if(mCardChecked.contains(pos)){
            mCardChecked.remove((Integer) pos); // remove the card
            getCard.setCardSelection(false); // set the checked value as false
        }
        else{
            getCard.setCardSelection(true); // Setting the card as checked
            mCardChecked.add(pos); // add the card to the arrayList
        }
    }

    // remove from the arraylist all the element
    public void removeSelected(){
        // remove the selected item from the list
        for(int i:mCardChecked)
           mCardList.remove(i);

        mCardChecked.clear(); // clear the checked arrayList
        notifyDataSetChanged(); // notify the change
    }

    public ArrayList<Integer> getPositionToDelete() {
        return mCardChecked;
    }

    // sort the arrayList in ascending order
    // Better because delete action is performed rarely, so it's better to sort instead of
    // saving the card in the arrayList (space saving)
    public void sortPosToDelete(){
        Collections.sort(mCardChecked, Collections.reverseOrder());
    }
}
