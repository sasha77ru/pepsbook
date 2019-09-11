<%@ page isELIgnored="false" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!--by me -->
<%@ page session="false" isELIgnored="false" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<table class="table table-hover">
<thead>
	<tr>
		<th scope="col">Имя</th>
		<th scope="col">Страна</th>
		<th scope="col"></th>
	</tr>
</thead>
<tbody>
	<%--@elvariable id="lizt" type="java.util.List"--%>
    <%--@elvariable id="i" type="ru.sasha77.spring.pepsbook.User"--%>
    <%--@elvariable id="currUser" type="ru.sasha77.spring.pepsbook.User"--%>
	<c:forEach var="i" items="${lizt}">
	<tr>
		<td>${i.name}</td>
		<td>${i.country}</td>
		<td>
			<ul class="nav">
				<li class="nav-item dropdown">
					<a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"></a>
					<div class="dropdown-menu" x-placement="bottom-start" style="position: absolute; will-change: transform; top: 0px; left: 0px; transform: translate3d(0px, 40px, 0px);">
						<a class="dropdown-item toFriends" ${!currUser.friends.contains(i)?"":"style=\"display: none\""}
						   href="javascript:undefined"
						   onclick="toFriends(this.parentNode,${i.id})">В друзья</a>
						<a class="dropdown-item fromFriends" ${currUser.friends.contains(i)?"":"style=\"display: none\""}
						   href="javascript:undefined"
						   onclick="fromFriends(this.parentNode,${i.id})">Из друзей</a>
						<!--<a class="dropdown-item" href="#">во враги</a>
						<a class="dropdown-item" href="#">из врагов</a>-->
					</div>
				</li>
				<li class="friendship">
					${currUser.friends.contains(i)
						?(currUser.mates.contains(i)
							?"<span class=\"badge badge-success\">Взаимный Друг</span>"
							:"<span class=\"badge badge-primary\">ПолуДруг</span>")
						:(currUser.mates.contains(i)
							?"<span class=\"badge badge-secondary\">Почитатель</span>"
							:"")}
				</li>
			</ul>
		</td>
	</tr>
	</c:forEach>
</tbody>
</table>
