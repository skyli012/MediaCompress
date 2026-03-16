package com.hailong.mediacompress.processor

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File

object VideoProcessor {

    interface VideoCompressListener {
        fun onProgress(progress: Float)
        fun onComplete(success: Boolean, outputPath: String?)
    }

    /**
     * 视频压缩
     * @param context 上下文
     * @param inputPath 输入视频路径
     * @param outputPath 输出视频路径
     * @param crf 控制压缩质量 (18-51, 默认28)
     * @param scale 分辨率缩放 (例如: 1280:720, 默认不缩放)
     * @param listener 回调监听器
     */
    fun compressVideo(
        context: Context,
        inputPath: String,
        outputPath: String,
        crf: Int = 28,
        scale: String? = null,
        preset: String = "medium",
        listener: VideoCompressListener
    ) {
        // 构建FFmpeg命令
        // -i input.mp4 -vcodec libx264 -crf 28 -preset fast -vf scale=1280:720 output.mp4
        val commandBuilder = StringBuilder()
        commandBuilder.append("-y ") // 覆盖输出文件
        commandBuilder.append("-i \"$inputPath\" ")
        commandBuilder.append("-vcodec libx264 ")
        commandBuilder.append("-crf $crf ")
        commandBuilder.append("-preset $preset ")
        
        if (scale != null) {
            commandBuilder.append("-vf scale=$scale ")
        }
        
        commandBuilder.append("\"$outputPath\"")

        val command = commandBuilder.toString()

        FFmpegKit.executeAsync(command, { session ->
            val state = session.state
            val returnCode = session.returnCode

            if (ReturnCode.isSuccess(returnCode)) {
                listener.onComplete(true, outputPath)
            } else {
                listener.onComplete(false, null)
            }
        }, { log ->
            // 这里可以解析log中的时间信息来计算进度
            // FFmpeg输出通常包含 time=00:00:00.00 格式的信息
        }, { statistics ->
            // FFmpegKit 提供的统计信息
            // 进度可以通过当前处理的时间除以总时间计算
            // listener.onProgress(...)
        })
    }
}
