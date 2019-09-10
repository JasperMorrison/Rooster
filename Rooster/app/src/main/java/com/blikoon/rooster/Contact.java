package com.blikoon.rooster;

/**
 * Created by gakwaya on 4/16/2016.
 */
public class Contact {
    private String jid;
    private String name;
    private String domain;

    public Contact(String name, String domain)
    {
        this.name = name;
        this.domain = domain;
    }

    public String getJid()
    {
        return name + "@" + domain;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getName() {
        return name;
    }
}
