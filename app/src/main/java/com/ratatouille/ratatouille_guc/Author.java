package com.ratatouille.ratatouille_guc;

import com.stfalcon.chatkit.commons.models.IUser;

/**
 * Created by hady on 11/29/17.
 */

public class Author implements IUser {

    String id, name, avatar;

    public Author(String id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }
}
