package com.zqc.itineraryweb.controllers.oss

import com.zqc.itineraryweb.entity.Result
import com.zqc.itineraryweb.entity.oss.StsResponse
import com.zqc.itineraryweb.utils.aliyun.AliYunOssStsUtils
import com.zqc.itineraryweb.utils.aliyun.AliYunOssUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(value = ["/oss"])
class OSSController {

    /**
     * 上传文件
     */
    @PostMapping(value = ["/upload"])
    fun upload(
        @RequestParam file: MultipartFile,
        @RequestParam bucketDirName: String,
        @RequestParam fileName: String
    ): Result<String> {
        // 单个文件大小不能超过5GB
        val maxSize = 5L * 1024 * 1024 * 1024
        if (file.size > maxSize) {
            return Result.error("上传文件大小不能超过5GB")
        }
        return AliYunOssUtils.upload(file, bucketDirName, fileName)
    }

    /**
     * 获取OSS对象存储 STS临时令牌
     */
    @GetMapping(value = ["/stsToken"])
    fun getStsToken(): Result<StsResponse> {
        return AliYunOssStsUtils.getStsToken()
    }

}
