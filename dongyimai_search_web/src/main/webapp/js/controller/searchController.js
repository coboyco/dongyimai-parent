app.controller('searchController',function($scope,$location,searchService){
    //定义前端向后端传递数据 json对象
    //keywords--->查询关键字
    //category---->分类
    //brand----->品牌
    //spec---->规格和规格选项集合
    //price---->价格
    //pageNo--当前页
    //pageSize--->每页显示的数量
    //sortFiled要排序查询的字段吧
    //sort//是降序还是升序
    $scope.searchMap={'keywords':'','category':'','brand':'','price':'','pageNo':1,'pageSize':20,'sortFiled':'','sort':'',spec:{}};
    //搜索
    $scope.search=function(){
        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
                buildPageLabel();
            }
        );
    }

    //添加搜索项
    $scope.addSearchItem=function (key,value) {
        //如果点击的是分类或者品牌
        if(key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = value;
        }else {
            $scope.searchMap.spec[key] = value;
        }
        //发出查询之前，初始化当前页面为1
        $scope.searchMap.pageNo = 1;

        $scope.search();//执行搜索
    }

    //撤销搜索项的方法  移除符合搜索条件
    $scope.removeSearchItem=function (key) {
        if(key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = "";
        }else {
            //是规格的话 delete 操作符用于删除对象的某个属性。
            delete $scope.searchMap.spec[key];
        }

        //发出查询之前，初始化当前页面为1
        $scope.searchMap.pageNo = 1;
        $scope.search();//执行搜索
    }

    //构建查询分页模
    buildPageLabel=function () {
       //定义页码框数组
        $scope.pageLabel=[];
        //获取最大的页数
        var maxPageNo = $scope.resultMap.totalPages;
        //开始页
        var firstPageNo = 1;
        //截止页码
        var lastPage = maxPageNo;

        //显示...的问题
        $scope.firstDot = true;
        $scope.lastDot = true;

        //判断  如果的总页数大于五页
        if($scope.resultMap.totalPages > 5){
            //如果当前页页码<=3  此时只显示前五条
            if($scope.searchMap.pageNo <= 3){
                lastPage = 5;
                //左边不显示.
                $scope.firstDot = false;
            }else if($scope.searchMap.pageNo >maxPageNo - 2){
                //如果当前页显示的是最后两页，则显示后五条
                firstPageNo = maxPageNo - 4;
                //右边不显示
                $scope.lastDot = false;
            }else{
                //如果不在上述两者范围，就显示当期页前边两页和后边两页
                firstPageNo = $scope.searchMap.pageNo - 2;
                lastPage =  $scope.searchMap.pageNo + 2;
                //两边都展示.
            }
        }else{
            //总的页数小于5 作弊那右边的都不显示
            $scope.firstDot = false;
            $scope.lastDot = false;
        }

        //对定义页码框数组进行存值
        var i = firstPageNo;
        for(i; i <= lastPage; i++){
            $scope.pageLabel.push(i);
        }
    }

    //跳转到指定页码
    $scope.queryByPage=function (pageNo) {
        //对pageNo进行判断
        if(pageNo <1 || pageNo > $scope.resultMap.totalPages){
            alert("页码不合法");
            return;
        }
        //页码合法，将其赋值给searchMap
        $scope.searchMap.pageNo = parseInt(pageNo);
        //进行查询
        $scope.search();
    }

    //判断当前页是否为第一页
    $scope.isTopPage=function () {
        if($scope.searchMap.pageNo == 1){
            return true;
        }else {
            return false;
        }
    }
    $scope.resultMap={totalPages:1};
    //判断当前页是否为最后一页
    $scope.isEndPage=function () {
        if($scope.searchMap.pageNo == $scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }

    //判断是否为当前页
    $scope.isPage = function (page) {
        if(parseInt(page) == parseInt($scope.searchMap.pageNo)){
            return true;
        }else{
            return false;
        }
    }

    //排序的方法
    $scope.sortSearch = function (sortFiled,sort) {

        //赋值
        $scope.searchMap.sortFiled = sortFiled;
        $scope.searchMap.sort = sort;
        //查询
        $scope.search();
    }

    //品牌包含用户输入的关键字就隐藏
    $scope.keywordsIsBrand=function () {
        for(var i = 0; i < $scope.resultMap.brandList.length; i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
                //此时说明用户输入的关键字包含有品牌列表的关键字
                return true;
            }
        }
        return false;
    }

    //加载由index.html界面传送过来的数据
    $scope.loadkeywords=function () {
       $scope.searchMap.keywords = $location.search()['keywords'];
       //查询
        $scope.search();
    }
});