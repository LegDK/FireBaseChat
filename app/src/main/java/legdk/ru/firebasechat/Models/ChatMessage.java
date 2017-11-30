package legdk.ru.firebasechat.Models;

import com.google.firebase.database.Exclude;

public class ChatMessage {

    private String message;
    private String sender;
    private String recipient;

    private int recipientOrSenderStatus;

    public ChatMessage() {
    }

    public ChatMessage(String message, String sender, String recipient) {
        this.message = message;
        this.recipient = recipient;
        this.sender = sender;
    }


    public void setRecipientOrSenderStatus(int recipientOrSenderStatus) {
        this.recipientOrSenderStatus = recipientOrSenderStatus;
    }


    public String getMessage() {
        return message;
    }

    public String getRecipient(){
        return recipient;
    }

    public String getSender(){
        return sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Exclude
    public int getRecipientOrSenderStatus() {
        return recipientOrSenderStatus;
    }
}
