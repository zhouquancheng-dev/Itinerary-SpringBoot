package com.zqc.itineraryweb.controllers.oss

import com.zqc.itineraryweb.entity.Result
import com.zqc.itineraryweb.utils.AliYunOSSUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(value = ["/oss"])
class OSSController {

    private val logger: Logger = LoggerFactory.getLogger(OSSController::class.java)

    @PostMapping(value = ["/upload"])
    fun upload(@RequestParam file: MultipartFile): Result<String?> {
        // 获取客户端上传的原始文件名 例如：xxx.jpg、xxx.txt 等
        val originalFilename: String? = file.originalFilename
        logger.info("原始文件名为: {}", originalFilename)
        val uploadUrl = AliYunOSSUtils.upload(file, originalFilename)
        return Result.success(uploadUrl)
    }

}