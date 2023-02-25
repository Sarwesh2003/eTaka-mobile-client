package com.example.etaka;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.etaka.ml.SoilNet;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private CardView iaCardView;
    private CardView lbaCardView;
    private CardView schemes;
    int imageSize = 224;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        iaCardView = findViewById(R.id.ia);
        lbaCardView = findViewById(R.id.lba);
        schemes = findViewById(R.id.schemes);
        iaCardView.setOnClickListener(v -> {
            if (checkPermission()) {
                ImagePicker.with(MainActivity.this)
                        .crop(1f,1f)	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA}, 1);
            }
        });

        lbaCardView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationBasedAnalysis.class);
            startActivity(intent);
        });
        schemes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SchemesDetails.class);
            startActivity(intent);
        });
    }
    public boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return permission1 == PackageManager.PERMISSION_GRANTED ;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            } else {
// Location permission denied, show an error message
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            imageUri = data.getData();
            try {
                Bitmap imageBitmap = (Bitmap) MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                int dimension = imageBitmap.getHeight();
                imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap,dimension,dimension);

                imageBitmap = Bitmap.createScaledBitmap(imageBitmap,imageSize,imageSize,false);
                classifyImage(imageBitmap);
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Error in retry", Toast.LENGTH_SHORT).show();
            }
//            image.setImageURI(imageUri);

//            lottieAnimationView.setVisibility(View.GONE);
//            image.getLayoutParams().height = image.getWidth();
//            image.requestLayout();

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }
    public void classifyImage(Bitmap image){
        try {
            SoilNet model1 = SoilNet.newInstance(getApplicationContext());


            // Creates inputs for reference.
            TensorBuffer inputFeature01 = TensorBuffer.createFixedSize(new int[]{1, 244, 244, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(4 * 244 * 244 * 3);
            byteBuffer1.order(ByteOrder.nativeOrder());

            int[] intValues1 = new int[244 * 244];
            image.getPixels(intValues1, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel1 = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < 244; i ++){
                for(int j = 0; j < 244; j++){
                    int val = intValues1[pixel1++]; // RGB
                    byteBuffer1.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer1.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer1.putFloat((val & 0xFF) * (1.f / 1));
                }
            }

            inputFeature01.loadBuffer(byteBuffer1);

            // Runs model inference and gets result.
            SoilNet.Outputs outputs1 = model1.process(inputFeature01);
            TensorBuffer outputFeature01 = outputs1.getOutputFeature0AsTensorBuffer();

            float[] confidences1 = outputFeature01.getFloatArray();
            Toast.makeText(this, "Confidence "+ Arrays.toString(confidences1) , Toast.LENGTH_SHORT).show();
            // find the index of the class with the biggest confidence.
            int maxPos1 = 0;
            float maxConfidence1 = 0;
            Log.d("Confidionce", String.valueOf(confidences1.length));
            for (int i = 0; i < confidences1.length; i++) {
                Log.d("Confidionce", String.valueOf(Math.round(confidences1[i])));
                if (confidences1[i] > maxConfidence1) {
                    maxConfidence1 = confidences1[i];
                    maxPos1 = i;
                }
            }
            String[] res = new String[]{"0: Alluvial Soil:-{ Rice,Wheat,Sugarcane,Maize,Cotton,Soyabean,Jute }",
                    "1: Black Soil:-{ Virginia, Wheat , Jowar,Millets,Linseed,Castor,Sunflower} ",
                    "2: Clay Soil:-{ Rice,Lettuce,Chard,Broccoli,Cabbage,Snap Beans }",
                    "3: Red Soil:{ Cotton,Wheat,Pilses,Millets,OilSeeds,Potatoes }"};
            Intent i = new Intent(MainActivity.this, ImageAnalysis.class);
            i.putExtra("imageUri",imageUri);
            i.putExtra("Type",maxPos1);
            startActivity(i);
            model1.close();
        } catch (IOException e) {
            Log.d("Confidionce", e.getMessage());
            // TODO Handle the exception
        }

    }
}