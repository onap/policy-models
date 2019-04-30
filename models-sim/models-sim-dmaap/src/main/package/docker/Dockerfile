#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019 Nordix Foundation.
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
# Docker file to build an image that runs the DMaaP simulator on Java 8 in alpine
#

FROM alpine:3.9

LABEL maintainer="Policy Team"

ARG BUILD_VERSION=${BUILD_VERSION}
ARG POLICY_LOGS=/var/log/onap/policy/dmaap-sim

ENV BUILD_VERSION ${BUILD_VERSION}
ENV POLICY_HOME=/opt/app/policy
ENV POLICY_DMAAP_SIM_HOME=${POLICY_HOME}/dmaap-sim
ENV POLICY_LOGS=${POLICY_LOGS}

RUN apk add --no-cache --update \
    bash \
    nss \
    procps \
    coreutils \
    findutils \
    grep \
    zip \
    unzip \
    curl \
    wget \
    openssh \
    iproute2 \
    iputils \
    vim \
    openjdk8

# Create DMaaP simulator user and group
# Add simulator-specific directories and set ownership as the simulator user
RUN addgroup -S dmaapsimuser && adduser -S dmaapsimuser -G dmaapsimuser \
    && mkdir -p ${POLICY_DMAAP_SIM_HOME} \
    && mkdir -p ${POLICY_DMAAP_SIM_HOME}/bin \
    && mkdir -p ${POLICY_LOGS} \
    && chown -R dmaapsimuser:dmaapsimuser ${POLICY_LOGS} \
    && mkdir /packages

# Unpack the tarball
COPY policy-models-sim-dmaap-tarball.tar.gz /packages
RUN tar xvfz /packages/policy-models-sim-dmaap-tarball.tar.gz --directory ${POLICY_DMAAP_SIM_HOME} \
    && rm /packages/policy-models-sim-dmaap-tarball.tar.gz

# Ensure everything has the correct permissions
# Copy examples to DMaaP simulator user area
COPY dmaap-sim.sh ${POLICY_DMAAP_SIM_HOME}/bin
RUN find /opt/app -type d -perm 755 \
    && find /opt/app -type f -perm 644 \
    && chmod a+x ${POLICY_DMAAP_SIM_HOME}/bin/*

USER dmaapsimuser
ENV PATH ${POLICY_DMAAP_SIM_HOME}/bin:$PATH
ENTRYPOINT [ "bash", "dmaap-sim.sh" ]
