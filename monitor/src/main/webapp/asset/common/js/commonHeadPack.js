var monitorWebBaseURL = "/monitor/";
var monitorAssetBaseURL = "/monitor/asset/";
var HitTuningAssetBaseURL= "/HitTuning/asset/";
var HitTuningrWebBaseURL = "/HitTuning/";
/*西工大集群
var monitorWebBaseURL = "http://10.69.35.174:8090/monitor/";
var monitorAssetBaseURL = "http://10.69.35.174:8090/monitor/asset/";
var HitTuningAssetBaseURL= "http://10.69.35.174:8090/HitTuning/asset/";
var HitTuningWebBaseURL = "http://10.69.35.174:8090/HitTuning/";*/

/*清华集群*/
/*var monitorWebBaseURL = "http://101.6.240.89:18090/monitor/";
var monitorAssetBaseURL = "http://101.6.240.89:18090/monitor/asset/";
var HitTuningAssetBaseURL= "http://101.6.240.89:18090/HitTuning/asset/";
var HitTuningWebBaseURL = "http://101.6.240.89:18090/HitTuning/";*/


var monitorMetaDefine = '<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">';
var monitorCssDepandency = '\
	<link rel="shortcut icon" href="' + monitorAssetBaseURL + 'common/img/favicon.ico" type="image/x-icon" />\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'plugins/bootstrap/css/bootstrap.min.css">\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'common/css/BootstrapExpand.css">\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'plugins/other/font-awesome/css/font-awesome.min.css">\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'plugins/other/ionicons/css/ionicons.min.css">\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'plugins/datatables/dataTables.bootstrap.css">\
	<!-- Theme style -->\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'common/css/AdminLTE.min.css">\
	<!-- AdminLTE Skins. Choose a skin from the css/skins\
	     folder instead of downloading all of them to reduce the load. -->\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'common/css/skins/_all-skins.min.css">\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'common/css/style.css">\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'common/css/newstyle.css">\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'common/css/datepicker3.css" rel="stylesheet">\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'common/css/styles.css" rel="stylesheet">\
	<script src="' + monitorAssetBaseURL + 'plugins/pace/pace.js"></script>\
	<link rel="stylesheet" href="' + monitorAssetBaseURL + 'plugins/pace/pace.css">\
	<!--[if lt IE 9]>\
	<script src="' + monitorAssetBaseURL + 'plugins/other/html5shiv.min.js"></script>\
	<script src="' + monitorAssetBaseURL + 'plugins/other/respond.min.js"></script>\
	<![endif]-->\
';
document.write(monitorMetaDefine);
document.write(monitorCssDepandency);

Date.prototype.customFormat = function (formatString) {
	var YYYY, YY, MMMM, MMM, MM, M, DDDD, DDD, DD, D, hhhh, hhh, hh, h, mm, m, ss, s, ampm, AMPM, dMod, th;
	YY = ((YYYY = this.getFullYear()) + "").slice(-2);
	MM = (M = this.getMonth() + 1) < 10 ? ('0' + M) : M;
	MMM = (MMMM = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"][M - 1]).substring(0, 3);
	DD = (D = this.getDate()) < 10 ? ('0' + D) : D;
	DDD = (DDDD = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"][this.getDay()]).substring(0, 3);
	th = (D >= 10 && D <= 20) ? 'th' : ((dMod = D % 10) == 1) ? 'st' : (dMod == 2) ? 'nd' : (dMod == 3) ? 'rd' : 'th';
	formatString = formatString.replace("#YYYY#", YYYY).replace("#YY#", YY).replace("#MMMM#", MMMM).replace("#MMM#", MMM).replace("#MM#", MM).replace("#M#", M).replace("#DDDD#", DDDD).replace("#DDD#", DDD).replace("#DD#", DD).replace("#D#", D).replace("#th#", th);
	h = (hhh = this.getHours());
	if (h == 0) h = 24;
	if (h > 12) h -= 12;
	hh = h < 10 ? ('0' + h) : h;
	hhhh = hhh < 10 ? ('0' + hhh) : hhh;
	AMPM = (ampm = hhh < 12 ? 'am' : 'pm').toUpperCase();
	mm = (m = this.getMinutes()) < 10 ? ('0' + m) : m;
	ss = (s = this.getSeconds()) < 10 ? ('0' + s) : s;
	return formatString.replace("#hhhh#", hhhh).replace("#hhh#", hhh).replace("#hh#", hh).replace("#h#", h).replace("#mm#", mm).replace("#m#", m).replace("#ss#", ss).replace("#s#", s).replace("#ampm#", ampm).replace("#AMPM#", AMPM);
};

// token:     description:             example:
// #YYYY#     4-digit year             1999
// #YY#       2-digit year             99
// #MMMM#     full month name          February
// #MMM#      3-letter month name      Feb
// #MM#       2-digit month number     02
// #M#        month number             2
// #DDDD#     full weekday name        Wednesday
// #DDD#      3-letter weekday name    Wed
// #DD#       2-digit day number       09
// #D#        day number               9
// #th#       day ordinal suffix       nd
// #hhhh#     2-digit 24-based hour    17
// #hhh#      military/24-based hour   17
// #hh#       2-digit hour             05
// #h#        hour                     5
// #mm#       2-digit minute           07
// #m#        minute                   7
// #ss#       2-digit second           09
// #s#        second                   9
// #ampm#     "am" or "pm"             pm
// #AMPM#     "AM" or "PM"             PM