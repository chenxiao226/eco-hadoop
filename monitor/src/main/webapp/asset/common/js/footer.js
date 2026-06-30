(function(){
    var footer = '<div style="position:fixed;bottom:0;right:0;padding:4px 14px;color:#666;font-size:13px;font-family:Arial,sans-serif;z-index:9999;text-align:right;line-height:1.6;">' +
        '<div id="footerClock"></div>' +
        '<div>Copyright &copy; 2026-2027 MDC@HIT. All rights reserved.</div>' +
        '</div>';
    document.write(footer);

    function updateClock() {
        var el = document.getElementById('footerClock');
        if (!el) return;
        var now = new Date();
        var pad = function(n) { return n < 10 ? '0' + n : n; };
        var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
        var offset = -now.getTimezoneOffset();
        var sign = offset >= 0 ? '+' : '-';
        var absOffset = Math.abs(offset);
        var tzStr = 'UTC' + sign + pad(Math.floor(absOffset/60)) + ':' + pad(absOffset%60);
        el.textContent = months[now.getMonth()] + ' ' + pad(now.getDate()) + ', ' + now.getFullYear() +
            '  ' + pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds()) +
            '  ' + tzStr;
    }

    document.addEventListener('DOMContentLoaded', function () {
        updateClock();
        setInterval(updateClock, 1000);
    });
})();
