# videoPicker
pick video từ máy ảnh
 
chú ý : trong file glade phải thêm:

 ndk {
            // config you want to support device
            abiFilters 'arm64-v8a', 'armeabi', 'armeabi-v7a', 'x86'
        }
để thư viện MxVideoPlayer tương thích máy android thật

và trong file mantifest.xml phải thêm : 

  android:requestLegacyExternalStorage="true"
để lấy content từ trong máy android 10