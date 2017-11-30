package com.ratatouille.ratatouille_guc.Utils;

import android.util.Log;
import android.view.View;

import com.ratatouille.ratatouille_guc.Message;
import com.stfalcon.chatkit.messages.MessageHolders;

/**
 * Created by hady on 11/30/17.
 */

public class CustomIncomingImageMessageViewHolder extends MessageHolders.IncomingImageMessageViewHolder<Message>{
    public CustomIncomingImageMessageViewHolder(View itemView) {
        super(itemView);

    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);

    }
}
