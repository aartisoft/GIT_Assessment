package com.pratham.assessment.async;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.gson.Gson;
import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.R;
import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.database.BackupDatabase;
import com.pratham.assessment.domain.Assessment;
import com.pratham.assessment.domain.AssessmentPaperForPush;
import com.pratham.assessment.domain.Attendance;
import com.pratham.assessment.domain.Crl;
import com.pratham.assessment.domain.DownloadMedia;
import com.pratham.assessment.domain.Groups;
import com.pratham.assessment.domain.Modal_Log;
import com.pratham.assessment.domain.Modal_RaspFacility;
import com.pratham.assessment.domain.Score;
import com.pratham.assessment.domain.Session;
import com.pratham.assessment.domain.Student;
import com.pratham.assessment.domain.SupervisorData;
import com.pratham.assessment.ui.login.MainActivity;
import com.pratham.assessment.utilities.Assessment_Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/******* This async task is used for data push******/
public class PushDataToServer extends AsyncTask {

    Context context;
    boolean autoPush;
    JSONArray scoreData;
    JSONArray assessmentScoreData;
    JSONArray attendanceData;
    JSONArray studentData;
    JSONArray crlData;
    JSONArray sessionData;
    JSONArray learntWords;
    JSONArray supervisorData;
    JSONArray groupsData;
    JSONArray assessmentData;
    JSONArray assessmentScienceData;
    JSONArray logsData;
    Boolean isConnectedToRasp = false;

    String programID = "";

    boolean dataPushed = false;
    int mediaCnt = 0;
    int videoRecCnt = 0;
    List<DownloadMedia> downloadMediaList = new ArrayList<>();
    List<DownloadMedia> videoRecordingList = new ArrayList<>();

    public PushDataToServer(Context context, boolean autoPush) {

        this.context = context;
        this.autoPush = autoPush;
        scoreData = new JSONArray();
        attendanceData = new JSONArray();
        crlData = new JSONArray();
        sessionData = new JSONArray();
        learntWords = new JSONArray();
        supervisorData = new JSONArray();
        groupsData = new JSONArray();
        logsData = new JSONArray();
        studentData = new JSONArray();
        assessmentData = new JSONArray();
        assessmentScienceData = new JSONArray();

    }


    @Override
    protected Object doInBackground(Object[] objects) {

        List<Score> scoreList = AppDatabase.getDatabaseInstance(context).getScoreDao().getAllPushScores("ece_assessment");
        scoreData = fillScoreData(scoreList);
        List<AssessmentPaperForPush> assessmentScoreList = AppDatabase.getDatabaseInstance(context).getAssessmentPaperForPushDao().getAllAssessmentPapersForPush();
        assessmentScoreData = fillAssessmentScoreData(assessmentScoreList);
        List<Attendance> attendanceList = AppDatabase.getDatabaseInstance(context).getAttendanceDao().getAllPushAttendanceEntries();
        attendanceData = fillAttendanceData(attendanceList);
        List<Student> studentList = AppDatabase.getDatabaseInstance(context).getStudentDao().getAllStudents();
        studentData = fillStudentData(studentList);
        List<Crl> crlList = AppDatabase.getDatabaseInstance(context).getCrlDao().getAllCrls();
        crlData = fillCrlData(crlList);
        List<Session> sessionList = AppDatabase.getDatabaseInstance(context).getSessionDao().getAllNewSessions();
        sessionData = fillSessionData(sessionList);
/*        List<LearntWords> learntWordsList = AppDatabase.getDatabaseInstance(context).getLearntWordDao().getAllData();
        learntWords = fillLearntWordsData(learntWordsList);
    */
        List<SupervisorData> supervisorDataList = AppDatabase.getDatabaseInstance(context).getSupervisorDataDao().getAllSupervisorData();
        supervisorData = fillSupervisorData(supervisorDataList);
        List<Modal_Log> logsList = AppDatabase.getDatabaseInstance(context).getLogsDao().getPushAllLogs();
        logsData = fillLogsData(logsList);
        List<Assessment> assessmentList = AppDatabase.getDatabaseInstance(context).getAssessmentDao().getAllECEAssessment();
        assessmentData = fillAssessmentData(assessmentList);
        List<Assessment> scienceAssessmentList = AppDatabase.getDatabaseInstance(context).getAssessmentDao().getAllScienceAssessment();
        assessmentScienceData = fillAssessmentData(scienceAssessmentList);

        List<Groups> groupsList = AppDatabase.getDatabaseInstance(context).getGroupsDao().getAllGroups();
        groupsData = fillGroupsData(groupsList);

        JSONObject rootJson = new JSONObject();

        try {
            Gson gson = new Gson();
            //iterate through all new sessions
            JSONObject metadataJson = new JSONObject();
/*            JSONArray sessionArray = new JSONArray();
            List<Session> newSessions = AppDatabase.getDatabaseInstance(context).getSessionDao().getAllNewSessions();

            for (Session session : newSessions) {
                //fetch all logs
                JSONArray logArray = new JSONArray();
                List<Modal_Log> allLogs = AppDatabase.getDatabaseInstance(context).getLogsDao().getAllLogs(session.getSessionID());
                for (Modal_Log log : allLogs)
                    logArray.put(new JSONObject(gson.toJson(log)));
                //fetch attendance
                JSONArray attendanceArray = new JSONArray();
                List<Attendance> newAttendance = AppDatabase.getDatabaseInstance(context).getAttendanceDao().getNewAttendances(session.getSessionID());
                for (Attendance att : newAttendance) {
                    attendanceArray.put(new JSONObject(gson.toJson(att)));
                }
                //fetch Scores & convert to Json Array
                JSONArray scoreArray = new JSONArray();
                List<Score> newScores = AppDatabase.getDatabaseInstance(context).getScoreDao().getAllNewScores(session.getSessionID());
                for (Score score : newScores) {
                    scoreArray.put(new JSONObject(gson.toJson(score)));
                }
                // fetch Session Data
                JSONObject sessionJson = new JSONObject();
                sessionJson.put("SessionID", session.getSessionID());
                sessionJson.put("fromDate", session.getFromDate());
                sessionJson.put("toDate", session.getToDate());
                sessionArray.put(sessionJson);

                JSONArray studentArray = new JSONArray();
                if (!COSApplication.isTablet) {
                    List<Student> newStudents = AppDatabase.getDatabaseInstance(context).getStudentDao().getAllNewStudents();
                    for (Student std : newStudents)
                        studentArray.put(new JSONObject(gson.toJson(std)));
                }
                if (!COSApplication.isTablet)
                    rootJson.put(COS_Constants.STUDENTS, studentArray);
                rootJson.put(COS_Constants.SESSION, sessionArray);
                rootJson.put(COS_Constants.ATTENDANCE, attendanceArray);
                rootJson.put(COS_Constants.SCORE, scoreArray);
                rootJson.put(COS_Constants.LOGS, logArray);
                rootJson.put(COS_Constants.ASSESSMENT, assessmentData);
                rootJson.put(COS_Constants.SUPERVISOR, supervisorData);
                rootJson.put(COS_Constants.LEARNTWORDS, learntWords);

            }*/
           /* List<com.pratham.cityofstories.domain.Status> metadata = AppDatabase.getDatabaseInstance(context).getStatusDao().getAllStatuses();
            for (com.pratham.cityofstories.domain.Status status : metadata) {
                metadataJson.put(status.getStatusKey(), status.getValue());
                if (status.getStatusKey().equalsIgnoreCase("programId"))
                    programID = status.getValue();
            }
            metadataJson.put(COS_Constants.SCORE_COUNT, (metadata.size() > 0) ? metadata.size() : 0);
            rootJson.put(COS_Constants.METADATA, metadataJson);
*/


            if (AssessmentApplication.wiseF.isDeviceConnectedToWifiNetwork()) {
                if (AssessmentApplication.wiseF.isDeviceConnectedToSSID(Assessment_Constants.PRATHAM_KOLIBRI_HOTSPOT)) {
                    try {
                        JSONObject object = new JSONObject();
                        object.put("username", "pratham");
                        object.put("password", "pratham");
/*                    new PD_ApiRequest(context, ContentPresenterImpl.this)
                            .getacilityIdfromRaspberry(COS_Constants.FACILITY_ID, COS_Constants.RASP_IP + "/api/session/", object);*/
                        AndroidNetworking.post(Assessment_Constants.RASP_IP + "/api/session/")
                                .addHeaders("Content-Type", "application/json")
                                .addJSONObjectBody(object)
                                .build()
                                .getAsString(new StringRequestListener() {
                                    @Override
                                    public void onResponse(String response) {
                                        Gson gson = new Gson();
                                        Modal_RaspFacility facility = gson.fromJson(response, Modal_RaspFacility.class);
                                        FastSave.getInstance().saveString(Assessment_Constants.FACILITY_ID, facility.getFacilityId());
                                        isConnectedToRasp = true;
                                    }

                                    @Override
                                    public void onError(ANError anError) {
//                            apiResult.notifyError(requestType/*, null*/);
                                        isConnectedToRasp = false;
                                        Log.d("Error::", anError.getErrorDetail());
                                        Log.d("Error::", anError.getMessage());
                                        Log.d("Error::", anError.getResponse().toString());
                                    }
                                });
                    } catch (Exception e) {
                        isConnectedToRasp = false;
                        e.printStackTrace();
                    }
                }
            } else isConnectedToRasp = false;
            programID = AppDatabase.appDatabase.getStatusDao().getValue("programId");


        } catch (Exception e) {
            e.printStackTrace();
        }
//        JSONObject requestJsonObject = generateRequestString(scoreData, attendanceData, sessionData, learntWords, supervisorData, logsData, assessmentData, studentData);
        JSONObject requestJsonObjectScience = generateRequestString(scoreData,assessmentScoreData, attendanceData, sessionData, learntWords, supervisorData, logsData, assessmentScienceData, studentData);

        //        if (checkEmptyness(requestString))

        if (!isConnectedToRasp) {
//            pushDataToServer(context, requestJsonObject, AssessmentApplication.uploadDataUrl);
            pushDataScienceToServer(context, requestJsonObjectScience, AssessmentApplication.uploadScienceUrl);
            //todo uncomment createMediaFileToPush();
            // createMediaFileToPush();
        } else {
            pushDataToRaspberry("" + Assessment_Constants.URL.DATASTORE_RASPBERY_URL.toString(),
                    "" + requestJsonObjectScience, programID, Assessment_Constants.USAGEDATA);
        }


        return null;
    }

    private void createMediaFileToPush() {
        String filePath = downloadMediaList.get(mediaCnt).getPhotoUrl();
        if (!filePath.equalsIgnoreCase("")) {
            File file = new File(filePath);
            pushMediaToServer(AssessmentApplication.uploadScienceFilesUrl, file, "answerVideo");

        }
    }

    private void pushMediaToServer(String url, File file, final String videoType) {
        AndroidNetworking.upload(url)
                .addMultipartFile(videoType, file)
//                .addMultipartParameter("key", "value")
//                .setTag("uploadTest")
//                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress

                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        if (videoType.equalsIgnoreCase("answerVideo")) {
                            mediaCnt++;
                            if (mediaCnt < downloadMediaList.size())
                                createMediaFileToPush();
                            else Toast.makeText(context, "Answer videos pushed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            videoRecCnt++;
                            if (videoRecCnt < videoRecordingList.size())
                                createMediaFileToPush();
                            else Toast.makeText(context, "Video recordings pushed successfully", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Toast.makeText(context, "video monitoring Media push failed", Toast.LENGTH_SHORT).show();
                        PushDataToServer.this.mediaCnt++;
                        if (PushDataToServer.this.mediaCnt < downloadMediaList.size())
                            createMediaFileToPush();
                    }
                });
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }

    private boolean checkEmptyness(String requestString) {
        try {
            JSONObject jsonObject = new JSONObject(requestString);
            JSONObject jsonObjectSession = jsonObject.getJSONObject("session");

            if (jsonObjectSession.getJSONArray("scoreData").length() > 0 ||
                    jsonObjectSession.getJSONArray("attendanceData").length() > 0 ||
                    jsonObjectSession.getJSONArray("sessionsData").length() > 0 ||
                    jsonObjectSession.getJSONArray("learntWordsData").length() > 0 ||
                    jsonObjectSession.getJSONArray("logsData").length() > 0 ||
                    jsonObjectSession.getJSONArray("assessmentData").length() > 0 ||
                    jsonObjectSession.getJSONArray("supervisor").length() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private JSONObject generateRequestString(JSONArray eceScoreData, JSONArray assessmentScoreData, JSONArray attendanceData, JSONArray sessionData, JSONArray learntWordsData, JSONArray supervisorData, JSONArray logsData, JSONArray assessmentData, JSONArray studentData) {
        String requestString = "";
        JSONObject rootJson = new JSONObject();

        try {
            JSONObject sessionObj = new JSONObject();
            JSONObject metaDataObj = new JSONObject();
            metaDataObj.put("ScoreCount", assessmentScoreData.length());

            metaDataObj.put("CRLID", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("CRLID"));
            metaDataObj.put("group1", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("group1"));
            metaDataObj.put("group2", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("group2"));
            metaDataObj.put("group3", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("group3"));
            metaDataObj.put("group4", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("group4"));
            metaDataObj.put("group5", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("group5"));
            metaDataObj.put("DeviceId", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("DeviceId"));
            metaDataObj.put("DeviceName", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("DeviceName"));
            metaDataObj.put("ActivatedDate", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("ActivatedDate"));
            metaDataObj.put("village", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("village"));
            metaDataObj.put("ActivatedForGroups", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("ActivatedForGroups"));
            metaDataObj.put("SerialID", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("SerialID"));
            metaDataObj.put("gpsFixDuration", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("gpsFixDuration"));
            metaDataObj.put("prathamCode", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("prathamCode"));
            metaDataObj.put("programId", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("programId"));
            metaDataObj.put("WifiMAC", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("wifiMAC"));
            metaDataObj.put("apkType", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("apkType"));
            metaDataObj.put("appName", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("appName"));
            metaDataObj.put("apkVersion", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("apkVersion"));
            metaDataObj.put("GPSDateTime", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("GPSDateTime"));
            metaDataObj.put("Latitude", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("Latitude"));
            metaDataObj.put("Longitude", AppDatabase.getDatabaseInstance(context).getStatusDao().getValue("Longitude"));

            sessionObj.put("scoreData", assessmentScoreData);
            sessionObj.put("eceScoreData", eceScoreData);
/*            if (!COS_Constants.SD_CARD_Content)
                sessionObj.put("studentData", studentData);*/
            sessionObj.put("attendanceData", attendanceData);
            sessionObj.put("sessionsData", sessionData);
            sessionObj.put("learntWordsData", learntWordsData);
            sessionObj.put("logsData", logsData);
            sessionObj.put("assessmentData", assessmentData);
            sessionObj.put("supervisor", supervisorData);

           /* requestString = "{ \"session\": " + sessionObj +
                    ", \"metadata\": " + metaDataObj +
                    "}";
*/
            rootJson.put("session", sessionObj);
            rootJson.put("metadata", metaDataObj);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return rootJson;
    }

    private JSONArray fillSessionData(List<Session> sessionList) {
        JSONArray newSessionsData = new JSONArray();
        JSONObject _sessionObj;
        try {
            for (int i = 0; i < sessionList.size(); i++) {
                _sessionObj = new JSONObject();
                _sessionObj.put("SessionID", sessionList.get(i).getSessionID());
                _sessionObj.put("fromDate", sessionList.get(i).getFromDate());
                _sessionObj.put("toDate", sessionList.get(i).getToDate());
                newSessionsData.put(_sessionObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newSessionsData;
    }
/*
    private JSONArray fillLearntWordsData(List<LearntWords> learntWordsList) {
        JSONArray newLearntWords = new JSONArray();
        JSONObject _learntWordsObj;
        try {
            for (int i = 0; i < learntWordsList.size(); i++) {
                _learntWordsObj = new JSONObject();
                _learntWordsObj.put("studentId", learntWordsList.get(i).getStudentId());
                _learntWordsObj.put("synId", learntWordsList.get(i).getSynId());
                _learntWordsObj.put("wordUUId", learntWordsList.get(i).getWordUUId());
                _learntWordsObj.put("word", learntWordsList.get(i).getWord());
                newLearntWords.put(_learntWordsObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newLearntWords;
    }
*/

    private JSONArray fillCrlData(List<Crl> crlsList) {

        JSONArray crlsData = new JSONArray();
        JSONObject _crlObj;
        try {
            for (int i = 0; i < crlsList.size(); i++) {
                _crlObj = new JSONObject();
                _crlObj.put("CRLId", crlsList.get(i).getCRLId());
                _crlObj.put("FirstName", crlsList.get(i).getFirstName());
                _crlObj.put("LastName", crlsList.get(i).getLastName());
                _crlObj.put("UserName", crlsList.get(i).getUserName());
                _crlObj.put("UserName", crlsList.get(i).getUserName());
                _crlObj.put("Password", crlsList.get(i).getPassword());
                _crlObj.put("ProgramId", crlsList.get(i).getProgramId());
                _crlObj.put("Mobile", crlsList.get(i).getMobile());
                _crlObj.put("State", crlsList.get(i).getState());
                _crlObj.put("Email", crlsList.get(i).getEmail());
                _crlObj.put("CreatedBy", crlsList.get(i).getCreatedBy());
                _crlObj.put("newCrl", !crlsList.get(i).isNewCrl());
                crlsData.put(_crlObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return crlsData;
    }

    private JSONArray fillStudentData(List<Student> studentList) {
        JSONArray studentData = new JSONArray();
        JSONObject _studentObj;
        try {
            for (int i = 0; i < studentList.size(); i++) {
                _studentObj = new JSONObject();
                _studentObj.put("StudentID", studentList.get(i).getStudentID());
                _studentObj.put("StudentUID", studentList.get(i).getStudentUID());
                _studentObj.put("FirstName", studentList.get(i).getFirstName());
                _studentObj.put("MiddleName", studentList.get(i).getMiddleName());
                _studentObj.put("LastName", studentList.get(i).getLastName());
                _studentObj.put("FullName", studentList.get(i).getFullName());
                _studentObj.put("Gender", studentList.get(i).getGender());
                _studentObj.put("regDate", studentList.get(i).getRegDate());
                _studentObj.put("Age", studentList.get(i).getAge());
                _studentObj.put("villageName", studentList.get(i).getVillageName());
                _studentObj.put("newFlag", studentList.get(i).getNewFlag());
                _studentObj.put("DeviceId", studentList.get(i).getDeviceId());
                studentData.put(_studentObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return studentData;
    }

    private JSONArray fillAttendanceData(List<Attendance> attendanceList) {
        JSONArray attendanceData = new JSONArray();
        JSONObject _obj;
        try {
            for (int i = 0; i < attendanceList.size(); i++) {
                _obj = new JSONObject();
                Attendance _attendance = attendanceList.get(i);
                _obj.put("attendanceID", _attendance.getAttendanceID());
                _obj.put("SessionID", _attendance.getSessionID());
                _obj.put("StudentID", _attendance.getStudentID());
                attendanceData.put(_obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return attendanceData;
    }

    private JSONArray fillScoreData(List<Score> scoreList) {
        JSONArray scoreData = new JSONArray();
        JSONObject _obj;
        try {
            for (int i = 0; i < scoreList.size(); i++) {
                _obj = new JSONObject();
                Score _score = scoreList.get(i);
//                _obj.put("ScoreId", _score.getScoreId());
                _obj.put("SessionID", _score.getSessionID());
                _obj.put("StudentID", _score.getStudentID());
                _obj.put("DeviceID", _score.getDeviceID());
                _obj.put("ResourceID", _score.getResourceID());
                _obj.put("QuestionId", _score.getQuestionId());
                _obj.put("ScoredMarks", _score.getScoredMarks());
                _obj.put("TotalMarks", _score.getTotalMarks());
                _obj.put("StartDateTime", _score.getStartDateTime());
                _obj.put("EndDateTime", _score.getEndDateTime());
                _obj.put("Level", _score.getLevel());
                _obj.put("Label", _score.getLabel());
//                _obj.put("isAttempted", _score.getIsAttempted());
//                _obj.put("isCorrect", _score.getIsCorrect());
//                _obj.put("userAnswer", _score.getUserAnswer());
//                _obj.put("examId", _score.getExamId());
                scoreData.put(_obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return scoreData;
    }

    private JSONArray fillAssessmentScoreData(List<AssessmentPaperForPush> paperList) {
        JSONArray paperData = new JSONArray();
        JSONObject _obj_paper = null;
        JSONArray scoreData = new JSONArray();

        JSONObject _obj_score;
        try {
            for (int p = 0; p < paperList.size(); p++) {
                _obj_paper = new JSONObject();
                AssessmentPaperForPush _paper = paperList.get(p);
                List<Score> scoreList = AppDatabase.getDatabaseInstance(context).getScoreDao().getAllNewScores(paperList.get(p).getSessionID());
                if (scoreList.size() > 0) {
                    _obj_paper.put("languageId", _paper.getLanguageId());
                    _obj_paper.put("subjectId", _paper.getSubjectId());
                    _obj_paper.put("examId", _paper.getExamId());
                    _obj_paper.put("paperId", _paper.getPaperId());
                    _obj_paper.put("paperStartTime", _paper.getPaperStartTime());
                    _obj_paper.put("paperEndTime", _paper.getPaperEndTime());
                    _obj_paper.put("outOfMarks", _paper.getOutOfMarks());
                    _obj_paper.put("totalMarks", _paper.getTotalMarks());
                    _obj_paper.put("studentId", _paper.getStudentId());
                    _obj_paper.put("SessionID", _paper.getSessionID());
                    DownloadMedia video = new DownloadMedia();
                    video.setPaperId(_paper.getPaperId());
                    video.setPhotoUrl(Environment.getExternalStorageDirectory() + "/.Assessment/Content/videoMonitoring/" + _paper.getPaperId() + ".mp4");
                    videoRecordingList.add(video);
                    scoreData = new JSONArray();
                    for (int i = 0; i < scoreList.size(); i++) {
                        _obj_score = new JSONObject();
                        Score _score = scoreList.get(i);
//                _obj.put("ScoreId", _score.getScoreId());
                        _obj_score.put("SessionID", _score.getSessionID());
                        _obj_score.put("StudentID", _score.getStudentID());
                        _obj_score.put("DeviceID", _score.getDeviceID());
                        _obj_score.put("ResourceID", _score.getResourceID());
                        _obj_score.put("QuestionId", _score.getQuestionId());
                        _obj_score.put("ScoredMarks", _score.getScoredMarks());
                        _obj_score.put("TotalMarks", _score.getTotalMarks());
                        _obj_score.put("StartDateTime", _score.getStartDateTime());
                        _obj_score.put("EndDateTime", _score.getEndDateTime());
                        _obj_score.put("questionLevel", _score.getLevel());
                        _obj_score.put("questionLabel", _score.getLabel());
                        _obj_score.put("isAttempted", _score.getIsAttempted());
                        _obj_score.put("isCorrect", _score.getIsCorrect());
                        _obj_score.put("userAnswer", _score.getUserAnswer());
                        _obj_score.put("paperId", _score.getPaperId());
                        downloadMediaList.addAll(AppDatabase.getDatabaseInstance(context).getDownloadMediaDao().getMediaByQidAndPaperId(_score.getQuestionId() + "", _score.getPaperId()));
//                    _obj_score.put("examId", _score.getExamId());
                        scoreData.put(_obj_score);
                    }
                }
                _obj_paper.put("assessmentScoreData", scoreData);
                paperData.put(_obj_paper);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return paperData;
    }

    private JSONArray fillSupervisorData(List<SupervisorData> supervisorDataList) {
        JSONArray supervisorData = new JSONArray();
        JSONObject _supervisorDataObj;
        try {
            for (int i = 0; i < supervisorDataList.size(); i++) {
                _supervisorDataObj = new JSONObject();
                SupervisorData supervisorDataTemp = supervisorDataList.get(i);
                _supervisorDataObj.put("sId", supervisorDataTemp.getsId());
                _supervisorDataObj.put("assessmentSessionId", supervisorDataTemp.getAssessmentSessionId());
                _supervisorDataObj.put("supervisorId", supervisorDataTemp.getSupervisorId());
                _supervisorDataObj.put("supervisorName", supervisorDataTemp.getSupervisorName());
                _supervisorDataObj.put("supervisorPhoto", supervisorDataTemp.getSupervisorPhoto());

                supervisorData.put(_supervisorDataObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return supervisorData;
    }

    private JSONArray fillLogsData(List<Modal_Log> logsList) {
        JSONArray logsData = new JSONArray();
        JSONObject _logsObj;
        try {
            for (int i = 0; i < logsList.size(); i++) {
                _logsObj = new JSONObject();
                Modal_Log modal_log = logsList.get(i);
                _logsObj.put("logId", modal_log.getLogId());
                _logsObj.put("deviceId", modal_log.getDeviceId());
                _logsObj.put("currentDateTime", modal_log.getCurrentDateTime());
                _logsObj.put("errorType", modal_log.getErrorType());
                _logsObj.put("exceptionMessage", modal_log.getExceptionMessage());
                _logsObj.put("exceptionStackTrace", modal_log.getExceptionStackTrace());
                _logsObj.put("groupId", modal_log.getGroupId());
                _logsObj.put("LogDetail", modal_log.getLogDetail());
                _logsObj.put("methodName", modal_log.getMethodName());

                logsData.put(_logsObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return logsData;
    }

    private JSONArray fillAssessmentData(List<Assessment> assessmentList) {
        JSONArray assessmentData = new JSONArray();
        JSONObject _assessmentobj;
        try {
            for (int i = 0; i < assessmentList.size(); i++) {
                _assessmentobj = new JSONObject();
                Assessment _Assessment = assessmentList.get(i);
                _assessmentobj.put("DeviceIDa", _Assessment.getDeviceIDa());
                _assessmentobj.put("EndDateTimea", _Assessment.getEndDateTime());
                _assessmentobj.put("Labela", _Assessment.getLabel());
                _assessmentobj.put("Levela", _Assessment.getLevela());
                _assessmentobj.put("QuestionIda", _Assessment.getQuestionIda());
                _assessmentobj.put("ResourceIDa", _Assessment.getResourceIDa());
                _assessmentobj.put("ScoredMarksa", _Assessment.getScoredMarksa());
                _assessmentobj.put("ScoreIda", _Assessment.getScoreIda());
                _assessmentobj.put("SessionIDa", _Assessment.getSessionIDa());
                _assessmentobj.put("SessionIDm", _Assessment.getSessionIDm());
                _assessmentobj.put("StartDateTimea", _Assessment.getStartDateTimea());
                _assessmentobj.put("StudentIDa", _Assessment.getStudentIDa());
                _assessmentobj.put("TotalMarksa", _Assessment.getTotalMarksa());


                assessmentData.put(_assessmentobj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return assessmentData;
    }

    private JSONArray fillGroupsData(List<Groups> groupsList) {
        JSONArray groupsData = new JSONArray();
        JSONObject _groupsObj;
        try {
            for (int i = 0; i < groupsList.size(); i++) {
                _groupsObj = new JSONObject();
                Groups group = groupsList.get(i);
                _groupsObj.put("GroupId", group.getGroupId());
                _groupsObj.put("DeviceId", group.getDeviceId());
                _groupsObj.put("GroupCode", group.getGroupCode());
                _groupsObj.put("GroupName", group.getGroupName());
                _groupsObj.put("ProgramId", group.getProgramId());
                _groupsObj.put("SchoolName", group.getSchoolName());
                _groupsObj.put("VillageId", group.getVillageId());
                _groupsObj.put("VIllageName", group.getVIllageName());

                groupsData.put(_groupsObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return groupsData;
    }

    private void pushDataToServer(final Context context, JSONObject requestJsonObject, String url) {
        try {
//            JSONObject jsonArrayData = new JSONObject(data);

            AndroidNetworking.post(url)
                    .addHeaders("Content-Type", "application/json")
                    .addJSONObjectBody(requestJsonObject)
                    .build()
                    .getAsString(new StringRequestListener() {

                        @Override
                        public void onResponse(String response) {
                            Log.d("PUSH_STATUS", "Data pushed successfully");
                            dataPushed = true;
                            if (!autoPush) {
                               /* new AlertDialog.Builder(context)
                                        .setMessage("Data pushed successfully")
                                        .setCancelable(false)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                ((MainActivity) context).onResponseGet();
                                            }
                                        }).create().show();*/
                            }
//                            setPushFlag();
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.d("PUSH_STATUS", "Data push failed");
                            dataPushed = false;
                           /* if (!autoPush) {
                                new AlertDialog.Builder(context)
                                        .setMessage("Data push failed")
                                        .setCancelable(false)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                ((MainActivity) context).onResponseGet();
                                            }
                                        }).create().show();
                            }*/
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void pushDataScienceToServer(final Context context, JSONObject requestJsonObject, String url) {
        try {
//            JSONObject jsonArrayData = new JSONObject(data);

            AndroidNetworking.post(url)
                    .addHeaders("Content-Type", "application/json")
                    .addJSONObjectBody(requestJsonObject)
                    .build()
                    .getAsString(new StringRequestListener() {

                        @Override
                        public void onResponse(String response) {
                            Log.d("PUSH_STATUS", "Data pushed successfully");
                            Drawable icon = context.getResources().getDrawable(R.drawable.ic_check);
                            if (!autoPush) {
                                CreateFilesforVideoMonitoring();
                                String msg = "Data pushed successfully";
                              /*  if (!dataPushed) {
                                    icon = context.getResources().getDrawable(R.drawable.ic_warning);
                                    msg = "Science data pushed successfully. ECE data push failed";
                                }*/

                                new AlertDialog.Builder(context)
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setIcon(icon)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                ((MainActivity) context).onResponseGet();
                                            }
                                        }).create().show();
                            }
                            setPushFlag();
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.d("PUSH_STATUS", "Science Data push failed");
                            if (!autoPush) {
                                String msg = "Science Data push failed";
                                if (dataPushed) {
                                    msg = "Other data pushed successfully.Science data push failed.";
                                }

                                new AlertDialog.Builder(context)
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                ((MainActivity) context).onResponseGet();
                                            }
                                        }).create().show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CreateFilesforVideoMonitoring() {
        if (videoRecordingList.size() > 0) {
            String filePath = videoRecordingList.get(videoRecCnt).getPhotoUrl();
            if (!filePath.equalsIgnoreCase("")) {
                try {
                    File file = new File(filePath);
                    if (file.exists())
                        pushMediaToServer(AssessmentApplication.uploadScienceFilesUrl, file, "videoMonitoring");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getAuthHeader() {
        String encoded = Base64.encodeToString(("pratham" + ":" + "pratham").getBytes(), Base64.NO_WRAP);
        return "Basic " + encoded;
    }

    public void pushDataToRaspberry(/*final String requestType, */String url, String data,
                                                                  String filter_name, String table_name) {
        AndroidNetworking.post(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", getAuthHeader())
                .addBodyParameter("filter_name", filter_name)
                .addBodyParameter("table_name", table_name)
                .addBodyParameter("facility", FastSave.getInstance().getString(Assessment_Constants.FACILITY_ID, ""))
                .addBodyParameter("data", data)
                .setExecutor(Executors.newSingleThreadExecutor())
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!autoPush) {
                            dataPushed = true;
                        }
                        setPushFlag();
                        BackupDatabase.backup(AssessmentApplication.getInstance());
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (!autoPush) {
                            new AlertDialog.Builder(context)
                                    .setMessage("Data push failed")
                                    .setCancelable(false)
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            ((MainActivity) context).onResponseGet();

                                        }
                                    }).create().show();
                        }
                        Log.d("Error::", anError.getErrorDetail());
                        Log.d("Error::", anError.getMessage());
                        Log.d("Error::", anError.getResponse().toString());
                    }
                });
    }


    private void setPushFlag() {
        AppDatabase.getDatabaseInstance(context).getLogsDao().setSentFlag();
        AppDatabase.getDatabaseInstance(context).getSessionDao().setSentFlag();
        AppDatabase.getDatabaseInstance(context).getAttendanceDao().setSentFlag();
        AppDatabase.getDatabaseInstance(context).getScoreDao().setSentFlag();
        AppDatabase.getDatabaseInstance(context).getAssessmentDao().setSentFlag();
//        AppDatabase.getDatabaseInstance(context).getSupervisorDataDao().setSentFlag();
        AppDatabase.getDatabaseInstance(context).getStudentDao().setSentFlag();
        AppDatabase.getDatabaseInstance(context).getAssessmentPaperForPushDao().setSentFlag();
//        AppDatabase.getDatabaseInstance(context).getLearntWordDao().setSentFlag();

    }
}
