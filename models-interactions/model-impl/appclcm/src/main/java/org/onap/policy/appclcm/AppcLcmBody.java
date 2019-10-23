/*
 * ============LICENSE_START=======================================================
 * appclcm
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.appclcm;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AppcLcmBody implements Serializable {

    private static final long serialVersionUID = -466220696716397231L;

    @SerializedName("input")
    private AppcLcmInput input;

    @SerializedName("output")
    private AppcLcmOutput output;

    public AppcLcmBody() {
        // Create a default AppcLcmBody instance
    }

    /**
     * Get the input.
     *
     * @return the input
     */
    public AppcLcmInput getInput() {
        return input;
    }

    /**
     * Set the input.
     *
     * @param input the input to set
     */
    public void setInput(AppcLcmInput input) {
        this.input = input;
    }

    public AppcLcmOutput getOutput() {
        return output;
    }

    /**
     * Set the output.
     *
     * @param output the output to set
     */
    public void setOutput(AppcLcmOutput output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "AppcLcmBody [input=" + input + ", output=" + output + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((input == null) ? 0 : input.hashCode());
        result = prime * result + ((output == null) ? 0 : output.hashCode());
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
        AppcLcmBody other = (AppcLcmBody) obj;
        if (input == null) {
            if (other.input != null) {
                return false;
            }
        } else if (!input.equals(other.input)) {
            return false;
        }
        if (output == null) {
            if (other.output != null) {
                return false;
            }
        } else if (!output.equals(other.output)) {
            return false;
        }
        return true;
    }

}
