/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2023 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.bkrepo.udt.example.echo

import com.tencent.bkrepo.udt.example.util.ServerUtil
import com.tencent.bkrepo.udt.netty.transport.UdtChannel
import com.tencent.bkrepo.udt.netty.transport.nio.NioUdtProvider
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.util.concurrent.DefaultThreadFactory

fun main() {
    val port = System.getProperty("port", "8007").toInt()
    val sslCtx = ServerUtil.buildSslContext()
    val bossGroup = NioEventLoopGroup(1, DefaultThreadFactory("boss"), NioUdtProvider.BYTE_PROVIDER)
    val workGroup = NioEventLoopGroup(1, DefaultThreadFactory("work"), NioUdtProvider.BYTE_PROVIDER)
    try {
        val b = ServerBootstrap()
        b.group(bossGroup, workGroup)
            .channelFactory(NioUdtProvider.BYTE_ACCEPTOR)
            .option(ChannelOption.SO_BACKLOG, 100)
            .handler(LoggingHandler(LogLevel.INFO))
            .childHandler(object : ChannelInitializer<UdtChannel>() {
                override fun initChannel(ch: UdtChannel) {
                    val p = ch.pipeline()
                    if (sslCtx != null) {
                        p.addLast(sslCtx.newHandler(ch.alloc()))
                    }
                    p.addLast(EchoServerHandler())
                }
            })
        val f = b.bind(port).sync()
        f.channel().closeFuture().sync()
    } finally {
        bossGroup.shutdownGracefully()
        workGroup.shutdownGracefully()
    }
}