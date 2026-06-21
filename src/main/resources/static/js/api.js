window.api = (function () {
  var BASE = '/api';

  function getToken() {
    return localStorage.getItem('lms_token');
  }

  function authHeaders() {
    var h = { 'Content-Type': 'application/json' };
    var token = getToken();
    if (token) h['Authorization'] = 'Bearer ' + token;
    return h;
  }

  async function request(method, path, body) {
    var opts = { method: method, headers: authHeaders() };
    if (body && !(body instanceof FormData)) {
      opts.body = JSON.stringify(body);
    } else if (body instanceof FormData) {
      opts.body = body;
      delete opts.headers['Content-Type'];
    }
    var res = await fetch(BASE + path, opts);
    if (res.status === 204) return null;
    var text = await res.text();
    var data;
    try { data = JSON.parse(text); } catch (e) { data = text; }
    if (!res.ok) {
      var msg = (data && data.message) || (data && data.error) || data || ('HTTP ' + res.status);
      throw new Error(msg);
    }
    return data;
  }

  return {
    auth: {
      login:      function (b) { return request('POST', '/v1/users/login', b); },
      register:   function (b) { return request('POST', '/v1/users/register', b); },
      verify:     function (token) { return request('GET',  '/v1/users/verify?token=' + encodeURIComponent(token)); },
      resendVerification: function (email) { return request('POST', '/v1/users/resend-verification', { email: email }); },
      allUsers:   function ()  { return request('GET',  '/v1/users/all'); },
      deleteUser: function (id){ return request('DELETE', '/v1/users/delete/' + id); },
      updateUser: function (id, b){ return request('PUT', '/v1/users/update/' + id, b); },
      setRole:    function (id, role){ return request('PUT', '/v1/users/' + id + '/role', { role: role }); },
    },
    courses: {
      all:    function (p, s) { return request('GET', '/course/all?pageNo=' + (p||1) + '&pageSize=' + (s||12) + '&sortBy=id&sortDir=asc'); },
      find:   function (id)   { return request('GET', '/course/find/' + id); },
      create: function (b)    { return request('POST',   '/course/add', b); },
      update: function (id,b) { return request('PUT',    '/course/update/' + id, b); },
      delete: function (id,a) { return request('DELETE', '/course/delete/' + id + '?isActive=' + a); },
    },
    enrollments: {
      all:    function ()  { return request('GET',  '/vi/enrollments/all'); },
      my:     function ()  { return request('GET',  '/vi/enrollments/my'); },
      enroll: function (b) { return request('POST', '/vi/enrollments', b); },
      progress: function (studentId, courseId) { return request('GET', '/vi/enrollments/progress?studentId=' + studentId + '&courseId=' + courseId); },
    },
    modules: {
      byCourse:  function (cid)    { return request('GET',  '/v1/modules/course/' + cid); },
      create:    function (b)      { return request('POST', '/v1/modules/create', b); },
      setStatus: function (mid, a) { return request('PUT',  '/v1/modules/update/' + mid + '/status?isActive=' + a); },
    },
    tasks: {
      byModule: function (mid) { return request('GET',  '/v1/tasks/module/' + mid); },
      create:   function (b)   { return request('POST', '/v1/tasks', b); },
    },
    submissions: {
      // Backend uploads the file straight to MinIO's private bucket — just send the multipart form.
      submit:   function (taskId, file) {
        var fd = new FormData();
        fd.append('taskId', taskId);
        fd.append('file', file);
        return request('POST', '/v1/submission/submit', fd);
      },
      ungraded: function ()         { return request('GET',  '/v1/submission/ungraded'); },
      // Returns a short-lived MinIO presigned URL for the submission file.
      fileUrl:  function (id)       { return request('GET',  '/v1/submission/' + id + '/file'); },
      grade:    function (id, sc, fb){ return request('PUT', '/v1/submission/' + id + '/grade?score=' + sc + '&teacherFeedback=' + encodeURIComponent(fb)); },

      // ✨ NEW: Get all graded submissions for current student
      getStudentGraded: function () { return request('GET', '/v1/submission/student/graded'); },

      // ✨ NEW: Get feedback and score for a specific submission
      getFeedback: function (id) { return request('GET', '/v1/submission/' + id + '/feedback'); },
    },
    payments: {
      createOrder: function (b)  { return request('POST', '/payments/orders/create', b); },
      captureOrder: function (id){ return request('POST', '/payments/orders/' + id + '/capture'); },
    },
    files: {
      upload: function (file) {
        var fd = new FormData();
        fd.append('file', file);
        return request('POST', '/v1/files/upload', fd);
      },
    },
  };
})();
