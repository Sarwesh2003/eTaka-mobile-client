package com.example.etaka;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.cert.CertificateNotYetValidException;


public class AnalysisHome extends Fragment {


    String response = "";
    Button prev, next;
    Activity activity;
    TextView maj, percentage, temp, hum;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_analysis_home, container, false);
        activity = getActivity();
        response = getArguments() != null ? getArguments().getString("RESPONSE") : null;
        JSONObject jsonObject = null;


// Extract fields from the JSONObject

        TextView title = activity.findViewById(R.id.layer_title);
        title.setText("General Information");
        prev = (Button) activity.findViewById(R.id.prev);
        next = (Button) activity.findViewById(R.id.next);
        maj = (TextView) root.findViewById(R.id.majFertility);
        percentage = (TextView) root.findViewById(R.id.ferPerc);
        temp = (TextView) root.findViewById(R.id.temp);
        hum = (TextView) root.findViewById(R.id.humidity);
        next.setEnabled(true);
        prev.setEnabled(false);
        next.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            Analysis05cm analysis05cm = new Analysis05cm();
            Bundle b = new Bundle();
            b.putString("RESPONSE", response);
            analysis05cm.setArguments(b);
            FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
               transaction.replace(R.id.data_place_holder, analysis05cm).addToBackStack(null);
            transaction.commit();
        });


        try {
            jsonObject = new JSONObject(response);
            String majority = jsonObject.getJSONObject("Fertility").getJSONObject("predictions").getString("majority");
            int totalPrediction = jsonObject.getJSONObject("Fertility").getJSONObject("predictions").getInt("totalPrediction");
            double humidity = jsonObject.getJSONObject("crop").getDouble("humidity");
            double temperature = jsonObject.getJSONObject("crop").getDouble("temperature");

            maj.setText(majority + "\nOverall");
            percentage.setText(totalPrediction+"%"+"\nFertile");
            hum.setText(humidity + "\nHumidity");
            temp.setText(temperature + "\nTemperature");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }

}