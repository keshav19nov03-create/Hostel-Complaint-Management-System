<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,edu.cu.hcms.model.*,edu.cu.hcms.util.*,edu.cu.hcms.web.*" %>
<%
    User me = (User) request.getAttribute("user");
    String ctx = request.getContextPath();
    String csrf = Csrf.token(request.getSession());
    String pageTitle = (String) request.getAttribute("pageTitle");
    if (pageTitle == null) { pageTitle = "Home"; }
%>
<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="csrf" content="<%= csrf %>"><meta name="base" content="<%= ctx %>">
<title><%= Html.esc(pageTitle) %> | Hostel Complaint Management System</title>
<link rel="stylesheet" href="<%= ctx %>/static/style.css"></head>
<body>
<header class="nav"><div class="wrap nav-in">
  <a class="brand" href="<%= ctx %>/"><span class="logo">H</span>Hostel Complaint Management System</a>
  <% if (me != null) { %>
    <% if (me.isAdmin()) { %>
      <a class="lnk" href="<%= ctx %>/admin">All Complaints</a>
    <% } else { %>
      <a class="lnk" href="<%= ctx %>/dashboard">Dashboard</a>
      <a class="lnk" href="<%= ctx %>/complaint/new">New Complaint</a>
      <a class="lnk" href="<%= ctx %>/complaints">My Complaints</a>
    <% } %>
    <span class="sp"></span><span class="chip"><b><%= Html.esc(me.name()) %></b> (<%= me.isAdmin() ? "Warden" : "Student" %>)</span>
    <form method="post" action="<%= ctx %>/logout"><input type="hidden" name="_csrf" value="<%= csrf %>"><button class="btn ghost">Logout</button></form>
  <% } else { %>
    <span class="sp"></span><a class="lnk" href="<%= ctx %>/login">Login</a><a class="lnk" href="<%= ctx %>/register">Register</a>
  <% } %>
</div></header>
<main><div class="wrap">
<% for (String[] fl : Flash.take(request.getSession())) { %>
  <div class="flash <%= fl[0] %>"><%= Html.esc(fl[1]) %></div>
<% } %>
