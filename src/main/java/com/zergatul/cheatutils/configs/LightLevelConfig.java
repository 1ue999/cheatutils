package com.zergatul.cheatutils.configs;

public class LightLevelConfig {

    public boolean enabled;
    public boolean display;
    public boolean showTracers;
    public boolean showLightLevelValue;
    public float maxDistance;

    public LightLevelConfig() {
        enabled = false;
        display = false;
        showTracers = true;
        maxDistance = 20;
    }
}