<%@ include file="/WEB-INF/jsp/header.jsp" %>
<%
    List<Complaint> rows = (List<Complaint>) request.getAttribute("rows");
    Stats stats = (Stats) request.getAttribute("stats");
    request.setAttribute("filterAction", ctx + "/admin");
%>
<h1>Warden Dashboard</h1><p class="sub">Monitor every complaint, change status instantly and track resolution time.</p>
<div class="grid g5" style="margin-bottom:18px">
<div class="stat"><div class="n"><%= stats.total() %></div><div class="l">Total complaints</div></div>
<div class="stat s-pending"><div class="n"><%= stats.count("Pending") %></div><div class="l">Pending</div></div>
<div class="stat s-in-progress"><div class="n"><%= stats.count("In Progress") %></div><div class="l">In progress</div></div>
<div class="stat s-resolved"><div class="n"><%= stats.count("Resolved") %></div><div class="l">Resolved</div></div>
<div class="stat red"><div class="n"><%= stats.avgResolutionHours() == null ? "-" : stats.avgResolutionHours().toString() %><small style="font-size:14px"> hrs</small></div><div class="l">Avg. resolution time</div></div></div>
<div class="grid g2"><div class="card"><h2>Complaints by status</h2><div id="chart-status"></div></div><div class="card"><h2>Complaints by category</h2><div id="chart-category"></div></div></div>
<div class="card"><h2>All complaints</h2><jsp:include page="/WEB-INF/jsp/filters.jsp"/>
<table><tr><th>Ticket</th><th>Student</th><th>Room</th><th>Title</th><th>Category</th><th>Priority</th><th>Status</th><th></th></tr>
<% for (Complaint c : rows) { %>
<tr><td><b><%= Html.esc(c.ticketNo()) %></b></td><td><%= Html.esc(c.student()) %></td><td><%= Html.esc(c.block()) %>-<%= Html.esc(c.room()) %></td><td><%= Html.esc(c.title()) %></td><td><%= Html.esc(c.category()) %></td><td><%= Html.prio(c.priority()) %></td>
<td><select class="inline-sel js-status" data-id="<%= c.id() %>"><%= Html.options(Config.STATUSES, c.status(), false) %></select></td>
<td><a href="<%= ctx %>/complaint/<%= c.id() %>">View</a></td></tr>
<% } %>
<% if (rows.isEmpty()) { %><tr><td colspan="8">No complaints found.</td></tr><% } %>
</table></div>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
