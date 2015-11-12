package com.capstone.application.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.capstone.application.R;
import com.capstone.application.model.NavigationDrawerItem;
import com.facebook.login.widget.ProfilePictureView;

import java.util.List;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    private List<NavigationDrawerItem> mData;

    private NavigationDrawerCallbacks mNavigationDrawerCallbacks;

    private View mSelectedView;

    private int mSelectedPosition;

    public NavigationDrawerAdapter(List<NavigationDrawerItem> data) {
        mData = data;
    }

    public NavigationDrawerCallbacks getNavigationDrawerCallbacks() {
        return mNavigationDrawerCallbacks;
    }

    public void setNavigationDrawerCallbacks(NavigationDrawerCallbacks navigationDrawerCallbacks) {
        mNavigationDrawerCallbacks = navigationDrawerCallbacks;
    }

    public void updateCounter(int position, int count) {
        mData.get(position).setCount(count);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_navigation_drawer, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedView != null) {
                    mSelectedView.setSelected(false);
                }

                mSelectedPosition = viewHolder.getAdapterPosition();
                v.setSelected(true);
                mSelectedView = v;
                if (mNavigationDrawerCallbacks != null) {
                    mNavigationDrawerCallbacks.onNavigationDrawerItemSelected(viewHolder.getAdapterPosition());
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.textView.setText(mData.get(i).getText());

        // If it has profile id defined, then it should put the logged user picture
        if (mData.get(i).getProfileId() != null) {
            viewHolder.profilePicView.setProfileId(mData.get(i).getProfileId());
            showProfilePic(true, viewHolder);
        } else {
            viewHolder.imgView.setImageResource(mData.get(i).getIcon());
            showProfilePic(false, viewHolder);
        }

        // if it has some counter set
        if (mData.get(i).getCount() != 0) {
            viewHolder.counterView.setText(" " + mData.get(i).getCount() + " ");
            viewHolder.counterView.setVisibility(View.VISIBLE);
        } else {
            // hide the counter view
            viewHolder.counterView.setVisibility(View.GONE);
        }

        if (mSelectedPosition == i) {
            if (mSelectedView != null) {
                mSelectedView.setSelected(false);
            }

            mSelectedPosition = i;
            mSelectedView = viewHolder.itemView;
            mSelectedView.setSelected(true);
        }
    }

    private void showProfilePic(boolean show, ViewHolder viewHolder) {
        // handle the value of the @layout_toRightOf text view element
        RelativeLayout.LayoutParams textViewParams = (RelativeLayout.LayoutParams) viewHolder.textView.getLayoutParams();
        textViewParams.addRule(RelativeLayout.RIGHT_OF, show ? R.id.imgProfilePic : R.id.imgHome);
        viewHolder.textView.setLayoutParams(textViewParams);

        // show or hide profile pic and icon view
        viewHolder.profilePicView.setVisibility(show ? View.VISIBLE : View.GONE);
        viewHolder.imgView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void selectPosition(int position) {
        mSelectedPosition = position;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ProfilePictureView profilePicView;
        public ImageView imgView;
        public TextView textView;
        public TextView counterView;

        public ViewHolder(View itemView) {
            super(itemView);
            profilePicView = (ProfilePictureView) itemView.findViewById(R.id.imgProfilePic);
            imgView = (ImageView) itemView.findViewById(R.id.imgHome);
            textView = (TextView) itemView.findViewById(R.id.txtItemName);
            counterView = (TextView) itemView.findViewById(R.id.txtItemCounter);
        }
    }
}