package com.jaoafa.jdavcspeaker.Framework.Event;

import com.jaoafa.jdavcspeaker.Lib.LibClassFinder;
import com.jaoafa.jdavcspeaker.Lib.LibFlow;
import com.jaoafa.jdavcspeaker.Lib.LibReporter;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.lang.reflect.Constructor;

public class EventRegister {
    public EventRegister(JDABuilder jdaBuilder) {
        try {
            for (Class<?> eventClass : new LibClassFinder().findClasses("com.jaoafa.jdavcspeaker.Event")) {
                if (!eventClass.getSimpleName().startsWith("Event_")
                    || eventClass.getEnclosingClass() != null
                    || eventClass.getName().contains("$")) {
                    new LibFlow().error("%sはEventクラスではありません。スキップします...", eventClass.getSimpleName());
                    continue;
                }

                Object instance = ((Constructor<?>) eventClass.getConstructor()).newInstance();
                if (instance instanceof ListenerAdapter) {
                    jdaBuilder.addEventListeners(instance);
                    new LibFlow().success("%sを登録しました。", eventClass.getSimpleName());
                }
            }
        } catch (Exception e) {
            new LibReporter(null, e);
        }
    }
}
