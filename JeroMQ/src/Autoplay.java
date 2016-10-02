import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.TouchPressType;
import com.android.monkeyrunner.MonkeyDevice;
import com.android.monkeyrunner.MonkeyRunner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Created by kyujin on 01/10/2016.
 */

class Note {
    int id;
    int position;
    long second;

    public Note(int id, int position, long second) {
        this.id = id;
        this.position = position;
        this.second = second;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getSecond() {
        return second;
    }

    public void setSecond(long second) {
        this.second = second;
    }
}


public class Autoplay {
    private ArrayList<Note> notes;

    public static void main(String[] args) {
        new Autoplay().play();
    }

    private void play() {
        notes = new ArrayList<>();
        System.out.print("ABS Filepath: ");
        String filepath = new Scanner(System.in).nextLine();
        readFile(filepath.equals("a") ? "/Users/kyujin/deresute-autoplay/309_4" : filepath);
        for(Note n : notes)
            System.out.printf("%3d | %d | %6d\n", n.getId(), n.getPosition(), n.getSecond());
        System.out.println("Waiting for device...");
        AdbBackend adb = new AdbBackend();
        IChimpDevice device = adb.waitForConnection();

        System.out.println("Device connected");
        JMQServer jmqServer = new JMQServer();
        ArrayList<String> times = jmqServer.waitForMessage();
        long timeDiff = Long.parseLong(times.get(1)) - Long.parseLong(times.get(0));
        double velocity = timeDiff / 250.0d;
        double length = Integer.parseInt(times.get(2)) / 2;
        double time = length * velocity;
        try {
            long timeGap = System.currentTimeMillis() - Long.parseLong(times.get(3));
            Thread.sleep((long) time - timeGap);
            long previousTime = notes.get(0).getSecond();
            for(Note note : notes) {
                device.touch(240, 500 + 400 * (note.getPosition() - 1), TouchPressType.DOWN_AND_UP);
                System.out.println("Sleeping: " + (note.getSecond() - previousTime));
                Thread.sleep(note.getSecond() - previousTime);
                previousTime = note.getSecond();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void readFile(String filePath) {
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader bfr = new BufferedReader(fr);

            String read;
            String out = "";

            while ((read = bfr.readLine()) != null) {
                out += read;
            }
            notes.clear();
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(out);
            int timeGap = 0;
            for (Object aJsonArray : jsonArray) {
                JSONObject note = (JSONObject) aJsonArray;
                if ((long) note.get("type") == 1) {
                    if(timeGap >= 100) {
                        notes.add(new Note((int) (long) note.get("id"), (int) (long) note.get("finishPos"), (long) (((Number) note.get("sec")).doubleValue() * 1000) + 1L));
                        timeGap -= 100L;
                    } else {

                        notes.add(new Note((int) (long)note.get("id"), (int) (long) note.get("finishPos"), (long) ((Number) note.get("sec")).doubleValue() * 1000));
                    }
                    timeGap += (long) (((Number) note.get("sec")).doubleValue() * 100000) - ((long) (((Number) note.get("sec")).doubleValue() * 1000)) * 100;
                }
            }
        }catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
