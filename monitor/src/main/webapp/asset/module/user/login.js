(function (){
	//alert(112);		
	$("#login_btn").click(function(){
			//alert(113);
			   		var username = $.trim($("#username").val());
			        var password = $.trim($("#password").val());
			         if(username == ""){
			             alert("请输入用户名");
			             return false;
			         }else if(password == ""){
			             alert("请输入密码");
			             return false;
			         }
			         //ajax去服务器端校验
			         var data= {username:username,password:password};
			         
			         $.ajax({
			             type:"POST",
			             url:"__CONTROLLER__/check_login",
			             data:data,
			             dataType:'json',
			             success:function(msg){
			                 //alert(msg);
			                 if(msg==1){
			                       window.location.href = "{:U('Index/personal')}";   
			                 }else{
			                     alert("登录失败，请重试!");
			                 }
			             }
			         });
			 }); 
		
	
	   
		        
		 
})();
