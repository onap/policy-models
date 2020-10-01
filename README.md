Copyright 2018 AT&T Intellectual Property. All rights reserved.
This file is licensed under the CREATIVE COMMONS ATTRIBUTION 4.0 INTERNATIONAL LICENSE
Full license text at https://creativecommons.org/licenses/by/4.0/legalcode

This source repository contains the ONAP Policy Model code that is agnostic to any PDP. It is
common amongst all the repositories.

To build it using Maven 3, run: mvn clean install

To build the onap/policy-models-simulator docker image:
1. Build the models repository
2. Build the docker profile of policy-models-sim/packages module. The docker images would be created.
3. This is not currently added to the Jenkins job and hence the docker image is not readily available in nexus3.
4. For testing purpose, this image could be pushed to the required docker hub.