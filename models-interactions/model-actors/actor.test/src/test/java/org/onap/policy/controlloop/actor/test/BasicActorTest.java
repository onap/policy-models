/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.controlloop.actor.test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;

public class BasicActorTest extends BasicActor {

    @Test
    public void testVerifyActorService_testStartOtherServices_testStopOtherServices() {
        // mostly empty service
        verifyActorService(DummyActor.NAME, "service.yaml");

        // service with Topics and HTTP Clients
        verifyActorService(DummyActor.NAME, "serviceFull.yaml");

        assertThatIllegalArgumentException()
                        .isThrownBy(() -> verifyActorService(DummyActor.NAME, "serviceInvalidHttp.yaml"));

        assertThatIllegalArgumentException()
                        .isThrownBy(() -> verifyActorService(DummyActor.NAME, "serviceMissingActors.yaml"));

        // config file not found
        assertThatThrownBy(() -> verifyActorService(DummyActor.NAME, "file-not-found.yaml"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasCauseInstanceOf(FileNotFoundException.class);
    }
}
