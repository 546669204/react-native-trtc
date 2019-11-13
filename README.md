# react-native-trtc
react-native-trtc 腾讯 trtc 的react-native 封装

基于最新版腾讯云TRTC SDK

目前只支持了 Android SDK 

后续可能会添加 ios 等版本

支持SDK的基本功能   
1. 视频聊天   
2. 语音聊天  
3. 屏幕共享  


# 安装  

```
yarn add react-native-trtc@git // 闲暇时间上传npm  现在先使用 git库安装吧 -。-
react-native link react-native-trtc
```


# 使用

    
//初始化引擎  
initEngine

//进入房间  
joinChannel

// 开关本地摄像头采集   
startLocalPreview  
stopLocalPreview


使用RTCView 渲染
userId 有值时 渲染远端 数据流
 为空时 渲染本地摄像头
 
 
有问题 issue


# 常见问题  

1. > More than one file was found with OS independent path 'lib/arm64-v8a/libc++_shared.so'  
```
packagingOptions {
    pickFirst 'lib/*/libc++_shared.so'
}
```
