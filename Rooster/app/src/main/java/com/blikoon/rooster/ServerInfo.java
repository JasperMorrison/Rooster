package com.blikoon.rooster;

public class ServerInfo {
    String ip = "10.78.137.7";
    String hostName = "localhost";
    String hostDomain = "localhost";

    public ServerInfo(){}
    public ServerInfo(String ip, String domain){
        this.ip = ip;
        this.hostDomain = domain;
    }

    public final static ServerInfo DEFALUT_SERVER = new ServerInfo();
}
