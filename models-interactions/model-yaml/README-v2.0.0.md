Copyright 2018-2019 AT&T Intellectual Property. All rights reserved.
Modifications Copyright (C) 2019 Nordix Foundation.
This file is licensed under the CREATIVE COMMONS ATTRIBUTION 4.0 INTERNATIONAL LICENSE
Full license text at https://creativecommons.org/licenses/by/4.0/legalcode

ONAP Control Loop Policy v2.0.0

A control loop policy is a YAML specification for creating and chaining policies for ControlLoop.

Features of ONAP Control Loop Policy v2.0.0:

* Backward compatible with ONAP Control Loop Policy v1.0.0
* A single DCAE Closed Loop Event is the trigger for the overall Control Loop Policy. 
* An overall timeout for the Control Loop Policy must be provided.
* An abatement flag indicating whether Policy will receive abatement event for the Control Loop could be provided.
* The Control Loop Policy can contain zero or more Operational Policies each chained together via outcomes of each policy.
* If there are zero Operational Policies, i.e. no automated action is to be taken, then the policy is an Open Loop policy.
* Operational policies can have target, retries and timeout's given to control how they are processed.
* Type and resourceID of the target could be provided to support the target in operational policies.
* Payload could be provided to support the recipe. 
* Multiple actors along with their supported recipes can be specified in operational policies that Policy will interact with. The following table summarizes the supported actors and recipes.

| Actor        | Recipe                      | Target   | Payload  |
| -------------|:---------------------------:| ---------| ------------:|
| APPC         | Restart                     | VM       | CloudVServerSelfLink, CloudIdentity |
| APPC         | Rebuild                     | VM   	| CloudVServerSelfLink, CloudIdentity |
| APPC         | Migrate          			 | VM   	| CloudVServerSelfLink, CloudIdentity |
| APPC         | ModifyConfig     			 | VNF  	| generic-vnf.vnf-id |
| SO           | VF Module Create 			 | VFC  	| optional |


This SDK helps build the YAML specification for ONAP Control Loop Policy v2.0.0.

# Create Builder Object

To begin with, the ControlLoopPolicyBuilder.Factory class has static methods that one should use to begin building a Control Loop Policy. It will return a [ControlLoopPolicyBuilder object](src/main/java/org/onap/policy/controlloop/policy/builder/ControlLoopPolicyBuilder.java) that can then be used to continue to build and define the Control Loop Policy.

```java
		ControlLoopPolicyBuilder builder = ControlLoopPolicyBuilder.Factory.buildControlLoop(
				UUID.randomUUID().toString(), 
				2400, 
				new Resource("sampleResource", ResourceType.VF),
				new Service("sampleService")
				);
```

# Define the Trigger Policy

After the name of the Control Loop and the resource and services have been defined, the next step would be to define the Operation Policy that is first to respond to an incoming DCAE Closed Loop Event. Use the setTriggerPolicy() method to do so.

```java
		Policy triggerPolicy = builder.setTriggerPolicy(
				"Restart the VM", 
				"Upon getting the trigger event, restart the VM", 
				"APPC", 
				new Target(TargetType.VM), 
				"Restart", 
				null,
				2, 
				300);
```

# Set the Abatement Flag for the Control Loop

After the trigger policy, the name, the resource(s) and services of the Control Loop have been defined, the next optional step would be to set the abatement flag that indicates whether DCAE will send Policy the abatement event for this Control Loop. If the abatement is not explicitly set, it is assumed that Policy will not receive the abatement event. Use the setAbatement() method to do so.

```java 
	    builder = builder.setAbatement(false);
```

# Chain Operational Policies Together Using Operational Results

Operational Policies are chained together using the results of each Operational Policy. The results are defined in [PolicyResult.java](src/main/java/org/onap/policy/controlloop/policy/PolicyResult.java). To create an Operational Policy that is tied to the result of another, use the 
setPolicyForPolicyResult() method.

```java
		Policy onRestartFailurePolicy = builder.setPolicyForPolicyResult(
				"Rebuild VM", 
				"If the restart fails, rebuild it.", 
				"APPC", 
				new Target(TargetType.VM), 
				"Rebuild", 
				null,
				1, 
				600, 
				triggerPolicy.id, 
				PolicyResult.FAILURE,
				PolicyResult.FAILURE_RETRIES,
				PolicyResult.FAILURE_TIMEOUT,
				PolicyResult.FAILURE_GUARD);
```

An Operational Policy MUST have place to go for every one of its results. By default, each result type goes to a Final Result. Optionally, using the setPolicyForPolicyResult() method is what allows the chaining of policies. Be aware of creating loops and set the overall Control Loop timeout to reasonable value. All paths MUST lead to a Final Result.



# Build the YAML Specification

When finished defining the Policies, build the specification and analyze the [Results.java](src/main/java/org/onap/policy/controlloop/policy/builder/Results.java)

```java
		Results results = builder.buildSpecification();
		if (results.isValid()) {
			System.out.println(results.getSpecification());
		} else {
			System.err.println("Builder failed");
			for (Message message : results.getMessages()) {
				System.err.println(message.getMessage());
			}
		}
```


# Use the YAML Specification to call the Create Policy API

Now that you have a valid YAML specification, call the createPolicy API via the ONAP Policy Platform API.


# YAML Specification

The YAML specification has 2 sections to it: [controlLoop](#controlloop-object) and [policies](#policies-array). The [controlLoop section](#controlloop-object) section is simply a header defining the Control Loop Policy, what services its for, which resource its for, or if its for a pnf, the overall timeout, the abatement flag, and which Operational Policy is triggered upon receiving the event. The [policies section](#policies-array) is simply an array of [Policy Objects](#policy-object).

## controlLoop Object

| Field Name      | Type          | Required   | Description  |
| -------------   |:-------------:| -----------| ------------:|
| controlLoopName | string        | required | Unique ID for the control Loop |
| version         | string        | required | Value for this release if 1.0.0 |
| services        | array of [service](#service-object) objects | optional | Zero or more services associated with this Control Loop |
| resources        | array of [resource](#resource-object) object | required (If NOT a pnf control loop) | The resource's associated with this Control Loop. |
| pnf             | [pnf](#pnf-object) object | required (If NOT a resource control loop) | The physical network function associated with this Control Loop. |
| trigger_policy  | string     | required | Either this is the ID of an Operation Policy (see policy object), or "Final_OpenLoop" indicating an Open Loop |
| timeout         | int | required | This is the overall timeout for the Control Loop Policy. It can be 0 for an Open Loop, but otherwise should total more than the timeouts specified in any Operational Policies |
| abatement       | boolean       | optional | This is an abatement flag indicating if DCAE will send abatement event to Policy for this Control Loop |

### resource Object

This object was derived via SDC Catalog API and SDC Data Dictionary (POC) in an attempt to use common naming conventions.

| Field Name      | Type          | Required   | Description  |
| -------------   |:-------------:| -----------| ------------:|
| resourceInvariantUUID | string - UUID | optional | via SDC, the unique ID for the resource version |
| resourceName | string | required if NO resourceUUID available | Name of the resource, ideally from SDC catalog. But if not available, use well-known name. |
| resourceType | string | optional | Use values defined by SDC: VF, VFC, VL, CP. |
| resourceUUID | string - UUID | required IF available, else populate resourceName | Unique ID for the resource as assigned via SDC.
| resourceVersion | string | optional | string version of the resource via SDC catalog


### service Object

This object was derived via SDC Catalog API and SDC Data Dictionary (POC) in an attempt to use common naming conventions.

| Field Name      | Type          | Required   | Description  |
| ---------------:| -------------:| ----------:| ------------:|
| serviceInvariantUUID | string - UUID | optional | via SDC catalog, the unique ID for the service version |
| serviceName | string | required if NO serviceUUID available | Name of the service, ideally from SDC catalog. But if not available, use well-known name. |
| serviceUUID | string - UUID | required IF available, else populate serviceName | Unique ID fort he service as assigned via SDC
| serviceVersion | string | optional | string version of the service via SDC catalog
    

### pnf Object

This object is used for a physical network function. Expect this object to change in the future when ONAP Policy fully integrates with A&AI.

| Field Name      | Type          | Required   | Description  |
| -------------   |:-------------:| -----------| ------------:|
| PNFName         | string        | required   | Name of the PNF. Should be supplied via A&AI. If not available use a well-known name. |
| PNFType         | string        | optional   | Type of PNF if available. |


## policies array

The policies section is an array of [Policy objects](#policy-object).

### Policy Object

This is an Operation Policy. It is used to instruct an actor (eg. APPC) to invoke a recipe (eg. "Restart") on a target entity (eg. a "VM"). An operation is simply defined as performing a recipe (or operation) on an actor.

| Field Name      | Type          | Required   | Description  |
| -------------   |:-------------:| -----------| ------------:|
| id              | string        | required   | Unique ID for the policy.
| name            | string        | required   | Policy name |
| description     | string        | optional   | Policy description |
| actor           | string        | required   | Name of the actor for this operation: Example: APPC |
| recipe          | string        | required   | Name of recipe to be performed. Example "Restart" |
| target          | [target](#target-object) object        | required   | Entity being targeted. Example: VM |
| timeout         | int           | required   | Timeout for the actor to perform the recipe. |
| retry           | int           | optional   | Optional number of retries for ONAP Policy to invoke the recipe on the actor. |
| success         | string        | required   | By default, this value should be FINAL_SUCCESS. Otherwise this can be the ID of the operational Policy (included in this specification) to invoke upon successfully completing the recipe on the actor.
| failure         | string        | required   | By default, this value should be FINAL_FAILURE. Otherwise this can be the ID of the operational Policy (included in this specification) to invoke upon failure to perform the operation. |
| failure_exception | string      | required   | By default, this value should be FINAL_FAILURE_EXCEPTION. Otherwise this can be the ID of an Operational Policy (included in this specification) to invoke upon an exception occurring while attempting to perform the operation. |
| failure_retries | string        | required   | By default, this value should be the FINAL_FAILURE_RETRIES. Otherwise this can be the ID of an Operational Policy (included in this specification) to invoke upon maxing out on retries while attempting to perform the operation. |
| failure_timeout | string        | required   | By default, this value should be FINAL_FAILURE_TIMEOUT. Otherwise this can be the ID of the operational Policy (included in this specification) to invoke upon a timeout occuring while performing an operation. |
| failure_guard   | string        | required   | By default, this value should be FINAL_FAILURE_GUARD. Otherwise this can be the ID of the operational Policy (included in this specification) to invoke upon Guard denies this operation. |

Every Operational Policy MUST have a place to go for every possible result (success, failure, failure_retries, failure_timeout, failure_exception, failure_guard). By default, all the results are final results.
  
#### target Object

This object is used for defining a target entity of a recipe.  

| Field Name      | Type          | Required   | Description  |
| -------------   |:-------------:| -----------| ------------:|
| type            | enums of VM, PNF and VNC | required   | Type of the target. |
| resourceID      | string        | optional   | Resource ID of the target. Should be supplied via SDC Catalog. |
  
  
## Examples of YAML Control Loops v2.0.0

[vService](src/test/resources/v2.0.0/policy_vService.yaml)
[ONAP-vFirewall](src/test/resources/v2.0.0/policy_ONAP_demo_vFirewall.yaml)
[ONAP-vDNS](src/test/resources/v2.0.0/policy_ONAP_demo_vDNS.yaml)

### vService
``` 
controlLoop:
  version: 2.0.0
  controlLoopName: ControlLoop-vService-cbed919f-2212-4ef7-8051-fe6308da1bda
  services: 
    - serviceName: service1
  resources: 
    - resourceName: resource1
      resourceType: VFC
    - resourceName: resource2
      resourceType: VFC
    - resourceName: resource3
      resourceType: VFC
    - resourceName: resource4
      resourceType: VFC
    - resourceName: resource5
      resourceType: VFC
  trigger_policy: unique-policy-id-1-restart
  timeout: 1200
  abatement: false

policies:
  - id: unique-policy-id-1-restart
    name: Restart Policy
    description:
    actor: APPC
    recipe: Restart
    target:
      type: VM
    retry: 2
    timeout: 300
    success: final_success
    failure: unique-policy-id-2-rebuild
    failure_timeout: unique-policy-id-2-rebuild
    failure_retries: unique-policy-id-2-rebuild
    failure_exception: final_failure_exception
    failure_guard: unique-policy-id-2-rebuild
  
  - id: unique-policy-id-2-rebuild
    name: Rebuild Policy
    description:
    actor: APPC
    recipe: Rebuild
    target:
      type: VM 
    retry: 0
    timeout: 600
    success: final_success
    failure: unique-policy-id-3-migrate
    failure_timeout: unique-policy-id-3-migrate
    failure_retries: unique-policy-id-3-migrate
    failure_exception: final_failure_exception
    failure_guard: unique-policy-id-3-migrate
  
  - id: unique-policy-id-3-migrate
    name: Migrate Policy
    description:
    actor: APPC
    recipe: Migrate
    target: 
      type: VM
    retry: 0
    timeout: 600
    success: final_success
    failure: final_failure
    failure_timeout: final_failure_timeout
    failure_retries: final_failure_retries
    failure_exception: final_failure_exception
    failure_guard: final_failure_guard
```



### ONAP vFirewall
```
controlLoop:
  version: 2.0.0
  controlLoopName: ControlLoop-vFirewall-d0a1dfc6-94f5-4fd4-a5b5-4630b438850a
  services: 
    - serviceInvariantUUID: 5cfe6f4a-41bc-4247-8674-ebd4b98e35cc
      serviceUUID: 0f40bba5-986e-4b3c-803f-ddd1b7b25f24
      serviceName: 57e66ea7-0ed6-45c7-970f
  trigger_policy: unique-policy-id-1-modifyConfig
  timeout: 1200

policies:
  - id: unique-policy-id-1-modifyConfig
    name: Change the Load Balancer
    description:
    actor: APPC
    recipe: ModifyConfig
    target:
      resourceID: Eace933104d443b496b8.nodes.heat.vpg
    payload:
      generic-vnf.vnf-id: {generic-vnf.vnf-id}
      ref$: payload.json
    retry: 0
    timeout: 300
    success: final_success
    failure: final_failure
    failure_timeout: final_failure_timeout
    failure_retries: final_failure_retries
    failure_exception: final_failure_exception
    failure_guard: final_failure_guard
```

### ONAP vDNS
```
controlLoop:
  version: 2.0.0
  controlLoopName: ControlLoop-vDNS-6f37f56d-a87d-4b85-b6a9-cc953cf779b3
  trigger_policy: unique-policy-id-1-scale-up
  timeout: 1200
  abatement: false

policies:
  - id: unique-policy-id-1-scale-up
    name: Create a new VF Module
    description:
    actor: SO
    recipe: VF Module Create
    target:
      type: VNF
    payload:
      requestParameters: '{"usePreload":true,"userParams":[]}'
      configurationParameters: '[{"ip-addr":"$.vf-module-topology.vf-module-parameters.param[9]","oam-ip-addr":"$.vf-module-topology.vf-module-parameters.param[16]","enabled":"$.vf-module-topology.vf-module-parameters.param[23]"}]'
    retry: 0
    timeout: 1200
    success: final_success
    failure: final_failure
    failure_timeout: final_failure_timeout
    failure_retries: final_failure_retries
    failure_exception: final_failure_exception
    failure_guard: final_failure_guard
```


# Control Loop Final Results Explained

A Control Loop Policy has the following set of final results, as defined in [FinalResult.java](src/main/java/org/onap/policy/controlloop/policy/FinalResult.java). A final result indicates when a Control Loop Policy has finished execution and is finished processing a Closed Loop Event. All paths must lead to a Final Result.

