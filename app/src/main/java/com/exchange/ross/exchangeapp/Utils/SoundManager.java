package com.exchange.ross.exchangeapp.Utils;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by ross on 5/1/15.
 */
@SuppressWarnings("deprecation")
public class SoundManager {

    private static SoundManager instance;
    public static synchronized SoundManager sharedManager() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void mute(Boolean on) {
        Context context = ApplicationContextProvider.getContext();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (context != null) {
            if (on) {
                if(Settings.getVibration()) {
                    audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                            AudioManager.VIBRATE_SETTING_ON);
                }
                else {
                    audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                            AudioManager.VIBRATE_SETTING_OFF);
                }
                //turn ringer silent
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                //turn off sound, disable notifications
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

                //notifications
                audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);

                //alarm
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);

                //ringer
                audioManager.setStreamMute(AudioManager.STREAM_RING, true);

                //media
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
            else {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                        AudioManager.VIBRATE_SETTING_ON);
                //turn ringer silent
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

                // turn on sound, enable notifications
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

                //notifications
                audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);

                //alarm
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);

                //ringer
                audioManager.setStreamMute(AudioManager.STREAM_RING, false);

                //media
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
        }
    }
}
