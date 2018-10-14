package com.zia.widget.bean;

/**
 * Created By zia on 2018/10/13.
 */
public class Config {
    public String key;
    public int version;
    public String url;
    public String able;
    public String message;
    public String other;

    public Config(String key, int version, String url, String able, String message, String other) {
        this.key = key;
        this.version = version;
        this.url = url;
        this.able = able;
        this.message = message;
        this.other = other;
    }

    @Override
    public String toString() {
        return "Config{" +
                "key=" + key +
                ", version=" + version +
                ", url='" + url + '\'' +
                ", able=" + able +
                ", message='" + message + '\'' +
                ", other='" + other + '\'' +
                '}';
    }
}
