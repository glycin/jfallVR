package com.jfall.alexander.photodemo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Alexander on 26/10/16.
 */

public class PanoramaMainActivity extends Activity {
    private static final String TAG = PanoramaMainActivity.class.getSimpleName();

    private VrPanoramaView panoWidgetView;
    private VrVideoView videoWidgetView;
    //Amount of images to choose from
    private static final int PICK_IMAGE = 100;

    // The fun begins
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        panoWidgetView = (VrPanoramaView) findViewById(R.id.pano_view);
        //videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        loadImageFromGallery();
        //loadVideo();
    }


    private void loadVideo(){
        VrVideoView.Options options = new VrVideoView.Options();
        options.inputType = Options.TYPE_STEREO_OVER_UNDER;
        try {
            videoWidgetView.loadVideoFromAsset("congo.mp4", options);
            videoWidgetView.playVideo();
        }catch(Exception e){
            Log.e(TAG,"Bad stuff happened opening the " + e);
        }

    }

    private void loadImageFromGallery() {
        Intent gallery =
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    // Picture was chosen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Options panoOptions = new Options();
        panoOptions.inputType = Options.TYPE_STEREO_OVER_UNDER;
        panoOptions.inputType = Options.TYPE_MONO;
        Bitmap bitmap = null;
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            }catch(Exception e){
                Log.e(TAG,"Bad stuff happened opening the image" + e);
            }
            panoWidgetView.loadImageFromBitmap(bitmap,panoOptions);
        }
    }

    // Standard Android Methods
    @Override
    protected void onPause() {
        panoWidgetView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoWidgetView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        panoWidgetView.shutdown();
        super.onDestroy();
    }
}
