package com.pratham.assessment.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.pratham.assessment.dao.AssessmentDao;
import com.pratham.assessment.dao.AttendanceDao;
import com.pratham.assessment.dao.CrlDao;
import com.pratham.assessment.dao.GroupDao;
import com.pratham.assessment.dao.LogDao;
import com.pratham.assessment.dao.ScoreDao;
import com.pratham.assessment.dao.SessionDao;
import com.pratham.assessment.dao.StatusDao;
import com.pratham.assessment.dao.StudentDao;
import com.pratham.assessment.dao.VillageDao;
import com.pratham.assessment.domain.Assessment;
import com.pratham.assessment.domain.Attendance;
import com.pratham.assessment.domain.ContentTable;
import com.pratham.assessment.domain.Crl;
import com.pratham.assessment.domain.Groups;
import com.pratham.assessment.domain.Modal_Log;
import com.pratham.assessment.domain.Score;
import com.pratham.assessment.domain.Session;
import com.pratham.assessment.domain.Status;
import com.pratham.assessment.domain.Student;
import com.pratham.assessment.domain.Village;


@Database(entities = {Crl.class,  Student.class, Score.class, Session.class, Attendance.class, Status.class,  Village.class, Groups.class, Assessment.class, Modal_Log.class, ContentTable.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase appDatabase;

    public static final String DB_NAME = "assessment_database";

    public abstract CrlDao getCrlDao();

    public abstract StudentDao getStudentDao();

    public abstract ScoreDao getScoreDao();

    public abstract AssessmentDao getAssessmentDao();

    public abstract SessionDao getSessionDao();

    public abstract AttendanceDao getAttendanceDao();

    public abstract VillageDao getVillageDao();

    public abstract GroupDao getGroupsDao();

    public abstract LogDao getLogsDao();

    public abstract ContentTable getContentTableDao();

    //new
    public abstract StatusDao getStatusDao();


   /* public static AppDatabase getDatabaseInstance(Context context) {
        if(appDatabase!=null) {
            appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class, AppDatabase.DB_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return appDatabase;
    }*/

    public static AppDatabase getDatabaseInstance(Context context) {
        if (appDatabase == null)
            appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "assessment_database").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        return appDatabase;
    }
}
