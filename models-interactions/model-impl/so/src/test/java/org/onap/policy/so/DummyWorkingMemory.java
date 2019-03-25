/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.so;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.drools.core.WorkingMemory;
import org.drools.core.WorkingMemoryEntryPoint;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.spi.AsyncExceptionHandler;
import org.drools.core.spi.GlobalResolver;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.FactHandle.State;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.time.SessionClock;

public class DummyWorkingMemory implements WorkingMemory {

    @Override
    public void addEventListener(RuleRuntimeEventListener listener) {
    }

    @Override
    public void addEventListener(AgendaEventListener listener) {
    }

    @Override
    public void addEventListener(KieBaseEventListener listener) {
    }

    @Override
    public void removeEventListener(RuleRuntimeEventListener listener) {
    }

    @Override
    public void removeEventListener(AgendaEventListener listener) {
    }

    @Override
    public void removeEventListener(KieBaseEventListener listener) {
    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        return null;
    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        return null;
    }

    @Override
    public Collection<KieBaseEventListener> getKieBaseEventListeners() {
        return null;
    }

    @Override
    public FactHandle insert(Object object, boolean dynamic) {
        return null;
    }

    @Override
    public FactHandle insert(Object object) {
        return null;
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getEntryPointId() {
        return null;
    }

    @Override
    public void retract(FactHandle handle) {
    }

    @Override
    public void delete(FactHandle handle) {
    }

    @Override
    public void delete(FactHandle handle, State fhState) {


    }

    @Override
    public void update(FactHandle handle, Object object) {
    }

    @Override
    public void update(FactHandle handle, Object object, String... modifiedProperties) {
    }

    @Override
    public Collection<? extends Object> getObjects() {
        return null;
    }

    @Override
    public Collection<? extends Object> getObjects(ObjectFilter filter) {
        return null;
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        return null;
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        return null;
    }

    @Override
    public long getFactCount() {
        return 0;
    }

    @Override
    public Agenda getAgenda() {
        return null;
    }

    @Override
    public void setGlobal(String identifier, Object value) {
    }

    @Override
    public Object getGlobal(String identifier) {
        return null;
    }

    @Override
    public Environment getEnvironment() {
        return null;
    }

    @Override
    public void setGlobalResolver(GlobalResolver globalResolver) {
    }

    @Override
    public GlobalResolver getGlobalResolver() {
        return null;
    }

    @Override
    public InternalKnowledgeBase getKnowledgeBase() {
        return null;
    }

    @Override
    public int fireAllRules() {
        return 0;
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter) {
        return 0;
    }

    @Override
    public int fireAllRules(int fireLimit) {
        return 0;
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter, int fireLimit) {
        return 0;
    }

    @Override
    public Object getObject(FactHandle handle) {
        return null;
    }

    @Override
    public FactHandle getFactHandle(Object object) {
        return null;
    }

    @Override
    public FactHandle getFactHandleByIdentity(Object object) {
        return null;
    }

    @Override
    public Iterator<?> iterateObjects() {
        return null;
    }

    @Override
    public Iterator<?> iterateObjects(ObjectFilter filter) {
        return null;
    }

    @Override
    public Iterator<InternalFactHandle> iterateFactHandles() {
        return null;
    }

    @Override
    public Iterator<InternalFactHandle> iterateFactHandles(ObjectFilter filter) {
        return null;
    }

    @Override
    public void setFocus(String focus) {
    }

    @Override
    public QueryResults getQueryResults(String query, Object... arguments) {
        return null;
    }

    @Override
    public void setAsyncExceptionHandler(AsyncExceptionHandler handler) {
    }

    @Override
    public void clearAgenda() {
    }

    @Override
    public void clearAgendaGroup(String group) {
    }

    @Override
    public void clearActivationGroup(String group) {
    }

    @Override
    public void clearRuleFlowGroup(String group) {
    }

    @Override
    public ProcessInstance startProcess(String processId) {
        return null;
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        return null;
    }

    @Override
    public ProcessInstance getProcessInstance(long id) {
        return null;
    }

    @Override
    public ProcessInstance getProcessInstance(long id, boolean readOnly) {
        return null;
    }

    @Override
    public WorkItemManager getWorkItemManager() {
        return null;
    }

    @Override
    public void halt() {
    }

    @Override
    public WorkingMemoryEntryPoint getWorkingMemoryEntryPoint(String id) {
        return null;
    }

    @Override
    public SessionClock getSessionClock() {
        return null;
    }

}
