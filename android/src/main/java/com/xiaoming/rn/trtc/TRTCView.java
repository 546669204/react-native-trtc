package com.xiaoming.rn.trtc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.ThemedReactContext;
import com.tencent.rtmp.ui.TXCloudVideoView;

import static com.xiaoming.rn.trtc.RNTRTCModule.frontCamera;
import static com.xiaoming.rn.trtc.RNTRTCModule.mEngine;

public class TRTCView extends FrameLayout implements LifecycleEventListener {

    private TXCloudVideoView mLocalView;
    private ThemedReactContext _context;

    private String userId = "";

    public TRTCView(ThemedReactContext context) {
        super(context);
        this._context = context;
        context.addLifecycleEventListener(this);

        RelativeLayout mCameraView = new RelativeLayout(this._context);
        mCameraView.setGravity(17);
        mCameraView.setBackgroundColor(255);

        mCameraView.layout(0,0,0,0);
        mLocalView = new TXCloudVideoView(this._context);

        mCameraView.addView(mLocalView);

        this.addView(mCameraView);

        if (mEngine == null) {
            return ;
        }
        if(userId.equals("")){
            mEngine.startLocalPreview(frontCamera,mLocalView);
        }else{
            mEngine.startRemoteView(userId, mLocalView);
        }

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
    }

    public void start(){
        if (mEngine == null) {
            return ;
        }
        if(userId.equals("")){
            mEngine.startLocalPreview(frontCamera,mLocalView);
        }else{
            mEngine.startRemoteView(userId, mLocalView);
        }

    }
    public void stop(){
        if (mEngine == null) {
            return ;
        }
        if(userId.equals("")){
            mEngine.stopLocalPreview();
        }else{
            mEngine.stopRemoteView(userId);
        }

    }
    public void setUserId(String var1){
        this.userId = var1;
    }


    @Override
    public void onHostResume() {
        if (hasCameraPermissions()) {

            //防止其他Activity resume触发刷新
//            Activity activity  = mReactContext.getCurrentActivity();
//            if (activity != null && activity == _context){
            Log.d("xm", "onHostResume: "+userId);
//            start();
//            }

        } else {
            Log.e("xm", "没有摄像机权限" );
        }

    }

    @Override
    public void onHostPause() {
//        stop();
    }

    @Override
    public void onHostDestroy() {
        //友情提示：rn的ReactContext传给了不同activity，如果不作处理，
        //其他RN Activity onDestroy时候会使得监听注销，页面无法刷新.
//        Activity activity  = _context.getCurrentActivity();
//        if (activity != null && activity == _context){
        stop();
        _context.removeLifecycleEventListener(this);
//        }

    }
    private boolean hasCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

}