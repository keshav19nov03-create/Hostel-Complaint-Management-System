<%@ include file="/WEB-INF/jsp/header.jsp" %>
<%
    Stats stats = (Stats) request.getAttribute("stats");
    List<Complaint> recent = (List<Complaint>) request.getAttribute("recent");
%>
<div style="display:flex;align-items:center"><div><h1>Welcome, <%= Html.esc(me.name()) %></h1>
<p class="sub">UID <%= Html.esc(me.uid()) %> &middot; Block <%= Html.esc(me.block()) %>, Room <%= Html.esc(me.room()) %></p></div><span class="sp" style="flex:1"></span>
<a class="btn red" href="<%= ctx %>/complaint/new">+ Raise New Complaint</a></div>
<div class="grid g4" style="margin:14px 0 18px">
<div class="stat"><div class="n"><%= stats.total() %></div><div class="l">Total complaints</div></div>
<div class="stat s-pending"><div class="n"><%= stats.count("Pending") %></div><div class="l">Pending</div></div>
<div class="stat s-in-progress"><div class="n"><%= stats.count("In Progress") %></div><div class="l">In progress</div></div>
<div class="stat s-resolved"><div class="n"><%= stats.count("Resolved") %></div><div class="l">Resolved</div></div></div>
<div class="card"><h2>Recent complaints</h2>
<% if (recent.isEmpty()) { %>
  <p class="sub">You have not raised any complaint yet.</p>
<% } else { %>
<table><tr><th>Ticket</th><th>Title</th><th>Category</th><th>Priority</th><th>Status</th><th>Raised on</th></tr>
<% for (Complaint c : recent) { %>
<tr><td><a href="<%= ctx %>/complaint/<%= c.id() %>"><b><%= Html.esc(c.ticketNo()) %></b></a></td><td><%= Html.esc(c.title()) %></td><td><%= Html.esc(c.category()) %></td>
<td><%= Html.prio(c.priority()) %></td><td><%= Html.status(c.status()) %></td><td><%= c.createdAt() %></td></tr>
<% } %>
</table>
<% } %>
</div>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
