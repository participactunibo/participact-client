/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most;

import org.most.event.EventReceiversWrapper;
import org.most.input.InputBus;
import org.most.persistence.DBAdapter;
import org.most.pipeline.PipelineBus;
import org.slf4j.LoggerFactory;

import android.app.Application;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.StatusPrinter;

public class MoSTApplication extends Application {

	public static final String ACTION_INPUT = "org.most.action_input";
	public static final String ACTION_PIPELINE = "org.most.action_pipeline";

	public static final String PREF_DB = "db_preferences";
	public static final String PREF_DB_TABLES_KEY = "db_tables";
	public static final String PREF_DB_NAME_KEY = "db_name";
	public static final String PREF_DB_NAME_DEFAULT = "most_db";
	public static final String PREF_MOST_SERVICE = "most_service";
	
	public static final String PREF_INPUT = "input_preferences";
	public static final String PREF_PIPELINES = "pipeline_preferences";

    private Controller _controller;
    private DataBundlePool _dataBundlePool;
    private InputBus _inputBus;
    private PipelineBus _pipelineBus;
    private InputManager _inputManager;
    private PipelineManager _pipelineManager;
    private WakeLockHolder _wakeLockHolder;
    private DBAdapter _dbAdapter;
    private InputsArbiter _inputArbiter;
    private EventReceiversWrapper _eventReceiversWrapper;

    @Override
    public void onCreate() {
        _dataBundlePool = new DataBundlePool();
        _inputBus = new InputBus();
        _pipelineBus = new PipelineBus();
        _wakeLockHolder = new WakeLockHolder(this);
        _inputManager = new InputManager(this);
        _pipelineManager = new PipelineManager(this);
        _controller = new Controller(this);
        _dbAdapter = DBAdapter.getInstance(this);
        _inputArbiter = new InputsArbiter(this);
        _eventReceiversWrapper = new EventReceiversWrapper(this);

        configureLogback();

        //set power policies for microphone
//        _inputManager.setPowerPolicyForInput(Input.Type.AUDIO, new AsymmetricDutyCyclePolicy(this, Input.Type.AUDIO));
//        _inputManager.setPowerPolicyForInput(Input.Type.ACCELEROMETER, new AsymmetricDutyCyclePolicy(this, Input.Type.ACCELEROMETER));

        //register event receivers
        _eventReceiversWrapper.registerAllEventReceivers();
    }


    public Controller getController() {
        return _controller;
    }

    public DataBundlePool getDataBundlePool() {
        return _dataBundlePool;
    }

    public InputBus getInputBus() {
        return _inputBus;
    }

    public PipelineBus getPipelineBus() {
        return _pipelineBus;
    }

    public InputManager getInputManager() {
        return _inputManager;
    }

    public PipelineManager getPipelineManager() {
        return _pipelineManager;
    }

    public WakeLockHolder getWakeLockHolder() {
        return _wakeLockHolder;
    }

    public DBAdapter getDbAdapter() {
        return _dbAdapter;
    }

    public InputsArbiter getInputsArbiter() {
        return _inputArbiter;
    }

    private void configureLogback() {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        final String LOG_DIR = getExternalFilesDir(null).getAbsolutePath();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setContext(context);

        // OPTIONAL: Set an active log file (separate from the rollover files).
        // If rollingPolicy.fileNamePattern already set, you don't need this.
        rollingFileAppender.setFile(LOG_DIR + "/most.log");

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setFileNamePattern(LOG_DIR + "/most.%i.log");
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

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.most");
        logger.setLevel(Level.DEBUG);
        logger.addAppender(rollingFileAppender);

        // print any status messages (warnings, etc) encountered in logback config
        StatusPrinter.print(context);
    }
}
