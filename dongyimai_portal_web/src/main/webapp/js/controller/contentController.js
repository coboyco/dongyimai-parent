//广告空值层
app.controller('contentController',function ($scope,$controller,contentService) {

    //初始化一个数组
    $scope.contentList = [];
    //根据广告分类id查询所有符合的广告
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId] = response;
        })
    }

    //跳转到其他服务的界面
    $scope.search=function () {
        location.href = "http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
})