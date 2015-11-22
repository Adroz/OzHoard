package com.workshoporange.android.ozhoard.data;

/**
 * Created by Nik on 22/11/2015.
 */
public class DealItem {

    private final String title;
    private final String link;

    public DealItem(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }
}