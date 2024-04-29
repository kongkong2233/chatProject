import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String CREATE_COMMAND = "/create";
    private static final String JOIN_COMMAND = "/join";
    private static final String LIST_COMMAND = "/list";
    private static final String EXIT_COMMAND = "/exit";
    private static final String BYE_COMMAND = "/bye";
    private static final String WHISPER_COMMAND = "/whisper";

    public static void main(String[] args) {
        String hostName = "127.0.0.1"; // 서버가 실행 중인 호스트의 이름 또는 IP 주소
        int portNumber = 12345; // 서버와 동일한 포트 번호 사용

        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try{
            socket = new Socket(hostName, portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner stdIn = new Scanner(System.in);

            // 서버로부터 메시지를 읽어 화면에 출력하는 별도의 스레드
            Thread readThread = new Thread(new ServerMessageReader(in));
            readThread.start(); // 메시지 읽기 스레드 시작

            // 사용자 입력 처리
            String userInput;
            while (true) {
                userInput = stdIn.nextLine();

                if (userInput.startsWith(CREATE_COMMAND)) {
                    handleCreateCommand(out);
                } else if (userInput.startsWith(JOIN_COMMAND)) {
                    handleJoinCommand(out, userInput);
                } else if (userInput.equals(LIST_COMMAND)) {
                    handleListCommand(out);
                } else if (userInput.equals(EXIT_COMMAND)) {
                    handleExitCommand(out);
                } else if (userInput.equals(BYE_COMMAND)) {
                    out.println(userInput);
                    break;
                } else if (userInput.startsWith(WHISPER_COMMAND)) {
                    handleWhisperCommand(out, userInput);
                } else {
                    // 서버에 메시지를 전송합니다.
                    out.println(userInput);
                }
            }

            // 클라이언트와 서버는 명시적으로 close를 합니다. close를 할 경우 상대방쪽의 readLine()이 null을 반환됩니다. 이 값을 이용하여 접속이 종료된 것을 알 수 있습니다.
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect to " + hostName + " on port " + portNumber);
            e.printStackTrace();
        }
    }

    //채팅방 생성 요청 서버에 전송
    private static void handleCreateCommand(PrintWriter out) {
        out.println(CREATE_COMMAND);
    }

    //채팅방 입장 요청 서버에 전송
    private static void handleJoinCommand(PrintWriter out, String userInput) {
        out.println(userInput.replace(JOIN_COMMAND, "/join"));
    }

    //서버로 명령어 전송 /list
    private static void handleListCommand(PrintWriter out) {
        out.println(LIST_COMMAND);
    }

    //서버로 명령어 전송 /exit
    private static void handleExitCommand(PrintWriter out) {
        out.println(EXIT_COMMAND);
    }

    //서버로 명령어 전송 /whisper
    private static void handleWhisperCommand(PrintWriter out, String userInput) {
        out.println(userInput);
    }
}