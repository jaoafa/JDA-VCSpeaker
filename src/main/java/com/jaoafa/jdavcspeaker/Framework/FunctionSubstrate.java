package com.jaoafa.jdavcspeaker.Framework;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.components.Button;
import org.json.JSONObject;

public interface FunctionSubstrate {
    String funcionName();

    String subFunction();

    JDA jda();

    Guild guild();

    MessageChannel channel();

    ChannelType type();

    Member member();

    User user();

    Event event();

    Message message();

    Button button();

    JSONObject data();
}
