package ru.netology;

public interface TCPConnectionListener {

    void onConnectionReady(TCPConnection tcpConnection);
    void onReceiveString (TCPConnection tcpConnection,String value);
    void onDisconnect (TCPConnection tcpConnection );
    void onException (TCPConnection tcpConnection, Exception ex);
}
