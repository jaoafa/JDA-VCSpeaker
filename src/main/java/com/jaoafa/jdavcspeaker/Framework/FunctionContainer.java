package com.jaoafa.jdavcspeaker.Framework;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.components.Button;
import org.json.JSONObject;

public class FunctionContainer {
    /**
     * データ
     */
    //機能の種類 : Command || Action
    private String functionType = null;
    //機能の名前 : Cmd_XXX || Act_XXX
    private String funcionName = null;
    //サブコマンド : subCmd:subArg
    private String subFunction = null;
    //ボタンデータ : json形式
    private JSONObject data = null;
    //以下Discordデータ
    private JDA jda = null;
    private Guild guild = null;
    private MessageChannel channel = null;
    private ChannelType channelType = null;
    private Member member = null;
    private User user = null;
    private Event event = null;
    private Message message = null;
    private Button button = null;

    /**
     * Getter
     */
    public String getFunctionType() {
        return this.functionType;
    }

    public String getFuncionName() {
        return this.funcionName;
    }

    public String getSubFunction() {
        return this.subFunction;
    }

    public JSONObject getData() {
        return this.data;
    }

    public JDA getJda() {
        return this.jda;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public MessageChannel getChannel() {
        return this.channel;
    }

    public ChannelType getChannelType() {
        return this.channelType;
    }

    public Member getMember() {
        return this.member;
    }

    public User getUser() {
        return this.user;
    }

    public Event getEvent() {
        return this.event;
    }

    public Message getMessage() {
        return this.message;
    }

    public Button getButton() {
        return this.button;
    }

    /**
     * Setter
     */
    public FunctionContainer setAll(String functionType, String functionName,
                       String subFunction, JDA jda, Guild guild,
                       MessageChannel channel, ChannelType channelType,
                       Member member, User user, Event event,
                       Message message, Button button, JSONObject data) {
        this.functionType = functionType;
        this.funcionName = functionName.substring(0, 1).toUpperCase() + functionName.substring(1).toLowerCase();;
        this.subFunction = subFunction;
        this.jda = jda;
        this.guild = guild;
        this.channel = channel;
        this.channelType = channelType;
        this.member = member;
        this.user = user;
        this.event = event;
        this.message = message;
        this.button = button;
        this.data = data;

        return this;
    }
}
