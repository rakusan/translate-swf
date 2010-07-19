package com.flagstone.translate;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.flagstone.transform.Event;

public class EventNode extends Node {

	private static final Map<String, Event> eventNames =
		new HashMap<String, Event>();

	private static final Map<String, Integer> keyNames =
		new HashMap<String, Integer>();

	static {
		eventNames.put("rollover", Event.ROLL_OVER);
		eventNames.put("rollout", Event.ROLL_OUT);
		eventNames.put("press", Event.PRESS);
		eventNames.put("release", Event.RELEASE);
		eventNames.put("dragout", Event.DRAG_OUT);
		eventNames.put("dragover", Event.DRAG_OVER);
		eventNames.put("releaseoutside", Event.RELEASE_OUT);
		eventNames.put("load", Event.LOAD);
		eventNames.put("enterframe", Event.ENTER_FRAME);
		eventNames.put("unload", Event.UNLOAD);
		eventNames.put("mousemove", Event.MOUSE_MOVE);
		eventNames.put("mousedown", Event.MOUSE_DOWN);
		eventNames.put("mouseup", Event.MOUSE_UP);
		eventNames.put("keydown", Event.KEY_DOWN);
		eventNames.put("keyup", Event.KEY_UP);
		eventNames.put("data", Event.DATA);

		keyNames.put("<left>", new Integer(512));
		keyNames.put("<right>", new Integer(1024));
		keyNames.put("<home>", new Integer(1536));
		keyNames.put("<end>", new Integer(2048));
		keyNames.put("<insert>", new Integer(2560));
		keyNames.put("<delete>", new Integer(3072));
		keyNames.put("<backspace>", new Integer(4096));
		keyNames.put("<enter>", new Integer(6656));
		keyNames.put("<up>", new Integer(7168));
		keyNames.put("<down>", new Integer(7680));
		keyNames.put("<pageUp>", new Integer(8192));
		keyNames.put("<pageDown>", new Integer(8704));
		keyNames.put("<tab>", new Integer(9216));
		keyNames.put("<escape>", new Integer(9728));
		keyNames.put("<space>", new Integer(16384));
	}

	public static Event getEvent(final String token) {
		return eventNames.get(token.toLowerCase());
	}

	public static Integer getKey(final String token) {
		return keyNames.get(token.toLowerCase());
	}

	private final Set<Event>events;
	private int key;

	public EventNode(NodeType type) {
		super(type);
		events = EnumSet.noneOf(Event.class);
	}

	public void addEvent(final Event event) {
		events.add(event);
	}

	public Set<Event> getEvents() {
		return events;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int code) {
		key = code;
	}
}
