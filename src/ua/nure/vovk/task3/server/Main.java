package ua.nure.vovk.task3.server;

import java.io.IOException;

public class Main {

    private static Server server;
    public static void main(String[] args) throws IOException {
        server = new Server(4444);
        server.start();
    }
}
