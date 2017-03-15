package elecompte.com.mbta;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class TripViewHolder extends RecyclerView.ViewHolder {
    TextView tripText;

    public TripViewHolder(View view) {
        super(view);
        tripText = (TextView) view.findViewById(R.id.textViewTrip);
    }
}


