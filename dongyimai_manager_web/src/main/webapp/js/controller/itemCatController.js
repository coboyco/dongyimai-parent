 //商品类目控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){

	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			//在添加之前，要把父亲id获取到，并且重新赋值给要添加对象的属性
			$scope.entity.parentId = $scope.parentId;
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//添加成功后，根据父亲id重新查询重新查询
		        	$scope.findByParentId($scope.parentId);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.findByParentId($scope.parentId);//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//根据上级id查询下级列表
	$scope.findByParentId=function (parentId) {
		//记住上级id
		$scope.parentId = parentId;
		itemCatService.findByParentId(parentId).success(function (response) {
			$scope.list = response;
		})
	}

	//设置级别  有顶层级别，一级级别，二级级别
	$scope.grade = 1;
	$scope.setGrade=function (value) {
		$scope.grade = value;
	}

	//读取列表
	$scope.selectList=function (p_entity) {
		//说明当前在第一层，需要将后边的两个导航去掉
		if($scope.grade == 1){
			$scope.entity_1 = null;
			$scope.entity_2 = null;
		}

		//说明当前是在第二层，后边第二层需要导航栏需要去掉,同时第一层导航条需要被赋值
		if($scope.grade == 2){
			$scope.entity_1 = p_entity;
			$scope.entity_2 = null;
		}

		//此时，说明已经走过第一层了，也就是第二个if语句已经执行了，只需要第二层进行展示即可
		if($scope.grade == 3){
			$scope.entity_2 = p_entity;
		}

		//除了顶级导航栏，一下的每一层在点击的时候，都要进行查询子列表
		$scope.findByParentId(p_entity.id);
	}

	//定义变量
	$scope.parentId = 0;

	//定义模板列表
	$scope.typeTemplateList={data:[{"id":1,"text":"选我"}]};
	//显示模板列表方法 当点击新建的时候，触发这个方法
	$scope.findtypeTemplateList=function () {
		typeTemplateService.selectOptionList().success(function (response) {
			$scope.typeTemplateList = {data:response};
		})
	}
});	