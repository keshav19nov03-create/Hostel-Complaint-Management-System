document.addEventListener("DOMContentLoaded", function () {
  var csrf = (document.querySelector('meta[name="csrf"]') || {}).content;
  var base = (document.querySelector('meta[name="base"]') || {}).content || "";

  // live character counter for the description box
  document.querySelectorAll("textarea[data-counter]").forEach(function (t) {
    var out = document.getElementById(t.dataset.counter);
    var upd = function () { out.textContent = t.value.length + " / 500 characters"; };
    t.addEventListener("input", upd); upd();
  });

  function toast(msg) {
    var el = document.getElementById("toast"); if (!el) return;
    el.textContent = msg; el.style.display = "block";
    setTimeout(function () { el.style.display = "none"; }, 2200);
  }

  // AJAX: warden changes status straight from the table
  document.querySelectorAll("select.js-status").forEach(function (sel) {
    sel.addEventListener("change", function () {
      fetch(base + "/api/complaint/" + sel.dataset.id + "/status", {
        method: "POST",
        headers: {"Content-Type": "application/json", "X-CSRF-Token": csrf},
        body: JSON.stringify({status: sel.value})
      }).then(function (r) { return r.json(); }).then(function (d) {
        if (d.ok) { toast("Status updated to " + d.status); drawCharts(d.stats); }
      });
    });
  });

  // simple bar charts drawn from /api/stats
  function bars(el, data, colors) {
    var max = Math.max.apply(null, Object.values(data).concat([1]));
    el.innerHTML = Object.keys(data).map(function (k) {
      var w = Math.round(data[k] / max * 100);
      return '<div class="bar"><span class="nm">' + k + '</span><div class="tr"><div class="fl" style="width:' +
        w + '%;background:' + ((colors && colors[k]) || "#1f2d4d") + '"></div></div><span class="v">' + data[k] + "</span></div>";
    }).join("");
  }
  function drawCharts(s) {
    var a = document.getElementById("chart-status"), b = document.getElementById("chart-category");
    if (a) bars(a, s.by_status, {"Pending": "#d98a00", "In Progress": "#2563eb", "Resolved": "#1a8a4a", "Rejected": "#a51c30"});
    if (b) bars(b, s.by_category);
  }
  if (document.getElementById("chart-status")) {
    fetch(base + "/api/stats").then(function (r) { return r.json(); }).then(drawCharts);
  }
});
