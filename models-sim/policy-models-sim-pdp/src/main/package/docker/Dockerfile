#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019-2021,2023-2025 OpenInfra Foundation Europe. All rights reserved.
#  Modifications Copyright (C) 2019 AT&T Intellectual Property.
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
# Docker file to build an image that runs the PDP simulator on Java 8 in alpine
#

FROM onap/policy-jre-alpine:5.0.0-SNAPSHOT

LABEL maintainer="Policy Team"

ARG POLICY_LOGS=/var/log/onap/policy/pdp-sim

ENV POLICY_HOME=/opt/app/policy
ENV POLICY_LOGS=${POLICY_LOGS}

# Create PDP simulator user and group
# Add simulator-specific directories and set ownership as the simulator user
USER root
RUN mkdir -p ${POLICY_HOME}/pdp-sim \
    && mkdir -p ${POLICY_HOME}/pdp-sim/bin \
    && mkdir -p ${POLICY_LOGS} \
    && chown -R policy:policy ${POLICY_LOGS} \
    && mkdir /packages

# Unpack the tarball
COPY policy-models-sim-pdp-tarball.tar.gz /packages
RUN tar xvfz /packages/policy-models-sim-pdp-tarball.tar.gz --directory ${POLICY_HOME}/pdp-sim \
    && rm /packages/policy-models-sim-pdp-tarball.tar.gz

# Ensure everything has the correct permissions
# Copy examples to PDP simulator user area
COPY pdp-sim.sh ${POLICY_HOME}/pdp-sim/bin
RUN find /opt/app -type d -perm 755 \
    && find /opt/app -type f -perm 644 \
    && chmod 755 ${POLICY_HOME}/pdp-sim/bin/*

USER policy
ENV PATH ${POLICY_HOME}/pdp-sim/bin:$PATH
ENTRYPOINT [ "sh", "/opt/app/policy/pdp-sim/bin/pdp-sim.sh" ]
