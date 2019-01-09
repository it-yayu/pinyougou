app.controller("baseController",function($scope){

	$scope.reloadList = function(){
		// $scope.findByPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
		$scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	
	// 定义一个数组:
	$scope.selectIds = [];
	// 更新复选框：
	$scope.updateSelection = function($event,id){
		// 复选框选中
		if($event.target.checked){
			// 向数组中添加元素
			$scope.selectIds.push(id);
		}else{
			// 从数组中移除
			var idx = $scope.selectIds.indexOf(id);
			$scope.selectIds.splice(idx,1);
		}
		
	}
	
	// 定义方法：获取JSON字符串中的某个key对应值的集合
	$scope.jsonToString = function(jsonStr,key){
		// 将字符串转成JSOn:
		var jsonObj = JSON.parse(jsonStr);
		
		var value = "";
		for(var i=0;i<jsonObj.length;i++){
			
			if(i>0){
				value += ",";
			}
			
			value += jsonObj[i][key];
		}
		return value;
	}
	
});