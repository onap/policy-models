/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2023-2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.policy.simulators;

import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.utils.network.NetworkUtil;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Util {
    public static final String AAISIM_SERVER_NAME = "aaiSim";
    public static final String SOSIM_SERVER_NAME = "soSim";
    public static final String XACMLSIM_SERVER_NAME = "xacmlSim";
    public static final String SDNCSIM_SERVER_NAME = "sdncSim";

    public static final int AAISIM_SERVER_PORT = 6666;
    public static final int SOSIM_SERVER_PORT = 6667;
    public static final int XACMLSIM_SERVER_PORT = 6669;
    public static final int SDNCSIM_SERVER_PORT = 6670;
    public static final int CDSSIM_SERVER_PORT = 6671;

    private static final String CANNOT_CONNECT = "cannot connect to port ";
    public static final String LOCALHOST = "localhost";

    /**
     * Build an A&AI simulator.
     *
     * @return the simulator
     * @throws InterruptedException if a thread is interrupted
     */
    public static HttpServletServer buildAaiSim() throws InterruptedException {
        final HttpServletServer testServer = HttpServletServerFactoryInstance.getServerFactory()
                .build(AAISIM_SERVER_NAME, LOCALHOST, AAISIM_SERVER_PORT, "/", false, true);
        testServer.addServletClass("/*", AaiSimulatorJaxRs.class.getName());
        testServer.waitedStart(5000);
        waitForServerToListen(testServer.getPort());
        return testServer;
    }

    /**
     * Build a CDS simulator.
     *
     * @return the simulator
     * @throws InterruptedException if a thread is interrupted
     * @throws IOException if an I/O error occurs
     */
    public static CdsSimulator buildCdsSim() throws InterruptedException, IOException {
        final var testServer = new CdsSimulator(LOCALHOST, CDSSIM_SERVER_PORT);
        testServer.start();
        waitForServerToListen(testServer.getPort());
        return testServer;
    }

    /**
     * Build an SDNC simulator.
     *
     * @return the simulator
     * @throws InterruptedException if a thread is interrupted
     */
    public static HttpServletServer buildSdncSim() throws InterruptedException {
        final HttpServletServer testServer = HttpServletServerFactoryInstance.getServerFactory()
                .build(SDNCSIM_SERVER_NAME, LOCALHOST, SDNCSIM_SERVER_PORT, "/", false, true);
        testServer.addServletClass("/*", SdncSimulatorJaxRs.class.getName());
        testServer.waitedStart(5000);
        waitForServerToListen(testServer.getPort());
        return testServer;
    }


    /**
     * Build a SO simulator.
     *
     * @return the simulator
     * @throws InterruptedException if a thread is interrupted
     */
    public static HttpServletServer buildSoSim() throws InterruptedException {
        final HttpServletServer testServer = HttpServletServerFactoryInstance.getServerFactory()
                .build(SOSIM_SERVER_NAME, LOCALHOST, SOSIM_SERVER_PORT, "/", false, true);
        testServer.addServletClass("/*", SoSimulatorJaxRs.class.getName());
        testServer.waitedStart(5000);
        waitForServerToListen(testServer.getPort());
        return testServer;
    }

    /**
     * Build a guard simulator.
     *
     * @return the simulator
     * @throws InterruptedException if a thread is interrupted
     */
    public static HttpServletServer buildGuardSim() throws InterruptedException {
        return buildXacmlSim();
    }

    /**
     * Build a xacml simulator.
     *
     * @return the simulator
     * @throws InterruptedException if a thread is interrupted
     */
    public static HttpServletServer buildXacmlSim() throws InterruptedException {
        HttpServletServer testServer = HttpServletServerFactoryInstance.getServerFactory().build(XACMLSIM_SERVER_NAME,
                LOCALHOST, XACMLSIM_SERVER_PORT, "/", false, true);
        testServer.addServletClass("/*", XacmlSimulatorJaxRs.class.getName());
        testServer.waitedStart(5000);
        waitForServerToListen(testServer.getPort());
        return testServer;
    }

    private static void waitForServerToListen(int port) throws InterruptedException {
        if (!NetworkUtil.isTcpPortOpen(LOCALHOST, port, 200, 250L)) {
            throw new IllegalStateException(CANNOT_CONNECT + port);
        }
    }
}
