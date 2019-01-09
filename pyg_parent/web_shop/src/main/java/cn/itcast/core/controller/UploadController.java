package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/uploadFile")
    public Result upload(MultipartFile file) throws Exception{
        try {
            //读取文件完整名称
            String fileName = file.getOriginalFilename();
            //创建一个fastDfs客户端
            FastDFSClient fastDFSClient =new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            //执行文件上传
            String path = fastDFSClient.uploadFile(file.getBytes(), fileName, file.getSize());
            //拼接完整的url  url+ip地址
            String url=FILE_SERVER_URL+path;
           return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"文件上传失败");

        }
    }
}
