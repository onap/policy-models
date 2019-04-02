/*-
 * ============LICENSE_START=======================================================
 * aai
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.aai;

import java.io.Serializable;

public class Pnf implements Serializable {
    private static final long serialVersionUID = -3535108358668248501L;

    private String pnfName;
    private PnfType pnfType;

    public Pnf() {

    }

    public Pnf(Pnf pnf) {
        this.pnfName = pnf.pnfName;
        this.pnfType = pnf.pnfType;
    }

    public String getPnfName() {
        return pnfName;
    }

    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

    public PnfType getPnfType() {
        return pnfType;
    }

    public void setPnfType(PnfType pnfType) {
        this.pnfType = pnfType;
    }

    @Override
    public String toString() {
        return "PNF [PNFName=" + pnfName + ", PNFType=" + pnfType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pnfName == null) ? 0 : pnfName.hashCode());
        result = prime * result + ((pnfType == null) ? 0 : pnfType.hashCode());
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
        Pnf other = (Pnf) obj;
        if (pnfName == null) {
            if (other.pnfName != null) {
                return false;
            }
        } else if (!pnfName.equals(other.pnfName)) {
            return false;
        }
        return pnfType == other.pnfType;
    }
}
