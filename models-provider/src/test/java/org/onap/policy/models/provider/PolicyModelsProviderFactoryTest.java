/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.provider;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import javax.ws.rs.core.Response.Status;
import lombok.ToString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.common.utils.time.CurrentTime;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.impl.DummyPolicyModelsProviderImpl;

/**
 * Test the {@link PolicyModelsProviderFactory} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@ToString
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PolicyModelsProviderFactoryTest {

    private static final String[] SEARCH_STRING_DEFAULTS = {"Connection refused", "Could not connect"};
    private static final String[] SEARCH_STRING_BAD = {"Connection not refused", "Could connect"};
    private static final int RETRY_PERIOD_SECONDS_DEFAULT = 30;
    private static final String SEARCH_STRING_1 = "SEARCH_STRING_1";
    private static final String SEARCH_STRING_2 = "SEARCH_STRING_2";
    private static final String SEARCH_STRING_3 = "SEARCH_STRING_3";

    @Mock
    private PolicyModelsProvider mockProvider;
    @Mock
    private CurrentTime mockCurrentTime;

    @Test
    public void testFactory() {
        PolicyModelsProviderFactory factory = new PolicyModelsProviderFactory();

        // @formatter:off
        assertThatThrownBy(() -> factory.createPolicyModelsProvider(null))
                .hasMessageMatching("^parameters is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            PolicyModelsProviderParameters pars = new PolicyModelsProviderParameters();
            pars.setImplementation(null);
            factory.createPolicyModelsProvider(pars);
        }).hasMessage("could not find implementation of the \"PolicyModelsProvider\" interface \"null\"");

        assertThatThrownBy(() -> {
            PolicyModelsProviderParameters pars = new PolicyModelsProviderParameters();
            pars.setImplementation("com.acmecorp.RoadRunner");
            factory.createPolicyModelsProvider(pars);
        }).hasMessage("could not find implementation of the \"PolicyModelsProvider\" "
                + "interface \"com.acmecorp.RoadRunner\"");

        assertThatThrownBy(() -> {
            PolicyModelsProviderParameters pars = new PolicyModelsProviderParameters();
            pars.setImplementation("java.lang.String");
            factory.createPolicyModelsProvider(pars);
        }).hasMessage(
                "the class \"java.lang.String\" is not an implementation of the \"PolicyModelsProvider\" interface");

        assertThatThrownBy(() -> {
            PolicyModelsProviderParameters pars = new PolicyModelsProviderParameters();
            pars.setImplementation("org.onap.policy.models.provider.impl.DummyBadProviderImpl");
            factory.createPolicyModelsProvider(pars);
        }).hasMessage("could not create an instance of PolicyModelsProvider "
                + "\"org.onap.policy.models.provider.impl.DummyBadProviderImpl\"");
        // @formatter:on
    }

    @Test
    public void testInitProviderWithRetryEventuallySucceeds() throws PfModelException, InterruptedException {

        Mockito
            .doThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_DEFAULTS[0]))
            .doThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_DEFAULTS[1]))
            .doNothing().when(mockProvider).init();
        Mockito.doNothing().when(mockCurrentTime).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);

        PolicyModelsProviderFactory factory = new PolicyModelsProviderFactory();
        assertThatCode(() ->
            factory.initProviderWithRetry(mockProvider, mockCurrentTime, new PolicyModelsProviderParameters()))
            .doesNotThrowAnyException();
        Mockito.verify(mockProvider, Mockito.times(3)).init();
        Mockito.verify(mockCurrentTime, Mockito.times(2)).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);
    }

    @Test
    public void testInitProviderWithRetryThrowsPfModelExceptionOnOtherException()
            throws PfModelException, InterruptedException {

        Mockito
            .doThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_DEFAULTS[0]))
            .doThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_BAD[0]))
            .doNothing().when(mockProvider).init();
        Mockito.doNothing().when(mockCurrentTime).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);

        PolicyModelsProviderFactory factory = new PolicyModelsProviderFactory();
        assertThrows(PfModelException.class, () ->
            factory.initProviderWithRetry(mockProvider, mockCurrentTime, new PolicyModelsProviderParameters()));
        Mockito.verify(mockProvider, Mockito.times(2)).init();
        Mockito.verify(mockCurrentTime, Mockito.times(1)).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);
    }

    @Test
    public void testInitProviderWithRetryUsesRetryPeriodParam() throws PfModelException, InterruptedException {

        final int retryPeriodSeconds = 97;
        PolicyModelsProviderParameters params = new PolicyModelsProviderParameters();
        params.setRetryPeriodSeconds(retryPeriodSeconds);

        Mockito
            .doThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_DEFAULTS[0]))
            .doNothing().when(mockProvider).init();
        Mockito.doNothing().when(mockCurrentTime).sleep(retryPeriodSeconds * 1000);

        PolicyModelsProviderFactory factory = new PolicyModelsProviderFactory();
        assertThatCode(() ->
            factory.initProviderWithRetry(mockProvider, mockCurrentTime, params))
            .doesNotThrowAnyException();

        Mockito.verify(mockProvider, Mockito.times(2)).init();
        Mockito.verify(mockCurrentTime, Mockito.times(1)).sleep(retryPeriodSeconds * 1000);
    }

    @Test
    public void testInitProviderWithRetryUsesSearchStringParam() throws PfModelException, InterruptedException {

        String[] searchStrings = {SEARCH_STRING_1, SEARCH_STRING_2, SEARCH_STRING_3};
        PolicyModelsProviderParameters params = new PolicyModelsProviderParameters();
        params.setConnectionFailedStrings(searchStrings);

        Mockito
            .doThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_1))
            .doThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_2))
            .doThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_3))
            .doNothing().when(mockProvider).init();
        Mockito.doNothing().when(mockCurrentTime).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);

        PolicyModelsProviderFactory factory = new PolicyModelsProviderFactory();
        assertThatCode(() ->
            factory.initProviderWithRetry(mockProvider, mockCurrentTime, params))
            .doesNotThrowAnyException();
        Mockito.verify(mockProvider, Mockito.times(4)).init();
        Mockito.verify(mockCurrentTime, Mockito.times(3)).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);
    }

    @Test
    public void testInitProviderWithRetrySucceedsWithNestedExceptions() throws PfModelException, InterruptedException {

        String[] searchStrings = {SEARCH_STRING_1, SEARCH_STRING_2, SEARCH_STRING_3};
        PolicyModelsProviderParameters params = new PolicyModelsProviderParameters();
        params.setConnectionFailedStrings(searchStrings);

        PfModelException nestedException =
            new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_BAD[0],
                new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_BAD[1],
                    new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_3)));

        Mockito
            .doThrow(nestedException)
            .doNothing().when(mockProvider).init();
        Mockito.doNothing().when(mockCurrentTime).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);

        PolicyModelsProviderFactory factory = new PolicyModelsProviderFactory();
        assertThatCode(() ->
            factory.initProviderWithRetry(mockProvider, mockCurrentTime, params))
            .doesNotThrowAnyException();
        Mockito.verify(mockProvider, Mockito.times(2)).init();
        Mockito.verify(mockCurrentTime, Mockito.times(1)).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);
    }

    @Test
    public void testInitProviderWithRetryThrowsLatestPfModelExceptionOnInterruptedException()
            throws PfModelException, InterruptedException {

        PfModelException exception0 = new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_DEFAULTS[0]);
        PfModelException exception1 = new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_DEFAULTS[1]);

        Mockito
            .doThrow(exception0)
            .doThrow(exception1)
            .doNothing().when(mockProvider).init();
        Mockito
            .doNothing()
            .doThrow(new InterruptedException())
            .when(mockCurrentTime).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);

        PolicyModelsProviderFactory factory = new PolicyModelsProviderFactory();
        assertThatThrownBy(() ->
            factory.initProviderWithRetry(mockProvider, mockCurrentTime, new PolicyModelsProviderParameters()))
            .hasCause(exception1);
        Mockito.verify(mockProvider, Mockito.times(2)).init();
        Mockito.verify(mockCurrentTime, Mockito.times(2)).sleep(RETRY_PERIOD_SECONDS_DEFAULT * 1000);
    }
}
