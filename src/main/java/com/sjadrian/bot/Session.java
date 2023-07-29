package com.sjadrian.bot;

public class Session {

    private final long id1;
    private final long id2;

    public Session(long id1, long id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public Long getPartner(Long id) {
        if (id1 == id) {
            return id2;
        }
        if (id2 == id) {
            return id1;
        }
        return null;
    }
}
