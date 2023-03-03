package com.jaoafa.jdavcspeaker.Framework;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONObject;

public interface FunctionSubstrate {
    String functionName();

    String subFunction();

    JDA jda();

    Guild guild();

    GuildMessageChannel channel();

    ChannelType type();

    Member member();

    User user();

    Event event();

    Message message();

    Button button();

    JSONObject data();
}
