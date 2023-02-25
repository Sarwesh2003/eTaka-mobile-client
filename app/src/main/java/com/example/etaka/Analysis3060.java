package com.example.etaka;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Analysis3060 extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 300;
    Activity activity;
    TextView title, pred05, nitr05, oc05, ocDen05, ph05, cec05, sand05, silt05, clay05, crop05;
    private RelativeLayout llPdf;
    private Bitmap bitmap;
    private Button prev;
    private Button next;
    private JSONObject jsonObject;
    String response;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_analysis3060, container, false);
        activity = getActivity();
        response = getArguments().getString("RESPONSE");

        title = root.findViewById(R.id.layer_title);
        title.setText("Report[30 - 60cm]");
        pred05 = root.findViewById(R.id.pred3060);
        nitr05 = root.findViewById(R.id.nitr3060);
        oc05 = root.findViewById(R.id.oc3060);
        ocDen05 = root.findViewById(R.id.ocDen3060);
        ph05 = root.findViewById(R.id.ph3060);
        cec05 = root.findViewById(R.id.cec3060);
        sand05 = root.findViewById(R.id.sand3060);
        silt05 = root.findViewById(R.id.silt3060);
        clay05 = root.findViewById(R.id.clay3060);
        crop05 = root.findViewById(R.id.crop3060);
        llPdf = root.findViewById(R.id.rl);
        prev = (Button) activity.findViewById(R.id.prev);
        next = (Button) activity.findViewById(R.id.next);
        next.setEnabled(true);
        next.setText("Export PDF");
        prev.setOnClickListener(v ->{
            activity.onBackPressed();
        });
        next.setOnClickListener(v -> {
            bitmap = loadBitmapFromView(llPdf, llPdf.getWidth(), llPdf.getHeight());
            createPdf();
        });
        try {
            jsonObject = new JSONObject(response);

            JSONObject fertility = jsonObject.getJSONObject("Fertility");
            double cec0_5cm = fertility.getJSONObject("cec").getDouble("cec[30 - 60cm]");
            String cecUnit = fertility.getJSONObject("cec").getString("unit");
            double clay0_5cm = fertility.getJSONObject("clay").getDouble("clay[30 - 60cm]");
            String clayUnit = fertility.getJSONObject("clay").getString("unit");
            double nitrogen0_5cm = fertility.getJSONObject("nitrogen").getDouble("nitrogen[30 - 60cm]");
            String nitrogenUnit = fertility.getJSONObject("nitrogen").getString("unit");
            double oc0_5cm = fertility.getJSONObject("oc").getDouble("oc[30 - 60cm]");
            String ocUnit = fertility.getJSONObject("oc").getString("unit");
            double ocd0_5cm = fertility.getJSONObject("ocd").getDouble("ocd[30 - 60cm]");
            String ocdUnit = fertility.getJSONObject("ocd").getString("unit");
            double ph0_5cm = fertility.getJSONObject("ph").getDouble("ph[30 - 60cm]");
            String phUnit = fertility.getJSONObject("ph").getString("unit");
            double sand0_5cm = fertility.getJSONObject("sand").getDouble("sand[30 - 60cm]");
            String sandUnit = fertility.getJSONObject("sand").getString("unit");
            double silt0_5cm = fertility.getJSONObject("silt").getDouble("silt[30 - 60cm]");
            String siltUnit = fertility.getJSONObject("silt").getString("unit");
            String pred = fertility.getJSONObject("predictions").getString("prediction[30 - 60cm]");
            String crop = jsonObject.getJSONObject("crop").getString("crop[30 - 60cm]");
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
    public void createPdf(){
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        //  Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float hight = displaymetrics.heightPixels ;
        float width = displaymetrics.widthPixels ;

        int convertHighet = (int) hight, convertWidth = (int) width;

//        Resources mResources = getResources();
//        Bitmap bitmap = BitmapFactory.decodeResource(mResources, R.drawable.screenshot);


        String encodedBitmap05 = getArguments().getString("bitmap05");
        byte[] decodedString = Base64.decode(encodedBitmap05, Base64.DEFAULT);
        Bitmap bitmap05 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        String encodedBitmap515 = getArguments().getString("bitmap515");
        decodedString = Base64.decode(encodedBitmap515, Base64.DEFAULT);
        Bitmap bitmap515 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        String encodedBitmap1530 = getArguments().getString("bitmap1530");
        decodedString = Base64.decode(encodedBitmap1530, Base64.DEFAULT);
        Bitmap bitmap1530 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo05 = new PdfDocument.PageInfo.Builder(convertWidth, convertHighet, 1).create();
        PdfDocument.Page page0 = document.startPage(pageInfo05);
        bitmap05 = Bitmap.createScaledBitmap(bitmap05, convertWidth, convertHighet, true);
        Canvas canvas05 = page0.getCanvas();
        Paint title = new Paint();
        Paint paint05 = new Paint();
        canvas05.drawPaint(paint05);
        paint05.setColor(Color.WHITE);

        canvas05.drawBitmap(bitmap05, 0, 0 , paint05); //Try null

        document.finishPage(page0);
//------------------------Page 1 Ends--------------------------------
        bitmap515 = Bitmap.createScaledBitmap(bitmap515, convertWidth, convertHighet, true);
        PdfDocument.PageInfo pageInfo515 = new PdfDocument.PageInfo.Builder(convertWidth, convertHighet, 2).create();
        PdfDocument.Page page1 = document.startPage(pageInfo515);
        bitmap515 = Bitmap.createScaledBitmap(bitmap515, convertWidth, convertHighet, true);
        Canvas canvas515 = page1.getCanvas();

        Paint paint515 = new Paint();
        canvas515.drawPaint(paint515);
        paint515.setColor(Color.BLUE);
        canvas515.drawBitmap(bitmap515, 0, 0 , paint515);
        document.finishPage(page1);
//------------------------Page 2 Ends---------------------------------

        bitmap1530 = Bitmap.createScaledBitmap(bitmap1530, convertWidth, convertHighet, true);
        PdfDocument.PageInfo pageInfo1530 = new PdfDocument.PageInfo.Builder(convertWidth, convertHighet, 3).create();
        PdfDocument.Page page2 = document.startPage(pageInfo1530);
        bitmap1530 = Bitmap.createScaledBitmap(bitmap1530, convertWidth, convertHighet, true);
        Canvas canvas1530 = page2.getCanvas();

        Paint paint1530 = new Paint();
        canvas1530.drawPaint(paint1530);
        paint1530.setColor(Color.BLUE);
        canvas1530.drawBitmap(bitmap1530, 0, 0 , paint1530);
        document.finishPage(page2);

//        ------------------Page 3 Ends-------------------------------
        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHighet, true);
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, convertHighet, 4).create();
        PdfDocument.Page page3 = document.startPage(pageInfo);
        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHighet, true);
        Canvas canvas = page3.getCanvas();

        Paint paint = new Paint();
        canvas.drawPaint(paint);
        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0 , paint);
        document.finishPage(page3);

        // write the document content
        String targetPdf = activity.getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/SoilHealth.pdf";
        File filePath;
        filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();
        Toast.makeText(activity, "PDF is created!!!", Toast.LENGTH_SHORT).show();

        openGeneratedPDF();
    }
    private void openGeneratedPDF(){
        File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/SoilHealth.pdf");
        if (file.exists())
        {
            Intent intent=new Intent(Intent.ACTION_VIEW);
            File filePath;
            filePath = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/SoilHealth.pdf");
            Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider",filePath);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try
            {
                startActivity(intent);
            }
            catch(ActivityNotFoundException e)
            {
                Toast.makeText(activity, "No Application available to view pdf", Toast.LENGTH_LONG).show();
            }
        }
    }

}