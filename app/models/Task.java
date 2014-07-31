package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import play.data.validation.Error;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import utils.Priority;
import utils.TaskUtil;

@Entity
@NamedQueries({
		@NamedQuery(name = TaskUtil.FIND_BY_IDS, query = "select t from Task t where t.id in :ids"),
		@NamedQuery(name = TaskUtil.FIND_BY_IDS_CONFIRM, query = "select t from Task t where t.id in :ids and t.isConfirmed = false"),
		@NamedQuery(name = TaskUtil.FIND_BY_IDS_CONFIRM_PRIORITY, query = "select t from Task t where t.id in :ids and t.isConfirmed = false and t.priority = :priority"),
		@NamedQuery(name = TaskUtil.FIND_BY_IDS_PRIORITY, query = "select t from Task t where t.id in :ids and t.priority = :priority"), })
public class Task extends Model {

	@Required
	private String Title;

	private Long userId;

	private Date createDate;

	@Required
	private boolean isConfirmed;

	@Required
	private Priority priority;

	private Date deadline;

	@Transient
	private String Owner;

	@Transient
	private boolean isDangerous;

	@Transient
	private boolean isHighPriority;

	public Task(Long userId, String title, boolean isConfirmed) {
		this.userId = userId;
		this.Title = title;
		this.isConfirmed = isConfirmed;
		this.createDate = new Date();
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public boolean isConfirmed() {
		return isConfirmed;
	}

	public void setConfirmed(boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}

	public static Task findByIdWithUserId(Object id, Long userId) {
		return find("id = ? and userId = ?", id, userId).first();
	}

	public static List<Task> findByUserId(Long userId) {
		List<Task> taskList = find("userId = ?", userId).fetch();
		setBoolean(taskList);
		return taskList;
	}

	public static List<Task> findAll(Long userId, boolean showOnlyNotConfirmed) {
		if (showOnlyNotConfirmed) {
			List<Task> taskList = find("userId = ? and isConfirmed = ?",
					userId, false).fetch();
			setBoolean(taskList);
			return taskList;
		}
		return findByUserId(userId);
	}

	public static List<Task> findAll(Long userId, boolean showOnlyNotConfirmed,
			String priority) {
		if (priority.equals(Priority.allString)) {
			return findAll(userId, showOnlyNotConfirmed);
		} else {
			JPAQuery query;
			if (showOnlyNotConfirmed) {
				query = find("userId = ? and isConfirmed = ? and priority = ?",
						userId, false, Priority.valueOf(priority));
			} else {
				query = find("userId = ? and priority = ?", userId,
						Priority.valueOf(priority));
			}
			List<Task> taskList = query.fetch();
			setBoolean(taskList);
			return taskList;
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
			setBoolean(dangerousList);
			return dangerousList;
		}
	}

	public static List<Task> findListByPage(Long userId, int page) {
		List<Task> taskList = find("userId = ?", userId).fetch(page, 10);
		setBoolean(taskList);
		return taskList;
	}

	public static List<Task> findListByPage(Long userId, int page,
			boolean showOnlyNotConfirmed) {
		if (showOnlyNotConfirmed) {
			List<Task> taskList = find("userId = ? and isConfirmed = ?",
					userId, false).fetch(page, 10);
			setBoolean(taskList);
			return taskList;
		}
		return findListByPage(userId, page);
	}

	public static List<Task> findListByPage(Long userId, int page,
			boolean showOnlyNotConfirmed, String priority) {
		if (priority.equals(Priority.allString)) {
			return findListByPage(userId, page, showOnlyNotConfirmed);
		} else {
			JPAQuery query;
			if (showOnlyNotConfirmed) {
				query = find("userId = ? and isConfirmed = ? and priority = ?",
						userId, false, Priority.valueOf(priority));
			} else {
				query = find("userId = ? and priority = ?", userId,
						Priority.valueOf(priority));
			}
			List<Task> taskList = query.fetch(page, 10);
			setBoolean(taskList);
			return taskList;
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
			setBoolean(dangerousList);
			return dangerousList;
		}
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public String getCreateDateToString() {
		return TaskUtil.toDateString(getCreateDate());
	}

	public String getDeadlineToString() {
		return TaskUtil.toDateString(getDeadline());
	}

	public void validate(Validation validation) {
		Date deadline = getDeadline();
		List<Error> errors = validation.errors();
		boolean invalid = false;
		for (Error error : errors) {
			if (error.message().equals("Deadline is incorrect value.")) {
				invalid = true;
				break;
			}
		}
		if (!invalid) {
			validation.required(deadline);
			if (deadline != null
					&& (deadline.before(getCreateDate()) || deadline
							.equals(getCreateDate()))) {
				validation.addError("",
						"Deadline is earlier than created Date.");
			}
		}
	}

	public boolean isDangerous() {
		Calendar deadlineCal = Calendar.getInstance();
		deadlineCal.setTime(getDeadline());
		Calendar createCal = Calendar.getInstance();
		createCal.setTime(getCreateDate());
		long difference = deadlineCal.getTimeInMillis()
				- createCal.getTimeInMillis();
		long oneDayDifference = 24 * 60 * 60 * 1000;
		if (difference >= 0 && difference <= oneDayDifference) {
			return true;
		}
		return false;
	}

	public boolean isHighPriority() {
		if (getPriority().equals(Priority.High)) {
			return true;
		}
		return false;
	}

	public static void setBoolean(List<Task> taskList) {
		for (Task task : taskList) {
			task.isDangerous = task.isDangerous();
			task.isHighPriority = task.isHighPriority();
			task.Owner = ((User) User.findById(task.userId)).getName();
		}
	}

	public String getOwner() {
		return Owner;
	}

	public void setOwner(String owner) {
		Owner = owner;
	}
}
