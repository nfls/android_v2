package io.nfls.williamxie.nflser;

import java.text.DecimalFormat;

public class ResourceFile {

    public static String[] units = {"B", "KB", "MB", "GB", "TB"};
    public static DecimalFormat df = new DecimalFormat("#.0");

    private String name;
    private long date;
    private long size;
    private String href;
    private String appHref;
    private boolean isFolder;
    private boolean downloaded;

    public ResourceFile(String name, long date, long size, String href, String appHref, boolean isFolder, boolean downloaded) {
        this.name = name;
        this.date = date;
        this.size = size;
        this.href = href;
        this.appHref = appHref;
        this.isFolder = isFolder;
        this.downloaded = downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getName() {
        return name;
    }

    public long getDate() {
        return date;
    }

    public long getSize() {
        return size;
    }

    public String getSizeWithUnit() {
        String str = null;

        double size = this.size;
        int scale = 0;

        while (size >= 1024) {
            size = size / 1024;
            scale ++;
            if (scale == 4) break;
        }

        str = df.format(size) + " " + units[scale];

        return str;
    }

    public String getHref() {
        return href;
    }

    public String getAppHref() {
        return appHref;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public boolean isDownloaded() {
        return downloaded;
    }
}
