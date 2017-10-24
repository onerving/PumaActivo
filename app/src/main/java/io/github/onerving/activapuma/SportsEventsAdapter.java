package io.github.onerving.activapuma;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by onerving on 20/10/17.
 */

class SportsEventsAdapter
        extends RecyclerView.Adapter<SportsEventsAdapter.SportsEventsAdapterViewHolder>{

    private JSONArray mSportsEvents;
    private final SportsEventsAdapterOnClickHandler mClickHandler;

    public interface SportsEventsAdapterOnClickHandler{
        void onClick(JSONObject eventJsonData);
    }

    public SportsEventsAdapter(SportsEventsAdapterOnClickHandler clickHandler){
        mClickHandler = clickHandler;
    }

    public class SportsEventsAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public final ImageView mEventImage;
        public final TextView mEventTitle;
        public final TextView mEventDescription;

        public SportsEventsAdapterViewHolder(View view) {
            super(view);
            mEventImage = (ImageView) view.findViewById(R.id.eventImage);
            mEventTitle = (TextView) view.findViewById(R.id.eventTitle);
            mEventDescription = (TextView) view.findViewById(R.id.eventDescription);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            try {
                JSONObject eventJsonData = mSportsEvents.getJSONObject(adapterPosition);
                mClickHandler.onClick(eventJsonData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public SportsEventsAdapter.SportsEventsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForEvent = R.layout.sports_events_list_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(layoutIdForEvent, parent, false);
        return new SportsEventsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SportsEventsAdapter.SportsEventsAdapterViewHolder holder, int position) {
        try {
            JSONObject eventJsonData = mSportsEvents.getJSONObject(position);
            String imageUrl = eventJsonData.getString("image");
            String title = eventJsonData.getString("name");
            String description = eventJsonData.getString("category");

            holder.mEventTitle.setText(title);
            holder.mEventDescription.setText(description);
            Picasso.with(holder.mEventImage.getContext())
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(holder.mEventImage);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        if(null == mSportsEvents)
            return 0;
        return mSportsEvents.length();
    }

    public void setmSportsEvents(JSONArray mSportsEvents) {
        this.mSportsEvents = mSportsEvents;
        notifyDataSetChanged();
    }
}
