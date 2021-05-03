/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

import lombok.Data;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.provider.impl.DatabasePolicyModelsProviderImpl;

// @formatter:off
/**
 * Class to hold all the plugin handler parameters.
 *
 * <p>The following parameters are defined:
 * <ol>
 * <li>name: A name for the parameters.
 * <li>implementation: The implementation of the PolicyModelsProvider to use for writing and reading concepts,
 * defaults to {@link DatabasePolicyModelsProviderImpl} and may not be null
 * <li>databaseUrl: The JDBC URL for the database, mandatory.
 * <li>databaseUser: The user id to use for connecting to the database, optional, defaults to null.
 * <li>databasePassword: The password to use for connecting to the database, optional,
 * defaults to null.
 * <li>persistenceUnit: The persistence unit refined in META-INF/persistence.xml to use for connecting
 * to the database, mandatory.
 * </ol>
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
//@formatter:on

@Data
public class PolicyModelsProviderParameters implements ParameterGroup {
    private static final String DEFAULT_IMPLEMENTATION = DatabasePolicyModelsProviderImpl.class.getName();

    private String name;
    @NotNull @NotBlank
    private String implementation = DEFAULT_IMPLEMENTATION;
    private String databaseType;
    @NotNull @NotBlank
    private String databaseDriver;
    @NotNull @NotBlank
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    @NotNull @NotBlank
    private String persistenceUnit;

    /**
     * Validate the model provider parameters.
     *
     */

    @Override
    public BeanValidationResult validate() {
        return new BeanValidator().validateTop(getClass().getSimpleName(), this);
    }
}
