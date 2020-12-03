//下载服务
app.service('uploadService',function($http){

    //获取登录人的名称
    this.uploadFile=function () {
        var formData = new FormData();
        formData.append("file",file.files[0]);
        return $http({
            method:'POST',
            url:"/upload.do",
            data: formData,
            /*anjularjs对于post和get请求默认的Content-Type header 是application/json。通过设置‘Content-Type’: undefined，这样浏览器会帮我们把Content-Type 设置为 multipart/form-data.*/
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        })
    }
});