//商品详细页（控制层）
app.controller('itemController',function ($scope,$http) {
//数量操作
    $scope.addNum = function (x) {
      $scope.num=$scope.num+x;
      if($scope.num < 1){
          $scope.num=1;
      }
    }

    $scope.specificationItems={};//记录用户选择的规格
    //用户选择规格
    $scope.selectSpecification=function (name,value) {
        $scope.specificationItems[name]=value;
        //用户选择规格后触发查询sku算法
        searchSku();
    }

    //判断某规格是否被用户选中
    $scope.isSelected=function (name,value) {
        if($scope.specificationItems[name] == value){
            return true;
        }else {
            return false;
        }
    }

    //加载默认的sku
    $scope.loadSku=function () {
        $scope.sku=skuList[0];
        $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //比价两个json对象看是否相同
    matchObject=function (json1,json2) {

        for(var k in json1){
            if(json1[k]!=json2[k]){
                return false;
            }
        }
        for(var j in json2){
            if(json2[j]!=json1[j]){
                return false;
            }
        }
        return true;
    }

    //当用户选中 指定规格选项，就调用本方法
    searchSku=function (){
        //遍历sku集合
        for(var i = 0; i < skuList.length;i++){
            //比对用户选中的规格和规格选项与集合比较
            if(matchObject(skuList[i].spec,$scope.specificationItems)){
                //如果为真，就将当前显示的sku重新赋值
                $scope.sku = skuList[i];
                //匹配成功，就立刻结束循环
                return;
            }
        }
        $scope.sku={"id":0,"title":"未适配sku","price":0.00};
    }

    //加入购物车
    $scope.addToCart=function () {
        alert($scope.sku.id)
        alert($scope.num)
        $http.get('http://localhost:9108/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(function (response) {
            if (response.success){
                //跳转到购物车界面
                location.href='http://localhost:9108/cart.html';
            }else {
                alert(response.message);
            }
        })
    }

})

