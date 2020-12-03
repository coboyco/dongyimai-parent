//运营商后台管理人员index界面的controller
app.controller('indexController',function ($scope,$controller,loginService) {

    //{$scope:$scope} 就是将baseController的$scope赋值到本controller的$scope
    $controller('baseController',{$scope:$scope});//继承

    //显示运行商登录后的名字
    $scope.showLoginName=function () {
        loginService.loginName().success(function (response) {
            $scope.loginName = response.loginName;
        })
    }
})