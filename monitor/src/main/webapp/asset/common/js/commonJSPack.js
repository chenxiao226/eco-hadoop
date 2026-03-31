var AdminLTEOptions = {
    //Enable sidebar expand on hover effect for sidebar mini
    //This option is forced to true if both the fixed layout and sidebar mini
    //are used together
    //BoxRefresh Plugin
    enableBoxRefresh: true,
    //Bootstrap.js tooltip
    enableBSToppltip: true,
    animationSpeed: 200,
    enableControlTreeView: false
};
var monitorJSDepandency = '\
	<script src="'+monitorAssetBaseURL+'plugins/jQuery/jquery-2.2.3.min.js"></script>\
	<script src="'+monitorAssetBaseURL+'plugins/bootstrap/js/bootstrap.min.js"></script>\
	<script src="'+monitorAssetBaseURL+'plugins/datatables/jquery.dataTables.min.js"></script>\
	<script src="'+monitorAssetBaseURL+'plugins/datatables/dataTables.bootstrap.min.js"></script>\
	<script src="'+monitorAssetBaseURL+'plugins/fastclick/fastclick.js"></script>\
	<script src="'+monitorAssetBaseURL+'common/js/resize.js"></script>\
	<!-- AdminLTE App -->\
	<script src="'+monitorAssetBaseURL+'common/js/app.min.js"></script>\
	<script src="'+monitorAssetBaseURL+'plugins/slimScroll/jquery.slimscroll.min.js"></script>\
';
document.write(monitorJSDepandency);