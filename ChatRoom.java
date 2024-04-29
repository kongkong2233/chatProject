import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ChatRoom {
    private Map<String, PrintWriter> clients; //채팅방에 참여한 클라이언트 목록
    private String roomId; //채팅방 식별 번호

    public ChatRoom() {
        this.roomId = roomId;
        this.clients = new HashMap<>();
    }

    //메시지 전송(모두에게)
    public synchronized void broadcast(String message, String senderId) {
        for (PrintWriter writer : clients.values()) {
            writer.println(senderId + ": " + message);
        }
    }

    //메시지 전송(귓속말)
    public synchronized void sendMsg(String message, String senderId, String receiverId) {
        PrintWriter receiverWriter = clients.get(receiverId);
        if (receiverWriter != null) {
            receiverWriter.println(senderId + "님의 귓속말: " + message);
        } else {
            System.out.println("수신자가 존재하지 않습니다. 아이디를 다시 한 번 확인해주세요.");
        }
    }

    //클라이언트 추가 및 제거
    public synchronized void addClient(String clientId, PrintWriter writer) {
        clients.put(clientId, writer);
    }

    public synchronized void removeClient(String clientId) {
        clients.remove(clientId);
    }
}
