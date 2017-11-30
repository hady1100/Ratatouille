package com.ratatouille.ratatouille_guc;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

/**
 * Created by hady on 11/29/17.
 */

public class Message implements IMessage, MessageContentType.Image, MessageContentType {
    String id, text, imageUrl, recipe_id;
    Author author;
    Date createdAt;

    public Message(String id, Author user, String text) {
        this(id, user, text, new Date());
    }

    public Message(String id, Author author, String text, Date createdAt) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Message(String id, Author author, String text, Date createdAt, String recipe_id) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.createdAt = createdAt;
        this.recipe_id = recipe_id;
    }

    public Message(String id, Author author, String text, Date createdAt, String imageUrl, String recipe_id) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.recipe_id = recipe_id;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Author getUser() {
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }
}
