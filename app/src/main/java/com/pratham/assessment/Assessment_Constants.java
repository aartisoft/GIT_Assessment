package com.pratham.assessment;

public class Assessment_Constants {
    public static String STORING_IN;


    public static final String PRATHAM_KOLIBRI_HOTSPOT = "prathamkolibri";
    public static  String FACILITY_ID = "facility_id";
    public static final String USAGEDATA = "USAGEDATA";
    public static final String BASE_URL = "http://prodigi.openiscool.org/api/pos/";
    public static String RASP_IP = "http://192.168.4.1:8080";
    public static final String SCORE_COUNT = "ScoreCount";
    public static final String STUDENTS = "students";
    public static final String METADATA = "metadata";
    public static final String SESSION = "session";
    public static final String GROUPID = "groupid";
    public static final String GROUPID1 = "group1";
    public static final String GROUPID2 = "group2";
    public static final String GROUPID3 = "group3";
    public static final String GROUPID4 = "group4";
    public static final String GROUPID5 = "group5";

    public static enum URL {
        BROWSE_BY_ID(BASE_URL + "get?id="),
        SEARCH_BY_KEYWORD(BASE_URL + "GetSearchList?"),
        POST_GOOGLE_DATA(BASE_URL + "PostGoogleSignIn"),
        GET_TOP_LEVEL_NODE(BASE_URL + "GetTopLevelNode?lang="),
        DATASTORE_RASPBERY_URL(RASP_IP + "/pratham/datastore/"),
        BROWSE_RASPBERRY_URL(RASP_IP + "/api/contentnode?parent="),
        GET_RASPBERRY_HEADER(RASP_IP + "/api/contentnode?content_id=f9da12749d995fa197f8b4c0192e7b2c"),
        POST_SMART_INTERNET_URL("http://www.rpi.prathamskills.org/api/pushdatasmartphone/post/"),
        POST_TAB_INTERNET_URL("http://www.rpi.prathamskills.org/api/pushdatapradigi/post/"),
        DOWNLOAD_RESOURCE(BASE_URL + "DownloadResource?resid=");
        public static final String SCORE_COUNT = "ScoreCount";
        public static final String STUDENTS = "students";
        public static final String METADATA = "metadata";

        private final String name;

        private URL(String s) {
            name = s;
        }

        public boolean equalsName(String otherName) {
            return (otherName == null) ? false : name.equals(otherName);
        }

        public String toString() {
            return name;
        }

    }

}
