package ua.nure.vovk.task3.chat;

public class Main {

    public static final String HOST = "localhost";
    public static final int PORT = 4444;

    public static void main(String[] args) {

        Chat chat = new Chat(HOST, PORT);
        if (!chat.connect()) {
            return;
        }
        chat.work(System.out);
    }
}
