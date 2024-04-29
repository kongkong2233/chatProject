import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    private static Map<Integer, ChatRoom> chatRooms = new HashMap<>(); //채팅방 목록
    private static ChatRoomManager roomManager;

    public ChatServer() {
        this.roomManager = new ChatRoomManager();
    }

    public static void main(String[] args) {
        //1. 서버소켓 생성
        try (ServerSocket serverSocket = new ServerSocket(12345);) {
            System.out.println("채팅 서버가 준비되었습니다.");

            roomManager = new ChatRoomManager();

            //여러명의 클라이언트의 정보를 기억할 공간이 필요 => 순서가 중요하진 않고 귓속말 같은 기능을 위해서는 키값이 있는 Map
            Map<String, PrintWriter> chatClients = new HashMap<>();

            //2. accept()를 통해 소켓 얻어오기 / 여러명의 클라이언트와 소통할 수 있어야함(반복문)
            while (true) {
                Socket socket = serverSocket.accept();

                //IP주소 출력
                String clientAddress = socket.getInetAddress().getHostAddress();
                System.out.println("클라이언트가 연결되었습니다. IP : " + clientAddress);

                //스레드 이용!
                new ChatThread(socket, chatClients, roomManager).start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

class ChatThread extends Thread {
    //생성자를 통해 클라이언트 소켓 얻어옴
    private Socket socket;
    private String id;
    private Map<String, PrintWriter> chatClients;
    private ChatRoomManager roomManager;
    private ChatRoom room;

    private BufferedReader in;
    private PrintWriter out;

    public ChatThread(Socket socket, Map<String, PrintWriter> chatClients, ChatRoomManager roomManager) {
        this.socket = socket;
        this.chatClients = chatClients;
        this.roomManager = roomManager;

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            synchronized (chatClients) {
                chatClients.put(id, out);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    //run
    @Override
    public void run() {
        //연결된 클라이언트가 메시지를 전송하면 그 메시지를 받아 다른 사용자들에게 보내줌
        try {
            out.print("닉네임을 입력하세요: ");
            id = in.readLine();
            System.out.println(id + "닉네임의 사용자가 연결했습니다.");

            out.println("채팅방 명령어");
            out.println("방 목록 보기 : /list\n" +
                    "방 생성 : /create\n" +
                    "방 입장 : /join [방번호]\n" +
                    "방 나가기 : /exit\n" +
                    "접속종료 : /bye\n");

            String msg;
            while((msg = in.readLine()) != null) {
                if (msg.startsWith("/")) {
                    handleCommand(msg);
                } else {
                    handleMessage(msg);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void handleCommand(String command) {
        String[] parts = command.split(" ");
        switch (parts[0]) {
            case "/create":
                int roomId = roomManager.createChatRoom();
                out.println(roomId + "번 방이 생성되었습니다.");
                break;
            case "/join":
                if (parts.length != 2) {
                    out.println("잘못 입력하셨습니다. 채팅방 번호를 입력하세요.");
                    break;
                }
                try {
                    int roomIdJ = Integer.parseInt(parts[1]);
                    room = roomManager.getChatRoom(roomIdJ);
                    if (room != null) {
                        room.addClient(id, out);
                        out.println(roomIdJ + "번 방에 입장하셨습니다.");
                    } else {
                        out.println("존재하지 않는 채팅방입니다.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println(e);
                }
            case "/list":
                out.println("현재 존재하는 채팅방 목록");
                out.println(roomManager.getChatRooms());
                break;
            case "/exit":
                if (room != null) {
                    room.removeClient(id);
                    out.println("방에서 퇴장하였습니다.");
                    room.decrementUserCount();
                    if (room.getUserCount() == 0) {
                        roomManager.removeChatRoom(room.getRoomId() + 1);
                        System.out.println(room.getRoomId() + 1 + "번 방이 삭제되었습니다.");
                    }
                    room = null;
                } else {
                    out.println("현재 방에 입장해 있지 않습니다.");
                }
                break;
            case "/bye":
                out.println("클라이언트를 종료합니다.");
                break;
            case "/whisper":
                handleWhisper(parts);
                break;
            default:
                out.println("잘못된 명령어입니다.");
                break;
        }
    }

    private void handleMessage(String message) {
        if (room != null) {
            room.broadcast(message, id);
        } else {
            out.println("채팅방에 입장해 있지 않습니다.");
        }
    }

    private void handleWhisper(String[] parts) {
        if (parts.length >= 3) {
            String receiverId = parts[1];
            StringBuilder message = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                message.append(parts[i]).append(" ");
            }
            String msg = message.toString().trim();

            if (room != null) {
                room.sendMsg(msg, id, receiverId);
            } else {
                out.println("채팅방에 입장해 있지 않습니다.");
            }
        } else {
            out.println("잘못된 귓속말 형식입니다. /whisper [대상 아이디] [메시지]");
        }
    }
}
