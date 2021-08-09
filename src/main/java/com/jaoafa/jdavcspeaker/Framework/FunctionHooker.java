package com.jaoafa.jdavcspeaker.Framework;

import com.jaoafa.jdavcspeaker.Framework.Action.ActionSubstrate;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibReporter;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class FunctionHooker extends ListenerAdapter {
    final String ROOT_PACKAGE = "com.jaoafa.jdavcspeaker";
    final String CMD_PACKAGE = "Command";
    final String ACT_PACKAGE = "Action";

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        String subCmdGroup = event.getSubcommandGroup();
        String subCmdName = event.getSubcommandName();

        boolean isSubCmdOnly = subCmdGroup == null && subCmdName != null;
        boolean isSubCmdAndGroup = subCmdGroup != null && subCmdName != null;

        String subCmd = null;
        if (isSubCmdOnly) subCmd = subCmdName;
        if (isSubCmdAndGroup) subCmd = "%s:%s".formatted(subCmdGroup, subCmdName);

        execute(new FunctionContainer().setAll(
            CMD_PACKAGE,
            event.getName(),
            subCmd,
            event.getJDA(),
            event.getGuild(),
            event.getChannel(),
            event.getChannelType(),
            event.getMember(),
            event.getUser(),
            event,
            null,
            null,
            null
        ));
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String[] buttonData = event.getId().split(":json");
        String buttonId = buttonData[0];
        JSONObject buttonJSON = new JSONObject(buttonData[1]);
        execute(new FunctionContainer().setAll(
            ACT_PACKAGE,
            buttonId,
            null,
            event.getJDA(),
            event.getGuild(),
            event.getChannel(),
            event.getChannelType(),
            event.getMember(),
            event.getUser(),
            event,
            event.getMessage(),
            event.getButton(),
            buttonJSON
        ));
    }

    private void execute(FunctionContainer container) {
        Object substrate;
        try {
            substrate = Class
                .forName("%s.%s.Cmd_%s".formatted(ROOT_PACKAGE, container.getFunctionType(), container.getFuncionName()))
                .getConstructor()
                .newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            new LibReporter(container.getChannel(), e);
            return;
        }
        switch (container.getFunctionType()) {
            case "Command" -> ((CmdSubstrate) substrate)
                .hooker(
                    container.getJda(),
                    container.getGuild(),
                    container.getChannel(),
                    container.getChannelType(),
                    container.getMember(),
                    container.getUser(),
                    (SlashCommandEvent) container.getEvent(),
                    container.getSubFunction()
                );
            case "Action" -> ((ActionSubstrate) substrate)
                .hooker(
                    container.getJda(),
                    container.getGuild(),
                    container.getChannel(),
                    container.getChannelType(),
                    container.getMember(),
                    container.getUser(),
                    container.getMessage(),
                    container.getButton(),
                    (ButtonClickEvent) container.getEvent(),
                    container.getData()
                );
        }
    }
}
