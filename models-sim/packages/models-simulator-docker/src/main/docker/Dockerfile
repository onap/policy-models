#
# ============LICENSE_START=======================================================
# ONAP
# ================================================================================
# Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
# Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

FROM onap/policy-jre-alpine:2.4.3

LABEL maintainer="Policy Team"

ARG POLICY_LOGS=/var/log/onap/policy/simulators

ENV POLICY_LOGS=$POLICY_LOGS
ENV POLICY_HOME=$POLICY_HOME/simulators

RUN mkdir -p $POLICY_HOME $POLICY_LOGS $POLICY_HOME/bin && \
    chown  -R policy:policy $POLICY_HOME $POLICY_LOGS && \
    mkdir /packages
COPY /maven/lib/models-simulator.tar.gz /packages

RUN tar xvfz /packages/models-simulator.tar.gz --directory ${POLICY_HOME} \
    && rm /packages/models-simulator.tar.gz

WORKDIR $POLICY_HOME
COPY simulators.sh bin/.

RUN chown -R policy:policy * && chmod 755 bin/*.sh && chown -R policy:policy /app

USER policy
WORKDIR $POLICY_HOME/bin
ENTRYPOINT [ "./simulators.sh" ]
