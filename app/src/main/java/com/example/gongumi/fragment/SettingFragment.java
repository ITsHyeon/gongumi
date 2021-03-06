package com.example.gongumi.fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gongumi.R;
import com.example.gongumi.activity.LoginActivity;
import com.example.gongumi.activity.MainActivity;
import com.example.gongumi.adapter.SettingJoinListRecyclerViewAdapter;
import com.example.gongumi.adapter.SettingPostListRecyclerViewAdapter;
import com.example.gongumi.custom.CustomDialog;
import com.example.gongumi.model.CustomDialogInterface;
import com.example.gongumi.model.JoinList;
import com.example.gongumi.model.Option;
import com.example.gongumi.model.Post;
import com.example.gongumi.model.PostList;
import com.example.gongumi.model.User;
import com.example.gongumi.service.GpsTracker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingFragment extends Fragment implements CustomDialogInterface {
    // layout
    public TextView mTvName, mTvAddress;
    private LinearLayout mLiPostList, mLiJoinList, mLiLogOut;
    private CircleImageView mCiChangeProfile;
    private RelativeLayout layout_PostList, layout_JoinList;

    // postList, joinList
    private RecyclerView recyclerView_PostList;
    private RecyclerView recyclerView_JoinList;
    private SettingPostListRecyclerViewAdapter postListRecyclerViewAdapter;
    private SettingJoinListRecyclerViewAdapter joinListRecyclerViewAdapter;
    private ArrayList<PostList> postLists;
    private ArrayList<JoinList> joinLists;

    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");

    // Animation
    Animation animationShow;
    Animation animationHidden;

    // gps
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] GPS_REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    // profile
    private Uri photoUri = null;
    private boolean check = false;
    private static final int PROFILE_PHOTO_REQUEST_CODE = 10;
    private static final int GALLERY_PERMISSIONS_REQUEST_CODE = 11;
    String[] GALLERY_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // database
    private DatabaseReference mDatabase;
    private User user;
    Map<String, Object> values = new HashMap<>();

    // storage
    private StorageReference storageRef;
    private StorageReference pathRef;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_setting, container, false);

        mTvName = view.findViewById(R.id.tvName);
        mTvAddress = view.findViewById(R.id.tvAdress);
        mLiPostList = view.findViewById(R.id.liPostList);
        mLiJoinList = view.findViewById(R.id.liJoinList);
        mLiLogOut = view.findViewById(R.id.liLogout);

        // 공구 게시 목록
        layout_PostList = view.findViewById(R.id.layout_post_write);

        recyclerView_PostList = view.findViewById(R.id.recyclerview_post_list);
        postLists = new ArrayList<>();
        postListRecyclerViewAdapter = new SettingPostListRecyclerViewAdapter(getActivity(), postLists);
        recyclerView_PostList.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_PostList.setAdapter(postListRecyclerViewAdapter);

        // 공구 참여 목록
        layout_JoinList = view.findViewById(R.id.layout_post_join);

        recyclerView_JoinList = view.findViewById(R.id.recyclerview_join_list);
        joinLists = new ArrayList<>();
        joinListRecyclerViewAdapter = new SettingJoinListRecyclerViewAdapter(getActivity(), joinLists);
        recyclerView_JoinList.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_JoinList.setAdapter(joinListRecyclerViewAdapter);

        // animation
        animationShow = AnimationUtils.loadAnimation(getActivity(), R.anim.fromdown);
        animationHidden = AnimationUtils.loadAnimation(getActivity(), R.anim.todown);

        mCiChangeProfile = view.findViewById(R.id.ciChangeProfile);
        Button mBtChangeName = view.findViewById(R.id.btChangeName);
        Button mBtChangeAdress = view.findViewById(R.id.btChangeAdress);

        // database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        storageRef = FirebaseStorage.getInstance("gs://gongumi-6995f.appspot.com").getReference().child("user_profile");
        pathRef = storageRef;

        // TODO : 스토리지에 있는 프로필 가져와서 setting 하기기
        // 기존 값 setting
        Intent intent = getActivity().getIntent();
        user = (User) intent.getSerializableExtra("user");
        storageRef = pathRef.child(user.getId()+".jpg");

        mTvName.setText(user.getName()); // 이름(name)
        mTvAddress.setText(user.getLocation()); // 주소(address)
        Glide.with(getActivity()).load(storageRef).into(mCiChangeProfile);

        mBtChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 커스텀 다이얼로그를 생성한다.
                CustomDialog customNameDialog = new CustomDialog(getActivity(), SettingFragment.this);
                customNameDialog.callFunction("변경할 이름을 입력해주세요.","name", user.getId());
            }
        });


        mBtChangeAdress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkLocationServicesStatus()){
                    showDialogForLocationServiceSetting();
                } else {
                    checkRunTimePermission();
                }
                getGPS();
                Toast.makeText(getContext(),"위치정보가 변경되었습니다.", Toast.LENGTH_LONG).show();
            }
        });

        // TODO : 프로필 이미지 변경
        mCiChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfile();
            }
        });

        // TODO : 공구 게시 목록
        mLiPostList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPostList();
                layout_PostList.setVisibility(View.VISIBLE);
                layout_PostList.startAnimation(animationShow);
                ((MainActivity)getActivity()).changeSettingPrev();
            }
        });

        // TODO : 공구 참여 목록
        mLiJoinList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getJoinList();
                layout_JoinList.setVisibility(View.VISIBLE);
                layout_JoinList.startAnimation(animationShow);
                ((MainActivity)getActivity()).changeSettingPrev();
            }
        });

        // TODO : 로그아웃
        mLiLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDialog();
            }
        });

        return view;

    }
    // GPS 가져오기
    public void getGPS(){
        gpsTracker = new GpsTracker(getContext());

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);
        values.put("location", address);
        FirebaseDatabase.getInstance().getReference().child("User/").child(user.getId()).updateChildren(values);
        mTvAddress.setText(address);
    }
    // GPS 퍼미션 확인
    void checkRunTimePermission() {
        //  런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        // 2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
        if (!(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED)) {
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), GPS_REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getContext(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), GPS_REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), GPS_REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    } // checkRunTimePermission()

    // GPS 주소 변환
    public String getCurrentAddress(double latitude, double longitude) {
        // Geocoder : GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1);
        } catch (IOException ioException) {
            // 네트워크 문제
            Toast.makeText(getContext(), "네트워크가 있는 곳에서 다시 실행해주세요", Toast.LENGTH_LONG).show();
            return "GPS";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getContext(), "잠시 후 다시 실행해주세요", Toast.LENGTH_LONG).show();
            return "GPS";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getContext(), "GPS를 인식하지 못했습니다. 잠시 후 다시 실행해주세요", Toast.LENGTH_LONG).show();
            return "GPS";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).substring(address.getAddressLine(0).indexOf("시") + 1,
                address.getAddressLine(0).indexOf("구") + 1).trim() + " " + address.getThoroughfare();


    } // getCurrentAddress()

    // 여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 GPS 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    } // showDialogForLocationServiceSetting()

    // GPS 활성화 결과
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case GPS_ENABLE_REQUEST_CODE:
                // 사용자가 GPS 활성 시켰는지 검사
                if(checkLocationServicesStatus()){
                    if(checkLocationServicesStatus()){
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;

                // profile
//            case PROFILE_PHOTO_REQUEST_CODE:
//                if(resultCode == RESULT_OK){
//
//                }

        }
    }

    // GPS 서비스 상태 확인
    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // 로그아웃 여부
    public void logoutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public void changeProfile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(Intent.createChooser(intent, "Get Album"), PROFILE_PHOTO_REQUEST_CODE);
    }

    public String DateToString(Post post) {
        String date = "";
        date = format.format(post.getStartDay()) + " ~ " + format.format(post.getEndDay());

        return date;
    }

    public void getPostList() {
        postLists.clear();
        postLists.add(new PostList("공구 물품", "공구 기간"));
        FirebaseDatabase.getInstance().getReference().child("Post").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    Post post = data.getValue(Post.class);

                    if(post.getUserId().equals(user.getId())) {
                        PostList postList = new PostList(post.getProduct(), DateToString(post));
                        postLists.add(postList);
                    }
                }
                postListRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getJoinList() {
        joinLists.clear();
        joinLists.add(new JoinList("공구 물품", "공구 기간", "옵션", "수량"));
        FirebaseDatabase.getInstance().getReference().child("Post").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    final Post post = data.getValue(Post.class);

                    FirebaseDatabase.getInstance().getReference().child("Post").child(post.getStartDay().getTime() + "").child("join").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot data : dataSnapshot.getChildren()) {
                                Option option = data.getValue(Option.class);

                                if(data.getKey().equals(user.getId())) {
                                    JoinList joinList = new JoinList(post.getProduct(), DateToString(post), option.getOpt(), option.getQty());
                                    joinLists.add(joinList);
                                    joinListRecyclerViewAdapter.notifyDataSetChanged();
                                    break;
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPositiveClick() {
        Log.d("after dialog", user.getName());
        // TODO : 바뀐 이름으로 setting하기
        mTvName.setText(user.getName());
    }

    public void changeLayout() {
        if(layout_PostList.getVisibility() == View.VISIBLE) {
            layout_PostList.setVisibility(View.GONE);
            layout_PostList.startAnimation(animationHidden);
        }
        else if(layout_JoinList.getVisibility() == View.VISIBLE) {
            layout_JoinList.setVisibility(View.GONE);
            layout_JoinList.startAnimation(animationHidden);
        }
    }
}
