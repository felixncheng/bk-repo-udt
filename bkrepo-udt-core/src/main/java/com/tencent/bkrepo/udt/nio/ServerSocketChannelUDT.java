/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.tencent.bkrepo.udt.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.ServerSocketChannel;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tencent.bkrepo.udt.SocketUDT;
import com.tencent.bkrepo.udt.TypeUDT;
import com.tencent.bkrepo.udt.anno.ThreadSafe;

/**
 * {@link ServerSocketChannel}-like wrapper for {@link SocketUDT} can be either
 * stream or message oriented, depending on {@link TypeUDT}
 * <p>
 * you must use {@link SelectorProviderUDT#openServerSocketChannel()} to obtain
 * instance of this class; do not use JDK
 * {@link ServerSocketChannel#open()};
 * <p>
 * example:
 * 
 * <pre>
 * SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
 * ServerSocketChannel acceptChannel = provider.openServerSocketChannel();
 * ServerSocket acceptSocket = acceptChannel.socket();
 * InetSocketAddress acceptAddress = new InetSocketAddress(&quot;localhost&quot;, 12345);
 * acceptorSocket.bind(acceptAddress);
 * assert acceptSocket.isBound();
 * SocketChannel connectChannel = acceptChannel.accept();
 * assert connectChannel.isConnected();
 * </pre>
 */
public class ServerSocketChannelUDT extends ServerSocketChannel implements
		ChannelUDT {

	protected static final Logger log = LoggerFactory
			.getLogger(ServerSocketChannelUDT.class);

	@ThreadSafe("this")
	protected NioServerSocketUDT socketAdapter;

	protected final SocketUDT socketUDT;

	protected ServerSocketChannelUDT( //
			final SelectorProviderUDT provider, //
			final SocketUDT socketUDT //
	) {

		super(provider);
		this.socketUDT = socketUDT;

	}

	@Override
	public SocketChannelUDT accept() throws IOException {
		try {

			begin();

			final SocketUDT clientUDT = socketUDT.accept();

			if (clientUDT == null) {

				return null;

			} else {

				return new SocketChannelUDT( //
						providerUDT(), //
						clientUDT, //
						clientUDT.isConnected() //
				);

			}
		} finally {
			end(true);
		}
	}

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		return null;
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
		socketUDT.close();
	}

	@Override
	protected void implConfigureBlocking(final boolean block)
			throws IOException {
		socketUDT.setBlocking(block);
	}

	@Override
	public boolean isConnectFinished() {
		return true;
	}

	@Override
	public KindUDT kindUDT() {
		return KindUDT.ACCEPTOR;
	}

	@Override
	public SelectorProviderUDT providerUDT() {
		return (SelectorProviderUDT) super.provider();
	}

	@Override
	public ServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
		return null;
	}

	@Override
	public <T> ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
		return null;
	}

	@Override
	public <T> T getOption(SocketOption<T> name) throws IOException {
		return null;
	}

	@Override
	public Set<SocketOption<?>> supportedOptions() {
		return null;
	}

	@Override
	public synchronized NioServerSocketUDT socket() {
		if (socketAdapter == null) {
			try {
				socketAdapter = new NioServerSocketUDT(this);
			} catch (final Exception e) {
				log.error("failed to make socket", e);
				return null;
			}
		}
		return socketAdapter;
	}

	@Override
	public SocketUDT socketUDT() {
		return socketUDT;
	}

	@Override
	public String toString() {
		return socketUDT.toString();
	}

	@Override
	public TypeUDT typeUDT() {
		return providerUDT().type();
	}

}
