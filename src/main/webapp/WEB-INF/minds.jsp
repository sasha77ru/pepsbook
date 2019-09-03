<%@ page isELIgnored="false" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page session="false" isELIgnored="false" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%--@elvariable id="lizt" type="java.util.List"--%>
<%--@elvariable id="i" type="ru.sasha77.spring.pepsbook.Mind"--%>
<%--@elvariable id="currUser" type="ru.sasha77.spring.pepsbook.User"--%>
<c:forEach var="i" items="${lizt}">
	<div class="card ${i.user.id==currUser.id?"border-primary":"border-light"} mb-3 mindEntity">
		<div class="card-header">
				${i.time}
				<span class="nav-item dropdown" style="display: inline">
					<a style="display: inline" class="nav-link dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"></a>
					<div class="dropdown-menu" x-placement="bottom-start" style="position: absolute; will-change: transform; top: 0px; left: 0px; transform: translate3d(0px, 40px, 0px);">
						<a class="dropdown-item ownerMenu" ${i.user.id==currUser.id?"":"style=\"display: none\""}
						   href="javascript:undefined"
						   onclick="openNewMindWindow(this,${i.id})">Редактировать</a>
						<a class="dropdown-item ownerMenu" ${i.user.id==currUser.id?"":"style=\"display: none\""}
						   href="javascript:removeMind(${i.id})">Удалить</a>
						<a class="dropdown-item">Ответить</a>
					</div>
				</span>
		</div>
		<div class="card-body">
			<h4 class="card-title">${i.user.name}</h4>
			<p class="card-text mindText">${i.text}</p>
		</div>
	</div>
</c:forEach>
<a class="btn btn-primary btn-lg" href="javascript:openNewMindWindow()" role="button" style="display: block;width: 40%;margin: 1em auto">Новая мысль</a>
