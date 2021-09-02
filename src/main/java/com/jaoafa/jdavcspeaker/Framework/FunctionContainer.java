package com.jaoafa.jdavcspeaker.Framework;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.components.Button;
import org.json.JSONObject;

public record FunctionContainer(FunctionType functionType,
                                String functionName,
                                String subFunction,
                                JSONObject data,
                                JDA jda,
                                Guild guild,
                                MessageChannel channel,
                                ChannelType channelType,
                                Member member,
                                User user,
                                Event event,
                                Message message,
                                Button button) {
}
