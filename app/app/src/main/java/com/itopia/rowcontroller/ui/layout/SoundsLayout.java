package com.itopia.rowcontroller.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.itopia.rowcontroller.R;
import com.itopia.rowcontroller.ui.view.SoundView;

import java.util.ArrayList;
import java.util.List;

public class SoundsLayout extends LinearLayout implements AdapterView.OnItemClickListener {
    private Callback callback;

    private List<Sound> sounds = new ArrayList<>();
    private SoundsAdapter soundsAdapter;
    private ListView listView;

    public SoundsLayout(Context context) {
        super(context);
    }

    public SoundsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SoundsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        sounds.add(new Sound("airhorn.mp3"));

        soundsAdapter = new SoundsAdapter();

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(soundsAdapter);

        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Sound sound = soundsAdapter.getItem(position);
        callback.playSound(sound);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public class Sound {
        public String name;

        public Sound(String name) {
            this.name = name;
        }
    }

    private class SoundsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return sounds.size();
        }

        @Override
        public Sound getItem(int position) {
            return sounds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SoundView soundView;
            if (convertView != null) {
                soundView = (SoundView) convertView;
            } else {
                soundView = (SoundView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sound, parent, false);
            }

            Sound sound = getItem(position);
            soundView.setText(sound.name);

            return soundView;
        }
    }

    public interface Callback {
        void playSound(Sound sound);
    }
}
