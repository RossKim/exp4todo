package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.User;
import play.data.validation.Valid;
import play.mvc.Controller;
import utils.HashUtil;
import utils.SessionUtil;
import utils.TaskUtil;

public class Application extends Controller {

	public static void index() {
		User user = SessionUtil.getUser(session);
		if (user != null) {
			redirect("/task/");
		}
		redirect("/signin");
	}

	public static void signUp() {
		render();
	}

	public static void submitSignUp(@Valid User user) {
		user.setHashedPassword(HashUtil.convertToHashedPassword(
				user.getStringId(), user.getPassword()));
		user.validate(validation);
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			signUp();
		}
		user.save();
		session.put("userId", user.getId());
		redirect("/");
	}

	public static void signIn() {
		render();
	}

	public static void login() {
		String stringId = params.get("stringId");
		String password = params.get("password");
		validation.required("ID", stringId);
		validation.required("Password", password);
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			signIn();
		}
		User loginUser = User.login(stringId, password);
		if (loginUser == null) {
			if (User.isExistUser(stringId)) {
				validation.addError("", "Password is not correct.");
			} else {
				validation.addError("", "ID is not correct.");
			}
			params.flash();
			validation.keep();
			signIn();
		}
		session.put("userId", loginUser.getId());
		redirect("/");
	}

	public static void signOut() {
		session.clear();
		redirect("/");
	}

	public static void editAccount() {
		User user = SessionUtil.getUser(session);
		if (user != null) {
			renderArgs.put("user", user);
			render();
		}
		redirect("/");
	}

	public static void submitEditAccount(@Valid User user) {
		if (user.getPassword() != null && !user.getPassword().equals("")) {
			user.setHashedPassword(HashUtil.convertToHashedPassword(
					user.getStringId(), user.getPassword()));
			session.clear();
		} else {
			user.setPassword(user.getHashedPassword());
		}
		user.validate(validation);
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			editAccount();
		}
		user.save();
		redirect("/");
	}

	public static void userListJson() {
		Map<String, Object> map = new HashMap<String, Object>();
		List<User> userList = User.findAll();
		User.deleteInformation(userList);
		map.put("selfId", SessionUtil.getUserId(session));
		map.put("userList", userList);
		renderJSON(map);
	}

	public static void deleteAccount() {
		User user = SessionUtil.getUser(session);
		if (user != null) {
			TaskUtil.deleteTaskByUser(user.getId());
			user.delete();
			session.clear();
		}
		redirect("/");
	}
}
