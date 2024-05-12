package zad1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChatReader extends Thread{
       private SocketChannel socketChannel;
    private List<String> log;
    private String userID;


    public ChatReader(SocketChannel socketChannel,String userID) {
        this.socketChannel = socketChannel;
        this.userID=userID;
        log= new ArrayList<>();

    }

    @Override
    public void run() {
        log.add("=== " + userID + " chat view");
        while (true){
            List<String> messages;
            try {
                messages = readResponse();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
                log.addAll(messages);
                if(log.get(log.size()-1).equals(userID+ " logged out")){
                    break;
                }
        }
    }
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);

    List<String> readResponse() throws IOException {
        List<String> result = new ArrayList<>();
        StringBuilder responseBuilder = new StringBuilder();
        boolean stillReading = true;
        while (stillReading) {
            readBuffer.clear();
            int read = socketChannel.read(readBuffer);
            if (read == 0) {
                continue;
            } else if (read == -1) {
                break;
            } else {
                readBuffer.flip();
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(readBuffer);
                while (charBuffer.hasRemaining()) {
                    char currentChar = charBuffer.get();
                    if (currentChar == '$') {
                        result.add(responseBuilder.toString());
                        if(responseBuilder.toString().equals(userID+ " logged out")){
                            stillReading = false;
                            break;
                        }
                        responseBuilder=new StringBuilder();
                    } else{
                        responseBuilder.append(currentChar);
                    }
                }
                stillReading=false;
            }
        }

        return result;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
