package com.jaoafa.jdavcspeaker.Framework.Command;


import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CmdDetail {
    private String emoji = null;
    private String detail = null;
    private CommandData data = null;
    private Permission[] limitPerm = null;
    private Role[] limitRole = null;
    private User[] limitUser = null;
    private String[] cmdArgs = null;
    private String[] exampleArgs = null;

    public String getEmoji() {
        return this.emoji;
    }

    public CmdDetail setEmoji(String emoji) {
        this.emoji = emoji;
        return this;
    }

    public String getDetail() {
        return this.detail;
    }

    public CmdDetail setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public CommandData getData() {
        return this.data;
    }

    public CmdDetail setData(CommandData data) {
        this.data = data;
        return this;
    }

    public Permission[] getLimitPerm() {
        return this.limitPerm;
    }

    public CmdDetail setLimitPerm(Permission... perm) {
        this.limitPerm = perm;
        return this;
    }

    public Role[] getLimitRole() {
        return this.limitRole;
    }

    public CmdDetail setLimitRole(Role... role) {
        this.limitRole = role;
        return this;
    }

    public User[] getLimitUser() {
        return this.limitUser;
    }

    public CmdDetail setLimitUser(User... user) {
        this.limitUser = user;
        return this;
    }

    public String[] getCmdArgs() {
        return this.cmdArgs;
    }

    public CmdDetail setCmdArgs(String... args) {
        this.cmdArgs = args;
        return this;
    }

    public String[] getExampleArgs() {
        return this.exampleArgs;
    }

    public CmdDetail setExampleArgs(String... args) {
        this.exampleArgs = args;
        return this;
    }
}
