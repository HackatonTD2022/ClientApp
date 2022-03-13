import javax.microedition.io.StreamConnection;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class ProcessConnectionThread implements Runnable {

    private class User implements Serializable {
        private final String UserName;
        private final String UUID;

        public User(String userName, String uuid) {
            UserName = userName;
            UUID = uuid;
        }

        public String getUserName() {
            return UserName;
        }

        public String getUUID() {
            return UUID;
        }
    }

    private ArrayList<User> userList;

    private StreamConnection streamConnection;

    private AESSecurityCap securityCap = null;

    private final int bufferSize = 2048;
    private byte[] buffer;
    private boolean running = false;

    private InputStream input = null;
    private OutputStream output = null;

    public ProcessConnectionThread(StreamConnection connection) {
        streamConnection = connection;
        running = true;

        securityCap = new AESSecurityCap();
        deserializeUserList();

        try {
            input = streamConnection.openInputStream();
            output = streamConnection.openOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean strEq(String as, String bs) {
        char[] a = as.toCharArray();
        char[] b = bs.toCharArray();
        boolean eq = true;
        for(int i = 0; i < b.length; i++) {
            if(a[i] != b[i]) {
                eq = false;
                break;
            }
        }
        return eq;
    }

    private void deserializeUserList() {
        // deserialize user list.
        try {
            FileInputStream fileIn = new FileInputStream("users.db");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            userList = (ArrayList<User>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            //e.printStackTrace();
        }

        if(Objects.isNull(userList))
            userList = new ArrayList<>();
    }

    private void serializeUserList() {
        // serialize user list
        try {
            FileOutputStream fileOut = new FileOutputStream("users.db");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(userList);
            out.close();
            fileOut.close();

        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private User getUserFromMessage(String Msg) {
        String tmp = Msg.substring(Msg.indexOf(' ') + 1);
        int index = tmp.indexOf(' ');
        String username = tmp.substring(0, index);
        tmp = tmp.substring(index + 1);
        String uuid = tmp.substring(tmp.indexOf(' ') + 1);
        uuid = uuid.substring(0, uuid.lastIndexOf(' '));

        return new User(username, uuid);
    }

    @Override
    public void run() {
        System.out.println("bluetooth connected");

        if(Objects.nonNull(input)) {
            while (running) {
                String msg = recieveFromServer();

                if(Objects.isNull(msg))
                    continue;

                if(strEq(msg, "close")) {
                    running = false;
                    System.out.println("Close socket");
                    serializeUserList();
                }
                else if(strEq(msg, "login")) {

                    User user = getUserFromMessage(msg);
                    boolean flag = false;

                    for(User u : userList) {
                        if(u.UserName.equals(user.UserName) && u.UUID.equals(user.UUID)) {
                            System.out.println("Login success");
                            // login by bash script
                            runLoginBash(user);
                            flag = true;
                            break;
                        }
                    }

                    if(flag)
                        sendToServer("ok");
                    else
                        sendToServer("fail");

                }
                else if(strEq(msg, "create_user")) {

                    User user = getUserFromMessage(msg);
                    boolean found = false;

                    for(User u : userList) {
                        if(u.UserName.equals(user.UserName) && u.UUID.equals(user.UUID)) {
                            found = true;
                            break;
                        }
                    }

                    if(!found) {
                        userList.add(user);
                        sendToServer("ok");
                        System.out.println("Registered new user " + user.UserName);
                        // register by bash script
                        runRegisterBash(user);
                    } else {
                        sendToServer("fail");
                    }
                }

            }
        }
    }

    private void runLoginBash(User user) {
        try {
            String[] cmd = new String[]{"/bin/sh", "/usr/bin/xinit_user", user.UserName};
            Process pr = Runtime.getRuntime().exec(cmd);
            //Process p = new ProcessBuilder("/bin/sh /usr/bin/xinit_user", user.UserName).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runRegisterBash(User user) {
        //StringBuilder sb = new StringBuilder();
        //sb.append(user.UserName).append(' ').append(user.UUID);


        try {
            String[] cmd = new String[]{"/bin/sh", "/usr/bin/create_user", user.UserName, user.UUID};
            Process pr = Runtime.getRuntime().exec(cmd);
            //Process p = new ProcessBuilder("/bin/sh /usr/bin/create_user", sb.toString()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToServer(String Message) {
        try {
            output.write(Message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String recieveFromServer() {
        byte[] buffer = new byte[bufferSize];
        String msg = null;
        try {
            int r = input.read(buffer);
            if(r > 0)
                msg = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }
}
