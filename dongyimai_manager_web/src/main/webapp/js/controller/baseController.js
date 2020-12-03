app.controller('baseController',function ($scope) {
    //定义分页配置
    $scope.paginationConf={
        currentPage: 1,//当前页
        totalItems: 10,//总记录数
        itemsPerPage: 10,//每页显示的记录数
        perPageOptions: [5,10,15,20,25] ,//选择每页显示的记录数
        onChange: function () {
            //当分页参数放生变化，调用方法
            $scope.reloadList();
        }
    }

    //定义中间方法，调用分页方法
    $scope.reloadList=function(){
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    //定义一个数组，记录要删除 品牌id
    $scope.selectIds=[];

    //定义一个，当用户点击指定品牌的复选框，记录数据到数组，当用户取消，从数组移除数据
    //参数1：事件源对象，可以捕获到复选框的状态
    //参数2：要删除id
    $scope.updateSelection=function ($event, id) {
        //判断当用户勾选复选框
        if($event.target.checked){
            //把品牌id，存储到要删除数组
            $scope.selectIds.push(id);
        }else {
            //取消选中，从数组移除数据

            //查询id所在角标
            var index=	$scope.selectIds.indexOf(id);
            //移除指定角标元素
            $scope.selectIds.splice(index,1);
        }
    }

    //提取指定json字符串的，指定key值，串联到一个字符串
    $scope.jsonToString=function (jsonStr, key) {

        if(jsonStr) {
            //把json字符串转换为json对象
            var jsonObj = JSON.parse(jsonStr);

            var value = "";

            //遍历json数组 [{"text":"内存大小"},{"text":"颜色"}]
            for (var i = 0; i < jsonObj.length; i++) {
                if (i > 0) {
                    value += ",";
                }
                //提取各个节点元素 {"text":"内存大小"}
                value += jsonObj[i][key];
            }

            return value;
        }
    }

    //查询一个集合中是否存在key对应的value
    $scope.searchObjectByKey=function (list,key,value) {
        //遍历数组  {attributeName:"电视",attributeValues:"5寸"}
        for(var i= 0; i < list.length; i++){
            if(list[i][key] == value){
                //返回整个map对象
                return list[i];
            }
        }
        return null;
    }
})