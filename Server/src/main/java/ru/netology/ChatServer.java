package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();

    }
    // так как кол-во соединений неизвестно, необходимо создать список соединений
    private final ArrayList<TCPConnection> connections = new ArrayList<>();




    private ChatServer(){
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(22)){
            while (true){
                try{
                    new TCPConnection(this,serverSocket.accept());//объект соккета при входящем соединении возвращает метод accept, который ждет нового соединения
                }catch(IOException e){
                    System.out.println("TCPConnection exception :" + e);
                }
            }
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
//так как потоков неизвестно сколько и методов ниже, соответсвенно, необходимо их синхронизировать для того чтобы из разных потоков нельзя было в них попасть одновременно
    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        //создаем логи - если соединение готово, оповещаем об этом всех клиентов:
        sendToAllConnections("Client connected: " + tcpConnection);


    }
//самое важное - работа с принятой строкой. Если мы примнаием строку, мы ее передаем всем клиентам:
    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
       sendToAllConnections(value);
    }
//удаление коннекшн из списка соединений в случае обрыва:
    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
       connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception ex) {
        System.out.println("TCPConnection exception:  " + ex);
    }

     // общий метод для рассылки сообщений всем
     private void sendToAllConnections(String  value){
         System.out.println(value);
         final int cnt = connections.size();
         for(int i = 0;i<cnt;i++)connections.get(i).sendString(value);

     }
}
