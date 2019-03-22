package com.pratham.assessment.ui.choose_assessment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.R;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.database.BackupDatabase;
import com.pratham.assessment.discrete_view.DSVOrientation;
import com.pratham.assessment.discrete_view.DiscreteScrollView;
import com.pratham.assessment.discrete_view.ScaleTransformer;
import com.pratham.assessment.domain.Assessment;
import com.pratham.assessment.domain.ECEModel;
import com.pratham.assessment.utilities.Assessment_Constants;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.pratham.assessment.BaseActivity.appDatabase;

public class ECEActivity extends AppCompatActivity implements DiscreteScrollView.OnItemChangedListener, AnswerClickedListener {
    @BindView(R.id.attendance_recycler_view)
    DiscreteScrollView discreteScrollView;

    @BindView(R.id.ll_progress)
    LinearLayout ll_progress;

    @BindView(R.id.btn_submit)
    Button submit;
    List<ECEModel> eceModelList;
    String eceStartTime = "";
    String resId;
    String crlId;
    int[] idArr = {R.id.step_1, R.id.step_2, R.id.step_3, R.id.step_4, R.id.step_5, R.id.step_6, R.id.step_7, R.id.step_8, R.id.step_9, R.id.step_10, R.id.step_11, R.id.step_12, R.id.step_13, R.id.step_14, R.id.step_15, R.id.step_16, R.id.step_17};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ece);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        resId = getIntent().getStringExtra("resId");
        crlId = getIntent().getStringExtra("crlId");

        /*Drawable drawable = ContextCompat.getDrawable(this,R.drawable.ic_submit_assessment);
        drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*2),
                (int)(drawable.getIntrinsicHeight()*2));
        ScaleDrawable sd = new ScaleDrawable(drawable, 0, 0, 0);
        submit.setCompoundDrawables(sd.getDrawable(), null, null, null);
        */
        JSONArray jsonArray = fetchJson("ece.json");
        eceModelList = parseJsonArray(jsonArray);
        //insertJsonToDB(jsonArray);


        eceStartTime = AssessmentApplication.getCurrentDateTime();

        ECEAdapter eceAdapter = new ECEAdapter(this, eceModelList);
        discreteScrollView.setOrientation(DSVOrientation.HORIZONTAL);
        discreteScrollView.addOnItemChangedListener(this);
        discreteScrollView.setItemTransitionTimeMillis(200);
        discreteScrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.5f)
                .build());
        discreteScrollView.setAdapter(eceAdapter);
        eceAdapter.notifyDataSetChanged();
        discreteScrollView.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
            @Override
            public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                for (int i = 0; i < eceModelList.size(); i++) {
                    if (eceModelList.get(i).getIsSelected() > 0) {
                        ImageView view = findViewById(idArr[i]);
//                        view.setBackgroundColor(getResources().getColor(R.color.catcho_primary));
                        view.setBackground(getResources().getDrawable(R.drawable.answered_ece_card));

                    } else {
                        ImageView view = findViewById(idArr[i]);
                        view.setBackground(getResources().getDrawable(R.drawable.ece_top_bg));
//                        view.setBackgroundColor(getResources().getColor(R.color.colorRed));

                    }
                }
                ImageView view = findViewById(idArr[adapterPosition]);
//                view.setBackgroundColor(getResources().getColor(R.color.color_bg));
                view.setBackground(getResources().getDrawable(R.drawable.current_ece_card));

            }
        });
    }

    @Override
    public void onBackPressed() {
        endTestSession();
        super.onBackPressed();
    }

    public void endTestSession() {
        try {
            new AsyncTask<Object, Void, Object>() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    try {
                        String toDateTemp = appDatabase.getSessionDao().getToDate(Assessment_Constants.assessmentSession);

                        if (toDateTemp.equalsIgnoreCase("na")) {
                            appDatabase.getSessionDao().UpdateToDate(Assessment_Constants.assessmentSession, AssessmentApplication.getCurrentDateTime());
                        }
                        BackupDatabase.backup(ECEActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<ECEModel> parseJsonArray(JSONArray jsonArray) {
        List<ECEModel> eceList = new ArrayList<>();
        try {

            for (int i = 0; i < jsonArray.length(); i++) {
                ECEModel eceModel = new ECEModel();
                eceModel.setQuestionId(jsonArray.getJSONObject(i).getString("questionId"));
                eceModel.setQuestion(jsonArray.getJSONObject(i).getString("question"));
                eceModel.setType(jsonArray.getJSONObject(i).getString("type"));
                eceModel.setTitle(jsonArray.getJSONObject(i).getString("title"));
                eceModel.setInstructions(jsonArray.getJSONObject(i).getString("instructions"));
                eceModel.setVideo(jsonArray.getJSONObject(i).getString("video"));
                eceModel.setRating(jsonArray.getJSONObject(i).getString("rating"));
                eceList.add(eceModel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return eceList;
    }

    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {

    }

    public JSONArray fetchJson(String jasonName) {
        JSONArray jsonArr = null;
        try {
            //InputStream is = new FileInputStream(COSApplication.pradigiPath + "/.LLA/English/RC/" + jasonName);
            InputStream is = this.getAssets().open("" + jasonName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonStr = new String(buffer);
            jsonArr = new JSONArray(jsonStr);
            //returnStoryNavigate = jsonObj.getJSONArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArr;
    }

    @OnClick(R.id.btn_submit)
    public void onSubmit() {
        int cnt = 0;
        for (int i = 0; i < eceModelList.size(); i++) {
            if (eceModelList.get(i).getIsSelected() != -1) {
                cnt++;
            } else {
                Toast.makeText(this, "Please complete all questions...", Toast.LENGTH_SHORT).show();
                discreteScrollView.scrollToPosition(i);
                break;
            }
        }
        if (cnt == eceModelList.size()) {
            showConfirmationDialog();
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Do you want to save this assessment?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveAssessmentToDB();
                finish();
            }
        });
        builder.setNegativeButton("Review", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private void saveAssessmentToDB() {
        List<Assessment> assessmentList = new ArrayList<>();

        for (int i = 0; i < eceModelList.size(); i++) {
            Assessment assessment = new Assessment();
            assessment.setResourceIDa(resId);
            assessment.setSessionIDa(Assessment_Constants.assessmentSession);
            assessment.setSessionIDm(Assessment_Constants.currentSession);
            assessment.setQuestionIda(0);
            if (eceModelList.get(i).getIsSelected() == 1)
                assessment.setScoredMarksa(10);
            else if (eceModelList.get(i).getIsSelected() == 2)
                assessment.setScoredMarksa(5);
            assessment.setTotalMarksa(10);
            assessment.setStudentIDa(Assessment_Constants.currentStudentID);
            assessment.setStartDateTimea(eceStartTime);
            assessment.setEndDateTime(AssessmentApplication.getCurrentDateTime());
            assessment.setDeviceIDa(crlId);
            assessment.setLevela(eceModelList.get(i).getIsSelected());
            assessment.setLabel(eceModelList.get(i).getQuestion());
            assessment.setSentFlag(0);
            assessmentList.add(assessment);
        }
        AppDatabase.getDatabaseInstance(this).getAssessmentDao().insertAllAssessments(assessmentList);
        BackupDatabase.backup(this);
    }

    @Override
    public void onAnswerClicked(int position, int answer) {
        eceModelList.get(position).setIsSelected(answer);
        ImageView view = findViewById(idArr[position]);
//        view.setBackgroundColor(getResources().getColor(R.color.catcho_primary));
        view.setBackground(getResources().getDrawable(R.drawable.answered_ece_card));

    }
}
