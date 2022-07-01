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

KEYSTORE="${KEYSTORE:-$POLICY_HOME/etc/ssl/policy-keystore}"
TRUSTSTORE="${TRUSTSTORE:-$POLICY_HOME/etc/ssl/policy-truststore}"
KEYSTORE_PASSWD="${KEYSTORE_PASSWD:-Pol1cy_0nap}"
TRUSTSTORE_PASSWD="${TRUSTSTORE_PASSWD:-Pol1cy_0nap}"

if [ "$#" -ge 1 ]; then
    CONFIG_FILE=$1
else
    CONFIG_FILE=${CONFIG_FILE}
fi

if [ -z "$CONFIG_FILE" ]; then
    CONFIG_FILE="${POLICY_HOME}/etc/mounted/simParameters.json"
fi

echo "Policy simulator config file: $CONFIG_FILE"

if [ -f "${POLICY_HOME}/etc/mounted/policy-truststore" ]; then
    echo "overriding policy-truststore"
    cp -f "${POLICY_HOME}"/etc/mounted/policy-truststore "${TRUSTSTORE}"
fi

if [ -f "${POLICY_HOME}/etc/mounted/policy-keystore" ]; then
    echo "overriding policy-keystore"
    cp -f "${POLICY_HOME}"/etc/mounted/policy-keystore "${KEYSTORE}"
fi

if [ -f "${POLICY_HOME}/etc/mounted/logback.xml" ]; then
    echo "overriding logback.xml"
    cp -f "${POLICY_HOME}"/etc/mounted/logback.xml "${POLICY_HOME}"/etc/
fi

${JAVA_HOME}/bin/java \
    -cp "${POLICY_HOME}/etc:${POLICY_HOME}/lib/*" \
    -Dlogging.config="${POLICY_HOME}/etc/logback.xml" \
    -Djavax.net.ssl.keyStore="${KEYSTORE}" \
    -Djavax.net.ssl.keyStorePassword="${KEYSTORE_PASSWD}" \
    -Djavax.net.ssl.trustStore="${TRUSTSTORE}" \
    -Djavax.net.ssl.trustStorePassword="${TRUSTSTORE_PASSWD}" \
    org.onap.policy.models.simulators.Main ${CONFIG_FILE}
