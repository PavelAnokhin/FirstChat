package ru.netology;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    // конструктор для создания соккета внутри класса
    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException{
        this(eventListener,new Socket(ipAddr,port));

    }

    //конструктор рассчитан на создание кем либо соккета из вне, снаружи
    public TCPConnection(final TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while(!rxThread.isInterrupted()){
                        //String msg = in.readLine();
                        eventListener.onReceiveString(TCPConnection.this,in.readLine());
                    }
                    String msg = in.readLine();
                }catch(IOException exception){
                    eventListener.onException(TCPConnection.this, exception);

                }finally {
                    eventListener.onDisconnect(TCPConnection.this);

                }
            }
        });
        rxThread.start();

    }

    public synchronized void sendString(String value){
        try{
            out.write(value + "\r\n");//символы возрата корретки и возврата строки необходимы чтобы на конце клиента мы понимали где заканчивается строка
            out.flush();//команда сбрасывает все буферы
        }catch (IOException ex){
            eventListener.onException(TCPConnection.this,ex);
            disconnect();
        }

    }

    public synchronized void disconnect(){
        rxThread.interrupt();
        try {
            socket.close();
        }catch (IOException ex){
            eventListener.onException(TCPConnection.this,ex);
        }

    }

    @Override
    public String toString() {
        return "TCPConnection:  " +socket.getInetAddress() + ": " + socket.getPort();

    }
}
