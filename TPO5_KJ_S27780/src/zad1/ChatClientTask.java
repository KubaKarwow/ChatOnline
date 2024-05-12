/**
 *
 *  @author Karwowski Jakub S27780
 *
 */

package zad1;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<String> {
    private  ChatClient client;
    private List<String> messages;
    private int wait;
    public ChatClientTask(ChatClient c, List<String> msgs, int wait) {
        super(() -> handleClient(c,msgs,wait));
        this.client=c;
        this.messages=msgs;
        this.wait=wait;
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(c,msgs,wait);
    }

    public static String handleClient(ChatClient client, List<String> requests, int wait) throws InterruptedException, IOException {
        client.connect();

        client.send("login " + client.getId());
        if(wait!=0){
            Thread.sleep(wait);
        }
        for (String request : requests) {
            client.send(request);
            if(wait!=0){
                Thread.sleep(wait);
            }
        }
        client.send("logout");
        if(wait!=0){
            Thread.sleep(wait);
        }
        // komentarz
        // logged out nie koniecznie musi byc jako ostatnia komenda ktora przeczytalismy
        // ale wydaje mi sie ze powinna byc
        // w kazdym razie taki for wypierdala sie bo concurrentMOdificationException
        boolean loggedOut=false;
        while(!loggedOut){
            List<String> log = client.getChatReader().getLog();
            if(log.size()!=0){
                for (String chatEntry :log) {
                    if(chatEntry.equals(client.getId() + " logged out")){
                        loggedOut=true;
                        break;
                    }
                }
            }
        }
        return client.getChatView();
    }

    public ChatClient getClient() {
        return client;
    }

    public void setClient(ChatClient client) {
        this.client = client;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }
}
