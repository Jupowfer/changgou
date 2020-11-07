package com.changgou.file.controller;

import com.changgou.file.pojo.FastDFSFile;
import com.changgou.file.util.FileUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
public class FileController {

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping(value = "/upload")
    public String upload(@RequestParam(value = "file") MultipartFile file) throws Exception{
        FastDFSFile fastDFSFile = new FastDFSFile();
        fastDFSFile.setContent(file.getBytes());//文件的内容
        fastDFSFile.setName(file.getOriginalFilename());//文件的名字 aaa.jpg
        fastDFSFile.setExt(StringUtils.getFilenameExtension(file.getOriginalFilename()));//文件的拓展名

        //文件的上传
        String[] upload = FileUtil.upload(fastDFSFile);
        //返回页面0:组名 1:全量路径名
        return upload[0] + "/" + upload[1];
    }
}
