package com.example.muuusicapp;

import java.io.Serializable;

public class AudioData implements Serializable {
    private String title;
    private String path;
    private String duration;

    public AudioData(String title, String path, String duration) {
        this.title = title;
        this.path = path;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
