/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.proxy.server;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.bookkeeper.test.PortManager;
import org.apache.pulsar.broker.auth.MockedPulsarServiceBaseTest;
import org.apache.pulsar.broker.authentication.AuthenticationProviderTls;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.impl.auth.AuthenticationTls;
import org.apache.pulsar.policies.data.loadbalancer.LoadManagerReport;
import org.apache.pulsar.policies.data.loadbalancer.LoadReport;
import org.eclipse.jetty.servlet.ServletHolder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

public class AuthedAdminProxyHandlerTest extends MockedPulsarServiceBaseTest {
    private final String TLS_TRUST_CERT_FILE_PATH = "./src/test/resources/authentication/tls/cacert.pem";
    private final String TLS_SERVER_CERT_FILE_PATH = "./src/test/resources/authentication/tls/server-cert.pem";
    private final String TLS_SERVER_KEY_FILE_PATH = "./src/test/resources/authentication/tls/server-key.pem";
    private final String TLS_CLIENT_CERT_FILE_PATH = "./src/test/resources/authentication/tls/client-cert.pem";
    private final String TLS_CLIENT_KEY_FILE_PATH = "./src/test/resources/authentication/tls/client-key.pem";
    private final String DUMMY_VALUE = "DUMMY_VALUE";

    private final String configClusterName = "test";
    private ProxyConfiguration proxyConfig = new ProxyConfiguration();
    private WebServer webServer;
    private BrokerDiscoveryProvider discoveryProvider;
    private AdminProxyWrapper adminProxyHandler;

    @BeforeMethod
    @Override
    protected void setup() throws Exception {
        // enable tls and auth&auth at broker
        conf.setAuthenticationEnabled(true);
        conf.setAuthorizationEnabled(true);

        conf.setTlsEnabled(true);
        conf.setTlsTrustCertsFilePath(TLS_TRUST_CERT_FILE_PATH);
        conf.setTlsCertificateFilePath(TLS_SERVER_CERT_FILE_PATH);
        conf.setTlsKeyFilePath(TLS_SERVER_KEY_FILE_PATH);
        conf.setTlsAllowInsecureConnection(true);

        Set<String> superUserRoles = new HashSet<>();
        superUserRoles.add("localhost");
        superUserRoles.add("superUser");
        conf.setSuperUserRoles(superUserRoles);

        conf.setBrokerClientAuthenticationPlugin(AuthenticationTls.class.getName());
        conf.setBrokerClientAuthenticationParameters(
            "tlsCertFile:" + TLS_CLIENT_CERT_FILE_PATH + "," + "tlsKeyFile:" + TLS_SERVER_KEY_FILE_PATH);
        conf.setBrokerClientTrustCertsFilePath(TLS_TRUST_CERT_FILE_PATH);
        Set<String> providers = new HashSet<>();
        providers.add(AuthenticationProviderTls.class.getName());
        conf.setAuthenticationProviders(providers);

        conf.setClusterName(configClusterName);

        super.init();

        // start proxy service
        proxyConfig.setAuthenticationEnabled(true);
        proxyConfig.setAuthenticationEnabled(true);

        proxyConfig.setServicePort(PortManager.nextFreePort());
        proxyConfig.setServicePortTls(PortManager.nextFreePort());
        proxyConfig.setWebServicePort(PortManager.nextFreePort());
        proxyConfig.setWebServicePortTls(PortManager.nextFreePort());
        proxyConfig.setTlsEnabledInProxy(true);
        proxyConfig.setTlsEnabledWithBroker(true);

        // enable tls and auth&auth at proxy
        proxyConfig.setTlsCertificateFilePath(TLS_SERVER_CERT_FILE_PATH);
        proxyConfig.setTlsKeyFilePath(TLS_SERVER_KEY_FILE_PATH);
        proxyConfig.setTlsTrustCertsFilePath(TLS_TRUST_CERT_FILE_PATH);

        proxyConfig.setBrokerClientAuthenticationPlugin(AuthenticationTls.class.getName());
        proxyConfig.setBrokerClientAuthenticationParameters(
            "tlsCertFile:" + TLS_CLIENT_CERT_FILE_PATH + "," + "tlsKeyFile:" + TLS_CLIENT_KEY_FILE_PATH);
        proxyConfig.setBrokerClientTrustCertsFilePath(TLS_TRUST_CERT_FILE_PATH);
        proxyConfig.setAuthenticationProviders(providers);

        proxyConfig.setZookeeperServers(DUMMY_VALUE);
        proxyConfig.setGlobalZookeeperServers(DUMMY_VALUE);

        webServer = new WebServer(proxyConfig);

        discoveryProvider = spy(new BrokerDiscoveryProvider(proxyConfig, mockZooKeeperClientFactory));
        LoadManagerReport report = new LoadReport(brokerUrl.toString(), brokerUrlTls.toString(), null, null);
        doReturn(report).when(discoveryProvider).nextBroker();

        adminProxyHandler = new AdminProxyWrapper(proxyConfig, discoveryProvider);
        ServletHolder servletHolder = new ServletHolder(adminProxyHandler);
        servletHolder.setInitParameter("preserveHost", "true");
        webServer.addServlet("/admin", servletHolder);
        webServer.addServlet("/lookup", servletHolder);

        // start web-service
        webServer.start();
    }

    @AfterMethod
    @Override
    protected void cleanup() throws Exception {
        webServer.stop();
        super.internalCleanup();
    }

    @Test
    public void testAuthenticatedProxy() throws Exception {
        Map<String, String> authParams = Maps.newHashMap();
        authParams.put("tlsCertFile", TLS_CLIENT_CERT_FILE_PATH);
        authParams.put("tlsKeyFile", TLS_CLIENT_KEY_FILE_PATH);

        admin = spy(PulsarAdmin.builder()
            .serviceHttpUrl("https://localhost:" + proxyConfig.getWebServicePortTls())
            .tlsTrustCertsFilePath(TLS_TRUST_CERT_FILE_PATH)
            .allowTlsInsecureConnection(true)
            .authentication(AuthenticationTls.class.getName(), authParams)
            .build());

        List<String> activeBrokers = admin.brokers().getActiveBrokers(configClusterName);
        Assert.assertEquals(activeBrokers.size(), 1);
        Assert.assertEquals(adminProxyHandler.rewrittenUrl, String.format("%s/admin/v2/brokers/%s",
                brokerUrlTls.toString(), configClusterName));
    }

    static class AdminProxyWrapper extends AdminProxyHandler {
        String rewrittenUrl;

        AdminProxyWrapper(ProxyConfiguration config, BrokerDiscoveryProvider discoveryProvider) {
            super(config, discoveryProvider);
        }

        @Override
        protected String rewriteTarget(HttpServletRequest clientRequest) {
            rewrittenUrl = super.rewriteTarget(clientRequest);
            return rewrittenUrl;
        }

    }

}
