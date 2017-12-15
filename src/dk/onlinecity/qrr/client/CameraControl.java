/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client;

import javax.microedition.lcdui.Canvas;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

public class CameraControl
{
	private static CameraControl instance;
	private Player player;
	private VideoControl videoControl;

	private CameraControl()
	{
		if (player == null) {
			initPlayer();
		} else {
			int state = player.getState();
			switch (state) {
			case Player.UNREALIZED:
				System.out.println("player is unrealized");
				initPlayer();
				break;
			case Player.REALIZED:
				System.out.println("player is realized");
				break;
			case Player.PREFETCHED:
				System.out.println("player is prefetched");
				break;
			case Player.STARTED:
				System.out.println("player is started");
				break;
			case Player.CLOSED:
				System.out.println("player is closed");
				break;
			}
		}
	}

	private Player createPlayer()
	{
		try {
			return Manager.createPlayer("capture://image");
		} catch(Exception e) {}
		return null;
	}

	private void initPlayer()
	{
		try {
			player = createPlayer();
			player.realize();
			player.prefetch();
		} catch (MediaException e) {}
	}
	public static void stop() {
		if (instance == null) {
			return;
		}
		instance.getPlayer().close();
		instance = null;
	}

	public static CameraControl getInstance()
	{
		if (instance == null) {
			instance = new CameraControl();
		}
		return instance;
	}

	public Player getPlayer()
	{
		return player;
	}

	public VideoControl getVideoControl(Canvas canvas)
	{
		if (videoControl == null) {
			videoControl = (VideoControl) player.getControl("VideoControl");
			videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, canvas);

		}
		return videoControl;
	}

	public byte[] getSnapshot() throws RuntimeException
	{
		if (videoControl == null) {
			throw new RuntimeException("VideoControl = null");
		}
		try {
			return videoControl.getSnapshot("encodig=jpeg");
		} catch (MediaException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Could not use camera");
	}
}
