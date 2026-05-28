window.UI = (function () {
  var toastContainer = null;

  function getToastContainer() {
    if (!toastContainer) {
      toastContainer = document.createElement('div');
      toastContainer.className = 'toast-container';
      document.body.appendChild(toastContainer);
    }
    return toastContainer;
  }

  function toast(message, type, duration) {
    type     = type     || 'info';
    duration = duration || 3500;
    var icons = { success: '✓', error: '✕', info: 'ℹ' };
    var el = document.createElement('div');
    el.className = 'toast ' + type;
    el.innerHTML = '<span style="font-size:1.1rem">' + (icons[type] || icons.info) + '</span><span>' + message + '</span>';
    getToastContainer().appendChild(el);
    setTimeout(function () {
      el.style.animation = 'fadeOut 0.3s ease forwards';
      setTimeout(function () { el.remove(); }, 300);
    }, duration);
  }

  function openModal(id) {
    var el = document.getElementById(id);
    if (el) el.classList.add('open');
  }

  function closeModal(id) {
    var el = document.getElementById(id);
    if (el) el.classList.remove('open');
  }

  function setLoading(btn, loading) {
    if (loading) {
      btn.dataset.orig = btn.innerHTML;
      btn.innerHTML = '<span class="spinner"></span>';
      btn.disabled = true;
    } else {
      btn.innerHTML = btn.dataset.orig || btn.innerHTML;
      btn.disabled = false;
    }
  }

  function renderLoading(container) {
    container.innerHTML = '<div class="loading-overlay"><div class="spinner" style="width:32px;height:32px;border-width:3px"></div><span>Loading...</span></div>';
  }

  function renderEmpty(container, icon, title, subtitle) {
    container.innerHTML =
      '<div class="empty-state">' +
        '<div class="empty-icon">' + icon + '</div>' +
        '<h3>' + title + '</h3>' +
        (subtitle ? '<p>' + subtitle + '</p>' : '') +
      '</div>';
  }

  function formatPrice(price) {
    if (!price || price === 0) return '<span class="price-free">Free</span>';
    return '<span class="price-tag">$' + Number(price).toFixed(2) + '</span>';
  }

  function formatDate(dt) {
    if (!dt) return '—';
    return new Date(dt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  var EMOJIS = ['📚','💻','🎓','🧠','🔬','🎨','📊','🌐','⚡','🔧','🚀','🎯'];
  function courseEmoji(id) {
    return EMOJIS[(id || 0) % EMOJIS.length];
  }

  function esc(s) {
    return String(s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  }

  // Close modal on overlay click
  document.addEventListener('click', function (e) {
    if (e.target.classList && e.target.classList.contains('modal-overlay')) {
      e.target.classList.remove('open');
    }
  });

  return {
    toast: toast,
    openModal: openModal,
    closeModal: closeModal,
    setLoading: setLoading,
    renderLoading: renderLoading,
    renderEmpty: renderEmpty,
    formatPrice: formatPrice,
    formatDate: formatDate,
    courseEmoji: courseEmoji,
    esc: esc,
  };
})();
