//管理运营后人员登录服务
app.service('loginService',function($http){

    //获取登录人的名称
    this.loginName=function () {
        return $http.get('/login/name.do');
    }
});