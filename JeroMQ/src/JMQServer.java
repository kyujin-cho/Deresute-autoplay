import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

/**
 * Created by kyujin on 29/09/2016.
 */
class JMQServer {
    ZMQ.Context context;
    ZMQ.Socket socket;
    JMQServer() {
        context = ZMQ.context(1);
        socket = context.socket(ZMQ.REP);
        socket.connect("tcp://192.168.2.133:5599");
    }

    ArrayList<String> waitForMessage() {
        ArrayList<String> message = new ArrayList<>();
        System.out.println("Socket open. waiting for message...");
        while(true){
            String msg = socket.recvStr();
            if(!msg.equals("")) {
                System.out.println("recv : " + System.currentTimeMillis());
                if (msg.contains("%")) {
                    System.out.println("Time:\t\t" + msg.split("%")[0]);
                    System.out.println("Message:\t\t" + msg.split("%")[1]);
                    message.add(msg.split("%")[0]);
                    if(message.size() == 2) {
                        message.add(msg.split("%")[2]);
                        message.add(Long.toString(System.currentTimeMillis()));
                        return message;
                    }
                }
                socket.send("Handshake!");
            } else {
                System.out.println("Blank MSG");
            }
        }
    }
}
