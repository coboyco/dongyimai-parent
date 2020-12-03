package com.offcn.upload.controller;

import com.offcn.entity.Result;
import com.offcn.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("upload")
    public Result upload(MultipartFile file){
        //获取文件对象的源名字
        String originalFilename = file.getOriginalFilename();
        //获取源文件的后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        try {
            //调用工具类创建一个FastDFSClient
            FastDFSClient dfsClient = new FastDFSClient("classpath:/config/fdfs_client.conf");
            //上传文件，返回一个路径
            String fileNameUrl = dfsClient.uploadFile(file.getBytes(), suffix);
            //拼接url和ip返回完整的ip
            String path = FILE_SERVER_URL + fileNameUrl;
            return new Result(true,path);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
