#!/bin/bash
#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019 Nordix Foundation.
#  Modifications copyright (C) 2020 Bell Canada. All rights reserved.
#  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

if [ -z "$DMAAP_SIM_HOME" ]
then
	DMAAP_SIM_HOME=/opt/app/policy/dmaap-sim
fi

JAVA_HOME=/usr/lib/jvm/java-11-openjdk

if [ "$#" -eq 1 ]
then
    CONFIG_FILE=$1
else
    CONFIG_FILE=${CONFIG_FILE}
fi

if [ -z "$CONFIG_FILE" ]
then
    CONFIG_FILE="$DMAAP_SIM_HOME/etc/DefaultConfig.json"
fi

echo "DMaaP simulation configuration file: $CONFIG_FILE"

$JAVA_HOME/bin/java \
    -cp "$DMAAP_SIM_HOME/etc:$DMAAP_SIM_HOME/lib/*" \
    org.onap.policy.models.sim.dmaap.startstop.Main \
    -c $CONFIG_FILE
