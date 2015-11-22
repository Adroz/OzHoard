package com.workshoporange.android.ozhoard.Deal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing deal content to deals list.
 *
 * @author Nicholas Moores, for Workshop Orange.
 */
public class DealContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Deal> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Deal> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyDeal(i));
        }
    }

    private static void addItem(Deal item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Deal createDummyDeal(int position) {
        return new Deal(String.valueOf(position), "Item " + position, "asdasdsad", makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Deal {
        public final String id;
        public final String title;
        public final String link;
        public final String details;

        public Deal(String id, String title, String link, String details) {
            this.id = id;
            this.title = title;
            this.link = link;
            this.details = details;
        }

        @Override
        public String toString() {
            return details;
        }
    }
}
