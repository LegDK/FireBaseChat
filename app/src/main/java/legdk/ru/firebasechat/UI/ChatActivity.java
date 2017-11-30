package legdk.ru.firebasechat.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blakequ.rsa.Base64Utils;
import com.blakequ.rsa.FileEncryptionManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import legdk.ru.firebasechat.Adapter.MessageChatAdapter;
import legdk.ru.firebasechat.Helpers.ExtraIntent;
import legdk.ru.firebasechat.Models.ChatMessage;
import legdk.ru.firebasechat.R;

public class ChatActivity extends Activity {

    private static final String TAG = ChatActivity.class.getSimpleName();

    @BindView(R.id.recycler_view_chat)
    RecyclerView chatRecyclerView;
    @BindView(R.id.edit_text_message)
    EditText userMessageChatText;


    private String recipientId;
    private String currentUserId;
    private MessageChatAdapter messageChatAdapter;
    private DatabaseReference messageChatDatabase;
    private ChildEventListener messageChatListener;
    String privateKey, publicKey;
    FileEncryptionManager fileEncryptionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        rsaInitial();
        bindButterKnife();
        setDatabaseInstance();
        setUsersId();
        setChatRecyclerView();
    }

    private void rsaInitial() {
        fileEncryptionManager = FileEncryptionManager.getInstance();
        try {
//            fileEncryptionManager.generateKey();
//            publicKey = fileEncryptionManager.getPublicKey();
//            privateKey = fileEncryptionManager.getPrivateKey();
//            System.out.println("public key " + fileEncryptionManager.getPublicKey()+" public key");
//            System.out.println("private key "+fileEncryptionManager.getPrivateKey()+" private key");
//            fileEncryptionManager.setRSAKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIqpnEQT/d6epYw8oHrHbaWBr2dIJTP6PnNGjo" +
//                    "bUK0nb9wwPz61jcYbQ6hveon6svA+OhRifJqT46kwrpzY0fo/CgufvzEYW3o2B4Q/M2CU+mtcmSS" +
//                    "Nm4OpdRVPNqqBwYUQN6wFHE3yRG7/RtpQeQHK8NO/7itLZsRyYaaJuPkPQIDAQAB","MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMiqmcRBP93p6ljDygesdtpYGvZ\n" +
//                    "glM/o+c0aOhtQrSdv3DA/PrWNxhtDqG96ifqy8D46FGJ8mpPjqTCunNjR+j8KC5+/MRhbejYHhD8" +
//                    "zYJT6a1yZJI2bg6l1FU82qoHBhRA3rAUcTfJEbv9G2lB5Acrw07/uK0tmxHJhpom4+Q9AgMBAAEC" +
//                    "gYBsWcnbcYwFzDdwpI9jI1mtviMY7oIsZ1GeFuuKjOyW0BK91EcFPkPTmZdr+DsGQL6yPPRcisrt" +
//                    "YnXYOxkoEvSRQQMUEehwcnI8hkUvZbInGwLZATqFfqMtidn/mFV6XMtIFft6flPZL9F5a2c1ME/E" +
//                    "uKii/yW3RCpWlKe4iHpkNQJBAO26NrKJsqguanTsNKqBtnSDuQ1RsVEPDVAdnbx0BEiYYpnGhV+e" +
//                    "cLYWH0k0iHEEZkqL0VijMdVVPZPRKE0XXBcCQQDYFyJWHUv/J8IYyO/TdAU/JP2WzGlGmF1a1sal" +
//                    "1KeBzsO8jfHncHIEBliZl+s3Dus1g/ICuQn7+SeMhn4npdLLAkBUu12Jj1jVZw3ctOAkse0VZGnN" +
//                    "3INF8AZ/ur6hD/0C3wWt57tcFH35+LKYBhhnp/jJs0IfxLMbZbF4AUHLEHtrAkEAvBLmf6zgCCPC" +
//                    "e2/nzFKefmbjj+w5dIgIOJfWLV74q7IaCnJAqP7lPTuIwaXHwxPpA5rjeBs05WnlUhl8k2VwIQJB" +
//                    "ANizIXQ7HCqgQ2bnlr4Rl7QfougBsfpXf7uOKB6v6OEgjudReNvKsKuP39Dqo438kNy2IhJYF+qu" +
//                    "jxsJHB7LWNw=",true);
        fileEncryptionManager.setRSAKey("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKh5AWf0f8FS78sydiYFa3bvoA8XGgWZ" +
                "owFutVrbYfoc4QvRK5acHFzXTm7w6MyVLD8zpIUN9YJBNzCT/Lg84vcCAwEAAQ==","MIIBOwIBAAJBAKh5AWf0f8FS78sydiYFa3bvoA8XGgWZowFutVrbYfoc4QvRK5ac\n" +
                "HFzXTm7w6MyVLD8zpIUN9YJBNzCT/Lg84vcCAwEAAQJAeaX2Z/PaE+QpFhY3zbh4" +
                "IodgkcTim0yWsUAZX6OtmYT4zo0VpOje4dDcBAFbssUTNHL2B3aZXge0ezfM8/CA" +
                "sQIhANUyKLcYuu49zARPbKrikkTWfy86+oLQFHJJtr2EcemjAiEAykwpSuFGhwg6" +
                "KRYmeI4Yd6Kp/j19T/C8+rlKgE36np0CIQCUOPdNtQfhs10wcfffJv8ClQ5/y8V3" +
                "RSux+cXvJTH9eQIgDJjbhWcrziLMBFz1vlIS4fhGH6fSktTJ388D/aNddN0CIQDH" +
                "rWSfesjlrDpS3TFQo1S/O3ZJ6OpOcZmIZlsZKUDzTg==",true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void setDatabaseInstance() {
        String chatRef = getIntent().getStringExtra(ExtraIntent.EXTRA_CHAT_REF);
        messageChatDatabase = FirebaseDatabase.getInstance().getReference().child(chatRef);
    }

    private void setUsersId() {
        recipientId = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_ID);
        currentUserId = getIntent().getStringExtra(ExtraIntent.EXTRA_CURRENT_USER_ID);
    }

    private void setChatRecyclerView() {
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setHasFixedSize(true);
        messageChatAdapter = new MessageChatAdapter(new ArrayList<ChatMessage>());
        chatRecyclerView.setAdapter(messageChatAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        messageChatListener = messageChatDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {

                if (dataSnapshot.exists()) {
                    ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                    if (newMessage.getSender().equals(currentUserId)) {
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.SENDER);
                    } else {
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.RECIPIENT);
                    }
                    try {
                        //Toast.makeText(getApplicationContext(),"rsa",Toast.LENGTH_SHORT).show();
                        byte[] decryptByte = fileEncryptionManager.decryptByPrivateKey(Base64Utils.decode(newMessage.getMessage()));
                        //Toast.makeText(getApplicationContext(),"ok",Toast.LENGTH_SHORT).show();
                        newMessage.setMessage(new String(decryptByte));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    messageChatAdapter.refillAdapter(newMessage);
                    chatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount() - 1);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();

        if (messageChatListener != null) {
            messageChatDatabase.removeEventListener(messageChatListener);
        }
        messageChatAdapter.cleanUp();

    }

    @OnClick(R.id.btn_send_message)
    public void btnSendMsgListener(View sendButton) {

        String senderMessage = userMessageChatText.getText().toString().trim();

        if (!senderMessage.isEmpty()) {
            try {
                byte[] encryptByte = fileEncryptionManager.encryptByPublicKey(senderMessage.getBytes());
                senderMessage = Base64Utils.encode(encryptByte);

                ChatMessage newMessage = new ChatMessage(senderMessage, currentUserId, recipientId);
                messageChatDatabase.push().setValue(newMessage);
                userMessageChatText.setText("");
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }


}
