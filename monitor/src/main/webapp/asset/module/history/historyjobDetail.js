(function() {
	// 获得参数Url？后面的参数
	function GetRequest() {
		var url = location.search; // 获取url中"?"符后的字串
		var theRequest = new Object();
		if (url.indexOf("?") != -1) {
			var str = url.substr(1);
			strs = str.split("&");
			for (var i = 0; i < strs.length; i++) {
				theRequest[strs[i].split("=")[0]] = unescape(strs[i].split("=")[1]);
			}
		}
		return theRequest;
	}
	// 初始化
	$(function() {
		var Request = new Object();
		Request = GetRequest();
		var Id = Request['Id'];
		if (Id.indexOf("#") > 0) { // 如果是带有#号，表明是<a>的标签操作
			return;// 终止
		}

		$.ajax({
			type : "POST",
			url : monitorWebBaseURL + "history/getHistoryDetailsById",
			cache : false,
			data : {
				'AppId' : Id
			},
			dataType : 'json',
			success : function(json) {
				var s = json.module;
				initAppResult(s);

				$(".heuristic_a").click(function() {
					var colClassName = 'metricsCol' + $(this).data('index');
					$("."+colClassName).click();
				})
			}
		});
	});

	function initAppResult(AppResult) {

		var app = AppResult;

		var user = app.username;
		var appid = app.id;
		var time = app.startTime;
		var name = app.name;
		var jobTrack = app.trackingUrl;
		var jobtype = app.jobType;
        var total_score = app.score;
		var HeList = app.yarnAppHeuristicResultDOList;

		var appjob = "<p class=\"panel-title\"></p>"
				+ "<p class=\"list-group-item-heading\"></p>"
				+ "<div class=\"left\">"
				+ "<h3 class=\"list-group-item-heading\">["
				+ user
				+ "] ["
				+ jobtype
				+ "] "
				+ appid
				+ "</h3>"
				+ "</div>"
				+ "<div id=\"time_application_1490880820138_0001\" class=\"right\"><h4>"
				+ formatDateTime(time) + "</h4></div>" + "<div><h4>"+name+"</h4></div>";

		appjob = appjob
				+ "<div><h4>Jobtracker:<a	href=\""+jobTrack+"\">"
				+ jobTrack + "</a></h4></div><br>";
		appjob = appjob + "<div>";
		for ( var i in HeList) {
			// alert(HeList[i].heuristicName); //根据不同的颜色
			appjob = appjob + "<a href=\"" + monitorWebBaseURL
					+ "asset/module/history/historyjobDetail.html?Id=" + appid
					+ "#" + HeList[i].heuristicName
					+ "\" data-index="+i+" class= \"label label-success heuristic_a\">"
					+ HeList[i].heuristicName + "</a> ";
		}
		appjob = appjob + "</div>";

		//终止动画
		$("#spinicon").css('display','none');
		$("#appJob").append(appjob);
		//更新得分
		$("#total_score").html(total_score); 
		var len = HeList.length;
		var flag = len % 2;
		var half = Math.floor(len / 2);
		var context = "";
		for (var i = 0; i < half; i++) {
			context = context
					+ "<div class=\"row\" style=\"margin: 0px 0px;\">";
			context = context + getItem(HeList[i*2],i*2);
			context = context + getItem(HeList[i*2 + 1],i*2+1);
			context = context + "</div>";
		}
		
		//终止动画
		$("#spinicon2").css('display','none');
		$("#kg_context").append(context);
       
		
		context = "";
		if (flag == 1) {
			context = context
					+ "<div class=\"row\" style=\"margin: 0px 0px;\">";
			context = context + getItem(HeList[HeList.length - 1],HeList.length - 1);
			context = context + "</div>";
			$("#kg_context").append(context);
		}
		
		//任务类型
        $("#reourceTypeLabel").html("I/O密集型");
		
		knob();//再次执行
	}
	function formatDateTime(inputTime) {
		var date = new Date(inputTime);
		var y = date.getFullYear();
		var m = date.getMonth() + 1;
		m = m < 10 ? ('0' + m) : m;
		var d = date.getDate();
		d = d < 10 ? ('0' + d) : d;
		var h = date.getHours();
		h = h < 10 ? ('0' + h) : h;
		var minute = date.getMinutes();
		var second = date.getSeconds();
		minute = minute < 10 ? ('0' + minute) : minute;
		second = second < 10 ? ('0' + second) : second;
		return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
	}

	function getItem(item,index) {
		// var g =item.severity;
		var context = "<div class=\"col-md-6\">"
				+ "<div class=\"box  collapsed-box\">"
				+ "<div class='row'>"
				+ "<div class='col-md-8  with-border'>"
				+ "<h4 class='box-title heuristicname'>"+ item.heuristicName+"</h4>"
				+ "</div>"
				+ "<div class='col-md-2 metricsLittleBoxHead'>"
				+ "<input type='text' class='knob' value='"+item.score+"' data-width='50'"
				+ "	data-height='50' data-fgcolor='#C2332C' data-bgcolor='#2F4656' data-readonly='true' style='margin-top: 25px'>"
				+ "</div>"
				+ " <div class='col-md-2'>"
				+ " <div class='box-tools pull-right'>"
				+ "	<button type='button' class='btn btn-box-tool metricsCol"+index+"' "
				+ "		data-widget='collapse'>详情"
				+ "		<i class='fa fa-plus'></i>"
				+ "	</button>"
				+ "</div>"
				+ "  </div>"
				+ "</div>"
				+ "<div class=\"box-body\" style=\"display: none;\">"
				+ "<div class=\"table-responsive\">"
				+ "<a name=\"Shuffle&amp;Sort\" class=\"list-group-item list-group-item-success\">"
				+ "<table class=\"list-group-item-text table table-condensed left-table\" style=\"table-layout: fixed;\">"
				+ "<thead><tr><th colspan=\"2\">Score: " + item.score
				+ "</th></tr></thead><tbody>";
		var list = item.yarnAppHeuristicResultDetailsDOList;
		for ( var i in list) {
			context = context + "<tr><td>" + list[i].name + "</td><td>"
					+ list[i].value + "</td></tr>";
		}
		context = context + "</tbody></table></a></div></div></div></div>";
		return context;
	}

	$(function() {
		/* jQueryKnob */
		knob();
	});

	function knob() {
		$(".knob").knob(
				{
					/*
					 * change : function (value) { //console.log("change : " +
					 * value); }, release : function (value) { console.log("release : " +
					 * value); }, cancel : function () { console.log("cancel : " +
					 * this.value); },
					 */
					draw : function() {

						// "tron" case
						if (this.$.data('skin') == 'tron') {

							var a = this.angle(this.cv) // Angle
							, sa = this.startAngle // Previous start angle
							, sat = this.startAngle // Start angle
							, ea // Previous end angle
							, eat = sat + a // End angle
							, r = true;

							this.g.lineWidth = this.lineWidth;

							this.o.cursor && (sat = eat - 0.3)
									&& (eat = eat + 0.3);

							if (this.o.displayPrevious) {
								ea = this.startAngle + this.angle(this.value);
								this.o.cursor && (sa = ea - 0.3)
										&& (ea = ea + 0.3);
								this.g.beginPath();
								this.g.strokeStyle = this.previousColor;
								this.g.arc(this.xy, this.xy, this.radius
										- this.lineWidth, sa, ea, false);
								this.g.stroke();
							}

							this.g.beginPath();
							this.g.strokeStyle = r ? this.o.fgColor
									: this.fgColor;
							this.g.arc(this.xy, this.xy, this.radius
									- this.lineWidth, sat, eat, false);
							this.g.stroke();

							this.g.lineWidth = 2;
							this.g.beginPath();
							this.g.strokeStyle = this.o.fgColor;
							this.g.arc(this.xy, this.xy, this.radius
									- this.lineWidth + 1 + this.lineWidth * 2
									/ 3, 0, 2 * Math.PI, false);
							this.g.stroke();

							return false;
						}
					}
				});
	}
})();