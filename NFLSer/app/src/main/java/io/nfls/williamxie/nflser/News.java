package io.nfls.williamxie.nflser;

import android.graphics.Bitmap;

public class News {
    private String time;
    private String title;
    private String type;
    private String detail;
    private String conf;
    private String imageUrl;
    private Bitmap image = null;

    public News(String time, String title, String type, String detail, String conf, String imageUrl) {
        this.time = time;
        this.title = title;
        this.type = type;
        this.detail = detail;
        this.conf = conf;
        this.imageUrl = imageUrl;
    }

    public News(String time, String title, String type, String detail, String conf, String imageUrl, Bitmap image) {
        this(time, title, type, detail, conf, imageUrl);
        this.image = image;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getDetail() {
        return detail;
    }

    public String getConf() {
        return conf;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        News news = (News) o;

        if (!time.equals(news.time)) return false;
        if (!title.equals(news.title)) return false;
        return conf.equals(news.conf);
    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + conf.hashCode();
        return result;
    }
}