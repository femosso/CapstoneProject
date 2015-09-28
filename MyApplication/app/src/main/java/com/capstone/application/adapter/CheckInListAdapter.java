package com.capstone.application.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.capstone.application.R;
import com.capstone.application.model.CheckIn;

import java.util.List;

public class CheckInListAdapter extends RecyclerView.Adapter<CheckInListAdapter.ViewHolderItem> {
    private static String TAG = "CheckInListAdapter";

    private List<CheckIn> mCheckInList;
    private static MyClickListener myClickListener;

    public static class ViewHolderItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView label;
        TextView dateTime;
        TextView date;

        public ViewHolderItem(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.textView);
            dateTime = (TextView) itemView.findViewById(R.id.textView2);
            date = (TextView) itemView.findViewById(R.id.releaseYear);

            Log.i(TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public CheckInListAdapter(List<CheckIn> checkInLIst) {
        mCheckInList = checkInLIst;
    }

    @Override
    public ViewHolderItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_card_view, parent, false);

        return new ViewHolderItem(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderItem holder, int position) {
        holder.label.setText(mCheckInList.get(position).getUser().getFirstName());
        holder.dateTime.setText(mCheckInList.get(position).getQuestion().getText());
        holder.date.setText(mCheckInList.get(position).getStringDate() + " " + mCheckInList.get(position).getStringTime());
    }

    public void addItem(CheckIn dataObj, int index) {
        mCheckInList.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mCheckInList.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mCheckInList.size();
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
    }
}