<%@ include file="/WEB-INF/jsp/header.jsp" %>
<%
    List<Complaint> rows = (List<Complaint>) request.getAttribute("rows");
    request.setAttribute("filterAction", ctx + "/complaints");
%>
<h1>My Complaints</h1><p class="sub">Search and filter every complaint you have raised.</p>
<div class="card"><jsp:include page="/WEB-INF/jsp/filters.jsp"/>
<% if (rows.isEmpty()) { %>
  <p class="sub">No complaints match your filters.</p>
<% } else { %>
<table><tr><th>Ticket</th><th>Title</th><th>Category</th><th>Priority</th><th>Status</th><th>Last update</th></tr>
<% for (Complaint c : rows) { %>
<tr><td><a href="<%= ctx %>/complaint/<%= c.id() %>"><b><%= Html.esc(c.ticketNo()) %></b></a></td><td><%= Html.esc(c.title()) %></td><td><%= Html.esc(c.category()) %></td>
<td><%= Html.prio(c.priority()) %></td><td><%= Html.status(c.status()) %></td><td><%= c.updatedAt() %></td></tr>
<% } %>
</table>
<% } %>
</div>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
