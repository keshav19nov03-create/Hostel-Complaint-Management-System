<%@ include file="/WEB-INF/jsp/header.jsp" %>
<div class="auth"><div class="card">
<h1>Login</h1><p class="sub">Sign in to raise or track hostel complaints.</p>
<form method="post" action="<%= ctx %>/login"><input type="hidden" name="_csrf" value="<%= csrf %>">
<div class="fld"><label>E-mail</label><input type="email" name="email" required placeholder="you@example.com"></div>
<div class="fld"><label>Password</label><input type="password" name="password" required></div>
<button class="btn red" style="width:100%">Login</button></form>
<p class="sub" style="margin:14px 0 0;text-align:center">New student? <a href="<%= ctx %>/register"><b>Create an account</b></a></p>
</div></div>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
