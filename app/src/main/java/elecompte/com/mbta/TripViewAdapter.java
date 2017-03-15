package elecompte.com.mbta;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import elecompte.com.mbta.model.Trip;

/**
 * Created by evan on 3/13/17.
 */

public class TripViewAdapter extends RecyclerView.Adapter<TripViewHolder> {
    private Context mContext;
    private final List<Trip> trips;

    public TripViewAdapter(Context context, List<Trip> data) {
        super();
        mContext = context;
        trips = data;
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.tripText.setText(trip.getPreAwayFormatted());
    }

    @Override
    public int getItemCount() {
        return trips == null ? 0 : trips.size();
    }
}


