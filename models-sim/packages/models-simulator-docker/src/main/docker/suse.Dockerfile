#
# ============LICENSE_START=======================================================
# ONAP
# ================================================================================
# Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
# Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

#
# Docker file to build an image that runs the simulators
#

FROM opensuse/leap:15.3

LABEL maintainer="Policy Team"

ARG POLICY_LOGS=/var/log/onap/policy/simulators

ENV POLICY_LOGS=$POLICY_LOGS
ENV POLICY_HOME=/opt/app/policy
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_HOME=/usr/lib64/jvm/java-11-openjdk-11

# Create DMaaP simulator user and group
# Add simulator-specific directories and set ownership as the simulator user
RUN zypper -n -q install --no-recommends gzip java-11-openjdk-headless netcat-openbsd tar && \
    zypper -n -q update; zypper -n -q clean --all && \
    groupadd --system policy && \
    useradd --system --shell /bin/sh -G policy policy && \
    mkdir -p /opt/app $POLICY_LOGS $POLICY_HOME/simulators $POLICY_HOME/simulators/bin && \
    chown -R policy:policy /opt/app $POLICY_HOME $POLICY_LOGS && \
    mkdir /packages

# Unpack the tarball
COPY /maven/* /packages
RUN tar xvfz /packages/models-simulator.tar.gz --directory ${POLICY_HOME}/simulators \
    && rm /packages/models-simulator.tar.gz

# Ensure everything has the correct permissions
# Copy scripts simulator user area
COPY simulators.sh ${POLICY_HOME}/simulators/bin
RUN find /opt/app -type d -perm 755 \
    && find /opt/app -type f -perm 644 \
    && chmod 755 ${POLICY_HOME}/simulators/bin/* \
    && chown -R policy:policy $POLICY_HOME $POLICY_LOGS

USER policy:policy

ENV PATH ${POLICY_HOME}/simulators/bin:$PATH
ENTRYPOINT [ "simulators.sh" ]
