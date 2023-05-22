package com.example.yearprojectfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

// This class is the adapter for the locations list view
public class LocationAdapter extends ArrayAdapter<LocationClass>
{
    Context context;
    List<LocationClass> objects;
    public LocationAdapter(Context context, int resource, int textViewResourceId, List<LocationClass> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context=context;
        this.objects=objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.location_layout, parent, false);
        }

        TextView tvLocationName = convertView.findViewById(R.id.tvLocationName);
        TextView tvLocationDesc = convertView.findViewById(R.id.tvLocationDescription);
        TextView tvLocationAddressName = convertView.findViewById(R.id.tvLocationAddressName);
        TextView tvLocationCoords = convertView.findViewById(R.id.tvLocationCoords);

        LocationClass temp = objects.get(position);
        tvLocationName.setText(temp.getName());
        tvLocationDesc.setText(temp.getDescription());
        tvLocationAddressName.setText(temp.getAddressName());
        tvLocationCoords.setText(temp.getLatitude() + ", " + temp.getLongitude());

        Button btnDone = convertView.findViewById(R.id.btnMoveToLocation);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                float zoomLevel = UtilityClass.zoomLevelByRadius(temp.getRadius());
                // Call removeTask method - handles both server DB and listview
                ((MainLocationsActivity)context)
                        .mMap
                        .animateCamera(CameraUpdateFactory.
                                newLatLngZoom(
                                        new LatLng(temp.getLatitude(), temp.getLongitude()),
                                        zoomLevel));
                ;

            }
        });

        return convertView;
    }
}

