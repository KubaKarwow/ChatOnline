/**
 * @author Karwowski Jakub S27780
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChatServer extends Thread {
    private Map<SocketChannel, String> userMaps;
    private Map<String, StringBuilder> logs;
    private StringBuilder serverLog;
    private String host;
    private int port;

    private ServerSocketChannel channel;
    private Selector selector;
    private volatile boolean isRunning = true;

    ChatServer(String host, int port) {
        userMaps = new HashMap<>();
        logs = new HashMap<>();
        serverLog = new StringBuilder();
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        ChatServer localhost = new ChatServer("localhost", 9999);
        localhost.startServer();
    }
    public void startServer() throws IOException {
        try {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(host, port));

            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_ACCEPT);

            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopServer() {
        isRunning = false;
        try {
            channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            selector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {
        try {
            handleConnections();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                selector.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void handleConnections() throws IOException {

        while (isRunning) {
            selector.select();
            if (!selector.isOpen()) {
                return;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    SocketChannel accept = channel.accept();
                    accept.configureBlocking(false);
                    accept.register(selector, SelectionKey.OP_READ);
                    continue;
                }

                if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.configureBlocking(false);
                    handleRequest(socketChannel);

                }
            }
        }
    }

    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

    public void handleRequest(SocketChannel socketChannel) throws IOException {
        if (!socketChannel.isOpen()) {
            return;
        }
        String request = readRequest(socketChannel);
        System.out.println(socketChannel.getRemoteAddress());
        if(request.startsWith("login")){
            login(request.split(" ")[1],socketChannel);
            writeLoginResponse(socketChannel);
        } else if(request.equals("logout")){
            logout(socketChannel);
            writeLogoutResponse(socketChannel);
            socketChannel.close();
        } else{
            writeRequestResponse(socketChannel,request);
        }

    }
    private void writeLogoutResponse(SocketChannel socketChannel) throws IOException {
        String userId = userMaps.get(socketChannel);
        String response = userId + " logged out$";
        ByteBuffer encode = Charset.defaultCharset().encode(response);
        while(encode.hasRemaining()){
            socketChannel.write(encode);
        }
    }
    private void writeLoginResponse(SocketChannel socketChannel) throws IOException {
        String userId = userMaps.get(socketChannel);
        String response = userId + " logged in$";
        ByteBuffer encode = Charset.defaultCharset().encode(response);
        while(encode.hasRemaining()){
            socketChannel.write(encode);
        }
    }
    private void logout(SocketChannel socketChannel){
        String userID = userMaps.get(socketChannel);
        serverLog.append(userID+" logged out\n");

    }
    private void login(String id, SocketChannel socketChannel){
        serverLog.append(id+" logged in\n");
        userMaps.put(socketChannel,id);
    }
    private void putNewUserInDB(String id, SocketChannel socketChannel) {
        userMaps.put(socketChannel, id);
        logs.put(id, new StringBuilder());
    }
    private String readRequest(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        boolean stillReading = true;
        StringBuilder resultBuilder = new StringBuilder();
        while (stillReading) {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == 0) {
                continue;
            }
            if (bytesRead == -1) {
                break;
            }
            buffer.flip();
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
            while(charBuffer.hasRemaining()){
                char currentChar = charBuffer.get();
                if(currentChar=='$'){
                    stillReading=false;
                    break;
                }
                resultBuilder.append(currentChar);
            }
        }
        System.out.println(resultBuilder.toString());
        return handleDollarSigns(resultBuilder.toString());

    }
    private String handleDollarSigns(String request){
        //
        return request.replaceAll("%20", "$");

    }
    public void writeRequestResponse(SocketChannel socketChannel, String request) throws IOException {
        String userID = userMaps.get(socketChannel);

        String response= userID+": "+request+", mówię ja, "+userID+"$";
        ByteBuffer encode = Charset.defaultCharset().encode(response);
        while(encode.hasRemaining()){
            socketChannel.write(encode);
        }
    }



//    private void updatePersonalLog(String request, String id) {
//        if (request.charAt(0) == 'l') {
//            logs.put(id, new StringBuilder());
//            logs.get(id).append("=== ").append(id).append(" log start ===\nlogged in\n");
//        } else if (request.charAt(0) == 'b') {
//            logs.get(id).append("logged out\n=== ").append(id).append(" log end ===\n");
//        } else {
//            logs.get(id).append("Request: ").append(request).append("\nResult:\n");
//            String[] dates = request.split(" ");
//            logs.get(id).append(Time.passed(strip(dates[0]), strip(dates[1]))).append('\n');
//        }
//    }

//    private void updateGeneralLog(String request, String id) {
//        LocalTime now = LocalTime.now();
//        String hourMinuteSecond = now.toString().substring(0, 9);
//        String nanoSeconds = (now.getNano() + "").substring(0, 3);
//        String time = hourMinuteSecond + nanoSeconds;
//        if (request.charAt(0) == 'l') {
//            serverLog.append(id).append(" logged in at ").append(time).append('\n');
//        } else if (request.charAt(0) == 'b') {
//            serverLog.append(id).append(" logged out at ").append(time).append('\n');
//
//        } else {
//            String s = request.replaceAll("\r\n", "");
//            serverLog.append(id).append(" request at ").
//                    append(time).append(": \"").append(s).append("\"").append("\n");
//        }
//    }

//    private String login(String id) {
//        updatePersonalLog("login", id);
//        updateGeneralLog("login", id);
//        return "logged in";
//    }



    private String getIdByChannel(SocketChannel socketChannel) {
        return userMaps.get(socketChannel);
    }

    private String strip(String string) {
        return string.replaceAll("\\s", "");
    }

    public Map<SocketChannel, String> getUserMaps() {
        return userMaps;
    }

    public void setUserMaps(Map<SocketChannel, String> userMaps) {
        this.userMaps = userMaps;
    }

    public Map<String, StringBuilder> getLogs() {
        return logs;
    }

    public void setLogs(Map<String, StringBuilder> logs) {
        this.logs = logs;
    }

    public StringBuilder getServerLog() {
        return serverLog;
    }

    public void setServerLog(StringBuilder serverLog) {
        this.serverLog = serverLog;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
