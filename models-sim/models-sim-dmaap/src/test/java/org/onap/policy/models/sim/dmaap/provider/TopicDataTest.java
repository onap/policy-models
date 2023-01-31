/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.models.sim.dmaap.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.springframework.test.util.ReflectionTestUtils;

public class TopicDataTest {
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String GROUP1 = "group-A";
    private static final String GROUP2 = "group-B";
    private static final String GROUP3 = "group-C";

    private TopicData data;
    private ConsumerGroupData consgrp1;
    private ConsumerGroupData consgrp2;
    private ConsumerGroupData consgrp3;
    private List<ConsumerGroupData> groups;

    /**
     * Sets up mocks and the initial data object.
     *
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        consgrp1 = mock(ConsumerGroupData.class);
        consgrp2 = mock(ConsumerGroupData.class);
        consgrp3 = mock(ConsumerGroupData.class);

        when(consgrp1.read(anyInt(), anyLong())).thenReturn(Collections.emptyList());
        when(consgrp2.read(anyInt(), anyLong())).thenReturn(Collections.emptyList());
        when(consgrp3.read(anyInt(), anyLong())).thenReturn(Collections.emptyList());

        groups = new LinkedList<>(Arrays.asList(consgrp1, consgrp2, consgrp3));

        data = new TopicData("my-topic") {
            @Override
            protected ConsumerGroupData makeData(String consumerGroup) {
                return groups.remove(0);
            }
        };
    }

    @Test
    public void testRemoveIdleConsumers() throws Exception {
        // force two consumers into the map
        data.read(GROUP1, 0, 0);
        data.read(GROUP2, 0, 0);
        data.read(GROUP3, 0, 0);

        // indicate that one should be removed
        when(consgrp1.shouldRemove()).thenReturn(true);

        // sweep
        data.removeIdleConsumers();

        assertEquals("[group-B, group-C]", new TreeSet<>(getGroups().keySet()).toString());

        // indicate that the others should be removed
        when(consgrp2.shouldRemove()).thenReturn(true);
        when(consgrp3.shouldRemove()).thenReturn(true);

        // sweep
        data.removeIdleConsumers();

        assertTrue(getGroups().isEmpty());
    }

    @Test
    public void testRead() throws Exception {
        List<String> lst = Collections.emptyList();

        when(consgrp1.read(anyInt(), anyLong())).thenReturn(ConsumerGroupData.UNREADABLE_LIST)
                        .thenReturn(ConsumerGroupData.UNREADABLE_LIST).thenReturn(lst);

        assertSame(lst, data.read(GROUP1, 10, 20));

        // should have invoked three times
        verify(consgrp1, times(3)).read(anyInt(), anyLong());

        // should have used the given values
        verify(consgrp1, times(3)).read(10, 20);

        // should not have allocated more than one group
        assertEquals(2, groups.size());
    }

    @Test
    public void testRead_MultipleGroups() throws Exception {
        List<String> lst1 = Collections.emptyList();
        when(consgrp1.read(anyInt(), anyLong())).thenReturn(lst1);

        List<String> lst2 = Collections.emptyList();
        when(consgrp2.read(anyInt(), anyLong())).thenReturn(lst2);

        // one from each group
        assertSame(lst1, data.read(GROUP1, 0, 0));
        assertSame(lst2, data.read(GROUP2, 0, 0));

        // repeat
        assertSame(lst1, data.read(GROUP1, 0, 0));
        assertSame(lst2, data.read(GROUP2, 0, 0));

        // again
        assertSame(lst1, data.read(GROUP1, 0, 0));
        assertSame(lst2, data.read(GROUP2, 0, 0));

        // should still have group3 in the list
        assertEquals(1, groups.size());
    }

    @Test
    public void testWrite() throws Exception {
        // no groups yet
        List<Object> messages = Arrays.asList("hello", "world");
        data.write(messages);

        // add two groups
        data.read(GROUP1, 0, 0);
        data.read(GROUP2, 0, 0);

        data.write(messages);

        // should have been written to both groups
        List<String> strings = messages.stream().map(Object::toString).collect(Collectors.toList());
        verify(consgrp1).write(strings);
        verify(consgrp2).write(strings);
    }

    @Test
    public void testConvertMessagesToStrings() {
        assertEquals("[abc, 200]", data.convertMessagesToStrings(Arrays.asList("abc", null, 200)).toString());
    }

    @Test
    public void testConvertMessageToString() throws CoderException {
        Coder coder = new StandardCoder();

        assertNull(data.convertMessageToString(null, coder));
        assertEquals("text-msg", data.convertMessageToString("text-msg", coder));
        assertEquals("100", data.convertMessageToString(100, coder));

        coder = mock(Coder.class);
        when(coder.encode(any())).thenThrow(new CoderException(EXPECTED_EXCEPTION));
        assertNull(data.convertMessageToString(new TreeMap<String, Object>(), coder));
    }

    @Test
    public void testMakeData() throws Exception {
        // use real objects instead of mocks
        TopicData data2 = new TopicData("real-data-topic");

        // force a group into the topic
        data2.read(GROUP1, 0, 0);

        data2.write(Arrays.asList("abc", "def", "ghi"));

        assertEquals("[abc, def]", data2.read(GROUP1, 2, 0).toString());
    }

    /**
     * Gets the consumer group map from the topic data object.
     *
     * @return the topic's consumer group map
     */
    @SuppressWarnings("unchecked")
    private Map<String, ConsumerGroupData> getGroups() {
        return (Map<String, ConsumerGroupData>) ReflectionTestUtils.getField(data, "group2data");
    }
}
