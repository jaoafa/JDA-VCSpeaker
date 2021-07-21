package com.jaoafa.jdavcspeaker.Framework.Action;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.json.JSONObject;

public interface ActionSubstrate {
    void hooker(JDA jda, Guild guild, MessageChannel channel,ChannelType type, Member member, User user, Message message, Button button, ButtonClickEvent event, JSONObject actionData);
}
