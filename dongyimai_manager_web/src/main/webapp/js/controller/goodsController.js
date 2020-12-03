//商品控制层
//商品控制层
app.controller('goodsController' ,function($scope,$controller   ,$location,goodsService,itemCatService,typeTemplateService){

	$controller('baseController',{$scope:$scope});//继承

	$scope.entity = {goods:{},goodsDesc:{itemImages:[],specificationItems:[],customAttributeItems:[]}};
	//读取列表数据绑定到表单中
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}
		);
	}

	//分页
	$scope.findPage=function(page,rows){
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

	//查询实体
	$scope.findOne=function(){
		//获取url参数的值
		var id = $location.search()['id'];
		if(id == null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				//商品介绍的内容复制给entity
				$scope.entity= response;
				editor.html($scope.entity.goodsDesc.introduction);
				//数据库里边存的是json类型的字符串，在传送到前端的时候会发生转义，所以将其转换成JSON格式的对象
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//显示sku列表
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//循环遍历sku列表
				for(var i = 0; i < $scope.entity.itemList.length; i++){
					//{"spec":{},"price":0,"num":0,"status":0,"isDefault":0}
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);
	}

	//保存
	$scope.save=function(){
		//获取文本编辑框的值
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加
		}
		serviceObject.success(
			function(response){
				if(response.success){
					alert(response.message)
					//重新查询
					//$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}
		);
	}


	//批量删除
	$scope.dele=function(){
		//获取选中的复选框
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}
			}
		);
	}

	$scope.searchEntity={};//定义搜索对象

	//搜索
	$scope.search=function(page,rows){
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

	//添加商品信息
	$scope.add=function () {
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(function (response) {
			if(response.success){
				alert(response.message);
				$scope.entity = {goods:{},goodsDesc:{itemImages:[],specificationItems:[],customAttributeItems:[]}};
				editor.html('');//清空富文本编辑器
			}else{
				alert(response.message);
			}
			$scope.entity = {};
		})
	}

	$scope.image_entity={url:""};



	//点击保存图片时需要在当期页显示出保存的图片信息

	$scope.add_entity_images = function () {
		//将图片信息保存到$scope.entity.goodsDesc.itemImage中，用于展示
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	//当点击删除图片时
	$scope.del_entity_images = function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}

	//查询一级目录的下拉菜单列表  selectItemCat1List
	$scope.selectItemCat1List = function () {
		itemCatService.findByParentId(0).success(function (response) {
			$scope.itemCat1List = response;
		})
	}
	//查询二级下拉菜单列表，当点击一级菜单值发生改变的时候，发送请求得到二级菜单数据，
	//$watch内置对象，相当于监听，  第一个参数：监听的对象值，第二个参数:发生改变后的新值  第三个参数：老的值
	$scope.$watch("entity.goods.category1Id",function (newValue,oldValue) {
		//判断，如果newValue为真，说明值发生了改变，及用户点击了
		if(newValue){
			itemCatService.findByParentId(newValue).success(function (response) {
				$scope.itemCat2List = response;
			})
		}
	})

	//查询三级菜单的数据，监听二级菜单的entity.goods.category2Id
	//与上边同理
	$scope.$watch("entity.goods.category2Id",function (newValue,oldValue) {
		if(newValue){
			itemCatService.findByParentId(newValue).success(function (response) {
				$scope.itemCat3List = response;
			})
		}
	})

	//获取模板的id监听三级菜单
	$scope.$watch("entity.goods.category3Id",function (newValue,oldValue) {
		if(newValue){
			itemCatService.findOne(newValue).success(function (response) {
				$scope.entity.goods.typeTemplateId = response.typeId;
			})
		}
	})

	//监听模板id 获取品牌下拉列表 获取规格和规格选项
	$scope.$watch("entity.goods.typeTemplateId",function (newValue,oldValue) {
		if(newValue){
			//发送请求查询模板对象
			typeTemplateService.findOne(newValue).success(function (response) {
				$scope.templateObj = response;
				//获取品牌列表 从后台读取的是json字符串，会发生转义，所以需要将其解析成json对象形式
				$scope.templateObj.brandIds = JSON.parse($scope.templateObj.brandIds);
				//扩展属性
				if($location.search()['id'] == null){
					$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.templateObj.customAttributeItems);
				}


			})

			//汇过去规格和规格选项
			typeTemplateService.findSpecList(newValue).success(function (response) {
				$scope.specList = response;
			})
		}

	})

	//$scope.entity = {goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
	//当点击规格选项的时候，给specificationItems进行初始化specificationItems:[{"attributeName":"","attributeValue":[]},{},{}]
	$scope.updateSpecAttribute=function($event,name,value){
		//用户选中的是否需要进行初始化，记录用户规格和规格选项数组，看是否存在
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);

		//对object进行判断 如果不为空
		if(object != null){
			//判断复选框的状态，如果为真
			if($event.target.checked){
				object.attributeValue.push(value);
			}else {
				//如果取消复选框的按钮，就将数据从attributeValue中剔除
				//先获取这个数据的索引
				var index = object.attributeValue.indexOf(value);
				//剔除数据
				object.attributeValue.splice(index,1);
				//当attributeValue数组没有数据时，也就是长度为0时，则剔除这个对象在specificationItems中的位置
				if(object.attributeValue.length == 0){
					//获取长度为0的对象的索引
					var index1 = $scope.entity.goodsDesc.specificationItems.indexOf(object);
					//剔除
					$scope.entity.goodsDesc.specificationItems.splice(index1,1);
				}
			}

		}else {
			//如果不为空，对specificationItems数组进行初始化
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}

	}

	//当用户点击或者取消复选框时，进行拼接sku列表
	$scope.createItemList=function () {
		//定义展示的模板
		$scope.entity.itemList = [{"spec":{},"price":0,"num":0,"status":0,"isDefault":0}];
		//读取用户选中的记录信息集合
		var items = $scope.entity.goodsDesc.specificationItems;
		//遍历items
		for(var i = 0; i < items.length; i++){
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}

	//定义扩充sku列表的方法
	addColumn=function(itemList,attributeName,attributeValue){
		//定义一个新的数组，用于存储扩充的sku列表
		var newList=[];
		//遍历itemList
		for(var i = 0; i < itemList.length;i++){
			//将空的{"spec":{},"price":0,"num":0,"status":0,"isDefault":0}赋值给oleRow
			var oleRow = itemList[i];
			//遍历规格选项数组
			for(var j = 0; j < attributeValue.length; j++){
				//深拷贝一个对象
				var newRow = JSON.parse(JSON.stringify(oleRow));
				//对newRow赋值
				newRow.spec[attributeName] = attributeValue[j];
				//将newRow存入到newList数组中
				newList.push(newRow);
			}
		}
		return newList;
	}

	//定义状态数组
	$scope.status = ['未审核','审核通过','审核不通过','关闭'];
	$scope.itemList = [];
	//获取分类表所有的分类数据
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(function (response) {
			//对返回的数据进行遍历
			for(var i = 0; i < response.length; i++){
				//数组对应位置存的就是分类名称 id - name对应
				$scope.itemList[response[i].id] = response[i].name;
			}
		})
	}

	//[{"attributeValue":["5.5寸"],"attributeName":"手机屏幕尺寸"},{"attributeValue":["移动4G","联通3G"],"attributeName":"网络"}]
	//让规格和规格选项回显  specName规格   specOption规格名称
	$scope.checkAttributeValue=function (specName,specOption) {
		var list = $scope.entity.goodsDesc.specificationItems;
		var obj =  $scope.searchObjectByKey(list,"attributeName",specName);
		if(obj == null){
			return;
		}
		if(obj != null){
			if(obj.attributeValue.indexOf(specOption) >= 0){
				return true;
			}else {
				return false;
			}
		}
	}

	//更新商品状态信息
	$scope.updateState=function (status) {
       goodsService.updateState($scope.selectIds,status).success(function (response) {
            if(response.success){
            	alert(response.message);
            	$scope.reloadList();
			}else{
				alert(response.message);
			}
	   })
	}
});