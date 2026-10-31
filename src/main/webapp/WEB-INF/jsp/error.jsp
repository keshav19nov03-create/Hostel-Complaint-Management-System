<%@ page isErrorPage="true" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>
<%
    Object codeObj = request.getAttribute("jakarta.servlet.error.status_code");
    int code = codeObj == null ? 500 : (Integer) codeObj;
    String msg = code == 403 ? "You are not allowed to view this page."
               : code == 404 ? "The page you requested does not exist."
               : code == 400 ? "The request could not be processed (bad request or expired form)."
               : "Something went wrong on the server.";
%>
<div class="card"><div class="big"><%= code %></div><p class="sub" style="text-align:center"><%= msg %></p>
<p style="text-align:center"><a class="btn" href="<%= ctx %>/">Go to home</a></p></div>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
