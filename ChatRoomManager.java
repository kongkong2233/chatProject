import java.util.HashMap;
import java.util.Map;

public class ChatRoomManager {
    private Map<Integer, ChatRoom> chatRooms;
    private int nextRoomId;

    public ChatRoomManager() {
        this.chatRooms = new HashMap<>();
        this.nextRoomId = 1;
    }

    //채팅방 생성
    public synchronized int createChatRoom() {
        int roomId = nextRoomId++;
        ChatRoom room = new ChatRoom();
        chatRooms.put(roomId, room);
        return roomId;
    }

    //채팅방 삭제
    public synchronized void removeChatRoom(int roomId) {
        chatRooms.remove(roomId);
    }

    //채팅방 번호로 채팅방 객체 가져오기
    public synchronized ChatRoom getChatRoom(int roomId) {
        return chatRooms.get(roomId);
    }

    //채팅방 목록 확인
    public synchronized String getChatRooms() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, ChatRoom> entry : chatRooms.entrySet()) {
            int roomId = entry.getKey();
            ChatRoom room = entry.getValue();
            builder.append("방 번호: ").append(roomId);
        }
        return builder.toString();
    }
}
