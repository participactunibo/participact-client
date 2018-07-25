/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact;

import android.content.Context;
import android.support.multidex.MultiDex;

import org.most.MoSTApplication;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.StatusPrinter;
import it.unibo.participact.broadcastreceivers.AlarmBroadcastReceiver;

public class ParticipActApplication extends MoSTApplication {

    Map<Long, AlarmBroadcastReceiver> alarmBR;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        configureLogback();
    }

    public ParticipActApplication() {
        super();
        alarmBR = new HashMap<Long, AlarmBroadcastReceiver>();
    }

    public Map<Long, AlarmBroadcastReceiver> getAlarmBR() {
        return alarmBR;
    }

    public void setAlarmBR(Map<Long, AlarmBroadcastReceiver> alarmBR) {
        this.alarmBR = alarmBR;
    }

    private void configureLogback() {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        //context.reset();

        final String PA_LOG_DIR = getExternalFilesDir(null).getAbsolutePath();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setContext(context);

        // OPTIONAL: Set an active log file (separate from the rollover files).
        // If rollingPolicy.fileNamePattern already set, you don't need this.
        rollingFileAppender.setFile(PA_LOG_DIR + "/pa.log");

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setFileNamePattern(PA_LOG_DIR + "/pa.%i.log");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(2);
        rollingPolicy.setParent(rollingFileAppender);  // parent and context required!
        rollingPolicy.setContext(context);
        rollingPolicy.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);

        SizeBasedTriggeringPolicy<ILoggingEvent> triggerPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        triggerPolicy.setMaxFileSize("10MB");

        rollingFileAppender.setTriggeringPolicy(triggerPolicy);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%date [%thread] %-5level %logger{36}.%method - %msg%n");
        encoder.setContext(context);
        encoder.start();

        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
//	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//	    root.setLevel(Level.DEBUG);
//	    root.addAppender(rollingFileAppender);

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("it.unibo.participact");
        logger.setLevel(Level.DEBUG);
        logger.addAppender(rollingFileAppender);

        // print any status messages (warnings, etc) encountered in logback config
        StatusPrinter.print(context);
    }

}
