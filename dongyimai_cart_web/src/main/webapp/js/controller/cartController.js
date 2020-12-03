//购物车控制层
app.controller('cartController',function($scope,cartService){
    //查询购物车列表
    $scope.findCartList=function(){
        cartService.findCartList().success(
            function(response){
                $scope.cartList=response;
                //秋季总数
                $scope.totalValue=cartService.sum($scope.cartList);
            }
        );
    }

    //添加购物车
    $scope.addGoodsToCartList=function (itemId,num) {
       cartService.addGoodsToCartList(itemId,num).success(function (response) {
            if (response.success){
                $scope.findCartList();
            }else {
                alert(response.message);
            }
       })
    }

    //获取地址列表
    $scope.findAddressList=function(){
        cartService.findAddressList().success(
            function(response){
                $scope.addressList=response;
                //设置默认地址
                for (var i =0; i<$scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault == '1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        );
    }

    //选择地址
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }
    //判断是否选择当前地址
    $scope.isSelectedAddress=function (address) {
        if(address == $scope.address){
            return true;
        }else {
            return false;
        }

    }

    $scope.order={paymentType:'1'}
    //选择师傅方式
    $scope.selectPayType=function (type) {
        $scope.order.paymentType=type;
    }

    //保存订单
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;
        $scope.order.receiverMobile=$scope.address.mobile;
        $scope.order.receiver=$scope.address.contact;
        cartService.submitOrder($scope.order).success(function (response) {
            if(response.success){
                //页面跳转如果是支付宝扫描
                if($scope.order.paymentType == '1'){
                    location.href="pay.html";
                }else {
                    //如果是货到付款
                    location.href="paysuccess.html";
                }
            }else {
                alert(response.message);
            }
        })
    }

});