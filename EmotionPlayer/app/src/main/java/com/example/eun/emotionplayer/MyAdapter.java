package com.example.eun.emotionplayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class MyAdapter extends BaseAdapter { // BaseAdapter를 상속받을 때 오버라이딩해야 하는 함수들 : getCount, getItem, getItemId, getView
    List<MusicDto> list;
    LayoutInflater inflater;
    Activity activity;

    public MyAdapter(Activity activity, List<MusicDto> list) {
        this.list = list;
        this.activity = activity;
        // 리스트뷰 객체화
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) { // 이미지 크기 조정
        if (convertView == null) { // view가 최초로 호출될 때에만 convertView가 null 값
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            convertView.setLayoutParams(layoutParams);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.album);
        Bitmap albumImage = getAlbumImage(activity, Integer.parseInt((list.get(position)).getAlbumId()), 170);
        imageView.setImageBitmap(albumImage);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(list.get(position).getTitle());
        title.setSelected(true);

        TextView artist = (TextView) convertView.findViewById(R.id.artist);
        artist.setText(list.get(position).getArtist());

        title.setTypeface(SongList.customFont);
        artist.setTypeface(SongList.customFont);
        return convertView;
    }

    private static final BitmapFactory.Options options = new BitmapFactory.Options();

    private static Bitmap getAlbumImage(Context context, int album_id, int MAX_IMAGE_SIZE) {
        ContentResolver res = context.getContentResolver(); // 객체 얻어오기
        // content provider 공부 (content provider의 고유 키 값/path/ID)
        Uri uri = Uri.parse("content://media/external/audio/albumart/" + album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality

                //크기를 얻어오기 위한옵션 ,
                //inJustDecodeBounds값이 true로 설정되면 decoder가 bitmap object에 대해 메모리를 할당하지 않고, 따라서 bitmap을 반환하지도 않는다.
                // 다만 options fields는 값이 채워지기 때문에 Load 하려는 이미지의 크기를 포함한 정보들을 얻어올 수 있다.
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, options);
                int scale = 0;
                if (options.outHeight > MAX_IMAGE_SIZE || options.outWidth > MAX_IMAGE_SIZE) {
                    scale = (int) Math.pow(2, (int) Math.round(Math.log(MAX_IMAGE_SIZE / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale; // 이미지 파일 줄이기(background보다 큰 이미지를 넣게 되면 out of memory발생)



                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, options);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (options.outWidth != MAX_IMAGE_SIZE || options.outHeight != MAX_IMAGE_SIZE) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE, true);
                        b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null; // 파일이 존재하면 이미지파일(bitmap) return, 파일이 없거나 에러가 발생하면 null return
    }


}
