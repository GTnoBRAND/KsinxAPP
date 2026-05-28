window.Auth = (function () {
  function saveSession(token, user) {
    localStorage.setItem('lms_token', token);
    localStorage.setItem('lms_user', JSON.stringify(user));
  }

  function clearSession() {
    localStorage.removeItem('lms_token');
    localStorage.removeItem('lms_user');
  }

  function getSession() {
    var token = localStorage.getItem('lms_token');
    var raw   = localStorage.getItem('lms_user');
    if (!token || !raw) return null;
    try { return { token: token, user: JSON.parse(raw) }; }
    catch (e) { return null; }
  }

  function requireAuth(allowedRoles) {
    var session = getSession();
    if (!session) { window.location.href = '/login.html'; return null; }
    if (allowedRoles && allowedRoles.length && !allowedRoles.includes(session.user.role)) {
      window.location.href = '/index.html'; return null;
    }
    return session;
  }

  function initNavUser() {
    var actionsEl = document.getElementById('navbar-actions');
    if (!actionsEl) return;
    var session = getSession();
    if (session) {
      var role     = session.user.role || 'STUDENT';
      var initials = (session.user.fullName || 'U').split(' ').map(function(w){ return w[0]; }).join('').slice(0,2).toUpperCase();
      var dashLink = role === 'ADMIN' ? '/admin.html' : role === 'TEACHER' ? '/teacher.html' : '/dashboard.html';
      actionsEl.innerHTML =
        '<a href="' + dashLink + '" class="btn btn-ghost btn-sm">Dashboard</a>' +
        '<div class="navbar-user">' +
          '<div class="avatar" title="' + session.user.fullName + '">' + initials + '</div>' +
          '<span>' + session.user.fullName.split(' ')[0] + '</span>' +
        '</div>' +
        '<button class="btn btn-outline btn-sm" onclick="Auth.logout()">Logout</button>';
    } else {
      actionsEl.innerHTML =
        '<a href="/login.html" class="btn btn-ghost btn-sm">Log in</a>' +
        '<a href="/register.html" class="btn btn-primary btn-sm">Get Started</a>';
    }
  }

  function logout() {
    clearSession();
    window.location.href = '/index.html';
  }

  return { saveSession: saveSession, clearSession: clearSession, getSession: getSession, requireAuth: requireAuth, initNavUser: initNavUser, logout: logout };
})();
