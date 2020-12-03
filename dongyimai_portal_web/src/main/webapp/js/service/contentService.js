//广告服务
app.service('contentService',function($http){

    //获取登录人的名称
    this.findByCategoryId=function (categoryId) {
        return $http.get('/content/findByCategoryId.do?categoryId='+categoryId);
    }
});