package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import play.data.validation.Required;
import play.data.validation.Unique;
import play.data.validation.Validation;
import play.db.jpa.Model;
import utils.HashUtil;
import utils.TaskUtil;

@Entity
@NamedQuery(name = TaskUtil.FIND_USER_BY_IDS, query = "select t from User t where t.id in :ids")
public class User extends Model {

	@Unique
	@Required
	private String stringId;

	@Transient
	private String password;

	private String HashedPassword;

	@Required
	private String name;

	public User(String stringId, String password, String name) {
		this.stringId = stringId;
		this.password = password;
		this.name = name;
	}

	public String getStringId() {
		return stringId;
	}

	public void setStringId(String stringId) {
		this.stringId = stringId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHashedPassword() {
		return HashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		HashedPassword = hashedPassword;
	}

	public void validate(Validation validation) {
		if (HashedPassword == null || HashedPassword.equals("")
				|| password == null || password.equals("")) {
			validation.addError("", "Password is empty.");
		}
	}

	public static User login(String stringId, String password) {
		if (User.isExistUser(stringId)) {
			User loginUser = User.findByStringId(stringId);
			if (loginUser != null) {
				if (loginUser.HashedPassword.equals(HashUtil
						.convertToHashedPassword(stringId, password))) {
					return loginUser;
				}
			}
		}
		return null;
	}

	public static boolean isExistUser(String stringId) {
		return (find("stringId = ?", stringId).fetch().size() > 0);
	}

	public static User findByStringId(String stringId) {
		return find("stringId = ?", stringId).first();
	}

	public static void deleteInformation(List<User> userList) {
		for (User user : userList) {
			user.setHashedPassword(null);
			user.setPassword(null);
		}
	}
}
