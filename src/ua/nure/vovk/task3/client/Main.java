package ua.nure.vovk.task3.client;

import java.util.Scanner;

public class Main {
    private static final String HOST = "localhost";
    private static final int PORT = 4444;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Client client = new Client(HOST, PORT);
        client.connect();

        while (client.isConnectionAlive()){
            System.out.print("Enter login: ");
            String login = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            if (client.authorize(login, password)) {
                break;
            }
        }

        client.work();
    }
}
