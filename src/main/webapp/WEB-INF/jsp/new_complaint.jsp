<%@ include file="/WEB-INF/jsp/header.jsp" %>
<% Map<String, String> f = (Map<String, String>) request.getAttribute("f"); %>
<div class="auth wide" style="max-width:720px"><div class="card">
<h1>Raise a Complaint</h1><p class="sub">Describe the problem clearly so that the warden can resolve it quickly.</p>
<form method="post" action="<%= ctx %>/complaint/new"><input type="hidden" name="_csrf" value="<%= csrf %>">
<div class="fld"><label>Complaint title</label><input name="title" value="<%= Html.val(f, "title") %>" maxlength="80" required placeholder="e.g. Ceiling fan not working"></div>
<div class="row"><div><label>Category</label><select name="category"><%= Html.options(Config.CATEGORIES, f.get("category"), false) %></select></div>
<div><label>Priority</label><select name="priority"><%= Html.options(Config.PRIORITIES, f.get("priority") == null ? "Medium" : f.get("priority"), false) %></select></div></div>
<div class="row"><div><label>Block</label><select name="block"><%= Html.options(Config.BLOCKS, f.get("block"), false) %></select></div>
<div><label>Room number</label><input name="room" value="<%= Html.val(f, "room") %>" required></div></div>
<div class="fld"><label>Description</label><textarea name="description" maxlength="500" data-counter="cnt" required><%= Html.val(f, "description") %></textarea><div class="hint" id="cnt"></div></div>
<button class="btn red">Submit Complaint</button> <a class="btn out" href="<%= ctx %>/dashboard">Cancel</a></form></div></div>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
