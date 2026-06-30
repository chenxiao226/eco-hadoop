(function() {
	$(function() {


		//清空规则的name和expression
		$("#clearTrigger").click(function() {
			$("#triggerExpression").val("");
			$("#triggerName").val("");
			return false;
		});
		
		$("#sendTrigger").click(function() {
			
			var name = $("#triggerName").val();
			if(name==null || name.length==0){
				alert("规则名不能为空！");
				return false;
			}
			var expression = $("#triggerExpression").val();
			if(expression==null || expression.length==0){
				alert("规则内容不能为空！");return false;
			}
			//向后台传递数据
			$.ajax({
				type : "POST",
				url : monitorWebBaseURL + "alertconf/add/trigger",
				cache : false,
				data : {
					'name' : name,
					'expression' :expression,
					 'code' :'trigger'
				},
				dataType : 'json',
				success : function(data) {
					//此处应有服务器的反馈信息
					if(data.success){
						alert("发送成功！");
					}else{
						alert("发送失败！");
					}
				}
			});
			
			//$("#clearTrigger").click();
			return false; //去掉自带的button提交事件
		});
		//清空规则的name和expression
		$("#clearLog").click(function() {
			
			$("#logExpression").val("");
			$("#logName").val("");
			$("#logHost").val("");
			return false;
		});
		
		$("#sendLog").click(function() {
			
			var host = $("#logHost").val();
			if(host==null || host.length==0){
				alert("host不能为空！");
				return false;
			}
			
			var name = $("#logName").val();
			if(name==null || name.length==0){
				alert("item不能为空！");
				return false;
			}
			var expression = $("#logExpression").val();
			if(expression==null || expression.length==0){
				alert("key能为空！");return false;
			}
			//向后台传递数据
			$.ajax({
				type : "POST",
				url : monitorWebBaseURL + "alertconf/add/log",
				cache : false,
				data : {
					'host' : host,
					'name' : name,
					'expression' :expression,
					'code' :'log'
				},
				dataType : 'json',
				success : function(data) {
					//此处应有服务器的反馈信息
					if(data.success){
						alert("发送成功！");
					}else{
						alert("发送失败！");
					}
				}
			});
			
			//$("#clearTrigger").click();
			return false; //去掉自带的button提交事件
		});

	});
})();