package utils;

import models.User;
import play.mvc.Scope.Session;

public class SessionUtil {

	final public static String showOnlyNotConfirmed = "confirmed";
	final public static String showPriority = "priority";
	final public static String showDangerous = "dangerous";

	public static boolean showOnlyNotConfirmed(Session session) {
		String confirmed = session.get(showOnlyNotConfirmed);
		if (confirmed != null && confirmed.equals("true")) {
			session.put(showOnlyNotConfirmed, "true");
			return true;
		} else {
			session.put(showOnlyNotConfirmed, "false");
			return false;
		}
	}

	public static String showPriority(Session session) {
		String priority = session.get(showPriority);
		if (priority == null || priority.equals("")) {
			session.put(showPriority, Priority.allString);
			return Priority.allString;
		} else {
			return priority;
		}
	}

	public static boolean showDangerous(Session session) {
		String dangerous = session.get(showDangerous);
		if (dangerous != null && dangerous.equals("true")) {
			session.put(showDangerous, "true");
			return true;
		} else {
			session.put(showDangerous, "false");
			return false;
		}
	}

	public static User getUser(Session session) {
		String userId = session.get("userId");
		if (userId != null) {
			User user = User.findById(Long.valueOf(userId));
			if (user != null) {
				return user;
			}
		}
		return null;
	}

	public static Long getUserId(Session session) {
		String userId = session.get("userId");
		if (userId != null && !userId.equals("")) {
			return Long.valueOf(userId);
		}
		return null;
	}
}
