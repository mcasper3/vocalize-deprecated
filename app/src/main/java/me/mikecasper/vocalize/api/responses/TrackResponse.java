package me.mikecasper.vocalize.api.responses;

import java.util.List;

public class TrackResponse {
    private List<TrackResponseItem> items;
    private String next;
    private int offset;
    private int total;
    private String previous;

    public TrackResponse(List<TrackResponseItem> items, String next, int offset, int total, String previous) {
        this.items = items;
        this.next = next;
        this.offset = offset;
        this.total = total;
        this.previous = previous;
    }

    public List<TrackResponseItem> getItems() {
        return items;
    }

    public String getNext() {
        return next;
    }

    public int getOffset() {
        return offset;
    }

    public int getTotal() {
        return total;
    }

    public String getPrevious() {
        return previous;
    }
}
