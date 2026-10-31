<%@ include file="/WEB-INF/jsp/header.jsp" %>
<% Map<String, String> f = (Map<String, String>) request.getAttribute("f"); %>
<div class="auth wide"><div class="card">
<h1>Student Registration</h1><p class="sub">Create your hostel account.</p>
<form method="post" action="<%= ctx %>/register"><input type="hidden" name="_csrf" value="<%= csrf %>">
<div class="row"><div><label>Full name</label><input name="name" value="<%= Html.val(f, "name") %>" required></div>
<div><label>University UID</label><input name="uid" value="<%= Html.val(f, "uid") %>" placeholder="26MCC20092" required></div></div>
<div class="fld"><label>E-mail</label><input type="email" name="email" value="<%= Html.val(f, "email") %>" required></div>
<div class="row"><div><label>Hostel block</label><select name="block"><%= Html.options(Config.BLOCKS, f == null ? null : f.get("block"), false) %></select></div>
<div><label>Room number</label><input name="room" value="<%= Html.val(f, "room") %>" required></div></div>
<div class="fld"><label>Password (min 6 characters)</label><input type="password" name="password" required></div>
<button class="btn red">Register</button></form></div></div>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
