import java.io.*;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;



public class Client {
    static String networkAddress, command;
    static int port, numRequests;
    static long totalTime, turnTime;
    static double avgTime;
    static Scanner scn;

    static ClientRequest[] threads;
    public static void main(String[] args) {
        //create scanner and get network address and port from user
        scn = new Scanner(System.in);
        System.out.println("Please Enter Network Address");
        networkAddress = scn.next();
        System.out.println("Please Enter Port");
        port = scn.nextInt();

        //set command to "not exit" to allow while loop to begin
        command = "not exit";
        //while loop displays menu to user and creates server request from user input
        while(!command.equals("exit")) {
            displayUserMenu();
            //if command = exit, send to createRequest and break loop
            if (command.equals("exit")){
                createRequests();
                break;
            }
            //make output easier to read
            System.out.println();
            createRequests();
        }
        //close scanner
        scn.close();


    }

    private static void displayUserMenu() {
        //display menu to user, linux virtual machine did not like text as a text block
        System.out.println("Please Enter Number Corresponding to Desired Command\n" +
                            "1 - Date and Time \n" +
                            "2 - Uptime \n" +
                            "3 - Memory Use \n" +
                            "4 - Netstat \n" +
                            "5 - Current Users \n" +
                            "6 - Running Process \n" +
                            "7 - Exit Program");
        //get user input
        String commandNum = scn.next();
        //based on user input set command variable, the linux virtual machine did not work with enhanced switch
        switch (commandNum) {
            case "1":
                command = "date";
                break;
            case "2":
                command = "uptime";
                break;
            case "3":
                command = "free";
                break;
            case "4":
                command = "netstat";
                break;
            case "5":
                command = "who";
                break;
            case "6":
                command = "ps";
                break;
            case "7":
                command = "exit";
                break;
            //if user input is invalid. inform user and redisplay menu
            default:
                System.out.println("\nInvalid Input");
                displayUserMenu();
                return;

        }
        //if command equals exit, return function, number of requests not needed
        if (command.equals("exit")){
            numRequests = 1;
            return;
        }

        getNumRequests();
    }

    private static void getNumRequests() {
        System.out.println("Please Enter Number of Request\n" +
                "Options: 1, 5, 10, 15, 20, 25");
        //get number of requests from user
        try {
            numRequests = scn.nextInt();
            if (!((numRequests == 1) || (numRequests == 5) || (numRequests == 10) || (numRequests == 15) || (numRequests == 20)
                    || (numRequests == 25))) {
                System.out.println("\nInvalid Input for Number of Requests");
                getNumRequests();
            }
        }
        catch (InputMismatchException e){

            System.out.println("Please Enter an Integer");
            scn.nextLine();
            getNumRequests();

        }
    }

    private static void createRequests() {
        //initialize array of ClientRequests(threads) of user specified size
        threads = new ClientRequest[numRequests];

        //loop for number of user requests
        for (int i = 0; i < numRequests; i++) {
            try{
                //create socket
                Socket socket = new Socket(networkAddress, port);
                //put thread into array
                threads[i] = new ClientRequest(socket, i);
                //call run method in ClientRequest class with start()
                threads[i].start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        //loop through array of threads to join all threads
        for (int i = 0; i < numRequests; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //if exit command was selected, return as average time and total time not needed
        if (command.equals("exit")){
            return;
        }

        //calculate average time and print both total time and average time
        avgTime = (double)totalTime/numRequests;
        System.out.println("\nTotal turn-around time for client requests: " + totalTime + "ms");
        System.out.println("Average turn-around time for client requests: " + avgTime + "ms\n");
        totalTime = 0;


    }

    private static class ClientRequest extends Thread {
        private Socket requestSocket;
        private OutputStream output;
        private PrintWriter writer;
        private InputStream input;
        private BufferedReader reader;
        private long timerStart;
        private int threadNum;

        public ClientRequest(Socket requestSocket, int threadNum){
            this.requestSocket = requestSocket;
            this.threadNum = threadNum;
        }
        @Override
        public void run(){
            try{
                //create OutputStream to send data to server
                output = requestSocket.getOutputStream();
                //create PrintWriter to send data to server as a String
                writer = new PrintWriter(output, true);

                //creat InputStream to read input from server
                input = requestSocket.getInputStream();
                //create BufferReader to read server input as a String
                reader = new BufferedReader(new InputStreamReader(input));

                //if command = exit, send to server and bypass else statement
                if (command.equals("exit")){
                    writer.println(command);

                //if command != exit, start timer, read server response, print time, and add to total time
                } else {
                    timerStart = System.currentTimeMillis();
                    writer.println(command);
                    String s;
                    while ((s = reader.readLine()) != null){
                        System.out.println(s);
                    }
                    turnTime = System.currentTimeMillis() - timerStart;
                    System.out.println("Turn-around time for client request number " + (threadNum + 1) + ": "
                            + turnTime + "ms\n");
                    totalTime = totalTime + turnTime;
                }

                //clean up resources
                output.close();
                writer.close();
                input.close();
                reader.close();
                requestSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
