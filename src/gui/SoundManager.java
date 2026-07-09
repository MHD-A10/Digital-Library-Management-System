package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static boolean enabled = true;
    private static float volume = 0.7f;
    private static final String[] SOUND_NAMES = {
            "back", "click", "delete", "error", "exit", "navigate", "success", "warning"
    };
    private static final Map<String, Clip> clips = new HashMap<>();
    private static boolean preloaded = false;

    public static synchronized void setEnabled(boolean v) {
        enabled = v;
    }

    public static synchronized void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        for (Clip clip : clips.values()) {
            applyVolume(clip);
        }
    }

    public static synchronized void preload() {
        if (preloaded) {
            return;
        }

        for (String name : SOUND_NAMES) {
            loadClip(name);
        }

        preloaded = true;
    }

    public static void play(String name) {
        Clip clip;
        synchronized (SoundManager.class) {
            if (!enabled || name == null || name.trim().isEmpty()) {
                return;
            }

            preload();
            clip = clips.get(name);
            if (clip == null) {
                return;
            }

            if (clip.isRunning()) {
                clip.stop();
            }

            clip.setFramePosition(0);
            applyVolume(clip);
            clip.start();
        }
    }

    private static void loadClip(String name) {
        if (clips.containsKey(name)) {
            return;
        }

        try {
            java.net.URL url = SoundManager.class.getResource("/sounds/" + name + ".wav");
            if (url == null) {
                return;
            }

            try (AudioInputStream audio = AudioSystem.getAudioInputStream(url)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                applyVolume(clip);
                clips.put(name, clip);
            }
        } catch (Exception ignored) {
            
        }
    }

    private static void applyVolume(Clip clip) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float min = gain.getMinimum();
        float max = gain.getMaximum();
        float target;

        if (volume <= 0f) {
            target = min;
        } else {
            target = (float) (20.0 * Math.log10(volume));
            target = Math.max(min, Math.min(max, target));
        }

        gain.setValue(target);
    }
}