#!/bin/sh
#
# ============LICENSE_START=======================================================
# ONAP
# ================================================================================
# Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
# Modifications copyright (C) 2020 Bell Canada. All rights reserved.
# Modifications Copyright (C) 2022 Nordix Foundation.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================
#

if [ -z "$SIMULATOR_HOME" ]
then
    SIMULATOR_HOME=${POLICY_HOME}/simulators
fi

KEYSTORE="${SIMULATOR_HOME}/etc/ssl/policy-keystore"
KEYSTORE_PASSWD="Pol1cy_0nap"
TRUSTSTORE="${SIMULATOR_HOME}/etc/ssl/policy-truststore"
TRUSTSTORE_PASSWD="Pol1cy_0nap"

${JAVA_HOME}/bin/java \
    -cp "${SIMULATOR_HOME}/etc:${SIMULATOR_HOME}/lib/*" \
    -Djavax.net.ssl.keyStore="${KEYSTORE}" \
    -Djavax.net.ssl.keyStorePassword="${KEYSTORE_PASSWD}" \
    -Djavax.net.ssl.trustStore="${TRUSTSTORE}" \
    -Djavax.net.ssl.trustStorePassword="${TRUSTSTORE_PASSWD}" \
    org.onap.policy.models.simulators.Main \
        ${SIMULATOR_HOME}/etc/mounted/simParameters.json
