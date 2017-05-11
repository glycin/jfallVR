package com.example.rajivaliexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.MotionEvent;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture.TextureException;
import org.rajawali3d.materials.textures.NormalMapTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.terrain.SquareTerrain;
import org.rajawali3d.terrain.TerrainGenerator;
import com.example.rajivaliexample.vr.renderer.VRRenderer;
import org.rajawali3d.util.RajLog;

public class RajawaliVRExampleRenderer extends VRRenderer {
    private SquareTerrain terrain;
    private Sphere bullet;
    private Cube cube;
    private CardboardAudioEngine cardboardAudioEngine;
    private volatile int laserSoundID = CardboardAudioEngine.INVALID_ID;
    private volatile int explosionSoundID = CardboardAudioEngine.INVALID_ID;

    public RajawaliVRExampleRenderer(Context context) {
        super(context);
    }

    @Override
    public void initScene() {
        DirectionalLight light = new DirectionalLight(0.2f, -1f, 0f);
        light.setPower(.7f);
        getCurrentScene().addLight(light);

        light = new DirectionalLight(0.2f, 1f, 0f);
        light.setPower(1f);
        getCurrentScene().addLight(light);

        getCurrentCamera().setFarPlane(1000);

        getCurrentScene().setBackgroundColor(0xdddddd);

        createTerrain();

        try {
            getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx, R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
        }catch (Exception e){
            RajLog.e(e.getStackTrace().toString());
        }

        cube = new Cube(2);
        Material cubeMat = new Material();
        cubeMat.setDiffuseMethod(new DiffuseMethod.Lambert());
        cubeMat.enableLighting(false);
        cube.setMaterial(cubeMat);
        cube.setColor(Color.YELLOW);
        cube.setPosition(5,5,-16);
        getCurrentScene().addChild(cube);

        initAudio();

    }

    private void initAudio() {
        cardboardAudioEngine =
                new CardboardAudioEngine(getContext().getAssets(), CardboardAudioEngine.RenderingQuality.HIGH);
        new Thread(
                new Runnable() {
                    public void run() {
                        cardboardAudioEngine.preloadSoundFile("laser.mp3");
                        cardboardAudioEngine.preloadSoundFile("explosion.mp3");
                    }
                })
                .start();
    }

    public void pauseAudio() {
        if(cardboardAudioEngine != null) {
            cardboardAudioEngine.pause();
        }
    }

    public void resumeAudio() {
        if(cardboardAudioEngine != null) {
            cardboardAudioEngine.resume();
        }
    }

    public void createTerrain() {

        Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.terrain);

        try {
            SquareTerrain.Parameters terrainParams = SquareTerrain.createParameters(bmp);
            terrainParams.setScale(4f, 54f, 4f);
            terrainParams.setDivisions(128);
            terrainParams.setTextureMult(4);
            terrainParams.setColorMapBitmap(bmp);
            terrain = TerrainGenerator.createSquareTerrainFromBitmap(terrainParams, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bmp.recycle();
        Material material = new Material();
        material.enableLighting(true);
        material.useVertexColors(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        try {
            Texture groundTexture = new Texture("ground", R.drawable.ground);
            groundTexture.setInfluence(.5f);
            material.addTexture(groundTexture);
            material.addTexture(new NormalMapTexture("groundNormalMap", R.drawable.groundnor));
            material.setColorInfluence(0);
        } catch (TextureException e) {
            e.printStackTrace();
        }
        material.setColorInfluence(.5f);
        terrain.setY(-100);
        terrain.setMaterial(material);

        getCurrentScene().addChild(terrain);
    }

    @Override
    public void onRender(long elapsedTime, double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        boolean isLookingAt = isLookingAtObject(cube);
        if(isLookingAt) {
            cube.setColor(Color.BLUE);
        } else {
            cube.setColor(Color.YELLOW);
        }
        if(bullet !=null){
            MoveBullet(bullet);
        }
        cardboardAudioEngine.setHeadRotation(
                (float)mHeadViewQuaternion.x, (float)mHeadViewQuaternion.y, (float)mHeadViewQuaternion.z, (float)mHeadViewQuaternion.w);
    }

    private void MoveBullet(Sphere bullet){
        bullet.moveForward(-5);
    }

    private void MoveCube(){
        double floatX = 0.5 + Math.random() * (25 - 0.5);
        double floatY = 0.5 + Math.random() * (25 - 0.5);
        double floatZ = 0.5 + Math.random() * (25 - 0.5);
        cube.setPosition(floatX,floatY,floatZ);
        explosionSoundID = cardboardAudioEngine.createSoundObject("explosion.mp3");
        cardboardAudioEngine.playSound(explosionSoundID, false);
    }

    public void spawnBullet(){
        if(bullet == null) {
            bullet = new Sphere(2f, 12, 12);
            Material spehreMat = new Material();
            spehreMat.setDiffuseMethod(new DiffuseMethod.Lambert());
            spehreMat.enableLighting(false);
            bullet.setMaterial(spehreMat);
            bullet.setColor(Color.RED);
            getCurrentScene().addChild(bullet);
        }
        bullet.setPosition(getCurrentCamera().getPosition().clone());
        bullet.setRotation(getCurrentCamera().getOrientation().clone());
        bullet.moveForward(-10);
        laserSoundID = cardboardAudioEngine.createSoundObject("laser.mp3");
        cardboardAudioEngine.playSound(laserSoundID, false);
        if(isLookingAtObject(cube)){
            MoveCube();
        }
    }
    @Override
    public void onTouchEvent(MotionEvent e){
        RajLog.d("PEW PEW PEW");
    }

    @Override
    public void onOffsetsChanged(float a, float b, float c, float d, int x , int i){

    }
}
