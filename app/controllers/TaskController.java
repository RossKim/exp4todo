package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Task;
import models.TaskRelative;
import models.User;
import play.data.binding.As;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Controller;
import utils.Priority;
import utils.SessionUtil;
import utils.TaskUtil;

public class TaskController extends Controller {

	public static void index() {
		session.put(SessionUtil.showOnlyNotConfirmed, "false");
		session.put(SessionUtil.showDangerous, false);
		session.put(SessionUtil.showPriority, Priority.allString);
		taskList();
	}

	public static void taskList() {
		User user = SessionUtil.getUser(session);
		if (user == null) {
			session.clear();
			redirect("/");
		} else {
			boolean confirmed = SessionUtil.showOnlyNotConfirmed(session);
			String priority = SessionUtil.showPriority(session);
			boolean dangerous = SessionUtil.showDangerous(session);
			renderArgs.put("taskList", TaskUtil.findAll(user.getId(),
					confirmed, priority, dangerous));
			renderArgs.put(SessionUtil.showOnlyNotConfirmed, confirmed);
			renderArgs.put("userName", user.getName());
			renderArgs.put("Priority", Priority.values());
			render();
		}
	}

	public static void taskListConfirmed(boolean showOnlyNotConfirmed) {
		session.put(SessionUtil.showOnlyNotConfirmed, showOnlyNotConfirmed);
		session.put(SessionUtil.showDangerous, false);
		taskList();
	}

	public static void taskPriority(String priority) {
		session.put(SessionUtil.showPriority, priority);
		taskList();
	}

	public static void taskDangerous() {
		session.put(SessionUtil.showOnlyNotConfirmed, false);
		session.put(SessionUtil.showDangerous, true);
		taskList();
	}

	public static void addTask() {
		Long userId = SessionUtil.getUserId(session);
		if (userId == null) {
			session.clear();
			redirect("/");
		}
		renderArgs.put("userId", userId);
		renderArgs.put("Priority", Priority.values());
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, 2);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		renderArgs.put("Today",
				TaskUtil.toDateString(new Date(cal.getTimeInMillis())));

		List<User> userList = User.findAll();
		User.deleteInformation(userList);
		renderArgs.put("userList", userList);
		render();
	}

	public static void submitTask(@Valid Task task,
			@As("yyyy-MM-dd'T'HH:mm") Date deadline,
			@Required List<Long> selList) {
		task.setCreateDate(new Date());
		task.setConfirmed(Boolean.valueOf(params.get("isConfirmed")));
		task.setPriority(Priority.valueOf(params.get("task.priority")));
		task.setDeadline(deadline);
		task.validate(validation);
		validation.required(selList);
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			addTask();
		}
		task.save();
		for (Long selId : selList) {
			TaskRelative rel = new TaskRelative(task.getId(), selId);
			rel.save();
		}
		redirect("/task/");
	}

	public static void editTask(Long id) {
		Long userId = SessionUtil.getUserId(session);
		if (userId == null) {
			session.clear();
			redirect("/");
		}
		Task task = Task.findById(id);
		if (task == null) {
			validation
					.addError("", "The Task which you select does not exist.");
			validation.keep();
			params.flash();
			redirect("/task/");
		}
		renderArgs.put("task", task);
		renderArgs.put("Priority", Priority.values());
		List<User> userList = User.findAll();
		User.deleteInformation(userList);
		renderArgs.put("selfId", userId);
		renderArgs.put("userList", userList);
		List<User> selList = TaskUtil.findUsersByTask(task.getId());
		renderArgs.put("selList", selList);
		render();
	}

	public static void editSubmitTask(@Valid Task task,
			@As("yyyy-MM-dd'T'HH:mm") Date deadline,
			@Required List<Long> selList) {
		task.setConfirmed(Boolean.valueOf(params.get("isConfirmed")));
		task.setPriority(Priority.valueOf(params.get("task.priority")));
		task.setDeadline(deadline);
		task.validate(validation);
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			editTask(task.id);
		}
		task.save();
		TaskRelative.deleteTaskRelativeByTaskId(task.getId());
		for (Long selId : selList) {
			TaskRelative rel = new TaskRelative(task.getId(), selId);
			rel.save();
		}
		redirect("/task/");
	}

	public static void deleteTask(Long id) {
		Task task = Task.findById(id);
		if (task == null) {
			validation
					.addError("", "The Task which you select does not exist.");
			validation.keep();
			params.flash();
			redirect("/task/");
		}
		TaskUtil.deleteTask(task);
		redirect("/task/");
	}

	public static void changeConfirm(@Required String id,
			@Required String confirmed) {
		Map<String, Object> map = new HashMap<String, Object>();
		Long userId = SessionUtil.getUserId(session);
		if (userId == null) {
			map.put("status", "Error");
			renderJSON(map);
		}
		Task task = Task.findById(Long.valueOf(id));
		if (task == null) {
			map.put("status", "Error");
			renderJSON(map);
		}
		task.setConfirmed(Boolean.valueOf(confirmed));
		task.save();

		// Map に結果を蓄え，JSON として出力
		map.put("status", "OK");
		map.put(SessionUtil.showOnlyNotConfirmed, task.isConfirmed());
		renderJSON(map);
	}

	public static void findTask(@Required String id) {
		Task task = Task.findById(Long.valueOf(id));
		// Map に結果を蓄え，JSON として出力
		Map<String, Object> map = new HashMap<String, Object>();
		Long userId = SessionUtil.getUserId(session);
		if (userId == null || task == null) {
			map.put("status", "Error");
		} else {
			map.put("status", "OK");
			map.put("task", task);
			List<User> allList = User.findAll();
			List<User> userList = new ArrayList<User>();
			List<User> selList = TaskUtil.findUsersByTask(task.getId());
			for (User user : allList) {
				if (!selList.contains(user)) {
					userList.add(user);
				}
			}
			User.deleteInformation(userList);
			User.deleteInformation(selList);
			map.put("selfId", SessionUtil.getUserId(session));
			map.put("userList", userList);
			map.put("selList", selList);
		}
		renderJSON(map);
	}

	public static void findTaskAll() {
		Long userId = SessionUtil.getUserId(session);
		Map<String, Object> map = new HashMap<String, Object>();
		if (userId == null) {
			map.put("status", "Error");
			renderJSON(map);
		}
		boolean confirmed = SessionUtil.showOnlyNotConfirmed(session);
		String priority = SessionUtil.showPriority(session);
		boolean dangerous = SessionUtil.showDangerous(session);
		map.put("status", "OK");
		map.put("taskList",
				TaskUtil.findAll(userId, confirmed, priority, dangerous));
		map.put("selfId", userId);
		renderJSON(map);
	}

	public static void findTaskByPage(String page) {
		Map<String, Object> map = new HashMap<String, Object>();
		Long userId = SessionUtil.getUserId(session);
		if (userId == null) {
			map.put("status", "Error");
			renderJSON(map);
		}
		if (page == null || page.equals("")) {
			findTaskAll();
		} else {
			boolean confirmed = SessionUtil.showOnlyNotConfirmed(session);
			String priority = SessionUtil.showPriority(session);
			boolean dangerous = SessionUtil.showDangerous(session);
			map.put("status", "OK");
			map.put("taskList", TaskUtil.findListByPage(userId,
					Integer.parseInt(page), confirmed, priority, dangerous));
			map.put("currentPage", page);
			map.put("selfId", userId);
			renderJSON(map);
		}
	}

	public static void editTaskJson(@Required String id,
			@Required String title, @Required String priority,
			@Required @As("yyyy-MM-dd'T'HH:mm") Date deadline) {
		Task task = Task.findById(Long.valueOf(id));
		Map<String, Object> map = new HashMap<String, Object>();
		Long userId = SessionUtil.getUserId(session);
		if (userId == null || task == null) {
			map.put("status", "Error");
			renderJSON(map);
		}

		task.setTitle(title);
		task.setPriority(Priority.valueOf(priority));
		task.setDeadline(deadline);
		task.save();
		TaskRelative.deleteTaskRelativeByTaskId(task.getId());
		String[] userIdArray = params.get("selList").split(",");
		for (String userIdStr : userIdArray) {
			TaskRelative rel = new TaskRelative(task.getId(),
					Long.valueOf(userIdStr));
			rel.save();
		}
		map.put("status", "OK");
		renderJSON(map);
	}

	public static void addTaskJson(@Required @Valid Task task,
			@Required @As("yyyy-MM-dd'T'HH:mm") Date deadline) {
		Map<String, Object> map = new HashMap<String, Object>();
		Long userId = SessionUtil.getUserId(session);
		if (userId == null) {
			map.put("status", "Error");
			renderJSON(map);
		}
		task.setUserId(userId);
		task.setCreateDate(new Date());
		task.setConfirmed(Boolean.valueOf(params.get("isConfirmed")));
		task.setPriority(Priority.valueOf(params.get("task.priority")));
		task.setDeadline(deadline);
		task.setOwner(((User) User.findById(userId)).getName());
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			map.put("status", "NO");
			renderJSON(map);
		}
		task.save();

		String[] userIdArray = params.get("selList").split(",");
		for (String userIdStr : userIdArray) {
			TaskRelative rel = new TaskRelative(task.getId(),
					Long.valueOf(userIdStr));
			rel.save();
		}
		map.put("status", "OK");
		renderJSON(map);
	}

	public static void deleteTaskJson(@Required String id) {
		Task task = Task.findById(Long.valueOf(id));
		Map<String, Object> map = new HashMap<String, Object>();
		Long userId = SessionUtil.getUserId(session);
		if (userId == null || task == null) {
			map.put("status", "Error");
			renderJSON(map);
		}

		TaskUtil.deleteTask(task);
		map.put("status", "OK");
		renderJSON(map);
	}
}
