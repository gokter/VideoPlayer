package com.flyingh.videoplayer;

import java.io.File;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private EditText pathEditText;
	private Button playButton;
	private Button pauseButton;
	private SurfaceView surfaceView;
	private MediaPlayer mediaPlayer;
	private int currentPosition;
	private SeekBar seekBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pathEditText = (EditText) findViewById(R.id.path);
		playButton = (Button) findViewById(R.id.play_btn);
		pauseButton = (Button) findViewById(R.id.pause_btn);
		surfaceView = (SurfaceView) findViewById(R.id.surface_view);
		pathEditText.setText("llw.mp4");
		seekBar = (SeekBar) findViewById(R.id.seek_bar);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mediaPlayer != null && mediaPlayer.isPlaying()) {
					mediaPlayer.seekTo(seekBar.getProgress());
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
		});
	}

	public void play(View view) {
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setScreenOnWhilePlaying(true);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDisplay(surfaceView.getHolder());
			surfaceView.getHolder().addCallback(new Callback() {

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					Log.i(TAG, "destroy!");
					if (mediaPlayer != null) {
						currentPosition = mediaPlayer.getCurrentPosition();
						stop(null);
					}
				}

				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					Log.i(TAG, "create");
					if (currentPosition > 0) {
						play(null);
					}
				}

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					Log.i(TAG, "change");
				}
			});
			mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() + File.separator
					+ pathEditText.getText().toString().trim());
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					mediaPlayer.start();
					mediaPlayer.seekTo(currentPosition);
					seekBar.setMax(mediaPlayer.getDuration());
					new Thread(new Runnable() {

						@Override
						public void run() {
							while (mediaPlayer != null && mediaPlayer.isPlaying()) {
								seekBar.setProgress(mediaPlayer.getCurrentPosition());
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}
							}
						}
					}).start();
				}
			});
			playButton.setEnabled(false);
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					playButton.setEnabled(true);
				}
			});
			mediaPlayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					playButton.setEnabled(true);
					return false;
				}
			});
		} catch (Exception e) {
			Log.e(TAG, "play fail!");
			Toast.makeText(this, "play fail!", Toast.LENGTH_SHORT).show();
		}

	}

	public void pause(View view) {
		if (mediaPlayer == null) {
			return;
		}
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			pauseButton.setText("Continue");
		} else {
			mediaPlayer.start();
			pauseButton.setText("Pause");
		}
	}

	public void replay(View view) {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.seekTo(0);
			return;
		}
		play(view);
	}

	public void stop(View view) {
		if (mediaPlayer == null) {
			return;
		}
		playButton.setEnabled(true);
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
