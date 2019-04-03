Copyright 2018 AT&T Intellectual Property. All rights reserved.
Modifications Copyright (C) 2019 Nordix Foundation.
This file is licensed under the CREATIVE COMMONS ATTRIBUTION 4.0 INTERNATIONAL LICENSE
Full license text at https://creativecommons.org/licenses/by/4.0/legalcode

ONAP Control Loop Guard

A control loop guard is a YAML specification for creating policy guard for ControlLoop.

ONAP Control Loop Guard Features:

* The Control Loop Guard can specify the frequency limiter and the blacklist of target entities but not both in the same Guard.
* Two parts are incorporated. One is the common guard header including guard version while the other part is a set of guard policies. 
* The Control Loop Guard should contain at least one guard policies.
* Each guard policy is bound to a specific Actor and Recipe.
* Each guard policy should have at least one limit constraints which define how the guard policy should be enforced.
* Supported Actors are APPC and SO. 

This SDK helps build the YAML specification for ONAP Control Loop Guard.

# Create Builder Object

To begin with, the ControlLoopGuardBuilder.Factory class has static methods that one should use to begin building a Control Loop Guard. It will return a [ControlLoopGuardBuilder object](src/main/java/org/onap/policy/controlloop/policy/guard/builder/ControlLoopGuardBuilder.java) that can then be used to continue to build and define the Control Loop Guard.

```java
		ControlLoopGuardBuilder builder = ControlLoopGuardBuilder.Factory.buildControlLoopGuard(new Guard());
```

# Add Guard Policy

After a guard builder has been created, the next step would be to add a guard policy to the newly created Control Loop Guard via the builder. To add a guard policy, use the addGuardPolicy() method.

```java
		GuardPolicy policy = new GuardPolicy(
								"unique_guard_vUSP_1", 
								"APPC 5 Restart", 
								"We only allow 5 restarts over 15 minute window during the day time hours (i.e. avoid midnight to 5am)",
								"APPC", 
								"Restart");	
		builder = builder.addGuardPolicy(policy);
```

# Add Limit Constraint to a Guard Policy

The limit constraint defines the details of how to enforce the guard policy. Each limit constraint can contain two types of constraints - frequency limiter and black list. At least one type of constraints should be specified, otherwise the limit constraint will be counted as invalid. To add a limit constraint to an existing guard policy, use the addLimitConstraint() method.

```java
		Map<String, String> time_in_range = new HashMap<String, String>();
		time_in_range.put("arg2", "PT5H");
		time_in_range.put("arg3", "PT24H");
		List<String> blacklist = new LinkedList<String>();
		blacklist.add("vm_name_1");
		blacklist.add("vm_name_2");
		Constraint cons = new Constraint(5, "PT15M", time_in_range, blacklist);
		builder = builder.addLimitConstraint(policy.id, cons);
```


# Build the YAML Specification

When finished defining the Guard Policies, build the specification and analyze the [Results.java](src/main/java/org/onap/policy/controlloop/policy/builder/Results.java)

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


# Use the YAML Specification to Generate the XACML Guard Policies

Now that you have a valid YAML specification, call the method in [PolicyGuardYamlToXacml.java](guard/src/main/java/org/onap/policy/guard/PolicyGuardYamlToXacml.java) to generate the XACML Guard Policies.


# YAML Specification

The YAML specification has 2 sections to it: [guard](#guard-object) and [guards](#guards-array). The [guard section](#guard-object) section is simply a header defining the version of this guard. The [guards section](#guards-array) is simply an array of [GuardPolicy objects](#guardpolicy-object).

## guard Object

| Field Name      | Type          | Required   | Description  |
| -------------   |:-------------:| -----------| ------------:|
| version         | string        | required   | Value for this release if 2.0.0 |


## guards array

The guards section is an array of [GuardPolicy objects](#guardpolicy-object).

### GuardPolicy Object

| Field Name      | Type          | Required   | Description  |
| -------------   |:-------------:| -----------| ------------:|
| id              | string        | required   | Unique ID for the policy. |
| name            | string        | required   | Policy name |
| description     | string        | optional   | Policy description |
| actor           | string        | required   | Name of the actor for this operation: Example: APPC |
| recipe          | string        | required   | Name of recipe to be performed. Example "Restart" |
| limit_constraints  | array of [constraint](#constraint-object) object | required | Constraints used to enforce the guard policy |

The guard policy is bound to a specific recipe performed by the actor. When the Control Loop tries to perform the recipe operation by the actor, this guard policy should be evaluated against all the specified constraints. If any of the constraints will be violated, the operation should be abandoned.

#### constraint Object

| Field Name      | Type          | Required   | Description  |
| -------------   |:-------------:| -----------| ------------:|
| num             | integer       | required if blacklist is not specified  | The limited number of the same operations |
| duration        | string        | required if blacklist is not specified  | Time window for counting the same operations |
| time_in_range   | map<string, string> | optional   | Valid time spans for enforcing the guard policy |
| blacklist       | array of string     | required if num and duration are not specified | A list of the entity names that should not be touched by the Control Loop |

The first three attributes define the frequency limiter which means that only a limited number of the same operations can be allowed within each valid time window. The last attribute defines a blacklist of the target entities on which the Control Loop should not perform the operation.
  
The "duration" parameter should have one of the following values: [5min, 10min, 30min, 1h, 12h, 1d, 5d, 1w, 1mon].

  
## Examples of YAML Control Loop Guards

[vService-Frequency-Limiter-Guard](src/test/resources/v2.0.0-guard/policy_guard_appc_restart.yaml)
[vService-Blacklist-Guard](src/test/resources/v2.0.0-guard/policy_guard_blacklist.yaml)
[ONAP-vDNS-Guard](src/test/resources/v2.0.0-guard/policy_guard_ONAP_demo_vDNS.yaml)


### vService Frequency Limiter Guard
```
guard:
  version: 2.0.0

guards:
  - id: unique_guard_vService_frequency_limiter
    name: APPC 5 Restart
    description: 
      We only allow 5 restarts over 15 minute window during the day time hours (i.e. avoid midnight to 5am)
    actor: APPC
    recipe: Restart
    limit_constraints:
      - num: 5
        duration: PT15M
        time_in_range:
          arg2: PT5H
          arg3: PT24H	
```


### vService Blacklist Guard
```
guard:
  version: 2.0.0

guards:
  - id: unique_guard_vService_blacklist
    name: APPC Restart Blacklist
    description: |
      We deny restart of the blacklisted targets (avoid midnight to 5am)
    actor: APPC
    recipe: Restart
    limit_constraints:
      - blacklist:
          - TargetName1
          - TargetName2
        time_in_range:
          arg2: 00:00:00-05:00
          arg3: 23:59:59-05:00
```


### ONAP vDNS Guard
```
guard:
  version: 2.0.0

guards:
  - id: unique_guard_ONAP_vDNS_1
    name: SO Spinup
    description: We only spin up 1 instance over a 10 minute window
    actor: SO
    recipe: VF Module Create
    limit_constraints:
      - num: 1
        duration: PT10M
```

