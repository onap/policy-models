#!/bin/bash
#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019-2020 Nordix Foundation.
#  Modifications copyright (C) 2020 Bell Canada. All rights reserved.
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

if [ -z "$PDP_SIM_HOME" ]
then
	PDP_SIM_HOME=/opt/app/policy/pdp-sim
fi

JAVA_HOME=/usr/lib/jvm/java-17-openjdk
KEYSTORE="${PDP_SIM_HOME}/etc/ssl/policy-keystore"
KEYSTORE_PASSWD="Pol1cy_0nap"
TRUSTSTORE="${PDP_SIM_HOME}/etc/ssl/policy-truststore"
TRUSTSTORE_PASSWD="Pol1cy_0nap"

if [ "$#" -eq 1 ]
then
    CONFIG_FILE=$1
else
    CONFIG_FILE=${CONFIG_FILE}
fi

if [ -z "$CONFIG_FILE" ]
then
    CONFIG_FILE="$PDP_SIM_HOME/etc/config/OnapPfConfig.json"
fi

echo "PDP simulator configuration file:" $CONFIG_FILE

$JAVA_HOME/bin/java \
    -cp "$PDP_SIM_HOME/etc:$PDP_SIM_HOME/lib/*" \
    -Djavax.net.ssl.keyStore="$KEYSTORE" \
    -Djavax.net.ssl.keyStorePassword="$KEYSTORE_PASSWD" \
    -Djavax.net.ssl.trustStore="$TRUSTSTORE" \
    -Djavax.net.ssl.trustStorePassword="$TRUSTSTORE_PASSWD" \
    org.onap.policy.models.sim.pdp.PdpSimulatorMain \
    -c $CONFIG_FILE
