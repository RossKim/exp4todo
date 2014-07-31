/* exp4todo.js */
function reloadTask(page) {
	$.ajax({
		url:'/task/findTaskByPage',
		type:'GET',
		data:{page:page},
		success:function(data) {
			if(data) {
				$('#readNextPage').remove();
				if(page == '1' || !page) {
					$('#taskList').empty();
				}
				for(var i=0;i<data.taskList.length;i++) {
					var task = data.taskList[i];
					var title = $("<td id='taskTitle'></td>").text(task.Title);
					var createDate = $("<td id='taskCreateDate'></td>").text(toDateString(new Date(task.createDate)));
					var priority = $("<td id='taskPriority'></td>").text(task.priority);
					var deadline = $("<td id='taskDeadline'></td>").text(toDateString(new Date(task.deadline)));
					var confirmed = $("<td id='taskConfirmed'></td>").text(task.isConfirmed);
					var owner = $("<td id='taskOwner'></td>").text(task.Owner);
					var tr = $("<tr></tr>");
					if(task.isConfirmed) {
						tr.attr('class', 'success');
					} else if(task.isDangerous) {
						tr.attr('class', 'danger').attr('data-toggle','tooltip').attr('data-placement','right')
						.attr('data-original-title','Hurry Up!!!');
						tr.tooltip('hide');
					} else if(task.isHighPriority) {
						tr.attr('class', 'warning');
					}
					tr.append(title,createDate,priority,deadline,confirmed,owner);
					tr.attr('taskid',task.id).attr('ownerid',task.userId)
						.unbind('click').bind('click', function() { showEditButton($(this), data.selfId); });
					tr.css('cursor', 'pointer');
					$('#taskList').append(tr);
				}
				if(data.taskList.length >= 10) {
					var atag = $("<a href='javascript:void(0)' class='btn btn-block btn-default'>").text('Read Next Page')
					.unbind('click').click(function() { reloadTask(parseInt(data.currentPage)+1); });
					var tr = $("<tr id='readNextPage'></tr>").append($("<td colspan='5'></td>").append(atag));
					$('#taskList').append(tr);
				}
			}
		},
		error:function() {
			alert("Fail to reload Task List.");
		}
	});
}


function changeToJavascript() {
	$('#editTool').remove();
	$('#dropActionContents').load("/public/html/dropdown.html", function() {
			$('#addTask').attr('href', 'javascript:void(0)')
				.unbind('click').click(function() { setAddTaskDialog(); });
			var modal = $('.modal');
			$('.modal').on('shown.bs.modal', function() {
				$(this).find("input[name='task.title']").focus();
			});
			$('#addTaskDialog').find("input[name='task.title']").keypress(function(event) {
				if(event.keyCode == 13) {
					return false;
				}
			});
			$('#editTaskDialog').find("input[name='task.title']").keypress(function(event) {
				if(event.keyCode == 13) {
					return false;
				}
			});
			reloadTask(1);
	});
	$('#fat-menu').remove();
	var userName = $('#dropUser').text();
	$('#dropUserContents').load("/public/html/dropdownUser.html", function() {
		$('#dropUser').text(userName).append($('<b class="caret"></b>'));
	});
}

function showEditButton(taskPath, selfId) {
	if(taskPath) {
		var id = taskPath.attr('taskid');
		var ownerId = taskPath.attr('ownerid');
		var title = taskPath.find('#taskTitle').get(0).textContent;
		
		var checkbox = $("<input type='checkbox' class='confirmedCheckbox' value='true' />")
		.unbind('change').change(function() {changeConfirm(this,id)});
		if(taskPath.find('#taskConfirmed').get(0).textContent == "true") {
			checkbox.attr('checked', 'checked');
		}
		
		var checkboxDiv = $('<label></label>').append(checkbox, document.createTextNode('Confirmed'));
		
		var editbutton = $("<button type='button' class='editButton btn btn-default'></button>").text('Edit')
		.unbind('click').click(function() {setEditTaskDialog(id);});
		 
		var deletebutton;
		if(selfId == ownerId) {
			deletebutton = $("<button type='button' class='deleteButton btn btn-default'></button>").text('Delete')
			.unbind('click').click(function() {setDeleteTaskDialog(id, title);});
		}
		
		var div = $("<tr class='taskItemEdit' taskid='" + id + "'></tr>").append($("<td colspan='5'></td>")
				.append(checkboxDiv, editbutton, deletebutton));
		
		taskPath.after(div);
		taskPath.unbind('click').click( function() { hideEditButton($(this), id, selfId); });
	}
}

function hideEditButton(taskPath, id, selfId) {
	var taskItem = taskPath.parent().find("tr[class='taskItemEdit'][taskid='" + id + "']");
	if(taskItem) {
		taskItem.remove();
		taskPath.unbind('click').click(function() { showEditButton($(this), selfId); });
	}
}

function changeConfirm(checkbox, id) {
	if(checkbox && id) {
		$.ajax({
			url:'/task/changeConfirm',
			type:'POST',
			data:{id:id, confirmed:checkbox.checked},
			success:function(data) {
				reloadTask(1);
			},
			error:function() {
				alert("Fail to Confirmed");
			}
		});
	}
}

function setEditTaskDialog(id) {
	$.ajax({
		url:'/task/findTask',
		type:'GET',
		data:{id:id},
		success: function(data) {
			if(data) {
				var dialog = $("#editTaskDialog");
				dialog.find("input[name='task.title']").val(data.task.Title);
				dialog.find("input[name='task.createDate']").val(toDateString(new Date(data.task.createDate)));
				dialog.find("select[name='task.priority']").val(data.task.priority);
				dialog.find("input[name='task.deadline']").val(toDateString(new Date(data.task.deadline)));
				dialog.find("#submitDialogButton").unbind('click').click(function() { editTask(id); });
				hideAlert('#errorEditAlert');
				
				var alluser = dialog.find("select[name='task.relative']");
				var seluser = dialog.find("select[name='task.selectedRelative']");
				alluser.empty();
				seluser.empty();
				$.each(data.userList, function(key, user) {
					alluser.append($("<option value='" + user.id + "'>" + user.name + " &lt;" + user.stringId + "&gt;</option>"));
				});
				$.each(data.selList, function(key, user) {
					var option = $('<option></option>').val(user.id);
					var name = user.name + " <" + user.stringId + ">";
					if(user.id == data.task.userId) {
						option.attr('disabled', '');
						name = name + " (Owner)"
					} 
					if(user.id == data.selfId) {
						name = name + " (You)";
					}
					option.text(name);
					seluser.append(option);
				});
				
				dialog.modal('show');
			}
		},
		error: function() {
			alert("Fail to get Task");
		}
	})
}

function hideAlert(id) {
	$(id).slideUp();
}

function editTask(id) {
	var dialog = $("#editTaskDialog");
	var createDate = dialog.find("input[name='task.createDate']");
	var titles = dialog.find("input[name='task.title']");
	var priority = dialog.find("select[name='task.priority']");
	var deadline = dialog.find("input[name='task.deadline']");
	var selList = dialog.find("select[name='task.selectedRelative']");
	var seluser = '';
	$.each(selList.children(), function(key, value) {
		seluser += value.value;
		if(selList.children().length != (key+1)) {
			seluser += ',';
		}
	});
	if(titles.length > 0) {
		var title = titles[0];
		if(!validateDialog(titles, createDate, priority, deadline, $('#errorEditAlert'), $('#errorEditContents'))) {
			$.ajax({
				url:'/task/editTaskJson',
				type:'POST',
				data:{'id':id,
					'title':title.value,
					'priority':priority.val(),
					'deadline':toDateString(new Date(deadline.val()), true),
					'selList':seluser},
				success: function(data) {
					dialog.modal('toggle');
					reloadTask(1);
				},
				error: function(XMLHttpRequest, textStatus, errorThrown) {
					$('#errorEditContents').text('Fail to edit Task.');
					$("#errorEditAlert").slideDown();
				}
			});
		}
	}
}

function setAddTaskDialog() {
	$.ajax({
		url:'/userListJson',
		type:'POST',
		success:function(data) {
			if(data) {
				var dialog = $('#addTaskDialog');
				hideAlert('#errorAddAlert');
				var title = dialog.find("input[name='task.title']");
				var confirmed = dialog.find("input[name='isConfirmed']");
				var priority = dialog.find("select[name='task.priority']");
				var deadline = dialog.find("input[name='task.deadline']");
				var alluser = dialog.find("select[name='task.relative']");
				var seluser = dialog.find("select[name='task.selectedRelative']");
				title.val('');
				confirmed.attr('checked', false);
				priority.val('Normal');
				var now = new Date();
				var tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate()+2,0,0,0,0);
				deadline.val(toDateString(tomorrow));
				alluser.empty();
				seluser.empty();
				$.each(data.userList, function(key, user) {
					if(data.selfId == user.id) {
						seluser.append($("<option value='" + user.id + "' disabled>" + user.name + " &lt;" + user.stringId + "&gt; (You)</option>"));
					} else {
						alluser.append($("<option value='" + user.id + "'>" + user.name + " &lt;" + user.stringId + "&gt;</option>"));
					}
				});
				dialog.modal('show');
			}
		},
		error:function() {
			alert("Fail to set dialog.");
		}
	});
	
}

function addTask() {
	var dialog = $('#addTaskDialog');
	if(dialog) {
		var title = dialog.find("input[name='task.title']");
		var confirmed = dialog.find("input[name='isConfirmed']");
		var priority = dialog.find("select[name='task.priority']");
		var deadline = dialog.find("input[name='task.deadline']");
		var selList = dialog.find("select[name='task.selectedRelative']");
		var seluser = '';
		$.each(selList.children(), function(key, value) {
			seluser += value.value;
			if(selList.children().length != (key+1)) {
				seluser += ',';
			}
		});
		
		if(!validateDialog(title, null, priority, deadline, $('#errorAddAlert'), $('#errorAddContents'))) {
			$.ajax({
				url:'/task/addTaskJson',
				type:'POST',
				data:{'task.title':title.val(),
					'task.priority':priority.val(),
					'deadline':toDateString(new Date(deadline.val()), true),
					'isConfirmed':confirmed.is(':checked'),
					'selList':seluser},
				success:function(data) {
					dialog.modal('toggle');
					reloadTask(1);
				},
				error:function(status) {
					$('#errorAddContents').text('Fail to add Task.');
					$('#errorAddAlert').slideDown();
				}
			});
		}
	}
}

function setDeleteTaskDialog(id, title) {
	var dialog = $('#deleteTaskDialog');
	dialog.find('#title').text("Are you sure you want to delete 「" + title + "」?");
	dialog.find("#submitDialogButton").unbind('click').click(function() { deleteTask(id); });
	dialog.modal('show');
}

function deleteTask(id) {
	if(id) {
		$.ajax({
			url:'/task/deleteTaskJson',
			type:'POST',
			data:{id:id},
			success:function() {
				$('#deleteTaskDialog').modal('toggle');
				reloadTask(1);
			},
			error:function() {
				alert("Fail to delete task.");
				$('#deleteTaskDialog').modal('toggle');
			}
		});
	}
}

function toDateString(date, isUTC) {
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var day = date.getDate();
	var hour = date.getHours();
	var minutes = date.getMinutes();
	if(isUTC) {
		year = date.getUTCFullYear();
		month = date.getUTCMonth() + 1;
		day = date.getUTCDate();
		hour = date.getUTCHours();
		minutes = date.getUTCMinutes();
	}
	if(month < 10) {
		month = '0' + month;
	}
	if(day < 10) {
		day = '0' + day;
	}
	if(hour < 10) {
		hour = '0' + hour;
	}
	if(minutes < 10) {
		minutes = '0' + minutes;
	}
	
	return year + '-' + month + '-' + day + 'T' + hour + ':' + minutes;
}

function validateDialog(title, createDate, priority, deadline, alert, alertContents) {
	var error = false;
	var errorString = '';
	if(title == null || title.val() == '') {
		errorString += '<p>Title is empty.</p>';
		error = true;
	}
	if(priority == null || priority.val() == '') {
		errorString += '<p>Priority is empty.</p>';
		error = true;
	}
	if(deadline == null || deadline.val() == '') {
		errorString += '<p>Deadline is empty.</p>';
		error = true;
	} else {
		var createDateSec = Date.parse(new Date());
		if(createDate) {
			createDateSec = Date.parse(new Date(createDate.val()));
		}
		var deadlineSec = Date.parse(deadline.val());
		if(!deadlineSec) {
			errorString += "<p>Deadline's format is not correct. It should be \"YYYY-MM-DD'T'HH:mm\".</p>";
			error = true;
		} else if(deadlineSec <= createDateSec) {
			errorString += '<p>Deadline is earlier than created Date.</p>';
			error = true;
		}
	}
	
	if(error) {
		alertContents.html(errorString);
		alert.slideDown();
	}
	return error;
}

function selectUser(dialogid) {
	var dialog = $(dialogid);
	var selList = dialog.find("select[name='task.selectedRelative']");
	var usrList = dialog.find("select[name='task.relative']");
	var selectedUserList = usrList.find(':selected');
	$.each(selectedUserList, function(key, value) {
		selList.append(value);
	});
	usrList.val('');
	selList.val('');
}

function deselectUser(dialogid) {
	var dialog = $(dialogid);
	var selList = dialog.find("select[name='task.selectedRelative']");
	var usrList = dialog.find("select[name='task.relative']");
	var selectedUserList = selList.find(':selected');
	$.each(selectedUserList, function(key, value) {
		usrList.append(value);
	});
	selList.val('');
	usrList.val('');
}

function selectAllUser(dialogid) {
	var dialog = $(dialogid);
	var selList = dialog.find("select[name='task.selectedRelative']");
	var usrList = dialog.find("select[name='task.relative']");
	$.each(usrList.children(), function(key, value) {
		selList.append(value);
	});
	selList.val('');
	usrList.val('');
}

function deselectAllUser(dialogid) {
	var dialog = $(dialogid);
	var selList = dialog.find("select[name='task.selectedRelative']");
	var usrList = dialog.find("select[name='task.relative']");
	var self = dialog.find(":disabled").get(0);
	$.each(selList.children(), function(key, value) {
		if(value != self) {
			usrList.append(value);
		}
	});
	selList.val('');
	usrList.val('');
}