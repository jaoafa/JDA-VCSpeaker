//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package com.jaoafa.jdavcspeaker.Lib;

import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.jda.JDACommandManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class JDACommandListener<C> extends ListenerAdapter {
    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_INVALID_SYNTAX = "Invalid Command Syntax. Correct command syntax is: ";
    private static final String MESSAGE_NO_PERMS = "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command";

    private final JDACommandManager<C> commandManager;

    public JDACommandListener(JDACommandManager<C> commandManager) {
        this.commandManager = commandManager;
    }
    @Override
    public final void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        C sender = this.commandManager.getCommandSenderMapper().apply(event);
        if (this.commandManager.getBotId() != event.getAuthor().getIdLong()) {
            String prefix = (String)this.commandManager.getPrefixMapper().apply(sender);
            String content = message.getContentRaw();
            if (content.startsWith(prefix)) {
                content = content.substring(prefix.length());
                this.commandManager.executeCommand(sender, content).whenComplete((commandResult, throwable) -> {
                    if (throwable != null) {
                        if (throwable instanceof InvalidSyntaxException) {
                            this.commandManager.handleException(sender, InvalidSyntaxException.class, (InvalidSyntaxException)throwable, (c, e) -> {
                                this.sendMessage(event, "間違ったコマンド形式です。:" + prefix + ((InvalidSyntaxException)throwable).getCorrectSyntax());
                            });
                        } else if (throwable instanceof InvalidCommandSenderException) {
                            this.commandManager.handleException(sender, InvalidCommandSenderException.class, (InvalidCommandSenderException)throwable, (c, e) -> {
                                this.sendMessage(event, throwable.getMessage());
                            });
                        } else if (throwable instanceof NoPermissionException) {
                            this.commandManager.handleException(sender, NoPermissionException.class, (NoPermissionException)throwable, (c, e) -> {
                                this.sendMessage(event, "あなたにはそのコマンドを実行する権限がありません！");
                            });
                        } else if (throwable instanceof NoSuchCommandException) {
                            this.commandManager.handleException(sender, NoSuchCommandException.class, (NoSuchCommandException)throwable, (c, e) -> {
                                this.sendMessage(event, "未知のコマンドです。");
                            });
                        } else if (throwable instanceof ArgumentParseException) {
                            this.commandManager.handleException(sender, ArgumentParseException.class, (ArgumentParseException)throwable, (c, e) -> {
                                this.sendMessage(event, "間違ったコマンド引数: " + throwable.getCause().getMessage());
                            });
                        } else if (throwable instanceof CommandExecutionException) {
                            this.commandManager.handleException(sender, CommandExecutionException.class, (CommandExecutionException)throwable, (c, e) -> {
                                this.sendMessage(event, "コマンド実行中に内部エラーが発生しました。");
                                throwable.getCause().printStackTrace();
                            });
                        } else {
                            this.sendMessage(event, throwable.getMessage());
                        }

                    }
                });
            }
        }
    }

    private void sendMessage(MessageReceivedEvent event, String message) {
        event.getChannel().sendMessage(message).queue();
    }
}
