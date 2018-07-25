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

public class Configuration {

    // old pair
    //server key AIzaSyDIQFQl2cOpWhgShNFXAnqnRWSAdc0NPxc
    //public static final String GCM_SENDER_ID = "136262898414";


    //  server key AIzaSyCiKo9ybcd6b3R6TSMVSM6RTxtPf8Kf5Fc
    public static final String GCM_SENDER_ID = "746019368415";


    //Default lifespan (7 days) of a reservation until it is considered expired.
    public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

    public static final int PHOTO_REQUEST_CODE = 12213;
    public static final int QUESTIONNAIRE_REQUEST_CODE = 13333;

    public static final String BASE_URL = "https://pabrain.ing.unibo.it:8443/participact-server/rest/";
//    public static final String BASE_URL = "http://X.X.X.X:8080/participact-server/rest/";
//    public static final String BASE_URL = "http://X.X.X.X:8080/participact-server_gioia_vadruccio_new/rest/";

    public static final int VERSION = 30;

    public static final String BASE_PARAMETER = "version=" + VERSION;

    public static final String LOGIN_URL = BASE_URL + "login?" + BASE_PARAMETER;

    public static final String GCM_REGISTER_URL = BASE_URL + "registergcm?" + BASE_PARAMETER + "&gcmid={gcmid}";

    public static final String GCM_UNREGISTER_URL = BASE_URL + "unregistergcm?" + BASE_PARAMETER;

    public static final String TASK_URL = BASE_URL + "taskflat?" + BASE_PARAMETER + "&type={type}&state={state}";

    public static final String CREATE_TASK_URL = BASE_URL + "taskflat";

    public static final String CREATED_TASK_STATE_URL = BASE_URL + "createdTaskflat/{state}";

    public static final String CREATED_TASK_URL = BASE_URL + "createdTaskflat?valutation={valutation}";

    public static final String ACCEPT_TASK_URL = BASE_URL + "acceptTask?" + BASE_PARAMETER + "&taskId={taskId}";

    public static final String REJECT_TASK_URL = BASE_URL + "rejectTask?" + BASE_PARAMETER + "&taskId={taskId}";

    public static final String COMPLETE_WITH_SUCCESS_TASK_URL = BASE_URL + "completeTask?" + BASE_PARAMETER + "&taskId={taskId}";

    public static final String COMPLETE_WITH_FAILURE_TASK_URL = BASE_URL + "completeTaskWithFailure?" + BASE_PARAMETER + "&taskId={taskId}&sensingProgress={sensingProgress}&photoProgress={photoProgress}&questionnaireProgress={questionnaireProgress}";

    public static final String COLLECTED_GEOBADGE_URL = BASE_URL + "geobadgeCollected?"+ BASE_PARAMETER +  "&taskId={taskId}&actionId={actionId}&timestamp={timestamp}&description={description}";

    public static final String RESULT_DATA_URL = BASE_URL + "upload/";

    public static final String STATISTICS_URL = BASE_URL + "statistics?" + BASE_PARAMETER;

    public static final String CHECK_CLIENT_APP_VERSION = BASE_URL + "clientversion?" + BASE_PARAMETER;

    public static final String TWITTER_URL = BASE_URL + "twitter?" + BASE_PARAMETER;

    public static final String LEADERBOARD_URL = BASE_URL + "leaderboard?type={type}";

    public static final String USER_URL = BASE_URL + "user/{id}";

    public static final String BADGES_FOR_USER_URL = BASE_URL + "badges/user/{id}";

    public static final String FRIENDS_GET_URL = BASE_URL + "user/friends?status={status}";

    public static final String FRIENDS_POST_URL = BASE_URL + "user/friends/{id}";

    public static final String FRIEND_STATUS_URL = BASE_URL + "user/friends/{id}";

    public static final String SOCIAL_PRESENCE_ADD_URL = BASE_URL + "user/social/{socialnetwork}";

    public static final String SOCIAL_PRESENCE_GET_FIENDS_URL = BASE_URL + "user/social/{socialnetwork}/friends";

}
