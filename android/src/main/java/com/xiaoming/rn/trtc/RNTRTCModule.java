package com.xiaoming.rn.trtc;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.BoringLayout;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.LifecycleEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.rtmp.TXLog;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudDef.*;
import com.tencent.trtc.TRTCCloudListener;
import com.tencent.trtc.TRTCStatistics;

import static android.app.Activity.RESULT_OK;


public class RNTRTCModule extends ReactContextBaseJavaModule  implements  LifecycleEventListener{

  private final ReactApplicationContext reactContext;
  public static TRTCCloud mEngine;
  public static Boolean frontCamera = true;
  private static String TAG = "xm.trtc";

  /** 是否已经开启视频录制 */
  private boolean isScreenRecord = false;
  private ScreenRecord screenRecord ;

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      if (requestCode == 1334) {
        if(resultCode == RESULT_OK) {
          // 获得权限，启动Service开始录制

//          Intent service = new Intent(reactContext, ScreenRecordService.class);
//          service.putExtra("code", resultCode);
//          service.putExtra("data", intent);
//          reactContext.startService(service);

          screenRecord = new ScreenRecord(reactContext);
          screenRecord.init(resultCode,intent);
          screenRecord.start();
          // 已经开始屏幕录制，修改UI状态
          isScreenRecord = true;
          Log.i(TAG, "Started screen recording");
        } else {
          Log.i(TAG, "User cancelled");
        }
      }
    }
  };

//  private Map<String, AliRtcVideoTrack> mVideoCanvasMap = new HashMap<>(16);
  public RNTRTCModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    reactContext.addLifecycleEventListener(this);
    reactContext.addActivityEventListener(mActivityEventListener);
  }


  @Override
  public void onHostResume() {
    // Activity `onResume`
  }

  @Override
  public void onHostPause() {
    // Activity `onPause`
    if (mEngine == null) {
      return;
    }
    try {
      mEngine.stopLocalPreview();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onHostDestroy() {
    // Activity `onDestroy`
    if(mEngine == null){
      return;
    }
    TRTCCloud.destroySharedInstance();
  }

  @ReactMethod
  public void initEngine(Promise promise) {
    try {
      mEngine = TRTCCloud.sharedInstance(reactContext);
      mEngine.setListener(_TRTCCloudListener);
//      mEngine.setListenerHandler();
      promise.resolve(true);
    }catch (Exception e){
      promise.reject(e);
    }

  }

  @ReactMethod
  public void joinChannel(ReadableMap data,Integer scene,Promise promise) {
    if (mEngine == null) {
      promise.reject("not init engine");
      return;
    }
    TRTCParams userInfo = new TRTCParams();
    userInfo.sdkAppId = data.getInt("sdkAppId");
    userInfo.userId = data.getString("userId");
    userInfo.userSig = data.getString("userSig");
    userInfo.roomId = data.getInt("roomId");
    userInfo.role = data.getInt("role");
    userInfo.privateMapKey = data.getString("privateMapKey");
    userInfo.businessInfo = data.getString("businessInfo");
    // 加入频道
    mEngine.enterRoom(userInfo, scene);
    promise.resolve(true);
  }

  @ReactMethod
  public void leaveChannel(Promise promise) {
    if (mEngine == null) {
      promise.reject("not init engine");
      return;
    }
    mEngine.exitRoom();
    promise.resolve(true);
  }

  @ReactMethod
  public void startLocalPreview() {
    mEngine.startLocalPreview(frontCamera,null);
  }

  @ReactMethod
  public void stopLocalPreview() {
    mEngine.stopLocalPreview();
  }


  @ReactMethod
  public void startLocalAudio() {
    mEngine.startLocalAudio();
  }

  @ReactMethod
  public void stopLocalAudio() {
    mEngine.stopLocalAudio();
  }

  @ReactMethod
  public void setAudioRoute(Integer route) {
    mEngine.setAudioRoute(route);
  }


  @TargetApi(21)
  @ReactMethod
  public void startScreenRecord() {
    if(isScreenRecord){
      screenRecord.start();
      return;
    }
    MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) reactContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    Intent permissionIntent = mediaProjectionManager.createScreenCaptureIntent();
    reactContext.startActivityForResult(permissionIntent, 1334,null);
  }


  @ReactMethod
  public void stopScreenRecord(Promise promise) {
    if(!isScreenRecord){
      promise.resolve(true);
      return;
    }
    screenRecord.stop();
    promise.resolve(true);
  }

  private void sendEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
  }


  @Override
  public String getName() {
    return "RNTRTCModule";
  }


  private TRTCCloudListener _TRTCCloudListener = new TRTCCloudListener(){

    /**
     * 加入房间回调
     *
     * @param elapsed 加入房间耗时，单位毫秒
     */
    @Override
    public void onEnterRoom(long elapsed) {
      Log.i(TAG, "onEnterRoom: elapsed = " + elapsed);
      WritableMap params = Arguments.createMap();
      params.putDouble("elapsed", elapsed);
      sendEvent(reactContext, "onEnterRoom", params);
    }

    /**
     * ERROR 大多是不可恢复的错误，需要通过 UI 提示用户
     * 然后执行退房操作
     *
     * @param errCode   错误码 TXLiteAVError
     * @param errMsg    错误信息
     * @param extraInfo 扩展信息字段，个别错误码可能会带额外的信息帮助定位问题
     */
    @Override
    public void onError(int errCode, String errMsg, Bundle extraInfo) {
      Log.i(TAG, "onError: errCode = " + errCode + " errMsg = " + errMsg);
      WritableMap params = Arguments.createMap();
      params.putDouble("errCode", errCode);
      params.putString("errMsg", errMsg);
      sendEvent(reactContext, "onError", params);
    }

    /**
     * 有新的主播{@link TRTCCloudDef#TRTCRoleAnchor}加入了当前视频房间
     * 该方法会在主播加入房间的时候进行回调，此时音频数据会自动拉取下来，但是视频需要有 View 承载才会开始渲染。
     * 为了更好的交互体验，Demo 选择在 onUserVideoAvailable 中，申请 View 并且开始渲染。
     * 您可以根据实际需求，选择在 onUserEnter 还是 onUserVideoAvailable 中发起渲染。
     *
     * @param userId 用户标识
     */
    @Override
    public void onUserEnter(String userId) {
      Log.i(TAG, "onUserEnter: userId = " + userId);
      WritableMap params = Arguments.createMap();
      params.putString("userId", userId);
      sendEvent(reactContext, "onUserEnter", params);
    }

    /**
     * 主播{@link TRTCCloudDef#TRTCRoleAnchor}离开了当前视频房间
     * 主播离开房间，要释放相关资源。
     * 1. 释放主画面、辅路画面
     * 2. 如果您有混流的需求，还需要重新发起混流，保证混流的布局是您所期待的。
     *
     * @param userId 用户标识
     * @param reason 离开原因代码，区分用户是正常离开，还是由于网络断线等原因离开。
     */
    @Override
    public void onUserExit(String userId, int reason) {
      Log.i(TAG, "onUserExit: userId = " + userId + " reason = " + reason);
      WritableMap params = Arguments.createMap();
      params.putString("userId", userId);
      params.putInt("reason", reason);
      sendEvent(reactContext, "onUserExit", params);
    }

    /**
     * 若当对应 userId 的主播有上行的视频流的时候，该方法会被回调，available 为 true；
     * 若对应的主播通过{@link TRTCCloud#muteLocalVideo(boolean)}，该方法也会被回调，available 为 false。
     * Demo 在收到主播有上行流的时候，会通过{@link TRTCCloud#startRemoteView(String, TXCloudVideoView)} 开始渲染
     * Demo 在收到主播停止上行的时候，会通过{@link TRTCCloud#stopRemoteView(String)} 停止渲染，并且更新相关 UI
     *
     * @param userId    用户标识
     * @param available 画面是否开启
     */
    @Override
    public void onUserVideoAvailable(final String userId, boolean available) {
      Log.i(TAG, "onUserVideoAvailable: userId = " + userId + " available = " + available);
      WritableMap params = Arguments.createMap();
      params.putString("userId", userId);
      params.putBoolean("available", available);
      sendEvent(reactContext, "onUserVideoAvailable", params);
    }

    /**
     * 是否有辅路上行的回调，Demo 中处理方式和主画面的一致 {@link TRTCCloudListenerImpl#onUserVideoAvailable(String, boolean)}
     *
     * @param userId    用户标识
     * @param available 屏幕分享是否开启
     */
    @Override
    public void onUserSubStreamAvailable(final String userId, boolean available) {
      Log.i(TAG, "onUserSubStreamAvailable: userId = " + userId + " available = " + available);
    }

    /**
     * 是否有音频上行的回调
     * <p>
     * 您可以根据您的项目要求，设置相关的 UI 逻辑，比如显示对端闭麦的图标等
     *
     * @param userId    用户标识
     * @param available true：音频可播放，false：音频被关闭
     */
    @Override
    public void onUserAudioAvailable(String userId, boolean available) {
      Log.i(TAG, "onUserAudioAvailable: userId = " + userId + " available = " + available);
    }

    /**
     * 视频首帧渲染回调
     * <p>
     * 一般客户可不关注，专业级客户质量统计等；您可以根据您的项目情况决定是否进行统计或实现其他功能。
     *
     * @param userId     用户 ID
     * @param streamType 视频流类型
     * @param width      画面宽度
     * @param height     画面高度
     */
    @Override
    public void onFirstVideoFrame(String userId, int streamType, int width, int height) {
      Log.i(TAG, "onFirstVideoFrame: userId = " + userId + " streamType = " + streamType + " width = " + width + " height = "+ height);
    }

    /**
     * 音量大小回调
     * <p>
     * 您可以用来在 UI 上显示当前用户的声音大小，提高用户体验
     *
     * @param userVolumes 所有正在说话的房间成员的音量（取值范围0 - 100）。即 userVolumes 内仅包含音量不为0（正在说话）的用户音量信息。其中本地进房 userId 对应的音量，表示 local 的音量，也就是自己的音量。
     * @param totalVolume 所有远端成员的总音量, 取值范围 [0, 100]
     */
    @Override
    public void onUserVoiceVolume(ArrayList<TRTCVolumeInfo> userVolumes, int totalVolume) {

    }

    /**
     * SDK 状态数据回调
     * <p>
     * 一般客户无需关注，专业级客户可以用来进行统计相关的性能指标；您可以根据您的项目情况是否实现统计等功能
     *
     * @param statics 状态数据
     */
    @Override
    public void onStatistics(TRTCStatistics statics) {

    }

    /**
     * 跨房连麦会结果回调
     *
     * @param userID
     * @param err
     * @param errMsg
     */
    @Override
    public void onConnectOtherRoom(final String userID, final int err, final String errMsg) {

    }

    /**
     * 断开跨房连麦结果回调
     *
     * @param err
     * @param errMsg
     */
    @Override
    public void onDisConnectOtherRoom(final int err, final String errMsg) {

    }

    /**
     * 网络行质量回调
     * <p>
     * 您可以用来在 UI 上显示当前用户的网络质量，提高用户体验
     *
     * @param localQuality  上行网络质量
     * @param remoteQuality 下行网络质量
     */
    @Override
    public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {

    }
  };
}
