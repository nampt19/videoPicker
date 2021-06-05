package com.nampt.videppickerlib;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

import hb.xvideoplayer.MxVideoPlayer;
import hb.xvideoplayer.MxVideoPlayerWidget;

public class MainActivity extends AppCompatActivity {
    Button btnPickVideo;
    FrameLayout containerVideo;
    MxVideoPlayerWidget videoWritePost;
    VideoBottomSheetDialogFragment videoBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControl();
        addEvent();
    }

    private void addControl() {
        btnPickVideo = findViewById(R.id.btn_pick);
        containerVideo = findViewById(R.id.container_video);
        videoWritePost = findViewById(R.id.video_view);
    }

    private void addEvent() {
        btnPickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissionAndSelectMedia();
            }
        });
    }

    private void requestPermissionAndSelectMedia() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                selectVideoFromBottomSheet();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Quyền bị từ chối\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("Nếu bạn không cấp quyền, bạn sẽ không thể sử dụng dịch vụ\n\nLàm ơn vào mục [Cài đặt] > [Quyền]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }

    private void selectVideoFromBottomSheet() {

        videoBottomSheet = new VideoBottomSheetDialogFragment(
                new ItemVideoListener() {
                    @Override
                    public void onVideoSelected(String path, long size) {
                        //remove bottom sheet after selected item
                        Fragment f1 = getSupportFragmentManager().findFragmentByTag("VIDEO-TAG");
                        getSupportFragmentManager().beginTransaction().remove(f1).commit();
                        containerVideo.setVisibility(View.VISIBLE);
                        videoWritePost.startPlay(path, MxVideoPlayer.SCREEN_LAYOUT_NORMAL, " ");
                    }
                });
        videoBottomSheet.show(getSupportFragmentManager(), "VIDEO-TAG");
    }

    @Override
    protected void onDestroy() {
        MxVideoPlayer.releaseAllVideos();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (MxVideoPlayer.backPress()) return;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VideoBottomSheetAdapter.REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {

            if (videoBottomSheet.isAdded())
                videoBottomSheet.dismiss();

            Uri videoUri = data.getData();
            Video video = getVideoEntityFromURI(videoUri);

            containerVideo.setVisibility(View.VISIBLE);
                videoWritePost.startPlay(video.getPath(),
                        MxVideoPlayer.SCREEN_LAYOUT_NORMAL, " ");
        }
    }

    private Video getVideoEntityFromURI(Uri contentURI) {
        long size = 0, duration = 0;
        String absolutePathOfImage = null;
        String thumbnail = null;
        int columnIndexData, columnThumb, columnSize, columnDuration;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);

        cursor.moveToFirst();

        columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        columnThumb = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);
        columnSize = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
        columnDuration = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
        absolutePathOfImage = cursor.getString(columnIndexData);
        thumbnail = cursor.getString(columnThumb);
        size = cursor.getLong(columnSize);
        duration = cursor.getLong(columnDuration);
        cursor.close();

        return new Video(absolutePathOfImage, thumbnail, size, duration);
    }
}