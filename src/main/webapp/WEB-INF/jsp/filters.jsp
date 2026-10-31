<%@ page import="java.util.*,edu.cu.hcms.util.*" %>
<%-- Search / filter bar. Expects: flt (Map<String,String>) and filterAction (String) as request attributes. --%>
<%
    Map<String, String> fltMap = (Map<String, String>) request.getAttribute("flt");
    String filterAction = (String) request.getAttribute("filterAction");
%>
<form class="filters" method="get" action="<%= filterAction %>">
  <div><label>Search</label><input name="q" value="<%= Html.val(fltMap, "q") %>" placeholder="Ticket, title or student"></div>
  <div><label>Status</label><select name="status"><%= Html.options(Config.STATUSES, fltMap.get("status"), true) %></select></div>
  <div><label>Category</label><select name="category"><%= Html.options(Config.CATEGORIES, fltMap.get("category"), true) %></select></div>
  <div><label>Priority</label><select name="priority"><%= Html.options(Config.PRIORITIES, fltMap.get("priority"), true) %></select></div>
  <button class="btn">Filter</button>
</form>
