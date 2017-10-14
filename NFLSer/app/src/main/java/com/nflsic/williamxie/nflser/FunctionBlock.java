package com.nflsic.williamxie.nflser;

import android.widget.ImageView;

public class FunctionBlock {

    private int icon;
    private int name;
    private int description;
    private int activity;

    public FunctionBlock(int name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public FunctionBlock(int name, int icon, int description) {
        this(name, icon);
        this.description = description;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public int getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public int getDescription() {
        return description;
    }

    public int getActivity() {
        return activity;
    }

}
