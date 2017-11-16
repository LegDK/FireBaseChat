package legdk.ru.firebasechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;



import java.util.List;

import legdk.ru.firebasechat.Helpers.ChatHelper;
import legdk.ru.firebasechat.Helpers.ExtraIntent;
import legdk.ru.firebasechat.Models.User;
import legdk.ru.firebasechat.R;
import legdk.ru.firebasechat.UI.ChatActivity;


public class UsersChatAdapter extends RecyclerView.Adapter<UsersChatAdapter.ViewHolderUsers> {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    private List<User> users;
    private Context context;
    private String currentUserEmail;
    private Long currentUserCreatedAt;
    private String currentUserId;

    public UsersChatAdapter(Context context, List<User> users) {
        this.users = users;
        this.context = context;
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(context,LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, int position) {

        User fireChatUser = users.get(position);

        // Set avatar
        int userAvatarId= ChatHelper.getDrawableAvatarId(fireChatUser.getAvatarId());
        Drawable  avatarDrawable = ContextCompat.getDrawable(context,userAvatarId);
        holder.getUserAvatar().setImageDrawable(avatarDrawable);

        // Set display name
        holder.getUserDisplayName().setText(fireChatUser.getDisplayName());

        // Set presence status
        holder.getStatusConnection().setText(fireChatUser.getConnection());

        // Set presence text color
        if(fireChatUser.getConnection().equals(ONLINE)) {
            // Green color
            holder.getStatusConnection().setTextColor(Color.parseColor("#00FF00"));
        }else {
            // Red color
            holder.getStatusConnection().setTextColor(Color.parseColor("#FF0000"));
        }

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void refill(User users) {
        this.users.add(users);
        notifyDataSetChanged();
    }

    public void changeUser(int index, User user) {
        this.users.set(index,user);
        notifyDataSetChanged();
    }

    public void setCurrentUserInfo(String userUid, String email, long createdAt) {
        currentUserId = userUid;
        currentUserEmail = email;
        currentUserCreatedAt = createdAt;
    }

    public void clear() {
        users.clear();
    }


    /* ViewHolder for RecyclerView */
    public class ViewHolderUsers extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView userAvatar;
        private TextView userDisplayName;
        private TextView statusConnection;
        private Context contextViewHolder;

        public ViewHolderUsers(Context context, View itemView) {
            super(itemView);
            userAvatar = (ImageView)itemView.findViewById(R.id.img_avatar);
            userDisplayName = (TextView)itemView.findViewById(R.id.text_view_display_name);
            statusConnection = (TextView)itemView.findViewById(R.id.text_view_connection_status);
            contextViewHolder = context;

            itemView.setOnClickListener(this);
        }

        public ImageView getUserAvatar() {
            return userAvatar;
        }

        public TextView getUserDisplayName() {
            return userDisplayName;
        }
        public TextView getStatusConnection() {
            return statusConnection;
        }


        @Override
        public void onClick(View view) {

            User user = users.get(getLayoutPosition());

            String chatRef = user.createUniqueChatRef(currentUserCreatedAt,currentUserEmail);

            Intent chatIntent = new Intent(contextViewHolder, ChatActivity.class);
            chatIntent.putExtra(ExtraIntent.EXTRA_CURRENT_USER_ID, currentUserId);
            chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_ID, user.getRecipientId());
            chatIntent.putExtra(ExtraIntent.EXTRA_CHAT_REF, chatRef);

            // Start new activity
            contextViewHolder.startActivity(chatIntent);

        }
    }

}
