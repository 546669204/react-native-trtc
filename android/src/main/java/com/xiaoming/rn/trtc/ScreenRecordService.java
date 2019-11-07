package com.xiaoming.rn.trtc;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.opengl.EGLContext;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.xiaoming.rn.trtc.customCapture.openGLBaseModule.GLThread;

@TargetApi(21)
public class ScreenRecordService extends  Service implements GLThread.IGLSurfaceTextureListener {

    private static final String TAG = "ScreenRecordingService";

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private int mResultCode;
    private Intent mResultData;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private TRTCCloud mTRTCCloud;
    private GLThread mGLThread;
    private boolean mIsSending;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        // TODO Auto-generated method stub
        Log.i(TAG, "Service onStartCommand() is called");

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();

        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        mScreenWidth = intent.getIntExtra("width", dm.widthPixels);
        mScreenHeight = intent.getIntExtra("height", dm.heightPixels);
        mScreenDensity = intent.getIntExtra("density", dm.densityDpi);
        Log.d(TAG,"width:"+mScreenWidth +"height:"+mScreenHeight+"dpi:"+mScreenDensity);
        mContext = this;
        mTRTCCloud = TRTCCloud.sharedInstance(this);
        start();

        return Service.START_NOT_STICKY;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void start(){
        mMediaProjection =  createMediaProjection();
        mGLThread = new GLThread();
        mGLThread.setListener(this);
        mGLThread.start();
        mIsSending = true;

    }
    public void stop(){
        if (mGLThread != null) mGLThread.stop();
        mIsSending = false;
    }

    private MediaProjection createMediaProjection() {
        Log.i(TAG, "Create MediaProjection");
        return ((MediaProjectionManager) mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(mResultCode, mResultData);
    }

    private VirtualDisplay createVirtualDisplay(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "Create VirtualDisplay");
        try {
            mGLThread.setInputSize(mScreenWidth, mScreenHeight);

            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, mScreenWidth, mScreenHeight, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, new Surface(surfaceTexture), null, null);
            Log.i(TAG, "Create VirtualDisplay"+mVirtualDisplay);

        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        return mVirtualDisplay;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture) {
        mVirtualDisplay = createVirtualDisplay(surfaceTexture);
    }

    @Override
    public int onTextureProcess(int textureId, EGLContext eglContext) {
        if (!mIsSending) return textureId;

        //将视频帧通过纹理方式塞给SDK
        TRTCCloudDef.TRTCVideoFrame videoFrame = new TRTCCloudDef.TRTCVideoFrame();
        videoFrame.texture = new TRTCCloudDef.TRTCTexture();
        videoFrame.texture.textureId = textureId;
        videoFrame.texture.eglContext14 = eglContext;
        videoFrame.width = mScreenWidth;
        videoFrame.height = mScreenHeight;
        videoFrame.pixelFormat = TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_Texture_2D;
        videoFrame.bufferType = TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_TEXTURE;
        mTRTCCloud.sendCustomVideoData(videoFrame);
        return textureId;
    }

    @Override
    public void onSurfaceTextureDestroy(SurfaceTexture surfaceTexture) {

    }

}