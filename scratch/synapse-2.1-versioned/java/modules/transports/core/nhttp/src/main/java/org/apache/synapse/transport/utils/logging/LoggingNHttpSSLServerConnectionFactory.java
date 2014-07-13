/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.transport.utils.logging;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLIOSession;
import org.apache.http.nio.reactor.ssl.SSLMode;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;

import javax.net.ssl.SSLContext;

/**
 * The SSL-enabled version of the LoggingNHttpServerConnectionFactory. Identical in behavior
 * to the parent class, but wraps IOSession instances with SSLIOSession instances.
 */
public class LoggingNHttpSSLServerConnectionFactory extends LoggingNHttpServerConnectionFactory {

    private SSLContext sslContext;
    private SSLSetupHandler sslSetupHandler;

    public LoggingNHttpSSLServerConnectionFactory(ConnectionConfig config, SSLContext sslContext,
                                                  SSLSetupHandler sslSetupHandler) {
        super(config);
        this.sslContext = sslContext;
        this.sslSetupHandler = sslSetupHandler;
    }

    @Override
    public DefaultNHttpServerConnection createConnection(IOSession session) {
        final SSLIOSession ssliosession = new SSLIOSession(
                session,
                SSLMode.SERVER,
                sslContext,
                sslSetupHandler);
        session.setAttribute(SSLIOSession.SESSION_KEY, ssliosession);
        return super.createConnection(ssliosession);
    }
}
