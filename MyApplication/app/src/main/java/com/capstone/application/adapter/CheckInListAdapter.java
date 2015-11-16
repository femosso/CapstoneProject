package com.capstone.application.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.capstone.application.R;
import com.capstone.application.model.Answer;
import com.capstone.application.model.CheckIn;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.List;

public class CheckInListAdapter extends RecyclerView.Adapter<CheckInListAdapter.ViewHolderItem> {
    private static final String TAG = CheckInListAdapter.class.getName();

    private static MyClickListener myClickListener;

    private List<CheckIn> mCheckInList;

    public static class ViewHolderItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView teenName;
        TextView mainQuestion;
        TextView checkInDate;

        public ViewHolderItem(View itemView) {
            super(itemView);
            teenName = (TextView) itemView.findViewById(R.id.txtTeenName);
            mainQuestion = (TextView) itemView.findViewById(R.id.txtFeedbackSummary);
            checkInDate = (TextView) itemView.findViewById(R.id.txtCheckInDate);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        CheckInListAdapter.myClickListener = myClickListener;
    }

    public CheckInListAdapter(List<CheckIn> checkInLIst) {
        mCheckInList = checkInLIst;
    }

    @Override
    public ViewHolderItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_check_in, parent, false);
        return new ViewHolderItem(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderItem holder, int position) {
        CheckIn checkIn = mCheckInList.get(position);

        if (checkIn != null) {
            User user = checkIn.getUser();

            if (user != null) {
                holder.teenName.setText(user.getFirstName() + " " + user.getLastName());
            }

            String feedback = "";

            List<Answer> answerList = checkIn.getAnswerList();
            for (Answer answer : answerList) {
                feedback = feedback + answer.getQuestion().getType() + ": " + answer.getText() + "\n";
            }
            holder.mainQuestion.setText("" + feedback);

            long date = checkIn.getDate();
            holder.checkInDate.setText(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).format(date));
        }

    }

    public CheckIn getItem(int position) {
        return mCheckInList.get(position);
    }

    @Override
    public int getItemCount() {
        return mCheckInList.size();
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
    }
}