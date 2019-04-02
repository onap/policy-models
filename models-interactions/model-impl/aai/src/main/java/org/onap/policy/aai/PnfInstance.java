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

public class PnfInstance implements Serializable {

    private static final long serialVersionUID = -3694152433472165034L;

    private String pnfName;
    private String pnfInstanceName;
    private PnfType pnfType;
    private String pnfSerial;

    public PnfInstance() {

    }

    /**
     * Create an instance from a given instance.
     *
     * @param instance the instance
     */
    public PnfInstance(PnfInstance instance) {
        if (instance == null) {
            return;
        }
        this.pnfName = instance.pnfName;
        this.pnfInstanceName = instance.pnfInstanceName;
        this.pnfType = instance.pnfType;
        this.pnfSerial = instance.pnfSerial;
    }

    public String getPnfName() {
        return pnfName;
    }

    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

    public String getPnfInstanceName() {
        return pnfInstanceName;
    }

    public void setPnfInstanceName(String pnfInstanceName) {
        this.pnfInstanceName = pnfInstanceName;
    }

    public PnfType getPnfType() {
        return pnfType;
    }

    public void setPnfType(PnfType pnfType) {
        this.pnfType = pnfType;
    }

    public String getPnfSerial() {
        return pnfSerial;
    }

    public void setPnfSerial(String pnfSerial) {
        this.pnfSerial = pnfSerial;
    }

    @Override
    public String toString() {
        return "PNFInstance [PNFName=" + pnfName + ", PNFInstanceName=" + pnfInstanceName + ", PNFType=" + pnfType
                + ", PNFSerial=" + pnfSerial + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pnfInstanceName == null) ? 0 : pnfInstanceName.hashCode());
        result = prime * result + ((pnfName == null) ? 0 : pnfName.hashCode());
        result = prime * result + ((pnfSerial == null) ? 0 : pnfSerial.hashCode());
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
        PnfInstance other = (PnfInstance) obj;
        if (pnfInstanceName == null) {
            if (other.pnfInstanceName != null) {
                return false;
            }
        } else if (!pnfInstanceName.equals(other.pnfInstanceName)) {
            return false;
        }
        if (pnfName == null) {
            if (other.pnfName != null) {
                return false;
            }
        } else if (!pnfName.equals(other.pnfName)) {
            return false;
        }
        if (pnfSerial == null) {
            if (other.pnfSerial != null) {
                return false;
            }
        } else if (!pnfSerial.equals(other.pnfSerial)) {
            return false;
        }
        return pnfType == other.pnfType;
    }
}
