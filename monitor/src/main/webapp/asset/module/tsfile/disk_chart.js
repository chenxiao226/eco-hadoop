/**
 * Created by liuyou on 2017/8/5.
 */

(function () {

	// 获取数据
	function fetchDiskDataInterval() {
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "tsfile/disk",
			data: {
				limit: 1
			},
			dataType: "json",
			success: function (data) {
				if (data.success) {
					var d = data.dataObject;
					var dataFileSize=getDataSize(d[0].sum);
					// 填入数据
					$('#dataFileSize').html(dataFileSize);
				}
			}
		});
	}

	$(function () {
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "tsfile/disk",
			data: {
				limit: 1
			},
			dataType: "json",
			success: function (data) {
				if (data.success) {
					var d = data.dataObject;
					var dataFileSize=getDataSize(d[0].sum);
					// 填入数据
					$('#dataFileSize').html(dataFileSize);
				}
			}
		});
		
		setInterval(fetchDiskDataInterval, 3000);
		
	});
	function getDataSize(dataFileSize){
		if(dataFileSize<1024){
			return dataFileSize+"<small>B</small>"; 
		}
		dataFileSize=(dataFileSize/1024).toFixed(2);
		if(dataFileSize<1024){
			return dataFileSize+"<small>KB</small>";
		}
		dataFileSize=(dataFileSize/1024).toFixed(2);
		if(dataFileSize<1024){
			return dataFileSize+"<small>MB</small>";
		}
		dataFileSize=(dataFileSize/1024).toFixed(2);
		if(dataFileSize<1024){
			return dataFileSize+"<small>GB</small>";
		}
		dataFileSize=(dataFileSize/1024).toFixed(2);
		if(dataFileSize<1024){
			return dataFileSize+"<small>PB</small>";
		}
		
	}
})();

