/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2021 Bell Canada. All rights reserved.
 * Modifications Copyright 2023-2024 Nordix Foundation.
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
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.common.utils.services.ServiceManagerContainer;
import org.onap.policy.simulators.CdsSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class runs all simulators specified in the parameter file.
 */
public class Main extends ServiceManagerContainer {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String CANNOT_CONNECT = "cannot connect to port ";

    @Getter(AccessLevel.PROTECTED)
    private static Main instance;


    /**
     * Runs the simulators.
     *
     * @param paramFile parameter file name
     */
    public Main(String paramFile) {
        super(Main.class.getPackage().getName());

        SimulatorParameters params = readParameters(paramFile);

        CdsServerParameters cdsServer = params.getGrpcServer();

        // Cds Simulator
        if (cdsServer != null) {
            AtomicReference<CdsSimulator> cdsSim = new AtomicReference<>();
            addAction(cdsServer.getName(), () -> cdsSim.set(buildCdsSimulator(cdsServer)), () -> cdsSim.get().stop());
        }

        // REST server simulators
        // @formatter:off
        for (ClassRestServerParameters restsim : params.getRestServers()) {
            AtomicReference<HttpServletServer> ref = new AtomicReference<>();
            if (StringUtils.isNotBlank(restsim.getResourceLocation())) {
                String resourceLocationId = restsim.getProviderClass() + "_RESOURCE_LOCATION";
                addAction(resourceLocationId,
                    () -> Registry.register(resourceLocationId, restsim.getResourceLocation()),
                    () -> Registry.unregister(resourceLocationId));
            }
            addAction(restsim.getName(),
                () -> ref.set(buildRestServer(restsim)),
                () -> ref.get().shutdown());
        }
        // @formatter:on
    }

    /**
     * The main method. The arguments are validated, thus adding the NOSONAR.
     *
     * @param args the arguments, the first of which is the name of the parameter file
     */
    public static void main(final String[] args) { // NOSONAR
        /*
         * Only one argument is used and is validated implicitly by the constructor (i.e.,
         * file-not-found), thus sonar is disabled.
         */

        try {
            if (args.length != 1) {
                throw new IllegalArgumentException("arg(s): parameter-file-name");
            }

            instance = new Main(args[0]);
            instance.start();

        } catch (RuntimeException e) {
            logger.error("failed to start simulators", e);
        }
    }

    private SimulatorParameters readParameters(String paramFile) {
        try {
            var paramsJson = getResourceAsString(paramFile);
            if (paramsJson == null) {
                throw new IllegalArgumentException(new FileNotFoundException(paramFile));
            }

            String hostName = NetworkUtil.getHostname();
            logger.info("replacing 'HOST_NAME' with {} in {}", hostName, paramFile);

            paramsJson = paramsJson.replace("${HOST_NAME}", hostName);

            return makeCoder().decode(paramsJson, SimulatorParameters.class);

        } catch (CoderException e) {
            throw new IllegalArgumentException("cannot decode " + paramFile, e);
        }
    }

    private CdsSimulator buildCdsSimulator(CdsServerParameters params) throws IOException {
        var cdsSimulator = new CdsSimulator(params.getHost(), params.getPort(), params.getResourceLocation(),
            params.getSuccessRepeatCount(), params.getRequestedResponseDelayMs());
        cdsSimulator.start();
        return cdsSimulator;
    }


    private HttpServletServer buildRestServer(ClassRestServerParameters params) {
        try {
            var props = getServerProperties(params);
            HttpServletServer testServer = makeServer(props);
            testServer.waitedStart(5000);

            String svcpfx = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + params.getName();
            String hostName = props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX);

            if (!isTcpPortOpen(hostName, testServer.getPort())) {
                throw new IllegalStateException(CANNOT_CONNECT + testServer.getPort());
            }

            return testServer;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while building " + params.getName(), e);
        }
    }


    /**
     * Creates a set of properties, suitable for building a REST server, from the
     * parameters.
     *
     * @param params parameters from which to build the properties
     * @return a Map of properties representing the given parameters
     */
    private static Properties getServerProperties(ClassRestServerParameters params) {
        final var props = new Properties();
        props.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, params.getName());

        final String svcpfx = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + params.getName();

        props.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, params.getName());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, params.getHost());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX,
                        Integer.toString(params.getPort()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX,
                        Boolean.toString(params.isHttps()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX,
                        params.getProviderClass());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "false");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, "false");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SNI_HOST_CHECK_SUFFIX, "false");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");

        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER, String.join(",",
                            GsonMessageBodyHandler.class.getName(), TextMessageBodyHandler.class.getName()));


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
