package models;

import java.util.List;

import javax.persistence.Entity;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class TaskRelative extends Model {

	@Required
	private Long taskId;

	@Required
	private Long userId;

	public TaskRelative(Long taskId, Long userId) {
		this.taskId = taskId;
		this.userId = userId;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public static void openPublicTask(Long taskId) {
		List<User> userList = User.findAll();
		List<TaskRelative> relList = find("taskId = ?", taskId).fetch();
		for (User user : userList) {
			boolean exist = false;
			for (TaskRelative rel : relList) {
				if (rel.getUserId().equals(user.getId())) {
					exist = true;
					break;
				}
			}
			if (!exist) {
				TaskRelative newRel = new TaskRelative(taskId, user.getId());
				newRel.save();
			}
		}
	}

	public static List<TaskRelative> findAllByTaskId(Long taskId) {
		return find("taskId = ?", taskId).fetch();
	}

	public static List<TaskRelative> findAllByUserId(Long userId) {
		return find("userId = ?", userId).fetch();
	}

	public static TaskRelative findTaskRelative(Long taskId, Long userId) {
		return find("taskId = ? and userId = ?", taskId, userId).first();
	}

	public static void deleteTaskRelativeByTaskId(Long taskId) {
		List<TaskRelative> relList = findAllByTaskId(taskId);
		for (TaskRelative rel : relList) {
			rel.delete();
		}
	}

	public static void deleteTaskRelativeByUserId(Long userId) {
		List<TaskRelative> relList = findAllByUserId(userId);
		for (TaskRelative rel : relList) {
			rel.delete();
		}
	}

}
