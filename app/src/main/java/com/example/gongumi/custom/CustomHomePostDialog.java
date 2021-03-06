package com.example.gongumi.custom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gongumi.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

// https://sharp57dev.tistory.com/10
public class CustomHomePostDialog  extends DialogFragment {

    private Context context;
    public TextView text;
    public EditText message;
    String showTxt;
    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Post/");
    private NameInputListener listener;

    public static CustomHomePostDialog newInstance(NameInputListener listener) {
        CustomHomePostDialog fragment = new CustomHomePostDialog();
        fragment.listener = listener;
        return fragment;
    }

    public interface NameInputListener
    {
        void onNameInputComplete(String text);
    }

    public void setText(String text) {
        showTxt = text;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_home_dialog, null);
        text = view.findViewById(R.id.tvInput);
        message = view.findViewById(R.id.etInput);

        text.setText(showTxt);

        builder.setView(view)
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                listener.onNameInputComplete(message
                                        .getText().toString());
                            }
                        }).setNegativeButton("취소", null);
        return builder.create();
    }

    /*// 호출할 다이얼로그 함수를 정의한다.
    public boolean callFunction(String editText, final String key, final String name, final String time){

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dialog = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dialog.setContentView(R.layout.custom_dialog);

        // 커스텀 다이얼로그의 사이즈를 지정한다.
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // 커스텀 다이얼로그를 노출한다.
        dialog.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        text = dialog.findViewById(R.id.tvInput);
        message = dialog.findViewById(R.id.etInput);
        Button btOk = dialog.findViewById(R.id.btOk);
        Button btCancel = dialog.findViewById(R.id.btCancel);

        text.setText(editText);

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // '확인' 버튼 클릭시 메인 액티비티에서 설정한 main_label에
                // 커스텀 다이얼로그에서 입력한 메세지를 대입한다.
                Toast.makeText(context,"\"" + message.getText().toString() + "\"을 입력하였습니다.", Toast.LENGTH_SHORT).show();
                Log.i("dialog info message",message.getText().toString());
                Log.i("dialog info name",name);
                Log.i("dialog info key",key);

                mDatabaseRef = mDatabaseRef.child(time + "/join/" + name);
                values.put(key, message.getText().toString());
                mDatabaseRef.updateChildren(values);

                Log.i("dialog info dbPath", mDatabaseRef.toString());
                Log.i("dialog info values", String.valueOf(values));

                dialog.dismiss();

            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }*/

}

