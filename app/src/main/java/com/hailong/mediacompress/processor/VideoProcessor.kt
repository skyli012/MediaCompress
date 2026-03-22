package com.hailong.mediacompress.processor

import android.content.Context
import android.util.Log
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
        extraFlags: String? = null,
        listener: VideoCompressListener
    ) {
        // 构建FFmpeg命令
        // -i input.mp4 -vcodec libx264 -crf 28 -preset fast -vf scale=1280:720 output.mp4
        val commandBuilder = StringBuilder()
        commandBuilder.append("-y ") // 覆盖输出文件
        commandBuilder.append("-i \"$inputPath\" ")
        
        // 使用 Android 硬件加速编码器 h264_mediacodec (替代缺失的 libx264)
        commandBuilder.append("-vcodec h264_mediacodec ")
        
        // 硬件编码器通常不支持 CRF，这里我们使用码率控制或默认质量
        // 也可以使用 -b:v 动态调整，这里先移除不支持的 -crf 和 -preset
        // commandBuilder.append("-crf $crf ")
        // commandBuilder.append("-preset fast ")
        
        // 强制音频使用 aac，增强兼容性
        commandBuilder.append("-acodec aac ")
        
        if (scale != null) {
            commandBuilder.append("-vf scale=$scale ")
        }
        
        if (extraFlags != null) {
            commandBuilder.append("$extraFlags ")
        }
        
        commandBuilder.append("\"$outputPath\"")

        val command = commandBuilder.toString()
        Log.d("VideoProcessor", "Executing FFmpeg command: $command")

        FFmpegKit.executeAsync(command, { session ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                Log.i("VideoProcessor", "FFmpeg command successful!")
                listener.onComplete(true, outputPath)
            } else {
                Log.e("VideoProcessor", "FFmpeg command failed with return code ${returnCode}.")
                Log.e("VideoProcessor", "Command: ${session.command}")
                Log.e("VideoProcessor", "Logs: ${session.allLogsAsString}")
                listener.onComplete(false, null)
            }
        }, { log ->
            Log.d("VideoProcessor", "FFmpeg log: ${log.message}")
        }, { statistics ->
            // FFmpegKit 提供的统计信息
            // 进度可以通过当前处理的时间除以总时间计算
            // listener.onProgress(...)
        })
    }
}
