package com.xiaoming.rn.trtc;

import androidx.annotation.Nullable;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

public class TRTCViewManager extends SimpleViewManager<TRTCView> {
    public static final String REACT_CLASS = "RNTRTCView";

    public enum Events {
        EVENT_ON_TRACK("onTrack"),
        EVENT_ON_FACE_DETECT("onFaceDetect");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    @Override
    @Nullable
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        for (Events event : Events.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
        }
        return builder.build();
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public TRTCView createViewInstance(ThemedReactContext context) {
        return new TRTCView(context);
    }

    @Override
    public void onDropViewInstance(TRTCView view) {
        view.stop();
        super.onDropViewInstance(view);
    }

    @ReactProp(name = "userId")
    public void setUserId(TRTCView view, String value) {
        view.setUserId(value);
    }


}
