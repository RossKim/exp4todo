package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import models.Task;
import models.TaskRelative;
import models.User;
import play.db.jpa.JPA;

public class TaskUtil {
	public final static String FIND_BY_IDS = "Task.findByIds";
	public final static String FIND_BY_IDS_CONFIRM = "Task.findByIdsConfirm";
	public final static String FIND_BY_IDS_PRIORITY = "Task.findByIdsPriority";
	public final static String FIND_BY_IDS_CONFIRM_PRIORITY = "Task.findByIdsConfirmPriority";
	public final static String FIND_USER_BY_IDS = "User.findByIds";

	/**
	 * Dateをyyyy-mm-dd'T'HH:mm景気に変換
	 * 
	 * @param date
	 * @return
	 */
	public static String toDateString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat timeFormat = new SimpleDateFormat("HH:mm");
		return dateFormat.format(date) + "T" + timeFormat.format(date);
	}

	/**
	 * TaskRelativeリストからTaskのidのリストを取得する。
	 * 
	 * @param relList
	 * @return
	 */
	public static List<Long> getTaskIdsFromList(List<TaskRelative> relList) {
		List<Long> ids = new ArrayList<Long>();
		for (TaskRelative rel : relList) {
			ids.add(rel.getTaskId());
		}
		if (ids.size() > 0) {
			return ids;
		} else {
			return null;
		}
	}

	/**
	 * TaskRelativeリストからUserのidのリストを取得する。
	 * 
	 * @param relList
	 * @return
	 */
	public static List<Long> getUserIdsFromList(List<TaskRelative> relList) {
		List<Long> ids = new ArrayList<Long>();
		for (TaskRelative rel : relList) {
			ids.add(rel.getUserId());
		}
		if (ids.size() > 0) {
			return ids;
		} else {
			return null;
		}
	}

	public static List<Task> findRelativedTasks(Long userId, Query query) {
		List<TaskRelative> relList = TaskRelative.findAllByUserId(userId);
		List<Long> ids = getTaskIdsFromList(relList);
		query.setParameter("ids", ids);
		List<Task> taskList = query.getResultList();
		Task.setBoolean(taskList);
		return taskList;
	}

	public static List<Task> findRelativedTasks(Long userId, Query query,
			int page) {
		List<TaskRelative> relList = TaskRelative.findAllByUserId(userId);
		List<Long> ids = getTaskIdsFromList(relList);
		query.setParameter("ids", ids);
		query.setFirstResult((page - 1) * 10);
		query.setMaxResults(10);
		List<Task> taskList = query.getResultList();
		Task.setBoolean(taskList);
		return taskList;
	}

	public static List<Task> findAll(Long userId) {
		Query query = JPA.em().createNamedQuery(FIND_BY_IDS, Task.class);
		return findRelativedTasks(userId, query);
	}

	public static List<Task> findAll(Long userId, boolean showOnlyNotConfirmed) {
		if (showOnlyNotConfirmed) {
			Query query = JPA.em().createNamedQuery(FIND_BY_IDS_CONFIRM,
					Task.class);
			return findRelativedTasks(userId, query);
		}
		return findAll(userId);
	}

	public static List<Task> findAll(Long userId, boolean showOnlyNotConfirmed,
			String priority) {
		if (priority.equals(Priority.allString)) {
			return findAll(userId, showOnlyNotConfirmed);
		} else {
			Query query;
			if (showOnlyNotConfirmed) {
				query = JPA.em().createNamedQuery(FIND_BY_IDS_CONFIRM_PRIORITY,
						Task.class);
			} else {
				query = JPA.em().createNamedQuery(FIND_BY_IDS_PRIORITY,
						Task.class);
			}
			query.setParameter("priority", Priority.valueOf(priority));
			return findRelativedTasks(userId, query);
		}
	}

	public static List<Task> findAll(Long userId, boolean showOnlyNotConfirmed,
			String priority, boolean showDangerous) {
		if (!showDangerous) {
			return findAll(userId, showOnlyNotConfirmed, priority);
		} else {
			List<Task> taskList = findAll(userId, true, priority);
			List<Task> dangerousList = new ArrayList<Task>();
			for (Task task : taskList) {
				if (task.isDangerous()) {
					dangerousList.add(task);
				}
			}
			Task.setBoolean(dangerousList);
			return dangerousList;
		}
	}

	public static List<Task> findListByPage(Long userId, int page) {
		Query query = JPA.em().createNamedQuery(FIND_BY_IDS, Task.class);
		return findRelativedTasks(userId, query, page);
	}

	public static List<Task> findListByPage(Long userId, int page,
			boolean showOnlyNotConfirmed) {
		if (showOnlyNotConfirmed) {
			Query query = JPA.em().createNamedQuery(FIND_BY_IDS_CONFIRM,
					Task.class);
			return findRelativedTasks(userId, query, page);
		}
		return findListByPage(userId, page);
	}

	public static List<Task> findListByPage(Long userId, int page,
			boolean showOnlyNotConfirmed, String priority) {
		if (priority.equals(Priority.allString)) {
			return findListByPage(userId, page, showOnlyNotConfirmed);
		} else {
			Query query;
			if (showOnlyNotConfirmed) {
				query = JPA.em().createNamedQuery(FIND_BY_IDS_CONFIRM_PRIORITY,
						Task.class);
			} else {
				query = JPA.em().createNamedQuery(FIND_BY_IDS_PRIORITY,
						Task.class);
			}
			query.setParameter("priority", Priority.valueOf(priority));
			return findRelativedTasks(userId, query, page);
		}
	}

	public static List<Task> findListByPage(Long userId, int page,
			boolean showOnlyNotConfirmed, String priority, boolean showDangerous) {
		if (!showDangerous) {
			return findListByPage(userId, page, showOnlyNotConfirmed, priority);
		} else {
			List<Task> taskList = findListByPage(userId, page, true, priority);
			List<Task> dangerousList = new ArrayList<Task>();
			for (Task task : taskList) {
				if (task.isDangerous()) {
					dangerousList.add(task);
				}
			}
			Task.setBoolean(dangerousList);
			return dangerousList;
		}
	}

	public static List<User> findUsersByTask(Long taskId) {
		List<TaskRelative> relList = TaskRelative.findAllByTaskId(taskId);
		List<Long> ids = getUserIdsFromList(relList);
		Query query = JPA.em().createNamedQuery(FIND_USER_BY_IDS, User.class);
		query.setParameter("ids", ids);
		return query.getResultList();
	}

	public static void deleteTask(Task task) {
		if (task != null) {
			task.delete();
			TaskRelative.deleteTaskRelativeByTaskId(task.getId());
		}
	}

	public static void deleteTaskByUser(Long userId) {
		List<Task> taskList = Task.findByUserId(userId);
		// Delete Tasks which task's owner is me.
		for (Task task : taskList) {
			TaskRelative.deleteTaskRelativeByTaskId(task.getId());
			task.delete();
		}

		// Delete TaskRelative
		TaskRelative.deleteTaskRelativeByUserId(userId);
	}
}
