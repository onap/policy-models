/*-
 * ============LICENSE_START=======================================================
 * policy-yaml
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.controlloop.policy;

import java.io.Serializable;

public class OperationsAccumulateParams implements Serializable {

    private static final long serialVersionUID = -3597358159130168247L;
    
    private String period;
    private Integer limit;
    
    public OperationsAccumulateParams() {
        // Does Nothing 
    } 
    
    public OperationsAccumulateParams(OperationsAccumulateParams ops) {
        this.period = ops.period;
        this.limit = ops.limit;
    }
    
    public OperationsAccumulateParams(String period, Integer limit) {
        this.period = period;
        this.limit = limit;
    }
    
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "OperationsAccumulateParams [period=" + period + ", limit=" + limit + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((period == null) ? 0 : period.hashCode());
        result = prime * result + ((limit == null) ? 0 : limit.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OperationsAccumulateParams other = (OperationsAccumulateParams) obj;
        if (period == null) {
            if (other.period != null) {
                return false;
            }
        } else if (!period.equals(other.period)) {
            return false;
        }
        if (limit == null) {
            if (other.limit != null) {
                return false;
            }
        } else if (!limit.equals(other.limit)) {
            return false;
        }
        return true;
    }

}
