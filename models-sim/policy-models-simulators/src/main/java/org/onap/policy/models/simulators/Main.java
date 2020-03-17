/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.simulators;

import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.Getter;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.common.utils.services.ServiceManagerContainer;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterGroup;
import org.onap.policy.models.sim.dmaap.provider.DmaapSimProvider;
import org.onap.policy.models.sim.dmaap.rest.CambriaMessageBodyHandler;
import org.onap.policy.models.sim.dmaap.rest.DmaapSimRestControllerV1;
import org.onap.policy.models.sim.dmaap.rest.TextMessageBodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class initiates the DMaaP simulator component.
 */
public class Main extends ServiceManagerContainer {
    public static final String HOST_NAME = NetworkUtil.getHostname();

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String PARAMETER_FILE = "SimulatorParameters.json";
    private static final String CANNOT_CONNECT = "cannot connect to port ";

    @Getter(AccessLevel.PROTECTED)
    private static Main instance;


    /**
     * Runs the simulators.
     */
    public Main() {
        super(Main.class.getPackage().getName());

        SimulatorParameters params = readParameters();

        DmaapSimParameterGroup dmaapProv = params.getDmaapProvider();
        String dmaapName = dmaapProv.getName();
        String provName = dmaapName.replace("simulator", "provider");

        AtomicReference<DmaapSimProvider> provref = new AtomicReference<>();
        addAction(provName, () -> provref.set(buildDmaapProvider(dmaapProv)), () -> provref.get().shutdown());

        for (ClassServerParameters sim : params.getSimulators()) {
            AtomicReference<HttpServletServer> ref = new AtomicReference<>();
            addAction(sim.getName(), () -> ref.set(buildSimulator(dmaapName, sim)), () -> ref.get().shutdown());
        }
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String[] args) {
        instance = new Main();
        instance.start();
    }

    private SimulatorParameters readParameters() {
        try {
            String paramsJson = getResourceAsString(PARAMETER_FILE);
            if (paramsJson == null) {
                logger.error("cannot read " + PARAMETER_FILE);
                throw new IllegalArgumentException(new FileNotFoundException(PARAMETER_FILE));
            }

            return makeCoder().decode(paramsJson, SimulatorParameters.class);

        } catch (CoderException e) {
            logger.error("cannot decode " + PARAMETER_FILE);
            throw new IllegalArgumentException(e);
        }
    }

    private DmaapSimProvider buildDmaapProvider(DmaapSimParameterGroup params) {
        DmaapSimProvider prov = new DmaapSimProvider(params);
        DmaapSimProvider.setInstance(prov);
        prov.start();

        return prov;
    }

    private HttpServletServer buildSimulator(String dmaapName, ClassServerParameters params) {
        try {
            Properties props = getServerProperties(dmaapName, params);
            HttpServletServer testServer = makeServer(props);
            testServer.waitedStart(5000);

            String svcpfx = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + params.getName();
            String hostName = props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX);

            if (!isTcpPortOpen(hostName, testServer.getPort())) {
                throw new IllegalStateException(CANNOT_CONNECT + testServer.getPort());
            }

            return testServer;

        } catch (InterruptedException e) {
            logger.warn("interrupted while building {}", params.getName());
            Thread.currentThread().interrupt();
            throw new IllegalStateException("error building simulators", e);
        }
    }

    /**
     * Creates a set of properties, suitable for building a REST server, from the
     * parameters.
     *
     * @param params parameters from which to build the properties
     * @return a set of properties representing the given parameters
     */
    private static Properties getServerProperties(String dmaapName, ClassServerParameters params) {
        String hostName = params.getHost().replace("${HOST_NAME}", HOST_NAME);

        final Properties props = new Properties();
        props.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, params.getName());

        final String svcpfx = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + params.getName();

        props.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, params.getName());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, hostName);
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX,
                        Integer.toString(params.getPort()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX,
                        Boolean.toString(params.isHttps()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX,
                        params.getProviderClass());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "false");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, "false");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX,
                        DmaapSimRestControllerV1.class.getName());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");

        if (dmaapName.equals(params.getName())) {
            props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER,
                            String.join(",", CambriaMessageBodyHandler.class.getName(),
                                            GsonMessageBodyHandler.class.getName(),
                                            TextMessageBodyHandler.class.getName()));
        } else {
            props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER, String.join(",",
                            GsonMessageBodyHandler.class.getName(), TextMessageBodyHandler.class.getName()));
        }

        return props;
    }

    // the following methods may be overridden by junit tests

    protected String getResourceAsString(String resourceName) {
        return ResourceUtils.getResourceAsString(resourceName);
    }

    protected Coder makeCoder() {
        return new StandardCoder();
    }

    protected HttpServletServer makeServer(Properties props) {
        return HttpServletServerFactoryInstance.getServerFactory().build(props).get(0);
    }

    protected boolean isTcpPortOpen(String hostName, int port) throws InterruptedException {
        return NetworkUtil.isTcpPortOpen(hostName, port, 100, 200L);
    }
}
