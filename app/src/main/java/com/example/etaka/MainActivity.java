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
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.etaka.ml.SoilModelV2;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private CardView iaCardView;
    private CardView lbaCardView;
    private CardView schemes;
    int imageSize = 224;
    private Uri imageUri;
    private Interpreter interpreter;

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

                imageBitmap = Bitmap.createScaledBitmap(imageBitmap,imageSize,imageSize,true);
                classifyImage(imageBitmap);
            }
            catch (Exception e)
            {
                Log.d("ErrorHere:", e.getMessage());
                Toast.makeText(this, "Error in here" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        int batchSize = 1;
        int inputSize = 224;
        int numChannels = 3;


        Interpreter tflite;
        try {
            tflite = new Interpreter(loadModelFile("SoilModelV2.tflite"));
            Bitmap resizedImage = Bitmap.createScaledBitmap(image, 244, 244, true);
            float[][][][] inputBuffer = new float[1][224][224][3];
            for (int y = 0; y < 224; y++) {
                for (int x = 0; x < 224; x++) {
                    int pixel = resizedImage.getPixel(x, y);
                    inputBuffer[0][y][x][0] = ((Color.red(pixel) - 127.5f) / 127.5f);
                    inputBuffer[0][y][x][1] = ((Color.green(pixel) - 127.5f) / 127.5f);
                    inputBuffer[0][y][x][2] = ((Color.blue(pixel) - 127.5f) / 127.5f);
                }
            }
            float[][] outputBuffer = new float[1][5]; // Assuming 5 classes in the output
            tflite.run(inputBuffer, outputBuffer);
            float[] predictions = outputBuffer[0];

// Find the index of the maximum prediction (argmax)
            int predictedClassIndex = 0;
            float maxPrediction = predictions[0];

            for (int i = 1; i < predictions.length; i++) {
                if (predictions[i] > maxPrediction) {
                    maxPrediction = predictions[i];
                    predictedClassIndex = i;
                }
            }
            Log.d("Class:", String.valueOf(predictedClassIndex));
        } catch (IOException e) {
            Log.d("Error in Classify: ", e.getMessage());
        }

    }
    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}