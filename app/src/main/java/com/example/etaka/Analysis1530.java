package com.example.etaka;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


public class Analysis1530 extends Fragment {
    Activity activity;
    TextView title, pred05, nitr05, oc05, ocDen05, ph05, cec05, sand05, silt05, clay05, crop05;
    private Button prev;
    private Button next;
    private JSONObject jsonObject;
    String response;
    RelativeLayout llpdf;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_analysis1530, container, false);
        activity = getActivity();
        response = getArguments().getString("RESPONSE");
        title = root.findViewById(R.id.layer_title);
        title.setText("Report[15 - 30cm]");
        prev = (Button) activity.findViewById(R.id.prev);
        next = (Button) activity.findViewById(R.id.next);
        pred05 = root.findViewById(R.id.pred1530);
        nitr05 = root.findViewById(R.id.nitr1530);
        oc05 = root.findViewById(R.id.oc1530);
        ocDen05 = root.findViewById(R.id.ocDen1530);
        ph05 = root.findViewById(R.id.ph1530);
        cec05 = root.findViewById(R.id.cec1530);
        sand05 = root.findViewById(R.id.sand1530);
        silt05 = root.findViewById(R.id.silt1530);
        clay05 = root.findViewById(R.id.clay1530);
        crop05 = root.findViewById(R.id.crop1530);
        llpdf = root.findViewById(R.id.rl);
        prev.setEnabled(true);
        prev.setOnClickListener(v ->{
            activity.onBackPressed();
        });
        next.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            Analysis3060 analysis3060 = new Analysis3060();
            Bundle b = new Bundle();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap bitmap = loadBitmapFromView(llpdf, llpdf.getWidth(), llpdf.getHeight());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encodedBitmap1530 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            String encodedBitmap05 = getArguments().getString("bitmap05");
            String encodedBitmap515 = getArguments().getString("bitmap515");
            b.putString("RESPONSE", response);
            b.putString("bitmap05", encodedBitmap05);
            b.putString("bitmap515", encodedBitmap515);
            b.putString("bitmap1530", encodedBitmap1530);
            analysis3060.setArguments(b);
            FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            transaction.replace(R.id.data_place_holder, analysis3060).addToBackStack(null);
            transaction.commit();
        });

        try {
            jsonObject = new JSONObject(response);

            JSONObject fertility = jsonObject.getJSONObject("Fertility");
            double cec0_5cm = fertility.getJSONObject("cec").getDouble("cec[15 - 30cm]");
            String cecUnit = fertility.getJSONObject("cec").getString("unit");
            double clay0_5cm = fertility.getJSONObject("clay").getDouble("clay[15 - 30cm]");
            String clayUnit = fertility.getJSONObject("clay").getString("unit");
            double nitrogen0_5cm = fertility.getJSONObject("nitrogen").getDouble("nitrogen[15 - 30cm]");
            String nitrogenUnit = fertility.getJSONObject("nitrogen").getString("unit");
            double oc0_5cm = fertility.getJSONObject("oc").getDouble("oc[15 - 30cm]");
            String ocUnit = fertility.getJSONObject("oc").getString("unit");
            double ocd0_5cm = fertility.getJSONObject("ocd").getDouble("ocd[15 - 30cm]");
            String ocdUnit = fertility.getJSONObject("ocd").getString("unit");
            double ph0_5cm = fertility.getJSONObject("ph").getDouble("ph[15 - 30cm]");
            String phUnit = fertility.getJSONObject("ph").getString("unit");
            double sand0_5cm = fertility.getJSONObject("sand").getDouble("sand[15 - 30cm]");
            String sandUnit = fertility.getJSONObject("sand").getString("unit");
            double silt0_5cm = fertility.getJSONObject("silt").getDouble("silt[15 - 30cm]");
            String siltUnit = fertility.getJSONObject("silt").getString("unit");
            String pred = fertility.getJSONObject("predictions").getString("prediction[15 - 30cm]");
            String crop = jsonObject.getJSONObject("crop").getString("crop[15 - 30cm]");
            pred = pred.equals("0")?"Fertile" : "Infertile";
            pred05.setText("Fertility:\n"+pred);
            nitr05.setText("Nitrogen"+"("+nitrogenUnit+")\n"+nitrogen0_5cm);
            oc05.setText("Organic Carbon" + "("+ocUnit+")\n" + oc0_5cm);
            ocDen05.setText("Organic Carbon Density" + "("+ocdUnit+")\n" + ocd0_5cm );
            ph05.setText("pH" + "("+phUnit+")\n" + ph0_5cm);
            cec05.setText("Cation Exchange Capacity" + "("+cecUnit+")\n" + cec0_5cm);
            sand05.setText("Sand" + "("+sandUnit+")\n" + sand0_5cm);
            silt05.setText("Silt" + "("+siltUnit+")\n" +silt0_5cm);
            clay05.setText("Clay" + "("+clayUnit+")\n" + clay0_5cm);
            crop05.setText("Crop\n" + crop);
        } catch (JSONException e) {
            Log.d("LOG_05", e.getMessage());
            e.printStackTrace();
        }
        return root;
    }
    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);

        return b;
    }
}