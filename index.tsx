import { NativeModules , NativeEventEmitter , requireNativeComponent } from "react-native";
import React from "react";

const RNModule = NativeModules.RNTRTCModule;
var RNRTCView = requireNativeComponent("RNTRTCView");
const eventEmitter = new NativeEventEmitter(RNModule);

type JoinChannelType = {
  sdkAppId:Number,
  userId:String,
  userSig:String,
  roomId:Number,
  role:Number,
  privateMapKey?:String
  businessInfo?:String
  streamId?:String
  userDefineRecordId?:String
}

enum Scene  {
  VideoCall,
  Live,
}

class RTCView extends React.Component {
  constructor(props){
    super(props)
  }
  render(){
    console.log("RTCView render",this.props)
    return <RNRTCView {...this.props} />;
  }
}
export default {
  initEngine():Promise<Boolean> {
    return RNModule.initEngine()
  },

  joinChannel(data:JoinChannelType,scene:Scene):Promise<Boolean> {
    data.privateMapKey = data.privateMapKey || "";
    data.businessInfo = data.businessInfo || "";
    data.streamId = data.streamId || "";
    data.userDefineRecordId = data.userDefineRecordId || "";

    return RNModule.joinChannel(data,scene)
  },
  leaveChannel():Promise<Boolean> {
    return RNModule.leaveChannel()
  },
  startLocalPreview():void{
    RNModule.startLocalPreview()
  },
  stopLocalPreview():void{
    RNModule.stopLocalPreview()
  },
  startLocalAudio():void{
    RNModule.startLocalAudio()
  },
  stopLocalAudio():void{
    RNModule.stopLocalAudio()
  },
  muteLocalAudio(mute:Boolean):void{
    RNModule.muteLocalAudio(mute)
  },
  muteRemoteAudio(userId:String,mute:Boolean):void{
    RNModule.muteRemoteAudio(userId,mute)
  },
  muteAllRemoteAudio(mute:Boolean):void{
    RNModule.muteAllRemoteAudio(mute)
  },
  enableAudioVolumeEvaluation(intervalMs:Number):void{
    RNModule.enableAudioVolumeEvaluation(intervalMs)
  },
  startAudioRecording(filePath:String):Promise<Number>{
    return RNModule.startAudioRecording(filePath)
  },
  stopAudioRecording():void{
    RNModule.stopAudioRecording()
  },
  startScreenRecord():void{
    RNModule.startScreenRecord()
  },
  stopScreenRecord():void{
    RNModule.stopScreenRecord()
  },
  /*
   * setAudioRoute
   * int  0 Speaker 1 Earpiece
   */
  setAudioRoute(router:Number):void{
    RNModule.setAudioRoute(router)
  },

  addListener(eventName,handler) {
    if(!eventName||!handler)return;
    return eventEmitter.addListener(eventName, handler)
  },
  removeListener(eventName,handler){
    if(!eventName)return;
    if(!handler){
      eventEmitter.removeAllListeners(eventName)
      return
    }
    eventEmitter.removeListener(eventName,handler)
  }
};


export {
  RTCView
}
