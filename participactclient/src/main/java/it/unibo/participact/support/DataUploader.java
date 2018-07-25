/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.support;

import android.content.ContentValues;
import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.most.persistence.DBAdapter;
import org.most.pipeline.PipelineAccelerometer;
import org.most.pipeline.PipelineAccelerometerClassifier;
import org.most.pipeline.PipelineActivityRecognitionCompare;
import org.most.pipeline.PipelineAppOnScreen;
import org.most.pipeline.PipelineAppsNetTraffic;
import org.most.pipeline.PipelineBattery;
import org.most.pipeline.PipelineBluetooth;
import org.most.pipeline.PipelineCell;
import org.most.pipeline.PipelineConnectionType;
import org.most.pipeline.PipelineDR;
import org.most.pipeline.PipelineDeviceNetTraffic;
import org.most.pipeline.PipelineGoogleActivityRecognition;
import org.most.pipeline.PipelineGyroscope;
import org.most.pipeline.PipelineInstalledApps;
import org.most.pipeline.PipelineLight;
import org.most.pipeline.PipelineLocation;
import org.most.pipeline.PipelineMagneticField;
import org.most.pipeline.PipelinePhoneCallDuration;
import org.most.pipeline.PipelinePhoneCallEvent;
import org.most.pipeline.PipelineSystemStats;
import org.most.pipeline.PipelineWifiScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import it.unibo.participact.domain.data.DataAccelerometer;
import it.unibo.participact.domain.data.DataAccelerometerClassifier;
import it.unibo.participact.domain.data.DataAppOnScreen;
import it.unibo.participact.domain.data.DataBattery;
import it.unibo.participact.domain.data.DataBluetooth;
import it.unibo.participact.domain.data.DataCell;
import it.unibo.participact.domain.data.DataGyroscope;
import it.unibo.participact.domain.data.DataInstalledApps;
import it.unibo.participact.domain.data.DataLight;
import it.unibo.participact.domain.data.DataLocation;
import it.unibo.participact.domain.data.DataMagneticField;
import it.unibo.participact.domain.data.DataPhoneCallDuration;
import it.unibo.participact.domain.data.DataPhoneCallEvent;
import it.unibo.participact.domain.data.DataSystemStats;
import it.unibo.participact.domain.data.DataWifiScan;
import it.unibo.participact.domain.persistence.DataQuestionnaireFlat;
import it.unibo.participact.domain.persistence.support.DomainDBHelper;
import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.network.request.ApacheHttpSpiceService;
import it.unibo.participact.network.request.DataAccelerometerClassifierUploadRequest;
import it.unibo.participact.network.request.DataAccelerometerUploadRequest;
import it.unibo.participact.network.request.DataActivityRecognitionCompareUploadRequest;
import it.unibo.participact.network.request.DataAppOnScreenUploadRequest;
import it.unibo.participact.network.request.DataAppsNetTrafficUploadRequest;
import it.unibo.participact.network.request.DataBatteryUploadRequest;
import it.unibo.participact.network.request.DataBluetoothUploadRequest;
import it.unibo.participact.network.request.DataCellUploadRequest;
import it.unibo.participact.network.request.DataConnectionTypeUploadRequest;
import it.unibo.participact.network.request.DataDRUploadRequest;
import it.unibo.participact.network.request.DataDeviceNetTrafficUploadRequest;
import it.unibo.participact.network.request.DataGoogleActivityRecognitionUploadRequest;
import it.unibo.participact.network.request.DataGyroscopeUploadRequest;
import it.unibo.participact.network.request.DataInstalledAppsUploadRequest;
import it.unibo.participact.network.request.DataLightUploadRequest;
import it.unibo.participact.network.request.DataLocationUploadRequest;
import it.unibo.participact.network.request.DataMagneticFieldUploadRequest;
import it.unibo.participact.network.request.DataPhoneCallDurationUploadRequest;
import it.unibo.participact.network.request.DataPhoneCallEventUploadRequest;
import it.unibo.participact.network.request.DataPhotoUploadRequest;
import it.unibo.participact.network.request.DataQuestionnaireFlatUploadRequest;
import it.unibo.participact.network.request.DataSystemStatsUploadRequest;
import it.unibo.participact.network.request.DataWifiScanUploadRequest;
import it.unibo.participact.network.request.LogUploadRequest;
import it.unibo.participact.network.request.LogUploadRequestListener;
import it.unibo.participact.network.request.StateUploadRequest;
import it.unibo.participact.network.request.StateUploadRequestListener;
import it.unibo.participact.support.preferences.DataUploaderLogPreferences;
import it.unibo.participact.support.preferences.DataUploaderPhotoPreferences;
import it.unibo.participact.support.preferences.DataUploaderPreferences;
import it.unibo.participact.support.preferences.DataUploaderQuestionnairePreferences;
import it.unibo.participact.support.preferences.DataUploaderStatePreferences;

public class DataUploader implements RequestListener<ResponseMessage> {

    private static DataUploader instance;
    private static final int DEFAULT_ROW_NUM = 5000;
    private static final int MIN_DATA_FOR_UPLOAD = 10;
    private static final int NUM_DIFF_TYPE_UPLOAD = 23;
    private static final String PA_LOG_FILENAME = "pa";
    private static final String MOST_LOG_FILENAME = "most";
    private AtomicInteger uploading;

    private static final Logger logger = LoggerFactory.getLogger(DataUploader.class);


    private SpiceManager spiceManager = new SpiceManager(ApacheHttpSpiceService.class);
    Context context;
    DBAdapter db;

    private DataUploader(Context context) {
        this.context = context;
        db = DBAdapter.getInstance(context);
        uploading = new AtomicInteger(0);
    }

    public static DataUploader getInstance(Context context) {
        if (instance == null) {
            instance = new DataUploader(context);
        }
        return instance;
    }

    @Override
    public void onRequestFailure(SpiceException arg0) {
        logger.error("Error during upload.", arg0);
        LoginUtility.checkIfLoginException(context, arg0);
        uploading.decrementAndGet();
    }

    @Override
    public void onRequestSuccess(ResponseMessage arg0) {
        logger.info("Request {} successful", arg0.getKey());
        uploading.decrementAndGet();
    }

    private synchronized void startSpiceManager() {
        if (!spiceManager.isStarted()) {
            logger.info("DataUploader starting SpiceManager");
            spiceManager.start(context);
        }
    }

    public void uploadDataAccelerometerClassifier() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineAccelerometerClassifier.TBL_ACCELEROMETER_CLASSIFIER, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataAccelerometerClassifier.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataAccelerometerClassifierUploadRequest request = new DataAccelerometerClassifierUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataAccelerometerClassifier.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataAccelerometer() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineAccelerometer.TBL_ACCELEROMETER, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataAccelerometer.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataAccelerometerUploadRequest request = new DataAccelerometerUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataAccelerometer.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataAppOnScreen() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineAppOnScreen.TBL_APP_ON_SCREEN, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataAppOnScreen.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataAppOnScreenUploadRequest request = new DataAppOnScreenUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataAppOnScreen.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataBattery() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineBattery.TBL_BATTERY, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataBattery.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataBatteryUploadRequest request = new DataBatteryUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataBattery.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataBluetooth() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineBluetooth.TBL_BLUETOOH, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataBluetooth.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataBluetoothUploadRequest request = new DataBluetoothUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataBluetooth.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataCell() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineCell.TBL_CELL, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataCell.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataCellUploadRequest request = new DataCellUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataCell.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataGyroscope() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineGyroscope.TBL_GYROSCOPE, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataGyroscope.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataGyroscopeUploadRequest request = new DataGyroscopeUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataGyroscope.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataInstalledApps() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineInstalledApps.TBL_INSTALLED_APPS, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataInstalledApps.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataInstalledAppsUploadRequest request = new DataInstalledAppsUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataInstalledApps.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataLight() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineLight.TBL_LIGHT, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataLight.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataLightUploadRequest request = new DataLightUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataLight.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataLocation() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineLocation.TBL_LOCATION, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataLocation.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataLocationUploadRequest request = new DataLocationUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataLocation.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataMagneticField() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineMagneticField.TBL_MAGNETIC_FIELD, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataMagneticField.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataMagneticFieldUploadRequest request = new DataMagneticFieldUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataMagneticField.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataPhoneCallDuration() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelinePhoneCallDuration.TBL_PHONE_CALL_DURATION, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataPhoneCallDuration.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataPhoneCallDurationUploadRequest request = new DataPhoneCallDurationUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataPhoneCallDuration.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataPhoneCallEvent() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelinePhoneCallEvent.TBL_PHONE_CALL_EVENT, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataPhoneCallEvent.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataPhoneCallEventUploadRequest request = new DataPhoneCallEventUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataPhoneCallEvent.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataSystemStats() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineSystemStats.TBL_SYSTEM_STATS, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataSystemStats.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataSystemStatsUploadRequest request = new DataSystemStatsUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataSystemStats.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataWifiScan() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineWifiScan.TBL_WIFI_SCAN, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(DataWifiScan.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataWifiScanUploadRequest request = new DataWifiScanUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(DataWifiScan.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataDeviceNetTraffic() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineDeviceNetTraffic.TBL_NET_TRAFFIC_DEVICE, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(PipelineDeviceNetTraffic.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataDeviceNetTrafficUploadRequest request = new DataDeviceNetTrafficUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(PipelineDeviceNetTraffic.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataAppsNetTraffic() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineAppsNetTraffic.TBL_NET_TRAFFIC_APPS, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(PipelineAppsNetTraffic.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataAppsNetTrafficUploadRequest request = new DataAppsNetTrafficUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(PipelineAppsNetTraffic.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }


    public void uploadDataConnnectionType() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineConnectionType.TBL_CONNECTION_TYPE, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(PipelineConnectionType.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataConnectionTypeUploadRequest request = new DataConnectionTypeUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(PipelineConnectionType.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataDR() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineDR.TBL_DR, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(PipelineDR.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataDRUploadRequest request = new DataDRUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(PipelineDR.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataActivityRecognitionCompare() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineActivityRecognitionCompare.TBL_ACTIVITY_RECOGNITION_COMPARE, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(PipelineActivityRecognitionCompare.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataActivityRecognitionCompareUploadRequest request = new DataActivityRecognitionCompareUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(PipelineActivityRecognitionCompare.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadDataGoogleActivityRecognition() throws Exception {
        List<ContentValues> values = db.getFIFOTuples(PipelineGoogleActivityRecognition.TBL_GOOGLE_ACTIVITY_RECOGNITION, DEFAULT_ROW_NUM);
        boolean upload = DataUploaderPreferences.getInstance(context).checkLastUpload(PipelineGoogleActivityRecognition.class.getSimpleName());

        if (values.size() != 0 && (values.size() >= MIN_DATA_FOR_UPLOAD || upload)) {
            logger.info("Dispatching upload request of {} tuples", values.size());
            DataGoogleActivityRecognitionUploadRequest request = new DataGoogleActivityRecognitionUploadRequest(context, values);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            DataUploaderPreferences.getInstance(context).setLastUpload(PipelineGoogleActivityRecognition.class.getSimpleName(), System.currentTimeMillis());
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadPhoto() throws Exception {
        if (DataUploaderPhotoPreferences.getInstance(context).getPhotoUpload()) {
            File[] files = ImageDescriptorUtility.getImageDescriptors(context);
            if (files != null && files.length > 0) {
                DataPhotoUploadRequest request = new DataPhotoUploadRequest(context, files[0].getName());
                spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
            } else {
                uploading.decrementAndGet();
            }
        } else {
            uploading.decrementAndGet();
        }
    }

    public void uploadQuestionnaire() throws Exception {
        if (DataUploaderQuestionnairePreferences.getInstance(context).getQuestionnaireUpload()) {
            DomainDBHelper dbHelper;
            try {
                dbHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
                RuntimeExceptionDao<DataQuestionnaireFlat, Long> dao = dbHelper.getRuntimeExceptionDao(DataQuestionnaireFlat.class);
                List<DataQuestionnaireFlat> list = dao.queryForAll();
                if (list.size() > 0) {
                    DataQuestionnaireFlatUploadRequest request = new DataQuestionnaireFlatUploadRequest(context, list);
                    spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, this);
                } else {
                    uploading.decrementAndGet();
                }
            } finally {
                OpenHelperManager.releaseHelper();
            }
        } else {
            uploading.decrementAndGet();
        }

    }

    public void uploadLog() throws Exception {
        if (DataUploaderLogPreferences.getInstance(context).getLogUpload()) {
            LogUploadRequest paLogRequest = new LogUploadRequest(context, PA_LOG_FILENAME);
            spiceManager.execute(paLogRequest, paLogRequest.getKey(), DurationInMillis.ALWAYS_EXPIRED, new LogUploadRequestListener(context));

            LogUploadRequest mostLogRequest = new LogUploadRequest(context, MOST_LOG_FILENAME);
            spiceManager.execute(mostLogRequest, mostLogRequest.getKey(), DurationInMillis.ALWAYS_EXPIRED, new LogUploadRequestListener(context));
        }
    }

    public void uploadState() throws Exception {
        if (DataUploaderStatePreferences.getInstance(context).getStateUpload()) {
            StateUploadRequest request = new StateUploadRequest(context);
            spiceManager.execute(request, request.getKey(), DurationInMillis.ALWAYS_EXPIRED, new StateUploadRequestListener(context));
        }
    }

    public void uploadOverWifi() {
        if (uploading.get() == 0) {
            startSpiceManager();
            uploading.set(NUM_DIFF_TYPE_UPLOAD);
            try {
                uploadDataAccelerometerClassifier();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataAccelerometer();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataAppOnScreen();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataBattery();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataBluetooth();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataCell();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataGyroscope();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataInstalledApps();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataLight();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataLocation();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataMagneticField();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataPhoneCallDuration();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataPhoneCallEvent();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataSystemStats();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataWifiScan();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataDeviceNetTraffic();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataAppsNetTraffic();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataActivityRecognitionCompare();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataGoogleActivityRecognition();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadPhoto();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadQuestionnaire();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataDR();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadDataConnnectionType();
            } catch (Exception e) {
                uploading.decrementAndGet();
                logger.warn("Exception uploading data.", e);
            }
            try {
                uploadLog();
            } catch (Exception e) {
                logger.warn("Exception uploading log.", e);
            }
            try {
                uploadState();
            } catch (Exception e) {
                logger.warn("Exception uploading state.", e);
            }
        }
    }

}
