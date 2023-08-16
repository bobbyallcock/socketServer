import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Server {
    static int port = 0;
    static ServerSocket serverSocket;
    static Socket socket;
    static String command, response;
    static Scanner in;
    public static void main(String[] args) {
        //initialize scanner
        in = new Scanner(System.in);
        //ask user for port number
        System.out.println("Please Enter Port Number");
        //call method to connect port
        connectPort();


        //while loop allowing server to indefinitely listen for client requests
        while (true) {
            try {
                //try to connect to client
                socket = serverSocket.accept();
                System.out.println("New client connected");

                //create InputStream to read data from client
                InputStream input = socket.getInputStream();
                //create bufferReader to read client data as a String
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                //create OutputStream to send data to client
                OutputStream output = socket.getOutputStream();
                //create PrintWriter to send data to client as a String
                PrintWriter writer = new PrintWriter(output, true);

                //command for server to execute is read from client
                command = reader.readLine();

                //if command equals exit, cleanup and break while loop to exit
                if (command.equals("exit")){
                    in.close();
                    input.close();
                    reader.close();
                    output.close();
                    writer.close();
                    socket.close();
                    break;
                }

                //allocate a String and create StringBuilder to process data from command
                String s;
                StringBuilder responseBuilder = new StringBuilder();

                //create process to execute linux system command
                Process p = Runtime.getRuntime().exec(command);

                //create BufferReader to read output from command
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));

                //create BufferReader to read errors from command
                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(p.getErrorStream()));


                // read the output from the command
                while((s = stdInput.readLine()) != null) {
                    responseBuilder.append(s);
                    responseBuilder.append(System.getProperty("line.separator"));
                }


                 //read any errors from the attempted command
                while((s = stdError.readLine()) != null) {
                    responseBuilder.append(s);
                    responseBuilder.append(System.getProperty("line.separator"));
                }

                //set response to StringBuilder string and send to client
                response = responseBuilder.toString();
                writer.println(response);

                //print to console command completed
                System.out.println("Completed the " + command + " command for client");

                //clean up resources
                in.close();
                input.close();
                reader.close();
                output.close();
                writer.close();
                stdError.close();
                stdInput.close();
                socket.close();


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void connectPort() {
        try {
            port = in.nextInt();
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);
        //if port is not valid or currently busy ask user to enter new port
        } catch (IOException ex) {
            in.nextLine();
            System.out.println("Please Enter a Different Port");
            connectPort();
        }
        //if user enters a non integer port number, ask for valid port
        catch (InputMismatchException e){
            in.nextLine();
            System.out.println("Please Enter a Valid Integer Port");
            connectPort();
        }
    }
}
