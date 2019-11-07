import { NativeModules , NativeEventEmitter , requireNativeComponent } from "react-native";
import React from "react";
const RNModule = NativeModules.RNTRTCModule;
var RNRTCView = requireNativeComponent("RNTRTCView");

type JoinChannelType = {
  sdkAppId:Number,
  userId:String,
  userSig:String,
  roomId:Number,
  role:Number,
  privateMapKey?:String
  businessInfo?:String
}
enum Scene  {
  VideoCall,
  Live,
}

class RTCVIew extends React.Component {
  constructor(props){
    super(props)
  }
  render(){
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
    const eventEmitter = new NativeEventEmitter(RNModule);
    return eventEmitter.addListener(eventName, handler)
  }
};


export {
  RTCVIew
}