package com.zqc.itineraryweb.controllers.tencent_im

import com.zqc.itineraryweb.entity.Result
import com.zqc.itineraryweb.utils.tencentyun.TLSSigAPIv2
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/tim"])
class TIMController {

    @Value("\${tim.appId}")
    private val sdkAppId: String? = null

    @Value("\${tim.secretKey}")
    private val secretKey: String? = null

    @GetMapping(value = ["/userSig"])
    fun getUserSig(@RequestParam userId: String) : Result<String> {
        val tlsSigAPIv2 = TLSSigAPIv2(sdkAppId!!.toLong(), secretKey)
        // 超时时间为一个月 单位为秒
        return Result.success(tlsSigAPIv2.genUserSig(userId, 24 * 60 * 60 * 30))
    }

}