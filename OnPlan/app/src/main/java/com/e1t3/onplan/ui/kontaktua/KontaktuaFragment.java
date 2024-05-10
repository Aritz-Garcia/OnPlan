package com.e1t3.onplan.ui.kontaktua;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import com.e1t3.onplan.R;
import com.e1t3.onplan.databinding.FragmentKontaktuaBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class KontaktuaFragment extends Fragment implements OnMapReadyCallback{

    private FragmentKontaktuaBinding binding;

    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_kontaktua, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        return rootView;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(43.27154779067166, -2.944718346574989);
        mMap.addMarker(new MarkerOptions()
                .position(sydney));
        mMap.setMinZoomPreference(18.0f);
        mMap.setMaxZoomPreference(42.0f);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}