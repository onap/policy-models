To build the onap/policy-models-simulator docker image:
1. Build the models repository
2. Build the docker profile of packages module. The docker images would be created.
3. This is not currently added to the Jenkins push and hence the docker image is not readily available in nexus3.
4. For testing purpose, this image could be pushed to the required docker hub.

To spin the kubernetes pod up:
1. Helm package is included in models. This contains the Helm chart related files for policy-models-simulator. 
2. Edit the name of the docker hub where the image is pushed in the values.yaml
2. Execute the below commands:
    helm package policy-models-simulator
    helm install --name <releaseName> policy-models-simulator 
