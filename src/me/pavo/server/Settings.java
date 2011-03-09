package me.pavo.server;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.lwuit.Display;

public class Settings {
	public static final String BT_POLL_DELAY_MIN = "bt_poll_delay_min";
	public static final String BT_POLL_DELAY_MAX = "bt_poll_delay_max";
	public static final String BT_POLL_DELAY_MULTIPLIER = "bt_poll_delay_multiplier";
	public static final String BT_CHANGES_THRESHOLD = "bt_changes_threshold";
	public static final String BT_SCAN_SIZE_THRESHOLD = "bt_scan_size_threshold";
	public static final String BT_BUFFER_TIME = "bt_buffer_time";
	public static final String DEVICE_VIDEO = "device_video";
	public static final String DEVICE_PHOTO = "device_photo";
	public static final String DEVICE_AUDIO = "device_audio";
	public static final String PHOTO_SNAPSHOT_PARAM = "photo_snapshot_param";
	public static final String VIDEO_PLAYBACK_FS = "video_playback_fs";
	public static final String VIDEO_CAPTURE_FULLSCREEN = "video_capture_fs";
	public static final String VIDEO_CAPTURE_MAX_TIME = "video_capture_max_len";
	public static final String VIDEO_CAPTURE_DIR = "video_capture_dir";
	public static final String VIDEO_CAPTURE_RECORD_BEFORE_PLAY = "video_capture_rbp";	
	public static final String AUDIO_CAPTURE_MAX_TIME = "audio_capture_max_len";
	public static final String VIDEO_CAPTURE_SIZE_LIMIT = "video_capture_size_limit";
	public static final String AUDIO_CAPTURE_SIZE_LIMIT = "audio_capture_size_limit";
	public static final String AUDIO_CAPTURE_DIR = "audio_capture_dir";
	public static final String PHOTO_CAPTURE_FULLSCREEN = "photo_capture_fs";
	public static final String VIDEO_FORMAT = "video_format";
	public static final String AUDIO_FORMAT = "audio_format";
	public static final String FS_MIN_FREE = "fs_min_free";
	public static final String FS_ALWAYS_FREE = "fs_always_free";
	public static final String URL_OPEN_DIE = "url_open_die";
	public static final String TWEET_LIST_REVERSED = "tweet_list_reversed";
	public static final String TOPIC_CACHE_SIZE = "topic_cache_size";
	public static final String IMAGE_CACHE_SIZE = "image_cache_size";
	public static final String MAIN_FONT = "main_font";
	public static final String MAIN_FONT_BOLD = "main_font_bold";
	public static final String LOCALE = "locale";
	public static final String NOTIFICATION_VIBRATE = "notification_vibrate";
	public static final String NOTIFICATION_VIBRATE_MENTIONS = "notification_vibrate_mentions";
	public static final String NOTIFICATION_VIBRATE_DM = "notification_vibrate_dm";
	public static final String VIBRATION_DURATION = "vibration_duration";
	public static final String NOTIFICATION_ANIMATION = "notification_animation";
	public static final String TOUCHSCREEN = "touchscreen";
	public static final String SMALLSCREEN = "smallscreen";
	public static final String SHORTSCREEN = "shortscreen";
	public static final String TOPICITEM_CACHE_SIZE = "topicitem_cache_size";
	public static final String INVISIBLE_T9 = "invisible_t9";
	public static final String PHOTO_SERVICE = "photo_service";
	public static final String LOREM = "lorem";
	public static final String NAVIGATION = "navigation";
	public static final String SWAP_CONTROLS = "swap_controls";
	public static final String AVATAR_FETCH_TIME = "avatar_fetch_time";
	public static final String AVATAR_CACHE_SIZE = "avatar_cache_size";
	public static final String AVATAR_SIZE = "avatar_size";
	public static final String AVATAR_FETCH_SIZE = "avatar_fetch_size";
	public static final String LOAD_AVATARS = "load_avatars";
	public static final String SHORTCUTS = "shortcuts";
	public static final String VKB = "vkb";
	public static final String VKB_ONLY = "vkb_only";
	public static final String QWERTY = "qwerty";
	public static final String SCALE_PHOTOS = "scale_photos";
	
	static Params settings = new Params();
	
	private static int idGenerator = 0;
	
	public static synchronized int uniqueId() {
		return idGenerator++;
	}

	public static synchronized void init() {
		set(BT_POLL_DELAY_MIN, 1000);
		set(BT_POLL_DELAY_MAX, 60000);
		set(BT_POLL_DELAY_MULTIPLIER, 2);
		set(BT_CHANGES_THRESHOLD, 3);
		set(BT_SCAN_SIZE_THRESHOLD, 5);
		set(BT_BUFFER_TIME, 120000);
		set(DEVICE_VIDEO, "capture://video");
		set(DEVICE_PHOTO, "capture://image");
		set(DEVICE_AUDIO, "capture://audio");
		set(PHOTO_SNAPSHOT_PARAM, "encoding=jpeg&quality=80&width=480&height=640");
		set(VIDEO_FORMAT, "video/3gpp");
		set(AUDIO_FORMAT, "audio/amr");
		set(VIDEO_PLAYBACK_FS, false);
		set(PHOTO_CAPTURE_FULLSCREEN, true);
		set(VIDEO_CAPTURE_FULLSCREEN, true);
		set(VIDEO_CAPTURE_DIR, System.getProperty("fileconn.dir.videos"));
		set(AUDIO_CAPTURE_DIR, System.getProperty("fileconn.dir.recordings"));
		set(VIDEO_CAPTURE_MAX_TIME, Integer.MAX_VALUE-1);
		set(AUDIO_CAPTURE_MAX_TIME, Integer.MAX_VALUE-1);
		set(VIDEO_CAPTURE_SIZE_LIMIT, 1024 * 1024 * 16); // 16Mb
		set(AUDIO_CAPTURE_SIZE_LIMIT, 1024 * 1024 * 16); // 16Mb
		set(FS_MIN_FREE, 256 * 1024); // min free size on filesystem
		set(FS_ALWAYS_FREE, 128 * 1024); // min free size on filesystem
		set(VIDEO_CAPTURE_RECORD_BEFORE_PLAY, false);
		set(URL_OPEN_DIE, false);
		set(TWEET_LIST_REVERSED, false);
		set(TOPIC_CACHE_SIZE, 10);
		set(IMAGE_CACHE_SIZE, 7);
		set(MAIN_FONT, "sys-main");
		set(MAIN_FONT_BOLD, "sys-main-bold"); 
		set(LOCALE, "en");
		set(NOTIFICATION_VIBRATE, true);
		set(NOTIFICATION_VIBRATE_MENTIONS, true);
		set(NOTIFICATION_VIBRATE_DM, true);
		set(VIBRATION_DURATION, 100);
		set(NOTIFICATION_ANIMATION, true);
		set(TOPICITEM_CACHE_SIZE, 20);
		set(AVATAR_CACHE_SIZE, 32);
		set(AVATAR_FETCH_TIME, 2000);
		set(AVATAR_SIZE, 32);
		set(AVATAR_FETCH_SIZE, 4);
		set(PHOTO_SERVICE, "tweetphoto");
		set(SWAP_CONTROLS, false);
		set(LOAD_AVATARS, true);
		set(SHORTCUTS, new Params());
		set(VKB, true);
		set(INVISIBLE_T9, true);
		set(VKB_ONLY, false);
		set(SCALE_PHOTOS, true);
	}
	
	public static synchronized void postDisplayInit() {
		set(TOUCHSCREEN, Display.getInstance().isTouchScreenDevice());
		set(NAVIGATION, Display.getInstance().getImplementation().getSoftkeyCount() > 1);
	}
	
	public static synchronized void update(Hashtable newSettings) {
		for(Enumeration e = newSettings.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			settings.put(key, newSettings.get(key));
		}
	}

	public static synchronized boolean has(String key) {
		return settings.has(key);
	}
	
	public static synchronized void set(String key, int value) {
		settings.set(key, value);
	}

	public static synchronized void set(String key, Params params) {
		settings.set(key, params);
	}
	
	public static synchronized int getInt(String key) {
		return settings.getInt(key);
	}
	
	public static synchronized void set(String key, String value) {
		settings.set(key, value);
	}

	public static synchronized void set(String key, boolean value) {
		settings.set(key, value);
	}
	
	public static synchronized String getString(String key) {
		return settings.getString(key);
	}
	
	public static synchronized boolean getBool(String key) {
		return settings.getBoolean(key);
	}
	
	public static synchronized Params getParams(String key) {
		return (Params) settings.get(key);
	}	
}
