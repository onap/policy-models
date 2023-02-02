/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021,2023 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.guard;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import lombok.Data;

@Entity
@Table(name = "operationshistory", indexes = {
    @Index(name = "operationshistory_clreqid_index", columnList = "requestId,closedLoopName"),
    @Index(name = "operationshistory_target_index", columnList = "target,operation,actor,endtime")
})
@Data
public class OperationsHistory implements Serializable {

    private static final long serialVersionUID = -551420180714993577L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "opHistoryIdGen")
    @TableGenerator(
        name = "opHistoryIdGen",
        table = "ophistory_id_sequence",
        pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT",
        pkColumnValue = "SEQ_GEN")
    @Column(name = "id")
    private Long id;

    @Column(name = "closedLoopName", length = 255)
    private String closedLoopName;

    @Column(name = "requestId", length = 50)
    private String requestId;

    @Column(name = "subrequestId", length = 50)
    private String subrequestId;

    @Column(name = "actor", length = 50)
    private String actor;

    @Column(name = "operation", length = 50)
    private String operation;

    @Column(name = "target", length = 50)
    private String target;

    @Column(name = "starttime")
    private Date starttime;

    @Column(name = "outcome", length = 50)
    private String outcome;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "endtime")
    private Date endtime;

}
