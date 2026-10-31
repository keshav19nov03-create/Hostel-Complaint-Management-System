<%@ include file="/WEB-INF/jsp/header.jsp" %>
<%
    Complaint c = (Complaint) request.getAttribute("c");
    List<Update> timeline = (List<Update>) request.getAttribute("timeline");
%>
<div class="card"><div style="display:flex;align-items:center;gap:12px"><h1 style="margin:0"><%= Html.esc(c.title()) %></h1><%= Html.status(c.status()) %><%= Html.prio(c.priority()) %></div>
<p class="sub" style="margin:4px 0 0">Ticket <%= Html.esc(c.ticketNo()) %></p>
<div class="meta"><div><span>Raised by</span><b><%= Html.esc(c.student()) %> (<%= Html.esc(c.studentUid()) %>)</b></div><div><span>Location</span><b>Block <%= Html.esc(c.block()) %>, Room <%= Html.esc(c.room()) %></b></div>
<div><span>Category</span><b><%= Html.esc(c.category()) %></b></div><div><span>Raised on</span><b><%= c.createdAt() %></b></div></div>
<h2>Description</h2><p style="margin:0"><%= Html.esc(c.description()) %></p></div>
<div class="grid g2"><div class="card"><h2>Progress timeline</h2><div class="tl">
<% for (Update t : timeline) { %>
<div class="it <%= t.byAdmin() ? "adm" : "" %>"><b><%= Html.esc(t.userName()) %></b> <% if (t.statusTo() != null) { %>&rarr; <%= Html.status(t.statusTo()) %><% } %><br><%= Html.esc(t.message()) %><br><small><%= t.createdAt() %></small></div>
<% } %>
</div></div>
<div class="card">
<% if (me.isAdmin()) { %>
<h2>Update status (Warden)</h2>
<form method="post" action="<%= ctx %>/complaint/<%= c.id() %>/update"><input type="hidden" name="_csrf" value="<%= csrf %>">
<div class="fld"><label>New status</label><select name="status"><%= Html.options(Config.STATUSES, c.status(), false) %></select></div>
<div class="fld"><label>Remarks for student</label><textarea name="remarks" style="min-height:90px" placeholder="e.g. Electrician assigned, will visit tomorrow."></textarea></div>
<button class="btn red">Save Update</button></form>
<% } else { %>
<h2>Add a comment</h2>
<form method="post" action="<%= ctx %>/complaint/<%= c.id() %>/comment"><input type="hidden" name="_csrf" value="<%= csrf %>">
<div class="fld"><textarea name="message" style="min-height:90px" placeholder="Add more details or ask for an update"></textarea></div><button class="btn">Post Comment</button></form>
<% } %>
</div></div>
<a class="btn out" href="<%= ctx %>/dashboard">&larr; Back</a>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
